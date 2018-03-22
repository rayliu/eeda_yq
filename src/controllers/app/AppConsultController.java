package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;

public class AppConsultController extends Controller {

    private Logger logger = Logger.getLogger(AppConsultController.class);
    
   
    /**
     * 回复列表内容
     * @throws UnsupportedEncodingException 
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_consult() throws UnsupportedEncodingException{
    	String shop_id = getPara("shop_id");
    	shop_id = URLDecoder.decode(shop_id, "UTF-8");
    	String login_id = getPara("login_id");
    	login_id = URLDecoder.decode(login_id, "UTF-8");
    	String remark = getPara("values");
    	remark = URLDecoder.decode(remark, "UTF-8");
    	String project = getPara("values");
    	project = URLDecoder.decode(project, "UTF-8");
    	
    	Record user = Db.findById("user_login", login_id);
    	String user_name = user.getStr("user_name");
    	String mobile = user.getStr("phone");
    	Date wedding_date = user.getDate("wedding_date");

    	Record consult = new Record();
    	consult.set("shop_id", shop_id);
    	consult.set("user_name", user_name);
    	consult.set("mobile", mobile);
    	consult.set("project", project);
    	consult.set("wedding_date", wedding_date);
    	consult.set("remark", remark);
    	consult.set("create_time", new Date());
    	consult.set("creator", login_id);
    	Db.save("wc_consult", consult);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }
    
    
    /**
     * 回复列表内容
     * @throws UnsupportedEncodingException 
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_feedback() throws UnsupportedEncodingException{
    	String login_id = getPara("login_id");
    	login_id = URLDecoder.decode(login_id, "UTF-8");
    	String value = getPara("values");
    	value = URLDecoder.decode(value, "UTF-8");
    	
    	Record feedback = new Record();
    	feedback.set("value", value);
    	feedback.set("create_time", new Date());
    	feedback.set("creator", login_id);
    	Db.save("wc_feedback", feedback);

    	Record data = new Record();
    	data.set("result", "true");
        renderJson(data);  
    }
    
    /**
     * 回复列表内容
     * @throws UnsupportedEncodingException 
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_wedding_date() throws UnsupportedEncodingException{
    	String login_id = getPara("login_id");
    	login_id = URLDecoder.decode(login_id, "UTF-8");
    	String value = getPara("values");
    	value = URLDecoder.decode(value, "UTF-8");
    	
    	Record user = new Record();
    	user.set("wedding_date", value);
    	user.set("id", login_id);
    	Db.update("user_login", user);

    	Record data = new Record();
    	data.set("result", "true");
        renderJson(data);  
    }
    
   
}
