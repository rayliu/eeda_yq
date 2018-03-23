package controllers.webadmin.user;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
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
public class ProjectController extends Controller {

	private Logger logger = Logger.getLogger(ProjectController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
	    //对应action
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){
	        String id = getPara("id");
//	      String title = getPara("edit_radioTitle");
//	      String content = getPara("edit_radioContent");
//	      Record r= Db.findById("msg_board", id);
//	      r.set("title", title);
//	      r.set("content", content);
//	      r.set("update_stamp", new Date());
//	      r.set("updator", LoginUserController.getLoginUserId(this));
//	      Db.update("msg_board", r);
        render(getRequest().getRequestURI()+"/edit.html");
    }
	 
	public void projectItem(){
		String order_id = getPara("order_id");
		String type = getPara("type");
		setAttr("order_id", order_id);
		setAttr("type", type);
		render("/WebAdmin/user/project/itemList.html");
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
    
    public void list(){
		//UserLogin user = LoginUserController.getLoginUser(this);
		//long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select * from wc_my_project mp";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by 'index' desc " +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    public void itemList(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String condition = "";
        String order_id = getPara("order_id");
        String type = getPara("type");
        if("byProject".equals(type)){
        	condition = " and item.order_id = "+order_id;
        }else if("byTime".equals(type)){
        	condition = " and item.by_time_order_id = "+order_id;
        }
    	String sql = "select item.*,ul.user_name from wc_my_project_item item"
    			+ " left join user_login ul on ul.id = item.creator"
    			+ " where 1 = 1 and item.item_name is not null";
    	
        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by item.id desc " +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    public void addProject(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	String project = getPara("project");
        String type = getPara("type");
         
        Record re = new Record();
        re.set("project", project);
        re.set("type", type);
        re.set("create_time", new Date());
        re.set("creator", user.get("id"));
	    if("byProject".equals(type)){
	    	Record re1 = Db.findFirst("select max(`index`) MaxIndex from wc_my_project where type='byProject'");
	    	int number = Integer.parseInt((String)re1.get("MaxIndex"));
	    	re.set("index", number+1);;
	    }else if("byTime".equals(type)){
	    	Record re1 = Db.findFirst("select max(`index`) MaxIndex from wc_my_project where type='byTime'");
	    	int number = Integer.parseInt((String)re1.get("MaxIndex"));
	    	re.set("index", number+1);;
	    }
        boolean result = Db.save("wc_my_project", re);
        
        renderJson("{\"result\":"+result+"}");
    }
    
    public void addProjectItem(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	String order_id = getPara("order_id");
    	String item_name = getPara("item_name");
        String type = getPara("type");
         
        Record re = new Record();
        re.set("item_name", item_name);
        re.set("create_time", new Date());
        re.set("creator", user.get("id"));
	    if("byProject".equals(type)){
	    	re.set("order_id", order_id);;
	    }else if("byTime".equals(type)){
	    	re.set("by_time_order_id", order_id);;
	    }
        boolean result = Db.save("wc_my_project_item", re);
        
        renderJson("{\"result\":"+result+"}");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void deleteProject(){
    	String id = getPara("id");
    	String type = getPara("type");
    	int resultNumber = Db.update("delete from wc_my_project where id = ?",id);
    	boolean itemResult = false;
    	boolean result = false;
    	List<Record> project_item_list = new ArrayList<Record>();
    	if("byProject".equals(type)){
    		project_item_list = Db.find("select*from wc_my_project_item where order_id = ?",id);
    		for(int i = 0; i<project_item_list.size(); i++){
    			Record project_item = project_item_list.get(i);
    			if(project_item!=null){
        			if(project_item.get("by_time_order_id")!=null){
        				 int num = Db.update("update wc_my_project_item set order_id = NULL where id = ?",project_item.get("id"));
        				 if(num>0){
        					 itemResult = true;
        				 }
        			}else{
        				itemResult = Db.delete("wc_my_project_item", project_item);
        			}
        		}
    		}
	    }else if("byTime".equals(type)){
	    	project_item_list = Db.find("select*from wc_my_project_item where by_time_order_id = ?",id);
	    	for(int i = 0; i<project_item_list.size(); i++){
	    		Record project_item = project_item_list.get(i);
		    	if(project_item!=null){ 
		    		if(project_item.get("order_id")!=null){
	   					int num = Db.update("update wc_my_project_item set by_time_order_id = NULL where id = ?",project_item.get("id"));
	   					if(num>0){
	   						itemResult = true;
	   					}
		   			}else{
		   				itemResult = Db.delete("wc_my_project_item", project_item);
		   			}
		    	}
	    	}
	    }
    	
    	if(project_item_list.size()>0){
    		if(resultNumber>0&&itemResult){
    			result = true;
    		}else{
    			result = false;
    		}
    	}else{
    		if(resultNumber>0){
    			result = true;
    		}
    	}
    	renderJson("{\"result\":"+result+"}");
    }

    public void deleteProjectItem(){
    	String id = getPara("id");
    	String type = getPara("type");
    	Record re = new Record();
    	
    	int resultNumber = 0;
    	if("byProject".equals(type)){
    		re = Db.findFirst("select*from wc_my_project_item where id = ?",id);
    		if(re!=null){
	    		if(re.get("by_time_order_id")!=null){
	    			resultNumber = Db.update("update wc_my_project_item set order_id = NULL where id = ?",id);
	   			}else{
	   				resultNumber = Db.update("delete from wc_my_project_item  where id = ?",id);
	   			}
	    	}
    	}else if("byTime".equals(type)){
    		re = Db.findFirst("select*from wc_my_project_item where id = ?",id);
	    	if(re!=null){
	    		if(re.get("order_id")!=null){
   					resultNumber = Db.update("update wc_my_project_item set by_time_order_id = NULL where id = ?",id);
	   			}else{
	   				resultNumber = Db.update("delete from wc_my_project_item  where id = ?",id);
	   			}
	    	}
    	}
    	
    	renderJson("{\"resultNumber\":"+resultNumber+"}");
    }
    
    public void updateProjectItem(){
    	String id = getPara("id");
    	String item_name = getPara("item_name");
    	int resultNumber = Db.update("update wc_my_project_item set item_name = ? where id = ?",item_name,id);
    	renderJson("{\"resultNumber\":"+resultNumber+"}");
    }
}
