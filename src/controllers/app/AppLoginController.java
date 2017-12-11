package controllers.app;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;
import sun.nio.cs.UnicodeEncoder;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.EedaHttpKit;
import controllers.util.MD5Util;

public class AppLoginController extends Controller {

    private Logger logger = Logger.getLogger(AppLoginController.class);
    
    /**
     * 问问
     * @throws IOException
     */
    public void askList() throws IOException{
    	String conditions = getRequest().getHeader("conditions");
    	
    	//商家列表
    	List<Record> askList = Db.find(""
    			+ " select wq.id id,wq.create_time,wq.title,wc.c_name shop_name,"
    			+ " cast((select count(0) from wc_response where question_id = wq.id) as char) answer_count"
    			+ " from wc_question wq"
    			+ " left join wc_company wc on wc.creator = wq.creator"
    			+ " order by wq.id desc");
    	
    	Record data = new Record();
    	data.set("askList", askList);
        renderJson(data);  
    }
    
    /**
     * 回复列表内容
     * @throws IOException
     */
    public void responseList() throws IOException{
    	String question_id = getRequest().getHeader("question_id");
    	
    	//商家列表
    	List<Record> responseList = Db.find(" select wr.id id,wr.create_time,wr.value,ul.user_name user_name"
    			+ " from wc_response wr"
    			+ " left join user_login ul on ul.id = wr.creator"
    			+ " where question_id = ?"
    			+ " order by wr.id desc",question_id);
    	
    	Record data = new Record();
    	data.set("responseList", responseList);
        renderJson(data);  
    }
    
    /**
     * 回复列表内容
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_register(){
    	
    	String invite_code = URLDecoder.decode(getRequest().getHeader("invite_code"));
    	String pwd = URLDecoder.decode(getRequest().getHeader("pwd"));
    	String mobile = URLDecoder.decode(getRequest().getHeader("mobile"));

    	Record user_login = new Record();
    	user_login.set("invitation_code", invite_code);
    	user_login.set("phone", mobile);
    	user_login.set("user_name", mobile);
    	user_login.set("system_type", "mobile");
    	user_login.set("password", pwd);
    	user_login.set("password_hint", pwd);
    	Db.save("user_login", user_login);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }
    
    @Before(Tx.class)
    public void login(){
    	boolean result = false;
    	String errMsg = null;
    	String user_id = null;
    	String password = URLDecoder.decode(getRequest().getHeader("password"));
    	String mobile = URLDecoder.decode(getRequest().getHeader("mobile"));

    	Record user = Db.findFirst("select * from user_login where phone = ? and password = ?",mobile, password);
    	if(user != null){
    		result = true;
    		user_id = user.getLong("id").toString();
    	}else{
    		errMsg = "用户名或密码不正确";
    	}
    	
    	Record data = new Record();
    	data.set("result", result);
    	data.set("user_id", user_id);
    	data.set("errMsg", errMsg);
        renderJson(data);  
    }
    
    @Before(Tx.class)
    public void save_answer(){
    	String value = EedaHttpKit.decodeHeadInfo(getRequest().getHeader("answerValue"));
    	String user_id = getRequest().getHeader("userId");
    	String question_id = getRequest().getHeader("questionId");

    	Record answer = new Record();
    	answer.set("value", value);
    	answer.set("question_id", question_id);
    	answer.set("create_time", new Date());
    	answer.set("creator", user_id);
    	Db.save("wc_response", answer);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }

}
