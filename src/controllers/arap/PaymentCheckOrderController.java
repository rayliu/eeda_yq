package controllers.arap;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeOrder;
import models.Party;
import models.UserLogin;
import models.yh.profile.Contact;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.OrderNoGenerator;
import controllers.util.OrderNoUtil;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PaymentCheckOrderController extends Controller {
    private Log logger = Log.getLog(PaymentCheckOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    public void index() {
        setAttr("type", "SERVICE_PROVIDER");
        setAttr("classify", "");
            render("/yh/arap/PaymentCheckOrder/PaymentCheckOrderList.html");
    }

    public void add() {
        setAttr("type", "SERVICE_PROVIDER");
        setAttr("classify", "");
            render("/yh/arap/PaymentCheckOrder/PaymentCheckOrderCreateSearchList.html");
    }

    public void create() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));
        
        setAttr("orderIds", ids);	 
        String beginTime = getPara("beginTime");
        if(beginTime != null && !"".equals(beginTime)){
        	setAttr("beginTime", beginTime);
        }
        String endTime = getPara("endTime");
        if(endTime != null && !"".equals(endTime)){
        	setAttr("endTime", endTime);	
        }
        String spId = getPara("spId");
        if(!"".equals(spId) && spId != null){
	        Party party = Party.dao.findById(spId);
	
	        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
	        setAttr("serviceProvider", contact);
	        setAttr("type", "SERVICE_PROVIDER");
	        setAttr("classify", "");
        }
        
        setAttr("saveOK", false);
        String name = (String) currentUser.getPrincipal();
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where user_name='" + name + "'");
        setAttr("create_by", users.get(0).get("id"));

        String sql = "select * from arap_audit_order order by id desc limit 0,1";
        setAttr("order_no", OrderNoGenerator.getNextOrderNo("DZ"));

        UserLogin userLogin = UserLogin.dao.findById(users.get(0).get("id"));
        setAttr("userLogin", userLogin);
            render("/yh/arap/PaymentCheckOrder/PaymentCheckOrderEdit.html");
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
        String sql = "select sum(tempcount) total from (select count(*) tempcount from transfer_order "
						+" union "
						+" select count(*) tempcount from depart_order)";
        Record rec = Db.findFirst(sql + fieldsWhere);
        logger.debug("total records:" + rec.get("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select tor.id,tor.order_no order_no,tor.create_stamp,tor.status,tor.remark,c.company_name from transfer_order tor "
										+" left join party p on p.id = tor.sp_id "
										+" left join contact c on c.id = p.contact_id "
										+" union " 
										+" select dor.id,depart_no order_no,dor.create_stamp,dor.status,dor.remark,c.company_name from depart_order dor "
										+" left join party p on p.id = dor.sp_id "
										+" left join contact c on c.id = p.contact_id order by create_stamp desc");
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.get("total"));
        orderMap.put("iTotalDisplayRecords", rec.get("total"));
        orderMap.put("aaData", orders);
        renderJson(orderMap);
    }

    // billing order 列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PCO_LIST})
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
