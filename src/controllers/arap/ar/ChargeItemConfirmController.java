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

import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeItemConfirmController extends Controller {
	private Log logger = Log.getLog(ChargeItemConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_AFFIRM})
	public void index() {
		render("/eeda/arap/ChargeItemConfirm/ChargeItemConfirmList.html");
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

	//
	public void list() {
		String customer = getPara("customer");//客户
		String beginTime = getPara("beginTime");//开始时间
		String endTime = getPara("endTime");//结束时间
		String transferOrderNo = getPara("transferOrderNo");//运输单号
		String customerNo = getPara("customerNo");//客户订单号
		String start = getPara("start");//始发地
		String orderNo= getPara("orderNo");//业务单号
		String serial_no = getPara("serial_no");//始发地
		String ref_no= getPara("ref_no");//业务单号
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
				&& getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
					+ getPara("iDisplayLength");
		}
		
		//升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.planning_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }
		
		String sqlTotal = "";
		String sql = "";
		// 收入状态条件没有过滤

		sql = "SELECT "
				+ "		ror.id,"
				+ " ror.order_no,"
				+ " ror.transaction_status,"
				+ " ror.remark,"
				+ " ror.receipt_date,"
				+ " ror.delivery_order_id,"
				+ " ror.transfer_order_id,"
				+ " ror.notity_party_id,"
				+ " ror.customer_id,"
				+ " (SELECT group_concat( DISTINCT toid.serial_no SEPARATOR '<br/>')"
				+ " FROM transfer_order_item_detail toid"
				+ " LEFT JOIN delivery_order_item doi ON toid.id = doi.transfer_item_detail_id"
				+ " LEFT JOIN delivery_order d_o ON d_o.id = doi.delivery_id"
				+ " WHERE d_o.id = ror.delivery_order_id) serial_no,"
				+ " dvr.ref_no ref_no,"
				+ "		  (select sum(amount) from return_order_fin_item rofi where rofi.return_order_id= ror.id) total_amount,"
				+ "        ror.path,"
				+ "        ror.create_date,"
				+ "        null change_amount,"
				+ "            IFNULL(tor.order_no, (SELECT "
				+ "                    GROUP_CONCAT(DISTINCT tor.order_no"
				+ "                            SEPARATOR '<br/>')"
				+ "                FROM"
				+ "                    delivery_order dvr"
				+ "                LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
				+ "                LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
				+ "                WHERE"
				+ "                    dvr.id = ror.delivery_order_id)) transfer_order_no,"
				+ "            '回单' order_tp,"
				+ "            dvr.order_no AS delivery_order_no,"
				+ "            IFNULL(c.abbr, c2.abbr) cname,"
				+ "            IFNULL(tor.planning_time, (SELECT "
				+ "                    GROUP_CONCAT(tor.planning_time SEPARATOR '<br/>')"
				+"                FROM"
				+"                    delivery_order dvr"
				+"                LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
				+"                LEFT JOIN transfer_order tor ON tor.id = doi.transfer_order_id"
				+"                WHERE"
				+"                    dvr.id = ror.delivery_order_id)) planning_time,"
				+"            ifnull(c.address, "
				+"            (select c.address from party p "
				+"            		LEFT JOIN contact c ON c.id = p.contact_id"
                +"                    where p.id = dvr.notify_party_id)"
				+"            ) address,"
				+"            IFNULL(tor.customer_order_no, tor2.customer_order_no) customer_order_no,"
				+"            IFNULL((SELECT "
				+"                    name"
				+"                FROM"
				+"                    location"
				+"                WHERE"
				+"                    code = tor.route_from), (SELECT "
				+"                    name"
				+"                FROM"
				+"                    location"
				+"                WHERE"
				+"                    code = tor2.route_from)) route_from,"
				+"            IFNULL((SELECT "
				+"                    name"
				+"                FROM"
				+"                    location"
				+"                WHERE"
				+"                    code = tor.route_to), (SELECT "
				+"                    name"
				+"                FROM"
				+"                    location"
				+"                WHERE"
				+"                    code = dvr.route_to)) route_to,"
				+"            (SELECT "
				+"                    SUM(rofi.amount)"
				+"                FROM"
				+"                    return_order_fin_item rofi"
				+"                WHERE"
				+"                    rofi.return_order_id = ror.id"
				+"                        AND rofi.fin_type = 'charge'"
				+"                        AND rofi.contract_id != '') contract_amount,"
				+"            DATE(dvr.appointment_stamp) AS depart_time,"
				+"            NULL pickup_amount,"
				+"            NULL step_amount,"
				+"            NULL wait_amount,"
				+"            NULL other_amount,"
				+"            NULL load_amount,"
				+"            NULL warehouse_amount,"
				+"            NULL transfer_amount,"
				+"            NULL send_amount,"
				+"            NULL installation_amount,"
				+"            NULL super_mileage_amount,"
				+"            NULL AS charge_total_amount,"
				+"            NULL insurance_amount,"
				+"            case tor.sp_id "
				+"            when tor.sp_id is not null then(  "
				+"            	select c.abbr from party p  "
				+"            		LEFT JOIN contact c ON c.id = p.contact_id "
                +"                    where p.id = tor.sp_id "
                +"                ) "
                +"            else ( "
				+"            	select c.abbr from party p  "
				+"            		LEFT JOIN contact c ON c.id = p.contact_id "
                +"                    where p.id = tor2.sp_id "
				+"            	) "
				+"            end sp    "
				+"    FROM"
				+"        return_order ror"
				+"    LEFT JOIN transfer_order tor ON tor.id = ror.transfer_order_id"
				+"    LEFT JOIN party p ON p.id = tor.customer_id"
				+"    LEFT JOIN contact c ON c.id = p.contact_id"
				+"    LEFT JOIN depart_transfer dt ON (dt.order_id = tor.id"
				+"        AND IFNULL(dt.pickup_id, 0) > 0)"
				+"    LEFT JOIN delivery_order dvr ON ror.delivery_order_id = dvr.id"
				+"    LEFT JOIN delivery_order_item doi ON doi.delivery_id = dvr.id"
				+"    LEFT JOIN transfer_order tor2 ON tor2.id = doi.transfer_order_id"
				+"    LEFT JOIN party p2 ON p2.id = tor2.customer_id"
				+"    LEFT JOIN contact c2 ON c2.id = p2.contact_id"
				+"    LEFT JOIN depart_order dor ON dor.id = dt.pickup_id"
				+"    LEFT JOIN pickup_order_fin_item dofi ON dofi.pickup_order_id = dor.id"
				+"    LEFT JOIN user_login usl ON usl.id = ror.creator"
				+"    WHERE"
				+"        ror.transaction_status = '已签收'"
				+"    AND ror.customer_id IN ( SELECT customer_id FROM user_customer WHERE user_name = '"+currentUser.getPrincipal()+"' )"
				+"    GROUP BY ror.id"
				+"    UNION (SELECT "
				+"			amco.id id,"
				+"            amco.order_no order_no,"
				+"            amco.status transaction_status,"
				+"            NULL receipt_date,"
				+"            amco.remark remark,"
				+"            NULL delivery_order_id,"
				+"            NULL transfer_order_id,"
				+"            NULL notity_party_id,"
				+"            amco.customer_id customer_id,"
				+" 		      null serial_no,"
				+" 		      null ref_no,"
				+"            amco.total_amount total_amount,"
				+"            NULL path,"
				+"            amco.create_stamp create_date,"
				+"            null change_amount,"
				+"            NULL transfer_order_no,"
				+"            '收入单' order_tp,"
				+"            NULL delivery_order_no,"
				+"            c.abbr cname,"
				+"            NULL planning_time,"
				+"            NULL address,"
				+"            (SELECT "
				+"                    GROUP_CONCAT(DISTINCT amcoi.customer_order_no"
				+"                            SEPARATOR '<br/>')"
				+"                FROM"
				+"                    arap_misc_charge_order_item amcoi"
				+"                WHERE"
				+"                    amcoi.misc_order_id = amco.id) customer_order_no,"
				+"            NULL route_from,"
				+"            NULL route_to,"
				+"            NULL contract_amount,"
				+"            NULL depart_time,"
				+"            NULL pickup_amount,"
				+"            NULL step_amount,"
				+"            NULL wait_amount,"
				+"            NULL other_amount,"
				+"            NULL load_amount,"
				+"            NULL warehouse_amount,"
				+"            NULL transfer_amount,"
				+"            NULL send_amount,"
				+"            NULL installation_amount,"
				+"            NULL super_mileage_amount,"
				+"            amco.total_amount charge_total_amount,"
				+"            NULL insurance_amount,"
				+"            c1.abbr sp"
				+"    FROM"
				+"        arap_misc_charge_order amco"
				+"    LEFT JOIN party p ON p.id = amco.customer_id"
				+"    LEFT JOIN contact c ON c.id = p.id"
				+"    LEFT JOIN party p1 ON p1.id = amco.sp_id"
				+"    LEFT JOIN contact c1 ON c1.id = p1.id"
				+"    WHERE"
				+"        amco.STATUS = '新建'"
				+"            AND amco.type = 'biz'"
				+"            AND amco.total_amount != 0)";
		if (customer == null && beginTime == null && endTime == null
				&& transferOrderNo == null && customerNo == null && start == null && orderNo==null&& serial_no==null&& ref_no==null) {
			sqlTotal = " select count(1) total from ("+ sql +") A";
			sql = "select * from ("+ sql +") A ";
		} else {
			if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			
			 String conditions = " where ifnull(A.cname,'')like '%"+customer+"%'"
				    + " and ifnull(A.route_from,'') like '%"+start
				    +"%' and ifnull(A.order_no,'') like '%"+orderNo
				    +"%' and ifnull(A.customer_order_no,'')like '%"+customerNo+"%'"
				    +"	 and ifnull(A.serial_no,'')like '%"+serial_no+"%'"
				    +"   and ifnull(A.ref_no,'')like '%"+ref_no+"%'"
				    +"   and IFNULL(A.transfer_order_no,'') like '%"+transferOrderNo
				    +"%' and IFNULL(A.planning_time,'1970-01-01')  between '"
					+ beginTime
					+ "' and '"
					+ endTime
					+ "' ";
			
			sqlTotal = " select count(1) total from ("+ sql +") A" + conditions;
			 
			sql = "select * from ("+ sql +") A " + conditions;
		}
		 
		
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		List<Record> BillingOrders = Db.find(sql + orderByStr  + sLimit);

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("sEcho", pageIndex);
		BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
		BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

		BillingOrderListMap.put("aaData", BillingOrders);

		renderJson(BillingOrderListMap);
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
