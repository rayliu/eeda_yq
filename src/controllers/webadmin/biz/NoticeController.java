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
		Record noticeRec = Db.findFirst("select * from wc_notice order by create_time desc;");
		 setAttr("notice", noticeRec);
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(Tx.class)
	public void update(){
		Long user_id = LoginUserController.getLoginUserId(this);
		String content = getPara("content");
		Record re = new Record();
		re.set("content", content);
		re.set("creator", user_id);
		re.set("create_time",new Date());
		Db.save("wc_notice", re);
		renderJson(true);
	}
	
}
