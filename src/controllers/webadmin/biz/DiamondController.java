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
public class DiamondController extends Controller {

	private Logger logger = Logger.getLogger(DiamondController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		Record re = Db.findFirst("select * from price_maintain where type = ?","钻石会员");
		setAttr("order", re);
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void edit(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }

    
    public void list(){
    	Long user_id = LoginUserController.getLoginUserId(this);
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         

        String sql="select wc.c_name,DATEDIFF(dim.end_date,dim.begin_date) days,dim.* from wc_ad_diamond dim "
        		+ "LEFT JOIN wc_company wc on dim.creator = wc.creator ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+" order by create_time desc "+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    @Before(Tx.class)
    public void updatePrice(){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String price = getPara("price");
    	
    	Record re = Db.findFirst("select * from price_maintain where type = '钻石会员'");
    	if(re == null){
    		re = new Record();
    		re.set("type", "钻石会员");
    		re.set("price",price);
    		re.set("update_time", new Date());
    		Db.save("price_maintain", re);
    	}else{
    		re.set("price",price);
    		re.set("update_time", new Date());
    		Db.update("price_maintain", re);
    	}
    	
    	renderJson(true);
    }
    
    
    @Before(Tx.class)
    public void updateStatus(){
    	String order_id = getPara("order_id");
    	String sql="update wc_ad_diamond set status = '已开通' where id ='"+ order_id+"'";
    	Db.update(sql);
    	renderJson(true);
    }
    
    
}
