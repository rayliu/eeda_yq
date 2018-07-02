package controllers.webadmin.biz;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.DateFormat;
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
public class SpController extends Controller {

	private Logger logger = Logger.getLogger(SpController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
        //获取地区
        String sql_loc="select loc.name city,lm.* from location_management lm "
        				+ "left join location loc on lm.code = loc.code";
        List<Record> locations = Db.find(sql_loc);
        setAttr("locations",locations);
        
        List<Record> categoryList = Db.find("select * from category ");
    	setAttr("categoryList",categoryList);
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){ 
		String id = getPara("id");
        String sql_user = "select ca.name trade_name,ul.id uid,ul.invitation_code,ul.influence,ul.user_name,wc.* "
        		+ " from user_login ul "
        		+ " left join wc_company wc on wc.creator = ul.id "
        		+ " left join category ca on ca.id = wc.trade_type "
        		+ " where ul.id = ?";
        Record re_user = Db.findFirst(sql_user,id);
        
        Record diamond = Db.findFirst("select  'Y' is_diamond, DATEDIFF(end_date, now()) leave_days,end_date last_date from wc_ad_diamond"
    			+ " where creator = ? and (now() BETWEEN begin_date and end_date) and status = '已开通' order by end_date desc",id);
        if(diamond != null){
        	setAttr("diamond",diamond);
        }else{
        	diamond = new Record();
        	diamond.set("is_diamond", "N");
        	diamond.set("leave_days", 0);
        	diamond.set("last_date", "");
        	setAttr("diamond",diamond);
        }
        
        String sql_cu  = "SELECT 'Y' is_cu,DATEDIFF(end_date,now()) leave_days, end_date end_date FROM `wc_ad_cu` "
        					+ " where creator = ? and (now() BETWEEN begin_date and end_date) and status = '开启' order by end_date desc";
        Record re_cu = Db.findFirst(sql_cu,id);
        if(re_cu != null){
        	setAttr("cu",re_cu);
        }else{
        	diamond = new Record();
        	diamond.set("is_cu", "N");
        	diamond.set("leave_days", 0);
        	diamond.set("end_date", "");
        	setAttr("cu",re_cu);
        }
        
        String sql_hui="select * from wc_ad_hui where creator = "+id;
        Record re_hui = Db.findFirst(sql_hui);
        setAttr("user",re_user);
        setAttr("hui",re_hui);
        render(getRequest().getRequestURI()+".html");
	}
	
	public void editList(){
		String id = getPara("id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	String sql=	"SELECT id,'促销广告' type,"
    			+ " create_time,order_no, cast(CONCAT(begin_date, ' ~ ', end_date) as char) duringday,"
    			+ " cast(DATEDIFF(end_date,begin_date) as char) total_day, price "
    			+ " FROM  wc_ad_cu "
    			+ " WHERE creator = "+id
    			+ " UNION ALL"
    			+ " SELECT id, '钻石会员' type, create_time, order_no, cast(CONCAT(begin_date, ' ~ ', end_date) as char) duringday,"
    			+ " cast(DATEDIFF(end_date,begin_date) as char) total_day, total_price  price"
    			+ " FROM wc_ad_diamond"
    			+ " WHERE creator = "+ id;
    	String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
	}
	
	@Before(Tx.class)
	public void updateDiamond(){
		DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		String id = getPara("id");
		String endDate = getPara("end_date");
		//String beginDate = (String) (getPara("begin_date").equals("begin_date")?new Date():getPara("begin_date"));
		Record re = new Record();
		re.set("order_no", "admin"+format.format(new Date()));
		re.set("begin_date", new Date());
		re.set("end_date", endDate);
		re.set("status", "已开通");
		re.set("creator", id);
		re.set("remark", "管理员续费");
		re.set("create_time", new Date());
		re.set("total_price", 0);
		Db.save("wc_ad_diamond", re);
		renderJson(true);
	}
	
	@Before(Tx.class)
	public void updateCu(){
		DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		String id = getPara("id");
		String endDate = getPara("end_date");
		//String beginDate = getPara("begin_date");
		Record re = new Record();
		re.set("order_no", "admin"+format.format(new Date()));
		re.set("begin_date", new Date());
		re.set("end_date", endDate);
		re.set("status", "开启");
		re.set("creator", id);
		re.set("title", "默认标题");
		re.set("content", "暂无");
		re.set("remark", "管理员续费");
		re.set("create_time", new Date());
		re.set("price", 0);
		Db.save("wc_ad_cu", re);
		renderJson(true);
	}
	
    @Before(Tx.class)
	public void updateHui(){
    	String status = getPara("status");
    	String id = getPara("id");
    	
    	Record order = Db.findFirst("select * from wc_ad_hui where creator = ?",id);
    	if(order != null){
    		if("开启".equals(status)){
        		order.set("status", status);
        		order.set("is_active", "Y");
        	}else{
        		order.set("status", status);
        		order.set("is_active", "N");
        	}
    		order.set("updator", LoginUserController.getLoginUserId(this));
    		order.set("update_time", new Date());
    		Db.update("wc_ad_hui", order);
    	}else{
    		order = new Record();
    		order.set("status", status);
    		if("开启".equals(status)){
    			order.set("is_active", "N");
    		}
    		order.set("updator", LoginUserController.getLoginUserId(this));
    		order.set("update_time", new Date());
    		Db.save("wc_ad_hui", order);
    	}

    	renderJson(true);
    }
	
	@Before(Tx.class)
	public void delete(){
		String id = getPara("id");
//		String sql_delcompany="delete from wc_company where creator = "+id;
//		Db.update(sql_delcompany);
		String sql = "update user_login set is_delete = 'Y' where id = "+id;
		Db.update(sql);
		renderJson(true);
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
        //主列表
        String sLimit = "";
        String pageIndex = getPara("draw");
        String location = getPara("location");
        String user_type = getPara("user_type");
        String category = getPara("category");
        String condition = "";
        if(StringUtils.isNotBlank(location)){
        	condition += " and wc.city = '"+location+"'";
        }
        if(StringUtils.isNotBlank(category)){
        	condition += " and ca.name = '"+category+"'";
        }
        if(StringUtils.isNotBlank(user_type)){
        	if(user_type.equals("1")){
        		condition += " and A.leave_days is null";
        	}else if(user_type.equals("2") ){
        		condition += " and A.leave_days is not null ";
        	}
        }
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	String sql="select A.leave_days,ca.name trade_type_name,ifnull(loc.name,'暂无') location,"
    			+ " ul.id uid,ul.user_name,ul.phone,wc.* "
    			+ " from user_login ul  "
    			+ " left join  wc_company wc ON wc.creator = ul.id  "
    			+ " LEFT JOIN category ca on ca.id = wc.trade_type "
    			+ " left join location loc on loc.code = ifnull(wc.city,wc.province)  and loc.code <>''"
    			+ " left join (select creator,datediff(max(end_date),now()) leave_days "
    			+ " from wc_ad_diamond wad  group by creator) A  on A.creator = ul.id "
    			+ " where ul.status = '通过' and ul.system_type = '商家后台' and is_delete != 'Y'"
    			+ condition;
        String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql +" order by create_time desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
    @Before(Tx.class)
    public void add_item(){
    	String user_id = getPara("user_id");
    	
    	Record order = new Record();
    	order.set("user_id", user_id);
    	Db.save("wc_inviter", order);
    	
    	renderJson(true);
    }
    
    @Before(Tx.class)
    public void delete_item(){
    	String item_id = getPara("item_id");
    	
    	Record order = new Record();
    	order.set("id", item_id);
    	order.set("is_delete", "Y");
    	Db.update("wc_inviter", order);
    	
    	renderJson(true);
    }
    
    @Before(Tx.class)
    public void update_item(){
    	String item_id = getPara("item_id");
    	String item_name = getPara("item_name");
    	String item_value = getPara("item_value");
    	
    	Record order = new Record();
    	order.set("id", item_id);
    	order.set(item_name, item_value);
    	Db.update("wc_inviter", order);
    	
    	renderJson(true);
    }
    
    public void inviteList(){
    	String user_id = getPara("user_id");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	String sql = " select * from wc_inviter iv where user_id = ? and is_delete != 'Y'";
    	String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal, user_id);
        List<Record> orderList = Db.find(sql + sLimit, user_id);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }
    
}
