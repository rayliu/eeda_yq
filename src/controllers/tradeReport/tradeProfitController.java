package controllers.tradeReport;

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
public class tradeProfitController extends Controller {
	private Log logger = Log.getLog(tradeProfitController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/tr/tradeReport/tradeProfit.html");
	}
	
	public long list() {
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return 0;
        }
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.id,A.customer_id,A.abbr,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb FROM ("
        		+" SELECT jo.id,jo.customer_id,p.abbr,"
        		+" IF(joa.order_type='charge' and joa.exchange_currency_id = 3,exchange_total_amount,0) charge_cny,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 6,exchange_total_amount,0) charge_usd,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 8,exchange_total_amount,0) charge_jpy,"
	    		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 9,exchange_total_amount,0) charge_hkd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 3,exchange_total_amount,0) cost_cny,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 6,exchange_total_amount,0) cost_usd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 8,exchange_total_amount,0) cost_jpy,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 9,exchange_total_amount,0) cost_hkd,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from trade_job_order jo "
        		+"  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+"  WHERE jo.office_id ="+office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 GROUP BY A.customer_id  ORDER BY abbr";
		
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
		String order_export_date_begin_time = getPara("order_export_date_begin_time");
		String order_export_date_end_time = getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
		String sp_id = "";
        if(customer_id==null||StringUtils.isBlank(customer_id)){
        	sp_id="";
        }else{
    		sp_id =" and customer_id="+customer_id;
        }
		if(order_export_date_begin_time==null){
			order_export_date_begin_time="";
		}
		if(order_export_date_end_time==null){
			order_export_date_end_time="";
		}
		
		String order_export_date = "";

		if(StringUtils.isNotBlank(order_export_date_begin_time)||StringUtils.isNotBlank(order_export_date_end_time)){
			order_export_date = " and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"')";
		}
		String condition = sp_id+order_export_date;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 3 "
			+"	  and joa.order_type = 'cost' "+condition
			+"	) cost_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from trade_job_order jo "
			+"	  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
			+"	  LEFT JOIN party p on p.id = jo.customer_id"
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'cost' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) cost_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trade_job_order jo "
			+"	LEFT JOIN trade_job_order_arap joa ON jo.id = joa.order_id "
			+"	LEFT JOIN party p ON p.id = jo.customer_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  trade_job_order jo "
			+"	LEFT JOIN trade_job_order_arap joa ON jo.id = joa.order_id "
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
	
	//导出excel对账单
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		if (user==null) {
            return;
        }
	    long office_id=user.getLong("office_id");
		String customer_id = getPara("customer_id");
		String order_export_date_begin_time = getPara("begin_time");
		String order_export_date_end_time = getPara("end_time");
		String customerId = "";
		if(StringUtils.isBlank(customer_id)){
			customerId = "";
		}else{
			customerId = " and customer_id = "+customer_id;
		}
		
		String order_export_date = "";
		String exportName = "";
		if(StringUtils.isNotBlank(order_export_date_begin_time)||StringUtils.isNotBlank(order_export_date_end_time)){
			order_export_date = "  and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"' )";
			exportName = order_export_date_begin_time+"~"+order_export_date_end_time;
		}
	    String condition = customerId+order_export_date;
		
/*		String sqlExport = " SELECT A.id,A.customer_id,A.abbr,A.user_name,A.royalty_rate,A.user_id,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb,(SUM(charge_rmb)-sum(cost_rmb)) profit,FORMAT((((SUM(charge_rmb)-sum(cost_rmb))/(sum(cost_rmb)))*100),2) profit_rate FROM ("
        		+" SELECT jo.id,jo.customer_id,jo.order_export_date,p.abbr,ul.c_name user_name,ul.id user_id,cs.royalty_rate,"
        		+" IF(joa.order_type='charge' and joa.exchange_currency_id = 3,exchange_total_amount,0) charge_cny,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 6,exchange_total_amount,0) charge_usd,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 8,exchange_total_amount,0) charge_jpy,"
	    		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 9,exchange_total_amount,0) charge_hkd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 3,exchange_total_amount,0) cost_cny,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 6,exchange_total_amount,0) cost_usd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 8,exchange_total_amount,0) cost_jpy,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 9,exchange_total_amount,0) cost_hkd,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from job_order jo "
        		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+" LEFT JOIN customer_salesman cs on cs.party_id = jo.customer_id"
        		+ " LEFT JOIN user_login ul on ul.id = cs.salesman_id"
        		+"  WHERE p.office_id ="+office_id+" "
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 "+condition+" GROUP BY A.customer_id  ORDER BY abbr";*/
		String sqlExport = " SELECT A.id,A.customer_id,A.abbr,sum(charge_cny) charge_cny,(SUM(charge_rmb)-sum(cost_rmb)) profit,"
				+ "ROUND(((SUM(charge_rmb)-sum(cost_rmb))/sum(cost_rmb))*100,2) profit_rate,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,"
				+ "sum(charge_hkd) charge_hkd,"
				+ "SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb FROM ("
        		+" SELECT jo.id,jo.customer_id,p.abbr,"
        		+" IF(joa.order_type='charge' and joa.exchange_currency_id = 3,exchange_total_amount,0) charge_cny,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 6,exchange_total_amount,0) charge_usd,"
        		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 8,exchange_total_amount,0) charge_jpy,"
	    		+"	IF(joa.order_type='charge' and joa.exchange_currency_id = 9,exchange_total_amount,0) charge_hkd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 3,exchange_total_amount,0) cost_cny,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 6,exchange_total_amount,0) cost_usd,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 8,exchange_total_amount,0) cost_jpy,"
	    		+"	IF(joa.order_type='cost' and joa.exchange_currency_id = 9,exchange_total_amount,0) cost_hkd,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from trade_job_order jo "
        		+"  LEFT JOIN trade_job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+"  WHERE jo.office_id ="+office_id+" "+condition
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 GROUP BY A.customer_id  ORDER BY abbr";
	    
		String total_name_header = "客户,折合应收(CNY),折合应付(CNY),利润,利润率 ";
		String[] headers = total_name_header.split(",");
		
		
		String[] fields = { "ABBR","CHARGE_RMB", "COST_RMB","PROFIT","PROFIT_RATE"};
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	} 
	
}
