package controllers.issue.ebay.ebayReturn;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.call.AddMemberMessageRTQCall;
import com.ebay.sdk.call.GetMemberMessagesCall;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.MemberMessageExchangeType;
import com.ebay.soap.eBLBaseComponents.MemberMessageType;
import com.ebay.soap.eBLBaseComponents.MessageTypeCodeType;
import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.oms.ebaySalesOrder.EbayApiContextUtil;
import controllers.profile.LoginUserController;
import controllers.profile.ali.AuthService;
import controllers.util.DbUtils;
import controllers.util.OkHttpUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class EbayReturnController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(EbayReturnController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() {

        // Record orderNopayRec =
        // Db.findFirst("select count(1) total from ebay_member_msg where paid_time is null");
        // setAttr("orderNopayCount", orderNopayRec.get("total"));
        //
        // Record orderNoshipRec =
        // Db.findFirst("select count(1) total from ebay_member_msg where shipped_time is null");
        // setAttr("orderNoshipCount", orderNoshipRec.get("total"));
        UserLogin user = LoginUserController.getLoginUser(this);
        String sql = "select * from ebay_seller_account where type='"
                + EbayApiContextUtil.configStr + "' and office_id = "
                + user.getLong("office_id");
        List<Record> ebayAcountList = Db.find(sql);
        setAttr("ebayAcountList", ebayAcountList);

        render("/issue/ebay/return/ebayReturnList.html");
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
        String sql = "select * from ebay_issue_return A where office_id = "
                + user.getLong("office_id");

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by creation_date desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void getMsgHistory() {
        String return_id = getPara("return_id");
        List<Record> rec = Db
                .find("select * from ebay_issue_return_response_history where return_id = ? ORDER BY creation_date",
                        return_id);

        renderJson(rec);
    }

    public void importReturn() {
        try {
            UserLogin user = LoginUserController.getLoginUser(this);
            long office_id = user.getLong("office_id");
            
            String token ="";
            Record ebayAccount = Db.findFirst("select * from ebay_seller_account where type='ebay' and office_id=?", office_id);
            if(ebayAccount!=null){
                token=ebayAccount.getStr("token");
            }
            
            Request request = new Request.Builder()
            .url("https://api.ebay.com/post-order/v2/return/search")
            .get()
            .addHeader("authorization", "TOKEN "+token)
            .addHeader("x-ebay-c-marketplace-id", "EBAY_US, EBAY_UK, EBAY_DE, EBAY_AU, EBAY_CA")
            .addHeader("content-type", "application/json")
            .addHeader("accept", "application/json")
            .build();
            Response response = OkHttpUtil.execute(request);
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
                handleResponse(responseStr, office_id, token);
            } else {
                throw new IOException("Unexpected code " + response);
            }
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            renderText("Failed");
        }
        renderText("OK");
    }

    private void handleResponse(String responseStr, long office_id, String token) throws Exception{
        List<String> returnIdList = new ArrayList<String>();
        
        Date rightNow = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Map json = new Gson().fromJson(responseStr, Map.class);
        List<Map> list = (List<Map>) json.get("members");
        for (Map map : list) {
            Record rec = new Record();
            String returnId= map.get("returnId").toString();
            rec.set("return_id", returnId);
            rec.set("buyer_login_name", map.get("buyerLoginName"));
            rec.set("seller_login_name", map.get("sellerLoginName"));
            rec.set("current_type", map.get("currentType"));
            rec.set("state", map.get("state"));
            rec.set("status", map.get("status"));
            Map creationInfo = (Map) map.get("creationInfo");
            Map item = (Map)creationInfo.get("item");
            rec.set("item_id", item.get("itemId"));
            rec.set("transaction_id", item.get("transactionId"));
            rec.set("return_quantity", item.get("returnQuantity"));
            rec.set("item_title", item.get("itemTitle"));
            
            rec.set("type", creationInfo.get("type"));
            rec.set("reason", creationInfo.get("reason"));
            Map comments = (Map)creationInfo.get("comments");
            rec.set("comments", comments.get("content"));
            
            Map creationDate = (Map)creationInfo.get("creationDate");
            String dateValue = (String) creationDate.get("value");
            Date d = sdf.parse(dateValue.replaceAll("Z$", "+0000"));
            rec.set("creation_date", d);
            
            Map sellerTotalRefund = (Map)map.get("sellerTotalRefund");
            Map estimatedRefundAmount = (Map)sellerTotalRefund.get("estimatedRefundAmount");
            rec.set("seller_estimate_total_refund", (Double)estimatedRefundAmount.get("value"));
            rec.set("seller_estimate_total_refund_currency", estimatedRefundAmount.get("currency"));
            
            Map actualRefundAmount = (Map)sellerTotalRefund.get("actualRefundAmount");
            if(actualRefundAmount!=null){
                rec.set("seller_actual_total_refund", (Double)actualRefundAmount.get("value"));
                rec.set("seller_actual_total_refund_currency", actualRefundAmount.get("currency"));
            }
            /*
            Map buyerTotalRefund = (Map)map.get("buyerTotalRefund");
            Map buyerEstimatedRefundAmount = (Map)buyerTotalRefund.get("estimatedRefundAmount");
            rec.set("buyer_estimate_total_refund", (Double)buyerEstimatedRefundAmount.get("value"));
            rec.set("buyer_estimate_total_refund_currency", buyerEstimatedRefundAmount.get("currency"));
            
            Map buyerActualRefundAmount = (Map)buyerTotalRefund.get("actualRefundAmount");
            if(buyerActualRefundAmount!=null){
                rec.set("buyer_actual_total_refund", (Double)buyerActualRefundAmount.get("value"));
                rec.set("buyer_actual_total_refund_currency", buyerActualRefundAmount.get("currency"));
            }
            */
            Map sellerResponseDue = (Map)map.get("sellerResponseDue");
            if(sellerResponseDue!=null){
                rec.set("seller_response_activity", (String)sellerResponseDue.get("activityDue"));
                Map respondByDate = (Map)sellerResponseDue.get("respondByDate");
                String respondByDateStr = respondByDate.get("value").toString().replaceAll("Z$", "+0000");
                d = sdf.parse(respondByDateStr);
                rec.set("seller_response_date", d);
            }
            
            Map buyerResponseDue = (Map)map.get("buyerResponseDue");
            if(buyerResponseDue!=null){
                rec.set("buyer_response_activity", (String)buyerResponseDue.get("activityDue"));
                Map respondByDate = (Map)sellerResponseDue.get("respondByDate");
                String respondByDateStr = respondByDate.get("value").toString().replaceAll("Z$", "+0000");
                d = sdf.parse(respondByDateStr);
                rec.set("buyer_response_date", d);
            }
            rec.set("office_id", office_id);
            
            Record oldRec = Db.findFirst("select * from ebay_issue_return where return_id=?", returnId);
            if(oldRec!=null){
                rec.set("id", oldRec.get("id"));
                Db.update("ebay_issue_return", rec);
            }else{
                Db.save("ebay_issue_return", rec);
            }
            returnIdList.add(returnId);
        }
        //更新对话记录
        handleResponseHistory(returnIdList, token);
    }
    
    private void handleResponseHistory(List<String> returnIdList, String token) throws Exception{
        for (String returnId : returnIdList) {
            Request request = new Request.Builder()
            .url("https://api.ebay.com/post-order/v2/return/"+returnId)
            .get()
            .addHeader("authorization", "TOKEN "+token)
            .addHeader("x-ebay-c-marketplace-id", "EBAY_US, EBAY_UK, EBAY_DE, EBAY_AU, EBAY_CA")
            .addHeader("content-type", "application/json")
            .addHeader("accept", "application/json")
            .build();
            Response response = OkHttpUtil.execute(request);
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
                proccessResponse(returnId, responseStr);
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
        
    }
    
    private void proccessResponse(String returnId, String responseStr) throws Exception{
        Date rightNow = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Map json = new Gson().fromJson(responseStr, Map.class);
       
        Map detail = (Map) json.get("detail");
        
        List<Map> list = (List<Map>) detail.get("responseHistory");
        int msgCount = list.size();
        
        Record oldRec = Db.findFirst("select count(*) count from ebay_issue_return_response_history where return_id=?", returnId);
        int msgDbCount = oldRec.getLong("count").intValue();
        //response条数相等就不用更新了
        if(msgCount<=msgDbCount)
            return;
        
        Db.update("delete from ebay_issue_return_response_history where return_id=?", returnId);
        
        for (Map hMap : list) {
            Record rec = new Record();
            rec.set("return_id", returnId);
            rec.set("author", hMap.get("author"));
            rec.set("activity", hMap.get("activity"));
            rec.set("from_state", hMap.get("fromState"));
            rec.set("to_state", hMap.get("toState"));
            
            Map creationDate = (Map)hMap.get("creationDate");
            String creationDateStr = creationDate.get("value").toString().replaceAll("Z$", "+0000");
            Date d = sdf.parse(creationDateStr);
            rec.set("creation_date", d);
            
            rec.set("notes", hMap.get("notes"));
            
            Map attributes = (Map)hMap.get("attributes");
            if(attributes!=null){
                Map sellerReturnAddress = (Map)attributes.get("sellerReturnAddress");
                if(sellerReturnAddress!=null){
                    rec.set("seller_return_address_name", sellerReturnAddress.get("name"));
                    
                    Map address = (Map)sellerReturnAddress.get("address");
                    rec.set("seller_return_address_line1", address.get("addressLine1"));
                    rec.set("seller_return_address_line2", address.get("addressLine2"));
                    rec.set("seller_return_address_city", address.get("city"));
                    rec.set("seller_return_address_postal_code", address.get("postalCode"));
                    rec.set("seller_return_address_country", address.get("country"));
                }
            }
            
            Db.save("ebay_issue_return_response_history", rec);
            
        }
    }
    
    public void replyMsg() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        apiContext = new EbayApiContextUtil(office_id).getApiContext();

        String id = getPara("id");
        String msg_id = getPara("msg_id");
        String response = getPara("response");
        String recipientID = getPara("recipient_id");
        Record rec = Db.findById("ebay_member_msg", Long.valueOf(id));
        rec.set("response", response);
        Db.update("ebay_member_msg", rec);

        AddMemberMessageRTQCall api = new AddMemberMessageRTQCall(
                this.apiContext);

        MemberMessageType mm = new MemberMessageType();
        mm.setBody(response);
        mm.setParentMessageID(msg_id);
        String recipientId[] = new String[] { recipientID };
        mm.setRecipientID(recipientId);
        api.setMemberMessage(mm);
        api.setMessageID(msg_id);
        try {
            api.addMemberMessageRTQ();
        } catch (Exception e) {
            e.printStackTrace();
            renderText("Failed");
        }
        renderJson(rec);
    }
}
