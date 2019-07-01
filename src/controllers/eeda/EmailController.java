package controllers.eeda;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.MailUtil;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

@Before({SetAttrLoginUserInterceptor.class,EedaMenuInterceptor.class})
public class EmailController extends Controller {
    private static final String RANDOM_CODE_KEY = "eeda";
	private Log logger = Log.getLog(EmailController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index(){
    	Record login_user = getAttr("login_user");
    	List<Record> emailList = Db.find("select * from email_setting where office_id = ?",login_user.getLong("office_id"));
    	for(Record email:emailList){
    		setAttr(email.getStr("type"), email);
    	}
    	render("/eeda/profile/email/email.html");
    }
    
    public void save(){
    	String params = getPara("params");
    	Gson gson = new Gson();
    	List<List<Map<String,Object>>> list = gson.fromJson(params,new TypeToken<List<List<Map<String,Object>>>>() { }.getType());
    	if(list.size()>0){
    		Record login_user = getAttr("login_user");
    		Db.update("delete from email_setting where office_id = ?",login_user.getLong("office_id"));
    		for(List<Map<String,Object>> i : list){
    			Record re = new Record();
        		for(Map<String,Object> map : i){
            		re.set((String)map.get("name"), map.get("value"));
            	}
        		re.set("office_id", login_user.getLong("office_id"));
        		Db.save("email_setting", re);
        	}
    	}
    	renderJson("{\"result\":"+true+"}");
    }
    
    /**
     * @param email_content 邮件内容
     * @param email_address	邮件地址
     * @param email_title	邮件标题
     */
    /**
     * 2019/6/28邮件测试
     */
  	public void sendTestEmailMsg(){
  		String type = getPara("type");
  		
  		Record login_user = getAttr("login_user");
    	Record emailRec = Db.findFirst("select * from email_setting where office_id = ? and type=?",
    			login_user.getLong("office_id"), type);
//    	LogKit.error(message);
  		String host = emailRec.getStr("host");//邮件服务器主机host，目前只支持SMTP协议(可以是163或者qq)pop3 smtp
		int port = Integer.valueOf(emailRec.getStr("port"));//邮件服务器端口 995
		String username = emailRec.getStr("name");//登录邮件服务器的账号
		String password = emailRec.getStr("pwd");//登录邮件服务器的密码，该密码通常是通过短信动态授权第三方登录的密码
		
		String mailFrom = "log@logclub.com";
		String to = getPara("to");
		
		MailUtil.sendMailForTest(host, port, username, password, mailFrom, to);
		renderText("ok");
		
  	}
}
