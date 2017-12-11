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

import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;

public class AppCategoryController extends Controller {

    private Logger logger = Logger.getLogger(AppCategoryController.class);
    
    /**
     * 商家分类
     * @throws IOException
     */
    public void searchShopByType() throws IOException{
    	String category_name = getRequest().getHeader("category_name");
    	System.out.println("before:"+category_name);
    	category_name = EedaHttpKit.decodeHeadInfo(category_name);
    	System.out.println("after:"+category_name);
    	String conditions = "";
    	
    	conditions += " and ctg.name = '"+category_name+"'";
    	
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
