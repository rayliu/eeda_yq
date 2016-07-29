package controllers.oms.jobOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAir;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderInsurance;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;

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
import com.jfinal.upload.UploadFile;

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
    	
    	String order_id=getPara("order_id");
    	String itemIds=getPara("itemIds");
    	if(StringUtils.isNotEmpty(order_id)){
    		//查询plan_order 里的计划单号
    		PlanOrder planOrder = PlanOrder.dao.findById(order_id);
        	setAttr("plan", planOrder);
    	}

    	if(StringUtils.isNotEmpty(itemIds)){
    		
    		String strAry[] = itemIds.split(",");
    		String id = strAry[0];
    		//查询plan_order_item
	    	PlanOrderItem plan_order_item = PlanOrderItem.dao.findById(id);
	    	setAttr("planOrder", plan_order_item);
    	}
    	
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    
    
    @SuppressWarnings("unchecked")
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
		
		//海运
		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
		DbUtils.handleList(shipment_detail, id, JobOrderShipment.class, "order_id");
		
		List<Map<String, String>> shipment_item = (ArrayList<Map<String, String>>)dto.get("shipment_list");
		DbUtils.handleList(shipment_item, id, JobOrderShipmentItem.class, "order_id");
		//空运
		List<Map<String, String>> air_detail = (ArrayList<Map<String, String>>)dto.get("air_detail");
		DbUtils.handleList(air_detail, id, JobOrderAir.class, "order_id");
		
		List<Map<String, String>> air_item = (ArrayList<Map<String, String>>)dto.get("air_list");
		DbUtils.handleList(air_item, id, JobOrderAirItem.class, "order_id");
		
		//陆运
		List<Map<String, String>> land_item = (ArrayList<Map<String, String>>)dto.get("land_list");
		DbUtils.handleList(land_item, id, JobOrderLandItem.class, "order_id");
		
		//报关
		List<Map<String, String>> custom_detail = (ArrayList<Map<String, String>>)dto.get("custom_detail");
		DbUtils.handleList(custom_detail, id, JobOrderCustom.class, "order_id");
		
		//保险
		List<Map<String, String>> insurance_detail = (ArrayList<Map<String, String>>)dto.get("insurance_detail");
		DbUtils.handleList(insurance_detail, id, JobOrderInsurance.class, "order_id");
		
		//费用明细，应收应付，chargeCost_detail
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, id, JobOrderArap.class, "order_id");
		List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
		DbUtils.handleList(chargeCost_list, id, JobOrderArap.class, "order_id");

		//相关文档
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, id, JobOrderDoc.class, "order_id");
		
		long creator = jobOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = jobOrder.toRecord();
   		r.set("creator_name", user_name);
   		
   		r.set("shipment", getItemDetail(id,"shipment"));
    	r.set("air", getItemDetail(id,"air"));
   		r.set("custom", getItemDetail(id,"custom"));
   		r.set("insurance", getItemDetail(id,"insure"));
   		
//   	r.set("airList", getItems(id,"air"));
//    	r.set("landList", getItems(id,"land"));
//    	r.set("shipmentList",getItems(id,"shipment"));
//   	r.set("docList", getItems(id,"doc"));
   		renderJson(r);
   	}
    
    //上传文件
    public void saveDocFile(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	int order_id = getParaToInt("order_id");
    	
    	List<UploadFile> returnImg = getFiles("doc");
    	
		for (int i = 0; i < returnImg.size(); i++) {
    		File file = returnImg.get(i).getFile();
    		String fileName = file.getName();
    		
			JobOrderDoc jobOrderDoc = new JobOrderDoc();
			jobOrderDoc.set("order_id", order_id);
			jobOrderDoc.set("uploader", user.getLong("id"));
			jobOrderDoc.set("doc_name", fileName);
			jobOrderDoc.set("upload_time", new Date());
			jobOrderDoc.save();
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    //删除文件
//    public void deleteDoc(){
//    	String id = getPara("id");
//    	String fileName = JobOrderDoc.dao.findById(id).getStr("doc_name");
//    	
//    	List<UploadFile> returnImg = getFiles("doc");
//		for (int i = 0; i < returnImg.size(); i++) {
//    		File file = returnImg.get(i).getFile();
//    		if(file.getName()==fileName){
//    			file.delete();
//    		}
//		}
//    }
    
    private Record getItemDetail(String id,String type){
    	Record re = null;
    	if("shipment".equals(type))
    		re = Db.findFirst("select jos.*, p1.abbr shipperAbbr , p2.abbr consigneeAbbr, p3.abbr notify_partyAbbr from job_order_shipment jos "
    				+ " left join party p1 on p1.id=jos.shipper"
    				+ " left join party p2 on p2.id=jos.consignee"
    				+ " left join party p3 on p3.id=jos.notify_party"
    				+ " where order_id = ?",id);
    	else if("custom".equals(type)){
    		re = Db.findFirst("select * from job_order_custom joc where order_id = ?",id);
    	}else if("insure".equals(type)){
    		re = Db.findFirst("select * from job_order_insurance joi where order_id = ?",id);
    	}else if("air".equals(type)){
    		re = Db.findFirst("select joa.* ,p1.abbr shipperAbbr,p2.abbr consigneeAbbr,p3.abbr notify_partyAbbr from job_order_air joa"
    				+ " left join party p1 on p1.id=joa.shipper"
    				+ " left join party p2 on p2.id=joa.consignee"
    				+ " left join party p3 on p3.id=joa.notify_party"
    				+ " where order_id=?", id);
    	}
		return re;
    }
    
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("shipment".equals(type)){
    		itemSql = "select * from job_order_shipment_item jos where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("air".equals(type)){
    		itemSql = "select * from job_order_air_item joa where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("land".equals(type)){
    		itemSql = "select * from job_order_land_item jol where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select * from job_order_arap  jor where order_id=? and order_type=?";
    		itemList = Db.find(itemSql, orderId,"charge");
    	}else if("cost".equals(type)){
	    	itemSql = "select * from job_order_arap  jor where order_id=? and order_type=?";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from job_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? ";
	    	itemList = Db.find(itemSql, orderId);
	    }
		return itemList;
	}
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	JobOrder jobOrder = JobOrder.dao.findById(id);
    	setAttr("order", jobOrder);

    	//获取海运明细表信息
    	setAttr("shipmentList", getItems(id,"shipment"));
    	setAttr("shipment", getItemDetail(id,"shipment"));
    	
    	//获取海运明细表信息
    	setAttr("airList", getItems(id,"air"));
    	setAttr("air", getItemDetail(id,"air"));
    	
    	//获取陆运明细表信息
    	setAttr("landList", getItems(id,"land"));
    	
    	//报关
    	setAttr("custom", getItemDetail(id,"custom"));
    	
    	//保险
    	setAttr("insurance", getItemDetail(id,"insure"));
    	
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));

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
    	list = getItems(order_id,type);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());

        map.put("aaData", list);

        renderJson(map); 
    }
   
   

}
