package controllers.msg.ebayMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.call.AddMemberMessageRTQCall;
import com.ebay.soap.eBLBaseComponents.MemberMessageType;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;

//@RequiresAuthentication

public class MsgController4App extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(MsgController4App.class);

   
    public void list() {
//        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = 5;//user.getLong("office_id");

        String type = getPara("type");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }else{
            sLimit = " LIMIT 0, 20";
        }
        String sql = "select * from ebay_member_msg where 1=1 ";

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by message_status desc, creation_date desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void replyMsg(){
        String parent_msg_id = getPara("msg_id");
        String response = getPara("response");
        String recipientID = getPara("recipient_id");
        Record rec = Db.findFirst("select * from ebay_member_msg where message_id=?", Long.valueOf(parent_msg_id));
        if(rec!=null){
            rec.set("response", response);
            Db.update("ebay_member_msg", rec);
        }else{
            renderText("No rec found.");
        }
        AddMemberMessageRTQCall api = new AddMemberMessageRTQCall(
                this.apiContext);
        
        MemberMessageType mm = new MemberMessageType();
        mm.setBody(response);
        mm.setParentMessageID(parent_msg_id);
        String recipientId[] = new String[]{recipientID};
        mm.setRecipientID(recipientId);
        api.setMemberMessage(mm);
        
//        try {
//            api.addMemberMessageRTQ();
//        } catch (Exception e) {
//            e.printStackTrace();
//            renderText("Failed");
//        }
        renderText("OK");
    }

}
