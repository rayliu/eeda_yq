package controllers.app;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.codec.Base64;

import sun.misc.BASE64Decoder;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

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
    	List<Record> askList = Db.find(" select wq.id id,wq.create_time,wq.title,wc.c_name shop_name"
    			+ " from wc_question wq"
    			+ " left join wc_company wc on wc.creator = wq.creator");
    	
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
    	List<Record> responseList = Db.find(" select wr.id id,wr.create_time,wr.value,wc.c_name shop_name"
    			+ " from wc_response wr"
    			+ " left join wc_company wc on wc.creator = wr.creator"
    			+ " where question_id = ?",question_id);
    	
    	Record data = new Record();
    	data.set("responseList", responseList);
        renderJson(data);  
    }
}
