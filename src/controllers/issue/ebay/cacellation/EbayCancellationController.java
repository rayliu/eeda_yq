package controllers.issue.ebay.cacellation;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class EbayCancellationController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(EbayCancellationController.class);

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

        render("/issue/cancellation/ebayCancellationList.html");
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
        String sql = "select * from ebay_cancellation A where office_id = "
                + user.getLong("office_id");

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by cancel_request_date desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }


    public void importCancellation() {
        try {
            UserLogin user = LoginUserController.getLoginUser(this);
            long office_id = user.getLong("office_id");
            
            String token ="";
            Record ebayAccount = Db.findFirst("select * from ebay_seller_account where type='ebay' and office_id=?", office_id);
            if(ebayAccount!=null){
                token=ebayAccount.getStr("token");
            }
            
            Request request = new Request.Builder()
            .url("https://api.ebay.com/post-order/v2/cancellation/search?creation_date_range_from=2017-02-01T00:00:00.000Z")
            .get()
            .addHeader("authorization", "TOKEN "+token)
            .addHeader("x-ebay-c-marketplace-id", "EBAY_US, EBAY_UK, EBAY_DE, EBAY_AU, EBAY_CA")
            .addHeader("content-type", "application/json")
            .addHeader("accept", "application/json")
            .build();
            Response response = OkHttpUtil.execute(request);
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
                handleResponse(responseStr, office_id);
            } else {
                throw new IOException("Unexpected code " + response);
            }
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            renderText("Failed");
        }
        renderText("OK");
    }

    private void handleResponse(String responseStr, long office_id) throws Exception{
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Map json = new Gson().fromJson(responseStr, Map.class);
        List<Map> list = (List<Map>) json.get("cancellations");
        for (Map map : list) {
            Record rec = new Record();
            String cancelId= map.get("cancelId").toString();
            rec.set("cancel_id", cancelId);
            rec.set("marketplace_id", map.get("marketplaceId"));
            rec.set("legacy_order_id", map.get("legacyOrderId"));
            rec.set("requestor_type", map.get("requestorType"));
            rec.set("cancel_reason", map.get("cancelReason"));
            rec.set("cancel_state", map.get("cancelState"));
            
            rec.set("cancel_status", map.get("cancelStatus"));
            rec.set("cancel_close_reason", map.get("cancelCloseReason"));
            rec.set("payment_status", map.get("paymentStatus"));
           
            Map requestRefundAmount = (Map)map.get("requestRefundAmount");
            rec.set("request_refund_amount", (Double)requestRefundAmount.get("value"));
            rec.set("request_refund_amount_currency", requestRefundAmount.get("currency"));
            
            Map cancelRequestDate = (Map)map.get("cancelRequestDate");
            String cancelRequestDateValue = (String) cancelRequestDate.get("value");
            Date d = sdf.parse(cancelRequestDateValue.replaceAll("Z$", "+0000"));
            rec.set("cancel_request_date", d);
            
            Map cancelCloseDate = (Map)map.get("cancelCloseDate");
            String cancelCloseDateValue = (String) cancelRequestDate.get("value");
            d = sdf.parse(cancelCloseDateValue.replaceAll("Z$", "+0000"));
            rec.set("cancel_close_date", d);
            
            rec.set("office_id", office_id);
            
            Record oldRec = Db.findFirst("select * from ebay_cancellation where cancel_id=?", cancelId);
            if(oldRec!=null){
                rec.set("id", oldRec.get("id"));
                Db.update("ebay_cancellation", rec);
            }else{
                Db.save("ebay_cancellation", rec);
            }
            
        }
    }
    
    public void replyCancellation() {
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
