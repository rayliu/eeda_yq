package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.DeliveryOrderFinItem;
import models.DepartOrder;
import models.DepartOrderFinItem;
import models.InsuranceFinItem;
import models.InsuranceOrder;
import models.Party;
import models.PickupOrderFinItem;
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
public class CostCheckOrderController extends Controller {
	private Log logger = Log.getLog(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_LIST })
	public void index() {
		setAttr("type", "CUSTOMER");
		setAttr("classify", "");
		render("/eeda/arap/CostCheckOrder/CostCheckOrderList.html");
	}

	public void add() {
		setAttr("type", "CUSTOMER");
		setAttr("classify", "");
		render("/eeda/arap/CostCheckOrder/CostCheckOrderCreateSearchList.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_CREATE })
	public void create() {
		String beginTime = getPara("beginTime");
		if (beginTime != null && !"".equals(beginTime)) {
			setAttr("beginTime", beginTime);
		}
		String endTime = getPara("endTime");
		if (endTime != null && !"".equals(endTime)) {
			setAttr("endTime", endTime);
		}
		String orderIds = getPara("ids");
		String orderNos = getPara("orderNos");
		String[] orderIdsArr = orderIds.split(",");
		String[] orderNoArr = orderNos.split(",");
		Double totalAmount = 0.0;
		Double changeAmount = 0.0;
		Long spId = null;
		for (int i = 0; i < orderIdsArr.length; i++) {
			Record rec = null;
			Record rec1 = null;
			if ("提货".equals(orderNoArr[i])) {
				rec = Db.findFirst(
						"select sum(amount) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付' and dofi.fin_item_id!=7 and IFNULL(dofi.cost_source,'') != '对账调整金额'",
						orderIdsArr[i]);
				if (rec.getDouble("sum_amount") != null) {
					totalAmount = totalAmount + rec.getDouble("sum_amount");
				}
				rec1 = Db
						.findFirst(
								"select sum(amount) change_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and dofi.fin_item_id!=7",
								orderIdsArr[i]);
				if (rec1.getDouble("change_amount") != null) {
					changeAmount = changeAmount
							+ rec1.getDouble("change_amount");
				} else {
					if (rec.getDouble("sum_amount") != null) {
						changeAmount = rec.getDouble("sum_amount")
								+ changeAmount;
					}
				}
				DepartOrder departOrder = DepartOrder.dao
						.findById(orderIdsArr[i]);
				spId = departOrder.getLong("sp_id");
			} else if ("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])) {
				rec = Db.findFirst(
						"select sum(amount) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额'",
						orderIdsArr[i]);
				if (rec.getDouble("sum_amount") != null) {
					totalAmount = totalAmount + rec.getDouble("sum_amount");
				}
				rec1 = Db
						.findFirst(
								"select sum(amount) change_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ?",
								orderIdsArr[i]);
				if (rec1.getDouble("change_amount") != null) {
					changeAmount = changeAmount
							+ rec1.getDouble("change_amount");
				} else {
					if (rec.getDouble("sum_amount") != null) {
						changeAmount = rec.getDouble("sum_amount")
								+ changeAmount;
					}
				}
				DepartOrder departOrder = DepartOrder.dao
						.findById(orderIdsArr[i]);
				spId = departOrder.getLong("sp_id");
			} else if ("配送".equals(orderNoArr[i])) {
				// DeliveryOrderFinItem deliveryorderfinitem
				// =DeliveryOrderFinItem.dao.findById(paymentId);
				rec = Db.findFirst(
						"select sum(amount) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额'",
						orderIdsArr[i]);
				if (rec.getDouble("sum_amount") != null) {
					totalAmount = totalAmount + rec.getDouble("sum_amount");
				}
				rec1 = Db
						.findFirst(
								"select sum(amount) change_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ?",
								orderIdsArr[i]);
				if (rec1.getDouble("change_amount") != null) {
					changeAmount = changeAmount
							+ rec1.getDouble("change_amount");
				} else {
					if (rec.getDouble("sum_amount") != null) {
						changeAmount = rec.getDouble("sum_amount")
								+ changeAmount;
					}
				}
				DeliveryOrder deliveryOrder = DeliveryOrder.dao
						.findById(orderIdsArr[i]);
				spId = deliveryOrder.getLong("sp_id");
			} else if ("成本单".equals(orderNoArr[i])) {
				// 这是成本单的应付
				rec = Db.findFirst(
						"select sum(amount) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'",
						orderIdsArr[i]);
				if (rec.getDouble("sum_amount") != null) {
					totalAmount = totalAmount + rec.getDouble("sum_amount");
				}
				rec1 = Db
						.findFirst(
								"select sum(amount) change_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ?",
								orderIdsArr[i]);
				if (rec1.getDouble("change_amount") != null) {
					changeAmount = changeAmount
							+ rec1.getDouble("change_amount");
				} else {
					if (rec.getDouble("sum_amount") != null) {
						changeAmount = rec.getDouble("sum_amount")
								+ changeAmount;
					}
				}

				ArapMiscCostOrder arapmisc = ArapMiscCostOrder.dao
						.findById(orderIdsArr[i]);
				spId = arapmisc.getLong("sp_id");
				/*
				 * InsuranceOrder insuraceOrder =
				 * InsuranceOrder.dao.findById(orderIdsArr[i]); spId =
				 * insuraceOrder.get("sp_id");
				 */
			} else {
				// 这是保险单的应付
				rec = Db.findFirst(
						"select sum(insurance_amount) sum_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ? and fi.type ='应付' and IFNULL(ifi.cost_source,'') != '对账调整金额'",
						orderIdsArr[i]);
				if (rec.getDouble("sum_amount") != null) {
					totalAmount = totalAmount + rec.getDouble("sum_amount");
				}
				rec1 = Db
						.findFirst(
								"select sum(insurance_amount) change_amount from insurance_fin_item ifi where ifi.insurance_order_id = ?",
								orderIdsArr[i]);
				if (rec1.getDouble("change_amount") != null) {
					changeAmount = changeAmount
							+ rec1.getDouble("change_amount");
				} else {
					if (rec.getDouble("sum_amount") != null) {
						changeAmount = rec.getDouble("sum_amount")
								+ changeAmount;
					}
				}
				InsuranceOrder insuraceOrder = InsuranceOrder.dao
						.findById(orderIdsArr[i]);
				spId = insuraceOrder.get("insurance_id");
			}
		}
		if (!"".equals(spId) && spId != null) {
			Party party = Party.dao.findById(spId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("sp", contact);
		} else {
			// 由于没有保险公司的维护，暂时实例化一个虚拟的
			Contact contact = new Contact();
			contact.set("company_name", "保险公司");
			setAttr("sp", contact);
		}
		Double actualAmount = totalAmount - changeAmount;
		setAttr("orderIds", orderIds);
		setAttr("orderNos", orderNos);
		setAttr("changeAmount", changeAmount);
		setAttr("is_pdf", "Y");
		String name = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + name + "'");
		setAttr("create_by", users.get(0).get("id"));
		UserLogin ul = UserLogin.dao.findById(users.get(0).get("id"));
		setAttr("create_name", ul.get("c_name"));
		setAttr("status", "new");
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}

	// 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
	public void createList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		// 根据company_id 过滤
		String colsLength = getPara("iColumns");
		String fieldsWhere = "AND (";
		for (int i = 0; i < Integer.parseInt(colsLength); i++) {
			String mDataProp = getPara("mDataProp_" + i);
			String searchValue = getPara("sSearch_" + i);
			logger.debug(mDataProp + "[" + searchValue + "]");
			if (searchValue != null && !"".equals(searchValue)) {
				if (mDataProp.equals("COMPANY_ID")) {
					fieldsWhere += "p.id" + " = " + searchValue + " AND ";
				} else {
					fieldsWhere += mDataProp + " like '%" + searchValue
							+ "%' AND ";
				}
			}
		}
		logger.debug("2nd filter:" + fieldsWhere);
		if (fieldsWhere.length() > 8) {
			fieldsWhere = fieldsWhere.substring(0, fieldsWhere.length() - 4);
			fieldsWhere += ')';
		} else {
			fieldsWhere = "";
		}
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total FROM RETURN_ORDER ro left join party p on ro.customer_id = p.id "
				+ "where ro.TRANSACTION_STATUS = 'confirmed' ";
		Record rec = Db.findFirst(sql + fieldsWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("SELECT ro.*, to.order_no as transfer_order_no, do.order_no as delivery_order_no, p.id as company_id, c.company_name FROM RETURN_ORDER ro "
						+ "left join transfer_order to on ro.transfer_order_id = to.id "
						+ "left join delivery_order do on ro.delivery_order_id = do.id "
						+ "left join party p on ro.customer_id = p.id "
						+ "left join contact c on p.contact_id = c.id where ro.TRANSACTION_STATUS = 'confirmed' "
						+ fieldsWhere);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
		orderMap.put("aaData", orders);
		renderJson(orderMap);
	}

	// billing order 列表
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_LIST })
	public void list() {
		String sLimit = "";		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String orderNo = getPara("order_no");
		String sp = getPara("sp");
		String status = getPara("status");
		String serial_no = getPara("serial_no")== null ? "" : getPara("serial_no");
		if("".equals(serial_no)){
			serial_no="''";
		}else{
			serial_no="'"+serial_no+"'";
		}
		
		String user_name = currentUser.getPrincipal().toString();
		
		String sqlTotal = "";
		String sql = "select aco.*,MONTH(aco.begin_time) as c_stamp,o.office_name oname, '' as company_name,"
				+ " group_concat(acoo.invoice_no separator ',') invoice_no,"
				+ " c.abbr cname,"
				+ " ifnull(ul.c_name,ul.user_name) creator_name,"
				+ " (select case "
				+ " when aciao. status = '已付款确认' then aciao.status "
				+ " when aciao.status != '已付款确认' and aciao.status !='' then '付款申请中' "
				+ " else acor.status end as status "
				+ " from arap_cost_order acor left join arap_cost_invoice_application_order aciao on acor.application_order_id = aciao.id where acor.id = aco.id) as order_status,"
				+ " (SELECT CASE " + " when "
				+ serial_no
				+ " != '' then "
				+ " concat((SELECT ifnull(group_concat(order_no,'-',"
				+ serial_no
				+ " SEPARATOR '\r\n'),'') from delivery_order where id in (SELECT delivery_id from transfer_order_item_detail where serial_no="
				+ serial_no
				+ ") and aci.ref_order_no='配送'),"
				+ " (SELECT ifnull(group_concat(depart_no,'-',"
				+ serial_no
				+ " SEPARATOR '\r\n'),'') from depart_order where id in (SELECT pickup_id from transfer_order_item_detail where serial_no="
				+ serial_no
				+ ") and aci.ref_order_no='提货'),"
				+ " (SELECT ifnull(group_concat(depart_no,'-',"
				+ serial_no
				+ " SEPARATOR '\r\n'),'') from depart_order where id in (SELECT depart_id from transfer_order_item_detail where serial_no="
				+ serial_no
				+ ") and aci.ref_order_no='零担') )"
				+ " ELSE '' end) serial_no "
				+ " from arap_cost_order aco "
				+ " left join party p on p.id = aco.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " LEFT JOIN arap_cost_item aci ON aci.cost_order_id = aco.id"
				+ " left join office o ON o.id=p.office_id"
				+ " left join user_login ul on ul.id = aco.create_by"
				+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id "
		        + " left join user_login u on aco.create_by = u.id"
		        + " left join user_office uo on u.user_name = uo.user_name and uo.is_main=1";
		String condition = " where 1=1 and uo.office_id in (select office_id from user_office where user_name='"+user_name+"')";
		        
		// TODO 始发地和目的地 客户没有做
		if (orderNo != null || sp != null || status != null|| (status != null && !"''".equals(serial_no))) {
			condition += " and aco.order_no like '%"
					+ orderNo
					+ "%' "
					+ " and ifnull(c.abbr,'') like '%"
					+ sp
					+ "%' "
					+ " and (select case "
					+ " when aciao. status = '已付款确认' then aciao.status "
					+ " when aciao.status != '已付款确认' and aciao.status !='' then '付款申请中' "
					+ " else acor.status end as status "
					+ " from arap_cost_order acor left join arap_cost_invoice_application_order aciao on acor.application_order_id = aciao.id where acor.id = aco.id) like '%"
					+ status + "%' ";
			if (serial_no != null && !"''".equals(serial_no)) {
				condition += " and ((aci.ref_order_id in (SELECT delivery_id from transfer_order_item_detail where serial_no="
						+ serial_no
						+ ") and aci.ref_order_no='配送' )"
						+ " or (aci.ref_order_id in (SELECT pickup_id from transfer_order_item_detail where serial_no="
						+ serial_no
						+ ") and aci.ref_order_no='提货' )"
						+ " or (aci.ref_order_id in (SELECT depart_id from transfer_order_item_detail where serial_no="
						+ serial_no + ") and aci.ref_order_no='零担' ))";
			}
		}

		sqlTotal = "select count(1) total from arap_cost_order aco "
				+ " left join party p on p.id = aco.payee_id"
				+ " left join contact c on c.id = p.contact_id"
				+ " left join user_login ul on ul.id = aco.create_by"
				+ " left join arap_cost_order_invoice_no acoo on acoo.cost_order_id = aco.id "
				+ " LEFT JOIN arap_cost_item aci ON aci.cost_order_id = aco.id";

		Record rec = Db.findFirst("select count(1) total from (" + sql + condition+"  group by aco.id order by aco.create_stamp desc ) a");
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> BillingOrders = Db.find(sql + condition
				+ " group by aco.id order by aco.create_stamp desc " + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_CREATE,
			PermissionConstant.PERMSSION_CCOI_UPDATE }, logical = Logical.OR)
	@Before(Tx.class)
	public void save() {
		ArapCostOrder arapAuditOrder = null;
		String costCheckOrderId = getPara("costCheckOrderId");
		String total_amount = getPara("total_amount");
		String debit_amount = getPara("debit_amount") == "" ? "0"
				: getPara("debit_amount");
		String orderIds = getPara("orderIds");
		String orderNos = getPara("orderNos");
		String[] orderIdsArr = orderIds.split(",");
		String[] orderNoArr = orderNos.split(",");
		if (!"".equals(costCheckOrderId) && costCheckOrderId != null) {
			arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
			// arapAuditOrder.set("order_type", );
			arapAuditOrder.set("status", "新建");
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			if (getParaToDate("begin_time") != null) {
				arapAuditOrder.set("begin_time", getPara("begin_time"));
			}
			if (getParaToDate("end_time") != null) {
				arapAuditOrder.set("end_time", getPara("end_time"));
			}
			arapAuditOrder.set("total_amount", total_amount);
			if (total_amount != null && !"".equals(total_amount)
					&& debit_amount != null && !"".equals(debit_amount)) {
				arapAuditOrder.set(
						"cost_amount",
						Double.parseDouble(total_amount)
								- Double.parseDouble(debit_amount));
			}
			arapAuditOrder.update();
			for (int i = 0; i < orderIdsArr.length; i++) {
				ArapCostItem arapAuditItem = ArapCostItem.dao
						.findFirst(
								"SELECT * from arap_cost_item  where ref_order_id=? and ref_order_no=?",
								orderIdsArr[i], orderNoArr[i]);
				if (arapAuditItem == null) {
					ArapCostItem addArapAuditItem = new ArapCostItem();
					addArapAuditItem.set("ref_order_id", orderIdsArr[i]);
					addArapAuditItem.set("ref_order_no", orderNoArr[i]);
					addArapAuditItem.set("cost_order_id",
							arapAuditOrder.get("id"));
					// arapAuditItem.set("item_status", "");
					addArapAuditItem.set("create_by", getPara("create_by"));
					addArapAuditItem.set("create_stamp", new Date());
					addArapAuditItem.save();
					if ("提货".equals(orderNoArr[i])) {
						DepartOrder departOrder = DepartOrder.dao
								.findById(orderIdsArr[i]);
						departOrder.set("audit_status", "对账中");
						departOrder.update();
					} else if ("零担".equals(orderNoArr[i])) {
						DepartOrder departOrder = DepartOrder.dao
								.findById(orderIdsArr[i]);
						departOrder.set("audit_status", "对账中");
						departOrder.update();
					} else if ("配送".equals(orderNoArr[i])) {
						DeliveryOrder deliveryOrder = DeliveryOrder.dao
								.findById(orderIdsArr[i]);
						deliveryOrder.set("audit_status", "对账中");
						deliveryOrder.update();
					} else if ("成本单".equals(orderNoArr[i])) {
						ArapMiscCostOrder arapmisc = ArapMiscCostOrder.dao
								.findById(orderIdsArr[i]);
						arapmisc.set("audit_status", "对账中");
						arapmisc.update();
					} else {
						InsuranceOrder insuranceOrder = InsuranceOrder.dao
								.findById(orderIdsArr[i]);
						insuranceOrder.set("audit_status", "对账中");
						insuranceOrder.update();
					}
				}
			}
		} else {
			arapAuditOrder = new ArapCostOrder();
			arapAuditOrder.set("order_no",
					OrderNoGenerator.getNextOrderNo("YFDZ"));
			// arapAuditOrder.set("order_type", );
			String sp_id = getPara("sp_id");
			if (sp_id == null || "".equals(sp_id)) {
				sp_id = null;
			}
			arapAuditOrder.set("status", "新建");
			arapAuditOrder.set("payee_id", sp_id);
			arapAuditOrder.set("create_by", getPara("create_by"));
			arapAuditOrder.set("create_stamp", new Date());
			arapAuditOrder.set("remark", getPara("remark"));
			if (getParaToDate("begin_time") != null) {
				arapAuditOrder.set("begin_time", getPara("begin_time"));
			}
			if (getParaToDate("end_time") != null) {
				arapAuditOrder.set("end_time", getPara("end_time"));
			}
			arapAuditOrder.set("total_amount", total_amount);
			if (total_amount != null && !"".equals(total_amount)
					&& debit_amount != null && !"".equals(debit_amount)) {
				arapAuditOrder.set(
						"cost_amount",
						Double.parseDouble(total_amount)
								- Double.parseDouble(debit_amount));
			}
			arapAuditOrder.save();
			for (int i = 0; i < orderIdsArr.length; i++) {
				ArapCostItem arapAuditItem = new ArapCostItem();
				// arapAuditItem.set("ref_order_type", );
				arapAuditItem.set("ref_order_id", orderIdsArr[i]);
				arapAuditItem.set("ref_order_no", orderNoArr[i]);
				arapAuditItem.set("cost_order_id", arapAuditOrder.get("id"));
				// arapAuditItem.set("item_status", "");
				arapAuditItem.set("create_by", getPara("create_by"));
				arapAuditItem.set("create_stamp", new Date());
				arapAuditItem.save();
			}

		}
		for (int i = 0; i < orderIdsArr.length; i++) {
			if ("提货".equals(orderNoArr[i])) {
				DepartOrder departOrder = DepartOrder.dao
						.findById(orderIdsArr[i]);
				departOrder.set("audit_status", "对账中");
				departOrder.update();
			} else if ("零担".equals(orderNoArr[i])) {
				DepartOrder departOrder = DepartOrder.dao
						.findById(orderIdsArr[i]);
				departOrder.set("audit_status", "对账中");
				departOrder.update();
			} else if ("配送".equals(orderNoArr[i])) {
				DeliveryOrder deliveryOrder = DeliveryOrder.dao
						.findById(orderIdsArr[i]);
				deliveryOrder.set("audit_status", "对账中");
				deliveryOrder.update();
			} else if ("成本单".equals(orderNoArr[i])) {
				ArapMiscCostOrder arapmisc = ArapMiscCostOrder.dao
						.findById(orderIdsArr[i]);
				arapmisc.set("audit_status", "对账中");
				arapmisc.update();

			} else {
				InsuranceOrder insuranceOrder = InsuranceOrder.dao
						.findById(orderIdsArr[i]);
				insuranceOrder.set("audit_status", "对账中");
				insuranceOrder.update();
			}
		}

		renderJson(arapAuditOrder);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_UPDATE })
	public void edit() {
		ArapCostOrder arapAuditOrder = ArapCostOrder.dao
				.findById(getPara("id"));
		Long spId = arapAuditOrder.get("payee_id");
		if (!"".equals(spId) && spId != null) {
			Party party = Party.dao.findById(spId);
			setAttr("party", party);
			Contact contact = Contact.dao.findById(party.get("contact_id")
					.toString());
			setAttr("sp", contact);
		}

		UserLogin create_user = UserLogin.dao.findById(arapAuditOrder
				.get("create_by"));
		setAttr("create_user", create_user);
		UserLogin confirm_user = UserLogin.dao.findById(arapAuditOrder
				.get("confirm_by"));
		setAttr("confirm_user", confirm_user);

		setAttr("arapAuditOrder", arapAuditOrder);
		setAttr("is_pdf", arapAuditOrder.get("is_pdf"));

		String orderIds = "";
		String orderNos = "";
		List<ArapCostItem> arapCostItems = ArapCostItem.dao.find(
				"select * from arap_cost_item where cost_order_id = ?",
				arapAuditOrder.get("id"));
		for (ArapCostItem arapCostItem : arapCostItems) {
			orderIds += arapCostItem.get("ref_order_id") + ",";
			orderNos += arapCostItem.get("ref_order_no") + ",";
		}
		orderIds = orderIds.substring(0, orderIds.length() - 1);
		orderNos = orderNos.substring(0, orderNos.length() - 1);
		setAttr("orderIds", orderIds);
		setAttr("orderNos", orderNos);
		// 调整金额
		String[] orderIdsArr = orderIds.split(",");
		String[] orderNoArr = orderNos.split(",");
		Record rec1 = null;
		Record rec = null;
		Double totalamount = 0.00;
		Double changeamount = 0.00;
		for (int i = 0; i < orderIdsArr.length; i++) {
			if ("提货".equals(orderNoArr[i])) {
				rec1 = Db
						.findFirst(
								"select ifnull(sum(amount),0) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额' and dofi.fin_item_id!=7",
								orderIdsArr[i]);
				if (rec1.getDouble("sum_amount") != null) {
					totalamount = totalamount + rec1.getDouble("sum_amount");
				}
				rec = Db.findFirst(
						"select sum(amount) change_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and dofi.fin_item_id!=7",
						orderIdsArr[i]);
				if (rec.getDouble("change_amount") != null) {
					changeamount = changeamount
							+ rec.getDouble("change_amount");
				}
			} else if ("零担".equals(orderNoArr[i])) {
				rec1 = Db
						.findFirst(
								"select ifnull(sum(amount),0) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'  and IFNULL(dofi.cost_source,'') !='对账调整金额'",
								orderIdsArr[i]);
				totalamount = totalamount + rec1.getDouble("sum_amount");
				rec = Db.findFirst(
						"select sum(amount) change_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? ",
						orderIdsArr[i]);
				if (rec.getDouble("change_amount") != null) {
					changeamount = changeamount
							+ rec.getDouble("change_amount");
				}
			} else if ("配送".equals(orderNoArr[i])) {
				rec1 = Db
						.findFirst(
								"select ifnull(sum(amount),0) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额'",
								orderIdsArr[i]);
				totalamount = totalamount + rec1.getDouble("sum_amount");
				rec = Db.findFirst(
						"select sum(amount) change_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ?",
						orderIdsArr[i]);
				if (rec.getDouble("change_amount") != null) {
					changeamount = changeamount
							+ rec.getDouble("change_amount");
				}
			} else if ("成本单".equals(orderNoArr[i])) {
				rec1 = Db
						.findFirst(
								"select ifnull(sum(amount),0) sum_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ? and fi.type ='应付'",
								orderIdsArr[i]);
				totalamount = totalamount + rec1.getDouble("sum_amount");
				rec = Db.findFirst(
						"select sum(amount) change_amount from arap_misc_cost_order_item amcoi left join fin_item fi on fi.id = amcoi.fin_item_id  where amcoi.misc_order_id = ?",
						orderIdsArr[i]);
				if (rec.getDouble("change_amount") != null) {
					changeamount = changeamount
							+ rec.getDouble("change_amount");
				}
			} else {
				rec1 = Db.findFirst(
						"select ifnull(sum(insurance_amount),0) sum_amount from insurance_fin_item ifi "
						+ " left join fin_item fi on fi.id = ifi.fin_item_id "
						+ " where ifi.insurance_order_id =? and fi.type ='应付'" ,orderIdsArr[i]);
				totalamount = totalamount + rec1.getDouble("sum_amount");
				rec = Db.findFirst(
						"select sum(ifi.insurance_amount) change_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ?",
						orderIdsArr[i]);
				if (rec.getDouble("change_amount") != null) {
					changeamount = changeamount
							+ rec.getDouble("change_amount");
				}
			}

			Double actualamount = totalamount - changeamount;

			setAttr("totalAmount",
					Double.valueOf(String.format("%.2f", totalamount)));
			setAttr("changeAmount",
					Double.valueOf(String.format("%.2f", changeamount)));
			setAttr("actualAmount", actualamount);
		}
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}

	// 审核

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_AFFIRM })
	@Before(Tx.class)
	public void auditCostCheckOrder() {
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostOrder arapAuditOrder = null;
		if (costCheckOrderId != null && !"".equals(costCheckOrderId)) {
			arapAuditOrder = ArapCostOrder.dao.findById(costCheckOrderId);
			arapAuditOrder.set("status", "已确认");
			String name = (String) currentUser.getPrincipal();
			List<UserLogin> users = UserLogin.dao
					.find("select * from user_login where user_name='" + name
							+ "'");
			arapAuditOrder.set("confirm_by", users.get(0).get("id"));
			arapAuditOrder.set("confirm_stamp", new Date());
			arapAuditOrder.update();
			List<ArapCostItem> list = ArapCostItem.dao.find(
					"select * from arap_cost_item where cost_order_id = ?",
					arapAuditOrder.get("id"));
			if (list.size() > 0) {
				for (ArapCostItem arapCostItem : list) {
					if ("零担".equals(arapCostItem.get("ref_order_no"))) {
						DepartOrder departOrder = DepartOrder.dao
								.findById(arapCostItem.get("ref_order_id"));
						departOrder.set("audit_status", "对账已确认");
						departOrder.update();
					} else if ("保险".equals(arapCostItem.get("ref_order_no"))) {
						InsuranceOrder insuranceOrder = InsuranceOrder.dao
								.findById(arapCostItem.get("ref_order_id"));
						insuranceOrder.set("audit_status", "对账已确认");
						insuranceOrder.update();
					} else if ("提货".equals(arapCostItem.get("ref_order_no"))) {
						DepartOrder pickupOrder = DepartOrder.dao
								.findById(arapCostItem.get("ref_order_id"));
						pickupOrder.set("audit_status", "对账已确认");
						pickupOrder.update();
					} else if ("配送".equals(arapCostItem.get("ref_order_no"))) {
						DeliveryOrder deliveryOrder = DeliveryOrder.dao
								.findById(arapCostItem.get("ref_order_id"));
						deliveryOrder.set("audit_status", "对账已确认");
						deliveryOrder.update();
					} else {
						ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao
								.findById(arapCostItem.get("ref_order_id"));
						arapMiscCostOrder.set("audit_status", "对账已确认");
						arapMiscCostOrder.update();
					}
				}
			}

			// updateReturnOrderStatus(arapAuditOrder, "对账已确认");
		}
		Map BillingOrderListMap = new HashMap();
		UserLogin ul = UserLogin.dao.findById(arapAuditOrder.get("confirm_by"));
		BillingOrderListMap.put("arapAuditOrder", arapAuditOrder);
		BillingOrderListMap.put("ul", ul);
		renderJson(BillingOrderListMap);
	}

	@Before(Tx.class)
	public void costConfirmList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String orderNos = getPara("orderNos");
		String orderIds = getPara("orderIds");
		String[] orderNoArr = orderNos.split(",");
		String[] orderIdArr = orderIds.split(",");
		String pickupOrderIds = "";
		String departOrderIds = "";
		String deliveryOrderIds = "";
		String insuranceOrderIds = "";
		String pickupOrderSql = "";
		String departOrderSql = "";
		String deliveryOrderSql = "";
		String insuranceOrderSql = "";
		for (int i = 0; i < orderNoArr.length; i++) {
			String preOrderNo = orderNoArr[i].substring(0, 2);
			if ("PC".equals(preOrderNo)) {
				pickupOrderIds += orderIdArr[i] + ",";
			} else if ("FC".equals(preOrderNo)) {
				departOrderIds += orderIdArr[i] + ",";
			} else if ("PS".equals(preOrderNo)) {
				deliveryOrderIds += orderIdArr[i] + ",";
			} else {
				insuranceOrderIds += orderIdArr[i] + ",";
			}
		}
		if (!"".equals(pickupOrderIds)) {
			pickupOrderIds = pickupOrderIds.substring(0,
					pickupOrderIds.length() - 1);
			pickupOrderSql = " union select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'提货' business_type, "
					+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
					+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>')"
					+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
					+ " from return_order ror "
					+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
					+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
					+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
					+ " left join depart_order dpr on dpr.id = dtr.pickup_id"
					+ " left join transfer_order tor on tor.id = dtr.order_id "
					+ " left join transfer_order_item_detail toid on toid.order_id = tor.id "
					+ " left join transfer_order_item toi on toi.id = toid.item_id "
					+ " left join product prod on toi.product_id = prod.id "
					+ " left join user_login ul on ul.id = dpr.create_by "
					+ " left join party p on p.id = dpr.sp_id "
					+ " left join contact c on c.id = p.contact_id"
					+ " left join office oe on oe.id = tor.office_id"
					+ " where dor.id = ror.delivery_order_id and dpr.id in("
					+ pickupOrderIds
					+ ") and (ifnull(dpr.id, 0) > 0)"
					+ " group by dpr.id ";
		}
		if (!"".equals(departOrderIds)) {
			departOrderIds = departOrderIds.substring(0,
					departOrderIds.length() - 1);
			departOrderSql = " union select distinct dpr.id,dpr.depart_no,dpr.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,'零担' business_type, "
					+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付') pay_amount, "
					+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>')"
					+ " transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name"
					+ " from return_order ror "
					+ " left join delivery_order dor on dor.id = ror.delivery_order_id  "
					+ " left join delivery_order_item doi on doi.delivery_id = dor.id"
					+ " left join depart_transfer dtr on dtr.order_id = doi.transfer_order_id"
					+ " left join depart_order dpr on dpr.id = dtr.depart_id"
					+ " left join transfer_order tor on tor.id = dtr.order_id "
					+ " left join transfer_order_item toi on toi.order_id = tor.id "
					+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id"
					+ " left join product prod on toi.product_id = prod.id "
					+ " left join user_login ul on ul.id = dpr.create_by "
					+ " left join party p on p.id = dpr.sp_id "
					+ " left join contact c on c.id = p.contact_id"
					+ " left join office oe on oe.id = tor.office_id"
					+ " where dor.id = ror.delivery_order_id and dpr.id in("
					+ departOrderIds
					+ ") and (ifnull(dpr.id, 0) > 0)"
					+ " group by dpr.id ";
		}
		if (!"".equals(deliveryOrderIds)) {
			deliveryOrderIds = deliveryOrderIds.substring(0,
					deliveryOrderIds.length() - 1);
			deliveryOrderSql = "select distinct dor.id,dor.order_no,dor.status,ror.transaction_status,c.abbr spname,toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,'配送' business_type, "
					+ " (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付') pay_amount, "
					+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '<br/>')"
					+ " transfer_order_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name"
					+ " from return_order ror "
					+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
					+ " left join party p on p.id = dor.sp_id "
					+ " left join contact c on c.id = p.contact_id "
					+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
					+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
					+ " left join transfer_order_item toi on toi.id = toid.item_id "
					+ " left join product prod on toi.product_id = prod.id "
					+ " left join user_login ul on ul.id = dor.create_by "
					+ " left join warehouse w on w.id = dor.from_warehouse_id "
					+ " left join office oe on oe.id = w.office_id"
					+ " where dor.id = ror.delivery_order_id and dor.id in("
					+ deliveryOrderIds + ") group by dor.id ";
		}
		if (!"".equals(insuranceOrderIds)) {
			insuranceOrderIds = insuranceOrderIds.substring(0,
					insuranceOrderIds.length() - 1);
			insuranceOrderSql = "";
		}
		String sqlTotal = "select count(1) total from (" + deliveryOrderSql
				+ " " + departOrderSql + " " + pickupOrderSql + ") a";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = deliveryOrderSql + " " + departOrderSql + " "
				+ pickupOrderSql + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void unSelectedList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_" + sortColIndex);
		String booking_id = getPara("booking_id");
		;
		String orderNo = getPara("orderNo");
		String serial_no = getPara("serial_no");
		String sp_id2 = getPara("sp_id2");
		String no = getPara("no");
		String sp_no = getPara("sp_no");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String type = getPara("type");
		String status = getPara("status");
		String ispage = getPara("ispage");
		
		String user_name = currentUser.getPrincipal().toString();
		
		String sqlTotal = "";
		String sql = " select * from (select distinct dor.id,dofi.id did,IFNULL(c2.address,IFNULL(toid.notify_party_company,'')) receivingunit, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name, tor.planning_time ,dor.order_no order_no,dor.status,"
				+ " c.id sp_id, c.abbr spname,c1.abbr customer_name,"
				+ " (SELECT sum(doi1.amount) FROM delivery_order_item doi1 WHERE doi1.delivery_id = dor.id ) amount, "
				+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,"
				+ " '配送' business_type, (select sum(amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额') pay_amount,"
				+ " (SELECT ROUND(sum(amount),2) FROM delivery_order_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id"
				+ " WHERE dofi.order_id = dor.id AND fi.type = '应付') change_amount,"
				+ " ((select sum(dofi.amount) from delivery_order_fin_item dofi where dofi.order_id = dor.id)-(select sum(tofi.amount) from	transfer_order tor"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id = tofi.fin_item_id"
				+ " where fi.type = '应付' and tor.id = doi.transfer_order_id)) alance,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '<br/>') transfer_order_no,dor.sign_status return_order_collection,dor.remark,dpr.booking_note_number,oe.office_name office_name, "
				+ " (select group_concat(toid.serial_no separator '<br/>') from delivery_order_item doi "
				+ " 	left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id"
				+ "         where  doi.delivery_id = dor.id)  serial_no,"
				+ " GROUP_CONCAT(DISTINCT (SELECT tor.customer_order_no"
				+ "   FROM transfer_order tor WHERE tor.id = doi.transfer_order_id GROUP BY tor.id)"
				+ "   SEPARATOR '<br/>') customer_order_no, "
				+ " dor.ref_no"
				+ " from delivery_order dor"
				+ " left join party p1 on dor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
				+ " left join party p on p.id = dor.sp_id "
				+ " LEFT JOIN delivery_order_fin_item dofi ON dofi.order_id = dor.id"
				+ " left join depart_order dpr on dpr.sp_id=p.id"
				+ " left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN party p2 ON p2.id = dor.notify_party_id"
				+ " LEFT JOIN contact c2 ON c2.id = p2.contact_id"
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
				+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
				+ " left join transfer_order_item toi on toi.id = toid.item_id "
				+ " left join transfer_order tor on tor.id = toi.order_id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dor.create_by "
				+ " left join warehouse w on w.id = dor.from_warehouse_id "
				+ " left join location lo on lo.code = dor.route_from "
				+ " left join location lo2 on lo2.code = dor.route_to "
				+ " left join office oe on oe.id = w.office_id where dor.audit_status='已确认' "
				+ " and dor.customer_id in(select customer_id from user_customer where user_name='"+user_name+"')"
		        + " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"')"
		        + " or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dor.id "
				+ " union"
				+ " select distinct dpr.id,dofi.id did,(CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address "
				+ " WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END)  receivingunit, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name ,tor.planning_time ,dpr.depart_no order_no,dpr.status,"
				+ "c.id sp_id, c.abbr spname,c1.abbr customer_name,"
				+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM transfer_order_item_detail toid  "
				+ " WHERE toid.depart_id = dpr.id ) WHEN tor.cargo_nature = 'cargo' THEN ( SELECT sum(toi.amount) FROM "
				+ " depart_order dpr2 LEFT JOIN depart_transfer dt ON dt.depart_id = dpr2.id "
				+ " LEFT JOIN transfer_order_item toi ON toi.order_id = dt.order_id  "
				+ " WHERE dpr2.id = dpr.id ) END ) amount, "
				+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,"
				+ " '零担' business_type, "
				+ " (select sum(amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额') pay_amount,"
				+ " (SELECT ROUND(sum(amount),2) FROM depart_order_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.depart_order_id = dpr.id AND fi.type = '应付') change_amount,"
				+ " ((select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id  and fi.type = '应付')-(select sum(tofi.amount)"
				+ " from transfer_order tor"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id = tofi.fin_item_id"
				+ " where fi.type = '应付' and tor.id = dtr.order_id)) alance,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,dpr.booking_note_number,oe.office_name office_name, "
				+ " '' serial_no,group_concat(DISTINCT (SELECT tor.customer_order_no FROM transfer_order tor WHERE tor.id = dtr.order_id) SEPARATOR '<br/>') customer_order_no, '' ref_no"
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.depart_id = dpr.id"
				+ " LEFT JOIN depart_order_fin_item dofi ON dofi.depart_order_id = dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join party p1 on tor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN warehouse w on w.id=tor.warehouse_id"
				+ " left join location lo on lo.code = dpr.route_from "
				+ " left join location lo2 on lo2.code = dpr.route_to "
				+ " left join office oe on oe.id = tor.office_id where  (ifnull(dtr.depart_id, 0) > 0) and dpr.audit_status='已确认' AND dpr.combine_type = 'DEPART' "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
				+ " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"')"
				+ " or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dpr.id"
				+ " union "
				+ " select distinct dpr.id,dofi.id did,(CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address "
				+ " WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END)  receivingunit, dpr.route_from ,lo.name from_name ,dpr.route_to ,lo2.name to_name ,tor.planning_time ,dpr.depart_no order_no,dpr.status,"
				+ " c.id sp_id, c.abbr spname,c1.abbr customer_name,"
				+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( "
				+ " SELECT count(toid.id) FROM transfer_order_item_detail toid "
				+ " WHERE toid.pickup_id = dpr.id ) "
				+ " WHEN tor.cargo_nature = 'cargo' THEN "
				+ " ( SELECT sum(toi.amount) FROM depart_order dpr2 "
				+ " LEFT JOIN depart_transfer dt ON dt.pickup_id = dpr2.id "
				+ " LEFT JOIN transfer_order_item toi ON toi.order_id = dt.order_id "
				+ " WHERE dpr2.id = dpr.id ) END ) amount,"
				+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,"
				+ " '提货' business_type, "
				+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额') pay_amount,"
				+ " (select ROUND(sum(amount),2) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') change_amount,"
				+ " ((select sum(pofi.amount) from pickup_order_fin_item pofi  where pofi.pickup_order_id = dpr.id )-(select sum(tofi.amount)"
				+ " from transfer_order tor"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id = tofi.fin_item_id"
				+ " where fi.type = '应付' and tor.id = dtr.order_id)) alance,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,dpr.sign_status return_order_collection,dpr.remark,dpr.booking_note_number,oe.office_name office_name, "
				+ " '' serial_no,group_concat(DISTINCT (SELECT tor.customer_order_no FROM transfer_order tor WHERE tor.id = dtr.order_id) SEPARATOR '<br/>') customer_order_no, '' ref_no"
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
				+ " LEFT JOIN pickup_order_fin_item dofi ON dofi.pickup_order_id = dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join party p1 on tor.customer_id = p1.id "
				+ " left join contact c1 on p1.contact_id = c1.id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN warehouse w on w.id=tor.warehouse_id"
				+ " left join location lo on lo.code = dpr.route_from "
				+ " left join location lo2 on lo2.code = dpr.route_to "
				+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.pickup_id, 0) > 0) and dpr.audit_status='已确认' AND dpr.combine_type = 'PICKUP' "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
                + " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"')"
                + "      or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dpr.id"
				+ " union "
				+ " select distinct ior.id,ifi.id did,NULL as receivingunit, tor.route_from ,lo.name from_name ,tor.route_to ,lo2.name to_name ,tor.planning_time ,ior.order_no order_no,ior.status,"
				+ " con.id sp_id, con.abbr spname,c_c.abbr customer_name,sum(toi.amount) amount,round(sum(ifnull(prod.volume,toi.volume)),2) volume,round(sum(ifnull(prod.weight,toi.weight)),2) weight,ior.create_stamp create_stamp,ul.user_name creator,"
				+ " '保险' business_type, "
				+ " round((select sum(insurance_amount) from insurance_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.insurance_order_id = ior.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') !='对账调整金额'),2) pay_amount, "
				+ " round((SELECT ROUND(sum(insurance_amount),2) FROM insurance_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.insurance_order_id = ior.id AND fi.type = '应付' ),2) change_amount,"
				+ " ((select sum(ifi.insurance_amount) from insurance_fin_item ifi  where ifi.insurance_order_id = ior.id )-(select sum(tofi.amount)"
				+ " from transfer_order tor"
				+ " left join transfer_order_fin_item tofi on tofi.order_id = tor.id"
				+ " left join fin_item fi on fi.id = tofi.fin_item_id"
				+ " where fi.type = '应付' and fi.name='保险费' and ior.id = tor.insurance_id)) alance,"
				+ " group_concat(distinct tor.order_no separator '<br/>') transfer_order_no,ior.sign_status return_order_collection,ior.remark,'' booking_note_number,oe.office_name office_name, "
				+ " '' serial_no, group_concat(DISTINCT tor.customer_order_no SEPARATOR '<br/>') customer_order_no,'' ref_no"
				+ " from insurance_order ior "
				+ " left join transfer_order tor on ior.id = tor.insurance_id "
				+ " left join party c_p on tor.customer_id = c_p.id"
				+ " left join contact c_c on c_p.contact_id = c_c.id"
				+ " LEFT JOIN insurance_fin_item ifi ON ifi.insurance_order_id = ior.id"
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join party p ON p.id=tor.sp_id"
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = ior.create_by"
				+ " left join office oe on oe.id = tor.office_id "
				+ " left join location lo on lo.code = tor.route_from "
				+ " left join location lo2 on lo2.code = tor.route_to"
				+ " LEFT JOIN party p1 ON p1.id = ior.insurance_id "
				+ " LEFT JOIN contact con ON con.id = p1.contact_id "
				+ " where ior.audit_status='已确认' "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
                + " and ior.office_id in (select office_id from user_office where user_name='"+user_name+"')"
			    + " group by ior.id"
				+ " union "
				+ " SELECT DISTINCT amco.id,amcoi.id did,NULL as receivingunit,amco.route_from,l. NAME route_name,"
				+ " amco.route_to,l1. NAME to_name,NULL AS planning_time,"
				+ " amco.order_no,amco. STATUS,"
				+ " c.id sp_id, c.abbr spname, c1.abbr customer_name, NULL AS amount,"
				+ " NULL AS volume,NULL AS weight,amco.create_stamp,"
				+ " ul.user_name creator,"
				+ " '成本单' business_type,"
				+ " (SELECT sum(amount) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id = amco.id AND fi.type = '应付' ) pay_amount,"
				+ " (SELECT ROUND(sum(amount),2) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id = amco.id AND fi.type = '应付') change_amount,"
				+ " null as alance,NULL AS transfer_order_no,"
				+ " NULL AS return_order_collection,amco.remark remark,"
				+ " NULL AS booking_note_number,NULL AS office_name,"
				+ " '' serial_no,'' customer_order_no, '' ref_no "
				+ " FROM arap_misc_cost_order amco"
				+ " LEFT JOIN arap_misc_cost_order_item amcoi ON amcoi.misc_order_id = amco.id"
				+ " LEFT JOIN user_login ul ON ul.id = amco.create_by"
				+ " LEFT JOIN party p1 ON amco.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " LEFT JOIN party p ON amco.sp_id = p.id"
				+ " LEFT JOIN contact c ON p.contact_id = c.id"
				+ " LEFT JOIN location l ON amco.route_from = l. CODE"
				+ " LEFT JOIN location l1 ON amco.route_to = l1. CODE"
				+ " WHERE amco.audit_status = '已确认'"
				+ " and amco.office_id in (select office_id from user_office where user_name='"+user_name+"')"
				+ " GROUP BY amco.id) as A ";
		String condition = "";
		if (orderNo != null || sp_id2 != null || serial_no != null
				|| no != null || beginTime != null || endTime != null
				|| type != null || status != null) {
			if (ispage != null) {
				condition = " where ifnull(serial_no,'') like '%" + serial_no
						+ "%' "
						+ " and ifnull(booking_note_number,'')  like '%"
						+ booking_id + "%'" + " and ifnull(spname,'')  like '%"
						+ sp_no + "%'";

			} else {
				condition = " where 1=1 ";
				if (!"".equals(sp_id2) && sp_id2 != null) {
					condition += " and sp_id = '" + sp_id2 + "' ";
				}
				if (!"".equals(orderNo) && orderNo != null) {
					condition += " and ifnull(transfer_order_no,'') like '%"
							+ orderNo + "%' ";
				}
				if (!"".equals(no) && no != null) {
					condition += " and order_no like '%" + no + "%' ";
				}
				if (!"".equals(type) && type != null) {
					condition += " and business_type like '%" + type + "%' ";
				}
				if (!"".equals(status) && status != null) {
					condition += " and status like '%" + status + "%' ";
				}
				if (!"".equals(serial_no) && serial_no != null) {
					condition += " and ifnull(serial_no,'') like '%"
							+ serial_no + "%' ";
				}
				if (!"".equals(booking_id) && booking_id != null) {
					condition += " and ifnull(booking_note_number,'')  like '%"
							+ booking_id + "%'";
				}
				if ((beginTime != null && !"".equals(beginTime))
						|| (endTime != null && !"".equals(endTime))) {
					if (beginTime == null || "".equals(beginTime)) {
						beginTime = "1970-01-01";
					}

					if (endTime == null || "".equals(endTime)) {
						endTime = "2037-12-31";
					}
					condition += " and ifnull(planning_time,'1970-01-01') between '"
							+ beginTime + "' and '" + endTime + " 23:59:59' ";
				}
			}
		}
		sqlTotal = "select count(1) total from (" + sql + condition + ") as B";

		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String orderByStr = " order by A.planning_time asc ";
		if (colName.length() > 0) {
			orderByStr = " order by A." + colName + " " + sortBy;
		}

		List<Record> BillingOrders = Db.find(sql + condition + orderByStr
				+ sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void costConfirmListById() {
		String orderIds = getPara("orderIds");
		String orderNos = getPara("orderNos");
		String pickupId = "";
		String departId = "";
		String deliveryId = "";
		String insuranceId = "";
		String arapmiscId = "";
		if (orderIds == null || orderIds == "") {
			pickupId = "-1";
			departId = "-1";
			deliveryId = "-1";
			insuranceId = "-1";
			arapmiscId = "-1";
		} else {
			String[] orderIdsArr = orderIds.split(",");
			String[] orderNoArr = orderNos.split(",");
			for (int i = 0; i < orderIdsArr.length; i++) {
				Record rec = null;
				if ("提货".equals(orderNoArr[i])) {
					pickupId += orderIdsArr[i] + ",";
				} else if ("零担".equals(orderNoArr[i])) {
					departId += orderIdsArr[i] + ",";
				} else if ("配送".equals(orderNoArr[i])) {
					deliveryId += orderIdsArr[i] + ",";
				} else if ("成本单".equals(orderNoArr[i])) {
					arapmiscId += orderIdsArr[i] + ",";
				} else {
					insuranceId += orderIdsArr[i] + ",";
				}
			}
			if (pickupId != null && !"".equals(pickupId)) {
				pickupId = pickupId.substring(0, pickupId.length() - 1);
			} else {
				pickupId = "-1";
			}
			if (departId != null && !"".equals(departId)) {
				departId = departId.substring(0, departId.length() - 1);
			} else {
				departId = "-1";
			}
			if (deliveryId != null && !"".equals(deliveryId)) {
				deliveryId = deliveryId.substring(0, deliveryId.length() - 1);
			} else {
				deliveryId = "-1";
			}
			if (arapmiscId != null && !"".equals(arapmiscId)) {
				arapmiscId = arapmiscId.substring(0, arapmiscId.length() - 1);
			} else {
				arapmiscId = "-1";
			}
			if (insuranceId != null && !"".equals(insuranceId)) {
				insuranceId = insuranceId
						.substring(0, insuranceId.length() - 1);
			} else {
				insuranceId = "-1";
			}
		}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String searchSql = "select distinct dor.id,dofi.id did,dor.order_no order_no,dor.status,c.abbr spname,c1.abbr customer_name,IFNULL(c2.address,IFNULL(toid.notify_party_company,'')) receivingunit,(select count(*) from delivery_order_item doit where doit.delivery_id = dor.id) amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dor.create_stamp create_stamp,ul.user_name creator,"
				+ "	'配送' business_type,'' booking_note_number,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = dor.route_from),'') route_from,ifnull((SELECT NAME FROM location WHERE CODE = dor.route_to),'') route_to,"
				+ " (select ifnull(sum(amount),0) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,(SELECT ROUND(ifnull(sum(amount),0),2) FROM delivery_order_fin_item dofi WHERE dofi.order_id = dor.id) change_amount,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '<br/>') transfer_order_no,"
				+ " group_concat(distinct (select tor.customer_order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '<br/>') customer_order_no,dor.ref_no,"
				+ " (SELECT group_concat(toid.serial_no SEPARATOR '\r\n') FROM	transfer_order_item_detail toid	WHERE	toid.delivery_id = dor.id) serial_no,dor.sign_status return_order_collection,dor.remark,oe.office_name office_name "
				+ " from delivery_order dor"
				+ " left join party p on p.id = dor.sp_id "
				+ " LEFT JOIN delivery_order_fin_item dofi on dofi.order_id=dor.id"
				+ " left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN party p1 ON dor.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " LEFT JOIN party p2 ON dor.notify_party_id = p2.id"
				+ " LEFT JOIN contact c2 ON p2.contact_id = c2.id"
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
				+ " left join transfer_order_item_detail toid on toid.id = doi.transfer_item_detail_id "
				+ " left join transfer_order_item toi on toi.id = toid.item_id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dor.create_by "
				+ " left join warehouse w on w.id = dor.from_warehouse_id "
				+ " left join office oe on oe.id = w.office_id where dor.id in("
				+ deliveryId
				+ ") group by dor.id "
				+ " union"
				+ " select distinct dpr.id,dofi.id did,dpr.depart_no order_no,dpr.status,c.abbr spname,c1.abbr customer_name,(CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit,"
				+ " (SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN (SELECT count(toid.id) FROM transfer_order_item_detail toid WHERE toid.depart_id = dpr.id )"
				+ " WHEN tor.cargo_nature = 'cargo' THEN ( SELECT sum(toi.amount) FROM depart_order dpr2 LEFT JOIN depart_transfer dt ON dt.depart_id = dpr2.id LEFT JOIN transfer_order_item toi ON toi.order_id = dt.order_id"
				+ " WHERE dpr2.id = dpr.id )END) amount,"
				+ " ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,"
				+ " '零担' business_type, dpr.booking_note_number,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),'') route_from,ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),'') route_to,"
				+ " (select ifnull(sum(amount),0) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,(SELECT ROUND(ifnull(sum(amount),0),2) FROM depart_order_fin_item dofi WHERE dofi.depart_order_id = dpr.id) change_amount,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,"
				+ " group_concat(distinct (select tor.customer_order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') customer_order_no,'' ref_no,"
				+ " (SELECT group_concat(toid.serial_no SEPARATOR '\r\n') FROM transfer_order_item_detail toid	WHERE toid.depart_id = dpr.id) serial_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name "
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.depart_id = dpr.id"
				+ " LEFT JOIN depart_order_fin_item dofi on dofi.depart_order_id=dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " LEFT JOIN party p1 ON tor.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
				+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.depart_id, 0) > 0) and dpr.id in("
				+ departId
				+ ") group by dpr.id"
				+ " union "
				+ " select distinct dpr.id,dofi.id did,dpr.depart_no order_no,dpr.status,c.abbr spname,c1.abbr customer_name,(CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit,"
				+ " toi.amount,ifnull(prod.volume,toi.volume) volume,ifnull(prod.weight,toi.weight) weight,dpr.create_stamp create_stamp,ul.user_name creator,"
				+ " '提货' business_type, '' booking_note_number,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),'') route_from,ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),'') route_to,"
				+ " (select ifnull(sum(amount),0) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付' and dofi.fin_item_id!=7 and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,(SELECT ROUND(ifnull(sum(amount),0),2) FROM pickup_order_fin_item dofi WHERE dofi.pickup_order_id = dpr.id and dofi.fin_item_id!=7) change_amount,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') transfer_order_no,"
				+ " group_concat(distinct (select tor.customer_order_no from transfer_order tor where tor.id = dtr.order_id) separator '<br/>') customer_order_no,'' ref_no,"
				+ " (SELECT	group_concat(toid.serial_no SEPARATOR '\r\n') FROM	transfer_order_item_detail toid	WHERE toid.pickup_id = dpr.id) serial_no,dpr.sign_status return_order_collection,dpr.remark,oe.office_name office_name "
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
				+ " LEFT JOIN pickup_order_fin_item dofi on dofi.pickup_order_id=dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " LEFT JOIN party p1 ON tor.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
				+ " left join user_login ul on ul.id = dpr.create_by left join party p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " left join office oe on oe.id = tor.office_id where (ifnull(dtr.pickup_id, 0) > 0) and dpr.id in("
				+ pickupId
				+ ") group by dpr.id"
				+ " union "
				+ " select distinct ior.id,ifi.id did,ior.order_no order_no,ior.status,'保险公司' spname,c1.abbr customer_name,'' receivingunit,sum(toi.amount) amount,sum(ifnull(prod.volume,toi.volume)) volume,sum(ifnull(prod.weight,toi.weight)) weight,ior.create_stamp create_stamp,ul.user_name creator,"
				+ " '保险' business_type, '' booking_note_number, '' route_from ,'' route_to,"
				+ " (select ifnull(sum(insurance_amount),0) from insurance_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.insurance_order_id = ior.id and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,(SELECT ROUND(ifnull(sum(insurance_amount),0),2) FROM insurance_fin_item dofi WHERE dofi.insurance_order_id = ior.id) change_amount, "
				+ " group_concat(distinct tor.order_no separator '<br/>') transfer_order_no,"
				+ " group_concat(distinct tor.customer_order_no separator '<br/>') customer_order_no, '' ref_no ,"
				+ " group_concat(DISTINCT toid.serial_no SEPARATOR '\r\n')serial_no,ior.sign_status return_order_collection,ior.remark,oe.office_name office_name "
				+ " from insurance_order ior "
				+ " left join transfer_order tor on ior.id = tor.insurance_id "
				+ " LEFT JOIN insurance_fin_item ifi ON ifi.insurance_order_id=ior.id"
				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id and toid.item_id = toi.id "
				+ " left join product prod on toi.product_id = prod.id "
				+ " LEFT JOIN party p1 ON tor.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " left join user_login ul on ul.id = ior.create_by "
				+ " left join office oe on oe.id = tor.office_id where ior.id in("
				+ insuranceId
				+ ") group by ior.id "
				+ " union "
				+ " SELECT DISTINCT amco.id,amcoi.id did,amco.order_no,amco. STATUS,c.abbr spname,c1.abbr customer_name,'' receivingunit,amco.total_amount,NULL AS volume,NULL AS weight,amco.create_stamp,ul.user_name creator,"
				+ " '成本单' business_type, '' booking_note_number, '' route_from ,'' route_to,"
				+ " (SELECT ifnull(sum(amount),0) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id= amco.id ) pay_amount,(SELECT ROUND(ifnull(sum(amount),0),2) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id= amco.id ) AS change_amount,"
				+ " NULL AS transfer_order_no ,null customer_order_no, '' ref_no,"
				+ " NULL AS serial_no,NULL AS return_order_collection,amco.remark remark,NULL AS office_name "
				+ " FROM arap_misc_cost_order amco "
				+ " LEFT JOIN arap_misc_cost_order_item amcoi ON amcoi.misc_order_id = amco.id "
				+ " LEFT JOIN user_login ul ON ul.id = amco.create_by "
				+ " LEFT JOIN party p ON amco.sp_id = p.id "
				+ " LEFT JOIN contact c ON p.contact_id = c.id"
				+ " LEFT JOIN party p1 ON amco.customer_id = p1.id "
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " WHERE amco.id in (" + arapmiscId + ") GROUP BY amco.id";

		String sqlTotal = "select count(1) total from (" + searchSql + ") a";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = searchSql + sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	public void checkCostMiscList() {
		String costCheckOrderId = getPara("costCheckOrderId");
		if (costCheckOrderId == null || "".equals(costCheckOrderId)) {
			costCheckOrderId = "-1";
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
		String sql = "select count(amco.id) total from arap_misc_cost_order amco"
				+ " left join arap_cost_order aco on amco.cost_order_id = aco.id"
				+ " left join arap_misc_cost_order_item amcoi on amcoi.misc_order_id = amco.id where aco.id = "
				+ costCheckOrderId;
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db
				.find("select amcoi.*,amco.order_no misc_order_no,c.abbr cname,fi.name name from arap_misc_cost_order amco"
						+ " left join arap_cost_order aco on aco.id = amco.cost_order_id "
						+ " left join arap_misc_cost_order_item amcoi on amcoi.misc_order_id = amco.id "
						+ " left join party p on p.id = aco.payee_id left join contact c on c.id = p.contact_id "
						+ " left join fin_item fi on amcoi.fin_item_id = fi.id where aco.id =  "
						+ costCheckOrderId + " " + sLimit);

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

	public void updateDepartOrderFinItem() {
		String tId = getPara("departId");
		String name = getPara("name");
		String value = getPara("value");
		String type = getPara("ty");
		String user = (String) currentUser.getPrincipal();
		List<UserLogin> users = UserLogin.dao
				.find("select * from user_login where user_name='" + user + "'");
		Record rec1 = null;
		double totalAmount = 0.0;
		if ("提货".equals(type)) {
			rec1 = Db
					.findFirst(
							"select ifnull(sum(amount),0) sum_amount from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = ? and fi.type = '应付'",
							tId);
			totalAmount = rec1.getDouble("sum_amount");
			Double newAmount = 0.0;
			if (Double.parseDouble((String) value) > 0) {
				newAmount = Double.parseDouble((String) value) - totalAmount;
			} else {
				newAmount = Double.parseDouble((String) value) + totalAmount;
			}
			if (newAmount != 0) {
				DecimalFormat df = new DecimalFormat("0.00");
				String num = df.format(newAmount);
				PickupOrderFinItem pofi = new PickupOrderFinItem();
				pofi.set("pickup_order_id", tId);
				pofi.set("fin_item_id", "1");
				pofi.set("amount", num);
				pofi.set("STATUS", "新建");
				pofi.set("create_date", new Date());
				pofi.set("creator", users.get(0).get("id"));
				pofi.set("remark", "对账调整金额");
				pofi.set("cost_source", "对账调整金额");
				pofi.save();
			}
		} else if ("零担".equals(type) || "整车".equals(type)) {
			rec1 = Db
					.findFirst(
							"select ifnull(sum(amount),0) sum_amount from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = ? and fi.type = '应付'",
							tId);
			totalAmount = rec1.getDouble("sum_amount");
			Double newAmount = 0.0;
			if (Double.parseDouble((String) value) > 0) {
				newAmount = Double.parseDouble((String) value) - totalAmount;
			} else {
				newAmount = Double.parseDouble((String) value) + totalAmount;
			}
			if (newAmount != 0) {
				DecimalFormat df = new DecimalFormat("0.00");
				String num = df.format(newAmount);
				DepartOrderFinItem dofi = new DepartOrderFinItem();
				dofi.set("depart_order_id", tId);
				dofi.set("fin_item_id", "1");
				dofi.set("amount", num);
				dofi.set("STATUS", "新建");
				dofi.set("create_date", new Date());
				dofi.set("create_name", "user");
				dofi.set("creator", users.get(0).get("id"));
				dofi.set("remark", "对账调整金额");
				dofi.set("cost_source", "对账调整金额");
				dofi.save();
			}
		} else if ("配送".equals(type)) {
			rec1 = Db
					.findFirst(
							"select ifnull(sum(amount),0) sum_amount from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = ? and fi.type = '应付'",
							tId);
			totalAmount = rec1.getDouble("sum_amount");
			Double newAmount = 0.0;
			if (Double.parseDouble((String) value) > 0) {
				newAmount = Double.parseDouble((String) value) - totalAmount;
			} else {
				newAmount = Double.parseDouble((String) value) + totalAmount;
			}
			if (newAmount != 0) {
				DecimalFormat df = new DecimalFormat("0.00");
				String num = df.format(newAmount);
				DeliveryOrderFinItem dofi = new DeliveryOrderFinItem();
				dofi.set("order_id", tId);
				dofi.set("fin_item_id", "1");
				dofi.set("amount", num);
				dofi.set("STATUS", "新建");
				dofi.set("create_date", new Date());
				dofi.set("creator", users.get(0).get("id"));
				dofi.set("create_name", "user");
				dofi.set("remark", "对账调整金额");
				dofi.set("cost_source", "对账调整金额");
				dofi.save();
			}
		} else if ("保险".equals(type)) {
			rec1 = Db
					.findFirst(
							"select ifnull(sum(insurance_amount),0) sum_amount from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id  where ifi.insurance_order_id = ? and fi.type ='应付'",
							tId);
			totalAmount = rec1.getDouble("sum_amount");
			Double newAmount = 0.0;
			if (Double.parseDouble((String) value) > 0) {
				newAmount = Double.parseDouble((String) value) - totalAmount;
			} else {
				newAmount = Double.parseDouble((String) value) + totalAmount;
			}
			if (newAmount != 0) {
				DecimalFormat df = new DecimalFormat("0.00");
				String num = df.format(newAmount);
				InsuranceFinItem ifi = new InsuranceFinItem();
				ifi.set("insurance_order_id", tId);
				ifi.set("fin_item_id", "18");
				ifi.set("insurance_amount", num);
				ifi.set("STATUS", "新建");
				ifi.set("create_stamp", new Date());
				ifi.set("create_by", users.get(0).get("id"));
				ifi.set("cost_source", "对账调整金额");
				ifi.save();
			}
		}
		renderJson("{\"success\":true}");
	}

	public void externalMiscOrderList() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sqlTotal = "select count(1) total from arap_misc_cost_order where ifnull(cost_order_id, 0) = 0";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select amco.*,aco.order_no cost_order_no from arap_misc_cost_order amco"
				+ " left join arap_cost_order aco on aco.id = amco.cost_order_id"
				+ " where ifnull(cost_order_id, 0) = 0 order by amco.create_stamp desc "
				+ sLimit;

		logger.debug("sql:" + sql);
		List<Record> BillingOrders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	// 保存后，手工单状态转为“已入对账单”。
	@Before(Tx.class)
	public void updateCostMiscOrder() {
		String micsOrderIds = getPara("micsOrderIds");
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostOrder arapCostOrder = ArapCostOrder.dao
				.findById(costCheckOrderId);
		if (micsOrderIds != null && !"".equals(micsOrderIds)) {
			String[] micsOrderIdArr = micsOrderIds.split(",");
			for (int i = 0; i < micsOrderIdArr.length; i++) {
				ArapMiscCostOrder arapMisccostOrder = ArapMiscCostOrder.dao
						.findById(micsOrderIdArr[i]);
				arapMisccostOrder.set("cost_order_id", costCheckOrderId);
				arapMisccostOrder.set("status", "已入对账单");
				arapMisccostOrder.update();
			}

			Record record = Db
					.findFirst(
							"select sum(amount) sum_amount from arap_misc_cost_order mco"
									+ " left join arap_misc_cost_order_item mcoi on mcoi.misc_order_id = mco.id where mco.cost_order_id = ?",
							costCheckOrderId);
			Double total_amount = arapCostOrder.getDouble("total_amount");
			Double debit_amount = record.getDouble("sum_amount");
			arapCostOrder.set("debit_amount", debit_amount);
			arapCostOrder.set("cost_amount", total_amount - debit_amount);
			arapCostOrder.update();
		}
		renderJson(arapCostOrder);
	}

	public void delete() {
		String id = getPara("id");
		String order_type = getPara("order_type");
		if ("提货".equals(order_type)) {
			DepartOrder pickupOrder = DepartOrder.dao.findById(id);
			pickupOrder.set("audit_status", "新建");
			pickupOrder.update();
		} else if ("零担".equals(order_type) || "整车".equals(order_type)) {
			DepartOrder departOrder = DepartOrder.dao.findById(id);
			departOrder.set("audit_status", "新建");
			departOrder.update();
		} else if ("配送".equals(order_type)) {
			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(id);
			deliveryOrder.set("audit_status", "新建");
			deliveryOrder.update();
		} else if ("成本单".equals(order_type)) {
			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao
					.findById(id);
			arapmisccostOrder.set("audit_status", "新建");
			arapmisccostOrder.update();
		} else {
			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(id);
			insuranceOrder.set("audit_status", "新建");
			insuranceOrder.update();
		}

		renderJson("{\"success\":true}");
	}

	public void deleteItem() {
		String id = getPara("id");
		String order_type = getPara("order_type");
		String costCheckOrderId = getPara("costCheckOrderId");
		ArapCostItem item = ArapCostItem.dao
				.findFirst(
						"select *  from arap_cost_item where ref_order_no=? and ref_order_id=? and cost_order_id=?",
						order_type, id, costCheckOrderId);
		if (item != null) {
			item.delete();
		}
		if ("提货".equals(order_type)) {
			DepartOrder pickupOrder = DepartOrder.dao.findById(id);
			pickupOrder.set("audit_status", "已确认");
			pickupOrder.update();
		} else if ("零担".equals(order_type) || "整车".equals(order_type)) {
			DepartOrder departOrder = DepartOrder.dao.findById(id);
			departOrder.set("audit_status", "已确认");
			departOrder.update();
		} else if ("配送".equals(order_type)) {
			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(id);
			deliveryOrder.set("audit_status", "已确认");
			deliveryOrder.update();
		} else if ("成本单".equals(order_type)) {
			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao
					.findById(id);
			arapmisccostOrder.set("audit_status", "已确认");
			arapmisccostOrder.update();
		} else {
			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(id);
			insuranceOrder.set("audit_status", "已确认");
			insuranceOrder.update();
		}

		renderJson("{\"success\":true}");
	}
	
	
	//撤销单据
	@Before(Tx.class)
	public void deleteOrder() {
		String id = getPara("orderId");
		if("".equals(id)||id==null)
			return;
		String sql = "SELECT * FROM `cost_application_order_rel` where order_type = '对账单' and cost_order_id =" + id;
		List<Record> nextOrders = Db.find(sql);
		if (nextOrders.size() == 0) {
			//更新相关单据的状态
			//先删除从表
			//删除主表
			//1.
			List<ArapCostItem> acis = ArapCostItem.dao.find("select * from arap_cost_item where cost_order_id = ?",id);
			for (ArapCostItem aci:acis) {
				long ref_order_id = aci.getLong("ref_order_id");
				String order_type = aci.getStr("ref_order_no");
				
				if ("提货".equals(order_type)) {
					DepartOrder pickupOrder = DepartOrder.dao.findById(ref_order_id);
					pickupOrder.set("audit_status", "已确认");
					pickupOrder.update();
				} else if ("零担".equals(order_type) || "整车".equals(order_type)) {
					DepartOrder departOrder = DepartOrder.dao.findById(ref_order_id);
					departOrder.set("audit_status", "已确认");
					departOrder.update();
				} else if ("配送".equals(order_type)) {
					DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(ref_order_id);
					deliveryOrder.set("audit_status", "已确认");
					deliveryOrder.update();
				} else if ("成本单".equals(order_type)) {
					ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao
							.findById(ref_order_id);
					arapmisccostOrder.set("audit_status", "已确认");
					arapmisccostOrder.update();
				} else {
					InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(ref_order_id);
					insuranceOrder.set("audit_status", "已确认");
					insuranceOrder.update();
				}
				
				//删除中间表数据集
				aci.delete();
			}
			
			//4.删除主表
			ArapCostOrder aco = ArapCostOrder.dao.findById(id);
			aco.delete();
			
			renderJson("{\"success\":true}");
		} else {
			renderJson("{\"success\":false}");
		}
	}
	
	
	public void updateOrderRemark(){
		String order_type = getPara("order_type");
		String remark = getPara("remark");
		String order_id = getPara("order_id");
		
		if("order_id".equals(order_id)||order_id==null){
			renderJson("{\"success\":false}");
		}else{
			if("配送".equals(order_type)){
				DeliveryOrder dor  = DeliveryOrder.dao.findById(order_id);
				dor.set("remark", remark).update();
			}else if("提货".equals(order_type) || "零担".equals(order_type) ){
				DepartOrder deo  = DepartOrder.dao.findById(order_id);
				deo.set("remark", remark).update();
			}else if("成本单".equals(order_type)){
				ArapMiscCostOrder aco  = ArapMiscCostOrder.dao.findById(order_id);
				aco.set("remark", remark).update();
			}else {
				InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(order_id);
				insuranceOrder.set("remark", remark).update();
			}
			renderJson("{\"success\":true}");
		}
		
	}
}
