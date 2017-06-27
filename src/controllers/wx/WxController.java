package controllers.wx;

import interceptor.JSSDKInterceptor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;

@Before(JSSDKInterceptor.class)
public class WxController extends Controller {

    private Logger logger = Logger.getLogger(WxController.class);
    
    public void demo() throws IOException{
        render("/wx/demo.html");
    }
    
    public void query() throws IOException{
        render("/wx/query.html");
    }
    
    public void showImg() throws IOException{
        render("/wx/show.html");
    }
    
  
    
}
