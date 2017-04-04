package controllers.oms.aliexpressSalesOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.ali.CommonUtil;

public class ListOrdersApi {

    public static List<Record> findOrderListQuery(long appKey,
            String appSecret, String access_token, String refresh_token)
            throws IOException {
        List list = Collections.EMPTY_LIST;
        UserLogin user = LoginUserController.getLoginUser(null);
        long office_id = user.getLong("office_id");

        String urlPath = "param2/1/aliexpress.open/api.findOrderListQuery/"
                + appKey;
        Map<String, String> params = new HashMap<String, String>();
        params.put("page", "1");
        params.put("pageSize", "50");
        params.put("access_token", access_token);

        String signStr = CommonUtil.signatureWithParamsAndUrlPath(urlPath,
                params, appSecret);

        // Create an instance of HttpClient.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = "http://gw.api.alibaba.com:80/openapi/"
                + urlPath
                + "?page=1&pageSize=50"
                + "&access_token="+access_token
                + "&_aop_signature="+signStr;
        System.out.println(url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpclient.execute(httpGet);
            System.out.println(response.getStatusLine());
            HttpEntity entity1 = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            System.out.println(result);
            Map jsonObject = new Gson().fromJson(result.toString(), Map.class);
            
            if(jsonObject.get("exception")!=null){
                throw new Exception(jsonObject.get("exception").toString());
            }
            System.out.println("totalItem = "
                    + jsonObject.get("totalItem").toString());
            Date rightNow = new Date();// 20170327 224206000-0700
            DateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss'-0700'");
            List<Map> orderList = (List<Map>) jsonObject.get("orderList");
            for (Map map : orderList) {
                Record rec = new Record();
                rec.set("biz_type", map.get("bizType").toString());
                rec.set("order_id", map.get("orderId").toString());
                rec.set("buyer_login_id", map.get("buyerLoginId").toString());
                rec.set("buyer_name", map.get("buyerSignerFullname").toString());
                rec.set("order_status", map.get("orderStatus").toString());
                Map payAmount = (Map) map.get("payAmount");
                rec.set("pay_amount", (Double) payAmount.get("amount"));
                rec.set("pay_currency_code",
                        (String) payAmount.get("currencyCode"));
                if (map.get("memo") != null)
                    rec.set("memo", map.get("memo").toString());

                Date d = sdf.parse(map.get("gmtCreate").toString());
                rec.set("create_time", d);
                System.out.println("gmtCreate Date=" + d + "...");
                rec.set("office_id", office_id);

                List<Record> itemList = buildItemList(map, rec);

                Record oldRec = Db
                        .findFirst(
                                "select * from aliexpress_sales_order where order_id=?",
                                map.get("orderId").toString());
                if (oldRec != null) {
                    rec.set("id", oldRec.get("id"));
                    Db.update("aliexpress_sales_order", rec);
                } else {
                    Db.save("aliexpress_sales_order", rec);
                }
                // 处理单据中的item
                for (Record itemRec : itemList) {
                    Record oldItemRec = Db
                            .findFirst(
                                    "select * from aliexpress_sales_order_product where child_id=?",
                                    itemRec.get("child_id"));
                    if (oldItemRec != null) {
                        itemRec.set("id", oldItemRec.get("id"));
                        Db.update("aliexpress_sales_order_product", itemRec);
                    } else {
                        Db.save("aliexpress_sales_order_product", itemRec);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            response.close();
        }
        return list;
    }

    private static List buildItemList(Map map, Record rec) {
        List itemList = new ArrayList();
        List<Object> productList = (List) map.get("productList");
        for (Object product : productList) {
            Map item = (Map) product;
            Record itemRec = new Record();
            itemRec.set("child_id", item.get("childId"));
            itemRec.set("order_id", item.get("orderId"));
            itemRec.set("logistics_service_name",
                    item.get("logisticsServiceName"));

            Map logAmount = (Map) map.get("logisticsAmount");
            if (logAmount != null) {
                rec.set("logistics_amount", (Double) logAmount.get("amount"));
                rec.set("logistics_amount_currency_code",
                        (String) logAmount.get("currencyCode"));
            }
            itemRec.set("product_count", item.get("productCount"));
            itemRec.set("product_id", item.get("productId"));
            itemRec.set("product_img_url", item.get("productImgUrl"));
            itemRec.set("product_name", item.get("productName"));
            itemRec.set("product_snap_url", item.get("productSnapUrl"));
            itemRec.set("product_unit", item.get("productUnit"));

            Map productUnitPrice = (Map) map.get("productUnitPrice");
            if (productUnitPrice != null) {
                rec.set("product_unit_price",
                        (Double) productUnitPrice.get("amount"));
                rec.set("product_unit_price_currency_code",
                        (String) productUnitPrice.get("currencyCode"));
            }
            itemRec.set("delivery_time", item.get("deliveryTime"));
            itemRec.set("logistics_type", item.get("logisticsType"));
            itemRec.set("memo", item.get("memo"));

            itemList.add(itemRec);
        }
        return itemList;
    }

}
