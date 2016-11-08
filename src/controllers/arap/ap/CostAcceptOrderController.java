package controllers.arap.ap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AppInvoiceDoc;
import models.ArapAccountAuditLog;
import models.ArapCostApplication;
import models.ArapCostOrder;
import models.ArapMiscCostOrder;
import models.CostApplicationOrderRel;
import models.Party;
import models.UserLogin;
import models.eeda.profile.Account;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.arap.prePayOrder.ArapPrePayOrder;
import models.yh.carmanage.CarSummaryOrder;
import models.yh.damageOrder.DamageOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
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
public class CostAcceptOrderController extends Controller {
    private Log logger = Log.getLog(CostAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {   
    	render("/eeda/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }
    
    
	@Before(Tx.class)
	public void save() throws InstantiationException, IllegalAccessException {
		String ids = getPara("ids");
		ArapCostApplication aca = null;
		String application_id = getPara("application_id");
		String paymentMethod = getPara("payment_method");//付款方式
		String bank_no = getPara("bank_no");          //收款账号
		String payee_name = getPara("payee_name");    //收款人
		String numname = getPara("account_name");   //账户名
		String payee_unit = getPara("payee_unit");      //收款单位
		String payee_id = getPara("payee_id")==""?null:getPara("payee_id");         //付款给
		String billing_unit = getPara("billing_unit"); //收款单位
		String billtype = getPara("invoice_type");   //开票类型
		String bank_name= getPara("deposit_bank");   //开户行
		String invoice_no= getPara("invoice_no"); 
		String total_amount = getPara("total_amount")==""?"0.00":getPara("total_amount");   //申请总金额
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
		
		if (!"".equals(application_id) && application_id != null) {
			aca = ArapCostApplication.dao.findById(application_id);
			aca.set("last_modified_by",LoginUserController.getLoginUserId(this));
			aca.set("last_modified_stamp", new Date());
			aca.set("payee_name", payee_name);
			aca.set("payment_method", paymentMethod);
			aca.set("payee_unit", payee_unit);
			aca.set("billing_unit", billing_unit);
			aca.set("bill_type", billtype);
			aca.set("bank_no", bank_no);
			aca.set("invoice_no", invoice_no);
			aca.set("bank_name", bank_name);
			aca.set("num_name", numname);
			if (total_amount != null && !"".equals(total_amount)) {
				aca.set("total_amount",total_amount);
			}
			aca.update();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");

				CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst("select * from cost_application_order_rel where cost_order_id =? and application_order_id = ?",id,application_id);
				costApplicationOrderRel.set("application_order_id", aca.getLong("id"));
				costApplicationOrderRel.set("cost_order_id", id);
				costApplicationOrderRel.set("order_type", order_type);
				costApplicationOrderRel.set("pay_amount", value);
				costApplicationOrderRel.update();
			}
		} else {
			aca = new ArapCostApplication();
			aca.set("order_no",OrderNoGenerator.getNextOrderNo("YFSQ", office_id));
			aca.set("status", "新建");
			aca.set("create_by", LoginUserController.getLoginUserId(this));
			aca.set("create_stamp", new Date());
			aca.set("payee_name", payee_name);
			aca.set("payment_method", paymentMethod);
			aca.set("office_id", office_id);
			aca.set("payee_unit", payee_unit);
			aca.set("invoice_no", invoice_no);
			aca.set("billing_unit", billing_unit);
			aca.set("bill_type", billtype);
			aca.set("bank_no", bank_no);
			aca.set("bank_name", bank_name);
			aca.set("num_name", numname);
			aca.set("payee_id", payee_id);
		
			if (total_amount != null && !"".equals(total_amount)) {
				aca.set("total_amount",total_amount);
			}
			aca.save();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");
				String cname = (String)map.get("payee_unit");

				CostApplicationOrderRel costApplicationOrderRel = new CostApplicationOrderRel();
				costApplicationOrderRel.set("application_order_id", aca.getLong("id"));
				costApplicationOrderRel.set("cost_order_id", id);
				costApplicationOrderRel.set("order_type", order_type);
				costApplicationOrderRel.set("pay_amount", value);
				
				if(cname!=null)
					costApplicationOrderRel.set("payee_unit", cname);
				costApplicationOrderRel.save();
				
                if(order_type.equals("应付对账单")){
					ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
					arapCostOrder.set("status", "付款申请中").update();
				}else if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
					arapMiscCostOrder.set("audit_status", "付款申请中").update();
				}
			}

		}
		
		String docStr=getPara("docJson"); 
		Gson gson = new Gson();
		List<Map<String, String>> docList = new Gson().fromJson(docStr, 
				new TypeToken<List<Map<String, String>>>(){}.getType());
		
		DbUtils.handleList(docList, aca.getLong("id").toString(), AppInvoiceDoc.class, "order_id");
		
		renderJson(aca);
	}
    
    
    
  //复核
	@Before(Tx.class)
    public void checkStatus(){
        String application_id=getPara("application_id");
        
        ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
        arapCostInvoiceApplication.set("status", "已复核");
        arapCostInvoiceApplication.set("check_by", LoginUserController.getLoginUserId(this));
        arapCostInvoiceApplication.set("check_stamp", new Date()).update();
        
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			
			if(order_type.equals("应付对账单")){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				Double total_amount = arapCostOrder.getDouble("total_amount");
				Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '应付对账单'",id);
				Double paid_amount = re.getDouble("total");
				if(!total_amount.equals(paid_amount)){
					arapCostOrder.set("status", "部分已复核").update();
				}else
					arapCostOrder.set("status", "已复核").update();
			}		
		}
		Record r = arapCostInvoiceApplication.toRecord();
		String check_name = LoginUserController.getUserNameById(arapCostInvoiceApplication.getLong("check_by").toString());
		r.set("check_name", check_name);
		renderJson(r);
    }
	
	//退回
	@Before(Tx.class)
    public void returnOrder(){
        String application_id=getPara("application_id");
        
        ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
        arapCostInvoiceApplication.set("status", "新建");
        arapCostInvoiceApplication.set("return_by", LoginUserController.getLoginUserId(this));
        arapCostInvoiceApplication.set("return_stamp", new Date()).update();
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			
			if(order_type.equals("应付对账单")){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				arapCostOrder.set("status", "付款申请中").update();
			}
		}
		renderJson("{\"success\":true}");
    }
	
	
	
	//撤销申请单据
	@Before(Tx.class)
    public void deleteOrder(){
		//先更改对应的单据状态
		//删除从表数据
		//删除主单据数据
		
        String application_id=getPara("application_id");
        //删除从表数据
        String sql = "select * from cost_application_order_rel "
				+ " where application_order_id = ?";
		List<CostApplicationOrderRel> rel = CostApplicationOrderRel.dao.find(sql,application_id);
		for(CostApplicationOrderRel crel:rel){
			long id = crel.getLong("cost_order_id");
			String order_type = crel.getStr("order_type");
			
			//修改相关单据状态
			if(order_type.equals("应付对账单")){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				arapCostOrder.set("status", "已确认").update();
			}
			
			//删除从表数据
			crel.delete();
		}
		
		//删除主表数据
		ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
        arapCostInvoiceApplication.delete();
	        
		
		renderJson("{\"success\":true}");
    }
	
	
    
	//付款确认
	@Before(Tx.class)
    public void confirmOrder(){
        String application_id=getPara("application_id");
        String pay_type = getPara("pay_type");
        String pay_bank_id = getPara("pay_bank");
        String pay_time = getPara("pay_time");
        
        if( pay_time==null||pay_time.equals("")){
   			pay_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
   		}
        
        ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
        String pay_amount = arapCostInvoiceApplication.getDouble("total_amount").toString();
        arapCostInvoiceApplication.set("status", "已付款");
        arapCostInvoiceApplication.set("pay_type", pay_type);
        if(pay_bank_id != null && !pay_bank_id.equals(""))
        	arapCostInvoiceApplication.set("confirm_bank_id", pay_bank_id);
        else{
        	arapCostInvoiceApplication.set("confirm_bank_id", 4);
        }
        if(pay_time==null || pay_time.equals(""))
        	arapCostInvoiceApplication.set("pay_time", new Date());
        else
        	arapCostInvoiceApplication.set("pay_time", pay_time);
	        arapCostInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
	        arapCostInvoiceApplication.set("confirm_stamp", new Date());
	        arapCostInvoiceApplication.update();
        
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			if(order_type.equals("应付对账单")){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				Double total_amount = arapCostOrder.getDouble("cost_amount");
				Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '应付对账单'",id);
				Double paid_amount = re.getDouble("total");
				if(total_amount!=paid_amount){
					arapCostOrder.set("status", "部分已付款").update();
				}else
					arapCostOrder.set("status", "已付款").update();
			}
		}
        
        
        
       //新建日记账表数据
		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", pay_type);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_COST);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", pay_time);
        if(pay_bank_id!=null && !pay_bank_id.equals("") )
        	auditLog.set("account_id", pay_bank_id);
        else
        	auditLog.set("account_id", 4);
        auditLog.set("source_order", "应付申请单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
                
        renderJson("{\"success\":true}");  
    }
	
	
	
	//付款确认退回
	//同时更新日记账里面的数据（退回金额）
	@Before(Tx.class)
    public void returnConfirmOrder(){
        String application_id=getPara("application_id");
        
        ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
        arapCostInvoiceApplication.set("status", "已复核");
        arapCostInvoiceApplication.set("return_confirm_by", LoginUserController.getLoginUserId(this));
        arapCostInvoiceApplication.set("return_confirm_stamp", new Date());
        arapCostInvoiceApplication.update();
       
        //更改原始单据状态
        String strJson = getPara("detailJson");
		Gson gson = new Gson();
		List<Map> idList = new Gson().fromJson(strJson, 
				new TypeToken<List<Map>>(){}.getType());
		for (Map map : idList) {
			String id = (String)map.get("id");
			String order_type = (String)map.get("order_type");

			if(order_type.equals("应付对账单")){
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
				Double total_amount = arapCostOrder.getDouble("cost_amount");
				Record re = Db.findFirst("select sum(pay_amount) total from cost_application_order_rel where cost_order_id =? and order_type = '应付对账单'",id);
				Double paid_amount = re.getDouble("total");
				if(!total_amount.equals(paid_amount)){
					arapCostOrder.set("status", "部分已复核").update();
				}else
					arapCostOrder.set("status", "已复核").update();
			}	
		}
		
		
		//撤销对应日记账信息
		ArapAccountAuditLog arapAccountAuditLog = ArapAccountAuditLog
				.dao.findFirst("select * from arap_account_audit_log where source_order = '应付申请单' and payment_type = 'COST' and invoice_order_id = ? ",application_id);
		arapAccountAuditLog.delete();

		renderJson("{\"success\":true}");
        
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
        		+ " select  aco.id, aco.order_no, aco.order_type, aco.status, aco.create_stamp, aco.total_amount totalCostAmount, aco.sp_id, p.company_name sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount"
				+ " from arap_cost_order aco "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id"
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.status!='新建' and aco.office_id = "+office_id
				+ " group by aco.id"
				+ " ) A where totalCostAmount>paid_amount";

        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc "+sLimit);
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
        		+ " select acao.id,acao.order_no application_order_no,acao.status,acao.payment_method,acao.create_stamp,acao.check_stamp,acao.pay_time, "
        		+ " acao.remark,acao.payee_unit,acao.payee_name, "
        		+ " '申请单' order_type,acao.total_amount,aco.order_no cost_order_no,u.c_name "
				+ " from arap_cost_application_order acao "
				+ " left join cost_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join arap_cost_order aco on aco.id = caor.cost_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ "	where acao.office_id = "+office_id
				+ " group by acao.id"
				+ " ) B where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " + sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    

    public void costOrderList() {
        String ids = getPara("ids");
        String application_id = getPara("application_id");
        String sql = "";
        if("".equals(application_id)||application_id==null){
		
			sql = " SELECT aco.id,aco.sp_id, p.company_name payee_name,aco.order_no, '应付对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount, "
					+ " sum(ifnull(c.pay_amount,0)) paid_amount,"
					+ " null item_ids,null payee_unit "
					+ " FROM arap_cost_order aco "
					+ " LEFT JOIN cost_application_order_rel c on c.cost_order_id = aco.id"
					+ " LEFT JOIN party p ON p.id = aco.sp_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " WHERE aco.id in(" + ids +") "
					+ " group by aco.id ";
		}else{
			
			sql = " SELECT aco.id,aco.sp_id, p.company_name payee_name,aco.order_no, '应付对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
					+ " p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.total_amount,"
					+ " (select sum(ifnull(c.pay_amount, 0)) from  cost_application_order_rel c where c.cost_order_id = aco.id) paid_amount,"
					+ " caor.pay_amount application_amount, acao.id app_id "
					+ " FROM arap_cost_order aco "
					+ " LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id"
					+ " LEFT JOIN arap_cost_application_order acao on acao.id = caor.application_order_id"
					+ " LEFT JOIN party p ON p.id = aco.sp_id"
					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+ " where acao.id="+application_id
				    + " GROUP BY aco.id ";
		}
		
		Map BillingOrderListMap = new HashMap();
		List<Record> recordList= Db.find(sql);
        BillingOrderListMap.put("iTotalRecords", recordList.size());
        BillingOrderListMap.put("iTotalDisplayRecords", recordList.size());
        BillingOrderListMap.put("aaData", recordList);

        renderJson(BillingOrderListMap);
	}
	
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        String ids = getPara("itemIds");
        String[] idArr=ids.split(",");
        setAttr("ids",ids);
        
        String payee_id = "";
        String payee_filter = "";
        String payee_name = "";
        String deposit_bank = "";
        String bank_no = "";
        String account_name = "";

        ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(idArr[0]);
        payee_id = arapCostOrder.getLong("sp_id").toString();

        if(StringUtils.isNotEmpty(payee_id)){
            Party contact = Party.dao.findFirst("select * from  party where id = ?",payee_id);
            payee_filter = contact.getStr("company_name");
            deposit_bank = contact.getStr("bank_name");
            bank_no = contact.getStr("bank_no");
            account_name = contact.getStr("receiver");
        }
        setAttr("payee_filter", payee_filter);
        setAttr("deposit_bank", deposit_bank);
        setAttr("bank_no", bank_no);
        setAttr("account_name", account_name);
        
        setAttr("payee_id", payee_id);
        setAttr("payee_name", payee_name);
        
            
        List<Record> Account = null;
        Account = Db.find("select * from fin_account where bank_name != '现金'");
        setAttr("accountList", Account);
        
        setAttr("submit_name", LoginUserController.getLoginUserName(this));
        setAttr("saveOK", false);
        setAttr("status", "new");
        render("/eeda/arap/CostAcceptOrder/payEdit.html");
    } 
    
    @Before(EedaMenuInterceptor.class) 
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapCostApplication aca = ArapCostApplication.dao.findById(id);
		setAttr("invoiceApplication", aca);
		
		Party con  = Party.dao.findFirst("select * from party  where id =?",aca.get("payee_id"));
		if(con!=null)
		    setAttr("payee_filter", con.get("company_name"));
		UserLogin userLogin = UserLogin.dao .findById(aca.get("create_by"));
		setAttr("submit_name", userLogin.get("c_name"));
		
		Long check_by = aca.getLong("check_by");
		if( check_by != null){
			userLogin = UserLogin.dao .findById(check_by);
			String check_name = userLogin.get("c_name");
			setAttr("check_name", check_name);
		}
		
		List<Record> list = null;
    	list = getItems(id);
    	setAttr("docList", list);
		
		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/CostAcceptOrder/payEdit.html");
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
			order.set("type", "cost");
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
    			+ " where aid.order_id=? and aid.type='cost' order by aid.id desc";
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
