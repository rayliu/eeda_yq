package controllers.oms.todo;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

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
    	 String ref_office = "";
         Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
         if(relist!=null){
         	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
         }
         String sql= ""
         	+ "select "
//         	+ "( "
//            +" SELECT count(1) total FROM (select po.id from plan_order_item poi "
//            +"    left join plan_order po on po.id = poi.order_id "
//            +"    WHERE"
//            + " 'N' in (select group_concat(confirm_shipment) from plan_order_item where order_id = po.id) "
//            +"    AND datediff(factory_loading_time, now())<=5  and "
//            + " (po.office_id="+office_id + " or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y')) and po.delete_flag = 'N'"
//            + " group by po.id ) A"
//            +") planOrderTodoCount, "
//			+"( "
//			+"    SELECT COUNT(1) total "
//			+"        FROM job_order_shipment jos "
//			+"        LEFT JOIN job_order jo on jo.id = jos.order_id "
//			+"        WHERE jos.si_flag = 'Y' and (jos.hbl_flag != 'Y' or jos.hbl_flag is null) "
//			+"    and (jo.office_id ="+office_id+ref_office+")"
//			+ " and jo.delete_flag = 'N'"
//			+") HBLTodoCount, "
			+"(SELECT COUNT(1) total "
			+"    FROM job_order jo LEFT JOIN job_order_shipment jos ON jo.id=jos.order_id "
			+"    WHERE jo.type='出口柜货' and jos.SONO is null and jo.transport_type LIKE '%ocean%' "
			+"    and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+") SOTodoCount, "
			+"( "
			+"    SELECT "
			+ " count(1) total "
			+"  FROM job_order_shipment jos "
			+ " LEFT JOIN job_order jo on jo.id = jos.order_id "
			+"  WHERE datediff(jo.order_export_date,now() )<=3"
			+ " and jos.aboutShipment_flag != 'Y'  "
			+"  and jo.transport_type LIKE '%ocean%'"
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+") WaitAboutShipmentTodoCount, "
			//待派车
			+" ( SELECT "
			+ " count(1) total "
			+ " from job_order jo  "
			+"  WHERE datediff(jo.order_export_date,now() )<=3"
			+ " and jo.send_truckorder_flag != 'Y'  "
			+"  and jo.transport_type LIKE '%land%'"
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+") TruckOrderTodoCount, "
			//头程资料
			+" ( SELECT "
			+ " count(1) total "
			+ " from job_order jo "
			+"  WHERE datediff(jo.order_export_date,now() )<=1"
			+ " and jo.print_shipmentHead_flag != 'Y'  "
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+") waitShipmentHeadTodoCount, "
			//VGM
            +"( "
            +"    SELECT count(1) total "
            +"        FROM job_order_shipment jos  "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE datediff(jo.order_export_date, now()) = 0 and ifnull(jos.vgm,'') = '' "
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            +") VGMTodoCount, "
            //HBL
            +"( "
            +"    SELECT COUNT(1) total "
            +"        FROM job_order_shipment jos "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE "
            + " (ifnull(jos.hbl_flag,'') != 'Y') "
            + " and datediff(jo.order_export_date, now()) = 0"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
			+") HBLTodoCount, "
            //MBL
            +"( "
            +"    SELECT COUNT(1) total "
            +"        FROM job_order_shipment jos "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE "
            + " jos.si_flag = 'Y' and ifnull(jos.mbl_flag,'') != 'Y' "
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
			+") MBLTodoCount, "
            //报关
            +"( "
            +"    select count(total_count) total from (  "
            +"        select count(1) total_count from job_order jo  "
            +"        LEFT JOIN job_order_custom joc on joc.order_id = jo.id "
            +"        left join job_order_custom_china_self_item jocc on jocc.order_id = jo.id "
            +"        where jo.transport_type LIKE '%custom%'"
            + " and (select group_concat(jcc.id) jccId from job_order_custom_china_self_item jcc where jcc.order_id = jo.id) is null "
            +"    and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            +"        group by jo.id) A "
            +") WaitCustomTodoCount, "
            //保险
            +"( "
            +"    SELECT count(1) total  "
            +"        FROM job_order jo "
            + " LEFT JOIN job_order_insurance joi ON jo.id = joi.order_id  "
            +"  WHERE transport_type LIKE '%insurance%' and joi.insure_no is NULL "
            + " and datediff(jo.order_export_date, now()) = 0 "
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
			+") WaitBuyInsuranceTodoCount, "
            
            +"( "
            +"    SELECT COUNT(1) total "
            +"        FROM job_order_shipment jos  "
            +"        LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"        WHERE  (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y'  "
            +"        and timediff(now(),jos.etd)<TIME('48:00:00')  "
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
			 +") WaitOverseaCustomTodoCount, "
            +"( "
            +"    SELECT count(1) total "
            +"       FROM job_order_shipment jos "
            +"       LEFT JOIN job_order jo on jo.id = jos.order_id "
            +"       WHERE TO_DAYS(jos.etd)= TO_DAYS(now()) and (jos.in_line_flag != 'Y' or jos.in_line_flag is null) "
            +"    and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
			 +") TlxOrderTodoCount; ";
         Record rec = Db.findFirst(sql);
         renderJson(rec);
    }
    
	
}
