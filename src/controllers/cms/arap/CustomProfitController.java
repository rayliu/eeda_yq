package controllers.cms.arap;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomProfitController extends Controller {
	private Log logger = Log.getLog(CustomProfitController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/cmsArap/customProfitAndPaymentRate/Profit.html");
	}
	
	public long list() {
		//String sLimit = "";
        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
			return 0;
		}
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.id,A.receive_sent_consignee,A.abbr,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb,"
        		+ " ROUND(SUM(charge_rmb)-sum(cost_rmb),2) profit,ROUND(((SUM(charge_rmb)-sum(cost_rmb))/sum(charge_rmb))*100,2) profit_rate "
        		+ " FROM ("
        		+"  SELECT jo.id,jo.receive_sent_consignee,p.abbr,"
        		+"  IF(joa.order_type='charge' and joa.currency_id = 3,total_amount,0) charge_cny,"
        		+"	IF(joa.order_type='charge' and joa.currency_id = 6,total_amount,0) charge_usd,"
        		+"	IF(joa.order_type='charge' and joa.currency_id = 8,total_amount,0) charge_jpy,"
	    		+"	IF(joa.order_type='charge' and joa.currency_id = 9,total_amount,0) charge_hkd,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 3,total_amount,0) cost_cny,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 6,total_amount,0) cost_usd,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 8,total_amount,0) cost_jpy,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 9,total_amount,0) cost_hkd,"
	    		+"	if(joa.order_type='charge',total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',total_amount,0) cost_rmb"
        		+"  from custom_plan_order jo "
        		+"  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
        		+"  WHERE jo.office_id ="+office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 GROUP BY A.receive_sent_consignee  ORDER BY abbr";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total = rec.getLong("total");
        List<Record> orderList = Db.find(sql);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		return total;
	}
	
	public void listTotal() {
		String customer_id =(String) getPara("customer");
		String begin_time =(String) getPara("begin_time");
		String end_time =(String) getPara("end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
			return;
		}
        long office_id=user.getLong("office_id");
		
		String receive_sent_consignee =" and receive_sent_consignee="+customer_id;
		if(StringUtils.isBlank(customer_id)){
			receive_sent_consignee = "";
		}
		if(begin_time==null){
			begin_time="";
		}
		if(end_time==null){
			end_time="";
		}
		
		String date_custom = "";

		if(StringUtils.isNotBlank(begin_time)||StringUtils.isNotBlank(end_time)){
			date_custom = " and (date_custom between '"+begin_time+"' and '"+end_time+"')";
		}
		String condition = receive_sent_consignee+date_custom;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'cost' "+condition
			+"	) cost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.total_amount),	0) "
			+"	FROM  custom_plan_order jo "
			+"	LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
			+"	LEFT JOIN party p ON p.id = jo.receive_sent_consignee "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.total_amount),	0) "
			+"	FROM  custom_plan_order jo "
			+"	LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
			+"	LEFT JOIN party p ON p.id = jo.receive_sent_consignee "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_cost";
		
		Record re = Db.findFirst(sql);
		long total = list();
		re.set("total", total);
		renderJson(re);
	}
	
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		if (user==null) {
            return;
        }
		long office_id = user.getLong("office_id");
		String customer_id = getPara("customer");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String customerId = "";
		String date_custom = "";
		if (StringUtils.isBlank(customer_id)) {
			customerId = "";
		} else {
			customerId = " and receive_sent_consignee = " + customer_id;
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			date_custom = "";
		} else {
			date_custom =  " and (date_custom between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = customerId+date_custom;

		 String sql = " SELECT A.id,A.receive_sent_consignee,A.abbr,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb,"
	        		+ " ROUND(SUM(charge_rmb)-sum(cost_rmb),2) profit,ROUND(((SUM(charge_rmb)-sum(cost_rmb))/sum(charge_rmb))*100,2) profit_rate "
	        		+ " FROM ("
	        		+"  SELECT jo.id,jo.receive_sent_consignee,p.abbr,"
	        		+"  IF(joa.order_type='charge' and joa.currency_id = 3,total_amount,0) charge_cny,"
	        		+"	IF(joa.order_type='charge' and joa.currency_id = 6,total_amount,0) charge_usd,"
	        		+"	IF(joa.order_type='charge' and joa.currency_id = 8,total_amount,0) charge_jpy,"
		    		+"	IF(joa.order_type='charge' and joa.currency_id = 9,total_amount,0) charge_hkd,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 3,total_amount,0) cost_cny,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 6,total_amount,0) cost_usd,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 8,total_amount,0) cost_jpy,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 9,total_amount,0) cost_hkd,"
		    		+"	if(joa.order_type='charge',total_amount,0) charge_rmb,"
		    		+"	if(joa.order_type='cost',total_amount,0) cost_rmb"
	        		+"  from custom_plan_order jo "
	        		+"  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
	        		+"  LEFT JOIN party p on p.id = jo.receive_sent_consignee"
	        		+"  WHERE jo.office_id ="+office_id+" "+condition
	        		+ " and jo.delete_flag = 'N'"
	    			+" ) A where 1=1 GROUP BY A.receive_sent_consignee  ORDER BY abbr";

        String sqlExport = sql;
		String total_name_header = "客户,折合应收(CNY),折合应付(CNY),利润(CNY),利润率(%)";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ABBR", "CHARGE_RMB", "COST_RMB", "PROFIT","PROFIT_RATE"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
}
