package controllers.oms.amazonSalesOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.datatype.XMLGregorianCalendar;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
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

import controllers.oms.amazonSalesOrder.util.MarketplaceWebServiceOrdersSampleConfig;
import controllers.oms.ebaySalesOrder.EbayApiContextUtil;
import controllers.profile.EbayAccountController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AmazonSalesOrderController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(AmazonSalesOrderController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	String sql = "select * from amazon_seller_account where office_id = "+ user.getLong("office_id");
        List<Record> amazonAcountList = Db.find(sql);
        setAttr("amazonAcountList", amazonAcountList);
    	
        String type = getPara("type");
        setAttr("type", type);
        render("/oms/amazonSalesOrder/amazonSalesOrderList.html");
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
        String sql = "select * from amazon_sales_order where 1=1 ";

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by last_update_date desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void importOrders() {
     // Get a client connection.
        // Make sure you've set the variables in MarketplaceWebServiceOrdersSampleConfig.
        MarketplaceWebServiceOrdersClient client = MarketplaceWebServiceOrdersSampleConfig.getClient();

        // Create a request.
        ListOrdersRequest request = new ListOrdersRequest();
        String sellerId = "A2EOZCNPFBJY0B";
        request.setSellerId(sellerId);
        //Secret Key?
//        String mwsAuthToken = "+qVn8uo1/sId/diaBqcOlSv96eObknLR7i7ABC2J";
//        request.setMWSAuthToken(mwsAuthToken);
//        Date now = new Date();
        Calendar nowDate = Calendar.getInstance();
        nowDate.add(Calendar.DAY_OF_MONTH, -7);//每次更新当前日期-7
        
        XMLGregorianCalendar createdAfter = MwsUtl.getDTF().newXMLGregorianCalendar();
        createdAfter.setYear(nowDate.get(Calendar.YEAR));
        createdAfter.setMonth(nowDate.get(Calendar.MONTH));
        createdAfter.setDay(nowDate.get(Calendar.DAY_OF_MONTH));
        logger.debug("createdAfter: "+createdAfter.toString());
        request.setCreatedAfter(createdAfter);
        
//        XMLGregorianCalendar createdBefore = MwsUtl.getDTF().newXMLGregorianCalendar();
//        request.setCreatedBefore(createdBefore);
//        XMLGregorianCalendar lastUpdatedAfter = MwsUtl.getDTF().newXMLGregorianCalendar();
//        request.setLastUpdatedAfter(lastUpdatedAfter);
//        XMLGregorianCalendar lastUpdatedBefore = MwsUtl.getDTF().newXMLGregorianCalendar();
//        request.setLastUpdatedBefore(lastUpdatedBefore);
        List<String> orderStatus = new ArrayList<String>();
        request.setOrderStatus(orderStatus);
        List<String> marketplaceId = new ArrayList<String>();
        
        marketplaceId.add("A1F83G8C2ARO7P");//UK
        marketplaceId.add("A1PA6795UKMFR9");//DE
        marketplaceId.add("A13V1IB3VIYZZH");//ES
        marketplaceId.add("A1RKKUPIHCS9HS");//FR
        marketplaceId.add("APJ6JRA9NG5V4");//IT
        request.setMarketplaceId(marketplaceId);
//        List<String> fulfillmentChannel = new ArrayList<String>();
//        fulfillmentChannel.add("AFN");
//        request.setFulfillmentChannel(fulfillmentChannel);
//        List<String> paymentMethod = new ArrayList<String>();
//        request.setPaymentMethod(paymentMethod);
//        String buyerEmail = "example";
//        request.setBuyerEmail(buyerEmail);
//        String sellerOrderId = "example";
//        request.setSellerOrderId(sellerOrderId);
        Integer maxResultsPerPage = 100;//每页可返回的最多订单数。默认100
        request.setMaxResultsPerPage(maxResultsPerPage);
        List<String> tfmShipmentStatus = new ArrayList<String>();
        request.setTFMShipmentStatus(tfmShipmentStatus);
        
        List<Record> recList = ListOrdersApi.invokeListOrders(client, request);
        
        renderJson(recList);
    }

   
}
