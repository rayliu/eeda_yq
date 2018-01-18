package controllers.bizadmin;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.Office;
import models.ParentOfficeModel;
import models.UserLogin;
import models.UserOffice;
import models.eeda.OfficeConfig;

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
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.EedaCommonHandler;
import controllers.util.MD5Util;
import controllers.util.ParentOffice;
import controllers.util.getCurrentPermission;

public class BizAdminController extends Controller {
	private Log logger = Log.getLog(BizAdminController.class);
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {
        // remember me 处理，自动帮user 登陆
        if (!currentUser.isAuthenticated() && currentUser.isRemembered()) {
            Object principal = currentUser.getPrincipal();
            if (null != principal) {
                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + String.valueOf(principal) + "' and (is_stop = 0 or is_stop is null)");
                if(user==null){//这里是预防user使用了remember me, 但是user ID在表中已删除
                	redirect("/BusinessAdmin/login");
                	return false;
                }
                String password = user.getStr("password");
                UsernamePasswordToken token = new UsernamePasswordToken(user.getStr("user_name"), password);
                token.setRememberMe(true);
                currentUser.login(token);// 登录
            }
        }

        if (!currentUser.isAuthenticated()) {
            redirect("/BusinessAdmin/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        // timeout:-1000ms 这样设置才能永不超时 
    	currentUser.getSession().setTimeout(-1000L);
    	
        return true;
    }
    
    
    @Before({EedaMenuInterceptor.class,SetAttrLoginUserInterceptor.class})
    public void index() {
        if (isAuthenticated()) {
            redirect("/BusinessAdmin/account");
        }
    }

	private void updateLastLogin(UserLogin user) {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");//可以方便地修改日期格式
		String currentTime = dateFormat.format( now );
		user.set("last_login", currentTime);
		user.update();
	}

	public void login(){
		render("/BusinessAdmin/login/login.html");
	}

    public void dologin() throws UnsupportedEncodingException {
        String strLoginPagePath = getRequest().getRequestURI()+"/login.html";
        HttpServletRequest request = getRequest();
        String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
        String errMsg = "";
        boolean result = false;
    	if (isAuthenticated()) {//如果已经登录, 跳转到管理平台首页
    		//redirect("/WebAdmin");
    		result=true;
    	}else{
    	    String method = getRequest().getMethod();
//    	    if("GET".equals(method)){
//    	        render(strLoginPagePath);
//    	        return;
//    	    }
    	
	    	String username = URLDecoder.decode(getPara("username"), "UTF-8");
	        String password = URLDecoder.decode(getPara("password"), "UTF-8");
	        
	        //setSysTitle();
	        String sha1Pwd = MD5Util.encode("SHA1", password);
	        UsernamePasswordToken token = new UsernamePasswordToken(username, sha1Pwd );
	        if (getPara("remember") != null && "Y".equals(getPara("remember")))
	            token.setRememberMe(true);
	        try {
	            currentUser.login(token);
	            if (getPara("remember") != null && "Y".equals(getPara("remember"))){
	                // timeout:-1000ms 这样设置才能永不超时 
	            	currentUser.getSession().setTimeout(-1000L);
	            }
	        } catch (UnknownAccountException uae) {
	            errMsg = "用户名不存在";
	            errMsg = "用户名/密码不正确";
	            uae.printStackTrace();
	        } catch (IncorrectCredentialsException ice) {
	            errMsg = "密码不正确";
	            errMsg = "用户名/密码不正确";
	            ice.printStackTrace();
	        } catch (LockedAccountException lae) {
	            errMsg = "用户名已被停用";
	            lae.printStackTrace();
	        } catch (AuthenticationException ae) {
	            errMsg = "用户名/密码不正确";
	            ae.printStackTrace();
	        }
	        if (errMsg.length()==0) {
	        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=? and (is_stop = 0 or is_stop is null) and system_type = '商家后台'",currentUser.getPrincipal());
	        	if(user == null){
	        		currentUser.logout();
	            	errMsg = "用户名不存在或已被停用";
	            	setAttr("errMsg", errMsg);
	            }else{
	            	result = true;
	            };
    		}
        }
    	Record re = new Record();
    	re.set("result", result);
    	re.set("errMsg", errMsg);
    	
    	renderJson(re);
    }
    
    public void logOut(){
    	currentUser.logout();
    	redirect("/BusinessAdmin/login");
    }

	private void setSysTitle() {
		String serverName = getRequest().getServerName();
        String basePath = getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest().getServerPort()+"/";
        
        logger.debug(serverName);
        OfficeConfig of = OfficeConfig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeConfig();
        	of.set("system_title", "易达物流");
        	of.set("logo", "/eeda/img/eeda_logo.ico");
        }
        UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name ='"+currentUser.getPrincipal()+"' and is_main=1");
        if(uo != null){
            Office office = Office.dao.findById(uo.get("office_id"));
            setAttr("office_name", office.get("office_name"));
        }
        setAttr("SYS_CONFIG", of);
	}

    public void logout() {
        currentUser.logout();
        redirect("/BusinessAdmin/login");
    }
    
    public void getLoginUser(){
    	Long user_id = LoginUserController.getLoginUserId(this);
    	Record diamond = Db.findFirst("select * from wc_ad_diamond"
    			+ " where creator = ? and (now() BETWEEN begin_date and end_date) and status = '已开通'",user_id);
    	
    	
    	String sql = "select *,ul.user_name from wc_company wc "
    			+ "	left join user_login ul on ul.id = wc.creator"
    			+ " where creator = "+user_id;
    	Record user = Db.findFirst(sql);
    	
    	Record re = user;
    	if(diamond != null){
    		re.set("diamond", "Y");
    	}else{
    		re.set("diamond", "N");
    	}
    	
    	renderJson(re);
    }
    
    public void forgetPwd(){
    	 render("/BusinessAdmin/login/forgetPwd.html");
    }

    public void searchPhone(){
    	String phone = getPara("phone");
    	Record user = Db.findFirst("select*from user_login where phone='"+phone+"'");
    	boolean result = false;
    	if(user!=null){
    		result = true;
    	}
    	Record re = new Record();
    	re.set("result", result);
    	//re.set("user_id", user.get("id"));
    	renderJson(re);
    }
    
    public void sendCode(){
    	
    	renderJson(true);
    }
    
    public void findPwd(){
    	String phone = getPara("phone");
    	String v_code = getPara("v_code");
    	String pwd = getPara("pwd");
    	String sha1Pwd = MD5Util.encode("SHA1", pwd);
    	
    	String code = "123";
    	
    	Record user = Db.findFirst("select * from user_login where phone=?",phone);
    	boolean result = false;
    	String errmsg = "";
    	if(StringUtils.isNotBlank(code)){
    		if(code.equals(v_code)){
        		user.set("password", sha1Pwd);
            	result = Db.update("user_login",user);
        	}else{
        		errmsg = "验证码不正确";
        	}
    	}
    	
    	Record re = new Record();
    	re.set("result", result);
    	re.set("errmsg",errmsg);
    	renderJson(re);
    }
}
