package controllers.arap.financial.inOutOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeInvoiceApplication;
import models.ArapChargeOrder;
import models.Party;
import models.UserLogin;
import models.eeda.profile.Account;
import models.yh.arap.ArapMiscCostOrderItem;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrderDTO;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrderItem;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
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
public class InOutMiscOrderController extends Controller {
	private Log logger = Log.getLog(InOutMiscOrderController.class);
	

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_IMO_LIST })
	public void index() {
		render("/yh/arap/InOutMiscOrder/InOutMiscOrderList.html");
	}


	@RequiresPermissions(value = { PermissionConstant.PERMSSION_IMO_LIST })
	public void list() {
		String order_no = getPara("order_no");
		String biz_type = getPara("biz_type");
		String charge_unit = getPara("charge_unit");
		String pay_unit = getPara("pay_unit");
		
		String beginTime = getPara("beginTime");
		String endTime = getPara("endTime");

		String conditions = "";
		if (order_no == null && biz_type == null && charge_unit == null && pay_unit == null
				&& beginTime == null && endTime == null) {

		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}

			conditions = " and ifnull(order_no, '') like '%" + order_no
					+ "%' and ifnull(charge_unit,'') like '%" + charge_unit
					+ "%' and ifnull(pay_unit,'') like '%" + pay_unit + "%' "
					+ " and create_date between " + " '" + beginTime
					+ "' and '" + endTime + " 23:59:59' ";
			if (biz_type != null && biz_type.length() > 0) {
				conditions += ("and biz_type = '" + biz_type + "' ");
			}
		}

		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		String sqlFrom = " from arap_in_out_misc_order ao "
				+ "left join user_login ul on ao.creator_id = ul.id";

		String sqlTotal = "select count(1) total " + sqlFrom + " where 1=1 "
				+ conditions;
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));

		String sql = "select ao.*, ifnull(ul.c_name, ul.user_name) user_name" + sqlFrom 
				+ " where 1=1 " + conditions
				+ " order by create_date desc " + sLimit;

		logger.debug("sql:" + sql);
		List<Record> orders = Db.find(sql);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", orders);

		renderJson(BillingOrderListMap);
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_IMO_CREATE })
	public void create() {
		UserLogin user=LoginUserController.getLoginUser(this);
		String sql = "select o.id, o.office_name, ifnull(o.is_stop, 0) is_stop from user_office uo, office o "
				+ "where uo.office_id = o.id and ifnull(o.is_stop, 0)!= 1 and uo.user_name ='"+user.getStr("user_name")+"'";
		//String sql = "select o.id,o.office_name,o.is_stop from office o where o.id in (select office_id from user_office where user_name='"+user.getStr("user_name")+"')";
		List<Record> offices = Db.find(sql);
		setAttr("officeList", offices);
		
		render("/yh/arap/InOutMiscOrder/InOutMiscOrderEdit.html");
	}

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_IMO_CREATE,
			PermissionConstant.PERMSSION_IMO_UPDATE }, logical = Logical.OR)
	@Before(Tx.class)
	public void save() throws Exception {
		String jsonStr = getPara("params");
		logger.debug(jsonStr);
		Gson gson = new Gson();
		Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);
		
		String orderId = (String) dto.get("order_id");		
		String issue_date_str = (String) dto.get("issue_date");
		Date issue_date = null;
		if(StringUtils.isNotEmpty(issue_date_str)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		    issue_date = sdf.parse(issue_date_str);
		}
	    
		String issue_office = (String) dto.get("issue_office_id");
		String order_type = (String) dto.get("order_type");
		String biz_type = (String) dto.get("biz_type");
		String ref_no = (String) dto.get("ref_no");
		
		String pay_unit = (String) dto.get("pay_unit");
		String pay_person = (String) dto.get("pay_person");
		String charge_amount = (String) dto.get("charge_amount");//收款金额：别人付款，我们收款
		
		
		String charge_unit = (String) dto.get("charge_unit");
		String charge_person = (String) dto.get("charge_person");//收款人
		String pay_amount = (String) dto.get("pay_amount");//付款金额：别人收款，我们付款
		
		String remark = (String) dto.get("remark");

		UserLogin user = LoginUserController.getLoginUser(this);
		
		if (StringUtils.isNotEmpty(orderId)) {
			updateOrder(orderId, issue_date, issue_office, order_type,
					biz_type, ref_no, pay_unit, pay_person, charge_amount,
					charge_unit, charge_person, pay_amount, remark);
		} else {
			Long id = createOrder(issue_date, issue_office, order_type, biz_type,
					ref_no, pay_unit, pay_person, charge_amount, charge_unit,
					charge_person, pay_amount, remark, user);
			orderId = String.valueOf(id);
		}

		String sql = "select * from arap_in_out_misc_order where id = ?";
		ArapInOutMiscOrder orderDto = ArapInOutMiscOrder.dao.findFirst(sql, orderId);
		
		renderJson(orderDto);
	}


	private Long createOrder(Date issue_date,
			String issue_office, String order_type, String biz_type,
			String ref_no, String pay_unit, String pay_person,
			String charge_amount, String charge_unit, String charge_person,
			String pay_amount, String remark, UserLogin user) {
		ArapInOutMiscOrder order = new ArapInOutMiscOrder();
		order.set("order_no", OrderNoGenerator.getNextOrderNo("WLPJ"));
		order.set("issue_date", issue_date);
		order.set("issue_office_id", issue_office);
		order.set("order_type", order_type);
		order.set("biz_type", biz_type);
		order.set("ref_no", ref_no);
		
		order.set("pay_unit", pay_unit);
		order.set("pay_person", pay_person);
		order.set("charge_amount", Double.parseDouble(charge_amount));//收款金额：别人付款，我们收款
		order.set("charge_status", "未收");
		
		order.set("charge_unit", charge_unit);
		order.set("charge_person", charge_person);//收款人
		order.set("pay_amount", Double.parseDouble(pay_amount));//付款金额：别人收款，我们付款
		order.set("pay_status", "未付");
		
		order.set("remark", remark);

		order.set("creator_id", user.getLong("id"));
		order.set("create_date", new Date());
		order.set("office_id", user.getLong("office_id"));
		
		order.save();
		return order.getLong("id");
	}


	private void updateOrder(String orderId, Date issue_date,
			String issue_office, String order_type, String biz_type,
			String ref_no, String pay_unit, String pay_person,
			String charge_amount, String charge_unit, String charge_person,
			String pay_amount, String remark) {
		ArapInOutMiscOrder order = ArapInOutMiscOrder.dao.findById(orderId);
		
		order.set("issue_date", issue_date);
		order.set("issue_office_id", issue_office);
		order.set("order_type", order_type);
		order.set("biz_type", biz_type);
		order.set("ref_no", ref_no);
		
		order.set("pay_unit", pay_unit);
		order.set("pay_person", pay_person);
		order.set("charge_amount", charge_amount);//收款金额：别人付款，我们收款
		
		order.set("charge_unit", charge_unit);
		order.set("charge_person", charge_person);//收款人
		order.set("pay_amount", pay_amount);//付款金额：别人收款，我们付款
		
		order.set("remark", remark);
		order.update();
	}

	

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_IMO_UPDATE })
	public void edit() throws ParseException {
		String id = getPara("id");

		UserLogin user=LoginUserController.getLoginUser(this);
		String officeSql = "select o.id, o.office_name, ifnull(o.is_stop, 0) is_stop from user_office uo, office o "
				+ "where uo.office_id = o.id and ifnull(o.is_stop, 0)!= 1 and uo.user_name ='"+user.getStr("user_name")+"'";
		
		List<Record> offices = Db.find(officeSql);
		setAttr("officeList", offices);
		
		String sql = "select ao.*, ifnull(ul.c_name, ul.user_name) user_name from arap_in_out_misc_order ao "
				+ "left join user_login ul on ao.creator_id = ul.id  where ao.id = ?";
		ArapInOutMiscOrder orderDto = ArapInOutMiscOrder.dao.findFirst(sql, id);
		setAttr("order", orderDto);
		
		render("/yh/arap/InOutMiscOrder/InOutMiscOrderEdit.html");
	}

	


	
}
