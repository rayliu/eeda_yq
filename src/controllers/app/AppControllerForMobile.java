package controllers.app;

import java.io.IOException;

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
    
    private boolean checkHeaderAuth(HttpServletRequest request)
            throws IOException {

        String auth = request.getHeader("Authorization");
        System.out.println("auth encoded in base64 is " + Base64.decodeToString(auth));

        if ((auth != null) && (auth.length() > 6)) {
            auth = auth.substring(6, auth.length());

            String decodedAuth = Base64.decodeToString(auth);
            System.out.println("auth decoded from base64 is " + decodedAuth);
            String[] authArr = decodedAuth.split(":");
            String sha1Pwd = MD5Util.encode("SHA1", authArr[1]);
            Record rec = Db.findFirst(
                    "select * from user_login where user_name=? and password=?",
                    authArr[0], sha1Pwd);
            
            if (rec != null) {
                request.getSession().setAttribute("authKey", auth);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public void index(){//app端的首页
        render("/lego_app/home.html");
    }
    public void mainMenu(){//首页应该是一个9宫格，或是统计数据
        render("/lego_app/login.html");
    }
    
    public void login(){
        render("/lego_app/login.html");
    }
    
    public void setting(){
        render("/lego_app/setting.html");
    }
}
