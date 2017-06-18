package controllers;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;
import com.jfinal.log.Log;

public class IndexController extends Controller {
	private Log logger = Log.getLog(IndexController.class);
    Subject currentUser = SecurityUtils.getSubject();

   
    public void index() {
       render(getRequest().getRequestURI()+"/index.html");
    }

}
