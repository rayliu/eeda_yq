package controllers.oms.todo;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TodoController extends Controller {

	private Logger logger = Logger.getLogger(TodoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void getPlanOrderTodoCount() {
		String sql = "SELECT count(1) total"
				    +" FROM plan_order_item "
				    + " WHERE is_gen_job='N' AND factory_loading_time is not NULL"
				    + " AND datediff(factory_loading_time, now())<=5";
		
		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		
		renderText(total);
	}
	
	public void getSOTodoCount() {
		String sql= " SELECT COUNT(1) total"
				+ " FROM job_order jo LEFT JOIN job_order_shipment jos ON jo.id=jos.order_id"
				+ " WHERE jo.type='出口柜货' and jos.SONO is null and jo.transport_type LIKE '%ocean%'";
		Record jobOrder=Db.findFirst(sql);
		String total = jobOrder.getLong("TOTAL").toString();
				renderText(total);
	}
	
	public void getTruckOrderTodoCount() {
		String sql = "SELECT count(1) total"
				+ " FROM job_order_land_item joli LEFT JOIN job_order jo on jo.id = joli.order_id"
				+ " WHERE datediff(eta,now() )<=3 and (truckorder_flag != 'Y' OR truckorder_flag is null) "
				+ "and transport_type LIKE '%land%'";
		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		
		renderText(total);
	}
	
	public void getSITodoCount() {
		String sql = "SELECT count(1) total"
				    +" FROM job_order_shipment "
				    +" WHERE TO_DAYS(export_date)=TO_DAYS(now()) and (si_flag != 'Y' or si_flag is null)";
		
		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		renderText(total);
	}

	public void getMBLTodoCount() {
		String sql = " SELECT COUNT(1) total,jos.si_flag,jos.mbl_flag"
				+ " FROM job_order_shipment jos"
				+ " WHERE si_flag = 'Y' and (mbl_flag != 'Y' or mbl_flag is null)";
		Record jobOrder = Db.findFirst(sql);
		String total = jobOrder.getLong("TOTAL").toString();
		renderText(total);
	}

	public void getWaitCustomTodoCount() {
		String sql = "select count(0) from(select poi.id from plan_order por"
				+ " LEFT JOIN plan_order_item poi on poi.order_id = por.id"
				+ " where poi.customs_type = '自理报关' and poi.is_gen_job = 'N'"
				+ " GROUP BY poi.id"
				+ " UNION all"
				+ " select jor.id from job_order jor "
				+ " LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
				+ " where jor.transport_type LIKE '%custom%' "
				+ " and ifnull(joc.custom_type,'') = '') A";

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();

		renderText(total);
	}

	public void getWaitBuyInsuranceTodoCount() {
		String sql = "SELECT count(1) total "
				+ " FROM job_order jo LEFT JOIN job_order_insurance joi ON jo.id = joi.order_id "
				+ " WHERE transport_type LIKE '%insurance%' and joi.insure_no is NULL";

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();

		renderText(total);
	}

	public void getWaitOverseaCustomTodoCount() {
		String sql = " SELECT COUNT(1) total"
				+ " FROM job_order_shipment "
				+ " WHERE  (afr_ams_flag !='Y' OR afr_ams_flag is  NULL) and wait_overseaCustom = 'Y' and timediff(now(),etd)<TIME('48:00:00') ";
		Record jobOrderShipment = Db.findFirst(sql);
        String total = jobOrderShipment.getLong("TOTAL").toString();
		renderText(total);
	}

	public void getTlxOrderTodoCount() {
		String sql = "SELECT count(1) total"
	               + " FROM job_order_shipment "
				   + " WHERE TO_DAYS(etd)= TO_DAYS(now()) and (in_line_flag != 'Y' or in_line_flag is null)";

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		renderText(total);

	}
	
}
