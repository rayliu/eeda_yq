package controllers.oms.todo;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
public class TodoController extends Controller {

	private Logger logger = Logger.getLogger(TodoController.class);
	Subject currentUser = SecurityUtils.getSubject();
	UserLogin user = LoginUserController.getLoginUser(this);
    long office_id=user.getLong("office_id");

    public void getYqTodoList(){
         String sql= "select ( "
            +"SELECT count(1) total FROM plan_order_item poi "
            +"    left join plan_order po on po.id = poi.order_id "
            +"    WHERE is_gen_job='N' AND factory_loading_time is not NULL "
            +"    AND datediff(factory_loading_time, now())<=5 and po.office_id="+office_id 
            +") planOrderTodoCount, "
            +"(SELECT COUNT(1) total "
            +"    FROM job_order jo LEFT JOIN job_order_shipment jos ON jo.id=jos.order_id "
            +"    WHERE jo.type='出口柜货' and jos.SONO is null and jo.transport_type LIKE '%ocean%' "
            +"    and jo.office_id ="+office_id 
            +") SOTodoCount, "
            +"( "
            +"    SELECT count(1) total "
            +"        FROM job_order_land_item joli LEFT JOIN job_order jo on jo.id = joli.order_id "
            +"        WHERE datediff(eta,now() )<=3 and (truckorder_flag != 'Y' OR truckorder_flag is null) "
            +"        and transport_type LIKE '%land%' and jo.office_id ="+office_id 
            +") TruckOrderTodoCount, "
            +"( "
            +"    SELECT count(1) total "
            +"        FROM job_order_shipment jos  "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE TO_DAYS(jos.export_date)=TO_DAYS(now()) and (jos.si_flag != 'Y' or jos.si_flag is null) "
            +"        and jo.office_id ="+office_id 
            +") SITodoCount, "
            +"( "
            +"    SELECT COUNT(1) total "
            +"        FROM job_order_shipment jos "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null) "
            + "and jo.office_id ="+office_id 
            +") MBLTodoCount, "
            +"( "
            +"    select count(1) total from plan_order por "
            +"        LEFT JOIN plan_order_item poi on poi.order_id = por.id "
            +"        where poi.customs_type = '自理报关' and poi.is_gen_job = 'N' and por.office_id="+office_id 
            +") WaitCustomTodoCountPlan, "
            +"( "
            +"    select count(total_count) total from (  "
            +"        select count(1) total_count from job_order jor  "
            +"        LEFT JOIN job_order_custom joc on joc.order_id = jor.id "
            +"        left join job_order_custom_china_self_item jocc on jocc.order_id = jor.id "
            +"        where jor.transport_type LIKE '%custom%' "
            +"        and isnull(joc.customs_broker) and isnull(jocc.custom_bank) and jor.office_id="+office_id 
            +"        group by jor.id) A "
            +") WaitCustomTodoCount, "
            +"( "
            +"    SELECT count(1) total  "
            +"        FROM job_order jo LEFT JOIN job_order_insurance joi ON jo.id = joi.order_id  "
            +"        WHERE transport_type LIKE '%insurance%' and joi.insure_no is NULL "
            +"        and jo.office_id="+office_id 
            +") WaitBuyInsuranceTodoCount, "
            +"( "
            +"    SELECT COUNT(1) total "
            +"        FROM job_order_shipment jos  "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE  (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y'  "
            +"        and timediff(now(),jos.etd)<TIME('48:00:00')  "
            +"        and jo.office_id="+office_id 
            +") WaitOverseaCustomTodoCount, "
            +"( "
            +"    SELECT count(1) total "
            +"       FROM job_order_shipment jos "
            +"       LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"       WHERE TO_DAYS(jos.etd)= TO_DAYS(now()) and (jos.in_line_flag != 'Y' or jos.in_line_flag is null) "
            +"       and jo.office_id="+office_id
            +") TlxOrderTodoCount; ";
         Record rec = Db.findFirst(sql);
         renderJson(rec);
    }
    
	
}
