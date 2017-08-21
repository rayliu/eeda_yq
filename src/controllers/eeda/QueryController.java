package controllers.eeda;

import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import cache.EedaServiceCache;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class QueryController extends Controller {
	private Log logger = Log.getLog(QueryController.class);

    Subject currentUser = SecurityUtils.getSubject();
    
    
    @SuppressWarnings("unchecked")
    public void index() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_name = getPara(0);
        String action = getPara(1);
        
        Record rec = Db.findFirst("select * from eeda_service_mapping where "
                + " module_name=?", module_name);
        if(rec ==null){
            redirect("/");
            return;
        }
        
        String serviceClass = rec.getStr("service");
        if(StrKit.isBlank(serviceClass)){
            redirect("/");
            return;
        }
        
        
        Map jsonMap = null;
        Object serviceInstance = null;
        try {
             //通过类装载器获取对象
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class c= loader.loadClass(serviceClass);//Class.forName(serviceClass);
            
            logger.debug("load service: "+serviceClass);
            serviceInstance = EedaServiceCache.getServiceInstance(serviceClass);
             
            //必须将request 传给service, 否则getPara 会取不到值
            setHttpSevletRequest(c, serviceInstance);
            Method method = c.getDeclaredMethod(action);//获取本类中的方法
            jsonMap = (Map) method.invoke(serviceInstance);//调用o对象的方法
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        renderJson(jsonMap);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setHttpSevletRequest(Class c, Object o)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, SecurityException {
        logger.debug("load service Superclass: "+c.getGenericSuperclass());
        Class superClass = c.getSuperclass();
        
        Field privateStringField =  superClass.getDeclaredField("request");
        privateStringField.setAccessible(true);
        privateStringField.set(o, this.getRequest());
//        
//        Method[] methods = superClass.getMethods();
//        
//        for(Method m: methods){
//            logger.debug("Superclass Method: "+m.getName());
//        }
//        
//        Method[] d_methods = superClass.getDeclaredMethods();
//        
//        for(Method m: d_methods){
//            logger.debug("Superclass Declared Method: "+m.getName());
//        }
//        Class<HttpServletRequest>[] paramRequest = new Class[1];
//        paramRequest[0] = HttpServletRequest.class;
//        c.getDeclaredFields()
//        Method setRequestMethod = c.getMethod("setRequest", paramRequest);
//        setRequestMethod.invoke(o, this.getRequest());

    }

	
}
