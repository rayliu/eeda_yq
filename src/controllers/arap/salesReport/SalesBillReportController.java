package controllers.arap.salesReport;

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
public class SalesBillReportController extends Controller {
	private Log logger = Log.getLog(SalesBillReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/salesBillReport");
        setAttr("listConfigList", configList);
		render("/eeda/arap/SalesReport/SalesBillReport.html");
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
        String sql = " SELECT A.*,SUM(charge_CNY) sum_charge_CNY,SUM(charge_USD) sum_charge_USD,SUM(charge_JPY) sum_charge_JPY ,SUM(charge_HKD) sum_charge_HKD,SUM(charge_total) sum_charge_total, "
        		+"			SUM(pay_charge_CNY) sum_pay_charge_CNY,SUM(pay_charge_USD) sum_pay_charge_USD,SUM(pay_charge_JPY) sum_pay_charge_JPY ,SUM(pay_charge_HKD)  "
        		+"			sum_pay_charge_HKD,SUM(pay_charge_total) sum_pay_charge_total, "
        		+"			SUM(cost_CNY) sum_cost_CNY,SUM(cost_USD) sum_cost_USD,SUM(cost_JPY) sum_cost_JPY ,SUM(cost_HKD) sum_cost_HKD,"
        		+ "         SUM(cost_total) sum_cost_total "
        		+" from( "
        		+"		SELECT jo.id,jo.order_no,jo.customer_id,jos.mbl_no,jo.order_export_date,ul.c_name user_name,p.abbr,cs.royalty_rate,(SELECT contract_no from customer_contract ccon "
        		+ "		LEFT JOIN customer_contract_location ccl on ccon.id = ccl.contract_id"
        		+ "		WHERE ccon.type = jo.type and ccon.customer_id = jo.customer_id and ccon.trans_clause = jo.trans_clause"
        		+ "		and ccon.trade_type = jo.trade_type and ccl.pol_id = jos.pol and ccl.pod_id = jos.pod "
        		+ "		and (jo.order_export_date BETWEEN ccon.contract_begin_time and ccon.contract_end_time)) contract_no, "
        		+"		if(cy.code='CNY' AND joa.order_type='charge',joa.total_amount,0) charge_CNY, "
        		+"		if(cy.code='USD' AND joa.order_type='charge',joa.total_amount,0) charge_USD, "
        		+"		if(cy.code='JPY' AND joa.order_type='charge',joa.total_amount,0) charge_JPY, "
        		+"		if(cy.code='HKD' AND joa.order_type='charge',joa.total_amount,0) charge_HKD, "
        		+"		if(joa.order_type='charge',joa.currency_total_amount,0) charge_total, "
        		+"		if(cy.code='CNY' AND joa.pay_flag='Y' AND joa.order_type='charge',joa.total_amount,0) pay_charge_CNY, "
        		+"		if(cy.code='USD' AND joa.pay_flag='Y' AND joa.order_type='charge',joa.total_amount,0) pay_charge_USD, "
        		+"		if(cy.code='JPY' AND joa.pay_flag='Y' AND joa.order_type='charge',joa.total_amount,0) pay_charge_JPY, "
        		+"		if(cy.code='HKD' AND joa.pay_flag='Y' AND joa.order_type='charge',joa.total_amount,0) pay_charge_HKD, "
        		+"		if(joa.order_type='charge' AND joa.pay_flag='Y',joa.currency_total_amount,0) pay_charge_total, "
        		+"		if(cy.code='CNY' AND joa.order_type='cost',joa.total_amount,0) cost_CNY, "
        		+"		if(cy.code='USD' AND joa.order_type='cost',joa.total_amount,0) cost_USD, "
        		+"		if(cy.code='JPY' AND joa.order_type='cost',joa.total_amount,0) cost_JPY, "
        		+"		if(cy.code='HKD' AND joa.order_type='cost',joa.total_amount,0) cost_HKD, "
        		+"		if(joa.order_type='cost',joa.currency_total_amount,0) cost_total "
        		+"		from job_order jo  "
        		+"		LEFT JOIN job_order_arap joa on joa.order_id = jo.id "
        		+"		LEFT JOIN currency cy on cy.id = joa.currency_id "
        		+"		LEFT JOIN job_order_shipment jos on jos.order_id = jo.id "
        		+"		LEFT JOIN party p on p.id = jo.customer_id "
        		+"		LEFT JOIN customer_salesman cs on cs.party_id =  jo.customer_id "
        		+"		LEFT JOIN user_login ul on ul.id = cs.salesman_id "
        		+"		WHERE jo.office_id = "+office_id
        		+" ) A  where 1= 1"+condition
        		+" GROUP BY A.order_no "
        		+ " ORDER BY order_export_date desc";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total = rec.getLong("total");
        List<Record> orderList = Db.find(sql+sLimit);
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
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 3 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 6 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 8 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.exchange_total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"	  WHERE jo.office_id = "+office_id+" and joa.exchange_currency_id = 9 "
			+"	  and joa.order_type = 'charge' and pay_flag!='Y' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
			+"	WHERE 	jo.office_id = "+office_id
			+"	AND joa.order_type = 'charge' "+condition+""
			+ " and jo.delete_flag = 'N'"
			+ ") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
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
