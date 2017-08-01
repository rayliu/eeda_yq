package controllers.bizadmin.register;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;


public class RegisterController extends Controller {

	private Logger logger = Logger.getLogger(RegisterController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/index.html");
	}
    
	public void info(){
		Record user = new Record();
		String login_name = getPara("login_name");
		String phone = getPara("phone");
		String password = getPara("password");
		user.set("login_name", login_name);
		user.set("phone", phone);
		user.set("password", password);
		setAttr("userInfo",user);
		List<Record> cateList = Db.find("select * from category");
		setAttr("cateList", cateList);
		
		
		render(getRequest().getRequestURI()+"/index.html");
	}
	
	
	public void exist(){
		boolean result = false;
		String userName = getPara("login_name");
		String sql = "select * from user_login where user_name = ?";
		Record re = Db.findFirst(sql,userName);
		if(re == null){
			result = true;
		}
		
		renderJson(result);
	}
	
	
	public void done(){
		String jsonStr=getPara("jsonStr");
		Gson gson=new Gson();
		Map<String,?> dto = gson.fromJson(jsonStr, HashMap.class);
		Record re=new Record();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String type=(String)dto.get("type");
		if(type.equals("1")){
			String id_card = (String) dto.get("id_card");
			re.set("id_card", id_card);
		}else{
			String company_pic = (String)dto.get("company_pic");
			String c_name = (String)dto.get("company_name");
			re.set("company_pic", company_pic);
			re.set("c_name", c_name);
		}
		String login_name = (String) dto.get("login_name");
		String phone = (String) dto.get("phone");
		String telephone = (String) dto.get("telephone");
		String password = (String) dto.get("password");
		String contact = (String) dto.get("contact");
		String shop_address = (String) dto.get("shop_address");
		String shop_telephone = (String) dto.get("shop_telephone");
		String about = (String)dto.get("about");
		String shop_city = (String) dto.get("shop_city");
		String trade_type = (String) dto.get("trade_type");
		String qq = (String) dto.get("qq");
		String logo = (String) dto.get("logo");
		re.set("create_time", df.format(new Date()));
		re.set("user_name", login_name);
		re.set("contact", contact);
		re.set("is_stop", 1);
		re.set("password", password);
		re.set("phone", phone);
		re.set("telephone", telephone);
		re.set("tarde_type", trade_type);
		re.set("address", shop_address);
		re.set("qq", qq);
		re.set("about", about);
		re.set("logo", logo);
		Db.save("user_login", re);
        render(getRequest().getRequestURI()+"/index.html");
    }
	
	public void saveFile(){
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
