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

public class AppBestCaseController extends Controller {

    private Logger logger = Logger.getLogger(AppBestCaseController.class);
    
    /**
     * 精选案例品页面数据加载
     * @throws IOException
     */
    public void orderData() throws IOException{
    	String conditions = getRequest().getHeader("conditions");
    	
    	//精选婚礼
    	List<Record> caseList = Db.find(" SELECT id, name title,picture_name cover "
    			+ " FROM"
    			+ " `wc_case`"
    			+ " where flag = '1'");
    	
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
}
