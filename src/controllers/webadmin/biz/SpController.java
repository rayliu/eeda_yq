package controllers.webadmin.biz;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){ 
		String id = getPara("id");
        String sql_user = "select * from user_login where id = "+id;
        String sql_dimond = "select if(DATEDIFF(max(end_date),now())>0,'Y','N') whether,DATEDIFF(max(end_date),"
        					+ "now()) leave_days,max(end_date) last_date from wc_ad_dimond where status = '已开通' and creator = "+id;
        String sql_cu =	"SELECT if(DATEDIFF(max(end_date),now())>0,cast(DATEDIFF(max(end_date),now()) as char),'0') leave_days,max(end_date) end_date FROM `wc_ad_cu` where status='开启' and creator ="+id;
        String sql_hui="select * from wc_ad_hui where creator = "+id;
        Record re_user = Db.findFirst(sql_user);
        Record re_dimond = Db.findFirst(sql_dimond);
        Record re_cu = Db.findFirst(sql_cu);
        Record re_hui = Db.findFirst(sql_hui);
        setAttr("user",re_user);
        setAttr("dimond",re_dimond);
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
    	String sql=	"SELECT id,'cu' type,"
    			+" create_time,order_no, cast(CONCAT(begin_date, '-', end_date) as char) duringday,"
    			+" if(DATEDIFF(end_date,begin_date)<0,'over time',cast(DATEDIFF(end_date,begin_date) as char)) total_day, price "
    			+" FROM  wc_ad_cu  WHERE id = "+id
    			+" UNION ALL"
    			+" SELECT id, 'dimond' type, create_time, order_no, cast(CONCAT(begin_date, '-', end_date) as char) duringday,"
    			+" if(DATEDIFF(end_date,begin_date)<0,'over time',cast(DATEDIFF(end_date,begin_date) as char)) total_day, total_price  price"
    			+" FROM wc_ad_dimond WHERE id = "+ id;
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
	public void updateDimond(){
		String id = getPara("id");
		String endDate = getPara("end_date");
		String beginDate = (String) (getPara("begin_date").equals("begin_date")?new Date():getPara("begin_date"));
		Record re = new Record();
		re.set("begin_date", beginDate);
		re.set("end_date", endDate);
		re.set("status", "已开通");
		re.set("creator", id);
		re.set("remark", "管理员续费");
		re.set("create_time", new Date());
		re.set("total_price", 0);
		Db.save("wc_ad_dimond", re);
		renderJson(true);
	}
	
	@Before(Tx.class)
	public void updateCu(){
		String id = getPara("id");
		String endDate = getPara("end_date");
		String beginDate = getPara("begin_date");
		Record re = new Record();
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
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	String sql="select * from user_login where status = '通过'";
    	String condition = DbUtils.buildConditions(getParaMap());

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

    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
}
