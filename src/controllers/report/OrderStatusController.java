package controllers.report;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OrderStatusController extends Controller {
    private Log logger = Log.getLog(OrderStatusController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	render("eeda/statusReport/orderStatus.html");
    }

    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
    	String sql = ""
    			+ " select * from(SELECT jor.id,p.abbr customer_name,jor.customer_id,"
    			+ " ( "
    			+ " SELECT CONCAT(cast(por.id as char),':',por.order_no,'-',por.status)"
    			+ " FROM  plan_order por"
    			+ " WHERE"
    			+ " por.id = jor.plan_order_id"
    			+ " ) plan_order_no,"
    			+ " CONCAT(cast(jor.id as char),':',jor.order_no,'-',jor.status) job_order_no,"
    			+ " ( "
    			+ " SELECT GROUP_CONCAT(CONCAT(cast(ach.id as char),':',ach.order_no,'-',ach.status) SEPARATOR '<br/>')"
    			+ " FROM arap_charge_order ach"
    			+ " LEFT JOIN arap_charge_item aci on aci.charge_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.id = jor.id"
    			+ " ) charge_check_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(CONCAT(cast(ach.id as char),':',ach.order_no,'-',ach.status) SEPARATOR '<br/>')"
    			+ " FROM arap_cost_order ach"
    			+ " LEFT JOIN arap_cost_item aci on aci.cost_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.id = jor.id"
    			+ " ) cost_check_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(CONCAT(cast(chin.id as char),':',chin.order_no,'-',chin.status) SEPARATOR '<br/>')"
    			+ " FROM arap_charge_invoice chin"
    			+ " LEFT JOIN arap_charge_order ach on ach.invoice_order_id = chin.id"
    			+ " LEFT JOIN arap_charge_item aci on aci.charge_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.id = jor.id"
    			+ " ) charge_nvoice_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(CONCAT(cast(app.id as char),':',app.order_no,'-',app.status) SEPARATOR '<br/>')"
    			+ " FROM arap_charge_application_order app"
    			+ " LEFT JOIN charge_application_order_rel relf on relf.application_order_id = app.id"
    			+ " LEFT JOIN arap_charge_invoice acin on acin.id = relf.charge_order_id and relf.order_type = '应收开票单'"
    			+ " LEFT JOIN arap_charge_order ach on (ach.id = relf.charge_order_id  and relf.order_type = '应收对账单') "
    			+ " or (ach.invoice_order_id = acin.id)"
    			+ " LEFT JOIN arap_charge_item aci on aci.charge_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.id = jor.id"
    			+ " ) charge_app_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(CONCAT(cast(app.id as char),':',app.order_no,'-',app.status) SEPARATOR '<br/>')"
    			+ " FROM arap_cost_application_order app"
    			+ " LEFT JOIN cost_application_order_rel relf on relf.application_order_id = app.id"
    			+ " LEFT JOIN arap_cost_order ach on ach.id = relf.cost_order_id"
    			+ " LEFT JOIN arap_cost_item aci on aci.cost_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.id = jor.id"
    			+ " ) cost_app_no"
    			+ " from job_order jor"
    			+ " LEFT JOIN party p on p.id = jor.customer_id) A where 1 = 1";
    	
    	String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
    }
    
    
}
