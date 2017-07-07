package controllers.wx;

import interceptor.JSSDKInterceptor;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@Before(JSSDKInterceptor.class)
public class WxController extends Controller {

    private Logger logger = Logger.getLogger(WxController.class);
    
    public void demo() throws IOException{
        render("/wx/demo.html");
    }
    
    public void query() throws IOException{
        render("/wx/query.html");
    }
    
    public void queryItemNo(){
    	String item_no = getPara("item_no");
    	
    	List<Record> list = Db.find("select pro.item_no,pro.item_name,count(1) total,sum(gi.quantity) totalQuantity "
    			+ " from gate_in gi "
    			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
    			+ " where gi.office_id=1"
    			+ " and gi.out_flag = 'N' and gi.error_flag = 'N'"
    			+ " and pro.item_no like '%"+item_no+"%'" 
    			+ " group by pro.item_no");
    	
    	renderJson(list);
    }
    
    public void queryPartNo(){
    	String order_no = getPara("order_no");
    	String type = getPara("type");
    	
    	List<Record> list = null;
    	if("item_no".equals(type)){
    		list = Db.find("select *,count(1) total,sum(quantity) totalQuantity from ("
    				+ " select gi.part_no,gi.quantity from gate_in gi"
    				+ " left join wmsproduct pro on pro.part_no = gi.part_no"
        			+ " where pro.item_no like '%"+order_no+"%'"
        			+ " and gi.out_flag = 'N' and gi.error_flag = 'N'"
        			+ " and gi.office_id = 1 GROUP BY gi.id"
        			+ " union"
        			+ " select part_no,0 quantity "
        			+ " from wmsproduct"
        			+ " where office_id = 1 "
        			+ " and item_no like '%"+order_no+"%'"
        			+ " ) A group by A.part_no");
    	}else{
    		list = Db.find("select *,count(1) total,sum(quantity) totalQuantity from ("
    				+ " select part_no,quantity from gate_in"
        			+ " where part_no like '%"+order_no+"%'"
        			+ " and out_flag = 'N' and error_flag = 'N'"
        			+ " and office_id = 1 "
        			+ " group by id"
        			+ " union"
        			+ " select part_no,0 quantity "
        			+ " from wmsproduct"
        			+ " where office_id = 1 "
        			+ " and part_no like '%"+order_no+"%'"
        			+ " ) A group by A.part_no");
    	}
    	
    	renderJson(list);
    }
    
    public void showImg() throws IOException{
    	String part_no = getPara("part_no");
    	String total = getPara("total");
    	String totalQuantity = getPara("totalQuantity");
    	setAttr("part_no", part_no);
    	setAttr("total", total);
    	setAttr("totalQuantity", totalQuantity);
    	render("/wx/show.html");
    }
    
  
    
}
