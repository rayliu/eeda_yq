package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Office;
import models.ParentOfficeModel;
import models.Warehouse;
import models.yh.profile.OfficeCofig;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import config.EedaConfig;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OfficeConfigController extends Controller{

    private Logger logger = Logger.getLogger(OfficeConfigController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void index() {
		Office office = Office.dao.findById(pom.getParentOfficeId());
		OfficeCofig officeConfig = OfficeCofig.dao.findFirst("select * from office_config where office_id = ?",pom.getParentOfficeId());
		setAttr("lu", office);
		setAttr("officeConfig", officeConfig);
		render("/yh/profile/officeConfig/edit.html");
	}
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_CREATE, PermissionConstant.PERMSSION_W_UPDATE}, logical=Logical.OR)
	public void save() {
		UploadFile uploadFile = getFile("fileupload");
        String spId = getPara("sp_id");
        String officeId = getPara("officeSelect");	
		Warehouse warehouse = null;
		String id = getPara("warehouse_id");	
		
		//Contact contact = null;
		//Party party = null;
		Date createDate = Calendar.getInstance().getTime();
		if (id != null && !id.equals("")) {
			//party = Party.dao.findFirst("select * from party where id = ?", getPara("notifyPartyId"));
			//contact = Contact.dao.findFirst("select * from contact where id = ?", party.get("contact_id"));
			//setContact(contact);
			//contact.update();
			warehouse = Warehouse.dao.findById(id);
			warehouse.set("warehouse_name", getPara("warehouse_name"));
			warehouse.set("warehouse_address", getPara("warehouse_address"));
			warehouse.set("status", getPara("warehouseStatus"));
			warehouse.set("warehouse_desc", getPara("warehouse_desc"));
			warehouse.set("notify_name", getPara("notify_name"));
			warehouse.set("notify_mobile", getPara("notify_mobile"));
			warehouse.set("location", getPara("location"));
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			//warehouse.set("notify_party_id", party.get("id"));
			if (getPara("warehouseType") != null && getPara("warehouseType").equals("ownWarehouse")) {
            	spId = null;
            }else{
            	officeId = null;
            }
            if(spId != null && !"".equals(spId)){
            	warehouse.set("sp_id", spId);
            }
            if(officeId != null && !"".equals(officeId)){
            	warehouse.set("office_id", officeId);
            }
			warehouse.set("warehouse_type", getPara("warehouseType"));
			warehouse.update();
		} else {
			//party = new Party();
			//contact = new Contact();
			//setContact(contact);
			//contact.save();
			//party.save();
			//party.set("party_type", Party.PARTY_TYPE_NOTIFY_PARTY);
			//party.set("contact_id", contact.get("id"));
			//party.update();
			warehouse = new Warehouse();
			warehouse.set("warehouse_name", getPara("warehouse_name"))
					 .set("warehouse_address", getPara("warehouse_address"))
					 .set("warehouse_desc", getPara("warehouse_desc"))
					 .set("status", getPara("warehouseStatus"))
					 .set("warehouse_area", getPara("warehouse_area"));	
			warehouse.set("notify_name", getPara("notify_name"));
			warehouse.set("notify_mobile", getPara("notify_mobile"));
			warehouse.set("location", getPara("location"));				 
			if(uploadFile != null){
				warehouse.set("path", uploadFile.getFileName());
			}
			//warehouse.set("notify_party_id", party.get("id"));
			if (getPara("warehouseType") != null && getPara("warehouseType").equals("ownWarehouse")) {
            	spId = null;
            }else{
            	officeId = null;
            }
            if(spId != null && !"".equals(spId)){
            	warehouse.set("sp_id", spId);
            }
            if(officeId != null && !"".equals(officeId)){
            	warehouse.set("office_id", officeId);
            }
			warehouse.set("warehouse_type", getPara("warehouseType"));
			//所属网点
			/*String name = (String) currentUser.getPrincipal();
	 		List<UserLogin> users = UserLogin.dao
	 				.find("select * from user_login where user_name='" + name + "'");
	 		warehouse.set("office_id", users.get(0).get("office_id"));*/
			
			warehouse.save();
		}
		setAttr("saveOK", true);
		redirect("/warehouse");
	}
	
	//更改系统配置信息
	public void updateOfficeConfig() {
		String logofileName=null;
		String bgfileName=null;
		UploadFile logofileImg = getFile("logofile");
		UploadFile bgfileimg = getFile("bgfile");
		if(getFile("logofile")!=null&&!"".equals(getFile("logofile"))){
	    	File file = logofileImg.getFile();
	    	logofileName = file.getPath().substring(17);
		}
        
		if(getFile("bgfile")!=null&&!"".equals(getFile("bgfile"))){
	    	File bg = bgfileimg.getFile();
	    	bgfileName = bg.getPath().substring(17);
		}

    	
    	String office_id = getPara("office_id");
    	String office_name = getPara("office_name");
    	String abbr = getPara("abbr");
    	
    	String secondDomain = getPara("secondDomain");
    	String email = getPara("email");
    	String domain = getPara("domain");
    	String serverType = getPara("serverType");
    	String portNo = getPara("portNo");
    	String password = getPara("password");
    	String sslayer = getPara("sslayer");
    	if(sslayer==null||sslayer==""){
    		sslayer="false";
    	}
    	if(sslayer.equals("on")){
    		sslayer="true";
    	}
		Office office = Office.dao.findById(office_id);
		office.set("office_name", office_name);
		office.set("abbr",abbr);
		office.update();
		
		OfficeCofig officeConfig = OfficeCofig.dao.findFirst("select * from office_config where office_id = ?",office_id);
		if(logofileName != null && !"".equals(logofileName)){
			logofileName=logofileName.replace(logofileName.substring(0,1), "/");
			officeConfig.set("logo", logofileName);
		}
		if(bgfileName != null && !"".equals(bgfileName)){
			bgfileName = bgfileName.replace(bgfileName.substring(0,1), "/");
			officeConfig.set("login_bg",bgfileName );
		}
		String secondDomain2=officeConfig.get("secondDomain");
		
		officeConfig.set("secondDomain",secondDomain );
		officeConfig.set("email",email );
		officeConfig.set("domain",domain );
		officeConfig.set("serverType",serverType );
		officeConfig.set("portNo",portNo );
		officeConfig.set("password",password );
		officeConfig.set("sslayer",sslayer );
		officeConfig.update();
		
		//二级域名改变的时候自动发送邮件
		if(!secondDomain2.equals(officeConfig.get("secondDomain"))){
	        Email emailTo = new SimpleEmail();
	        emailTo.setHostName("smtp.exmail.qq.com");
	        emailTo.setSmtpPort(465);
	        
	        /*输入公司的邮箱和密码*/
	        /*EedaConfig.mailUser, EedaConfig.mailPwd*/
	        emailTo.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));        
	        emailTo.setSSLOnConnect(true);
	        try{
	            /*EedaConfig.mailUser*/
	            emailTo.setFrom(EedaConfig.mailUser);//设置发信人
	            emailTo.setSubject("域名更新");
	            emailTo.setSubject("域名更新信息");
	            Date date = new Date();
	            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	            String newDate = sf.format(date);
	            String basePath=null;
	            if(secondDomain==null||"".equals(secondDomain)){
	            	basePath =newDate + "  " + office_name + "已经删除掉了二级域名.";
	            }else{
	            	 basePath =newDate + "  " + office_name + "的二级域名已经更改为：" + secondDomain + "。";
	            }  
	            emailTo.setMsg(basePath);
	            /*添加邮件收件人*/
	            emailTo.addTo("ray_liuyu@qq.com");//设置收件人
	            //emailTo.addTo("kate.lin@eeda123.com");
	            emailTo.send();
	         }catch(Exception e){
	             e.printStackTrace();
	         }finally{
	         }
		}
		
		setAttr("officeConfig", officeConfig);
        setAttr("lu", office);
        render("/yh/profile/officeConfig/edit.html");
    }
	public void searchAllOffice() {	
		String a="select o.id,o.office_name,o.is_stop from office o where o.id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')";
		List<Record> offices = Db.find("select o.id,o.office_name,o.is_stop from office o where o.id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')");
		renderJson(offices); 
	}
	public void searchAllWarehouse() {
		String officeName = getPara("office_name")==null?"":getPara("office_name");
		List<Record> offices=null;
		if("".equals(officeName)){
			 offices = Db.find("select w.* from warehouse w left join office o on o.id = w.office_id where o.id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')");
		}else{
			offices=Db.find("select w.* from warehouse w left join office o on o.id = w.office_id where o.id='"+officeName+"' and o.id IN (SELECT office_id FROM user_office WHERE user_name = '"+currentUser.getPrincipal()+"')");
		}
		
		renderJson(offices); 
	}

	//邮箱发送测试
	public void test(){
			boolean valid=false;
		    String email = getPara("email");
    	    String password = getPara("password");
    	    String portNo = getPara("portNo");
    	    String serverType = getPara("serverType");
    	    String sslayer = getPara("sslayer");
    	    
	        Email emailTo = new SimpleEmail();
	        emailTo.setHostName(serverType);
	        emailTo.setSmtpPort(Integer.parseInt(portNo));
	        //输入公司的邮箱和密码
	        //EedaConfig.mailUser, EedaConfig.mailPwd
	        emailTo.setAuthenticator(new DefaultAuthenticator(email, password));        
	        emailTo.setSSLOnConnect(Boolean.parseBoolean(sslayer));
	        try{
	            //EedaConfig.mailUser
	            emailTo.setFrom(email);//设置发信人
	            emailTo.setSubject("发送测试");
	            Date date = new Date();
	            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	            String newDate = sf.format(date);
	            String basePath=null;
	            basePath =newDate + "邮箱" + email + "测试成功。";
	            emailTo.setMsg(basePath);
	            //添加邮件收件人
	            emailTo.addTo(email);//设置收件人
	            //emailTo.addTo("ray_liuyu@qq.com");
	            emailTo.send();
	            valid=true;
	         }catch(Exception e){
	        	 System.out.println(e.getMessage());
	            //e.printStackTrace();
	         }
	        Record check  = new Record();
	        check.set("valid", valid);
	        renderJson(check);
	}

}
