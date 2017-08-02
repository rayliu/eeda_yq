package controllers.webadmin.ad;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TaoController extends Controller {

	private Logger logger = Logger.getLogger(TaoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		//获取广告价格
		String sql1 = "select type,price from price_maintain where type ='第一张广告'";
		String sql2 = "select type,price from price_maintain where type ='第二张广告'";
		String sql3 = "select type,price from price_maintain where type ='第三张广告'";
		String sql4 = "select type,price from price_maintain where type ='第四张广告'";
        Record re1 = Db.findFirst(sql1);
        Record re2 = Db.findFirst(sql2);
        Record re3 = Db.findFirst(sql3);
        Record re4 = Db.findFirst(sql4);
		if(re1==null){
        	re1 = new Record();
        	re1.set("type", "第一张广告");
        	re1.set("price",0);
        	Db.save("price_maintain", re1);
        }else{
        	setAttr("first_price",re1.get("price"));
        }
		
		if(re2 == null){
        	re2 = new Record();
        	re2.set("type", "第二张广告");
        	re2.set("price",0);
        	Db.save("price_maintain", re2);
        }else{
        	setAttr("second_price",re2.get("price"));
        }
		
		if(re3 == null){
        	re3 = new Record();
        	re3.set("type", "第三张广告");
        	re3.set("price",0);
        	Db.save("price_maintain", re3);
        }else{
        	setAttr("third_price",re3.get("price"));
        }
		
		if(re4 == null){
        	re4 = new Record();
        	re4.set("type", "第四张广告");
        	re4.set("price", 0);
        	Db.save("price_maintain", re4);
        }else{
        	setAttr("fourth_price",re4.get("price"));
        }
		
		Record one = Db.findFirst("select * from wc_ad_banner_photo where ad_index = 1");
        Record two = Db.findFirst("select * from wc_ad_banner_photo where ad_index = 2");
        Record three = Db.findFirst("select * from wc_ad_banner_photo where ad_index = 3");
        Record four = Db.findFirst("select * from wc_ad_banner_photo where ad_index = 4");
        setAttr("one", one);
        setAttr("two", two);
        setAttr("three", three);
        setAttr("four", four);
        
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){
	        String id = getPara("id");
	       
	        render(getRequest().getRequestURI()+"/edit.html");
	    }
	
	

	 
    @Before(Tx.class)
   	public void save() throws Exception {
    	String user_id = getPara("user_id");
    	String product_id = getPara("product_id");
    	String begin_date = getPara("begin_date");
    	String end_date = getPara("end_date");
    	String photo = getPara("photo");
    	String index = getPara("index");
    	String default_flag = getPara("default_flag");
    	
    	Record order = Db.findFirst("select * from wc_ad_banner_photo where ad_index =?", index);
    	if(order == null){
    		order = new Record();
    		order.set("ad_index", index);
    		if("Y".equals(default_flag)){
    			order.set("default_photo", photo);
    			order.set("default_user_id", user_id);
        		order.set("default_product_id", product_id);
    	    }else{
    	    	order.set("photo", photo);
    	    	order.set("user_id", user_id);
        		order.set("product_id", product_id);
        		order.set("begin_date", begin_date);
        		order.set("end_date", end_date);
    	    }
    		order.set("creator", LoginUserController.getLoginUserId(this));
    		order.set("create_time", new Date());
    		Db.save("wc_ad_banner_photo", order);
    	}else{
    		if("Y".equals(default_flag)){
    			order.set("defaut_photo", photo);
    			order.set("defaut_user_id", user_id);
        		order.set("defaut_product_id", product_id);
    	    }else{
    	    	order.set("photo", photo);
    	    	order.set("user_id", user_id);
        		order.set("product_id", product_id);
        		order.set("begin_date", begin_date);
        		order.set("end_date", end_date);
    	    }
    		order.set("update_by", LoginUserController.getLoginUserId(this));
    		order.set("update_time", new Date());
    		Db.update("wc_ad_banner_photo", order);
    	}

        renderJson(true);
   	}
    
    @Before(Tx.class)
    public void saveOfMsgBoard() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	Record r= new Record();
    	r.set("title", title);
    	r.set("content", content);
    	r.set("office_id", office_id);
    	r.set("create_stamp", new Date());
    	r.set("creator", LoginUserController.getLoginUserId(this));
    	Db.save("msg_board", r);
    	redirect("/msgBoard");
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
    
   @Before(Tx.class)
   public void updatePrice(){
	   String id = getPara("id");
	   String price = getPara("price") == null?"0":getPara("price");
	   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   String sql="update price_maintain set price ="+price+" ,  update_time='"+df.format(new Date())+"' where id="+id;
	   Db.update(sql);
   }
   
   @Before(Tx.class)
   public void whetherApprove(){
	   String status=getPara("status");
	   String info="";
	   if(status.equals("Y")){
		   info="已审批";
	   }else if(status.equals("N")){
		   info="已拒绝";
	   }
	   String id = getPara("id");
	   String sql = "update wc_ad_banner set status = '"+info+"' where id="+id+""; 
	   Db.update(sql);
	   renderJson(true);
   }
   
    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
