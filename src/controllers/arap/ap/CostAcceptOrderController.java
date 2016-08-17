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
    	render("/yh/arap/CostAcceptOrder/CostAcceptOrderList.html");
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
    	
    	render("/yh/arap/CostAcceptOrder/invoiceEdit.html");
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
        String pageIndex = getPara("sEcho");
        //String customer = getPara("companyName");
        String spName = getPara("sp")!=null?getPara("sp"):"";
        String beginTime = getPara("beginTime")!=null?getPara("beginTime"):"";
        String endTime = getPara("endTime")!=null?getPara("endTime"):"";
        String orderNo = getPara("orderNo")!=null?getPara("orderNo"):"";
        String status = getPara("status")!=null?getPara("status"):"";
        String orderType = getPara("orderType")!=null?getPara("orderType"):"";
        String status2 = "";
        String status3 = "";
        String status4 = "";
        String status5 = "";
        String status6 = "";
        
		
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
        
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
      
        //String statusStr = " ('已复核')";
        if(status == null || status.equals("")){
        	status  = "'已审批','付款申请中','部分已复核','部分已付款'";        //申请单
        	status2 = "'新建','付款申请中','部分已复核','部分已付款'";         //手工单
        	status3 = "'已审核','付款申请中','部分已复核','部分已付款'";       //报销单/行车报销单
        	status4 = "'未付','付款申请中','部分已复核','部分已付款'";         //往来票据单
        	status5 = "'已确认','付款申请中','部分已复核','部分已付款'";         //应付对账单
        	status6 = "'已确认','单据处理中'";         //货损单
        }else if(status.equals("部分申请中")){
        	status = status2 = status3 = status4 = status5 = "'付款申请中'";
        	status6 = "'已确认'";
        }else if(status.equals("部分复核中")){
        	status = status2 = status3 = status4 = status5 = "'部分已复核'";
        	status6 = "'已确认'";
        }else if(status.equals("部分付款中")){
        	status = status2 = status3 = status4 = status5 = "'部分已付款'";
        	status6 = "'已确认'";
        }
        
        String condition = "";
        if(spName != null || status != null || beginTime != null || endTime != null|| orderNo != null)
        {
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
		condition = " where nopaid_amount!=0 "
					+ " and ifnull(cname,'') like '%" + spName + "%' "
					+ " and ifnull(order_type,'') like '%" + orderType + "%' "
					+ " and create_time between '" + beginTime + "' and '" + endTime+ " 23:59:59' "
				    + " and ifnull(order_no,'') like '%" + orderNo + "%' ";
        }
        

        String sql = "select * from( SELECT ro.id, ro.order_no,'报销单' as order_type,ro.payment_type as payment_method,ro.account_name as payee_name,null as account_id,"
        		+ " ro.STATUS,null as invoice_no,ro.create_stamp create_time,ro.remark,"
        		+ " ro.amount total_amount,"
        		+ " ro.amount as application_amount ,"
        		+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "  //已付未付
				+ " WHERE caor.cost_order_id = ro.id AND caor.order_type = '报销单' "
				+ " ) paid_amount,"
				+ " (ro.amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
				+ " FROM cost_application_order_rel caor "
				+ " WHERE caor.cost_order_id = ro.id AND caor.order_type = '报销单'"
				+ " )) nopaid_amount ," //-----------------
        		+ " null as cname"
        		+ " FROM reimbursement_order ro"
        		+ " LEFT JOIN car_summary_order cso on cso.id in(ro.car_summary_order_ids)"
        		+ " where ro.STATUS in ("+status2+")"
        		+ " UNION "
        		+ " SELECT amco.id, amco.order_no,'成本单' as order_type, NULL AS payment_method, amco.others_name AS payee_name, NULL AS account_id, amco. STATUS, "
        		+ "  NULL AS invoice_no, amco.create_stamp create_time, amco.remark, amco.total_amount total_amount, "
        		+ " NULL AS application_amount, "
        		+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "  //已付未付
				+ " WHERE caor.cost_order_id = amco.id AND caor.order_type = '成本单' "
				+ " ) pay_amount,"
				+ " (amco.total_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
				+ " FROM cost_application_order_rel caor "
				+ " WHERE caor.cost_order_id = amco.id AND caor.order_type = '成本单'"
				+ " )) yufu_amount ," //-----------------
        		+ " (case amco.cost_to_type"
        		+ " when 'sp' then "
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.sp_id)"
                + " when 'customer' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.customer_id)"
                + " when 'insurance' then"
				+ " 	(select c.abbr from party p  "
				+ " 		LEFT JOIN contact c ON c.id = p.contact_id"
                + "         where p.id = amco.insurance_id)"
                + " end"
                + " ) AS cname FROM arap_misc_cost_order amco WHERE amco.STATUS in ("+status2+")"
                + " and amco.type = 'non_biz'  and amco.total_amount>=0 "
                + " UNION"
                + " SELECT cso.id,cso.order_no,'行车单' AS order_type,'' AS payment_method,cso.main_driver_name AS payee_name,NULL AS account_id,cso. STATUS,"
        		+ "  NULL AS invoice_no,cso.create_data create_time,'' AS remark,cso.actual_payment_amount total_amount,0 application_amount,"
        		+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "   //-------------
				+ " WHERE caor.cost_order_id = cso.id AND caor.order_type = '行车单' "
				+ " ) pay_amount,"
				+ " (cso.actual_payment_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
				+ " FROM cost_application_order_rel caor "
				+ " WHERE caor.cost_order_id = cso.id AND caor.order_type = '行车单'"
				+ " )) yufu_amount ,"  //-----------------
        		+ " '' cname"
        		+ " FROM car_summary_order cso"
        		+ " WHERE cso. STATUS in ("+status+")"
        		+ " AND cso.reimbursement_order_id IS NULL"
        		+ " UNION"
        		+ " SELECT aio.id, aio.order_no, '往来票据单' AS order_type, NULL AS payment_method, aio.charge_person AS payee_name,"
        		+ " NULL AS account_id, aio.pay_status status, NULL AS invoice_no, aio.create_date create_time,"
        		+ " aio.remark, aio.pay_amount total_amount, NULL AS application_amount,"
        		+ " ( SELECT ifnull(sum(caor.pay_amount), 0) FROM cost_application_order_rel caor "
        		+ " WHERE caor.cost_order_id = aio.id AND caor.order_type = '往来票据单'  ) paid_amount, "
        		+ " ( aio.pay_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) FROM cost_application_order_rel caor "
        		+ " WHERE caor.cost_order_id = aio.id AND caor.order_type = '往来票据单' ) ) nopaid_amount,"//已付未付
        		+ " aio.charge_unit AS cname"
        		+ " FROM arap_in_out_misc_order aio WHERE aio.pay_status IN (" + status4 + ")"
        		+ " UNION"
        		+ " SELECT aco.id, aco.order_no, '应付对账单' AS order_type, null AS payment_method,"
        		+ " null AS payee_name, NULL AS account_id, aco.status STATUS, NULL AS invoice_no, aco.create_stamp create_time,"
        		+ " aco.remark, aco.cost_amount total_amount, NULL AS application_amount,"
        		+ " ( SELECT ifnull(sum(caor.pay_amount),0) FROM cost_application_order_rel caor "  //-----------------------
				+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '对账单' "
				+ " ) pay_amount,"
				+ " (aco.cost_amount - (SELECT ifnull(sum(caor.pay_amount), 0) "
				+ " FROM cost_application_order_rel caor "
				+ " WHERE caor.cost_order_id = aco.id AND caor.order_type = '对账单'"
				+ " )) nopaid_amount ,"//*-----------------------
        		+ " c.abbr cname FROM arap_cost_order aco"
        		+ " LEFT JOIN party p ON p.id = aco.payee_id"
        		+ " LEFT JOIN contact c ON c.id = p.contact_id WHERE aco.status IN ("+ status5 +")"
        		+ " UNION "
        		+ " SELECT app.id, app.order_no, '预付单' AS order_type, null AS payment_method, "
        		+ " null AS payee_name, NULL AS account_id, null STATUS, NULL AS invoice_no, app.create_date create_time,"
        		+ " app.remark, app.total_amount total_amount, 0 AS application_amount,"
        		+ " ( SELECT ifnull(sum(caor.pay_amount),0 ) "
				+ " FROM cost_application_order_rel caor "   //------------------
				+ " WHERE caor.cost_order_id = app.id"
				+ " AND caor.order_type = '预付单'"
				+ " ) pay_amount,"
				+ " ( app.total_amount - ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay"
				+ " FROM cost_application_order_rel caor "
				+ " WHERE caor.cost_order_id = app.id "
				+ " AND caor.order_type = '预付单' ) ) yufu_amount ,"//-------------------
        		+ " c.abbr cname FROM arap_pre_pay_order app"
        		+ " LEFT JOIN party p ON p.id = app.sp_id"
        		+ " LEFT JOIN contact c ON c.id = p.contact_id WHERE app.status IN ("+ status2 +")"
        	    + " UNION"
        	    + " SELECT dor.id, dor.order_no, '货损单' AS order_type, 	dofi.fin_method AS payment_method, "
        	    + " '' AS payee_name,"
        	    + " NULL AS account_id, dofi.status STATUS, NULL AS invoice_no, dor.create_date create_time, dofi.remark,"
        	    + " ifnull(sum(dofi.amount),0) total_amount,"
        	    + " 0 AS application_amount,"
        	    + " ( SELECT ifnull(sum(caor.pay_amount), 0)"
        	    + " FROM cost_application_order_rel caor WHERE caor.cost_order_id = dor.id AND caor.order_type = '货损单' and caor.payee_unit = dofi.party_name ) pay_amount,"
        	    + " (ifnull(sum(dofi.amount),0) - ( SELECT ifnull(sum(caor.pay_amount), 0) total_pay FROM cost_application_order_rel caor"
        	    + " WHERE caor.cost_order_id = dor.id AND caor.order_type = '货损单' and caor.payee_unit = dofi.party_name )) yufu_amount,"
        	    + " (case when dofi.party_type ='客户' "
        	    + " then c.abbr "
        	    + " else"
        	    + " dofi.party_name"
        	    + " end) cname"
        	    + " FROM damage_order dor"
        	    + " LEFT JOIN damage_order_fin_item dofi on dofi.order_id = dor.id and dofi.type = 'cost' "
        	    + " LEFT JOIN party p ON p.id = dor.customer_id"
        	    + " LEFT JOIN contact c ON c.id = p.contact_id"
        	    + " WHERE"
        	    + " dofi. STATUS IN ("+ status6 +")"
        	    + " GROUP BY dofi.party_name  , dor.id"
        		+ ") A ";
        
        
        Record rec = Db.findFirst("select count(*) total from (" + sql + condition + " ) B");
        logger.debug("total records:" + rec.getLong("total"));

        String orderByStr = " order by A.create_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }
        List<Record> BillingOrders = Db.find(sql+ condition + orderByStr +sLimit);

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
        String spNameNo = getPara("sp")!=null?getPara("sp"):"";
        String spName=spNameNo.replace("\\", "%");
        String beginTime = getPara("beginTime")!=null?getPara("beginTime"):"";
        String endTime = getPara("endTime")!=null?getPara("endTime"):"";
        String applicationOrderNo = getPara("applicationOrderNo")!=null?getPara("applicationOrderNo"):"";
        String orderNo = getPara("orderNo")!=null?getPara("orderNo"):"";
        String status = getPara("status")!=null?getPara("status"):"";
        String check_begin_time = getPara("check_begin_date")!=null?getPara("check_begin_date"):"";
        String check_end_time = getPara("check_end_date")!=null?getPara("check_end_date"):"";
        String confirmBeginTime = getPara("confirmBeginTime")!=null?getPara("confirmBeginTime"):"";
        String confirmEndTime = getPara("confirmEndTime")!=null?getPara("confirmEndTime"):"";
        String insurance = getPara("insurance")!=null?getPara("insurance"):"";
		
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
        
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
      
        //String statusStr = " ('已复核')";
        if(status.equals("未复核")){
        	status = "'新建','已审批'";
        }else if(status.equals("已复核")){
        	status  = "'已复核'";
        }else if(status.equals("已付款")){
        	status  = "'已付款'";
        }else if(status.equals("旧数据")){
        	status  = "'已付款'";
        }
        
        String condition = "";
        if(!status.equals("")){
        	condition = " where aci.status in ("+status+")";
        }
        
        
        String conditions = " where 1=1 ";
        if (StringUtils.isNotEmpty(spName)){
        	conditions+=" and ifnull(cname,'') like '%" + spName + "%' ";
        }
        if (StringUtils.isNotEmpty(insurance)){
        	conditions+=" and ifnull(payee_id,'') = '" + insurance + "' ";
        }
        if (StringUtils.isNotEmpty(beginTime)){
        	beginTime = " and create_time between'"+beginTime+"'";
        }else{
        	beginTime =" and create_time between '1970-1-1'";
        }
        if (StringUtils.isNotEmpty(endTime)){
        	endTime =" and '"+endTime+" 23:59:59'";
        }else{
        	endTime =" and '2037-12-31'";
        }
        conditions+=beginTime+endTime;
        
        if (StringUtils.isNotEmpty(check_begin_time)){
        	check_begin_time = "and check_time between'"+check_begin_time+"'";
        }else{
        	check_begin_time =" and check_time between '0000-00-00'";
        }
        if (StringUtils.isNotEmpty(check_end_time)){
        	check_end_time =" and '"+check_end_time+" 23:59:59'";
        }else{
        	check_end_time =" and '2037-12-31'";
        }
        conditions+=check_begin_time+check_end_time;
        
        if (StringUtils.isNotEmpty(confirmBeginTime)){
        	confirmBeginTime = "and confirm_time between'"+confirmBeginTime+"'";
        }else{
        	confirmBeginTime =" and confirm_time between '0000-00-00'";
        }
        if (StringUtils.isNotEmpty(confirmEndTime)){
        	confirmEndTime =" and '"+confirmEndTime+" 23:59:59'";
        }else{
        	confirmEndTime =" and '2037-12-31'";
        }
        conditions+=confirmBeginTime+confirmEndTime;
        if (StringUtils.isNotEmpty(applicationOrderNo)){
        	conditions+=" and ifnull(application_order_no,'') like '%" + applicationOrderNo + "%' ";
        }
        if (StringUtils.isNotEmpty(orderNo)){
        	conditions+=" and ifnull(order_no,'') like '%" + orderNo + "%' ";
        }
        
        String sql = "select * from(select aci.id, aci.order_no application_order_no,'申请单' as order_type,aci.payee_id,"
        		+ " aci.payment_method, aci.payee_name, aci.account_id, aci.status, aci.create_stamp create_time,aci.check_stamp check_time,aci.confirm_stamp confirm_time ,aci.remark,"
                + " ( select sum(cao.pay_amount) from cost_application_order_rel cao where cao.application_order_id = aci.id ) application_amount,"
                + " GROUP_CONCAT( "
                + " case "
                + " when cao.order_type='对账单' "
                + " then (select order_no from arap_cost_order where id = cao.cost_order_id)"
                + " when cao.order_type='行车单'"
                + " then (select order_no from car_summary_order where id = cao.cost_order_id)"
                + " when cao.order_type='预付单'"
                + " then (select order_no from arap_pre_pay_order where id = cao.cost_order_id)"
                + " when cao.order_type='成本单'"
                + " then (select order_no from arap_misc_cost_order where id = cao.cost_order_id)"
                + " when cao.order_type='报销单'"
                + " then (select order_no from reimbursement_order where id = cao.cost_order_id)"
                + " when cao.order_type='往来票据单'"
                + " then (select order_no from arap_in_out_misc_order where id = cao.cost_order_id)"
                + " when cao.order_type='货损单'"
                + " then (select order_no from damage_order where id = cao.cost_order_id)"
                + " end SEPARATOR '</br>') order_no, "
        		+ " c.abbr cname "
        		+ " from arap_cost_invoice_application_order aci "
        		+ " LEFT JOIN cost_application_order_rel cao on cao.application_order_id = aci.id"
        		+ " left join party p on p.id = aci.payee_id left join contact c on c.id = p.contact_id "
        		+ condition
        		+ " group by aci.id "
        		+ ") A";
        
        
        Record rec = Db.findFirst("select count(*) total from (" + sql + conditions + " ) B");
        logger.debug("total records:" + rec.getLong("total"));

        String orderByStr = " order by A.create_time desc ";
        if(colName!=null){
        	if(colName.length()>0){
            	orderByStr = " order by A."+colName+" "+sortBy;
            }
        }
        
        List<Record> BillingOrders = Db.find(sql+ conditions + orderByStr +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
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
