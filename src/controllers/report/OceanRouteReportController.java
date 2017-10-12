package controllers.report;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OceanRouteReportController extends Controller {
    private Log logger = Log.getLog(OceanRouteReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/oceanRouteReport");
		setAttr("listConfigList", configList);
		
    	render("eeda/report/oceanRouteReport/list.html");
    }
   
    public long list() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String customer_id = getPara("customer_id");
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        String date_type = getPara("date_type");
        String type = getPara("type");
        String condition = "";
        String group_condition="";
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        if("year".equals(date_type)){
            if(StringUtils.isNotEmpty(begin_date)){
                begin_date = begin_date+"-01-01 00:00:00";
            }else{
                begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
                end_date = end_date+"-12-31 23:59:59";
            }else{
                end_date = "2037-12-31 23:59:59";
            }
            
            group_condition = " cast(year(jo.order_export_date) as char)";
        }else if(!"day".equals(date_type)){
            if(StringUtils.isNotEmpty(begin_date)){
                begin_date = begin_date+"-01 00:00:00";
            }else{
                begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
                end_date = end_date+"-31 23:59:59";
            }else{
                end_date = "2037-12-31 23:59:59";
            }
            
            if("season".equals(date_type)){
                group_condition = " (case"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 1 and 3 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第一季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 4 and 6 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第二季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 7 and 9 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第三季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 10 and 12 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第四季') as char)"
                        + " end)";
            }else{
                group_condition = " cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char)";
            }
            
        }
        
        if(StringUtils.isNotEmpty(customer_id)){
            condition += " and jo.customer_id = "+customer_id;
        }
        if(StringUtils.isNotBlank(type)){
        	condition+=" and jo.type = '"+type+"'";
        }
        if(StringUtils.isNotBlank(begin_date)||StringUtils.isNotBlank(end_date)){
        	condition += " and jo.order_export_date between '"+begin_date+"' and '"+end_date+"' "; 
        }
        
        String sql = "SELECT  "
                + "        order_export_date, "
                + "        customer_id, "
                + "        customer_name, "
                + "        route, "
                + "        SUM(IFNULL(pieces, 0)) pieces, "
                + "        SUM(IFNULL(gross_weight, 0)) gross_weight, "
                + "        SUM(IFNULL(volume, 0)) volume, "
                + "        SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu, "
                + "        SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm "
                + "    FROM "
                + "        (SELECT "+group_condition+" order_export_date, "
                + "                jo.customer_id, "
                + "                concat(ifnull(pol.name,''), ' -', ifnull(hub.name,''), '- ', ifnull(pod.name,'')) route, "
                + "                jo.pieces, "
                + "                jo.gross_weight, "
                + "                jo.volume, "
                + "                p.abbr customer_name, "
                + "                (select ( "
                + "                    count(case when container_type = '20''GP' then container_type end) + "
                + "                    count(case when container_type = '40''GP' then container_type end)*2 + "
                + "                    count(case when container_type = '45''GP' then container_type end)*2 + "
                + "                    count(case when container_type = '40''HQ' then container_type end)*2) gp20 "
                + "                 from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id  "
                + "                    and jo.type in('出口柜货', '进口柜货') "
                + "                ) ocean_fcl_teu, "
                + "                (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm "
                + "        FROM "
                + "            job_order jo "
                + "        left join job_order_shipment jos on jos.order_id = jo.id "
                + "        left join location pol on jos.pol = pol.id "
                + "        left join location hub on jos.hub = hub.id "
                + "        left join location pod on jos.pod = pod.id "
                + "        LEFT JOIN party p ON p.id = jo.customer_id "
                + "        WHERE jo.type in ('出口柜货', '进口柜货','出口散货', '进口散货') "
                + "           and (jo.office_id="+office_id+ ref_office+ ")"
                + condition
                + " and jo.delete_flag = 'N'"
				+ " ) A where 1=1"
                + " GROUP BY A.order_export_date,A.customer_id, A.route"
                + " ORDER BY A.customer_id , A.order_export_date";
        
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total=rec.getLong("total");
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
        return total;
    }
    
    public void listTotal(){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String customer_id = getPara("customer_id");
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        String date_type = getPara("date_type");
        String type = getPara("type");
        String condition = "";
        String group_condition="";
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        if(StringUtils.isNotEmpty(customer_id)){
            condition += " and jo.customer_id = "+customer_id;
        }
        
        if("year".equals(date_type)){
            if(StringUtils.isNotEmpty(begin_date)){
                begin_date = begin_date+"-01-01 00:00:00";
            }else{
                begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
                end_date = end_date+"-12-31 23:59:59";
            }else{
                end_date = "2037-12-31 23:59:59";
            }
            
            group_condition = " cast(year(jo.order_export_date) as char)";
        }else if(!"day".equals(date_type)){
            if(StringUtils.isNotEmpty(begin_date)){
                begin_date = begin_date+"-01 00:00:00";
            }else{
                begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
                end_date = end_date+"-31 23:59:59";
            }else{
                end_date = "2037-12-31 23:59:59";
            }
            
            if("season".equals(date_type)){
                group_condition = " (case"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 1 and 3 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第一季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 4 and 6 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第二季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 7 and 9 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第三季') as char)"
                        + " when cast(month(jo.order_export_date) as char)"
                        + " between 10 and 12 "
                        + " then cast(CONCAT(year(jo.order_export_date),'-','第四季') as char)"
                        + " end)";
            }else{
                group_condition = " cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char)";
            }
            
        }
        
        condition += " and jo.order_export_date between '"+begin_date+"' and '"+end_date+"' "; 
        if(StringUtils.isNotBlank(type)){
        	condition+=" and jo.type = '"+type+"'";
        }
        
        String sql = "select ifnull(SUM(pieces),0) pieces_total,ifnull(SUM(gross_weight),0) gross_weight_total,ifnull(SUM(volume),0) volume_total,"
        		+ "  ifnull(SUM(ocean_fcl_teu),0) ocean_fcl_teu_total,ifnull(SUM(ocean_lcl_cbm),0) ocean_lcl_cbm_total"
        		+ " from (SELECT  "
                + "        order_export_date, "
                + "        customer_id, "
                + "        customer_name, "
                + "        route, "
                + "        SUM(IFNULL(pieces, 0)) pieces, "
                + "        SUM(IFNULL(gross_weight, 0)) gross_weight, "
                + "        SUM(IFNULL(volume, 0)) volume, "
                + "        SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu, "
                + "        SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm "
                + "    FROM "
                + "        (SELECT  "
                + "            CAST(CONCAT(YEAR(jo.order_export_date), '-', MONTH(jo.order_export_date)) "
                + "                    AS CHAR) order_export_date, "
                + "                jo.customer_id, "
                + "                concat(ifnull(pol.name,''), ' -', ifnull(hub.name,''), '- ', ifnull(pod.name,'')) route, "
                + "                jo.pieces, "
                + "                jo.gross_weight, "
                + "                jo.volume, "
                + "                p.abbr customer_name, "
                + "                (select ( "
                + "                    count(case when container_type = '20''GP' then container_type end) + "
                + "                    count(case when container_type = '40''GP' then container_type end)*2 + "
                + "                    count(case when container_type = '45''GP' then container_type end)*2 + "
                + "                    count(case when container_type = '40''HQ' then container_type end)*2) gp20 "
                + "                 from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id  "
                + "                    and jo.type in('出口柜货', '进口柜货') "
                + "                ) ocean_fcl_teu, "
                + "                (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm "
                + "        FROM "
                + "            job_order jo "
                + "        left join job_order_shipment jos on jos.order_id = jo.id "
                + "        left join location pol on jos.pol = pol.id "
                + "        left join location hub on jos.hub = hub.id "
                + "        left join location pod on jos.pod = pod.id "
                + "        LEFT JOIN party p ON p.id = jo.customer_id "
                + "        WHERE jo.type in ('出口柜货', '进口柜货','出口散货', '进口散货') "
                + "           and (jo.office_id="+office_id+ ref_office+ ")"
                + condition
                + " and jo.delete_flag = 'N'"
				+ " ) A where 1=1"
                + " GROUP BY A.order_export_date,A.customer_id, A.route"
                + " ORDER BY A.customer_id , A.order_export_date"
                + " )B";
        Record rec = Db.findFirst(sql);
        long total=list();
        rec.set("total", total);
        renderJson(rec);
    }  
    
    public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		long office_id = user.getLong("office_id");
		 String customer_id = getPara("customer_id");
	        String begin_date = getPara("begin_date");
	        String end_date = getPara("end_date");
	        String date_type = getPara("date_type");
	        String type = getPara("type");
	        String condition = "";
	        String group_condition="";
	        
	        String ref_office = "";
	        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
	        if(relist!=null){
	        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
	        }
	        
	        if(StringUtils.isNotEmpty(customer_id)){
	        	condition += " and jo.customer_id = "+customer_id;
	        }
	        
	        if("year".equals(date_type)){
	        	if(StringUtils.isNotEmpty(begin_date)){
	            	begin_date = begin_date+"-01-01 00:00:00";
	            }else{
	            	begin_date = "1970-01-01 00:00:00";
	            }
	            
	            if(StringUtils.isNotEmpty(end_date)){
	            	end_date = end_date+"-12-31 23:59:59";
	            }else{
	            	end_date = "2037-12-31 23:59:59";
	            }
	            
	            group_condition = " cast(year(jo.order_export_date) as char)";
	        }else if(!"day".equals(date_type)){
	        	if(StringUtils.isNotEmpty(begin_date)){
	            	begin_date = begin_date+"-01 00:00:00";
	            }else{
	            	begin_date = "1970-01-01 00:00:00";
	            }
	            
	            if(StringUtils.isNotEmpty(end_date)){
	            	end_date = end_date+"-31 23:59:59";
	            }else{
	            	end_date = "2037-12-31 23:59:59";
	            }
	            
	            if("season".equals(date_type)){
	            	group_condition = " (case"
	            			+ " when cast(month(jo.order_export_date) as char)"
	            			+ " between 1 and 3 "
	            			+ " then cast(CONCAT(year(jo.order_export_date),'-','第一季') as char)"
	            			+ " when cast(month(jo.order_export_date) as char)"
	            			+ " between 4 and 6 "
	            			+ " then cast(CONCAT(year(jo.order_export_date),'-','第二季') as char)"
	            			+ " when cast(month(jo.order_export_date) as char)"
	            			+ " between 7 and 9 "
	            			+ " then cast(CONCAT(year(jo.order_export_date),'-','第三季') as char)"
	            			+ " when cast(month(jo.order_export_date) as char)"
	            			+ " between 10 and 12 "
	            			+ " then cast(CONCAT(year(jo.order_export_date),'-','第四季') as char)"
	            			+ " end)";
	            }else{
	            	group_condition = " cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char)";
	            }
	            
	        }
	        
	        condition += " and jo.order_export_date between '"+begin_date+"' and '"+end_date+"' "; 
	        if(StringUtils.isNotBlank(type)){
	        	condition+=" and jo.type = '"+type+"'";
	        }
	        
	        String sql = "SELECT  "
	                + "        order_export_date, "
	                + "        customer_id, "
	                + "        customer_name, "
	                + "        route, "
	                + "        SUM(IFNULL(pieces, 0)) pieces, "
	                + "        SUM(IFNULL(gross_weight, 0)) gross_weight, "
	                + "        SUM(IFNULL(volume, 0)) volume, "
	                + "        SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu, "
	                + "        SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm "
	                + "    FROM "
	                + "        (SELECT  "
	                + "            CAST(CONCAT(YEAR(jo.order_export_date), '-', MONTH(jo.order_export_date)) "
	                + "                    AS CHAR) order_export_date, "
	                + "                jo.customer_id, "
	                + "                concat(ifnull(pol.name,''), ' -', ifnull(hub.name,''), '- ', ifnull(pod.name,'')) route, "
	                + "                jo.pieces, "
	                + "                jo.gross_weight, "
	                + "                jo.volume, "
	                + "                p.abbr customer_name, "
	                + "                (select ( "
	                + "                    count(case when container_type = '20''GP' then container_type end) + "
	                + "                    count(case when container_type = '40''GP' then container_type end)*2 + "
	                + "                    count(case when container_type = '45''GP' then container_type end)*2 + "
	                + "                    count(case when container_type = '40''HQ' then container_type end)*2) gp20 "
	                + "                 from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id  "
	                + "                    and jo.type in('出口柜货', '进口柜货') "
	                + "                ) ocean_fcl_teu, "
	                + "                (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm "
	                + "        FROM "
	                + "            job_order jo "
	                + "        left join job_order_shipment jos on jos.order_id = jo.id "
	                + "        left join location pol on jos.pol = pol.id "
	                + "        left join location hub on jos.hub = hub.id "
	                + "        left join location pod on jos.pod = pod.id "
	                + "        LEFT JOIN party p ON p.id = jo.customer_id "
	                + "        WHERE jo.type in ('出口柜货', '进口柜货','出口散货', '进口散货') "
	                + "           and (jo.office_id="+office_id+ ref_office+ ")"
	                + condition
	                + " and jo.delete_flag = 'N'"
					+ " ) A where 1=1"
	                + " GROUP BY A.order_export_date,A.customer_id, A.route"
	                + " ORDER BY A.customer_id , A.order_export_date";
        String sqlExport = sql;
		String total_name_header = "单据日期,客户名称,路线,总件数,总重量(毛重),总体积(立方),海运货量TEU,海运散货(立方)";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ORDER_EXPORT_DATE", "CUSTOMER_NAME", "ROUTE", "PIECES",
				"GROSS_WEIGHT", "VOLUME", "OCEAN_FCL_TEU","OCEAN_LCL_CBM"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
    
}
