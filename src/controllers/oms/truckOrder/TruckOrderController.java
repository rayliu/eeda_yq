package controllers.oms.truckOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.oms.SalesOrder;
import models.eeda.oms.truckOrder.TruckOrder;
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

import controllers.oms.custom.CustomManager;
import controllers.oms.custom.dto.DingDanDto;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TruckOrderController extends Controller {

	private Logger logger = Logger.getLogger(TruckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/oms/TruckOrder/TruckOrderList.html");
	} 
	
    public void create() {
        render("/oms/TruckOrder/TruckOrderEdit.html");
    } 
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        TruckOrder TruckOrder = new TruckOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			TruckOrder = TruckOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, TruckOrder);
   			
   			//需后台处理的字段
   			TruckOrder.set("update_by", user.getLong("id"));
   			TruckOrder.set("update_stamp", new Date());
   			TruckOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, TruckOrder);
   			
   			//需后台处理的字段
   			TruckOrder.set("log_no", OrderNoGenerator.getNextOrderNo("YD"));
   			TruckOrder.set("create_by", user.getLong("id"));
   			TruckOrder.set("create_stamp", new Date());
   			TruckOrder.save();
   			
   			id = TruckOrder.getLong("id").toString();
   		}
   		
   		//List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("cargo_list");
		//DbUtils.handleList(itemList, id, Goods.class, "order_id");

   		//return dto
   		renderJson(TruckOrder);
   	}
    
    
    private List<Record> getSalesOrderGoods(long orderId) {
		String itemSql = "select * from sales_order_goods where order_id=?";
		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	TruckOrder truckOrder = TruckOrder.dao.findById(id);
    	setAttr("order", truckOrder);
    	
    	//订单ID
    	SalesOrder salesOrder = SalesOrder.dao.findById(truckOrder.getLong("sales_order_id"));
    	if(salesOrder != null){
    		long sales_order_id = truckOrder.getLong("sales_order_id");
    		long custom_id = salesOrder.getLong("custom_id");
    		
    		//获取明细表信息
        	setAttr("itemList", getSalesOrderGoods(sales_order_id));
        	
        	//获取报关企业信息
        	CustomCompany custom = CustomCompany.dao.findById(custom_id);
        	setAttr("custom", custom);
    		
    	}
    	
    	
    	
    	//用户信息
    	long create_by = truckOrder.getLong("create_by");
    	UserLogin user = UserLogin.dao.findById(create_by);
    	setAttr("user", user);
    	
        render("/oms/TruckOrder/TruckOrderEdit.html");
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

        String sql = "SELECT sor.*, ifnull(u.c_name, u.user_name) creator_name "
    			+ "  from sales_order sor "
    			+ "  left join user_login u on u.id = sor.create_by"
    			+ "   where 1 =1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap); 
    }
    
    public void getCustomCompany() {
    	String custom_id = getPara("params");
    	CustomCompany customCompany = CustomCompany.dao.findById(custom_id);
    	renderJson(customCompany);
    }

    public void submitDingDan(){
    	DingDanDto dto = new DingDanDto();
    	
    	CustomManager.getInstance().sendDingDan(dto);
    }

}
