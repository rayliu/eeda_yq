package interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import config.EedaConfig;

public class ActionCostInterceptor implements Interceptor {
    private Log logger = Log.getLog(ActionCostInterceptor.class);
  
    @Override
    public void intercept(Invocation ai) {
        Record rec = Db.findFirst("select * from t_sys_dict where code='check_wx_browser'");
        String checkWxFlag = rec.getStr("value");
        if("Y".equals(checkWxFlag) && !checkIsWxBrowser(ai))
            return;
        long start = System.currentTimeMillis();
        ai.invoke();
        long end = System.currentTimeMillis();
        long renderTime = end - start;
        logger.info(ai.getControllerKey()+"."+ai.getMethodName()+" action cost:"+renderTime+"ms");
        
    }

    private boolean checkIsWxBrowser(Invocation ai) {
        Controller c=ai.getController();
        String action=ai.getActionKey();
        if(action.indexOf("/app")>-1 && !EedaConfig.isLocalhost) {
            String userAgent = c.getRequest().getHeader("User-Agent")!=null?ai.getController().getRequest().getHeader("User-Agent").toLowerCase():"";
            boolean isWeiXin = false;
            if(!(userAgent.indexOf("micromessenger")>-1)){
                ai.getController().render("/lego_app/weiXinError.html");
                return false;
            }
        }
        return true;
    }

}
