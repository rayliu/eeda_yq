package controllers.bizadmin.ad;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AdController extends Controller {

	private Logger logger = Logger.getLogger(AdController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/buy_cu/edit.html");
	}
	
	public void hui(){
	    Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from wc_ad_hui where creator=?", userId);
	    setAttr("hui", rec);
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void hui_save(){
        Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from wc_ad_hui where creator=?", userId);
        if(rec!=null){
            rec.set("is_active", getPara("is_active"));
            rec.set("discount", getPara("discount"));
            Db.update("wc_ad_hui", rec);
        }
        
        renderText("OK");
    }
	
	public void dimond(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void mobile(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void mobile_save(){
		String jsonStr=getPara("jsonStr");
		Long userId = LoginUserController.getLoginUserId(this);
		Gson gson=new Gson();
		Map<String,?> dto = gson.fromJson(jsonStr, HashMap.class);
		
		String amount = (String)dto.get("amount");
		String put_in_time = (String)dto.get("put_in_time");
		String price = (String)dto.get("price");
		String total_price = (String)dto.get("total_price");
		String phone = (String)dto.get("phone");
		
		Record rec = new Record();
		rec.set("creator", userId);
		rec.set("create_time", new Date());
		rec.set("amount", amount);
		rec.set("put_in_time", put_in_time);
		rec.set("price", price);
		rec.set("total_price",total_price);
		rec.set("phone",phone);
		Db.save("mobile_ad_promotion", rec);
		
		renderJson(true);
	}
	
	public void banner(){
        String id = getPara("id");
        System.out.println(getRequest().getRequestURI()+"/edit.html");
        render(getRequest().getRequestURI()+"/edit.html");
        //BusinessAdmin/ad/banner/edit.html
    }
	public void saveBanner(){

		String advantage=getPara("advantage");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(advantage, HashMap.class);
        String begin_date=(String) dto.get("begin_date");
    	String ad_location = (String) dto.get("ad_location");
		String end_date = (String) dto.get("end_date");
		String price = (String) dto.get("price");
		String telephone = (String) dto.get("phone");
		String total_price=(String)dto.get("total_price");
		Record order = new Record();
    	order.set("begin_date", begin_date);
    	order.set("end_date", end_date);
    	order.set("telephone", telephone);
    	order.set("price", price);
    	order.set("total_price", total_price);
    	order.set("ad_location",ad_location);
    	order.set("create_time", new Date());
    	Db.save("ad_banner", order);
		renderJson(order);
		
	}
}
