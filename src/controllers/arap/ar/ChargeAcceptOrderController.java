package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapChargeInvoice;
import models.ArapChargeOrder;
import models.eeda.profile.Account;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeAcceptOrderController extends Controller {
    private Log logger = Log.getLog(ChargeAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COLLECTIONCONFIRM_LIST})
    public void index() {
    	String page = getPara("page");
    	setAttr("page", page);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	render("/yh/arap/ChargeAcceptOrder/ChargeAcceptOrderList.html");
    }

    // billing order 列表
    public void list() {
        String sLimit = "";
        String status = getPara("status")==null?"":getPara("status");
        String orderNo_filter = getPara("orderNo_filter")==null?"":getPara("orderNo_filter");
        String customer_filter = getPara("customer_filter")==null?"":getPara("customer_filter");
        String beginTime = getPara("beginTime_filter")==null?"":getPara("beginTime_filter");
        String endTime = getPara("endTime_filter")==null?"":getPara("endTime_filter");
        String status2 = "";
        String status3 = "";
        String status4 = "";       
        
        if(status == null || status.equals("")){
        	status = "'已审批','收款申请中','部分已复核','部分已收款'";    //开票记录单
        	status2 = "'新建','收款申请中','部分已复核','部分已收款'";     //手工单
        	status3 = "'已确认','收款申请中','部分已复核','部分已收款'";    //对账单
        	status4 = "'未收','收款申请中','部分已复核','部分已收款'";  //往来票据单                                            
        }else if(status.equals("部分申请中")){
        	status = status2 = status3 = status4 = "'收款申请中'";
        	
        }else if(status.equals("部分复核中")){
        	status = status2 = status3 = status4 = "'部分已复核'";
        	
        }else if(status.equals("部分收款中")){
        	status = status2 = status3 = status4 = "'部分已付款'";
        }
        
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        //升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }


        String sql = "select * from( select aci.id, '开票记录单' order_type,aci.order_no, aci.status, "
        		+ " (select  group_concat( aco.payee SEPARATOR '<br/>' ) from arap_charge_order aco where aco.invoice_order_id = aci.id ) payee, "
        		+ " (select group_concat( aco.invoice_no SEPARATOR '<br/>' ) "
        		+ " from arap_charge_order aco where aco.invoice_order_id = aci.id ) invoice_no,"
        		+ " aci.create_stamp create_time, aci.remark,aci.total_amount total_amount,ifnull(c.abbr ,c1.abbr ) cname ,"
        		+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单' "
				+ " ) receive_amount,"
				+ " (aci.total_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
				+ " FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '开票记录单'"
				+ " )) noreceive_amount"
        		+ " from arap_charge_invoice aci "
        		+ " left join party p on p.id = aci.payee_id "
        		+ " left join contact c on c.id = p.contact_id"
        		+ " left join contact c1 on c1.id = aci.sp_id"
        		+ " where aci.status in(" + status + ") "
        	    + " UNION "
        	    + " select amco.id,'手工收入单' order_type, amco.order_no,amco.status, amco.others_name payee ,"
        	    + " '' invoice_no,amco.create_stamp create_time,amco.remark,amco.total_amount,"
        	    + " ( case when amco.charge_from_type='sp' "
        	    + " then c1.abbr "
        	    + " when amco.charge_from_type='customer' "
        	    + " then c.abbr end) cname,"
        	    + " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = amco.id AND caor.order_type = '手工收入单' "
				+ " ) receive_amount,"
				+ " (amco.total_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
				+ " FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = amco.id AND caor.order_type = '手工收入单'"
				+ " )) noreceive_amount"
        	    + " from arap_misc_charge_order amco "
        	    + " LEFT JOIN party p ON p.id = amco.customer_id "
        	    + " LEFT JOIN contact c ON c.id = p.contact_id "
        	    + " LEFT JOIN party p1 ON p1.id = amco.sp_id "
        	    + " LEFT JOIN contact c1 ON c1.id = p1.contact_id "
        	    + " where amco.status in(" + status2 + ") "
        	    + " and amco.type = 'non_biz'"
        	    + " and amco.total_amount >= 0"
        	    + " UNION "
        	    + " select amco.id,'应收对账单' order_type, amco.order_no,amco.status, amco.payee payee ,"
        	    + " amco.invoice_no invoice_no,amco.create_stamp create_time,amco.remark,amco.charge_amount,ifnull(c.abbr,c1.abbr) cname ,"
        	    + " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = amco.id AND caor.order_type = '应收对账单' "
				+ " ) receive_amount,"
				+ " (amco.charge_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
				+ " FROM charge_application_order_rel caor "
				+ " WHERE caor.charge_order_id = amco.id AND caor.order_type = '应收对账单'"
				+ " )) noreceive_amount"
        	    + " from arap_charge_order amco "
        	    + " LEFT JOIN party p ON p.id = amco.payee_id "
        	    + " LEFT JOIN contact c ON c.id = p.contact_id "
        	    + " LEFT JOIN party p1 ON p1.id = amco.sp_id "
        	    + " LEFT JOIN contact c1 ON c1.id = p1.contact_id "
        	    + " where amco.status in(" + status3 + ") and amco.have_invoice = 'N'"
        		+ " UNION"
        		+ " SELECT aio.id, '往来票据单' order_type, aio.order_no, aio.charge_status status , aio.charge_person AS payee,"
        		+ " NULL AS invoice_no, aio.create_date create_stamp,"
        		+ " aio.remark, aio.charge_amount charge_amount,null cname,"
        		 + " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
 				+ " WHERE caor.charge_order_id = aio.id AND caor.order_type = '往来票据单' "
 				+ " ) receive_amount,"
 				+ " (aio.charge_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
 				+ " FROM charge_application_order_rel caor "
 				+ " WHERE caor.charge_order_id = aio.id AND caor.order_type = '往来票据单'"
 				+ " )) noreceive_amount"
        		+ " FROM arap_in_out_misc_order aio WHERE aio.charge_status IN (" + status4 + ")"
        		+ " union"
        		+ " SELECT dor.id,'货损单' AS order_type, dor.order_no, dofi.status STATUS, "
        		+ " ''  AS payee_name,"
        		+ " NULL AS invoice_no, dor.create_date create_stamp,  dofi.remark,"
        		+ " ifnull(sum(dofi.amount),0) charge_amount,"
        		+ " (case "
        		+ " when dofi.party_type ='客户'"
          	    + " then c.abbr "
          	    + " when dofi.party_type ='供应商'"
        	    + " then c2.abbr "
        	    + " when dofi.party_type ='保险公司'"
          	    + " then c3.abbr "
          	    + " else "
          	    + " dofi.party_name"
          	    + " end) cname ,"
        		+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
        		+ " WHERE caor.charge_order_id = dor.id AND caor.order_type = '货损单' and caor.payee_unit = dofi.party_name "
        		+ " ) receive_amount,"
        		+ " (ifnull(sum(dofi.amount),0) - (SELECT ifnull(sum(caor.receive_amount), 0) "
        		+ " FROM charge_application_order_rel caor "
        		+ " WHERE caor.charge_order_id = dor.id AND caor.order_type = '货损单' and caor.payee_unit = dofi.party_name"
        		+ " )) noreceive_amount"
        		+ " FROM damage_order dor"
        	    + " LEFT JOIN damage_order_fin_item dofi on dofi.order_id = dor.id and dofi.type = 'charge' "
        	    + " LEFT JOIN party p ON p.id = dor.customer_id"
        	    + " LEFT JOIN contact c ON c.id = p.contact_id"
        	    + " LEFT JOIN party p2 ON p2.id = dor.sp_id"
        	    + " LEFT JOIN contact c2 ON c2.id = p2.contact_id"
        	    + " LEFT JOIN party p3 ON p3.id = dor.insurance_id"
        	    + " LEFT JOIN contact c3 ON c3.id = p3.contact_id"
        	    + " WHERE"
        	    + " dofi. STATUS = '已确认'"
        	    + " GROUP BY dofi.party_name , dor.id"
        	    + " ) A";


        String conditions=" where 1=1 and noreceive_amount != 0 ";
        if (StringUtils.isNotEmpty(orderNo_filter)){
        	conditions+=" and UPPER(order_no) like '%"+orderNo_filter.toUpperCase()+"%'";
        }
        if (StringUtils.isNotEmpty(customer_filter)){
        	conditions+=" and UPPER(cname) like '%"+customer_filter+"%'";
        }   
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and create_time between'"+beginTime+"'";
        }else{
        	beginTime =" and create_time between '1970-1-1'";
        }
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+"'";
        }else{
        	endTime =" and '3000-1-1'";
        }
        conditions+=beginTime+endTime;

        
        
        String sqlTotal = "select count(1) total from ("+sql+") tab " ;
        Record rec = Db.findFirst(sqlTotal+ conditions);
        logger.debug("total records:" + rec.getLong("total"));
        
       
        List<Record> BillingOrders = Db.find("select * from (" + sql + conditions + " order by create_time desc) A "+orderByStr + sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    
    
    
    public void applicationList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String cname = getPara("cname")!=null?getPara("cname"):"";
        String beginTime = getPara("beginTime")!=null?getPara("beginTime"):"";
        String endTime = getPara("endTime")!=null?getPara("endTime"):"";
        String orderNo = getPara("orderNo")!=null?getPara("orderNo"):"";
        String applictionOrderNo = getPara("applicationOrderNo")!=null?getPara("applicationOrderNo"):"";
        String status = getPara("status")!=null?getPara("status"):"";
		
//		String sortColIndex = getPara("iSortCol_0");
//		String sortBy = getPara("sSortDir_0");
//		String colName = getPara("mDataProp_"+sortColIndex);
        
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
      
        //String statusStr = " ('已复核')";
        if(status == null || status.equals("")){
        	status  = "'新建','已审批','已复核','已收款'";        //申请单
        }else if(status.equals("未复核")){
        	status = "'新建','已审批'";
        }else if(status.equals("已复核")){
        	status  = "'已复核'";
        }else if(status.equals("已收款")){
        	status  = "'已收款'";
        }
        
        String condition = "";
        if(cname != null || status != null || beginTime != null || endTime != null|| orderNo != null)
        {
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
		condition = " where "
					+ " ifnull(cname,'') like '%" + cname + "%' "
					+ " and create_time between '" + beginTime + "' and '" + endTime+ " 23:59:59' "
				    + " and ifnull(order_no,'') like '%" + orderNo + "%' "
				    + " and ifnull(application_order_no,'') like '%" + applictionOrderNo + "%' ";
        }
        
        String sql = "select * from(select aci.id, aci.order_no application_order_no,'应收申请单' as order_type, aci.payment_method, aci.account_id, aci.status, group_concat(invoice_item.invoice_no separator '\r\n') invoice_no, aci.create_stamp create_time, aci.remark,"
        		+ " aci.total_amount total_amount, "
        		+ " GROUP_CONCAT( "
                + " case "
                + " when cao.order_type='应收对账单' "
                + " then (select order_no from arap_charge_order where id = cao.charge_order_id)"
                + " when cao.order_type='开票记录单'"
                + " then (select order_no from arap_charge_invoice where id = cao.charge_order_id)"
                + " when cao.order_type='手工收入单'"
                + " then (select order_no from arap_misc_charge_order where id = cao.charge_order_id)"
                + " when cao.order_type='往来票据单'"
                + " then (select order_no from arap_in_out_misc_order where id = cao.charge_order_id)"
                + " when cao.order_type='货损单'"
                + " then (select order_no from damage_order where id = cao.charge_order_id)"
                + " end SEPARATOR '</br>') order_no, "
        		+ " ( select sum(cao.receive_amount) from charge_application_order_rel cao where cao.application_order_id = aci.id ) application_amount, "
        		+ " c.abbr cname "
        		+ " from arap_charge_invoice_application_order aci "
        		+ " LEFT JOIN charge_application_order_rel cao on cao.application_order_id = aci.id"
        		+ " left join party p on p.id = aci.payee_id "
        		+ " left join contact c on c.id = p.contact_id "
        		+ " left join arap_cost_invoice_item_invoice_no invoice_item on aci.id = invoice_item.invoice_id where aci.status in ("+status+") group by aci.id "
        		+ ") A";

        
        
        Record rec = Db.findFirst("select count(*) total from (" + sql + condition + " ) B");
        logger.debug("total records:" + rec.getLong("total"));

        String orderByStr = " order by A.create_time desc ";
//        if(colName.length()>0){
//        	orderByStr = " order by A."+colName+" "+sortBy;
//        }
        List<Record> BillingOrders = Db.find(sql+ condition + orderByStr +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
    
    
    
    
    // 收款
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COLLECTIONCONFIRM_CONFIRM})
    public void chargeAccept(){
    	ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(getPara("chargeCheckOrderId"));
    	arapAuditOrder.set("status", "completed");
    	arapAuditOrder.update();
        renderJson("{\"success\":true}");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COLLECTIONCONFIRM_CONFIRM})
    @Before(Tx.class)
    public void save(){
    	String chargeIds = getPara("chargeIds");
    	String paymentMethod = getPara("paymentMethod");
    	String accountId = getPara("accountTypeSelect");
    	String[] chargeIdArr = null; 
    	if(chargeIds != null && !"".equals(chargeIds)){
    		chargeIdArr = chargeIds.split(",");
    	}
    	for(int i=0;i<chargeIdArr.length;i++){
    		String[] arr = chargeIdArr[i].split(":");
    		String orderId = arr[0];
    		String orderNo = arr[1];
            if(orderNo.startsWith("SGSK")){
				ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(orderId);
				arapMiscChargeOrder.set("status", "已收款确认");
				arapMiscChargeOrder.update();
            }else{
                ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(orderId);
                arapChargeInvoice.set("status", "已收款确认");
                arapChargeInvoice.update();
                /*//收款确认后，改变应收开票申请和应收对账单以及手工收款单的状态
                List<ArapChargeInvoiceApplication> list = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?",orderId);
                for (ArapChargeInvoiceApplication application : list) {
                	application.set("status", "已收款确认");
                	application.update();
					List<ArapChargeInvoiceApplicationItem> inList = ArapChargeInvoiceApplicationItem.dao.find("select * from arap_charge_invoice_application_item where invoice_application_id = ?",application.get("id"));
					for (ArapChargeInvoiceApplicationItem arapChargeInvoiceApplicationItem : inList) {
						ArapChargeOrder arapAuditOrder = ArapChargeOrder.dao.findById(arapChargeInvoiceApplicationItem.get("charge_order_id"));
						arapAuditOrder.set("status", "已收款确认");
						arapAuditOrder.update();
						List<ArapMiscChargeOrder> arapMiscChargeOrderList = ArapMiscChargeOrder.dao.find("select * from arap_misc_charge_order where charge_order_id = ?",arapAuditOrder.get("id"));
						if(arapMiscChargeOrderList.size()>0){
							for (ArapMiscChargeOrder arapMiscChargeOrder : arapMiscChargeOrderList) {
								arapMiscChargeOrder.set("status", "已收款确认");
								arapMiscChargeOrder.update();
							}
						}
					}
                }*/
                
            }
			
			//现金 或 银行  金额处理
			if("cash".equals(paymentMethod)){
				Account account = Account.dao.findFirst("select * from fin_account where bank_name ='现金'");
				if(account!=null){
					Record rec = null;
					if(orderNo.startsWith("SGSK")){
						rec = Db.findFirst("select sum(amcoi.amount) total from arap_misc_charge_order amco, arap_misc_charge_order_item amcoi "
								+ "where amco.id = amcoi.misc_order_id and amco.order_no='"+orderNo+"'");
						if(rec!=null){
							double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
							//现金账户 金额处理
							account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) + total).update();
							//日记账
							createAuditLog(orderId, account, total, paymentMethod, "手工收款单");
						}
					}else{
						rec = Db.findFirst("select aci.total_amount total from arap_charge_invoice aci where aci.order_no='"+orderNo+"'");
						if(rec!=null){
							double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
							//现金账户 金额处理
							account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) + total).update();
							//日记账
							createAuditLog(orderId, account, total, paymentMethod, "应收开票记录单");
						}
					}
				}
			}else{//银行账户  金额处理
			    Account account = Account.dao.findFirst("select * from fin_account where id ="+accountId);
                if(account!=null){
                	Record rec = null;
					if(orderNo.startsWith("SGSK")){
						rec = Db.findFirst("select sum(amcoi.amount) total from arap_misc_charge_order amco, arap_misc_charge_order_item amcoi "
								+ "where amco.id = amcoi.misc_order_id and amco.order_no='"+orderNo+"'");
	                    if(rec!=null){
	                        double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
	                        //银行账户 金额处理
	                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) + total).update();
	                        //日记账
	                        createAuditLog(orderId, account, total, paymentMethod, "手工收款单");
	                    }
					}else{
						rec = Db.findFirst("select aci.total_amount total from arap_charge_invoice aci where aci.order_no='"+orderNo+"'");
	                    if(rec!=null){
	                        double total = rec.getDouble("total")==null?0.0:rec.getDouble("total");
	                        //银行账户 金额处理
	                        account.set("amount", (account.getDouble("amount")==null?0.0:account.getDouble("amount")) + total).update();
	                        //日记账
	                        createAuditLog(orderId, account, total, paymentMethod, "应收开票记录单");
	                    }
					}
                }
			}
    	}
    	redirect("/chargeAcceptOrder");
    }

    private void createAuditLog(String orderId, Account account, double total, String paymentMethod, String sourceOrder) {
        ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", paymentMethod);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
        auditLog.set("amount", total);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", new Date());
        auditLog.set("misc_order_id", orderId);
        auditLog.set("invoice_order_id", null);
        auditLog.set("account_id", account.get("id"));
        auditLog.set("source_order", sourceOrder);
        auditLog.save();
    }
    
    
    @Before(Tx.class)
    public void checkOrder(){
        String all=getPara("ids");
        String[] alls=all.split(",");
        
        for(int i=0;i< alls.length;i++){
        	String[] one = alls[i].split(":");
			String id = one[0];
			String order_type = one[1];
			if(order_type.equals("开票记录单")){
	            ArapChargeInvoice arapChargeInvoice= ArapChargeInvoice.dao.findById(id);
	            arapChargeInvoice.set("status","已复核");
	            arapChargeInvoice.update();
	        }else if(order_type.equals("对账单")){
        		ArapChargeOrder arapChargeOrder =ArapChargeOrder.dao.findById(id);
        		arapChargeOrder.set("status", "已复核");
        		arapChargeOrder.update();
	        }else if(order_type.equals("报销单")){
        		ReimbursementOrder reimbursementorder =ReimbursementOrder.dao.findById(id);
        		reimbursementorder.set("status", "已复核");
        		reimbursementorder.update();
	        }else if(order_type.equals("手工收入单")){
        		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findById(id);
        		arapMiscChargeOrder.set("status", "已复核");
        		arapMiscChargeOrder.update();
        		
        		//更新手工成本单往来账 的附带单
        		String order_no = arapMiscChargeOrder.getStr("order_no");
        		String order_no_head = arapMiscChargeOrder.getStr("order_no").substring(0, 4);
        		if(order_no_head.equals("SGFK")){
        			ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findFirst("select * from arap_misc_cost_order where order_no =?",order_no);
        			arapMiscCostOrder.set("status", "已复核").update();
        		}
	        }else if(order_type.equals("往来票据单")){
        		ArapInOutMiscOrder arapInOutMiscOrder =ArapInOutMiscOrder.dao.findById(id);
        		arapInOutMiscOrder.set("charge_status", "已复核");
        		arapInOutMiscOrder.update();
	        }
	        renderJson("{\"success\":true}");	 	 
        }
    }
}
