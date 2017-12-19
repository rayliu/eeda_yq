package controllers.webadmin.biz;

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
public class MobilePushController extends Controller {

	private Logger logger = Logger.getLogger(MobilePushController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		List<Record> prices = Db.find("select price from price_maintain where id in (18,20)");
		setAttr("prices", prices);
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){
	        String id = getPara("id");
	        render(getRequest().getRequestURI()+"/edit.html");
	    }
	 
    @Before(Tx.class)
   	public void save() throws Exception {
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
        redirect("/");
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
    	UserLogin user = LoginUserController.getLoginUser(this);
    	Long user_id = LoginUserController.getLoginUserId(this);
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
        String sql="select wc.id wc_id, wc.c_name productor,map.* "
        		+ " from wc_ad_mobile_promotion map "
        		+ " LEFT JOIN wc_company wc on wc.creator = map.creator"
        		+ " order by create_time desc";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+" "+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    @Before(Tx.class)
   public void exam(){
	   String id = getPara("id");
	   String sql = "update wc_ad_mobile_promotion set status='已审批'  where id = "+id;
	   Db.update(sql);
	   renderJson(true);
	   
   }
    
    @Before(Tx.class)
    public void rollBack(){
 	   String id = getPara("id");
 	   String sql = "update wc_ad_mobile_promotion set status='新建'  where id = "+id;
 	   Db.update(sql);
 	   renderJson(true);
    }
   
   @Before(Tx.class)
   public void updateDiamond(){
	   String price = getPara("price");

       Record re = Db.findFirst("select * from price_maintain where type='钻石推送广告'");
	   if(re == null){
		   re = new Record();
		   re.set("type", "钻石推送广告");
		   re.set("price", price);
		   re.set("update_time", new Date());
		   Db.save("price_maintain",re);
	   }else {
		   re.set("price", price);
		   re.set("update_time", new Date());
		   Db.update("price_maintain",re);
	   }
       
	   renderJson(true);
   }
   
   @Before(Tx.class)
   public void updateMobile(){
	   String  price = getPara("price");
	   
	   Record re = Db.findFirst("select * from price_maintain where type='推送广告'");
	   if(re == null){
		   re = new Record();
		   re.set("price", price);
		   re.set("type", "推送广告");
		   re.set("update_time", new Date());
		   Db.save("price_maintain",re);
	   }else {
		   re.set("price", price);
		   re.set("update_time", new Date());
		   Db.update("price_maintain",re);
	   }

	   renderJson(true);
   }

    
   public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r = Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
