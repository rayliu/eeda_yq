package controllers.oms.truckOrder;

import interceptor.EedaMenuInterceptor;
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
import models.eeda.oms.jobOrder.JobOrderLandItem;
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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TruckOrderController extends Controller {

	private Logger logger = Logger.getLogger(TruckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	@Before(EedaMenuInterceptor.class)
	public void index() {
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/truckOrder");
        setAttr("listConfigList", configList);
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
   	public void save(){	
    	
   		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
   		String id = (String) dto.get("id");
   		JobOrderLandItem truckOrder = JobOrderLandItem.dao.findById(id);
   	
		DbUtils.setModelValues(dto, truckOrder);
		truckOrder.update();
   		
   		Record r = truckOrder.toRecord();
   		renderJson(r);
   	}
    
 
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	String order_id = getPara("order_id");
    	JobOrderLandItem truckOrder = JobOrderLandItem.dao.findById(id);
    	JobOrder jobOrder = JobOrder.dao.findById(order_id);
    	setAttr("truckOrder", truckOrder);
    	setAttr("jobOrder", jobOrder);
        render("/oms/TruckOrder/TruckOrderEdit.html");
    }
    
    
  
    public void list() {
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        String sql = "SELECT * from ( select jol.*,jo.order_no,jo.create_stamp"
    			+ "  from job_order_land_item jol "
    			+ "  left join job_order jo on jo.id=jol.order_id"
    			 + " and jo.delete_flag = 'N'"
 				+ "   ) A where 1 =1";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> list = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", list);
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
        map.put("iTotalRecords", list != null? list.size():0);
        map.put("iTotalDisplayRecords",  list != null? list.size():0);

        map.put("aaData", list);

        renderJson(map); 
    }
    
}
