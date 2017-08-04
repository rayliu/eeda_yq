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
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){ 
		String id = getPara("id");
        String sql_user = "select ca.name trade_name,ul.id uid,ul.user_name,wc.* from user_login ul "
			        		+ "left join wc_company wc on wc.creator = ul.id "
			        		+"left join category ca on ca.id=wc.trade_type "
			        		+ "where ul.id = "+id;
        String sql_diamond = "select if(DATEDIFF(max(end_date),now())>0,'Y','N') whether,DATEDIFF(max(end_date),"
        					+ "now()) leave_days,max(end_date) last_date from wc_ad_diamond where status = '已开通' and creator = "+id;
        String sql_cu =	"SELECT if(DATEDIFF(max(end_date),now())>0,cast(DATEDIFF(max(end_date),now()) as char),'0') leave_days,max(end_date) end_date FROM `wc_ad_cu` where status='开启' and creator ="+id;
        String sql_hui="select * from wc_ad_hui where creator = "+id;
        Record re_user = Db.findFirst(sql_user);
        Record re_diamond = Db.findFirst(sql_diamond);
        Record re_cu = Db.findFirst(sql_cu);
        Record re_hui = Db.findFirst(sql_hui);
        setAttr("user",re_user);
        setAttr("diamond",re_diamond);
        setAttr("cu",re_cu);
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
    	String sql=	"SELECT id,'促销' type,"
    			+" create_time,order_no, cast(CONCAT(begin_date, '-', end_date) as char) duringday,"
    			+" if(DATEDIFF(end_date,begin_date)<0,'over time',cast(DATEDIFF(end_date,begin_date) as char)) total_day, price "
    			+" FROM  wc_ad_cu  WHERE creator = "+id
    			+" UNION ALL"
    			+" SELECT id, '钻石会员' type, create_time, order_no, cast(CONCAT(begin_date, '-', end_date) as char) duringday,"
    			+" if(DATEDIFF(end_date,begin_date)<0,'over time',cast(DATEDIFF(end_date,begin_date) as char)) total_day, total_price  price"
    			+" FROM wc_ad_diamond WHERE creator = "+ id;
    	String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql+ " order by create_time desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
	}
	
	@Before(Tx.class)
	public void updatediamond(){
		DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		String id = getPara("id");
		String endDate = getPara("end_date");
		String beginDate = (String) (getPara("begin_date").equals("begin_date")?new Date():getPara("begin_date"));
		Record re = new Record();
		re.set("order_no", "A"+format.format(new Date()));
		re.set("begin_date", beginDate);
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
		String beginDate = getPara("begin_date");
		Record re = new Record();
		re.set("order_no", "A"+format.format(new Date()));
		re.set("begin_date", beginDate);
		re.set("end_date", endDate);
		re.set("status", "开启");
		re.set("creator", id);
		re.set("remark", "管理员续费");
		re.set("create_time", new Date());
		re.set("price", 0);
		Db.save("wc_ad_cu", re);
		renderJson(true);
	}
	
    @Before(Tx.class)
	public void updateStatus(){
    	String status=getPara("status");
    	String id=getPara("id");
    	String sql="update wc_ad_hui set is_active='"+status+"' where creator="+id;
    	Db.update(sql);
    	renderJson(true);
    }
	
	@Before(Tx.class)
	public void delete(){
		String id = getPara("id");
		String sql_delcompany="delete from wc_company where creator = "+id;
		Db.update(sql_delcompany);
		String sql = "delete from user_login where id = "+id;
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
        long office_id=user.getLong("office_id");
        //主列表
        String sLimit = "";
        String pageIndex = getPara("draw");
        String location = getPara("location");
        String user_type = getPara("user_type");
        String condition = "";
        if(location != "" && location != null){
        	condition += " and city = "+location+"";
        }
        if(user_type != "" && user_type != null){
        	if(user_type.equals("1")){
        		condition += " and A.leave_days  is null";
        	}else if(user_type.equals("2") ){
        		condition += " and A.leave_days is not null ";
        	}
        }
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	String sql="select 	A.leave_days,ca.name trade_type_name,ifnull(loc.name,'暂无') location,ul.id uid,ul.user_name,ul.phone,wc.* from user_login ul  "
	    			+ "left join  wc_company wc ON wc.creator = ul.id  "
	    			+ "LEFT JOIN category ca on ca.id = ul.id "
	    			+ "left join location loc on loc.code = ifnull(wc.city,wc.province) "
	    			+ "left join (select creator,datediff(max(end_date),now()) leave_days from wc_ad_diamond wad  group by creator) A  on A.creator = ul.id "
	    			+ "where ul.status = '通过' ";
    	
    	String sss = sql+condition;
        String sqlTotal = "select count(1) total from ("+sql+condition+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +" order by create_time desc " +sLimit);
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
    
}
