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
    	String conditions = getRequest().getHeader("conditions");
    	
    	//横幅广告
    	List<Record> bannerList = Db.find(" SELECT  id,  ad_index, "
    			+ " ifnull(user_id, default_user_id) user_id, "
    			+ " ifnull( product_id, default_product_id ) product_id,"
    			+ " if(photo = '' || photo is null, default_photo, photo) photo"
    			+ " FROM"
    			+ " `wc_ad_banner_photo`; ");
    	//横幅广告
    	List<Record> cuList = Db.find(" select "
    			+ " wcu.id ,cgr.`name` trade_type,com.c_name compnay_name,wcu.begin_date,wcu.end_date,wcu.title,wcu.content"
    			+ " from wc_ad_cu wcu "
    			+ " LEFT JOIN wc_company com on com.creator = wcu.creator"
    			+ " LEFT JOIN category cgr on cgr.id = com.trade_type");
    	Record data = new Record();
    	data.set("bannerList", bannerList);
    	data.set("cuList", cuList);
        renderJson(data);  
    }
}
