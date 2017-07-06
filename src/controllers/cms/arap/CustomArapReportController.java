package controllers.cms.arap;

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
public class CustomArapReportController extends Controller {
	private Log logger = Log.getLog(CustomArapReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/arapReport");
		setAttr("listConfigList", configList);
		render("/cms/arap/ArapReport/ArapReport.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * ,if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from custom_arap_charge_order aco "
					+" 						LEFT JOIN custom_arap_charge_item aci on aco.id=aci.custom_charge_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 		),(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from custom_arap_cost_order aco  "
					+" 						LEFT JOIN custom_arap_cost_item aci on aco.id=aci.custom_cost_order_id "
					+" 					WHERE aci.ref_order_id=B.id "
					+" 						) "
					+" )check_order_no, "
					+" if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from custom_arap_charge_order aco  "
					+" 							LEFT JOIN custom_arap_charge_item aci on aco.id=aci.custom_charge_order_id "
					+" 							WHERE aci.ref_order_id=B.id "
					+" 			),(SELECT GROUP_CONCAT(aco.id  SEPARATOR ',') from custom_arap_cost_order aco  "
					+" 							LEFT JOIN custom_arap_cost_item aci on aco.id=aci.custom_cost_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 				) "
					+" )check_order_id ,"
					+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
					+" 		FROM	custom_arap_charge_application_order acao "
					+" 		LEFT JOIN custom_charge_application_order_rel caor ON acao.id = caor.application_order_id "
					+" 		WHERE		caor.job_order_arap_id = B.id "
					+" 	),	(	SELECT	GROUP_CONCAT(acao.order_no SEPARATOR ',') "
					+" 		FROM	custom_arap_cost_application_order acao "
					+" 		LEFT JOIN custom_cost_application_order_rel caor ON acao.id = caor.application_order_id  "
					+" 		WHERE	caor.job_order_arap_id = B.id "
					+" 	) "
					+" ) application_order_no, "
					+" IF (	B.order_type = 'charge',(SELECT	GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
					+" 		FROM	 custom_charge_application_order_rel caor  "
					+" 		WHERE		caor.job_order_arap_id = B.id "
					+" 	), "
					+" 	(SELECT		GROUP_CONCAT(caor.application_order_id SEPARATOR ',') "
					+" 		FROM  custom_cost_application_order_rel caor  "
					+" 		WHERE	caor.job_order_arap_id = B.id "
					+" 	) "
					+" ) application_order_id "
        		+ " from(  "
         		+ " select  joa.id,joa.price,joa.amount,jo.type,joa.order_type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.charge_id fin_item_id,"
         		+" (CASE WHEN joa.pay_flag='Y' THEN '费用已结算' WHEN joa.create_flag='Y' THEN '费用已创建申请单' WHEN joa.billConfirm_flag='Y' THEN '费用已创建对账单并已确认'" 
         		+" WHEN joa.bill_flag='Y' THEN '费用已创建对账单' WHEN joa.audit_flag='Y' THEN '费用已确认' ELSE '费用未确认' end) flag, "
         		+ " joa.exchange_rate, "
         		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.carrier, "
         		+ " p.abbr sp_name,p1.abbr customer_name, "
         		+ " f.name fin_name, "
         		+ " cur.name currency_name "
         		+ " from custom_plan_order_arap joa "
 				+ " left join custom_plan_order jo on jo.id=joa.order_id "
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.carrier "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join fin_item f on f.id=joa.charge_id "
 				+ " where  jo.office_id = "+office_id
 				+ " and jo.delete_flag = 'N'"
				+ " GROUP BY joa.id "
 				+ " ) B where 1=1 ";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}