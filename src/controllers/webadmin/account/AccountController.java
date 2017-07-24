package controllers.webadmin.account;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountController extends Controller {

	private Logger logger = Logger.getLogger(AccountController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		  Long user_id = LoginUserController.getLoginUserId(this);
		  String sql="select * from user_login where id="+user_id;
		  Record user=Db.findFirst(sql);
		  setAttr("user", user);
		render(getRequest().getRequestURI()+"/edit.html");
	}
	public void update(){
		 Long user_id = LoginUserController.getLoginUserId(this);
		String user_name=getPara("user_name");
		String password=getPara("password");
		String sql="update user_login set user_name=?,password=? where id=?";
		Db.update(sql, user_name,password,user_id);
		renderJson(true);
	}
	
	
}
