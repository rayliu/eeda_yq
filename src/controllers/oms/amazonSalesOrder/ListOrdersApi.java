/*******************************************************************************
 * Copyright 2009-2017 Amazon Services. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 *
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at: http://aws.amazon.com/apache2.0
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 *******************************************************************************
 * Marketplace Web Service Orders
 * API Version: 2013-09-01
 * Library Version: 2017-02-22
 * Generated: Thu Mar 02 12:41:03 UTC 2017
 */
package controllers.oms.amazonSalesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.amazonservices.mws.client.MwsUtl;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersClient;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersException;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrdersResult;
import com.amazonservices.mws.orders._2013_09_01.model.Order;
import com.amazonservices.mws.orders._2013_09_01.model.ResponseHeaderMetadata;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.oms.amazonSalesOrder.util.MarketplaceWebServiceOrdersSampleConfig;


/** Sample call for ListOrders. */
public class ListOrdersApi {

    /**
     * Call the service, log response and exceptions.
     *
     * @param client
     * @param request
     *
     * @return The response.
     */
    public static List<Record> invokeListOrders(
            MarketplaceWebServiceOrders client, 
            ListOrdersRequest request) {
        try {
            //在本项目中由于使用了xalan.jar里面实现了DocumentBuilder， 本应该使用rt.jar里的DocumentBuilder
            System.setProperty( "javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl" );
            // Call the service.
            ListOrdersResponse response = client.listOrders(request);
            
            
            ResponseHeaderMetadata rhmd = response.getResponseHeaderMetadata();
            // We recommend logging every the request id and timestamp of every call.
            System.out.println("-----Response----");
            System.out.println("RequestId: "+rhmd.getRequestId());
            System.out.println("Timestamp: "+rhmd.getTimestamp());
            
            
            String responseXml = response.toXML();
            System.out.println(responseXml);
            System.out.println("-----Done-----");
            
            ListOrdersResult listOrdersResult = response.getListOrdersResult();
            List<Order> orders = listOrdersResult.getOrders();
            
            List<Record> recList = new ArrayList<Record>(orders.size());
            for (Order order : orders) {
                Record rec = new Record();

                rec.set("amazon_order_id", order.getAmazonOrderId());
                rec.set("buyer_name", order.getBuyerName());
                if(order.getOrderTotal()!=null){
                    rec.set("order_currency_code", order.getOrderTotal().getCurrencyCode());
                    rec.set("total_amount", order.getOrderTotal().getAmount());
                }
                
                rec.set("fulfillment_channel", order.getFulfillmentChannel());
                rec.set("purchase_date", order.getPurchaseDate().toGregorianCalendar().getTime());
                rec.set("order_status", order.getOrderStatus());
                rec.set("last_update_date", order.getLastUpdateDate().toGregorianCalendar().getTime());
                
                Record oldRec = Db.findFirst("select * from amazon_sales_order where amazon_order_id=?", order.getAmazonOrderId());
                if(oldRec != null){
                    rec.set("id", oldRec.getLong("id"));
                    Db.update("amazon_sales_order", rec);
                }else{
                    Db.save("amazon_sales_order", rec);
                }
                
                recList.add(rec);
            }
            return recList;
        } catch (MarketplaceWebServiceOrdersException ex) {
            // Exception properties are important for diagnostics.
            System.out.println("Service Exception:");
            ResponseHeaderMetadata rhmd = ex.getResponseHeaderMetadata();
            if(rhmd != null) {
                System.out.println("RequestId: "+rhmd.getRequestId());
                System.out.println("Timestamp: "+rhmd.getTimestamp());
            }
            System.out.println("Message: "+ex.getMessage());
            System.out.println("StatusCode: "+ex.getStatusCode());
            System.out.println("ErrorCode: "+ex.getErrorCode());
            System.out.println("ErrorType: "+ex.getErrorType());
            throw ex;
        }
    }

    /**
     *  Command line entry point.
     */
    public static void main(String[] args) {

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
        XMLGregorianCalendar createdAfter = MwsUtl.getDTF().newXMLGregorianCalendar();
        createdAfter.setYear(2017);
        createdAfter.setMonth(1);//1-12
        createdAfter.setDay(1);
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
        marketplaceId.add("A1F83G8C2ARO7P");
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
        Integer maxResultsPerPage = 1;//每页可返回的最多订单数。默认100
        request.setMaxResultsPerPage(maxResultsPerPage);
        List<String> tfmShipmentStatus = new ArrayList<String>();
        request.setTFMShipmentStatus(tfmShipmentStatus);

        // Make the call.
        ListOrdersApi.invokeListOrders(client, request);

    }

}
