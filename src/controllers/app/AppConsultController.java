package controllers.app;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;
import sun.nio.cs.UnicodeEncoder;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;

public class AppConsultController extends Controller {

    private Logger logger = Logger.getLogger(AppConsultController.class);
    
   
    /**
     * 回复列表内容
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_consult(){
    	String shop_id = getRequest().getHeader("shop_id");
    	String login_id = getRequest().getHeader("login_id");
    	String user_name = URLDecoder.decode(getRequest().getHeader("user_name"));
    	String mobile = URLDecoder.decode(getRequest().getHeader("mobile"));
    	String wedding_date = URLDecoder.decode(getRequest().getHeader("wedding_date"));
    	String remark = URLDecoder.decode(getRequest().getHeader("remark"));

    	Record consult = new Record();
    	consult.set("shop_id", shop_id);
    	consult.set("user_name", user_name);
    	consult.set("mobile", mobile);
    	consult.set("wedding_date", wedding_date);
    	consult.set("remark", remark);
    	consult.set("create_time", new Date());
    	consult.set("creator", login_id);
    	Db.save("wc_consult", consult);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }
    
   
}
