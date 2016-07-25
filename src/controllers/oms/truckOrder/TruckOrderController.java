package controllers.oms.truckOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;
import models.eeda.oms.truckOrder.TruckOrder;
import models.eeda.oms.truckOrder.TruckOrderArap;
import models.eeda.oms.truckOrder.TruckOrderCargo;

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
public class TruckOrderController extends Controller {

	private Logger logger = Logger.getLogger(TruckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/oms/TruckOrder/TruckOrderList.html");
	} 
	
	public void create() {
    	
    	String order_id=getPara("order_id");
    	String itemIds=getPara("itemIds");
    	if(StringUtils.isNotEmpty(order_id)){
    		//查询job_order 里的工作单号
    		JobOrder jobOrder = JobOrder.dao.findById(order_id);
        	setAttr("jobOrder", jobOrder);
    	}

    	if(StringUtils.isNotEmpty(itemIds)){
    		//查询job_order_cargo
			String sql="select * from job_order_cargo where id in("+itemIds+")";
	    	List<Record> jobOrderCargo= Db.find(sql);
	    	for(Record re : jobOrderCargo){
	    		re.set("id", null);
	    	}
	    	setAttr("cargoList", jobOrderCargo);
    	}
    	
        render("/oms/TruckOrder/TruckOrderEdit.html");
    } 
    
    
    @Before(Tx.class)
   	public void save() throws Exception {	
    	
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        TruckOrder truckOrder = new TruckOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			truckOrder =TruckOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, truckOrder);
   			
   			truckOrder.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, truckOrder);
   			
   			//需后台处理的字段
   			truckOrder.set("order_no", OrderNoGenerator.getNextOrderNo("PC"));
   			truckOrder.set("creator", user.getLong("id"));
   			truckOrder.set("create_stamp", new Date());
   			truckOrder.save();
   			
   			id = truckOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> cargoList = (ArrayList<Map<String, String>>)dto.get("cargo_list");
		DbUtils.handleList(cargoList, id, TruckOrderCargo.class, "order_id");
		
		List<Map<String, String>> chargeList = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(chargeList, id, TruckOrderArap.class, "order_id");        
		
		long creator = truckOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
   		Record r = truckOrder.toRecord();
   		r.set("creator_name", user_name);
   		
   		renderJson(r);
   	}
    
 
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	TruckOrder truckOrder = TruckOrder.dao.findById(id);
    	setAttr("order", truckOrder);
    	//获取明细表信息
    	setAttr("cargoList", getItems(id,"cargo"));
    	//获取明细表信息
    	setAttr("chargeList", getItems(id,"charge"));
    	
    	//查询customer_id,sp_id,load_sp_id,unload_sp_id
    	//构造供应商回显
    	Party sp = Party.dao.findById(truckOrder.getLong("sp_id"));
    	setAttr("sp",sp);
    	
    	Party unload_sp = Party.dao.findById(truckOrder.getLong("unload_sp_id"));
    	setAttr("unload_sp",unload_sp);
    	
    	Party load_sp = Party.dao.findById(truckOrder.getLong("load_sp_id"));
    	setAttr("load_sp",load_sp);
    	
    	Party customer = Party.dao.findById(truckOrder.getLong("customer_id"));
    	setAttr("customer",customer);
    	
    	//创建人回显信息
    	long create_by = truckOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(create_by);
    	setAttr("user", user);
    	
        render("/oms/TruckOrder/TruckOrderEdit.html");
    }
    
    
  
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        
        String sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p2.abbr sp_name"
    			+ "  from truck_order jor "
    			+ "  left join party p on p.id = jor.customer_id"
    			+ "  left join party p2 on p2.id = jor.sp_id"
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


    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("cargo".equals(type)){
    		itemSql = "select * from truck_order_cargo where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select * from truck_order_arap where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}
		
		return itemList;
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
