package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ChargeCheckOrderController extends Controller {

	private Logger logger = Logger.getLogger(ChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		
		render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
	}
	
	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        ArapChargeOrder order = new ArapChargeOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = ArapChargeOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSDZ"));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}

   		ArapChargeItem aci = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String action = item.get("action");
			String itemId = item.get("id");
			if("CREATE".equals(action)){
				aci = new ArapChargeItem();
				aci.set("ref_order_type", "工作单");
				aci.set("ref_order_id", itemId);
				aci.set("charge_order_id", id);
				aci.save();
                JobOrderArap jobOrderArap = JobOrderArap.dao.findById(itemId);
                jobOrderArap.set("bill_flag", "Y");
                jobOrderArap.update();

			}
		}
		
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}

    
   
    public void edit(){
		String id = getPara("id");//arap_charge_order id
		String condition = "select ref_order_id from arap_charge_item where charge_order_id ="+id;
		
		String sql = " select aco.*,p.abbr sp_name,p.contact_person,p.phone,p.address,u.c_name creator_name,u1.c_name confirm_by_name from arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record rec =Db.findFirst(sql,id);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		setAttr("itemList",getItemList(condition));
		setAttr("order",rec);
		render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}
    

    
    public void list() {
    	String checked = getPara("checked");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	
        	 sql = "select * from(  "
            		+ " select joa.id,joa.type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
            		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
            		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
            		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
            		+ " cur.name currency_name,joli.truck_type "
    				+ " from job_order_arap joa "
    				+ " left join job_order jo on jo.id=joa.order_id "
    				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
    				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
    				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
    				+ " left join party p on p.id=joa.sp_id "
    				+ " left join party p1 on p1.id=jo.customer_id "
    				+ " left join location l on l.id=jos.fnd "
    				+ " left join currency cur on cur.id=joa.currency_id "
    				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
    				+ " where joa.order_type='charge' and joa.audit_flag='Y' "
    				+ " GROUP BY joa.id "
    				+ " ) B where 1=1 ";
        	}else{
        		 sql = "select * from(  "
                 		+ " select joa.id,joa.type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
                 		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
                 		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
                 		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
                 		+ " cur.name currency_name,joli.truck_type "
         				+ " from job_order_arap joa "
         				+ " left join job_order jo on jo.id=joa.order_id "
         				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
         				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
         				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
         				+ " left join party p on p.id=joa.sp_id "
         				+ " left join party p1 on p1.id=jo.customer_id "
         				+ " left join location l on l.id=jos.fnd "
         				+ " left join currency cur on cur.id=joa.currency_id "
         				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
         				+ " where joa.order_type='charge' and joa.audit_flag='Y' and joa.bill_flag='N' "
         				+ " GROUP BY joa.id "
         				+ " ) B where 1=1 ";
        			}
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
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
    
    public void checkedList(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        			
        String sql = "select * from(  "
        		+ " select aco.id,aco.order_no,aco.create_stamp,aco.status,aco.total_amount,c.pay_amount paid_amount,p.abbr sp_name "
				+ " from arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " left join charge_application_order_rel c on c.charge_order_id=aco.id "
				+ " ) B where 1=1 ";
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
    
    
    public List<Record> getItemList(String ids){
    	String sql = " select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume vgm,"
    			+ " jo.net_weight gross_weight,"
    			+ " ifnull(joa.currency_total_amount,0) rmb,"
    			+ " if(cur.name='USD',joa.total_amount,0) usd,"
    			+ " jo.total_profitTotalRMB totalrmb,jo.ref_no ref_no,"
    			+ " p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination,jos.hbl_no,jols.truck_type truck_type,"
    			+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
    			+ " from job_order_arap joa"
    			+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
    			+ "	left join job_order jo on jo.id=joa.order_id "
    			+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
    			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
    			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
    			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "
    			+ "	left join party p1 on p1.id=joa.sp_id "
    			+ "	left join location l on l.id=jos.fnd "
    			+ "	where joa.order_type='charge' and joa.audit_flag='Y' "
    			+ " and joa.id in("+ids+")"
    			+ " GROUP BY joa.id";	
    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    
	public void create(){
		String ids = getPara("idsArray");//job_order_arap ids
		
		String sql = "SELECT p.phone,p.contact_person,p.address,joa.sp_id,joa.order_id,"
				+ " sum( ifnull(joa.currency_total_amount,0) ) total_amount "
				+ " FROM job_order_arap joa"
				+ " left join party p on p.id = joa.sp_id "
				+ " WHERE joa.id in("+ ids +")"
				+ " group by joa.order_id";
		Record rec =Db.findFirst(sql);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("user", LoginUserController.getLoginUserName(this));
		setAttr("itemList",getItemList(ids));
		setAttr("order",rec);
		render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

	
	//异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	String condition = "select ref_order_id from arap_charge_item where charge_order_id ="+order_id;
    	list = getItemList(condition);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }

    
    public void confirm(){
		String id = getPara("id");
		ArapChargeOrder aco = ArapChargeOrder.dao.findById(id);
		aco.set("status","已确认");
		aco.set("confirm_stamp", new Date());
		aco.set("confirm_by", LoginUserController.getLoginUserId(this));
		aco.update();
		Record r = aco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
		renderJson(r);
	}

}
