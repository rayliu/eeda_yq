package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.ChargeApplicationOrderRel;
import models.Party;
import models.eeda.profile.Account;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ReimbursementOrder;
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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeAcceptOrderController extends Controller {
    private Log logger = Log.getLog(ChargeAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COLLECTIONCONFIRM_LIST})
    public void index() {
    	render("/eeda/arap/ChargeAcceptOrder/ChargeAcceptOrderList.html");
    }
    
    
    public void create() {
		String idsArray = getPara("idsArray");
		setAttr("idsArray", idsArray);
		
		String payee_id = "";
		String[] orderArrId=idsArray.split(",");
		for (int i=0;i<orderArrId.length;i++) {
			String[] array = orderArrId[0].split(":");
			String id = array[0];
			String order_type = array[1];
			if("对账单".equals(order_type)){
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				//payee_id = arapChargeOrder.getLong("payee_id").toString();
			}else if("开票单".equals(order_type)){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				//payee_id = arapChargeInvoice.getLong("payee_id").toString();
			}
		}

		setAttr("payee_id", payee_id);
			
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
        String sql = " select * from (SELECT "
        		+ " aco.id, aco.order_no, '对账单' order_type, aco.check_amount total_amount, "
        		+ " sum(ifnull(caor.receive_amount,0)) receive_amount, aco.status,"
        		+ " aco.invoice_no, p.abbr payee_name, aco.remark "
        		+ " FROM"
        		+ " arap_charge_order aco"
        		+ " LEFT JOIN party p ON p.id = aco.payee_id"
        		+ " LEFT JOIN charge_application_order_rel caor ON caor.charge_order_id = aco.id and caor.order_type = '对账单'"
        		+ " where have_invoice = 'N'"
        		+ " GROUP BY aco.id"
        		+ " union"
        		+ " SELECT"
        		+ " aci.id, aci.order_no, '开票单' order_type, aci.total_amount total_amount,"
        		+ " sum(ifnull(caor.receive_amount,0)) receive_amount, aci.status, aco.invoice_no, p.abbr payee_name,"
        		+ " aci.remark "
        		+ " FROM arap_charge_invoice aci"
        		+ " LEFT JOIN party p ON p.id = aci.payee_id"
        		+ " LEFT JOIN arap_charge_order aco on aco.invoice_order_id = aci.id"
        		+ " LEFT JOIN charge_application_order_rel caor ON caor.charge_order_id = aci.id and caor.order_type = '开票单'"
        		+ " GROUP BY aci.id"
        		+ " ) A where total_amount>receive_amount ";
		
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
        String sql = "select * from(  "
        		+ " select acao.id,acao.order_no,acao.status,'申请单' order_type,acao.total_amount, "
        		+ " acao.remark,p.abbr payee_name,ul.c_name create_name "
				+ " from arap_charge_application_order acao "
				+ " left join user_login ul on ul.id = acao.create_by "
				+ " left join party p on p.id = acao.payee_id "
				+ " ) A where 1=1 ";
		
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
          String dz_id ="" ;//对账单
          String kpjl_id = "";//开票记录单
          String sr_id = "";//收入单
          String wl_id = "";//往来票据单
          String hs_id = "";//货损单
          String sql = "";
          String cname = "";
          String payee_unit = "";
          
          if(application_id.equals("")){
          	if(!application_id.equals(ids)){
          		String[] orderArrId=ids.split(",");
   				for (int i=0;i<orderArrId.length;i++) {
   					String[] one=orderArrId[i].split(":");
   					String id = one[0];
   					String orderType = one[1];
   					
   					
   					if("对账单".equals(orderType)){
   						dz_id += id+",";
   					}else if("开票记录单".equals(orderType)){
   						kpjl_id += id+",";
   					}else if("手工收入单".equals(orderType)){
   						sr_id += id+",";
   					}else if("往来票据单".equals(orderType)){
   						wl_id += id+",";
   					}else if("货损单".equals(orderType)){
   						payee_unit = one[2];
   	 					cname =" and dofi.party_name = '"+ one[2] +"'";
   						hs_id += id+",";
   					}
   				}
   				if(!dz_id.equals(""))
   					dz_id = dz_id.substring(0, dz_id.length()-1);
   				else
   					dz_id = "''";
   				if(!kpjl_id.equals(""))
   					kpjl_id = kpjl_id.substring(0, kpjl_id.length()-1);
   				else
   					kpjl_id = "''";
   				if(!sr_id.equals(""))
   					sr_id = sr_id.substring(0, sr_id.length()-1);
   				else
   					sr_id = "''";
   				if(!wl_id.equals(""))
   					wl_id = wl_id.substring(0, wl_id.length()-1);
   				else
   					wl_id = "''";
   				if(!hs_id.equals(""))
   					hs_id = hs_id.substring(0, hs_id.length()-1);
   				else
   					hs_id = "''";
          	}
  	       
  			
  		
  			sql = " SELECT aco.id,aco.payee_id,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
  					+ " p.abbr payee_name, ifnull(ul.c_name, ul.user_name) create_name, aco.check_amount total_amount,"
  					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '对账单' "
  					+ " ) receive_amount,"
  					+ " (aco.check_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
  					+ " FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '对账单'"
  					+ " )) noreceive_amount"
  					+ " FROM arap_charge_order aco "
  					+ " LEFT JOIN party p ON p.id = aco.payee_id"
  					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
  					+ " WHERE "
  					+ " aco.id in(" + dz_id +")"
  					+ " union "
  					+ " SELECT aci.id, aci.payee_id payee_id,aci.order_no, '开票单' order_type, aci. STATUS, aci.remark, "
  					+ " aci.create_stamp create_stamp, p.abbr cname, ifnull(ul.c_name, ul.user_name) create_name, aci.total_amount, "
  					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
  					+ "  FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票单' ) receive_amount, "
  					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
  					+ " charge_application_order_rel caor"
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票单' ) ) noreceive_amount"
  					+ " FROM arap_charge_invoice aci "
  					+ " LEFT OUTER JOIN party p ON aci.payee_id = p.id "
  					+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id "
  					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
  					+ " LEFT OUTER JOIN office o ON ul.office_id = o.id "
  					+ " WHERE aci.id in(" + kpjl_id +")";

  		}else{
  			sql = " select *  from (SELECT aco.id,aco.payee_id,payee payee_name,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
  					+ " c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aco.charge_amount,"
  					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id and caor.application_order_id = aciao.id AND caor.order_type = '应收对账单' ) receive_amount,"
  					+ " (aco.charge_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
  					+ " FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单'  )) noreceive_amount, aciao.id app_id "
  					+ " FROM arap_charge_order aco "
  					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aco.id"
  					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
  					+ " LEFT JOIN party p ON p.id = aco.payee_id"
  					+ " LEFT JOIN contact c ON c.id = p.contact_id"
  					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
  					+ " where caor.order_type = '应收对账单'"
  					+ " union "
  					+ " SELECT aci.id, aci.payee_id payee_id, NULL payee_name, aci.order_no, '开票记录单' order_type, aci. STATUS, aci.remark, "
  					+ " aci.create_stamp create_stamp, c.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name, aci.total_amount charge_amount, "
  					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
  					+ "  FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aci.id and caor.application_order_id = aciao.id AND caor.order_type = '开票记录单' ) receive_amount, "
  					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
  					+ " charge_application_order_rel caor"
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单' ) ) noreceive_amount, aciao.id app_id  "
  					+ " FROM arap_charge_invoice aci "
  					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aci.id"
  					+ " LEFT JOIN arap_charge_invoice_application_order aciao on aciao.id = caor.application_order_id"
  					+ " LEFT OUTER JOIN party p ON aci.payee_id = p.id "
  					+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id "
  					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
  					+ " LEFT OUTER JOIN office o ON ul.office_id = o.id "
  					+ " where caor.order_type = '开票记录单'"
  					+ " ) A where app_id ="+application_id ;	    
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
	public void save() {
		ArapChargeInvoiceApplication arapAuditInvoiceApplication = null;
		String application_id = getPara("application_id");
		String paymentMethod = getPara("payment_method");//收款方式
		String bank_no = getPara("bank_no");          //收款账号
		String payee_name = getPara("payee_name");    //收款人
		String numname = getPara("account_name");   //账户名
		String payee_unit = getPara("payee_unit");      //收款单位
		String payee_id = getPara("payee_id")==""?null:getPara("payee_id");         //付款给
		String billing_unit = getPara("billing_unit"); //收款单位
		String billtype = getPara("invoice_type");   //开票类型
		String bank_name = getPara("deposit_bank");   //开户行
		String total_amount = getPara("total_amount")==""?"0.00":getPara("total_amount");   //申请总金额

		
		if (!"".equals(application_id) && application_id != null) {
			arapAuditInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(application_id);
			arapAuditInvoiceApplication.set("last_modified_by",LoginUserController.getLoginUserId(this));
			arapAuditInvoiceApplication.set("last_modified_stamp", new Date());
			arapAuditInvoiceApplication.set("payee_name", payee_name);
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing_unit);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			arapAuditInvoiceApplication.set("num_name", numname);
			if (total_amount != null && !"".equals(total_amount)) {
				arapAuditInvoiceApplication.set("total_amount",total_amount);
			}
			arapAuditInvoiceApplication.update();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");

				ChargeApplicationOrderRel chargeApplicationOrderRel = ChargeApplicationOrderRel.dao.findFirst("select * from charge_application_order_rel where charge_order_id =? and application_order_id = ?",id,application_id);
				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				chargeApplicationOrderRel.set("charge_order_id", id);
				chargeApplicationOrderRel.set("order_type", order_type);
				chargeApplicationOrderRel.set("receive_amount", value);
				chargeApplicationOrderRel.update();
			}
		} else {
			arapAuditInvoiceApplication = new ArapChargeInvoiceApplication();
			arapAuditInvoiceApplication.set("order_no",
					OrderNoGenerator.getNextOrderNo("YSSQ"));
			arapAuditInvoiceApplication.set("status", "新建");
			arapAuditInvoiceApplication.set("create_by", LoginUserController.getLoginUserId(this));
			arapAuditInvoiceApplication.set("create_stamp", new Date());
			arapAuditInvoiceApplication.set("payee_name", payee_name);
			arapAuditInvoiceApplication.set("payment_method", paymentMethod);
			arapAuditInvoiceApplication.set("payee_unit", payee_unit);
			arapAuditInvoiceApplication.set("billing_unit", billing_unit);
			arapAuditInvoiceApplication.set("bill_type", billtype);
			arapAuditInvoiceApplication.set("bank_no", bank_no);
			arapAuditInvoiceApplication.set("bank_name", bank_name);
			arapAuditInvoiceApplication.set("num_name", numname);
			arapAuditInvoiceApplication.set("payee_id", payee_id);
			
			if (total_amount != null && !"".equals(total_amount)) {
				arapAuditInvoiceApplication.set("total_amount",total_amount);
			}
			arapAuditInvoiceApplication.save();
			
			String strJson = getPara("detailJson");
			Gson gson = new Gson();
			List<Map> idList = new Gson().fromJson(strJson, 
					new TypeToken<List<Map>>(){}.getType());
			for (Map map : idList) {
				String id = (String)map.get("id");
				String order_type = (String)map.get("order_type");
				String value = (String)map.get("value");
				String cname = (String)map.get("payee_unit");

				ChargeApplicationOrderRel chargeApplicationOrderRel = new ChargeApplicationOrderRel();
				chargeApplicationOrderRel.set("application_order_id", arapAuditInvoiceApplication.getLong("id"));
				chargeApplicationOrderRel.set("charge_order_id", id);
				chargeApplicationOrderRel.set("order_type", order_type);
				chargeApplicationOrderRel.set("receive_amount", value);
				if(cname!=null)
					chargeApplicationOrderRel.set("payee_unit", cname);
				
				chargeApplicationOrderRel.save();
				
                if(order_type.equals("应收对账单")){
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
					arapChargeOrder.set("status", "收款申请中").update();
				}else if(order_type.equals("手工收入单")){
					ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
					arapMiscChargeOrder.set("status", "收款申请中").update();
				}else if(order_type.equals("开票记录单")){
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
					arapChargeInvoice.set("status", "收款申请中").update();
				}else if(order_type.equals("往来票据单")){
					ArapInOutMiscOrder arapInOutMiscOrder = ArapInOutMiscOrder.dao.findById(id);
					arapInOutMiscOrder.set("charge_status", "收款申请中").update();
				}else if(order_type.equals("货损单")){
					DamageOrder damageOrder = DamageOrder.dao.findById(id);
					if(!damageOrder.getStr("status").equals("已结案"))
						damageOrder.set("status", "单据处理中").update();
				}
			}
		}
		renderJson(arapAuditInvoiceApplication);
	}
}
