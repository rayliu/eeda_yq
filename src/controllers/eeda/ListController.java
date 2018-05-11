package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.oms.jobOrder.JobOrderController;
//import controllers.oms.jobOrder.JobOrderService;
import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ListController extends Controller {
	private Log logger = Log.getLog(ListController.class);

    Subject currentUser = SecurityUtils.getSubject();

    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user == null)
   			return;
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_name = getPara(0);
        if("jobOrder".equals(module_name)){
            //JobOrderController c = new JobOrderController();
//            c.setHttpServletRequest(this.getRequest());
//            c.index();
            
            Map<String, String> templateMap = EedaMenuInterceptor.menuUrlTemplateCache.get(user_id);
            String templatePath = null;
            if(templateMap != null){
            	templatePath = templateMap.get("/list/"+module_name);
                if(StrKit.isBlank(templatePath)){
                    redirect("/");
                    return;
                }
            }
            
            render(templatePath);
        }else{
            String url = "list/"+module_name;
            Record rec = Db.findFirst("select p.* from permission p, eeda_modules m where "
                    + "p.module_id = m.id and m.office_id=? and p.url=?", office_id, url);
            if(rec ==null){
                redirect("/");
                return;
            }
            
            String templatePath = rec.getStr("template_path");
            if(StrKit.isBlank(templatePath)){
                redirect("/");
                return;
            }
            render(templatePath);
        }
    }
}
