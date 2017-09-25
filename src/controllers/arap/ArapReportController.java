package controllers.arap;

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

import com.google.gson.Gson;
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
public class ArapReportController extends Controller {
	private Log logger = Log.getLog(ArapReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/arapReport");
		setAttr("listConfigList", configList);
		render("/eeda/arap/ArapReport/ArapReport.html");
	}
	
	public String list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String sql = "select * ,if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from arap_charge_order aco "
					+" 						LEFT JOIN arap_charge_item aci on aco.id=aci.charge_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 		),(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from arap_cost_order aco  "
					+" 						LEFT JOIN arap_cost_item aci on aco.id=aci.cost_order_id "
					+" 					WHERE aci.ref_order_id=B.id "
					+" 						) "
					+" ) check_order_no, "
					+" if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from arap_charge_order aco  "
					+" 							LEFT JOIN arap_charge_item aci on aco.id=aci.charge_order_id "
					+" 							WHERE aci.ref_order_id=B.id "
					+" 			),(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from arap_cost_order aco  "
					+" 							LEFT JOIN arap_cost_item aci on aco.id=aci.cost_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 				) "
					+" )check_order_id ,"
					+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
					+" 		FROM	arap_charge_application_order acao "
					+" 		LEFT JOIN charge_application_order_rel caor ON acao.id = caor.application_order_id "
					+" 		WHERE		caor.job_order_arap_id = B.id "
					+" 	),	(	SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
					+" 		FROM	arap_cost_application_order acao "
					+" 		LEFT JOIN cost_application_order_rel caor ON acao.id = caor.application_order_id  "
					+" 		WHERE	caor.job_order_arap_id = B.id "
					+" 	) "
					+" ) application_order_no, "
					+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
					+" 		FROM	 charge_application_order_rel caor  "
					+" 		WHERE		caor.job_order_arap_id = B.id "
					+" 	), "
					+" 	(SELECT		GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
					+" 		FROM  cost_application_order_rel caor  "
					+" 		WHERE	caor.job_order_arap_id = B.id "
					+" 	) "
					+" ) application_order_id "
        		+ " from(  "
         		+ " select joa.id,joa.type,joa.order_type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.charge_id fin_item_id,"
         		+" (CASE WHEN joa.pay_flag='Y' THEN '费用已结算' WHEN joa.create_flag='Y' THEN '费用已创建申请单' WHEN joa.billConfirm_flag='Y' THEN '费用已创建对账单并已确认'" 
         		+" WHEN joa.bill_flag='Y' THEN '费用已创建对账单' WHEN joa.audit_flag='Y' THEN '费用已确认' ELSE '费用未确认' end) flag,"
         		+ " joa.exchange_rate,joa.exchange_currency_rate,ifnull(joa.currency_total_amount,0) currency_total_amount,ifnull(joa.exchange_total_amount,0) exchange_total_amount,"
         		+ " jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id, "
         		+ " p.abbr sp_name,p1.abbr customer_name, "
         		+ " f.name fin_name,cur1.name exchange_currency_name, "
         		+ " cur.name currency_name "
 				+ " from job_order_arap joa "
 				+ " left join job_order jo on jo.id=joa.order_id "
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.customer_id "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
 				+ " left join fin_item f on f.id=joa.charge_id "
 				+ " where  jo.office_id = "+office_id + ref_office
 				+ " and jo.delete_flag = 'N'"
				+ " GROUP BY joa.id "
 				+ " ) B where 1=1 ";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql+ condition + " order by order_export_date desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		return sql;
	}
			//导出excel对账单
			public void downloadExcelList(){
				UserLogin user = LoginUserController.getLoginUser(this);
			    long office_id=user.getLong("office_id");
			    
				String jsonStr = getPara("params");
				Gson gson = new Gson();
				Map<String,?> dto = gson.fromJson (jsonStr,HashMap.class);
				String order_no = (String)dto.get("order_no");
				String customer_id = (String)dto.get("customer_id");
				String sp_id = (String)dto.get("sp_id");
				String fin_item = (String)dto.get("fin_item");
				String flag = (String)dto.get("flag");
				String order_export_date_begin_time = (String)dto.get("order_export_date_begin_time");
				String order_export_date_end_time = (String)dto.get("order_export_date_end_time");
				
				String orderNo = " and order_no like '%"+order_no+"%'";
				String customerId =" and customer_id = '"+customer_id+"'";
				String spId = " and sp_id = '"+sp_id+"'";
				String finItem = " and fin_item_id = '"+fin_item+"'";
				String flag_condition = " and flag = '"+flag+"'";
				String order_export_date = " and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"' )";
				String export = "";
				if(StringUtils.isBlank(order_no)){
					orderNo = "";
				}
				if(StringUtils.isBlank(customer_id)){
					customerId = "";
				}
				if(StringUtils.isBlank(sp_id)){
					spId = "";
				}
				if(StringUtils.isBlank(fin_item)){
					finItem = "";
				}
				if(StringUtils.isBlank(flag)){
					flag_condition = "";
				}
				if(StringUtils.isBlank(order_export_date_begin_time)||StringUtils.isBlank(order_export_date_end_time)){
					order_export_date = "";
				}else{
					export = order_export_date_begin_time+"~"+order_export_date_end_time;
				}
				
				String condition = orderNo+customerId+spId+finItem+flag_condition+order_export_date;
				
				String sql = "select * ,if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from arap_charge_order aco "
						+" 						LEFT JOIN arap_charge_item aci on aco.id=aci.charge_order_id "
						+" 						WHERE aci.ref_order_id=B.id "
						+" 		),(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from arap_cost_order aco  "
						+" 						LEFT JOIN arap_cost_item aci on aco.id=aci.cost_order_id "
						+" 					WHERE aci.ref_order_id=B.id "
						+" 						) "
						+" )check_order_no, "
						+" if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from arap_charge_order aco  "
						+" 							LEFT JOIN arap_charge_item aci on aco.id=aci.charge_order_id "
						+" 							WHERE aci.ref_order_id=B.id "
						+" 			),(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from arap_cost_order aco  "
						+" 							LEFT JOIN arap_cost_item aci on aco.id=aci.cost_order_id "
						+" 						WHERE aci.ref_order_id=B.id "
						+" 				) "
						+" )check_order_id ,"
						+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
						+" 		FROM	arap_charge_application_order acao "
						+" 		LEFT JOIN charge_application_order_rel caor ON acao.id = caor.application_order_id "
						+" 		WHERE		caor.job_order_arap_id = B.id "
						+" 	),	(	SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
						+" 		FROM	arap_cost_application_order acao "
						+" 		LEFT JOIN cost_application_order_rel caor ON acao.id = caor.application_order_id  "
						+" 		WHERE	caor.job_order_arap_id = B.id "
						+" 	) "
						+" ) application_order_no, "
						+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
						+" 		FROM	 charge_application_order_rel caor  "
						+" 		WHERE		caor.job_order_arap_id = B.id "
						+" 	), "
						+" 	(SELECT		GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
						+" 		FROM  cost_application_order_rel caor  "
						+" 		WHERE	caor.job_order_arap_id = B.id "
						+" 	) "
						+" ) application_order_id "
	        		+ " from(  "
	         		+ " select joa.id,joa.type,(CASE WHEN joa.order_type = 'charge' THEN '应收费用' WHEN joa.order_type = 'cost' THEN '应付费用' END) order_type,"
	         		+ " joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.charge_id fin_item_id,"
	         		+ " (CASE WHEN joa.pay_flag='Y' THEN '费用已结算' WHEN joa.create_flag='Y' THEN '费用已创建申请单' WHEN joa.billConfirm_flag='Y' THEN '费用已创建对账单并已确认'" 
	         		+ " WHEN joa.bill_flag='Y' THEN '费用已创建对账单' WHEN joa.audit_flag='Y' THEN '费用已确认' ELSE '费用未确认' end) flag,"
	         		+ " joa.exchange_rate,joa.exchange_currency_rate,ifnull(joa.currency_total_amount,0) currency_total_amount,ifnull(joa.exchange_total_amount,0) exchange_total_amount,"
	         		+ " jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id, "
	         		+ " p.abbr sp_name,p1.abbr customer_name, "
	         		+ " f.name fin_name,cur1.name exchange_currency_name, "
	         		+ " cur.name currency_name "
	 				+ " from job_order_arap joa "
	 				+ " left join job_order jo on jo.id=joa.order_id "
	 				+ " left join party p on p.id=joa.sp_id "
	 				+ " left join party p1 on p1.id=jo.customer_id "
	 				+ " left join currency cur on cur.id=joa.currency_id "
	 				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	 				+ " left join fin_item f on f.id=joa.charge_id "
	 				+ " where  jo.office_id = "+office_id
	 				+ " and jo.delete_flag = 'N'"
					+ " GROUP BY joa.id "
	 				+ " ) B where 1=1 ";
				String sqlExport = sql+condition;
				
				String total_name_header = "工作单号,出货时间,客户,结算公司,费用类型,状态,类型,费用名称,货币类型,金额,对(CNY)汇率,"
                                          +"金额（CNY）,结算币制,结算汇率,结算金额,对账单号,申请单号,创建时间";
				String[] headers = total_name_header.split(",");
				
				
				String[] fields = {"ORDER_NO", "ORDER_EXPORT_DATE", "CUSTOMER_NAME", "SP_NAME", "ORDER_TYPE", "FLAG", "TYPE", "FIN_NAME", "CURRENCY_NAME", 
									"TOTAL_AMOUNT", "EXCHANGE_RATE", "CURRENCY_TOTAL_AMOUNT","EXCHANGE_CURRENCY_NAME","EXCHANGE_CURRENCY_RATE", "EXCHANGE_TOTAL_AMOUNT", "CHECK_ORDER_NO", "APPLICATION_ORDER_NO","CREATE_STAMP"};
				String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,export);
				renderText(fileName);
			} 
	
}
