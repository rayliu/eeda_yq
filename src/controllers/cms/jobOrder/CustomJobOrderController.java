package controllers.cms.jobOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
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
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomJobOrderController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(CustomJobOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render("/cms/customJobOrder/JobOrderList.html");
	}
	
    public void create() {
        render("/cms/customJobOrder/JobOrderEdit.html");
    }
    
    
    //根据工作单类型生成不同前缀
    public String generateJobPrefix(String type){
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
			return prefix;
    }
    
    @SuppressWarnings("unchecked")
	@Before(Tx.class)
   	public void save() throws Exception {	
    	
   		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String type = (String) dto.get("type");//根据工作单类型生成不同前缀
        
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		Record r = new Record();
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			r = Db.findById("custom_job_order", id);
   			
   			if(type!=r.get("type")){
	   			String order_no = OrderNoGenerator.getNextOrderNo(generateJobPrefix(type));
	            r.set("order_no", order_no);
   			}
            
   			r.set("updator", user.getLong("id"));
   			r.set("update_stamp", new Date());
   			DbUtils.setModelValues(dto, r, "custom_job_order");;
   			
   			Db.update("custom_job_order",r);
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, r, "custom_job_order");
   			
   			//需后台处理的字段
   			String order_no = OrderNoGenerator.getNextOrderNo(generateJobPrefix(type));
            r.set("order_no", order_no);
   			r.set("creator", user.getLong("id"));
   			r.set("create_stamp", new Date());
   			r.set("office_id", office_id);
   			Db.save("custom_job_order",r);
   			id = r.getLong("id").toString();
   			
   		}
		
		//海运
		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
		DbUtils.handleList(shipment_detail, "custom_job_order_shipment", id, "order_id");
		List<Map<String, String>> shipment_item = (ArrayList<Map<String, String>>)dto.get("shipment_item");
		DbUtils.handleList(shipment_item, "custom_job_order_shipment_item", id, "order_id");
		//空运
		List<Map<String, String>> air_detail = (ArrayList<Map<String, String>>)dto.get("air_detail");
		DbUtils.handleList(air_detail, "custom_job_order_air", id, "order_id");
		List<Map<String, String>> air_item = (ArrayList<Map<String, String>>)dto.get("air_item");
		DbUtils.handleList(air_item, "custom_job_order_air_item", id, "order_id");
		List<Map<String, String>> air_cargoDescItem = (ArrayList<Map<String, String>>)dto.get("air_cargoDescItem");
		DbUtils.handleList(air_cargoDescItem, "custom_job_order_air_cargodesc", id, "order_id");
		
		//陆运
		List<Map<String, String>> land_item = (ArrayList<Map<String, String>>)dto.get("load_item");
		DbUtils.handleList(land_item, "custom_job_order_land", id, "order_id");
		//费用明细
		List<Map<String, String>> charge_item = (ArrayList<Map<String, String>>)dto.get("charge_item");
		DbUtils.handleList(charge_item, "custom_job_order_charge", id, "order_id");
		List<Map<String, String>> cost_item = (ArrayList<Map<String, String>>)dto.get("cost_item");
		DbUtils.handleList(cost_item, "custom_job_order_cost", id, "order_id");
		
		//报关
		List<Map<String, String>> custom_detail = (ArrayList<Map<String, String>>)dto.get("custom_detail");
		DbUtils.handleList(custom_detail, "custom_job_order_custom", id, "order_id");
		List<Map<String, String>> custom_item = (ArrayList<Map<String, String>>)dto.get("custom_item");
		DbUtils.handleList(custom_item, "custom_job_order_custom_item", id, "order_id");
		
		long creator = r.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
   		r.set("creator_name", user_name);
   		
   		r.set("shipment", getItemDetail(id,"shipment"));
   		r.set("air", getItemDetail(id,"air"));
    	r.set("custom", getItemDetail(id,"custom"));
   		renderJson(r);
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
			Db.save("custom_job_order_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
  //上传陆运签收文件描述
    @Before(Tx.class)
    public void uploadSignDesc(){
    	String id = getPara("id");
    	List<UploadFile> fileList = getFiles("\\");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		Record order = Db.findById("custom_job_order_land", id);
    		String sign_desc = order.getStr("sign_desc");
    		if("".equals(sign_desc)||sign_desc==null){
    			order.set("sign_desc", fileName);
    		}else{
    			order.set("sign_desc", sign_desc+","+fileName);
    		}
    		Db.update("custom_job_order_land",order);
		}
		renderJson("{\"result\":true}");
    }
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("custom_job_order_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("custom_job_order_doc",r);
            resultMap.put("result", result);
        }else{
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }

    //返回对象	
    private Record getItemDetail(String id,String type){
    	Record re = null;
    	if("shipment".equals(type)){
    		re = Db.findFirst("select jos.*, p1.abbr shipperAbbr , p2.abbr consigneeAbbr, p3.abbr notify_partyAbbr, p4.abbr carrier_name,"
    				+ " p5.abbr head_carrier_name,p6.abbr oversea_agent_name,p7.abbr booking_agent_name,"
    				+ " lo.name por_name,lo1.name pol_name,lo2.name pod_name, lo3.name fnd_name,lo4.name hub_name"
    				+ " from custom_job_order_shipment jos "
    				+ " left join party p1 on p1.id=jos.shipper"
    				+ " left join party p2 on p2.id=jos.consignee"
    				+ " left join party p3 on p3.id=jos.notify_party"
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
    	}else if("custom".equals(type)){
    		re = Db.findFirst("select * from custom_job_order_custom where order_id = ?",id);
    	}else if("air".equals(type)){
    		re = Db.findFirst("select joa.* ,p1.abbr shipperAbbr,p2.abbr consigneeAbbr,p3.abbr notify_partyAbbr,p4.abbr booking_agent_name from custom_job_order_air joa"
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
    		itemSql = "select jos.*,CONCAT(u.name,u.name_eng) unit_name from custom_job_order_shipment_item jos"
    				+ " left join unit u on u.id=jos.unit_id"
    				+ " where order_id=? order by jos.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("air".equals(type)){
    		itemSql = "select joa.*, pa.abbr air_company_name from custom_job_order_air_item joa"
    		        + " left join party pa on pa.id=joa.air_company"
    		        + " where order_id=? order by joa.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("cargoDesc".equals(type)){
    		itemSql = "select * from custom_job_order_air_cargodesc where order_id=? order by id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("land".equals(type)){
    		itemSql = "select jol.*, p.abbr transport_company_name,"
    		        + " p1.abbr consignor_name, p2.abbr consignee_name from custom_job_order_land jol "
    				+ " left join party p on p.id=jol.transport_company"
    				+ " left join party p1 on p1.id=jol.consignor"
    				+ " left join party p2 on p2.id=jol.consignee"
    				+ " where order_id=? order by jol.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("charge".equals(type)){
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name from custom_job_order_charge jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " where order_id=? order by jor.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("cost".equals(type)){
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name from custom_job_order_cost jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
	    	        + " where order_id=? order by jor.id";
	    	itemList = Db.find(itemSql, orderId);
    	}else if("doc".equals(type)){
	    	itemSql = "select jod.*,u.c_name from custom_job_order_doc jod left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=? order by jod.id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("mail".equals(type)){
	    	itemSql = "select * from custom_job_order_sendMail where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
	    }else if("custom".equals(type)){
            itemSql = "select * from custom_order where job_order_id=? order by id";
            itemList = Db.find(itemSql, orderId);
        }else if("customItem".equals(type)){
	    	itemSql = "select * from custom_job_order_custom_item where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
        }
		return itemList;
	}
    
    @Before(Tx.class)
    public void edit() {
    	String id = getPara("id");
    	String sql = "select c.*,u.c_name,p.abbr from custom_job_order c "
    			+ " left join user_login u on u.id = c.creator"
    			+ " left join party p on p.id = c.customer_id"
    			+ " where c.id = ?";
    	Record r = Db.findFirst(sql,id);
    	setAttr("order", r);
    	//报关
        setAttr("custom", getItemDetail(id,"custom"));
//        setAttr("customList", getItems(id,"custom"));
        setAttr("customItemList", getItems(id,"customItem"));
        //空运
        setAttr("airList", getItems(id,"air"));
    	setAttr("cargoDescList", getItems(id,"cargoDesc"));
    	setAttr("air", getItemDetail(id,"air"));
    	//获取海运明细表信息
    	setAttr("shipmentList", getItems(id,"shipment"));
    	setAttr("shipment", getItemDetail(id,"shipment"));
    	//获取陆运明细表信息
    	setAttr("landList", getItems(id,"land"));
    	//获取费用明细
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	//相关文档
    	setAttr("docList", getItems(id,"doc"));
    	//邮件记录
    	setAttr("mailList", getItems(id,"mail"));
    	//当前登陆用户
    	setAttr("loginUser", LoginUserController.getLoginUserName(this));
    	
        render("/cms/customJobOrder/JobOrderEdit.html");
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
    	
        MultiPartEmail email = new MultiPartEmail();  
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
        	String[] arr = userEmail.split("\\s+|,|，|;|；");//以空格或 ， ,；;分割
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
        	String[] arr = ccEmail.split("\\s+|,|，|;|；");//以空格或 ， ,；;分割
        	for(int i=0;i<arr.length;i++){
        		email.addCc(arr[i]);
        	}
        }
        //密送
        if(StringUtils.isNotEmpty(bccEmail)){
        	String[] arr = bccEmail.split("\\s+|,|，|;|；");//以空格或 ， ,；;分割
        	for(int i=0;i<arr.length;i++){
        		email.addBcc(arr[i]);
        	}
        }
        
        //添加附件
        if(StringUtils.isNotEmpty(docs)){
    		String strAry[] = docs.split(",");
	        for(int i=0;i<strAry.length;i++){
	        	
	        	String filePath = getRequest().getServletContext().getRealPath("/")+"\\upload\\doc\\"+strAry[i];
	            File file = new File(filePath);
	            if (file.exists() && file.isFile()) {
	            	
	            	EmailAttachment attachment = new EmailAttachment();
	            	attachment.setPath(filePath);  
	            	attachment.setDisposition(EmailAttachment.ATTACHMENT); 
	            	attachment.setName(strAry[i]); 
	            	email.attach(attachment);
	            }
	        }
        }
        try{
        	email.setCharset("gbk"); 
        	email.send();
        	Record jsm = new Record();
        	jsm.set("order_id", order_id);
        	jsm.set("mail_title", mailTitle);
        	jsm.set("doc_name", docs.replace(",", "  "));
        	jsm.set("receive_mail", userEmail);
        	jsm.set("cc_mail", ccEmail);
        	jsm.set("bcc_mail", bccEmail);
        	jsm.set("sender", LoginUserController.getLoginUserName(this));
        	jsm.set("send_time", new Date());
        	Db.save("custom_job_order_sendMail",jsm);
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
    	String customer_code=getPara("customer_code")==null?"":getPara("customer_code");
    	String customer_name=getPara("customer")==null?"":getPara("customer");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        if("sowait".equals(type)){
        	sql=" SELECT jor.*,ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order jor "
        			+ " LEFT JOIN job_order_shipment jos on jor.id = jos.order_id "
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator "
        			+ " WHERE jor.office_id="+office_id
        			+ " and jor.type = '出口柜货' AND jos.SONO IS NULL AND jor.transport_type LIKE '%ocean%'";        	
        }else if("truckorderwait".equals(type)){
        	 sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order_land_item joli"
        			+ " left join job_order jor on jor.id = joli.order_id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
        			+ " and datediff(joli.eta, now()) <= 3 AND (joli.truckorder_flag != 'Y' OR joli.truckorder_flag IS NULL)"
        			+ " AND jor.transport_type LIKE '%land%'";
        	
        	
        } else if("siwait".equals(type)){
        	 sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        	 		+ " FROM job_order_shipment jos"
        	 		+ " left join job_order jor on jos.order_id = jor.id"
        	 		+ " left join party p on p.id = jor.customer_id"
        	 		+ " left join user_login u on u.id = jor.creator "
        	 		+ " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(export_date)=TO_DAYS(now())";
        	
        } else if("mblwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order_shipment jos "
        			+ " left join job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and  jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null)";
        	
        } else if("customwait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,jos.export_date sent_out_time "
        			+ " from job_order jor "
        			+ "	LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ "	where jor.office_id="+office_id
                    + " and  jor.transport_type LIKE '%custom%'"
        			+ "	and ifnull(joc.custom_type,'') = ''";
        	
        } else if("insurancewait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order jor LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
        			+ " left join job_order_shipment jos on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and  jor.transport_type LIKE '%insurance%' and joi.insure_no is NULL";
        } else if("overseacustomwait".equals(type)){
        	sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order_shipment jos "
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y' "
        			+ " and timediff(now(),jos.etd)<TIME('48:00:00') ";
        } else if("tlxOrderwait".equals(type)){
        	sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name, jos.export_date sent_out_time"
        			+ " FROM job_order_shipment jos"
        			+ " LEFT JOIN job_order jor on jos.order_id = jor.id"
        			+ " left join party p on p.id = jor.customer_id"
        			+ " left join user_login u on u.id = jor.creator"
        			+ " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(jos.etd)= TO_DAYS(now())";
        }
        else{
        	
         sql = "SELECT * from (select jo.*, jos.export_date sent_out_time, ifnull(u.c_name, u.user_name) creator_name, p.abbr customer_name,p.code "
    			+ " from job_order jo "
    			+ " left join job_order_shipment jos on jos.order_id = jo.id"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " left join user_login u on u.id = jo.creator"
    			+ " where jo.office_id="+office_id
                + " and abbr like '%"
    			+ customer_name
    			+ "%' and code like '%"
    			+ customer_code
    			+ "%' )A"
         	    + " where 1 =1 ";
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
  
    public void searchContacts(){
    	String id = getPara("id");
    	String sql = "select ifnull(phone,'') phone, (CASE WHEN ISNULL(contact_person) THEN ifnull(contact_person_eng,'') ELSE contact_person END) contacts"
    			+ " from party where id = ?";
    	Record r = Db.findFirst(sql,id);
    	renderJson(r);
    }
    
}
