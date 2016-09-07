package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
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
            
        PlanOrder planOrder = new PlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
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
   			planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("JH"));
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    
    private List<Record> getPlanOrderItems(String orderId) {
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,"
                + " p.abbr carrier_name "
                + " from plan_order_item pi "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
                +" left join party p on pi.carrier=p.id"
                +" left join unit u on u.id=pi.unit_id"
                +" where order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    
    @Before(Tx.class)
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
    	
        render("/oms/ChargeCheckOrder/ChargeCheckOrderEdit.html");
    }
    

    
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        
        	sql = " select * from (select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume vgm,"
        			+ "jo.net_weight gross_weight,jo.total_chargeRMB rmb,jo.total_chargeUSD usd,jo.ref_no ref_no,"
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
    
    public void create2(){
    	render("/eeda/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
    }
    
	public void create(){
		//当前登陆用户
		String OrderIds = getPara("OrderIds");
		String totalAmount = getPara("totalAmount");
		
//		String strAry[] = OrderIds.split(",");
//		String id = strAry[0];
		String sql = " select joa.sp_id,p.company_name sp_name from job_order_arap joa "
				   + " left join party p on p.id = joa.sp_id "
				   + " where joa.id in (?) ";
		setAttr("sp",Db.find(sql,OrderIds));
		setAttr("totalAmount",totalAmount);
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
