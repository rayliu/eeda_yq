package controllers.bizadmin.weddingcase;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;

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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CaseController extends Controller {

	private Logger logger = Logger.getLogger(CaseController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		String id = getPara("id");
		if(StringUtils.isNotBlank(id)){
			Record order = Db.findById("wc_case", id);
			setAttr("wccase", order);
			
			String itemList="select * from wc_case_item where order_id=?";
	    	List<Record> pictures = Db.find(itemList ,id);
	    	setAttr("pictures",pictures);
		}else{//编辑
			setAttr("wccase", new Record());
		}
		
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	public void list(){
	    String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
		Long user_id = LoginUserController.getLoginUserId(this);
		String sqlTol="select count(1) total from wc_case where creator = "+user_id+" order by create_time desc "+sLimit;
		Record rec=Db.findFirst(sqlTol);
		String sql="select * from wc_case where creator = "+user_id;
		List example_list=Db.find(sql);
		Map map=new HashMap();
	    map.put("recordsTotal", rec.getLong("total"));
	    map.put("recordsFiltered", rec.getLong("total"));
		map.put("data", example_list);
		renderJson(map);
	}
	
	public void modify_info(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	

	public void modify_pwd(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
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
	
	@Before(Tx.class)
	public void save(){
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String id=(String) dto.get("id");
        Double img_num = (Double) dto.get("img_num");
        String name=(String) dto.get("name");
        String picture_name=(String) dto.get("picture_name");
        Record example=null;
        if(StringUtils.isNotBlank(id)){
        		example=Db.findById("wc_case", id);
        		example.set("name", name);
        		example.set("picture_name", picture_name);
        		example.set("creator",userId);
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
            		re.set("create_time", new Date());
                	Db.save("wc_case_item", re);
        		}
    		}
        }else{
        		example = new Record();
        	 	example.set("name", name);
     			example.set("picture_name", picture_name);
     			example.set("creator",userId);
     			example.set("create_time",new Date());
     			 Db.save("wc_case", example);
     			Record orderItem=null;
            	for (int i = 1;i <= img_num; i++) {
            		orderItem=new Record();
	            	orderItem.set("photo", (String) dto.get("photo"+i));
	            	orderItem.set("seq", i);
	            	orderItem.set("order_id",example.get("id"));
	            	orderItem.set("create_time",new Date());
	            	Db.save("wc_case_item", orderItem);
        		}
        }
        renderJson(example);
	}
	
	
	public boolean deletePicture(String pic_name){
		String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\"+pic_name;
		File file = new File(filePath);
		boolean result = false;
		if(file.exists()&&file.isFile()){
			result = file.delete();
			result = true;
		}
		return result;
	}
	
	@Before(Tx.class)
	public void delete(){
		String id = getPara("id");
		boolean result=false;
		if(StringUtils.isNotBlank(id)){
			String sql = "delete from wc_case_item where order_id = "+id; 
			List<Record> pictures = Db.find("select * from wc_case_item where order_id = "+id);
			for(Record re : pictures){
				String picture_name = re.getStr("photo");
				deletePicture(picture_name);
			}
			boolean res = deletePicture(Db.findById("wc_case", id).getStr("picture_name"));
			Db.update(sql);
			Db.deleteById("wc_case", id);
			result=true;
		}
		renderJson(result);
	}
	
	@Before(Tx.class)
	public void deleteItem(){
		String item_id = getPara("item_id");
		String photo = getPara("photo");
		String product_id = getPara("product_id");
		
		Db.deleteById("wc_case_item", item_id);
		deletePicture(photo);
		
		//重新排序
		List<Record> items = Db.find("select * from wc_case_item where order_id = ? order by create_time asc",product_id);
		int i = 1;
		for (Record item : items) {
			item.set("seq", i);
			Db.update("wc_case_item",item);
			++ i;
		}
		renderJson(true);
	}
}
