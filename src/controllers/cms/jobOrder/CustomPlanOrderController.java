package controllers.cms.jobOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.cms.CustomPlanOrder;
import models.eeda.cms.CustomPlanOrderItem;

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
public class CustomPlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(CustomPlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/cms/customPlanOrder/CustomPlanOrderlist.html");
	}
	
    public void create() {
        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        CustomPlanOrder customPlanOrder = new CustomPlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			customPlanOrder = CustomPlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("updator", user.getLong("id"));
   			customPlanOrder.set("update_stamp", new Date());
   			customPlanOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("order_no", OrderNoGenerator.getNextOrderNo("BGSQ"));
   			customPlanOrder.set("creator", user.getLong("id"));
   			customPlanOrder.set("create_stamp", new Date());
   			customPlanOrder.set("office_id", office_id);
   			customPlanOrder.save();
   			
   			id = customPlanOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, CustomPlanOrderItem.class, "order_id");

		long creator = customPlanOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = customPlanOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    
    private List<Record> getCustomPlanOrderItems(String orderId) {
        String itemSql = "SELECT * FROM"
        		+ "	custom_plan_order_item"
        		+ " WHERE order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	CustomPlanOrder customPlanOrder = CustomPlanOrder.dao.findById(id);
    	setAttr("order", customPlanOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getCustomPlanOrderItems(id));
    	
    	//回显客户信息
//    	Party party = Party.dao.findById(customPlanOrder.getLong("customer_id"));
//    	setAttr("party", party);

    	//用户信息
    	long creator = customPlanOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    

    
    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	
    	String customer_code=getPara("customer_code")==null?"":getPara("customer_code");
    	String customer_name=getPara("customer")==null?"":getPara("customer");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        
        	sql = "SELECT * from (SELECT cpo.id,cpo.order_no,cpo.type,cpo.application_company_input application_company_name,ul.c_name creator_name,"
        			+ " cpo.create_stamp,cpo.status"
        			+ " FROM custom_plan_order cpo"
        			+ " LEFT JOIN user_login ul on ul.id = cpo.creator"
        			+ " where cpo.office_id="+office_id+")A"
    		        + " where 1 =1 ";

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
    	list = getCustomPlanOrderItems(order_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
   
    
    //提交申请单给报关行
    public void confirmCompleted(){
    	String id = getPara("id");
    	String plan_order_no = getPara("plan_order_no");
    	String btnId = getPara("btnId");
    	CustomPlanOrder order = CustomPlanOrder.dao.findById(id);
    	if("confirmCompleted".equals(btnId)){
    		order.set("status","处理中");
    	}
    	if("passBtn".equals(btnId)){
    		order.set("status","审核通过");
    		Record r = new Record();
    		r.set("plan_order_id", id);
    		r.set("plan_order_no", plan_order_no);
    		
       		UserLogin user = LoginUserController.getLoginUser(this);
       		long office_id = user.getLong("office_id");
       		r.set("creator", user.getLong("id"));
   			r.set("create_stamp", new Date());
   			r.set("office_id", office_id);
    		r.set("order_no", OrderNoGenerator.getNextOrderNo("BGGZD"));
    		Db.save("custom_job_order", r);
    		id = r.getLong("id").toString();
    		
    		
    	}
    	if("refuseBtn".equals(btnId)){
    		order.set("status","审核不通过");
    	}
    	order.update();
    	
    	Record rec = order.toRecord();
    	rec.set("job_order_id", id);
    	renderJson(rec);
    }
    

}
