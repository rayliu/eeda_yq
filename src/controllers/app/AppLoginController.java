package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;
import sun.nio.cs.UnicodeEncoder;

import com.aliyuncs.exceptions.ClientException;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.AliSmsUtil;
import controllers.util.EedaHttpKit;
import controllers.util.HttpUtils;
import controllers.util.MD5Util;
import controllers.util.PhoneAddress;

public class AppLoginController extends Controller {

    private Logger logger = Logger.getLogger(AppLoginController.class);
    
    
    /**
     * 回复列表内容
     * @throws UnsupportedEncodingException 
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_register() throws UnsupportedEncodingException{
    	boolean result = false;
    	String errMsg = "";

    	String invite_code = URLDecoder.decode(getPara("invite_code"), "UTF-8");
    	String user_name = URLDecoder.decode(getPara("user_name"), "UTF-8");
    	String wedding_date = URLDecoder.decode(getPara("wedding_date"), "UTF-8");
    	String pwd = URLDecoder.decode(getPara("pwd"), "UTF-8");
    	String mobile = URLDecoder.decode(getPara("mobile"), "UTF-8");
    	
    	Record user = Db.findFirst("select * from user_login where phone = ? and system_type = 'mobile'", mobile);
    	if(user == null){
    		String sha1Pwd = MD5Util.encode("SHA1",pwd);
    		Record user_login = new Record();
        	user_login.set("invitation_code", invite_code);
        	user_login.set("phone", mobile);
        	user_login.set("location", PhoneAddress.check2city(mobile));
        	user_login.set("wedding_date", wedding_date);
        	user_login.set("user_name", user_name);
        	user_login.set("system_type", "mobile");
        	user_login.set("password", sha1Pwd);
        	user_login.set("password_hint", pwd);
        	user_login.set("status", "通过");
        	user_login.set("create_time", new Date());
        	Db.save("user_login", user_login);
        	
        	if(StringUtils.isNotBlank(invite_code)){
        		Record re = Db.findFirst("select * from user_login where invitation_code = ? and  system_type = '商家后台'",invite_code);
        		if(re != null){
        			String code = re.getStr("influence");
        			re.set("influence", Integer.parseInt(code) + 1);
        			Db.update("user_login",re);
        		} else {  //子级要邀请人
        			Record child = Db.findFirst("select * from wc_inviter where invite_code = ? and is_delete != 'Y'",invite_code);
        			if(child != null){
        				String user_id = child.get("id").toString();
        				Record u = Db.findById("user_login", user_id);
        				if(u != null){
                			String code2 = u.getStr("influence");
                			u.set("influence", Integer.parseInt(code2) + 1);
                			Db.update("user_login",re);
                		}
        			}
        		}
        	}
        	result = true;
    	} else {
    		errMsg = "手机号码已被注册";
    	}
    	
    	Record data = new Record();
    	data.set("result", result);
    	data.set("errMsg", errMsg);
        renderJson(data);  
    }
    
    
    @Before(Tx.class)
    public void login() throws UnsupportedEncodingException{
    	boolean result = false;
    	String errMsg = null;
    	String login_id = null;
    	String wedding_date = null;
    	String user_name = null;
    	String password = getPara("password");
    	String mobile = getPara("mobile");
    	String sha1Pwd = MD5Util.encode("SHA1",password);
    	Record user = Db.findFirst("select * from user_login where phone = ? and system_type = 'mobile'",mobile);
    	if(user != null){
    		if("停用".equals(user.getStr("status"))){
    			errMsg = "账号已被停用";
    		}else{
    			if(sha1Pwd.equals(user.getStr("password"))){
    				result = true;
            		login_id = user.getLong("id").toString();
            		user_name = user.getStr("user_name");
            		wedding_date = user.get("wedding_date").toString();
    			}else{
    				errMsg = "密码不正确，请重新输入";
    			}
    		}
    	}else{
    		errMsg = "此手机未注册";
    	}
    	
    	Record data = new Record();
    	data.set("result", result);
    	data.set("login_id", login_id);
    	data.set("wedding_date", wedding_date);
    	data.set("user_name", user_name);
    	data.set("errMsg", errMsg);
        renderJson(data);  
    }
    
    
    public void send_code() throws ClientException{
    	boolean result = false;
    	String errMsg = "";
    	
    	String mobile = getPara("mobile");
    	int code = 0;
    	//Record user = Db.findFirst("select * from user_login where phone=? and system_type = 'mobile' ",mobile);
    	//if(user != null){
    		code= (int)((Math.random()*9+1)*1000);//4位数随机码
        	AliSmsUtil.sendSms("{\"code\":\""+code +"\"}", mobile,"SMS_116480127");
        	result = true;
//    	}else{
//    		errMsg = "此号码未注册";
//    	}
    	
    	Record data = new Record();
    	data.set("result", result);
    	data.set("code", String.valueOf(code));
    	renderJson(data);
    }
    
    
    @Before(Tx.class)
    public void reset_pwd() throws UnsupportedEncodingException{
    	boolean result = false;
    	String errMsg = "";

    	String pwd = getPara("pwd");
    	String encryptionPwd = MD5Util.encode("SHA1", pwd);
    	String mobile = getPara("mobile");
    	Record user = Db.findFirst("select * from user_login where phone=? and system_type = 'mobile' ",mobile);
    	if(user != null){
    		user.set("password", encryptionPwd);
        	result = Db.update("user_login",user);
    	}else{
    		errMsg = "此号码未注册";
    	}
    	
    	Record data = new Record();
    	data.set("result", result);
    	data.set("errMsg", errMsg);
        renderJson(data);  
    }
    
}
