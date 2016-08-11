package controllers.oms.todo;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TodoController extends Controller {

	private Logger logger = Logger.getLogger(TodoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void getPlanOrderTodoCount() {
		renderText("-1");
	}
	
	public void getSOTodoCount() {
		renderText("-1");
	}
	
	public void getTruckOrderTodoCount() {
		renderText("-1");
	}
	
	public void getSITodoCount() {
		renderText("-1");
	}
	
	public void getMBLTodoCount() {
		renderText("-1");
	}
	
	public void getWaitCustomTodoCount() {
		renderText("-1");
	}
	
	public void getWaitBuyInsuranceTodoCount() {
		renderText("-1");
	}
	
	public void getWaitOverseaCustomTodoCount() {
		renderText("-1");
	}
	
	public void getTlxOrderTodoCount() {
		renderText("-1");
	}
	
	
}
