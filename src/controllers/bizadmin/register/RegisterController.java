package controllers.bizadmin.register;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
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

import controllers.util.MD5Util;
import freemarker.template.utility.StringUtil;


public class RegisterController extends Controller {

	private Logger logger = Logger.getLogger(RegisterController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/index.html");
	}
	
	public void agreement(){
		render("/BusinessAdmin/register/agreement.html");
	}
    
	@Before(Tx.class)
	public void info() throws UnsupportedEncodingException{
		String login_name = URLDecoder.decode(getPara("login_name"), "UTF-8");
		String phone = URLDecoder.decode(getPara("phone"), "UTF-8");
		String password = URLDecoder.decode(getPara("password"), "UTF-8");
		Record re = new Record();
		re.set("user_name", login_name);
		re.set("phone", phone);
		re.set("password", password);
		setAttr("creator",re);
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
	
	public void check_mobile(){
		boolean result = false;
		String mobile = getPara("phone");
		String sql = "select * from user_login where phone = ?";
		Record re = Db.findFirst(sql,mobile);
		if(re == null){
			result = true;
		}
		renderJson(result);
	}
	
    //生成随机数字和字母,  
    public String getStringRandom(int length) {  
          
        String val = "";  
        Random random = new Random();  
          
        //参数length，表示生成几位随机数  
        for(int i = 0; i < length; i++) {  
            //输出字母还是数字  
            if( i<1 ) {  
                //输出是大写字母还是小写字母  
                //int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;  
                val += (char)(random.nextInt(26) + 97);  //默认小写
            } else  {  
                val += String.valueOf(random.nextInt(10));  
            }  
        }  
        return val;  
    } 
	
	@Before(Tx.class)
	public void done() throws UnsupportedEncodingException{
		//user
		String type = getPara("type");
		String user_name =  URLDecoder.decode(getPara("user_name"), "UTF-8");
		String password = getPara("password");
		String sha1Pwd = MD5Util.encode("SHA1", password);
		String phone = getPara("phone");
		//info
		String telephone = getPara("telephone");
		String contact =  URLDecoder.decode(getPara("contact"), "UTF-8");
		String shop_address =  URLDecoder.decode(getPara("shop_address"), "UTF-8");
		String shop_telephone = getPara("shop_telephone");
		String about =  URLDecoder.decode(getPara("about"), "UTF-8");
		String shop_city = getPara("shop_city");
		String shop_province = getPara("shop_province");
		String shop_district = getPara("shop_district");
		String trade_type = getPara("trade_type");
		String qq = getPara("qq");
		String logo =  URLDecoder.decode(getPara("logo"), "UTF-8");
		String company_pic =  URLDecoder.decode(getPara("company_pic"), "UTF-8");
		String c_name =  URLDecoder.decode(getPara("company_name"), "UTF-8");
		String id_card =  URLDecoder.decode(getPara("id_card"), "UTF-8");
		
		//查询随机生成的邀请码是否在系统中存在
		String invitation_code = getStringRandom(5);
		for(int i = 0; i <= 10; i++){
			Record re = Db.findFirst("select * from user_login where invitation_code = ?",invitation_code);
			if(re != null){
				invitation_code = getStringRandom(5);
			}else{
				break;
			}
		}
		//user_login
		Record order = new Record();
		order.set("user_name", user_name);
		order.set("password", sha1Pwd);
		order.set("phone", phone);
		order.set("create_time", new Date());
		order.set("is_stop", 1);
		order.set("office_id", 1);
		order.set("invitation_code", invitation_code);
		Db.save("user_login", order);
		//wc_company
		Long creator_id = order.getLong("id");
		if(StringUtils.isNotBlank(creator_id.toString())){
			Record item = new Record();
			if("1".equals(type)){
				item.set("id_card", id_card);
				item.set("contact", contact);
				item.set("c_name", contact);
				item.set("company_name", contact);
			}else{
				item.set("company_pic", company_pic);
				item.set("company_name", c_name);
				item.set("c_name", c_name);
				item.set("contact", contact);
			}
			
			item.set("create_time", new Date());
			item.set("telephone", telephone);
			item.set("shop_telephone", shop_telephone);
			item.set("trade_type", trade_type);
			item.set("province", shop_province);
			if(!shop_city.equals("undefined")){
				item.set("city", shop_city);
			}
			item.set("district", shop_district);
			item.set("address", shop_address);
			item.set("qq", qq);
			item.set("user_type", type);
			item.set("about", about);
			item.set("logo", logo);
			item.set("creator", creator_id);
			Db.save("wc_company", item);
		}
		
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
