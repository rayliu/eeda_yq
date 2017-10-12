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
public class AirRouteReportController extends Controller {
    private Log logger = Log.getLog(AirRouteReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/airRouteReport");
		setAttr("listConfigList", configList);
		render("eeda/report/airRouteReport/list.html");
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
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String condition = "";
        String group_condition="";
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
        
        if(StringUtils.isNotBlank(begin_date)||StringUtils.isNotBlank(end_date)){
        	condition += " and jo.order_export_date between '"+begin_date+"' and '"+end_date+"' "; 
        }
        if(StringUtils.isNotBlank(type)){
        	condition+=" and jo.type = '"+type+"'";
        }
        
        String sql = " SELECT "
                + "     order_export_date,"
                + "     customer_id,"
                + "     customer_name,"
                + "     cast(route as char) route,"
                + "     SUM(IFNULL(pieces, 0)) pieces,"
                + "     SUM(IFNULL(gross_weight, 0)) gross_weight,"
                + "     SUM(IFNULL(volume, 0)) volume,"
                + "     SUM(IFNULL(ari_kg, 0)) ari_kg"
                + " FROM"
                + "     (SELECT "+group_condition+" order_export_date,"
                + "             jo.customer_id,"
                + "             p.abbr customer_name,"
                + "             (select group_concat("
                + "                 concat("
                + "                     ifnull(lo.name,joai.start_from_input), ' - ', ifnull(lo1.name,joai.destination_input)"
                + "                 ) separator ' - '"
                + "             ) from job_order_air_item joai "
                + "				left join location lo on lo.id = joai.start_from "
                + "				left join location lo1 on lo1.id = joai.destination"
                + "				 where joai.order_id = jo.id) route,"
                + "             jo.pieces,"
                + "             jo.gross_weight,"
                + "             jo.volume,"
                + "             (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg"
                + "     FROM job_order jo"
                + "     LEFT JOIN party p ON p.id = jo.customer_id"
                + "     WHERE jo.type in ('出口空运', '进口空运')"
                + "     and (jo.office_id="+office_id+ ref_office+ ")"
                + condition
                + " and jo.delete_flag = 'N'"
				+ " ) A where 1=1"
                + " GROUP BY A.order_export_date,A.customer_id, A.route"
                + " ORDER BY A.customer_id , A.order_export_date";
        
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total= rec.getLong("total");
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
        
        String sql = "select ifnull(SUM(pieces),0) pieces_total,ifnull(SUM(gross_weight),0) gross_weight_total,ifnull(SUM(volume),0) volume_total, "
        		+ " ifnull(SUM(ari_kg),0) ari_kg_total from ( SELECT "
                + "     order_export_date,"
                + "     customer_id,"
                + "     customer_name,"
                + "     route,"
                + "     SUM(IFNULL(pieces, 0)) pieces,"
                + "     SUM(IFNULL(gross_weight, 0)) gross_weight,"
                + "     SUM(IFNULL(volume, 0)) volume,"
                + "     SUM(IFNULL(ari_kg, 0)) ari_kg"
                + " FROM"
                + "     (SELECT "
                + "         CAST(CONCAT(YEAR(jo.order_export_date), '-', MONTH(jo.order_export_date))"
                + "                 AS CHAR) order_export_date,"
                + "             jo.customer_id,"
                + "             p.abbr customer_name,"
                + "             (select group_concat("
                + "                 concat("
                + "                     ifnull(joai.start_from,''), ' - ', ifnull(joai.destination,'')"
                + "                 ) separator ' - '"
                + "             ) from job_order_air_item joai where joai.order_id = jo.id) route,"
                + "             jo.pieces,"
                + "             jo.gross_weight,"
                + "             jo.volume,"
                + "             (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg"
                + "     FROM job_order jo"
                + "     LEFT JOIN party p ON p.id = jo.customer_id"
                + "     WHERE jo.type in ('出口空运', '进口空运')"
                + "           and (jo.office_id="+office_id+ ref_office+ ")"
                + condition
                + " and jo.delete_flag = 'N'"
				+ " ) A where 1=1"
                + " GROUP BY A.order_export_date,A.customer_id, A.route"
                + " ORDER BY A.customer_id , A.order_export_date"
                + ") B";
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
  	        
  	        String ref_office = "";
            Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
            if(relist!=null){
          	  ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
            }
  	        
  	        
  	        String group_condition="";
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
  	        
  	      String sql = " SELECT "
                  + "     order_export_date,"
                  + "     customer_id,"
                  + "     customer_name,"
                  + "     cast(route as char) route,"
                  + "     SUM(IFNULL(pieces, 0)) pieces,"
                  + "     SUM(IFNULL(gross_weight, 0)) gross_weight,"
                  + "     SUM(IFNULL(volume, 0)) volume,"
                  + "     SUM(IFNULL(ari_kg, 0)) ari_kg"
                  + " FROM"
                  + "     (SELECT "
                  + "         CAST(CONCAT(YEAR(jo.order_export_date), '-', MONTH(jo.order_export_date))"
                  + "                 AS CHAR) order_export_date,"
                  + "             jo.customer_id,"
                  + "             p.abbr customer_name,"
                  + "             (select group_concat("
                  + "                 concat("
                  + "                     ifnull(joai.start_from,''), ' - ', ifnull(joai.destination,'')"
                  + "                 ) separator ' - '"
                  + "             ) from job_order_air_item joai where joai.order_id = jo.id) route,"
                  + "             jo.pieces,"
                  + "             jo.gross_weight,"
                  + "             jo.volume,"
                  + "             (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg"
                  + "     FROM job_order jo"
                  + "     LEFT JOIN party p ON p.id = jo.customer_id"
                  + "     WHERE jo.type in ('出口空运', '进口空运')"
                  + "           and (jo.office_id="+office_id+ ref_office+ ")"
                  + condition
                  + " and jo.delete_flag = 'N'"
  				+ " ) A where 1=1"
                  + " GROUP BY A.order_export_date,A.customer_id, A.route"
                  + " ORDER BY A.customer_id , A.order_export_date";
          String sqlExport = sql;
  		String total_name_header = "单据日期,客户名称,路线,总件数,总体积(立方),空运货量(公斤)";
  		String[] headers = total_name_header.split(",");

  		String[] fields = { "ORDER_EXPORT_DATE", "CUSTOMER_NAME", "ROUTE", "PIECES",
  				"VOLUME", "ARI_KG"};
  		
  		String exportName = "";
  		
  		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
  		renderText(fileName);
  	}
}
