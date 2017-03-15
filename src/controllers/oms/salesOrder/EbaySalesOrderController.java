package controllers.oms.salesOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.call.GetOrdersCall;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.OrderIDArrayType;
import com.ebay.soap.eBLBaseComponents.OrderStatusCodeType;
import com.ebay.soap.eBLBaseComponents.OrderType;
import com.ebay.soap.eBLBaseComponents.TradingRoleCodeType;
import com.ebay.soap.eBLBaseComponents.WarningLevelCodeType;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.EbayAccountController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class EbaySalesOrderController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(EbaySalesOrderController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() {
        String type = getPara("type");
        setAttr("type", type);
        render("/oms/SalesOrder/ebaySalesOrderList.html");
    }

    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");

        String type = getPara("type");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from ebay_order where 1=1 ";

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by created_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void importOrders() {
        OrderType[] orders = getOrders();
        int size = orders != null ? orders.length : 0;
        String[] columnNames = { "OrderId", "NumberOfTrans", "TransPrice",
                "CreatedDate", "ShippingServiceSelected", "InsuranceWanted",
                "IsMultiLegShipping" };

        List<Record> recList = new ArrayList<Record>(size);
        
        for (int i = 0; i < size; i++) {
            Record rec = new Record();
            OrderType order = orders[i];
            rec.set("order_id", order.getOrderID());
            rec.set("created_time", order.getCreatedTime().getTime());
//            rec.set("transaction_id", order.getTransactionArray().getTransaction())
            rec.set("total", order.getTotal().getValue());
            rec.set("buyer_user_ID", order.getBuyerUserID());
            rec.set("seller_user_ID", order.getSellerUserID());
            Db.save("ebay_order", rec);
            
            recList.add(rec);
        }
        
        renderJson(recList);
    }

    private OrderType[] getOrders() {
        apiContext = new EbayApiContextUtil().getApiContext();
        OrderType[] orders = null;
        try {
            String ids = "";// this.txtOrderId.getText().trim();
            // if (ids.length() == 0) {
            // throw new Exception("Please enter valid OrderIds.");
            // }

            DetailLevelCodeType[] detailLevels = new DetailLevelCodeType[] {
                    DetailLevelCodeType.RETURN_ALL,
                    DetailLevelCodeType.ITEM_RETURN_ATTRIBUTES,
                    DetailLevelCodeType.ITEM_RETURN_DESCRIPTION };

            GetOrdersCall api = new GetOrdersCall(this.apiContext);
            // api.setDetailLevel(detailLevels);

            StringTokenizer st = new StringTokenizer(ids, ",");
            ArrayList lstOrders = new ArrayList();
            while (st.hasMoreTokens()) {
                lstOrders.add(st.nextToken());
            }

            int size = lstOrders.size();
            String[] orderIds = new String[size];
            for (int i = 0; i < size; i++) {
                orderIds[i] = lstOrders.get(i).toString().trim();
            }

            OrderIDArrayType oiat = new OrderIDArrayType();
            // oiat.setOrderID(orderIds);
            // api.setOrderIDArray(oiat);

            api.setOrderStatus(OrderStatusCodeType.COMPLETED);

            api.setOrderRole(TradingRoleCodeType.SELLER);

            // if (this.txtStartDate.getText().trim().length() > 0) {
            // Calendar date = GuiUtil.getCalendarFromField(this.txtStartDate);
            // api.setCreateTimeFrom(date);
            // }
            //
            // if (this.txtEndDate.getText().trim().length() > 0) {
            // Calendar date = GuiUtil.getCalendarFromField(this.txtEndDate);
            // api.setCreateTimeTo(date);
            // }

            api.setWarningLevel(WarningLevelCodeType.HIGH);
            api.setNumberOfDays(7);

            orders = api.getOrders();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return orders;
    }
}
