package controllers.app;

import java.io.IOException;
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
    			+ " where question_id = ?",question_id);
    	
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
    	String value = changeStr(getRequest().getHeader("questionValue"));
    	String user_id = getRequest().getHeader("userId");

    	Record question = new Record();
    	question.set("title", value);
    	question.set("create_time", new Date());
    	question.set("creator", user_id);
    	Db.save("wc_question", question);

    	Record data = new Record();
    	data.set("result", true);
        renderJson(data);  
    }
    
    public String changeStr(String ascii) {  
        int n = ascii.length() / 6;  
        StringBuilder sb = new StringBuilder(n);  
        for (int i = 0, j = 2; i < n; i++, j += 6) {  
            String code = ascii.substring(j, j + 4);  
            char ch = (char) Integer.parseInt(code, 16);  
            sb.append(ch);  
        }  
        return sb.toString();  
    }  
}
