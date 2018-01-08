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
public class TransCostBalanceReportController extends Controller {
	private Log logger = Log.getLog(TransCostBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/costBalanceReport");
		 setAttr("listConfigList", configList);
		render("/tms/arap/transCostBalanceReport/CostBalanceReport.html");
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
        String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,A.car_id,A.car_no_name,sum(cost_cny) cost_cny,"
        		+ " SUM(cost_usd) cost_usd,SUM(cost_jpy) cost_jpy,sum(cost_hkd) cost_hkd,"
        		+ " SUM(uncost_cny) uncost_cny,SUM(uncost_usd) uncost_usd,sum(uncost_jpy) uncost_jpy,"
        		+ " SUM(uncost_hkd) uncost_hkd,SUM(cost_rmb) cost_rmb,sum(uncost_rmb) uncost_rmb"
        		+ " FROM (SELECT jo.id,jo.customer_id,p.abbr,c.car_no car_no_name,joa.sp_id,joa.car_id,IF (joa.order_type = 'cost'"
        		+ " AND joa.currency_id = 3,currency_total_amount,0) cost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 6,"
        		+ " currency_total_amount,0) cost_usd,IF (joa.order_type = 'cost'"
        		+ " AND joa.currency_id = 8,currency_total_amount,0) cost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 9,"
        		+ " currency_total_amount,0) cost_hkd,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 3 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 6 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_usd,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 8 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 9 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_hkd,"
        		+ " IF (joa.order_type = 'cost',currency_total_amount,0) cost_rmb,"
        		+ " IF (joa.order_type = 'cost' AND pay_flag!='Y',currency_total_amount,0) uncost_rmb"
        		+ " FROM trans_job_order jo"
        		+ " LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
   				+ " LEFT JOIN carinfo c ON c.id = joa.car_id "
        		+ " WHERE jo.office_id =" +office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+ " ) A"
        		+ " WHERE (A.sp_id IS NOT NULL or A.car_id is not null) AND A.cost_rmb!=0"
        		+ " GROUP BY A.sp_id,A.car_id"
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
		String carid =(String) getPara("car_id");
		String charge_time_begin_time =(String) getPara("charge_time_begin_time");
		String charge_time_end_time =(String) getPara("charge_time_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
		
		String car_id =" and car_id="+carid;
		if(" and car_id=null".equals(car_id)||" and car_id=".equals(car_id)){
			car_id="";
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
		String condition = car_id+charge_time;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.currency_total_amount),0)"
			+"	  from trans_job_order jo "
			+"	  LEFT JOIN trans_job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncost_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_cost,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trans_job_order jo "
			+"	LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'cost' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_uncost";
		
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
		String car_id = getPara("car_id");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String carId = "";
		String charge_time = "";
		if (StringUtils.isBlank(car_id)) {
			carId = "";
		} else {
			carId = " and joa.car_id = " + car_id;
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			charge_time = "";
		} else {
			charge_time =  " and (charge_time between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = carId+charge_time;

		String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,A.car_id,A.car_no_name,sum(cost_cny) cost_cny,"
        		+ " SUM(cost_usd) cost_usd,SUM(cost_jpy) cost_jpy,sum(cost_hkd) cost_hkd,"
        		+ " SUM(uncost_cny) uncost_cny,SUM(uncost_usd) uncost_usd,sum(uncost_jpy) uncost_jpy,"
        		+ " SUM(uncost_hkd) uncost_hkd,SUM(cost_rmb) cost_rmb,sum(uncost_rmb) uncost_rmb,"
        		+ " round(((SUM(cost_rmb)-sum(uncost_rmb))/sum(cost_rmb))*100,2)  payment_rate"
        		+ " FROM (SELECT jo.id,jo.customer_id,p.abbr,c.car_no car_no_name,joa.sp_id,joa.car_id,IF (joa.order_type = 'cost'"
        		+ " AND joa.currency_id = 3,currency_total_amount,0) cost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 6,"
        		+ " currency_total_amount,0) cost_usd,IF (joa.order_type = 'cost'"
        		+ " AND joa.currency_id = 8,currency_total_amount,0) cost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 9,"
        		+ " currency_total_amount,0) cost_hkd,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 3 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_cny,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 6 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_usd,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 8 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_jpy,"
        		+ " IF (joa.order_type = 'cost' AND joa.currency_id = 9 AND pay_flag!='Y',"
        		+ " currency_total_amount,0) uncost_hkd,"
        		+ " IF (joa.order_type = 'cost',currency_total_amount,0) cost_rmb,"
        		+ " IF (joa.order_type = 'cost' AND pay_flag!='Y',currency_total_amount,0) uncost_rmb"
        		+ " FROM trans_job_order jo"
        		+ " LEFT JOIN trans_job_order_arap joa ON jo.id = joa.order_id"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
   				+ " LEFT JOIN carinfo c ON c.id = joa.car_id "
        		+ " WHERE jo.office_id =" +office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+ " ) A"
        		+ " WHERE (A.sp_id IS NOT NULL or A.car_id is not null) AND A.cost_rmb!=0"
        		+ " GROUP BY A.sp_id,A.car_id"
        		+ " ORDER BY uncost_rmb desc";

        String sqlExport = sql;
		String total_name_header = "结算公司,结算车牌,CNY应付),HKD(应付),折合CNY(应付),CNY(未付),HKD(未付),折合CNY(未付),付款率";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ABBR","CAR_NO_NAME","COST_CNY", "COST_HKD","COST_RMB","UNCOST_CNY","UNCOST_HKD","UNCOST_RMB","PAYMENT_RATE"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
	

}
