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
import models.eeda.oms.bookOrder.BookOrderDoc;
import models.eeda.oms.bookOrder.BookingOrder;
import models.eeda.oms.bookOrder.BookingOrderDoc;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAir;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderLandItem;
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
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.eeda.SysInfoController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.FileUploadUtil;
import controllers.util.OrderCheckOfficeUtil;
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
   		
   		Record re = Db.findFirst("select * from party where ref_office_id =? and office_id = ?",office_id,office_id);
   		if(re!=null){
   			String address = re.getStr("address");
   			String contact_person = re.getStr("contact_person");
   			String phone = re.getStr("phone");
   			String fax = re.getStr("fax");
   			String zip_code = re.getStr("zip_code");
   			if(StringUtils.isEmpty(address)){
   				address="";
   			}
   			if(contact_person==""||contact_person=="undefined"||contact_person==null){
                contact_person="";
	        }else{
	         	contact_person="ATTN:"+contact_person+'\r';
	        }
   			if(phone==""||phone=="undefined"||phone==null){
                phone="";
   			}else{
   				phone = "TEL:"+phone+" ";
   			}
   			if(fax==""||fax=="undefined"||fax==null){
   	            fax="";
   	        }else{
   	        	fax = "FAX:"+fax;
   	        }
   			if(zip_code==""||zip_code=="undefined"||zip_code==null){
   	        	zip_code="";
   	        }else{
   	        	zip_code = "ZIP CODE:"+zip_code+'\r';
   	        }
   			String system_info=address+'\r'+zip_code+contact_person+phone+fax;
   			Long ref_office_id = re.getLong("ref_office_id");
   	   		Office ref_office = Office.dao.findById(ref_office_id);
   	   		setAttr("ref_office", ref_office);
   	   		setAttr("self_info", system_info);
   		}
   		setAttr("self", re);
   		
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
			sql ="SELECT bcd.*,p.abbr custom_broker_name,p.ref_office_id custom_broker_ref_office_id"
					+ " ,p1.ref_office_id arrive_custom_broker_ref_office_id,p1.abbr arrive_custom_broker_name "
					+ " from booking_custom_detail bcd"
					+ " LEFT JOIN party p on p.id = bcd.custom_broker"
					+ " LEFT JOIN party p1 on p1.id = bcd.arrive_custom_broker"
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

		String action_type="add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type="update";
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

//		//记录结算费用使用历史  
//		saveFinItemQueryHistory(charge_list);
		
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
   		
   		SysInfoController.saveLog(jsonStr, id, user, action_type, "Booking", "");
		Record r = bookingOrder.toRecord();
		r.set("creator_name", user_name);
		r.set("ocean", getDetail(id,"ocean"));
		r.set("air", getDetail(id,"air"));
		r.set("land", getDetail(id,"land"));
		r.set("custom", getDetail(id,"custom"));
   		renderJson(r);
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
            String type = getPara("docType");
            Long userId = LoginUserController.getLoginUserId(this);
            
            uploadFile(fileList, order_id, userId, type , "booking_order_doc", false);
            
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
    
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	BookingOrderDoc bookingOrderDoc = BookingOrderDoc.dao.findById(id);
    	String fileName = bookingOrderDoc.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            bookingOrderDoc.delete();
            resultMap.put("result", result);
        }else{
        	bookingOrderDoc.delete();
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
    	UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("booking_order", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
    	BookingOrder bookingOrder = BookingOrder.dao.findFirst(
    			" SELECT border.*,p1.abbr shipper_name,p2.abbr consignee_name,p3.abbr notify_name,o.office_name,"
    			+ " p4.abbr entrust_name,p4.ref_office_id entrust_ref_office_id,o.office_name,ul.c_name creator_name,CONCAT(ut.name,ut.name_eng) order_unit_input"
    			+ " from booking_order border "
    			+ " LEFT JOIN office o on o.id =  border.office_id "
    			+ " LEFT JOIN unit ut on ut.id =  border.order_unit "
    			+ " LEFT JOIN party p1 on p1.id = border.shipper"
    			+ " LEFT JOIN party p2 on p2.id = border.consignee"
    			+ " LEFT JOIN party p3 on p3.id = border.notify"
    			+ " LEFT JOIN party p4 on p4.id = border.entrust"
    			+ " LEFT JOIN user_login ul on ul.id = border.creator"
    			+ "  WHERE border.id = ?",id);
//    	Long plan_order_id = bookOrder.getLong("plan_item_id");
    	
    	String sqlString=" SELECT "
    			+" 	*, IFNULL(vessel_voyage, air_flight_no_voyage_no ) vessel_voyage_or_flight_no_voyage_no"
    			+" FROM  ( "
    			+" 		SELECT jo.id, jo.order_no, jo.order_export_date,jo.pieces,jo.volume,jo.gross_weight,jo.type,jo.trade_type,jo.trans_clause, "
    			+" 			jo.job_unit,CONCAT(ut.name,ut.name_eng) job_unit_name,jos.MBLshipper,jos.MBLnotify_party, "
    			+" 			jos.MBLconsignee,jos.HBLshipper,jos.HBLconsignee,jos.HBLnotify_party,jos.MBLshipper_info, "
    			+" 			jos.MBLconsignee_info,jos.MBLnotify_party_info,jos.HBLshipper_info, "
    			+" 			jos.HBLconsignee_info,jos.HBLnotify_party_info,jos.cargo_desc,jos.etd,jos.eta,jos.ata,jos.atd, "
    			+" 			jos.closing_date,jos.vessel,jos.voyage,jos.pol,jos.pod,p4.abbr carrier_name,jos.carrier, "
    			+" 			pMBLShipper.company_name MBLshipper_name,pMBLconsignee.company_name MBLconsignee_name,pMBLnotify_party.company_name MBLnotify_name, "
    			+" 			pHBLshipper.company_name HBLshipper_name,p9.abbr HBLconsignee_name,pHBLnotify_party.company_name HBLnotify_name, "
    			+" 			CONCAT(lo1. NAME, ' -', lo1. CODE) pol_name,CONCAT(lo2. NAME, ' -', lo2. CODE) pod_name, "
    			+" 			CONCAT(loAir1. NAME, ' -', loAir1. CODE) air_pol_name,CONCAT(loAir2. NAME, ' -', loAir2. CODE) air_pod_name, "
    			+" 			pAir.abbr air_company_name, joai.air_company,joai.eta air_eta,joai.etd air_etd,joai.start_from, "
    			+" 			joai.destination,dock1.dock_name take_address_name,dock2.dock_name delivery_address_name, "
    			+" 			joli.delivery_address,joli.eta land_eta,joli.consignee, "
    			+" 			pLandConsigee.company_name land_consigee_name,joli.consignee_contact_man land_consignee_contact_man, "
    			+" 			joli.consignee_phone land_consignee_phone,joli.driver,joli.driver_tel,			 "
    			+" 			CONCAT_WS('-', jos.vessel, jos.voyage) vessel_voyage, "
    			+" 			IFNULL( CONCAT_WS('-', jos.vessel, jos.voyage),GROUP_CONCAT( CONCAT_WS( ' / ', joai.flight_no,joai.voyage_no) "
    			+" 				) "
    			+" 			) vessel_voyage_or_air_info, ( SELECT GROUP_CONCAT( CONCAT_WS( '-', joai2.flight_no,joai2.voyage_no )) "
    			+" 				FROM job_order_air_item joai2 "
    			+ "             WHERE joai2.order_id = jo.id ) air_flight_no_voyage_no, "
    			+" 			( SELECT GROUP_CONCAT(CONCAT(josi2.container_no,' / ',josi2.seal_no) SEPARATOR ';')  "
    			+" 				FROM job_order_shipment_item josi2 "
    			+" 				WHERE josi2.order_id = jo.id ) ship_container_no_seal_no, "
    			+" 			( SELECT GROUP_CONCAT(josi2.seal_no) "
    			+" 				FROM job_order_shipment_item josi2 "
    			+" 				WHERE josi2.order_id = jo.id ) ship_seal_no, "
    			+" 			( SELECT GROUP_CONCAT(joli2.take_address) "
    			+" 				FROM job_order_land_item joli2 "
    			+" 				WHERE joli2.order_id = jo.id ) take_address, "
    			+" 			( SELECT GROUP_CONCAT(joli2.truck_type) "
    			+" 				FROM job_order_land_item joli2 "
    			+" 				WHERE joli2.order_id = jo.id ) truck_type, "
    			+" 			( SELECT COUNT(truck_type) "
    			+" 				FROM job_order_land_item joli2 "
    			+" 				WHERE joli2.order_id = jo.id ) truck_count "
    			+" 		FROM booking_order border "
    			+" 		LEFT JOIN job_order jo ON jo.id = border.to_order_id "
    			+" 		LEFT JOIN unit ut on ut.id = jo.job_unit "
    			+" 		LEFT JOIN job_order_shipment jos ON jos.order_id = jo.id "
    			+" 		LEFT JOIN job_order_air_item joai ON joai.order_id = jo.id "
    			+" 		LEFT JOIN party pMBLShipper on pMBLShipper.id = jos.MBLshipper "
    			+" 		LEFT JOIN party pMBLconsignee on pMBLconsignee.id = jos.MBLconsignee "
    			+" 		LEFT JOIN party pMBLnotify_party on pMBLnotify_party.id = jos.MBLnotify_party "
    			+" 		LEFT JOIN party pHBLshipper on pHBLshipper.id = jos.HBLshipper "
    			+" 		LEFT JOIN party p9 ON p9.id = jos.HBLconsignee "
    			+" 		LEFT JOIN party pHBLnotify_party on pHBLnotify_party.id = jos.HBLnotify_party "
    			+" 		LEFT JOIN party p4 ON p4.id = jos.carrier "
    			+" 		LEFT JOIN job_order_shipment_item josi ON josi.order_id = jo.id "
    			+" 		LEFT JOIN location loAir1 on loAir1.id = joai.start_from "
    			+" 		LEFT JOIN location loAir2 on loAir2.id = joai.destination "
    			+" 		LEFT JOIN party pAir on pAir.id = joai.air_company "
    			+" 		LEFT JOIN job_order_land_item joli ON joli.order_id = jo.id "
    			+" 		LEFT JOIN dockinfo dock1 on dock1.id = joli.take_address "
    			+" 		LEFT JOIN dockinfo dock2 on dock2.id = joli.delivery_address "
    			+" 		LEFT JOIN party pLandConsigee on pLandConsigee.id = joli.consignee  "
    			+" 		LEFT JOIN location lo1 ON lo1.id = jos.pol "
    			+" 		LEFT JOIN location lo2 ON lo2.id = jos.pod  "
    			+" 		WHERE jo.delete_flag = 'N' AND border.id = "+id
    			+" 	) B ";
    	Record re = Db.findFirst(sqlString);
    	if(re != null){
    		setAttr("jobOrder", re);
    	
	    	//获取预约到达时间
	    	Long job_order_id = re.getLong("id");
	    	Record jobland = Db.findFirst("select SUBSTR(eta,1,12) eta from job_order_land_item where order_id = ?",job_order_id);
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

	    	Record planCustom = Db.findFirst("SELECT cjo.id,cjo.order_no custom_plan_no,o.office_name custom_bank,cjo. STATUS applybill_status,	cjo.ref_no custom_order_no, "
	    			+"	cjo.custom_state custom_status,	ul.c_name creator,cjo.create_stamp,ul2.c_name fill_name,cjo.fill_stamp,	cjo.customs_billCode,SUBSTR(cjo.date_custom,1,10) date_custom"
	    			+" FROM 	custom_plan_order cjo "
	    			+" LEFT JOIN user_login ul ON ul.id = cjo.creator "
	    			+" LEFT JOIN user_login ul2 ON ul2.id = cjo.fill_by "
	    			+" LEFT JOIN office o ON o.id = cjo.to_office_id "
	    			+" WHERE cjo.ref_job_order_id = ? "
	    			+" AND ul.office_id = ?  AND cjo.delete_flag = 'N' ",job_order_id,re.get("office_id"));
	    	
	    	if(planCustom != null){
	    		String custom_order_no = planCustom.getStr("customs_billCode");
	    		String status = planCustom.getStr("custom_status");
	    		Date create_stamp = planCustom.get("date_custom");
	    		setAttr("job_custom_order_no", custom_order_no);
	    		setAttr("job_status", status);
	    		setAttr("date_custom", create_stamp);
	    	}else if(jobcustom != null){  		
	    		String custom_order_no = jobcustom.getStr("custom_order_no");
	    		String status = jobcustom.getStr("status");
	    		Date create_stamp = jobcustom.get("create_stamp");
	    		setAttr("job_custom_order_no", custom_order_no);
	    		setAttr("job_status", status);
	    		setAttr("date_custom", create_stamp);
	    	}
	    	
	    	//上船时间
	    	String sql_Ship = "select SUBSTR(etd,1,10) etd_date,jos.*,l.name pol_name,l1.name pod_name  from job_order_shipment jos"
	    			+ " LEFT JOIN location l on l.id = jos.pol"
	    			+ " LEFT JOIN location l1 on l1.id = jos.pod"
	    			+ " where order_id = ?";
	    	Record jobShip = Db.findFirst(sql_Ship,job_order_id);
	    	if(jobShip != null){
	    		if(jobShip.get("etd_date")!=null){
	    			String atd = jobShip.get("etd_date").toString();
	    			setAttr("ocean_job_etd", atd);
	    		}
	    		if(jobShip.get("eta")!=null){
	    			String ata = jobShip.get("eta").toString();
	    			setAttr("ocean_job_eta", ata);
	    		}
	    		if(jobShip.get("atd")!=null){
	    			String atd = jobShip.get("atd").toString();
	    			setAttr("ocean_job_atd", atd);
	    		}
	    		if(jobShip.get("ata")!=null){
	    			String ata = jobShip.get("ata").toString();
	    			setAttr("ocean_job_ata", ata);
	    		}
	    		if(jobShip.get("pol_name")!=null){
	    			String pol_name = jobShip.get("pol_name").toString();
	    			setAttr("pol_name", pol_name);
	    		}
	    		if(jobShip.get("pod_name")!=null){
	    			String pod_name = jobShip.get("pod_name").toString();	    			
	    			setAttr("pod_name", pod_name);
	    		}
	    	}
	    	
	    	//空运时间
	    	Record jobAir = Db.findFirst("select * from job_order_air_item where order_id = ?",job_order_id);
	    	if(jobAir != null){
	    		if(jobAir.get("etd")!=null){
	    			String etd = jobAir.get("etd").toString();
	    			setAttr("air_etd", etd);
	    		}
	    		
	    		if(jobAir.get("eta")!=null){
	    			String eta = jobAir.get("eta").toString();
	    			setAttr("air_eta", eta);
	    		}
	    	}

    	}
    	//贸易单据回显
    	Record tradeOrder = Db.findFirst("select * from trade_job_order where from_order_id=? and from_order_type=?",id,"bookingOrder");
    	setAttr("tradeOrder", tradeOrder);
    	
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
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));
    	//应付费用明细回显
    	setAttr("costList",getCostItems(id));

    	  
        render("/oms/bookingOrder/bookingOrderEdit.html");
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
     		+ " ifnull(u.c_name, u.user_name) creator_name,ifnull(o.office_name,p.abbr) sp_name,"
     		+ " (SELECT  count(jod0.id) FROM booking_order_doc jod0 WHERE  jod0.order_id =bo.id and (jod0.type='zero' or jod0.type='two' or jod0.type='four')  and   jod0.send_status='已发送' ) new_count,"
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
     		+ " left join (select * from job_order where from_order_type = 'booking') jor on jor.from_order_id = bo.id"
     		+ " LEFT JOIN job_order_land_item joli on joli.order_id = jor.id"
     		+ " LEFT JOIN job_order_custom_china_self_item jocc on jocc.order_id = jor.id"
     		+ " LEFT JOIN job_order_shipment jos on jos.order_id = jor.id "
     		+ "	LEFT JOIN plan_order po on po.id = bo.plan_order_id"
     		+ "	LEFT JOIN office o on o.id = po.to_entrusted_id"
     		+ "	LEFT JOIN party p on p.id = bo.entrust"
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


  //删除常用模版
    @Before(Tx.class)
    public void deleteTradeSaleTemplate(){
    	String id = getPara("id");
    	Db.update("delete from book_order_trade_sale_template where id = ?",id);
    	renderJson("{\"result\":true}");
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

    
    //确认发送文档
    @Before(Tx.class)
    public void confirmSend(){
    	String id = getPara("docId");
    	String job_order_id = getPara("job_order_id");
    	BookingOrderDoc bookingOrderDoc = BookingOrderDoc.dao.findById(id);
    	bookingOrderDoc.set("sender", LoginUserController.getLoginUserId(this));
    	bookingOrderDoc.set("send_time", new Date());
    	bookingOrderDoc.set("send_status", "已发送");
    	bookingOrderDoc.update();
    	
    	Record jobDoc = new Record();
    	jobDoc.set("order_id", job_order_id);
    	jobDoc.set("type", bookingOrderDoc.getStr("type"));
    	jobDoc.set("uploader", bookingOrderDoc.getLong("uploader"));
    	jobDoc.set("doc_name", bookingOrderDoc.getStr("doc_name"));
    	jobDoc.set("upload_time", bookingOrderDoc.get("upload_time"));
    	jobDoc.set("remark", bookingOrderDoc.getStr("remark"));
    	jobDoc.set("sender", bookingOrderDoc.getLong("sender"));
    	jobDoc.set("send_time", bookingOrderDoc.get("send_time"));
    	jobDoc.set("send_status", bookingOrderDoc.getStr("send_status"));
    	jobDoc.set("ref_doc_id", id);
    	Db.save("job_order_doc", jobDoc);
      
        renderJson(bookingOrderDoc);
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
    
    public List<Record> getCostItems(String orderId){
    	String  itemSql = "";
    	itemSql = "SELECT f.name charge_name,f.name_eng charge_name_eng,joa.price price,joa.amount amount,u.name unit_name,joa.total_amount,c.name currency_name FROM booking_order bo "
    			+ " LEFT JOIN job_order jo ON jo.from_order_id = bo.id "
    			+ " LEFT JOIN job_order_arap joa ON joa.order_id = jo.id "
    			+ " LEFT JOIN party pr ON pr.id = joa.sp_id "
    			+ " LEFT JOIN fin_item f ON f.id = joa.charge_id "
    			+ " LEFT JOIN unit u ON u.id = joa.unit_id "
    			+ " LEFT JOIN currency c ON c.id = joa.currency_id "
    			+ " where joa.audit_flag='Y' and joa.order_type='charge' and jo.from_order_id = "+orderId;
    	List<Record> itemList = Db.find(itemSql);
    	return itemList;
    }
    
    public List<Record> getDocItems(String orderId,String type){
    	String  itemSql = "";
    	itemSql = "SELECT * ,"
    			+ " (SELECT  count(jod0.id) FROM booking_order_doc jod0 WHERE "
    			+ " jod0.order_id ="+orderId+" 	AND jod0.type ='"+type+"' and   jod0.send_status='已发送' ) new_count"
    			+ " FROM("
    			+ " select jod.*,u.c_name,u1.c_name sender_name "
    			+ " from booking_order_doc jod "
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
    	BookingOrderDoc order = BookingOrderDoc.dao.findById(id);
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
    	BookingOrderDoc order = BookingOrderDoc.dao.findById(id);
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
    	String shipper = null;
    	if(booking.get("shipper")!=null){
    		shipper = booking.get("shipper").toString();
    	}
    	String shipper_info = booking.getStr("shipper_info");
    	
    	String consignee = null;
    	if(booking.get("consignee")!=null){
    		consignee = booking.get("consignee").toString();
    	}
    	String consignee_info = booking.getStr("consignee_info");
    	
    	
    	String notify=null;
    	if(booking.get("notify")!=null){
    		notify = booking.get("notify").toString();
    	}
    	String notify_info = booking.getStr("notify_info");
    	
    	
    	String transport_type = booking.getStr("transport_type");
    	String booking_no = booking.getStr("booking_no");
    	String outer_order_no = booking.getStr("outer_order_no");
    	String order_unit = booking.getStr("order_unit");
    	String gargo_name = booking.getStr("gargo_name");
    	Date order_export_date = booking.get("order_export_date");
    	String type =booking.getStr("type");
    	String pieces = null;
    	if(booking.get("pieces")!=null){
    		pieces = booking.get("pieces").toString();
    	}
    	String gross_weight = null;
    	if(booking.get("gross_weight")!=null){
    		gross_weight = booking.get("gross_weight").toString();
    	}
    	String volume = null;
    	if(booking.get("volume")!=null){
    		volume = booking.get("volume").toString();
    	}
    	
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
       		if(reOcean.get("pol_id")!=null){
       			pol_id = reOcean.getLong("pol_id").toString();
       		}
       		if(reOcean.get("pod_id")!=null){
       			pod_id = reOcean.getLong("pod_id").toString();
       		}
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
       		if(reAir.get("air_pol_id")!=null){
       			air_pol_id = reAir.get("air_pol_id").toString();
    		}
       		if(reAir.get("air_pod_id")!=null){
       			air_pod_id = reAir.get("air_pod_id").toString();
    		}
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
    		if(entrust_agent!=null){
    			Office entrust_office = Office.dao.findById(entrust_agent.getLong("ref_office_id")); 
    		
    		   		
    		//entrust_office信息
        	Long entrust_officeNo = null;//生成单号要用到的office_id
            if(entrust_office != null){
            	entrust_officeNo = entrust_office.getLong("id");
            }else{
            	String err = "系统不存在该代理";
				renderText(err);
				return ;
            }
    		
    		String system_type = entrust_office.getStr("type");
    		if("customer".equals(system_type)){
    			return;
    		}else if("forwarderCompany".equals(system_type)){
    			JobOrder order  = JobOrder.dao.findFirst("select * from job_order where from_order_id = ? and from_order_type = 'booking' ",booking_id);
    			String order_no = "";
    			if(order==null){
    	       		order  = new JobOrder();

	                String newDateStr = "";
	                SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
	                Date date= booking.get("order_export_date");
	                newDateStr=sdf.format(date);

	           		order_no = OrderNoGenerator.getNextOrderNo("EKYZH", newDateStr, entrust_officeNo==null?customer_office_id:entrust_officeNo);
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
	                if(transport_type.contains("ocean")){
	                	JobOrderShipment ocean  = JobOrderShipment.dao.findFirst("select * from job_order_shipment where order_id = ? ",to_order_id);
		                if(ocean==null){
		                	ocean  = new JobOrderShipment();
		                	ocean.set("order_id", to_order_id);
		                	
		                	ocean.set("HBLshipper", shipper);
		                	ocean.set("HBLshipper_info", shipper_info);
		                	
		                	ocean.set("HBLconsignee", consignee);
		                	ocean.set("HBLconsignee_info", consignee_info);
		                	
		                	ocean.set("HBLnotify_party", notify);
		                	ocean.set("HBLnotify_party_info", notify_info);
		                	
		                	ocean.set("pol", pol_id);
		                	ocean.set("pod", pod_id);
		                	ocean.set("cargo_desc", gargo_name);
		                	ocean.save();
		                }
	                }
	                
	                if(transport_type.contains("air")){
		                JobOrderAirItem air  = JobOrderAirItem.dao.findFirst("select * from job_order_air_item where order_id = ? ",to_order_id);
		                JobOrderAir airDetail  = JobOrderAir.dao.findFirst("select * from job_order_air where order_id = ? ",to_order_id);
		                if(airDetail==null){
		                	airDetail  = new JobOrderAir();
		                	airDetail.set("order_id", to_order_id);
		                	airDetail.set("shipper", shipper);
		                	airDetail.set("shipper_info", shipper_info);
		                	airDetail.set("consignee", consignee);
		                	airDetail.set("consignee_info", consignee_info);
		                	airDetail.set("notify_party", notify);
		                	airDetail.set("notify_party_info", notify_info);
		                	airDetail.save();
		                }
		                if(air==null){
		                	air  = new JobOrderAirItem();
		                	air.set("order_id", to_order_id);
		                	air.set("start_from", air_pol_id);
		                	air.set("destination", air_pod_id);
		                	air.save();
		                }
	                }
	                //提货
	                if(transport_type.contains("land")){
		                JobOrderLandItem take_land  = JobOrderLandItem.dao.findFirst("select * from job_order_land_item where order_id = ? ",to_order_id);
		                if(take_land==null){
		                	take_land  = new JobOrderLandItem();
		                	if(reLand.get("take_eta")!=null){
		                		take_land.set("order_id", to_order_id);
		                		take_land.set("eta", reLand.get("take_eta"));
		                		take_land.set("truck_type", truck_type);
		                	}	                	
		                	take_land.save();
		                }
	                }
	                booking.set("to_order_id", to_order_id);
	                booking.set("to_order_type", "forwarderJobOrder");
	                booking.set("relation_no", order_no);
	                booking.set("booking_submit_flag", "Y");
	                booking.set("status", "已提交");
	                booking.update();
    			}
    			renderJson("order_no",order_no);	        	
    	       	
    		}
		}else{
	    	String err = "提交失败，委托公司为空";
			renderText(err);
			return ;
	    }
     }else{
    	
    	Party ocean_agent = Party.dao.findById(booking.getLong("ocean_party_id"));
    	
    	Party air_agent = Party.dao.findById(booking.getLong("air_party_id"));
 		if(ocean_agent!=null||air_agent!=null){
 			Office ocean_agent_office = Office.dao.findById(ocean_agent.getLong("ref_office_id"));
 			Office air_agent_office = Office.dao.findById(air_agent.getLong("ref_office_id")); 
 		//entrust_office信息
     	Long ocean_officeNo = null;//生成单号要用到的office_id
     	Long air_officeNo = null;//生成单号要用到的office_id
         if(ocean_agent_office != null){
        	 ocean_officeNo = ocean_agent_office.getLong("id");
         }else{
        	 String err = "系统不存在该海运代理";
				renderText(err);
				return ;
         } 
        if(air_agent_office!=null){
       	 	air_officeNo = air_agent_office.getLong("id");
        }else{
       	 	String err = "系统不存在该空运代理";
			renderText(err);
			return ;
        }
 			JobOrder oceanOrder  = JobOrder.dao.findFirst("select * from job_order where from_order_id = ? and from_order_type = 'booking' and office_id=? ",booking_id,ocean_officeNo);
 			JobOrder airOrder  = JobOrder.dao.findFirst("select * from job_order where from_order_id = ? and from_order_type = 'booking' and office_id=? ",booking_id,ocean_officeNo);
 			String order_no = "";
 			if(oceanOrder==null){ 				
 				oceanOrder  = new JobOrder();
 				oceanOrder.set("creator", user.getLong("id"));
 				oceanOrder.set("create_stamp", new Date());
 				oceanOrder.set("updator", user.getLong("id"));
 				oceanOrder.set("update_stamp", new Date());
 				oceanOrder.set("from_order_type", "booking");
 				oceanOrder.set("from_order_id", booking_id);
 				oceanOrder.set("from_order_no", booking_no);
 				oceanOrder.set("old_order_no", outer_order_no);                
 				oceanOrder.set("job_unit", order_unit);                      
 				oceanOrder.set("trans_clause", (trans_clause==null?air_trans_clause:trans_clause));
 				oceanOrder.set("trade_type", (trade_type==null?air_trade_type:trade_type));
 				oceanOrder.set("type", type);
 				oceanOrder.set("order_export_date", order_export_date);
 				oceanOrder.set("pieces", pieces);
 				oceanOrder.set("gross_weight", gross_weight);
 				oceanOrder.set("volume", volume);
 				oceanOrder.set("transport_type", transport_type);
	                //-----------默认
 				oceanOrder.set("billing_method", "perWeight");
            	if(StringUtils.isNotBlank(customer_office_id.toString())){
                	Record customer = Db.findFirst("select * from party where type='CUSTOMER' and ref_office_id = ? and office_id =? ",customer_office_id,ocean_officeNo);
                	if(customer!=null){
                		Long customer_id = customer.getLong("id");
                		oceanOrder.set("customer_id", customer_id);
                	}
                }
            	order_no = OrderNoGenerator.getOrderNo("jobOrder",ocean_officeNo);
            	oceanOrder.set("order_no", order_no);
            	oceanOrder.set("office_id", ocean_officeNo);
            	oceanOrder.save();
                Long to_order_id = oceanOrder.getLong("id");
                if(transport_type.contains("ocean")){
                	JobOrderShipment ocean  = JobOrderShipment.dao.findFirst("select * from job_order_shipment where order_id = ? ",to_order_id);
	                if(ocean==null){
	                	ocean  = new JobOrderShipment();
	                	ocean.set("order_id", to_order_id);
	                	
	                	ocean.set("HBLshipper", shipper);
	                	ocean.set("HBLshipper_info", shipper_info);
	                	
	                	ocean.set("HBLconsignee", consignee);
	                	ocean.set("HBLconsignee_info", consignee_info);
	                	
	                	ocean.set("HBLnotify_party", notify);
	                	ocean.set("HBLnotify_party_info", notify_info);
	                	
	                	ocean.set("pol", pol_id);
	                	ocean.set("pod", pod_id);
	                	ocean.set("cargo_desc", gargo_name);
	                	ocean.save();
	                }
                }
 			}
 			if(airOrder!=null){
 				airOrder  = new JobOrder();
 				airOrder.set("creator", user.getLong("id"));
 				airOrder.set("create_stamp", new Date());
 				airOrder.set("updator", user.getLong("id"));
 				airOrder.set("update_stamp", new Date());
 				airOrder.set("from_order_type", "booking");
 				airOrder.set("from_order_id", booking_id);
 				airOrder.set("from_order_no", booking_no);
 				airOrder.set("old_order_no", outer_order_no);                
 				airOrder.set("job_unit", order_unit);                      
 				airOrder.set("trans_clause", (trans_clause==null?air_trans_clause:trans_clause));
 				airOrder.set("trade_type", (trade_type==null?air_trade_type:trade_type));
 				airOrder.set("type", type);
 				airOrder.set("order_export_date", order_export_date);
 				airOrder.set("pieces", pieces);
 				airOrder.set("gross_weight", gross_weight);
 				airOrder.set("volume", volume);
 				airOrder.set("transport_type", transport_type);
	                //-----------默认
 				airOrder.set("billing_method", "perWeight");
            	if(StringUtils.isNotBlank(customer_office_id.toString())){
                	Record customer = Db.findFirst("select * from party where type='CUSTOMER' and ref_office_id = ? and office_id =? ",customer_office_id,air_officeNo);
                	if(customer!=null){
                		Long customer_id = customer.getLong("id");
                		airOrder.set("customer_id", customer_id);
                	}
            	order_no = OrderNoGenerator.getOrderNo("jobOrder",air_officeNo);
            	airOrder.set("order_no", order_no);
            	airOrder.set("office_id", air_officeNo);
            	airOrder.save();
            }
            	Long to_order_id = oceanOrder.getLong("id");
            	if(transport_type.contains("air")){
	                JobOrderAirItem air  = JobOrderAirItem.dao.findFirst("select * from job_order_air_item where order_id = ? ",to_order_id);
	                JobOrderAir airDetail  = JobOrderAir.dao.findFirst("select * from job_order_air where order_id = ? ",to_order_id);
	                if(airDetail==null){
	                	airDetail  = new JobOrderAir();
	                	airDetail.set("order_id", to_order_id);
	                	airDetail.set("shipper", shipper);
	                	airDetail.set("shipper_info", shipper_info);
	                	airDetail.set("consignee", consignee);
	                	airDetail.set("consignee_info", consignee_info);
	                	airDetail.set("notify_party", notify);
	                	airDetail.set("notify_party_info", notify_info);
	                	airDetail.save();
	                }
	                if(air==null){
	                	air  = new JobOrderAirItem();
	                	air.set("order_id", to_order_id);
	                	air.set("start_from", air_pol_id);
	                	air.set("destination", air_pod_id);
	                	air.save();
	                }
                }	
 			}
 			
 			renderJson("order_no",order_no);
		}else{
	    	String err = "提交失败，委托公司为空";
			renderText(err);
			return ;
	    }
  
     }
			
    }
    
}
