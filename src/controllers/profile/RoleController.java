package controllers.profile;import interceptor.EedaMenuInterceptor;import interceptor.SetAttrLoginUserInterceptor;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import java.util.Map;import models.ParentOfficeModel;import models.Role;import models.RolePermission;import models.UserLogin;import models.UserRole;import org.apache.shiro.SecurityUtils;import org.apache.shiro.authz.annotation.RequiresAuthentication;import org.apache.shiro.authz.annotation.RequiresPermissions;import org.apache.shiro.subject.Subject;import com.google.gson.Gson;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.json.JFinalJson;import com.jfinal.kit.LogKit;import com.jfinal.kit.StrKit;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import com.jfinal.plugin.activerecord.tx.Tx;import controllers.util.ParentOffice;import controllers.util.PermissionConstant;@RequiresAuthentication@Before({SetAttrLoginUserInterceptor.class,EedaMenuInterceptor.class})public class RoleController extends Controller {	private Log logger = Log.getLog(RoleController.class);	Subject currentUser = SecurityUtils.getSubject();	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);	public void index() {		Record re = Db.findFirst("select * from t_rbac_ref_user_role where user_name = ?",currentUser.getPrincipal());        setAttr("this_rode", re.get("role_code"));		render("/eeda/profile/role/RoleList.html");	}		public void permission() {	    UserLogin user= LoginUserController.getLoginUser(this);        Long office_id = user.get("office_id");	    String role_id = getPara();        Record roleRec = Db.findFirst("select * from t_rbac_role where id = ? and office_id=?", role_id, office_id);        setAttr("role", roleRec);        render("/eeda/profile/role/RolePermissionEdit.html");    }		public void getMenuList() {	    List<Record> menuList= new ArrayList<Record>();	    UserLogin user= LoginUserController.getLoginUser(this);	    Long office_id = user.get("office_id");	    String role_id=getPara("role_id");	    List<Record> level1List = Db.find("select *, 1 level from eeda_modules "	            + "where parent_id is null and delete_flag='N' and office_id=? order by seq", office_id);	    for(Record lvl1:level1List) {	        //查询后台设置中，开放出来的module	        List<Record> level2List = Db.find("select m.*, 2 level from eeda_modules m, eeda_form_define fd "	                + " where fd.module_id=m.id and fd.is_public='Y'"	                + " and m.parent_id=? and m.delete_flag='N' and m.office_id=? order by seq", lvl1.getLong("id"), office_id);	        if(level2List==null || level2List.size()==0)	            continue;	        //获取CRUD权限	        handleOpeationPermission(level2List, office_id, role_id);	        	        menuList.add(lvl1);	        for(Record lvl2:level2List) {	            //判断当前的角色role是否有该menu的权限	            String sql = "select rp.role_id, rp.permission_id, " + 	                    " p.permission_name, p.permission_type, pm.menu_id, m.module_name menu_name" +	                    " from t_rbac_ref_role_permission rp " +	                    " left join t_rbac_permission p on rp.permission_id = p.id" +	                    " left join t_rbac_ref_permission_menu pm on rp.permission_id = pm.permission_id and pm.office_id=?" +	                    " left join eeda_modules m on pm.menu_id = m.id"+	                    " where rp.role_id=? and m.id=?";	            Record menuRec = Db.findFirst(sql, office_id, role_id, lvl2.getLong("id"));	            if(menuRec!=null) {	                lvl2.set("is_menu_open", "Y");	            }else {	                lvl2.set("is_menu_open", "N");	            }	            menuList.add(lvl2);	        }	    }	    	    Map<String,Object> orderListMap = new HashMap<String,Object>();        orderListMap.put("draw", 0);        orderListMap.put("recordsTotal", menuList.size());        orderListMap.put("recordsFiltered", menuList.size());        orderListMap.put("data", menuList);        renderJson(orderListMap);	}		private void handleOpeationPermission(List<Record> level2List, long office_id, String role_id) {	    for(Record lvl2:level2List) {	        Long moduleId = lvl2.getLong("id");            //判断当前的角色role是否有CRUD operation的权限            String sql = "select rp.role_id, rp.permission_id, " +                     "   p.permission_name, p.permission_type" +                     "   from t_rbac_ref_role_permission rp " +                     "   left join t_rbac_permission p on rp.permission_id = p.id" +                     " where p.permission_type='operation' and rp.office_id=? and rp.role_id=? and p.module_id=?";            List<Record> permissionList = Db.find(sql, office_id, role_id, moduleId);            lvl2.set("permission_list", permissionList);        }	}		@Before(Tx.class)	public void setPermissionMenu() {	    UserLogin user= LoginUserController.getLoginUser(this);        Long office_id = user.get("office_id");        String role_id = getPara("role_id");        //先查某个菜单的权限是否已添加到permission, permission_menu两个表        String menu_id = getPara("menu_id");        Record menuRec = Db.findFirst("select * from eeda_modules where id=?", menu_id);        String menu_name=menuRec.getStr("module_name");        //permission中是否有该权限点        Record permissionRec = Db.findFirst("select * from t_rbac_permission where permission_name=? and office_id=? and module_id=?", menu_name, office_id, menu_id);        if(permissionRec!=null) {            LogKit.info("permissionRec 已存在");        }else {//无该权限点，添加一个            LogKit.info("无permissionRec，添加一个");            permissionRec = new Record();            permissionRec.set("permission_name", menu_name);            permissionRec.set("permission_type", "menu");            permissionRec.set("office_id", office_id);            permissionRec.set("module_id", menu_id);            Db.save("t_rbac_permission", permissionRec);                        //该权限点还需关联对应的menu            Record permissionMenuRec = new Record();            permissionMenuRec.set("permission_id", permissionRec.getLong("id"));            permissionMenuRec.set("menu_id", menu_id);            permissionMenuRec.set("office_id", office_id);            Db.save("t_rbac_ref_permission_menu", permissionMenuRec);        }                //为该role设置permission        boolean isChecked = getParaToBoolean("checked");        long permission_id=permissionRec.getLong("id");        if(isChecked) {            Record rec = new Record();            rec.set("permission_id", permission_id);            rec.set("role_id", role_id);            rec.set("office_id", office_id);            Db.save("t_rbac_ref_role_permission", rec);        }else {            Record rec = Db.findFirst("select * from t_rbac_ref_role_permission where permission_id=? and role_id=? and office_id=?",permission_id, role_id,office_id);            Db.delete("t_rbac_ref_role_permission", rec);        }        clearMenuCache(user.getLong("id"));        renderNull();    }		//设置模块的操作权限，CRUD是系统默认的，后续增加的按钮或元素是自定义的	@Before(Tx.class)    public void setModulePermission() {        UserLogin user= LoginUserController.getLoginUser(this);        Long office_id = user.get("office_id");        String role_id = getPara("role_id");        //先查某个module的权限是否已添加到permission, permission_operation两个表        String module_id = getPara("module_id");        String operation_name = getPara("operation_name");        //permission总表中是否有该权限点        Record permissionRec = Db.findFirst("select * from t_rbac_permission where permission_name=? and office_id=? and module_id=?", operation_name, office_id, module_id);        if(permissionRec!=null) {            LogKit.info("permissionRec 已存在");        }else {//无该权限点，添加一个permission，一个operation            LogKit.info("无permissionRec，添加一个");            permissionRec = new Record();            permissionRec.set("permission_name", operation_name);            permissionRec.set("permission_type", "operation");            permissionRec.set("office_id", office_id);            permissionRec.set("module_id", module_id);            Db.save("t_rbac_permission", permissionRec);            //添加operation            Record operationRec = new Record();            operationRec.set("operation_name", operation_name);            operationRec.set("office_id", office_id);            Db.save("t_rbac_operation", operationRec);            //该权限点还需关联对应的operation            Record permissionOperationRec = new Record();            permissionOperationRec.set("permission_id", permissionRec.getLong("id"));            permissionOperationRec.set("operation_id", operationRec.getLong("id"));            permissionOperationRec.set("office_id", office_id);            Db.save("t_rbac_ref_permission_operation", permissionOperationRec);        }                //为该role设置permission        boolean isChecked = getParaToBoolean("checked");        long permission_id=permissionRec.getLong("id");        if(isChecked) {            Record rec = new Record();            rec.set("permission_id", permission_id);            rec.set("role_id", role_id);            rec.set("office_id", office_id);            Db.save("t_rbac_ref_role_permission", rec);        }else {            Record rec = Db.findFirst("select * from t_rbac_ref_role_permission where permission_id=? and role_id=? and office_id=?",permission_id, role_id,office_id);            Db.delete("t_rbac_ref_role_permission", rec);        }                renderNull();    }		public void list() {	    String pageIndex = getPara("draw");		// 获取总条数		String totalWhere = "";		String sql = "";		String querySQL = "";		Long parentID = pom.getParentOfficeId();		sql = "select count(1) total from t_rbac_role where is_delete='N' and office_id = "+ parentID;		querySQL ="select r.*, (select count(1) from t_rbac_ref_role_permission rp where rp.role_id=r.id) permission_count from t_rbac_role r where is_delete='N' and office_id = "+ parentID;		// 获取当前页的数据		Record rec = Db.findFirst(sql + totalWhere);		logger.debug("total records:" + rec.getLong("total"));				// 获取当前页的数据		List<Record> orders = Db.find(querySQL);		Map orderMap = new HashMap();		orderMap.put("draw", pageIndex);		orderMap.put("recordsTotal", rec.getLong("total"));		orderMap.put("recordsFiltered", rec.getLong("total"));		orderMap.put("data", orders);		renderJson(orderMap);	}		public void add() {	    render("/eeda/profile/role/RoleEdit.html");    }	//没有系统管理员	public void listPart() {		String sLimit = "";		String pageIndex = getPara("sEcho");		if (getPara("iDisplayStart") != null		        && getPara("iDisplayLength") != null) {			sLimit = " LIMIT " + getPara("iDisplayStart") + ", "			        + getPara("iDisplayLength");		}		//获取总公司的ID		Long officeId = pom.getParentOfficeId();		// 获取总条数		String totalWhere = "";		String sql = "select * from t_rbac_role where office_id = ?";		Record rec = Db.findFirst("select count(1) total from ("+sql+") B " + totalWhere, officeId);		logger.debug("total records:" + rec.getLong("total"));		// 获取当前页的数据		List<Record> orders = Db.find(sql, officeId);		Map orderMap = new HashMap();		orderMap.put("sEcho", pageIndex);		orderMap.put("iTotalRecords", rec.getLong("total"));		orderMap.put("iTotalDisplayRecords", rec.getLong("total"));		orderMap.put("aaData", orders);		renderJson(orderMap);	}		// 点击创建角色保存//	@RequiresPermissions(value = {PermissionConstant.PERMSSION_R_CREATE})	public void SaveRole() {						Long parentID = pom.getParentOfficeId();		Role r = new Role();		String name = getPara("rolename");		String remark = getPara("roleremark");		String code = getPara("rolecode");		r.set("name", name).set("code", code).set("remark", remark)		.set("office_id", parentID).save();		redirect("/role");	}	// 点击保存	@Before(Tx.class)	public void save() {		String jsonStr=getPara("submitObj");		logger.debug("permissionJsonStr: " + jsonStr);		UserLogin user= LoginUserController.getLoginUser(this);        Long office_id = user.get("office_id");		Gson gson = new Gson();          Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);                String role_id = dto.get("role_id").toString();        String rolename = dto.get("role_name").toString();        String remark = dto.get("role_desc").toString();                Role role = new Role();        if(StrKit.isBlank(role_id)){            role.set("name", rolename).set("remark", remark).set("office_id", office_id).save();        }else{            role = Role.dao.findById(role_id);            role.set("name", rolename).set("remark", remark).update();        }		renderJson(role);	}		@Before(EedaMenuInterceptor.class)	public void edit() {	    String role_id = getPara("id");	    if (role_id != null) {	        Role h = Role.dao.findById(role_id);	        setAttr("role", h);	        render("/eeda/profile/role/RoleEdit.html");	    }	}    private List<Record> filterOutNullPermissionModule(List<Record> moduleList) {        List<Record> newModuleList = new ArrayList<Record>();        for (Record module : moduleList) {            List<Record> permission_list = module.get("permission_list");            if(permission_list.size()>0){                newModuleList.add(module);            }        }        return newModuleList;    }	// 删除	public void deleteRole() {		String id = getPara();		if (id != null) {			Role l = Role.dao.findById(id);			List<UserRole> ulist = UserRole.dao.find("select * from t_rbac_ref_user_role where role_id =?",id);			if(ulist.size()>0){				for (UserRole userRole : ulist) {				    userRole.set("is_delete", "Y").update();				}			}			List<RolePermission> rlist = RolePermission.dao.find("select * from role_permission where role_id = ?",id);			if(rlist.size()>0){				for (RolePermission rolePermission : rlist) {					rolePermission.delete();				}			}			l.set("is_delete", "Y").update();		}				redirect("/role");	}	public void checkRoleNameExit(){		String name = getPara("rolename");		boolean isExit;		Long parentID = pom.getParentOfficeId();		Role role = Role.dao.findFirst("select * from t_rbac_role where name=? and (office_id is null or office_id = ?)",name,parentID);				if(role==null){			isExit=true;		}else{			isExit=false;		}		renderJson(isExit);	}	public void checkRoleCodeExit(){		String code = getPara("rolecode");		boolean isExit;		Long parentID = pom.getParentOfficeId();		Role role = Role.dao.findFirst("select * from t_rbac_role where code=? and (office_id is null or office_id = ?)",code,parentID);				if(role==null){			isExit=true;		}else{			isExit=false;		}		renderJson(isExit);	}		public void clearMenuCache(Long user_id) {	    EedaMenuInterceptor.menuCache=null;//        Map cache = EedaMenuInterceptor.menuCache;//        String str = "No cache";//        //if(cache!=null && cache.get(user_id)!=null){//            cache.remove(user_id);//            logger.debug("");//            str = "User ID("+user_id+") menu cache removed.";//        //}    }}