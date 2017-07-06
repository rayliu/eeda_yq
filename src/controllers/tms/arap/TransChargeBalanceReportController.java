package controllers.tms.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransChargeBalanceReportController extends Controller {
	private Log logger = Log.getLog(TransChargeBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
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
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT D.id,D.customer_id,D.abbr,D.sp_id,charge_cny,charge_usd,charge_jpy,charge_hkd,	"
        		+ "  (charge_cny-charge_have_pay) uncharge_cny,uncharge_usd,uncharge_jpy,uncharge_hkd,charge_have_pay,charge_rmb,(charge_rmb-charge_have_pay) uncharge_rmb	"
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
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
		
		String sp_id =" and sp_id="+spid;
		if(" and sp_id=".equals(sp_id)){
			sp_id="";
		}
		if(order_export_date_begin_time==null){
			order_export_date_begin_time="";
		}
		if(order_export_date_end_time==null){
			order_export_date_end_time="";
		}
		
		String order_export_date =  " and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"')";

		if(order_export_date_begin_time==""||order_export_date_begin_time==""){
			order_export_date="";
		}
		String condition = sp_id+order_export_date;
		
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
	
	
	
	
	
}