package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import controllers.util.MD5Util;

public class AppBestCaseController extends Controller {

    private Logger logger = Logger.getLogger(AppBestCaseController.class);
    
    /**
     * 精选案例品页面数据加载
     * @throws IOException
     */
    public void orderData() throws IOException{
    	String cityCode = getPara("cityCode");
    	
    	String conditions = "";

    	if(StringUtils.isNotBlank(cityCode)){
    		conditions += " and wc.city = '" + cityCode + "'";
    	}
    	
    	//精选婚礼
    	List<Record> caseList = Db.find(" SELECT cas.id, cas.name title,cas.picture_name cover,"
    			+ " wc.c_name shop_name ,wc.logo shop_logo "
    			+ " FROM"
    			+ " `wc_case` cas"
    			+ " left join wc_company wc on wc.creator = cas.creator"
    			+ " where cas.flag = '1'"
    			+ conditions);
    	
    	//案例明细表关联
    	for (Record item : caseList) {
    		Long order_id = item.getLong("id");
    		List<Record> itemList = Db.find(" SELECT id,photo"
        			+ "  FROM"
        			+ " `wc_case_item` where order_id = ? order by create_time",order_id);
    		item.set("itemList", itemList);
		}
    	
    	Record data = new Record();
    	data.set("caseList", caseList);
        renderJson(data);  
    }
    
    /*
     * 精选明细
     */
    public void findById() throws UnsupportedEncodingException{
    	String case_id = getPara();
    	case_id = URLDecoder.decode(case_id, "UTF-8");
    	//店铺信息
    	Record re = Db.findById("wc_case", case_id);
    	//商家列表
    	List<Record> shopList = Db.find(" select ul.id shop_id,wc.logo,ul.influence, "
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
    			+ " where ul.id = ?"
    			+ " group by ul.id",re.getLong("creator"));
    	//案例明细
    	List<Record> caseList = Db.find(""
    			+ " select * from wc_case_item"
    			+ " where order_id = ?",case_id);
    	
    	Record data = new Record();
    	data.set("shop", shopList);
    	data.set("caseList", caseList);
        renderJson(data);  	
    }
    
    /*
     * 视频案例
     */
    public void video_case() throws UnsupportedEncodingException{
    	String case_id = getPara();
    	case_id = URLDecoder.decode(case_id, "UTF-8");
    	
    	//店铺信息
    	List<Record> caseData = Db.find(""
    			+ " select wc.creator shop_id,wc.logo,ul.influence, "
    			+ " ifnull(wc.c_name,wc.company_name) company_name,"
    			+ " if(dio.id >0 ,'Y','N') diamond,"
    			+ " if(cu.id >0 ,'Y','N') cu,"
    			+ " if(hui.is_active = 'Y' ,'Y','N') hui,"
    			+ " cor.name category_name,wc.address,wc.about,"
    			+ " vc.name title,vc.video_url "
    			+ " from video_case vc "
    			+ " left join wc_company wc on wc.creator = vc.creator"
    			+ " left join user_login ul on ul.id = vc.creator"
    			+ " left join category cor on cor.id = wc.trade_type"
    			+ " left join wc_ad_diamond dio on dio.creator = vc.creator"
    			+ " and ((now() BETWEEN dio.begin_date and dio.end_date) and dio.status = '已开通')"
    			+ " left join wc_ad_cu cu on cu.creator = vc.creator"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " left join wc_ad_hui hui on hui.creator = vc.creator"
    			+ " where vc.id = ?"
    			+ " group by vc.id",case_id);
    	
    	Record data = new Record();
    	data.set("data", caseData);
        renderJson(data);  	
    }
    
    
    public void get_more_case() throws UnsupportedEncodingException{
    	String shop_id = getPara("shop_id");
    	shop_id = URLDecoder.decode(shop_id, "UTF-8");
    	
    	String type = getPara("type");
    	type = URLDecoder.decode(type, "UTF-8");
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
    	
    	
    	//案例信息
    	List<Record> caseList =  null;
    	if("case".equals(type)){
    		caseList = Db.find(" select wc.*,wc.picture_name cover from wc_case wc"
        			+ " where wc.creator = ?  order by id desc",shop_id);
    	}else{
    		caseList = Db.find(" select wc.*  from video_case wc"
        			+ " where wc.creator = ?  order by id desc",shop_id);
    	}
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
    	data.set("caseList", caseList);
        renderJson(data);  
    }
}
