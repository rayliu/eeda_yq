package controllers.bizadmin.register;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;


public class RegisterController extends Controller {

	private Logger logger = Logger.getLogger(RegisterController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/index.html");
	}
    
	public void info(){
	    render(getRequest().getRequestURI()+"/index.html");
	}
	
	public void done(){
        render(getRequest().getRequestURI()+"/index.html");
    }
}
