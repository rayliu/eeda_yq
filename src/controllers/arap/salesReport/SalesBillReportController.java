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
import controllers.oms.jobOrder.JobOrderController;
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
        
        String customer_id =getPara("customer_id");
        if(StringUtils.isNotEmpty(getPara("customer_id"))){
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,customer_id,"CUSTOMER");
    	}
        
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.*,SUM(charge_CNY) sum_charge_CNY,SUM(charge_USD) sum_charge_USD,SUM(charge_JPY) sum_charge_JPY ,SUM(charge_HKD) sum_charge_HKD,SUM(charge_total) sum_charge_total, "
        		+"			SUM(pay_charge_CNY) sum_pay_charge_CNY,SUM(pay_charge_USD) sum_pay_charge_USD,SUM(pay_charge_JPY) sum_pay_charge_JPY ,SUM(pay_charge_HKD)  "
        		+"			sum_pay_charge_HKD,SUM(pay_charge_total) sum_pay_charge_total, "
        		+"			SUM(cost_CNY) sum_cost_CNY,SUM(cost_USD) sum_cost_USD,SUM(cost_JPY) sum_cost_JPY ,SUM(cost_HKD) sum_cost_HKD,"
        		+ "         SUM(cost_total) sum_cost_total "
        		+" from( "
        		+"		SELECT jo.id,jo.order_no,jo.fee_count,jo.customer_id,jos.mbl_no,jo.order_export_date,IFNULL(locean1.name,lair1.name) pol_name,"
        		+ "		IFNULL(locean2.name,lair2.name) pod_name,ul.c_name user_name,p.abbr customer_name,cs.royalty_rate,"
        		+ " (SELECT contract_no from customer_contract ccon"
        		+ "		LEFT JOIN customer_contract_location ccl ON ccl.contract_id = ccon.id"
        		+ "		WHERE	ccon.type = jo.type	AND ccon.customer_id = jo.customer_id"
        		+ "		AND ccon.trans_clause = jo.trans_clause	AND ccon.trade_type = jo.trade_type"
        		+ "		AND ccl.pol_id = jos.pol AND ccl.pod_id = jos.pod	AND (jo.order_export_date BETWEEN ccon.contract_begin_time"
        		+ "		AND ccon.contract_end_time)	and ccon.customer_id = jo.customer_id)	contract_no, "
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
        		+"		LEFT JOIN customer_contract ccon ON ccon.customer_id = jo.customer_id "
        		+"		LEFT JOIN customer_contract_location ccl ON ccl.contract_id = ccon.id "
        		+"		WHERE (jo.office_id="+office_id+ ref_office+ ") and jo.delete_flag = 'N'"
        		+"		AND p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+" ) A  where 1= 1"+condition
        		+" GROUP BY A.order_no "
        		+ " ORDER BY order_export_date desc";
		
        String sqlTotal = "select count(1) total from ("+sql+") B";
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
		String customer_id = getPara("customer_id");
		String customer_name = getPara("customer_name");
		String user_name = getPara("user_name");
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
		
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String customerId = "";
		if(StringUtils.isBlank(customer_id)){
			customerId="";
		}else{
			customerId =" and customer_id="+customer_id;
		}
		String customerName = "";
		if(StringUtils.isBlank(customer_name)){
			customerName="";
		}else{
			customerName =" and customer_name like '%"+customer_name+"%'";
		}
		String userName = "";
		if(StringUtils.isBlank(user_name)){
			userName="";
		}else{
			userName =" and user_name='"+user_name+"'";
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
		String condition = customerId+customerName+userName+order_export_date;
		
		String sql = " SELECT IFNULL(SUM(sum_charge_total),0.00) sum_foot_charge_total,IFNULL(SUM(sum_cost_total),0.00) sum_foot_cost_total,IFNULL(SUM(sum_pay_charge_total),0.00) sum_foot_pay_charge_total "
				+ " FROM(SELECT A.*,"
				+ " SUM(charge_total) sum_charge_total,"
				+ " SUM(pay_charge_total) sum_pay_charge_total,"
				+ " SUM(cost_total) sum_cost_total,"
				+ " CAST(IFNULL(round(SUM(charge_total), 2) - round(SUM(cost_total), 2),'') as char) gross_profit,"
				+ " CAST(IFNULL(round(SUM(pay_charge_total), 2) - round(SUM(cost_total), 2),'') as char) current_profit"
        		+" from( "
        		+"		SELECT jo.id,jo.order_no,jo.fee_count,jo.customer_id,jo.order_export_date,"
        		+ "		ul.c_name user_name,p.abbr customer_name,cs.royalty_rate, "
        		+"		if(joa.order_type='charge',joa.currency_total_amount,0) charge_total, "
        		+"		if(joa.order_type='charge' AND joa.pay_flag='Y',joa.currency_total_amount,0) pay_charge_total, "
        		+"		if(joa.order_type='cost',joa.currency_total_amount,0) cost_total "
        		+"		from job_order jo  "
        		+"		LEFT JOIN job_order_arap joa on joa.order_id = jo.id "
        		+"		LEFT JOIN currency cy on cy.id = joa.currency_id "
        		+"		LEFT JOIN party p on p.id = jo.customer_id "
        		+"		LEFT JOIN customer_salesman cs on cs.party_id =  jo.customer_id "
        		+"		LEFT JOIN user_login ul on ul.id = cs.salesman_id "
        		+"		WHERE (jo.office_id="+office_id+ ref_office+ ") and jo.delete_flag = 'N'"
        		+"		AND p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+" ) A  where 1= 1"+condition
        		+" GROUP BY A.order_no "
        		+ " ORDER BY order_export_date desc ) B where 1=1";
		
		Record re = Db.findFirst(sql);
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
		
		String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
		
		String order_export_date = "";
		if (StringUtils.isBlank(customer_id)) {
			customerId = "";
		} else {
			customerId = " and customer_id = " + customer_id;
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

		String sql = " SELECT A.*,"
				+ " CAST(CONCAT(IF(SUM(charge_CNY)!=0,CONCAT('CNY:',round(SUM(charge_CNY),2)),''),"
				+ " '  ',IF(SUM(charge_USD)!=0,CONCAT('USD:',round(SUM(charge_USD),2)),''),'  ',IF(SUM(charge_JPY)!=0,CONCAT('JPY:',round(SUM(charge_JPY),2)),''),"
				+ " '  ',IF(SUM(charge_HKD)!=0,CONCAT('HKD:',round(SUM(charge_HKD),2)),''))as char) receivable,"
				+ "	CAST(CONCAT(IF(SUM(pay_charge_CNY)!=0,CONCAT('CNY:',round(SUM(pay_charge_CNY),2)),''),"
				+ "'  ',IF(SUM(pay_charge_USD)!=0,CONCAT('USD:',round(SUM(pay_charge_USD),2)),''),"
				+ "'  ',IF(SUM(pay_charge_JPY)!=0,CONCAT('JPY:',round(SUM(pay_charge_JPY),2)),''),"
				+ "'  ',IF(SUM(pay_charge_HKD)!=0,CONCAT('HKD:',round(SUM(pay_charge_HKD),2)),'')) as char) receipts,"
				+ "	CAST(CONCAT(IF(SUM(cost_CNY)!=0,CONCAT('CNY:',round(SUM(cost_CNY),2)),''),"
				+ "'  ',IF(SUM(cost_USD)!=0,CONCAT('USD:',round(SUM(cost_USD),2)),''),"
				+ "'  ',IF(SUM(cost_JPY)!=0,CONCAT('JPY:',round(SUM(cost_JPY),2)),''),"
				+ "'  ', IF(SUM(cost_HKD)!=0,CONCAT('HKD:',round(SUM(cost_HKD),2)),'')) as char) payable,"
				+ " round(SUM(charge_total),2) sum_charge_total,"
				+ " round(SUM(pay_charge_total),2) sum_pay_charge_total,"
				+ " round(SUM(cost_total),2) sum_cost_total,"
				+ " CAST(IFNULL(round(SUM(charge_total), 2) - round(SUM(cost_total), 2),'') as char) gross_profit,"
				+ " CAST(IFNULL(round(SUM(pay_charge_total), 2) - round(SUM(cost_total), 2),'') as char) current_profit,"
				+ " (SUM(charge_total)-SUM(cost_total))*(royalty_rate/100) receivable_tiji,"
				+ " (SUM(pay_charge_total)-SUM(cost_total))*(royalty_rate/100) net_receivable_tiji"
        		+" from( "
        		+"		SELECT jo.id,jo.order_no,jo.fee_count,jo.customer_id,jos.mbl_no,jo.order_export_date,IFNULL(locean1.name,lair1.name) pol_name,"
        		+ "		IFNULL(locean2.name,lair2.name) pod_name,ul.c_name user_name,p.abbr,cs.royalty_rate,"
        		+ " (SELECT contract_no from customer_contract ccon"
        		+ "		LEFT JOIN customer_contract_location ccl ON ccl.contract_id = ccon.id"
        		+ "		WHERE	ccon.type = jo.type	AND ccon.customer_id = jo.customer_id"
        		+ "		AND ccon.trans_clause = jo.trans_clause	AND ccon.trade_type = jo.trade_type"
        		+ "		AND ccl.pol_id = jos.pol AND ccl.pod_id = jos.pod	AND (jo.order_export_date BETWEEN ccon.contract_begin_time"
        		+ "		AND ccon.contract_end_time)	and ccon.customer_id = jo.customer_id)	contract_no, "
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
        		+"		WHERE (jo.office_id="+office_id+ ref_office+ ") and jo.delete_flag = 'N'"
        		+"		AND p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')"
        		+" ) A  where 1= 1"+condition
        		+" GROUP BY A.order_no "
        		+ " ORDER BY order_export_date desc";

        String sqlExport = sql;
		String total_name_header = "工作单号,提单号,出货期日(日),业务员,客户,合同编号,起运港,目的港,计费数量,应收,折合应收CNY,实收,折合实收CNY,应付,折合应付CNY,毛利润(CNY),当前盈亏(CNY),提成比例%,应收计提,实收计提";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ORDER_NO", "MBL_NO", "ORDER_EXPORT_DATE", "USER_NAME",
				"ABBR", "CONTRACT_NO", "POL_NAME","POD_NAME","FEE_COUNT","RECEIVABLE","SUM_CHARGE_TOTAL","RECEIPTS","SUM_PAY_CHARGE_TOTAL","PAYABLE","SUM_COST_TOTAL","GROSS_PROFIT","CURRENT_PROFIT","ROYALTY_RATE","RECEIVABLE_TIJI","NET_RECEIVABLE_TIJI"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
	
	
	
	
}
