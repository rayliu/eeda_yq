package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ReimbursementOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.carmanage.CarSummaryOrder;

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

import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostAcceptOrderController extends Controller {
    private Log logger = Log.getLog(CostAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_LIST})
    public void index() {   
    	String page = getPara("page");
    	setAttr("page", page);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
    	render("/eeda/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }
    
    
    
    public void edit2() {
    	
    	String id = getPara("id");
    	String attribute = getPara("attribute");
    	String sql = "SELECT ul.c_name,c.abbr cname,aciao.* FROM arap_cost_invoice_application_order aciao "
    			+ " LEFT JOIN party p ON p.id = aciao.payee_id "
    			+ " LEFT JOIN contact c ON c.id = p.contact_id "
    			+ " LEFT JOIN user_login ul on ul.id = aciao.create_by "
    			+ " where aciao.id = '"+id+"'";
    	Record re = Db.findFirst(sql);
    	setAttr("invoiceApplication", re);
    	setAttr("attribute", attribute);
    	
    	render("/eeda/arap/CostAcceptOrder/invoiceEdit.html");
    }
    
    
    public void costOrderList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        String id = getPara("id");
        String sqlTotal = "";
        String sql = "";
        List<Record> record = null;
        Record re = null;
        if (id != null && !"".equals(id)) {
			//String[] idArray = id.split(",");
			sql = "SELECT '对账单' type,caor.pay_amount,(aco.cost_amount - (select sum(caor1.pay_amount) from cost_application_order_rel caor1 where caor1.cost_order_id = aco.id)) daifu,aco.* FROM arap_cost_invoice_application_order aciao "
					+ " LEFT JOIN cost_application_order_rel caor on caor.application_order_id = aciao.id "
					+ " LEFT JOIN arap_cost_order aco on aco.id = caor.cost_order_id "
					+ " where aciao.id ='"+id+"'";
			record = Db.find(sql);			
		}

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", record.size());
        BillingOrderListMap.put("iTotalDisplayRecords", record.size());

        BillingOrderListMap.put("aaData", record);

        renderJson(BillingOrderListMap);
    }


    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(  "
        		+ " select  aco.id,aco.order_no,aco.order_type,aco.cost_amount totalCostAmount,aco.sp_id,p.company_name sp_name,c.pay_amount paid_amount "
				+ " from arap_cost_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id "
				+ " where c.pay_amount is null or aco.cost_amount>c.pay_amount "
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
    
    
    public void applicationList() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(  "
        		+ " select jo.order_no,acao.order_no application_order_no,acao.STATUS,acao.payment_method,acao.create_stamp,acao.check_stamp,acao.pay_time, "
        		+ " acao.remark,acao.payee_unit,acao.payee_name, "
        		+ " caor.order_type,caor.pay_amount,joa.sp_id "
				+ " from arap_cost_application_order acao "
				+ " left join cost_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join job_order jo on jo.id=acao.order_id "
				+ " left join job_order_arap joa on joa.order_id=acao.order_id "
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
    
    
    
    @Before(Tx.class)
    public void checkStatus(){
        String orderId=getPara("ids");
        String order=getPara("order");
        String[] orderArrId=orderId.split(",");
        String[] orderArr=order.split(",");
        List<Record> recordList= new ArrayList<Record>();
        for(int i=0;i<orderArrId.length;i++){
        	if(orderArr[i].equals("申请单")){
	            ArapCostInvoiceApplication arapcostinvoiceapplication= ArapCostInvoiceApplication.dao.findById(orderArrId[i]);
	            arapcostinvoiceapplication.set("status","已复核");
	            arapcostinvoiceapplication.update();
        	}else if(orderArr[i].equals("报销单")||orderArr[i].equals("行车报销单")){
        		ReimbursementOrder reimbursementorder =ReimbursementOrder.dao.findById(orderArrId[i]);
        		reimbursementorder.set("status", "已复核");
        		reimbursementorder.update();
        	}else if(orderArr[i].equals("成本单")){
        		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(orderArrId[i]);
        		arapMiscCostOrder.set("status", "已复核");
        		arapMiscCostOrder.update();
        		
        		//更新手工收入单往来账 的附带单
        		String order_no = arapMiscCostOrder.getStr("order_no");
        		String order_no_head = arapMiscCostOrder.getStr("order_no").substring(0, 4);
        		if(order_no_head.equals("SGSK")){
        			ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao.findFirst("select * from arap_misc_charge_order where order_no =?",order_no);
        			arapMiscChargeOrder.set("status", "已复核").update();
        		}
        	}else if(orderArr[i].equals("行车单")){
        		CarSummaryOrder carsummaryorder = CarSummaryOrder.dao.findById(orderArrId[i]);
        		carsummaryorder.set("status", "已复核");
        		carsummaryorder.update();
        	}else if(orderArr[i].equals("往来票据单")){
        		ArapInOutMiscOrder arapInOutMiscOrder =ArapInOutMiscOrder.dao.findById(orderArrId[i]);
        		arapInOutMiscOrder.set("pay_status", "已复核");
        		arapInOutMiscOrder.update();
	        }
            renderJson("{\"success\":true}");
        }
    }
    

    // 收款
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_CONFIRM})
    @Before(Tx.class)
    public void costAccept(){
    	ArapCostOrder arapAuditOrder = ArapCostOrder.dao.findById(getPara("costCheckOrderId"));
    	arapAuditOrder.set("status", "completed");
    	arapAuditOrder.update();
        renderJson("{\"success\":true}");
    }
    
    
}
