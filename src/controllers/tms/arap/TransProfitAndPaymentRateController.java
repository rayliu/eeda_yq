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
public class TransProfitAndPaymentRateController extends Controller {
	private Log logger = Log.getLog(TransProfitAndPaymentRateController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/profitAndPaymentRate");
        setAttr("listConfigList", configList);
		render("/tms/arap/transProfitAndPaymentRate/ProfitAndPaymentRate.html");
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
        String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb FROM ("
        		+" SELECT jo.id,jo.customer_id,p.abbr,joa.sp_id,"
        		+" IF(joa.order_type='charge' and joa.currency_id = 3,currency_total_amount,0) charge_cny,"
        		+"	IF(joa.order_type='charge' and joa.currency_id = 6,currency_total_amount,0) charge_usd,"
        		+"	IF(joa.order_type='charge' and joa.currency_id = 8,currency_total_amount,0) charge_jpy,"
	    		+"	IF(joa.order_type='charge' and joa.currency_id = 9,currency_total_amount,0) charge_hkd,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 3,currency_total_amount,0) cost_cny,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 6,currency_total_amount,0) cost_usd,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 8,currency_total_amount,0) cost_jpy,"
	    		+"	IF(joa.order_type='cost' and joa.currency_id = 9,currency_total_amount,0) cost_hkd,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from trans_job_order jo "
        		+"  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p ON p.id = joa.sp_id"
        		+"  WHERE jo.office_id ="+office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where A.sp_id is NOT NULL GROUP BY A.sp_id  ORDER BY abbr";
		
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
        long office_id=user.getLong("office_id");
		
		String sp_id =" and sp_id="+spid;
		if(" and sp_id=".equals(sp_id)){
			sp_id="";
		}
		if(charge_time_begin_time==null){
			charge_time_begin_time="1970-01-01";
		}
		if(charge_time_end_time==null){
			charge_time_end_time="2037-12-31";
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
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
			+"	LEFT JOIN party p ON p.id = jo.customer_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
			+"	LEFT JOIN party p ON p.id = jo.customer_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_cost";
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}
	
}
