package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import cache.EedaServiceCache;

//import cache.EedaServiceCache;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FormController extends Controller {
	private Log logger = Log.getLog(FormController.class);

    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    @SuppressWarnings("unchecked")
    public void index() {
        logger.debug("thread["+Thread.currentThread().getName()+
                "] -------------Eeda form---------------");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user == null)
   			return;
        long office_id = user.getLong("office_id");
        
        String module_name = getPara(0);
        
        //以下为表单的标准 action
        //add 跳转到新增页面; 
        //doAdd 新增动作
        //update 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        //doDelete 表单删除的动作
        String action = getPara(1);  
        logger.debug("-------------Eeda module:"+module_name+", action: "+action+"---------------");
        
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
        
        String err = "";
        Object returnObject = null;
        Object serviceInstance = null;
        try {
            Class c= Class.forName(serviceClass);
            serviceInstance = EedaServiceCache.getServiceInstance(serviceClass);
            
            //必须将request 传给service, 否则getPara 会取不到值
            setHttpSevletRequest(c, serviceInstance);
            
            Method method = c.getDeclaredMethod(action);//获取本类中的方法
            returnObject =  method.invoke(serviceInstance);//调用o对象的方法
        } catch (Exception ex) {
            ex.printStackTrace();
            err = ex.getMessage();
        }
        
        if(action.indexOf("do") == 0){
            if(err.length()==0){
                renderJson((Record)returnObject);
                return;
            }else{
                renderText(err);
                return;
            }
        }
        if("query".equals(action)){
            if(err.length()==0){
                renderJson((Map)returnObject);
                return;
            }else{
                renderText(err);
                return;
            }
        }
            String url = "form/"+module_name+"-"+action;
            Record recTemplate = Db.findFirst("select p.* from permission p, eeda_modules m where "
                    + "p.module_id = m.id and m.office_id=? and p.url=?", office_id, url);
            if(recTemplate ==null){
                redirect("/");
                return;
            }
            
            String templatePath = recTemplate.getStr("template_path");
            if(StrKit.isBlank(templatePath)){
                redirect("/");
                return;
            }
            render(templatePath);
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setHttpSevletRequest(Class c, Object o)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, SecurityException {
        Class superClass = c.getSuperclass();
        
        Field privateStringField =  superClass.getDeclaredField("request");
        privateStringField.setAccessible(true);
        privateStringField.set(o, this.getRequest());
    }

	
}
