package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.ReturnOrder;
import models.UserLogin;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.profile.Contact;
import models.yh.returnOrder.ReturnOrderFinItem;

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
public class ChargeItemConfirmController extends Controller {
	private Log logger = Log.getLog(ChargeItemConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
	public void index() {
		render("/eeda/arap/ChargeItemConfirm/ChargeItemConfirm.html");
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
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,p.company_name customer,p1.company_name sp_name,f.name charge_name,u.name unit_name,c.name currency_name "
				+ " from job_order jo "
				+ " left join job_order_arap joa on jo.id=joa.order_id "
				+ " left join party p on p.id=jo.customer_id "
				+ " left join party p1 on p1.id=joa.sp_id "
				+ " left join fin_item f on f.id=joa.charge_id "
				+ " left join unit u on u.id=joa.unit_id "
				+ " left join currency c on c.id=joa.currency_id "
				+ " where joa.order_type='charge') A where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCO_UPDATE })
	@Before(Tx.class)
	public void updateOrderFinItem() {
		String order_ty = getPara("order_ty");//单据类型
		String order_id = getPara("order_id");//单据ID
		String value = getPara("value");//更改值
		if("回单".equals(order_ty)){
			List<ReturnOrderFinItem> ordeItems = ReturnOrderFinItem.dao.find("select * from return_order_fin_item where return_order_id=?", order_id);
			Double originTotal = 0.0;
			for(ReturnOrderFinItem orderItem : ordeItems){
				originTotal += orderItem.getDouble("amount")==null?0.0:orderItem.getDouble("amount");
			}
			Double newAmount=0.0;
			if(Double.parseDouble(value)>0){
				newAmount =Double.parseDouble(value)-originTotal;
			}
			else{
				newAmount =Double.parseDouble(value)+originTotal;
			}
			if(newAmount!=0){
				String name = (String) currentUser.getPrincipal();
				List<UserLogin> users = UserLogin.dao
						.find("select * from user_login where user_name='" + name
								+ "'");
				ReturnOrderFinItem orderItem1 = new ReturnOrderFinItem();
				orderItem1.set("return_order_id", order_id);
				orderItem1.set("amount", newAmount);
				orderItem1.set("fin_item_id", 4);
				orderItem1.set("status", "新建");
				orderItem1.set("creator", users.get(0).get("id"));
				orderItem1.set("remark", "对账调整金额");
				orderItem1.set("create_date", new Date());
				orderItem1.save();
			}
		}
		renderJson("{\"success\":true}");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
	public void chargeConfirmReturnOrder() {
		String orderno = getPara("orderno");
		String returnOrderIds = getPara("returnOrderIds");
		String[] returnOrderArr = returnOrderIds.split(",");
		String[] ordernoArr = orderno.split(",");
		for (int i = 0; i < returnOrderArr.length; i++) {
			if ("回单".equals(ordernoArr[i])) {
				ReturnOrder returnOrder = ReturnOrder.dao
						.findById(returnOrderArr[i]);
				returnOrder.set("transaction_status", "已确认");
				returnOrder.update();
			}
			if ("收入单".equals(ordernoArr[i])) {
				ArapMiscChargeOrder arapmiscchargeorder = ArapMiscChargeOrder.dao
						.findById(returnOrderArr[i]);
				arapmiscchargeorder.set("STATUS", "已确认");
				arapmiscchargeorder.update();
			}
		}
		renderJson("{\"success\":true}");
	}
}
