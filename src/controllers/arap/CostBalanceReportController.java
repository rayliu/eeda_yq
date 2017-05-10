package controllers.arap;

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
public class CostBalanceReportController extends Controller {
	private Log logger = Log.getLog(CostBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/costBalanceReport");
		 setAttr("listConfigList", configList);
		render("/eeda/arap/CostBalanceReport/CostBalanceReport.html");
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
        String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,sum(cost_cny) cost_cny,"
        		+ " SUM(cost_usd) cost_usd,SUM(cost_jpy) cost_jpy,sum(cost_hkd) cost_hkd,"
        		+ " SUM(uncost_cny) uncost_cny,SUM(uncost_usd) uncost_usd,sum(uncost_jpy) uncost_jpy,"
        		+ " SUM(uncost_hkd) uncost_hkd,SUM(cost_rmb) cost_rmb,sum(uncost_rmb) uncost_rmb"
        		+ " FROM (SELECT jo.id,jo.customer_id,p.abbr,joa.sp_id,IF (joa.order_type = 'cost'"
        		+ " AND joa.exchange_currency_id = 3,exchange_total_amount,0) cost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 6,"
        		+ " exchange_total_amount,0) cost_usd,IF (joa.order_type = 'cost'"
        		+ " AND joa.exchange_currency_id = 8,exchange_total_amount,0) cost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 9,"
        		+ " exchange_total_amount,0) cost_hkd,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 3 AND pay_flag!='Y',"
        		+ " exchange_total_amount,0) uncost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 6 AND pay_flag!='Y',"
        		+ " exchange_total_amount,0) uncost_usd,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 8 AND pay_flag!='Y',"
        		+ " exchange_total_amount,0) uncost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.exchange_currency_id = 9 AND pay_flag!='Y',"
        		+ " exchange_total_amount,0) uncost_hkd,"
        		+ " IF (joa.order_type = 'cost',currency_total_amount,0) cost_rmb,"
        		+ " IF (joa.order_type = 'cost' AND pay_flag!='Y',currency_total_amount,0) uncost_rmb"
        		+ " FROM job_order jo"
        		+ " LEFT JOIN job_order_arap joa ON jo.id = joa.order_id"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
        		+ " WHERE jo.office_id =" +office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+ " ) A"
        		+ " WHERE A.sp_id IS NOT NULL AND A.cost_rmb!=0"
        		+ " GROUP BY A.sp_id"
        		+ " ORDER BY uncost_rmb desc";
		
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
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 3 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 3 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_cost,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_uncost";
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}

}
