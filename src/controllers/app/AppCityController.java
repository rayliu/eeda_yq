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
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;

public class AppCityController extends Controller {

    private Logger logger = Logger.getLogger(AppCityController.class);
    
    /**
     * 问问
     * @throws IOException
     */
    public void getDate() {
    	String login_id = getPara("login_id");
    	
    	//商家列表
    	List<Record> locList = Db.find(""
    			+ " select *,loc.name city_name FROM location_management lm"
    			+ " left join location loc on loc.code = lm.code");
    	
    	Record data = new Record();
    	data.set("locList", locList);
        renderJson(data);  
    }
}
