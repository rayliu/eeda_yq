package controllers.msg.ebayMsg;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.call.AddMemberMessageRTQCall;
import com.ebay.sdk.call.GetMemberMessagesCall;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.MemberMessageExchangeType;
import com.ebay.soap.eBLBaseComponents.MemberMessageType;
import com.ebay.soap.eBLBaseComponents.MessageStatusTypeCodeType;
import com.ebay.soap.eBLBaseComponents.MessageTypeCodeType;
import com.ebay.soap.eBLBaseComponents.PaginationType;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.oms.ebaySalesOrder.EbayApiContextUtil;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class EbayMemberMsgController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(EbayMemberMsgController.class);

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
        String sql = "select * from ebay_seller_account where type='"+EbayApiContextUtil.configStr+"' and office_id = "+ user.getLong("office_id");
        List<Record> ebayAcountList = Db.find(sql);
        setAttr("ebayAcountList", ebayAcountList);

        render("/msg/ebayMemberMsg/ebayMemberMsgList.html");
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
        String sql = "select * from (select * from ebay_member_msg group by item_id, sender_id, subject, message_status) A where 1=1 ";

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " "
                + " order by message_status desc, creation_date desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void getMemberMsg(){
        long id = getParaToLong("id");
        String sender_id = getPara("sender_id");
        List<Record> recs = new ArrayList<Record>();
        if(getParaToLong("item_id")!=null){
            long item_id = getParaToLong("item_id");
            
            recs = Db.find("select * from (select id, item_id, message_type, sender_id,recipient_id ,"
            		+ " creation_date ,body, subject,'N' replay_flag, message_id, response, last_modified_date"
            		+ " from ebay_member_msg where sender_id=?"
            		+ " union "
            		+ " select id, item_id, '' message_type, sender_id, recipient_id, creation_date, body,"
                    + " subject, 'Y' replay_flag, 0 message_id, body response, creation_date last_modified_date"
            		+ " from ebay_member_msg_reply where recipient_id=?"
                    +") A "
            		+ " where item_id = ? ORDER BY creation_date", sender_id, sender_id, item_id);
        }else{
            recs = Db.find("select id, item_id, message_type, sender_id,recipient_id ,"
                    + " creation_date, body, subject, 'N' replay_flag, message_id, response, last_modified_date"
                    + " from ebay_member_msg emm where sender_id=?"
                    + " union "
                    + " select id, item_id, '' message_type, sender_id, recipient_id, creation_date, body,"
                    + " subject,'Y' replay_flag, 0 message_id, body response, creation_date last_modified_date"
                    + " from ebay_member_msg_reply where recipient_id=?", sender_id, sender_id);
            
        }
        renderJson(recs);
    }
    
    public void importMemberMsg() {
        try {
            UserLogin user = LoginUserController.getLoginUser(this);
            long office_id = user.getLong("office_id");
            apiContext = new EbayApiContextUtil(office_id).getApiContext();
            
            GetMemberMessagesCall api = new GetMemberMessagesCall(
                    this.apiContext);

            // api.setDisplayToPublic(new
            // Boolean(this.ckbDisplayToPublic.isSelected()));

            // Calendar cd =
            // GuiUtil.getCalendarFromField(this.txtEndCreationDate);
            // if (cd != null) {
            // api.setEndCreationTime(cd);
            // }

            // 必填项
            Calendar cd = Calendar.getInstance();
            cd.add(Calendar.DATE, -7);
            api.setStartCreationTime(cd);

            // String itemId = this.txtItemId.getText().trim();
            // if (itemId.length() > 0) {
            // api.setItemID(itemId);
            // }

            
            // api.setMessageStatus(MessageStatusTypeCodeType.ANSWERED);
             PaginationType pt = new PaginationType();
             pt.setEntriesPerPage(100);
             pt.setPageNumber(1);
             api.setPagination(pt);
            api.setMailMessageType(MessageTypeCodeType.ALL);
            DetailLevelCodeType[] detailLevel = { DetailLevelCodeType.RETURN_ALL };
            api.setDetailLevel(detailLevel);
            api.setMessageStatus(MessageStatusTypeCodeType.CUSTOM_CODE);//这里设置了才能获取所有回复
            api.setDisplayToPublic(false);
            MemberMessageExchangeType[] arrMessages = api.getMemberMessages();
            handleMsg(arrMessages);

        } catch (Exception ex) {
            ex.printStackTrace();
            Record rec = new Record();
            rec.set("status", "failed");
            rec.set("msg", ex.getMessage());
            renderJson(rec);
        }
        Record rec = new Record();
        rec.set("status", "OK");
        rec.set("msg", "OK");
        renderJson(rec);
    }

    private void handleMsg(MemberMessageExchangeType[] arrMessages) {
        
            int size = arrMessages != null ? arrMessages.length : 0;
            logger.debug("arrMessages.length="+arrMessages.length);
            for (int i = 0; i < size; i++) {
                Record rec = new Record();
                MemberMessageExchangeType msg = arrMessages[i];
                if(msg.getItem()!=null){
                    rec.set("item_id", msg.getItem().getItemID());
                    rec.set("seller_id", msg.getItem().getSeller().getUserID());
                    rec.set("selling_currency", msg.getItem().getSellingStatus().getCurrentPrice().getCurrencyID().value());
                    rec.set("selling_price", msg.getItem().getSellingStatus().getCurrentPrice().getValue());
                    rec.set("title", msg.getItem().getTitle());
                }
                
                rec.set("message_id", msg.getQuestion().getMessageID());
                rec.set("message_type", msg.getQuestion().getMessageType().value());
                rec.set("question_type", msg.getQuestion().getQuestionType().value());
                rec.set("display_to_public", msg.getQuestion().isDisplayToPublic()==true?"Y":"N");
                
                rec.set("sender_id", msg.getQuestion().getSenderID());
                rec.set("sender_email", msg.getQuestion().getSenderEmail());
                
                String rId = StringUtils.join(msg.getQuestion().getRecipientID(),",");
                rec.set("recipient_id", rId);//TODO

                rec.set("subject", msg.getQuestion().getSubject());
                rec.set("body", msg.getQuestion().getBody());
                
                if(msg.getResponse().length>0)
                    rec.set("response", msg.getResponse()[0]);//TODO
                
                rec.set("message_status", msg.getMessageStatus().value());
                rec.set("creation_date", msg.getCreationDate().getTime());
                rec.set("last_modified_date", msg.getLastModifiedDate().getTime());

                Record oldRec = Db.findFirst(
                        "select * from ebay_member_msg where message_id=?", msg
                                .getQuestion().getMessageID());
                if (oldRec != null) {
                    rec.set("id", oldRec.getLong("id"));
                    Db.update("ebay_member_msg", rec);
                } else {
                    Db.save("ebay_member_msg", rec);
                }
            }
        
    }

    
    public void replyMsg(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        apiContext = new EbayApiContextUtil(office_id).getApiContext();
        
        String msg_response = getPara("msg_response");
        String msg_id = getPara("msg_id");
        String item_id = getPara("item_id");
        String sender_id = getPara("sender_id");
        String subject = getPara("subject");
        String recipient_id = getPara("recipient_id");
        //Record rec = Db.findById("ebay_member_msg_replay", Long.valueOf(id));
        Record rec = new Record();
        rec.set("body", msg_response);
        rec.set("item_id", item_id);
        rec.set("recipient_id", sender_id);
        rec.set("sender_id", recipient_id);
        rec.set("subject", subject);
        rec.set("creation_date", new Date());
        Db.save("ebay_member_msg_reply", rec);
        
        AddMemberMessageRTQCall api = new AddMemberMessageRTQCall(
                this.apiContext);
        
        MemberMessageType mm = new MemberMessageType();
        mm.setBody(msg_response);
        mm.setParentMessageID(msg_id);
        String recipientId[] = new String[]{sender_id};
        mm.setRecipientID(recipientId);
        api.setMemberMessage(mm);
        api.setMessageID(msg_id);
        
        //TODO: 发送失败应该在UI上提示失败！
        try {
            api.addMemberMessageRTQ();
        } catch (Exception e) {
            e.printStackTrace();
            Record r = new Record();
            rec.set("status", "failed");
            rec.set("msg", e.getMessage());
            renderJson(r);
        }
        renderJson(rec);
    }
}
