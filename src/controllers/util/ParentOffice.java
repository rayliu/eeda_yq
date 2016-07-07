package controllers.util;

import java.util.HashMap;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.UserLogin;
import models.UserOffice;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;



public class ParentOffice{
	
	private ParentOffice(){
	}
	
	public static ParentOffice getInstance(){
		return new ParentOffice();
	}
	
	public ParentOfficeModel getOfficeId(Controller controller) {
		Subject currentUser = SecurityUtils.getSubject();
		String userName;
		UserLogin currentOffice = new UserLogin();
		Office parentOffice = new Office();
		try{
			userName = currentUser.getPrincipal().toString();
			currentOffice = UserLogin.dao.findFirst("select * from user_login where user_name = ?",userName);
		    parentOffice = Office.dao.findFirst("select * from office where id = ?",currentOffice.get("office_id"));
		}catch(NullPointerException ex){
			ex.printStackTrace();
			controller.redirect("/login");
		}
	    Long parentID = parentOffice.get("belong_office");
	    if(parentID == null || "".equals(parentID)){
			parentID = parentOffice.getLong("id");
		}
	    
	   /* Map<String, Long> map = new HashMap<String, Long>();
	    map.put("currentOfficeId", (Long) currentOffice.get("office_id"));
	    map.put("parentOfficeId", parentID);
	    map.put("currentBelongOfficeId", (Long) parentOffice.get("belong_office"));*/
	    ParentOfficeModel pom = new ParentOfficeModel();
	    pom.setBelongOffice((Long) parentOffice.get("belong_office"));
	    pom.setCurrentOfficeId((Long) currentOffice.get("office_id"));
	    pom.setParentOfficeId(parentID);
		return pom;
	}
	
}
