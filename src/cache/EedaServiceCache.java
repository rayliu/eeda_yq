package cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.log.Log;

import controllers.eeda.FormController;

public class EedaServiceCache {
    private static Log logger = Log.getLog(EedaServiceCache.class);
    
    private static ThreadLocal<Object> serviceThreadLocal = new ThreadLocal<Object>();
    
    public static Object getServiceInstance(String serviceClass){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //如果serviceThreadLocal没有本线程对应的Service创建一个新的Service，
        //并将其保存到线程本地变量中。
        Object serviceInstance = null;
        if (serviceThreadLocal.get() == null) {
            try {
                Class c= loader.loadClass(serviceClass);//Class.forName(serviceClass);
                logger.debug("load service from new111: "+serviceClass);
                //获取类的默认构造器对象并通过它实例化  
                Constructor cons = c.getDeclaredConstructor((Class[])null);    
//                Car car = (Car)cons.newInstance();   
                serviceInstance = cons.newInstance();
                //必须将request 传给service, 否则getPara 会取不到值
//                    setHttpSevletRequest(c, serviceInstance);
                serviceThreadLocal.set(serviceInstance);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            serviceInstance = serviceThreadLocal.get();//直接返回线程本地变量
        }
        return serviceInstance;
    }
   
}
