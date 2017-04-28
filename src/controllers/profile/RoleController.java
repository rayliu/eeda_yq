package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.Role;
import models.RolePermission;
import models.UserLogin;
import models.UserRole;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.json.JFinalJson;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class RoleController extends Controller {
	private Log logger = Log.getLog(RoleController.class);
	Subject currentUser = SecurityUtils.getSubject();

	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/profile/role/RoleList.html");
	}
	
	public void list() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}

		// 获取总条数
		String totalWhere = "";
		String sql = "";
		String querySQL = "";
		Long parentID = pom.getParentOfficeId();
		sql = "select count(1) total from role where office_id is null or office_id = "+ parentID;
		querySQL ="select * from role where office_id is null or office_id = "+ parentID;
		
		

		// 获取当前页的数据
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));
		
		// 获取当前页的数据
		List<Record> orders = Db.find(querySQL);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}
	//没有系统管理员
	public void listPart() {
		String sLimit = "";
		String pageIndex = getPara("sEcho");
		if (getPara("iDisplayStart") != null
		        && getPara("iDisplayLength") != null) {
			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
			        + getPara("iDisplayLength");
		}
		//获取总公司的ID
		Long parentID = pom.getParentOfficeId();
		// 获取总条数
		String totalWhere = "";
		String sql = "select count(1) total from role where (code is null or code!='admin') and (office_id is null or office_id = " + parentID +")";
		Record rec = Db.findFirst(sql + totalWhere);
		logger.debug("total records:" + rec.getLong("total"));

		// 获取当前页的数据
		List<Record> orders = Db.find("select * from role where (code is null or code!='admin') and (office_id is null or office_id = ?)",parentID);
		Map orderMap = new HashMap();
		orderMap.put("sEcho", pageIndex);
		orderMap.put("iTotalRecords", rec.getLong("total"));
		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

		orderMap.put("aaData", orders);

		renderJson(orderMap);

	}


		// 点击创建角色保存
//	@RequiresPermissions(value = {PermissionConstant.PERMSSION_R_CREATE})
	public void SaveRole() {
		
		
		Long parentID = pom.getParentOfficeId();
		Role r = new Role();
		String name = getPara("rolename");
		String remark = getPara("roleremark");
		String code = getPara("rolecode");
		r.set("name", name).set("code", code).set("remark", remark)
		.set("office_id", parentID).save();
		redirect("/role");
	}

	// 点击保存
	@Before(Tx.class)
	public void save() {
		
		
		String jsonStr=getPara("submitObj");
		logger.debug("permissionJsonStr: " + jsonStr);
		Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        
        String role_id = dto.get("role_id").toString();
        String rolename = dto.get("role_name").toString();
        String remark = dto.get("role_desc").toString();
		
		Role role = Role.dao.findById(role_id);
		role.set("name", rolename).set("remark", remark).update();

		List role_permisstions = (List)dto.get("role_permisstions");
		for (Object p : role_permisstions) {
            Map per = (Map)p;
            
            String module_id = per.get("module_id").toString();
            String permission_id = per.get("permission_id").toString();
            String permission_code = per.get("permission_code").toString();
            String role_permission_id = per.get("role_permission_id").toString();
            Boolean isCheck = (Boolean)per.get("checked");
            int isAuth = (isCheck==true?1:0);
            
            if(!StrKit.isBlank(role_permission_id)){
                Db.update("update role_permission set is_authorize=?, permission_code=? where id=?", isAuth, permission_code, role_permission_id);
            }else{
                logger.debug("role_permission_id 为空，需要新增 "
                        + "role_role_permission[role_id: "+role_id
                        +", module_id:"+module_id
                        +", permission_id:"+permission_id+"]");
                long moduleRoleId = 0;
                Record moduleRoleRec = Db.findFirst("select * from module_role where module_id=? and role_id=?", module_id, role_id);
                if(moduleRoleRec==null){
                    Record newModuleRoleRec = new Record();
                    newModuleRoleRec.set("module_id", module_id);
                    newModuleRoleRec.set("role_id", role_id);
                    Db.save("module_role", newModuleRoleRec);
                    moduleRoleId = newModuleRoleRec.getLong("id");
                }else{
                    moduleRoleId = moduleRoleRec.getLong("id");
                }
                
                if(moduleRoleId>0){
                    Record newRolePermissionRec = new Record();
                    newRolePermissionRec.set("module_id", module_id);
                    newRolePermissionRec.set("role_id", role_id);
                    newRolePermissionRec.set("permission_id", permission_id);
                    newRolePermissionRec.set("permission_code", permission_code);
                    newRolePermissionRec.set("module_role_id", moduleRoleId);
                    newRolePermissionRec.set("is_authorize", isAuth);
                    Db.save("role_permission", newRolePermissionRec);
                }
            }
        }
		
		renderText("ok");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void ClickRole() {
	    String role_id = getPara("id");
	    if (role_id != null) {
	        Role h = Role.dao.findById(role_id);
	        setAttr("role", h);
	        
	        //获取office_id
	        UserLogin user = LoginUserController.getLoginUser(this);
	        long office_id = user.getLong("office_id");
	        
	        //获取一级菜单
	        List<Record> menuList = Db.find("select id, module_name, parent_id, office_id, seq, version, url from eeda_modules "
	        + "where office_id=?  and parent_id is null order by seq", office_id);

	        for (Record menu : menuList) {
                long menu_id = menu.getLong("id");
                List<Record> moduleList = Db.find("select id, module_name, parent_id, office_id, seq, version, url "
                        + "from eeda_modules where office_id=? and parent_id =? and is_public='Y' order by seq", office_id, menu_id);
                
                for (Record module : moduleList) {
                    long module_id = module.getLong("id");
                    logger.debug(module.getStr("module_name")+" module_id:" + module_id);
                    //查询当前Module有多少个权限点，该role是否已授权
                    List<Record> authRecs = Db.find(
                            "select rp.id, rp.permission_id, rp.permission_code, p.name permission_name, rp.module_id, rp.is_authorize from role_permission rp "
                                    +" left join permission p on rp.permission_id = p.id"
                                    +" where rp.permission_id in(select id from permission where module_id=?)"
                                    +" and rp.role_id=? and rp.module_id=?;", module_id, role_id, module_id);
                    //如果有role, 获取其权限点
                    if(authRecs.size()>0){
                         module.set("permission_list", authRecs);
                    }else{
                        authRecs = Db.find(
                                "select id permission_id, code permission_code, name permission_name, 0 as is_authorize from permission where module_id=?", module_id);
                        module.set("permission_list", authRecs); 
                    }
                }
                
                //过滤没有权限的Module，不显示出来
                List<Record> newModuleList = filterOutNullPermissionModule(moduleList);
                
                menu.set("module_list", newModuleList);
            }
	        
	        //过滤没有权限的一级Menu，不显示出来
	        List<Record> newMenuList = new ArrayList<Record>();
	        for (Record menu : menuList) {
	            logger.debug(menu.getStr("module_name")+":"+menu.get("module_list"));
	            List<Record> moduleList = menu.get("module_list");
	           if(moduleList.size()>0){
	               newMenuList.add(menu);
	           }
	        }
	        
	        setAttr("menu_list", newMenuList);
	        
	        render("/eeda/profile/role/RoleEdit.html");
	    }

	}

    private List<Record> filterOutNullPermissionModule(List<Record> moduleList) {
        List<Record> newModuleList = new ArrayList<Record>();
        for (Record module : moduleList) {
            List<Record> permission_list = module.get("permission_list");
            if(permission_list.size()>0){
                newModuleList.add(module);
            }
        }

        return newModuleList;
    }

	// 删除
//	@RequiresPermissions(value = {PermissionConstant.PERMSSION_R_DELETE})
	public void deleteRole() {
		String id = getPara();
		if (id != null) {
			Role l = Role.dao.findById(id);
			List<UserRole> ulist = UserRole.dao.find("select * from user_role where role_code =?",l.get("code"));
			if(ulist.size()>0){
				for (UserRole userRole : ulist) {
					userRole.delete();
				}
			}
			List<RolePermission> rlist = RolePermission.dao.find("select * from role_permission where role_code = ?",l.get("code"));
			if(rlist.size()>0){
				for (RolePermission rolePermission : rlist) {
					rolePermission.delete();
				}
			}
			
			l.delete();
		}
		
		redirect("/role");

	}
	public void checkRoleNameExit(){
		String name = getPara("rolename");
		boolean isExit;
		Long parentID = pom.getParentOfficeId();
		Role role = Role.dao.findFirst("select * from role where name=? and (office_id is null or office_id = ?)",name,parentID);
		
		if(role==null){
			isExit=true;
		}else{
			isExit=false;
		}
		renderJson(isExit);
	}
	public void checkRoleCodeExit(){
		String code = getPara("rolecode");
		boolean isExit;
		Long parentID = pom.getParentOfficeId();
		Role role = Role.dao.findFirst("select * from role where code=? and (office_id is null or office_id = ?)",code,parentID);
		
		if(role==null){
			isExit=true;
		}else{
			isExit=false;
		}
		renderJson(isExit);
	}


}
