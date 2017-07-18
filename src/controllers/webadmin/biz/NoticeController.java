package controllers.webadmin.biz;

import java.text.SimpleDateFormat;
import java.util.Date;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class NoticeController extends Controller {

	private Logger logger = Logger.getLogger(NoticeController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	@Before(Tx.class)
	public void update(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String content=getPara("content");
		String sql="update price_maintain set remark= '"+content+"' , update_time= '"+df.format(new Date())+"' where type='公共信息'";
		Db.update(sql);
		renderJson(true);
	}
	
}
