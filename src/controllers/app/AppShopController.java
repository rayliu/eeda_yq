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

public class AppShopController extends Controller {

    private Logger logger = Logger.getLogger(AppShopController.class);
    
    /**
     * 商家分类
     * @throws IOException
     */
    public void shopList() throws IOException{
    	String user_id = getRequest().getHeader("user_id");
    	
    	//商家列表
    	List<Record> shopList = Db.find(" select wc.logo,wc.c_name company_name,ctg.name category_name,wc.address,wc.about from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " where ul.id = ?",user_id);
    	
    	
    	//产品信息
    	List<Record> productList = Db.find(" select pro.cover, pro.name,pro.price from wc_product pro"
    			+ " where pro.id = ?",user_id);
    	
    	//案例信息
    	List<Record> caseList = Db.find(" select wc.*  from wc_case wc"
    			+ " where wc.id = ?",user_id);
    	
    	//视频信息
    	List<Record> videoList = Db.find("select wc.*  from wc_case wc"
    			+ " where wc.id = ?",user_id);
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
    	data.set("productList", productList);
    	data.set("caseList", caseList);
    	data.set("videoList", videoList);
        renderJson(data);  
    }
}
