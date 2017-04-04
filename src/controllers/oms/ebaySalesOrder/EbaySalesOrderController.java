package controllers.oms.ebaySalesOrder;

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
import com.ebay.soap.eBLBaseComponents.ShipmentTrackingDetailsType;
import com.ebay.soap.eBLBaseComponents.TradingRoleCodeType;
import com.ebay.soap.eBLBaseComponents.TransactionType;
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
    	UserLogin user = LoginUserController.getLoginUser(this);
        String sql = "select * from ebay_seller_account where type='"+EbayApiContextUtil.configStr+"' and office_id = "+ user.getLong("office_id");
        List<Record> ebayAcountList = Db.find(sql);
        setAttr("ebayAcountList", ebayAcountList);
        
        Record orderNopayRec = Db.findFirst("select count(1) total from ebay_order where paid_time is null");
        setAttr("orderNopayCount", orderNopayRec.get("total"));
        
        Record orderNoshipRec = Db.findFirst("select count(1) total from ebay_order where shipped_time is null");
        setAttr("orderNoshipCount", orderNoshipRec.get("total"));
        
        render("/oms/ebaySalesOrder/ebaySalesOrderList.html");
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

    //专供页面单独调用
    public void importOrders() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        importOrders(office_id);
    }
    
    //供job调用
    public void importOrders(long office_id) {
        
        OrderType[] orders = getOrders(office_id);
        
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
            
            
            TransactionType[] tType = order.getTransactionArray().getTransaction();
            TransactionType transaction = tType[0];//只获取第一个
            
            rec.set("transaction_id", transaction.getTransactionID());
            rec.set("item_id", transaction.getItem().getItemID());
            rec.set("sku", transaction.getItem().getSKU());
            rec.set("total", order.getTotal().getValue());
            rec.set("total_currency_id", order.getTotal().getCurrencyID().value());
            rec.set("buyer_user_ID", order.getBuyerUserID());
            rec.set("seller_user_ID", order.getSellerUserID());
            rec.set("order_status", order.getOrderStatus().value());
            if(order.getShippedTime()!=null)
                rec.set("shipped_time", order.getShippedTime().getTime());
            if(order.getPaidTime()!=null)
                rec.set("paid_time", order.getPaidTime().getTime());
            
            if(transaction.getShippingDetails().getShipmentTrackingDetails().length>0){
                ShipmentTrackingDetailsType stdt = transaction.getShippingDetails().getShipmentTrackingDetails(0);
                rec.set("shipment_tracking_number", stdt.getShipmentTrackingNumber());
                rec.set("shipping_carrier_used", stdt.getShippingCarrierUsed());//送货公司
            }
            
            rec.set("sales_record_number", transaction.getShippingDetails().getSellingManagerSalesRecordNumber());
            
            Record oldRec = Db.findFirst("select * from ebay_order where order_id=?", order.getOrderID());
            if(oldRec != null){
                rec.set("id", oldRec.getLong("id"));
                Db.update("ebay_order", rec);
            }else{
                Db.save("ebay_order", rec);
            }
            
            
            
            recList.add(rec);
        }
        
        renderJson(recList);
    }

    private OrderType[] getOrders(long office_id) {
        apiContext = new EbayApiContextUtil(office_id).getApiContext();
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
