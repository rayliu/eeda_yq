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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class YqDashBoardController extends Controller {

	private Logger logger = Logger.getLogger(YqDashBoardController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
	  //公告
        UserLogin u = LoginUserController.getLoginUser(this);
        long office_id=u.getLong("office_id");
        String sql = "select m.id, m.create_stamp, u.c_name,"
                + " (case when length(m.title)>50 then CONCAT(substr(m.title,1,70),'....') else m.title end) title,"
                + " (case when length(m.content)>50 then CONCAT(substr(m.content,1,75),'....') else m.content end) content"
                + " from msg_board m "
                + " left join user_login u on u.id = m.creator "
                + " WHERE m.office_id="+office_id
                + " order by create_stamp desc"
                + " LIMIT 0,8 ";
        setAttr("msgBoardInfo",Db.find(sql));
	    render("/eeda/home.html");
	}
	
    @Before(Tx.class)
   	public void save() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	UserLogin user = LoginUserController.getLoginUser(this);
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
    
    @Before(Tx.class)
    public void saveOfMsgBoard() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	Record r= new Record();
    	r.set("title", title);
    	r.set("content", content);
    	r.set("office_id", office_id);
    	r.set("create_stamp", new Date());
    	r.set("creator", LoginUserController.getLoginUserId(this));
    	Db.save("msg_board", r);
    	redirect("/msgBoard");
    }
    
    public void list(){
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select * from ("
    			+ " select m.*, u.c_name create_name, u1.c_name update_name"
        		+ " from msg_board m "
        		+ " left join user_login u on u.id = m.creator"
        		+ " left join user_login u1 on u1.id = m.updator"
        		+ " where m.office_id="+office_id
        		+ " ) A where 1=1 ";
    	
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
