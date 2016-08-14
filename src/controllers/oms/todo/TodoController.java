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
		renderText("-1");
	}
	
	public void getTruckOrderTodoCount() {
		String sql = "SELECT count(1) total"
			    +" FROM plan_order_item "
			    + " WHERE is_gen_job='N' AND factory_loading_time is not NULL"
			    + " AND datediff(factory_loading_time, now())<=3";
		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		
		renderText(total);
	}
	
	public void getSITodoCount() {
		String sql = "SELECT count(1) total"
				    +" FROM job_order_shipment "
				    +" WHERE TO_DAYS(export_date)=TO_DAYS(now())";
		
		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		renderText(total);
	}

	public void getMBLTodoCount() {
		renderText("-1");
	}

	public void getWaitCustomTodoCount() {
		String sql = "SELECT count(1) total"
	               + " FROM job_order jo LEFT JOIN  job_order_custom joc ON jo.id=joc.order_id" 
	               + " WHERE transport_type like '%custom%' and joc.customs_broker is null" ;

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();

		renderText(total);
	}

	public void getWaitBuyInsuranceTodoCount() {
		String sql = "SELECT count(1) total"
	               + " FROM job_order " 
	               + " WHERE transport_type like '%insurance%'";

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();

		renderText(total);
	}

	public void getWaitOverseaCustomTodoCount() {
		renderText("-1");
	}

	public void getTlxOrderTodoCount() {
		String sql = "SELECT count(1) total"
	               + " FROM job_order_shipment "
				   + " WHERE TO_DAYS(etd)= TO_DAYS(now())";

		Record planOrder = Db.findFirst(sql);
		String total = planOrder.getLong("TOTAL").toString();
		renderText(total);

	}
	
}
