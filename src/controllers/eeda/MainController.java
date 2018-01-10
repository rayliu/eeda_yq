package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.OfficeConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
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
    	//currentUser.getSession().setTimeout(-1000L);
    	
        return true;
    }
    
    public void index() {
    	String serverName = getRequest().getServerName();
        String basePath = getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest().getServerPort()+"/";
        
        logger.debug(serverName);
        OfficeConfig of = OfficeConfig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        setAttr("SYS_CONFIG",of);
        render("/eeda/index.html");
    }
    @Before(EedaMenuInterceptor.class)
    public void home() {
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
            	setSessionAttr(ShiroKit.getSavedRequestKey(), null);
                if(savedRequestUrl.split("/").length>2){
                    int index = savedRequestUrl.lastIndexOf("/");
                    savedRequestUrl = savedRequestUrl.substring(0, index);
                }
            	redirect(savedRequestUrl);
            }else{
            	String officeConfig="select oc.index_page_path from office_config oc "
            			+ " where oc.office_id =?";
            	Record rec = Db.findFirst(officeConfig, user.getLong("office_id"));
            	if(rec == null || rec.getStr("index_page_path") == null){
            	    if(getAttr("modules")==null){
                        redirect("/");                    	
                    }else{
                        List<Record> moduleList = (List<Record>)getAttr("modules");
                        if(moduleList.size()==0){
                        	redirect("/module");
                        }else{
                        	List<Record> orderList = (List<Record>)moduleList.get(0).get("orders");
                            Record firstModule = (Record)orderList.get(0);
                            
                            redirect(firstModule.getStr("url"));
                        }
                        
                    };
            	}else{
            		render(rec.getStr("index_page_path"));
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

    	if (isAuthenticated()) {
    		redirect("/home");
    	}
    	
    	//获取子系统的名字,显示在登陆页面
    	handleLoginSubtitle();
    	
        String username = getPara("username");
        
        setSysTitle();
        
        if (username == null) {
            render("/eeda/login.html");
            return;
        }
        String sha1Pwd = MD5Util.encode("SHA1", getPara("password"));
        UsernamePasswordToken token = new UsernamePasswordToken(username, sha1Pwd );

        if (getPara("remember") != null && "Y".equals(getPara("remember")))
            token.setRememberMe(true);

        Record errRe = new Record();
        String errMsg = "";
        try {
            currentUser.login(token);
//            if (getPara("remember") != null && "Y".equals(getPara("remember"))){
//                // timeout:-1000ms 这样设置才能永不超时 
//            	currentUser.getSession().setTimeout(-1000L);
//            }

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
        errRe.set("errMsg", errMsg);
        if (errMsg.length()==0) {
        	UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=? and (is_stop = 0 or is_stop is null)", username);
        	if(user==null){
            	errMsg = "用户名不存在或已被停用";
            	setAttr("errMsg", errMsg);
            	errRe.set("errMsg", errMsg);
            	renderJson(errRe);
            }else if(user.get("c_name") != null && !"".equals(user.get("c_name"))){
            	setAttr("userId", user.get("c_name"));
            	
            	setLoginLog(user);

            	redirect("/home");
            	//render("/eeda/index.html");
            }else{
            	setAttr("userId",currentUser.getPrincipal());
            	setLoginLog(user);
            	redirect("/home");
            	//render("/eeda/index.html");
            };
        } else {
            renderJson(errRe);
        }
    }
    
    private void handleLoginSubtitle(){
        
        StringBuffer url = getRequest().getRequestURL();
        String systemName = "";
        if(url.toString().contains("enkyosys")){
        	systemName = "远桥供应链管理系统";
        }else{
        	systemName = "检单供应链管理系统";
        }
        String prefix = url.toString().substring(7).split("\\.")[0];
        logger.debug("prefix:"+prefix);
        if("booking".equals(prefix)){
            systemName = "Booking管理系统";
        }else if("forwarder".equals(prefix)){
             systemName = "货代管理系统";
        }else if("custom".equals(prefix)){
            systemName = "关务管理系统";
        }else if("trans".equals(prefix)){
            systemName = "运输管理系统";
        }else if("trade".equals(prefix)){
            systemName = "贸易管理系统";
        }
           
        setAttr("system_name", systemName);
        
    }
    
    private void setLoginLog(UserLogin user) {

		String localip = getIpAddress(this.getRequest());
        Record rec = new Record();
        rec.set("log_type", "登录");
        rec.set("create_stamp", new Date());
        rec.set("user_id", user.get("id"));
        rec.set("ip", localip);
        rec.set("office_id", user.getLong("office_id"));
        
        Db.save("sys_log", rec);
    }
    
    public static String getIpAddress(HttpServletRequest request){   
         String ipAddress = request.getHeader("x-forwarded-for");
          
         if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
              ipAddress = request.getHeader("Proxy-Client-IP");
         }
         if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
              ipAddress = request.getHeader("WL-Proxy-Client-IP");
         }
         if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
             ipAddress = request.getRemoteAddr();
             
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                 //根据网卡获取本机配置的IP地址
                 InetAddress inetAddress = null;
                 try {
                	 if(inetAddress != null){
                		 inetAddress = InetAddress.getLocalHost();
                	 }
                 } catch (UnknownHostException e) {
                     e.printStackTrace();
                 }
                 ipAddress = inetAddress.getHostAddress();
             }
         }
         
         //对于通过多个代理的情况，第一个IP为客户端真实的IP地址，多个IP按照','分割
         if(null != ipAddress && ipAddress.length() > 15){
             //"***.***.***.***".length() = 15
             if(ipAddress.indexOf(",") > 0){
                 ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
             }
         }
         return ipAddress;
    }
    
	private void setSysTitle() {
		String serverName = getRequest().getServerName();
        //String basePath = getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest().getServerPort()+"/";
        
        logger.debug(serverName);
        OfficeConfig of = OfficeConfig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeConfig();
        	of.set("system_title", "易达物流");
        	of.set("logo", "/eeda/img/eeda_logo.ico");
        }
//        UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name ='"+userName+"' and is_main=1");
//        if(uo != null){
//            Office office = Office.dao.findById(uo.get("office_id"));
//            setAttr("office_name", office.get("office_name"));
//        }
        setAttr("SYS_CONFIG", of);
	}

    public void logout() {
        currentUser.logout();
        redirect("/login");
    }

    // 使用common-email, javamail
    public void testMail() throws EmailException{
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
    
    public void getTodoList(){
        Map<String,Object> orderMap = new HashMap<String,Object>();
        String pageIndex = getPara("sEcho");
        String sLimit = "";
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        Calendar pastDay = Calendar.getInstance();
        pastDay.add(Calendar.DAY_OF_WEEK, -5);
        String sql = " select * from (SELECT "
                +"     'PS' type, dor.id, dor.order_no, cast(dor.business_stamp as char) business_stamp, "
                +"     (select group_concat(serial_no separator ',') from transfer_order_item_detail toid where toid.delivery_id = dor.id) serial_no,"
                +"     status, dor.route_from, lf.name from_name, dor.route_to, lt.name to_name, "
                +" 	   ifnull(GROUP_CONCAT(cast(o.id as char)),(select GROUP_CONCAT(cast(o.id as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = dor.route_from)) office_id,"
                +"     ifnull(GROUP_CONCAT(cast(o.office_name as char)),(select GROUP_CONCAT(cast(o.office_name as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = dor.route_from)) office_name,"
                +"		(case   when (select l.id from location l  "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = dor.route_from and l2.pcode = 1) is null"
                +" 		then"
                +" 		(select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
                +" 		where l3.code = dor.route_from)"
                +" 		when "
                +" 		(select l.id from location l "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = dor.route_from  and l2.pcode = 1) is not null"
                +" 		then"
                +" 		 (select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		where l2.code = dor.route_from)"
                +" 		end"
                +" 		) province "
                +" FROM"
                +"     delivery_order dor"
                +"     left join location lf on lf.code = dor.route_from"
                +"     left join location lt on lt.code = dor.route_to"
                +"     left join office o on o.location = dor.route_from"
                +"     left join user_office uo on o.id = uo.office_id and uo.user_name = '"+currentUser.getPrincipal()+"'"
                +" WHERE"
                +"     status = '新建'"
                +"         AND (business_stamp > DATE_SUB(NOW(), INTERVAL 5 DAY)"
                +"         OR NOW() >= business_stamp)"
                +" union"
                +"    select 'YS' type, tor.id, tor.order_no, '' business_stamp, '' serial_no, group_concat(distinct tor.status separator ',') status, "
                +"    tor.route_from, lf.name from_name, tor.route_to, lt.name to_name, "
                +"    ifnull(GROUP_CONCAT(cast(o.id as char)),(select GROUP_CONCAT(cast(o.id as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = tor.route_to)) office_id, "
                +"    ifnull(GROUP_CONCAT(cast(o.office_name as char)),(select GROUP_CONCAT(cast(o.office_name as char)) from office o LEFT JOIN location l on l.pcode = o.location where l.code = tor.route_to)) office_name,"
                +"		(case   when (select l.id from location l  "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = tor.route_to and l2.pcode = 1) is null"
                +" 		then"
                +" 		(select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		LEFT JOIN location l3 on l3.pcode = l2.code"
                +" 		where l3.code = tor.route_to)"
                +" 		when "
                +" 		(select l.id from location l "
                +" 		LEFT JOIN location l2 on l2.code = l.pcode "
                +" 		where l.code = tor.route_to  and l2.pcode = 1) is not null"
                +" 		then"
                +" 		 (select l.code from location l "
                +" 		LEFT JOIN location l2 on l2.pcode = l.code"
                +" 		where l2.code = tor.route_to)"
                +" 		end"
                +" 		) province "
                +" from transfer_order tor"
                +"      left join depart_pickup dp on tor.id = dp.order_id"
                +"      left join depart_transfer dt on tor.id = dt.order_id "
                +"      left join location lf on lf.code = tor.route_from"
                +"      left join location lt on lt.code = tor.route_to"
                +"      left join office o on o.location = tor.route_to"
                +"      left join user_office uo on o.id = uo.office_id and uo.user_name = '"+currentUser.getPrincipal()+"'"
                +"    where tor.status not in ('新建', '已签收', '已入库' ,'已收货','配送中', '取消', '部分配送中', '手动删除', '已投保', '部分已签收')"
                +"    group by tor.id) B"
                +" 	  where "
                +" 	  province IN ( SELECT l.pcode FROM user_office uo  "
                +" 	  LEFT JOIN office o on o.id = uo.office_id "
                +" 	  LEFT JOIN location l on l.`code` = o.location"
                +" 	  WHERE"
                +" 	  user_name = '" + currentUser.getPrincipal() +"')";
//                + "office_id in (select office_id from user_office where user_name = '" + currentUser.getPrincipal() +"')";
        
        Record rec = Db.findFirst("select count(1) total from (" + sql + ") A");
        
        List<Record> list = Db.find(sql + sLimit);
        
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", list);
        renderJson(orderMap);
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
        if(user==null){
			return;
		}
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
    
    public void layui() {
        render("/larrycms/admin/index.html");
    }
  
}
