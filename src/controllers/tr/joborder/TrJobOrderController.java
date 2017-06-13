package controllers.tr.joborder;

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

import models.ParentOfficeModel;
import models.Party;
import models.UserCustomer;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.tr.tradeJoborder.TradeJobOrder;
import models.eeda.tr.tradeJoborder.TradeJobOrderArap;
import models.eeda.tr.tradeJoborder.TradeJobOrderDoc;
import models.eeda.tr.tradeJoborder.TradeJobOrderSendMail;
import models.eeda.tr.tradeJoborder.TradeJobOrderSendMailTemplate;

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
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TrJobOrderController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(TrJobOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type = getPara("type");
		setAttr("type",type);
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/trJobOrder");
        setAttr("listConfigList", configList);
		render("/tr/trJobOrder/trJobOrderList.html");
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
        	Party party = Party.dao.findById(planOrder.get("customer_id"));
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
    	setAttr("loginUser",LoginUserController.getLoginUserName(this));
    	setAttr("login_id", LoginUserController.getLoginUserId(this));
        render("/tr/trJobOrder/trJobOrderEdit.html");
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
    	TradeJobOrder job_order = TradeJobOrder.dao.findById(order_id);
    	job_order.set("send_truckorder_flag", "Y");
    	job_order.update();
    	renderJson("{\"result\":true,\"send_truckorder_flag\":\"Y\"}");
    }
  
    
  
    
    
    //根据工作单类型生成不同前缀
    public String generateJobPrefix(String type){
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
        
        TradeJobOrder jobOrder = new TradeJobOrder();
        
        //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		String export_date = (String)dto.get("order_export_date");
        String newDateStr = "";
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");//分析日期
        SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
        try {
            Date date=parseFormat.parse(export_date);
            newDateStr=sdf.format(date);
        } catch (ParseException ex) {
            logger.debug("处理工作单出货日期出错："+ex.getMessage());
        }
        logger.debug("工作单出货日期："+newDateStr);
        
        
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			jobOrder = TradeJobOrder.dao.findById(id);
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
   			if(!type.equals(jobOrder.get("type")) || 
   			        ( 
   			             StrKit.notBlank(oldDateStr) && 
   			             !newDateStr.equals(oldDateStr) && 
   			             !newDateStr.equals(oldOrderNoDate)
   			        ) 
   			  ){
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
   		long customerId = Long.valueOf(dto.get("customer_id").toString());
   		saveCustomerQueryHistory(customerId);
		
		//费用明细，应收应付
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, id, TradeJobOrderArap.class, "order_id");
		List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
		DbUtils.handleList(chargeCost_list, id, TradeJobOrderArap.class, "order_id");
		//记录结算公司使用历史	
		saveAccoutCompanyQueryHistory(charge_list);
		saveAccoutCompanyQueryHistory(chargeCost_list);
		//记录结算费用使用历史  
		saveFinItemQueryHistory(charge_list);
		saveFinItemQueryHistory(chargeCost_list);
		
		//相关文档
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, id, TradeJobOrderDoc.class, "order_id");
		
		//报关
		List<Map<String, String>> chinaCustom = (ArrayList<Map<String, String>>)dto.get("chinaCustom");
		List<Map<String, String>> abroadCustom = (ArrayList<Map<String, String>>)dto.get("abroadCustom");
		List<Map<String, String>> hkCustom = (ArrayList<Map<String, String>>)dto.get("hkCustom");
		List<Map<String, String>> chinaCustom_self_item = (ArrayList<Map<String, String>>)dto.get("chinaCustom_self_item");
		DbUtils.handleList(chinaCustom, id, JobOrderCustom.class, "trade_order_id");
		DbUtils.handleList(chinaCustom_self_item, "job_order_custom_china_self_item", id, "trade_order_id");
		DbUtils.handleList(abroadCustom, id, JobOrderCustom.class, "trade_order_id");
		DbUtils.handleList(hkCustom, id, JobOrderCustom.class, "trade_order_id");
		
		
		

		//贸易
		List<Map<String, String>> trade_detail = (ArrayList<Map<String, String>>)dto.get("trade_detail");
		DbUtils.handleList(trade_detail,"trade_job_order_trade",id,"order_id");
		List<Map<String, String>> trade_cost_list = (ArrayList<Map<String, String>>)dto.get("trade_cost");
		DbUtils.handleList(trade_cost_list,"trade_job_order_trade_cost",id,"order_id");
		
		Model<?> model = (Model<?>) TradeJobOrderArap.class.newInstance();
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
   		r.set("trade", getItemDetail(id,"trade"));
    	   		
   	    //费用明细，应收应付模版
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
    	
    	String chargeSql = "select parent_id from trade_job_order_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
    	String costSql = "select parent_id from trade_job_order_arap_template where"
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
                Db.save("trade_job_order_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("trade_job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update trade_job_order_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
                Db.save("trade_job_order_arap_template", all);  
                
        		//保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "cost");
                r.set("order_type", order_type);
                r.set("json_value", costObject);
                r.set("parent_id",  all.getLong("id"));
                Db.save("trade_job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = costRec.getLong("parent_id");
        	Db.update("update trade_job_order_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
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
    	
    	String chargeSql = "select parent_id from trade_job_order_trade_service_template where"
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
                Db.save("trade_job_order_trade_service_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("trade_job_order_trade_service_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update trade_job_order_trade_service_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
    	
    	String chargeSql = "select parent_id from trade_job_order_trade_sale_template where"
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
                Db.save("trade_job_order_trade_sale_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("trade_job_order_trade_sale_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update trade_job_order_trade_sale_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
    	TradeJobOrderSendMailTemplate order = new TradeJobOrderSendMailTemplate();
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
   
    
    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile() throws Exception{
    	try {
            String order_id = getPara("order_id");
            List<UploadFile> fileList = getFiles("doc");
            Long userId = LoginUserController.getLoginUserId(this);
            
            FileUploadUtil.uploadFile(fileList, order_id, userId, "trade_job_order_doc", false);
            
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
    
    //报关的文档上传
    @Before(Tx.class)
    public void uploadCustomDoc() throws Exception{
        try {
            String order_id = getPara("order_id");
            List<UploadFile> fileList = getFiles("doc");
            Long userId = LoginUserController.getLoginUserId(this);
            
            FileUploadUtil.uploadFile(fileList, order_id, userId, "job_order_custom_doc", false);
            
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
    	TradeJobOrderDoc jobOrderDoc = TradeJobOrderDoc.dao.findById(id);
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
    	if("trade".equals(type)){
	    	re = Db.findFirst("select j.*,p.abbr cost_company_name, c.name cost_currency_name from trade_job_order_trade j "
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
     if("charge".equals(type)){
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from trade_job_order_arap jor "
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
	    			+ " from trade_job_order_arap jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
	    	        + " where order_id=? and order_type=? order by jor.id";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from trade_job_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? order by jod.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from trade_job_order_sendMail where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_cost".equals(type)){
	    	itemSql = "select jotc.*,p.abbr sp_name,c.name currency_name,ifnull(ti.commodity_name,jotc.commodity_name) commodity_name from trade_job_order_trade_cost jotc"
	    			+ " left join party p on p.id = jotc.sp"
	    			+ "	left join trade_item ti on ti.id = jotc.commodity_id"
	    			+ " left join currency c on c.id = jotc.custom_currency"
	    			+ " where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_sale".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name, f.name_eng charge_name_eng, u.name unit_name, c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from trade_job_order_arap jor "
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
    				+ " from trade_job_order_arap jor "
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
	    			+ " where order_type='trJobOrder' and trade_order_id="+orderId+" order by j.id ";
	    	itemList = Db.find(itemSql);
	    }else if("custom_doc".equals(type)){
//	    	itemSql = "select jod.*,u.c_name from job_order_custom_doc jod left join user_login u on jod.uploader=u.id "
//	    			+ " where order_id=? order by jod.id";
	        itemSql = "select cpo.ref_job_order_id, jocd.id,jocd.doc_name,jocd.upload_time,jocd.remark,"
	                + " ul.c_name c_name,jocd.uploader, jocd.share_flag ,null share_flag from job_order_custom_doc jocd"
                    + " LEFT JOIN user_login ul on ul.id = jocd.uploader"
                    + " LEFT JOIN custom_plan_order cpo on cpo.ref_job_order_id = jocd.order_id and jocd.share_flag = 'Y' and cpo.delete_flag='N'"
                    + " where jocd.order_id =?"
                    + " union all"
                    + " select cpo.ref_job_order_id, null id ,jod.doc_name,jod.upload_time,jod.remark,u.c_name c_name,"
                    + " jod.uploader,null share_flag, jod.cms_share_flag"
                    + " from custom_plan_order_doc jod "
                    + " left join custom_plan_order cpo on cpo.id = jod.order_id"
                    + " left join user_login u on jod.uploader=u.id "
                    + " where cpo.ref_job_order_id=? and cpo.delete_flag='N'";
	    	itemList = Db.find(itemSql, orderId, orderId);
	    }else if("custom_app".equals(type)){
	    	itemList = Db.find("SELECT"
	    			+ " cjo.id, cjo.order_no custom_plan_no, o.office_name custom_bank,cjo.status applybill_status,"
	    			+ " cjo.ref_no custom_order_no, cjo.custom_state custom_status, ul.c_name creator,"
	    			+ " cjo.create_stamp, ul2.c_name fill_name, cjo.fill_stamp,cjo.customs_billCode"
	    			+ " FROM custom_plan_order cjo"
	    			+ " LEFT JOIN user_login ul ON ul.id = cjo.creator"
	    			+ " LEFT JOIN user_login ul2 ON ul2.id = cjo.fill_by"
	    			+ " left join office o on o.id = cjo.to_office_id"
	    			+ " WHERE cjo.ref_job_order_id = ?  and cjo.delete_flag='N'",orderId);
	    }
		return itemList;
	}

    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	TradeJobOrder jobOrder = TradeJobOrder.dao.findById(id);
    	setAttr("order", jobOrder);
    	
    	//获取汇率日期信息
    	Record r = Db.findFirst("SELECT * from ( SELECT min(to_stamp) min_stamp FROM currency_rate) A WHERE min_stamp > now()");
    	if(r==null){
    		setAttr("rateExpired", "Y");
    	}
    	//贸易
    	setAttr("trade", getItemDetail(id,"trade"));
    	setAttr("trade_cost_list", getItems(id,"trade_cost"));
    	setAttr("trade_charge_service_list", getItems(id,"trade_service"));
    	setAttr("trade_charge_sale_list", getItems(id,"trade_sale"));
    	
    	//报关
    	setAttr("customItemList",getItems(id, "custom_app"));
    	setAttr("custom",Db.findFirst("select * from job_order_custom joc where custom_type = ? and order_type='trJobOrder' and trade_order_id="+id,"china"));
   		setAttr("abroadCustom", Db.findFirst("select * from job_order_custom joc where custom_type = ? and order_type='trJobOrder' and trade_order_id="+id,"abroad"));
   		setAttr("hkCustom", Db.findFirst("select * from job_order_custom joc where custom_type = ? and order_type='trJobOrder' and trade_order_id="+id,"HK/MAC"));
   		setAttr("customSelf", Db.findFirst("select * from job_order_custom joc where custom_type = ? and order_type='trJobOrder' and trade_order_id="+id,"china_self"));
   		setAttr("customSelfItemList", getItems(id,"china_self"));
   		setAttr("customDocList", getItems(id,"custom_doc"));
    	
    	
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));
    	//邮件记录
    	setAttr("mailList", getItems(id,"mail"));
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	//客户回显
    	Party party = Party.dao.findById(jobOrder.get("customer_id"));
    	setAttr("party", party);
    	//工作单创建人
    	long creator = jobOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));
    	setAttr("login_id", LoginUserController.getLoginUserId(this));
    	  
        render("/tr/trJobOrder/trJobOrderEdit.html");
    }
    
    //常用邮箱模版
    public List<Record> getEmailTemplateInfo(){
    	List<Record> list = Db.find("select t.* from trade_job_order_sendmail_template t"
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
    	List<Record> list = Db.find("select * from trade_job_order_arap_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
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
    	List<Record> list = Db.find("select * from trade_job_order_trade_service_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    public void getTradeSaleTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from trade_job_order_trade_sale_template "
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
        	TradeJobOrderSendMail jsm = new TradeJobOrderSendMail();
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
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        if("sowait".equals(type)){
        	sql=" ";        	
        }        
        else{
		         sql = "SELECT * from ("
		         		+ " select jo.*, if(jo.office_id != "+office_id+",'other','self') other_flag,"
		         		 + " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount)) from trade_job_order_arap joa"
							+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
							+ " WHERE joa.order_id=jo.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
							+ "cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount)) from trade_job_order_arap joa"
							+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
							+ " WHERE joa.order_id=jo.id and joa.order_type='charge'  group by joa.order_type) as char) charge, "
		         		+ " ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p.company_name,p.code customer_code"
		         		+ "	from trade_job_order jo"
		         		+ "	left join party p on p.id = jo.customer_id"
		         		+ "	left join user_login u on u.id = jo.creator"
		         		+ "	where (jo.office_id="+office_id+ref_office+ ")"
		         	    + " and jo.delete_flag = 'N'"
		         	    + " GROUP BY jo.id "
		         	    + " ) A where 1 = 1 ";
         }
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by order_export_date desc " +sLimit);
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
	    String itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from trade_job_order_arap jor "
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
   
    //确认已完成工作单
    @Before(Tx.class)
    public void confirmCompleted(){
    	String id = getPara("id");
    	TradeJobOrder order = TradeJobOrder.dao.findById(id);
    	order.set("status", "已完成");
    	order.update();
    	renderJson("{\"result\":true}");
    }
    
    //费用应收打印debite_note PDF前保存
    @Before(Tx.class)
    public void saveDebitNote(){
    	String ids = getPara("itemIds");
    	String invoiceNo = getPara("invoiceNo");
    	Db.update("update trade_job_order_arap set invoice_no ='"+invoiceNo+"' where id in ("+ids+")");
    	renderJson("{\"result\":true}");
    }
    
    //陆运打印Invoice(分单)前保存hbl_no
    @Before(Tx.class)
    public void saveDebitNoteOfLand(){
    	String ids = getPara("landIds");
    	String invoice_land_hbl_no = getPara("invoice_land_hbl_no");
    	String land_ref_no = getPara("land_ref_no");
    	Db.update("update trade_job_order_arap set invoice_land_hbl_no='"+invoice_land_hbl_no+"',land_ref_no='"+land_ref_no+"'  where land_item_id in ("+ids+")");
    	
     	renderJson("{\"result\":true}");
    }
    
    //删除费用明细常用信息模版
    @Before(Tx.class)
    public void deleteArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trade_job_order_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
   
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeSaleTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trade_job_order_trade_sale_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeServiceTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trade_job_order_trade_service_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //删除邮箱常用模版
    @Before(Tx.class)
    public void deleteEmailTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trade_job_order_sendmail_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //费用明细确认
    @Before(Tx.class)
    public void feeConfirm(){
		String id = getPara("id");
		if (id != null) {
        	TradeJobOrderArap joa = TradeJobOrderArap.dao.findFirst("select * from trade_job_order_arap where id = ?",id);
           		joa.set("audit_flag", "Y");
        	   	joa.update();
        }
		//Db.update("update trade_job_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from trade_job_order_arap where id = ?",id);
		renderJson(re);
	 }
  //费用明细取消确认，
    @Before(Tx.class)
    public void feeCancelConfirm(){
		String id = getPara("id");
		if (id != null) {
        	TradeJobOrderArap joa = TradeJobOrderArap.dao.findFirst("select * from trade_job_order_arap where id = ?",id);
        	if( joa.get("audit_flag").equals("Y")&&joa.get("bill_flag").equals("N")){
        		joa.set("audit_flag", "N");
        	}
        	joa.update();
        }
		//Db.update("update trade_job_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from trade_job_order_arap where id = ?",id);
		renderJson(re);
	 }
    
    @Before(Tx.class)
    public void updateShare(){
    	String item_id = getPara("item_id");
    	String check = getPara("check");
    	String order_id = getPara("order_id");
    	
    	if(StringUtils.isEmpty(item_id)){//全选
    		Db.update("update job_order_custom_doc set share_flag =? where trade_order_id = ?",check,order_id);
    	}else{//单选
    		Db.update("update job_order_custom_doc set share_flag =? where id = ?",check,item_id);
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
    	Db.update("update trade_job_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
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
        Model<?> model = (Model<?>) TradeJobOrderArap.class.newInstance();
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


}
