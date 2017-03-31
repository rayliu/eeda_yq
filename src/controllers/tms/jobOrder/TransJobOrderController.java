package controllers.tms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
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
import models.eeda.oms.jobOrder.JobOrderSendMail;
import models.eeda.oms.jobOrder.JobOrderSendMailTemplate;
import models.eeda.tms.TransJobOrder;
import models.eeda.tms.TransJobOrderArap;
import models.eeda.tms.TransJobOrderDoc;
import models.eeda.tms.TransJobOrderLandItem;

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
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.sun.org.apache.bcel.internal.generic.NEW;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransJobOrderController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(TransJobOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type = getPara("type");
		setAttr("type",type);
		render("/tms/TransJobOrder/JobOrderList.html");
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
	    	
	    	//返回海运的港口名称,加多一个船公司
	    	String port_sql = "select lo.name por_name,lo1.name pol_name,lo2.name pod_name,p.abbr carrier_name from plan_order_item joi"
				    			+" LEFT JOIN location lo on lo.id = joi.por"
				    			+" LEFT JOIN location lo1 on lo1.id = joi.pol"
				    			+" LEFT JOIN location lo2 on lo2.id = joi.pod"
				    			+ " left join party p on p.id = joi.carrier"
				    			+" where joi.id = ?";
	    	setAttr("portCreate",Db.findFirst(port_sql,id));
	    	
    	}
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	setAttr("loginUser",LoginUserController.getLoginUserName(this));
        render("/tms/TransJobOrder/JobOrderEdit.html");
    }
    

    



    
    //插入派车单打印动作标记
    public void truckOrderflag(){
    	String jsonStr = getPara("itemId");
    	TransJobOrderLandItem tjoli = TransJobOrderLandItem.dao.findFirst("select id from trans_job_order_land_item where id = ?",jsonStr);
    	tjoli.set("truckorder_flag", "Y");
    	tjoli.update();
    	renderJson("{\"result\":true}");
    }
    

    
    //根据工作单类型生成不同前缀
    public String generateJobPrefix(String type){
    		String prefix = "";
			if(type.equals("出口柜货")||type.equals("进口柜货")||type.equals("出口散货")||type.equals("内贸海运")){
				prefix+="EKO";
			}
			else if(type.equals("出口空运")||type.equals("进口空运")){
				prefix+="EKA";
			}
			else if(type.equals("香港头程")||type.equals("香港游")||type.equals("进口散货")){
				prefix+="EKL";
			}
			else if(type.equals("加贸")||type.equals("园区游")){
				prefix+="EKP";
			}
			else if(type.equals("陆运")){
				prefix+="EKT";
			}
			else if(type.equals("报关")){
				prefix+="EKC";
			}
			else if(type.equals("快递")){
				prefix+="EKE";
			}
			else if(type.equals("贸易")){
				prefix+="EKB";
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
//      String planOrderItemID = (String) dto.get("plan_order_item_id");
        String type = (String) dto.get("type");//根据工作单类型生成不同前缀
        String customer_id = (String)dto.get("customer_id");
        
        TransJobOrder transJobOrder = new TransJobOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			transJobOrder = TransJobOrder.dao.findById(id);

   			if(!type.equals(transJobOrder.get("type"))){
	   			String order_no = OrderNoGenerator.getNextOrderNo(generateJobPrefix(type), office_id);
	   			transJobOrder.set("order_no", order_no);
   			}
            
   			transJobOrder.set("updator", user.getLong("id"));
   			transJobOrder.set("update_stamp", new Date());
   			DbUtils.setModelValues(dto, transJobOrder);
   			
   			transJobOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, transJobOrder);
   			
   			//需后台处理的字段
   			String order_no = OrderNoGenerator.getNextOrderNo(generateJobPrefix(type), office_id);
   			transJobOrder.set("order_no", order_no);
   			transJobOrder.set("creator", user.getLong("id"));
   			transJobOrder.set("create_stamp", new Date());
   			transJobOrder.set("office_id", office_id);
   			transJobOrder.save();
   			id = transJobOrder.getLong("id").toString();
   			
   			//创建过工作单，设置plan_order_item的字段
//   			PlanOrderItem planOrderItem = PlanOrderItem.dao.findById(planOrderItemID);
//   			if(planOrderItem!=null){
//                   planOrderItem.set("is_gen_job", "Y");
//                   planOrderItem.update();
//   			}
   		}
   		long customerId = Long.valueOf(dto.get("customer_id").toString());
   		saveCustomerQueryHistory(customerId);
		
		//陆运
		List<Map<String, String>> land_item = (ArrayList<Map<String, String>>)dto.get("land_list");
		DbUtils.handleList(land_item, id, TransJobOrderLandItem.class, "order_id");
		for(int i=0;i<land_item.size();i++){
			Map<String, ?> map=land_item.get(i);
			if(StringUtils.isEmpty((String) map.get("id"))){
				String sqlString="SELECT A.*,c.name currency_name from( SELECT pq.* FROM party_quotation pq "
						+" WHERE pq.party_id = "+customerId
						+" and pq.loading_wharf1= "+map.get("LOADING_WHARF1")
						+" and pq.loading_wharf2= "+map.get("LOADING_WHARF2")
						+" and pq.take_address= "+map.get("TAKE_ADDRESS")
						+" and pq.delivery_address= "+map.get("DELIVERY_ADDRESS")
						+" and pq.truck_type= '"+map.get("truck_type")+" ' "
						+" )A left join currency c on c.id=A.currency_id";
				List<Record> reItem=Db.find(sqlString);
				if(reItem.size()==1){
					TransJobOrderArap tArap=new TransJobOrderArap();
					tArap.set("order_id", id);
					tArap.set("order_type", "charge");
					tArap.set("type", "陆运");
					tArap.set("sp_id", customerId);
					String str1="select id from fin_item where office_id ="+office_id+" and name='运费'";
					List<Record> re1=(List<Record>) Db.find(str1); 
					tArap.set("charge_id", re1.get(0).get("ID"));
					tArap.set("price", reItem.get(0).get("tax_free_freight"));
					tArap.set("amount", 1);
					tArap.set("currency_id", reItem.get(0).get("currency_id"));
					tArap.set("total_amount", reItem.get(0).get("tax_free_freight"));
					tArap.set("exchange_rate", 1.00000);
					tArap.set("currency_total_amount", reItem.get(0).get("tax_free_freight"));
					tArap.set("create_time", new Date());
//					tArap.set("ref_land_id", );
					tArap.save();	
				}	
			}
			
		}
		
		//费用明细，应收应付
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, id, TransJobOrderArap.class, "order_id");
		List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
		DbUtils.handleList(chargeCost_list, id, TransJobOrderArap.class, "order_id");
		//记录结算公司使用历史	
		saveAccoutCompanyQueryHistory(charge_list);
		saveAccoutCompanyQueryHistory(chargeCost_list);
		//记录结算费用使用历史  
		saveFinItemQueryHistory(charge_list);
		saveFinItemQueryHistory(chargeCost_list);
		
		//相关文档
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, id, TransJobOrderDoc.class, "order_id");

		
		
		
		 //费用明细，应收应付
		List<Map<String, String>> charge_template = (ArrayList<Map<String, String>>)dto.get("charge_template");
		List<Map<String, String>> cost_template = (ArrayList<Map<String, String>>)dto.get("cost_template");
		List<Map<String, String>> allCharge_template = (ArrayList<Map<String, String>>)dto.get("allCharge_template");
		List<Map<String, String>> allCost_template = (ArrayList<Map<String, String>>)dto.get("allCost_template");
   		saveArapTemplate(type,customer_id,charge_template,cost_template,allCharge_template,allCost_template);
		long creator = transJobOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = transJobOrder.toRecord();
   		r.set("creator_name", user_name);
	
   		//保存空运填写模板

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
    	
    	String chargeSql = "select parent_id from trans_job_order_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
    	String costSql = "select parent_id from trans_job_order_arap_template where"
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
                Db.save("trans_job_order_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("trans_job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update trans_job_order_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
                Db.save("trans_job_order_arap_template", all);  
                
        		//保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "cost");
                r.set("order_type", order_type);
                r.set("json_value", costObject);
                r.set("parent_id",  all.getLong("id"));
                Db.save("trans_job_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = costRec.getLong("parent_id");
        	Db.update("update trans_job_order_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
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
    public void saveDocFile(){
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
				Db.save("trans_job_order_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    //报关的文档上传

    
    //上传陆运签收文件描述
    @Before(Tx.class)
    public void uploadSignDesc(){
		String id = getPara("id");
		List<UploadFile> fileList = getFiles("doc");
		File file = fileList.get(0).getFile();
		String fileName = file.getName();
		
		Record r = new Record();
		r.set("land_id", id);
		r.set("doc_name", fileName);
		r.set("uploader", LoginUserController.getLoginUserId(this));
		r.set("upload_time", new Date());
		Db.save("trans_job_order_land_doc",r);
		renderJson("{\"result\":true}");
    }
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("trans_job_order_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
    	
    	File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("trans_job_order_doc",r);
            resultMap.put("result", result);
        }else{
        	Db.delete("trans_job_order_doc",r);
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    //删除报关文档

    
    //删除陆运签收文件
    @Before(Tx.class)
    public void deleteSignDesc(){
    	String id = getPara("id");
    	String path = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\";
    	
    	String sql = "select GROUP_CONCAT(doc_name) doc_name from trans_job_order_land_doc where land_id=?";
    	Record r = Db.findFirst(sql, id);
    	String fileName = r.getStr("doc_name");
    	String[] arr = fileName.split(",");
    	for (int i = 0; i < arr.length; i++) {
	    	File file = new File(path+arr[i]);
	    	if (file.exists() && file.isFile()) {
	    		file.delete();
	    		Db.update("delete from trans_job_order_land_doc where land_id=?", id);
	    	}else{
	    		Db.update("delete from trans_job_order_land_doc where land_id=?", id);
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
			Db.update("delete from trans_job_order_land_doc where id = ?", id);
		}else{
			Db.update("delete from trans_job_order_land_doc where id = ?", id);
		}
    	renderJson("{\"result\":true}");
    }

    //返回对象	

    
    //返回list
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	 if("land".equals(type)){
    		itemSql = " select tjol.*,ci.car_no car_no_name,d1.dock_name take_address_name,d2.dock_name delivery_address_name,d3.dock_name loading_wharf1_name,d4.dock_name loading_wharf2_name,"
    				+ " p.abbr transport_company_name,CAST(GROUP_CONCAT(tjold.id) as char ) trans_job_order_land_doc_id, GROUP_CONCAT(tjold.doc_name) doc_name,"
    				+ " p1.abbr consignor_name, p2.abbr consignee_name from trans_job_order_land_item tjol"
    				+ " left join carinfo ci on ci.id=tjol.car_no"
    				+ " left join dockinfo d1 on d1.id=tjol.take_address"
    				+ " left join dockinfo d2 on d2.id=tjol.delivery_address"
    				+ " left join dockinfo d3 on d3.id=tjol.loading_wharf1"
    				+ " left join dockinfo d4 on d4.id=tjol.loading_wharf2"
    				+ " left join party p on p.id=tjol.transport_company"
    				+ " left join party p1 on p1.id=tjol.consignor"
    				+ " left join party p2 on p2.id=tjol.consignee"
    				+ " left join trans_job_order_land_doc tjold on tjold.land_id=tjol.id"
    				+ " where tjol.order_id=? GROUP BY tjol.id order by tjol.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = " select tjor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name ,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from trans_job_order_arap tjor"
    				+ " left join party pr on pr.id=tjor.sp_id"
    				+ " left join fin_item f on f.id=tjor.charge_id"
    				+ " left join unit u on u.id=tjor.unit_id"
    				+ " left join currency c on c.id=tjor.currency_id"
    		        + " left join currency c1 on c1.id=tjor.exchange_currency_id"
    				+ " where tjor.order_id=? and order_type=? order by tjor.id";
    		itemList = Db.find(itemSql, orderId,"charge");
    	}else if("cost".equals(type)){
	    	itemSql = " select tjor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name ,"
	    			+ " c1.name exchange_currency_id_name"
	    			+ " from trans_job_order_arap tjor"
	    			+ "	left join party pr on pr.id=tjor.sp_id"
	    			+ "	left join fin_item f on f.id=tjor.charge_id"
	    			+ "	left join unit u on u.id=tjor.unit_id"
	    			+ " left join currency c on c.id=tjor.currency_id"
    		        + " left join currency c1 on c1.id=tjor.exchange_currency_id"
	    			+ "	where tjor.order_id=? and order_type=? order by tjor.id";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = " select tjod.*,u.c_name from trans_job_order_doc tjod left join user_login u on tjod.uploader=u.id "
	    			+ "	where tjod.order_id=? order by tjod.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from trans_job_order_sendMail where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }
		return itemList;
	}

    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	String str=" select tjo.*,di.dock_name take_wharf_name ,di1.dock_name back_wharf_name from trans_job_order tjo "
					+" LEFT JOIN dockinfo di on di.id=tjo.take_wharf "
					+" LEFT JOIN dockinfo di1 on di1.id=tjo.back_wharf "
					+"  where tjo.id= "+id;
    	Record re= Db.find(str).get(0);
    	setAttr("order", re);
    	//获取空运运明细表信息
    	//获取陆运明细表信息
    	setAttr("landList", getItems(id,"land"));
    	//贸易
    	//报关
    	//保险
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));
    	//邮件记录
    	setAttr("mailList", getItems(id,"mail"));
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	//客户回显
    	Party party = Party.dao.findById(re.get("customer_id"));
    	setAttr("party", party);
    	//头程船公司回显
    	Party head_carrier = Party.dao.findById(re.get("head_carrier"));
    	setAttr("head", head_carrier);
    	//工作单创建人
    	long creator = re.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));
    	//海运头程资料
//   		setAttr("oceanHead", Db.findFirst("select * from job_order_shipment_head where order_id = ?",id));
//   		setAttr("truckHead", Db.findFirst("select * from job_order_land_cabinet_truck where order_id = ?",id)); 
        render("/tms/TransJobOrder/JobOrderEdit.html");
    }
    
    /**
     * 获取应收模板信息
     */
    public void getArapTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from trans_job_order_arap_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    
    //常用邮箱模版
    public List<Record> getEmailTemplateInfo(){
    	List<Record> list = Db.find("select t.* from trans_job_order_sendmail_template t"
                + " where t.creator=?", LoginUserController.getLoginUserId(this));
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
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        if("sowait".equals(type)){
        	sql=" SELECT jor.*,ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        			+ " FROM job_order jor "
        			+ " LEFT JOIN job_order_shipment jos on jor.id = jos.order_id "
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator "
        			+ " WHERE jor.office_id="+office_id
        			+ " and jor.type = '出口柜货' AND jos.SONO IS NULL AND jor.transport_type LIKE '%ocean%'"
        			+ " and jor.delete_flag = 'N'";        	
        }else if("truckorderwait".equals(type)){
        	 sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        			+ " FROM job_order_land_item joli"
        			+ " left join job_order jor on jor.id = joli.order_id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
        			+ " and datediff(joli.eta, now()) <= 3 AND (joli.truckorder_flag != 'Y' OR joli.truckorder_flag IS NULL)"
        			+ " AND jor.transport_type LIKE '%land%'"
        			+ " and jor.delete_flag = 'N'";
        	
        	
        } else if("siwait".equals(type)){
        	 sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        	 		+ " FROM job_order_shipment jos"
        	 		+ " left join job_order jor on jos.order_id = jor.id"
        	 		+ " LEFT JOIN job_order_air joa on joa.order_id = jor.id"
        	 		+ " left join party p on p.id = jor.customer_id"
        	 		+ " left join user_login u on u.id = jor.creator "
        	 		+ " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(jos.export_date)=TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
        	
        } else if("mblwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,jos.export_date) sent_out_time"
        			+ " FROM job_order_shipment jos "
        			+ " left join job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and  jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null)"
                    + " and jor.delete_flag = 'N'";
        	
        } else if("customwait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,ifnull(jos.export_date,joa.export_date) sent_out_time "
        			+ " from job_order jor "
        			+ " LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " where jor.office_id="+office_id
                    + "  and  jor.transport_type LIKE '%custom%'"
        			+ "  and ifnull(joc.customs_broker,'') = ''"
        			+ " and jor.delete_flag = 'N'";
        	
        } else if("insurancewait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        			+ " FROM job_order jor LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and  jor.transport_type LIKE '%insurance%' and joi.insure_no is NULL"
                    + " and jor.delete_flag = 'N'";
        } else if("overseacustomwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        			+ " FROM job_order_shipment jos "
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jo.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y' "
        			+ " and timediff(now(),jos.etd)<TIME('48:00:00') "
        			+ " and jor.delete_flag = 'N'";
        } else if("tlxOrderwait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, ifnull(jos.export_date,joa.export_date) sent_out_time"
        			+ " FROM job_order_shipment jos"
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " LEFT JOIN job_order_air joa on joa.order_id = jo.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(jos.etd)= TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
        }
        else{
		         sql = "SELECT * from (select tjo.*,tjo.land_export_date sent_out_time,"
		         		+ " ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p.company_name,p.code customer_code"
		         		+ "	from trans_job_order tjo"
		         		+ "	left join party p on p.id = tjo.customer_id"
		         		+ "	left join user_login u on u.id = tjo.creator"
		         		+ "	where tjo.office_id="+office_id
		         		+ "	and tjo.delete_flag = 'N'"
		         		+ "	) A where 1 = 1 ";
         }
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
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
    	TransJobOrder order = TransJobOrder.dao.findById(id);
    	order.set("status", "已完成");
    	order.update();
    	renderJson("{\"result\":true}");
    }
    
    //费用应收打印PDF前保存
    @Before(Tx.class)
    public void saveDebitNote(){
    	String ids = getPara("itemIds");
    	String[] idArr = ids.split(",");
    	String invoiceNo = getPara("invoiceNo");
    	TransJobOrderArap order = null;
    	//checkbox选中的几条发票号一样
    	for(int i=0;i<idArr.length;i++){
    		order = TransJobOrderArap.dao.findById(idArr[i]);
    		order.set("invoice_no", invoiceNo);
    		order.update();
    	}
    	renderJson("{\"result\":true}");
    }
    
    //删除海运常用信息模版

    //删除空运常用信息模版

    //删除费用明细常用信息模版
    @Before(Tx.class)
    public void deleteArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trans_job_order_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
    //删除邮箱常用模版
    @Before(Tx.class)
    public void deleteEmailTemplate(){
    	String id = getPara("id");
    	Db.update("delete from trans_job_order_sendmail_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //费用明细确认
    @Before(Tx.class)
    public void feeConfirm(){
		String id = getPara("id");
			TransJobOrderArap tjoa = TransJobOrderArap.dao.findFirst("select * from trans_job_order_arap where id = ?",id);
			tjoa.set("audit_flag", "Y");
			tjoa.update();
			renderJson(tjoa);
	 }
    
    @Before(Tx.class)
    public void updateShare(){
    	String item_id = getPara("item_id");
    	String check = getPara("check");
    	String order_id = getPara("order_id");
    	
    	if(StringUtils.isEmpty(item_id)){//全选
    		Db.update("update job_order_custom_doc set share_flag =? where order_id = ?",check,order_id);
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
    	Db.update("update trans_job_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
    			+ " delete_reason='"+delete_reason+"' where id = ?  ",id);
    	renderJson("{\"result\":true}");
    }
    

}
