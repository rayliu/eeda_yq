package controllers.profile;

import interceptor.EedaMenuInterceptor;
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
import models.UserLogin;
import models.UserOffice;
import models.UserRole;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.CompareStrList;
import controllers.util.DbUtils;
import controllers.util.ParentOffice;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class UserRoleController extends Controller {
	private Log logger = Log.getLog(PrivilegeController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

	
	
	@Before(EedaMenuInterceptor.class)
	public void index(){
		render("/eeda/profile/userRole/userRoleList.html");
	}
	
	/*查询用户角色*/
//	@RequiresPermissions(value = {PermissionConstant.PERMSSION_UR_LIST})
	public void list(){
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

		String totalWhere ="";
		String sql = "";
		
		Long parentID =pom.getBelongOffice();
		if(parentID == null || "".equals(parentID)){
			parentID = pom.getParentOfficeId();
			totalWhere ="select count(1) total from user_role ur "
					+ " left join role r on r.id = ur.role_id "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " where !isnull(ul.is_stop) != 1 and r.office_id = " + parentID;
			sql = "select ur.user_name,ul.c_name,group_concat(r.name separator '<br>') name,ur.remark,ur.role_code "
					+ " from user_role ur left join role r on r.id=ur.role_id "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " left join office o on ul.office_id = o.id "
					+ " where !isnull(ul.is_stop) != 1 and (o.id = " + parentID + " or o.belong_office = " + parentID + ") "
							+ "and (r.office_id = " + parentID + " or r.office_id is null) group by ur.user_name" + sLimit;

		}else{
			totalWhere ="select count(1) total from user_role ur "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " where !isnull(ul.is_stop) != 1 and ul.office_id = " + pom.getCurrentOfficeId();
			sql = "select ur.user_name,ul.c_name,group_concat(r.name separator '<br>') name,ur.remark,ur.role_code "
					+ " from user_role ur left join role r on r.id=ur.role_id "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " where !isnull(ul.is_stop) != 1 and ul.office_id = " + pom.getCurrentOfficeId() + " and r.office_id = " + parentID + " group by ur.user_name" + sLimit;
		}
		// 获取总条数
       /* String sql = "select ur.user_name,group_concat(r.name separator '<br>') name,ur.remark,ur.role_code from user_role ur left join role r on r.code=ur.role_code group by ur.user_name" + sLimit;*/

		String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
	}
	/*编辑*/
	@Before(EedaMenuInterceptor.class)
	public void edit(){
		String user_name = getPara("username");
		setAttr("user_name", user_name);		
		render("/eeda/profile/userRole/assigning_roles.html");
	}
	
	/*给新用户分配角色*/
	@Before(EedaMenuInterceptor.class)
	public void add(){
		render("/eeda/profile/userRole/addRole.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void addOrUpdate(){
		String id = getPara("id");
		UserLogin user = UserLogin.dao.findFirst("select * from user_login where id = ?",id);
		List<UserRole> list = UserRole.dao.find("select * from user_role where user_name = ?",user.get("user_name"));
		if(list.size()>0){
			setAttr("user_name", user.get("user_name"));		
			render("/eeda/profile/userRole/assigning_roles.html");
		}else{
			render("/eeda/profile/userRole/addRole.html");
		}
		
	}
	/*列出没有角色的用户*/
	public void userList(){
		String sql = "";
		Long parentID = pom.getBelongOffice();
		//系统管理员
		if(parentID == null || "".equals(parentID)){
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_code is null and (o.id = " + pom.getParentOfficeId() +" or o.belong_office= "+ pom.getParentOfficeId() +")";
		}else{
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_code is null and o.id = " + pom.getCurrentOfficeId();
		}
		
		List<Record> orders = Db.find(sql);
        renderJson(orders);
	}
	public void saveUserRole(){
		String name = getPara("name");
		String r = getPara("roles");
		String[] roleIds = r.split(",");
		for (String id : roleIds) {
			UserRole ur = new UserRole();
			ur.set("user_name", name);
			ur.set("role_id", id);
			ur.save();
		}
		renderJson();
	}
	public void updateRole(){
		String name = getPara("name");
		String r = getPara("roles");
		
		String[] roles = r.split(",");
		
		
        List<UserRole> list = UserRole.dao.find("select id from user_role where user_name=?",name);
        
        List<Object> ids = new ArrayList<Object>();
        for (UserRole ur : list) {
            ids.add(ur.get("id"));
        }
        
        CompareStrList compare = new CompareStrList();
        
        List<Object> returnList = compare.compare(ids, roles);
        
        ids = (List<Object>) returnList.get(0);
        List<String> saveList = (List<String>) returnList.get(1);
        if(ids.size()>0){
        	for (Object id : ids) {
                UserRole.dao.findFirst("select * from user_role where id=?", id).delete();
            }
        }
        
        if(saveList.size()>0){
        	for (Object object : saveList) {
                UserRole ur = new UserRole();
                ur.set("user_name", name);
                /*根据id找到Role*/
                Role role = Role.dao.findFirst("select * from role where id=?",object);
                if(role != null){
                	ur.set("role_code", role.get("code"));
                    ur.save();
                }
                
            }
        }
        
		renderJson();
	}
	public void roleList() {
		//获取选中的用户
		String username = getPara("username");
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		String sql = "";
		
		
		Long parentID = pom.getBelongOffice();
		if(parentID == null || "".equals(parentID)){
			parentID = pom.getParentOfficeId();
		}
		Record rec = Db.findFirst("select count(1) total from user_role ur"
                + " left join role r on (ur.role_id=r.id or ur.role_code=r.code)"
                + " where ifnull(r.code,'')!='admin' and ur.user_name =? "
                + " and (r.office_id is null or r.office_id =? ) " ,username,parentID);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db.find("select ur.*, r.code,r.name from user_role ur"
		        + " left join role r on (ur.role_id=r.id or ur.role_code=r.code)"
		        + " where ifnull(r.code,'')!='admin' and ur.user_name =? "
		        + " and (r.office_id is null or r.office_id =? ) ",username,parentID);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}
	
	@Before(EedaMenuInterceptor.class)
	public void userPermissionRender(){
		String username = getPara("username");
		setAttr("username", username);
		render("/eeda/profile/userRole/userPermission.html");
	}
	//查询用户的权限
	public void permissionList(){
		/*获取到用户的名称*/
		String username = getPara("username");
		//查询当前用户的父类公司的id
		Office parentOffice = getCurrentUserOffice();
		Long parentID = parentOffice.get("belong_office");
		if(parentID == null || "".equals(parentID)){
			parentID = parentOffice.getLong("id");
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
			/*select p.code, p.name,p.module_name ,r.permission_code from permission p left join  (select * from role_permission rp where rp.role_code =?) r on r.permission_code = p.code where p.module_name=?*/
			
			List<RolePermission> childOrders = RolePermission.dao.find("select distinct p.id, p.code, p.name,p.module_name ,r.permission_code from permission p left join (select rp.* from user_role  ur left join role_permission  rp on rp.role_code = ur.role_code where ur.user_name =? and  rp.office_id =  " + parentID + ")r on r.permission_code = p.code where p.module_name=? order by p.id",username,key);
			Record r = new Record();
			r.set("module_name", key);
			r.set("childrens", childOrders);
			r.set("is_authorize", rp.get("is_authorize"));
			orders.add(r);
			
		}
		Map orderMap = new HashMap();
		orderMap.put("aaData", orders);

		orderMap.put("aaData", orders);

		renderJson(orderMap);
	}

	private Office getCurrentUserOffice() {
		String userName = currentUser.getPrincipal().toString();
		UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
		Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
		return parentOffice;
	}
	
}
