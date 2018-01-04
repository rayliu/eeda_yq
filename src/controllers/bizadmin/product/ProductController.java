package controllers.bizadmin.product;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.FetchProfile.Item;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.FileUploadUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ProductController extends Controller {

	private Logger logger = Logger.getLogger(ProductController.class);

	public void index() {
		Long userId = LoginUserController.getLoginUserId(this);
		String sql_cu  = "SELECT if(DATEDIFF(max(end_date),now())>0,cast(DATEDIFF(max(end_date),now()) as char),'-1') leave_days,max(end_date) end_date "
				+ "FROM `wc_ad_cu` "
				+ "where status='开启' and creator ="+userId;
		Record re_cu = Db.findFirst(sql_cu);
		if(re_cu == null){
			re_cu = new Record();
			re_cu.set("leave_days", 0);
		}
		setAttr("cu",re_cu);
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	public void list() {
	    String sLimit = "";
        String pageIndex = getPara("draw");
        Long userId = LoginUserController.getLoginUserId(this);
        
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        //String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * from wc_product where creator = "+ userId;
        
        Record rec = Db.findFirst("select count(1) total from ("+sql+") B");
        List<Record> orderList = Db.find(sql + " order by id desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
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
	public void deletePicture(){
		String pic_name = getPara("value");
		String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\"+pic_name;
		File file = new File(filePath);
		boolean result = false;
		if(file.exists()&&file.isFile()){
			result = file.delete();
			result = true;
		}
		renderJson(result);
	}
	@Before(Tx.class)
	public void deleteItem(){
		String item_id = getPara("item_id");
		String photo = getPara("photo");
		String product_id = getPara("product_id");
		
		Db.deleteById("wc_product_pic", item_id);
		deletePicture(photo);
		
		//重新排序
		List<Record> items = Db.find("select * from wc_product_pic where order_id = ? order by create_time asc",product_id);
		int number = items.size();
		int i = 1;
		for (Record item : items) {
			item.set("seq", i);
			Db.update("wc_product_pic",item);
			++ i;
		}
		
		renderJson(true);
	}
	
	
	@Before(Tx.class)
	public void deleteProduct(){
		String id = getPara("id");
		boolean result = false;
		if(StringUtils.isNotBlank(id)){
			
			List<Record> pictures = Db.find("select * from wc_product_pic where order_id = "+id);
			for(Record re : pictures){
				String picture_name = re.getStr("photo");
				deletePicture(picture_name);
			}
			boolean res = deletePicture(Db.findById("wc_product", id).getStr("cover"));
			
			Db.update("delete from wc_product_pic where order_id = ?",id);
			
			Db.update("delete from wc_product where id = ?",id);
			
			result = true;
		}
		
		renderJson(result);
	}

	public void setActive(){
		String id = getPara("id");
        String flag = getPara("flag");
        boolean result = false;
        if(StringUtils.isNotBlank(id)){
        	Record rec = Db.findFirst("select * from wc_product where id = "+ id);
        	rec.set("is_active", flag);
        	Db.update("wc_product",rec);
        	result = true;
        }
        renderJson(result);
    }
	
	@Before(Tx.class)
	public void updateProduct(){
		String id = getPara("id");
		String flag = getPara("flag");
		String sql = "update wc_product set cu_flag = '"+flag+"' where id ="+id+"";
		Db.update(sql);
		renderJson(true);
	}
	
	@Before(Tx.class)
	public void openHui(){
		Long userId = LoginUserController.getLoginUserId(this);
		String flag = getPara("flag");
		String sql = "update wc_ad_hui set is_active ='"+flag+"'";
		Db.update(sql);
		renderJson(true);
	}
	
	public void edit(){
    	String id = getPara("id");
   		String sql_cat = "select * from category ";
    	List<Record> categorys = Db.find(sql_cat);
    	setAttr("categorys",categorys);
        if(StringUtils.isBlank(id)){
            render(getRequest().getRequestURI()+"/edit.html");
            setAttr("product", new Record());
        }else{
            Record rec = Db.findFirst("select * from wc_product where id = ?",id);
            setAttr("product", rec);
            List<Record> productItem = Db.find("select * from wc_product_pic where order_id = ? order by seq asc ", id);
            setAttr("productItem", productItem);
            render(getRequest().getRequestURI()+"/edit.html");
        }
        
    }
	
	@Before(Tx.class)
	public void save(){
		//String rootPath = PathKit.getWebRootPath();
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String order_id = (String) dto.get("id");
        String name = (String) dto.get("name");
        String category = (String) dto.get("category");
        String price_type = (String) dto.get("price_type");
        String price =  (String)dto.get("price");
        String unit = (String) dto.get("unit");
        String content = (String) dto.get("content");
        String cover = "".equals((String) dto.get("cover"))?null:(String) dto.get("cover");
        Double img_num = (Double) dto.get("img_num");


        Record order = null;
        if(StringUtils.isNotBlank(order_id)){
        	order = Db.findById("wc_product", order_id);
        	order.set("category", category);
        	order.set("name", name);
        	order.set("price_type", price_type);
        	order.set("price", price);
        	order.set("unit", unit);
        	order.set("is_active", "Y");
        	order.set("content", content);
        	order.set("cover", cover);
        	order.set("update_stamp", new Date());
        	order.set("updater", userId);
        	Db.update("wc_product", order);
        	
        	List<Record> orderItem = Db.find("select * from wc_product_pic where order_id = ? order by seq asc",order_id);
        	for (int i = 1; i <= img_num; i++) {
        		int order_num = orderItem.size();
        		Record re = null;
        		if(i > order_num){
        			re = new Record();
        			re.set("photo", (String) dto.get("photo"+i));
            		re.set("seq", i);
            		re.set("order_id", order.get("id"));
            		re.set("create_time", new Date());
                	Db.save("wc_product_pic", re);
        		}
    		}
        }else{
        	order = new Record();
        	order.set("name", name);
        	order.set("category", category);
        	order.set("price_type", price_type);
        	order.set("price", price);
        	order.set("unit", unit);
        	order.set("content", content);
        	order.set("cover", cover);
        	order.set("creator", userId);
        	order.set("create_stamp", new Date());
        	Db.save("wc_product", order);
        	
        	Record orderItem = null;
        	for (int i = 1; i <= img_num; i++) {
        		orderItem = new Record();
        		orderItem.set("photo", (String) dto.get("photo"+i));
        		orderItem.set("seq", i);
        		orderItem.set("order_id", order.get("id"));
        		orderItem.set("create_time", new Date());
            	Db.save("wc_product_pic", orderItem);
    		}
        }
        renderJson(order);
	}
	
	//开通促销
	public void cuOperation(){
		
	}
	
	//上传相关文档
    @Before(Tx.class)
    public void saveFile() throws Exception{
    	Record re = new Record();
    	try {
            UploadFile file = getFile();
            re.set("name", file.getFileName());
        } catch (Exception e) {
            e.getMessage();
        }
    	renderJson(re);
    }
}
