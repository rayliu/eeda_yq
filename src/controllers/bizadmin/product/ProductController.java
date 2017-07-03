package controllers.bizadmin.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

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
			Db.update("delete from wc_product_pic where product_id = ?",id);
			
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
        if(id == null){
            render(getRequest().getRequestURI()+"/edit.html");
            return;
        }else{
            Record rec = Db.findFirst("select * from wc_product where id = "+ id);
            setAttr("product", rec);
            
            List<Record> picList = Db.find("select * from wc_product_pic where product_id =? order by seq ", rec.getLong("id"));
            setAttr("imgs", picList);
            
            render(getRequest().getRequestURI()+"/edit.html");
        }
        
    }
	
}
