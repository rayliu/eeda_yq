package controllers.bizadmin.ad;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AdController extends Controller {

	private Logger logger = Logger.getLogger(AdController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/buy_cu/edit.html");
	}
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select id,order_no,price,total_price,phone,amount,CONVERT(SUBSTR(put_in_time,1,10),CHAR) put_in_time,status from mobile_ad_promotion where creator = "+ userId;
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
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
	
	public void dimond_save(){
		String years = getPara("years");
		String total_price = getPara("total_price");
		String status = getPara("status");
		Long userId = LoginUserController.getLoginUserId(this);
		Record rec = new Record();
		rec.set("years", years);
		rec.set("total_price", total_price);
		rec.set("status", status);
		rec.set("creator", userId);
		rec.set("create_time", new Date());
		Db.save("dimond", rec);
		renderJson(true);
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
		String order_no = (String)dto.get("order_no");
		String amount = (String)dto.get("amount");
		String put_in_time = (String)dto.get("put_in_time");
		String price = (String)dto.get("price");
		String total_price = (String)dto.get("total_price");
		String phone = (String)dto.get("phone");
		String status = (String)dto.get("status");
		
		Record rec = new Record();
		rec.set("creator", userId);
		rec.set("create_time", new Date());
		rec.set("order_no", order_no);
		rec.set("amount", amount);
		rec.set("put_in_time", put_in_time);
		rec.set("price", price);
		rec.set("total_price",total_price);
		rec.set("phone",phone);
		rec.set("status",status);
		Db.save("mobile_ad_promotion", rec);
		
		renderJson(true);
	}
	
	public void banner(){
        String id = getPara("id");
        System.out.println(getRequest().getRequestURI()+"/edit.html");
        Long user_id = LoginUserController.getLoginUserId(this);
        String sql="select * from wc_ad_banner where creator ="+user_id;
        Record user=Db.findFirst(sql);
        setAttr("user",user);
        render(getRequest().getRequestURI()+"/edit.html");
        //BusinessAdmin/ad/banner/edit.html
    }
	public void saveBanner(){

		String advantage=getPara("advantage");
        Gson gson = new Gson();  
        Map<String, ?> dto = gson.fromJson(advantage, HashMap.class);
        String id=(String)dto.get("id");
        String begin_date = (String) dto.get("begin_date");
    	String ad_location = (String) dto.get("ad_location");
		String end_date = (String) dto.get("end_date");
		String price = (String) dto.get("price");
		String telephone = (String) dto.get("phone");
		String total_price = (String)dto.get("total_price");
		String total_day=(String)dto.get("total_day");
		Record exist=Db.findById("advertisement_banner", id);
		Long user_id = LoginUserController.getLoginUserId(this);
		Record order = new Record();
		order.set("id", id);
    	order.set("begin_date", begin_date);
    	order.set("end_date", end_date);
    	order.set("phone", telephone);
    	order.set("price", price);
    	order.set("total_price", total_price);
    	order.set("ad_location",ad_location);
    	order.set("create_time", new Date());
    	order.set("creator", user_id);
    	order.set("total_day", total_day);
    	if(exist==null){
    		Db.save("wc_ad_banner", order);
    	}
    	else{
    		Db.update("wc_ad_banner", order);
    	}
		renderJson(order);
	}
	}
	
	public void update(){
		 String jsonStr=getPara("jsonStr");
		 Gson gson = new Gson();  
	     Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);
	     String id = (String) dto.get("id");
	     String amount = (String) dto.get("amount");
	     String put_in_time = (String) dto.get("put_in_time");
		 String price = (String) dto.get("price");
	 	 String total_price = (String) dto.get("total_price");
		 String phone = (String) dto.get("phone");
		 
		 Record re = Db.findById("mobile_ad_promotion", id);
		 re.set("amount", amount);
		 re.set("put_in_time", put_in_time);
		 re.set("price", price);
		 re.set("total_price", total_price);
		 re.set("phone", phone);
		 Db.update("mobile_ad_promotion",re);
		 renderJson(true);
	}
	
	
        public void list(){
	    String sLimit = "";
       		String pageIndex = getPara("draw");
       	 	if (getPara("start") != null && getPara("length") != null) {
            		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        	}
		Long user_id = LoginUserController.getLoginUserId(this);
		String sqlTotal = "select count(1) total from wc_ad_banner where creator = "+user_id ;
		Record rec = Db.findFirst(sqlTotal);
		String sql = "select * from wc_ad_banner where  creator = "+user_id+" order by begin_date desc " +sLimit;
		List<Record> orderList =  Db.find(sql);
	   	Map map = new HashMap();
	        map.put("draw", pageIndex);
       	 	map.put("recordsTotal", rec.getLong("total"));
        	map.put("recordsFiltered", rec.getLong("total"));
       		 map.put("data", orderList);
        	renderJson(map);
        }

       	
}
