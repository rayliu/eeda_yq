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

public class SetAttrLoginUserInterceptor implements Interceptor{
	private Log logger = Log.getLog(SetAttrLoginUserInterceptor.class);
	@Override
	public void intercept(Invocation ai) {
		Subject currentUser = SecurityUtils.getSubject();
		if(currentUser.isAuthenticated()){
			UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?",currentUser.getPrincipal());
			if(user.get("c_name") != null && !"".equals(user.get("c_name"))){
				ai.getController().setAttr("userId", user.get("c_name"));
			}else{
				ai.getController().setAttr("userId", currentUser.getPrincipal());
			}
			
			UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name ='"+currentUser.getPrincipal()+"' and is_main=1");
	        if(uo != null){
	            Office office = Office.dao.findById(uo.getLong("office_id"));
	            ai.getController().setAttr("office_name", office.get("office_name"));
	        }
	        
			ai.getController().setAttr("user_login_id", currentUser.getPrincipal());
			ai.getController().setAttr("permissionMap", ai.getController().getSessionAttr("permissionMap"));
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
