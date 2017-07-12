package controllers.bizadmin.product;

import java.io.File;
import java.io.IOException;
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
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	public void list() {
	    String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select * from wc_product where creator = "+ userId;
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by seq " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }
	
	public void deleteProduct(){
		String id = getPara("id");
		boolean result = false;
		if(StringUtils.isNotBlank(id)){
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
	
	
	public void edit(){
        String id = getPara("id");
        if(StringUtils.isBlank(id)){
            render(getRequest().getRequestURI()+"/edit.html");
            setAttr("product", new Record());
            setAttr("productItem", new Record());
        }else{
            Record rec = Db.findFirst("select * from wc_product where id = ?",id);
            setAttr("product", rec);
            
            Record productItem = Db.findFirst("select * from wc_product_pic where order_id = ? ", id);
            setAttr("productItem", productItem);
            
            render(getRequest().getRequestURI()+"/edit.html");
        }
        
    }
	
	
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
        String photo1 = "".equals((String) dto.get("photo1"))?null:(String) dto.get("photo1");
        String photo2 = "".equals((String) dto.get("photo2"))?null:(String) dto.get("photo2");
        String photo3 = "".equals((String) dto.get("photo3"))?null:(String) dto.get("photo3");
        String photo4 = "".equals((String) dto.get("photo4"))?null:(String) dto.get("photo4");
        String photo5 = "".equals((String) dto.get("photo5"))?null:(String) dto.get("photo5");
        String photo6 = "".equals((String) dto.get("photo6"))?null:(String) dto.get("photo6");
        String photo7 = "".equals((String) dto.get("photo7"))?null:(String) dto.get("photo7");
        String photo8 = "".equals((String) dto.get("photo8"))?null:(String) dto.get("photo8");
        String photo9 = "".equals((String) dto.get("photo9"))?null:(String) dto.get("photo9");
        String photo10 = "".equals((String) dto.get("photo10"))?null:(String) dto.get("photo10");
      
        Record order = null;
        if(StringUtils.isNotBlank(order_id)){
        	order = Db.findById("wc_product", order_id);
        	order.set("category", category);
        	order.set("price_type", price_type);
        	order.set("price", price);
        	order.set("unit", unit);
        	order.set("is_active", "Y");
        	order.set("content", content);
        	order.set("cover", cover);
        	Db.update("wc_product", order);
        	
        	Record orderItem = Db.findFirst("select * from wc_product_pic where order_id = ?",order_id);
        	orderItem.set("photo1", photo1);
        	orderItem.set("photo2", photo2);
        	orderItem.set("photo3", photo3);
        	orderItem.set("photo4", photo4);
        	orderItem.set("photo5", photo5);
        	orderItem.set("photo6", photo6);
        	orderItem.set("photo7", photo7);
        	orderItem.set("photo8", photo8);
        	orderItem.set("photo9", photo9);
        	orderItem.set("photo10", photo10);
        	Db.update("wc_product_pic", orderItem);
        	
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
        	
        	Record orderItem = new Record();
        	orderItem.set("photo1", photo1);
        	orderItem.set("photo2", photo2);
        	orderItem.set("photo3", photo3);
        	orderItem.set("photo4", photo4);
        	orderItem.set("photo5", photo5);
        	orderItem.set("photo6", photo6);
        	orderItem.set("photo7", photo7);
        	orderItem.set("photo8", photo8);
        	orderItem.set("photo9", photo9);
        	orderItem.set("photo10", photo10);
        	orderItem.set("order_id", order.get("id"));
        	Db.save("wc_product_pic", orderItem);
        }
        renderJson(order);
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
