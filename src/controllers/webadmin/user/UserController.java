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
        
        String location = getPara("location");
        String condition = "";
        if(StringUtils.isNotBlank(location)){
        	condition += " and ul.location like '%"+location+"%'";
        }
         
    	String sql = "select * from ("
    			+ " SELECT ul.id,ul.user_name,ul.phone,ul.location,"
    			+ " (select com.company_name from wc_inviter inv"
    			+ " left join wc_company com on com.creator = inv.user_id"
    			+ " where inv.invite_code = ul.invitation_code and ifnull(inv.invite_code,'')!='' ) parent_name,"
    			+ " (select c_ul.invitation_code from wc_inviter inv "
    			+ " left join user_login c_ul on c_ul.id = inv.user_id"
    			+ " where inv.invite_code = ul.invitation_code and ifnull(inv.invite_code,'')!='') parent_code,"
    			+ " (select inv.inviter_name from wc_inviter inv"
    			+ " where inv.invite_code = ul.invitation_code and ifnull(inv.invite_code,'')!=''"
    			+ " ) inviter_name,"
    			+ " ul.invitation_code,ul.wedding_date,ul.create_time,ul.status,"
    			+ " ul.remark1,ul.remark2,ul.remark3 "
    			+ " FROM user_login ul "
    			+ "	where ul.system_type='mobile'"
    			+ condition
        		+ " ) A where 1=1 ";
    			
    	
    	//String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql + " order by create_time desc " +sLimit);
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
    	
    	String sql = "select mp.project , item.item_name,ref.complete_date"
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
    
    @Before(Tx.class)
    public void update_remark(){
    	Record result = new Record();
    	String id = getPara("id");
    	String index = getPara("index");
    	String remark1 = getPara("remark1");
    	String remark2 = getPara("remark2");
    	String remark3 = getPara("remark3");
    	
    	if(StringUtils.isNotBlank(id)){
    		Record order = Db.findById("user_login", id);
    		if("1".equals(index)){
    			order.set("remark1", remark1);
    		}else if("2".equals(index)){
    			order.set("remark2", remark2);
    		}else if("3".equals(index)){
    			order.set("remark3", remark3);
    		}
    		Db.update("user_login",order);
    		
    		result.set("result", true);
    	}else{
    		result.set("result", false);
    	}
    	
    	renderJson(result);
    	
    }
    
}
