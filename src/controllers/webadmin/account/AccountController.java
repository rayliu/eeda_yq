package controllers.webadmin.account;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.Map;

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
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountController extends Controller {

	private Logger logger = Logger.getLogger(AccountController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		  Long user_id = LoginUserController.getLoginUserId(this);
		  String sql = "select * from user_login where id="+user_id;
		  Record user = Db.findFirst(sql);
		  setAttr("user", user);
		  render(getRequest().getRequestURI()+"/pwd/edit.html");
	}
	
	public void info(){
		Long user_id = LoginUserController.getLoginUserId(this);
		String sql = "select * from wc_company where creator = "+user_id;
		Record user = Db.findFirst(sql);
		setAttr("user", user);
		render(getRequest().getRequestURI()+"/edit.html");
	}
	
	@Before(Tx.class)
	public void updateInfo(){
		Long user_id = LoginUserController.getLoginUserId(this);
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class); 
		String phone = (String) dto.get("phone");
		String telephone = (String) dto.get("telephone");
		String address = (String) dto.get("address");
		String about = (String) dto.get("about");
		String qq = (String) dto.get("qq");
		String contact = (String) dto.get("contact");
		String logo = (String) dto.get("logo");
		Record re = null;
		if(StringUtils.isNotBlank(user_id.toString())){
			re=Db.findFirst("select * from wc_company where creator = "+userId);
			re.set("telephone", phone);
			re.set("shop_telephone", telephone);
			re.set("about", about);
			re.set("address", address);
			re.set("qq", qq);
			re.set("contact", contact);
			re.set("logo", logo);
			Db.update("wc_company",re);
		}	
		renderJson(re);
	}
	
	@Before(Tx.class)
	public void update(){
		Long user_id = LoginUserController.getLoginUserId(this);
		String user_name=getPara("user_name");
		String password=getPara("password");
		String sql="update user_login set user_name=?,password=? where id=?";
		Db.update(sql, user_name,password,user_id);
		renderJson(true);
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
