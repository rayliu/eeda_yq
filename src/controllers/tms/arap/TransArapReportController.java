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
public class TransArapReportController extends Controller {
	private Log logger = Log.getLog(TransArapReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/transArapReport");
		setAttr("listConfigList", configList);
		render("/tms/arap/ArapReport/ArapReport.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * ,if(B.order_type='charge',(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from trans_arap_charge_order aco "
					+" 						LEFT JOIN trans_arap_charge_item aci on aco.id=aci.charge_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 		),(SELECT GROUP_CONCAT(aco.order_no SEPARATOR ',') from trans_arap_cost_order aco  "
					+" 						LEFT JOIN trans_arap_cost_item aci on aco.id=aci.cost_order_id "
					+" 					WHERE aci.ref_order_id=B.id "
					+" 						) "
					+" )check_order_no, "
					+" if(B.order_type='charge',(SELECT CAST(GROUP_CONCAT(aco.id SEPARATOR ',') as char ) from trans_arap_charge_order aco  "
					+" 							LEFT JOIN trans_arap_charge_item aci on aco.id=aci.charge_order_id "
					+" 							WHERE aci.ref_order_id=B.id "
					+" 			),(SELECT CAST(GROUP_CONCAT(aco.id SEPARATOR ',') as char ) from trans_arap_cost_order aco  "
					+" 							LEFT JOIN trans_arap_cost_item aci on aco.id=aci.cost_order_id "
					+" 						WHERE aci.ref_order_id=B.id "
					+" 				) "
					+" )check_order_id "
        		+ " from(  "
         		+ " select joa.id,joa.type,joa.order_type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.charge_id fin_item_id,"
         		+" (CASE WHEN joa.billConfirm_flag='Y' THEN '费用已创建对账单并已确认'" 
         		+" WHEN joa.bill_flag='Y' THEN '费用已创建对账单' WHEN joa.audit_flag='Y' THEN '费用已确认' ELSE '费用未确认' end) flag,"
         		+ " joa.exchange_rate,joa.exchange_currency_rate,ifnull(joa.currency_total_amount,0) currency_total_amount,ifnull(joa.exchange_total_amount,0) exchange_total_amount,"
         		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.customer_id, "
         		+ " p.abbr sp_name,p1.abbr customer_name, "
         		+ " f.name fin_name,cur1.name exchange_currency_name, "
				+ " CONVERT(substring(tjol.cabinet_date,1,10),char) cabinet_date,"
         		+ " cur.name currency_name "
 				+ " from trans_job_order_arap joa "
 				+ " left join trans_job_order jo on jo.id=joa.order_id "
				+ " LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = joa.order_id"
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.customer_id "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
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
