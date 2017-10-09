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
			+"(select count(1) total from(SELECT jo.id "
			+"  FROM job_order jo"
			+ " LEFT JOIN job_order_shipment jos ON jo.id=jos.order_id "
			+"  WHERE jo.type='出口柜货' and jos.SONO is null"
			+ " and jo.transport_type LIKE '%ocean%' "
			+"  and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+ " group by jo.id"
			+") A) SOTodoCount, "
			+"(select count(1) total from(SELECT jo.id "
			+"  FROM job_order jo"
			+ " LEFT JOIN job_order_shipment jos  on jo.id = jos.order_id "
			+"  WHERE"
			+ " jo.type='出口柜货'"
			+ " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
			+ " and jos.por = 3732"
			+ " and datediff(jo.order_export_date,now() )<=3"
			+ " and jos.aboutShipment_flag != 'Y'  "
			+"  and jo.transport_type LIKE '%ocean%'"
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+ " group by jo.id"
			+") A) WaitAboutShipmentTodoCount, "
			//待派车
			+"(select count(1) total from(SELECT jo.id "
			+ " from job_order jo  "
			+"  WHERE "
			+ " datediff(jo.order_export_date,now() )<=3"
			+ " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
			+ " and jo.send_truckorder_flag != 'Y'  "
			+"  and jo.transport_type LIKE '%land%'"
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+ " group by jo.id"
			+") A) TruckOrderTodoCount, "
			//头程资料
			+"(select count(1) total from(SELECT jo.id "
			+ " from job_order jo "
			+"  WHERE "
			+ " datediff(jo.order_export_date,now() )<=1"
			+ " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
			+ " and jo.print_shipmentHead_flag != 'Y' "
			+ " and jo.transport_type LIKE '%ocean%' "
			+ " and (jo.office_id ="+office_id+ref_office+")"
			+ " and jo.delete_flag = 'N'"
			+ " group by jo.id"
			+") A) waitShipmentHeadTodoCount, "
			//VGM
			+"(select count(1) total from(SELECT jo.id "
            +"        FROM job_order jo"
            +"        LEFT JOIN job_order_shipment jos   on jo.id = jos.order_id "
            +"        WHERE"
            + " datediff(jo.order_export_date, now()) <= 0 and ifnull(jos.vgm,'') = '' "
            + " and datediff(jo.order_export_date,'2017-10-08') > 0"  //统计2017-10-08后的数据，之前的忽略
            + " and jo.transport_type LIKE '%ocean%'"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) VGMTodoCount, "
            //HBL
            +"(select count(1) total from(SELECT jo.id "
            +"        FROM job_order jo"
            +"        LEFT JOIN job_order_shipment jos   on jo.id = jos.order_id "
            +"        WHERE "
            + " ifnull(jos.hbl_flag,'') != 'Y' "
            + " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
            + " and jo.transport_type LIKE '%ocean%'"
            + " and datediff(jo.order_export_date, now()) <= 0"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) HBLTodoCount, "
            //MBL
			+"(select count(1) total from(SELECT jo.id "
            +"        FROM job_order jo"
            +"        LEFT JOIN job_order_shipment jos   on jo.id = jos.order_id "
            +"        WHERE "
            //+ " jos.si_flag = 'Y' and ifnull(jos.mbl_flag,'') != 'Y' "
            + " ifnull(jos.mbl_flag,'') != 'Y' "
            + " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
            + " and datediff(jo.order_export_date, now()) <= 0"
            + " and jo.transport_type LIKE '%ocean%'"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) MBLTodoCount, "
           
            //保险
			+"(select count(1) total from(SELECT jo.id "
            +"        FROM job_order jo "
            + " LEFT JOIN job_order_insurance joi ON joi.order_id = jo.id  "
            +"  WHERE "
            + " datediff(jo.order_export_date, now()) <= 0 "
            + " and datediff(jo.order_export_date,'2017-10-08') > 0"  //统计2017-10-08后的数据，之前的忽略
            + " and  jo.transport_type LIKE '%insurance%'"
            + " and joi.insure_no is NULL "
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) WaitBuyInsuranceTodoCount, "
            
			+"(select count(1) total from(SELECT jo.id "
            +"       FROM job_order jo"
            +"        LEFT JOIN job_order_shipment jos   on jo.id = jos.order_id "
            +"        where"
            + "  (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y'  "
            + " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
            +"  and timediff(now(),jos.etd)<TIME('48:00:00')  "
            + " and jo.transport_type LIKE '%ocean%'"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) WaitOverseaCustomTodoCount, "
			 +"(select count(1) total from(SELECT jo.id "
            +"       FROM job_order jo"
            +"        LEFT JOIN job_order_shipment jos   on jo.id = jos.order_id "
            +"       WHERE"
            + " datediff(jos.etd, now()) <= 0"
            + " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
            + " and ifnull(jos.in_line_flag,'') != 'Y' "
            + " and jo.transport_type LIKE '%ocean%'"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            + " group by jo.id"
            +") A) TlxOrderTodoCount,"
			 //报关
            +"( "
            +"    select count(1) total from (  "
            +"        select jo.id from job_order jo  "
            +"        LEFT JOIN job_order_custom joc on joc.order_id = jo.id "
            +"        left join job_order_custom_china_self_item jocc on jocc.order_id = jo.id "
            +"        where jo.transport_type LIKE '%custom%'"
            + " and datediff(jo.order_export_date,'2017-01-01') > 0"  //统计2017后的数据，之前的忽略
            + " and ifnull((select group_concat(jcc.id) jccId from job_order_custom_china_self_item jcc where jcc.order_id = jo.id),'') = ''"
            + " and ifnull((select group_concat(cpo.id) from custom_plan_order cpo where cpo.ref_job_order_id = jo.id),'') = ''"
            + " and ifnull((SELECT group_concat(custom_order_no) FROM job_order_custom where custom_type = 'abroad' and order_id = jo.id),'') = ''"
            + " and ifnull((SELECT group_concat(custom_order_no) FROM job_order_custom where custom_type = 'HK/MAC' and order_id = jo.id),'') = ''"
            +"  and (jo.office_id ="+office_id+ref_office+")"
            + " and jo.delete_flag = 'N'"
            +"  group by jo.id) A "
            +") WaitCustomTodoCount; ";
         Record rec = Db.findFirst(sql);
         renderJson(rec);
    }
    
	
}
