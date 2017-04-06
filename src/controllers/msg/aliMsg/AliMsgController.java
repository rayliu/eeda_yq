package controllers.msg.aliMsg;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AliMsgController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(AliMsgController.class);

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

        render("/msg/aliMsg/aliMsgList.html");
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
        String sql = "select * from aliexpress_msg_relation A where office_id = "
                + user.getLong("office_id");

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by message_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void getMemberMsg() {
        long id = getParaToLong("id");
        long item_id = getParaToLong("item_id");
        // Record rec = Db.findById("ebay_member_msg", id);

        List<Record> rec = Db
                .find("select * from ebay_member_msg where item_id = ? ORDER BY creation_date",
                        item_id);

        renderJson(rec);
    }

    public void importMsg() {
        try {
            UserLogin user = LoginUserController.getLoginUser(this);
            long office_id = user.getLong("office_id");
            apiContext = new EbayApiContextUtil(office_id).getApiContext();
            List<Record> accountList = Db
                    .find("select * from aliexpress_seller_account where office_id=?",
                            office_id);
            for (Record setting : accountList) {
                long appKey = setting.getLong("client_id");
                String appSecret = setting.getStr("secret");
                String access_token = setting.getStr("access_token");
                String refresh_token = setting.getStr("refresh_token");

                Date access_token_timeout = setting
                        .getDate("access_token_timeout");
                Date now = new Date();
                System.out.println("access_token_timeout:"+access_token_timeout.toString());
                if (now.after(access_token_timeout)) {
                    String host = "gw.api.alibaba.com";
                    Map<String, String> token_params = new HashMap<String, String>();
                    token_params.put("client_id", String.valueOf(appKey));
                    token_params.put("client_secret", appSecret);
                    token_params.put("refresh_token", refresh_token);
                    String refreshTokenResult = AuthService.refreshToken(host,
                            token_params);
                    System.out.println("refreshTokenResult: "
                            + refreshTokenResult);
                    Map jsonObject = new Gson().fromJson(
                            refreshTokenResult.toString(), Map.class);

                    String accessToken = (String) jsonObject
                            .get("access_token");
                    Record setRec = Db
                            .findFirst(
                                    "select * from aliexpress_seller_account where office_id=? and client_id=?",
                                    office_id, appKey);
                    if (setRec != null) {
                        setRec.set("access_token", accessToken);
                        Calendar cd = Calendar.getInstance();
                        cd.add(Calendar.HOUR, 10);// 10小时后过期
                        setRec.set("access_token_timeout", cd.getTime());
                        Db.update("aliexpress_seller_account", setRec);
                    } else {
                        throw new Exception("not found setting");
                    }
                }
                AliMsgApi.queryMsgRelationList(appKey, appSecret, access_token,
                        refresh_token);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            renderText("Failed");
        }
        renderText("OK");
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
