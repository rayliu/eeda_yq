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

public class AppProductController extends Controller {

    private Logger logger = Logger.getLogger(AppProductController.class);
    
    /**
     * 问问
     * @throws IOException
     */
    public void orderData() throws IOException{
    	String product_id = getRequest().getHeader("product_id");
    	
    	//产品信息
    	List<Record> product = Db.find("SELECT pro.*,com.c_name shop_name,cat.name category_name FROM wc_product pro"
    			+ " left join wc_company com on com.creator = pro.creator"
    			+ " left join category cat on cat.id = com.trade_type"
    			+ " WHERE pro.id = ?",product_id);
    	//明细表信息
    	List<Record> productItemList = Db.find("SELECT * FROM wc_product_pic WHERE order_id = ?",product_id);
    	
    	
    	Record data = new Record();
    	data.set("product", product);
    	data.set("productItemList", productItemList);
        renderJson(data);  
    }
    
    /**
     * 回复列表内容
     * @throws IOException
     */
    public void responseList() throws IOException{
    	String question_id = getRequest().getHeader("question_id");
    	
    	//商家列表
    	List<Record> responseList = Db.find(" select cast(wr.id as char) id,wr.create_time,wr.value,wc.c_name shop_name"
    			+ " from wc_response wr"
    			+ " left join wc_company wc on wc.creator = wr.creator"
    			+ " where question_id = ?",question_id);
    	
    	Record data = new Record();
    	data.set("responseList", responseList);
        renderJson(data);  
    }
}
