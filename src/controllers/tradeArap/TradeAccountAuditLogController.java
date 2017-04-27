package controllers.tradeArap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TradeAccountAuditLogController extends Controller {
    private Log logger = Log.getLog(TradeAccountAuditLogController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
       
    	List<ArapAccountAuditLog> list = ArapAccountAuditLog.dao.find("SELECT DISTINCT source_order FROM arap_account_audit_log");
    	setAttr("List", list);
    	
    	List<ArapAccountAuditLog> accountlist = ArapAccountAuditLog.dao.find("SELECT DISTINCT a.bank_name FROM arap_account_audit_log aaa left join fin_account a on a.id = aaa.account_id");
    	setAttr("accountList", accountlist);
    	render("/eeda/arap/AccountAuditLog/AccountAuditLogList.html");
    }

   
}
