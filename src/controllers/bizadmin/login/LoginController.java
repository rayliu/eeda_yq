package controllers.bizadmin.login;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;


public class LoginController extends Controller {

	private Logger logger = Logger.getLogger(LoginController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/login.html");
	}
    
	public void login(){
	    render(getRequest().getRequestURI()+"/login.html");
	}
}
