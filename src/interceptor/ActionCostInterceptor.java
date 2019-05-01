package interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;

import config.EedaConfig;

public class ActionCostInterceptor implements Interceptor {
    private Log logger = Log.getLog(ActionCostInterceptor.class);
  
    @Override
    public void intercept(Invocation ai) {
        long start = System.currentTimeMillis();
        ai.invoke();
        long end = System.currentTimeMillis();
        long renderTime = end - start;
        logger.info(ai.getControllerKey()+"."+ai.getMethodName()+" action cost:"+renderTime+"ms");
        Controller c=ai.getController();
        String action=ai.getActionKey();
        if(action.indexOf("/app")>-1 && !EedaConfig.isLocalhost) {
            String userAgent = c.getRequest().getHeader("User-Agent")!=null?ai.getController().getRequest().getHeader("User-Agent").toLowerCase():"";
            boolean isWeiXin = false;
            if(!(userAgent.indexOf("micromessenger")>-1)){
                ai.getController().render("/lego_app/weiXinError.html");
                return;
            }
        }
    }

}
