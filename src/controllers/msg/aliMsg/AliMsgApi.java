package controllers.msg.aliMsg;

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

public class AliMsgApi {

    public static List<Record> queryMsgRelationList(long appKey,
            String appSecret, String access_token, String refresh_token)
            throws IOException {
        List list = Collections.EMPTY_LIST;
        UserLogin user = LoginUserController.getLoginUser(null);
        long office_id = user.getLong("office_id");

        String urlPath = "param2/1/aliexpress.open/api.queryMsgRelationList/"
                + appKey;
        Map<String, String> params = new HashMap<String, String>();
        params.put("currentPage", "1");
        params.put("pageSize", "50");
        params.put("msgSources", "message_center");
        params.put("access_token", access_token);

        String signStr = CommonUtil.signatureWithParamsAndUrlPath(urlPath,
                params, appSecret);

        // Create an instance of HttpClient.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = "http://gw.api.alibaba.com:80/openapi/"
                + urlPath
                + "?currentPage=1&pageSize=50&msgSources=message_center"
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
            
            Date rightNow = new Date();// 20170327 224206000-0700
            DateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss'-0700'");
            List<Map> resultList = (List<Map>) jsonObject.get("result");
            for (Map map : resultList) {
                Record rec = new Record();
                rec.set("channel_id", map.get("channelId").toString());
                rec.set("child_id", map.get("childId"));
                rec.set("deal_stat", map.get("dealStat").toString());
                rec.set("last_message_content", map.get("lastMessageContent").toString());
                rec.set("last_message_id", map.get("lastMessageId"));
                rec.set("last_message_is_own", ((Boolean)map.get("lastMessageIsOwn"))==true?"Y":"N");
                
                Double time = (Double)map.get("messageTime");
                rec.set("message_time", new Date(time.longValue()));
                
                rec.set("other_login_id", (String)map.get("otherLoginId"));
                rec.set("other_name", (String)map.get("otherName"));
                rec.set("rank", (String)map.get("rank"));
                rec.set("read_stat", (String)map.get("readStat"));
                rec.set("unread_count", ((Double)map.get("unreadCount")).intValue());
                rec.set("office_id", office_id);

                //List<Record> itemList = buildItemList(map, rec);

                Record oldRec = Db
                        .findFirst(
                                "select * from aliexpress_msg_relation where channel_id=?",
                                map.get("channelId").toString());
                if (oldRec != null) {
                    rec.set("id", oldRec.get("id"));
                    Db.update("aliexpress_msg_relation", rec);
                } else {
                    Db.save("aliexpress_msg_relation", rec);
                }
                // 处理channel 的会话
                /*for (Record itemRec : itemList) {
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
                }*/
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
