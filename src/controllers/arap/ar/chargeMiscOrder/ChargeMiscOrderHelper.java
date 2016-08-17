package controllers.arap.ar.chargeMiscOrder;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;

import models.UserLogin;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrderItem;
import controllers.util.OrderNoGenerator;

public class ChargeMiscOrderHelper {
	private static ChargeMiscOrderHelper me = null;

	private ChargeMiscOrderHelper() {
	};

	public static ChargeMiscOrderHelper getInstance() {
		if (me == null) {
			return new ChargeMiscOrderHelper();
		}
		return me;
	}

	public ArapMiscCostOrder buildNewCostMiscOrder(
			ArapMiscChargeOrder originOrder, UserLogin user)
			throws IllegalAccessException, InvocationTargetException {
		long originId = originOrder.getLong("id");

		ArapMiscCostOrder orderDest = new ArapMiscCostOrder();
		orderDest.set("type", originOrder.getStr("type"));
		orderDest.set("cost_to_type", originOrder.getStr("charge_from_type"));
		orderDest.set("sp_id", originOrder.getStr("sp_id"));
		orderDest.set("customer_id", originOrder.getStr("customer_id"));
		orderDest
				.set("total_amount", 0 - originOrder.getDouble("total_amount"));
		orderDest.set("others_name", originOrder.getStr("others_name"));
		orderDest.set("ref_no", originOrder.getStr("ref_no"));
		orderDest.set("create_by", user.getLong("id"));
		orderDest.set("office_id", user.getLong("office_id"));
		orderDest.set("status", "新建");

		orderDest.set("order_no", originOrder.getStr("order_no"));
		orderDest.set("ref_order_id", originOrder.getLong("id"));
		orderDest.set("ref_order_no", originOrder.get("order_no"));
		orderDest.save();

		List<ArapMiscChargeOrderItem> originItems = ArapMiscChargeOrderItem.dao
				.find("select * from arap_misc_charge_order_item where misc_order_id = ?",
						originOrder.getLong("id"));

		for (ArapMiscChargeOrderItem originItem : originItems) {
			ArapMiscCostOrderItem newItem = new ArapMiscCostOrderItem();
			newItem.set("status", "新建");
			newItem.set("creator", user.getLong("id"));
			newItem.set("create_date", new Date());
			newItem.set("customer_order_no",
					originItem.get("customer_order_no"));
			newItem.set("item_desc", originItem.get("item_desc"));
			newItem.set("fin_item_id", originItem.get("fin_item_id"));

			newItem.set("misc_order_id", orderDest.get("id"));
			newItem.set("amount", 0 - originItem.getDouble("amount"));
			newItem.set("change_amount", 0 - originItem.getDouble("amount"));
			newItem.save();
		}
		return orderDest;
	}
	
	public ArapMiscCostOrder updateCostMiscOrder(
			ArapMiscChargeOrder originOrder, UserLogin user)
			throws IllegalAccessException, InvocationTargetException {
		
		ArapMiscCostOrder orderDest = ArapMiscCostOrder.dao.findFirst(
				"select * from arap_misc_cost_order where order_no=?",
				originOrder.getStr("order_no"));
		
		orderDest.set("type", originOrder.getStr("type"));
		orderDest.set("cost_to_type", originOrder.getStr("charge_from_type"));
		orderDest.set("sp_id", originOrder.getStr("sp_id"));
		orderDest.set("customer_id", originOrder.getStr("customer_id"));
		orderDest.set("total_amount", 0 - originOrder.getDouble("total_amount"));
		orderDest.set("others_name", originOrder.getStr("others_name"));
		orderDest.set("ref_no", originOrder.getStr("ref_no"));
		
		orderDest.update();
		
		Db.update("delete from arap_misc_cost_order_item where misc_order_id=?",
				orderDest.getLong("id"));

		List<ArapMiscChargeOrderItem> originItems = ArapMiscChargeOrderItem.dao
				.find("select * from arap_misc_charge_order_item where misc_order_id = ?",
						originOrder.getLong("id"));

		for (ArapMiscChargeOrderItem originItem : originItems) {
			ArapMiscCostOrderItem newItem = new ArapMiscCostOrderItem();
			newItem.set("status", "新建");
			newItem.set("creator", user.getLong("id"));
			newItem.set("create_date", new Date());
			newItem.set("customer_order_no",
					originItem.get("customer_order_no"));
			newItem.set("item_desc", originItem.get("item_desc"));
			newItem.set("fin_item_id", originItem.get("fin_item_id"));

			newItem.set("misc_order_id", orderDest.get("id"));
			newItem.set("amount", 0 - originItem.getDouble("amount"));
			newItem.set("change_amount", 0 - originItem.getDouble("amount"));
			newItem.save();
		}
		return orderDest;
	}

	public void deleteCostMiscOrder(ArapMiscChargeOrder originOrder) {
		ArapMiscCostOrder arapMiscCostOrder = ArapMiscCostOrder.dao.findFirst(
				"select * from arap_misc_cost_order where order_no=?",
				originOrder.getStr("order_no"));
		// 删除从表
		Db.update("delete from arap_misc_cost_order_item where misc_order_id=?",
				arapMiscCostOrder.getLong("id"));
		// 删除主表
		arapMiscCostOrder.delete();
	}
}
