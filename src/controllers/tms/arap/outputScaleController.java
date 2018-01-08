package controllers.tms.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.tms.TransJobOrderArap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class outputScaleController extends Controller {

	private static Logger logger = Logger.getLogger(outputScaleController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/tms/arap/transCostConfirm/outputScaleList.html");
	}
     
	public String list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        String car_id = "";
        String car_no ="";
        if(StringUtils.isNotEmpty(getPara("car_id_other"))){
        	car_id= " AND tjol.car_no ="+getPara("car_id_other");
        }
        if(StringUtils.isNotEmpty(getPara("car_no_other"))){
        	car_no= " AND car.car_no ='"+getPara("car_no_other")+"'";
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return null;
        }
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * from( "
        		+" SELECT tjo.id tjoid,tjol.id ,tjo.office_id,tjo.delete_flag,tjo.order_no,tjo.lading_no,p.company_name customer_name,tjo.customer_id customer_id,"
        		+" IFNULL(CONVERT (substring(tjol.cabinet_date, 1, 10),CHAR),CONVERT (substring(tjol.closing_date, 1, 10),CHAR)) c_date" 
        		+" ,tjo.type,dock.dock_name take_wharf_name, "
        		+" dock1.dock_name back_wharf_name,dock2.dock_name loading_wharf1_name,dock3.dock_name " 
        		+" loading_wharf2_name,tjo.container_no,tjo.cabinet_type,tjol.unload_type,car.car_no,tjol.car_no car_id,tjo.remark, "
        		+" (CONVERT (substring(tjol.closing_date, 1, 10),CHAR)) create_stamp,tjol.driver,car.car_owned,CONCAT(IFNULL(CONCAT(dock.dock_name,'-'),''),IFNULL(CONCAT(dock2.dock_name,'-'),''),IFNULL(CONCAT(dock3.dock_name,'-'),''),IFNULL(" 
        		+ "dock1.dock_name,'')) combine_wharf, "
        		+" (SELECT GROUP_CONCAT(car.car_no SEPARATOR '<br>/') from trans_job_order tjo1  "
        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
        		+" WHERE tjo.id = tjo1.id) combine_car_no, "
        		+" (SELECT GROUP_CONCAT(tjol.unload_type SEPARATOR '/') from trans_job_order tjo1 "
        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
        		+" WHERE tjo.id = tjo1.id) combine_unload_type ,"
        		+ " (SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'CHARGE' AND tjoa.charge_id = ( SELECT id FROM "
        		+ " fin_item f WHERE f. NAME = '运费' AND f.office_id = "+office_id+") ) freight,(SELECT SUM(tjoa.street_vehicle_freight) "
        		+ "FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.charge_id) street_vehicle_freight,"
        		+ " (SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'COST' AND tjoa.car_id = car.id  AND tjoa.charge_id = ( SELECT id FROM "
        		+ " fin_item f WHERE f. NAME = '运费' AND f.office_id = "+office_id+") ) outputScale,(SELECT SUM(tjoa.currency_total_amount) "
        		+ "FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'CHARGE' AND tjoa.charge_id = (SELECT id FROM fin_item f WHERE f. NAME = '压夜费' AND f.office_id = 4)) night_fee,"
        		+ "tjol.export_flag,"
        		+ " cast(substring(tjo.charge_time, 1, 10) AS CHAR) charge_time"
        		+" from trans_job_order_land_item tjol  "
        		+" LEFT JOIN trans_job_order tjo on tjol.order_id = tjo.id "
        		+" LEFT JOIN dockinfo dock on dock.id = tjo.take_wharf "
        		+" LEFT JOIN dockinfo dock1 on dock1.id = tjo.back_wharf "
        		+" LEFT JOIN dockinfo dock2 on dock2.id = tjol.loading_wharf1 "
        		+" LEFT JOIN dockinfo dock3 on dock3.id = tjol.loading_wharf2 "
        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
        		+" LEFT JOIN party p ON p.id = tjo.customer_id "
        		+ " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id +condition+car_id+car_no
					+ " ) A where 1=1 GROUP BY car_id,tjoid ";
		
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ " order by charge_time " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
        return sql;
		
	}
		
	@SuppressWarnings("unchecked")
	public void downloadList(){
		String jsonStr = getPara("params");
		String sign = getPara("sign");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        
        
        Record reFee = Db.findFirst("SELECT	f.* FROM 	fin_item f 	WHERE	f.office_id = ? and f.name = '运费'",office_id);
        Record reUnit = Db.findFirst("select * from unit WHERE type = 'charge' and name = 'B/L' ");
		Record reCurrency = Db.findFirst("select * from currency WHERE  name = 'CNY' ");
        
        String charge_id = reFee.getLong("id").toString();//费用名称id        
        String unit_id = reUnit.getLong("id").toString();//费用名称id
		String currency_id = reCurrency.getLong("id").toString();//币制名称id
		
        String amount = "1";//获取数量
        String exchange_rate = "1";
        
        List<Map<String, String>> outputScale_detail = (ArrayList<Map<String, String>>)dto.get("param");
		if(outputScale_detail!=null){
	    	for (Map<String, String> rowMap : outputScale_detail) {//获取每一行			    		
	    		String rowId = rowMap.get("id");
	    		String car_id = rowMap.get("car_id");//获取结算车牌id
	    		Record reland = Db.findById("trans_job_order_land_item", rowId);
	    		String job_order_id = reland.getLong("order_id").toString();
	    		TransJobOrderArap transJobArap = TransJobOrderArap.dao.findFirst("select * from trans_job_order_arap  where order_id = ? and car_id = ? and charge_id = ?",job_order_id,car_id,charge_id);
	    		
	    		String price = "0";//获取产值
	    		if(StringUtils.isNotBlank(rowMap.get("outputScale"))){
	    			 price = rowMap.get("outputScale").replaceAll(",","");//获取产值
	    		}
	    		Double total_amount = Double.parseDouble(price) * Double.parseDouble(amount);	    		
	    		Double currency_total_amount = total_amount * Double.parseDouble(exchange_rate);	    		
	    		if(transJobArap==null){	    			
		    		String unload_type = reland.getStr("unload_type");    		
	    			transJobArap =new TransJobOrderArap();
	    			transJobArap.set("order_id", job_order_id);
		    		transJobArap.set("type", unload_type);
		    		transJobArap.set("order_type", "cost");
		    		if(StringUtils.isNotBlank(car_id)){
		    			transJobArap.set("car_id", car_id);
		    		}
		    		transJobArap.set("charge_id", charge_id);
		    		transJobArap.set("price", price);
		    		transJobArap.set("amount", amount);
		    		transJobArap.set("unit_id", unit_id);
		    		transJobArap.set("total_amount", total_amount);
		    		transJobArap.set("currency_id", currency_id);
		    		transJobArap.set("exchange_rate", exchange_rate);
		    		transJobArap.set("currency_total_amount", currency_total_amount);
		    		transJobArap.save();
	    		}else{
	    			transJobArap.set("price", price);
	    			transJobArap.set("total_amount", total_amount);
	    			transJobArap.set("currency_total_amount", currency_total_amount);
	    			transJobArap.update();
	    		}
	    	}
		}
        
        if(StringUtils.isBlank(sign)){
        	sign="";
        }
		if(sign=="导出"||sign.equals("导出")){
			String car_no = getPara("car_no");
			String car_id = getPara("car_id");
			String driver = getPara("driver");
			
			String sql_car_no ="";
			String sql_driver ="";
			String sql_id ="";
			String ids = getPara("itemIds");
			String idAttr[] = ids.split(",");
			
			if(StringUtils.isNotBlank(car_id)){
				 sql_car_no = " and tjol.car_no ='"+car_id+"'";
			}
			if(StringUtils.isNotBlank(driver)){
				 sql_driver = " and tjol.driver='"+driver+"'";
			}
			if(StringUtils.isBlank(car_no)){
				car_no=driver;
			}
			if(StringUtils.isNotEmpty(ids)){
				sql_id =" and tjol.id in ("+ids+")";
			}
			
			String condition =sql_car_no+sql_driver+sql_id;	
			
			
			String sql = "select * from( "
		        		+" SELECT tjo.id tjoid,tjol.id ,tjo.office_id,tjo.delete_flag,tjo.order_no,tjo.lading_no,p.company_name customer_name,tjo.customer_id customer_id,"
		        		+" if(tjol.unload_type='移柜',CONVERT (substring(tjo.charge_time, 1, 10),CHAR),IFNULL(CONVERT (substring(tjol.cabinet_date, 1, 10),CHAR),CONVERT (substring(tjol.closing_date, 1, 10),CHAR))) c_date" 
		        		+" ,tjo.type,dock.dock_name take_wharf_name, "
		        		+" dock1.dock_name back_wharf_name,dock2.dock_name loading_wharf1_name,dock3.dock_name " 
		        		+" loading_wharf2_name,tjo.container_no,tjo.cabinet_type,tjol.unload_type,car.car_no,tjol.car_no car_id,tjo.remark, "
		        		+" (CONVERT (substring(tjol.closing_date, 1, 10),CHAR)) create_stamp,tjol.driver,car.car_owned,CONCAT(IFNULL(CONCAT(dock.dock_name,'-'),''),IFNULL(CONCAT(dock2.dock_name,'-'),''),IFNULL(CONCAT(dock3.dock_name,'-'),''),IFNULL(" 
		        		+ "dock1.dock_name,'')) combine_wharf, "
		        		+" (SELECT GROUP_CONCAT(if(tjol.unload_type!='移柜',car.car_no,'') SEPARATOR '/') from trans_job_order tjo1  "
		        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
		        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
		        		+" WHERE tjo.id = tjo1.id) combine_car_no,(SELECT GROUP_CONCAT(if(tjol.unload_type!='移柜',tjol.driver,'') SEPARATOR '/') FROM trans_job_order tjo1 "
		        		+" LEFT JOIN trans_job_order_land_item tjol ON tjol.order_id = tjo1.id LEFT JOIN carinfo car ON car.id = tjol.car_no WHERE tjo.id = tjo1.id ) combine_driver,"
		        		+" (SELECT GROUP_CONCAT(if(tjol.unload_type!='移柜',tjol.unload_type,'') SEPARATOR '/') from trans_job_order tjo1 "
		        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
		        		+" WHERE tjo.id = tjo1.id) combine_unload_type,(SELECT GROUP_CONCAT(if(tjol.unload_type='移柜',car.car_no,'') SEPARATOR '') FROM trans_job_order tjo1 "
		        		+ " LEFT JOIN trans_job_order_land_item tjol ON tjol.order_id = tjo1.id LEFT JOIN carinfo car ON car.id = tjol.car_no WHERE tjo.id = tjo1.id) yigui_car_no, "
		        		+ " (SELECT GROUP_CONCAT(if(tjol.unload_type='移柜',tjol.driver,'') SEPARATOR '') FROM trans_job_order tjo1 "
		        		+ " LEFT JOIN trans_job_order_land_item tjol ON tjol.order_id = tjo1.id LEFT JOIN carinfo car ON car.id = tjol.car_no WHERE tjo.id = tjo1.id) yigui_driver,"
		        		+ " (SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'CHARGE' AND tjoa.charge_id = ( SELECT id FROM "
		        		+ " fin_item f WHERE f. NAME = '运费' AND f.office_id = "+office_id+") ) freight,(SELECT SUM(tjoa.street_vehicle_freight) "
		        		+ "FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.charge_id) street_vehicle_freight,"
		        		+ " (SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'COST' AND tjoa.car_id = car.id  AND tjoa.charge_id = ( SELECT id FROM "
		        		+ " fin_item f WHERE f. NAME = '运费' AND f.office_id = "+office_id+") ) outputScale,cast((SELECT GROUP_CONCAT(CONCAT('（应收）',fi. NAME,':',round(tjoa.currency_total_amount,2))) "
		        		+ " FROM trans_job_order_arap tjoa LEFT JOIN fin_item fi ON fi.id = tjoa.charge_id WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'charge' AND fi. NAME != '运费' GROUP BY tjoa.order_type) AS CHAR ) other_fee,"
		        		//+ "(SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'CHARGE' AND tjoa.charge_id = (SELECT id FROM fin_item f WHERE f. NAME = '压夜费' AND f.office_id = 4)) night_fee,"
		        		+ "tjol.export_flag,"
		        		+ " cast(substring(tjo.charge_time, 1, 10) AS CHAR) charge_time"
		        		+" from trans_job_order_land_item tjol  "
		        		+" LEFT JOIN trans_job_order tjo on tjol.order_id = tjo.id "
		        		+" LEFT JOIN dockinfo dock on dock.id = tjo.take_wharf "
		        		+" LEFT JOIN dockinfo dock1 on dock1.id = tjo.back_wharf "
		        		+" LEFT JOIN dockinfo dock2 on dock2.id = tjol.loading_wharf1 "
		        		+" LEFT JOIN dockinfo dock3 on dock3.id = tjol.loading_wharf2 "
		        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
		        		+" LEFT JOIN party p ON p.id = tjo.customer_id "
		        		+ " WHERE  tjo.delete_flag = 'N' AND tjo.office_id ="+office_id+ condition
							+ " ) A where 1=1 and delete_flag ='N' ";
			

			String sqlExport = sql;
			String[] headers = new String[]{"提单号", "提/收柜日期", "客户", "类型", "拖柜地址", "柜号", "柜型", "提柜类型", "结算车牌","司机","移柜车牌","移柜司机", "产值","运费","街车运费","其他费用",
					"备注"};
			String[] fields = new String[]{"LADING_NO", "C_DATE", "CUSTOMER_NAME", "TYPE", "COMBINE_WHARF", "CONTAINER_NO", "CABINET_TYPE", "COMBINE_UNLOAD_TYPE", "COMBINE_CAR_NO","COMBINE_DRIVER",
					"YIGUI_CAR_NO","YIGUI_DRIVER","OUTPUTSCALE","FREIGHT","STREET_VEHICLE_FREIGHT","OTHER_FEE","REMARK"};
			String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,car_no);
			renderText(fileName);
			if(fileName!=null){
				for(int i=0 ; i<idAttr.length ; i++){
					Record re = Db.findFirst("select * from trans_job_order_land_item tjol where id = ?",idAttr[i]);
					long arapId = re.getLong("id");
					Db.update("UPDATE trans_job_order_land_item SET export_flag = 'Y' WHERE id=?",arapId);
				}
			}
		}
	}
	
	
	public void costConfirm(){
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			Record re = Db.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
			long arapId = re.getLong("id");
			Db.update("UPDATE trans_job_order_arap SET audit_flag = 'Y' WHERE id=?",arapId);
		}
		renderJson("{\"result\":true}");
	}
   
	
}
