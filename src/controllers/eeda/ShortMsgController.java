package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.profile.OfficeConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.CaptchaRender;

import controllers.profile.LoginUserController;
import controllers.util.EedaCommonHandler;
import controllers.util.MD5Util;
import controllers.util.ParentOffice;
import controllers.util.getCurrentPermission;
@Before({SetAttrLoginUserInterceptor.class,EedaMenuInterceptor.class})
public class ShortMsgController extends Controller {
    private static final String RANDOM_CODE_KEY = "eeda";
	private Log logger = Log.getLog(ShortMsgController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index(){
    	Record login_user = getAttr("login_user");
    	Record shortMsg = Db.findFirst("select * from short_setting where office_id = ?",login_user.getLong("office_id"));
    		setAttr("shortMsg", shortMsg);
    	render("/eeda/profile/shortMsg/shortMsg.html");
    }
    
    public void save(){
    	String params = getPara("params");
    	Gson gson = new Gson();
    	List<Map<String,Object>> list = gson.fromJson(params,new TypeToken<List<Map<String,Object>>>(){ }.getType());
    	
		Record login_user = getAttr("login_user");
		boolean result = false;
		if(list.size()>0){
			Db.update("delete from short_setting where office_id = ?",login_user.getLong("office_id"));
			Record re = new Record();
			for(Map<String,Object> map : list){
	        	re.set((String)map.get("name"), map.get("value"));
	    	}
			re.set("office_id", login_user.getLong("office_id"));
			result = Db.save("short_setting", re);
		}
    	renderJson("{\"result\":"+result+"}");
    }
}
