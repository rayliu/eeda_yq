package controllers.webadmin.data;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TaoManageController extends Controller {

	private Logger logger = Logger.getLogger(TaoManageController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	
}
