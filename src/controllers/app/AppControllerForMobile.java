package controllers.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import sun.misc.BASE64Decoder;

import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.MD5Util;

//@RequiresAuthentication
//@Before(SetAttrLoginUserInterceptor.class)
public class AppControllerForMobile extends Controller {

    private Logger logger = Logger.getLogger(AppControllerForMobile.class);
    
    public void signIn() throws IOException{
        if (!checkHeaderAuth(getRequest())) {
            getResponse().setStatus(401);
            getResponse().setHeader("Cache-Control", "no-store");
            getResponse().setDateHeader("Expires", 0);
            getResponse().setHeader("WWW-authenticate", "Basic Realm=\"test\"");
            Record rec = new Record();
            rec.set("msg", "用户未登录!");
            renderJson(rec);
            return;
        }
        Record rec = new Record();
        rec.set("authKey", getRequest().getSession().getAttribute("authKey").toString());
        renderJson(rec);
        
    }
    
    public static boolean checkHeaderAuth(HttpServletRequest request)
            throws IOException {

        boolean isAuth =false;
        String auth = request.getHeader("Authorization");
        System.out.println("auth encoded in base64 is " + getFromBASE64(auth));

        if ((auth != null) && (auth.length() > 6)) {
            auth = auth.substring(6, auth.length());

            String decodedAuth = getFromBASE64(auth);
            System.out.println("auth decoded from base64 is " + decodedAuth);
            String[] authArr = decodedAuth.split(":");
            String sha1Pwd = MD5Util.encode("SHA1", authArr[1]);
            Record rec = Db.findFirst(
                    "select * from user_login where user_name=? and password=?",
                    authArr[0], sha1Pwd);
            
            if (rec != null) {
                request.getSession().setAttribute("authKey", auth);
                isAuth = true;
            } else {
                isAuth = false;
            }
        } else {
            isAuth = false;
        }
        
        
        return isAuth;
    }

    private static String getFromBASE64(String s) {
        if (s == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

    
}
