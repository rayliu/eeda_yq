package controllers.webadmin.ad;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
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
public class CuController extends Controller {

	private Logger logger = Logger.getLogger(CuController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		Record re = Db.findFirst("select * from price_maintain where type = ?","促广告");
		setAttr("order", re);
		String sql_loc = "select loc.name city,lm.* from location_management lm "
				+ "left join location loc on lm.code = loc.code";
		String sql_cat = "select * from category ";
		List<Record> locations = Db.find(sql_loc);
		List<Record> categorys = Db.find(sql_cat);
		setAttr("locations",locations);
		setAttr("categorys",categorys);
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
    	String location = getPara("location");
        String category = getPara("category");
        String sLimit = "";
        String condition = " ";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
        if(StringUtils.isNotBlank(location)){
        	condition += " and city = "+location+"";
        }
        if(StringUtils.isNotBlank(category)){
        		condition += " and trade_type = "+category;
        }

     String sql="  select loc.name loc_name,wc.c_name, if(DATEDIFF(end_date,now())<0,'已经过期',cast(DATEDIFF(end_date,begin_date) as char) ) leave_days,"
    		 	+" DATEDIFF(end_date,begin_date) total_days,wab.* from wc_ad_cu wab "
    		 	+" LEFT JOIN wc_company wc ON wab.creator = wc.creator "
    		 	+" left join category cat on cat.id = wc.trade_type"
    		 	+" left join location loc on loc.code = ifnull(wc.city,wc.province)"
    		 	+" where 1  = 1"	;

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+condition+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    @Before(Tx.class)
    public void updateCu(){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String price=getPara("price");
    	String sql="update price_maintain set price= " +price +", update_time='"+df.format(new Date())+"' where type='促广告'";
    	Db.update(sql);
    	renderJson(true);
    }
    
    @Before(Tx.class)
    public void whetherCarriage(){
 	   String status=getPara("status");
 	   String info="";
 	   if(status.equals("toUp")){
 		   info="开启";
 	   }else if(status.equals("toDown")){
 		   info="关闭";
 	   }
 	   String id = getPara("id");
 	   String sql = "update wc_ad_cu set status = '"+info+"' where id="+id+""; 
 	   Db.update(sql);
 	   renderJson(true);
    }

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
