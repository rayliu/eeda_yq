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
import models.eeda.oms.jobOrder.JobOrderAirCargoDesc;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderInsurance;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderSendMail;
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

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import config.EedaConfig;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class JobOrderController extends Controller {

	private Logger logger = Logger.getLogger(JobOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		String type = getPara("type");
		setAttr("type",type);
		render("/oms/JobOrder/JobOrderList.html");
	}
	
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
	    	
	    	//返回海运的港口名称
	    	String port_sql = "select lo.name por_name,lo1.name pol_name,lo2.name pod_name from plan_order_item jos"
				    			+" LEFT JOIN location lo on lo.id = jos.por"
				    			+" LEFT JOIN location lo1 on lo1.id = jos.pol"
				    			+" LEFT JOIN location lo2 on lo2.id = jos.pod"
				    			+" where jos.id = ?";
	    	setAttr("port",Db.findFirst(port_sql,id));
	    	
    	}
    	setAttr("loginUser",LoginUserController.getLoginUserName(this));
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    //插入动作MBL标识符
    public void mblflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jobOrderShipment = new JobOrderShipment();
    	jobOrderShipment = JobOrderShipment.dao.findFirst("select id,mbl_flag from job_order_shipment where order_id = ?",jsonStr);
    	jobOrderShipment.set("mbl_flag", "Y");
    	jobOrderShipment.update();
    	renderJson("{\"result\":true}");
    }
    
    //已电放确认表标识
    public void alreadyInlineFlag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jobOrderShipment = new JobOrderShipment();
    	jobOrderShipment = JobOrderShipment.dao.findFirst("select id,in_line_flag from job_order_shipment where order_id = ?",jsonStr);
    	jobOrderShipment.set("in_line_flag", "Y");
    	jobOrderShipment.update();
    	renderJson("{\"result\":true}");
    }

    //插入打印动作SI标识符
    public void siflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jobOrderShipment = new JobOrderShipment();
    	jobOrderShipment = JobOrderShipment.dao.findFirst("select id,si_flag from job_order_shipment where order_id = ?",jsonStr);
    	jobOrderShipment.set("si_flag", "Y");
    	jobOrderShipment.update();
    	renderJson("{\"result\":true}");
    }
    
    //插入派车单打印动作标记
    public void truckOrderflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderLandItem jobOrderLandItem = new JobOrderLandItem();
    	jobOrderLandItem = JobOrderLandItem.dao.findFirst("select id,truckorder_flag from job_order_land_item where order_id = ?",jsonStr);
    	jobOrderLandItem.set("truckorder_flag", "Y");
    	jobOrderLandItem.update();
    	renderJson("{\"result\":true}");
    }
    
    //插入打印动作AFR/AMS标识符
    public void aframsflag(){
    	String jsonStr = getPara("order_id");
    	JobOrderShipment jobOrderShipment = new JobOrderShipment();
    	jobOrderShipment = JobOrderShipment.dao.findFirst("select id,afr_ams_flag from job_order_shipment where order_id = ?",jsonStr);
    	jobOrderShipment.set("afr_ams_flag", "Y");
    	jobOrderShipment.update();
    	renderJson("{\"result\":true}");
    }
    
    @SuppressWarnings("unchecked")
	@Before(Tx.class)
   	public void save() throws Exception {	
    	
   		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String planOrderItemID = (String) dto.get("plan_order_item_id");
            
        JobOrder jobOrder = new JobOrder();
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
   			String type = (String) dto.get("type");
   			String prefix = "";
   			if(type.equals("出口柜货")||type.equals("进口柜货")||type.equals("出口散货")||type.equals("进口散货")){
   				prefix+="EKO";
   			}
   			else if(type.equals("出口空运")||type.equals("进口空运")){
   				prefix+="EKA";
   			}
   			else if(type.equals("香港头程")||type.equals("香港游")){
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
   			String orderPrefix = OrderNoGenerator.getNextOrderNo(prefix);
            jobOrder.set("order_no", orderPrefix);
   			jobOrder.set("creator", user.getLong("id"));
   			jobOrder.set("create_stamp", new Date());
   			jobOrder.save();
   			id = jobOrder.getLong("id").toString();
   			
   			//创建过工作单，设置plan_order_item的字段
   			PlanOrderItem planOrderItem = PlanOrderItem.dao.findById(planOrderItemID);
   			planOrderItem.set("is_gen_job", "Y");
   			planOrderItem.update();
   		}
		
		//海运
		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
		DbUtils.handleList(shipment_detail, id, JobOrderShipment.class, "order_id");
		
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
		
		//报关
		List<Map<String, String>> chinaCustom = (ArrayList<Map<String, String>>)dto.get("chinaCustom");
		List<Map<String, String>> abroadCustom = (ArrayList<Map<String, String>>)dto.get("abroadCustom");
		List<Map<String, String>> hkCustom = (ArrayList<Map<String, String>>)dto.get("hkCustom");
		if(chinaCustom!=null){
			DbUtils.handleList(chinaCustom, id, JobOrderCustom.class, "order_id");
		}
		if(abroadCustom!=null){
			DbUtils.handleList(abroadCustom, id, JobOrderCustom.class, "order_id");
		}
		if(hkCustom!=null){
			DbUtils.handleList(hkCustom, id, JobOrderCustom.class, "order_id");
		}
		
		//保险
		List<Map<String, String>> insurance_detail = (ArrayList<Map<String, String>>)dto.get("insurance_detail");
		DbUtils.handleList(insurance_detail, id, JobOrderInsurance.class, "order_id");
		
		//费用明细，应收应付
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, id, JobOrderArap.class, "order_id");
		List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
		DbUtils.handleList(chargeCost_list, id, JobOrderArap.class, "order_id");

		long creator = jobOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		
		Record r = jobOrder.toRecord();
   		r.set("creator_name", user_name);
   		
   		//海运头程资料
   		r.set("oceanHead", Db.findFirst("select * from job_order_shipment_head where order_id = ?",id));
   		
   		r.set("custom",Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china"));
   		r.set("abroadCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"abroad"));
   		r.set("hkCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"HK/MAC"));
   		
   		r.set("shipment", getItemDetail(id,"shipment"));
    	r.set("air", getItemDetail(id,"air"));
   		r.set("insurance", getItemDetail(id,"insure"));
   		
   		//返回海运的港口名称
    	String port_sql = "select lo.name por_name,lo1.name pol_name,lo2.name pod_name from job_order_shipment jos"
						+" LEFT JOIN location lo on lo.id = jos.por"
						+" LEFT JOIN location lo1 on lo1.id = jos.pol"
						+" LEFT JOIN location lo2 on lo2.id = jos.pod"
						+" where order_id = ?";
   		r.set("port",Db.findFirst(port_sql,id));
   		
   		renderJson(r);
   	}
    
    
    
    //上传文件
    @Before(Tx.class)
    public void saveDocFile(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	String order_id = getPara("order_id");
    	
    	List<UploadFile> fileList = getFiles("doc");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
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
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }

    //返回对象	
    private Record getItemDetail(String id,String type){
    	Record re = null;
    	if("shipment".equals(type))
    		re = Db.findFirst("select jos.*, p1.abbr shipperAbbr , p2.abbr consigneeAbbr, p3.abbr notify_partyAbbr, p4.abbr carrier_name,p5.abbr head_carrier_name,p6.abbr oversea_agent_name,p7.abbr booking_agent_name from job_order_shipment jos "
    				+ " left join party p1 on p1.id=jos.shipper"
    				+ " left join party p2 on p2.id=jos.consignee"
    				+ " left join party p3 on p3.id=jos.notify_party"
    				+ " left join party p4 on p4.id=jos.carrier"
    				+ " left join party p5 on p5.id=jos.head_carrier"
    				+ " left join party p6 on p6.id=jos.oversea_agent"
    				+ " left join party p7 on p7.id=jos.booking_agent"
    				+ " where order_id = ?",id);
    	else if("insure".equals(type)){
    		re = Db.findFirst("select * from job_order_insurance joi where order_id = ?",id);
    	}else if("air".equals(type)){
    		re = Db.findFirst("select joa.* ,p1.abbr shipperAbbr,p2.abbr consigneeAbbr,p3.abbr notify_partyAbbr,p4.abbr booking_agent_name from job_order_air joa"
    				+ " left join party p1 on p1.id=joa.shipper"
    				+ " left join party p2 on p2.id=joa.consignee"
    				+ " left join party p3 on p3.id=joa.notify_party"
    				+ " left join party p4 on p4.id=joa.booking_agent"
    				+ " where order_id=?", id);
    	}
		return re;
    }
    
    //返回list
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("shipment".equals(type)){
    		itemSql = "select jos.*,u.name unit_name from job_order_shipment_item jos"
    				+ " left join unit u on u.id=jos.unit_id"
    				+ " where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("air".equals(type)){
    		itemSql = "select joa.*, pa.abbr air_company_name from job_order_air_item joa"
    		        + " left join party pa on pa.id=joa.air_company"
    		        + " where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("cargoDesc".equals(type)){
    		itemSql = "select * from job_order_air_cargodesc joac where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("land".equals(type)){
    		itemSql = "select jol.*,p.abbr transport_company_name from job_order_land_item jol "
    				+ " left join party p on p.id=jol.transport_company"
    				+ " where order_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,u.name unit_name,c.name currency_name from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " where order_id=? and order_type=?";
    		itemList = Db.find(itemSql, orderId,"charge");
    	}else if("cost".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name cost_name,u.name unit_name,c.name currency_name from job_order_arap jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
	    	        + " where order_id=? and order_type=?";
	    	itemList = Db.find(itemSql, orderId,"cost");
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from job_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? ";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from job_order_sendMail where order_id=?";
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

    	//获取空运运明细表信息
    	setAttr("airList", getItems(id,"air"));
    	setAttr("cargoDescList", getItems(id,"cargoDesc"));
    	setAttr("air", getItemDetail(id,"air"));
    	
    	//获取陆运明细表信息
    	setAttr("landList", getItems(id,"land"));
    	
    	//报关
    	setAttr("custom",Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"china"));
   		setAttr("abroadCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"abroad"));
   		setAttr("hkCustom", Db.findFirst("select * from job_order_custom joc where order_id = ? and custom_type = ?",id,"HK/MAC"));
    	
    	//保险
    	setAttr("insurance", getItemDetail(id,"insure"));
    	
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));
    	setAttr("mailList", getItems(id,"mail"));

    	//客户回显
    	Party party = Party.dao.findById(jobOrder.get("customer_id"));
    	setAttr("party", party);
    	
    	//工作单创建人
    	long creator = jobOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
    	//返回海运的港口名称
    	String port_sql = "select lo.name por_name,lo1.name pol_name,lo2.name pod_name, lo3.name fnd_name,lo4.name hub_name from job_order_shipment jos"
						+" LEFT JOIN location lo on lo.id = jos.por"
						+" LEFT JOIN location lo1 on lo1.id = jos.pol"
						+" LEFT JOIN location lo2 on lo2.id = jos.pod"
						+" LEFT JOIN location lo3 on lo3.id = jos.fnd"
						+" LEFT JOIN location lo4 on lo4.id = jos.hub"
						+" where order_id = ?";
   		setAttr("port",Db.findFirst(port_sql,id)); 
    	
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));
    	
    	//海运头程资料
   		setAttr("oceanHead", Db.findFirst("select * from job_order_shipment_head where order_id = ?",id));
    	  
        render("/oms/JobOrder/JobOrderEdit.html");
    }
    
    //使用common-email, javamail
    @Before(Tx.class)
    public void sendMail() throws Exception {
    	String order_id = getPara("order_id");
    	String userEmail = getPara("userEmail");
    	String mailTitle = getPara("mailTitle");
    	String mailContent = getPara("mailContent");
    	String docs = getPara("docs");
    	
        MultiPartEmail email = new MultiPartEmail();  
        email.setCharset("GB2312");
        /*smtp.exmail.qq.com*/
        email.setHostName("smtp.exmail.qq.com");
        email.setSmtpPort(465);
        
        /*输入公司的邮箱和密码*/
        /*EedaConfig.mailUser, EedaConfig.mailPwd*/
        email.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));        
        email.setSSLOnConnect(true);
        
        /*EedaConfig.mailUser*/
        email.setFrom(EedaConfig.mailUser);//设置发信人
        
        //设置收件人，邮件标题，邮件内容
        if(StringUtils.isNotEmpty(userEmail)){
	        email.addTo(userEmail);
        }
        if(StringUtils.isNotEmpty(mailTitle)){
	        email.setSubject(mailTitle);
        }
        if(StringUtils.isNotEmpty(mailContent)){
	        email.setMsg(mailContent);
        }
        
        //添加附件
        if(StringUtils.isNotEmpty(docs)){
    		String strAry[] = docs.split(",");
	        for(int i=0;i<strAry.length;i++){
	        	
	        	String filePath = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\"+strAry[i];
	            File file = new File(filePath);
	            if (file.exists() && file.isFile()) {
	            	
	            	String localAttachmentPath = filePath;
	            	EmailAttachment attachment = new EmailAttachment();
	            	attachment.setPath(localAttachmentPath);  
	            	attachment.setDisposition(EmailAttachment.ATTACHMENT); 
	            	attachment.setName(strAry[i]); 
	            	email.attach(attachment);
	            }
	        }
        }
        try{
        	email.send();
        	JobOrderSendMail jsm = new JobOrderSendMail();
        	jsm.set("order_id", order_id);
        	jsm.set("mail_title", mailTitle);
        	jsm.set("doc_name", docs);
        	jsm.set("receive_mail", userEmail);
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
    	String type=getPara("type");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        if("sowait".equals(type)){
        	sql=" SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order jor "
        			+ " LEFT JOIN job_order_shipment jos ON jor.id = jos.order_id "
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator "
        			+ " WHERE jor.type = '出口柜货' AND jos.SONO IS NULL AND jor.transport_type LIKE '%ocean%'";        	
        }else if("truckorderwait".equals(type)){
        	 sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order_land_item joli"
        			+ " left join job_order jor on jor.id = joli.order_id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE datediff(joli.eta, now()) <= 3 AND (joli.truckorder_flag != 'Y' OR joli.truckorder_flag IS NULL)"
        			+ " AND jor.transport_type LIKE '%land%'";
        	
        	
        } else if("siwait".equals(type)){
        	 sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        	 		+ " FROM job_order_shipment jos"
        	 		+ " left join job_order jor on jos.order_id = jor.id"
        	 		+ " left join party p on p.id = jor.customer_id"
        	 		+ " left join user_login u on u.id = jor.creator "
        	 		+ "WHERE TO_DAYS(export_date)=TO_DAYS(now())";
        	
        } else if("mblwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order_shipment jos "
        			+ " left join job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null)";
        	
        } else if("customwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order jor LEFT JOIN  job_order_custom joc ON jor.id=joc.order_id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.transport_type like '%custom%' and joc.customs_broker is null";
        	
        } else if("insurancewait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order jor LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.transport_type LIKE '%insurance%' and joi.insure_no is NULL";
        } else if("overseacustomwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order_shipment jos "
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE  (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y' "
        			+ "and timediff(now(),jos.etd)<TIME('48:00:00') ";
        } else if("tlxOrderwait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
        			+ " FROM job_order_shipment jos"
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE TO_DAYS(jos.etd)= TO_DAYS(now())";
        }
        else{

         sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name "
    			+ "  from job_order jor "
    			+ "  left join party p on p.id = jor.customer_id"
    			+ "  left join user_login u on u.id = jor.creator"
    			+ "   where 1 =1 ";}
        
        String condition = DbUtils.buildConditions(getParaMap());

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
   
   
}
