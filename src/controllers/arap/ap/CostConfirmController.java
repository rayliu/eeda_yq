package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapCostApplication;
import models.ArapCostOrder;
import models.ArapCostPayConfirmOrder;
import models.ArapCostPayConfirmOrderDtail;
import models.ArapCostPayConfirmOrderDtailLog;
import models.ArapCostPayConfirmOrderLog;
import models.CostApplicationOrderRel;
import models.DepartOrder;
import models.InsuranceOrder;
import models.eeda.profile.Account;
import models.yh.arap.ArapAccountAuditSummary;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.carmanage.CarSummaryOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.OrderNoGenerator;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostConfirmController extends Controller {
    private Log logger = Log.getLog(CostConfirmController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
    public void index() {
    	render("/eeda/arap/CostConfirm/CostConfirmList.html");
    }
    
   	public void edit() {
   		String id = getPara("id");
   		String order_type = "";
   		setAttr("confirmId", id);
   		
   		List<ArapCostPayConfirmOrderDtail> list = ArapCostPayConfirmOrderDtail.dao.find("select * from arap_cost_pay_confirm_order_detail where order_id in(?)",id);
   		for(ArapCostPayConfirmOrderDtail a: list){
   			if(a.getLong("application_order_id")!=null){
   				order_type = "申请单";
   				setAttr("order_type", "申请单");
   			}else if(a.getLong("misc_cost_order_id")!=null){
   				order_type = "成本单";
   				setAttr("order_type", "成本单");
   			}else if(a.getLong("car_summary_order_id")!=null){
   				order_type = "行车单";
   				setAttr("order_type", "行车单");
   			}else if(a.getLong("reimbursement_order_id")!=null){
   				order_type = "报销单";
   				setAttr("order_type", "报销单");
   			}else if(a.getLong("wlpj_order_id")!=null){
   				order_type = "往来票据单";
   				setAttr("order_type", "往来票据单");
   			}
   		}
   		//setAttr("order_type", order_type);
   		ArapCostPayConfirmOrder arapCostPayConfirmOrder = ArapCostPayConfirmOrder.dao.findById(id);
   		setAttr("arapCostPayConfirmOrder", arapCostPayConfirmOrder);
   		
   		if(arapCostPayConfirmOrder.getLong("sp_id")!= null){
	   		String sql1 = "select * from contact c where c.id = '"+arapCostPayConfirmOrder.getLong("sp_id")+"'";
	   		Record re1 = Db.findFirst(sql1);
	   		String aa= re1.getStr("company_name");
	   		setAttr("cname", re1.getStr("company_name"));
   		}
   		String sql = "";
   		if(order_type.equals("成本单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(misc_cost_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("申请单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(application_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("行车单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(car_summary_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("报销单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(reimbursement_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("往来票据单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(wlpj_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_cost_pay_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_cost_pay_confirm_order acp WHERE acp.id = '"+id+"'";
   		}
   		
		Record re = Db.findFirst(sql);
		setAttr("orderIds", re.get("ids"));
		
		
		//获取已付款金额
   		String sql2 = "select sum(acp.amount) total from arap_cost_pay_confirm_order_log acp "
				+ "  where acp.order_id = '"+id+"'";
		Record re2 = Db.findFirst(sql2);
		setAttr("total_pay", re2.get("total"));

   		render("/eeda/arap/CostConfirm/CostConfrimAdd.html");
   	}
   	
   	
	@SuppressWarnings("null")
	@Before(Tx.class)
	public void save() {
   		String pay_type = getPara("pay_type");
   		String pay_bank = getPara("pay_bank");
   		String pay_account_no = getPara("pay_account_no");
   		String pay_amount = getPara("pay_amount");
   		String nopay_amount = getPara("nopay_amount");
		
		ArapCostPayConfirmOrder arapCostPayConfirmOrder = null;
		Record re = null;
   		String confirmId = getPara("confirmId");
   		String orderIds = getPara("orderIds");
   		
   		String sp_filter = getPara("sp_filter");       //供应商
   		String payee_unit = getPara("payee_unit");    //收款单位
   		String account_name = getPara("account_name");//账户人名
   		String invoice_type = getPara("invoice_type");//开票类型
   		String payee_name = getPara("payee_name");    //收款人
   		String deposit_bank = getPara("deposit_bank");//开户行
   		String billing_unit = getPara("billing_unit"); //开票单位
   		String bank_no = getPara("bank_no");   
   		String order_type = getPara("order_type"); //单据类型
   		
   		if(confirmId != ""&& confirmId != null){
			//更新主表
			arapCostPayConfirmOrder = ArapCostPayConfirmOrder.dao.findById(confirmId);
			arapCostPayConfirmOrder.set("last_updator", LoginUserController.getLoginUserId(this));
			arapCostPayConfirmOrder.set("last_update_date", new Date()).update();				
		} else {
			Contact contact = Contact.dao.findFirst("select * from contact where company_name = '"+sp_filter+"'");
			String name = (String) currentUser.getPrincipal();
	        Long userId =  LoginUserController.getLoginUserId(this);
	      //创建主表
			arapCostPayConfirmOrder = new ArapCostPayConfirmOrder();
			arapCostPayConfirmOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YFQR"));
			if(sp_filter!=null&&!sp_filter.equals("")){
				arapCostPayConfirmOrder.set("sp_id",contact.getLong("id"));
			}
			arapCostPayConfirmOrder.set("status", "新建");
			arapCostPayConfirmOrder.set("invoice_type",invoice_type);
			arapCostPayConfirmOrder.set("invoice_company", billing_unit);
			arapCostPayConfirmOrder.set("receive_company", payee_unit);
			arapCostPayConfirmOrder.set("receive_person", payee_name);
			arapCostPayConfirmOrder.set("receive_bank", deposit_bank);
			arapCostPayConfirmOrder.set("receive_bank_person_name", account_name);//账户人名
			arapCostPayConfirmOrder.set("receive_account_no",bank_no);
			arapCostPayConfirmOrder.set("creator",userId);
			arapCostPayConfirmOrder.set("create_date",new Date());
			arapCostPayConfirmOrder.save();		
			
			//新建字表
			String[] idArray = orderIds.split(",");
			for (int i = 0; i < idArray.length; i++) {
				ArapCostPayConfirmOrderDtail arapCostPayConfirmOrderDtail =new ArapCostPayConfirmOrderDtail();
				arapCostPayConfirmOrderDtail.set("order_id", arapCostPayConfirmOrder.getLong("id"));
				if(order_type.equals("成本单")){
					arapCostPayConfirmOrderDtail.set("misc_cost_order_id", idArray[i]);
				}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
					arapCostPayConfirmOrderDtail.set("reimbursement_order_id", idArray[i]);
				}else if(order_type.equals("行车单")||order_type.equals("行车报销单")){
					arapCostPayConfirmOrderDtail.set("car_summary_order_id", idArray[i]);
				}else if(order_type.equals("往来票据单")){
					arapCostPayConfirmOrderDtail.set("wlpj_order_id", idArray[i]);
				}else{
					arapCostPayConfirmOrderDtail.set("application_order_id", idArray[i]);
				}
				arapCostPayConfirmOrderDtail.save();
				
				//更新申请单状态
				if(order_type.equals("成本单")){
					ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(idArray[i]);
					arapMiscCostOrder.set("status", "付款确认中").update();
					
					//更新手工收入单往来账 的附带单
	        		String order_no = arapMiscCostOrder.getStr("order_no");
	        		String order_no_head = arapMiscCostOrder.getStr("order_no").substring(0, 4);
	        		if(order_no_head.equals("SGSK")){
	        			ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findFirst("select * from arap_misc_charge_order where order_no =?",order_no);
	        			arapMiscChargeOrder.set("status", "付款确认中").update();
	        		}
				}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
					ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(idArray[i]);
					reimbursementOrder.set("status", "付款确认中").update();
				}else if(order_type.equals("行车单")){
					CarSummaryOrder carsummaryorder = CarSummaryOrder.dao.findById(idArray[i]);
					carsummaryorder.set("status", "付款确认中").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(idArray[i]);
					arapInOutMiscOrder.set("pay_status", "付款确认中").update();
				}else{
					ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(idArray[i]);
					arapCostInvoiceApplication.set("status", "付款确认中").update();
					List<ArapCostOrder> arapcostorderList = ArapCostOrder.dao.find("select id, status from arap_cost_order where application_order_id = ?",idArray[i]);
					for(ArapCostOrder arapCostorder : arapcostorderList){
						arapCostorder.set("status", "付款确认中").update();
			    	}
				}
			}
		}
   		renderJson(arapCostPayConfirmOrder);
   	}
	
	@Before(Tx.class)
	public void saveConfirmLog(){
		String confirmId = getPara("confirmId");
		String pay_type = getPara("pay_type");
   		String pay_bank = getPara("pay_bank");
   		String pay_account_no = getPara("pay_account_no");
   		String pay_amount = getPara("pay_amount");
   		String total_amount = getPara("total_amount");
   		String orderIds = getPara("orderIds");
   		String pay_time= getPara("pay_time");
   		String account_id= getPara("account_id");
   		String order_type = getPara("order_type"); //单据类型
   		String detailJson = getPara("detailJson");
   		String applicationId = "";
   		String value = "";
   		String sql = "";
   		
   		
   		if( pay_time==null||pay_time.equals("")){
   			pay_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
   		}
   		
   		Gson gson = new Gson();
		List<LinkedTreeMap> list = gson.fromJson(detailJson, new TypeToken<List<LinkedTreeMap>>(){}.getType());
		for (Map obj: list) {
			logger.debug(obj.get("id").toString());
			logger.debug(obj.get("value").toString());
			applicationId = obj.get("id").toString();
			value = obj.get("value").toString();
			if(order_type.equals("成本单")){
				sql = "select * from arap_cost_pay_confirm_order_detail where misc_cost_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
				sql = "select * from arap_cost_pay_confirm_order_detail where reimbursement_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("行车单")){
				sql = "select * from arap_cost_pay_confirm_order_detail where car_summary_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("往来票据单")){
				sql = "select * from arap_cost_pay_confirm_order_detail where wlpj_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else{
				sql = "select * from arap_cost_pay_confirm_order_detail where application_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}
			
			ArapCostPayConfirmOrderDtail detail = ArapCostPayConfirmOrderDtail.dao.findFirst(sql);
			ArapCostPayConfirmOrderDtailLog arapCostPayConfirmOrderDtailLog = new ArapCostPayConfirmOrderDtailLog();
			arapCostPayConfirmOrderDtailLog.set("order_id", confirmId);
			arapCostPayConfirmOrderDtailLog.set("detail_id", detail.getLong("id"));
			arapCostPayConfirmOrderDtailLog.set("pay_amount", value);
			arapCostPayConfirmOrderDtailLog.set("create_date", new Date());
			arapCostPayConfirmOrderDtailLog.save();
		}
		
		ArapCostPayConfirmOrderLog arapCostPayConfirmOrderLog = null;
   		//Account account = Account.dao.findFirst("select * from fin_account where account_no = '"+pay_account_no+"'");
		//创建付款LOG记录表
		arapCostPayConfirmOrderLog = new ArapCostPayConfirmOrderLog();
		if(!account_id.equals("") && account_id!=null)
			arapCostPayConfirmOrderLog.set("pay_out_bank_id", account_id);
		arapCostPayConfirmOrderLog.set("pay_type", pay_type);
		arapCostPayConfirmOrderLog.set("pay_out_bank_name", pay_bank);
		arapCostPayConfirmOrderLog.set("pay_out_account_no", pay_account_no);
		arapCostPayConfirmOrderLog.set("amount", pay_amount);
		arapCostPayConfirmOrderLog.set("create_date", pay_time);
		arapCostPayConfirmOrderLog.set("creator",LoginUserController.getLoginUserId(this));
		arapCostPayConfirmOrderLog.set("order_id",confirmId);
		arapCostPayConfirmOrderLog.save();
		
		//获取已付款金额
   		sql = "select sum(acp.amount) total from arap_cost_pay_confirm_order_log acp "
				+ "  where acp.order_id = '"+confirmId+"'";
		Record re = Db.findFirst(sql);
		
		
		ArapCostPayConfirmOrder arapCostPayConfirmOrder = ArapCostPayConfirmOrder.dao.findById(confirmId);
		
		String[] idArray = orderIds.split(",");
		if(order_type.equals("成本单")){
			ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapCostPayConfirmOrder.set("status", "已付款").update();
				arapMiscCostOrder.set("status", "已付款").update();
				
				//更新手工收入单往来账 的附带单
        		String order_no = arapMiscCostOrder.getStr("order_no");
        		String order_no_head = arapMiscCostOrder.getStr("order_no").substring(0, 4);
        		if(order_no_head.equals("SGSK")){
        			ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findFirst("select * from arap_misc_charge_order where order_no =?",order_no);
        			arapMiscChargeOrder.set("status", "已付款").update();
        		}
			}else{
				arapCostPayConfirmOrder.set("status", "部分已付款").update();
				arapMiscCostOrder.set("status", "部分已付款").update();
			}
		}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
			ReimbursementOrder reimbursementOrder = ReimbursementOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapCostPayConfirmOrder.set("status", "已付款").update();
				reimbursementOrder.set("status", "已付款").update();
			}else{
				arapCostPayConfirmOrder.set("status", "部分已付款").update();
				reimbursementOrder.set("status", "部分已付款").update();
			}
		}else if(order_type.equals("行车单")){
			CarSummaryOrder carsummaryorder = CarSummaryOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapCostPayConfirmOrder.set("status", "已付款").update();
				carsummaryorder.set("status", "已付款").update();
			}else{
				arapCostPayConfirmOrder.set("status", "部分已付款").update();
				carsummaryorder.set("status", "部分已付款").update();
			}
		}else if(order_type.equals("往来票据单")){
			ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapCostPayConfirmOrder.set("status", "已付款").update();
				arapInOutMiscOrder.set("pay_status", "已付款").update();
			}else{
				arapCostPayConfirmOrder.set("status", "部分已付款").update();
				arapInOutMiscOrder.set("pay_status", "部分已付款").update();
			}
		}else{  //应付申请单
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				//更新确认表状态
				arapCostPayConfirmOrder.set("status", "已付款").update();
				
				//更新申请单状态
				for (int i = 0; i < idArray.length; i++) {
					ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(idArray[i]);
					arapCostInvoiceApplication.set("status", "已付款确认").update();
					//List<Record> list= Db.find("SELECT STATUS from arap_cost_order where application_order_id =?",idArray[i]);
					List<ArapCostOrder> arapcostorderList = ArapCostOrder.dao.find("select id,status from arap_cost_order where application_order_id = ?",idArray[i]);
					for(ArapCostOrder arapCostorder : arapcostorderList){
						arapCostorder.set("status", "已付款确认").update();
			    	}
				}
			}else{
				arapCostPayConfirmOrder.set("status", "部分已付款").update();
			}
		}
		
		
		//新建日记账表数据
		 ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
	        auditLog.set("payment_method", pay_type);
	        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_COST);
	        auditLog.set("amount", pay_amount);
	        auditLog.set("creator", LoginUserController.getLoginUserId(this));
	        auditLog.set("create_date", pay_time);
	        if(!account_id.equals("") && account_id!=null)
	        	auditLog.set("account_id", account_id);
	        else
	        	auditLog.set("account_id", 4);
	        if(order_type.equals("成本单")){
	        	auditLog.set("source_order", "成本单");
	        }else if(order_type.equals("行车单")){
	        	auditLog.set("source_order", "行车单");
	        }else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
	        	auditLog.set("source_order", "行车报销单");
	        }else if(order_type.equals("往来票据单")){
	        	auditLog.set("source_order", "往来票据单");
	        }else{
	        	auditLog.set("source_order", "应付开票申请单");
	        }
	        auditLog.set("invoice_order_id", confirmId);
	        auditLog.save();
	        
	        if("cash".equals(pay_type)){
	        	Account cashAccount = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
	        	//cashAccount.set("amount", (cashAccount.getDouble("amount")==null?0.0:cashAccount.getDouble("amount")) - Double.parseDouble(pay_amount)).update();
	        	
	        	updateAccountSummary(pay_amount,cashAccount.getLong("id"),pay_time);
	        	
	        }else{
	        	//Account account = Account.dao.findFirst("select * from fin_account where account_id = ");
	        	//account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - Double.parseDouble(pay_amount)).update();
	        	
	        	updateAccountSummary(pay_amount, Long.parseLong(account_id) ,pay_time);
	        }
		
		Map BillingOrderListMap = new HashMap();
   		BillingOrderListMap.put("arapCostPayConfirmOrder", arapCostPayConfirmOrder);
   		BillingOrderListMap.put("re", re);
   		renderJson(BillingOrderListMap);
	}

	//更新日记账的账户期初结余
	//本期结余 = 期初结余 + 本期总收入 - 本期总支出
	private void updateAccountSummary(String pay_amount, Long acountId, String pay_time) {
		Calendar cal = Calendar.getInstance();  
		int this_year = cal.get(Calendar.YEAR);  
		int this_month = cal.get(Calendar.MONTH)+1;  
		String year = pay_time.substring(0, 4);
		String month = pay_time.substring(5, 7);
		
		if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
			ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
					"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
					, acountId, year, month);
			if(aaas!=null){
				aaas.set("total_cost", (aaas.getDouble("total_cost")==null?0.0:aaas.getDouble("total_cost")) + Double.parseDouble(pay_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")+aaas.getDouble("total_charge")- aaas.getDouble("total_cost")));
				aaas.update();
			}else{//add a new
				//1.该账户没有记录
				//2.该月份没有，从上月拷贝一条，考虑：上月也没有（跨1-N月）， 跨年
				ArapAccountAuditSummary newSummary = new ArapAccountAuditSummary();
			}
		}else{
			ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
					"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
					, acountId, year, month);
			
			if(aaas!=null){
				aaas.set("total_cost", (aaas.getDouble("total_cost")==null?0.0:aaas.getDouble("total_cost")) + Double.parseDouble(pay_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")+aaas.getDouble("total_charge")- aaas.getDouble("total_cost")));
				aaas.update();
				
				
				for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
					ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
							"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
							, acountId, this_year, Integer.parseInt(month)+i);
					
					this_aaas.set("init_amount", this_aaas.getDouble("init_amount")==null?0.0:(this_aaas.getDouble("init_amount") - Double.parseDouble(pay_amount)));
					this_aaas.set("balance_amount", this_aaas.getDouble("balance_amount")==null?0.0:(this_aaas.getDouble("balance_amount") - Double.parseDouble(pay_amount)));
					this_aaas.update();
				}
				
				
			}
		}
	}
	
   	
	public void logList() {
        String pageIndex = getPara("sEcho");
		String confirmId = getPara("confirmId");
		String sql = "select acp.*,ul.c_name from arap_cost_pay_confirm_order_log acp "
				+ " left join user_login ul on ul.id = acp.creator "
				+ "  where acp.order_id = '"+confirmId+"'";
		List<Record> re = Db.find(sql);
		
		Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", re.size());
        BillingOrderListMap.put("iTotalDisplayRecords", re.size());

        BillingOrderListMap.put("aaData", re);

		renderJson(BillingOrderListMap);
	}
	
	
    
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CPIO_CREATE})
	
	public void create() {
		String ids = getPara("ids");
		String order_type = getPara("order_type");
		setAttr("orderIds", ids);
		setAttr("order_type", order_type);
		String sql = "";
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
			if(order_type.equals("成本单")){
				sql = " SELECT c.abbr,amco.others_name payee_name FROM arap_misc_cost_order amco "
						+ " LEFT JOIN party p ON p.id = amco.sp_id "
						+ " LEFT JOIN office o ON o.id = p.office_id "
						+ " LEFT JOIN contact c ON c.id = p.contact_id "
						+ " WHERE amco.id = '"+idArray[0]+"'";
			}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
				sql = " SELECT  ro.account_name payee_unit, ro.account_no bank_no, ro.account_bank bank_name"
					+ " FROM reimbursement_order ro WHERE ro.id = '"+idArray[0]+"'";
			}else if(order_type.equals("行车单")){
				sql = " SELECT cso.main_driver_name AS payee_name FROM car_summary_order cso where id='"+idArray[0]+"'";
			}else if(order_type.equals("往来票据单")){
				sql = "SELECT aio.charge_unit payee_unit,aio.charge_person payee_name "
						+ " FROM `arap_in_out_misc_order` aio"
						+ " where aio.id = '"+idArray[0]+"'";
			}else{
				sql = "SELECT c.company_name,aci.bill_type,aci.billing_unit,aci.payee_unit,aci.payee_name,aci.bank_no,aci.bank_name FROM `arap_cost_invoice_application_order` aci"
						+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
						+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
						+ " LEFT JOIN party p ON p.id = aci.payee_id "
						+ " LEFT JOIN office o ON o.id = p.office_id "
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " where aci.id = '"+idArray[0]+"'";
			}
			
			Record record = Db.findFirst(sql);
			setAttr("invoiceApplicationOrder", record);
			setAttr("userName", LoginUserController.getLoginUserName(this));
		}
		render("/eeda/arap/CostConfirm/CostConfrimAdd.html");
	}
	
	
	public void applicationList() {
        
        String pageIndex = getPara("sEcho");
        String orderIds = getPara("orderIds");
        String order_type = getPara("order_type");
       
        String sql = "";
        List<Record> record = null;
        Record re = null;
        if (orderIds != null && !"".equals(orderIds)) {
			String[] idArray = orderIds.split(",");
			if(order_type.equals("成本单")){
				sql = " SELECT amco.id,amco.order_no,amco.create_stamp cost_stamp,amco.total_amount pay_amount,"
						+ " ifnull(amco.total_amount-( select sum(acpcodl.pay_amount) from arap_cost_pay_confirm_order_detail_log acpcodl  "
						+ " LEFT JOIN arap_cost_pay_confirm_order_detail acpcod on acpcod.id = acpcodl.detail_id "
						+ " where acpcod.misc_cost_order_id = amco.id ),amco.total_amount) nopay_amount "
						+ " FROM arap_misc_cost_order amco "
						+ " WHERE amco.id in(" + orderIds + ")";
			}else if(order_type.equals("报销单")||order_type.equals("行车报销单")){
				sql = "SELECT ro.id, ro.order_no, ro.create_stamp cost_stamp, ro.amount pay_amount, "
						+ "ifnull( ro.amount - ( SELECT sum(acpcodl.pay_amount)"
						+ "FROM arap_cost_pay_confirm_order_detail_log acpcodl"
						+ " LEFT JOIN arap_cost_pay_confirm_order_detail acpcod ON acpcod.id = acpcodl.detail_id"
						+ " WHERE acpcod.reimbursement_order_id = ro.id ), ro.amount ) nopay_amount"
						+ " FROM reimbursement_order ro WHERE ro.id IN ("+orderIds+")";
			}else if(order_type.equals("行车单")){
				sql = "SELECT cso.id,cso.order_no, cso.create_data cost_stamp, cso.actual_payment_amount pay_amount,"
						+ "ifnull( cso.actual_payment_amount - ( SELECT sum(acpcodl.pay_amount)"
						+ "FROM arap_cost_pay_confirm_order_detail_log acpcodl"
						+ " LEFT JOIN arap_cost_pay_confirm_order_detail acpcod ON acpcod.id = acpcodl.detail_id"
						+ " WHERE acpcod.car_summary_order_id = cso.id ), cso.actual_payment_amount ) nopay_amount"
						+ " FROM car_summary_order cso WHERE cso.id IN ("+orderIds+")";
			}else if(order_type.equals("往来票据单")){
				sql = " SELECT aio.id,aio.order_no,aio.create_date cost_stamp,aio.pay_amount pay_amount,"
						+ " ifnull(aio.pay_amount-( select sum(acpcodl.pay_amount) "
						+ " from arap_cost_pay_confirm_order_detail_log acpcodl  "
						+ " LEFT JOIN arap_cost_pay_confirm_order_detail acpcod on acpcod.id = acpcodl.detail_id "
						+ " where acpcod.wlpj_order_id = aio.id ),aio.pay_amount) nopay_amount "
						+ " FROM arap_in_out_misc_order aio "
						+ " WHERE aio.id in(" + orderIds + ")";
			}else{//order_type.equals("申请单")
				sql = "SELECT aci.id,aci.order_no, cao.order_type,"
						+ "    case cao.order_type"
						+ "		when '对账单' then("
						+ "			SELECT "
						+ "				GROUP_CONCAT(DISTINCT aco.order_no SEPARATOR '<br/>')"
						+ "			FROM"
						+ "				arap_cost_order aco"
						+ "					LEFT JOIN"
						+ "				cost_application_order_rel caor ON caor.cost_order_id = aco.id and caor.order_type ='对账单'"
						+ "			WHERE"
						+ "				caor.application_order_id = aci.id) "
						+ "		when '预付单'then("
						+ "			SELECT "
						+ "				GROUP_CONCAT(DISTINCT apo.order_no SEPARATOR '<br/>')"
						+ "			FROM"
						+ "				arap_pre_pay_order apo"
						+ "					LEFT JOIN"
						+ "				cost_application_order_rel caor ON caor.cost_order_id = apo.id and caor.order_type ='预付单'"
						+ "			WHERE"
						+ "				caor.application_order_id = aci.id) "
						+ "    end cost_order_no, "
						+ "    case cao.order_type"
						+ "		when '对账单' then(SELECT "
						+ "					SUM(caor.pay_amount)"
						+ "				FROM"
						+ "					arap_cost_order aco"
						+ "						LEFT JOIN"
						+ "					cost_application_order_rel caor ON caor.cost_order_id = aco.id and caor.order_type ='对账单'"
						+ "				WHERE"
						+ "					caor.application_order_id = aci.id)"
						+ "		when '预付单'then("
						+ "			SELECT "
						+ "				SUM(apo.total_amount)"
						+ "			FROM"
						+ "				arap_pre_pay_order apo"
						+ "					LEFT JOIN"
						+ "				cost_application_order_rel caor ON caor.cost_order_id = apo.id and caor.order_type ='预付单'"
						+ "			WHERE"
						+ "				caor.application_order_id = aci.id) "
						+ "    end pay_amount,"
						+ "	case cao.order_type"
						+ "		when '对账单' then("
						+ "			(SELECT "
						+ "                    SUM(caor.pay_amount)"
						+ "                FROM"
						+ "                    arap_cost_order aco"
						+ "                        LEFT JOIN"
						+ "                    cost_application_order_rel caor ON caor.cost_order_id = aco.id and caor.order_type ='对账单'"
						+ "                WHERE"
						+ "                    caor.application_order_id = aci.id) - ifnull((SELECT "
						+ "                    SUM(acpcodl.pay_amount)"
						+ "                FROM"
						+ "                    arap_cost_pay_confirm_order_detail_log acpcodl"
						+ "                WHERE"
						+ "                    acpcodl.order_id = acpcod.order_id"
						+ "                        AND acpcodl.detail_id = acpcod.id),0)"
						+ "        )"
						+ "		when '预付单'then("
						+ "			(SELECT "
						+ "				SUM(apo.total_amount)"
						+ "			FROM"
						+ "				arap_pre_pay_order apo"
						+ "					LEFT JOIN"
						+ "				cost_application_order_rel caor ON caor.cost_order_id = apo.id and caor.order_type ='预付单'"
						+ "			WHERE"
						+ "				caor.application_order_id = aci.id) - ifnull((SELECT "
						+ "                    SUM(acpcodl.pay_amount)"
						+ "                FROM"
						+ "                    arap_cost_pay_confirm_order_detail_log acpcodl"
						+ "                WHERE"
						+ "                    acpcodl.order_id = acpcod.order_id"
						+ "                        AND acpcodl.detail_id = acpcod.id),0)"
						+ "        ) "
						+ "    end nopay_amount,"
						+ "    aco.create_stamp cost_stamp"
						+ " FROM"
						+ "    arap_cost_invoice_application_order aci"
						+ "        LEFT JOIN"
						+ "    cost_application_order_rel cao ON cao.application_order_id = aci.id"
						+ "        LEFT JOIN"
						+ "    arap_cost_order aco ON aco.id = cao.cost_order_id"
						+ "        LEFT JOIN"
						+ "    arap_cost_pay_confirm_order_detail acpcod ON acpcod.application_order_id = aci.id "
		        		+ " where aci.id in(" + orderIds + ") GROUP BY aci.id " ;
			}
			record = Db.find(sql);			
		}

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", record.size());
        BillingOrderListMap.put("iTotalDisplayRecords", record.size());

        BillingOrderListMap.put("aaData", record);

        renderJson(BillingOrderListMap);
    }
	
	
	
	

	// 更新支付金额信息
	public void updateMoney() {
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		String costOrderId = getPara("costOrderId");
		String sql = "select * from cost_application_order_rel where application_order_id = '"+costPreInvoiceOrderId+"' and cost_order_id = '"+costOrderId+"'";
		CostApplicationOrderRel costApplicationOrderRel = CostApplicationOrderRel.dao.findFirst(sql);
		String name = getPara("name");
		String value = getPara("value");
		
		if ("pay_amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		costApplicationOrderRel.set(name, value);
		costApplicationOrderRel.update();
		
		renderJson(costApplicationOrderRel);
	}

	
	
	
	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}

    // 付款复核， 已付款确认单列表
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
    	String orderNo = getPara("orderNo")==null?null:getPara("orderNo").trim();
    	String businessOrderNo = getPara("businessNo")==null?null:getPara("businessNo").trim();
		String status = getPara("status")==null?null:getPara("status").trim();
		String spName = getPara("sp")==null?null:getPara("sp").trim();
		String receiverName = getPara("receiver")==null?null:getPara("receiver").trim();
		String beginTime = getPara("beginTime")==null?null:getPara("beginTime").trim();
		String endTime = getPara("endTime")==null?null:getPara("endTime").trim();
		
    	String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
               
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
        String fromSql = " from arap_cost_pay_confirm_order cpco "
        			+ " LEFT JOIN arap_cost_pay_confirm_order_detail acp on acp.order_id = cpco.id"
        			+ " left join party p1 on cpco.sp_id = p1.id "
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join user_login ul on ul.id=cpco.creator";
        
       
        
        String columsSql = "select * from(select cpco.*, "
        		+ " (select group_concat( DISTINCT cao.order_no SEPARATOR '<br/>' ) "
        		+ " FROM arap_cost_pay_confirm_order_detail co, arap_cost_invoice_application_order cao "
				+ " where co.application_order_id = cao.id and co.order_id = cpco.id) fksq_no,"
				+ " (SELECT group_concat( DISTINCT amco.order_no SEPARATOR '<br/>' )"
				+ " FROM arap_cost_pay_confirm_order_detail co, arap_misc_cost_order amco"
				+ " WHERE co.misc_cost_order_id = amco.id AND co.order_id = cpco.id ) misc_no,"
				+ " (SELECT group_concat(DISTINCT cso.order_no SEPARATOR '<br/>')"
				+ " FROM arap_cost_pay_confirm_order_detail co,car_summary_order cso"
				+ " WHERE co.car_summary_order_id = cso.id AND co.order_id = cpco.id) car_no,"
				+ " (SELECT group_concat( DISTINCT ro.order_no SEPARATOR '<br/>' )"
				+ " FROM arap_cost_pay_confirm_order_detail co, reimbursement_order ro"
				+ " WHERE co.reimbursement_order_id = ro.id AND co.order_id = cpco.id ) reimbursement_no, "
				+ " ( SELECT group_concat( DISTINCT ro.order_no SEPARATOR '<br/>' ) "
				+ " FROM arap_cost_pay_confirm_order_detail co, arap_in_out_misc_order ro "
				+ " WHERE co.wlpj_order_id = ro.id AND co.order_id = cpco.id ) wlpj_no,"
                + " ( case when acp.application_order_id is not null "
                + " then "
                + " ( SELECT sum(caor.pay_amount) FROM cost_application_order_rel caor "
                + " LEFT JOIN arap_cost_pay_confirm_order_detail acp on acp.application_order_id = caor.application_order_id"
                + " where acp.order_id = cpco.id)"
                + " when acp.car_summary_order_id is not null"
                + " then "
                + " ( SELECT sum(cso.actual_payment_amount) FROM car_summary_order cso"
                + " LEFT JOIN arap_cost_pay_confirm_order_detail acp on acp.car_summary_order_id = cso.id"
                + " where acp.order_id = cpco.id)"
                + " when acp.reimbursement_order_id is not null"
                + " then "
                + " (SELECT IFNULL(sum(ro.amount), 0) FROM reimbursement_order ro "
                + " LEFT JOIN arap_cost_pay_confirm_order_detail acp on acp.reimbursement_order_id = ro.id"
                + " where acp.order_id = cpco.id)"
                + " when acp.misc_cost_order_id is not null"
                + " then"
                + " ( SELECT IFNULL(sum(amco.total_amount), 0) FROM arap_misc_cost_order amco "
                + "  LEFT JOIN arap_cost_pay_confirm_order_detail acp on acp.misc_cost_order_id = amco.id"
                + " where acp.order_id = cpco.id) "
                + " WHEN acp.wlpj_order_id IS NOT NULL "
                + " then "
                + " ( SELECT IFNULL(sum(aio.pay_amount), 0) "
                + " FROM arap_in_out_misc_order aio "
                + " LEFT JOIN arap_cost_pay_confirm_order_detail acp ON acp.wlpj_order_id = aio.id"
                + " WHERE acp.order_id = cpco.id )"
                + " end "
                + " ) pay_amount,"
				+ " (SELECT	ifnull(sum(log.amount), 0) FROM arap_cost_pay_confirm_order_log log "
				+ " where log.order_id = cpco.id) already_pay, "
        		+ " c1.abbr sp_name,"
        		+ " ifnull(nullif(ul.c_name,''), ul.user_name) user_name,"
        		+ " (select group_concat(cast(acr.create_date as char) separator '</br>') "
        		+ " from arap_cost_pay_confirm_order_log acr where acr.order_id = cpco.id) confirm_time "
        		+ fromSql;
        
        
        String conditions=") A where 1=1 ";
        
        if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and UPPER(order_no) like '%"+orderNo.toUpperCase()+"%'";
        }

        if (StringUtils.isNotEmpty(status)){
        	conditions+=" and status = '"+status+"'";
        }
        if (StringUtils.isNotEmpty(spName)){
        	conditions+=" and ifnull(sp_name,'') like '%"+spName+"%'";
        }
        if (StringUtils.isNotEmpty(receiverName)){
        	conditions+=" and receive_person like '%"+receiverName+"%'";
        }
        if (StringUtils.isNotEmpty(businessOrderNo)){
        	conditions+=" and ifnull(fksq_no,ifnull(misc_no,ifnull(reimbursement_no,car_no))) LIKE  '%"+businessOrderNo+"%'";
        }
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and create_date between'"+beginTime+"'";
        }else{
        	beginTime =" and create_date between '1970-1-1'";
        }
        
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+" 23:59:59'";
        }else{
        	endTime =" and '2037-12-31'";
        }
        conditions+=beginTime+endTime;
        
        
        
        String orderByStr= " group by id order by id desc ";
        if(colName.length()>0){
        	orderByStr = "group by id order by A."+colName+" "+sortBy;
        }
        columsSql+=conditions + orderByStr;
        
        String totalSql = "select count(1) total from ( " + columsSql + ") B";
        
        Record recTotal = Db.findFirst(totalSql);
        Long total = recTotal.getLong("total");
        logger.debug("total records:" + total);
        
        List<Record> costPayConfirmOrders = Db.find(columsSql + sLimit);

        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", total);
        orderListMap.put("iTotalDisplayRecords", total);

        orderListMap.put("aaData", costPayConfirmOrders);
        


        renderJson(orderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void costConfiremReturnOrder(){
    	String ids = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] idArr = ids.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		if("提货".equals(orderNoArr[i])){
    			DepartOrder pickupOrder = DepartOrder.dao.findById(idArr[i]);
    			pickupOrder.set("audit_status", "已确认");
    			pickupOrder.update();
    		}else if("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])){
    			DepartOrder departOrder = DepartOrder.dao.findById(idArr[i]);
    			departOrder.set("audit_status", "已确认");
    			departOrder.update();
    		}else if("配送".equals(orderNoArr[i])){
    			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(idArr[i]);
    			deliveryOrder.set("audit_status", "已确认");
    			deliveryOrder.update();
    		}else if("成本单".equals(orderNoArr[i])){
    			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao.findById(idArr[i]);
    			arapmisccostOrder.set("audit_status", "已确认");
    			arapmisccostOrder.update();
    		}else{
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(idArr[i]);
    			insuranceOrder.set("audit_status", "已确认");
    			insuranceOrder.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }
}
