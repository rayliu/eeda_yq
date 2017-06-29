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
    
    public void queryPartNo(){
    	String part_no=getPara("part_no");
    	List<Record> list=Db.find("select part_no,count(1) total from gate_in"
    			+ " where part_no like '%"+part_no+"%'"
    			+ " and out_flag = 'N' and error_flag = 'N'"
    			+ " and office_id = 1 GROUP BY part_no");

    	renderJson(list);
    }
    
    public void showImg() throws IOException{
    	String part_no=getPara("part_no");
    	String total=getPara("total");
    	setAttr("part_no", part_no);
    	setAttr("total", total);
    	render("/wx/show.html");
    }
    
  
    
}
