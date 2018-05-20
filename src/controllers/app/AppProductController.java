package controllers.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
    	String product_id = getPara();
    	product_id = URLDecoder.decode(product_id, "UTF-8");
    	String limit = "";
    	String page_size = getPara("page_size");
    	String data_index = getPara("data_index");
    	if(StringUtils.isNotBlank(page_size)){
    		if(StringUtils.isBlank(data_index)){
    			data_index = "0";
    		}
    		limit = " limit " + data_index + ","+ page_size;
    	}
    	
    	//产品信息
    	List<Record> product = Db.find("SELECT pro.*,com.c_name shop_name,"
    			+ " cat.name category_name,"
    			+ " if(cu.id >0 ,'Y','N') cu,"
    			+ " if(cu.id >0 ,cu.content,'') cu_desc,"
    			+ " if(hui.is_active = 'Y' ,'Y','N') hui,"
    			+ " if(hui.is_active = 'Y' ,hui.discount,'') hui_discount"
    			+ " FROM wc_product pro"
    			+ " left join wc_company com on com.creator = pro.creator"
    			+ " left join category cat on cat.id = com.trade_type"
    			+ " left join wc_ad_cu cu on cu.creator = pro.creator"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " left join wc_ad_hui hui on hui.creator = pro.creator"
    			+ " WHERE pro.id = ?"
    			+ " group by pro.id",product_id);
    	//明细表信息
    	List<Record> productItemList = Db.find("SELECT * FROM wc_product_pic WHERE order_id = ?"+limit,product_id);
    	
    	
    	Record data = new Record();
    	data.set("product", product);
    	data.set("productItemList", productItemList);
        renderJson(data);  
    }
    
    public void picList(){
    	String product_id = getPara("prod_id");
    	String limit = "";
    	String page_size = getPara("page_size");
    	String data_index = getPara("data_index");
    	if(StringUtils.isNotBlank(page_size)){
    		if(StringUtils.isBlank(data_index)){
    			data_index = "0";
    		}
    		limit = " limit " + data_index + ","+ page_size;
    	}
    	
    	//明细表信息
    	List<Record> productItemList = Db.find("SELECT * FROM wc_product_pic WHERE order_id = ?" + limit,product_id);
    	
    	
    	Record data = new Record();
    	data.set("itemList", productItemList);
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
    
    
    public void get_more() throws UnsupportedEncodingException{
    	String shop_id = getPara();
    	shop_id = URLDecoder.decode(shop_id, "UTF-8");
    	//商家列表
    	List<Record> shopList = Db.find(" select wc.logo,ul.influence, "
    			+ " ifnull(wc.c_name,wc.company_name) company_name,"
    			+ " if(dio.id >0 ,'Y','N') diamond,"
    			+ " if(cu.id >0 ,'Y','N') cu,"
    			+ " if(hui.is_active = 'Y' ,'Y','N') hui,"
    			+ " ctg.name category_name,wc.address,wc.about"
    			+ " from user_login ul"
    			+ " left join wc_company wc on wc.creator = ul.id"
    			+ " left join category ctg on ctg.id = wc.trade_type"
    			+ " left join wc_ad_diamond dio on dio.creator = ul.id"
    			+ " and ((now() BETWEEN dio.begin_date and dio.end_date) and dio.status = '已开通')"
    			+ " left join wc_ad_cu cu on cu.creator = ul.id"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " left join wc_ad_hui hui on hui.creator = ul.id"
    			+ " where ul.id = ?",shop_id);
    	
    	
    	//产品信息
    	List<Record> productList = Db.find(" select pro.id, pro.cover, pro.name,pro.price,pro.cu_flag,"
    			+ " if(cu.id >0 ,'Y','N') user_cu"
    			+ " from wc_product pro"
    			+ " left join wc_ad_cu cu on cu.creator = pro.creator"
    			+ " and ((now() BETWEEN cu.begin_date and cu.end_date) and cu.status = '开启')"
    			+ " where pro.creator = ? and pro.is_active != 'N'"
    			+ " group by pro.id order by pro.id desc",shop_id);
    	
    	Record data = new Record();
    	data.set("shopList", shopList);
    	data.set("productList", productList);
        renderJson(data);  
    }
}
