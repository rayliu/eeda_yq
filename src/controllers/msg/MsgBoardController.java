package controllers.msg;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.List;

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
public class MsgBoardController extends Controller {

	private Logger logger = Logger.getLogger(MsgBoardController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		String sql = "select m.*,u.c_name from msg_board m left join user_login u on u.id = m.creator";
		setAttr("msgBoardInfo",Db.find(sql));
		render("/eeda/msgBoard/msgBoardList.html");
	}
	
    @Before(Tx.class)
   	public void save() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	Record r= new Record();
        r.set("title", title);
        r.set("content", content);
        r.set("create_stamp", new Date());
        r.set("creator", LoginUserController.getLoginUserId(this));
        Db.save("msg_board", r);
        redirect("/");
   	}
    
    public void edit(){
    	
    }

}
