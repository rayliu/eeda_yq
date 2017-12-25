package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
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
     * 筹备项目
     * @throws IOException
     */
    public void orderData() throws IOException{
//    	String conditions = getRequest().getHeader("conditions");
    	String login_id = getPara();
    	
    	if(StringUtils.isNotBlank(login_id)){
    		login_id = URLDecoder.decode(login_id, "UTF-8");
    	}
    	
    	//筹备列表
    	List<Record> orderList = Db.find("select * from wc_my_project");
    	for(Record re :orderList){
    		Long order_id = re.getLong("id");
    		List<Record> item = Db.find("select item.*,if(ref.id>0,'Y','N') is_check,ref.complete_date new_complete_date from wc_my_project_item item"
    				+ " left join wc_my_project_ref ref on ref.item_id = item.id and ref.user_id = ?"
    				+ " where item.order_id = ?",login_id, order_id);
    		
    		List<Record> checkItem = Db.find("select ref.id from wc_my_project_ref ref"
    				+ " left join wc_my_project_item item on item.id = ref.item_id"
    				+ " where item.order_id = ? and ref.user_id = ?"
    				+ " group by ref.id",order_id,login_id);
    		re.set("item_list", item);
    		re.set("check_item", checkItem);
    		
    	}
    	
    	Record data = new Record();
    	data.set("orderList", orderList);
        renderJson(data);  
    }
    
    /**
     * 设置日期
     * @throws UnsupportedEncodingException 
     * @throws IOException
     */
    @Before(Tx.class)
    public void save_date() throws UnsupportedEncodingException{
    	String user_id = URLDecoder.decode(getPara("user_id"), "UTF-8");
    	String item_id = URLDecoder.decode(getPara("item_id"), "UTF-8");
    	String is_check = URLDecoder.decode(getPara("is_check"), "UTF-8");
    	String complete_date = URLDecoder.decode(getPara("complete_date"), "UTF-8");

    	Record return_data = new Record();
        Record rec = Db.findFirst("select * from wc_my_project_ref where user_id="+user_id+" and item_id="+ item_id);
        if(rec != null){
        	rec.set("complete_date", complete_date);
        	Db.update("wc_my_project_ref", rec);
        }
        return_data.set("result", true);

        renderJson(return_data);  
    }
    
    @Before(Tx.class)
    public void save_check() throws UnsupportedEncodingException{
    	String user_id = URLDecoder.decode(getPara("user_id"), "UTF-8");
    	String item_id = URLDecoder.decode(getPara("item_id"), "UTF-8");
    	String is_check = URLDecoder.decode(getPara("is_check"), "UTF-8");
    	String complete_date = URLDecoder.decode(getPara("complete_date"), "UTF-8");

        Record return_data = new Record();
        Record rec = Db.findFirst("select * from wc_my_project_ref where user_id="+user_id+" and item_id="+ item_id);
        if(rec != null){
        	if("N".equals(is_check)){
        		Db.delete("wc_my_project_ref", rec);
        	}
        }else{
        	if("Y".equals(is_check)){
	        	rec = new Record();
	            rec.set("item_id", item_id);
	            rec.set("user_id", user_id);
	            if(StringUtils.isNotBlank(complete_date)){
	                rec.set("complete_date", complete_date);
	            }
	            rec.set("user_id", user_id);
	            Db.save("wc_my_project_ref", rec);
        	}
        }
        return_data.set("result", true);

        renderJson(return_data);  
    }

}
