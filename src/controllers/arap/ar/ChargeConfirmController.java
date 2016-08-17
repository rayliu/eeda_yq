package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.ArapChargeReceiveConfirmOrder;
import models.ArapChargeReceiveConfirmOrderDtail;
import models.ArapChargeReceiveConfirmOrderDtailLog;
import models.ArapChargeReceiveConfirmOrderLog;
import models.CostApplicationOrderRel;
import models.DepartOrder;
import models.InsuranceOrder;
import models.ReturnOrder;
import models.eeda.profile.Account;
import models.yh.arap.ArapAccountAuditSummary;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
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
public class ChargeConfirmController extends Controller {
    private Log logger = Log.getLog(ChargeConfirmController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CONFIRMATION})
    public void index() {
    	render("/yh/arap/ChargeConfirm/ChargeConfirmList.html");
    }
    
   	public void edit() {
   		String id = getPara("id");
   		String order_type = "";
   		setAttr("confirmId", id);
   		
   		List<ArapChargeReceiveConfirmOrderDtail> list = ArapChargeReceiveConfirmOrderDtail.dao.find("select * from arap_charge_receive_confirm_order_detail where order_id in(?)",id);
   		for(ArapChargeReceiveConfirmOrderDtail a: list){
   			if(a.getLong("invoice_order_id")!=null){
   				order_type = "开票记录单";
   				setAttr("order_type", "开票记录单");
   			}else if(a.getLong("misc_charge_order_id")!=null){
   				order_type = "手工收入单";
   				setAttr("order_type", "手工收入单");
   			}else if(a.getLong("dz_order_id")!=null){
   				order_type = "对账单";
   				setAttr("order_type", "对账单");
   			}else if(a.getLong("wlpj_order_id")!=null){
   				order_type = "往来票据单";
   				setAttr("order_type", "往来票据单");
   			}
   		}
   		//setAttr("order_type", order_type);
   		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(id);
   		setAttr("arapChargeReceiveConfirmOrder", arapChargeReceiveConfirmOrder);
   		
   		Long customer_id = arapChargeReceiveConfirmOrder.getLong("customer_id");
   		if(customer_id != null){
	   		Contact contact = Contact.dao.findById(customer_id);
	   		setAttr("customer_filter", contact.getStr("company_name"));
   		}
   		Long sp_id = arapChargeReceiveConfirmOrder.getLong("sp_id");
   		if(sp_id != null){
   			Contact contact = Contact.dao.findById(sp_id);
	   		setAttr("sp_filter", contact.getStr("company_name"));
   		}
   		String sql = "";
   		if(order_type.equals("手工收入单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(misc_charge_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_charge_receive_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_charge_receive_confirm_order acp WHERE acp.id = '"+id+"'";
   			
   		}else if(order_type.equals("开票记录单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(invoice_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_charge_receive_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_charge_receive_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("对账单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(dz_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_charge_receive_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_charge_receive_confirm_order acp WHERE acp.id = '"+id+"'";
   		}else if(order_type.equals("往来票据单")){
   			sql = "SELECT "
   	   				+ " (SELECT group_concat(cast(wlpj_order_id as char) SEPARATOR ',' ) "
   	   				+ "FROM arap_charge_receive_confirm_order_detail where order_id = acp.id ) ids "
   	   				+ "FROM arap_charge_receive_confirm_order acp WHERE acp.id = '"+id+"'";
   		}
   		
		Record re = Db.findFirst(sql);
		setAttr("orderIds", re.get("ids"));
		
		
		//获取已付款金额
   		String sql2 = "select sum(acp.amount) total from arap_charge_receive_confirm_order_log acp "
				+ "  where acp.order_id = '"+id+"'";
		Record re2 = Db.findFirst(sql2);
		setAttr("total_receive", re2.get("total"));

   		render("/yh/arap/ChargeConfirm/ChargeConfirmEdit.html");
   	}
   	
   	
	@SuppressWarnings("null")
	@Before(Tx.class)
	public void save() {
   		String receive_type = getPara("receive_type");
   		String receive_bank = getPara("receive_bank");
   		String receive_account_no = getPara("receive_account_no");
   		String receive_amount = getPara("receive_amount");
   		String noreceive_amount = getPara("noreceive_amount");
		
   		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = null;
		Record re = null;
   		String confirmId = getPara("confirmId");
   		String orderIds = getPara("orderIds");
   		
   		String sp_filter = getPara("sp_filter");       //供应商
   		String customer_filter = getPara("customer_filter");//客户
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
   			arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(confirmId);
   			arapChargeReceiveConfirmOrder.set("last_updator", LoginUserController.getLoginUserId(this));
   			arapChargeReceiveConfirmOrder.set("last_update_date", new Date()).update();				
		} else {
			Contact contact = Contact.dao.findFirst("select * from contact where company_name = '"+sp_filter+"'");
			Contact contact1 = Contact.dao.findFirst("select * from contact where company_name = '"+customer_filter+"'");
			String name = (String) currentUser.getPrincipal();
	        Long userId =  LoginUserController.getLoginUserId(this);
	      //创建主表
	        arapChargeReceiveConfirmOrder = new ArapChargeReceiveConfirmOrder();
	        arapChargeReceiveConfirmOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YSQR"));
			if(sp_filter!=null&&!sp_filter.equals("")){
				arapChargeReceiveConfirmOrder.set("sp_id",contact.getLong("id"));
			}
			if(customer_filter!=null&&!customer_filter.equals("")){
				arapChargeReceiveConfirmOrder.set("customer_id",contact1.getLong("id"));
			}
			arapChargeReceiveConfirmOrder.set("status", "新建");
//			arapChargeReceiveConfirmOrder.set("invoice_type",invoice_type);
			arapChargeReceiveConfirmOrder.set("invoice_company", billing_unit);
			arapChargeReceiveConfirmOrder.set("receive_company", payee_unit);
			arapChargeReceiveConfirmOrder.set("receive_person", payee_name); //收款人
//			arapChargeReceiveConfirmOrder.set("receive_bank", deposit_bank);
//			arapChargeReceiveConfirmOrder.set("receive_bank_person_name", account_name);//账户人名
//			arapChargeReceiveConfirmOrder.set("receive_account_no",bank_no);
			arapChargeReceiveConfirmOrder.set("creator",userId);
			arapChargeReceiveConfirmOrder.set("create_date",new Date());
			arapChargeReceiveConfirmOrder.save();		
			
			//新建字表
			String[] idArray = orderIds.split(",");
			for (int i = 0; i < idArray.length; i++) {
				ArapChargeReceiveConfirmOrderDtail arapChargeReceiveConfirmOrderDtail =new ArapChargeReceiveConfirmOrderDtail();
				arapChargeReceiveConfirmOrderDtail.set("order_id", arapChargeReceiveConfirmOrder.getLong("id"));
				if(order_type.equals("手工收入单")){
					arapChargeReceiveConfirmOrderDtail.set("misc_charge_order_id", idArray[i]);
				}else if(order_type.equals("开票记录单")){
					arapChargeReceiveConfirmOrderDtail.set("invoice_order_id", idArray[i]);
				}else if(order_type.equals("对账单")){
					arapChargeReceiveConfirmOrderDtail.set("dz_order_id", idArray[i]);
				}else if(order_type.equals("往来票据单")){
					arapChargeReceiveConfirmOrderDtail.set("wlpj_order_id", idArray[i]);
				}
				arapChargeReceiveConfirmOrderDtail.save();
				
				//更新开票记录单状态
				if(order_type.equals("手工收入单")){
					ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(idArray[i]);
					arapMiscChargeOrder.set("status", "收款确认中").update();
					
					//更新手工成本单往来账 的附带单
	        		String order_no = arapMiscChargeOrder.getStr("order_no");
	        		String order_no_head = arapMiscChargeOrder.getStr("order_no").substring(0, 4);
	        		if(order_no_head.equals("SGFK")){
	        			ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findFirst("select * from arap_misc_cost_order where order_no =?",order_no);
	        			arapMiscCostOrder.set("status", "收款确认中").update();
	        		}
				}else if(order_type.equals("开票记录单")){
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(idArray[i]);
					arapChargeInvoice.set("status", "收款确认中").update();
					List<ArapChargeInvoiceApplication> chargeOrderList = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?",idArray[i]);
					for(ArapChargeInvoiceApplication arapChargeInvoiceApplication : chargeOrderList){
						arapChargeInvoiceApplication.set("status", "收款确认中").update();
			    	}
				}else if(order_type.equals("对账单")){
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[i]);
					arapChargeOrder.set("status", "收款确认中").update();
					List<ReturnOrder> returnOrderList = ReturnOrder.dao.find("select * from return_order ror left join arap_charge_item  aci on aci.ref_order_id = ror.id where aci.charge_order_id =? and aci.ref_order_type = '回单'",idArray[i]);
					for(ReturnOrder returnOrder : returnOrderList){
						returnOrder.set("transaction_status", "收款确认中").update();
				    }
					
					
					List<ArapMiscChargeOrder> miscOrderList = ArapMiscChargeOrder.dao.find("select * from arap_misc_charge_order amc left join arap_charge_item aci on aci.ref_order_id = amc.id where aci.charge_order_id =? and aci.ref_order_type = '收入单'",idArray[i]);
					for(ArapMiscChargeOrder arapMiscChargeOrder : miscOrderList){
						arapMiscChargeOrder.set("status", "收款确认中").update();
				    }
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(idArray[i]);
					arapInOutMiscOrder.set("charge_status", "收款确认中").update();
				}
			}
		}
   		renderJson(arapChargeReceiveConfirmOrder);
   	}
	
	@Before(Tx.class)
	public void saveConfirmLog(){
		String confirmId = getPara("confirmId");
		String receive_type = getPara("receive_type");
   		String receive_bank = getPara("receive_bank");
   		String receive_account_no = getPara("receive_account_no");
   		String receive_amount = getPara("receive_amount");
   		String total_amount = getPara("total_amount");
   		String orderIds = getPara("orderIds");
   		String receive_time = getPara("receive_time"); //收款确认时间
   		String order_type = getPara("order_type"); //单据类型
   		String detailJson = getPara("detailJson");
   		String applicationId = "";
   		String value = "";
   		String sql = "";
   		
   		if( receive_time==null||receive_time.equals("")){
   			receive_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
   		}
   		
   		
   		Gson gson = new Gson();
		List<LinkedTreeMap> list = gson.fromJson(detailJson, new TypeToken<List<LinkedTreeMap>>(){}.getType()); 
		for (Map obj: list) {
			logger.debug(obj.get("id").toString());
			logger.debug(obj.get("value").toString());
			applicationId = obj.get("id").toString();
			value = obj.get("value").toString();
			if(order_type.equals("手工收入单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where misc_charge_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("开票记录单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where invoice_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("对账单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where dz_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}else if(order_type.equals("往来票据单")){
				sql = "select * from arap_charge_receive_confirm_order_detail where wlpj_order_id = '"+applicationId+"' and order_id = '"+confirmId+"'";
			}
			
			ArapChargeReceiveConfirmOrderDtail detail = ArapChargeReceiveConfirmOrderDtail.dao.findFirst(sql);
			ArapChargeReceiveConfirmOrderDtailLog arapChargeReceiveConfirmOrderDtailLog = new ArapChargeReceiveConfirmOrderDtailLog();
			arapChargeReceiveConfirmOrderDtailLog.set("order_id", confirmId);
			arapChargeReceiveConfirmOrderDtailLog.set("detail_id", detail.getLong("id"));
			arapChargeReceiveConfirmOrderDtailLog.set("receive_amount", value);
			arapChargeReceiveConfirmOrderDtailLog.set("create_date", new Date());
			arapChargeReceiveConfirmOrderDtailLog.save();
		}
		
		ArapChargeReceiveConfirmOrderLog arapChargeReceiveConfirmOrderLog = null;
   		Account account = Account.dao.findFirst("select * from fin_account where bank_name = '"+receive_bank+"'");
		//创建付款LOG记录表
   		arapChargeReceiveConfirmOrderLog = new ArapChargeReceiveConfirmOrderLog();
		if(account!=null)
			arapChargeReceiveConfirmOrderLog.set("receive_in_bank_id", account.getLong("id"));
		arapChargeReceiveConfirmOrderLog.set("receive_type", receive_type);
		arapChargeReceiveConfirmOrderLog.set("receive_in_bank_name", receive_bank);
		arapChargeReceiveConfirmOrderLog.set("receive_in_account_no", receive_account_no);
		arapChargeReceiveConfirmOrderLog.set("amount", receive_amount);
		arapChargeReceiveConfirmOrderLog.set("create_date", receive_time);
		arapChargeReceiveConfirmOrderLog.set("creator",LoginUserController.getLoginUserId(this));
		arapChargeReceiveConfirmOrderLog.set("order_id",confirmId);
		arapChargeReceiveConfirmOrderLog.save();
		
		//获取已收款金额
   		sql = "select sum(acp.amount) total from arap_charge_receive_confirm_order_log acp "
				+ "  where acp.order_id = '"+confirmId+"'";
		Record re = Db.findFirst(sql);
		
		
		ArapChargeReceiveConfirmOrder arapChargeReceiveConfirmOrder = ArapChargeReceiveConfirmOrder.dao.findById(confirmId);
		
		String[] idArray = orderIds.split(",");
		if(order_type.equals("手工收入单")){
			ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(applicationId);
			String order_no = arapMiscChargeOrder.getStr("order_no");
    		String order_no_head = arapMiscChargeOrder.getStr("order_no").substring(0, 4);
    		ArapMiscCostOrder arapMiscCostOrder = null;
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				arapMiscChargeOrder.set("status", "已收款").update();
				
				//更新手工成本单往来账 的附带单
        		if(order_no_head.equals("SGFK")){
        			arapMiscCostOrder = ArapMiscCostOrder.dao.findFirst("select * from arap_misc_cost_order where order_no =?",order_no);
        			arapMiscCostOrder.set("status", "已收款").update();
        		}
			}else{
				arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
			}
		}else if(order_type.equals("开票记录单")){
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				//更新确认表状态
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				
				//更新申请单状态
				for (int i = 0; i < idArray.length; i++) {
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(idArray[i]);
					arapChargeInvoice.set("status", "已收款确认").update();
					//List<Record> list= Db.find("SELECT STATUS from arap_cost_order where application_order_id =?",idArray[i]);
					List<ArapChargeInvoiceApplication> arapChargeInvoiceApplicationList = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?",idArray[i]);
					for(ArapChargeInvoiceApplication arapChargeInvoiceApplication : arapChargeInvoiceApplicationList){
						arapChargeInvoiceApplication.set("status", "已收款确认").update();
			    	}
				}
			}else{
				arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
			}
		}else if(order_type.equals("对账单")){
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				//更新确认表状态
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				
				//更新申请单状态
				for (int i = 0; i < idArray.length; i++) {
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[i]);
					arapChargeOrder.set("status", "已收款确认").update();
					//List<Record> list= Db.find("SELECT STATUS from arap_cost_order where application_order_id =?",idArray[i]);
					List<ReturnOrder> returnOrderList = ReturnOrder.dao.find("select * from return_order ror left join arap_charge_item  aci on aci.ref_order_id = ror.id where aci.charge_order_id =? and aci.ref_order_type = '回单'",idArray[i]);
					for(ReturnOrder returnOrder : returnOrderList){
						returnOrder.set("transaction_status", "收款确认中").update();
				    }
					List<ArapMiscChargeOrder> miscOrderList = ArapMiscChargeOrder.dao.find("select * from arap_misc_charge_order amc left join arap_charge_item aci on aci.ref_order_id = amc.id where aci.charge_order_id =? and aci.ref_order_type = '收入单'",idArray[i]);
					for(ArapMiscChargeOrder arapMiscChargeOrder : miscOrderList){
						arapMiscChargeOrder.set("status", "收款确认中").update();
				    }
				}
			}
		}else if(order_type.equals("往来票据单")){
			ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(applicationId);
			if(re.getDouble("total") == Double.parseDouble(total_amount)){
				arapChargeReceiveConfirmOrder.set("status", "已收款").update();
				arapInOutMiscOrder.set("charge_status", "已收款").update();
			}else{
				arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
				arapInOutMiscOrder.set("charge_status", "部分已收款").update();
			}
		}else{
			arapChargeReceiveConfirmOrder.set("status", "部分已收款").update();
		}
		
		
		//新建日记账表数据
		 ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
	        auditLog.set("payment_method", receive_type);
	        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
	        auditLog.set("amount", receive_amount);
	        auditLog.set("creator", LoginUserController.getLoginUserId(this));
	        auditLog.set("create_date", receive_time);
	        if(account!=null)
	        	auditLog.set("account_id", account.get("id"));
	        else
	        	auditLog.set("account_id", 4);
	        if(order_type.equals("手工收入单")){
	        	auditLog.set("source_order", "手工收入单");
	        }else if(order_type.equals("开票记录单")){
	        	auditLog.set("source_order", "应收开票记录单");
	        }else if(order_type.equals("对账单")){
	        	auditLog.set("source_order", "应收对账单");
	        }else if(order_type.equals("往来票据单")){
	        	auditLog.set("source_order", "往来票据单");
	        }
	        auditLog.set("invoice_order_id", confirmId);
	        auditLog.save();
	        
	        if("cash".equals(receive_type)){
	        	Account cash = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
	        	cash.set("amount", (cash.getDouble("amount")==null?0.0:cash.getDouble("amount")) - Double.parseDouble(receive_amount)).update();
	        	
	        	updateAccountSummary(receive_amount, cash.getLong("id"),receive_time);
	        }else{
	        	account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) - Double.parseDouble(receive_amount)).update();
	        	
	        	updateAccountSummary(receive_amount, account.getLong("id"),receive_time);
	        }
		
		Map BillingOrderListMap = new HashMap();
   		BillingOrderListMap.put("arapChargeReceiveConfirmOrder", arapChargeReceiveConfirmOrder);
   		BillingOrderListMap.put("re", re);
   		renderJson(BillingOrderListMap);
	}
	
	//更新日记账的账户期初结余
	//本期结余 = 期初结余 + 本期总收入 - 本a期总支出
	private void updateAccountSummary(String receive_amount, Long acountId,String receive_time) {
		Calendar cal = Calendar.getInstance();  
		int this_year = cal.get(Calendar.YEAR);  
		int this_month = cal.get(Calendar.MONTH)+1;  
		String year = receive_time.substring(0, 4);
		String month = receive_time.substring(5, 7);
		
		if(String.valueOf(this_year).equals(year) && String.valueOf(this_month).equals(month)){
			ArapAccountAuditSummary aaas = ArapAccountAuditSummary.dao.findFirst(
					"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
					, acountId, year, month);
			if(aaas!=null){
				aaas.set("total_charge", (aaas.getDouble("total_charge")==null?0.0:aaas.getDouble("total_charge")) + Double.parseDouble(receive_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")-aaas.getDouble("total_cost")+ aaas.getDouble("total_charge")));
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
				aaas.set("total_charge", (aaas.getDouble("total_charge")==null?0.0:aaas.getDouble("total_charge")) + Double.parseDouble(receive_amount));
				aaas.set("balance_amount", (aaas.getDouble("init_amount")+aaas.getDouble("total_charge")- aaas.getDouble("total_cost")));
				aaas.update();
			
				
				for(int i = 1 ;i<=(this_month - Integer.parseInt(month)); i++){
					ArapAccountAuditSummary this_aaas = ArapAccountAuditSummary.dao.findFirst(
							"select * from arap_account_audit_summary where account_id =? and year=? and month=?"
							, acountId, this_year, Integer.parseInt(month)+i);
					
					this_aaas.set("init_amount", this_aaas.getDouble("init_amount")==null?0.0:(this_aaas.getDouble("init_amount") + Double.parseDouble(receive_amount)));
					this_aaas.set("balance_amount", this_aaas.getDouble("balance_amount")==null?0.0:(this_aaas.getDouble("balance_amount") + Double.parseDouble(receive_amount)));
					this_aaas.update();	
				}
				
			}
		}
	}
	
	
	
   	
	public void logList() {
        String pageIndex = getPara("sEcho");
		String confirmId = getPara("confirmId");
		String sql = "select acp.*,ul.c_name from arap_charge_receive_confirm_order_log acp "
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
		
		String all=getPara("chargeIds2");
		setAttr("all", all);
        String[] alls=all.split(",");
        String orderIds = "";
        String order_type = "";

        for(int i=0;i< alls.length;i++){
        	String[] one = alls[i].split(":");
        	if(orderIds == ""){
        		orderIds=one[0];
        	}else{
        		orderIds+=","+one[0];
        	}
        	order_type =one[1];
        }
        setAttr("orderIds", orderIds);
        setAttr("order_type", order_type);
          
        String sql = "";
        for(int i=0;i< alls.length;i++){
        	String[] one = alls[i].split(":");
			String id = one[0];
			if(order_type.equals("手工收入单")){
				sql = "SELECT c.company_name sp_filter ,c1.company_name customer,amco.others_name payee_name, "
						+ " null,null,null,null,null,null"
						+ " FROM arap_misc_charge_order amco "
						+ " LEFT JOIN party p on p.id = amco.sp_id"
						+ " left join contact c on c.id = p.id  "
						+ " LEFT JOIN party p1 on p1.id = amco.customer_id"
						+ " left join contact c1 on c1.id = p1.id  "
						+ " where amco.id ='"+id+"'";
			}else if(order_type.equals("报销单")){
				sql = "SELECT c.company_name,aci.bill_type,aci.billing_unit,aci.payee_unit,aci.payee_name,aci.bank_no,aci.bank_name"
						+ " FROM `arap_cost_invoice_application_order` aci"
						+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
						+ " LEFT JOIN arap_cost_order aco on aco.id = cao.cost_order_id "
						+ " LEFT JOIN party p ON p.id = aci.payee_id "
						+ " LEFT JOIN office o ON o.id = p.office_id "
						+ " LEFT JOIN contact c ON c.id = p.contact_id"
						+ " where aci.id = '"+id+"'";
			}else if(order_type.equals("开票记录单")){
				sql = "SELECT c.company_name customer,"
					+ " c1.company_name sp_filter,"
					+ " (select group_concat(distinct aco.payee SEPARATOR '<br/>' )"
					+ "  from arap_charge_order aco where aco.invoice_order_id = aci.id) payee_name,"
					+ " (select group_concat(distinct aco.billing_unit SEPARATOR '<br/>' )"
					+ "  from arap_charge_order aco where aco.invoice_order_id = aci.id)  billing_unit,"
					+ " null,null,null"
					+ " FROM arap_charge_invoice aci "
					+ " LEFT JOIN party p on p.id = aci.payee_id"
					+ " left join contact c on c.id = p.id "
					+ " left join contact c1 on c1.id = aci.sp_id "
					+ " where aci.id = '"+id+"'";
			}else if(order_type.equals("对账单")){
				sql = "SELECT c.company_name customer,"
						+ " c1.company_name sp_filter,aci.payee payee_name,aci.billing_unit"
						+ " FROM arap_charge_order aci "
						+ " LEFT JOIN party p on p.id = aci.payee_id"
						+ " left join contact c on c.id = p.id "
						+ " left join contact c1 on c1.id = aci.sp_id "
						+ " where aci.id = '"+id+"'";
			}else if(order_type.equals("往来票据单")){
				sql = "SELECT aio.charge_unit payee_unit,aio.charge_person payee_name "
						+ " FROM `arap_in_out_misc_order` aio"
						+ " where aio.id = '"+id+"'";
        	}
        }
		Record record = Db.findFirst(sql);
		setAttr("re", record);
		setAttr("userName", LoginUserController.getLoginUserName(this));
		render("/yh/arap/ChargeConfirm/ChargeConfirmEdit.html");

	}
	
	
	public void applicationList() {
        
        String pageIndex = getPara("sEcho");
        String orderIds = getPara("orderIds");
        String order_type = getPara("order_type");
        
        List<Record> record = null;
        String sql = "";
        Record re = null;
        if (orderIds != null && !"".equals(orderIds)) {
			String[] idArray = orderIds.split(",");
			if(order_type.equals("手工收入单")){
				sql = " SELECT amco.id,amco.order_no,amco.create_stamp create_stamp,amco.total_amount total_amount, "
						+ " ifnull(amco.total_amount-( select sum(acpcodl.receive_amount)  "
						+ " from arap_charge_receive_confirm_order_detail_log acpcodl  "
						+ " LEFT JOIN arap_charge_receive_confirm_order_detail acpcod on acpcod.id = acpcodl.detail_id"
						+ " where acpcod.misc_charge_order_id = amco.id ),amco.total_amount) noreceive_amount "
						+ " FROM arap_misc_charge_order amco "
						+ " WHERE amco.id in(" + orderIds + ")";
			}else if(order_type.equals("报销单")){
				sql = "";
			}else if(order_type.equals("对账单")){
				sql = " SELECT aci.charge_amount total_amount,aci.id,aci.order_no,aci.create_stamp ,  "
						+ " IFNULL( aci.charge_amount - ( SELECT sum(acrcodl.receive_amount) FROM arap_charge_receive_confirm_order_detail_log acrcodl "
						+ " LEFT JOIN arap_charge_receive_confirm_order_detail acrcod on acrcod.id = acrcodl.detail_id "
						+ " WHERE  acrcod.dz_order_id = aci.id ), "
						+ " aci.charge_amount ) noreceive_amount FROM arap_charge_order aci WHERE aci.id IN("+orderIds+")";
			}else if(order_type.equals("往来票据单")){
				sql = " SELECT aio.id,aio.order_no,aio.create_date create_stamp,aio.charge_amount total_amount,"
						+ " ifnull(aio.charge_amount-( select sum(acrl.receive_amount) "
						+ " from arap_charge_receive_confirm_order_detail_log acrl  "
						+ " LEFT JOIN arap_charge_receive_confirm_order_detail acrd on acrd.id = acrl.detail_id "
						+ " where acrd.wlpj_order_id = aio.id ),aio.charge_amount) noreceive_amount "
						+ " FROM arap_in_out_misc_order aio "
						+ " WHERE aio.id in(" + orderIds + ")";
			}else{
				sql = "SELECT aci.*,  "
						+ " IFNULL( aci.total_amount - ( SELECT sum(acrcodl.receive_amount) FROM arap_charge_receive_confirm_order_detail_log acrcodl "
						+ " LEFT JOIN arap_charge_receive_confirm_order_detail acrcod on acrcod.id = acrcodl.detail_id "
						+ " WHERE  acrcod.invoice_order_id = aci.id ), "
						+ " aci.total_amount ) noreceive_amount FROM arap_charge_invoice aci WHERE aci.id IN("+orderIds+")";
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

    // 付款确认单列表
    //@RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
    	String orderNo = getPara("orderNo")==null?null:getPara("orderNo").trim();
    	String receiveConfirnNo = getPara("receiveConfirnNo")==null?null:getPara("receiveConfirnNo").trim();
		String status = getPara("status")==null?null:getPara("status").trim();
		String spName = getPara("sp_name")==null?null:getPara("sp_name").trim();
		String customerName = getPara("customer_name")==null?null:getPara("customer_name").trim();
		String receiverName = getPara("receiverName")==null?null:getPara("receiverName").trim();
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
               
        String fromSql = " from arap_charge_receive_confirm_order cpco "
        			+ " LEFT JOIN arap_charge_receive_confirm_order_detail acrc on acrc.order_id = cpco.id "
        			+ " LEFT JOIN party p ON cpco.sp_id = p.id "
        			+ " LEFT JOIN contact c ON p.contact_id = c.id "
        			+ " LEFT JOIN contact c2 ON c2.id = cpco.customer_id"
					+ " left join user_login ul on ul.id=cpco.creator";
        
        //String totalSql = "select count(1) total" + fromSql;
        
        String columsSql = "select cpco.*, "
        		+ " ( SELECT group_concat( DISTINCT aci .order_no SEPARATOR '<br/>' ) FROM arap_charge_invoice aci  "
        		+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr on acr.invoice_order_id = aci.id "
        		+ " WHERE acr.order_id = cpco.id ) sksq_no,"
        		+ " ( SELECT group_concat( DISTINCT aci.order_no SEPARATOR '<br/>' ) FROM arap_charge_order aci  "
        		+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr on acr.dz_order_id = aci.id "
        		+ " WHERE acr.order_id = cpco.id ) skdz_no,"
				+ " (SELECT group_concat( DISTINCT amco.order_no SEPARATOR '<br/>' )"
				+ " FROM arap_charge_receive_confirm_order_detail co, arap_misc_charge_order amco"
				+ " WHERE co.misc_charge_order_id = amco.id AND co.order_id = cpco.id ) misc_no,"
				+ " (SELECT group_concat( DISTINCT aio.order_no SEPARATOR '<br/>' )"
				+ " FROM arap_charge_receive_confirm_order_detail co, arap_in_out_misc_order aio"
				+ " WHERE co.wlpj_order_id = aio.id AND co.order_id = cpco.id ) wlpj_no,"
				+ " (case when acrc.invoice_order_id is not null "
				+ " then (SELECT ifnull(sum(aci.total_amount), 0)"
				+ " FROM arap_charge_invoice aci "
				+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr ON acr.invoice_order_id = aci.id"
				+ " WHERE acr.order_id = cpco.id )  "
				+ " when acrc.dz_order_id is not null "
				+ " then (SELECT ifnull(sum(aci.charge_amount), 0)"
				+ " FROM arap_charge_order aci "
				+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr ON acr.dz_order_id = aci.id"
				+ " WHERE acr.order_id = cpco.id )  "
				+ " when acrc.wlpj_order_id is not null "
				+ " then (SELECT ifnull(sum(aio.charge_amount), 0)"
				+ " FROM arap_in_out_misc_order aio "
				+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr ON acr.wlpj_order_id = aio.id"
				+ " WHERE acr.order_id = cpco.id )  "
				+ " when acrc.misc_charge_order_id is not null"
				+ " then (SELECT ifnull(sum(amco.total_amount), 0) "
				+ " FROM arap_misc_charge_order amco "
				+ " LEFT JOIN arap_charge_receive_confirm_order_detail acr ON acr.misc_charge_order_id = amco.id "
				+ " WHERE acr.order_id = cpco.id ) end ) pay_amount,"
				+ " (SELECT	ifnull(sum(log.amount), 0) FROM arap_charge_receive_confirm_order_log log "
				+ " where log.order_id = cpco.id) already_pay, "
        		+ " c.abbr sp_name,"
        		+ " c2.abbr customer_name,"
        		+ "ifnull(nullif(ul.c_name,''), ul.user_name) user_name "
        		+ fromSql;
        //String orderBy= "group by cpco.id order by cpco.create_date desc ";
        
        String conditions=" where 1=1 ";
        if (StringUtils.isNotEmpty(receiveConfirnNo)){
        	conditions+=" and UPPER(order_no) like '%"+receiveConfirnNo.toUpperCase()+"%'";
        }
        
        if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and (UPPER(sksq_no) like '%"+orderNo.toUpperCase()+"%' or UPPER(misc_no) like '%"+orderNo.toUpperCase()+"%')" ;
        }

        if (StringUtils.isNotEmpty(spName)){
        	conditions+=" and sp_name like '%"+spName+"%'";
        }
        if (StringUtils.isNotEmpty(customerName)){
        	conditions+=" and customer_name like '%"+customerName+"%'";
        }
        if (StringUtils.isNotEmpty(receiverName)){
        	conditions+=" and receive_person like '%"+receiverName+"%'";
        }
        
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and create_date between'"+beginTime+"'";
        }else{
        	beginTime =" and create_date between '1970-1-1'";
        }
        
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions+=beginTime+endTime;
        
        
        //排序
        String orderByStr= " group by id order by create_date desc ";
        if(colName.length()>0){
        	orderByStr = "group by id order by A."+colName+" "+sortBy;
        }
        
        conditions += orderByStr + sLimit;
        
        Record recTotal = Db.findFirst("select count(*) total from (select * from ("+columsSql+") A ) B");
        Long total = recTotal.getLong("total");
        logger.debug("total records:" + total);
        
        //columsSql+=conditions + orderBy + sLimit;
        List<Record> chargeReceiveConfirmOrders = Db.find("select * from ("+columsSql+") A " + conditions);

        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", total);
        orderListMap.put("iTotalDisplayRecords", total);

        orderListMap.put("aaData", chargeReceiveConfirmOrders);

        renderJson(orderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void chargeConfiremReturnOrder(){
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
