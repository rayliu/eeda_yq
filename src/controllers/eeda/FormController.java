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
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_id = getPara(0);
        
        //以下为表单的标准 action
        //add 跳转到新增页面; 
        //doAdd 新增动作
        //edit 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        //doDelete 表单删除的动作
        String action = getPara(1);  
        logger.debug("-------------Eeda module:"+module_id+", action: "+action+"---------------");
        
        Record rec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(rec ==null){
            redirect("/");
            return;
        }
        
        if(action.indexOf("do") == -1){
            setAttr("form_content", rec.getStr("template_content"));
            render("/eeda/form/template.html");
        }else{
            renderJson();
        }
        
    }

    
}
