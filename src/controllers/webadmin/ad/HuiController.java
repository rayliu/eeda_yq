package controllers.webadmin.ad;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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
public class HuiController extends Controller {

	private Logger logger = Logger.getLogger(HuiController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		String sql_loc = "select loc.name city,lm.* from location_management lm "
						+ "left join location loc on lm.code = loc.code";
		String sql_cat = "select * from category ";
		List<Record> locations = Db.find(sql_loc);
		List<Record> categorys = Db.find(sql_cat);
		setAttr("locations",locations);
		setAttr("categorys",categorys);
		render(getRequest().getRequestURI()+"/list.html");
	}
	

	 public void edit(){
	        String id = getPara("id");
//	      String title = getPara("edit_radioTitle");
//	      String content = getPara("edit_radioContent");
//	      Record r= Db.findById("msg_board", id);
//	      r.set("title", title);
//	      r.set("content", content);
//	      r.set("update_stamp", new Date());
//	      r.set("updator", LoginUserController.getLoginUserId(this));
//	      Db.update("msg_board", r);
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
    
    public void list(){
        String location = getPara("location");
        String category = getPara("category");
        
        String sLimit = "";
        String pageIndex = getPara("draw"); 
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        String condition = "";
        if(StringUtils.isNotBlank(location)){
        	condition += " and city = "+location+"";
        }
        if(category != "" && category != null){
        		condition += " and trade_type = "+category;
        }

        String sql = "select "
        		+ " loc.name location, cat.name trade_type_name,wc.c_name productor,wah.* "
        		+ " from wc_ad_hui wah "
        		+ " LEFT JOIN wc_company wc on wah.creator=wc.creator"
        		+ " left join location loc on loc.code = wc.city"
        		+ " left join category cat on cat.id = wc.trade_type"
        		+ " where 1 = 1 ";
    	

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
    public void updateStatus(){
    	String status=getPara("status");
    	String id=getPara("id");
    	String sql="update wc_ad_hui set is_active='"+status+"' where id="+id;
    	Db.update(sql);
    	renderJson(true);
    }

}
