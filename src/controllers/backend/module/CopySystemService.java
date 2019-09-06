package controllers.backend.module;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.RoleController;
import models.UserLogin;

public class CopySystemService {
    private Controller cont = null;

    public CopySystemService(Controller cont) {
        this.cont = cont;
    }

    @Before(Tx.class)
    public void copySystem(String fromSystemId, String toSystemId, UserLogin user) {
        CopyModuleService service = new CopyModuleService(cont);
        List<Record> parentModuleList= Db.find("select * from eeda_modules where office_id=? "
                + " and parent_id is null and delete_flag='N'", fromSystemId);
        //第一层是目录
        for (Record parentModuleRec : parentModuleList) {
            Long moduleId = parentModuleRec.getLong("id");
            List<Record> formModuleList= Db.find("select * from eeda_modules where office_id=? "
                    + " and parent_id=? and delete_flag='N'", fromSystemId, moduleId);
            parentModuleRec.remove("id").set("office_id", toSystemId);
            Db.save("eeda_modules", parentModuleRec);
            Long newParentModuleId = parentModuleRec.getLong("id");
            for (Record fromModule : formModuleList) {//第2层是form
                Long fromModuleId = fromModule.getLong("id");
                fromModule.remove("id").set("office_id", toSystemId).set("parent_id", newParentModuleId);
                Db.save("eeda_modules", fromModule);
                Long toModuleId = fromModule.getLong("id");
                service.copyModuleWithEvent(fromModuleId.toString(), toModuleId.toString(), toSystemId);
            }
        }
        //为admin设置权限, 先获取role_id
        Record roleRec=Db.findFirst("select * from t_rbac_role where office_id=? and code='admin' order by id desc", toSystemId);
        String role_id=roleRec.getLong("id").toString();
        //查询后台设置中，开放出来的module
        String office_id = toSystemId;
        List<Record> level2List = Db.find("select m.*, 2 level from eeda_modules m, eeda_form_define fd "
                + " where fd.module_id=m.id and fd.is_public='Y'"
                + " and m.delete_flag='N' and m.office_id=? order by seq", office_id);
        RoleController rContr = new RoleController();
        for (Record record : level2List) {
            rContr.setPermission(role_id, record.getLong("id").toString(), user, true);
        }
    }
    
}
