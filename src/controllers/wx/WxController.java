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
    	
    	List<Record> list = Db.find("select *,sum(if(A.hav='Y',1,0)) total_box from("
    			+ " select gi.id, pro.item_no,pro.item_name,'Y' hav"
    			+ " from gate_in gi "
    			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
    			+ " where gi.office_id=1"
    			+ " and gi.out_flag = 'N' and gi.error_flag = 'N'"
    			+ " and pro.item_no like '%"+item_no+"%'" 
    			+ " union"
    			+ " select pro.id,pro.item_no,pro.item_name,'N' hav"
    			+ " from wmsproduct pro"
    			+ " where amount>0 and pro.office_id=1"
    			+ " and pro.item_no like '%"+item_no+"%'" 
    			+ " ) A group by A.item_no");
    	
//    	sql = "select * from(select pro.item_no,pro.item_name,'Y' flag "
//    			+ " from gate_in gi "
//    			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
//    			+ " where gi.office_id=1"
//    			+ " and out_flag = 'N' and error_flag = 'N'"
//    			+ condition 
//    			+ " union"
//    			+ " select pro.item_no,pro.item_name,'N' flag from wmsproduct pro"
//    			+ " where amount>0 and pro.office_id=1"
//    			+ proCondition 
//    			+ " ) A "
//    			+ " group by A.item_no";

    	
    	renderJson(list);
    }
    
    public void queryPartNo(){
    	String order_no = getPara("order_no");
    	String type = getPara("type");
    	
    	List<Record> list = null;
    	if("item_no".equals(type)){
    		list = Db.find("select *,count(IF(A.quantity = 0, null,A.id)) total,sum(quantity) totalQuantity from ("
    				+ " select gi.id,gi.part_no,pro.part_name,gi.quantity from gate_in gi"
    				+ " left join wmsproduct pro on pro.part_no = gi.part_no"
        			+ " where pro.item_no like '%"+order_no+"%'"
        			+ " and gi.out_flag = 'N' and gi.error_flag = 'N'"
        			+ " and gi.office_id = 1 GROUP BY gi.id"
        			+ " union"
        			+ " select id,part_no,part_name,0 quantity "
        			+ " from wmsproduct"
        			+ " where office_id = 1 "
        			+ " and item_no like '%"+order_no+"%'"
        			+ " ) A group by A.part_no");
    	}else{
    		list = Db.find("select *,count(IF (A.quantity = 0, null,A.id)) total,sum(quantity) totalQuantity from ("
    				+ " select gi.id,gi.part_no,pro.part_name,gi.quantity from gate_in gi"
    				+ " left join wmsproduct pro on gi.part_no=pro.part_no"
        			+ " where gi.part_no like '%"+order_no+"%'"
        			+ " and gi.out_flag = 'N' and gi.error_flag = 'N'"
        			+ " and gi.office_id = 1 "
        			+ " group by gi.id"
        			+ " union"
        			+ " select id, part_no,part_name,0 quantity "
        			+ " from wmsproduct"
        			+ " where office_id = 1 "
        			+ " and part_no like '%"+order_no+"%'"
        			+ " ) A group by A.part_no ");
    	}
    	
    	renderJson(list);
    }
    
    public void showImg() throws IOException{
    	String part_no = getPara("part_no");
    	String total = getPara("total");
    	String totalQuantity = getPara("totalQuantity");
    	String part_name = getPara("part_name");
    	setAttr("part_no", part_no);
    	setAttr("total", total);
    	setAttr("totalQuantity", totalQuantity);
    	setAttr("part_name", part_name);
    	render("/wx/show.html");
    }
    
  
    
}
