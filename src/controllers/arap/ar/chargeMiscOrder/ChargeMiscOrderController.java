package controllers.arap.ar.chargeMiscOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeApplication;
import models.ArapChargeOrder;
import models.Party;
import models.UserLogin;
import models.eeda.profile.Account;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrderDTO;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrderItem;
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
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeMiscOrderController extends Controller {
	private Log logger = Log.getLog(ChargeMiscOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render("/eeda/arap/ChargeMiscOrder/ChargeMiscOrderList.html");
	}

	public void confirm() {
		String ids = getPara("ids");
		String[] idArray = ids.split(",");
		logger.debug(String.valueOf(idArray.length));

		String customerId = getPara("customerId");
		Party party = Party.dao.findById(customerId);

		Contact contact = Contact.dao.findById(party.get("contact_id")
				.toString());
		setAttr("customer", contact);
		setAttr("type", "CUSTOMER");
		setAttr("classify", "receivable");
		render("/eeda/arap/ChargeAcceptOrder/ChargeCheckOrderEdit.html");
	}

	public void list() {
		String orderNo = getPara("orderNo");
		String type = getPara("type");
		String customer = getPara("customer");
		String sp = getPara("sp");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		String conditions = "";
		if (orderNo == null && type == null && customer == null && sp == null
				&& beginTime == null && endTime == null) {

		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}

			conditions = " and ifnull(order_no, '') like '%" + orderNo
					+ "%' and ifnull(customer_name,'') like '%" + customer
					+ "%' and ifnull(sp_name,'') like '%" + sp + "%' "
					+ " and create_stamp between " + " '" + beginTime
					+ "' and '" + endTime + " 23:59:59' ";
			if (type != null && type.length() > 0) {
				conditions += ("and type = '" + type + "' ");
			}
		}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlFrom = " from(select amco.*, "
				+ " (select c.abbr from party p left join contact c on p.contact_id = c.id where p.id = amco.customer_id) customer_name, "
				+ " (select c.abbr from party p left join contact c on p.contact_id = c.id where p.id = amco.sp_id) sp_name "
				+ " from arap_misc_charge_order amco where amco.order_no like 'SGSK%') A";

		String sqlTotal = "select count(1) total " + sqlFrom + " where 1=1 "
				+ conditions;
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		String orderByStr = " order by create_stamp asc ";
        if(colName!=null && colName.length()>0){
        	orderByStr = " order by "+colName+" "+sortBy;
        }
		String sql = "select * " + sqlFrom + " where 1=1 " + conditions
				+ orderByStr + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void create() {
		String ids = getPara("ids");
		if (ids != null && !"".equals(ids)) {
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));

			setAttr("chargeCheckOrderIds", ids);
			ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao
					.findById(idArray[0]);
			Long customerId = arapChargeOrder.get("payee_id");
			if (!"".equals(customerId) && customerId != null) {
				Party party = Party.dao.findById(customerId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
				setAttr("customer", contact);
				setAttr("type", "CUSTOMER");
				setAttr("classify", "");
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
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);
		setAttr("status", "new");

		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);

		render("/eeda/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}

	@Before(Tx.class)
	public void save() throws Exception {
		String jsonStr = getPara("params");
		logger.debug(jsonStr);
		Gson gson = new Gson();
		Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);

		ArapMiscChargeOrder arapMiscChargeOrder = new ArapMiscChargeOrder();
		String chargeMiscOrderId = (String) dto.get("chargeMiscOrderId");
		String biz_type = (String) dto.get("biz_type");
		String charge_from_type = (String) dto.get("charge_from_type");
		String customer_id = (String) dto.get("customer_id");
		String sp_id = (String) dto.get("sp_id");
		String others_name = (String) dto.get("others_name");
		String ref_no = (String) dto.get("ref_no");
		Double amount = (Double) dto.get("amount");
		String remark = (String) dto.get("remark");

		UserLogin user = LoginUserController.getLoginUser(this);
		String old_biz_type = "";
		if (!"".equals(chargeMiscOrderId) && chargeMiscOrderId != null) {
			arapMiscChargeOrder = ArapMiscChargeOrder.dao
					.findById(chargeMiscOrderId);

			old_biz_type = arapMiscChargeOrder.getStr("type");
			arapMiscChargeOrder.set("type", biz_type);
			arapMiscChargeOrder.set("charge_from_type", charge_from_type);
			if (sp_id != null && !"".equals(sp_id)) {
				arapMiscChargeOrder.set("sp_id", sp_id);
			}
			if (customer_id != null && !"".equals(customer_id)) {
				arapMiscChargeOrder.set("customer_id", customer_id);
			}
			arapMiscChargeOrder.set("total_amount", amount);
			arapMiscChargeOrder.set("others_name", others_name);
			arapMiscChargeOrder.set("ref_no", ref_no);
			arapMiscChargeOrder.set("remark", remark);
			arapMiscChargeOrder.update();
		} else {
			arapMiscChargeOrder.set("status", "新建");
			arapMiscChargeOrder.set("type", biz_type);
			arapMiscChargeOrder.set("charge_from_type", charge_from_type);

			if (!"".equals(customer_id) && customer_id != null) {
				arapMiscChargeOrder.set("customer_id", customer_id);
			}
			if (sp_id != null && !"".equals(sp_id)) {
				arapMiscChargeOrder.set("sp_id", sp_id);
			}

			arapMiscChargeOrder.set("order_no",
					OrderNoGenerator.getNextOrderNo("SGSK", user.getLong("office_id")));
			if (getPara("chargeCheckOrderIds") != null
					&& !"".equals(getPara("chargeCheckOrderIds"))) {
				arapMiscChargeOrder.set("charge_order_id",
						getPara("chargeCheckOrderIds"));
			}

			arapMiscChargeOrder.set("total_amount", amount);
			arapMiscChargeOrder.set("others_name", others_name);
			arapMiscChargeOrder.set("ref_no", ref_no);

			arapMiscChargeOrder.set("create_by", user.getLong("id"));
			arapMiscChargeOrder.set("create_stamp", new Date());
			arapMiscChargeOrder.set("office_id", user.getLong("office_id"));
			arapMiscChargeOrder.set("remark", remark);
			arapMiscChargeOrder.save();
		}

		// Db.update(
		// "delete from arap_misc_charge_order_item where misc_order_id = ?",
		// chargeMiscOrderId);

		List<Map> items = (List<Map>) dto.get("items");
		for (Map item : items) {
			String itemId = (String) item.get("ID");
			String action = (String) item.get("ACTION");
			if ("".equals(itemId)) {
				ArapMiscChargeOrderItem arapMiscChargeOrderItem = new ArapMiscChargeOrderItem();

				arapMiscChargeOrderItem.set("status", "新建");
				arapMiscChargeOrderItem.set("creator", user.get("id"));
				arapMiscChargeOrderItem.set("create_date", new Date());
				arapMiscChargeOrderItem.set("customer_order_no",
						item.get("CUSTOMER_ORDER_NO"));
				arapMiscChargeOrderItem.set("item_desc", item.get("ITEM_DESC"));
				arapMiscChargeOrderItem.set("fin_item_id", item.get("NAME"));
				arapMiscChargeOrderItem.set("amount", item.get("AMOUNT"));
				arapMiscChargeOrderItem.set("misc_order_id",
						arapMiscChargeOrder.getLong("id"));
				arapMiscChargeOrderItem.save();
			} else {
				ArapMiscChargeOrderItem arapMiscChargeOrderItem = ArapMiscChargeOrderItem.dao
						.findById(itemId);
				if ("DELETE".equals(action)) {
					if(arapMiscChargeOrderItem!=null)
						arapMiscChargeOrderItem.delete();
				} else {
					arapMiscChargeOrderItem.set("customer_order_no",
							item.get("CUSTOMER_ORDER_NO"));
					arapMiscChargeOrderItem.set("item_desc",
							item.get("ITEM_DESC"));
					arapMiscChargeOrderItem
							.set("fin_item_id", item.get("NAME"));
					arapMiscChargeOrderItem.set("amount", item.get("AMOUNT"));
					arapMiscChargeOrderItem.update();
				}
			}

		}

		
		List<ArapMiscChargeOrderItem> itemList = 
				ArapMiscChargeOrderItem.dao.find("select amcoi.*, fi.name from arap_misc_charge_order_item amcoi, fin_item fi"
						+ " where amcoi.fin_item_id=fi.id and misc_order_id=?", chargeMiscOrderId);
		
		ArapMiscChargeOrderDTO orderDto = new ArapMiscChargeOrderDTO();
		orderDto.setOrder(arapMiscChargeOrder);
		orderDto.setItemList(itemList);
		renderJson(orderDto);
	}


	// 审核
	public void auditChargePreInvoiceOrder() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if (chargePreInvoiceOrderId != null
				&& !"".equals(chargePreInvoiceOrderId)) {
			ArapChargeApplication arapAuditOrder = ArapChargeApplication.dao
					.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审核");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("audit_by", users.get(0).get("id"));
			arapAuditOrder.set("audit_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}

	// 审批
	public void approvalChargePreInvoiceOrder() {
		String chargePreInvoiceOrderId = getPara("chargePreInvoiceOrderId");
		if (chargePreInvoiceOrderId != null
				&& !"".equals(chargePreInvoiceOrderId)) {
			ArapChargeApplication arapAuditOrder = ArapChargeApplication.dao
					.findById(chargePreInvoiceOrderId);
			arapAuditOrder.set("status", "已审批");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("approver_by", users.get(0).get("id"));
			arapAuditOrder.set("approval_stamp", new Date());
			arapAuditOrder.update();
		}
		renderJson("{\"success\":true}");
	}

	public void edit() throws ParseException {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.emptyList();
		receivableItemList = Db.find("select * from fin_item where type='应收'");
		setAttr("receivableItemList", receivableItemList);

		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
				.findById(id);
		UserLogin userLogin = UserLogin.dao.findById(arapMiscChargeOrder
				.get("create_by"));

		setAttr("userLogin", userLogin);

		if (arapMiscChargeOrder.getLong("ref_order_id") != null) {
			ArapMiscChargeOrder refOrder = ArapMiscChargeOrder.dao
					.findFirst(
							"select amcoi.* from arap_misc_charge_order amcoi where id=?",
							arapMiscChargeOrder.getLong("ref_order_id"));
			if (refOrder != null)
				setAttr("ref_order_no", refOrder.get("order_no"));
		} else {
			ArapMiscChargeOrder refOrder = ArapMiscChargeOrder.dao
					.findFirst(
							"select amcoi.* from arap_misc_charge_order amcoi where ref_order_id=?",
							id);
			if (refOrder != null)
				setAttr("ref_order_no", refOrder.get("order_no"));
		}
		setAttr("arapMiscChargeOrder", arapMiscChargeOrder);

		Record r = Db
				.findFirst("select * from party p left join contact c on p.contact_id = c.id where p.id = '"
						+ arapMiscChargeOrder.getLong("customer_id") + "'");
		if (r != null)
			setAttr("customer_name", r.get("company_name"));
		Record q = Db
				.findFirst("select * from party p left join contact c on p.contact_id = c.id where p.id = '"
						+ arapMiscChargeOrder.getLong("sp_id") + "'");
		if (q != null)
			setAttr("sp_name", q.get("company_name"));

		// 获取从表
		List<Record> itemList = Db
				.find("select amcoi.*,amco.order_no charge_order_no,c.abbr cname,fi.name name from arap_misc_charge_order_item amcoi"
						+ " left join arap_misc_charge_order amco on amco.id = amcoi.misc_order_id"
						+ " left join arap_charge_order aco on aco.id = amco.charge_order_id"
						+ " left join party p on p.id = aco.payee_id"
						+ " left join contact c on c.id = p.contact_id"
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
						+ " where amco.id =?", id);
		setAttr("itemList", itemList);
		render("/eeda/arap/ChargeMiscOrder/ChargeMiscOrderEdit.html");
	}

	public void chargeMiscOrderItemList() {
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		if (chargeMiscOrderId == null || "".equals(chargeMiscOrderId)) {
			chargeMiscOrderId = "-1";
		}
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		Map orderMap = new HashMap();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from arap_misc_charge_order_item amcoi where amcoi.misc_order_id = "
				+ chargeMiscOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*, fi.name name from arap_misc_charge_order_item amcoi "
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id"
						+ " where amcoi.misc_order_id = "
						+ chargeMiscOrderId);

	

		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	public void searchAllAccount() {
		List<Account> accounts = Account.dao
				.find("select * from fin_account where type != 'PAY' and bank_name != '现金'");
		renderJson(accounts);
	}

	public void addNewFee() {
		ArapMiscChargeOrderItem arapMiscChargeOrderItem = new ArapMiscChargeOrderItem();
		arapMiscChargeOrderItem.set("status", "新建");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		arapMiscChargeOrderItem.set("creator", users.get(0).get("id"));
		arapMiscChargeOrderItem.set("create_date", new Date());
		arapMiscChargeOrderItem.set("fin_item_id", 4);
		arapMiscChargeOrderItem.set("misc_order_id",
				getPara("chargeMiscOrderId"));
		arapMiscChargeOrderItem.save();
		renderJson(arapMiscChargeOrderItem);
	}

	public void updateChargeMiscOrderItem() {
		String paymentId = getPara("paymentId");
		String chargeMiscOrderId = getPara("chargeMiscOrderId");
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		String name = getPara("name");
		String value = getPara("value");
		if ("amount".equals(name) && "".equals(value)) {
			value = "0";
		}
		if (paymentId != null && !"".equals(paymentId)) {
			ArapMiscChargeOrderItem arapMiscChargeOrderItem = ArapMiscChargeOrderItem.dao
					.findById(paymentId);
			arapMiscChargeOrderItem.set(name, value);
			arapMiscChargeOrderItem.update();
		}
		ArapMiscChargeOrder arapMiscChargeOrder = ArapMiscChargeOrder.dao
				.findById(chargeMiscOrderId);
		Record record = Db
				.findFirst(
						"select sum(amount) sum_amount from arap_misc_charge_order_item where misc_order_id = ?",
						chargeMiscOrderId);
		arapMiscChargeOrder.set("total_amount", record.get("sum_amount"));
		arapMiscChargeOrder.update();

		if (chargeCheckOrderIds != null && !"".equals(chargeCheckOrderIds)) {
			if ("amount".equals(name) && value != null && !"".equals(value)) {
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao
						.findById(chargeCheckOrderIds);
				Double debit_amount = arapChargeOrder.getDouble("debit_amount");
				Double misc_total_amount = Double.parseDouble(value);
				Double total_amount = arapChargeOrder.getDouble("total_amount");
				arapChargeOrder.set("debit_amount", debit_amount
						+ misc_total_amount);
				arapChargeOrder.set("charge_amount", total_amount
						- (debit_amount + misc_total_amount));
				arapChargeOrder.update();
			}
		}
		renderJson(arapMiscChargeOrder);
	}

	

	public void chargeCheckList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String chargeCheckOrderIds = getPara("chargeCheckOrderIds");
		if (chargeCheckOrderIds == null || "".equals(chargeCheckOrderIds)) {
			chargeCheckOrderIds = "-1";
		}
		String sqlTotal = "select count(1) total from arap_charge_order";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " where aao.id in ("
				+ chargeCheckOrderIds
				+ ") order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void chargeCheckOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(distinct aao.id) total from arap_charge_order aao"
				+ " left join arap_misc_charge_order amco on amco.charge_order_id = aao.id"
				+ " where ifnull(amco.charge_order_id,0)>0";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select distinct aao.*, usl.user_name as creator_name,c.abbr cname"
				+ " from arap_charge_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login usl on usl.id=aao.create_by"
				+ " left join arap_misc_charge_order amco on amco.charge_order_id = aao.id"
				+ " where ifnull(amco.charge_order_id,0)>0"
				+ " order by aao.create_stamp desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}
}
