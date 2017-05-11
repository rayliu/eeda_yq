package controllers.eeda;

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
import models.yh.profile.OfficeCofig;

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
import com.jfinal.core.JFinal;
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

public class MainController extends Controller {
	private Log logger = Log.getLog(MainController.class);
    // in config route已经将路径默认设置为/eeda
    // me.add("/eeda", controllers.yh.AppController.class, "/eeda");
    Subject currentUser = SecurityUtils.getSubject();

    private boolean isAuthenticated() {

        // remember me 处理，自动帮user 登陆
        if (!currentUser.isAuthenticated() && currentUser.isRemembered()) {
            Object principal = currentUser.getPrincipal();
            if (null != principal) {
                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='" + String.valueOf(principal) + "' and (is_stop = 0 or is_stop is null)");
                if(user==null){//这里是预防user使用了remember me, 但是user ID在表中已删除
                	redirect("/login");
                	return false;
                }
                String password = user.getStr("password");
                UsernamePasswordToken token = new UsernamePasswordToken(user.getStr("user_name"), password);
                token.setRememberMe(true);
                currentUser.login(token);// 登录
            }
        }

        if (!currentUser.isAuthenticated()) {
            redirect("/login");
            return false;
        }
        setAttr("userId", currentUser.getPrincipal());
        // timeout:-1000ms 这样设置才能永不超时 
    	currentUser.getSession().setTimeout(-1000L);
    	
        return true;
    }
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	setSysTitle();
        if (isAuthenticated()) {
        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?", currentUser.getPrincipal());
        	
            if(user.get("c_name")!=null&&!"".equals(user.get("c_name"))){
            	setAttr("userId", user.get("c_name"));
            }else{
            	setAttr("userId", currentUser.getPrincipal());
            }
            
            setAttr("user_login_id", currentUser.getPrincipal());
            setAttr("login_time",user.get("last_login"));
            setAttr("lastIndex",user.get("last_index") == null ? "pastOneDay" : user.get("last_index"));

            //更新当前用户最后一次登陆的时间
            updateLastLogin(user);
            
            //查询当前用户权限，并且将其设置到会话当中
            setPermissionToSession();	

            String savedRequestUrl = this.getSessionAttr(ShiroKit.getSavedRequestKey());
            if(savedRequestUrl!=null){
                System.out.println("111111");
            	setSessionAttr(ShiroKit.getSavedRequestKey(), null);
                if(savedRequestUrl.split("/").length>2){
                    int index = savedRequestUrl.lastIndexOf("/");
                    savedRequestUrl = savedRequestUrl.substring(0, index);
                }
            	redirect(savedRequestUrl);
            }else{
                //当模块没有正确配置url路由时，显示无权限
                if(getPara()!=null){
                    String moduleUrl = "/"+getPara();
                    System.out.println("moduleUrl:"+moduleUrl);
                    
                    List<String> actionList = JFinal.me().getAllActionKeys();
                    if(!actionList.contains(moduleUrl)){
                        renderError(403, "/eeda/noPermission.html");
                        return;
                    }
                }
                //以下为显示login page的处理
            	String officeConfig="select oc.index_page_path from office_config oc "
            			+ " where oc.office_id =?";
            	Record rec = Db.findFirst(officeConfig, user.getLong("office_id"));
            	if(rec == null || rec.getStr("index_page_path") == null){
            	    if(getAttr("modules")==null){
                        redirect("/");
                    }else{
                        List<Record> moduleList = (List<Record>)getAttr("modules");
                        List<Record> orderList = (List<Record>)moduleList.get(0).get("orders");
                        Record firstModule = (Record)orderList.get(0);
                        
                        redirect(firstModule.getStr("url"));
                    };
            	}else{
            		render(rec.getStr("index_page_path"));//显示不同URL对应的不同的login页面
            	}
            }
        }

    }

	private void setPermissionToSession() {
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		getCurrentPermission getPermission = getCurrentPermission.getInstance();
		Map<String,String> map = getPermission.currentHasPermission(currentUser,pom);
		setSessionAttr("permissionMap", map);
		setAttr("permissionMap", map);
	}

	private void updateLastLogin(UserLogin user) {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");//可以方便地修改日期格式
		String currentTime = dateFormat.format( now );
		user.set("last_login", currentTime);
		user.update();
	}

    public void login() {
        String register= getPara("register");
        if(register!=null){
            setAttr("registerMsg", register);
        }
        String strLoginPagePath = "/eeda/login.html";
        
        HttpServletRequest request = getRequest();
        String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
        
        logger.debug("Current host path:"+basePath);
        OfficeCofig of = OfficeCofig.dao.findFirst("select * from office_config where domain like '"
                +serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的login信息, 不知道显示那个系统的login页面, 跳到公司首页
            redirect("/");
        }else{
            if(StringUtils.isNotEmpty(of.getStr("index_page_path")))
                strLoginPagePath = of.getStr("index_page_path");
        }

                
    	if (isAuthenticated()) {//如果已经登录, 跳转到系统管理平台首页
    		redirect("/");
    	}
        String username = getPara("username");
        
        setSysTitle();
        
        if (username == null) {
            render(strLoginPagePath);
            return;
        }
        String sha1Pwd = MD5Util.encode("SHA1", getPara("password"));
        System.out.println("sha1Pwd:"+sha1Pwd);
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
            	/*setAttr("login_time",user.get("last_login"));*/
            	redirect("/");
            	//render("/eeda/index.html");
            }else{
            	setAttr("userId",currentUser.getPrincipal());
            	/*setAttr("login_time",user.get("last_login"));*/
            	redirect("/");
            	//render("/eeda/index.html");
            };
          
            
        } else {
            setAttr("errMsg", errMsg);
            render(strLoginPagePath);
        }
    }

	private void setSysTitle() {
		String serverName = getRequest().getServerName();
        String basePath = getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest().getServerPort()+"/";
        
        logger.debug(serverName);
        OfficeCofig of = OfficeCofig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeCofig();
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
        redirect("/login");
    }

    // 使用common-email, javamail
    public void testMail() throws Exception {
        Email email = new SimpleEmail();
        email.setHostName("smtp.exmail.qq.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("",""));
        email.setSSLOnConnect(true);

        email.setFrom("");
        email.setSubject("忘记密码");
        email.setMsg("你的密码已重置");
        email.addTo("");
        email.send();
        
    }
    
    
    @Before(SetAttrLoginUserInterceptor.class)
    public void m() {
        String module_id = getPara(0);
        String param1 = getPara(1);
        
        String page = "";
        if(param1 == null){
            page = "/eeda/profile/module/searchOrder.html";
        }else{
            if(StringUtils.isNumeric(param1)){//edit
                setAttr("order_id", param1);
            }else if("add".equals(param1)){
                
            }
            page = "/eeda/profile/module/editOrder.html";
        }

        UserLogin user = LoginUserController.getLoginUser(this);
        //查询当前用户菜单
        String sql ="select distinct module.* from modules o, modules module "
                +"where o.parent_id = module.id and o.office_id=? and o.status = '启用' order by seq";
        List<Record> modules = Db.find(sql, user.get("office_id"));
        for (Record module : modules) {
            sql ="select * from modules where parent_id =? and status = '启用' order by seq";
            List<Record> orders = Db.find(sql, module.get("id"));
            module.set("orders", orders);
        }
        setAttr("modules", modules);
        setAttr("module_id", module_id);
        render(page);
    }
    
    @Before(Tx.class)
    public void m_save() {
        String jsonStr=getPara("params");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String orderId = dto.get("id").toString();
        if(StringUtils.isNotEmpty(orderId)){//update
            EedaCommonHandler.commonUpdate(dto);
        }else{//insert
            orderId = EedaCommonHandler.commonInsert(dto);
        }
        
        //返回order
//        String module_id = dto.get("module_id").toString();
//        ModuleController mc = new ModuleController();
//        Record sRec = mc.getOrderStructureDto(module_id);
//        sRec.set("id", orderId);
//        Record orderDto =EedaCommonHandler.getOrderDto(sRec.toJson());
        Record orderDto = new Record();
        orderDto.set("id", orderId);
        renderJson(orderDto);
    }

    
    public void m_getOrderData() {
        Record orderDto = new Record();
        String jsonStr=getPara("params");
        orderDto = EedaCommonHandler.getOrderDto(jsonStr);
        renderJson(orderDto);
    }
    
    public void m_search() {
        Enumeration<String>  paraNames= getParaNames();
        Map map= EedaCommonHandler.searchOrder(paraNames, getRequest());
        renderJson(map);
    }
    
}
