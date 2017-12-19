package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

public class AppBestCaseController extends Controller {

    private Logger logger = Logger.getLogger(AppBestCaseController.class);
    
    /**
     * 精选案例品页面数据加载
     * @throws IOException
     */
    public void orderData() throws IOException{
    	//精选婚礼
    	List<Record> caseList = Db.find(" SELECT cas.id, cas.name title,cas.picture_name cover,"
    			+ " wc.c_name shop_name ,wc.logo shop_logo "
    			+ " FROM"
    			+ " `wc_case` cas"
    			+ " left join wc_company wc on wc.creator = cas.creator"
    			+ " where cas.flag = '1'");
    	
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
    	List<Record> shop = Db.find(""
    			+ " select wc.*,cor.name category_name"
    			+ " from wc_case cc "
    			+ " left join wc_company wc on wc.creator = cc.creator"
    			+ " left join category cor on cor.id = wc.trade_type"
    			+ " where cc.id = ?",case_id);
    	//案例明细
    	List<Record> caseList = Db.find(""
    			+ " select * from wc_case_item"
    			+ " where order_id = ?",case_id);
    	
    	Record data = new Record();
    	data.set("shop", shop);
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
    			+ " select wc.*,cor.name category_name,vc.name title,vc.video_url "
    			+ " from video_case vc "
    			+ " left join wc_company wc on wc.creator = vc.creator"
    			+ " left join category cor on cor.id = wc.trade_type"
    			+ " where vc.id = ?",case_id);
    	
    	Record data = new Record();
    	data.set("data", caseData);
        renderJson(data);  	
    }
    
    
}
