package controllers.oms.planOrder;

import interceptor.SetAttrLoginUserInterceptor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import models.UserLogin;
import models.eeda.oms.SalesOrderGoods;
import models.eeda.oms.SalesOrder;
import models.eeda.profile.CustomCompany;
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
import controllers.oms.custom.dto.DingDanDto;
import controllers.oms.custom.dto.DingDanGoodsDto;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(PlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/oms/PlanOrder/PlanOrderList.html");
	}
	
    public void create() {
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        SalesOrder salesOrder = new SalesOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			salesOrder = SalesOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, salesOrder);
   			
   			//需后台处理的字段
   			salesOrder.set("update_by", user.getLong("id"));
   			salesOrder.set("update_stamp", new Date());
   			salesOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, salesOrder);
   			
   			//需后台处理的字段
   			salesOrder.set("order_no", OrderNoGenerator.getNextOrderNo("DD"));
   			salesOrder.set("create_by", user.getLong("id"));
   			salesOrder.set("create_stamp", new Date());
   			salesOrder.save();
   			
   			id = salesOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("cargo_list");
		DbUtils.handleList(itemList, id, SalesOrderGoods.class, "order_id");

   		//return dto
   		renderJson(salesOrder);
   	}
    
    
    private List<Record> getSalesOrderGoods(String orderId) {
		String itemSql = "select * from sales_order_goods where order_id=?";
		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	SalesOrder salesOrder = SalesOrder.dao.findById(id);
    	setAttr("order", salesOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getSalesOrderGoods(id));

    	//用户信息
    	long create_by = salesOrder.getLong("create_by");
    	UserLogin user = UserLogin.dao.findById(create_by);
    	setAttr("user", user);
    	
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    
    
    @Before(Tx.class)
    public void getUser() {
    	String id = getPara("params");
    	UserLogin user = UserLogin.dao.findById(id);
    	renderJson(user);
    }
    
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT po.*, ifnull(u.c_name, u.user_name) creator_name "
    			+ "  from plan_order po "
    			+ "  left join user_login u on u.id = po.creator"
    			+ "   where 1 =1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by create_time desc " +sLimit);
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap); 
    }
   

}
