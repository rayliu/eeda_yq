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

public class AppTaoController extends Controller {

    private Logger logger = Logger.getLogger(AppTaoController.class);
    
    /**
     * 婚掏品页面数据加载
     * @throws IOException
     */
    public void orderData() throws IOException{
    	//横幅广告
    	List<Record> bannerList = Db.find(" SELECT  id,  ad_index, "
    			+ " if((now() BETWEEN begin_date and end_date),user_id,default_user_id) user_id,"
    			+ " ifnull( product_id, default_product_id ) product_id,"
    			+ " if((now() BETWEEN begin_date and end_date),photo,default_photo) photo"
    			+ " FROM"
    			+ " `wc_ad_banner_photo`"
    			+ " order by ad_index asc; ");
    	//促销广告
    	List<Record> cuList = Db.find(" select "
    			+ " wcu.id id ,cgr.`name` trade_type,wcu.cover,"
    			+ " com.c_name compnay_name,wcu.begin_date,wcu.end_date,wcu.title,wcu.content,"
    			+ " wcu.creator user_id"
    			+ " from wc_ad_cu wcu "
    			+ " LEFT JOIN wc_company com on com.creator = wcu.creator"
    			+ " LEFT JOIN category cgr on cgr.id = com.trade_type"
    			+ " where ifnull(wcu.title,'') != '' "
    			+ "	and (now() BETWEEN wcu.begin_date and wcu.end_date)"
    			+ " order by wcu.create_time desc");
    	Record data = new Record();
    	data.set("bannerList", bannerList);
    	data.set("cuList", cuList);
        renderJson(data);  
    }
}
