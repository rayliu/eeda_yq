package controllers.webadmin.biz;

import freemarker.template.utility.StringUtil;
import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.aliyuncs.exceptions.ClientException;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.AliSmsUtil;
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
        String sql = "select cat.name trade_type_name,ul.invitation_code,loc.name location,ul.id uid,ul.user_name,ul.phone ,wc.* from user_login ul "
        			+ "left join wc_company wc on wc.creator = ul.id  "
        			+ "left join category cat on wc.trade_type = cat.id "
        			+ "left join location loc on loc.code = wc.city"
        			+ " where ul.id = "+id;
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
	public void pass() throws ClientException{
		String id = getPara("id");
		String status = getPara("status");
		boolean result = false;
		
		Record user = null;
		if(StringUtils.isNotBlank(id)){
			user = Db.findById("user_login", id);
			user.set("status", status);
			user.set("is_stop", 0);
			Db.update("user_login",user);
			result = true;
			//短信提醒用户
			sendMsg(user.getStr("phone"));
		}
		
		renderJson(result);
	}
	
	
	@Before(Tx.class)
	public void refuse() throws ClientException{
		String id = getPara("id");
		String reason = getPara("reason");
		boolean result = false;
		
		Record user = null;
		if(StringUtils.isNotBlank(id)){
			user = Db.findById("user_login", id);
			user.set("status", "已拒绝");
			user.set("refuse_reason", reason);
			user.set("is_stop", 0);
			Db.update("user_login",user);
			result = true;
			//短信提醒用户
			sendMsg(user.getStr("phone"));
		}
		renderJson(result);
	}
	
	@Before(Tx.class)
	private void sendMsg(String mobile) throws ClientException{
    	AliSmsUtil.sendSms(null, mobile,"sendMsg");//发送通知信息
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
    	String sql = "select ul.user_name,ca.name trade_type_name,ifnull(loc.name,'暂无') location,ul.id uid,ul.status,if(wc.user_type=2,'企业','个人') user_type_name,wc.* "
    				+ "from user_login ul  "
    				+ "LEFT JOIN wc_company wc on wc.creator = ul.id "
    				+" LEFT JOIN category ca on ca.id = wc.trade_type "
    				+" left join location loc on loc.code = ifnull(wc.city,wc.province) and loc.code <>''"
    				+" where ul.status != '通过' ";
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
