package controllers.arap.salesReport;

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
        		+"		SELECT jo.id,jo.order_no,jo.fee_count,jo.customer_id,jos.mbl_no,jo.order_export_date,IFNULL(locean1.name,lair1.name) pol_name,"
        		+ "		IFNULL(locean2.name,lair2.name) pod_name,ul.c_name user_name,p.abbr,cs.royalty_rate,(SELECT contract_no from customer_contract ccon "
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
        		+"		LEFT JOIN job_order_air_item joai on joai.order_id = jo.id "
        		+"		LEFT JOIN location locean1 on locean1.id = jos.pol "
        		+"		LEFT JOIN location locean2 on locean2.id = jos.pod "
        		+"		LEFT JOIN location lair1 on lair1.id = joai.start_from "
        		+"		LEFT JOIN location lair2 on lair2.id = joai.destination "
        		+"		LEFT JOIN party p on p.id = jo.customer_id "
        		+"		LEFT JOIN customer_salesman cs on cs.party_id =  jo.customer_id "
        		+"		LEFT JOIN user_login ul on ul.id = cs.salesman_id "
        		+"		WHERE jo.office_id = "+office_id+" and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
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
		String spid =(String) getPara("customer_id");
		String user_name = getPara("user_name");
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
		
        String sp_id = "";
		if(StringUtils.isBlank(spid)){
			sp_id="";
		}else{
			sp_id =" and p.id="+spid;
		}
		String userName = "";
		if(StringUtils.isBlank(user_name)){
			userName="";
		}else{
			userName =" and ul.c_name='"+user_name+"'";
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
		String condition = sp_id+userName+order_export_date;
		
		String sql=" SELECT *,SUM(foot_charge_total) sum_foot_charge_total,SUM(foot_cost_total) sum_foot_cost_total,(SUM(foot_charge_total)-SUM(foot_cost_total)) sum_foot_gross_profit,  "
				+" SUM(foot_pay_charge_total) sum_foot_pay_charge_total, (SUM(foot_pay_charge_total)- SUM(foot_cost_total))  sum_foot_current_profit, "
				+" CONVERT(SUM(commission_money),decimal(10,2)) foot_commission_money from ( "
				+" SELECT *,SUM(charge_total) foot_charge_total,SUM(pay_charge_total) foot_pay_charge_total,SUM(cost_total) foot_cost_total,"
				+ "(((SUM(charge_total)-SUM(cost_total))*royalty_rate)/100) commission_money from ( "
				+" SELECT 	cs.royalty_rate,jo.order_no,jo.order_export_date, "
				+" IF ( joa.order_type = 'charge',joa.currency_total_amount,0) charge_total, "
				+" IF ( joa.order_type = 'charge' AND joa.pay_flag = 'Y',joa.currency_total_amount,0) pay_charge_total, "
				+" IF ( joa.order_type = 'cost',joa.currency_total_amount,0) cost_total,ul.c_name"
				+" FROM job_order jo "
				+" LEFT JOIN job_order_arap joa ON joa.order_id = jo.id "
				+" LEFT JOIN party p ON p.id = jo.customer_id "
				+" LEFT JOIN customer_salesman cs ON cs.party_id = jo.customer_id "
				+" LEFT JOIN user_login ul ON ul.id = cs.salesman_id "
				+" WHERE "
				+" 	jo.office_id ="+office_id
				+" AND p.id IN ( 	SELECT  customer_id "
				+" 	FROM user_customer "
				+" 	WHERE user_name = '"+currentUser.getPrincipal()+"' )  "+condition
				+" ) A "
				+" WHERE  	1 = 1  GROUP BY  	A.order_no  ORDER BY  	order_export_date desc "
				+"  "
				+" ) B  ";

		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}
	
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		long office_id = user.getLong("office_id");
		String customer_id = getPara("customer_id");
		String user_name = getPara("user_name");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		String customerId = "";
		String userName = "";
		String order_export_date = "";
		if (StringUtils.isBlank(customer_id)) {
			customerId = "";
		} else {
			customerId = " and jo.customer_id = " + customer_id;
		}
		if (StringUtils.isBlank(user_name)) {
			userName = "";
		} else {
			userName = " and user_name = '" + user_name+"'";
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			order_export_date = "";
		} else {
			order_export_date =  " and (order_export_date between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = customerId+userName+order_export_date;

		String sql = " SELECT A.*,CONCAT(IF(SUM(charge_CNY)!=0,CONCAT('CNY:',round(SUM(charge_CNY),2)),''),"
				+ " '  ',IF(SUM(charge_USD)!=0,CONCAT('USD:',round(SUM(charge_USD),2)),''),'  ',IF(SUM(charge_JPY)!=0,CONCAT('JPY:',round(SUM(charge_JPY),2)),''),"
				+ " '  ',IF(SUM(charge_HKD)!=0,CONCAT('HKD:',round(SUM(charge_HKD),2)),'')) receivable,"
				+ "	CONCAT(IF(SUM(pay_charge_CNY)!=0,CONCAT('CNY:',round(SUM(pay_charge_CNY),2)),''),"
				+ "'  ',IF(SUM(pay_charge_USD)!=0,CONCAT('USD:',round(SUM(pay_charge_USD),2)),''),"
				+ "'  ',IF(SUM(pay_charge_JPY)!=0,CONCAT('JPY:',round(SUM(pay_charge_JPY),2)),''),"
				+ "'  ',IF(SUM(pay_charge_HKD)!=0,CONCAT('HKD:',round(SUM(pay_charge_HKD),2)),'')) receipts,"
				+ "	CONCAT(IF(SUM(cost_CNY)!=0,CONCAT('CNY:',round(SUM(cost_CNY),2)),''),"
				+ "'  ',IF(SUM(cost_USD)!=0,CONCAT('USD:',round(SUM(cost_USD),2)),''),"
				+ "'  ',IF(SUM(cost_JPY)!=0,CONCAT('JPY:',round(SUM(cost_JPY),2)),''),"
				+ "'  ', IF(SUM(cost_HKD)!=0,CONCAT('HKD:',round(SUM(cost_HKD),2)),'')) payable,"
				+ " round(SUM(charge_total),2) sum_charge_total,"
				+ " round(SUM(pay_charge_total),2) sum_pay_charge_total,"
				+ " round(SUM(cost_total),2) sum_cost_total,"
				+ " IFNULL(round(SUM(charge_total), 2) - round(SUM(cost_total), 2),'') gross_profit,"
				+ " IFNULL(round(SUM(pay_charge_total), 2) - round(SUM(cost_total), 2),'') current_profit,"
				+ " round(((round(SUM(charge_total), 2) - round(SUM(cost_total), 2))*royalty_rate)/100,2) commission_money "
        		+" from( "
        		+"		SELECT jo.id,jo.order_no,jo.fee_count,jo.customer_id,jos.mbl_no,jo.order_export_date,IFNULL(locean1.name,lair1.name) pol_name,"
        		+ "		IFNULL(locean2.name,lair2.name) pod_name,ul.c_name user_name,p.abbr,cs.royalty_rate,(SELECT contract_no from customer_contract ccon "
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
        		+"		LEFT JOIN job_order_air_item joai on joai.order_id = jo.id "
        		+"		LEFT JOIN location locean1 on locean1.id = jos.pol "
        		+"		LEFT JOIN location locean2 on locean2.id = jos.pod "
        		+"		LEFT JOIN location lair1 on lair1.id = joai.start_from "
        		+"		LEFT JOIN location lair2 on lair2.id = joai.destination "
        		+"		LEFT JOIN party p on p.id = jo.customer_id "
        		+"		LEFT JOIN customer_salesman cs on cs.party_id =  jo.customer_id "
        		+"		LEFT JOIN user_login ul on ul.id = cs.salesman_id "
        		+"		WHERE jo.office_id = "+office_id+" and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+" ) A  where 1= 1"+condition
        		+" GROUP BY A.order_no "
        		+ " ORDER BY order_export_date desc";

        String sqlExport = sql;
		String total_name_header = "工作单号,提单号,出货期日(日),业务员,客户,合同编号,起运港,目的港,计费数量,应收,折合应收CNY,实收,折合实收CNY,应付,折合应付CNY,毛利润(CNY),当前盈亏(CNY),提成比例%,提成(金额)";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ORDER_NO", "MBL_NO", "ORDER_EXPORT_DATE", "USER_NAME",
				"ABBR", "CONTRACT_NO", "POL_NAME","POD_NAME","FEE_COUNT","RECEIVABLE","SUM_CHARGE_TOTAL","RECEIPTS","SUM_PAY_CHARGE_TOTAL","PAYABLE","SUM_COST_TOTAL","GROSS_PROFIT","CURRENT_PROFIT","ROYALTY_RATE","COMMISSION_MONEY"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
	
	
	
	
}
