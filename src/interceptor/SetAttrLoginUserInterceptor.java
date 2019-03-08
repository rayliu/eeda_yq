package interceptor;

import javax.servlet.http.HttpServletRequest;

import models.Office;
import models.UserLogin;
import models.UserOffice;
import models.eeda.profile.OfficeConfig;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class SetAttrLoginUserInterceptor implements Interceptor{
	private Log logger = Log.getLog(SetAttrLoginUserInterceptor.class);
	@Override
	public void intercept(Invocation ai) {
		Subject currentUser = SecurityUtils.getSubject();
		if(currentUser.isAuthenticated()){
			Record login_user = (Record)currentUser.getSession().getAttribute("login_user");
			if(login_user==null){
				login_user = Db.findFirst("select ul.*,r.code role_code"
						+ " from user_login ul"
						+ " left join user_role ur on ur.user_name = ul.user_name"
						+ " left join role r on r.id = ur.role_id"
						+ " where ul.user_name=?",currentUser.getPrincipal());
				currentUser.getSession().setAttribute("login_user",login_user);
			}
			Long officeId=login_user.getLong("office_id");
			ai.getController().setAttr("login_user", login_user);
			
			if(login_user.get("c_name") != null && !"".equals(login_user.get("c_name"))){
				ai.getController().setAttr("userId", login_user.get("c_name"));
			}else{
				ai.getController().setAttr("userId", currentUser.getPrincipal());
			}
			
			UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name =? and office_id=? and is_main=1", currentUser.getPrincipal(), officeId);
	        if(uo != null){
	            Office office = Office.dao.findById(uo.getLong("office_id"));
	            ai.getController().setAttr("office_name", office.get("office_name"));
	        }
	        
			ai.getController().setAttr("user_login_id", currentUser.getPrincipal());
			ai.getController().setAttr("permissionMap", ai.getController().getSessionAttr("permissionMap"));
		}else{
			ai.getController().redirect("/login");
			return;
		}
		setSysTitle(ai.getController());
		ai.invoke();
	}
	
	private void setSysTitle(Controller controller) {
		HttpServletRequest request = controller.getRequest();
		String serverName = request.getServerName();
        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
        
        logger.debug("Current host path:"+basePath);
        OfficeConfig of = OfficeConfig.dao.findFirst("select * from office_config where domain like '"+serverName +"%' or domain like '%"+serverName +"%'");
        if(of==null){//没有配置公司的信息会导致页面出错，显示空白页
        	of = new OfficeConfig();
        	of.set("system_title", "易达物流");
        	of.set("logo", "/yh/img/eeda_logo.ico");
        }
        controller.setAttr("SYS_CONFIG", of);
	}

}
