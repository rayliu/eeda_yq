package controllers.webadmin.biz;

import freemarker.template.utility.StringUtil;
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
public class ReminderController extends Controller {

	private Logger logger = Logger.getLogger(ReminderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	

	public void edit(){
        String id = getPara("id");
        String sql = "select ul.id uid,ul.user_name,ul.phone ,wc.* from user_login ul "
        			+ "left join wc_company wc on wc.creator = ul.id  where ul.id = "+id;
        Record re=Db.findFirst(sql);
        setAttr("user",re);
        render(getRequest().getRequestURI()+".html");
    }
	
	@Before(Tx.class)
	public void delete(){
		String id = getPara("id");
		String sql_company = "delete from wc_company where creator = "+id;
		Db.update(sql_company);
		String sql_user = "delete from user_login where id = "+id;
		Db.update(sql_user);
		renderJson(true);
	}
	
	@Before(Tx.class)
	public void pass(){
		String id = getPara("id");
		String status = getPara("status");
		String sql="update user_login set status = '"+status+"' where id = "+id;
		Db.update(sql);
		renderJson(true);
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
    	String sql = "select ul.id uid,ul.status,wc.* from user_login ul  "
    			+ "LEFT JOIN wc_company wc on wc.creator = ul.id  where ul.status != '通过' ";
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ " order by create_time desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
   

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
