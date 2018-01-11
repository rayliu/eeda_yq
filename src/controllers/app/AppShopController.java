package controllers.app;

import java.io.IOException;
import java.net.URLDecoder;
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
    	//String shop_id = getRequest().getHeader("shop_id");
    	String shop_id = getPara();
    	shop_id = URLDecoder.decode(shop_id, "UTF-8");
    	
    	//商家列表
    	List<Record> shopList = Db.find(" select wc.logo,ul.influence, "
    			+ " ifnull(wc.c_name,wc.company_name) company_name,"
    			+ " if(dio.id >0 ,'Y','N') diamond,"
    			+ " if(cu.id >0 ,'Y','N') cu,"
    			+ " if(hui.is_active = 'Y' ,'Y','N') hui,"
    			+ " ctg.name category_name,wc.address,wc.about"
    			+ " from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " left join wc_ad_diamond dio on dio.creator = ul.id"
    			+ " and ((now() BETWEEN dio.begin_date and dio.end_date) and dio.status = '已开通')"
    			+ " left join wc_ad_cu cu on cu.creator = ul.id"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " left join wc_ad_hui hui on hui.creator = ul.id"
    			+ " where ul.id = ?",shop_id);
    	
    	
    	//产品信息
    	List<Record> productList = Db.find(" select pro.id, pro.cover, pro.name,pro.price from wc_product pro"
    			+ " where pro.creator = ? order by pro.id desc limit 0,3",shop_id);
    	
    	//案例信息
    	List<Record> caseList = Db.find(" select wc.*  from wc_case wc"
    			+ " where wc.creator = ? order by wc.id desc limit 0,3",shop_id);
    	
    	//视频信息
    	List<Record> videoList = Db.find("select vc.*  from video_case vc"
    			+ " where vc.creator = ? order by vc.id desc limit 0,3",shop_id);
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
    	data.set("productList", productList);
    	data.set("caseList", caseList);
    	data.set("videoList", videoList);
        renderJson(data);  
    }
}
