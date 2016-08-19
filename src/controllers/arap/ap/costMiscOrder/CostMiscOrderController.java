package controllers.arap.ap.costMiscOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostInvoiceApplication;
import models.ArapCostOrder;
import models.DepartOrder;
import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.eeda.profile.Account;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderDTO;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.LocationUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostMiscOrderController extends Controller {
    private Log logger = Log.getLog(CostMiscOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
    public void index() {
    	   render("/eeda/arap/CostMiscOrder/CostMiscOrderList.html");
    }


    public void confirm() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/eeda/arap/CostAcceptOrder/CostCheckOrderEdit.html");
    }

    public void list() {
    	String sp = getPara("sp");
        String customer = getPara("companyName");
        String spName = getPara("spName");
        String beginTime = getPara("beginTime");
        String endTime = getPara("endTime");
        String orderNo = getPara("orderNo");
        String status = getPara("status");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        
        String sql = "select amco.*, c1.abbr customer_name, c2.abbr sp_name from arap_misc_cost_order amco"
					+ " left join party p1 on amco.customer_id = p1.id"
					+ " left join party p2 on amco.sp_id = p2.id"
					+ " left join contact c1 on p1.contact_id = c1.id"
					+ " left join contact c2 on p2.contact_id = c2.id"
					+ " where amco.order_no not like 'SGSK%'";
        logger.debug("sql:" + sql);
        String condition = "";
        //TODO 始发地和目的地 客户没有做
        if(sp != null || customer != null || spName != null
        		|| status != null || beginTime != null || endTime != null|| orderNo != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			if (status!=null && "业务收款".equals(status)) {
				status = "biz";
			}
			if (status!=null && "非业务收款".equals(status)) {
				status = "non_biz";
			}
			condition = " and "
					+ " ifnull(c2.abbr,'') like '%" + spName + "%' "
					+ " and ifnull(c1.abbr,'') like '%" + customer + "%' "
					+ " and amco.order_no like '%" + orderNo + "%' "
					+ " and amco.create_stamp between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
			if(status!=null && status.length()>0){
				condition  += " and amco.type = '" + status + "' ";
			}
			
        }
        

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by amco.create_stamp desc " +sLimit);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    
	public void create() {
		String ids = getPara("ids");
		if(ids != null && !"".equals(ids)){
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));
	
			setAttr("costCheckOrderIds", ids); 
			ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(idArray[0]);
			Long spId = arapCostOrder.get("payee_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id").toString());
				setAttr("sp", contact); 
			}
		}

		setAttr("saveOK", false);
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));
		
		UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("userLogin", userLogin);

		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");
		setAttr("audit_status", "new");
		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);
		
			render("/eeda/arap/CostMiscOrder/CostMiscOrderEdit.html");
	}
    

    private ArapMiscCostOrder getDestOrder(String originOrderId) throws Exception {
    	ArapMiscCostOrder destOrder = ArapMiscCostOrder.dao.findFirst(
				"select * from arap_misc_cost_order where ref_order_id=?",
				originOrderId);
		if (destOrder == null)
			return destOrder;
		if (!"新建".equals(destOrder.getStr("status"))) {
			throw new Exception("对应的成本单状态已不是“新建”，不能修改本张单据。");
		}
		return destOrder;
	}

     
    
    
    @Before(Tx.class)
	public void save() throws Exception {		
		String jsonStr=getPara("params");
    	logger.debug(jsonStr);
    	 Gson gson = new Gson();  
         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
         
        ArapMiscCostOrder arapMiscCostOrder = new ArapMiscCostOrder();
		String costMiscOrderId = (String) dto.get("costMiscOrderId");
		String biz_type = (String) dto.get("biz_type");
		String customerId = (String) dto.get("customer_id");
		String spId = (String) dto.get("sp_id");
		String insurance_id = (String) dto.get("insurance_id");
		String routeFrom = (String) dto.get("route_from");
		String routeTo = (String) dto.get("route_to");
		String others_name = (String) dto.get("others_name");
		String ref_no = (String) dto.get("ref_no");
		String remark = (String) dto.get("remark");
		Double amount = (Double) dto.get("amount");
		String cost_to_type = (String) dto.get("cost_to_type");
		UserLogin user = LoginUserController.getLoginUser(this);
		
		String old_biz_type = "";
		if (!"".equals(costMiscOrderId) && costMiscOrderId != null) {
			//TODO: 如果已经应付确认过，就不能修改了
			arapMiscCostOrder = ArapMiscCostOrder.dao.findById(costMiscOrderId);
			old_biz_type = arapMiscCostOrder.getStr("type");
			
			if (!"".equals(customerId) && customerId != null) {
				arapMiscCostOrder.set("customer_id",customerId);
			}
			if (!"".equals(insurance_id) && insurance_id != null) {
				arapMiscCostOrder.set("insurance_id",insurance_id);
			}
			if (spId != null && !"".equals(spId)) {
				arapMiscCostOrder.set("sp_id",spId);
			}
			arapMiscCostOrder.set("others_name",others_name);
			arapMiscCostOrder.set("ref_no",ref_no);
			arapMiscCostOrder.set("type", biz_type);
			arapMiscCostOrder.set("cost_to_type", cost_to_type);
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("remark", remark);
			if (amount != null && !"".equals(amount)) {
				arapMiscCostOrder.set("total_amount", amount);
			}
			
			arapMiscCostOrder.update();
		} else {
			arapMiscCostOrder = new ArapMiscCostOrder();
			arapMiscCostOrder.set("status", "新建");
			if (!"".equals(customerId) && customerId != null) {
				arapMiscCostOrder.set("customer_id",customerId);
			}
			if (spId != null && !"".equals(spId)) {
				arapMiscCostOrder.set("sp_id",spId);
			}
			if (!"".equals(insurance_id) && insurance_id != null) {
				arapMiscCostOrder.set("insurance_id",insurance_id);
			}
			arapMiscCostOrder.set("others_name",others_name);
			arapMiscCostOrder.set("ref_no",ref_no);
			arapMiscCostOrder.set("type", biz_type);
			arapMiscCostOrder.set("cost_to_type", cost_to_type);
			arapMiscCostOrder.set("route_from", routeFrom);
			arapMiscCostOrder.set("route_to", routeTo);
			arapMiscCostOrder.set("remark", remark);
			arapMiscCostOrder.set("audit_status", "新建");
			arapMiscCostOrder.set("office_id", user.getLong("office_id"));
			arapMiscCostOrder.set("create_by", user.getLong("id"));
			arapMiscCostOrder.set("create_stamp", new Date());
			arapMiscCostOrder.set("order_no", OrderNoGenerator.getNextOrderNo("SGFK"));
			
			if (amount != null && !"".equals(amount)) {
				arapMiscCostOrder.set("total_amount", amount);
			}	
			arapMiscCostOrder.save();
		}
		
		
		List<Map> items =  (List<Map>) dto.get("items");
		for(Map item : items){
			String itemId = (String) item.get("ID");
			String action = (String) item.get("ACTION");
			
			
			if ("".equals(itemId)) {
				ArapMiscCostOrderItem arapMiscCostOrderItem = new ArapMiscCostOrderItem();
				arapMiscCostOrderItem.set("status", "新建");
				arapMiscCostOrderItem.set("fin_item_id", item.get("NAME"));
				arapMiscCostOrderItem.set("amount", item.get("AMOUNT"));
				arapMiscCostOrderItem.set("creator", user.getLong("id"));
				arapMiscCostOrderItem.set("create_date", new Date());
				arapMiscCostOrderItem.set("customer_order_no", item.get("CUSTOMER_ORDER_NO"));
				arapMiscCostOrderItem.set("item_desc", item.get("ITEM_DESC"));
				arapMiscCostOrderItem.set("misc_order_id", arapMiscCostOrder.getLong("id"));
				arapMiscCostOrderItem.save();
			}else{
				ArapMiscCostOrderItem arapMiscCostOrderItem = ArapMiscCostOrderItem.dao.findById(itemId);
				if ("delete".equals(action)) {
					if(arapMiscCostOrderItem != null)
						arapMiscCostOrderItem.delete();
				}else{
					arapMiscCostOrderItem.set("fin_item_id", item.get("NAME"));
					arapMiscCostOrderItem.set("amount", item.get("AMOUNT"));
					arapMiscCostOrderItem.set("creator", user.getLong("id"));
					arapMiscCostOrderItem.set("last_update_date", new Date());
					arapMiscCostOrderItem.set("customer_order_no", item.get("CUSTOMER_ORDER_NO"));
					arapMiscCostOrderItem.set("item_desc", item.get("ITEM_DESC"));
					arapMiscCostOrderItem.update();
				}
				
			}
			
		}

		
		List<ArapMiscCostOrderItem> itemList = 
				ArapMiscCostOrderItem.dao.find("select amcoi.*, fi.name from arap_misc_cost_order_item amcoi, fin_item fi"
						+ " where amcoi.fin_item_id=fi.id and misc_order_id=?", costMiscOrderId);
		
		ArapMiscCostOrderDTO returnDto = new ArapMiscCostOrderDTO();
		returnDto.setOrder(arapMiscCostOrder);
		returnDto.setItemList(itemList);
		renderJson(returnDto);
	}
	// 审核
	public void auditCostPreInvoiceOrder(){
		String costPreInvoiceOrderId = getPara("costPreInvoiceOrderId");
		if(costPreInvoiceOrderId != null && !"".equals(costPreInvoiceOrderId)){
			ArapCostInvoiceApplication arapAuditOrder = ArapCostInvoiceApplication.dao.findById(costPreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
            String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
        renderJson("{\"success\":true}");
	}
    
	public void edit() {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);
		
		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(id);
		Long spId = null;
		if(arapMiscCostOrder!=null)	
			spId = arapMiscCostOrder.get("sp_id");
		
		if (!"".equals(spId) && spId != null) {
			Party party = Party.dao.findById(spId);
			setAttr("spParty", party);
			Contact contact = Contact.dao.findById(party.get("contact_id").toString());
			setAttr("spContact", contact); 
		}
		
		Long customerId = null;
		if(arapMiscCostOrder!=null)	
			customerId = arapMiscCostOrder.get("customer_id");
		
		if (!"".equals(customerId) && customerId != null) {
			Party party = Party.dao.findById(customerId);
			setAttr("customerparty", party);
			Contact contact = Contact.dao.findById(party.get("contact_id").toString());
			setAttr("customerContact", contact); 
		}
		Long insuranceId = null;
		if(arapMiscCostOrder!=null)	
			insuranceId = arapMiscCostOrder.get("insurance_id");
		if (!"".equals(insuranceId) && insuranceId != null) {
			Party party = Party.dao.findById(insuranceId);
			setAttr("insuranceparty", party);
			Contact contact = Contact.dao.findById(party.get("contact_id").toString());
			setAttr("insuranceContact", contact); 
		}
		
		String routeFrom = arapMiscCostOrder.get("route_from");
		Location locationFrom = LocationUtil.getLocation(routeFrom);
		setAttr("locationFrom", locationFrom);
		
		String routeTo = arapMiscCostOrder.get("route_to");
		Location locationTo = LocationUtil.getLocation(routeTo);
		setAttr("locationTo", locationTo);
				
		UserLogin userLogin = UserLogin.dao.findById(arapMiscCostOrder.get("create_by"));
		setAttr("userLogin", userLogin);
		setAttr("arapMiscCostOrder", arapMiscCostOrder);
		
		
		// 获取当前页的数据
			List<Record> itemList = Db
					.find("select amcoi.*,"
							+ "fi.name name "
						+ " from arap_misc_cost_order_item amcoi"
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
						+ " where amcoi.misc_order_id = '"+ id +"' ");
			setAttr("itemList", itemList);
			render("/eeda/arap/CostMiscOrder/CostMiscOrderEdit.html");
	}
    

	public void searchAllAccount(){
		List<Account> accounts = Account.dao.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}
	
	public void addNewFee(){
		ArapMiscCostOrderItem arapMiscCostOrderItem = new ArapMiscCostOrderItem();
		arapMiscCostOrderItem.set("status", "新建");
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
		arapMiscCostOrderItem.set("creator", users.get(0).get("id"));
		arapMiscCostOrderItem.set("create_date", new Date());
		arapMiscCostOrderItem.set("fin_item_id", "1");
		arapMiscCostOrderItem.set("misc_order_id", getPara("costMiscOrderId"));
		arapMiscCostOrderItem.save();
		renderJson(arapMiscCostOrderItem);
	}
	
	public void updateCostMiscOrderItem(){
		String paymentId = getPara("paymentId");
		String costMiscOrderId = getPara("costMiscOrderId");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			ArapMiscCostOrderItem arapMiscCostOrderItem = ArapMiscCostOrderItem.dao.findById(paymentId);
			arapMiscCostOrderItem.set(name, value);
			arapMiscCostOrderItem.update();
		}
		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findById(costMiscOrderId);
		Record record = Db.findFirst("select sum(amount) sum_amount from arap_misc_cost_order_item where misc_order_id = ?", costMiscOrderId);
		arapMiscCostOrder.set("total_amount", record.get("sum_amount"));
		arapMiscCostOrder.update();
		
		
		renderJson(arapMiscCostOrder);
	}
	
	public void finItemdel(){
		ArapMiscCostOrderItem.dao.deleteById(getPara());
		renderJson("{\"success\":true}");
	}
	
	public void costCheckList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String costCheckOrderIds = getPara("costCheckOrderIds");
		if(costCheckOrderIds == null || "".equals(costCheckOrderIds)){
			costCheckOrderIds = "-1";
		}
		String sqlTotal = "select count(1) total from arap_cost_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_cost_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " where aao.id in ("+costCheckOrderIds+") order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
	
	public void costCheckorderListById(){
		String costCheckOrderIds = getPara("costCheckOrderIds");
		if(costCheckOrderIds == null || "".equals(costCheckOrderIds)){
			costCheckOrderIds = "-1";
		}
		String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from arap_cost_order";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aco.*,group_concat(acoo.invoice_no separator ',') invoice_no,c.abbr cname,ul.user_name creator_name from arap_cost_order aco"
        		+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id"
        		+ " left join user_login ul on ul.id = aco.create_by"
        		+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id where aco.id = "+costCheckOrderIds+" group by aco.id";

        logger.debug("sql:" + sql);
        List<Record> BillingOrders = Db.find(sql);

        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
	}
	
	
	public void saveMiscPartyInfo(){
		String miscId = getPara("miscId");
		String partyId = getPara("partyId");
		String partyType = getPara("partyType");
		ArapMiscCostOrder misc = ArapMiscCostOrder.dao.findById(miscId);
		if(Party.PARTY_TYPE_SERVICE_PROVIDER.equals(partyType))
			misc.set("payee_id", partyId).update();
		else
			misc.set("customer_id", partyId).update();
		renderJson("{\"success\":true}");
	}
	
	//根据单据类型按日期条件查找相应单号
	public void findOrderNoByOrderType(){
		String input = getPara("input");
		String orderType = getPara("orderType");
		String orderStamp = getPara("orderStamp");
		List<Record> recordList = new ArrayList<Record>();
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		if("transferOrder".equals(orderType))
			recordList = Db.find("select tor.id,tor.order_no from transfer_order tor "
					+ " left join office o on o.id = tor.office_id "
					+ " where to_days(create_stamp) = to_days('" + orderStamp + "') "
					+ " and tor.order_no like '%" + input + "%' "
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("pickupOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.depart_no  as order_no from depart_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where dor.combine_type = '" + DepartOrder.COMBINE_TYPE_PICKUP + "'"
					+ " and	to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.depart_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("departOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.depart_no  as order_no from depart_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where dor.combine_type = '" + DepartOrder.COMBINE_TYPE_DEPART + "'"
					+ " and	to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.depart_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");
		else if("deliveryOrder".equals(orderType))
			recordList = Db.find("select dor.id,dor.order_no from delivery_order dor"
					+ " left join user_login u on u.id = dor.create_by"
					+ " left join office o on o.id = u.office_id"
					+ " where to_days(create_stamp) = to_days('" + orderStamp + "')"
					+ " and dor.order_no like '%" + input + "%'"
					+ " and (o.id = " + parentID + " or o.belong_office = " + parentID + ");");	
		renderJson(recordList);
	}
	
	
	
}
