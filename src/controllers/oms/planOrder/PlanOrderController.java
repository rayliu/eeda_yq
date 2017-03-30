package controllers.oms.planOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.bookOrder.BookOrder;

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
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(PlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/oms/PlanOrder/PlanOrderList.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        PlanOrder planOrder = new PlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			planOrder = PlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("updator", user.getLong("id"));
   			planOrder.set("update_stamp", new Date());
   			planOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("JH", office_id));
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.set("office_id", office_id);
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");
		
		
		List<PlanOrderItem> reList = PlanOrderItem.dao.find("select * from plan_order_item where order_id = ?",id);
		for(PlanOrderItem item:reList){
			createBookOrder(item,id);
		}
		

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    @Before(Tx.class)
    public void createBookOrder(PlanOrderItem item,String id){
    	Long item_id = item.getLong("id");
    	BookOrder order  = BookOrder.dao.findFirst("select * from book_order where plan_item_id = ? ",item_id);
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
    	if(order==null){
    		PlanOrder re = PlanOrder.dao.findById(id);
       		order  = new BookOrder();
        	order.set("order_no", OrderNoGenerator.getNextOrderNo("BK", office_id));
        	order.set("creator", user.getLong("id"));
        	order.set("create_stamp", new Date());
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("plan_order_no", re.getStr("order_no"));
            order.set("plan_order_id", id);
            order.set("customer_id", re.getLong("customer_id"));
            order.set("plan_item_id", item.getLong("id"));
            order.set("pieces", item.get("pieces"));
            order.set("net_weight", item.get("net_weight"));
            order.set("gross_weight", item.get("gross_weight"));
            order.set("volume", item.get("volume"));
            order.save();
    		
    	}else{
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("pieces", item.get("pieces"));
            order.set("net_weight", item.get("net_weight"));
            order.set("gross_weight", item.get("gross_weight"));
            order.set("volume", item.get("volume"));
            
            order.update();
    	}
    }

    
    private List<Record> getPlanOrderItems(String orderId) {
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,bor.order_no book_order_no,"
                + " p.abbr carrier_name "
                + " from plan_order_item pi "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
                + " left join book_order bor on bor.plan_item_id = pi.id"
                +" left join party p on pi.carrier=p.id"
                +" left join unit u on u.id=pi.unit_id"
                +" where order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	PlanOrder planOrder = PlanOrder.dao.findById(id);
    	setAttr("order", planOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getPlanOrderItems(id));
    	
    	//回显客户信息
    	Party party = Party.dao.findById(planOrder.getLong("customer_id"));
    	setAttr("party", party);

    	//用户信息
    	long creator = planOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    

    
    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	String type=getPara("type");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        if("todo".equals(type)){
        	sql =" SELECT po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name "
        			+ " FROM plan_order po "
        			+ " LEFT JOIN plan_order_item poi ON po.id = poi.order_id "
        			+ " left join party p on p.id = po.customer_id "
        			+ " left join user_login u on u.id = po.creator "
        			+ " WHERE po.office_id="+office_id+" and is_gen_job='N' AND factory_loading_time is not NULL "
        			+ " AND datediff(factory_loading_time, now())<=5"
        			+ " and po.delete_flag = 'N'";
        }else if ("customwaitPlan".equals(type)){
        	sql =" SELECT po.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p. CODE"
        			+ " FROM"
        			+ "	plan_order po"
        			+ " LEFT JOIN plan_order_item poi ON poi.order_id = po.id"
        			+ " LEFT JOIN user_login u ON u.id = po.creator"
        			+ " LEFT JOIN party p ON p.id = po.customer_id"
        			+ " WHERE"
        			+ " po.office_id="+office_id+" and poi.customs_type = '自理报关'"
        			+ " AND poi.is_gen_job = 'N'"
        			+ " and po.delete_flag = 'N'"
        			+ " GROUP BY poi.id ";
        }else{
        	sql = "SELECT * from (select po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name,p.code customer_code"
    			+ " from plan_order po "
    			+ " left join party p on p.id = po.customer_id "
    			+ " left join user_login u on u.id = po.creator"
    			+ " where po.office_id="+office_id
    			+ " and po.delete_flag = 'N'"
    			+ " ) A where 1=1 ";
        }
        condition = DbUtils.buildConditions(getParaMap());
        
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	list = getPlanOrderItems(order_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
   
    
    //确认已完成计划单
    public void confirmCompleted(){
    	String id = getPara("id");
    	PlanOrder order = PlanOrder.dao.findById(id);
    	order.set("status", "已完成");
    	renderJson("{\"result\":true}");
    }
    
    //删除单据，设置为已删
    @Before(Tx.class)
    public void deleteOrder(){
    	String id = getPara("id");
    	String delete_reason = getPara("delete_reason");
    	Long deletor = LoginUserController.getLoginUserId(this);
    	Date date = new Date();
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String delete_stamp = sf.format(date);
    	Db.update("update plan_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
    			+ " delete_reason='"+delete_reason+"' where id = ?  ",id);
    	renderJson("{\"result\":true}");
    }
    

}
