package controllers.bizadmin.account;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;
import models.wedding.WcCompany;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.MD5Util;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountController extends Controller {

	private Logger logger = Logger.getLogger(AccountController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
	    Record noticeRec = Db.findFirst("select * from wc_notice order by id desc;");
	    setAttr("notice", noticeRec.get("content"));
	    
	    
		render(getRequest().getRequestURI()+"/list.html");
	}
	

	public void modify_info(){
        Long userId = LoginUserController.getLoginUserId(this);
        
        Record rec = Db.findFirst("select * from wc_company where creator =?", userId);
        String province = "";
        String city = "";
        String district = "";
        if(rec != null){
        	  province = rec.getStr("province");
              city = rec.getStr("city");
              district = rec.getStr("district");
        }
        
        String p_c_d = null;
        String p_c_d_name = null;
        if(StringUtils.isNotBlank(district)){
        	Record districtRe = Db.findFirst("select name from location where code = ?",district);
        	Record cityRe = Db.findFirst("select name from location where code = ?",city);
        	Record provinceRe = Db.findFirst("select name from location where code = ?",province);
        	p_c_d = province+"-"+city+"-"+district;
        	p_c_d_name = provinceRe.getStr("name")+"-"+cityRe.getStr("name")+"-"+districtRe.getStr("name");
        }else if(StringUtils.isNotBlank(city)){
        	Record cityRe = Db.findFirst("select name from location where code = ?",city);
        	Record provinceRe = Db.findFirst("select name from location where code = ?",province);
        	p_c_d_name = provinceRe.getStr("name")+"-"+cityRe.getStr("name");
        	p_c_d = province+"-"+city;
        }
        
        setAttr("p_c_d", p_c_d);
        setAttr("p_c_d_name", p_c_d_name);
        setAttr("company", rec);
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void save_info(){
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String orderId = (String) dto.get("id");
        WcCompany order = new WcCompany();
        if(StringUtils.isNotBlank(orderId)){
        	//update
        	DbUtils.setModelValues(dto, order);
        	order.set("update_by", userId);
        	order.set("update_time", new Date());
        	order.update();
        }else{
        	//create
        	DbUtils.setModelValues(dto, order);
        	order.set("creator", userId);
        	order.set("create_time", new Date());
        	order.save();
        }
        renderJson(order);
        //setAttr("company", order);
        
        //render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void check_pwd(){
		boolean result = false;
        String old_password = getPara("old_password");
        UserLogin user = LoginUserController.getLoginUser(this);
        String password = user.getStr("password");
        String sha1Pwd = MD5Util.encode("SHA1", old_password);
        if(password.equals(sha1Pwd)){
        	result = true;
        }
        renderJson(result);
    }
	
	public void modify_pwd(){
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	
	public void update_pwd(){
		String password = getPara("password");
        String sha1Pwd = MD5Util.encode("SHA1", password);
        UserLogin ul = LoginUserController.getLoginUser(this);
        ul.set("password", sha1Pwd).update();
        
        renderJson(ul);
	}
	
    public void saveFile() throws Exception{
    	Record re = new Record();
    	try {
            UploadFile file = getFile();
            re.set("name", file.getFileName());
        } catch (Exception e) {
            e.getMessage();
        }
    	renderJson(re);
    }
    
}
