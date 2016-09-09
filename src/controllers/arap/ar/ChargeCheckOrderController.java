package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	
	public void save(){
		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String ids = (String) dto.get("ids");
        
        
        ArapChargeOrder aco = new ArapChargeOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			aco = ArapChargeOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, aco);
   			aco.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, aco);
	   		String orderPrefix = OrderNoGenerator.getNextOrderNo("YSDZ");
	        aco.set("order_no", orderPrefix);
			aco.set("create_by", user.getLong("id"));
			aco.set("create_stamp", new Date());
			aco.save();
			id = aco.getLong("id").toString();
			//设置已创建过对账单flag
			String idAttr[] = ids.split(",");
			for(int i=0 ; i<idAttr.length ; i++){
				JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
				joa.set("bill_flag", "Y");
				joa.update();
			}
   		}
   		
   		String sql = " select aco.*,p.company_name sp_name from arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " where aco.id = ? ";
   		
   		Record r = new Record();
   		r.set("charge", Db.findFirst(sql,id));
   		r.set(""
   				+ "", LoginUserController.getLoginUserName(this));
   		renderJson(r);
	}
	
    
    
    @Before(Tx.class)
	public void edit(){
		String id = getPara("id");
		String sql = " select aco.*,p.company_name sp_name,u.c_name from arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by"
   				+ " where aco.id = ? ";
		setAttr("order", Db.findFirst(sql,id));
		render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}
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
//        render("/oms/ChargeCheckOrder/ChargeCheckOrderEdit.html");
//    }
    

    
    public void list() {
    	String checked = getPara("checked");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        if(checked.equals("Y")){
        	sql = " select * from (select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume vgm,"
        			+ "jo.net_weight gross_weight,jo.total_profitRMB rmb,jo.total_profitUSD usd,jo.total_profitTotalRMB totalrmb,jo.ref_no ref_no,"
        			+ "p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination,jos.hbl_no,jols.truck_type truck_type,"
        			+ "GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
        			+ " from job_order_arap joa"
        			+ "	left join job_order jo on jo.id=joa.order_id "
        			+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
        			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
        			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
        			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "
        			+ "	left join party p1 on p1.id=joa.sp_id "
        			+ "	left join location l on l.id=jos.fnd "
        			+ "	where joa.order_type='charge' and joa.audit_flag='Y' "
        			+ " GROUP BY joa.id) A where 1 = 1 ";
        	}else{
        		sql = " select * from (select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume vgm,"
            			+ "jo.net_weight gross_weight,jo.total_chargeRMB rmb,jo.total_chargeUSD usd,jo.total_profitTotalRMB totalrmb,jo.ref_no ref_no,"
            			+ "p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination,jos.hbl_no,jols.truck_type truck_type,"
            			+ "GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
            			+ " from job_order_arap joa"
            			+ "	left join job_order jo on jo.id=joa.order_id "
            			+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
            			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
            			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
            			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "
            			+ "	left join party p1 on p1.id=joa.sp_id "
            			+ "	left join location l on l.id=jos.fnd "
            			+ "	where joa.order_type='charge' and joa.audit_flag='Y' and joa.bill_flag !='Y' "
            			+ " GROUP BY joa.id) A where 1 = 1 ";		
        				
        			}
        
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
    
    public void checkedList(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";        
        		sql = " select * from (select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume vgm,"
            			+ "jo.net_weight gross_weight,jo.total_chargeRMB rmb,jo.total_chargeUSD usd,jo.total_profitTotalRMB totalrmb,jo.ref_no ref_no,"
            			+ "p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination,jos.hbl_no,jols.truck_type truck_type,"
            			+ "GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
            			+ " from job_order_arap joa"
            			+ "	left join job_order jo on jo.id=joa.order_id "
            			+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
            			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
            			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
            			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "
            			+ "	left join party p1 on p1.id=joa.sp_id "
            			+ "	left join location l on l.id=jos.fnd "
            			+ "	where joa.order_type='charge' and joa.audit_flag='Y' and joa.bill_flag='Y'"
            			+ " GROUP BY joa.id) A where 1 = 1 ";	  				
        			
        
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
    
	public void create(){
		//当前登陆用户
		String OrderIds = getPara("OrderIds");
		String totalAmount = getPara("totalAmount");
		
		String strAry[] = OrderIds.split(",");
		String id = strAry[0];
		String sql = " select * from ( SELECT joa.sp_id,p.company_name sp_name,jos.shipper_info,"
				+ " jo.total_profitRMB total_profitRMB,jo.total_profitTotalCost total_profitTotalCost,jo.total_profitTotalRMB total_profitTotalRMB"
				+ " FROM job_order_arap joa	"
				+ " LEFT JOIN party p ON p.id = joa.sp_id "
				+ " LEFT JOIN job_order_shipment jos on jos.order_id = joa.order_id"
				+ " LEFT JOIN job_order  jo on jo.id = joa.order_id"
				+ " where joa.id= ? ) A where 1 = 1";
		Record rec =Db.findFirst(sql,id);
		
		String shipper_info= rec.get("shipper_info");
		if(shipper_info.isEmpty()){
			shipper_info="";
		}
		String[] info = shipper_info.split("\n");
		String address = info[0];
		String name = info[1];
		String phone = info[2];
		setAttr("sp",rec);
		setAttr("totalAmount",totalAmount);
		setAttr("address",address);
		setAttr("name",name);
		setAttr("phone",phone);
		setAttr("ids",OrderIds);
		setAttr("loginUser", LoginUserController.getLoginUserName(this));
		render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}
    
    
    
    
    
    
    
    public void create1(){
    	String orderIds = getPara("returnOrderIds");
    	String[] orderId = orderIds.split(",");
    	JobOrderArap joa = null;
    	for(int i=0;i<orderId.length;i++){
    		joa = JobOrderArap.dao.findById(orderId[i]);
    		joa.set("bill_flag", "Y");    		
    		joa.update();
    	}
    	renderJson(joa);
    }
   

}
