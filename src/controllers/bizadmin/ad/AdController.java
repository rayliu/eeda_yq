package controllers.bizadmin.ad;

import java.util.Date;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

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
		String delivery_number = getPara("delivery_number");
		String delivery_time = getPara("delivery_time");
		String price=getPara("price");
		String settlement_price=getPara("settlement_price");
		String contact_phone=getPara("contact_phone");
		System.out.println(delivery_number);
		Long userId = LoginUserController.getLoginUserId(this);
		
		Record rec = new Record();
		rec.set("creator", userId);
		rec.set("create_time", new Date());
		rec.set("delivery_number", delivery_number);
		rec.set("delivery_time", delivery_time);
		rec.set("price", price);
		rec.set("settlement_price",settlement_price);
		rec.set("contact_phone",contact_phone);
		Db.save("mobile_ad_promotion", rec);
		
		renderJson(true);
	}
	
	public void banner(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
}
