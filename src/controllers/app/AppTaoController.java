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

import controllers.util.MD5Util;
import freemarker.template.utility.StringUtil;

public class AppTaoController extends Controller {

    private Logger logger = Logger.getLogger(AppTaoController.class);
    
    /**
     * 婚掏品页面数据加载
     * @throws IOException
     */
    public void orderData() throws IOException{
    	String cityCode = getPara("cityCode");
    	String bannerUser= " ban.user_id";
    	String bannerPhoto= " ban.photo";
    	String cuCondition = "";
    	if(StringUtils.isNotBlank(cityCode)){
    		bannerUser = "if((select wc.id from wc_company wc where wc.city = '" + cityCode + "'"
    				+ " and wc.creator = ban.creator) is not null,ban.user_id, ban.default_user_id)";
    		bannerPhoto = "if((select wc.id from wc_company wc where wc.city = '" + cityCode + "'"
    				+ " and wc.creator = ban.creator) is not null,ban.photo, ban.default_photo)";
    		cuCondition = " and com.city ='" + cityCode + "'";
    	}
    	//横幅广告
    	List<Record> bannerList = Db.find(" SELECT ban.id,  ban.ad_index, "
    			+ " if((now() BETWEEN ban.begin_date and ban.end_date)," + bannerUser + ",ban.default_user_id) user_id,"
    			+ " if((now() BETWEEN ban.begin_date and ban.end_date)," + bannerPhoto + ",ban.default_photo) photo"
    			+ " FROM"
    			+ " `wc_ad_banner_photo` ban"
    			+ " order by ban.ad_index asc; ");
    	
   
    	//促销广告
    	List<Record> cuList = Db.find(" select "
    			+ " wcu.id id ,cgr.`name` trade_type,wcu.cover,"
    			+ " com.c_name company_name,wcu.begin_date,wcu.end_date,wcu.title,wcu.content,"
    			+ " wcu.creator user_id"
    			+ " from wc_ad_cu wcu "
    			+ " LEFT JOIN wc_company com on com.creator = wcu.creator"
    			+ " LEFT JOIN category cgr on cgr.id = com.trade_type"
    			+ " where ifnull(wcu.title,'') != '' and wcu.status = '开启' "
    			+ " and delete_flag != 'Y'"
    			+ "	and (now() BETWEEN wcu.begin_date and wcu.end_date) "
    			+ cuCondition
    			+ " order by wcu.create_time desc");
    	Record data = new Record();
    	data.set("bannerList", bannerList);
    	data.set("cuList", cuList);
        renderJson(data);  
    }
}
