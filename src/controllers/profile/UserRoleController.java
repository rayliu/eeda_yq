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
import models.RolePermission;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
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
					+ " where ifnull(ul.is_stop, 0) != 1 and r.office_id = " + parentID;
			sql = "select ul.id,ur.user_name,ul.c_name,group_concat(r.name separator '<br>') name,ur.remark,ur.role_code "
					+ " from user_role ur left join role r on r.id=ur.role_id "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " left join office o on ul.office_id = o.id "
					+ " where ifnull(ul.is_stop, 0) != 1 and (o.id = " + parentID + " or o.belong_office = " + parentID + ") "
							+ "and (r.office_id = " + parentID + " or r.office_id is null) group by ur.user_name" + sLimit;

		}else{
			totalWhere ="select count(1) total from user_role ur "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " where ifnull(ul.is_stop, 0) != 1 and ul.office_id = " + pom.getCurrentOfficeId();
			sql = "select ul.id,ur.user_name,ul.c_name,group_concat(r.name separator '<br>') name,ur.remark,ur.role_code "
					+ " from user_role ur left join role r on r.id=ur.role_id "
					+ " left join user_login ul on ur.user_name = ul.user_name "
					+ " where ifnull(ul.is_stop, 0) != 1 and ul.office_id = " + pom.getCurrentOfficeId() + " and r.office_id = " + parentID + " group by ur.user_name" + sLimit;
		}
		
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
		String id = getPara("id");
		String user_name = getPara("username");
		UserLogin user1 = LoginUserController.getLoginUser(this);
		if (user1==null) {
            return;
        }
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("user_login", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
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
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_id is null and (o.id = " + pom.getParentOfficeId() +" or o.belong_office= "+ pom.getParentOfficeId() +")";
		}else{
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_id is null and o.id = " + pom.getCurrentOfficeId();
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
		renderJson("{\"result\":true}");
	}
	public void updateRole(){
		String userName = getPara("name");
		String r = getPara("roles");
		
		if(StrKit.isBlank(r)){
		    Db.update("delete from user_role where user_name= ? ", userName);
		}else{
		    Db.update("delete from user_role where user_name= ? and role_id not in("+r+")", userName);
		
            String[] role_ids = r.split(",");
            //要添加的role
            
            if(role_ids.length>0){
            	for (Object role_id : role_ids) {
                    UserRole ur = new UserRole();
                    ur.set("user_name", userName);
                    logger.debug("role_id:"+role_id);
                    UserRole role = UserRole.dao.findFirst("select * from user_role where user_name=? and role_id=?", userName, role_id);
                    if(role == null){
                    	ur.set("role_id", role_id);
                        ur.save();
                    }
                }
            }
		}
		clearMenuCache(userName);
		renderJson("{\"result\":true}");
	}
	
	//清除菜单缓存
	private void clearMenuCache(String userName) {
	    Record user = Db.findFirst("select * from user_login where user_name=?", userName);
	    if(user!=null){
            long user_id = user.getLong("id");
            Map cache = EedaMenuInterceptor.menuCache;
            if(cache!=null && cache.get(user_id)!=null){
                cache.remove(user_id);
            }
	    }
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

		Long officeId = pom.getBelongOffice();
		if(officeId == null || "".equals(officeId)){
		    officeId = pom.getParentOfficeId();
		}
		
		String sql = "SELECT  r.id, r.code, r.name, "
		        + " if("
		        + "      (select count(1) from user_role ur1 where user_name=?"
                + "             and ur1.role_id = r.id"
                + "      )>0,"
                + "      'Y', 'N') is_assign FROM role r"
		        +"     LEFT JOIN  user_role ur ON ur.role_id = r.id"
		        +"     WHERE r.office_id = ? group by id";
		Record rec = Db.findFirst("select count(1) total from ("+sql+") B " ,username, officeId);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db.find(sql,username, officeId);
		
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
