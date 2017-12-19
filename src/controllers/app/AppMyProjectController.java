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

public class AppMyProjectController extends Controller {

    private Logger logger = Logger.getLogger(AppMyProjectController.class);
    
    /**
     * 问问
     * @throws IOException
     */
    public void orderData() throws IOException{
    	String conditions = getRequest().getHeader("conditions");
    	String login_id = getRequest().getHeader("login_id");
    	
    	//商家列表
    	List<Record> orderList = Db.find("select * from wc_my_project");
    	for(Record re :orderList){
    		Long order_id = re.getLong("id");
    		List<Record> item = Db.find("select item.*,if(ref.id>0,'Y','N') is_check from wc_my_project_item item"
    				+ " left join wc_my_project_ref ref on ref.item_id = item.id and ref.user_id = ?"
    				+ " where item.order_id = ?",login_id, order_id);
    		
    		List<Record> checkItem = Db.find("select * from wc_my_project_ref ref"
    				+ " left join wc_my_project_item item on item.id = ref.item_id"
    				+ " where item.order_id = ? and ref.user_id = ?",order_id,login_id);
    		re.set("item_list", item);
    		re.set("check_item", checkItem);
    		
    	}
    	
    	Record data = new Record();
    	data.set("orderList", orderList);
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
