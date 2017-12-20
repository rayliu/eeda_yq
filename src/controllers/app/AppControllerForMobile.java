package controllers.app;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.MD5Util;

//@RequiresAuthentication
//@Before(SetAttrLoginUserInterceptor.class)
public class AppControllerForMobile extends Controller {

    private Logger logger = Logger.getLogger(AppControllerForMobile.class);
    
    public void signIn() throws IOException{
        Record returnData = new Record();
        Record rec = checkHeaderAuth(getRequest());
        if (rec == null) {
            getResponse().setStatus(401);
            getResponse().setHeader("Cache-Control", "no-store");
            getResponse().setDateHeader("Expires", 0);
            getResponse().setHeader("WWW-authenticate", "Basic Realm=\"test\"");
            
            String errMsg = "用户名或密码不正确";
            returnData.set("errMsg", errMsg);
            renderJson(returnData);
        }else{
            returnData.set("authKey", getRequest().getSession().getAttribute("authKey").toString());
            returnData.set("result", true);
            returnData.set("login_id", rec.get("id"));
            returnData.set("wedding_date", rec.get("wedding_date"));
            returnData.set("user_name", rec.get("user_name"));
            renderJson(returnData);  
        }
    }
    
    private Record checkHeaderAuth(HttpServletRequest request)
            throws IOException {

        String auth = request.getHeader("Authorization");
        System.out.println("auth encoded in base64 is " + URLDecoder.decode(auth, "UTF-8"));
        Record rec = null;
        if ((auth != null) && (auth.length() > 6)) {
            auth = auth.substring(6, auth.length());

            String decodedAuth = Base64.decodeToString(auth);
            System.out.println("auth decoded from base64 is " + decodedAuth);
            String[] authArr = decodedAuth.split(":");
            String phone = authArr[0];
            String sha1Pwd = MD5Util.encode("SHA1",authArr[1]);
            rec = Db.findFirst(
                    "select * from user_login where phone=? and password=?",
                    authArr[0], sha1Pwd);
            
            if (rec != null) {
                request.getSession().setAttribute("authKey", auth);
            } 
        } 
        return rec;

    }

    
}
