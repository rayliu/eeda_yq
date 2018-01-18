package controllers.webadmin.user;

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

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class UserController extends Controller {

	private Logger logger = Logger.getLogger(UserController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/userList.html");
	}
	
	public void edit() {
		String id = getPara("id");
		
	    Record user = Db.findById("user_login", id);
	    setAttr("user", user);
		
		
		render("/WebAdmin/user/edit.html");
	}
	
    public void list(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select * from ("
    			+ " SELECT id,user_name,phone,invitation_code,wedding_date,create_time,status "
    			+ " FROM user_login where system_type='mobile'"
        		+ " ) A where 1=1 ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_time desc " +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    public void myprojectList(){
    	String sLimit = "";
    	String pageIndex = getPara("draw");
    	String user_id = getPara("user_id");
    	if (getPara("start") != null && getPara("length") != null) {
    		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
    	}
    	
    	String sql = "select mp.project , item.item_name,item.complete_date"
    			+ " from wc_my_project_ref ref "
    			+ " LEFT JOIN wc_my_project_item item on item.id = ref.item_id"
    			+ " LEFT JOIN wc_my_project mp on mp.id = item.order_id"
    			+ " where ref.user_id = ? "
    			+ " group by ref.id"
    			+ " order by mp.index asc";
    	
    	Record rec = Db.findFirst("select count(0) total from (" + sql + ") A",user_id);
    	
    	List<Record> orderList = Db.find(sql + sLimit,user_id);
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("draw", pageIndex);
    	map.put("recordsTotal", rec.getLong("total"));
    	map.put("recordsFiltered", rec.getLong("total"));
    	map.put("data", orderList);
    	renderJson(map); 
    }
    
    public void deleteUser(){
    	String user_id = getPara("user_id");
    	boolean result = false;
    	
    	if(StringUtils.isNotBlank(user_id)){
    		Record user = Db.findById("user_login", user_id);
    		if(user != null){
    			user.set("status", "停用");
        		Db.update("user_login",user);
        		result = true;
    		}
    	}
    	
    	renderJson(result);
    }
    
}
