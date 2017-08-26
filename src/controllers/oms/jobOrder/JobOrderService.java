package controllers.oms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.UserCustomer;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.bookOrder.BookOrderDoc;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAir;
import models.eeda.oms.jobOrder.JobOrderAirCargoDesc;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderExpress;
import models.eeda.oms.jobOrder.JobOrderInsurance;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderSendMail;
import models.eeda.oms.jobOrder.JobOrderSendMailTemplate;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import sun.misc.BASE64Encoder;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.FileUploadUtil;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class JobOrderService extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(JobOrderService.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
    public String show() {
	    String template = "";
	    String type="";
        try {
            type = getPara("type");
            setAttr("type",type);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        List<Record> configList = ListConfigController.getConfig(user_id, "/jobOrder");
        setAttr("listConfigList", configList);
        if("lock".equals(type)){
            template="/oms/JobOrder/JobOrderLockList.html";
            render(template);
        }else{
            template="/oms/JobOrder/JobOrderList.html";
            render(template);
        }
        return template;
	}
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type="";
        try {
            type = getPara("type");
            setAttr("type",type);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/jobOrder");
        setAttr("listConfigList", configList);
        if("lock".equals(type)){
        	render("/oms/JobOrder/JobOrderLockList.html");
        }else{
        	render("/oms/JobOrder/JobOrderList.html");
        }
		
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
    	
    	String order_id=getPara("order_id");
    	String itemIds=getPara("itemIds");
    	if(StringUtils.isNotEmpty(order_id)){
    		//查询plan_order 里的计划单号
    		PlanOrder planOrder = PlanOrder.dao.findById(order_id);
        	setAttr("planOrder", planOrder);
        	//客户回显
        	Party party = Party.dao.findById(planOrder.get("entrusted_id"));
        	setAttr("party", party);
    	}

    	if(StringUtils.isNotEmpty(itemIds)){
    		
    		String strAry[] = itemIds.split(",");
    		String id = strAry[0];
    		//查询plan_order_item
	    	PlanOrderItem plan_order_item = PlanOrderItem.dao.findById(id);
	    	setAttr("planOrderItem", plan_order_item);
	    	
	    	String transport_type = "";
	    	String truct_type = plan_order_item.getStr("truck_type");
	    	String container_type = plan_order_item.getStr("container_type");
	    	if(StringUtils.isNotBlank(truct_type)){
	    		if(StringUtils.isBlank(transport_type)){
	    			transport_type += "land";
	    		}else{
	    			transport_type += ",land";
	    		}
	    	}
	    	if(StringUtils.isNotBlank(container_type)){
	    		if(StringUtils.isBlank(transport_type)){
	    			transport_type += "ocean";
	    		}else{
	    			transport_type += ",ocean";
	    		}
	    	}
	    	setAttr("transport_type", transport_type);
	    	
	    	
	    	//返回海运的港口名称,加多一个船公司
	    	String port_sql = "select lo.name por_name,lo1.name pol_name,lo2.name pod_name,p.abbr carrier_name from plan_order_item joi"
				    			+" LEFT JOIN location lo on lo.id = joi.por"
				    			+" LEFT JOIN location lo1 on lo1.id = joi.pol"
				    			+" LEFT JOIN location lo2 on lo2.id = joi.pod"
				    			+ " left join party p on p.id = joi.carrier"
				    			+" where joi.id = ?";
	    	setAttr("portCreate",Db.findFirst(port_sql,id));
    	}
    	setAttr("usedOceanInfo", getUsedOceanInfo());
    	setAttr("usedAirInfo", getUsedAirInfo());
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUser(this));
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    //插入动作MBL标识符
    public void mblflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jos = JobOrderShipment.dao.findFirst("select id from job_order_shipment where order_id = ?",jsonStr);
    	jos.set("mbl_flag", "Y");
    	jos.update();
    	renderJson("{\"result\":true,\"mbl_flag\":\"Y\"}");
    }
   //已派车的标记位
    public void sendTruckorder(){
    	String order_id = getPara("order_id");
    	JobOrder job_order = JobOrder.dao.findById(order_id);
    	job_order.set("send_truckorder_flag", "Y");
    	job_order.update();
    	renderJson("{\"result\":true,\"send_truckorder_flag\":\"Y\"}");
    }
    
    //已电放确认表标识
    public void alreadyInlineFlag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jos = JobOrderShipment.dao.findFirst("select id from job_order_shipment where order_id = ?",jsonStr);
    	jos.set("in_line_flag", "Y");
    	jos.update();
    	renderJson("{\"result\":true}");
    }

    //插入打印动作SI标识符
    public void siflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jos = JobOrderShipment.dao.findFirst("select id from job_order_shipment where order_id = ?",jsonStr);
    	jos.set("si_flag", "Y");
    	jos.update();
    	renderJson("{\"result\":true}");
    }
    
    //插入派车单打印动作标记
    public void truckOrderflag(){
    	String jsonStr = getPara("itemId");
    	String order_id = getPara("order_id");
    	JobOrderLandItem joli = JobOrderLandItem.dao.findFirst("select id from job_order_land_item where id = ?",jsonStr);
    	joli.set("truckorder_flag", "Y");
    	joli.update();
    	
    	Record count_joli = Db.findFirst("SELECT count(1) count from job_order jo "
				+ " LEFT JOIN job_order_land_item joli on jo.id = joli.order_id WHERE joli.order_id = ?", order_id);
    	
    	Record rjoli = Db.findFirst("SELECT GROUP_CONCAT(truckorder_flag) truckorder_msg from job_order jo "
    								+ " LEFT JOIN job_order_land_item joli on jo.id = joli.order_id WHERE joli.order_id = ?", order_id);
    	long  count = count_joli.getLong("count");
    	long count_flag = 0;
    	String truckorder_msg = rjoli.get("truckorder_msg");
    	String [] stringArr= truckorder_msg.split(",");
    	for(String s :stringArr){
    		if("Y".equals(s)){
    			count_flag++;
    		}
    	}
    	if(count_flag==count){
    		 Db.update("UPDATE job_order SET send_truckorder_flag='Y' WHERE id = ?",order_id);
    	}else{
    		 Db.update("UPDATE job_order SET send_truckorder_flag='N' WHERE id = ?",order_id);
    	}
    	renderJson("{\"result\":true}");
    }
    
    //插入打印动作AFR/AMS标识符
    public void aframsflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jos = JobOrderShipment.dao.findFirst("select id from job_order_shipment where order_id = ?",jsonStr);
    	jos.set("afr_ams_flag", "Y");
    	jos.update();
    	renderJson("{\"result\":true}");
    }
    
    //根据工作单类型生成不同前缀
    public static String generateJobPrefix(String type){
    		String prefix = "";
			if(type.equals("出口柜货")||type.equals("进口柜货")||type.equals("出口散货")||type.equals("内贸海运")){
				prefix+="O";
			}
			else if(type.equals("出口空运")||type.equals("进口空运")){
				prefix+="A";
			}
			else if(type.equals("香港头程")||type.equals("香港游")||type.equals("进口散货")){
				prefix+="L";
			}
			else if(type.equals("加贸")||type.equals("园区游")){
				prefix+="P";
			}
			else if(type.equals("陆运")){
				prefix+="T";
			}
			else if(type.equals("报关")){
				prefix+="C";
			}
			else if(type.equals("快递")){
				prefix+="E";
			}
			else if(type.equals("贸易")){
				prefix+="B";
			}
			return prefix;
    }
    
    @SuppressWarnings("unchecked")
	@Before(Tx.class)
   	public void save() throws Exception {	
    	
   		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String planOrderItemID = (String) dto.get("plan_order_item_id");
        String type = (String) dto.get("type");//根据工作单类型生成不同前缀
        String customer_id = (String)dto.get("customer_id");
        String supplier_contract_type = (String) dto.get("supplier_contract_type");
        String customer_contract_type = (String) dto.get("customer_contract_type");
        
        JobOrder jobOrder = new JobOrder();
        
        //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		String export_date = (String)dto.get("order_export_date");
        String newDateStr = "";
        String newDateStrMM = "";
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");//分析日期
        SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
        SimpleDateFormat sdfMM = new SimpleDateFormat("MM");//转换后的格式
        try {
            Date date=parseFormat.parse(export_date);
            newDateStr=sdf.format(date);
            newDateStrMM=sdfMM.format(date);
        } catch (ParseException ex) {
            logger.debug("处理工作单出货日期出错："+ex.getMessage());
        }
        logger.debug("工作单出货日期："+newDateStr);
        
        
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			jobOrder = JobOrder.dao.findById(id);
   			//版本(时间戳)校验，不对的话就不让更新保存
   			Timestamp page_update_stamp = Timestamp.valueOf(dto.get("update_stamp").toString());
   			Timestamp order_update_stamp = jobOrder.getTimestamp("update_stamp");
   			if(!order_update_stamp.equals(page_update_stamp)){
   			    Record rec = new Record();
   			    rec.set("err_code", "update_stamp_not_equal");
   			    rec.set("err_msg", "当前单据已被更改，请刷新页面获取最新数据，重新操作。");
   			    renderJson(rec);
   			    return;
   			}
   			
   			Date old_export_date=jobOrder.get("order_export_date");
   			String oldDateStr="";
   			if(old_export_date != null){
   			    oldDateStr=sdf.format(old_export_date);
   			}
   			logger.debug("工作单出货 旧日期："+oldDateStr);
   			String oldOrderNo=jobOrder.get("order_no");
   			String oldOrderNoDate = oldOrderNo.substring(3, 7);
   			logger.debug("工作单号 旧日期："+oldOrderNoDate);
   			if(( 
   			             StrKit.notBlank(oldDateStr) && 
   			             !newDateStr.equals(oldDateStr) && 
   			             !newDateStr.equals(oldOrderNoDate)
   			        ) 
   			  ){
   				oldOrderNo = OrderNoGenerator.getNextOrderNo("EKYZH", newDateStr, office_id);
   				StringBuilder sb = new StringBuilder(oldOrderNo);//构造一个StringBuilder对象
	   			sb.insert(5, generateJobPrefix(type));//在指定的位置1，插入指定的字符串
	   			oldOrderNo = sb.toString();
	   			jobOrder.set("order_no", oldOrderNo);
	   		}
   			if(!type.equals(jobOrder.get("type"))){
   				StringBuilder sb = new StringBuilder(oldOrderNo);//构造一个StringBuilder对象
				sb.replace(5, 6, generateJobPrefix(type));
				oldOrderNo =sb.toString();
				jobOrder.set("order_no", oldOrderNo);
   			}

   			DbUtils.setModelValues(dto, jobOrder);
   			jobOrder.set("updator", user.getLong("id"));
            jobOrder.set("update_stamp", new Date());
            
   			jobOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, jobOrder);
//   			String newOrder_on ="EKYZH"+generateJobPrefix(type);
   			if(office_id!=6){
   			//需后台处理的字段
   	   			String order_no = OrderNoGenerator.getNextOrderNo("EKYZH", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
   	   			sb.insert(5, generateJobPrefix(type));//在指定的位置1，插入指定的字符串
   	   			order_no = sb.toString();
   	            jobOrder.set("order_no", order_no);
   	   			jobOrder.set("creator", user.getLong("id"));
   	   			jobOrder.set("create_stamp", new Date());
   	   			jobOrder.set("updator", user.getLong("id"));
   	            jobOrder.set("update_stamp", new Date());
   	   			jobOrder.set("office_id", office_id);
   	   			jobOrder.save();
   	   			id = jobOrder.getLong("id").toString();
   	   			
   	   			//创建过工作单，设置plan_order_item的字段
   	   			PlanOrderItem planOrderItem = PlanOrderItem.dao.findById(planOrderItemID);
   	   			if(planOrderItem!=null){
   	                   planOrderItem.set("is_gen_job", "Y");
   	                   planOrderItem.update();
   	   			}
   			}
   			if(office_id==6){
   			//需后台处理的字段
   	   			String order_no = OrderNoGenerator.getNextOrderNo("KF", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
   	   			sb.insert(2, generateJobPrefix(type));//在指定的位置1，插入指定的字符串
   	   			sb.insert(5, newDateStrMM);//在指定的位置1，插入指定的字符串
   	   			order_no = sb.toString();
   	            jobOrder.set("order_no", order_no);
   	   			jobOrder.set("creator", user.getLong("id"));
   	   			jobOrder.set("create_stamp", new Date());
   	   			jobOrder.set("updator", user.getLong("id"));
   	            jobOrder.set("update_stamp", new Date());
   	   			jobOrder.set("office_id", office_id);
   	   			jobOrder.save();
   	   			id = jobOrder.getLong("id").toString();
   	   			
   	   			//创建过工作单，设置plan_order_item的字段
   	   			PlanOrderItem planOrderItem = PlanOrderItem.dao.findById(planOrderItemID);
   	   			if(planOrderItem!=null){
   	                   planOrderItem.set("is_gen_job", "Y");
   	                   planOrderItem.update();
   	   			}
   			}
   			
   		}
   		long customerId = Long.valueOf(dto.get("customer_id").toString());
   		//常用客户保存进入历史记录
   		saveCustomerQueryHistory(customerId);
		//海运
		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
		DbUtils.handleList(shipment_detail, id, JobOrderShipment.class, "order_id");
		//当hbl为空时，赋值为order_no
		JobOrderShipment jos=JobOrderShipment.dao.findFirst("select * from job_order_shipment where order_id="+id);
		if(jos!=null && StringUtils.isEmpty((String) jos.get("hbl_no"))){
			jos.set("hbl_no", jobOrder.get("order_no"));
			jos.update();
		}
		List<Map<String, String>> shipment_item = (ArrayList<Map<String, String>>)dto.get("shipment_list");
		DbUtils.handleList(shipment_item, id, JobOrderShipmentItem.class, "order_id");
		//空运
		List<Map<String, String>> air_detail = (ArrayList<Map<String, String>>)dto.get("air_detail");
		DbUtils.handleList(air_detail, id, JobOrderAir.class, "order_id");
		
		List<Map<String, String>> air_cargoDesc = (ArrayList<Map<String, String>>)dto.get("air_cargoDesc");
		DbUtils.handleList(air_cargoDesc, id, JobOrderAirCargoDesc.class, "order_id");
		
		List<Map<String, String>> air_item = (ArrayList<Map<String, String>>)dto.get("air_list");
		DbUtils.handleList(air_item, id, JobOrderAirItem.class, "order_id");
		
		//陆运
		List<Map<String, String>> land_item = (ArrayList<Map<String, String>>)dto.get("land_list");
		DbUtils.handleList(land_item, id, JobOrderLandItem.class, "order_id");
		List<Map<String, String>> land_shipment_item = (ArrayList<Map<String, String>>)dto.get("land_shipment_list");
		DbUtils.handleList(land_shipment_item, id, JobOrderLandItem.class, "order_id");
		
		//快递
		List<Map<String, String>> express_detail = (ArrayList<Map<String, String>>)dto.get("express_detail");
		DbUtils.handleList(express_detail, id, JobOrderExpress.class, "order_id");
		
		//报关
		List<Map<String, String>> chinaCustom = (ArrayList<Map<String, String>>)dto.get("chinaCustom");
		List<Map<String, String>> abroadCustom = (ArrayList<Map<String, String>>)dto.get("abroadCustom");
		List<Map<String, String>> hkCustom = (ArrayList<Map<String, String>>)dto.get("hkCustom");
		List<Map<String, String>> chinaCustom_self_item = (ArrayList<Map<String, String>>)dto.get("chinaCustom_self_item");
		DbUtils.handleList(chinaCustom, id, JobOrderCustom.class, "order_id");
		DbUtils.handleList(chinaCustom_self_item, "job_order_custom_china_self_item", id, "order_id");
		DbUtils.handleList(abroadCustom, id, JobOrderCustom.class, "order_id");
		DbUtils.handleList(hkCustom, id, JobOrderCustom.class, "order_id");
		
		//保险
		List<Map<String, String>> insurance_detail = (ArrayList<Map<String, String>>)dto.get("insurance_detail");
		DbUtils.handleList(insurance_detail, id, JobOrderInsurance.class, "order_id");
		
		//费用明细，应收应付
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, id, JobOrderArap.class, "order_id");
		List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
		DbUtils.handleList(chargeCost_list, id, JobOrderArap.class, "order_id");
		//记录结算公司使用历史	
		saveAccoutCompanyQueryHistory(charge_list);
		saveAccoutCompanyQueryHistory(chargeCost_list);
		//记录结算费用使用历史  
		saveFinItemQueryHistory(charge_list);
		saveFinItemQueryHistory(chargeCost_list);
		
		//相关文档
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, id, JobOrderDoc.class, "order_id");

		//贸易
		List<Map<String, String>> trade_detail = (ArrayList<Map<String, String>>)dto.get("trade_detail");
		DbUtils.handleList(trade_detail,"job_order_trade",id,"order_id");
		List<Map<String, String>> trade_cost_list = (ArrayList<Map<String, String>>)dto.get("trade_cost");
		DbUtils.handleList(trade_cost_list,"job_order_trade_cost",id,"order_id");
		
		Model<?> model = (Model<?>) JobOrderArap.class.newInstance();
		List<Map<String, String>> trade_service_list = (ArrayList<Map<String, String>>)dto.get("trade_service");
		
		List<Map<String, String>> trade_sale_list = (ArrayList<Map<String, String>>)dto.get("trade_sale");
		if(trade_service_list!=null){
	        for(int i=0;i<trade_service_list.size();i++){
	        	Map<String, String> map=trade_service_list.get(i);
	        	DbUtils.setModelValues(map,model);
	        	model.set("order_id", id);
	        	model.set("type", "贸易");
	        	model.set("order_type", "charge");
	        	model.set("trade_fee_flag", "trade_service_fee");
	        	if("UPDATE".equals(map.get("action"))){
	        		model.update();
	        	}else if("DELETE".equals(map.get("action"))){
	        		if(map.get("id")!=null)
	        		model.delete();
	        	}else{
	        		model.save();
	        	}
	        }
		}
		if(trade_sale_list!=null){
	        for(int i=0;i<trade_sale_list.size();i++){
	        	Map<String, String> map=trade_sale_list.get(i);
	        	DbUtils.setModelValues(map,model);
	        	model.set("order_id", id);
	        	model.set("order_type", "charge");
	        	model.set("type", "贸易");
	        	model.set("trade_fee_flag", "trade_sale_fee");
	        	if("UPDATE".equals(map.get("action"))){
	        		model.update();
	        	}else if("DELETE".equals(map.get("action"))){
	        		if(map.get("id")!=null)
	        		model.delete();
	        	}else{
	        		model.save();
	        	}
	        }
		}

		long creator = jobOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = jobOrder.toRecord();
//		System.out.println(new java.sql.Timestamp(r.getDate("update_stamp").getTime()));
		r.set("update_stamp", new java.sql.Timestamp(r.getDate("update_stamp").getTime()));
   		r.set("creator_name", user_name);
   		r.set("custom",Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china"));
   		r.set("abroadCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"abroad"));
   		r.set("hkCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"HK/MAC"));
   		r.set("customSelf", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china_self"));
   		r.set("shipment", getItemDetail(id,"shipment"));
   		r.set("trade", getItemDetail(id,"trade"));
    	r.set("air", getItemDetail(id,"air"));
    	r.set("express", getItemDetail(id,"express"));
   		r.set("insurance", getItemDetail(id,"insure"));
   		
   		//保存海运填写模板
   		saveOceanTemplate(shipment_detail);
   		//保存空运填写模板
   		saveAirTemplate(air_detail);
   		
   		//海 陆 空运带出合同费用信息(供应商)
   		if(supplier_contract_type.indexOf("ocean")>-1){
   			if(type.indexOf("柜货")>-1){
   	   			saveJobOceanGHSpContractConditions(jobOrder);
   	   		}else if(type.indexOf("散货")>-1){
   	   			saveJobOceanSHSpContractConditions(jobOrder);
   	   		}
   		}
   		if(supplier_contract_type.indexOf("air")>-1){
   			saveJobAirSpContractConditions(jobOrder);
   		}
   		if(supplier_contract_type.indexOf("land")>-1){
   			saveJobLandSpContractConditions(jobOrder);
   		}
   		
   		//海 陆 空运带出合同费用信息(客户)
   		if(customer_contract_type.indexOf("ocean")>-1){
   			saveJobOceanCustomerContractConditions(jobOrder);
   		}
   		if(customer_contract_type.indexOf("air")>-1){
   			saveJobAirCustomerContractConditions(jobOrder);
   		}
   		if(customer_contract_type.indexOf("land")>-1){
   			saveJobLandCustomerContractConditions(jobOrder);
   		}

   		//空运带出合同费用信息(供应商)
   		saveJobAirSpContractConditions(jobOrder);
   		//陆运带出合同费用信息(供应商)
   		saveJobLandSpContractConditions(jobOrder);
   		//空运带出合同费用信息(客户)
   		saveJobAirCustomerContractConditions(jobOrder);
   		//陆运带出合同费用信息(客户)
   		saveJobLandCustomerContractConditions(jobOrder);
   		
   	    //费用明细，应收应付
		List<Map<String, String>> charge_template = (ArrayList<Map<String, String>>)dto.get("charge_template");
		List<Map<String, String>> cost_template = (ArrayList<Map<String, String>>)dto.get("cost_template");
		List<Map<String, String>> allCharge_template = (ArrayList<Map<String, String>>)dto.get("allCharge_template");
		List<Map<String, String>> allCost_template = (ArrayList<Map<String, String>>)dto.get("allCost_template");
   		saveArapTemplate(type,customer_id,charge_template,cost_template,allCharge_template,allCost_template);
     	//贸易信息，应收服务，销售应收模板
		List<Map<String, String>> chargeService_template = (ArrayList<Map<String, String>>)dto.get("chargeService_template");
		List<Map<String, String>> allChargeService_template = (ArrayList<Map<String, String>>)dto.get("allChargeService_template");
		saveTradeServiceTemplate(type,customer_id,chargeService_template,allChargeService_template);
		List<Map<String, String>> chargeSale_template = (ArrayList<Map<String, String>>)dto.get("chargeSale_template");
		List<Map<String, String>> allChargeSale_template = (ArrayList<Map<String, String>>)dto.get("allChargeSale_template");
   		saveTradeSaleTemplate(type,customer_id,chargeSale_template,allChargeSale_template);
   	   	   		
	
   		renderJson(r);
   	}
    
    /**
     * 客户合同条件
     * @param order
     */
    private void saveJobOceanCustomerContractConditions(JobOrder order){
    	String order_id =  order.get("id").toString();
    	String customer_id = order.get("customer_id").toString();//客户
    	String trans_clause = order.getStr("trans_clause");//运输条款
    	String trade_type = order.getStr("trade_type");//贸易类型
    	
    	String order_export_date = order.get("order_export_date").toString();// 出货日期 *
    	String type = order.getStr("type");//类型
    	
    	Record oseanRe = Db.findFirst("select * from job_order_shipment where order_id = ?",order_id);
    	String pol="";
    	String pod=""; 
    	if(oseanRe!=null){
    		if(oseanRe.get("pol") != null && oseanRe.get("pod")!= null){
    			pol = oseanRe.get("pol").toString();
           	 	pod = oseanRe.get("pod").toString();
    		}
    		 
    	}
    	
    	
    	List<Record> oseanItem = Db.find("SELECT count(1) count,container_type FROM `job_order_shipment_item`"
    			+ "  where order_id = ? GROUP BY container_type;",order_id);
    	
    	List jArray = new ArrayList();
    	for(Record re :oseanItem){
    		String container_type = re.getStr("container_type");
    		String count = re.get("count").toString();
    		
    		if(StringUtils.isNotBlank(container_type)){
    			//Map<String, String> map = new HashMap<String, String>();
    			Record map1 = new Record();
                map1.set("container_type", container_type.replaceAll("'", ""));
                map1.set("count", count);
                jArray.add(map1);
    		}
    	}
    	
    	Gson json = new Gson();
    	//JSONObject json=new JSONObject();  
    	Record map = new Record();
    	map.set("customer_id", customer_id);
    	map.set("trans_clause", trans_clause);
    	map.set("trade_type", trade_type);
    	//json.put("order_export_date", order_export_date);
    	map.set("type", type);
    	map.set("pol", pol);
    	map.set("pod", pod);
    	map.set("jArray", jArray);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='charge' and order_type='ocean'",order_id);
    	if(reJCC != null){
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='charge' and order_type='ocean' and ? between contract_begin_time and contract_end_time",order_id,order_export_date);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'charge' and type='海运' and audit_flag = 'Y' and cus_contract_flag = 'Y'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'charge' and type='海运' and cus_contract_flag = 'Y'",order_id);
        			getOceanCustomerContractMsg(order_id,jsonStr,jArray,order_export_date);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("order_id", order_id);
        	re.set("charge_type", "charge");
        	re.set("order_type", "ocean");
        	Db.save("job_contract_compare", re);
        	
        	getOceanCustomerContractMsg(order_id,jsonStr,jArray,order_export_date);
    	}
    }
    
    
    /**
     * 获取客户合同费用明细信息
     * @param order_no
     * @return
     * @throws JSONException 
     */
    private void getOceanCustomerContractMsg(String order_id,String jsonStr,List jArray,String order_export_date){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String customer_id =  dto.getStr("CUSTOMER_ID")==null?"":dto.getStr("CUSTOMER_ID");
    	String trans_clause =  dto.getStr("TRANS_CLAUSE")==null?"":dto.getStr("TRANS_CLAUSE");
    	String trade_type =  dto.getStr("TRADE_TYPE")==null?"":dto.getStr("TRADE_TYPE");
    	String type =  dto.getStr("TYPE")==null?"":dto.getStr("TYPE");
    	String pol =  dto.getStr("POL")==null?"":dto.getStr("POL");
    	String pod =  dto.getStr("POD")==null?"":dto.getStr("POD");

    	
    	String container_types = "''";
    	for (int i = 0; i < jArray.size(); i++) {
    		Record map=new Record();  
    		map = (Record) jArray.get(i);
    		String container_type = (String)map.get("container_type");
    		if(i==0){
    			container_types = "'"+container_type+"'";
    		}else{
    			container_types += ",'"+container_type+"'";
    		}
		}

    	String sql = "select cci.*,cc.contract_begin_time,cc.contract_end_time from customer_contract_location ccl"
    			+ " LEFT JOIN customer_contract cc on cc.id = ccl.contract_id"
    			+ " LEFT JOIN customer_contract_item cci on cci.customer_loc_id = ccl.id"
    			+ " where "
    			+ " ifnull(customer_id,'') = '"+customer_id+"' and ifnull(cc.type,'') = '"+type+"' and ('"+order_export_date+"' BETWEEN cc.contract_begin_time and cc.contract_end_time)"
    			+ " and ifnull(cc.trans_clause,'') = '"+trans_clause+"' and ifnull(cc.trade_type,'') = '"+trade_type+"'"
    			+ " and ifnull(ccl.pol_id,'') = '"+pol+"' and ifnull(ccl.pod_id,'') = '"+pod+"'"
    			+ " and (cci.container_type in ("+container_types+") or ifnull(cci.container_type,'')='')"
    			+ " and contract_type = 'ocean'"
    			+ "	and cc.office_id = "+office_id
    			+ " GROUP BY cci.id";
    	List<Record> itemList = Db.find(sql) ;
    	
    	Date begin_time = null;
    	Date end_time = null;
    	for(Record re : itemList ){
    		begin_time = re.getDate("contract_begin_time");
    		end_time = re.getDate("contract_end_time");
    		Double amount = 1.0;
    		String thsi_container_type = re.getStr("container_type");
    		for (int i = 0; i < jArray.size(); i++) {
    			Record map=new Record();  
        		map = (Record) jArray.get(i);
        		String json_container_type = (String)map.get("container_type");
        		String json_amount = (String)map.get("count");
        		if(json_container_type.equals(thsi_container_type)){
        			amount = Double.parseDouble(json_amount);
        		}
    		}
    		
    		JobOrderArap jarap = new JobOrderArap();
    		jarap.set("order_type", "charge");
    		jarap.set("type", "海运");
    		jarap.set("sp_id", customer_id);
    		jarap.set("charge_id", re.get("fee_id"));
    		jarap.set("charge_eng_id", re.get("fee_id"));
    		jarap.set("price", re.get("price"));
    		jarap.set("amount", amount);
    		jarap.set("unit_id", re.get("uom"));
    		Double total_amount = re.getDouble("price")*amount;
    		jarap.set("total_amount", total_amount);
    		jarap.set("currency_id", re.get("currency_id"));
    		//获取汇率
    		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
    		Double rate = 1.0;
    		if(crc != null){
    			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
    		}
    		jarap.set("exchange_rate", rate);
    		jarap.set("currency_total_amount", total_amount*rate);
    		jarap.set("exchange_currency_id", re.get("currency_id"));
    		
    		jarap.set("exchange_currency_rate_rmb", rate);
    		jarap.set("exchange_total_amount", total_amount);
    		jarap.set("exchange_currency_rate",1.0);
    		jarap.set("exchange_total_amount_rmb", total_amount*rate);
    		jarap.set("rmb_difference", 0.00);
    		jarap.set("order_id", order_id);
    		jarap.set("cus_contract_flag","Y");//标记位 
    		jarap.save();
    	}
    	
    	//更新合同时间到对比表，方便校验
    	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='charge' and order_type='ocean'",order_id);
    	if(jc != null){
    		jc.set("contract_begin_time",begin_time );
    		jc.set("contract_end_time", end_time);
    		Db.update("job_contract_compare", jc);
    	}
    }
    
    
    
    /**
     * 海运柜货
     * @param order
     */
    private void saveJobOceanGHSpContractConditions(JobOrder order){
    	String order_id =  order.get("id").toString();
    	String type = order.getStr("type");//类型
    	
    	Record oseanRe = Db.findFirst("select * from job_order_shipment where order_id = ?",order_id);
    	String pol = null;
    	String pod = null;
    	String por = null;
    	String hub = null;
    	String booking_agent = null;  //订舱代理
    	String sailing_date = null;
    	String carrier = null;//创公司
    	if(oseanRe!=null){
    		if(oseanRe.get("por") != null){
    			por = oseanRe.get("por").toString();
    		}
    		if(oseanRe.get("pol")!= null){
    			pol = oseanRe.get("pol").toString();
    		}
    		if(oseanRe.get("hub")!= null){
    			hub = oseanRe.get("hub").toString();
    		}
    		if(oseanRe.get("pod")!= null){
    			pod = oseanRe.get("pod").toString();
    		}
    		if(oseanRe.get("booking_agent")!= null){
    			booking_agent = oseanRe.get("booking_agent").toString();
    		}
    		if(oseanRe.getDate("sailing_date")!= null){
    			sailing_date = oseanRe.getDate("sailing_date").toString();
    		}
    		if(oseanRe.get("carrier")!= null){
    			carrier = oseanRe.get("carrier").toString();
    		}
    	}
    	
    	
    	List<Record> oseanItem = Db.find("SELECT count(1) count,container_type FROM `job_order_shipment_item`"
    			+ "  where order_id = ? GROUP BY container_type;",order_id);
    	
    	List jArray = new ArrayList();
    	for(Record re :oseanItem){
    		String container_type = re.getStr("container_type");
    		String count = re.get("count").toString();
    		
    		if(StringUtils.isNotBlank(container_type)){
    			Record map1 = new Record();
                map1.set("container_type", container_type.replaceAll("'", ""));
                map1.set("count", count);
                jArray.add(map1);
    		}
    	}
    	
    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("booking_agent", booking_agent);
    	map.set("pol", pol);
    	map.set("pod", pod);
    	map.set("por", por);
    	map.set("hub", hub);
    	map.set("sailing_date", sailing_date);
    	map.set("carrier", carrier);
    	map.set("jArray", jArray);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='cost' and order_type='ocean_gh'",order_id);
    	if(reJCC != null){
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='cost' and order_type='ocean_gh' and ? between contract_begin_time and contract_end_time",order_id,sailing_date);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'cost' and type = '海运' and audit_flag = 'Y' and cus_contract_flag = 'GH'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'cost' and type = '海运' and cus_contract_flag = 'GH'",order_id);
        			getOceanGHSpContractMsg(order_id,jsonStr,jArray,sailing_date);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "cost");
        	re.set("order_type", "ocean_gh");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getOceanGHSpContractMsg(order_id,jsonStr,jArray,sailing_date);
    	}
    }
    
    /**
     * 海运柜货
     * @param order_id
     * @param jsonStr
     * @param jArray
     * @param sailing_date
     */
    private void getOceanGHSpContractMsg(String order_id,String jsonStr,List jArray,String sailing_date){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String booking_agent =  dto.getStr("BOOKING_AGENT");//订舱代理
    	String pol =  dto.getStr("POL")==null?"":dto.getStr("POL");
    	String pod =  dto.getStr("POD")==null?"":dto.getStr("POD");
    	String por =  dto.getStr("POR")==null?"":dto.getStr("POR");
    	String hub =  dto.getStr("HUB")==null?"":dto.getStr("HUB");
    	String carrier =  dto.getStr("CARRIER")==null?"":dto.getStr("CARRIER");

    	String container_types = "''";
    	for (int i = 0; i < jArray.size(); i++) {
    		Record map=new Record();  
    		map = (Record) jArray.get(i);
    		String container_type = (String)map.get("container_type");
    		if(i==0){
    			container_types = "'"+container_type+"'";
    		}else{
    			container_types += ",'"+container_type+"'";
    		}
		}

    	String sql = "select sci.*,sc.contract_begin_time,sc.contract_end_time from supplier_contract_location scl"
    			+ " LEFT JOIN supplier_contract sc on sc.id = scl.contract_id"
    			+ " LEFT JOIN supplier_contract_item sci on sci.supplier_loc_id = scl.id"
    			+ " where "
    			+ " ifnull(sc.customer_id,'') = '"+booking_agent+"'  and ('"+sailing_date+"' BETWEEN sc.contract_begin_time and sc.contract_end_time)"
    			+ " and ifnull(scl.pol_id,'"+pol+"') = '"+pol+"' and ifnull(scl.pod_id,'') = '"+pod+"'"
    			+ " and ifnull(scl.por_id,'') = '"+por+"' and ifnull(scl.hub_id,'') = '"+hub+"' and  ifnull(scl.carrier_id,'') = '"+carrier+"'"
    			+ " and (ifnull(sci.container_type,'') in ("+container_types+") "
    			+ " or (ifnull(sci.container_type,'')='' and ifnull(sci.gross_weight1,'')='' and ifnull(sci.gross_weight2,'')=''"
    			+ " and ifnull(sci.volume1,'')='' and ifnull(sci.volume2,'')=''))"
    			+ " and contract_type = 'ocean'"
    			+ "	and sc.office_id = "+office_id
    			+ " GROUP BY sci.id";
    	List<Record> itemList = Db.find(sql) ;
    	
    	Date begin_time = null;
    	Date end_time = null;
    	for(Record re : itemList ){
    		begin_time = re.getDate("contract_begin_time");
    		end_time = re.getDate("contract_end_time");
    		Double amount = 1.0;
    		String thsi_container_type = re.getStr("container_type");
    		for (int i = 0; i < jArray.size(); i++) {
    			Record map=new Record();  
        		map = (Record) jArray.get(i);
        		String json_container_type = (String)map.get("container_type");
        		String json_amount = (String)map.get("count");
        		if(json_container_type.equals(thsi_container_type)){
        			amount = Double.parseDouble(json_amount);
        		}
    		}
    		
    		JobOrderArap jarap = new JobOrderArap();
    		jarap.set("order_type", "cost");
    		jarap.set("type", "海运");
    		jarap.set("sp_id", booking_agent);
    		jarap.set("charge_id", re.get("fee_id"));
    		jarap.set("charge_eng_id", re.get("fee_id"));
    		jarap.set("price", re.get("price"));
    		jarap.set("amount", amount);
    		jarap.set("unit_id", re.get("uom"));
    		Double total_amount = re.getDouble("price")*amount;
    		jarap.set("total_amount", total_amount);
    		jarap.set("currency_id", re.get("currency_id"));
    		//获取汇率
    		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
    		Double rate = 1.0;
    		if(crc != null){
    			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
    		}
    		jarap.set("exchange_rate", rate);
    		jarap.set("currency_total_amount", total_amount*rate);
    		jarap.set("exchange_currency_id", re.get("currency_id"));
    		
    		jarap.set("exchange_currency_rate_rmb", rate);
    		jarap.set("exchange_total_amount", total_amount);
    		jarap.set("exchange_currency_rate",1.0);
    		jarap.set("exchange_total_amount_rmb", total_amount*rate);
    		jarap.set("rmb_difference", 0.00);
    		jarap.set("order_id", order_id);
    		jarap.set("cus_contract_flag","GH");//标记位 
    		jarap.save();
    	}
    	
    	//更新合同时间到对比表，方便校验
    	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'cost' and order_type='ocean_gh'",order_id);
    	if(jc != null){
    		jc.set("contract_begin_time",begin_time );
    		jc.set("contract_end_time", end_time);
    		Db.update("job_contract_compare", jc);
    	}
    }
    
    
    /**
     * 海运散货
     * @param order
     */
    private void saveJobOceanSHSpContractConditions(JobOrder order){
    	String order_id =  order.get("id").toString();
    	String type = order.getStr("TYPE");//类型
    	String billing_method =  order.getStr("BILLING_METHOD");//计费方式
    	String fee_count =  order.getStr("FEE_COUNT")==null?"1":order.getStr("FEE_COUNT");//计费数量
    	
    	Record oseanRe = Db.findFirst("select * from job_order_shipment where order_id = ?",order_id);
    	String pol = null;
    	String pod = null;
    	String por = null;
    	String hub = null;
    	String booking_agent = null;  //订舱代理
    	String sailing_date = null;
    	String carrier = null;//创公司
    	
    	if(oseanRe!=null){
    		if(oseanRe.get("por") != null){
    			por = oseanRe.get("por").toString();
    		}
    		if(oseanRe.get("pol")!= null){
    			pol = oseanRe.get("pol").toString();
    		}
    		if(oseanRe.get("hub")!= null){
    			hub = oseanRe.get("hub").toString();
    		}
    		if(oseanRe.get("pod")!= null){
    			pod = oseanRe.get("pod").toString();
    		}
    		if(oseanRe.get("booking_agent")!= null){
    			booking_agent = oseanRe.get("booking_agent").toString();
    		}
    		if(oseanRe.getDate("sailing_date")!= null){
    			sailing_date = oseanRe.getDate("sailing_date").toString();
    		}
    		if(oseanRe.get("carrier")!= null){
    			carrier = oseanRe.get("carrier").toString();
    		}
    	}
    	

    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("booking_agent", booking_agent);
    	map.set("pol", pol);
    	map.set("pod", pod);
    	map.set("por", por);
    	map.set("hub", hub);
    	map.set("sailing_date", sailing_date);
    	map.set("carrier", carrier);
    	map.set("billing_method", billing_method);
    	map.set("fee_count", fee_count);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='cost' and order_type='ocean_sh'",order_id);
    	if(reJCC != null){
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='cost' and order_type='ocean_sh' and ? between contract_begin_time and contract_end_time",order_id,sailing_date);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'cost' and type = '海运' and audit_flag = 'Y' and cus_contract_flag = 'SH'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'cost' and type = '海运' and cus_contract_flag = 'SH'",order_id);
        			getOceanSHSpContractMsg(order_id,jsonStr,sailing_date);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "cost");
        	re.set("order_type", "ocean_sh");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getOceanSHSpContractMsg(order_id,jsonStr,sailing_date);
    	}
    }
    
  
    /**
     * 海运散货
     * @param order_id
     * @param jsonStr
     * @param sailing_date
     */
    private void getOceanSHSpContractMsg(String order_id,String jsonStr,String sailing_date){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String booking_agent =  dto.getStr("BOOKING_AGENT");//订舱代理
    	String pol =  dto.getStr("POL")==null?"":dto.getStr("POL");
    	String pod =  dto.getStr("POD")==null?"":dto.getStr("POD");
    	String por =  dto.getStr("POR")==null?"":dto.getStr("POR");
    	String hub =  dto.getStr("HUB")==null?"":dto.getStr("HUB");
    	String carrier =  dto.getStr("CARRIER")==null?"":dto.getStr("CARRIER");
    	String fee_count =  dto.getStr("FEE_COUNT")==null?"1":dto.getStr("FEE_COUNT");
    	String billing_method =  dto.getStr("BILLING_METHOD")==null?"":dto.getStr("BILLING_METHOD");
    	
    	String billing_method_condition = "";
    	if(billing_method.equals("perWeight")){
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(sci.gross_weight1,0)"
    				+ " and ifnull("+fee_count+",-1) <= ifnull(sci.gross_weight2,10000) "
    				+ " and (ifnull(sci.gross_weight1,'') !='' or ifnull(sci.gross_weight2,'') !=''))";
    	}else{
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(sci.volume1,0) "
    				+ " and ifnull("+fee_count+",-1) <= ifnull(sci.volume2,10000)"
    				+ " and (ifnull(sci.volume1,'') !='' or ifnull(sci.volume2,'') !=''))";
    	}


    	String sql = "select sci.*,sc.contract_begin_time,sc.contract_end_time,"
    			+ " if(ifnull(sci.container_type,'')='' and ifnull(sci.gross_weight1,'')='' and ifnull(sci.gross_weight2,'')=''"
    			+ " and ifnull(sci.volume1,'')='' and ifnull(sci.volume2,'')='','Y','N') isNull"
    			+ " from supplier_contract_location scl"
    			+ " LEFT JOIN supplier_contract sc on sc.id = scl.contract_id"
    			+ " LEFT JOIN supplier_contract_item sci on sci.supplier_loc_id = scl.id"
    			+ " where "
    			+ " sc.customer_id = '"+booking_agent+"' "
    			+ " and ('"+sailing_date+"' BETWEEN sc.contract_begin_time and sc.contract_end_time)"
    			+ " and ifnull(scl.pol_id,'') = '"+pol+"' and ifnull(scl.pod_id,'') = '"+pod+"'"
    			+ " and ifnull(scl.por_id,'') = '"+por+"' and ifnull(scl.hub_id,'') = '"+hub+"' and  ifnull(scl.carrier_id,'') = '"+carrier+"'"
    			+ " and ("+billing_method_condition
    			+ " or (ifnull(sci.container_type,'')='' and ifnull(sci.gross_weight1,'')='' and ifnull(sci.gross_weight2,'')=''"
    			+ " and ifnull(sci.volume1,'')='' and ifnull(sci.volume2,'')=''))"
    			+ " and contract_type = 'ocean'"
    			+ "	and sc.office_id = "+office_id
    			+ " GROUP BY sci.id";
    	List<Record> itemList = Db.find(sql) ;
    	
    	Date begin_time = null;
    	Date end_time = null;
    	for(Record re : itemList ){
    		begin_time = re.getDate("contract_begin_time");
    		end_time = re.getDate("contract_end_time");
    		Double amount = 1.0;
    		
    		if ("N".equals(re.getStr("isNull"))) {
    			amount = Double.parseDouble(fee_count);
    		}
    		
    		JobOrderArap jarap = new JobOrderArap();
    		jarap.set("order_type", "cost");
    		jarap.set("type", "海运");
    		jarap.set("sp_id", booking_agent);
    		jarap.set("charge_id", re.get("fee_id"));
    		jarap.set("charge_eng_id", re.get("fee_id"));
    		jarap.set("price", re.get("price"));
    		jarap.set("amount", amount);
    		jarap.set("unit_id", re.get("uom"));
    		Double total_amount = re.getDouble("price")*amount;
    		jarap.set("total_amount", total_amount);
    		jarap.set("currency_id", re.get("currency_id"));
    		//获取汇率
    		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
    		Double rate = 1.0;
    		if(crc != null){
    			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
    		}
    		jarap.set("exchange_rate", rate);
    		jarap.set("currency_total_amount", total_amount*rate);
    		jarap.set("exchange_currency_id", re.get("currency_id"));
    		
    		jarap.set("exchange_currency_rate_rmb", rate);
    		jarap.set("exchange_total_amount", total_amount);
    		jarap.set("exchange_currency_rate",1.0);
    		jarap.set("exchange_total_amount_rmb", total_amount*rate);
    		jarap.set("rmb_difference", 0.00);
    		jarap.set("order_id", order_id);
    		jarap.set("cus_contract_flag","SH");//标记位 
    		jarap.save();
    	}
    	
    	//更新合同时间到对比表，方便校验
    	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'cost' and order_type='ocean'",order_id);
    	if(jc != null){
    		jc.set("contract_begin_time",begin_time );
    		jc.set("contract_end_time", end_time);
    		Db.update("job_contract_compare", jc);
    	}
    }
    
    
    
    
    /**
     * 保存空运工作单-供应商合同条件
     * @param order
     */
    private void saveJobAirSpContractConditions(JobOrder order){
    	String order_id =  order.get("ID").toString();
    	String type = order.getStr("TYPE");//类型
    	String billing_method =  order.getStr("BILLING_METHOD");//计费方式
    	String fee_count =  order.getStr("FEE_COUNT")==null?"1":order.getStr("FEE_COUNT");//计费数量
    	
    	Record orderRe = Db.findFirst("select * from job_order_air where order_id = ?",order_id);
    	String booking_agent = null;  //订舱代理
    	if(orderRe!=null){
    		if(orderRe.get("booking_agent")!= null){
    			booking_agent = orderRe.get("booking_agent").toString();//船舱代理
    		}
    	}
    	
    	List<Record> itemReList = Db.find("select * from job_order_air_item where order_id = ? order by id",order_id);
    	String air_company = null;  //航空公司
    	String start_from = null;//起始地
    	String destination = null; //目的地
    	String etd = null;  //对应合同有效期
    	if(itemReList.size()>0){
    		if(itemReList.size()>1){
    			if(itemReList.get(0).get("air_company") != null){
    				air_company = itemReList.get(0).get("air_company").toString();
    			}
    			if(itemReList.get(0).get("start_from") != null){
    				start_from = itemReList.get(0).get("start_from").toString();
    			}
    			if(itemReList.get(0).get("etd") != null){
    				etd = itemReList.get(0).get("etd").toString();
    			}
    			if(itemReList.get(itemReList.size()-1).get("destination") != null){
    				destination = itemReList.get(itemReList.size()-1).get("destination").toString();//拿最后一条的地址
    			}
    			
    		}else{
    			if(itemReList.get(0).get("air_company") != null){
    				air_company = itemReList.get(0).get("air_company").toString();
    			}
    			if(itemReList.get(0).get("start_from") != null){
    				start_from = itemReList.get(0).get("start_from").toString();
    			}
    			if(itemReList.get(0).get("etd") != null){
    				etd = itemReList.get(0).get("etd").toString();
    			}
    			if(itemReList.get(0).get("destination") != null){
    				destination = itemReList.get(0).get("destination").toString();
    			}
    			
    		}
    	}
    	

    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("booking_agent", booking_agent);
    	map.set("air_company", air_company);
    	map.set("start_from", start_from);
    	map.set("destination", destination);
    	map.set("etd",etd);
    	map.set("billing_method", billing_method);
    	map.set("fee_count", fee_count);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='cost' and order_type='air'",order_id);
    	if(reJCC != null){ 
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='cost' and order_type='air' and ? between contract_begin_time and contract_end_time",order_id,etd);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'cost' and type = '空运' and audit_flag = 'Y' and cus_contract_flag = 'Y'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'cost' and type = '空运' and cus_contract_flag = 'Y'",order_id);
        			getAirSpContractMsg(order_id,jsonStr);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "cost");
        	re.set("order_type", "air");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getAirSpContractMsg(order_id,jsonStr);
    	}
    }
    
  
    /**
     * 空运供应商合同
     * @param order_id
     * @param jsonStr
     * @param atd
     */
    private void getAirSpContractMsg(String order_id,String jsonStr){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String booking_agent =  dto.getStr("BOOKING_AGENT")==null?"":dto.getStr("BOOKING_AGENT");//订舱代理
    	String air_company =  dto.getStr("AIR_COMPANY")==null?"":dto.getStr("AIR_COMPANY");
    	String start_from =  dto.getStr("START_FROM")==null?"":dto.getStr("START_FROM");
    	String destination =  dto.getStr("DESTINATION")==null?"":dto.getStr("DESTINATION");
    	String etd =  dto.getStr("ETD")==null?"":dto.getStr("ETD");
    	String fee_count =  dto.getStr("FEE_COUNT")==null?"":dto.getStr("FEE_COUNT");
    	String billing_method =  dto.getStr("BILLING_METHOD")==null?"":dto.getStr("BILLING_METHOD");
    	
    	String billing_method_condition = "";
    	if(billing_method.equals("perWeight")){
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(sci.gross_weight1,0)"
    				+ " and ifnull("+fee_count+",-1) <= ifnull(sci.gross_weight2,10000) "
    				+ " and (ifnull(sci.gross_weight1,'') !='' or ifnull(sci.gross_weight2,'') !=''))";
    	}else{
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(sci.volume1,0) "
    				+ " and ifnull("+fee_count+",-1) <= ifnull(sci.volume2,10000)"
    				+ " and (ifnull(sci.volume1,'') !='' or ifnull(sci.volume2,'') !=''))";
    	}
    	String sql = "select sci.*,sc.contract_begin_time,sc.contract_end_time,"
    			+ " if(ifnull(sci.container_type,'')='' and ifnull(sci.gross_weight1,'')='' and ifnull(sci.gross_weight2,'')=''"
    			+ " and ifnull(sci.volume1,'')='' and ifnull(sci.volume2,'')='','Y','N') isNull"
    			+ " from supplier_contract_location scl"
    			+ " LEFT JOIN supplier_contract sc on sc.id = scl.contract_id"
    			+ " LEFT JOIN supplier_contract_item sci on sci.supplier_loc_id = scl.id"
    			+ " where "
    			+ " ifnull(sc.customer_id,'') = '"+booking_agent+"' "
    			+ " and ('"+etd+"' BETWEEN sc.contract_begin_time and sc.contract_end_time)"
    			+ " and ifnull(scl.pol_id,'') = '"+start_from+"' and ifnull(scl.pod_id,'') = '"+destination+"'"
    			+ " and  ifnull(scl.air_company,'') = '"+air_company+"'"
    			+ " and ("+billing_method_condition
    			+ " or (ifnull(sci.container_type,'')='' and ifnull(sci.gross_weight1,'')='' and ifnull(sci.gross_weight2,'')=''"
    			+ " and ifnull(sci.volume1,'')='' and ifnull(sci.volume2,'')=''))"
    			+ " AND contract_type = 'air'"
    			+ "	and sc.office_id = "+office_id
    			+ " GROUP BY sci.id";
    	List<Record> itemList = Db.find(sql) ;
    	
    	Date begin_time = null;
    	Date end_time = null;
    	for(Record re : itemList ){
    		begin_time = re.getDate("contract_begin_time");
    		end_time = re.getDate("contract_end_time");
    		Double amount = 1.0;
    		
    		if ("N".equals(re.getStr("isNull"))) {
    			amount = Double.parseDouble(fee_count);
    		}
    		
    		JobOrderArap jarap = new JobOrderArap();
    		Double price = re.getDouble("price") == null?0.0:re.getDouble("price");
    		if(price < 0){
    			price = price * -1;
    			jarap.set("order_type", "charge");
    		}else{
    			jarap.set("order_type", "cost");
    		}
    		jarap.set("type", "空运");
    		jarap.set("sp_id", booking_agent);
    		jarap.set("charge_id", re.get("fee_id"));
    		jarap.set("charge_eng_id", re.get("fee_id"));
    		jarap.set("price", price);
    		jarap.set("amount", amount);
    		jarap.set("unit_id", re.get("uom"));
    		Double total_amount = price*amount;
    		jarap.set("total_amount", total_amount);
    		jarap.set("currency_id", re.get("currency_id"));
    		//获取汇率
    		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
    		Double rate = 1.0;
    		if(crc != null){
    			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
    		}
    		jarap.set("exchange_rate", rate);
    		jarap.set("currency_total_amount", total_amount*rate);
    		jarap.set("exchange_currency_id", re.get("currency_id"));
    		
    		jarap.set("exchange_currency_rate_rmb", rate);
    		jarap.set("exchange_total_amount", total_amount);
    		jarap.set("exchange_currency_rate",1.0);
    		jarap.set("exchange_total_amount_rmb", total_amount*rate);
    		jarap.set("rmb_difference", 0.00);
    		jarap.set("order_id", order_id);
    		jarap.set("cus_contract_flag","Y");//标记位 
    		jarap.save();
    	}
    	
    	//更新合同时间到对比表，方便校验
    	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'cost' and order_type='air'",order_id);
    	if(jc != null){
    		jc.set("contract_begin_time",begin_time );
    		jc.set("contract_end_time", end_time);
    		Db.update("job_contract_compare", jc);
    	}
    }
    
    
    
    /**
     * 保存空运工作单-客户合同条件
     * @param order
     */
    private void saveJobAirCustomerContractConditions(JobOrder order){
    	String order_id =  order.get("ID").toString();
    	String customer_id = order.get("CUSTOMER_ID").toString();//客户
    	String type = order.getStr("TYPE");//类型
    	String billing_method =  order.getStr("BILLING_METHOD");//计费方式
    	String fee_count =  order.getStr("FEE_COUNT")==null?"1":order.getStr("FEE_COUNT");//计费数量
    	String trans_clause = order.getStr("TRANS_CLAUSE");//运输条款
    	String trade_type = order.getStr("TRADE_TYPE");//贸易类型
    	
    	List<Record> itemReList = Db.find("select * from job_order_air_item where order_id = ? order by id",order_id);
    	String start_from = null;//起始地
    	String destination = null; //目的地
    	String etd = null;  //对应合同有效期
    	if(itemReList.size()>0){
    		if(itemReList.size()>1){
    			if(itemReList.get(0).get("start_from") != null){
    				start_from = itemReList.get(0).get("start_from").toString();
    			}
    			if(itemReList.get(0).get("etd") != null){
    				etd = itemReList.get(0).get("etd").toString();
    			}
    			if(itemReList.get(itemReList.size()-1).get("destination") != null){
    				destination = itemReList.get(itemReList.size()-1).get("destination").toString();//拿最后一条的地址
    			}
    			
    		}else{
    			if(itemReList.get(0).get("start_from") != null){
    				start_from = itemReList.get(0).get("start_from").toString();
    			}
    			if(itemReList.get(0).get("etd") != null){
    				etd = itemReList.get(0).get("etd").toString();
    			}
    			if(itemReList.get(0).get("destination") != null){
    				destination = itemReList.get(0).get("destination").toString();
    			}
    			
    		}
    	}
    	

    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("customer_id", customer_id);
    	map.set("type", type);
    	map.set("trans_clause", trans_clause);
    	map.set("trade_type", trade_type);
    	map.set("start_from", start_from);
    	map.set("destination", destination);
    	map.set("etd",etd);
    	map.set("billing_method", billing_method);
    	map.set("fee_count", fee_count);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='charge' and order_type='air'",order_id);
    	if(reJCC != null){ 
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='charge' and order_type='air' and ? between contract_begin_time and contract_end_time",order_id,etd);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'charge' and type = '空运' and audit_flag = 'Y' and cus_contract_flag = 'Y'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'charge' and type = '空运' and cus_contract_flag = 'Y'",order_id);
        			getAirCustomerContractMsg(order_id,jsonStr);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "charge");
        	re.set("order_type", "air");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getAirCustomerContractMsg(order_id,jsonStr);
    	}
    }
    
  
    /**
     * 空运客户合同
     * @param order_id
     * @param jsonStr
     * @param atd
     */
    private void getAirCustomerContractMsg(String order_id,String jsonStr){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
        String customer_id =  dto.getStr("CUSTOMER_ID");
    	String type =  dto.getStr("TYPE");
    	String trans_clause =  dto.getStr("TRANS_CLAUSE")==null?"":dto.getStr("TRANS_CLAUSE");
    	String trade_type =  dto.getStr("TRADE_TYPE")==null?"":dto.getStr("TRADE_TYPE");
    	String start_from =  dto.getStr("START_FROM")==null?"":dto.getStr("START_FROM");
    	String destination =  dto.getStr("DESTINATION")==null?"":dto.getStr("DESTINATION");
    	String etd =  dto.getStr("ETD")==null?"":dto.getStr("ETD");
    	String fee_count =  dto.getStr("FEE_COUNT")==null?"":dto.getStr("FEE_COUNT");
    	String billing_method =  dto.getStr("BILLING_METHOD")==null?"":dto.getStr("BILLING_METHOD");

    	String billing_method_condition = "";
    	if(billing_method.equals("perWeight")){
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(cci.gross_weight1,0)"
    				+ " and ifnull("+fee_count+",-1) <= ifnull(cci.gross_weight2,10000) "
    				+ " and (ifnull(cci.gross_weight1,'') !='' or ifnull(cci.gross_weight2,'') !=''))";
    	}else{
    		billing_method_condition = " (ifnull("+fee_count+",-1) >= ifnull(cci.volume1,0) "
    				+ " and ifnull("+fee_count+",-1) <= ifnull(cci.volume2,10000)"
    				+ " and (ifnull(cci.volume1,'') !='' or ifnull(cci.volume2,'') !=''))";
    	}

    	String sql = "select cci.*,cc.contract_begin_time,cc.contract_end_time,"
    			+ " if(ifnull(cci.container_type,'')='' and ifnull(cci.gross_weight1,'')='' and ifnull(cci.gross_weight2,'')=''"
    			+ " and ifnull(cci.volume1,'')='' and ifnull(cci.volume2,'')='','Y','N') isNull"
    			+ " from customer_contract_location ccl"
    			+ " LEFT JOIN customer_contract cc on cc.id = ccl.contract_id"
    			//+ " LEFT JOIN customer_contract_item cci on cci.customer_loc_id = ccl.id"
    			+ " LEFT JOIN customer_contract_item cci on cci.contract_id = cc.id"
    			+ " where "
    			+ " ifnull(cc.customer_id,'') = '"+customer_id+"' and ifnull(cc.type,'') = '"+type+"'"
    			+ " and ifnull(cc.trans_clause,'') = '"+trans_clause+"' and ifnull(cc.trade_type,'') = '"+trade_type+"'"
    			+ " and ('"+etd+"' BETWEEN cc.contract_begin_time and cc.contract_end_time)"
    			+ " and ifnull(ccl.pol_id,'') = '"+start_from+"' and ifnull(ccl.pod_id,'') = '"+destination+"'"
    			+ " and ("+billing_method_condition
    			+ " or (ifnull(cci.container_type,'')='' and ifnull(cci.gross_weight1,'')='' and ifnull(cci.gross_weight2,'')=''"
    			+ " and ifnull(cci.volume1,'')='' and ifnull(cci.volume2,'')=''))"
    			+ " AND contract_type = 'air'"
    			+ "	and cc.office_id = "+office_id
    			+ " GROUP BY cci.id";
    	
    	List<Record> itemList = Db.find(sql) ;
    	
    	Date begin_time = null;
    	Date end_time = null;
    	for(Record re : itemList ){
    		begin_time = re.getDate("contract_begin_time");
    		end_time = re.getDate("contract_end_time");
    		Double amount = 1.0;
    		
    		if ("N".equals(re.getStr("isNull"))) {
    			amount = Double.parseDouble(fee_count);
    		}

    		JobOrderArap jarap = new JobOrderArap();
    		Double price = re.getDouble("price") == null?0.0:re.getDouble("price");
    		if(price < 0){
    			price = price * -1;
    			jarap.set("order_type", "cost");
    		}else{
    			jarap.set("order_type", "charge");
    		}
    		jarap.set("sp_id", customer_id);
    		jarap.set("type", "空运");;
    		jarap.set("charge_id", re.get("fee_id"));
    		jarap.set("charge_eng_id", re.get("fee_id"));
    		jarap.set("price", price);
    		jarap.set("amount", amount);
    		jarap.set("unit_id", re.get("uom"));
    		Double total_amount = price*amount;
    		jarap.set("total_amount", total_amount);
    		jarap.set("currency_id", re.get("currency_id"));
    		//获取汇率
    		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
    		Double rate = 1.0;
    		if(crc != null){
    			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
    		}
    		jarap.set("exchange_rate", rate);
    		jarap.set("currency_total_amount", total_amount*rate);
    		jarap.set("exchange_currency_id", re.get("currency_id"));
    		
    		jarap.set("exchange_currency_rate_rmb", rate);
    		jarap.set("exchange_total_amount", total_amount);
    		jarap.set("exchange_currency_rate",1.0);
    		jarap.set("exchange_total_amount_rmb", total_amount*rate);
    		jarap.set("rmb_difference", 0.00);
    		jarap.set("order_id", order_id);
    		jarap.set("cus_contract_flag","Y");//标记位 
    		jarap.save();
    	}
    	
    	//更新合同时间到对比表，方便校验
    	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'charge' and order_type='air'",order_id);
    	if(jc != null){
    		jc.set("contract_begin_time",begin_time );
    		jc.set("contract_end_time", end_time);
    		Db.update("job_contract_compare", jc);
    	}
    }
    
    
    
    /**
     * 保存陆运工作单-供应商合同条件
     * @param order
     */
    private void saveJobLandSpContractConditions(JobOrder order){
    	String order_id =  order.get("ID").toString();
    	String type = order.getStr("TYPE");//类型
    	String order_export_date = order.get("order_export_date").toString();//出货日期
    	
    	List<Record> itemReList = Db.find("select * from job_order_land_item where order_id = ? order by id",order_id);
    	List jArray = new ArrayList();
    	for(Record re :itemReList){
    		String transport_company = null;  //运输公司
    		String truck_type = null;         //车型
    		String take_address = null;       //发货地址
    		String delivery_address = null;   //收货地点
    		if(re.get("transport_company") != null){
    			transport_company = re.get("transport_company").toString();
    		}
    		if(re.get("truck_type") != null){
    			truck_type = re.get("truck_type").toString();
    		}
    		if(re.get("take_address") != null){
    			take_address = re.get("take_address").toString();
    		}
    		if(re.get("delivery_address") != null){
    			delivery_address = re.get("delivery_address").toString();
    		}
    		
    		Record map = new Record();
            map.set("transport_company", transport_company);
            map.set("truck_type", truck_type);
            map.set("take_address", take_address);
            map.set("delivery_address", delivery_address);
            jArray.add(map);
    	}
    	
    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("order_export_date", order_export_date);
    	map.set("jArray", jArray);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='cost' and order_type='land'",order_id);
    	if(reJCC != null){ 
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='cost' and order_type='land' and ? between contract_begin_time and contract_end_time",order_id,order_export_date);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'cost' and audit_flag = 'Y' and cus_contract_flag = 'Y' and type = '陆运'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'cost' and cus_contract_flag = 'Y' and type = '陆运'",order_id);
        			getLandSpContractMsg(order_id,jsonStr,jArray);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "cost");
        	re.set("order_type", "land");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getLandSpContractMsg(order_id,jsonStr,jArray);
    	}
    }
    
  
    /**
     * 陆运供应商合同
     * @param order_id
     * @param jsonStr
     * @param atd
     */
    private void getLandSpContractMsg(String order_id,String jsonStr,List jArray){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String order_export_date =  dto.getStr("ORDER_EXPORT_DATE");
    	
    	String container_types = "''";
    	for (int i = 0; i < jArray.size(); i++) {
    		Record map=new Record();  
    		map = (Record) jArray.get(i);
    		String transport_company = (String)map.get("TRANSPORT_COMPANY")==null?"":dto.getStr("TRANSPORT_COMPANY");
    		String truck_type = (String)map.get("TRUCK_TYPE")==null?"":dto.getStr("TRUCK_TYPE");
    		String take_address = (String)map.get("TAKE_ADDRESS")==null?"":dto.getStr("TAKE_ADDRESS");
    		String delivery_address = (String)map.get("DELIVERY_ADDRESS")==null?"":dto.getStr("DELIVERY_ADDRESS");
    		
    		String sql = "select sci.*,sc.contract_begin_time,sc.contract_end_time"
        			+ " from supplier_contract_location scl"
        			+ " LEFT JOIN dockinfo dc_f on dc_f.id = scl.pol_id"
        			+ " LEFT JOIN dockinfo dc_t on dc_t.id = scl.pod_id"
        			+ " LEFT JOIN supplier_contract sc on sc.id = scl.contract_id"
        			+ " LEFT JOIN supplier_contract_item sci on sci.supplier_loc_id = scl.id"
        			+ " where "
        			+ " ifnull(sc.customer_id,'') = '"+transport_company+"' "
        			+ " and ('"+order_export_date+"' BETWEEN sc.contract_begin_time and sc.contract_end_time)"
        			+ " and ((ifnull(dc_f.dock_name,'') = '"+take_address+"' and ifnull(dc_t.dock_name,'') = '"+delivery_address+"') "
        			+ " or"
        			+ " (ifnull(dc_f.dock_name,'') = '"+delivery_address+"' and ifnull(dc_t.dock_name,'') = '"+take_address+"'))"
        			+ " and (ifnull(sci.truck_type,'') = '"+truck_type+"'"
        			+ " or ifnull(sci.truck_type,'') ='')"
        			+ " AND contract_type = 'land'"
        			+ "	and sc.office_id = "+office_id
        			+ " GROUP BY sci.id";
        	List<Record> itemList = Db.find(sql) ;
        	
        	Date begin_time = null;
        	Date end_time = null;
        	for(Record re : itemList ){
        		begin_time = re.getDate("contract_begin_time");
        		end_time = re.getDate("contract_end_time");
        		Double amount = 1.0;

        		JobOrderArap jarap = new JobOrderArap();
        		Double price = re.getDouble("price") == null?0.0:re.getDouble("price");
        		if(price < 0){
        			price = price * -1;
        			jarap.set("order_type", "charge");
        		}else{
        			jarap.set("order_type", "cost");
        		}
        		jarap.set("type", "陆运");
        		jarap.set("sp_id", transport_company);
        		jarap.set("charge_id", re.get("fee_id"));
        		jarap.set("charge_eng_id", re.get("fee_id"));
        		jarap.set("price", price);
        		jarap.set("amount", amount);
        		jarap.set("unit_id", re.get("uom"));
        		Double total_amount = price*amount;
        		jarap.set("total_amount", total_amount);
        		jarap.set("currency_id", re.get("currency_id"));
        		//获取汇率
        		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
        		Double rate = 1.0;
        		if(crc != null){
        			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
        		}
        		jarap.set("exchange_rate", rate);
        		jarap.set("currency_total_amount", total_amount*rate);
        		jarap.set("exchange_currency_id", re.get("currency_id"));
        		
        		jarap.set("exchange_currency_rate_rmb", rate);
        		jarap.set("exchange_total_amount", total_amount);
        		jarap.set("exchange_currency_rate",1.0);
        		jarap.set("exchange_total_amount_rmb", total_amount*rate);
        		jarap.set("rmb_difference", 0.00);
        		jarap.set("order_id", order_id);
        		jarap.set("cus_contract_flag","Y");//标记位 
        		jarap.save();
        	}
        	
        	//更新合同时间到对比表，方便校验
        	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'cost' and order_type='land'",order_id);
        	if(jc != null){
        		jc.set("contract_begin_time",begin_time );
        		jc.set("contract_end_time", end_time);
        		Db.update("job_contract_compare", jc);
        	}
		}
    }
    
    
    /**
     * 保存陆运工作单-客户合同条件
     * @param order
     */
    private void saveJobLandCustomerContractConditions(JobOrder order){
    	String order_id =  order.get("ID").toString();
    	String type = order.getStr("TYPE");//类型
    	String customer_id = order.getStr("CUSTOMER_ID");//类型
    	String order_export_date = order.get("ORDER_EXPORT_DATE").toString();//出货日期
    	
    	List<Record> itemReList = Db.find("select * from job_order_land_item where order_id = ? order by id",order_id);
    	List jArray = new ArrayList();
    	for(Record re :itemReList){
    		String truck_type = null;         //车型
    		String take_address = null;       //发货地址
    		String delivery_address = null;   //收货地点
    		if(re.get("truck_type") != null){
    			truck_type = re.get("truck_type").toString();
    		}
    		if(re.get("take_address") != null){
    			take_address = re.get("take_address").toString();
    		}
    		if(re.get("delivery_address") != null){
    			delivery_address = re.get("delivery_address").toString();
    		}
    		
    		Record map = new Record();
            map.set("truck_type", truck_type);
            map.set("take_address", take_address);
            map.set("delivery_address", delivery_address);
            jArray.add(map);
    	}
    	
    	Gson json = new Gson();
    	Record map = new Record();
    	map.set("customer_id", customer_id);
    	map.set("order_export_date", order_export_date);
    	map.set("jArray", jArray);
    	
    	String jsonStr = json.toJson(map);
    	Record reJCC = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type='charge' and order_type='land'",order_id);
    	if(reJCC != null){ 
    		Record re = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type ='charge' and order_type='land' and ? between contract_begin_time and contract_end_time",order_id,order_export_date);
    		
    		String reConditions = reJCC.getStr("conditions");
    		if(!jsonStr.equals(reConditions) || re == null){
    			//不同更新
    			reJCC.set("conditions", jsonStr);
    			Db.update("job_contract_compare", reJCC);
    			
    			//校验是否带过来的合同费用是否已确认
    			Record reArap = Db.findFirst("select * from job_order_arap "
    					+ " where order_id = ? and order_type = 'charge' and audit_flag = 'Y' and cus_contract_flag = 'Y' and type = '陆运'",order_id);
    			if(reArap == null){
    				//先删除原来的再把最新的合同费用明细带过去
        			Db.update("delete from `job_order_arap` where order_id = ? and order_type = 'charge' and cus_contract_flag = 'Y' and type = '陆运'",order_id);
        			getLandCustomerContractMsg(order_id,jsonStr,jArray);
    			}else{
    				//已确认后无法更新费用明细
    			}
    		}
    	}else{
    		Record re = new Record();
        	re.set("conditions", jsonStr);
        	re.set("charge_type", "charge");
        	re.set("order_type", "land");
        	re.set("order_id", order_id);
        	Db.save("job_contract_compare", re);
        	
        	getLandCustomerContractMsg(order_id,jsonStr,jArray);
    	}
    }
    
  
    /**
     * 陆运供应商合同
     * @param order_id
     * @param jsonStr
     * @param atd
     */
    private void getLandCustomerContractMsg(String order_id,String jsonStr,List jArray){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        Gson gson = new Gson();
        Record dto= gson.fromJson(jsonStr, Record.class);   
    	String order_export_date =  dto.getStr("ORDER_EXPORT_DATE");
    	String customer_id =  dto.getStr("CUSTOMER_ID");
    	
    	for (int i = 0; i < jArray.size(); i++) {
    		Record map=new Record();  
    		map = (Record) jArray.get(i);
    		String truck_type = (String)map.get("TRUCK_TYPE")==null?"":dto.getStr("TRUCK_TYPE");
    		String take_address = (String)map.get("TAKE_ADDRESS")==null?"":dto.getStr("TAKE_ADDRESS");
    		String delivery_address = (String)map.get("DELIVERY_ADDRESS")==null?"":dto.getStr("DELIVERY_ADDRESS");
    		
    		String sql = "select sci.*,sc.contract_begin_time,sc.contract_end_time"
        			+ " from customer_contract_location scl"
        			+ " LEFT JOIN dockinfo dc_f on dc_f.id = scl.pol_id"
        			+ " LEFT JOIN dockinfo dc_t on dc_t.id = scl.pod_id"
        			+ " LEFT JOIN customer_contract sc on sc.id = scl.contract_id"
        			//+ " LEFT JOIN supplier_contract_item sci on sci.supplier_loc_id = scl.id"
        			+ " LEFT JOIN customer_contract_item sci on sci.contract_id = sc.id"
        			+ " where "
        			+ " ifnull(sc.customer_id,'') = '"+customer_id+"' "
        			+ " and ('"+order_export_date+"' BETWEEN sc.contract_begin_time and sc.contract_end_time)"
        			+ " and ((ifnull(dc_f.dock_name,'') = '"+take_address+"' and ifnull(dc_t.dock_name,'') = '"+delivery_address+"') "
        			+ " or"
        			+ " (ifnull(dc_f.dock_name,'') = '"+delivery_address+"' and ifnull(dc_t.dock_name,'') = '"+take_address+"'))"
        			+ " and (ifnull(sci.truck_type,'') = '"+truck_type+"'"
        			+ " or ifnull(sci.truck_type,'') ='')"
        			+ " AND contract_type = 'land'"
        			+ "	and sc.office_id = "+office_id
        			+ " GROUP BY sci.id";
        	List<Record> itemList = Db.find(sql) ;
        	
        	Date begin_time = null;
        	Date end_time = null;
        	for(Record re : itemList ){
        		begin_time = re.getDate("contract_begin_time");
        		end_time = re.getDate("contract_end_time");
        		Double amount = 1.0;

        		JobOrderArap jarap = new JobOrderArap();
        		Double price = re.getDouble("price") == null?0.0:re.getDouble("price");
        		if(price < 0){
        			price = price * -1;
        			jarap.set("order_type", "cost");
        		}else{
        			jarap.set("order_type", "charge");
        		}
        		jarap.set("type", "陆运");
        		jarap.set("sp_id", customer_id);
        		jarap.set("charge_id", re.get("fee_id"));
        		jarap.set("charge_eng_id", re.get("fee_id"));
        		jarap.set("price", price);
        		jarap.set("amount", amount);
        		jarap.set("unit_id", re.get("uom"));
        		Double total_amount = price*amount;
        		jarap.set("total_amount", total_amount);
        		jarap.set("currency_id", re.get("currency_id"));
        		//获取汇率
        		Record crc = Db.findFirst("select * from currency_rate where currency_id = ? and office_id = ?",re.get("currency_id"),office_id);
        		Double rate = 1.0;
        		if(crc != null){
        			rate = crc.getDouble("rate")==null?1.0:crc.getDouble("rate");
        		}
        		jarap.set("exchange_rate", rate);
        		jarap.set("currency_total_amount", total_amount*rate);
        		jarap.set("exchange_currency_id", re.get("currency_id"));
        		
        		jarap.set("exchange_currency_rate_rmb", rate);
        		jarap.set("exchange_total_amount", total_amount);
        		jarap.set("exchange_currency_rate",1.0);
        		jarap.set("exchange_total_amount_rmb", total_amount*rate);
        		jarap.set("rmb_difference", 0.00);
        		jarap.set("order_id", order_id);
        		jarap.set("cus_contract_flag","Y");//标记位 
        		jarap.save();
        	}
        	
        	//更新合同时间到对比表，方便校验
        	Record jc = Db.findFirst("select * from job_contract_compare where order_id = ? and charge_type = 'charge' and order_type='land'",order_id);
        	if(jc != null){
        		jc.set("contract_begin_time",begin_time );
        		jc.set("contract_end_time", end_time);
        		Db.update("job_contract_compare", jc);
        	}
		}
    }
    
    
    private StringBuilder StringBuilder(String order_no) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * 保存费用模板
     * @param shipment_detail
     */
    public void saveArapTemplate(String order_type,String customer_id,
    		List<Map<String, String>> charge_list,List<Map<String, String>> cost_list,
    		List<Map<String, String>> charge_list_all,List<Map<String, String>> cost_list_all){
        if((charge_list==null||charge_list.size()<=0) && (cost_list==null||cost_list.size()<=0) )
            return;

        Gson gson = new Gson();
        String chargeObject = gson.toJson(charge_list);
        String costObject = gson.toJson(cost_list);
        String chargeObjectAll = gson.toJson(charge_list_all);
        String costObjectAll = gson.toJson(cost_list_all);
        
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String chargeSql = "select parent_id from job_order_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
    	String costSql = "select parent_id from job_order_arap_template where"
                + " arap_type = 'cost' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+costObject+"' and parent_id is not null ";

        Record chargeRec = Db.findFirst(chargeSql);
        Record costRec = Db.findFirst(costSql);

        if(chargeRec == null){
        	if(!(charge_list==null||charge_list.size()<=0)){
        		//保存全部信息
                Record all= new Record();
                all.set("creator_id", creator_id);
                all.set("customer_id", customer_id);
                all.set("arap_type", "charge");
                all.set("order_type", order_type);
                all.set("json_value", chargeObjectAll);          
                Db.save("job_order_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update job_order_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
        }
        
        if(costRec == null){
        	if(!(cost_list==null||cost_list.size()<=0)){
        		//保存全部信息
                Record all = new Record();
                all.set("creator_id", creator_id);
                all.set("customer_id", customer_id);
                all.set("arap_type", "cost");
                all.set("order_type", order_type);
                all.set("json_value", costObjectAll);
                Db.save("job_order_arap_template", all);  
                
        		//保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "cost");
                r.set("order_type", order_type);
                r.set("json_value", costObject);
                r.set("parent_id",  all.getLong("id"));
                Db.save("job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = costRec.getLong("parent_id");
        	Db.update("update job_order_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
        }
    }
    
    /**
     * 保存费用模板
     * @param shipment_detail
     */
    public void saveLandArapTemplate(String order_type,String customer_id,
    		List<Map<String, String>> charge_list,List<Map<String, String>> cost_list,
    		List<Map<String, String>> charge_list_all,List<Map<String, String>> cost_list_all){
        if((charge_list==null||charge_list.size()<=0) && (cost_list==null||cost_list.size()<=0) )
            return;

        Gson gson = new Gson();
        String chargeObject = gson.toJson(charge_list);
//        String costObject = gson.toJson(cost_list);
        String chargeObjectAll = gson.toJson(charge_list_all);
//        String costObjectAll = gson.toJson(cost_list_all);
        
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String chargeSql = "select parent_id from job_order_land_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
//    	String costSql = "select parent_id from job_order_land_arap_template where"
//                + " arap_type = 'cost' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
//                + " and  json_value = '"+costObject+"' and parent_id is not null ";

        Record chargeRec = Db.findFirst(chargeSql);
//        Record costRec = Db.findFirst(costSql);

        if(chargeRec == null){
        	if(!(charge_list==null||charge_list.size()<=0)){
        		//保存全部信息
                Record all= new Record();
                all.set("creator_id", creator_id);
                all.set("customer_id", customer_id);
                all.set("arap_type", "charge");
                all.set("order_type", order_type);
                all.set("json_value", chargeObjectAll);          
                Db.save("job_order_land_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("job_order_land_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update job_order_land_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
        }
        
//        if(costRec == null){
//        	if(!(cost_list==null||cost_list.size()<=0)){
//        		//保存全部信息
//                Record all = new Record();
//                all.set("creator_id", creator_id);
//                all.set("customer_id", customer_id);
//                all.set("arap_type", "cost");
//                all.set("order_type", order_type);
//                all.set("json_value", costObjectAll);
//                Db.save("job_order_land_arap_template", all);  
//                
//        		//保存局部信息
//        		Record r= new Record();
//                r.set("creator_id", creator_id);
//                r.set("customer_id", customer_id);
//                r.set("arap_type", "cost");
//                r.set("order_type", order_type);
//                r.set("json_value", costObject);
//                r.set("parent_id",  all.getLong("id"));
//                Db.save("job_order_land_arap_template", r);  
//       		}
//        }else{
//        	Long parent_id = costRec.getLong("parent_id");
//        	Db.update("update job_order_land_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
//        }
    }
    /**
     * 保存费用模板
     * @param shipment_detail
     */
    public void saveTradeServiceTemplate(String order_type,String customer_id,
    		List<Map<String, String>> charge_list,List<Map<String, String>> charge_list_all){
        if((charge_list==null||charge_list.size()<=0) )
            return;

        Gson gson = new Gson();
        String chargeObject = gson.toJson(charge_list);
        String chargeObjectAll = gson.toJson(charge_list_all);
        
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String chargeSql = "select parent_id from job_order_trade_service_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
        Record chargeRec = Db.findFirst(chargeSql);

        if(chargeRec == null){
        	if(!(charge_list==null||charge_list.size()<=0)){
        		//保存全部信息
                Record all= new Record();
                all.set("creator_id", creator_id);
                all.set("customer_id", customer_id);
                all.set("arap_type", "charge");
                all.set("order_type", order_type);
                all.set("json_value", chargeObjectAll);          
                Db.save("job_order_trade_service_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("job_order_trade_service_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update job_order_trade_service_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
        }
        
    }
    //常用贸易
    /**
     * 保存费用模板
     * @param shipment_detail
     */
    public void saveTradeSaleTemplate(String order_type,String customer_id,
    		List<Map<String, String>> charge_list, List<Map<String, String>> charge_list_all){
        if((charge_list==null||charge_list.size()<=0) )
            return;

        Gson gson = new Gson();
        String chargeObject = gson.toJson(charge_list);
        String chargeObjectAll = gson.toJson(charge_list_all);
        
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String chargeSql = "select parent_id from job_order_trade_sale_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";

        Record chargeRec = Db.findFirst(chargeSql);

        if(chargeRec == null){
        	if(!(charge_list==null||charge_list.size()<=0)){
        		//保存全部信息
                Record all= new Record();
                all.set("creator_id", creator_id);
                all.set("customer_id", customer_id);
                all.set("arap_type", "charge");
                all.set("order_type", order_type);
                all.set("json_value", chargeObjectAll);          
                Db.save("job_order_trade_sale_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("job_order_trade_sale_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update job_order_trade_sale_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
        }
    }
    
    //保存常用邮箱模版
    public void saveEmailTemplate(){
    	String email = getPara("email");
    	String ccEmail = getPara("ccEmail");
    	String bccEmail = getPara("bccEmail");
    	String remark = getPara("remark");
    	String regex = "\\s+|,|，|;|；";//以空格或 ， ,；;分割
    	
    	//验证邮箱合法性
    	String[] arr = email.split(regex);
    	String reg = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    	for(int i=0;i<arr.length;i++){
    		if(!arr[i].matches(reg)){
    			renderJson("{\"result\":\"添加失败，收件人含有不合法邮箱\"}");
    			return;
    		}
    	}
    	if(StringUtils.isNotEmpty(ccEmail)){
	    	String[] arr1 = ccEmail.split(regex);
	    	for(int i=0;i<arr1.length;i++){
	    		if(!arr1[i].matches(reg)){
	    			renderJson("{\"result\":\"添加失败，抄送人含有不合法邮箱\"}");
	    			return;
	    		}
	    	}
    	}
    	if(StringUtils.isNotEmpty(bccEmail)){
	    	String[] arr2 = bccEmail.split(regex);
	    	for(int i=0;i<arr2.length;i++){
	    		if(!arr2[i].matches(reg)){
	    			renderJson("{\"result\":\"添加失败，密送人含有不合法邮箱\"}");
	    			return;
	    		}
	    	}
    	}
    	JobOrderSendMailTemplate order = new JobOrderSendMailTemplate();
    	order.set("receive_mail", email);
    	order.set("cc_mail", ccEmail);
    	order.set("bcc_mail", bccEmail);
    	order.set("remark", remark);
    	order.set("creator", LoginUserController.getLoginUserId(this));
    	order.save();
    	renderJson("{\"result\":true}");
    }
    
    //保存海运填写模板
    public void saveOceanTemplate(List<Map<String, String>> shipment_detail){
        if(shipment_detail==null||shipment_detail.size()<=0)
            return;
        
        Map<String, String> recMap=shipment_detail.get(0);
    	
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	String MBLshipper = recMap.get("MBLshipper");
    	String MBLconsignee = recMap.get("MBLconsignee");
    	String MBLnotify_party = recMap.get("MBLnotify_party");
    	String HBLshipper = recMap.get("HBLshipper");
    	String HBLconsignee = recMap.get("HBLconsignee");
    	String HBLnotify_party = recMap.get("HBLnotify_party");
    	String por = recMap.get("por");
    	String pol = recMap.get("pol");
    	String pod = recMap.get("pod");
    	String fnd = recMap.get("fnd");
    	String booking_agent = recMap.get("booking_agent");
    	String carrier = recMap.get("carrier");
    	String head_carrier = recMap.get("head_carrier");
    	String oversea_agent = recMap.get("oversea_agent");
    	String release_type = recMap.get("release_type");
    	String cargo_desc = recMap.get("cargo_desc");
    	String shipping_mark = recMap.get("shipping_mark");
        
        if(por!=null&&!"".equals(por)){
        	 savePortQueryHistory(por);
        }
        if(pol!=null&&!"".equals(pol)){
        	 savePortQueryHistory(pol);
        }
        if(pod!=null&&!"".equals(pod)){
        	 savePortQueryHistory(pod);
        }
        if(fnd!=null&&!"".equals(fnd)){
        	 savePortQueryHistory(fnd);
        }
        String content = MBLshipper+MBLconsignee+MBLnotify_party+HBLshipper+HBLconsignee+HBLnotify_party+por+pol+pod+fnd+booking_agent+carrier+head_carrier+oversea_agent;
        if("".equals(content)){
        	return;
        }
        
        String sql = "select 1 from job_order_ocean_template where"
                + " creator_id = "+creator_id;
        if(StringUtils.isNotEmpty(MBLshipper)){
        	sql+=" and MBLshipper='"+MBLshipper+"'";
        }
        if(StringUtils.isNotEmpty(MBLconsignee)){
        	sql+=" and MBLconsignee= '"+MBLconsignee+"'";
        }
        if(StringUtils.isNotEmpty(MBLnotify_party)){
        	sql+=" and MBLnotify_party= '"+MBLnotify_party+"'";
        }
        if(StringUtils.isNotEmpty(HBLshipper)){
        	sql+=" and HBLshipper= '"+HBLshipper+"'";
        }
        if(StringUtils.isNotEmpty(HBLconsignee)){
        	sql+=" and HBLconsignee= '"+HBLconsignee+"'";
        }
        if(StringUtils.isNotEmpty(HBLnotify_party)){
        	sql+=" and HBLnotify_party= '"+HBLnotify_party+"'";
        }
        if(StringUtils.isNotEmpty(por)){
        	sql+=" and por="+por;
        }
        if(StringUtils.isNotEmpty(pol)){
        	sql+=" and pol="+pol;
        }
        if(StringUtils.isNotEmpty(pod)){
        	sql+=" and pod="+pod;
        }
        if(StringUtils.isNotEmpty(fnd)){
        	sql+=" and fnd="+fnd;
        }
        if(StringUtils.isNotEmpty(booking_agent)){
        	sql+=" and booking_agent="+booking_agent;
        }
        if(StringUtils.isNotEmpty(carrier)){
        	sql+=" and carrier="+carrier;
        }
        if(StringUtils.isNotEmpty(head_carrier)){
        	sql+=" and head_carrier="+head_carrier;
        }
        if(StringUtils.isNotEmpty(oversea_agent)){
        	sql+=" and oversea_agent="+oversea_agent;
        }
        if(StringUtils.isNotEmpty(release_type)){
        	sql+=" and release_type='"+release_type+"'";
        }
        if(StringUtils.isNotEmpty(cargo_desc)){
        	sql+=" and cargo_desc='"+cargo_desc+"'";
        }
        if(StringUtils.isNotEmpty(shipping_mark)){
        	sql+=" and shipping_mark='"+shipping_mark+"'";
        }
      
        Record checkRec = Db.findFirst(sql);
        if(checkRec==null){
            Record r= new Record();
            r.set("creator_id", creator_id);
            r.set("MBLshipper", MBLshipper);
            r.set("MBLconsignee", MBLconsignee);
            r.set("MBLnotify_party", MBLnotify_party);
            r.set("HBLshipper", HBLshipper);
            r.set("HBLconsignee", HBLconsignee);
            r.set("HBLnotify_party", HBLnotify_party);
            r.set("por", por);
            r.set("pol", pol);
            r.set("pod", pod);
            r.set("fnd", fnd);
            r.set("booking_agent", booking_agent);
            r.set("carrier", carrier);
            r.set("head_carrier", head_carrier);
            r.set("oversea_agent", oversea_agent);
            r.set("release_type", release_type);
            r.set("cargo_desc", cargo_desc);
            r.set("shipping_mark", shipping_mark);
            Db.save("job_order_ocean_template", r);
        }
    }
    
    private void savePortQueryHistory(String portId){
        Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from user_query_history where type='port' and ref_id=? and user_id=?", portId, userId);
        if(rec==null){
            rec = new Record();
            rec.set("ref_id", portId);
            rec.set("type", "port");
            rec.set("user_id", userId);
            rec.set("query_stamp", new Date());
            Db.save("user_query_history", rec);
        }else{
            rec.set("query_stamp", new Date());
            Db.update("user_query_history", rec);
        }
    }
    
    //记录费用使用历史
    private void saveFinItemQueryHistory(List<Map<String, String>> list) throws InstantiationException, IllegalAccessException{
        Long userId = LoginUserController.getLoginUserId(this);
        
        for (Map<String, String> rowMap : list) {//获取每一行
            String accComId = rowMap.get("CHARGE_ID");
            if(StringUtils.isNotEmpty(accComId)){
                addHistoryRecord(userId, accComId, "ARAP_FIN");
            }
        }
    }
    
    //记录结算公司使用历史
    private void saveAccoutCompanyQueryHistory(List<Map<String, String>> list) throws InstantiationException, IllegalAccessException{
        Long userId = LoginUserController.getLoginUserId(this);
        
        for (Map<String, String> rowMap : list) {//获取每一行
            String accComId = rowMap.get("SP_ID");
            if(StringUtils.isNotEmpty(accComId)){
                addHistoryRecord(userId, accComId, "ARAP_COM");
            }
        }
    }

    private void addHistoryRecord(long userId, String partyId, String type) {
        Record rec = Db.findFirst("select * from user_query_history where type='"+type+"' and ref_id=? and user_id=?", partyId, userId);
        if(rec==null){
            rec = new Record();
            rec.set("ref_id", partyId);
            rec.set("type", type);
            rec.set("user_id", userId);
            rec.set("query_stamp", new Date());
            Db.save("user_query_history", rec);
        }else{
            rec.set("query_stamp", new Date());
            Db.update("user_query_history", rec);
        }
    }
    //常用客户保存进入历史记录
    private void saveCustomerQueryHistory(long customerId){
        Long userId = LoginUserController.getLoginUserId(this);
        Record rec = Db.findFirst("select * from user_query_history where type='CUSTOMER' and ref_id=? and user_id=?", customerId, userId);
        if(rec==null){
            rec = new Record();
            rec.set("ref_id", customerId);
            rec.set("type", "CUSTOMER");
            rec.set("user_id", userId);
            rec.set("query_stamp", new Date());
            Db.save("user_query_history", rec);
        }else{
            rec.set("query_stamp", new Date());
            Db.update("user_query_history", rec);
        }
    }
    //保存空运填写模板
    public void saveAirTemplate(List<Map<String, String>> detail){
    	if(detail==null||detail.size()<=0)
    		return;
    	
    	Map<String, String> recMap=detail.get(0);
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String shipper = recMap.get("shipper");
    	String consignee = recMap.get("consignee");
    	String notify_party = recMap.get("notify_party");
    	String booking_agent = recMap.get("booking_agent");
    	String goods_mark = recMap.get("goods_mark");
    	String shipping_mark = recMap.get("shipping_mark");
    	
    	String content = shipper+consignee+notify_party+booking_agent+shipping_mark+goods_mark;
        if("".equals(content)){
        	return;
        }
    	String sql = "select 1 from job_order_air_template where"
                + " creator_id = "+creator_id;
        if(StringUtils.isNotEmpty(shipper)){
        	sql+=" and shipper= '"+shipper+"'";
        }
        if(StringUtils.isNotEmpty(consignee)){
        	sql+=" and consignee= '"+consignee+"'";
        }
        if(StringUtils.isNotEmpty(notify_party)){
        	sql+=" and notify_party= '"+notify_party+"'";
        }
        if(StringUtils.isNotEmpty(booking_agent)){
        	sql+=" and booking_agent= '"+booking_agent+"'";
        }
        if(StringUtils.isNotEmpty(goods_mark)){
        	sql+=" and goods_mark= '"+goods_mark+"'";
        }
        if(StringUtils.isNotEmpty(shipping_mark)){
        	sql+=" and shipping_mark= '"+shipping_mark+"'";
        }
    	Record checkRec = Db.findFirst(sql);
    	if(checkRec==null){
    		Record r= new Record();
    		r.set("creator_id", creator_id);
    		r.set("shipper", shipper);
    		r.set("consignee", consignee);
    		r.set("notify_party", notify_party);
    		r.set("booking_agent", booking_agent);
    		r.set("shipping_mark", shipping_mark);
    		r.set("goods_mark", goods_mark);
    		Db.save("job_order_air_template", r);
    	}
    }
    
    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile() throws Exception{
    	try {
            String order_id = getPara("order_id");
            List<UploadFile> fileList = getFiles("doc");
            Long userId = LoginUserController.getLoginUserId(this);
            String type= getPara("type");
            
            uploadFile(fileList, order_id, userId,type, "job_order_doc", false);
            
            renderJson("{\"result\":true}");
        } catch (Exception e) {
            String msg = e.getMessage();
            Record rec = new Record();
            rec.set("result", false);
            if(msg.indexOf("Posted content")>0){
                rec.set("errMsg", "文件不能大于10M.");
            }else{
                rec.set("errMsg", msg);
            }
    	    renderJson(rec);
        }
    }
    
    public void uploadFile(List<UploadFile> fileList, 
	        String orderId,
	        Long userId, String type,
	        String tableName, boolean isLand) throws Exception {
	    for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i).getFile();
            //file.length()/1024/1024
            if(FileUploadUtil.getFileSize(file)>10){
                throw new Exception("文件不能超过10M.");
            }
            String fileName = file.getName();
            
            Record r = new Record();
            r.set("type", type);
            r.set("order_id", orderId);
            r.set("uploader", userId);
            r.set("doc_name", fileName);
            r.set("upload_time", new Date());
            Db.save(tableName, r);
        }
		
	}
    
    
    
    
    
    
    //报关的文档上传
    @Before(Tx.class)
    public void uploadCustomDoc() throws Exception{
        try {
            String order_id = getPara("order_id");
            List<UploadFile> fileList = getFiles("doc");
            Long userId = LoginUserController.getLoginUserId(this);
            
//            FileUploadUtil.uploadFile(fileList, order_id, userId, "job_order_custom_doc", false);
            UserLogin userLogin=LoginUserController.getLoginUser(this);
			String reString="select * from office where id="+userLogin.getOfficeId();
			Record record=Db.findFirst(reString);
            FileUploadUtil.uploadTypeFile(fileList, order_id, userId, "job_order_custom_doc", false,record.get("type").toString());
            
            
            renderJson("{\"result\":true}");
        } catch (Exception e) {
            String msg = e.getMessage();
            Record rec = new Record();
            rec.set("result", false);
            if(msg.indexOf("Posted content")>0){
                rec.set("errMsg", "文件不能大于10M.");
            }else{
                rec.set("errMsg", msg);
            }
            renderJson(rec);
        }
    }
    
    //上传陆运签收文件描述
    @Before(Tx.class)
    public void uploadSignDesc() throws Exception{
        try {
            String id = getPara("id");
            List<UploadFile> fileList = getFiles("doc");
            Long userId = LoginUserController.getLoginUserId(this);
            
            FileUploadUtil.uploadFile(fileList, id, userId, "job_order_land_doc", false);
            
            renderJson("{\"result\":true}");
        } catch (Exception e) {
            String msg = e.getMessage();
            Record rec = new Record();
            rec.set("result", false);
            if(msg.indexOf("Posted content")>0){
                rec.set("errMsg", "文件不能大于10M.");
            }else{
                rec.set("errMsg", msg);
            }
            renderJson(rec);
        }
    }
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	JobOrderDoc jobOrderDoc = JobOrderDoc.dao.findById(id);
    	String fileName = jobOrderDoc.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            jobOrderDoc.delete();
            resultMap.put("result", result);
        }else{
        	jobOrderDoc.delete();
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    //删除报关文档
    @Before(Tx.class)
    public void deleteCustomDoc(){
    	String id = getPara("id");
    	Record r = Db.findById("job_order_custom_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
    	File file = new File(filePath);
    	if (file.exists() && file.isFile()) {
    		boolean result = file.delete();
    		Db.delete("job_order_custom_doc",r);
    		resultMap.put("result", result);
    	}else{
    		Db.delete("job_order_custom_doc", r);
    		resultMap.put("result", "文件不存在可能已被删除!");
    	}
    	renderJson(resultMap);
    }
    
    //删除陆运签收文件
    @Before(Tx.class)
    public void deleteSignDesc(){
    	String id = getPara("id");
    	String path = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\";
    	
    	String sql = "select GROUP_CONCAT(doc_name) doc_name from job_order_land_doc where land_id=?";
    	Record r = Db.findFirst(sql, id);
    	String fileName = r.getStr("doc_name");
    	String[] arr = fileName.split(",");
    	for (int i = 0; i < arr.length; i++) {
	    	File file = new File(path+arr[i]);
	    	if (file.exists() && file.isFile()) {
	    		file.delete();
	    		Db.update("delete from job_order_land_doc where land_id=?", id);
	    	}else{
	    		Db.update("delete from job_order_land_doc where land_id=?", id);
	    	}
    	}
    	renderJson("{\"result\":true}");
    }
    //删除一个陆运签收文件
    @Before(Tx.class)
    public void deleteOneSignDesc(){
    	String id = getPara("id");
    	String name = getPara("name");
    	String path = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\";
    	File file = new File(path+name);
		if (file.exists() && file.isFile()) {
			file.delete();
			Db.update("delete from job_order_land_doc where id = ?", id);
		}else{
			Db.update("delete from job_order_land_doc where id = ?", id);
		}
    	renderJson("{\"result\":true}");
    }

    //返回对象	
    private Record getItemDetail(String id,String type){
    	Record re = null;
    	if("shipment".equals(type)){
    		re = Db.findFirst("select jos.*, p1.abbr MBLshipperAbbr , p2.abbr MBLconsigneeAbbr, p3.abbr MBLnotify_partyAbbr, "
    				+ " p8.abbr HBLshipperAbbr , p9.abbr HBLconsigneeAbbr, p10.abbr HBLnotify_partyAbbr,p4.abbr carrier_name,"
    				+ " p5.abbr head_carrier_name,p6.abbr oversea_agent_name,p7.abbr booking_agent_name,"
    				+ " CONCAT(lo.name,' -', lo.code) por_name, "
    				+ " CONCAT(lo1.name,' -', lo1.code) pol_name, "
    				+ " CONCAT(lo2.name,' -', lo2.code) pod_name, "
    				+ " CONCAT(lo3.name,' -', lo3.code) fnd_name,"
    				+ " CONCAT(lo4.name,' -', lo4.code) hub_name"
    				+ " from job_order_shipment jos "
    				+ " left join party p1 on p1.id=jos.MBLshipper"
    				+ " left join party p2 on p2.id=jos.MBLconsignee"
    				+ " left join party p3 on p3.id=jos.MBLnotify_party"
    				+ " left join party p8 on p8.id=jos.HBLshipper"
    				+ " left join party p9 on p9.id=jos.HBLconsignee"
    				+ " left join party p10 on p10.id=jos.HBLnotify_party"
    				+ " left join party p4 on p4.id=jos.carrier"
    				+ " left join party p5 on p5.id=jos.head_carrier"
    				+ " left join party p6 on p6.id=jos.oversea_agent"
    				+ " left join party p7 on p7.id=jos.booking_agent"
    				+ " LEFT JOIN location lo on lo.id = jos.por"
					+ " LEFT JOIN location lo1 on lo1.id = jos.pol"
					+ " LEFT JOIN location lo2 on lo2.id = jos.pod"
					+ " LEFT JOIN location lo3 on lo3.id = jos.fnd"
					+ " LEFT JOIN location lo4 on lo4.id = jos.hub"
    				+ " where order_id = ?",id);
    	}else if("insure".equals(type)){
    		re = Db.findFirst("select * from job_order_insurance joi where order_id = ?",id);
    	}else if("express".equals(type)){
    		re = Db.findFirst("select * from job_order_express joe where order_id = ?",id);
    	}else if("air".equals(type)){
    		re = Db.findFirst("select joa.* ,p1.abbr shipperAbbr,p2.abbr consigneeAbbr,p3.abbr notify_partyAbbr,p4.abbr booking_agent_name from job_order_air joa"
    				+ " left join party p1 on p1.id=joa.shipper"
    				+ " left join party p2 on p2.id=joa.consignee"
    				+ " left join party p3 on p3.id=joa.notify_party"
    				+ " left join party p4 on p4.id=joa.booking_agent"
    				+ " where order_id=?", id);
    	}else if("trade".equals(type)){
	    	re = Db.findFirst("select j.*,p.abbr cost_company_name, c.name cost_currency_name from job_order_trade j "
	    			+ " left join party p on p.id = j.cost_company"
	    			+ " left join currency c on c.id = j.cost_currency"
	    			+ " where order_id=?", id);
    	}
		return re;
    }
    
    //返回list
    private List<Record> getItems(String orderId,String type) {
     	String itemSql = "";
    	List<Record> itemList = null;	
    	Office office=LoginUserController.getLoginUserOffice(this);
    	if("shipment".equals(type)){
    		itemSql = "select jos.*,CONCAT(u.name,u.name_eng) unit_name from job_order_shipment_item jos"
    				+ " left join unit u on u.id=jos.unit_id"
    				+ " where order_id=? order by jos.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("air".equals(type)){
    		itemSql = "select IFNULL(concat(lo.name,' -',lo.code),joai.start_from_input) start_from_name,"
    				+ " IFNULL(concat(lo1.name,' -',lo1.code),joai.destination_input) destination_name,"
    				+ " joai.*, pa.abbr air_company_name from job_order_air_item joai"
    		        + " left join party pa on pa.id=joai.air_company"
    				+ " LEFT JOIN location lo on lo.id = joai.start_from"
    		        + " LEFT JOIN location lo1 on lo1.id = joai.destination"
    		        + " where joai.order_id=? order by joai.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("cargoDesc".equals(type)){
    		itemSql = "select * from job_order_air_cargodesc where order_id=? order by id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("land".equals(type)){
    		itemSql = "select jol.*, p.abbr transport_company_name,CAST(GROUP_CONCAT(jold.id) as char ) job_order_land_doc_id, GROUP_CONCAT(jold.doc_name) doc_name,"
    		        + " p1.abbr consignor_name, p2.abbr consignee_name, CONCAT(u.name,u.name_eng) unit_name "
    		        + " from job_order_land_item jol "
    				+ " left join party p on p.id=jol.transport_company"
    				+ " left join party p1 on p1.id=jol.consignor"
    				+ " left join party p2 on p2.id=jol.consignee"
    				+ " left join job_order_land_doc jold on jold.land_id=jol.id"
    				+ " left join unit u on u.id=jol.unit_id"
    				+ " where order_id=?  GROUP BY jol.id order by jol.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("landShipment".equals(type)){
    		itemSql = "select jol.*,CONCAT(IFNULL(p1.address,''),',',GROUP_CONCAT(di.dock_name)) dock_names, p.abbr transport_company_name,CAST(GROUP_CONCAT(jold.id) as char ) job_order_land_doc_id, GROUP_CONCAT(jold.doc_name) doc_name,"
    		        + " p1.abbr consignor_name, p2.abbr consignee_name, CONCAT(u.name,u.name_eng) unit_name "
    		        + " from job_order_land_item jol "
    				+ " left join party p on p.id=jol.transport_company"
    				+ " left join party p1 on p1.id=jol.consignor"
    				+ " left join dockinfo di on di.party_id=jol.consignor "
    				+ " left join party p2 on p2.id=jol.consignee"
    				+ " left join job_order_land_doc jold on jold.land_id=jol.id"
    				+ " left join unit u on u.id=jol.unit_id"
    				+ " where order_id=? and jol.land_type='cabinet_car' GROUP BY jol.id order by jol.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where order_id=? and order_type=? order by jor.id";
    		itemList = Db.find(itemSql, orderId,"charge");
    	}else if("cost".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
	    			+ " c1.name exchange_currency_id_name"
	    			+ " from job_order_arap jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
	    	        + " where order_id=? and order_type=? order by jor.id";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from job_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? order by jod.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from job_order_sendMail where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_cost".equals(type)){
	    	itemSql = "select jotc.*,p.abbr sp_name,c.name currency_name,ifnull(ti.commodity_name,jotc.commodity_name) commodity_name from job_order_trade_cost jotc"
	    			+ " left join party p on p.id = jotc.sp"
	    			+ "	left join trade_item ti on ti.id = jotc.commodity_id"
	    			+ " left join currency c on c.id = jotc.custom_currency"
	    			+ " where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_sale".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name, f.name_eng charge_name_eng, u.name unit_name, c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where jor.order_id=? and jor.order_type=? and jor.trade_fee_flag=? order by jor.id";
    		itemList = Db.find(itemSql, orderId,"charge","trade_sale_fee");
	    }else if("trade_service".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where jor.order_id=? and jor.order_type=? and jor.trade_fee_flag=? order by jor.id";
    		itemList = Db.find(itemSql, orderId,"charge","trade_service_fee");
	    }else if("china_self".equals(type)){
	    	itemSql = "select j.*,p.abbr custom_bank_name from job_order_custom_china_self_item j"
	    			+ " left join party p on p.id = j.custom_bank"
	    			+ " where j.order_id=? order by j.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("custom_doc".equals(type)){
//	    	itemSql = "select jod.*,u.c_name from job_order_custom_doc jod left join user_login u on jod.uploader=u.id "
//	    			+ " where order_id=? order by jod.id";
	        itemSql = "select cpo.ref_job_order_id, jocd.id,jocd.doc_name,jocd.upload_time,jocd.remark,"
	                + " ul.c_name c_name,jocd.uploader, jocd.share_flag  from job_order_custom_doc jocd"
                    + " LEFT JOIN user_login ul on ul.id = jocd.uploader"
                    + " LEFT JOIN custom_plan_order cpo on cpo.ref_job_order_id = jocd.order_id  "                    
                    + " where jocd.order_id =?"
                    + " and jocd.order_type = '"+office.get("type")+"' "
            		+ " and ifnull(cpo.delete_flag,'N') = 'N'"
                    + " union all"
                    + " select cpo.ref_job_order_id, null id ,jod.doc_name,jod.upload_time,jod.remark,u.c_name c_name,"
                    + " jod.uploader, jod.cms_share_flag"
                    + " from custom_plan_order_doc jod " 
                    + " left join custom_plan_order cpo on cpo.id = jod.order_id"
                    +" LEFT JOIN user_login u1 ON cpo.creator = u1.id"
                    + " left join user_login u on jod.uploader=u.id "
                    + " where cpo.ref_job_order_id=? "
                    + " and ifnull(cpo.delete_flag,'N') = 'N' and (u.office_id = "+office.getLong("id")+" or u1.office_id = "+office.getLong("id")+") ";
	    	itemList = Db.find(itemSql, orderId, orderId);
	    }else if("custom_app".equals(type)){
	    	itemSql = "SELECT"
	    			+ " cjo.id, cjo.order_no custom_plan_no, o.office_name custom_bank,cjo.status applybill_status,"
	    			+ " cjo.ref_no custom_order_no, cjo.custom_state custom_status, ul.c_name creator,"
	    			+ " cjo.create_stamp, ul2.c_name fill_name, cjo.fill_stamp,cjo.customs_billCode"
	    			+ " FROM custom_plan_order cjo"
	    			+ " LEFT JOIN user_login ul ON ul.id = cjo.creator"
	    			+ " LEFT JOIN user_login ul2 ON ul2.id = cjo.fill_by"
	    			+ " left join office o on o.id = cjo.to_office_id"
	    			+ " WHERE cjo.ref_job_order_id = "+orderId+" and ul.office_id = "+office.getLong("id")+"  and cjo.delete_flag='N'";
	    	itemList = Db.find(itemSql);
	    }
		return itemList;
	}

    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	JobOrder jobOrder = JobOrder.dao.findById(id);
    	Long job_office_id=jobOrder.getLong("office_id");
    	setAttr("order", jobOrder);
    	UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        
        //判断工作单与登陆用户的office_id是否一致
//        if(!OrderCheckOfficeUtil.checkOfficeEqual("job_order", Long.valueOf(id), office_id)){
//        	renderError(403);// no permission
//            return;
//
//        }
        Date today = new Date();  
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");//分析日期
        String date=parseFormat.format(today);
        
    	//获取汇率日期信息
    	Record r = Db.findFirst("SELECT * from ( SELECT min(to_stamp) min_stamp,office_id FROM currency_rate where office_id=?) A WHERE min_stamp >= ? ",office_id,date);
    	if(r==null){
    		setAttr("rateExpired", "Y");
    	}
    	//获取海运明细表信息
    	setAttr("usedOceanInfo", getUsedOceanInfo());
    	setAttr("shipmentList", getItems(id,"shipment"));
    	setAttr("shipment", getItemDetail(id,"shipment"));
    	//获取空运运明细表信息
    	setAttr("usedAirInfo", getUsedAirInfo());
    	setAttr("airList", getItems(id,"air"));
    	setAttr("cargoDescList", getItems(id,"cargoDesc"));
    	setAttr("air", getItemDetail(id,"air"));
    	//获取陆运明细表信息
    	setAttr("landList", getItems(id,"land"));
    	setAttr("landShipmentList", getItems(id,"landShipment"));
    	//贸易
    	setAttr("trade", getItemDetail(id,"trade"));
    	setAttr("trade_cost_list", getItems(id,"trade_cost"));
    	setAttr("trade_charge_service_list", getItems(id,"trade_service"));
    	setAttr("trade_charge_sale_list", getItems(id,"trade_sale"));

    	//报关
    	setAttr("customItemList",getItems(id, "custom_app"));
    	setAttr("custom",Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china"));
   		setAttr("abroadCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"abroad"));
   		setAttr("hkCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"HK/MAC"));
   		setAttr("customSelf", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china_self"));
   		setAttr("customSelfItemList", getItems(id,"china_self"));
   		setAttr("customDocList", getItems(id,"custom_doc"));
    	//保险
    	setAttr("insurance", getItemDetail(id,"insure"));
    	//快递
    	setAttr("express", getItemDetail(id,"express"));
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("zeroDocList", getDocItems(id,"zero"));
    	setAttr("docList", getItems(id,"doc"));
    	setAttr("oneDocList", getDocItems(id,"one"));
    	setAttr("twoDocList", getDocItems(id,"two"));
    	setAttr("threeDocList", getDocItems(id,"three"));
    	setAttr("fourDocList", getDocItems(id,"four"));
    	
    	//邮件记录
    	setAttr("mailList", getItems(id,"mail"));
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	//客户回显
    	Party party = Party.dao.findById(jobOrder.get("customer_id"));
    	setAttr("party", party);
    	//回显计量单位
    	String job_unit = (String)jobOrder.get("job_unit");
    	setAttr("unit", Db.findFirst("SELECT CONCAT(name,name_eng) job_unit_name from unit WHERE id = ?",job_unit));
    	
    	//工作单创建人
    	long creator = jobOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUser(this));
    	//海运头程资料
   		setAttr("oceanHead", Db.findFirst("select * from job_order_shipment_head where order_id = ?",id));
   		setAttr("truckHead", Db.findFirst("select * from job_order_land_cabinet_truck where order_id = ?",id));
    	  
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    public List<Record> getDocItems(String orderId,String type){
    	String  itemSql = "";
   		 itemSql = "SELECT * ,"
     			+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE "
     			+ " jod0.order_id ="+orderId+" 	AND jod0.type ='"+type+"' and   jod0.send_status='已发送' ) new_count"
     			+ " FROM("
     			+ " select jod.*,u.c_name,u1.c_name sender_name "
     			+ " from job_order_doc jod "
     			+ " left join user_login u on jod.uploader=u.id "
     			+ " LEFT JOIN user_login u1 ON jod.SENDER = u1.id "
         			+ " where jod.order_id="+orderId+" and jod.type='"+type+"' order by jod.id"
         			+ ")B WHERE 1=1 ";
    	List<Record> itemList = Db.find(itemSql);
    	
    	return itemList;
    }
    
    
    public void docTableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	
    	List<Record> list = null;
    	list = getDocItems(order_id,type);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
    }
    
    //常用邮箱模版
    public List<Record> getEmailTemplateInfo(){
    	List<Record> list = Db.find("select t.* from job_order_sendmail_template t"
                + " where t.creator=?", LoginUserController.getLoginUserId(this));
        return list;
    }
    
    
    /**
     * 获取应收模板信息
     */
    public void getArapTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from job_order_arap_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id desc", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    /**
     * 获取陆运常用费用模板信息
     */
    public void getLandArapTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from job_order_land_arap_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    //常用贸易信息
    public void getTradeServiceTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from job_order_trade_service_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    public void getTradeSaleTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from job_order_trade_sale_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    
    //常用海运信息
    public List<Record> getUsedOceanInfo(){
        List<Record> list = Db.find("select t.*,"
                + " p1.abbr MBLshipperAbbr , "
                + " concat(ifnull(p1.address_eng, p1.address), '\r', ifnull(p1.contact_person_eng, p1.contact_person), '\r', ifnull(p1.phone,'')) MBLshipper_info,"
                + " p2.abbr MBLconsigneeAbbr,"
                + " concat(ifnull(p2.address_eng, p2.address), '\r', ifnull(p2.contact_person_eng, p2.contact_person), '\r', ifnull(p2.phone,'')) MBLconsignee_info,"
                + " p3.abbr MBLnotify_partyAbbr,"
                + " concat(ifnull(p3.address_eng, p3.address), '\r', ifnull(p3.contact_person_eng, p3.contact_person), '\r', ifnull(p3.phone,'')) MBLnotify_info,"
                + " p8.abbr HBLshipperAbbr , "
                + " concat(ifnull(p8.address_eng, p8.address), '\r', ifnull(p8.contact_person_eng, p8.contact_person), '\r', ifnull(p8.phone,'')) HBLshipper_info,"
                + " p9.abbr HBLconsigneeAbbr,"
                + " concat(ifnull(p9.address_eng, p9.address), '\r', ifnull(p9.contact_person_eng, p9.contact_person), '\r', ifnull(p9.phone,'')) HBLconsignee_info,"
                + " p10.abbr HBLnotify_partyAbbr,"
                + " concat(ifnull(p10.address_eng, p10.address), '\r', ifnull(p10.contact_person_eng, p10.contact_person), '\r', ifnull(p10.phone,'')) HBLnotify_info,"
                + " p4.abbr carrier_name,p5.abbr head_carrier_name,p6.abbr oversea_agent_name,p7.abbr booking_agent_name,"
                + " concat(ifnull(p6.address_eng, p6.address), '\r', ifnull(p6.contact_person_eng, p6.contact_person), '\r', ifnull(p6.phone,'')) oversea_agent_info,"
                + " lo.name por_name,lo1.name pol_name,lo2.name pod_name, lo3.name fnd_name from job_order_ocean_template t "
                + " left join party p1 on p1.id= t.MBLshipper"
                + " left join party p2 on p2.id= t.MBLconsignee"
                + " left join party p3 on p3.id= t.MBLnotify_party"
                + " left join party p4 on p4.id=t.carrier"
        		+ " left join party p5 on p5.id=t.head_carrier"
        		+ " left join party p6 on p6.id=t.oversea_agent"
        		+ " left join party p7 on p7.id=t.booking_agent"
                + " LEFT JOIN location lo on lo.id = t.por"
                + " LEFT JOIN location lo1 on lo1.id = t.pol"
                + " LEFT JOIN location lo2 on lo2.id = t.pod"
                + " LEFT JOIN location lo3 on lo3.id = t.fnd"
                + " left join party p8 on p8.id= t.HBLshipper"
                + " left join party p9 on p9.id= t.HBLconsignee"
                + " left join party p10 on p10.id= t.HBLnotify_party"
                + " where t.creator_id=? order by t.id", LoginUserController.getLoginUserId(this));
        return list;
    }
    //常用空运信息
    public List<Record> getUsedAirInfo(){
    	List<Record> list = Db.find("select t.*,"
    			+ " p1.abbr shipperAbbr , "
    			+ " concat(ifnull(p1.address_eng, p1.address), '\r', ifnull(p1.contact_person_eng, p1.contact_person), '\r', ifnull(p1.phone,'')) shipper_info,"
    			+ " p2.abbr consigneeAbbr,"
    			+ " concat(ifnull(p2.address_eng, p2.address), '\r', ifnull(p2.contact_person_eng, p2.contact_person), '\r', ifnull(p2.phone,'')) consignee_info,"
    			+ " p3.abbr notify_partyAbbr,"
    			+ " concat(ifnull(p3.address_eng, p3.address), '\r', ifnull(p3.contact_person_eng, p3.contact_person), '\r', ifnull(p3.phone,'')) notify_info,"
    			+ " p7.abbr booking_agent_name from job_order_air_template t "
    			+ " left join party p1 on p1.id= t.shipper"
    			+ " left join party p2 on p2.id= t.consignee"
    			+ " left join party p3 on p3.id= t.notify_party"
    			+ " left join party p7 on p7.id=t.booking_agent"
    			+ " where t.creator_id=? order by t.id", LoginUserController.getLoginUserId(this));
    	return list;
    }
    
    //使用common-email, javamail
    @Before(Tx.class)
    public void sendMail() throws Exception {
    	String order_id = getPara("order_id");
    	String userEmail = getPara("email");
    	String ccEmail = getPara("ccEmail");
    	String bccEmail = getPara("bccEmail");
    	String mailTitle = getPara("mailTitle");
    	String mailContent = getPara("mailContent");
    	String docs = getPara("docs");
    	String regex = "\\s+|,|，|;|；";//以空格或 ， ,；;分割
    	
        MultiPartEmail email = new MultiPartEmail();  
        /*smtp.exmail.qq.com*/
        email.setHostName("smtp.mxhichina.com");
        email.setSmtpPort(465);
        
        /*输入公司的邮箱和密码*/
        email.setAuthenticator(new DefaultAuthenticator("info@yq-scm.com", "Enkyo123"));        
        email.setSSLOnConnect(true);
        email.setFrom("info@yq-scm.com","Enkyo珠海远桥");//设置发信人
        //设置收件人，邮件标题，邮件内容
        if(StringUtils.isNotEmpty(userEmail)){
        	String[] arr = userEmail.split(regex);
        	for(int i=0;i<arr.length;i++){
        		email.addTo(arr[i]);
        	}
        }
        if(StringUtils.isNotEmpty(mailTitle)){
	        email.setSubject(mailTitle);
        }
        if(StringUtils.isNotEmpty(mailContent)){
	        email.setMsg(mailContent);
        }
        
        //抄送
        if(StringUtils.isNotEmpty(ccEmail)){
        	String[] arr = ccEmail.split(regex);
        	for(int i=0;i<arr.length;i++){
        		email.addCc(arr[i]);
        	}
        }
       //密送
        if(StringUtils.isNotEmpty(bccEmail)){
        	String[] arr = bccEmail.split(regex);
        	for(int i=0;i<arr.length;i++){
        		email.addBcc(arr[i]);
        	}
        }
        
        //添加附件
        if(StringUtils.isNotEmpty(docs)){
    		String strAry[] = docs.split(",");
	        for(int i=0;i<strAry.length;i++){
	        	
	        	String filePath = getRequest().getServletContext().getRealPath("/")+"/upload/doc/"+strAry[i];
	            File file = new File(filePath);
	            if (file.exists() && file.isFile()) {
	            	EmailAttachment attachment = new EmailAttachment();
	            	attachment.setPath(filePath);  
	            	attachment.setDisposition(EmailAttachment.ATTACHMENT); 
	            	 
	                //设置附件的中文乱码问题，解决附件的中文名称 乱码问题
	                BASE64Encoder enc = new BASE64Encoder();
	                String fileName= strAry[i];
	            	attachment.setName("=?GBK?B?"+enc.encode(fileName.getBytes())+"?="); 
	            	email.attach(attachment);
	            }
	        }
        }
        try{
        	email.setCharset("UTF-8"); 
        	email.send();
        	JobOrderSendMail jsm = new JobOrderSendMail();
        	jsm.set("order_id", order_id);
        	jsm.set("mail_title", mailTitle);
        	jsm.set("doc_name", docs.replace(",", "  "));
        	jsm.set("receive_mail", userEmail);
        	jsm.set("cc_mail", ccEmail);
        	jsm.set("bcc_mail", bccEmail);
        	jsm.set("sender", LoginUserController.getLoginUserName(this));
        	jsm.set("send_time", new Date());
        	jsm.save();
        	renderJson("{\"result\":true}");
        }catch(Exception e){
        	e.printStackTrace();
        	renderJson("{\"result\":false}");
        }
       
    }
     
    public void list() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	String type=getPara("type");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "order_export_date";
        	sort = "desc";
        }
        
        if (getPara("start") != null  && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
            if("lock".equals(type)){
            	sLimit = "";
            }
        }
        String sql = "";
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jor.office_id in ("+relist.getStr("office_id")+")";
        }
        
        if("sowait".equals(type)){
        	sql=" SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag,ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three')  and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order jor "
        			+ " LEFT JOIN job_order_shipment jos on jor.id = jos.order_id "
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator "
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ ref_office+ ")"
        			+ " and jor.type = '出口柜货' AND jos.SONO IS NULL AND jor.transport_type LIKE '%ocean%'"
        			+ " and jor.delete_flag = 'N'";        	
        }else if("truckorderwait".equals(type)){
        	 sql = "SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
		         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order_land_item joli"
        			+ " left join job_order jor on jor.id = joli.order_id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
        			+ " and datediff(joli.eta, now()) <= 3 AND jor.send_truckorder_flag != 'Y'"
        			+ " AND jor.transport_type LIKE '%land%'"
        			+ " and jor.delete_flag = 'N'";
        	
        	
        } else if("siwait".equals(type)){
        	 sql = " SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
		         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        	 		+ " FROM job_order_shipment jos"
        	 		+ " left join job_order jor on jos.order_id = jor.id"
        	 		+ " left join party p on p.id = jor.customer_id"
        	 		+ " left join user_login u on u.id = jor.creator "
        	 		+ " left join user_login u1 ON u1.id = jor.updator"
        	 		+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and TO_DAYS(jos.export_date)=TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
        	
        } else if("mblwait".equals(type)){
        	sql = "SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order_shipment jos "
        			+ " left join job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and  jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null)"
                    + " and jor.delete_flag = 'N'";
        	
        } else if("customwait".equals(type)){
        	sql = " SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id  and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " from job_order jor "
        			+ " LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " left join job_order_custom_china_self_item jocc on jocc.order_id = jor.id"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and  jor.transport_type LIKE '%custom%'"
        			+ " and isnull(joc.customs_broker) and isnull(jocc.custom_bank)"
        			+ " and jor.delete_flag = 'N'"
        			+ " GROUP BY jor.id";
        	
        } else if("insurancewait".equals(type)){
        	sql = " SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order jor "
        			+ " LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and  jor.transport_type LIKE '%insurance%' and joi.insure_no is NULL"
                    + " and jor.delete_flag = 'N'";
        } else if("overseacustomwait".equals(type)){
        	sql = "SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order_shipment jos "
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y' "
        			+ " and timediff(now(),jos.etd)<TIME('48:00:00') "
        			+ " and jor.delete_flag = 'N'";
        } else if("tlxOrderwait".equals(type)){
        	sql = " SELECT jor.*,if(jor.office_id != "+office_id+",'other','self') other_flag, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(u1.c_name, u1.user_name) updator_name,"
	         		+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
        			+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
					+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
					+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
					+ " LEFT JOIN currency c ON c.id = joa.currency_id "
					+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge "
        			+ " FROM job_order_shipment jos"
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " left join user_login u1 ON u1.id = jor.updator"
        			+ " WHERE (jor.office_id="+office_id+ref_office+ ")"
                    + " and TO_DAYS(jos.etd)= TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
        }
        else{
		         sql = 		"SELECT * from (select jor.*, loc.name as pod_name,jos.sono,jos.mbl_no,concat(ifnull(jos.sono, \"\"),ifnull(concat(\" / \",jos.mbl_no), \"\")) AS sono_mbl,if(jor.office_id != "+office_id+",'other','self') other_flag,"
		         			+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
		         			+" (SELECT GROUP_CONCAT(josi.container_no SEPARATOR '<br>' ) "
		        		 	+" FROM  job_order_shipment_item josi  "
		        		 	+" LEFT JOIN job_order jo on jo.id=josi.order_id "
		        		 	+" WHERE josi.order_id =jor.id) container_no, "
		        		 	+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
							+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
							+ " LEFT JOIN currency c ON c.id = joa.currency_id "
							+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
							+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
							+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
							+ " LEFT JOIN currency c ON c.id = joa.currency_id "
							+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge, "
		         		+ " ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p.company_name,p.code customer_code,ifnull(u1.c_name, u1.user_name) updator_name"
		         		+ "	from job_order jor"
		         		+ "	left join job_order_shipment jos on jos.order_id = jor.id"
		         		+ " left join location loc on jos.pod=loc.id"
		         		+ "	left join party p on p.id = jor.customer_id"
		         		+ "	left join user_login u on u.id = jor.creator"
		         		+ " left join user_login u1 ON u1.id = jor.updator"
		         		+ " WHERE (jor.office_id="+office_id+ ref_office+ ")"
		         	    + " and jor.delete_flag = 'N'"
		         	    + " GROUP BY jor.id "
		         	    + " ) A where 1 = 1 ";
         }
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        System.out.println(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
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
    
    //异步刷新字表
    public void tableListOfLandCharge(){
    	
    	//搜索此陆运相关的应收费用，用来打印debit_note
    	String order_id = getPara("order_id");
    	String land_item_id = getPara("land_item_id");
	    String itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_eng_name,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where order_id=? and order_type=? and land_item_id=? order by jor.id";
	    List<Record> list = Db.find(itemSql, order_id,"charge",land_item_id);
	    
    	
    	Map map = new HashMap();
    	map.put("sEcho", 1);
    	map.put("iTotalRecords", list.size());
    	map.put("iTotalDisplayRecords", list.size());
    	map.put("aaData", list);
    	renderJson(map); 
    }
    
    @Before(Tx.class)
    public void saveParty(){
    	String jsonStr=getPara("params");
       	String id = null;
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        Party order = new Party();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (true)  {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("creator", user.getLong("id"));
   			order.set("create_date", new Date());
   			order.set("office_id", pom.getCurrentOfficeId());
   			order.save();
   			
   			id = order.getLong("id").toString();
   			UserCustomer  customer = new UserCustomer();
   			customer.set("customer_id", id);
   			customer.set("user_name", user.getStr("user_name"));
   			customer.save();
   		}
   		renderJson(order);
    }
   
    //工作单锁单解锁
    @Before(Tx.class)
    public void confirmCompleted(){
    	String id = getPara("id"); 
    	String[] idArray = id.split(",");
    	String action = getPara("action");
    	long user_id = LoginUserController.getLoginUser(this).getLong("id");
    	String order_type = "jobOrderLock";
    	Date action_time = new Date();
    	if(action=="lock"||action.equals("lock")){
    		for (int i = 0; i < idArray.length; i++) {
        		JobOrder order = JobOrder.dao.findById(idArray[i]);
            	order.set("status", "已完成");
            	order.update();
            	Record re = new Record();
            	re.set("order_id", idArray[i]);
            	re.set("user_id", user_id);
            	re.set("order_type", order_type);
            	re.set("action_time", action_time);
            	re.set("action", action);
            	Db.save("status_audit", re);
    		}
    	}
    	if(action=="unLock"||action.equals("unLock")){
    		for (int i = 0; i < idArray.length; i++) {
        		JobOrder order = JobOrder.dao.findById(idArray[i]);
            	order.set("status", "新建");
            	order.update();
            	Record re = new Record();
            	re.set("order_id", idArray[i]);
            	re.set("user_id", user_id);
            	re.set("order_type", order_type);
            	re.set("action_time", action_time);
            	re.set("action", action);
            	Db.save("status_audit", re);
    		}
    	}
    	renderJson("{\"result\":true}");
    }
    
    //费用应收打印debite_note PDF前保存
    @Before(Tx.class)
    public void saveDebitNote(){
    	String ids = getPara("itemIds");
    	String invoiceNo = getPara("invoiceNo");
    	Db.update("update job_order_arap set invoice_no ='"+invoiceNo+"' where id in ("+ids+")");
    	renderJson("{\"result\":true}");
    }
    
    //陆运打印Invoice(分单)前保存hbl_no
    @Before(Tx.class)
    public void saveDebitNoteOfLand(){
    	String ids = getPara("landIds");
    	String invoice_land_hbl_no = getPara("invoice_land_hbl_no");
    	String land_ref_no = getPara("land_ref_no");
    	Db.update("update job_order_arap set invoice_land_hbl_no='"+invoice_land_hbl_no+"',land_ref_no='"+land_ref_no+"'  where land_item_id in ("+ids+")");
    	
     	renderJson("{\"result\":true}");
    }
    
    //删除费用明细常用信息模版
    @Before(Tx.class)
    public void deleteArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
    //删除陆运费用明细常用信息模版
    @Before(Tx.class)
    public void deleteLandArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_land_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeSaleTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_trade_sale_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeServiceTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_trade_service_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除海运常用信息模版
    @Before(Tx.class)
    public void deleteOceanTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_ocean_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除空运常用信息模版
    @Before(Tx.class)
    public void deleteAirTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_air_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除邮箱常用模版
    @Before(Tx.class)
    public void deleteEmailTemplate(){
    	String id = getPara("id");
    	Db.update("delete from job_order_sendmail_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //费用明细确认
    @Before(Tx.class)
    public void feeConfirm(){
		String id = getPara("id");
		if (id != null) {
        	JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap where id = ?",id);
           		joa.set("audit_flag", "Y");
        	   	joa.update();
        }
		//Db.update("update job_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from job_order_arap where id = ?",id);
		renderJson(re);
	 }
  //费用明细取消确认，
    @Before(Tx.class)
    public void feeCancelConfirm(){
		String id = getPara("id");
		if (id != null) {
        	JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap where id = ?",id);
        	if( joa.get("audit_flag").equals("Y")&&joa.get("bill_flag").equals("N")){
        		joa.set("audit_flag", "N");
        	}
        	joa.update();
        }
		//Db.update("update job_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from job_order_arap where id = ?",id);
		renderJson(re);
	 }
    
    @Before(Tx.class)
    public void updateShare(){
    	String item_id = getPara("item_id");
    	String check = getPara("check");
    	String order_id = getPara("order_id");
    	Office office=LoginUserController.getLoginUserOffice(this);
    	if(StringUtils.isEmpty(item_id)){//全选
    		Db.update("update job_order_custom_doc set share_flag =? where order_id = ? and order_type = '"+office.get("type")+"' ",check,order_id);
    	}else{//单选
    		Db.update("update job_order_custom_doc set share_flag =? where id = ? and order_type='forwarderCompany'",check,item_id);
//    		
//    		List<Record> CPOList = Db.find("select cpod.* from custom_plan_order cpo where cpo.ref_job_order_id = ?",order_id);
//    		for(Record re :CPOList){
//    			List<Record> reList = Db.find("select cpod.* from custom_plan_order cpo where cpo.ref_job_order_id = ?",order_id);
//    			
//    			long docId = re.getLong("id");
//    			Db.update("insert into custom_plan_order_doc(order_id,uploader,doc_name,upload_time,remark) "
//    					+ " values",docId,order_id,);
//    		}
    	}
    	
    	renderJson("{\"result\":true}");
    }
    
  //删除单据，设置为已删
    @Before(Tx.class)
    public void deleteOrder(){
    	String id = getPara("id");
    	String delete_reason = getPara("delete_reason");
    	Long deletor = LoginUserController.getLoginUserId(this);
    	Date date = new Date();
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String delete_stamp = sf.format(date);
    	Db.update("update job_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
    			+ " delete_reason='"+delete_reason+"' where id = ?  ",id);
    	renderJson("{\"result\":true}");
    }
    
    
    //保存陆运相关的应收费用
    @Before(Tx.class)
    public void saveLandCharge() throws InstantiationException, IllegalAccessException{
    	
    	String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String order_id = (String) dto.get("order_id");
        String land_item_id = (String) dto.get("land_item_id");
    	
        List<Map<String, String>> land_charge_item = (ArrayList<Map<String, String>>)dto.get("land_charge_item");
        Model<?> model = (Model<?>) JobOrderArap.class.newInstance();
        for(int i=0;i<land_charge_item.size();i++){
        	Map<String, String> map=land_charge_item.get(i);
        	
        	DbUtils.setModelValues(map,model);
        	model.set("land_item_id", land_item_id);
        	model.set("order_id", order_id);
        	if("UPDATE".equals(map.get("action"))){
        		model.update();
        	}else if("DELETE".equals(map.get("action"))){
        		model.delete();
        	}else{
        		model.save();
        	}
        }
      //保存陆运费用模版
        String type = (String) dto.get("type");//根据工作单类型生成不同前缀
        String customer_id = (String)dto.get("customer_id");
   		List<Map<String, String>> land_charge_template = (ArrayList<Map<String, String>>)dto.get("land_charge_template");
		List<Map<String, String>> land_cost_template = (ArrayList<Map<String, String>>)dto.get("land_cost_template");
		List<Map<String, String>> land_allCharge_template = (ArrayList<Map<String, String>>)dto.get("land_allCharge_template");
		List<Map<String, String>> land_allCost_template = (ArrayList<Map<String, String>>)dto.get("land_allCost_template");
		saveLandArapTemplate(type,customer_id,land_charge_template,land_cost_template,land_allCharge_template,land_allCost_template);
   		
        renderJson("{\"result\":true}");
    }
    
    //新文档上传标记
    @Before(Tx.class)
    public void newFlag(){
        //获取office_id
    	String id = getPara("id");
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if(office_id!=1&&office_id!=2){
   			Db.update("update job_order_custom_doc set new_flag ='N' where id = ?",id);
   		}
    	renderJson("{\"result\":true}");
    }
    
    
    //商品名名称下拉列表
    public void searchCommodity(){
    	String input = getPara("input");
    	List<Record> recs = null;
    	UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
    	String sql = "select * from trade_item where 1=1 and office_id = "+office_id;
    	if(StringUtils.isNotEmpty(input)){
    		sql+=" and commodity_name like '%"+ input +"%' ";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    
    
    //文件下载
    @Before(Tx.class)
    public void downloadDoc(){
    	String id = getPara("docId");
    	JobOrderDoc jobOrderDoc = JobOrderDoc.dao.findById(id);
    	jobOrderDoc.set("receiver", LoginUserController.getLoginUserId(this));
    	jobOrderDoc.set("receive_time", new Date());
    	jobOrderDoc.set("send_status", "已接收");
    	jobOrderDoc.update();
    	
    	Long ref_doc_id = jobOrderDoc.getLong("ref_doc_id");
    	BookOrderDoc bookOrderDoc = BookOrderDoc.dao.findById(ref_doc_id);
    	if(bookOrderDoc!=null){
    		bookOrderDoc.set("receiver", LoginUserController.getLoginUserId(this));
    		bookOrderDoc.set("receive_time", new Date());
    		bookOrderDoc.set("send_status", "已接收");
    		bookOrderDoc.update();
    	}
        renderJson(jobOrderDoc);
    }
    
    //文件确认
    @Before(Tx.class)
    public void confirmDoc(){
    	String id = getPara("docId");
    	JobOrderDoc jobOrderDoc = JobOrderDoc.dao.findById(id);
    	jobOrderDoc.set("confirm", LoginUserController.getLoginUserId(this));
    	jobOrderDoc.set("confirm_time", new Date());
    	jobOrderDoc.set("send_status", "已确认");
    	jobOrderDoc.update();
    	
    	Long ref_doc_id = jobOrderDoc.getLong("ref_doc_id");
    	BookOrderDoc bookOrderDoc = BookOrderDoc.dao.findById(ref_doc_id);
    	if(bookOrderDoc!=null){
    		bookOrderDoc.set("confirm", LoginUserController.getLoginUserId(this));
    		bookOrderDoc.set("confirm_time", new Date());
    		bookOrderDoc.set("send_status", "已确认");
    		bookOrderDoc.update();
    	}
        renderJson(jobOrderDoc);
    }
    
    
    //确认发送文档
    @Before(Tx.class)
    public void confirmSend(){
    	String id = getPara("docId");
    	String plan_order_item_id = getPara("plan_order_item_id");
    	String plan_order_id = getPara("plan_order_id");
    	JobOrderDoc jobOrderDoc = JobOrderDoc.dao.findById(id);
    	jobOrderDoc.set("sender", LoginUserController.getLoginUserId(this));
    	jobOrderDoc.set("send_time", new Date());
    	jobOrderDoc.set("send_status", "已发送");
    	jobOrderDoc.update();
    	
    	Record re = Db.findFirst("select * from book_order where plan_item_id = ?",plan_order_item_id);
    	
    	Record bookDoc = new Record();
    	bookDoc.set("order_id", re.getLong("id"));
    	bookDoc.set("type", jobOrderDoc.getStr("type"));
    	bookDoc.set("uploader", jobOrderDoc.getLong("uploader"));
    	bookDoc.set("doc_name", jobOrderDoc.getStr("doc_name"));
    	bookDoc.set("upload_time", jobOrderDoc.get("upload_time"));
    	bookDoc.set("remark", jobOrderDoc.getStr("remark"));
    	bookDoc.set("sender", jobOrderDoc.getLong("sender"));
    	bookDoc.set("send_time", jobOrderDoc.get("send_time"));
    	bookDoc.set("send_status", jobOrderDoc.getStr("send_status"));
    	bookDoc.set("ref_doc_id", id);
    	Db.save("book_order_doc", bookDoc);
      
        renderJson(jobOrderDoc);
    }


}