package controllers.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.Permission;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.core.Controller;

public class getCurrentPermission extends Controller{
	private getCurrentPermission(){
		
	}
	public static getCurrentPermission getInstance(){
		return new getCurrentPermission();
	}	
	public Map<String,String> currentHasPermission(Subject currentUser,ParentOfficeModel pom){
		String sql = "select distinct p.id, p.code, p.name,r.permission_code,r.is_authorize from permission p left join (select rp.* from t_rbac_ref_user_role  ur left join role_permission  rp on rp.role_code = ur.role_code where ur.user_name ='" + currentUser.getPrincipal() + "' and  rp.office_id =  " + pom.getParentOfficeId() + ")r on r.permission_code = p.code ";
		Map<String,String> map = new HashMap<String,String>(); 
		List<Permission> list = Permission.dao.find(sql);
		for (Permission permission : list) {
			String code = permission.get("code").toString();
			String value = "";
			if("".equals(permission.get("permission_code")) || permission.get("permission_code") == null){
				value = "";
			}else{
				if(pom.getBelongOffice() == null || "".equals(pom.getBelongOffice())){
					if(permission.get("is_authorize") == null || "".equals(permission.get("is_authorize")) || permission.getInt("is_authorize") == 0){
						value="";
					}else{
						value = permission.get("permission_code").toString();
					}
				}else{
					value = permission.get("permission_code").toString();
				}
			}
			if(code.indexOf(".") >=0){
				String[] orderData = code.split("\\.");
				map.put(orderData[0] + "_" + orderData[1] , value);
			}else{
				map.put(code , value);
			}
			
		}
		return map;
	}

}
