package controllers.report;

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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OrderStatusController extends Controller {
    private Log logger = Log.getLog(OrderStatusController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/orderStatus");
		setAttr("listConfigList", configList);
		render("eeda/statusReport/orderStatus.html");
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
        
        String customer_id = getPara("customer");        
        Long userId = LoginUserController.getLoginUserId(this);
        if(StringUtils.isNotEmpty(getPara("customer"))){
    		//常用party查询保存进入历史记录
          	JobOrderController.addHistoryRecord(userId,customer_id,"ARAP_COM");
    	}
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jor.office_id in ("+relist.getStr("office_id")+")";
        }
        
    	String sql = "select * from (SELECT jor.id,p.abbr customer_name,jor.customer_id,"
    			+ " ( "
    			+ " SELECT CONCAT(cast(por.id as char),':',por.order_no,'-',por.status)"
    			+ " FROM  plan_order por"
    			+ " WHERE"
    			+ " por.id = jor.plan_order_id"
    			+ " ) plan_order_no,"
    			+ " CONCAT(cast(jor.id as char),':',jor.order_no,'-',jor.status) job_order_no,"
    			+ " ( "
    			+ " SELECT GROUP_CONCAT(distinct CONCAT(cast(ach.id as char),':',ach.order_no,'-',ach.status) SEPARATOR '<br/>')"
    			+ " FROM arap_charge_order ach"
    			+ " LEFT JOIN arap_charge_item aci on aci.charge_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.order_id = jor.id"
    			+ " ) charge_check_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(distinct CONCAT(cast(ach.id as char),':',ach.order_no,'-',ach.status) SEPARATOR '<br/>')"
    			+ " FROM arap_cost_order ach"
    			+ " LEFT JOIN arap_cost_item aci on aci.cost_order_id = ach.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = aci.ref_order_id"
    			+ " WHERE joa.order_id = jor.id"
    			+ " ) cost_check_no,"
    			+ " ("
    			+ " SELECT GROUP_CONCAT(distinct CONCAT(cast(app.id as char),':',app.order_no,'-',app.status) SEPARATOR '<br/>')"
    			+ " FROM arap_charge_application_order app"
    			+ " LEFT JOIN charge_application_order_rel relf on relf.application_order_id = app.id"
    			+ " LEFT JOIN job_order_arap joa on joa.id = relf.job_order_arap_id"
    			+ " WHERE joa.order_id = jor.id"
    			+ " ) charge_app_no,"
    			+ " GROUP_CONCAT(DISTINCT CONCAT(cast(app_cost.id AS CHAR),':',app_cost.order_no,'-',app_cost. STATUS) SEPARATOR '<br/>') cost_app_no"
    			+ " from job_order jor"
    			+ " LEFT JOIN party p on p.id = jor.customer_id"
    			+ " LEFT JOIN job_order_arap joa ON joa.order_id = jor.id"
    			+ " LEFT JOIN cost_application_order_rel relf_cost ON relf_cost.job_order_arap_id = joa.id"
    			+ " LEFT JOIN arap_cost_application_order app_cost ON app_cost.id = relf_cost.application_order_id"
    			+ " where (jor.office_id = "+office_id + ref_office+")"
    			 + " and jor.delete_flag = 'N' GROUP BY id"
 				+") a where 1 = 1";
    	
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
