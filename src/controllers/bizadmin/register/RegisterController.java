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
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;


public class RegisterController extends Controller {

	private Logger logger = Logger.getLogger(RegisterController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/index.html");
	}
    
	@Before(Tx.class)
	public void info(){
		String login_name = getPara("login_name");
		String phone = getPara("phone");
		String password = getPara("password");
		Record re = new Record();
		re.set("user_name", login_name);
		re.set("phone", phone);
		re.set("password", password);
		re.set("is_stop", 1);
		Db.save("user_login", re);
		setAttr("creator",re.get("id"));
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
	
	@Before(Tx.class)
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
		String telephone = (String) dto.get("telephone");
		String contact = (String) dto.get("contact");
		String shop_address = (String) dto.get("shop_address");
		String shop_telephone = (String) dto.get("shop_telephone");
		String about = (String)dto.get("about");
		String shop_city = (String) dto.get("shop_city");
		String shop_province = (String) dto.get("shop_province");
		String shop_district = (String) dto.get("shop_district");
		String trade_type = (String) dto.get("trade_type");
		String qq = (String) dto.get("qq");
		String logo = (String) dto.get("logo");
		String creator = (String) dto.get("creator");
		re.set("create_time", df.format(new Date()));
		re.set("contact", contact);
		re.set("telephone", telephone);
		re.set("trade_type", trade_type);
		re.set("province", shop_province);
		re.set("city", shop_city);
		re.set("district", shop_district);
		re.set("address", shop_address);
		re.set("qq", qq);
		re.set("user_type", type);
		re.set("about", about);
		re.set("logo", logo);
		re.set("creator", creator);
		Db.save("wc_company", re);
        render(getRequest().getRequestURI()+"/index.html");
    }
	
	@Before(Tx.class)
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
