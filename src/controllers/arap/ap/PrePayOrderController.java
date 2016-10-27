package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostOrder;
import models.Party;
import models.UserLogin;
import models.eeda.profile.Account;
import models.yh.arap.prePayOrder.ArapPrePayOrder;
import models.yh.arap.prePayOrder.ArapPrePayOrderItem;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PrePayOrderController extends Controller {
	private Log logger = Log.getLog(PrePayOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_PrePayOrder_LIST })
	public void index() {
		render("/eeda/arap/PrePayOrder/PrePayOrderList.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_PrePayOrder_LIST })
	public void list() {
		String spName = getPara("spName");
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");
		String orderNo = getPara("orderNo");
		// String status = getPara("status");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}

		String sql = "select appo.*, c.abbr as sp_name, ifnull(ul.c_name, ul.user_name) creator_name from arap_pre_pay_order appo"
				+ " left join user_login ul on appo.creator = ul.id"
				+ " left join party p on appo.sp_id = p.id"
				+ " left join contact c on p.contact_id = c.id";
		logger.debug("sql:" + sql);
		String condition = "";
		if (spName != null || beginTime != null || endTime != null
				|| orderNo != null) {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-1-1";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}

			condition = " where " + " ifnull(c.abbr,'') like '%" + spName
					+ "%' " + " and appo.order_no like '%" + orderNo + "%' "
					+ " and appo.create_date between '" + beginTime + "' and '"
					+ endTime + " 23:59:59' ";
		}

		String sqlTotal = "select count(1) total from (" + sql + condition
				+ ") A";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		List<Record> BillingOrders = Db.find(sql + condition
				+ " order by appo.create_date desc " + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_PrePayOrder_CREATE })
	public void create() {
		String ids = getPara("ids");
		if (ids != null && !"".equals(ids)) {
			String[] idArray = ids.split(",");
			logger.debug(String.valueOf(idArray.length));

			setAttr("costCheckOrderIds", ids);
			ArapCostOrder arapCostOrder = ArapCostOrder.dao
					.findById(idArray[0]);
			Long spId = arapCostOrder.get("payee_id");
			if (!"".equals(spId) && spId != null) {
				Party party = Party.dao.findById(spId);
				setAttr("party", party);
				Contact contact = Contact.dao.findById(party.get("contact_id")
						.toString());
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

		List<Record> itemList = Collections.emptyList();
		setAttr("itemList", itemList);

		render("/eeda/arap/PrePayOrder/PrePayOrderEdit.html");
	}

	private ArapPrePayOrder getDestOrder(String originOrderId) throws Exception {
		ArapPrePayOrder destOrder = ArapPrePayOrder.dao.findFirst(
				"select * from arap_pre_pay_order where ref_order_id=?",
				originOrderId);
		if (destOrder == null)
			return destOrder;
		if (!"新建".equals(destOrder.getStr("status"))) {
			throw new Exception("对应的预付单状态已不是“新建”，不能修改本张单据。");
		}
		return destOrder;
	}

	private ArapPrePayOrder buildDestPrePayOrder(ArapPrePayOrder originOrder, ArapPrePayOrder destOrder,
			UserLogin user) throws IllegalAccessException,
			InvocationTargetException {
		long originId = originOrder.getLong("id");

		if(destOrder == null){
			destOrder = new ArapPrePayOrder();
			destOrder.set("status", "新建");
	
			if (originOrder.getStr("sp_id") != null
					&& !"".equals(originOrder.getStr("sp_id"))) {
				destOrder.set("sp_id", originOrder.getStr("sp_id"));
			}
	
			destOrder.set("ref_no", originOrder.getStr("ref_no"));
			destOrder.set("remark", originOrder.getStr("remark"));
			destOrder.set("creator", user.getLong("id"));
			destOrder.set("create_date", new Date());
			destOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YF",  user.getLong("office_id")));
			if (originOrder.getDouble("total_amount") != null
					&& !"".equals(originOrder.getDouble("total_amount"))) {
				destOrder.set("total_amount",
						0 - originOrder.getDouble("total_amount"));
			}
			destOrder.set("ref_order_no", originOrder.getStr("order_no"));
			destOrder.set("ref_order_id", originOrder.getLong("id"));
			destOrder.set("office_id", user.getLong("office_id"));
			destOrder.set("type", "dest");
			destOrder.save();
	
			List<ArapPrePayOrderItem> originItems = ArapPrePayOrderItem.dao.find(
					"select * from arap_pre_pay_order_item where order_id = ?",
					originId);
	
			for (ArapPrePayOrderItem originItem : originItems) {
				ArapPrePayOrderItem newItem = new ArapPrePayOrderItem();
				newItem.set("status", "新建");
				newItem.set("fin_item_id", originItem.get("fin_item_id"));
				if (originItem.getDouble("AMOUNT") != null)
					newItem.set("amount", 0 - originItem.getDouble("AMOUNT"));
	
				newItem.set("item_desc", originItem.get("ITEM_DESC"));
				newItem.set("order_id", destOrder.getLong("id"));
				newItem.save();
			}
		}else{
			if (originOrder.getStr("sp_id") != null
					&& !"".equals(originOrder.getStr("sp_id"))) {
				destOrder.set("sp_id", originOrder.getStr("sp_id"));
			}
	
			destOrder.set("ref_no", originOrder.getStr("ref_no"));
			destOrder.set("remark", originOrder.getStr("remark"));
			
			if (originOrder.getDouble("total_amount") != null
					&& !"".equals(originOrder.getDouble("total_amount"))) {
				destOrder.set("total_amount",
						0 - originOrder.getDouble("total_amount"));
			}
			destOrder.update();
			
			Db.update("delete from arap_pre_pay_order_item where order_id = ?",
					destOrder.getLong("id"));
	
			List<ArapPrePayOrderItem> originItems = ArapPrePayOrderItem.dao.find(
					"select * from arap_pre_pay_order_item where order_id = ?",
					originId);
	
			for (ArapPrePayOrderItem originItem : originItems) {
				ArapPrePayOrderItem newItem = new ArapPrePayOrderItem();
				newItem.set("status", "新建");
				newItem.set("fin_item_id", originItem.get("fin_item_id"));
				if (originItem.getDouble("AMOUNT") != null)
					newItem.set("amount", 0 - originItem.getDouble("AMOUNT"));
	
				newItem.set("item_desc", originItem.get("ITEM_DESC"));
				newItem.set("order_id", destOrder.getLong("id"));
				newItem.save();
			}
		}
		return destOrder;
	}

	@RequiresPermissions(value = {
			PermissionConstant.PERMSSION_PrePayOrder_CREATE,
			PermissionConstant.PERMSSION_PrePayOrder_UPDATE }, logical = Logical.OR)
	@Before(Tx.class)
	public void save() throws Exception {
		String jsonStr = getPara("params");
		logger.debug(jsonStr);
		Gson gson = new Gson();
		Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);

		ArapPrePayOrder arapPrePayOrder = new ArapPrePayOrder();
		String orderId = (String) dto.get("orderId");
		String spId = (String) dto.get("sp_id");
		String ref_no = (String) dto.get("ref_no");
		String remark = (String) dto.get("remark");
		Double amount = (Double) dto.get("amount");
		UserLogin user = LoginUserController.getLoginUser(this);

		if (!"".equals(orderId) && orderId != null) {
			arapPrePayOrder = ArapPrePayOrder.dao.findById(orderId);

			if (spId != null && !"".equals(spId)) {
				arapPrePayOrder.set("sp_id", spId);
			}
			arapPrePayOrder.set("ref_no", ref_no);
			arapPrePayOrder.set("remark", remark);
			if (amount != null && !"".equals(amount)) {
				arapPrePayOrder.set("total_amount", amount);
			}

			arapPrePayOrder.update();
		} else {
			arapPrePayOrder = new ArapPrePayOrder();
			arapPrePayOrder.set("status", "新建");
			if (spId != null && !"".equals(spId)) {
				arapPrePayOrder.set("sp_id", spId);
			}

			arapPrePayOrder.set("ref_no", ref_no);
			arapPrePayOrder.set("remark", remark);
			arapPrePayOrder.set("office_id", user.getLong("office_id"));
			arapPrePayOrder.set("creator", user.getLong("id"));
			arapPrePayOrder.set("create_date", new Date());
			arapPrePayOrder.set("type", "origin");
			arapPrePayOrder.set("order_no",
					OrderNoGenerator.getNextOrderNo("YF", user.getLong("office_id")));

			if (amount != null && !"".equals(amount)) {
				arapPrePayOrder.set("total_amount", amount);
			}
			arapPrePayOrder.save();
		}

		List<Map> items = (List<Map>) dto.get("items");
		for (Map item : items) {
			String itemId = (String) item.get("ID");
			String action = (String) item.get("ACTION");
			if ("".equals(itemId)) {
				ArapPrePayOrderItem arapPrePayOrderItem = new ArapPrePayOrderItem();
				arapPrePayOrderItem.set("order_id",
						arapPrePayOrder.getLong("id"));
				arapPrePayOrderItem.set("status", "新建");
				arapPrePayOrderItem.set("fin_item_id", item.get("FIN_ITEM_ID"));
				arapPrePayOrderItem.set("amount", item.get("AMOUNT"));
				arapPrePayOrderItem.set("item_desc", item.get("ITEM_DESC"));
				arapPrePayOrderItem.save();
			} else {
				ArapPrePayOrderItem arapPrePayOrderItem = ArapPrePayOrderItem.dao
						.findById(itemId);
				if ("DELETE".equals(action)) {
					if(arapPrePayOrderItem != null)
					arapPrePayOrderItem.delete();
				} else {
					arapPrePayOrderItem.set("fin_item_id",
							item.get("FIN_ITEM_ID"));
					arapPrePayOrderItem.set("amount", item.get("AMOUNT"));
					arapPrePayOrderItem.set("item_desc", item.get("ITEM_DESC"));
					arapPrePayOrderItem.update();
				}
			}
		}

		ArapPrePayOrder destOrder = getDestOrder(orderId);
		destOrder = buildDestPrePayOrder(arapPrePayOrder, destOrder, user);

		if (destOrder != null) {
			arapPrePayOrder.set("ref_order_no", destOrder.getStr("order_no"));
			arapPrePayOrder.set("ref_order_id", destOrder.getLong("id"));
			arapPrePayOrder.update();
		}
		renderJson(arapPrePayOrder);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_PrePayOrder_UPDATE })
	public void edit() {
		String id = getPara("id");
		List<Record> receivableItemList = Collections.EMPTY_LIST;
		receivableItemList = Db.find("select * from fin_item where type='应付'");
		setAttr("receivableItemList", receivableItemList);

		ArapPrePayOrder arapPrePayOrder = ArapPrePayOrder.dao.findById(id);
		Long spId = arapPrePayOrder.get("sp_id");
		if (!"".equals(spId) && spId != null) {
			Record rec = Db.findFirst(
					"select c.company_name from party p, contact c where"
							+ " p.contact_id = c.id and p.id=? ", spId);

			setAttr("sp_name", rec.getStr("company_name"));
		}

		UserLogin userLogin = UserLogin.dao.findById(arapPrePayOrder
				.get("creator"));
		setAttr("creator_name", userLogin.getStr("c_name"));

		setAttr("arapPrePayOrder", arapPrePayOrder);

		// 获取当前页的数据
		List<Record> itemList = Db.find("select appoi.*, fi.name name "
				+ " from arap_pre_pay_order_item appoi"
				+ " left join fin_item fi on appoi.fin_item_id = fi.id"
				+ " where appoi.order_id = '" + id + "' ");
		setAttr("itemList", itemList);
		render("/eeda/arap/PrePayOrder/PrePayOrderEdit.html");
	}

	public void searchAllAccount() {
		List<Account> accounts = Account.dao
				.find("select * from fin_account where type != 'REC' and bank_name != '现金'");
		renderJson(accounts);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_PrePayOrder_CANCEL })
	public void cancel() {

	}
}
