package controllers.webadmin.biz;

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
public class InviterDetailController extends Controller {

	private Logger logger = Logger.getLogger(InviterDetailController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		 //获取地区
        String sql_loc="select loc.name city,lm.* from location_management lm "
        				+ "left join location loc on lm.code = loc.code";
        List<Record> locations = Db.find(sql_loc);
        setAttr("locations",locations);
		render(getRequest().getRequestURI()+"/list.html");
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
        
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        String location = getPara("location");
        
        String condition = "";
        if(StringUtils.isNotBlank(begin_date) || StringUtils.isNotBlank(end_date)){
        	if(StringUtils.isBlank(begin_date)){
        		begin_date = "2000-01-01" ;
        	}else{
        		begin_date = begin_date+"-01" ;
        	}
        	
        	if(StringUtils.isNotBlank(end_date)){
        		end_date = end_date + "-31";
        	}else{
        		end_date = "2037-01-01";
        	}
        	
        	condition = " and ul.create_time between '" + begin_date + "' and '" + end_date + "'";
        }
        if(StringUtils.isNotBlank(location)){
        	condition += " and ul.location like '%"+location+"%'";
        }
         
    	String sql = ""
    			+ " SELECT ul.id,ul.user_name,ul.phone,"
    			+ " p_com.company_name parent_name,"
    			+ " p_ul.invitation_code parent_code,"
    			+ " iv.inviter_name inviter_name,"
    			+ " ul.invitation_code,"
    			+ " ul.wedding_date,"
    			+ " ul.create_time,"
    			+ " ul.location,"
    			+ " ul.remark1,"
    			+ " ul.remark2,"
    			+ " ul.remark3 "
    			+ " FROM wc_inviter iv "
    			+ " left join user_login p_ul on p_ul.id = iv.user_id"
    			+ " left join wc_company p_com on p_com.creator = iv.user_id"
    			+ " left join user_login ul on ul.invitation_code = iv.invite_code and ifnull(ul.invitation_code,'') != '' "
    			+ " left join wc_company com on com.creator = ul.id"
    			+ "	where ul.system_type='mobile'"
    			+ "	and ifnull(ul.invitation_code,'') != ''"
    			+ condition
        		+ " group by ul.id ";
    	

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql + " order by iv.id, ul.create_time desc " +sLimit);
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
