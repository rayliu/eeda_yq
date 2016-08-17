package controllers.arap.ar;

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
public class ChargeAdjustOrderController extends Controller {
    private Log logger = Log.getLog(ChargeAdjustOrderController.class);

    public void index() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAdjustOrder/ChargeAdjustOrderList.html");
    }

    public void add() {
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAdjustOrder/ChargeCheckOrderCreateSearchList.html");
    }

    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/yh/arap/ChargeAdjustOrder/ChargeCheckOrderEdit.html");
    }

    // 创建应收结帐单时，先选取合适的对账单，条件：客户，时间段
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

        String sqlTotal = "select count(1) total from arap_audit_order aao where aao.status in ('confirmed','completed')";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        String sql = "select aao.*,c.abbr cname,ror.order_no return_order_no,tor.order_no transfer_order_no,dor.order_no delivery_order_no,ul.user_name creator_name from arap_audit_order aao "
				+ " left join party p on p.id = aao.payee_id "
				+ " left join contact c on c.id = p.contact_id "
				+ " left join arap_audit_item aai on aai.audit_order_id= aao.id "
				+ " left join return_order ror on ror.id = aai.ref_order_id "
				+ " left join transfer_order tor on tor.id = ror.transfer_order_id "
				+ " left join delivery_order dor on dor.id = ror.delivery_order_id "
				+ " left join user_login ul on ul.id = aao.create_by where aao.status in ('confirmed','completed') order by aao.create_stamp desc " + sLimit;

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
