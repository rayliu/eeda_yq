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

import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrders;
import com.amazonservices.mws.orders._2013_09_01.MarketplaceWebServiceOrdersException;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsRequest;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResponse;
import com.amazonservices.mws.orders._2013_09_01.model.ListOrderItemsResult;
import com.amazonservices.mws.orders._2013_09_01.model.OrderItem;
import com.amazonservices.mws.orders._2013_09_01.model.ResponseHeaderMetadata;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/** Sample call for ListOrders. */
public class ListOrderItemsApi {

    /**
     * Call the service, log response and exceptions.
     *
     * @param client
     * @param request
     *
     * @return The response.
     */
    public static ListOrderItemsResponse invokeListOrderItems(
            MarketplaceWebServiceOrders client, ListOrderItemsRequest request) {
        try {
            // 在本项目中由于使用了xalan.jar里面实现了DocumentBuilder，
            // 本应该使用rt.jar里的DocumentBuilder
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
            // Call the service.
            ListOrderItemsResponse response = client.listOrderItems(request);
            
            ListOrderItemsResult itemsResult = response.getListOrderItemsResult();
            String amazonOrderId = itemsResult.getAmazonOrderId();
            List<OrderItem> itemList = itemsResult.getOrderItems();
            
            List<Record> recList = new ArrayList<Record>(itemList.size());
            for (OrderItem oItem : itemList) {
                Record rec = new Record();

                rec.set("amazon_order_id", amazonOrderId);
                rec.set("asin", oItem.getASIN());
                rec.set("order_item_id", oItem.getOrderItemId());
                rec.set("seller_sku", oItem.getSellerSKU());
                rec.set("title", oItem.getTitle());
                rec.set("quantity_ordered", oItem.getQuantityOrdered());
                rec.set("quantity_shipped", oItem.getQuantityShipped());
                
                Record oldRec = Db.findFirst("select * from amazon_sales_order_item where amazon_order_id=?", amazonOrderId);
                if(oldRec != null){
//                    rec.set("id", oldRec.getLong("id"));
//                    Db.update("amazon_sales_order_item", rec);
                }else{
                    Db.save("amazon_sales_order_item", rec);
                }
                
                recList.add(rec);
            }
            return response;
        } catch (MarketplaceWebServiceOrdersException ex) {
            // Exception properties are important for diagnostics.
            System.out.println("Service Exception:");
            ResponseHeaderMetadata rhmd = ex.getResponseHeaderMetadata();
            if (rhmd != null) {
                System.out.println("RequestId: " + rhmd.getRequestId());
                System.out.println("Timestamp: " + rhmd.getTimestamp());
            }
            System.out.println("Message: " + ex.getMessage());
            System.out.println("StatusCode: " + ex.getStatusCode());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            System.out.println("ErrorType: " + ex.getErrorType());
            throw ex;
        }
    }

}
