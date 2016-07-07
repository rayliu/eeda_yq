package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.Permission;
import models.Role;
import models.RolePermission;
import models.UserOffice;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.CompareStrList;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PrivilegeController extends Controller {
	private Logger logger = Logger.getLogger(PrivilegeController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	 ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	
	
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	public void index() {
		render("/eeda/profile/privilege/PrivilegeList.html");
	}
	//@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	//编辑和分配都是用这个list
	public void list() {
		//查询当前用户的父类公司的id
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		
		String rolename = getPara("rolename");
		String code = null;
		if(rolename!=null){
			Role role = Role.dao.findFirst("select * from role where name=? and office_id=?",rolename,parentID);
			code = role.get("code");
		}
		
		List<Record> orders = new ArrayList<Record>();
		//List<Permission> parentOrders =Permission.dao.find("select module_name from permission group by module_name");
		List<Permission> parentOrders = Permission.dao.find("select p.module_name,rp.is_authorize from permission p left join role_permission rp on rp.permission_code = p.code where rp.role_code ='admin' and rp.office_id = ?",parentID);
		List<Permission> po = new ArrayList<Permission>();
		for (int i = 0; i < parentOrders.size(); i++) {
			if(i!=0){
				if(!parentOrders.get(i).get("module_name").equals(parentOrders.get(i-1).get("module_name"))){
					po.add(parentOrders.get(i));
				}
			}else{
				po.add(parentOrders.get(i));
			}
			
		}	
		
		for (Permission rp : po) {
			String key = rp.get("module_name");
			List<RolePermission> childOrders = RolePermission.dao.find("select p.code, p.name,p.module_name ,r.permission_code,r.is_authorize from permission p left join  (select * from role_permission rp where rp.role_code =? and rp.office_id =?) r on r.permission_code = p.code"
					+ "  where p.module_name=?",code,parentID,key);
			Record r = new Record();
			r.set("module_name", key);
			r.set("childrens", childOrders);
			r.set("is_authorize", rp.get("is_authorize"));
			orders.add(r);
			
		}
		
		Map orderMap = new HashMap();
		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_LIST})
	public void roleList() {
		Long parentID = pom.getParentOfficeId();
		
		String sql_m = "select distinct r.name,r.code from role_permission  rp left join role r on r.code =rp.role_code  where (r.office_id = " + parentID + " and rp.office_id =  " + parentID + ") or rp.office_id is null group by r.name,r.code";

		// 获取当前页的数据
		List<Record> orders = Db.find(sql_m);
		renderJson(orders);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	public void add(){
		render("/eeda/profile/privilege/RolePrivilege.html");
	}
	public void addOrUpdate(){
		String id = getPara("id");
		String userName = currentUser.getPrincipal().toString();
		UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
		Role role = Role.dao.findById(id);
		List<RolePermission> list = RolePermission.dao.find("select * from role_permission where role_code = ? and office_id = ?",role.get("code"),currentoffice.get("office_id"));
		if(list.size()>0){
			setAttr("rolename", role.get("name"));
			render("/eeda/profile/privilege/RolePrivilegeEdit.html");
		}else{
			render("/eeda/profile/privilege/RolePrivilege.html");
		}
	}
	/*查找没有权限的角色*/
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	public void seachNewRole(){
		Long parentID = pom.getParentOfficeId();
		String sql_m = "select r.code,r.name from role r left join (select * from role_permission where office_id = " + parentID +" ) rp on r.code = rp.role_code where rp.role_code is null and r.office_id = " + parentID;
		List<Record> orders = Db.find(sql_m);
		renderJson(orders);
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_CREATE})
	@Before(Tx.class)
	public void save(){
		String rolename = getPara("name");
		String permissions = getPara("permissions");
		String[] ps = permissions.split(",");
		//根据角色名称找到角色代码
		Long parentID = pom.getParentOfficeId();
		Role role = Role.dao.findFirst("select * from role where name=?",rolename);
		for (String str : ps) {
			RolePermission r = new RolePermission();
			r.set("role_code", role.get("code"));
			r.set("permission_code", str);
			r.set("office_id",parentID);
			Permission per = Permission.dao.findFirst("select * from permission where code = ?",str);
			//判断当前用户是否收费了
			r.set("is_authorize", per.get("is_authorize"));
			r.save();
		}
		renderJson();
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_UPDATE})
	@Before(Tx.class)
	public void update(){
		String rolename = getPara("name");
		String permissions = getPara("permissions");
		String[] ps = permissions.split(",");
		//根据角色名称找到角色代码
		//查询当前用户的父类公司的id
		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		
		
		Role role = Role.dao.findFirst("select * from role where name=? and office_id = ?",rolename,parentID);
		List<RolePermission> rp = RolePermission.dao.find("select * from role_permission where role_code =? and office_id = ?",role.get("code"),parentID);
		
		 List<Object> ids = new ArrayList<Object>();
		 for (RolePermission r : rp) {
			ids.add(r.get("permission_code"));
		 }
		
		 CompareStrList compare = new CompareStrList();
	        
	     List<Object> returnList = compare.compare(ids, ps);
	     
	     ids = (List<Object>) returnList.get(0);
	     List<String> saveList = (List<String>) returnList.get(1);
	     for (Object pc : ids) {
	    	 RolePermission.dao.findFirst("select * from role_permission where role_code=? and permission_code=? and office_id = ?", role.get("code"),pc,parentID).delete();
        }
        
        for (Object object : saveList) {
        	
			RolePermission r = new RolePermission();
			r.set("role_code", role.get("code"));
			r.set("permission_code", object);
			r.set("office_id", parentID);
			Permission per = Permission.dao.findFirst("select * from permission where code = ?",object);
			//判断当前用户是否收费了
			r.set("is_authorize", per.get("is_authorize"));
			r.save();
			
        }
		renderJson();
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_RP_UPDATE})
	public void edit(){
		String rolename = getPara("rolename");
		setAttr("rolename", rolename);
		render("/eeda/profile/privilege/RolePrivilegeEdit.html");
	}
}
