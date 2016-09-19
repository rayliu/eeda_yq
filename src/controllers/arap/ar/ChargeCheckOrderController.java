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
import models.eeda.oms.jobOrder.JobOrderAir;
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
		String id = getPara("id");
		String condition = "select ref_order_id from arap_charge_item where charge_order_id ="+id;
		
		ArapChargeOrder aco = ArapChargeOrder.dao.findById(id);
		long create_by = aco.getLong("create_by");
		UserLogin ul = UserLogin.dao.findById(create_by);
		setAttr("checkOrder", aco);
		setAttr("user", ul);
		
		
		String sql = "SELECT jos.shipper_info, "
				+ " sum(case when cur.name = 'CNY' "
				+ " then (joa.total_amount) "
				+ " when cur.name = 'USD'  "
				+ " then (joa.total_amount*joa.exchange_rate)"
				+ " end ) total_CNY"
				+ " FROM"
				+ " job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " LEFT JOIN job_order_shipment jos ON jos.order_id = joa.order_id"
				+ " LEFT JOIN job_order jo ON jo.id = joa.order_id"
				+ " WHERE"
				+ " joa.id in("+ condition +")"
				+ " group by joa.order_id";
		Record rec =Db.findFirst(sql);
		
		String address = null;
		String customer = null;
		String phone = null;
		String shipper_info = rec.get("shipper_info");
		if(StringUtils.isNotEmpty(shipper_info)){
			String[] info = shipper_info.split("\n");
			if(info.length == 3){
				 address = info[0];
				 customer = info[1];
				 phone = info[2];
			}else if(info.length == 2){
				 address = info[0];
				 customer = info[1];
			}else{
				 address = info[0];
			}
		}	
		rec.set("address", address);
		rec.set("customer", customer);
		rec.set("phone", phone);
		rec.set("user", LoginUserController.getLoginUserName(this));
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
            		+ " jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
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
                 		+ " jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
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
		//当前登陆用户
		String ids = getPara("idsArray");
		
		
		String sql = "SELECT jos.shipper_info, "
				+ " sum( ifnull(joa.currency_total_amount,0) ) total_amount "
				+ " FROM "
				+ " job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " LEFT JOIN job_order_shipment jos ON jos.order_id = joa.order_id"
				+ " LEFT JOIN job_order jo ON jo.id = joa.order_id"
				+ " WHERE"
				+ " joa.id in("+ ids +")"
				+ " group by joa.order_id";
		Record rec =Db.findFirst(sql);
		
		String address = null;
		String customer = null;
		String phone = null;
		String shipper_info = rec.get("shipper_info");
		if(StringUtils.isNotEmpty(shipper_info)){
			String[] info = shipper_info.split("\n");
			if(info.length == 3){
				 address = info[0];
				 customer = info[1];
				 phone = info[2];
			}else if(info.length == 2){
				 address = info[0];
				 customer = info[1];
			}else{
				 address = info[0];
			}
		}	
		rec.set("idsArray", ids);
		rec.set("address", address);
		rec.set("customer", customer);
		rec.set("phone", phone);
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


}
