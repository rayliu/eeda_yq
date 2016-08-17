package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeApplicationInvoiceNo;
import models.ArapChargeInvoice;
import models.ArapChargeInvoiceApplication;
import models.ArapChargeInvoiceItemInvoiceNo;
import models.ArapChargeOrder;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.OrderNoGenerator;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeInvoiceOrderController extends Controller {
    private Log logger = Log.getLog(ChargeInvoiceOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_LIST})
    public void index() {
    	    render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "");
        render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderCreateSearchList.html");
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_CREATE})
    public void create() {
        String ids = getPara("ids");
        String order_type = getPara("order_type");
        setAttr("order_type", order_type);

        DecimalFormat df = new DecimalFormat("0.00");

        if(order_type.equals("对账单")){
        	setAttr("chargePreInvoiceOrderIds", ids);	
            if(ids != null && !"".equals(ids)){
    			String[] idArray = ids.split(",");
    			logger.debug(String.valueOf(idArray.length));
    			Double totalAmount = 0.0;
    			for(int i=0;i<idArray.length;i++){
    				ArapChargeOrder rOrder = ArapChargeOrder.dao.findById(idArray[i]);
    				Double amount = rOrder.getDouble("charge_amount")==null?0.0:rOrder.getDouble("charge_amount");
    				totalAmount = totalAmount + amount;
    			}
    			setAttr("totalAmount", df.format(totalAmount));
    			
    			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArray[0]);
    			Long customerId = arapChargeOrder.get("payee_id");
    			Long spId = arapChargeOrder.get("sp_id");
    			setAttr("spId", spId);
    	        if(!"".equals(customerId) && customerId != null){
    		        Party party = Party.dao.findById(customerId);
    		        setAttr("party", party);	        
    		        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
    		        setAttr("customer", contact);
    		        setAttr("type", "CUSTOMER");
    		    	setAttr("classify", "");
    	        }
            }
        }else if(order_type.equals("申请单")){
        	setAttr("chargePreInvoiceOrderIds", ids);	
            if(ids != null && !"".equals(ids)){
    			String[] idArray = ids.split(",");
    			logger.debug(String.valueOf(idArray.length));
    			Double totalAmount = 0.0;
    			for(int i=0;i<idArray.length;i++){
    				ArapChargeInvoiceApplication rOrder = ArapChargeInvoiceApplication.dao.findById(idArray[i]);
    				Double amount = rOrder.getDouble("total_amount")==null?0.0:rOrder.getDouble("total_amount");
    				totalAmount = totalAmount + amount;
    			}
    			setAttr("totalAmount", totalAmount);
    			
    	        ArapChargeInvoiceApplication arapChargeInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(idArray[0]);
    			Long customerId = arapChargeInvoiceApplication.get("payee_id");
    			Long spId = arapChargeInvoiceApplication.get("sp_id");
    			setAttr("spId", spId);
    	        if(!"".equals(customerId) && customerId != null){
    		        Party party = Party.dao.findById(customerId);
    		        setAttr("party", party);	        
    		        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
    		        setAttr("customer", contact);
    		        setAttr("type", "CUSTOMER");
    		    	setAttr("classify", "");
    	        }
            }
        }
        
        
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);

        setAttr("status", "新建");
    		render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
    	String sLimit = "";
        String chargePreInvoiceOrderIds = getPara("chargePreInvoiceOrderIds");
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
      //升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_stamp desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }
        
        
        //TODO 网点没做,状态过滤没做
        String companyName = getPara("companyName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String orderNo = getPara("orderNo");
		//String status = getPara("status");
		String office = getPara("office");

		String sql = " select '对账单' order_type,aco.id,aco.order_no,aco.`STATUS`,aco.charge_amount total_amount,aco.remark,aco.create_stamp,c.abbr cname,"
				+ " ul.c_name create_by,c1.abbr sp,aco.begin_time check_stamp"
				+ " from arap_charge_order aco"
				+ " LEFT JOIN party p ON p.id = aco.payee_id"
				+ " LEFT JOIN contact c ON c.id = p.contact_id"
				+ " LEFT JOIN contact c1 ON c1.id = aco.sp_id"
				+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
				+ " where aco.STATUS ='已确认' and aco.have_invoice = 'Y'";
		
		String condition = "";
		if(companyName != null || beginTime != null || endTime != null
				|| orderNo != null || office != null ){
			
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			
			condition = " where ifnull(cname,'') like '%" + companyName + "%' "
					  + " and ifnull(order_no,'') like '%" + orderNo + "%' "
					  + " and create_stamp between '" + beginTime + "' and '" + endTime + "' ";

		}
        
        Record rec = Db.findFirst("select count(*) total from(select *  from ("+sql+") A "+condition+") B");
        logger.debug("total records:" + rec.getLong("total"));

        
        List<Record> BillingOrders = Db.find("select *  from ("+sql+") A "+condition + orderByStr + sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }

    // billing order 列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
      //升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_stamp desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }
        
        
    	String companyName = getPara("companyName");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("endTime");
    	String orderNo = getPara("orderNo");
    	String status = getPara("status");
    	String office = getPara("office");
    	String sp = getPara("sp");
    	String dzOrderNo = getPara("dzOrderNo");
    	String address = getPara("address");
    	//TODO 网点，提货地点，供应商没有做条件过滤
        String sqlTotal = "select count(1) total from arap_charge_invoice aci";

        
        String sql = "select aci.*,"
        		+ " (select group_concat(distinct aco.invoice_no separator '</br>') "
        		+ " from arap_charge_order aco where aco.invoice_order_id = aci.id) invoice_item_no ,"
        		+ " ifnull((select  group_concat( DISTINCT order_no SEPARATOR '</br>' ) "
        		+ " from arap_charge_invoice_application_order   "
        		+ " where invoice_order_id = aci.id), "
        		+ " (select  group_concat( DISTINCT order_no SEPARATOR '</br>' ) "
        		+ " from arap_charge_order where invoice_order_id = aci.id)) charge_order_no,"
        		+ " ul.c_name creator_name,c.abbr cname,c1.abbr sp "
        		+ " from arap_charge_invoice aci"
				+ " left join arap_charge_invoice_item_invoice_no acio on acio.invoice_id = aci.id "
				+ " left join user_login ul on ul.id = aci.create_by "
				+ " left join party p on p.id = aci.payee_id "
				+ " left join contact c on c.id = p.contact_id "
                + " left join contact c1 on c1.id = aci.sp_id ";
       String condition = "";
       if(companyName != null || beginTime != null || endTime != null || orderNo != null
    		  || status != null || office != null || sp != null || address != null || dzOrderNo != null){
    	    if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "9999-12-31";
			}
			condition = " where ifnull(c.abbr,'') like '%" + companyName + "%' "
					+ " and ifnull(aci.order_no,'') like '%" + orderNo + "%' "
					+ " and (select group_concat(distinct aco.order_no separator '</br>') "
					+ " from arap_charge_order aco where aco.invoice_order_id = aci.id) like '%" + dzOrderNo + "%' "
					+ " and aci.create_stamp between '" + beginTime + "' "
							+ " and '" + endTime + "' ";
			if(!"".equals(status) && status != null){
				condition = condition + " and aci.status = '" + status + "' "; 
			}
			sqlTotal = "select count(1) total from arap_charge_invoice aci"
					+ " left join user_login ul on ul.id = aci.create_by "
					+ " left join party p on p.id = aci.payee_id "
					+ " left join contact c on c.id = p.contact_id ";
       }
        
        
        Record rec = Db.findFirst(sqlTotal + condition );
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> BillingOrders = Db.find("select * from (" + sql + condition +  " group by aci.id order by aci.create_stamp desc " +") A"+ orderByStr + sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_CREATE, PermissionConstant.PERMSSION_CIO_UPDATE}, logical=Logical.OR)
    public void save(){
    	ArapChargeInvoice arapAuditInvoice = null;
    	String order_type = getPara("order_type");
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	String total_amount= getPara("total_amount");
    	if(order_type.equals("申请单")){
    		if(!"".equals(chargeInvoiceOrderId) && chargeInvoiceOrderId != null){
        		arapAuditInvoice = ArapChargeInvoice.dao.findById(chargeInvoiceOrderId);
    	    	arapAuditInvoice.set("create_stamp", new Date());
    	    	arapAuditInvoice.set("remark", getPara("remark"));
    	    	arapAuditInvoice.set("last_modified_by", getPara("create_by"));
    	    	arapAuditInvoice.set("last_modified_stamp", new Date());
    	    	arapAuditInvoice.update();
        	}else{
        		arapAuditInvoice = new ArapChargeInvoice();
                String sql = "select * from arap_charge_invoice order by id desc limit 0,1";
                arapAuditInvoice.set("order_no", OrderNoGenerator.getNextOrderNo("YSFP") );
        		arapAuditInvoice.set("create_by", getPara("create_by"));
        		arapAuditInvoice.set("status", getPara("status"));
    	    	arapAuditInvoice.set("create_stamp", new Date());
    	    	if(total_amount != null && !"".equals(total_amount)){
    	    		arapAuditInvoice.set("total_amount", total_amount);
    	    	}
    	    	arapAuditInvoice.set("remark", getPara("remark"));
    	    	if(getPara("customer_id") != null && !"".equals(getPara("customer_id"))){
    	    		arapAuditInvoice.set("payee_id", getPara("customer_id"));
    	    	}
    	    	String spId = getPara("spId");
    	    	if(spId != null && !"".equals(spId)){
    	    		arapAuditInvoice.set("sp_id", spId);
    	    	}
    	    	arapAuditInvoice.save();
    	    	
    	    	String ids = getPara("chargePreInvoiceOrderIds");
    	    	String[] idArr = null;
    	    	if(ids != null && !"".equals(ids)){
    	    		idArr = ids.split(",");
    	    		for(int i=0;i<idArr.length;i++){
    	    			ArapChargeInvoiceApplication application = ArapChargeInvoiceApplication.dao.findById(idArr[i]);
    	    			application.set("status", "开票中");
    	    			application.set("invoice_order_id", arapAuditInvoice.get("id"));
    	    			application.update();
    	    		}
    	    	}
        	}
    	}else if(order_type.equals("对账单")){
    		if(!"".equals(chargeInvoiceOrderId) && chargeInvoiceOrderId != null){
        		arapAuditInvoice = ArapChargeInvoice.dao.findById(chargeInvoiceOrderId);
    	    	arapAuditInvoice.set("create_stamp", new Date());
    	    	arapAuditInvoice.set("remark", getPara("remark"));
    	    	arapAuditInvoice.set("last_modified_by", getPara("create_by"));
    	    	arapAuditInvoice.set("last_modified_stamp", new Date());
    	    	arapAuditInvoice.update();
        	}else{
        		arapAuditInvoice = new ArapChargeInvoice();
                String sql = "select * from arap_charge_invoice order by id desc limit 0,1";
                arapAuditInvoice.set("order_no", OrderNoGenerator.getNextOrderNo("YSFP") );
        		arapAuditInvoice.set("create_by", getPara("create_by"));
        		arapAuditInvoice.set("status", getPara("status"));
    	    	arapAuditInvoice.set("create_stamp", new Date());
    	    	if(total_amount != null && !"".equals(total_amount)){
    	    		arapAuditInvoice.set("total_amount", total_amount);
    	    	}
    	    	arapAuditInvoice.set("remark", getPara("remark"));
    	    	if(getPara("customer_id") != null && !"".equals(getPara("customer_id"))){
    	    		arapAuditInvoice.set("payee_id", getPara("customer_id"));
    	    	}
    	    	String spId = getPara("spId");
    	    	if(spId != null && !"".equals(spId)){
    	    		arapAuditInvoice.set("sp_id", spId);
    	    	}
    	    	arapAuditInvoice.save();
    	    	
    	    	String ids = getPara("chargePreInvoiceOrderIds");
    	    	String[] idArr = null;
    	    	if(ids != null && !"".equals(ids)){
    	    		idArr = ids.split(",");
    	    		for(int i=0;i<idArr.length;i++){
    	    			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArr[i]);
    	    			arapChargeOrder.set("status", "开票中");
    	    			arapChargeOrder.set("invoice_order_id", arapAuditInvoice.get("id"));
    	    			arapChargeOrder.update();
    	    		}
    	    	}
        	}
    	}
    	
        renderJson(arapAuditInvoice);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CIO_UPDATE})
    public void edit() throws ParseException{
    	ArapChargeInvoice arapAuditInvoice = ArapChargeInvoice.dao.findById(getPara("id"));  	
    	UserLogin userLogin = UserLogin.dao.findById(arapAuditInvoice.get("create_by"));
    	setAttr("userLogin", userLogin);
    	setAttr("arapAuditInvoice", arapAuditInvoice); 
    	
    	Long customerId = arapAuditInvoice.get("payee_id");
    	if(customerId != null && !"".equals(customerId)){
    		Party party = Party.dao.findById(customerId);
	        setAttr("party", party);	        
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("customer", contact);
	        setAttr("type", "CUSTOMER");
	    	setAttr("classify", "");
    	}
    	String chargePreInvoiceOrderIds = "";
    	List<ArapChargeInvoiceApplication> arapChargeInvoiceApplications = ArapChargeInvoiceApplication.dao.find("select * from arap_charge_invoice_application_order where invoice_order_id = ?", getPara("id"));
    	if(arapChargeInvoiceApplications.size()>0){
    		for(ArapChargeInvoiceApplication arapChargeInvoiceApplication : arapChargeInvoiceApplications){
        		chargePreInvoiceOrderIds += arapChargeInvoiceApplication.get("id") + ",";
        	}
        	chargePreInvoiceOrderIds = chargePreInvoiceOrderIds.substring(0, chargePreInvoiceOrderIds.length() - 1);
        	setAttr("order_type", "申请单");
    	}else{
    		List<ArapChargeOrder> arapChargeOrderLsst = ArapChargeOrder.dao.find("select * from arap_charge_order where invoice_order_id = ?", getPara("id"));
    		for(ArapChargeOrder arapChargeOrder : arapChargeOrderLsst){
        		chargePreInvoiceOrderIds += arapChargeOrder.get("id") + ",";
        	}
        	chargePreInvoiceOrderIds = chargePreInvoiceOrderIds.substring(0, chargePreInvoiceOrderIds.length() - 1);
        	setAttr("order_type", "对账单");
    	}
    	
		setAttr("chargePreInvoiceOrderIds", chargePreInvoiceOrderIds);
    	render("/yh/arap/ChargeInvoiceOrder/ChargeInvoiceOrderEdit.html");
    }
    
    public void chargeInvoiceOrderList() {
    	String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from arap_charge_order aao where aao.id in ("+chargeCheckOrderIds+")";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db
                .find("select sum(tofi.amount) amount,aao.*,ror.order_no return_order_no, usl.user_name as creator_name,dor.order_no as delivery_order_no, ifnull(c.contact_person,c2.contact_person) cname, "
                		+ " ifnull(tor.order_no,(select group_concat(tor3.order_no separator '\r\n') from delivery_order dor  left join delivery_order_item doi2 on doi2.delivery_id = dor.id  left join transfer_order tor3 on tor3.id = doi2.transfer_order_id)) transfer_order_no from arap_charge_order aao "
						+ "	left join arap_charge_item aai on aai.charge_order_id = aao.id"
						+ "	left join return_order ror on ror.id = aai.ref_order_id"
						+ "	left join transfer_order tor on tor.id = ror.transfer_order_id  left join party p on p.id = tor.customer_id left join contact c on c.id = p.contact_id"
						+ "	left join delivery_order dor on ror.delivery_order_id = dor.id left join delivery_order_item doi on doi.delivery_id = dor.id"
						+ "	left join transfer_order tor2 on tor2.id = doi.transfer_order_id left join party p2 on p2.id = tor2.customer_id left join contact c2 on c2.id = p2.contact_id  left join user_login  usl on usl.id=ror.creator "                     
						+ "	left join transfer_order_fin_item tofi on tofi.order_id = ifnull(tor.id,tor2.id)"             
						+ "	left join fin_item fi on fi.id = tofi.fin_item_id" 
						+ "	where aao.id in ("+chargeCheckOrderIds+") group by ror.id order by ror.create_date desc " + sLimit);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
            orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
    // 添加发票
    public void addInvoiceItem(){
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if(chargeInvoiceOrderId != null && !"".equals(chargeInvoiceOrderId)){
    		ArapChargeInvoiceItemInvoiceNo arapChargeInvoiceItemInvoiceNo = new ArapChargeInvoiceItemInvoiceNo();
    		arapChargeInvoiceItemInvoiceNo.set("invoice_id", chargeInvoiceOrderId);
    		arapChargeInvoiceItemInvoiceNo.save();
    	}
        renderJson("{\"success\":true}");
    }
    
    // 发票号列表
    public void chargeInvoiceItemList(){
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	if(chargeInvoiceOrderId == null || "".equals(chargeInvoiceOrderId)){
    		chargeInvoiceOrderId = "-1";
    	}
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map orderMap = new HashMap();
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from arap_charge_invoice_item_invoice_no where invoice_id=" + chargeInvoiceOrderId;
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select group_concat(acao.order_no separator '\r\n') pre_order_no,c.abbr cname,acio.*"
        		+ " from arap_charge_invoice_item_invoice_no acio "
				+ " left join arap_charge_application_invoice_no acai on acai.invoice_no = acio.invoice_no"
				+ " left join arap_charge_invoice_application_order acao on acao.id = acai.application_order_id"
				+ " left join arap_charge_invoice aci on aci.id = acio.invoice_id"
				+ " left join party p on p.id = aci.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " where acio.invoice_id=" + chargeInvoiceOrderId + " group by acio.id " + sLimit);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
    // 更新InvoiceItem信息
    public void updateInvoiceItem(){
    	String chargeId  = getPara("chargeId");
    	String name = getPara("name");
    	String value = getPara("value");
    	
    	ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(chargeId);
    	arapChargeOrder.set(name, value);
    	arapChargeOrder.update();
        renderJson(arapChargeOrder);
    }
    
    // 获取所有的发票号
    public void findAllInvoiceItemNo(){
    	List<ArapChargeInvoiceItemInvoiceNo> arapChargeInvoiceItemInvoiceNos = ArapChargeInvoiceItemInvoiceNo.dao.find("select * from arap_charge_invoice_item_invoice_no where invoice_id = ?", getPara("chargeInvoiceOrderId"));
        renderJson(arapChargeInvoiceItemInvoiceNos);
    }
    
    // 更新开票申请单
    public void updatePreInvoice(){
    	String name = getPara("name");
    	String value = getPara("value");
    	
    	String[] values = null;
    	Map<String,String[]> map = getParaMap();
    	for(Map.Entry<String,String[]> entry : map.entrySet()){
    		if("value[]".equals(entry.getKey())){
    			values = entry.getValue();
    		}
    	}
    	String preInvoiceId = getPara("preInvoiceId");
    	List<ArapChargeApplicationInvoiceNo> arapChargeApplicationInvoiceNos = ArapChargeApplicationInvoiceNo.dao.find("select * from arap_charge_application_invoice_no where application_order_id = ?", preInvoiceId);
    	for(ArapChargeApplicationInvoiceNo chargeApplicationInvoiceNo : arapChargeApplicationInvoiceNos){
    		chargeApplicationInvoiceNo.delete();
    	}
    	for(int i=0;i<values.length;i++){
	    	ArapChargeApplicationInvoiceNo arapChargeApplicationInvoiceNo = ArapChargeApplicationInvoiceNo.dao.findFirst("select * from arap_charge_application_invoice_no where application_order_id = ? and invoice_no = ?", preInvoiceId, values[i]);
	    	if(arapChargeApplicationInvoiceNo == null){
	    		arapChargeApplicationInvoiceNo = new ArapChargeApplicationInvoiceNo();
	    		arapChargeApplicationInvoiceNo.set(name, values[i]);
		    	arapChargeApplicationInvoiceNo.set("application_order_id", preInvoiceId);
		    	arapChargeApplicationInvoiceNo.set("invoice_order_id", getPara("chargeInvoiceOrderId"));
		    	arapChargeApplicationInvoiceNo.save();
	    	}
    	}
        renderJson("{\"success\":true}");
    }
    
    public void chargePreInvoiceOrderList(){
    	String sLimit = "";
        String chargePreInvoiceOrderIds = getPara("chargePreInvoiceOrderIds");
        String order_type = getPara("order_type");
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        if(chargePreInvoiceOrderIds == null || "".equals(chargePreInvoiceOrderIds)){
        	chargePreInvoiceOrderIds = "-1";
        }
        String sqlTotal = "select count(1) total from arap_charge_order where id in("+chargePreInvoiceOrderIds+")";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        String sql  = "";
        if(order_type!=null){
        	if(order_type.equals("申请单")){
            	sql = "select aaia.*,c.abbr cname,ul.c_name create_by,ul2.user_name audit_by,ul3.user_name approval_by,"
        				+ " ( SELECT group_concat(aciii.invoice_no) FROM arap_charge_invoice_item_invoice_no aciii WHERE aciii.invoice_id = aaia.invoice_order_id ) invoice_no,'' check_stamp "
                		+ " from arap_charge_invoice_application_order aaia "
                		+ " left join party p on p.id = aaia.payee_id"
        				+ " left join contact c on c.id = p.contact_id"
                		+ " left join user_login ul on ul.id = aaia.create_by"
        				+ " left join user_login ul2 on ul2.id = aaia.audit_by"
        				+ " left join user_login ul3 on ul3.id = aaia.approver_by "
        				+ " where aaia.id in("+chargePreInvoiceOrderIds+") order by aaia.create_stamp desc " + sLimit;
            }else if(order_type.equals("对账单")){
            	sql = "select aaia.id ,aaia.order_no,aaia.remark,aaia.create_stamp,aaia.status,aaia.charge_amount total_amount,c.abbr cname,ul.c_name create_by,"
        				+ " aaia.invoice_no,aaia.begin_time check_stamp "
                		+ " from arap_charge_order aaia "
                		+ " left join party p on p.id = aaia.payee_id"
        				+ " left join contact c on c.id = p.contact_id"
                		+ " left join user_login ul on ul.id = aaia.create_by"
        				+ " where aaia.id in("+chargePreInvoiceOrderIds+") order by aaia.create_stamp desc " + sLimit;
            }
        }else{
        	sql = "select aaia.*,c.abbr cname,ul.user_name create_by,"
    				+ " (select group_concat(acai.invoice_no) from arap_charge_application_invoice_no acai where acai.application_order_id = aaia.id) invoice_no,aaia.begin_time check_stamp "
            		+ " from arap_charge_order aaia "
            		+ " left join party p on p.id = aaia.payee_id"
    				+ " left join contact c on c.id = p.contact_id"
            		+ " left join user_login ul on ul.id = aaia.create_by"
    				+ " where aaia.id in("+chargePreInvoiceOrderIds+") order by aaia.create_stamp desc " + sLimit;
        }
        
        
        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    //审核
    @Before(Tx.class)
    public void confirm(){
    	String chargeInvoiceOrderId = getPara("chargeInvoiceOrderId");
    	ArapChargeInvoice arapAuditInvoice = null;
    	if(!"".equals(chargeInvoiceOrderId) && chargeInvoiceOrderId != null){
    		arapAuditInvoice = ArapChargeInvoice.dao.findById(chargeInvoiceOrderId);
	    	arapAuditInvoice.set("status", "已审批");
	    	arapAuditInvoice.update();
    	}
    	
    	String ids = getPara("chargePreInvoiceOrderIds");
    	String order_type = getPara("order_type");
    	if(order_type.equals("申请单")){
    		String[] idArr = null;
        	if(ids != null && !"".equals(ids)){
        		idArr = ids.split(",");
        		for(int i=0;i<idArr.length;i++){
        			ArapChargeInvoiceApplication arapChargeInvoiceApplication = ArapChargeInvoiceApplication.dao.findById(idArr[i]);
        			arapChargeInvoiceApplication.set("status", "已开票");
        			arapChargeInvoiceApplication.update();
        		}
        	}
    	}else if(order_type.equals("对账单")){
    		String[] idArr = null;
    		if(ids != null && !"".equals(ids)){
        		idArr = ids.split(",");
        		for(int i=0;i<idArr.length;i++){
        			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(idArr[i]);
        			arapChargeOrder.set("status", "已开票");
        			arapChargeOrder.update();
        		}
        	}
    	}
    	
    	
    	renderJson(arapAuditInvoice);
    }
    
    
  //撤销单据
  	@Before(Tx.class)
  	public void deleteOrder() {
  		String id = getPara("orderId");
  		if("".equals(id)||id==null)
  			return;
  		String sql = "SELECT * FROM `charge_application_order_rel` where order_type = '开票记录单' and charge_order_id =" + id;
  		List<Record> nextOrders = Db.find(sql);
  		if (nextOrders.size() == 0) {
  			//更新相关单据的状态
  			//删除主表
  			//1.
  			List<ArapChargeOrder> acos = ArapChargeOrder.dao.find("select * from arap_charge_order where invoice_order_id = ?",id);
  			for (ArapChargeOrder aco:acos) {
  				aco.set("status", "已确认");
  				//清空开票单据号
  				aco.set("invoice_order_id", null).update();
  			}
  			
  			//2.删除主表
  			ArapChargeInvoice acic = ArapChargeInvoice.dao.findById(id);
  			acic.delete();
  			
  			renderJson("{\"success\":true}");
  		} else {
  			renderJson("{\"success\":false}");
  		}
  	}
    
}
