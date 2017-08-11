package controllers.oms.bookingOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
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
import models.eeda.cms.CustomPlanOrder;
import models.eeda.oms.bookOrder.BookOrder;
import models.eeda.oms.bookOrder.BookOrderArap;
import models.eeda.oms.bookOrder.BookOrderDoc;
import models.eeda.oms.bookOrder.BookOrderSendMail;
import models.eeda.oms.bookOrder.BookOrderSendMailTemplate;
import models.eeda.oms.bookOrder.BookingOrder;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.tms.TransJobOrder;
import models.eeda.tr.tradeJoborder.TradeJobOrder;

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
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.FileUploadUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class BookingOrderController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(BookingOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type = getPara("type");
		setAttr("type",type);
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/bookOrder");
        setAttr("listConfigList", configList);
		render("/oms/bookingOrder/bookingOrderList.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
		
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		Office office = Office.dao.findById(office_id);
   		setAttr("office", office);
   		
   		Record re = Db.findFirst("select * from party where type='SP' and ref_office_id is not null and office_id = ?",office_id);
   		if(re!=null){
   			Long ref_office_id = re.getLong("ref_office_id");
   	   		Office ref_office = Office.dao.findById(ref_office_id);
   	   		setAttr("ref_office", ref_office);
   		}
   		
   		render("/oms/bookingOrder/bookingOrderEdit.html");
        
    }
	
	private Record getDetail(String id,String type){
		Record re = null;
		String sql ="";
		if("ocean".equals(type)){
			sql =" SELECT bod.*,p.abbr ocean_agent_name,p.ref_office_id ocean_agent_ref_office_id,l1.name pol_name,"
					+ " l2.name pod_name,p1.abbr carrier_name"
					+ " from booking_ocean_detail bod"
					+ " left join party p on p.id = bod.ocean_agent"
					+ " left join party p1 on p1.id = bod.carrier"
					+ " LEFT JOIN location l1 on l1.id = bod.pol_id"
					+ " LEFT JOIN location l2 on l2.id = bod.pod_id"
					+ " WHERE order_id = ?";
			re = Db.findFirst(sql,id);
		}else if("air".equals(type)){
			sql ="SELECT bad.*,p.abbr air_agent_name,p.ref_office_id air_agent_ref_office_id,l1.name air_pol_name,"
					+ " l2.name air_pod_name,p1.abbr  air_company_name"
					+ " from booking_air_detail bad"
					+ " LEFT JOIN party p on p.id = bad.air_agent"
					+ " LEFT JOIN party p1 on p1.id = bad.air_company"
					+ " LEFT JOIN location l1 on l1.id = bad.air_pol_id"
					+ " LEFT JOIN location l2 on l2.id = bad.air_pod_id"
					+ " WHERE order_id = ?";
			re = Db.findFirst(sql,id);
		}else if("land".equals(type)){
			sql ="SELECT bld.*,p.abbr land_take_agent_name,p.ref_office_id land_take_agent_ref_office_id,"
					+ " p1.abbr  land_delivery_agent_name,p1.ref_office_id land_delivery_agent_ref_office_id"
					+ " from booking_land_detail bld"
					+ " LEFT JOIN party p on p.id = bld.land_take_agent"
					+ " LEFT JOIN party p1 on p1.id = bld.land_delivery_agent"
					+ " WHERE order_id = ?";
			re = Db.findFirst(sql,id);
		}else if("custom".equals(type)){
			sql ="SELECT bcd.*,p.abbr custom_broker_name,p.ref_office_id custom_broker_ref_office_id "
					+ " from booking_custom_detail bcd"
					+ " LEFT JOIN party p on p.id = bcd.custom_broker"
					+ " WHERE order_id =?";
			re = Db.findFirst(sql,id);
		}
		return re;
	}
    

    
    //根据工作单类型生成不同前缀
    public String generateBookPrefix(String type){
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
        String id =(String) dto.get("id");
        
        BookingOrder bookingOrder = new BookingOrder();
        
        //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");

        String newDateStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
        Date date=new Date();
		newDateStr=sdf.format(date);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			bookingOrder = BookingOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, bookingOrder);
   			bookingOrder.set("updator", user.getLong("id"));
            bookingOrder.set("update_stamp", new Date());
            
   			bookingOrder.update();
   		} else {
   			//create
   			DbUtils.setModelValues(dto, bookingOrder);
   			//需后台处理的字段
   			String order_no = OrderNoGenerator.getNextOrderNo("BK", newDateStr, office_id);
            bookingOrder.set("booking_no", order_no);
   			bookingOrder.set("creator", user.getLong("id"));
   			bookingOrder.set("create_stamp", new Date());
   			bookingOrder.set("updator", user.getLong("id"));
            bookingOrder.set("update_stamp", new Date());
   			bookingOrder.set("office_id", office_id);
   			bookingOrder.save();
   			id = bookingOrder.getLong("id").toString();
   		}
//   		long customerId = Long.valueOf(dto.get("customer_id").toString());
//   		saveCustomerQueryHistory(customerId);
	

//		//记录结算费用使用历史  
//		saveFinItemQueryHistory(charge_list);
//		saveFinItemQueryHistory(chargeCost_list);
		
   		//海运信息写入表
		List<Map<String, String>> ocean_detail = (ArrayList<Map<String, String>>)dto.get("ocean_detail");
		DbUtils.handleList(ocean_detail, "booking_ocean_detail", id, "order_id");
		
		//空运信息写入表
		List<Map<String, String>> air_detail = (ArrayList<Map<String, String>>)dto.get("air_detail");
		DbUtils.handleList(air_detail, "booking_air_detail", id, "order_id");
		
		//陆运信息写入表
		List<Map<String, String>> land_detail = (ArrayList<Map<String, String>>)dto.get("land_detail");
		DbUtils.handleList(land_detail, "booking_land_detail", id, "order_id");
		
		//报关信息写入表
		List<Map<String, String>> custom_detail = (ArrayList<Map<String, String>>)dto.get("custom_detail");
		DbUtils.handleList(custom_detail, "booking_custom_detail", id, "order_id");
   		
   		
		//相关文档
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, id, BookOrderDoc.class, "order_id");

		long creator = bookingOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = bookingOrder.toRecord();
		r.set("creator_name", user_name);
		r.set("ocean", getDetail(id,"ocean"));
		r.set("air", getDetail(id,"air"));
		r.set("land", getDetail(id,"land"));
		r.set("custom", getDetail(id,"custom"));
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
    	
    	String chargeSql = "select parent_id from book_order_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
    	String costSql = "select parent_id from book_order_arap_template where"
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
                Db.save("book_order_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("book_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update book_order_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
                Db.save("book_order_arap_template", all);  
                
        		//保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "cost");
                r.set("order_type", order_type);
                r.set("json_value", costObject);
                r.set("parent_id",  all.getLong("id"));
                Db.save("book_order_arap_template", r);  
       		}
        }else{
        	Long parent_id = costRec.getLong("parent_id");
        	Db.update("update book_order_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
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
    	
    	String chargeSql = "select parent_id from book_order_land_arap_template where"
                + " arap_type = 'charge' and creator_id = "+creator_id+" and customer_id = "+customer_id+" and order_type = '"+order_type+"' "
                + " and  json_value = '"+chargeObject+"' and parent_id is not null";
//    	String costSql = "select parent_id from book_order_land_arap_template where"
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
                Db.save("book_order_land_arap_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("book_order_land_arap_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update book_order_land_arap_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
//                Db.save("book_order_land_arap_template", all);  
//                
//        		//保存局部信息
//        		Record r= new Record();
//                r.set("creator_id", creator_id);
//                r.set("customer_id", customer_id);
//                r.set("arap_type", "cost");
//                r.set("order_type", order_type);
//                r.set("json_value", costObject);
//                r.set("parent_id",  all.getLong("id"));
//                Db.save("book_order_land_arap_template", r);  
//       		}
//        }else{
//        	Long parent_id = costRec.getLong("parent_id");
//        	Db.update("update book_order_land_arap_template set json_value = ? where id = ?",costObjectAll,parent_id);
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
    	
    	String chargeSql = "select parent_id from book_order_trade_service_template where"
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
                Db.save("book_order_trade_service_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("book_order_trade_service_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update book_order_trade_service_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
    	
    	String chargeSql = "select parent_id from book_order_trade_sale_template where"
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
                Db.save("book_order_trade_sale_template", all);  
        		
                //保存局部信息
        		Record r= new Record();
                r.set("creator_id", creator_id);
                r.set("customer_id", customer_id);
                r.set("arap_type", "charge");
                r.set("order_type", order_type);
                r.set("json_value", chargeObject);
                r.set("parent_id", all.getLong("id"));
                Db.save("book_order_trade_sale_template", r);  
       		}
        }else{
        	Long parent_id = chargeRec.getLong("parent_id");
        	Db.update("update book_order_trade_sale_template set json_value = ? where id = ?",chargeObjectAll,parent_id);
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
    	BookOrderSendMailTemplate order = new BookOrderSendMailTemplate();
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
        
        String sql = "select 1 from book_order_ocean_template where"
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
            Db.save("book_order_ocean_template", r);
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
    	String sql = "select 1 from book_order_air_template where"
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
    		Db.save("book_order_air_template", r);
    	}
    }
    
    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile() throws Exception{
    	try {
            String order_id = getPara("order_id");
            List<UploadFile> fileList = getFiles("doc");
            String type = getPara("type");
            Long userId = LoginUserController.getLoginUserId(this);
            
            uploadFile(fileList, order_id, userId, type , "book_order_doc", false);
            
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
            
            FileUploadUtil.uploadFile(fileList, order_id, userId, "book_order_custom_doc", false);
            
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
            
            FileUploadUtil.uploadFile(fileList, id, userId, "book_order_land_doc", false);
            
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
    	BookOrderDoc bookOrderDoc = BookOrderDoc.dao.findById(id);
    	String fileName = bookOrderDoc.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            bookOrderDoc.delete();
            resultMap.put("result", result);
        }else{
        	bookOrderDoc.delete();
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    //删除报关文档
    @Before(Tx.class)
    public void deleteCustomDoc(){
    	String id = getPara("id");
    	Record r = Db.findById("book_order_custom_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
    	File file = new File(filePath);
    	if (file.exists() && file.isFile()) {
    		boolean result = file.delete();
    		Db.delete("book_order_custom_doc",r);
    		resultMap.put("result", result);
    	}else{
    		Db.delete("book_order_custom_doc", r);
    		resultMap.put("result", "文件不存在可能已被删除!");
    	}
    	renderJson(resultMap);
    }
    
    //删除陆运签收文件
    @Before(Tx.class)
    public void deleteSignDesc(){
    	String id = getPara("id");
    	String path = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\";
    	
    	String sql = "select GROUP_CONCAT(doc_name) doc_name from book_order_land_doc where land_id=?";
    	Record r = Db.findFirst(sql, id);
    	String fileName = r.getStr("doc_name");
    	String[] arr = fileName.split(",");
    	for (int i = 0; i < arr.length; i++) {
	    	File file = new File(path+arr[i]);
	    	if (file.exists() && file.isFile()) {
	    		file.delete();
	    		Db.update("delete from book_order_land_doc where land_id=?", id);
	    	}else{
	    		Db.update("delete from book_order_land_doc where land_id=?", id);
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
			Db.update("delete from book_order_land_doc where id = ?", id);
		}else{
			Db.update("delete from book_order_land_doc where id = ?", id);
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
    				+ " lo.name por_name,lo1.name pol_name,lo2.name pod_name, lo3.name fnd_name,lo4.name hub_name"
    				+ " from book_order_shipment jos "
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
    		re = Db.findFirst("select * from book_order_insurance joi where order_id = ?",id);
    	}else if("express".equals(type)){
    		re = Db.findFirst("select * from book_order_express joe where order_id = ?",id);
    	}else if("air".equals(type)){
    		re = Db.findFirst("select joa.* ,p1.abbr shipperAbbr,p2.abbr consigneeAbbr,p3.abbr notify_partyAbbr,p4.abbr booking_agent_name from book_order_air joa"
    				+ " left join party p1 on p1.id=joa.shipper"
    				+ " left join party p2 on p2.id=joa.consignee"
    				+ " left join party p3 on p3.id=joa.notify_party"
    				+ " left join party p4 on p4.id=joa.booking_agent"
    				+ " where order_id=?", id);
    	}else if("trade".equals(type)){
	    	re = Db.findFirst("select j.*,p.abbr cost_company_name, c.name cost_currency_name from book_order_trade j "
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
    		itemSql = "select jos.*,CONCAT(u.name,u.name_eng) unit_name from book_order_shipment_item jos"
    				+ " left join unit u on u.id=jos.unit_id"
    				+ " where order_id=? order by jos.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("air".equals(type)){
    		itemSql = "select joa.*, pa.abbr air_company_name from book_order_air_item joa"
    		        + " left join party pa on pa.id=joa.air_company"
    		        + " where order_id=? order by joa.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("cargoDesc".equals(type)){
    		itemSql = "select * from book_order_air_cargodesc where order_id=? order by id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("land".equals(type)){
    		itemSql = "select jol.*, p.abbr transport_company_name,CAST(GROUP_CONCAT(jold.id) as char ) book_order_land_doc_id, GROUP_CONCAT(jold.doc_name) doc_name,"
    		        + " p1.abbr consignor_name, p2.abbr consignee_name, CONCAT(u.name,u.name_eng) unit_name "
    		        + " from book_order_land_item jol "
    				+ " left join party p on p.id=jol.transport_company"
    				+ " left join party p1 on p1.id=jol.consignor"
    				+ " left join party p2 on p2.id=jol.consignee"
    				+ " left join book_order_land_doc jold on jold.land_id=jol.id"
    				+ " left join unit u on u.id=jol.unit_id"
    				+ " where order_id=? GROUP BY jol.id order by jol.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from book_order_arap jor "
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
	    			+ " from book_order_arap jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
	    	        + " where order_id=? and order_type=? order by jor.id";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from book_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? order by jod.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from book_order_sendMail where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_cost".equals(type)){
	    	itemSql = "select jotc.*,p.abbr sp_name,c.name currency_name,ifnull(ti.commodity_name,jotc.commodity_name) commodity_name from book_order_trade_cost jotc"
	    			+ " left join party p on p.id = jotc.sp"
	    			+ "	left join trade_item ti on ti.id = jotc.commodity_id"
	    			+ " left join currency c on c.id = jotc.custom_currency"
	    			+ " where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("trade_sale".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name, f.name_eng charge_name_eng, u.name unit_name, c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from book_order_arap jor "
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
    				+ " from book_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where jor.order_id=? and jor.order_type=? and jor.trade_fee_flag=? order by jor.id";
    		itemList = Db.find(itemSql, orderId,"charge","trade_service_fee");
	    }else if("china_self".equals(type)){
	    	itemSql = "select j.*,p.abbr custom_bank_name from book_order_custom_china_self_item j"
	    			+ " left join party p on p.id = j.custom_bank"
	    			+ " where j.order_id=? order by j.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("custom_doc".equals(type)){
//	    	itemSql = "select jod.*,u.c_name from book_order_custom_doc jod left join user_login u on jod.uploader=u.id "
//	    			+ " where order_id=? order by jod.id";
	        itemSql = "select cpo.ref_job_order_id, jocd.id,jocd.doc_name,jocd.upload_time,jocd.remark,"
	                + " ul.c_name c_name,jocd.uploader, jocd.share_flag ,null share_flag from book_order_custom_doc jocd"
                    + " LEFT JOIN user_login ul on ul.id = jocd.uploader"
                    + " LEFT JOIN custom_plan_order cpo on cpo.ref_job_order_id = jocd.order_id and jocd.share_flag = 'Y'"
                    + " where jocd.order_id =?  and cpo.delete_flag='N' "
                    + " union all"
                    + " select cpo.ref_job_order_id, null id ,jod.doc_name,jod.upload_time,jod.remark,u.c_name c_name,"
                    + " jod.uploader,null share_flag, jod.cms_share_flag"
                    + " from custom_plan_order_doc jod "
                    + " left join custom_plan_order cpo on cpo.id = jod.order_id"
                    + " left join user_login u on jod.uploader=u.id "
                    + " where cpo.ref_job_order_id=?"
                    +" and cpo.delete_flag='N' "
       			 + "";
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
	    			+ " WHERE cjo.ref_job_order_id = ?  and cjo.delete_flag='N' ",orderId);
	    }
		return itemList;
	}

    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	BookingOrder bookingOrder = BookingOrder.dao.findFirst(
    			" SELECT border.*,p1.abbr consignor_name,p2.abbr consignee_name,p3.abbr notify_name,"
    			+ " p4.abbr entrust_name,p4.ref_office_id entrust_ref_office_id,o.office_name,ul.c_name creator_name,CONCAT(ut.name,ut.name_eng) order_unit_input"
    			+ " from booking_order border "
    			+ " LEFT JOIN office o on o.id =  border.office_id "
    			+ " LEFT JOIN unit ut on ut.id =  border.order_unit "
    			+ " LEFT JOIN party p1 on p1.id = border.consignor"
    			+ " LEFT JOIN party p2 on p2.id = border.consignee"
    			+ " LEFT JOIN party p3 on p3.id = border.notify"
    			+ " LEFT JOIN party p4 on p4.id = border.entrust"
    			+ " LEFT JOIN user_login ul on ul.id = border.creator"
    			+ "  WHERE border.id = ?",id);
//    	Long plan_order_id = bookOrder.getLong("plan_item_id");
    	
    	String sqlString=" SELECT *,IFNULL(vessel_voyage,air_flight_no_voyage_no)vessel_voyage_or_flight_no_voyage_no "
    			+ " ,IFNULL(abbr,air_company) carrier_or_air_company FROM ("
    			+ " SELECT "
    			+" 	jo.id,jo.order_no,jo.order_export_date,jo.pieces,jo.gross_weight, "
    			+" 	jo.volume, "
    			+" 	CONCAT(lo1. NAME, ' -', lo1. CODE) pol_name, "
    			+" 	CONCAT(lo2. NAME, ' -', lo2. CODE) pod_name, "
    			+ " CONCAT_WS('-',jos.vessel,jos.voyage) vessel_voyage,"
    			+" 	p9.abbr HBLconsignee_name,p4.abbr, "
    			+" 	IFNULL(CONCAT_WS('-',jos.vessel,jos.voyage), "
    			+" 			GROUP_CONCAT(CONCAT_WS('-',joai.flight_no,joai.voyage_no) )) vessel_voyage_or_air_info, "
    			+ " (SELECT GROUP_CONCAT(pa.abbr )  FROM  job_order_air_item joai2 "
    			+ "								left join party pa on pa.id=joai2.air_company "
    			+ "		        		 WHERE joai2.order_id =jo.id ) air_company, "
    			+" 	(SELECT GROUP_CONCAT(CONCAT_WS('-',joai2.flight_no,joai2.voyage_no) )  FROM  job_order_air_item joai2  "
    			+" 		        		 WHERE joai2.order_id =jo.id ) air_flight_no_voyage_no, "
    			+" 	(SELECT GROUP_CONCAT(josi2.container_no )  FROM  job_order_shipment_item josi2  "
    			+" 									 WHERE josi2.order_id =jo.id ) ship_container_no, "
    			+" 	(SELECT GROUP_CONCAT(josi2.seal_no )  FROM  job_order_shipment_item josi2  "
    			+" 									 WHERE josi2.order_id =jo.id ) ship_seal_no, "
    			+" 	jos.closing_date,jos.etd,jos.eta, "
    			+" 	(SELECT GROUP_CONCAT(joli2.take_address )  FROM  job_order_land_item joli2  "
    			+" 									 WHERE joli2.order_id =jo.id ) take_address, "
    			+" 	(SELECT GROUP_CONCAT(joli2.truck_type )  FROM  job_order_land_item joli2  "
    			+" 									 WHERE joli2.order_id =jo.id ) truck_type, "
    			+" 		(SELECT COUNT(truck_type)  FROM  job_order_land_item joli2  "
    			+" 									 WHERE joli2.order_id =jo.id  ) truck_count "
    			+" 	FROM "
    			+" 		job_order jo "
    			+" LEFT JOIN job_order_shipment jos ON jos.order_id = jo.id "
    			+" LEFT JOIN job_order_air_item joai ON joai.order_id = jo.id "
    			+" left join party p4 on p4.id=jos.carrier "
    			+" left join party p9 on p9.id= jos.HBLconsignee "
    			+" LEFT JOIN job_order_shipment_item josi on josi.order_id = jo.id "
    			+" LEFT JOIN job_order_land_item joli on joli.order_id = jo.id "
    			+" LEFT JOIN location lo1 ON lo1.id = jos.pol "
    			+" LEFT JOIN location lo2 ON lo2.id = jos.pod "
    			+" WHERE  "
    			+" 	jo.plan_order_item_id = '' "//+plan_order_id
    			+"  AND jo.delete_flag = 'N' "
    			+ " )B";
    	Record re = Db.findFirst(sqlString);
    	if(re != null){
    		setAttr("jobOrder", re);
    	
	    	//获取预约到达时间
	    	Long job_order_id = re.getLong("id");
	    	Record jobland = Db.findFirst("select * from job_order_land_item where order_id = ?",job_order_id);
	    	if(jobland!=null){
	    		if(jobland.get("eta")==null){
	        		Record joblandcab = Db.findFirst("select * from job_order_land_cabinet_truck where order_id = ?",job_order_id);
	        		if(joblandcab!=null){
	        			if(joblandcab.get("cabinet_arrive_date") != null&&joblandcab.get("cabinet_arrive_date")!=""){
		        			String cabinet_arrive_date = (joblandcab.get("cabinet_arrive_date")).toString();
		        			setAttr("job_eta", cabinet_arrive_date.substring(0,cabinet_arrive_date.length()-2));
		        		}
	        		}
	    		}else{
	    			String eta = jobland.get("eta").toString();
	    			
	        		setAttr("job_eta", eta.substring(0,eta.length()-2));
	        	}
	    	}
	    	
	    	
	    	Record jobcustom = Db.findFirst("select * from job_order_custom_china_self_item where order_id = ?",job_order_id);
	    	if(jobcustom != null){
	    		String custom_order_no = jobcustom.getStr("custom_order_no");
	    		String status = jobcustom.getStr("status");
	    		setAttr("job_custom_order_no", custom_order_no);
	    		setAttr("job_status", status);
	    	}
	    	
	    	//上船时间
	    	Record jobShip = Db.findFirst("select * from job_order_shipment where order_id = ?",job_order_id);
	    	if(jobShip != null){
	    		if(jobShip.get("atd")!=null){
	    			String atd = jobShip.get("atd").toString();
	    			setAttr("job_atd", atd);
	    		}
	    		
	    		if(jobShip.get("ata")!=null){
	    			String ata = jobShip.get("ata").toString();
	    			setAttr("job_ata", ata);
	    		}
	    	}
    	}
//    	Record re = Db.findFirst("");
    	
    	String create_stamp = bookingOrder.get("create_stamp").toString();
    	bookingOrder.set("create_stamp",create_stamp.substring(0, create_stamp.length()-2));
    	setAttr("order", bookingOrder);
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));
    	setAttr("zeroDocList", getDocItems(id,"zero"));
    	setAttr("oneDocList", getDocItems(id,"one"));
    	setAttr("twoDocList", getDocItems(id,"two"));
    	setAttr("threeDocList", getDocItems(id,"three"));
    	setAttr("fourDocList", getDocItems(id,"four"));
    	//海运信息回显
    	setAttr("ocean", getDetail(id,"ocean"));
    	//空运信息回显
    	setAttr("air", getDetail(id,"air"));
    	//陆运信息回显
    	setAttr("land", getDetail(id,"land"));
    	//报关信息回显
    	setAttr("custom", getDetail(id,"custom"));
    	//邮件记录
    	setAttr("mailList", getItems(id,"mail"));
    	setAttr("emailTemplateInfo", getEmailTemplateInfo());
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));

    	  
        render("/oms/bookingOrder/bookingOrderEdit.html");
    }
    
    //常用邮箱模版
    public List<Record> getEmailTemplateInfo(){
    	List<Record> list = Db.find("select t.* from book_order_sendmail_template t"
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
    	List<Record> list = Db.find("select * from book_order_arap_template "
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
    	List<Record> list = Db.find("select * from book_order_land_arap_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    //常用贸易信息
    public void getTradeServiceTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from book_order_trade_service_template "
    			+ " where creator_id =? and customer_id = ? and order_type = ? and arap_type = ? and parent_id is null"
    			+ " order by id", LoginUserController.getLoginUserId(this),customer_id,order_type,arap_type);
    	renderJson(list);
    }
    public void getTradeSaleTemplate(){
    	String order_type = getPara("order_type");
    	String customer_id = getPara("customer_id");
    	String arap_type = getPara("arap_type");
    	List<Record> list = Db.find("select * from book_order_trade_sale_template "
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
                + " lo.name por_name,lo1.name pol_name,lo2.name pod_name, lo3.name fnd_name from book_order_ocean_template t "
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
    			+ " p7.abbr booking_agent_name from book_order_air_template t "
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
        	BookOrderSendMail jsm = new BookOrderSendMail();
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
        
        Office office = Office.dao.findById(office_id);
        
        String forwarderTodo = "(CASE"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='zero')=0 "
                + "     THEN '头程资料待生成'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='one' and jod0.send_status='已发送')>0"
                + "     THEN 'packing资料待下载'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='two')=0 "
                + "     THEN 'B/L文件待生成'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='three' and jod0.send_status='已发送')>0"
                + "     THEN '盖章保函资料待下载'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='four')=0"
                + "     THEN '盖章HBL资料待上传'"
                + " end"
                + " ) to_do";
        
        String factoryTodo = "(CASE"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='zero' and jod0.send_status='已发送')>0 "
                + "     THEN '头程资料待下载'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='one')=0 "
                + "     THEN 'packing资料待上传'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='two' and jod0.send_status='已发送')>0 "
                + "     THEN 'B/L文件待下载'"
                + " WHEN  (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='three')=0 "
                + "     THEN '盖章保函资料待上传'"
                + " WHEN (SELECT count(jod0.id) FROM book_order_doc jod0 WHERE jod0.order_id =bo.id and jod0.type='four' and jod0.send_status='已发送')>0 "
                + "     THEN '盖章HBL待下载'"
                + " end"
                + " ) to_do";
        
        sql = "SELECT * from (select bo.*,"
     		+ " ifnull(u.c_name, u.user_name) creator_name,o.office_name sp_name,"
     		+ " (SELECT  count(jod0.id) FROM book_order_doc jod0 WHERE  jod0.order_id =bo.id and (jod0.type='zero' or jod0.type='two' or jod0.type='four')  and   jod0.send_status='已发送' ) new_count,"
     		+ " (CASE"
     		+ " WHEN jos.ata is not null"
     		+ " THEN '已到港'"
     		+ " WHEN jos.atd is not null"
     		+ " THEN '已上船'"
     		+ " WHEN (jocc.custom_order_no is not null or jocc.status is not null)"
     		+ " THEN '已报关'"
     		+ " WHEN joli.eta is not null"
     		+ " THEN '已派车'"
     		+ " else"
     		+ " '新建'"
     		+ " end"
     		+ " ) order_status, "
     		+ (office.getStr("type").equals("customer")?factoryTodo:forwarderTodo)
     		+ "	from booking_order bo"
     		+ " LEFT JOIN job_order jor on jor.plan_order_item_id = bo.plan_item_id"
     		+ " LEFT JOIN job_order_land_item joli on joli.order_id = jor.id"
     		+ " LEFT JOIN job_order_custom_china_self_item jocc on jocc.order_id = jor.id"
     		+ " LEFT JOIN job_order_shipment jos on jos.order_id = jor.id "
     		+ "	LEFT JOIN plan_order po on po.id = bo.plan_order_id"
     		+ "	LEFT JOIN office o on o.id = po.to_entrusted_id"
//     		+ " left join office oe1 on oe1.id  = bo.ref_office_id"
     		+ "	left join user_login u on u.id = bo.creator"
     		+ "	where bo.office_id="+office_id
     	    + " and bo.delete_flag = 'N'"
     	    + " group by bo.id"
     	    + " ) A where 1 = 1 ";
         
        
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
    				+ " from book_order_arap jor "
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
    	BookOrder order = BookOrder.dao.findById(id);
    	order.set("status", "已完成");
    	order.update();
    	renderJson("{\"result\":true}");
    }
    
    //费用应收打印debite_note PDF前保存
    @Before(Tx.class)
    public void saveDebitNote(){
    	String ids = getPara("itemIds");
    	String invoiceNo = getPara("invoiceNo");
    	Db.update("update book_order_arap set invoice_no ='"+invoiceNo+"' where id in ("+ids+")");
    	renderJson("{\"result\":true}");
    }
    
    //陆运打印Invoice(分单)前保存hbl_no
    @Before(Tx.class)
    public void saveDebitNoteOfLand(){
    	String ids = getPara("landIds");
    	String invoice_land_hbl_no = getPara("invoice_land_hbl_no");
    	String land_ref_no = getPara("land_ref_no");
    	Db.update("update book_order_arap set invoice_land_hbl_no='"+invoice_land_hbl_no+"',land_ref_no='"+land_ref_no+"'  where land_item_id in ("+ids+")");
    	
     	renderJson("{\"result\":true}");
    }
    
    //删除费用明细常用信息模版
    @Before(Tx.class)
    public void deleteArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
    //删除陆运费用明细常用信息模版
    @Before(Tx.class)
    public void deleteLandArapTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_land_arap_template where id = ? or parent_id = ?",id,id);
    	renderJson("{\"result\":true}");
    }
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeSaleTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_trade_sale_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeServiceTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_trade_service_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除海运常用信息模版
    @Before(Tx.class)
    public void deleteOceanTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_ocean_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除空运常用信息模版
    @Before(Tx.class)
    public void deleteAirTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_air_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    //删除邮箱常用模版
    @Before(Tx.class)
    public void deleteEmailTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_sendmail_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //费用明细确认
    @Before(Tx.class)
    public void feeConfirm(){
		String id = getPara("id");
		if (id != null) {
        	BookOrderArap joa = BookOrderArap.dao.findFirst("select * from book_order_arap where id = ?",id);
           		joa.set("audit_flag", "Y");
        	   	joa.update();
        }
		//Db.update("update book_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from book_order_arap where id = ?",id);
		renderJson(re);
	 }
  //费用明细取消确认，
    @Before(Tx.class)
    public void feeCancelConfirm(){
		String id = getPara("id");
		if (id != null) {
        	BookOrderArap joa = BookOrderArap.dao.findFirst("select * from book_order_arap where id = ?",id);
        	if( joa.get("audit_flag").equals("Y")&&joa.get("bill_flag").equals("N")){
        		joa.set("audit_flag", "N");
        	}
        	joa.update();
        }
		//Db.update("update book_order_arap set audit_flag = 'Y' where id = ?", id);
		Record re = Db.findFirst("select * from book_order_arap where id = ?",id);
		renderJson(re);
	 }
    
    @Before(Tx.class)
    public void updateShare(){
    	String item_id = getPara("item_id");
    	String check = getPara("check");
    	String order_id = getPara("order_id");
    	
    	if(StringUtils.isEmpty(item_id)){//全选
    		Db.update("update book_order_custom_doc set share_flag =? where order_id = ?",check,order_id);
    	}else{//单选
    		Db.update("update book_order_custom_doc set share_flag =? where id = ?",check,item_id);
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
    	Db.update("update book_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
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
        Model<?> model = (Model<?>) BookOrderArap.class.newInstance();
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
   			Db.update("update book_order_custom_doc set new_flag ='N' where id = ?",id);
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
    
    //确认发送文档
    @Before(Tx.class)
    public void confirmSend(){
    	String id = getPara("docId");
    	String job_order_id = getPara("job_order_id");
    	BookOrderDoc bookOrderDoc = BookOrderDoc.dao.findById(id);
    	bookOrderDoc.set("sender", LoginUserController.getLoginUserId(this));
    	bookOrderDoc.set("send_time", new Date());
    	bookOrderDoc.set("send_status", "已发送");
    	bookOrderDoc.update();
    	
    	Record jobDoc = new Record();
    	jobDoc.set("order_id", job_order_id);
    	jobDoc.set("type", bookOrderDoc.getStr("type"));
    	jobDoc.set("uploader", bookOrderDoc.getLong("uploader"));
    	jobDoc.set("doc_name", bookOrderDoc.getStr("doc_name"));
    	jobDoc.set("upload_time", bookOrderDoc.get("upload_time"));
    	jobDoc.set("remark", bookOrderDoc.getStr("remark"));
    	jobDoc.set("sender", bookOrderDoc.getLong("sender"));
    	jobDoc.set("send_time", bookOrderDoc.get("send_time"));
    	jobDoc.set("send_status", bookOrderDoc.getStr("send_status"));
    	jobDoc.set("ref_doc_id", id);
    	Db.save("job_order_doc", jobDoc);
      
        renderJson(bookOrderDoc);
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
    
    
    public List<Record> getDocItems(String orderId,String type){
    	String  itemSql = "";
    	itemSql = "SELECT * ,"
    			+ " (SELECT  count(jod0.id) FROM book_order_doc jod0 WHERE "
    			+ " jod0.order_id ="+orderId+" 	AND jod0.type ='"+type+"' and   jod0.send_status='已发送' ) new_count"
    			+ " FROM("
    			+ " select jod.*,u.c_name,u1.c_name sender_name "
    			+ " from book_order_doc jod "
    			+ " left join user_login u on jod.uploader=u.id "
    			+ " LEFT JOIN user_login u1 ON jod.SENDER = u1.id "
        			+ " where jod.order_id="+orderId+" and jod.type='"+type+"' order by jod.id"
        			+ ")B WHERE 1=1 ";
    	
    	List<Record> itemList = Db.find(itemSql);
    	
    	return itemList;
    }
    
    //文件下载
    @Before(Tx.class)
    public void downloadDoc(){
    	String id = getPara("docId");
    	BookOrderDoc order = BookOrderDoc.dao.findById(id);
    	order.set("receiver", LoginUserController.getLoginUserId(this));
    	order.set("receive_time", new Date());
    	order.set("send_status", "已接收");
    	order.update();
    	
    	Long ref_doc_id = order.getLong("ref_doc_id");
    	JobOrderDoc refOrder = JobOrderDoc.dao.findById(ref_doc_id);
    	if(refOrder!=null){
    		refOrder.set("receiver", LoginUserController.getLoginUserId(this));
    		refOrder.set("receive_time", new Date());
    		refOrder.set("send_status", "已接收");
    		refOrder.update();
    	}
        renderJson(order);
    }
    
    //文件确认
    @Before(Tx.class)
    public void confirmDoc(){
    	String id = getPara("docId");
    	BookOrderDoc order = BookOrderDoc.dao.findById(id);
    	order.set("confirm", LoginUserController.getLoginUserId(this));
    	order.set("confirm_time", new Date());
    	order.set("send_status", "已确认");
    	order.update();
    	
    	Long ref_doc_id = order.getLong("ref_doc_id");
    	JobOrderDoc refOrder = JobOrderDoc.dao.findById(ref_doc_id);
    	if(refOrder!=null){
    		refOrder.set("confirm", LoginUserController.getLoginUserId(this));
    		refOrder.set("confirm_time", new Date());
    		refOrder.set("send_status", "已确认");
    		refOrder.update();
    	}
        renderJson(order);
    }
    @SuppressWarnings("null")
	public void submitBooking(){
    	String booking_id = getPara("order_id");
    	BookingOrder booking = BookingOrder.dao.findById(booking_id);
    	String entrust_type = booking.getStr("entrust_type");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	Long customer_office_id = user.getLong("office_id");
    	
    	
    	
    	//booking主表信息
    	String consignor_man = booking.getStr("consignor_man");
    	String consignor_phone = booking.getStr("consignor_phone");
    	String consignor_address = booking.getStr("consignor_address");
    	String consignee = null;
    	if(booking.get("consignee")!=null){
    		consignee = booking.get("consignee").toString();
    	}
    	String consignee_man = booking.getStr("consignee_man");
    	String consignee_phone = booking.getStr("consignee_phone");
    	String consignee_address = booking.getStr("consignee_address");
    	
    	
    	String notify=null;
    	if(booking.get("notify")!=null){
    		notify = booking.get("notify").toString();
    	}
    	String notify_man = booking.getStr("notify_man");
    	String notify_phone = booking.getStr("notify_phone");
    	String notify_address = booking.getStr("notify_address");
    	
    	
    	String transport_type = booking.getStr("transport_type");
    	String booking_no = booking.getStr("booking_no");
    	String outer_order_no = booking.getStr("outer_order_no");
    	String relation_no = booking.getStr("relation_no");
    	String order_unit = booking.getStr("order_unit");
    	String gargo_name = booking.getStr("gargo_name");
    	Date order_export_date = booking.get("order_export_date");
    	String pieces = booking.get("pieces").toString();
    	String type =booking.getStr("type");
    	String gross_weight = booking.get("gross_weight").toString();
    	String volume = booking.get("volume").toString();
    	
    	//booking海运信息
    	String ocean_party_id = null;
    	Record reOcean = Db.findFirst("select * from booking_ocean_detail where order_id = ?",booking_id);
    	String trans_clause = null;
   		String trade_type = null;
   		String pol_id = null;
    	String pod_id = null;
    	if(reOcean!=null){
    		if(reOcean.get("ocean_agent")!=null){
    			ocean_party_id = reOcean.get("ocean_agent").toString();
    		}
    		trans_clause = reOcean.getStr("trans_clause");
       		trade_type = reOcean.getStr("trade_type");
    	   	pol_id = reOcean.getLong("pol_id").toString();
        	pod_id = reOcean.getLong("pod_id").toString();
    	}
    	
    	//booking空运信息
    	String air_party_id = null;
    	Record reAir = Db.findFirst("select * from booking_air_detail where order_id = ?",booking_id);
    	String air_trans_clause = null;
   		String air_trade_type = null;
   		String air_pol_id = null;
    	String air_pod_id = null;
    	if(reAir!=null){
    		if(reAir.get("air_agent")!=null){
    			air_party_id = reAir.get("air_agent").toString();
    		}
    		air_trans_clause = reAir.getStr("air_trans_clause");
       		air_trade_type = reAir.getStr("air_trade_type");
    	   	air_pol_id = reAir.getLong("air_pol_id").toString();
    	   	air_pod_id = reAir.getLong("air_pod_id").toString();
    	}
    	
    	//booking陆运信息
    	String land_take_party_id = null;
    	String land_delivery_party_id = null;
    	Record reLand = Db.findFirst("select * from booking_land_detail where order_id = ?",booking_id);
    	
    	String take_address = null;
   		String destination = null;

    	String consignee_land_name = null;
    	String truck_type = null;
		
		land_delivery_party_id = null;		
		String delivery_take_address = null;
   		String delivery_destination = null;
	   	Date delivery_eta = null;
    	String delivery_consignee_land_name = null;
    	String delivery_truck_type = null;
    	if(reLand!=null){
    		if(reLand.get("land_take_agent")!=null){
    			land_take_party_id = reLand.get("land_take_agent").toString();
    		}
    		
    		take_address = reLand.getStr("take_address");
       		destination = reLand.getStr("destination");

        	consignee_land_name = reLand.getStr("consignee_land_name");
        	truck_type = reLand.getStr("truck_type");
    		
        	if(reLand.get("land_delivery_agent")!=null){
        		land_delivery_party_id = reLand.get("land_delivery_agent").toString();
    		}
    		
    		delivery_take_address = reLand.getStr("delivery_take_address");
       		delivery_destination = reLand.getStr("delivery_destination");
    	    delivery_eta = reLand.get("delivery_eta");
        	delivery_consignee_land_name = reLand.getStr("delivery_consignee_land_name");
        	delivery_truck_type = reLand.getStr("delivery_truck_type");
    	}
    	
    	//booking报关信息
    	String custom_party_id = null;
    	Record reCustom = Db.findFirst("select * from booking_custom_detail where order_id = ?",booking_id);
    	if(reCustom!=null){
    		if(reCustom.get("custom_broker")!=null){
    			custom_party_id = reCustom.get("custom_broker").toString();
    		}
    	}
   		
    	if("统一".equals(entrust_type)){
    		Party entrust_agent = Party.dao.findById(booking.getLong("entrust"));
    		Office entrust_office = Office.dao.findById(entrust_agent.getLong("ref_office_id"));    		
    		//entrust_office信息
        	Long entrust_officeNo = null;//生成单号要用到的office_id
            if(entrust_office != null){
            	entrust_officeNo = entrust_office.getLong("id");
            }else{
            	String err = "不存在该代理";
				renderText(err);
				return ;
            }
    		
    		String system_type = entrust_office.getStr("type");
    		if("customer".equals(system_type)){
    			return;
    		}else if("forwarderCompany".equals(system_type)){
    			JobOrder order  = JobOrder.dao.findFirst("select * from job_order where from_order_id = ? and from_order_type = 'booking' ",booking_id);
    			if(order==null){
    	       		order  = new JobOrder();

	                String newDateStr = "";
	                SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
	                Date date= booking.get("order_export_date");
	                newDateStr=sdf.format(date);

	           		String order_no = OrderNoGenerator.getNextOrderNo("EKYZH", newDateStr, entrust_officeNo==null?customer_office_id:entrust_officeNo);
	       			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
	       			sb.insert(5, JobOrderController.generateJobPrefix(booking.getStr("type")));//在指定的位置1，插入指定的字符串
	       			order_no = sb.toString();
	       			order.set("order_no", order_no);
	            	order.set("creator", user.getLong("id"));
	            	order.set("create_stamp", new Date());
	            	order.set("updator", user.getLong("id"));
	            	order.set("update_stamp", new Date());
	                order.set("office_id", entrust_officeNo);
	                order.set("from_order_type", "booking");
	                order.set("from_order_id", booking_id);
	                order.set("from_order_no", booking_no);
	                order.set("old_order_no", outer_order_no);
	                order.set("relation_no", relation_no);	                
	                order.set("job_unit", order_unit);	                
	                                
	                order.set("trans_clause", (trans_clause==null?air_trans_clause:trans_clause));
	                order.set("trade_type", (trade_type==null?air_trade_type:trade_type));
	                
	                if(StringUtils.isNotBlank(customer_office_id.toString())){
	                	Record customer = Db.findFirst("select * from party where type='CUSTOMER' and ref_office_id = ? and office_id =? ",customer_office_id,entrust_officeNo);
	                	if(customer!=null){
	                		Long customer_id = customer.getLong("id");
	                		order.set("customer_id", customer_id);
	                	}
	                }
	                
	                order.set("type", type);
	                order.set("order_export_date", order_export_date);
	                order.set("pieces", pieces);
	//                order.set("net_weight", re.get("net_weight"));
	                order.set("gross_weight", gross_weight);
	                order.set("volume", volume);
	                order.set("transport_type", transport_type);
	                //-----------默认
	                order.set("billing_method", "perWeight");
	                order.save();
	                
	                Long to_order_id = order.getLong("id");
	                
	                JobOrderShipment ocean  = JobOrderShipment.dao.findFirst("select * from job_order_shipment where order_id = ? ",to_order_id);
	                if(ocean==null){
	                	ocean  = new JobOrderShipment();
	                	ocean.set("order_id", to_order_id);
	                	ocean.set("pol", pol_id);
	                	ocean.set("pod", pod_id);
	                	ocean.set("cargo_desc", gargo_name);
	                	ocean.save();
	                }
	                
	                JobOrderAirItem air  = JobOrderAirItem.dao.findFirst("select * from job_order_air_item where order_id = ? ",to_order_id);
	                if(air==null){
	                	air  = new JobOrderAirItem();
	                	air.set("order_id", to_order_id);
	                	air.set("start_from", air_pol_id);
	                	air.set("destination", air_pod_id);
	                	air.save();
	                }
	                
	                //提货
	                JobOrderLandItem take_land  = JobOrderLandItem.dao.findFirst("select * from job_order_land_item where order_id = ? ",to_order_id);
	                if(take_land==null){
	                	take_land  = new JobOrderLandItem();
	                	if(reLand.get("take_eta")!=null){
	                		take_land.set("eta", reLand.get("take_eta"));
	                	}	                	
	                	take_land.save();
	                }

	                booking.set("to_order_id", to_order_id);
	                booking.set("to_order_type", "forwarderJobOrder");
	                booking.set("booking_submit_flag", "Y");
	                booking.set("status", "已提交");
	                booking.update();
    			}
    			renderJson("{\"result\":true}");	        	
    	       	
    		}else if("landCompany".equals(system_type)){
    			TransJobOrder order  = TransJobOrder.dao.findFirst("select * from trans_job_order where from_order_id = ? and from_order_type = 'booking' ",booking_id);
    	    	
    		}else if("customCompany".equals(system_type)){
    			CustomPlanOrder order  = CustomPlanOrder.dao.findFirst("select * from custom_plan_order where from_order_id = ? and from_order_type = 'booking' ",booking_id);
    	    	
    		}else if("tradeCompany".equals(system_type)){
    			TradeJobOrder order  = TradeJobOrder.dao.findFirst("select * from trade_job_order where from_order_id = ? and from_order_type = 'booking' ",booking_id);
    		}
    	}
   		
//        	if(StringUtils.isNotBlank(truct_type)){
//        		if(StringUtils.isBlank(transport_type)){
//        			transport_type += "land";
//        		}else{
//        			transport_type += ",land";
//        		}
//        	}
//        	if(StringUtils.isNotBlank(container_type)){

//        		else{
//        			transport_type += ",ocean";
//        		}
//        	}

            //从表默认选项
//            if(StringUtils.isNotBlank(container_type)){
//            	String[] array = container_type.split(",");
//            	for (int i = 0; i < array.length; i++) {
//            		String[] ctypeMsg = array[i].split("X");
//            		String con_type = ctypeMsg[0];
//            		String number = ctypeMsg[1];
//            		for (int j = 0; j < Integer.parseInt(number); j++) {
//            			Record landItem = new Record();
//            			landItem.set("order_id", order.get("id"));
//            			landItem.set("container_type", con_type);
//            			Db.save("job_order_shipment_item", landItem);
//					}
//            		
//            	}
//        		
//        		
//        		
//        	}
//        	if(StringUtils.isNotBlank(truct_type)){
//        		String[] array = truct_type.split(",");
//            	for (int i = 0; i < array.length; i++) {
//            		String[] ctypeMsg = array[i].split("X");
//            		String tr_type = ctypeMsg[0];
//            		String number = ctypeMsg[1];
//            		for (int j = 0; j < Integer.parseInt(number); j++) {
//            			Record oceanItem = new Record();
//            			oceanItem.set("order_id", order.get("id"));
//            			oceanItem.set("status", "待发车");
//            			oceanItem.set("truck_type", tr_type);
//            			Db.save("job_order_land_item", oceanItem);
//					}
//            	}
//            	
//            	Record oceanDetail = new Record();
//            	oceanDetail.set("order_id", order.get("id"));
//            	oceanDetail.set("pol", item.get("pol"));
//            	oceanDetail.set("pod", item.get("pod"));
//            	
//            	oceanDetail.set("carrier", item.get("carrier"));
//            	oceanDetail.set("vessel", item.get("vessel"));
//            	oceanDetail.set("voyage", item.get("voyage"));
//            	oceanDetail.set("eta", item.get("eta"));
//            	oceanDetail.set("etd", item.get("etd"));
//            	//oceanDetail.set("SONO", item.get("SONO"));
//    			Db.save("job_order_shipment", oceanDetail);
//            	
//        	}

    
    }


}
