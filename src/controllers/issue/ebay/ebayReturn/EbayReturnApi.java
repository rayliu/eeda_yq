package controllers.issue.ebay.ebayReturn;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.OkHttpUtil;

public class EbayReturnApi {

    public static void sendMessage(long returnId, String msg, long office_id)
            throws IOException {
        String token ="";
        Record ebayAccount = Db.findFirst("select * from ebay_seller_account where type='ebay' and office_id=?", office_id);
        if(ebayAccount!=null){
            token=ebayAccount.getStr("token");
        }
        
        String jsonInString = "{'message' : {'content': '"+msg+"'}}";
        
        RequestBody body = RequestBody.create(MediaType
                .parse("application/json"), jsonInString);
        
        Request request = new Request.Builder()
        .url("https://api.ebay.com/post-order/v2/return/"+returnId+"/send_message")
        .post(body)
        .addHeader("authorization", "TOKEN "+token)
        .addHeader("content-type", "application/json")
        .addHeader("x-ebay-c-marketplace-id", "EBAY_US, EBAY_UK, EBAY_DE, EBAY_AU, EBAY_CA")
        .addHeader("accept", "application/json")
        .build();
        Response response = OkHttpUtil.execute(request);
        if (response.isSuccessful()) {
            
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

}
