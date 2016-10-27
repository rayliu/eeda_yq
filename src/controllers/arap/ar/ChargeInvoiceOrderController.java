package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeInvoice;
import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;

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
public class ChargeInvoiceOrderController extends Controller {

	private Logger logger = Logger.getLogger(ChargeInvoiceOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		
		render("/eeda/arap/ChargeInvoiceOrder/ChargeInvoiceOrderList.html");
	}
	
    public void create(){
    	String total_amount = getPara("total_amount");
		String ids = getPara("idsArray");
		String[] idArr = ids.split(",");
		String sql = " select aco.sp_id,p.contact_person,p.phone,p.address from arap_charge_order aco "
				+ " left join party p on p.id = aco.sp_id "
				+ " where aco.id = ? ";
		setAttr("create",Db.findFirst(sql,idArr[0]));
		setAttr("itemList",getItemList(ids));
		setAttr("total_amount",total_amount);
		render("/eeda/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
	}
    
    
    public List<Record> getItemList(String ids){
    	String sql = "select acor.id,acor.order_no,acor.total_amount,acor.`status`,acor.invoice_no,p.abbr payee_name,acor.create_stamp, ul.c_name create_name"
    			+ " from arap_charge_order acor"
    			+ " LEFT JOIN party p on p.id = acor.sp_id"
    			+ " LEFT JOIN user_login ul on ul.id = acor.create_by"
    			+ " where acor.id in("+ids+")" ;	
    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    

    public void createlist() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        
    	sql = " select * from (select acor.id,acor.order_no,acor.total_amount,acor.status,p.abbr payee_name,acor.create_stamp, ul.c_name create_name"
    			+ " from arap_charge_order acor"
    			+ " LEFT JOIN party p on p.id = acor.sp_id"
    			+ " LEFT JOIN user_login ul on ul.id = acor.create_by"
    			+ " where acor.have_invoice='Y' and acor.invoice_order_id is null and acor.status='已确认' "
    			+ " ) A where 1 = 1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }   
    
    
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        
    	sql = " select * from (select acor.id,acor.order_no ,acor.total_amount,p.abbr payee_name ,acor.`status`,acor.create_stamp, ul.c_name create_name"
    			+ " from arap_charge_invoice acor"
    			+ " LEFT JOIN party p on p.id = acor.sp_id"
    			+ " LEFT JOIN user_login ul on ul.id = acor.create_by"
    			+ " ) A where 1 = 1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }   
    

    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        ArapChargeInvoice order = new ArapChargeInvoice();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = ArapChargeInvoice.dao.findById(id);
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSKP", user.getLong("office_id")));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}

   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String item_id = item.get("id");
			String invoice_no = item.get("invoice_no");
			
			ArapChargeOrder aco = ArapChargeOrder.dao.findById(item_id);
			aco.set("invoice_order_id",id);
			aco.set("invoice_no",invoice_no);
			aco.update();
		}
		
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    /**确认动作
     * 
     */
    @Before(Tx.class)
    public void confirm(){
    	String id = getPara("id");
    	ArapChargeInvoice order = ArapChargeInvoice.dao.findById(id);
    	order.set("status","已确认");
    	order.update();
    	
    	renderJson(order);
    }
    
    
    public void edit(){
		String id = getPara("id");//invoice_order_id
		
		String itemSql = "select acor.id,acor.order_no,acor.total_amount,acor.`status`,acor.invoice_no,p.abbr payee_name,acor.create_stamp, ul.c_name create_name"
    			+ " from arap_charge_order acor"
    			+ " LEFT JOIN party p on p.id = acor.sp_id"
    			+ " LEFT JOIN user_login ul on ul.id = acor.create_by"
    			+ " where acor.invoice_order_id = ? ";
		
		String sql = " select aci.*,u.c_name from arap_charge_invoice aci "
				+ " left join user_login u on u.id = aci.create_by "
				+ " where aci.id = ? ";
		
		setAttr("invoiceOrder", Db.findFirst(sql,id));
		setAttr("itemList",Db.find(itemSql,id));
		render("/eeda/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html"); 
	}
    
    
  //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	String condition = "select id from arap_charge_order where invoice_order_id ="+order_id;
    	list = getItemList(condition);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }

}
