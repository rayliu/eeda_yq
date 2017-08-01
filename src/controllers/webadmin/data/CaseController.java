package controllers.webadmin.data;

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

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CaseController extends Controller {

	private Logger logger = Logger.getLogger(CaseController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
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
	 
    @Before(Tx.class)
   	public void update() throws Exception {
    	String jsonStr=getPara("jsonStr");
    	Gson gson = new Gson();  
    	Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);
    	String id=(String) dto.get("id");
    	Double img_num = (Double) dto.get("img_num");
    	String name=(String) dto.get("name");
        String picture_name=(String) dto.get("picture_name");
        Record example=null;
        if(StringUtils.isNotBlank(id)){
    		example=Db.findById("wc_case", id);
    		example.set("name", name);
    		example.set("picture_name", picture_name);
    		example.set("create_time",new Date());
    		Db.update("wc_case", example);
     	List<Record> orderItem = Db.find("select * from wc_case_item where order_id = ?",id);
    	for (int i = 1; i <= img_num; i++) {
    		int order_num = orderItem.size();
    		Record re = null;
    		if(i > order_num){
    			re = new Record();
    			re.set("photo", (String) dto.get("photo"+i));
        		re.set("seq", i);
        		re.set("order_id", id);
            	Db.save("wc_case_item", re);
	    		}
			}
	    }
        renderJson(true);
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
    
	@Before(Tx.class)
	public void deletePicture(){
		String id=getPara("id");
		Db.deleteById("wc_case_item", id);
		renderJson(true);
	}
    
    public void list(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	   Long user_id = LoginUserController.getLoginUserId(this);
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " select wco.c_name productor,wc.* from wc_case wc "
        				+ "left join wc_company wco on wc.creator=wco.creator ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    @Before(Tx.class)
    public void updateFlag(){
    	String id = getPara("id");
  	   	String flag = getPara("flag");
  	   	String sql = "update wc_case set flag = "+flag+" where id="+id;
  	   	Db.update(sql);
  	   	renderJson(true);
    }
    
	@Before(Tx.class)
	public void saveFile(){
		Record re = new Record();
    	try {
            UploadFile file = getFile();
            re.set("name", file.getFileName());
        } catch (Exception e) {
            e.getMessage();
        }
    	renderJson(re);
	}
	
    
    public void modify(){
    	String id = getPara("id");
    	String sql="select * from wc_case where id = "+id;
    	String pic="select * from wc_case_item where order_id="+id;
    	Record  re = Db.findFirst(sql);
    	List  pictures = Db.find(pic);
    	setAttr("wccase",re);
    	setAttr("pictures",pictures);
    	System.out.println(getRequest().getRequestURI()+"modify/edit.html");
    	render(getRequest().getRequestURI()+"/edit.html");
    }
    
    @Before(Tx.class)
    public void delete(){
    	String id = getPara("id");
    	boolean result=false;
    	if(StringUtils.isNotBlank(id)){
	    	String sql = "delete from wc_case_item where order_id = "+id; 
			Db.update(sql);
	    	Db.deleteById("wc_case", id);
	    	result=true;
    	}
    	renderJson(result);
    }
   
   

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
