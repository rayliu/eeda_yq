package controllers.alipay;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;


public class AlipayController extends Controller {

    private Logger logger = Logger.getLogger(AlipayController.class);
    
    public void demo(){
        renderJsp("/alipay/index.jsp");
    }

    public void ali_notify(){
    	String method = getRequest().getMethod();
        if("GET".equals(method)){
            redirect("/");
            return;
        }

        String out_trade_no = getPara("out_trade_no");
        logger.debug("out_trade_no ="+out_trade_no);
        Record rec = Db.findFirst("select * from wc_ad_diamond where id=?", out_trade_no);
        if(rec != null){
            String trade_status = getPara("trade_status");
            logger.debug("getPara(\"trade_status\") =" + trade_status);
            rec.set("trade_status", trade_status);
            Db.update("wc_ad_diamond", rec);
        }
        
    	redirect("/BusinessAdmin/ad/diamond");
        //renderText("notify...");
    }
    
    public void ali_return(){
        //renderText("return...");
    	redirect("/BusinessAdmin/ad/diamond");
    }
}
