package controllers.bizadmin.account;

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
	    Record noticeRec = Db.findFirst("select * from wc_notice order by id desc;");
	    setAttr("notice", noticeRec.get("content"));
	    
	    
		render(getRequest().getRequestURI()+"/list.html");
	}
	

	public void modify_info(){
        Long userId = LoginUserController.getLoginUserId(this);
        
        Record rec = Db.findFirst("select * from wc_company where creator=?", userId);
        setAttr("company", rec);
        
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void save_info(){
        Long userId = LoginUserController.getLoginUserId(this);
        
        Record rec = Db.findFirst("select * from wc_company where creator=?", userId);
        setAttr("company", rec);
        
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void modify_pwd(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
}
