package controllers.app;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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

public class AppAskController extends Controller {

    private Logger logger = Logger.getLogger(AppAskController.class);
    
    /**
     * 问问
     * @throws IOException
     */
    public void askList() throws IOException{
    	String conditions = getRequest().getHeader("conditions");
    	
    	//商家列表
    	List<Record> askList = Db.find(""
    			+ " select wq.id id,wq.create_time,wq.title,ul.user_name login_name,"
    			+ " cast((select count(0) from wc_response where question_id = wq.id) as char) answer_count"
    			+ " from wc_question wq"
    			+ " left join user_login ul on ul.id = wq.creator"
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
    public void save_question(){
    	String value = EedaHttpKit.decodeHeadInfo(getRequest().getHeader("questionValue"));
    	String login_id = getRequest().getHeader("login_id");

    	Record question = new Record();
    	question.set("title", value);
    	question.set("create_time", new Date());
    	question.set("creator", login_id);
    	Db.save("wc_question", question);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }
    
    @Before(Tx.class)
    public void save_answer(){
    	String value = EedaHttpKit.decodeHeadInfo(getRequest().getHeader("answerValue"));
    	String login_id = getRequest().getHeader("login_id");
    	String question_id = getRequest().getHeader("questionId");

    	Record answer = new Record();
    	answer.set("value", value);
    	answer.set("question_id", question_id);
    	answer.set("create_time", new Date());
    	answer.set("creator", login_id);
    	Db.save("wc_response", answer);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }

}
