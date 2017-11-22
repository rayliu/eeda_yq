package controllers.app;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.MD5Util;

public class AppCategoryController extends Controller {

    private Logger logger = Logger.getLogger(AppCategoryController.class);
    
    /**
     * 商家分类
     * @throws IOException
     */
    public void searchShopByType() throws IOException{
    	String category_name = getRequest().getHeader("category_name");
    	
    	String conditions = "";
    	if("weddingDress".equals(category_name)){
    		category_name = "婚纱";
    		conditions += " and ctg.name = '"+category_name+"'";
    	}
		if("studio".equals(category_name)){
			category_name = "影楼";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		if("marriagePackage".equals(category_name)){
			category_name = "婚策套餐";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		if("hotel".equals(category_name)){
			category_name = "酒店";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		if("camera".equals(category_name)){
			category_name = "摄像";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		if("makeup".equals(category_name)){
			category_name = "化妆";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		if("honeymoon".equals(category_name)){
			category_name = "蜜月";
			conditions += " and ctg.name = '"+category_name+"'";
		}
		
    	//商家列表
    	List<Record> shopList = Db.find(" select ul.id shop_id, "
    			+ " ifnull(wc.c_name,wc.company_name) company_name,"
    			+ " ctg.name category_name,ul.create_time,"
    			+ " wc.logo"
    			+ " from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " where 1 = 1 "
    			+ conditions);
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
        renderJson(data);  
    }
}
