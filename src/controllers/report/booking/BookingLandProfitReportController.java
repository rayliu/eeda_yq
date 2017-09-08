package controllers.report.booking;

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
public class BookingLandProfitReportController extends Controller {
    private Log logger = Log.getLog(BookingLandProfitReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    long total=0;
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/profitReport");
        setAttr("listConfigList", configList);
    	render("eeda/statusReport/profitReport.html");
    }

    public  long list() {
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
        
    	String sql = "select B.*, "
    	        + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '40HQ') hq40_count, "
    	        + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '40GP') gp40_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '20GP') gp20_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '1.5T') t1p5_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '3T') t3_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '5T') t5_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '8T') t8_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '10T') t10_count "  //get_truck_type(B.truck_order_ids)''
    	        + " from (select order_export_date,customer_id,"
    			+ " sum(ifnull(pieces,0)) pieces,"
    			+ " sum(ifnull(gross_weight,0)) gross_weight, "
    			+ " sum(ifnull(volume,0)) volume, "
    			+ " SUM(IFNULL(ocean_fcl_bill, 0)) ocean_fcl_bill, "
    			+ " SUM(IFNULL(ocean_lcl_bill, 0)) ocean_lcl_bill, "
    			+ " SUM(IFNULL(ari_kg_bill, 0)) ari_kg_bill, "
    			+ " customer_name,"
    			+ " SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu,"
    			+ " SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm,"
    			+ " SUM(IFNULL(ari_kg, 0)) ari_kg,"
    			+ " group_concat(CAST(id as CHAR) separator ', ' ) truck_order_ids"
    			+ " from ("
    			+ " select "+group_condition+" order_export_date, jo.customer_id, jo.pieces,"
    			+ " jo.gross_weight, jo.volume,p.abbr customer_name,"
    			+ " (select ("
    			+ "    count(case when container_type = '20''GP' then container_type end) +"
    			+ "    count(case when container_type = '40''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '45''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '40''HQ' then container_type end)*2) gp20"
    			+ " from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id "
    			+ "    and jo.type in('出口柜货', '进口柜货')"
    			+ " ) ocean_fcl_teu,"
    			+ " (SELECT   COUNT(container_type) FROM job_order_shipment_item josi WHERE "
    			+ "	josi.load_type = 'FCL' AND josi.order_id = jo.id and container_type is not null "
    			+ "	AND jo.type IN ('出口柜货','进口柜货') ) ocean_fcl_bill, "
    			+ " (SELECT count(volume) FROM job_order jo1 WHERE jo1.id = jo.id and volume is NOT null "
    			+ "	 AND jo1.type IN ('出口散货','进口散货')) ocean_lcl_bill, "
    			+ " (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm,"
    			+ " (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg,"
    			+ " (SELECT	count(gross_weight) FROM job_order jo1 WHERE jo1.id = jo.id and jo1.gross_weight is not null "
    			+ "	 AND jo1.type IN ('出口空运','进口空运')) ari_kg_bill, "
    			+ " jo.id "
    			+ " from job_order jo"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " WHERE jo.office_id="+office_id
    			+ condition
    			 + " and jo.delete_flag = 'N'"
 				+ " ) A where 1=1"
    			+ " GROUP BY A.order_export_date,A.customer_id"
    			+ " )B ORDER BY B.customer_id , B.order_export_date"
    			+ "  ";
    	
    	//String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql +sLimit);
        long total = rec.getLong("total");
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
        if(StringUtils.isNotEmpty(customer_id)){
        	condition += " and jo.customer_id = "+customer_id;
        }
        if(StringUtils.isNotBlank(type)){
        	condition+=" and jo.type = '"+type+"'";
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
        }else if(!"day".equals(date_type)) {
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
        
  /*      String sql = "  SELECT "
        		+" 	SUM(gross_weight) gross_weight_total,SUM(pieces) pieces_total,SUM(volume) volume_total,SUM(ocean_fcl_bill) ocean_fcl_bill_total, "
        		+" 	SUM(ocean_lcl_bill) ocean_lcl_bill_total,SUM(ari_kg_bill) ari_kg_bill_total,SUM(ocean_fcl_teu) ocean_fcl_teu_total,SUM(ocean_lcl_cbm)  "
        		+" ocean_lcl_cbm_total,SUM(ari_kg) ari_kg_total "
    	        + " from (select order_export_date,customer_id,"
    			+ " sum(ifnull(pieces,0)) pieces,"
    			+ " sum(ifnull(gross_weight,0)) gross_weight, "
    			+ " sum(ifnull(volume,0)) volume, "
    			+ " SUM(IFNULL(ocean_fcl_bill, 0)) ocean_fcl_bill, "
    			+ " SUM(IFNULL(ocean_lcl_bill, 0)) ocean_lcl_bill, "
    			+ " SUM(IFNULL(ari_kg_bill, 0)) ari_kg_bill, "
    			+ " customer_name,"
    			+ " SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu,"
    			+ " SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm,"
    			+ " SUM(IFNULL(ari_kg, 0)) ari_kg,"
    			+ " group_concat(CAST(id as CHAR) separator ', ' ) truck_order_ids"
    			+ " from ("
    			+ " select "+group_condition+" order_export_date, jo.customer_id, jo.pieces,"
    			+ " jo.gross_weight, jo.volume,p.abbr customer_name,"
    			+ " (select ("
    			+ "    count(case when container_type = '20''GP' then container_type end) +"
    			+ "    count(case when container_type = '40''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '45''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '40''HQ' then container_type end)*2) gp20"
    			+ " from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id "
    			+ "    and jo.type in('出口柜货', '进口柜货')"
    			+ " ) ocean_fcl_teu,"
    			+ " (SELECT   COUNT(container_type) FROM job_order_shipment_item josi WHERE "
    			+ "	josi.load_type = 'FCL' AND josi.order_id = jo.id and container_type is not null "
    			+ "	AND jo.type IN ('出口柜货','进口柜货') ) ocean_fcl_bill, "
    			+ " (SELECT count(volume) FROM job_order jo1 WHERE jo1.id = jo.id and volume is NOT null "
    			+ "	 AND jo1.type IN ('出口散货','进口散货')) ocean_lcl_bill, "
    			+ " (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm,"
    			+ " (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg,"
    			+ " (SELECT	count(gross_weight) FROM job_order jo1 WHERE jo1.id = jo.id and jo1.gross_weight is not null "
    			+ "	 AND jo1.type IN ('出口空运','进口空运')) ari_kg_bill, "
    			+ " jo.id "
    			+ " from job_order jo"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " WHERE jo.office_id="+office_id
    			+ condition
    			 + " and jo.delete_flag = 'N'"
 				+ " ) A where 1=1"
    			+ " GROUP BY A.order_export_date,A.customer_id"
    			+ " )B ORDER BY B.customer_id , B.order_export_date"
    			+ "  ";*/
        
    	String sql = " select ifnull(sum(gross_weight),0) gross_weight_total,ifnull(sum(volume),0) volume_total,ifnull(sum(ocean_fcl_teu),0) ocean_fcl_teu_total,ifnull(sum(ocean_fcl_bill),0) ocean_fcl_bill_total,ifnull(sum(ocean_lcl_cbm),0) ocean_lcl_cbm_total,ifnull(sum(ocean_lcl_bill),0) ocean_lcl_bill_total,ifnull(sum(ari_kg),0) ari_kg_total,ifnull(sum(ari_kg_bill),0) ari_kg_bill_total,ifnull(sum(pieces),0) pieces_total"
    			+ " from ("
    			+ " select B.*, "
    	        + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '40HQ') hq40_count, "
    	        + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '40GP') gp40_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '20GP') gp20_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '1.5T') t1p5_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '3T') t3_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '5T') t5_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '8T') t8_count,"
                + " (select count(1) from job_order_land_item  where order_id in (B.truck_order_ids) and truck_type= '10T') t10_count "  //get_truck_type(B.truck_order_ids)''
    	        + " from (select order_export_date,customer_id,"
    			+ " sum(ifnull(pieces,0)) pieces,"
    			+ " sum(ifnull(gross_weight,0)) gross_weight, "
    			+ " sum(ifnull(volume,0)) volume, "
    			+ " SUM(IFNULL(ocean_fcl_bill, 0)) ocean_fcl_bill, "
    			+ " SUM(IFNULL(ocean_lcl_bill, 0)) ocean_lcl_bill, "
    			+ " SUM(IFNULL(ari_kg_bill, 0)) ari_kg_bill, "
    			+ " customer_name,"
    			+ " SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu,"
    			+ " SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm,"
    			+ " SUM(IFNULL(ari_kg, 0)) ari_kg,"
    			+ " group_concat(CAST(id as CHAR) separator ', ' ) truck_order_ids"
    			+ " from ("
    			+ " select "+group_condition+" order_export_date, jo.customer_id, jo.pieces,"
    			+ " jo.gross_weight, jo.volume,p.abbr customer_name,"
    			+ " (select ("
    			+ "    count(case when container_type = '20''GP' then container_type end) +"
    			+ "    count(case when container_type = '40''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '45''GP' then container_type end)*2 +"
    			+ "    count(case when container_type = '40''HQ' then container_type end)*2) gp20"
    			+ " from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id "
    			+ "    and jo.type in('出口柜货', '进口柜货')"
    			+ " ) ocean_fcl_teu,"
    			+ " (SELECT   COUNT(container_type) FROM job_order_shipment_item josi WHERE "
    			+ "	josi.load_type = 'FCL' AND josi.order_id = jo.id and container_type is not null "
    			+ "	AND jo.type IN ('出口柜货','进口柜货') ) ocean_fcl_bill, "
    			+ " (SELECT count(volume) FROM job_order jo1 WHERE jo1.id = jo.id and volume is NOT null "
    			+ "	 AND jo1.type IN ('出口散货','进口散货')) ocean_lcl_bill, "
    			+ " (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm,"
    			+ " (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg,"
    			+ " (SELECT	count(gross_weight) FROM job_order jo1 WHERE jo1.id = jo.id and jo1.gross_weight is not null "
    			+ "	 AND jo1.type IN ('出口空运','进口空运')) ari_kg_bill, "
    			+ " jo.id "
    			+ " from job_order jo"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " WHERE jo.office_id="+office_id
    			+ condition
    			 + " and jo.delete_flag = 'N'"
 				+ " ) A where 1=1"
    			+ " GROUP BY A.order_export_date,A.customer_id"
    			+ " )B "
    			+ "  )C";
        
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
	        
	    	String sql = "select B.* "
//	    			+ " CAST(concat("
//	    			+ " IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '40HQ')>0,concat('40HQ(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '40HQ'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '40GP')>0,concat('40GP(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '40GP'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '20GP')>0,concat('20GP(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '20GP'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '1.5T')>0,concat('1.5T(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '1.5T'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '3T')>0,concat('3T(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '3T'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '5T')>0,concat('5T(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '5T'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '8T')>0,concat('8T(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '8T'),')',' '),''),"
//	    			+ "	IF((SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '10T')>0,concat('10T(',(SELECT count(1) FROM job_order_land_item WHERE order_id IN (B.truck_order_ids) AND truck_type = '10T'),')'),'')"
//	    			+ " ) AS char) truck_type " //get_truck_type(B.truck_order_ids)''
	    	        + " from (select order_export_date,customer_id,"
	    			+ " sum(ifnull(pieces,0)) pieces,"
	    			+ " sum(ifnull(gross_weight,0)) gross_weight, "
	    			+ " sum(ifnull(volume,0)) volume, "
	    			+ " SUM(IFNULL(ocean_fcl_bill, 0)) ocean_fcl_bill, "
	    			+ " SUM(IFNULL(ocean_lcl_bill, 0)) ocean_lcl_bill, "
	    			+ " SUM(IFNULL(ari_kg_bill, 0)) ari_kg_bill, "
	    			+ " customer_name,"
	    			+ " SUM(IFNULL(ocean_fcl_teu, 0)) ocean_fcl_teu,"
	    			+ " SUM(IFNULL(ocean_lcl_cbm, 0)) ocean_lcl_cbm,"
	    			+ " SUM(IFNULL(ari_kg, 0)) ari_kg,"
	    			+ " group_concat(CAST(id as CHAR) separator ', ' ) truck_order_ids"
	    			+ " from ("
	    			+ " select "+group_condition+" order_export_date, jo.customer_id, jo.pieces,"
	    			+ " jo.gross_weight, jo.volume,p.abbr customer_name,"
	    			+ " (select ("
	    			+ "    count(case when container_type = '20''GP' then container_type end) +"
	    			+ "    count(case when container_type = '40''GP' then container_type end)*2 +"
	    			+ "    count(case when container_type = '45''GP' then container_type end)*2 +"
	    			+ "    count(case when container_type = '40''HQ' then container_type end)*2) gp20"
	    			+ " from job_order_shipment_item josi where josi.load_type='FCL'and josi.order_id =jo.id "
	    			+ "    and jo.type in('出口柜货', '进口柜货')"
	    			+ " ) ocean_fcl_teu,"
	    			+ " (SELECT   COUNT(container_type) FROM job_order_shipment_item josi WHERE "
	    			+ "	josi.load_type = 'FCL' AND josi.order_id = jo.id and container_type is not null "
	    			+ "	AND jo.type IN ('出口柜货','进口柜货') ) ocean_fcl_bill, "
	    			+ " (SELECT count(volume) FROM job_order jo1 WHERE jo1.id = jo.id and volume is NOT null "
	    			+ "	 AND jo1.type IN ('出口散货','进口散货')) ocean_lcl_bill, "
	    			+ " (select sum(volume) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口散货', '进口散货')) ocean_lcl_cbm,"
	    			+ " (select sum(gross_weight) from job_order jo1 where jo1.id=jo.id and jo1.type in('出口空运', '进口空运')) ari_kg,"
	    			+ " (SELECT	count(gross_weight) FROM job_order jo1 WHERE jo1.id = jo.id and jo1.gross_weight is not null "
	    			+ "	 AND jo1.type IN ('出口空运','进口空运')) ari_kg_bill, "
	    			+ " jo.id "
	    			+ " from job_order jo"
	    			+ " left join party p on p.id = jo.customer_id"
	    			+ " WHERE jo.office_id="+office_id
	    			+ condition
	    			 + " and jo.delete_flag = 'N'"
	 				+ " ) A where 1=1"
	    			+ " GROUP BY A.order_export_date,A.customer_id"
	    			+ " )B ORDER BY B.customer_id , B.order_export_date"
	    			+ "  ";
        String sqlExport = sql;
		String total_name_header = "单据日期,客户名称,总件数,总重量(毛重),总体积(立方),海运货量TEU,票数（海运货量）,海运散货(立方),票数（海运散货）,空运货量(公斤),票数（空运货量）',陆运车型(车次)";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ORDER_EXPORT_DATE", "CUSTOMER_NAME", "PIECES", "GROSS_WEIGHT",
				"VOLUME", "OCEAN_FCL_TEU", "OCEAN_FCL_BILL","OCEAN_LCL_CBM","OCEAN_LCL_BILL","ARI_KG","ARI_KG_BILL","TRUCK_TYPE"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
}
