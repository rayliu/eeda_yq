package controllers.bizadmin.ad;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;
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
import com.jfinal.plugin.activerecord.tx.Tx;

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
	
	
	
	public void hui(){
	    Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from wc_ad_hui where creator=?", userId);
	    setAttr("hui", rec);
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	@Before(Tx.class)
	public void hui_save(){
        Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from wc_ad_hui where creator = ?", userId);
        if(rec != null){
            rec.set("is_active", getPara("is_active"));
            rec.set("discount", getPara("discount"));
            Db.update("wc_ad_hui", rec);
        }
        
        renderJson(rec);
    }
	
	public void dimond(){
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	@Before(Tx.class)
	public void dimond_save(){
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
		String years = (String)dto.get("years");
		String end_date = (String)dto.get("end_date");
		String put_in_days = (String)dto.get("put_in_days");
		String total_price = (String)dto.get("total_price");
		String status = "新建";
		
		Record order = new Record();
		order.set("years", years);
		order.set("total_price", total_price);
		order.set("status", status);
		order.set("begin_date", new Date());
		order.set("end_date", end_date);
		order.set("put_in_days", put_in_days);
		order.set("creator", userId);
		order.set("create_time", new Date());
		Db.save("wc_ad_dimond", order);
		renderJson(order);
	}
	
	public void mobile(){
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	@Before(Tx.class)
	public void mobile_save(){
		String jsonStr=getPara("jsonStr");
		Long userId = LoginUserController.getLoginUserId(this);
		Gson gson=new Gson();
		Map<String,?> dto = gson.fromJson(jsonStr, HashMap.class);
		String order_id = (String)dto.get("order_id");
		String amount = (String)dto.get("amount");
		String put_in_time = (String)dto.get("put_in_time");
		String price = (String)dto.get("price");
		String total_price = (String)dto.get("total_price");
		String phone = (String)dto.get("phone");
		
		if(StringUtils.isNotBlank(order_id)){
			Record order = Db.findById("wc_ad_mobile_promotion", order_id);
			order.set("amount", amount);
			order.set("put_in_time", put_in_time);
			order.set("price", price);
			order.set("total_price",total_price);
			order.set("phone",phone);
			
			Db.save("wc_ad_mobile_promotion", order);
		}else{
			Record order = new Record();
			DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
			order.set("order_no", format.format(new Date()));
			order.set("amount", amount);
			order.set("put_in_time", put_in_time);
			order.set("price", price);
			order.set("total_price",total_price);
			order.set("phone",phone);
			order.set("status","新建");
			order.set("creator", userId);
			order.set("create_time", new Date());
			Db.save("wc_ad_mobile_promotion", order);
		}
		
		renderJson(true);
	}
	
	/**
	 * 手机推广列表
	 */
	public void mobilelist() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select * "
        		+ " from wc_ad_mobile_promotion "
        		+ " where creator = "+ userId;
        
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
