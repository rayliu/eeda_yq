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
    	String conditions = getRequest().getHeader("conditions");
    	
    	//商家列表
    	List<Record> shopList = Db.find(" select wc.c_name company_name,ctg.name category_name from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " where ctg.name = '婚纱'");
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
        renderJson(data);  
    }
}
