package controllers.webadmin;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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
import controllers.util.DbUtils;
import controllers.util.EedaCommonHandler;
import controllers.util.MD5Util;
import controllers.util.ParentOffice;
import controllers.util.getCurrentPermission;

public class WebAdminController extends Controller {
	private Log logger = Log.getLog(WebAdminController.class);
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {
        // remember me 处理，自动帮user 登陆
        if (!currentUser.isAuthenticated() && currentUser.isRemembered()) {
            Object principal = currentUser.getPrincipal();
            if (null != principal) {
                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + String.valueOf(principal) + "' and (is_stop = 0 or is_stop is null)");
                if(user==null){//这里是预防user使用了remember me, 但是user ID在表中已删除
                	redirect("/WebAdmin/login");
                	return false;
                }
                String password = user.getStr("password");
                UsernamePasswordToken token = new UsernamePasswordToken(user.getStr("user_name"), password);
                token.setRememberMe(true);
                currentUser.login(token);// 登录
            }
        }

        if (!currentUser.isAuthenticated()) {
            redirect("/WebAdmin/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        // timeout:-1000ms 这样设置才能永不超时 
    	currentUser.getSession().setTimeout(-1000L);
    	
        return true;
    }
    
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        if (isAuthenticated()) {
        	//注册用户数
        	Record user = Db.findFirst("select count(1) total from user_login");
        	setAttr("user", user);
        	//上传产品
        	Record product = Db.findFirst("select count(1) total from wc_product");//全部（不管是否上架）
        	setAttr("product", product);
        	//“促”用户数
        	Record ad_cu = Db.findFirst("select count(1) total from wc_ad_deal group by creator");//全部（不管是否上架）
        	setAttr("ad_cu", ad_cu);
        	//“惠”用户数
        	Record ad_hui = Db.findFirst("select count(1) total from wc_ad_hui group by creator");//全部（不管是否上架）
        	setAttr("ad_hui", ad_hui);
        	//钻石商家数
        	Record diamond = Db.findFirst("select count(1) total from wc_ad_diamond group by creator");//全部（不管是否上架）
        	setAttr("diamond", diamond);
        	//上传案例数
        	Record wccase = Db.findFirst("select count(1) total from wc_case");//所有
        	setAttr("wccase", wccase);
        	//视频案例数
        	Record video = Db.findFirst("select count(1) total from video_case");
        	setAttr("wcvideo", video);
            render("/WebAdmin/dashBoard/list.html");
        }
    }

	private void updateLastLogin(UserLogin user) {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");//可以方便地修改日期格式
		String currentTime = dateFormat.format( now );
		user.set("last_login", currentTime);
		user.update();
	}

    public void login() {
        
        String strLoginPagePath = getRequest().getRequestURI()+"/login.html";
        
        HttpServletRequest request = getRequest();
        String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
       
        
        
    	if (isAuthenticated()) {//如果已经登录, 跳转到管理平台首页
    		redirect("/WebAdmin");
    	}else{
    	    String method = getRequest().getMethod();
    	    if("GET".equals(method)){
    	        render(strLoginPagePath);
    	        return;
    	    }
    	}
        String username = getPara("username");
        
        setSysTitle();
        
        if (username == null) {
            render(strLoginPagePath);
            return;
        }
        String sha1Pwd = MD5Util.encode("SHA1", getPara("password"));
        UsernamePasswordToken token = new UsernamePasswordToken(username, sha1Pwd );

        if (getPara("remember") != null && "Y".equals(getPara("remember")))
            token.setRememberMe(true);

        String errMsg = "";
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
        	
        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=? and (is_stop = 0 or is_stop is null)",currentUser.getPrincipal());
        	
        	
        	if(user==null){
            	errMsg = "用户名不存在或已被停用";
            	setAttr("errMsg", errMsg);
            	render(strLoginPagePath);
            }else if(user.get("c_name") != null && !"".equals(user.get("c_name"))){
            	setAttr("userId", user.get("c_name"));
            	
            	redirect("/WebAdmin");
            }else{
            	setAttr("userId",currentUser.getPrincipal());
            	
            	redirect("/WebAdmin");
            };
          
            
        } else {
            setAttr("errMsg", errMsg);
            render(strLoginPagePath);
        }
    }
    
    public void logOut(){
    	currentUser.logout();
    	redirect("/WebAdmin");
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
        redirect("/WebAdmin/login");
    }
    
    public void getLoginUser(){
    	UserLogin ul = LoginUserController.getLoginUser(this);
    	//商家入驻申请
    	String sql_nopass = "select * from user_login where status = '新建' ";
    	//横幅广告申请
    	String sql_banner = "select * from wc_ad_banner where status =  '新建'";
    	//手机推送申请
    	String sql_mobile = "select * from wc_ad_mobile_promotion where status = '新建'";
    	List<Record> nopass = Db.find(sql_nopass);
    	List<Record> banner = Db.find(sql_banner);
    	List<Record> mobile = Db.find(sql_mobile);
    	Record data = new Record();
    	data.set("nopass", nopass.size());
    	data.set("banner", banner.size());
    	data.set("mobile", mobile.size());
    	data.set("user", ul);
    	renderJson(data);
    }
    
    public void listLocation(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select * from location_management";
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql+" " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }
    
    @Before(Tx.class)
    public void addLocation(){
    	String address = getPara("address");
    	String[] detail = address.split("\\-");
    	String provice = "";
    	String city = "";
    	String district = "";
    	String location="";
    	Record re = new Record();
    	String code = detail.length>=1?detail[1]:detail[0];
    	re.set("code", code);
    	for(int i=0;i<detail.length;i++){
    		if(i==0){
    			provice = Db.findFirst("select * from location where code = "+detail[i]).getStr("name");
    		}
    		if(i==1){
    			city = "-"+Db.findFirst("select * from location where code = "+detail[i]).getStr("name");
    		}
    		if(i==2){
    			district = "-"+Db.findFirst("select * from location where code = "+detail[i]).getStr("name");
    		}
    	}
    	location = provice+city+district;
    	re.set("name", location);
    	re.set("is_active", 1);
    	re.set("create_time", new Date());
    	Db.save("location_management", re);
    	renderJson(true);
    }
    
    @Before(Tx.class)
    public void addCategory(){
    	String name = getPara("name");
    	Record re = new Record();
    	re.set("name", name);
    	Db.save("category", re);
    }
    
    @Before(Tx.class)
    public void deleteLocation(){
    	String id = getPara("id");
    	String sql = "delete from location_management where id = "+id;
    	Db.update(sql);
    	renderJson(true);
    }
    
    @Before(Tx.class)
    public void deleteCategory(){
    	String id = getPara("id");
    	String sql = "delete from category where id = "+id;
    	Db.update(sql);
    	renderJson(true);
    }
    
    
    public void listCategory(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select * from category";
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql+" " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

}
