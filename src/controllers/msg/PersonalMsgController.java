package controllers.msg;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PersonalMsgController extends Controller {

	private Logger logger = Logger.getLogger(PersonalMsgController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
	    UserLogin user = LoginUserController.getLoginUser(this);
	    if(user == null)
   			return;
	    long user_id = user.getLong("id");
	    
	    List<Record> configList = ListConfigController.getConfig(user_id, "/personalMsg");
	    
	    setAttr("listConfigList", configList);
		render("/eeda/personalMsg/list.html");
	}
	
    @Before(Tx.class)
   	public void save() {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        long office_id=user.getLong("office_id");
    	Record r= new Record();
        r.set("title", title);
        r.set("content", content);
        r.set("office_id", office_id);
        r.set("create_stamp", new Date());
        r.set("creator", LoginUserController.getLoginUserId(this));
        Db.save("msg_board", r);
        redirect("/");
   	}
  
    public void list(){
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select * from personal_msg where office_id="+office_id;
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    public void edit(){
    	String id = getPara("edit_id");
    	String title = getPara("edit_radioTitle");
    	String content = getPara("edit_radioContent");
    	Record r= Db.findById("msg_board", id);
    	r.set("title", title);
    	r.set("content", content);
    	r.set("update_stamp", new Date());
    	r.set("updator", LoginUserController.getLoginUserId(this));
    	Db.update("msg_board", r);
    	redirect("/msgBoard");
    }

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
