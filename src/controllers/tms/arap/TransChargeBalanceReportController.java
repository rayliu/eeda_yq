package controllers.tms.arap;

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
public class TransChargeBalanceReportController extends Controller {
	private Log logger = Log.getLog(TransChargeBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/chargeBalanceReport");
        setAttr("listConfigList", configList);
		render("/tms/arap/transChargeBalanceReport/ChargeBalanceReport.html");
	}

	public long list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return 0;
        }
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT D.id,D.customer_id,D.abbr,D.sp_id,charge_cny,charge_usd,charge_jpy,charge_hkd,	"
        		+ "  (charge_cny-charge_have_pay) uncharge_cny,uncharge_usd,uncharge_jpy,uncharge_hkd,charge_have_pay,charge_rmb,(charge_rmb-charge_have_pay) uncharge_rmb"
        		+ " from("
        		+ " SELECT A.id,A.customer_id,A.abbr,A.sp_id,sum(charge_cny) charge_cny,"
        		+ " SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,"
        		+ " SUM(uncharge_usd) uncharge_usd,sum(uncharge_jpy) uncharge_jpy,"
        		+ " SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb"
        		+ " ,IFNULL((SELECT SUM(tacri.receive_cny) FROM trans_arap_charge_receive_item tacri "
        		+ "	LEFT JOIN trans_arap_charge_order taco on tacri.charge_order_id=taco.id "
        		+ "	WHERE taco.sp_id=A.sp_id GROUP BY taco.sp_id "
        		+ "	),0) charge_have_pay"
        		+ " FROM (SELECT jo.id,jo.customer_id,p.abbr,joa.sp_id,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 3,currency_total_amount,0) charge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6,"
        		+ " currency_total_amount,0) charge_usd,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 8,currency_total_amount,0) charge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9,"
        		+ " currency_total_amount,0) charge_hkd,"
//        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND pay_flag!='Y',"
//        		+ " currency_total_amount,0) uncharge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncharge_usd,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncharge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncharge_hkd,"
        		+ " IF (joa.order_type = 'charge',currency_total_amount,0) charge_rmb"
//        		+ " ,IF (joa.order_type = 'charge' AND pay_flag!='Y',currency_total_amount,0) uncharge_rmb"
        		+ " FROM trans_job_order_arap joa"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
        		+ " LEFT JOIN trans_job_order jo ON jo.id = joa.order_id"

        		+ " WHERE jo.office_id =" +office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
				+ " ) A"
        		+ " WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0"
        		+ " GROUP BY A.sp_id"
        		+ ")D "
        		+ " ORDER BY uncharge_rmb desc";
		
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
		String charge_time_begin_time =(String) getPara("charge_time_begin_time");
		String charge_time_end_time =(String) getPara("charge_time_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
		
		String sp_id =" and sp_id="+spid;
		if(" and sp_id=null".equals(sp_id)||" and sp_id=".equals(sp_id)){
			sp_id="";
		}
		if(charge_time_begin_time==null){
			charge_time_begin_time="";
		}
		if(charge_time_end_time==null){
			charge_time_end_time="";
		}
		
		String charge_time =  " and (charge_time between '"+charge_time_begin_time+"' and '"+charge_time_end_time+"')";

		if(charge_time_begin_time==""||charge_time_end_time==""){
			charge_time="";
		}
		String condition = sp_id+charge_time;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition+""
			+ " and jo.delete_flag = 'N'"
			+ ") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
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
		if (user==null) {
            return;
        }
		long office_id = user.getLong("office_id");
		String sp_id = getPara("sp_id");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String spId = "";
		String charge_time = "";
		if (StringUtils.isBlank(sp_id)) {
			spId = "";
		} else {
			spId = " and joa.sp_id = " + sp_id;
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			charge_time = "";
		} else {
			charge_time =  " and (charge_time between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = spId+charge_time;

		 String sql = " SELECT D.id,D.customer_id,D.abbr,D.sp_id,charge_cny,charge_usd,charge_jpy,charge_hkd,	"
	        		+ "  (charge_cny-charge_have_pay) uncharge_cny,uncharge_usd,uncharge_jpy,uncharge_hkd,charge_have_pay,charge_rmb,(charge_rmb-charge_have_pay) uncharge_rmb,"
	        		+ "	 round(((charge_rmb-(charge_rmb - charge_have_pay))/charge_rmb)*100,2)  payment_rate"
	        		+ " from("
	        		+ " SELECT A.id,A.customer_id,A.abbr,A.sp_id,sum(charge_cny) charge_cny,"
	        		+ " SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,"
	        		+ " SUM(uncharge_usd) uncharge_usd,sum(uncharge_jpy) uncharge_jpy,"
	        		+ " SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb"
	        		+ " ,IFNULL((SELECT SUM(tacri.receive_cny) FROM trans_arap_charge_receive_item tacri "
	        		+ "	LEFT JOIN trans_arap_charge_order taco on tacri.charge_order_id=taco.id "
	        		+ "	WHERE taco.sp_id=A.sp_id GROUP BY taco.sp_id "
	        		+ "	),0) charge_have_pay"
	        		+ " FROM (SELECT jo.id,jo.customer_id,p.abbr,joa.sp_id,IF (joa.order_type = 'charge'"
	        		+ " AND joa.currency_id = 3,currency_total_amount,0) charge_cny,"
	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6,"
	        		+ " currency_total_amount,0) charge_usd,IF (joa.order_type = 'charge'"
	        		+ " AND joa.currency_id = 8,currency_total_amount,0) charge_jpy,"
	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9,"
	        		+ " currency_total_amount,0) charge_hkd,"
	//	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND pay_flag!='Y',"
	//	        		+ " currency_total_amount,0) uncharge_cny,"
	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND pay_flag!='Y',"
	        		+ " currency_total_amount,0) uncharge_usd,"
	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND pay_flag!='Y',"
	        		+ " currency_total_amount,0) uncharge_jpy,"
	        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND pay_flag!='Y',"
	        		+ " currency_total_amount,0) uncharge_hkd,"
	        		+ " IF (joa.order_type = 'charge',currency_total_amount,0) charge_rmb"
	//	        		+ " ,IF (joa.order_type = 'charge' AND pay_flag!='Y',currency_total_amount,0) uncharge_rmb"
	        		+ " FROM trans_job_order jo"
	        		+ " LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id"
	        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
	        		+ " WHERE jo.office_id =" +office_id+" "+condition
	        		+ " and jo.delete_flag = 'N'"
					+ " ) A"
	        		+ " WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0"
	        		+ " GROUP BY A.sp_id"
	        		+ ")D "
	        		+ " ORDER BY uncharge_rmb desc";

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
