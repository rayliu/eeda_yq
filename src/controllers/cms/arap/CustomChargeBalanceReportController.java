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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomChargeBalanceReportController extends Controller {
	private Log logger = Log.getLog(CustomChargeBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/chargeBalanceReport");
        setAttr("listConfigList", configList);
		render("/eeda/cmsArap/customChargeBalanceReport/ChargeBalanceReport.html");
	}
	
	public long list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.id,A.receive_sent_consignee,A.abbr,A.sp_id,IFNULL(sum(charge_cny) ,0) charge_cny, "
        		 		+"   SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd, "
        				 +"        		  IFNULL(sum(uncharge_cny) ,0) uncharge_cny,SUM(uncharge_usd) uncharge_usd, sum(uncharge_jpy) uncharge_jpy, "
        				 +"        		  SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb,sum( uncharge_rmb) uncharge_rmb "
        				 +"        		  FROM (SELECT jo.id,jo.receive_sent_consignee,p.abbr,joa.sp_id,IF (joa.order_type = 'charge' "
        				 +"        		  AND joa.currency_id = 3,total_amount,0) charge_cny, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 6, "
        				 +"        		  total_amount,0) charge_usd,IF (joa.order_type = 'charge' "
        				 +"        		  AND joa.currency_id = 8,total_amount,0) charge_jpy, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 9, "
        				 +"        		  total_amount,0) charge_hkd, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND  pay_flag!='Y', "
        				 +"        		  total_amount,0) uncharge_cny, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND  pay_flag!='Y', "
        				 +"        		  total_amount,0) uncharge_usd, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND  pay_flag!='Y', "
        				 +"        		  total_amount,0) uncharge_jpy, "
        				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND  pay_flag!='Y', "
        				 +"        		  total_amount,0) uncharge_hkd, "
        				 +"        		  IF (joa.order_type = 'charge',total_amount,0) charge_rmb, "
        				 +"        		  IF (joa.order_type = 'charge' AND pay_flag!='Y',total_amount, 0) uncharge_rmb "
        				 +"        		  FROM custom_plan_order jo "
        				 +"        		  LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
        				 +"        		  LEFT JOIN party p ON p.id = joa.sp_id "
        				 +"        		  WHERE jo.office_id = " +office_id+" "+condition
        				 +"        		  and jo.delete_flag = 'N' "
        				 +"			  ) A "
        				 +"        		  WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0 "
        				 +"        		  GROUP BY A.sp_id "
        				 +"     		  ORDER BY uncharge_rmb desc ";
        				
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total = rec.getLong("total");
        List<Record> orderList = Db.find(sql);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
        
        return total;
		
	}
	
	public void listTotal() {
		String spid =(String) getPara("sp_id");
		String date_custom_begin_time =(String) getPara("date_custom_begin_time");
		String date_custom_end_time =(String) getPara("date_custom_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
		
		String sp_id =" and sp_id="+spid;
		if(" and sp_id=".equals(sp_id)){
			sp_id="";
		}
		if(date_custom_begin_time==null){
			date_custom_begin_time="";
		}
		if(date_custom_end_time==null){
			date_custom_end_time="";
		}
		
		String date_custom =  " and (date_custom between '"+date_custom_begin_time+"' and '"+date_custom_end_time+"')";

		if(date_custom_begin_time==""||date_custom_begin_time==""){
			date_custom="";
		}
		String condition = sp_id+date_custom;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from custom_plan_order jo "
			+"	  LEFT JOIN custom_plan_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.total_amount),	0) "
			+"	FROM  custom_plan_order jo "
			+"	LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition+""
			+ " and jo.delete_flag = 'N'"
			+ ") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.total_amount),	0) "
			+"	FROM  custom_plan_order jo "
			+"	LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_uncharge";
		
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}
	
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		long office_id = user.getLong("office_id");
		String sp_id = getPara("sp_id");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String spId = "";
		String date_custom = "";
		if (StringUtils.isBlank(sp_id)) {
			spId = "";
		} else {
			spId = " and joa.sp_id = " + sp_id;
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			date_custom = "";
		} else {
			date_custom =  " and (date_custom between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = spId+date_custom;

		 String sql = " SELECT A.id,A.receive_sent_consignee,A.abbr,A.sp_id,IFNULL(sum(charge_cny) ,0) charge_cny, "
 		 		+"   SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd, "
 				 +"        		  IFNULL(sum(uncharge_cny) ,0) uncharge_cny,SUM(uncharge_usd) uncharge_usd, sum(uncharge_jpy) uncharge_jpy, "
 				 +"        		  SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb,sum( uncharge_rmb) uncharge_rmb,"
 				 +"				  round(((SUM(charge_rmb)-sum(uncharge_rmb))/sum(charge_rmb))*100,2)  payment_rate "
 				 +"        		  FROM (SELECT jo.id,jo.receive_sent_consignee,p.abbr,joa.sp_id,IF (joa.order_type = 'charge' "
 				 +"        		  AND joa.currency_id = 3,total_amount,0) charge_cny, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 6, "
 				 +"        		  total_amount,0) charge_usd,IF (joa.order_type = 'charge' "
 				 +"        		  AND joa.currency_id = 8,total_amount,0) charge_jpy, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 9, "
 				 +"        		  total_amount,0) charge_hkd, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND  pay_flag!='Y', "
 				 +"        		  total_amount,0) uncharge_cny, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND  pay_flag!='Y', "
 				 +"        		  total_amount,0) uncharge_usd, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND  pay_flag!='Y', "
 				 +"        		  total_amount,0) uncharge_jpy, "
 				 +"        		  IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND  pay_flag!='Y', "
 				 +"        		  total_amount,0) uncharge_hkd, "
 				 +"        		  IF (joa.order_type = 'charge',total_amount,0) charge_rmb, "
 				 +"        		  IF (joa.order_type = 'charge' AND pay_flag!='Y',total_amount, 0) uncharge_rmb "
 				 +"        		  FROM custom_plan_order jo "
 				 +"        		  LEFT JOIN custom_plan_order_arap joa ON jo.id = joa.order_id "
 				 +"        		  LEFT JOIN party p ON p.id = joa.sp_id "
 				 +"        		  WHERE jo.office_id = " +office_id+" "+condition
 				 +"        		  and jo.delete_flag = 'N' "
 				 +"			  ) A "
 				 +"        		  WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0 "
 				 +"        		  GROUP BY A.sp_id "
 				 +"     		  ORDER BY uncharge_rmb desc ";

        String sqlExport = sql;
		String total_name_header = "结算公司,CNY(应收),USD(应收),JPY(应收),HKD(应收),折合CNY(应收),CNY(未收),USD(未收),JPY(未收),HKD(未收),折合CNY(未收),回款率";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ABBR", "CHARGE_CNY", "CHARGE_USD", "CHARGE_JPY",
				"CHARGE_HKD", "CHARGE_RMB", "UNCHARGE_CNY","UNCHARGE_USD","UNCHARGE_JPY","UNCHARGE_HKD","UNCHARGE_RMB","PAYMENT_RATE"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
	
	
	
}
