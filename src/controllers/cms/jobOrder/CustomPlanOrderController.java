package controllers.cms.jobOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.cms.CustomPlanOrder;
import models.eeda.cms.CustomPlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;

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
public class CustomPlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(CustomPlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/cms/customPlanOrder/CustomPlanOrderlist.html");
	}
	
    public void create() {
        String jobId = getPara("jobOrderId");
        JobOrder jo = JobOrder.dao.findById(jobId);
        setAttr("jobOrder", jo);

        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        CustomPlanOrder customPlanOrder = new CustomPlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			customPlanOrder = CustomPlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("updator", user.getLong("id"));
   			customPlanOrder.set("update_stamp", new Date());
   			customPlanOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("order_no", OrderNoGenerator.getNextOrderNo("BGSQ", office_id));
   			customPlanOrder.set("creator", user.getLong("id"));
   			customPlanOrder.set("create_stamp", new Date());
   			customPlanOrder.set("office_id", office_id);
   			customPlanOrder.save();
   			
   			id = customPlanOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, CustomPlanOrderItem.class, "order_id");
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, "custom_plan_order_doc", id, "order_id");

		long creator = customPlanOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = customPlanOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
   
    //返回list
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("cargo".equals(type)){
    		itemSql = " SELECT cpo.*,cur.name currency_name FROM custom_plan_order_item cpo"
    				+ " left join currency cur on cur.id =cpo.currency where cpo.order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("doc".equals(type)){
    		itemSql = "select jod.*,u.c_name from custom_plan_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? order by jod.id";
    		itemList = Db.find(itemSql, orderId);
    	}
    	return itemList;
    }
    
    
    public void edit() {
    	String id = getPara("id");
    	String sql = "select cpo.*,l.name export_country_name,l1.name import_country_name,l2.name trade_country_name,sm.name supervise_mode_name"
    			+ " from custom_plan_order cpo "
    			+ " left join location l on l.id=cpo.export_country"
    			+ " left join location l1 on l1.id=cpo.import_country"
    			+ " left join location l2 on l2.id=cpo.trade_country"
    			+ " left join supervision_method sm on sm.id = cpo.supervise_mode"
    			+ " where cpo.id = ?";
    	Record r = Db.findFirst(sql,id);
    	setAttr("order", r);
    	setAttr("itemList", getItems(id,"cargo"));
    	setAttr("docList", getItems(id,"doc"));
    	
    	//用户信息
    	long creator = r.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    

    
    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	
    	String customer_code=getPara("customer_code")==null?"":getPara("customer_code");
    	String customer_name=getPara("customer")==null?"":getPara("customer");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        
        	sql = "SELECT * from (SELECT cpo.id,cpo.order_no,cpo.type,cpo.application_company_input application_company_name,ul.c_name creator_name,"
        			+ " cpo.create_stamp,cpo.status"
        			+ " FROM custom_plan_order cpo"
        			+ " LEFT JOIN user_login ul on ul.id = cpo.creator"
        			+ " where cpo.office_id="+office_id+")A"
    		        + " where 1 =1 ";

        condition = DbUtils.buildConditions(getParaMap());
        
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
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
   
    
    //提交申请单给报关行
    public void confirmCompleted(){
    	String id = getPara("id");
    	String plan_order_no = getPara("plan_order_no");
    	String customer_id= getPara("customer_id");
    	String btnId = getPara("btnId");
    	CustomPlanOrder order = CustomPlanOrder.dao.findById(id);
    	if("confirmCompleted".equals(btnId)){
    		order.set("status","处理中");
    	}
    	if("passBtn".equals(btnId)){
    		order.set("status","审核通过");
    		Record r = new Record();
    		r.set("plan_order_id", id);
    		r.set("plan_order_no", plan_order_no);
    		r.set("customer_id", customer_id);
    		
       		UserLogin user = LoginUserController.getLoginUser(this);
       		long office_id = user.getLong("office_id");
       		r.set("creator", user.getLong("id"));
   			r.set("create_stamp", new Date());
   			r.set("office_id", office_id);
    		r.set("order_no", OrderNoGenerator.getNextOrderNo("BGGZD", office_id));
    		Db.save("custom_job_order", r);
    		id = r.getLong("id").toString();
    		
    		
    	}
    	if("refuseBtn".equals(btnId)){
    		order.set("status","审核不通过");
    	}
    	order.update();
    	
    	Record rec = order.toRecord();
    	rec.set("job_order_id", id);
    	renderJson(rec);
    }
    
  //上传相关文档
    @Before(Tx.class)
    public void uploadDocFile(){
    	String order_id = getPara("order_id");
    	List<UploadFile> fileList = getFiles("doc");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		
			Record r = new Record();
			r.set("order_id", order_id);
			r.set("uploader", LoginUserController.getLoginUserId(this));
			r.set("doc_name", fileName);
			r.set("upload_time", new Date());
			Db.save("custom_plan_order_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("custom_plan_order_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("custom_plan_order_doc",r);
            resultMap.put("result", result);
        }else{
        	Db.delete("custom_plan_order_doc",r);
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    

}
