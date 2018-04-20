package controllers.app;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
    	String category_name = getPara("category_name");
    	category_name = URLDecoder.decode(category_name, "UTF-8");
    	String cityCode = getPara("cityCode");
    	
    	String conditions = "";

    	conditions += " and ctg.name = '"+category_name+"'";
    	if(StringUtils.isNotBlank(cityCode)){
    		conditions += " and wc.city = '" + cityCode + "'";
    	}
    	
    	List<Record> list = Db.find("select * from category ");
    	
    	//商家列表
    	List<Record> shopList = Db.find(" select * from(select ul.id shop_id,ul.influence, "
    			+ " ifnull(wc.c_name,wc.company_name) company_name,"
    			+ " if(dio.id >0 ,'Y','N') diamond,"
    			+ " if(cu.id >0 ,'Y','N') cu,"
    			+ " if(hui.is_active = 'Y' ,'Y','N') hui,"
    			+ " ctg.name category_name,ul.create_time,"
    			+ " wc.logo"
    			+ " from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " left join wc_ad_diamond dio on dio.creator = ul.id"
    			+ " and ((now() BETWEEN dio.begin_date and dio.end_date) and dio.status = '已开通')"
    			+ " left join wc_ad_cu cu on cu.creator = ul.id"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " left join wc_ad_hui hui on hui.creator = ul.id"
    			+ " where 1 = 1 and system_type ='商家后台' and ul.status = '通过'"
    			+ conditions
    			+ "group by ul.id) A order by diamond desc,cu desc,hui desc,influence desc");
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
    	data.set("categoryList", list);
        renderJson(data);  
    }
    
    public void categoryList(){
    	List<Record> list = Db.find("select * from category ");
    	
    	Record data = new Record();
    	data.set("categoryList", list);
        renderJson(data);  
    }
}
