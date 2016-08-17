package controllers.arap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import models.Party;
import models.yh.profile.Contact;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CopeCheckOrderController extends Controller {
    private Log logger = Log.getLog(CopeCheckOrderController.class);

    public void index() {
    	setAttr("type", "SERVICE_PROVIDER");
    	setAttr("classify", "cope");
        render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderList.html");
    }

    public void add() {
    	setAttr("type", "SERVICE_PROVIDER");
    	setAttr("classify", "cope");
        render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderCreateSearchList.html");
    }

    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "SERVICE_PROVIDER");
    	setAttr("classify", "cope");
        render("/yh/arap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
    }

    // 创建应收对帐单时，先选取合适的回单，条件：客户，时间段
    public void createList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
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
                    fieldsWhere += mDataProp + " like '%" + searchValue + "%' AND ";
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
                        + "left join contact c on p.contact_id = c.id where ro.TRANSACTION_STATUS = 'confirmed' " + fieldsWhere);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // billing order 列表
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sqlTotal = "select count(1) total from billing_order where order_type='pay_audit_order'";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        // 左连接party, contact取到company_name
        // 左连接transfer_order取到运输单号
        // 左连接delivery_order取到配送单号
        String sql = "select bo.*,  p.id, p.party_type, t.company_name, to.order_no as transfer_order_no, "
                + "do.order_no as delivery_order_no, u.user_name as creator_name from billing_order bo "
                + " left join party p on bo.customer_id =p.id and bo.customer_type =p.party_type "
                + " left join contact t on p.contact_id = t.id left join transfer_order to on bo.transfer_order_id = to.id "
                + "left join user_login u on bo.creator = u.id"
                + " left join delivery_order do on bo.delivery_order_id = do.id where bo.order_type='pay_audit_order'";

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
