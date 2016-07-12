package controllers.oms.jobOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCargo;
import models.eeda.oms.jobOrder.JobOrderShipment;

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
public class JobOrderController extends Controller {

	private Logger logger = Logger.getLogger(JobOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/oms/JobOrder/JobOrderList.html");
	}
	
    public void create() {
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        JobOrder jobOrder = new JobOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			jobOrder = JobOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, jobOrder);
   			
   			//需后台处理的字段
//   			jobOrder.set("update_by", user.getLong("id"));
//   			jobOrder.set("update_stamp", new Date());
   			jobOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, jobOrder);
   			
   			//需后台处理的字段
   			jobOrder.set("order_no", OrderNoGenerator.getNextOrderNo("GZ"));
   			jobOrder.set("creator", user.getLong("id"));
   			jobOrder.set("create_stamp", new Date());
   			jobOrder.save();
   			
   			id = jobOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, JobOrderCargo.class, "order_id");
		
		List<Map<String, String>> chargeList = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(chargeList, id, JobOrderArap.class, "order_id");
		
		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
		DbUtils.handleList(shipment_detail, id, JobOrderShipment.class, "order_id");
		
		//获取shipment_id
		JobOrderShipment jst = getShiment(id) ;

		long creator = jobOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = jobOrder.toRecord();
   		r.set("creator_name", user_name);
   		
   		r.set("shipment", jst);
   		renderJson(r);
   	}
    
    private JobOrderShipment getShiment(String id){
		JobOrderShipment jst = JobOrderShipment.dao.findFirst("select * from job_order_shipment jos where order_id = ?",id);
		return jst;
    }
    
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("cargo".equals(type)){
    		itemSql = "select * from job_order_cargo where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select * from job_order_arap where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}
		
		return itemList;
	}
   
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	JobOrder jobOrder = JobOrder.dao.findById(id);
    	setAttr("order", jobOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getItems(id,"cargo"));
    	
    	//获取明细表信息
    	setAttr("chargeList", getItems(id,"charge"));
    	
    	setAttr("shipment", getShiment(id));

    	//客户回显
    	Party party = Party.dao.findById(jobOrder.get("customer_id"));
    	setAttr("party", party);
    	
    	
    	//用户信息
    	long creator = jobOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
     
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name "
    			+ "  from job_order jor "
    			+ "  left join party p on p.id = jor.customer_id"
    			+ "  left join user_login u on u.id = jor.creator"
    			+ "   where 1 =1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("sEcho", pageIndex);
        map.put("iTotalRecords", rec.getLong("total"));
        map.put("iTotalDisplayRecords", rec.getLong("total"));

        map.put("aaData", BillingOrders);

        renderJson(map); 
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	
    	List<Record> list = null;
    	if("cargo".equals(type)){
    		list = getItems(order_id,type);
    	}else if("charge".equals(type)){
    		list = getItems(order_id,type);
    	}
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());

        map.put("aaData", list);

        renderJson(map); 
    }
   
   

}
