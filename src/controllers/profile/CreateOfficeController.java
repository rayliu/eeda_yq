package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.List;

import models.Office;
import models.Permission;
import models.Role;
import models.RolePermission;
import models.UserLogin;
import models.UserRole;
import models.eeda.profile.Module;
import models.eeda.profile.ModuleRole;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.MD5Util;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CreateOfficeController extends Controller {
    private Log logger = Log.getLog(CreateOfficeController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        render("/eeda/profile/createOffice/edit.html");
    }

    @Before(Tx.class)
    public void step1() {
        String officeName = getPara("office_name");
        String user = getPara("user");
        String pwd = getPara("pwd");
        String type = getPara("type");

        Office officeRec = new Office();
        officeRec.set("office_name", officeName);
        officeRec.set("type", type);
        officeRec.save();
        Long officeId = officeRec.getLong("id");
        logger.debug("officeId:" + officeId);

        UserLogin userRec = new UserLogin();
        userRec.set("user_name", user);
        String sha1Pwd = MD5Util.encode("SHA1", pwd);
        userRec.set("password", sha1Pwd);
        userRec.set("office_id", officeId);
        userRec.set("c_name", user);
        userRec.save();
        Long userId = userRec.getLong("id");
        logger.debug("userId:" + userId);

        Role roleRec = new Role();
        roleRec.set("code", "admin");
        roleRec.set("name", "系统管理员");
        roleRec.set("office_id", officeId);
        roleRec.save();
        Long roleId = roleRec.getLong("id");
        logger.debug("roleId:" + roleId);

        UserRole ur = new UserRole();
        ur.set("user_name", user);
        ur.set("role_id", roleId);
        ur.set("role_code", "admin");
        ur.save();
        
        Record rec = new Record();
        rec.set("office_id", officeId);
        rec.set("role_id", roleId);
        List<Office> list = Office.dao.find(
                "select * from office where type=? and id!=?", type, officeId);
        rec.set("office_list", list);

        renderJson(rec);
    }

    @Before(Tx.class)
    public void step2() {
        String officeId = getPara("office_id");
        String roleId = getPara("role_id");
        String copyOfficeId = getPara("to_office_id");
        logger.debug("to_office_id:" + copyOfficeId);

        List<Module> list = Module.dao
                .find("select * from eeda_modules where parent_id is null and office_id=?",
                        copyOfficeId);
        for (Module module : list) {
            Long moduleId = module.getLong("id");

            module.set("id", null).set("office_id", officeId).save();
            Long newModuleId = module.getLong("id");
            List<Module> subList = Module.dao
                    .find("select * from eeda_modules where parent_id=? and office_id=?",
                            moduleId, copyOfficeId);
            for (Module module2 : subList) {
                Long subModuleId = module2.getLong("id");
                module2.set("id", null);
                module2.set("office_id", officeId);
                module2.set("parent_id", newModuleId);
                module2.save();

                // 每个新子模块都默认有admin的role
                Long newSubModuleId = module2.getLong("id");
                ModuleRole mr = new ModuleRole();
                mr.set("module_id", newSubModuleId);
                mr.set("role_id", roleId);
                mr.save();
                Long newModuleRoleId = mr.getLong("id");

                // copy 每个子模块 permission
                List<Permission> permissionList = Permission.dao
                        .find("select * from permission where  module_id=?", subModuleId);
                for (Permission p : permissionList) {
                    Long pId = p.getLong("id");
                    p.set("id", null);
                    p.set("module_id", newSubModuleId);
                    p.save();
                    Long newPermissionId = p.getLong("id");
                    List<RolePermission> rpList = RolePermission.dao
                            .find("select * from role_permission where permission_id=? and module_id=?",
                                    pId, subModuleId);
                    for (RolePermission rolePermission : rpList) {
                        rolePermission.set("id", null);
                        rolePermission.set("role_id", roleId);
                        rolePermission.set("permission_id", newPermissionId);
                        rolePermission.set("module_id", newSubModuleId);
                        rolePermission.set("module_role_id", newModuleRoleId);
                        rolePermission.save();
                    }
                }
            }

        }
        
        renderText("OK");
    }
}
