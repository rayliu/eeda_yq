package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.eeda.oms.jobOrder.JobOrderArap;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeInvoiceOrderController extends Controller {

	private Logger logger = Logger.getLogger(ChargeInvoiceOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		
		render("/eeda/arap/ChargeInvoiceOrder/ChargeInvoiceOrderList.html");
	}
	
    public void create() {
        render("/oms/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }
    
//    @Before(Tx.class)
//   	public void save() throws Exception {		
//   		String jsonStr=getPara("params");
//       	
//       	Gson gson = new Gson();  
//        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
//            
//        PlanOrder planOrder = new PlanOrder();
//   		String id = (String) dto.get("id");
//   		
//   		UserLogin user = LoginUserController.getLoginUser(this);
//   		
//   		if (StringUtils.isNotEmpty(id)) {
//   			//update
//   			planOrder = PlanOrder.dao.findById(id);
//   			DbUtils.setModelValues(dto, planOrder);
//   			
//   			//需后台处理的字段
//   			planOrder.set("updator", user.getLong("id"));
//   			planOrder.set("update_stamp", new Date());
//   			planOrder.update();
//   		} else {
//   			//create 
//   			DbUtils.setModelValues(dto, planOrder);
//   			
//   			//需后台处理的字段
//   			planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("JH"));
//   			planOrder.set("creator", user.getLong("id"));
//   			planOrder.set("create_stamp", new Date());
//   			planOrder.save();
//   			
//   			id = planOrder.getLong("id").toString();
//   		}
//   		
//   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
//		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");
//
//		long creator = planOrder.getLong("creator");
//   		String user_name = LoginUserController.getUserNameById(creator);
//		Record r = planOrder.toRecord();
//   		r.set("creator_name", user_name);
//   		renderJson(r);
//   	}
//    
//    
//    private List<Record> getPlanOrderItems(String orderId) {
//        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,"
//                + " p.abbr carrier_name "
//                + " from plan_order_item pi "
//                +" left join location l_por on pi.por=l_por.id"
//                +" left join location l_pol on pi.pol=l_pol.id"
//                +" left join location l_pod on pi.pod=l_pod.id"
//                +" left join party p on pi.carrier=p.id"
//                +" left join unit u on u.id=pi.unit_id"
//                +" where order_id=?";
//
//		List<Record> itemList = Db.find(itemSql, orderId);
//		return itemList;
//	}
    
//    
//    @Before(Tx.class)
//    public void edit() {
//    	String id = getPara("id");
//    	PlanOrder planOrder = PlanOrder.dao.findById(id);
//    	setAttr("order", planOrder);
//    	
//    	//获取明细表信息
//    	setAttr("itemList", getPlanOrderItems(id));
//    	
//    	//回显客户信息
//    	Party party = Party.dao.findById(planOrder.getLong("customer_id"));
//    	setAttr("party", party);
//
//    	//用户信息
//    	long creator = planOrder.getLong("creator");
//    	UserLogin user = UserLogin.dao.findById(creator);
//    	setAttr("user", user);
//    	
//        render("/oms/ChargeConfirm/ChargeConfirmEdit.html");
//    }
    

    
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        
        	sql = " select * from (SELECT joa.*, jo.order_no order_no,pr.abbr sp_name,f.name cost_name,jo.create_stamp,"
        			+ " u. NAME unit_name,c. NAME currency_name"
        			+ " FROM job_order_arap joa"
        			+ " LEFT JOIN job_order jo ON joa.order_id = jo.id"
        			+ " LEFT JOIN party pr ON pr.id = joa.sp_id "
        			+ " LEFT JOIN fin_item f ON f.id = joa.charge_id"
        			+ " LEFT JOIN unit u ON u.id = joa.unit_id"
        			+ " LEFT JOIN currency c ON c.id = joa.currency_id"
        			+ " WHERE joa.order_type = 'charge') A where 1 = 1 ";
        
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
    
    public void chargeConFirmReturnOrder(){
    	String returnOrderIds = getPara("returnOrderIds");
    	String orderType = getPara("orderTypes");
    	String[] idArr = returnOrderIds.split(",");
    	String[] orderTypeArr = orderType.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		if("海运".equals(orderTypeArr[i])){
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}else if("空运".equals(orderTypeArr[i])){
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}else if("陆运".equals(orderTypeArr[i])){
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}else if("报关".equals(orderTypeArr[i])){
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}else if("保险".equals(orderTypeArr[i])){
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}else{
    			JobOrderArap jobOrderArap = JobOrderArap.dao.findById(idArr[i]);
    			jobOrderArap.set("audit_flag", "Y");
    			jobOrderArap.set("create_time", new Date());
    			jobOrderArap.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }


}
