package controllers.bizadmin.ad;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CuController extends Controller {

	private Logger logger = Logger.getLogger(CuController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	public void buy(){
        String id = getPara("id");
        Long user_id = LoginUserController.getLoginUserId(this);
        String sql="select * from wc_ad_cu where creator ="+user_id;
        String per_price="select * from price_maintain where type  ='促广告'";
        Record user=Db.findFirst(sql);
        Record price=Db.findFirst(per_price);
        setAttr("user", user);
        setAttr("per_price", price.get("price"));
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	
	public void list(){
	    String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Long userId = LoginUserController.getLoginUserId(this);
        String sql = "select * from wc_ad_cu where creator = "+ userId;
        
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
	
	public void edit(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	@Before(Tx.class)
	public void update(){
		String id = getPara("id");
		String title = getPara("title");
		String content = getPara("content");
		
		Record order = Db.findById("wc_ad_cu", id);
		order.set("title", title);
		order.set("content", content);
		Db.update("wc_ad_cu", order);
		
		renderJson(order);
	}
	
	@Before(Tx.class)
	public void save(){
		String advertisement = getPara("advertisement");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(advertisement, HashMap.class);
        String begin_date=(String) dto.get("begin_date");
        String end_date=(String) dto.get("end_date");
        String total_day=(String) dto.get("total_day");
        String remark=(String) dto.get("remark");
        String price=(String) dto.get("price");
        Long user_id = LoginUserController.getLoginUserId(this);
        
        Record order = new Record();

		DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		order.set("order_no", format.format(new Date()));
    	order.set("begin_date", begin_date);
    	order.set("end_date", end_date);
    	order.set("total_day", total_day);
    	order.set("price", price);
    	order.set("remark", remark);
    	order.set("create_time", new Date());
    	order.set("creator",user_id);
    	Db.save("wc_ad_cu", order);
    	renderJson(order);
	}
}
