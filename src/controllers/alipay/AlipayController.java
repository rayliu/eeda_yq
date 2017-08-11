package controllers.alipay;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;


public class AlipayController extends Controller {

    private Logger logger = Logger.getLogger(AlipayController.class);
    
    public void demo(){
        renderJsp("/alipay/index.jsp");
    }

    
}
