package controllers.arap.ar;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AppInvoiceDoc;
import models.ArapAccountAuditLog;
import models.ArapChargeApplication;
import models.ArapChargeInvoice;
import models.ArapChargeOrder;
//import models.ChargeAppOrderRel;
import models.ChargeApplicationOrderRel;
import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.damageOrder.DamageOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeAcceptOrderController extends Controller {
    private Log logger = Log.getLog(ChargeAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	render("/eeda/arap/ChargeAcceptOrder/ChargeAcceptOrderList.html");
    }
    
    @Before(EedaMenuInterceptor.class) 
    public void create() {
		String idsArray = getPara("idsArray");
		setAttr("idsArray", idsArray);
		
		String[] orderArrId=idsArray.split(",");
		Record r = Db.findFirst("select aco.sp_id,ifnull(company_name,company_name_eng) payee_unit,ifnull(contact_person,contact_person_eng) payee_name"
				+ " from arap_charge_order aco left join party p on p.id = aco.sp_id where aco.id = ?",orderArrId[0]);

		setAttr("sp_id", r.get("SP_ID"));
		setAttr("payee_unit", r.getStr("PAYEE_UNIT"));
		setAttr("payee_name", r.getStr("PAYEE_NAME"));
			
		List<Record> Account = null;
		Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/ChargeAcceptOrder/chargeEdit.html");
	}
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = " select * from ("
        		+ " select  aco.*, p.company_name sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount,"
        		+ " sum(ifnull(c.paid_usd,0)) paid_usd,"
        		+ " sum(ifnull(c.paid_cny,0)) paid_cny,"
        		+ " sum(ifnull(c.paid_hkd,0)) paid_hkd,"
        		+ " sum(ifnull(c.paid_jpy,0)) paid_jpy,"
        		+ " group_concat((select concat(order_no,'-',status) from arap_charge_application_order where id = c.application_order_id) SEPARATOR '<br/>') app_msg"
				+ " from arap_charge_order aco "
				+ " left join charge_application_order_rel c on c.charge_order_id=aco.id"
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.status!='新建' and aco.office_id = "+office_id
				+ " group by aco.id"
				+ " ) A where (ifnull(usd,0)>paid_usd or ifnull(cny,0)>paid_cny or ifnull(hkd,0)>paid_hkd or ifnull(jpy,0)>paid_jpy)";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition +") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    public void applicationList() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select acao.*, acao.order_no application_order_no, "
        		+ " '申请单' order_type,aco.order_no cost_order_no,u.c_name "
				+ " from arap_charge_application_order acao "
				+ " left join charge_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join arap_charge_order aco on aco.id = caor.charge_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ "	where acao.office_id = "+office_id
				+ " group by acao.id"
				+ " ) B where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }

  	//新逻辑
  	public void chargeOrderList() {
          String ids = getPara("idsArray");
          String application_id = getPara("application_id");
          String sql = "";
          
          if("".equals(application_id)||application_id==null){
  	       
          	sql = " SELECT aco.*, p.company_name payee_name, '应收对账单' order_type,"
					+ " p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
					+ " sum(ifnull(c.pay_amount,0)) paid_amount,"
					+ " sum(ifnull(c.paid_usd,0)) paid_usd,"
					+ " sum(ifnull(c.paid_cny,0)) paid_cny,"
					+ " sum(ifnull(c.paid_jpy,0)) paid_jpy,"
					+ " sum(ifnull(c.paid_hkd,0)) paid_hkd"
					+ " FROM arap_charge_order aco "
					+ " LEFT JOIN charge_application_order_rel c on c.charge_order_id = aco.id"
					+ " LEFT JOIN party p ON p.id = aco.sp_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " WHERE aco.id in(" + ids +") "
					+ " group by aco.id ";

  		}else{
  			sql = " SELECT aco.*, p.company_name payee_name, '应收对账单' order_type,"
					+ " p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
					+ " (select sum(ifnull(c.pay_amount, 0)) from  charge_application_order_rel c where c.charge_order_id = aco.id) paid_amount,"
					+ " (select sum(ifnull(c.paid_usd, 0)) from  charge_application_order_rel c where c.charge_order_id = aco.id) paid_usd,"
					+ " (select sum(ifnull(c.paid_cny, 0)) from  charge_application_order_rel c where c.charge_order_id = aco.id) paid_cny,"
					+ " (select sum(ifnull(c.paid_jpy, 0)) from  charge_application_order_rel c where c.charge_order_id = aco.id) paid_jpy,"
					+ " (select sum(ifnull(c.paid_hkd, 0)) from  charge_application_order_rel c where c.charge_order_id = aco.id) paid_hkd,"
					+ " acao.app_usd, acao.app_cny, acao.app_hkd, acao.app_jpy, acao.id app_id "
					+ " FROM arap_charge_order aco "
					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aco.id"
					+ " LEFT JOIN arap_charge_application_order acao on acao.id = caor.application_order_id"
					+ " LEFT JOIN party p ON p.id = aco.sp_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " where acao.id="+application_id
				    + " GROUP BY aco.id ";    
  		}
  		
  		Map BillingOrderListMap = new HashMap();
  		List<Record> recordList= Db.find(sql);
        BillingOrderListMap.put("draw", recordList.size());
        BillingOrderListMap.put("recordsTotal", recordList.size());
        BillingOrderListMap.put("recordsFiltered", recordList.size());
        BillingOrderListMap.put("data", recordList);
        

        renderJson(BillingOrderListMap);
  	}
  	
  	
  	
  	@Before(Tx.class)
	public void save() throws InstantiationException, IllegalAccessException {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        ArapChargeApplication order = new ArapChargeApplication();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = ArapChargeApplication.dao.findById(id);
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSSQ", user.getLong("office_id")));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.set("office_id", office_id);
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> docList = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(docList, id, AppInvoiceDoc.class, "order_id");
   		
   		ChargeApplicationOrderRel caor = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String action = item.get("action");
			String itemId = item.get("id");
			String app_usd = item.get("app_usd");
			String app_hkd = item.get("app_hkd");
			String app_cny = item.get("app_cny");
			String app_jpy = item.get("app_jpy");
			String order_type = item.get("order_type");
			if("CREATE".equals(action)){
				caor = new ChargeApplicationOrderRel();
				caor.set("application_order_id", id);
				caor.set("charge_order_id", itemId);
				caor.set("order_type", order_type);
				
				caor.set("paid_usd", app_usd);
				caor.set("paid_hkd", app_hkd);
				caor.set("paid_cny", app_cny);
				caor.set("paid_jpy", app_jpy);
				caor.save();
				
                if("应收对账单".equals(order_type)){
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(itemId);
					arapChargeOrder.set("audit_status", "收款申请中").update();
				}
			}else{
				caor = ChargeApplicationOrderRel.dao.findFirst("select * from charge_application_order_rel where charge_order_id =? and application_order_id = ?",itemId,id);
				caor.set("paid_usd", app_usd);
				caor.set("paid_hkd", app_hkd);
				caor.set("paid_cny", app_cny);
				caor.set("paid_jpy", app_jpy);
				caor.update();
			}
		}
		
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
	}
  	
  	@Before(EedaMenuInterceptor.class)
  	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeApplication order = ArapChargeApplication.dao.findById(id);
		
		Party p  = Party.dao.findById(order.getLong("sp_id"));
		if(p != null){
			setAttr("party", p);
		}
		
		UserLogin userLogin = null;
		userLogin = UserLogin.dao .findById(order.get("create_by"));
		String creator_name = userLogin.get("c_name");
		
		userLogin = UserLogin.dao .findById(order.get("check_by"));
		String check_name = null;
		if(userLogin != null){
			check_name = userLogin.get("c_name");
		}
		
		List<Record> list = null;
    	list = getItems(id);
    	setAttr("docList", list);
		
		Record r = order.toRecord();
		r.set("creator_name", creator_name);
		r.set("check_name", check_name);
		setAttr("order", r);

		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/ChargeAcceptOrder/chargeEdit.html");
	}
  	
  	
    //复核
  	@Before(Tx.class)
    public void checkOrder(){
        String application_id=getPara("order_id");
          
        ArapChargeApplication order = ArapChargeApplication.dao.findById(application_id);
        order.set("status", "已复核");
        order.set("check_by", LoginUserController.getLoginUserId(this));
        order.set("check_stamp", new Date()).update();
     
        //更改原始单据状态
        List<Record> res = Db.find("select * from charge_application_order_rel where application_order_id = ?",application_id);
  		for (Record re : res) {
  			Long id = re.getLong("charge_order_id");
  			String order_type = re.getStr("order_type");

  			if("应收对账单".equals(order_type)){
			    ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				arapChargeOrder.set("audit_status", "已复核").update();
			}
  		}
  		  
  		long check_by = order.getLong("check_by");
   		String user_name = LoginUserController.getUserNameById(check_by);
  		  
  		Record re = order.toRecord();
  		re.set("check_name",user_name);
  	    renderJson(re);
    }
  	
  	
    //收款确认
  	@Before(Tx.class)
	public void confirmOrder(){
  		String jsonStr=getPara("params");
  		int receive_bank = 4;
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
   		String id = (String) dto.get("id");
   		String receive_type = (String) dto.get("receive_type");
   		if(StringUtils.isNotEmpty((String)dto.get("receive_bank"))){
   			receive_bank = Integer.parseInt((String)dto.get("receive_bank")) ;
   		}
   		
   		String receive_time = (String) dto.get("receive_time");
   		String receive_amount = (String) dto.get("receive_amount");
   		
        ArapChargeApplication arapChargeInvoiceApplication = ArapChargeApplication.dao.findById(id);
        arapChargeInvoiceApplication.set("status", "已收款");
        arapChargeInvoiceApplication.set("receive_type", receive_type);
        arapChargeInvoiceApplication.set("receive_bank_id", receive_bank);
        arapChargeInvoiceApplication.set("receive_time", receive_time);
        arapChargeInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
        arapChargeInvoiceApplication.set("confirm_stamp", new Date());
        arapChargeInvoiceApplication.update();
          
        //更改原始单据状态
        List<Record> res = Db.find("select * from charge_application_order_rel where application_order_id = ?",id);
  		for (Record re : res) {
  			Long charge_order_id = re.getLong("charge_order_id");
  			String order_type = re.getStr("order_type");

  			if("应收对账单".equals(order_type)){
			    ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(charge_order_id);
				arapChargeOrder.set("audit_status", "已收款").update();
			}
  		}
          
        //新建日记账表数据
  		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", receive_type);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
        auditLog.set("amount", receive_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("account_id", receive_bank);
        auditLog.set("source_order", "应收申请单");
        auditLog.set("invoice_order_id", id);
        auditLog.save();
                  
        renderJson("{\"success\":true}");  
    }
  	
    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
    	String order_id = getPara("order_id");
    	List<UploadFile> fileList = getFiles("doc");
    	
    	AppInvoiceDoc order = new AppInvoiceDoc();
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();

			order.set("order_id", order_id);
			order.set("uploader", LoginUserController.getLoginUserId(this));
			order.set("doc_name", fileName);
			order.set("type", "charge");
			order.set("upload_time", new Date());
			order.save();
		}

    	renderJson(order);
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	
    	List<Record> list = null;
    	list = getItems(order_id);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());

        map.put("aaData", list);

        renderJson(map); 
    }
    
  //返回list
    private List<Record> getItems(String orderId) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	
    	itemSql = "select aid.*,u.c_name from app_invoice_doc aid left join user_login u on aid.uploader=u.id "
    			+ " where aid.order_id=? order by aid.id desc";
    	itemList = Db.find(itemSql, orderId);
	    
		return itemList;
	}
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	AppInvoiceDoc order = AppInvoiceDoc.dao.findById(id);
    	String fileName = order.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            order.delete();
            resultMap.put("result", result);
        }else{
        	order.delete();
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }

}
