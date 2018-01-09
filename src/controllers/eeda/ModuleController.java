package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Permission;
import models.RolePermission;
import models.UserLogin;
import models.eeda.profile.Module;
import models.eeda.profile.ModuleRole;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ModuleController extends Controller {

    private Log logger = Log.getLog(ModuleController.class);
    Subject currentUser = SecurityUtils.getSubject();

    // ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    @RequiresRoles("admin")
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
    	
        List<UserLogin> users = UserLogin.dao.find("select * from user_login where office_id=?", user.get("office_id"));
        setAttr("users", users);
        render("/profile/module/moduleList.html");
    }

    public void getActiveModules() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        String sql = "select id, module_name, parent_id, office_id, seq from eeda_modules "
                + "where status = '启用' and sys_only ='N' and office_id="
                + user.get("office_id");

        List<Record> modules = Db.find(sql);
        if (modules == null) {
            modules = Collections.EMPTY_LIST;
        } else {
            for (Record module : modules) {
                String fieldSql = "select f.* from eeda_structure s, eeda_field f where  f.structure_id = s.id and s.parent_id is null and s.module_id=?";
                List<Record> fields = Db.find(fieldSql, module.get("id"));
                if (fields != null && fields.size() > 0) {
                    module.set("field_list", fields);
                    module.set("structure_id", fields.get(0)
                            .get("structure_id"));
                }
            }
        }
        renderJson(modules);
    }

    /**
     * 获取下拉列表和数据列表
     */
    public void getDataList() {
        Record s = getStructureByName("数据列表");
        Record field = getFieldByName("数据列表.名称");
        String fieldName = "F" + field.get("id").toString() + "_"
                + field.getStr("field_name");
        String sql = "select id," + fieldName + " name from t_"
                + s.getLong("id");
        // +"where office_id="+LoginUserController.getLoginUser().get("office_id");

        List<Record> modules = Db.find(sql);
        if (modules == null) {
            modules = Collections.EMPTY_LIST;
        }
        for (Record m : modules) {
            String m_sql = "select * from t_" + s.getLong("id") + " where id=?";
            Record sRec = Db.findFirst(m_sql, m.getLong("id"));

            String colStr = sRec.getStr("F48_XSZD");
            String[] colArr = colStr.split(";");
            List fList = new ArrayList();
            for (String fName : colArr) {
                Record f = getFieldByName(fName);
                fList.add(f);
            }
            m.set("FIELD_LIST", fList);
        }
        renderJson(modules);
    }

    public void searchModule() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        String parent_id = getPara("id");
        String sql = "SELECT "
                +" id,"
                +" module_name,"
                +" parent_id,"
                +" office_id,"
                +" seq,"
                +" version,"
                +" url,"
                +" is_public,"
                +" if((select count(1)>0 from eeda_modules m where m.parent_id = e.id), 'Y','N') is_parent"
            +" FROM "
             +"    eeda_modules e where office_id="
                + user.get("office_id");

        List<Record> modules = null;
        if (StringUtils.isEmpty(parent_id)) {
            modules = Db.find(sql + " and parent_id is null order by seq");
        } else {
            modules = Db
                    .find(sql + " and parent_id =? order by seq", parent_id);
        }
        renderJson(modules);
    }

    public void addModule() {
        String parent_id = getPara("parent_id");
        String module_name = getPara("name");
        UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        Long office_id = user
                .get("office_id");

        Module module = new Module();
        if (!StringUtils.isEmpty(parent_id)) {
            module.set("parent_id", parent_id);
        }
        module.set("module_name", module_name);
        module.set("office_id", office_id);

        String sql = "select max(seq) seq from eeda_modules where office_id="
                + office_id;
        Module m = Module.dao.findFirst(sql);
        if (m.getDouble("seq") != null) {
            module.set("seq", m.getDouble("seq") + 1);
        } else {
            module.set("seq", 1);
        }
        module.save();

        renderJson(module);
    }

    public void updateModule() {
        String id = getPara("id");
        String module_name = getPara("module_name");
        String parent_id = getPara("parent_id");

        Module module = Module.dao.findById(id);
        if (module != null) {
            if (!StringUtils.isEmpty(parent_id)) {
                module.set("parent_id", parent_id);
            }
            module.set("module_name", module_name);
            module.update();
        }

        renderJson(module);
    }

    @Before(Tx.class)
    public void updateModuleSeq() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        String node_id = getPara("node_id");
        String target_node_id = getPara("target_node_id");
        String move_type = getPara("move_type");

        Module module = Module.dao.findById(node_id);
        Module target_module = Module.dao.findById(target_node_id);

        if (module != null && target_module != null) {
            // 移动单据到另一个模块下
            if (module.getLong("parent_id") != target_module
                    .getLong("parent_id")) {
                module.set("parent_id", target_module.getLong("parent_id"))
                        .update();
            }

            if ("inner".equals(move_type)) {
                module.set("parent_id", target_node_id).update();
            } else if ("prev".equals(move_type)) {
                double target_seq = target_module.getDouble("seq");
                module.set("seq", target_seq - 0.5).update();
            } else {
                double target_seq = target_module.getDouble("seq");
                module.set("seq", target_seq + 0.5).update();
            }
        }

        // 重新算序号
        String sql = "select id, module_name, parent_id, office_id, seq from eeda_modules where office_id="
                + user.get("office_id")
                + " order by seq";
        List<Module> modules = Module.dao.find(sql);
        int newSeq = 1;
        for (Module m : modules) {
            m.set("seq", newSeq).update();
            newSeq++;
        }

        renderJson(module);
    }

    @Before(Tx.class)
    public void saveStructure() throws InstantiationException,
            IllegalAccessException {
        String jsonStr = getPara("params");

        Gson gson = new Gson();
        Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);
        String module_id = (String) dto.get("module_id");
        String is_public = ((Boolean)dto.get("is_public")==true?"Y":"N");
        String url = (String) dto.get("url");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user == null)
   			return;

        Db.update(" update eeda_modules set url = ?, is_public=? where id=?", url, is_public, module_id);

        List<Map<String, String>> permission_list = (ArrayList<Map<String, String>>) dto
                .get("permission_list");
        DbUtils.handleList(permission_list, module_id, Permission.class,
                "module_id");

        List<Map<String, ?>> auth_list = (ArrayList<Map<String, ?>>) dto
                .get("auth_list");
        if(auth_list != null){
        for (Map<String, ?> map : auth_list) {
            String rowId = (String) map.get("id");
            String role_id = (String) map.get("role_id");
            String role_code = (String) map.get("role_code");
            String action = (String) map.get("action");
            if (StringUtils.isEmpty(rowId)) {
                if ("CREATE".equals(action)) {

                    Db.update(
                            "insert into module_role(module_id, role_id) values(?, ?)",
                            module_id, role_id);

                    Record rec = Db
                            .findFirst(
                                    "select id from module_role where module_id=? and role_id=?",
                                    module_id, role_id);
                    long module_role_id = rec.getLong("id");

                    List<Map<String, String>> permissionList = (List) map
                            .get("permission_list");
                    if(permissionList != null){
                    	for (Map<String, ?> p : permissionList) {
                            String permissionId = (String) p.get("permission_id");
                            String permissionCode = (String) p
                                    .get("permission_code");
                            
                            boolean is_auth = (Boolean) p.get("is_authorize");
                            RolePermission rp = new RolePermission();
                            rp.set("module_id", module_id);
                            rp.set("role_id", role_id);
                            rp.set("role_code", role_code);
                            rp.set("permission_id", permissionId);
                            rp.set("permission_code", permissionCode);
                            rp.set("module_role_id", module_role_id);
                            rp.set("is_authorize", is_auth);
                            rp.save();
                        }
                    }
                }
            } else {
                if ("DELETE".equals(action)) {// delete
                    Db.update("delete from module_role where id=?", rowId);

                    Db.update(
                            "delete from role_permission where module_role_id=?",
                            rowId);
                } else {// UPDATE
                    ModuleRole mr = ModuleRole.dao.findById(rowId);
                    String mr_role_id = mr.getLong("role_id").toString();
                    if (!mr_role_id.equals(role_id)) {
                        mr.set("role_id", role_id).save();
                    }
                    String mr_id = mr.getLong("id").toString();
                    List<Map<String, String>> permissionList = (List) map
                            .get("permission_list");
                    if(permissionList != null){
                    	for (Map<String, ?> p : permissionList) {
                    		String row_id = (String) p.get("id");
                    		String permissionId = (String) p.get("permission_id");
                    		String permissionCode = (String) p
                    				.get("permission_code");
                    		boolean is_auth = (Boolean) p.get("is_authorize");
                    		RolePermission rp = RolePermission.dao.findById(row_id);
                    		if (rp != null) {
                    			rp.set("module_id", module_id);
                    			rp.set("role_id", role_id);
                    			rp.set("role_code", role_code);
                    			rp.set("permission_id", permissionId);
                    			rp.set("permission_code", permissionCode);
                    			rp.set("is_authorize", is_auth);
                    			rp.set("module_role_id", mr_id);
                    			rp.update();
                    		} else {
                    			rp = new RolePermission();
                    			rp.set("module_id", module_id);
                    			rp.set("role_id", role_id);
                    			rp.set("role_code", role_code);
                    			rp.set("permission_id", permissionId);
                    			rp.set("permission_code", permissionCode);
                    			rp.set("is_authorize", is_auth);
                    			rp.set("module_role_id", mr_id);
                    			rp.save();
                    		}
                    	}
                    }
                }
            }
        }
        }
        Record orderRec = Db.findById("eeda_modules", module_id);
        renderJson(orderRec);
    }

    private void searchHandle(Map<String, ?> dto, String module_id) {
        String searchStr = (String) dto.get("search_obj");
        Map<String, ?> searchDto = new Gson()
                .fromJson(searchStr, HashMap.class);
        String viewName = null;
        if(searchDto != null){
        	viewName = searchDto.get("view_name").toString();
        }
        if (StringUtils.isEmpty(viewName)) {
            Db.update(
                    "delete from eeda_module_customize_search where module_id=?",
                    module_id);
            return;
        }
        Record module = Db.findFirst(
                "select * from eeda_module_customize_search where module_id=?",
                module_id);
        if (module == null) {
            Db.update(
                    "insert into eeda_module_customize_search(module_id, setting_json) values(?, ?)",
                    module_id, searchStr);
        } else {
            Db.update(
                    "update eeda_module_customize_search set setting_json=? where module_id=?",
                    searchStr, module_id);
        }

    }

    private void eventHandle(Map<String, ?> dto, String module_id) {
        List<Map<String, ?>> event_list = (ArrayList<Map<String, ?>>) dto
                .get("event_list");
        if (event_list == null)
            return;
        
        if (event_list.size() == 0)
            return;

        // 先处理删除
        List<String> deleteIds = new ArrayList<String>();
        for (Map<String, ?> row : event_list) {
        	if(row != null){
        		if (row.get("id") == null)
        			continue;
        		String event_id = row.get("id").toString();
        		deleteIds.add(event_id);
        	}
        }
        if (deleteIds.size() > 0)
            Db.update(
                    "delete from eeda_module_event where module_id=? and id not in(?)",
                    module_id, StringUtils.join(deleteIds, ","));

        for (Map<String, ?> row : event_list) {
        	if(row != null){
        		String id = row.get("id").toString();
                String event_name = row.get("event_name").toString();
                String event_type = row.get("event_type").toString();
                String event_script = row.get("event_script").toString();
                Record rec = Db
                        .findFirst(
                                "select * from eeda_module_event where module_id=? and id=? ",
                                module_id, id);
                if (rec != null) {// update
                    String sql = "update eeda_module_event set event_name=?, event_type=?, event_script=? where id=?";
                    Db.update(sql, event_name, event_type,
                            event_script, id);
                } else {// insert
                    String sql = "insert into eeda_module_event (module_id, event_name, event_type, event_script) values(?, ?, ?, ?)";
                    Db.update(sql, module_id, event_name, event_type, event_script);
                }
        	}
        }
    }

    private void authHandle(Map<String, ?> dto, String module_id) {
        List<Map<String, ?>> auth_list = (ArrayList<Map<String, ?>>) dto
                .get("auth_list");
        if (auth_list == null)
            return;
        if (auth_list.size() == 0)
            return;
        List<String> roleIds = new ArrayList<String>();
        for (Map<String, ?> row : auth_list) {
        	if(row != null){
        		if (row.get("role_id") == null)
        			continue;
        		String role_id = (String)row.get("role_id");
        		roleIds.add(role_id);
        	}
        }
        if (roleIds.size() > 0)
            Db.update(
                    "delete from eeda_module_permission where module_id=? and role_id not in(?)",
                    module_id, StringUtils.join(roleIds, ","));

        for (Map<String, ?> row : auth_list) {
        	if(row == null){
        		continue;
        	}
            List<Map<String, String>> role_auth = (List<Map<String, String>>) row
                    .get("role_auth_list");
            if(role_auth == null){
            	continue;
            }
            if (row.get("role_id") == null && role_auth.size() == 0)
                continue;
            String role_id = (String)row.get("role_id");
            logger.debug("role_id=" + role_id);
            
            for (Map<String, ?> role_auth_map : role_auth) {
                String permission_id = (String)role_auth_map.get("id");
                if (StringUtils.isEmpty(permission_id)) {
                    String action_name = (String)role_auth_map.get("name");
                    String sql = "select * from eeda_structure_action where module_id=? and action_name=?";
                    Record rec = Db.findFirst(sql, module_id, action_name);
                    if (rec != null) {
                        permission_id = rec.get("id").toString();
                    }
                }
                String is_auth = role_auth_map.get("bAuth") == Boolean.TRUE ? "Y"
                        : "N";
                Record rec = Db
                        .findFirst(
                                "select * from eeda_module_permission where module_id=? and role_id=? and permission_id=?",
                                module_id, role_id, permission_id);
                if (rec != null) {// update
                    String sql = "update eeda_module_permission set permission_id=?, is_auth=?, office_id=? where id=?";
                    Db.update(sql, permission_id, is_auth,
                            null, rec.get("id"));
                } else {// insert
                    String sql = "insert into eeda_module_permission (module_id, role_id, permission_id, is_auth, office_id) values(?, ?, ?, ?, ?)";
                    Db.update(sql, module_id, role_id, permission_id, is_auth,
                            null);
                }
            }
        }
    }

    @Before(Tx.class)
    private void activateModule(String module_id) {
        logger.debug("start to generate tables....");
        Db.update(" update eeda_modules set status = '启用' where id=?",
                module_id);
        // module 运输单 id=13，那么table_name 生成： T_13
        // find table record
        String structureSql = "select * from eeda_structure where module_id=?";
        List<Record> sList = Db.find(structureSql, module_id);

        for (Record structure : sList) {
            String structureId = structure.get("id").toString();

            String tableName = "t_" + structureId;

            // 每个子表中默认有ID, PARENT_ID两个字段，请勿添加同名字段。
            String createTableSql = "CREATE TABLE if not exists `" + tableName
                    + "` (" + " `id` BIGINT(20) NOT NULL AUTO_INCREMENT,"
                    + " `parent_id` BIGINT(20) NULL,"
                    + " `ref_t_id` BIGINT(20) NULL ,"
                    + " `eeda_delete` char(1) NOT NULL DEFAULT 'N',"
                    + "PRIMARY KEY (`id`))";
            Db.update(createTableSql);

            String fieldSql = "select * from eeda_field where structure_id=?";
            List<Record> fieldList = Db.find(fieldSql, structureId);
            for (Record field : fieldList) {
                String fieldName = "F" + field.get("id").toString() + "_"
                        + field.getStr("field_name");
                String createField = "";
                // 根据ID判断字段是否已存在
                Record oldFieldRec = Db.findFirst("show columns from "
                        + tableName + " like '" + "F"
                        + field.get("id").toString() + "_%'");
                if ("日期编辑框".equals(field.getStr("field_type"))) {
                    if (oldFieldRec != null) {
                        createField = "ALTER TABLE `" + tableName + "` "
                                + "CHANGE COLUMN `"
                                + oldFieldRec.getStr("field") + "` `"
                                + fieldName
                                + "` TIMESTAMP NULL DEFAULT NULL COMMENT ''";
                    } else {
                        createField = "ALTER TABLE `" + tableName
                                + "` ADD COLUMN `" + fieldName
                                + "` TIMESTAMP NULL COMMENT ''";
                    }
                } else {
                    if (oldFieldRec != null) {
                        createField = "ALTER TABLE `" + tableName + "` "
                                + "CHANGE COLUMN `"
                                + oldFieldRec.getStr("field") + "` `"
                                + fieldName
                                + "` VARCHAR(255) NULL DEFAULT NULL COMMENT ''";
                    } else {
                        createField = "ALTER TABLE `" + tableName
                                + "` ADD COLUMN `" + fieldName
                                + "` VARCHAR(255) NULL COMMENT ''";
                    }
                }
                Db.update(createField);
            }
        }
    }

    public void getOrderStructure() {
        String module_id = getPara("module_id");
        Record rec = getOrderStructureDto(module_id);
        renderJson(rec);
    }

    public Record getOrderStructureDto(String module_id) {
        Record module = Db.findFirst("select * from eeda_modules where id=?",
                module_id);
        String sys_only = module.getStr("sys_only");

        //List<Record> structure_list = null;// getStructureRecs(module_id);
        // List<Record> action_list = getActionList(module_id);
        // List<Record> event_list = getEventList(module_id);
        List<Record> permission_list = getPermissionList(module_id);
        List<Record> auth_list = getAuthList(module_id);
        // String search_obj = getSearchObj(module_id);

        Record rec = new Record();
        rec.set("module_id", module_id);
        rec.set("module_version", module.get("version"));
        rec.set("sys_only", sys_only);
        rec.set("module_name", module.get("module_name"));
        // rec.set("structure_list", structure_list);
        // rec.set("action_list", action_list);
        // rec.set("event_list", event_list);
        rec.set("permission_list", permission_list);
        rec.set("auth_list", auth_list);
        // rec.set("search_obj", search_obj);
        return rec;
    }

    private String getSearchObj(String module_id) {
        Record rec = Db.findFirst(
                "select * from eeda_module_customize_search where module_id=?",
                module_id);
        String returnStr = "";
        if (rec != null) {
            returnStr = rec.getStr("setting_json");
        }
        return returnStr;
    }

    private List<Record> getActionList(String module_id) {
        List<Record> action_list = null;
        if (true) {// user.isAdmin()
            String authSql = "select  sa.*, 'Y' is_auth from eeda_structure_action sa where sa.module_id=? ";
            action_list = Db.find(authSql, module_id);
            if (action_list.size() == 0) {// 如果为0，则取系统默认的几个按钮出来
                action_list = Db
                        .find("select * from eeda_structure_action where module_id is null");
            }
        } else {
            // 获取user的权限,看看是否有对应权限
            String authSql = "select  sa.*, mp.is_auth, mp.role_id, er.name role_name, ur.user_name "
                    + " from eeda_structure_action sa, eeda_module_permission mp, role er, eeda_user_role ur "
                    + " where sa.module_id = mp.module_id and sa.id = mp.permission_id and mp.role_id =er.id and mp.role_id = ur.role_id"
                    + " and sa.module_id=? and ur.user_name=?";

            action_list = Db.find(authSql, module_id, LoginUserController
                    .getLoginUser(this).getStr("user_name"));

        }
        return action_list;
    }

    private List<Record> getEventList(String module_id) {
        List<Record> event_list = Db.find(
                "select * from eeda_module_event where module_id=?", module_id);
        if (event_list == null) {
            event_list = Collections.EMPTY_LIST;
        }

        for (Record record : event_list) {
            String event_script = record.getStr("EVENT_SCRIPT");
            if (StringUtils.isEmpty(event_script))
                continue;
            List<Record> commandRecList = new ArrayList<Record>();

            List<Map> commandList = new Gson().fromJson(event_script,
                    new TypeToken<List<Map>>() {
                    }.getType());
            for (Map dto : commandList) {
                Record commandRec = new Record();

                String commandName = (String)dto.get("COMMAND_NAME");
                commandRec.set("COMMAND_NAME", commandName);
                // TODO condition 没处理

                Map operation_obj = (Map) dto.get("OPERATION_OBJ");
                String operation_obj_exp = null;
                if(operation_obj != null){
                	operation_obj_exp = (String)operation_obj.get("EXP");
                }
                
                String operation_obj_exp_key = operation_obj_exp;

                String[] objs = operation_obj_exp.split(";");
                for (String fieldName : objs) {
                    Record field = getFieldByName(fieldName);
                    String transFieldName = "t_"
                            + field.getLong("structure_id") + ".f"
                            + field.getLong("id") + "_"
                            + field.getStr("field_name");
                    operation_obj_exp_key = operation_obj_exp_key.replaceAll(
                            fieldName, transFieldName);
                }
                Record operation_obj_rec = new Record();
                operation_obj_rec.set("EXP", operation_obj_exp);
                operation_obj_rec.set("EXP_KEY", operation_obj_exp_key);

                commandRec.set("OPERATION_OBJ", operation_obj_rec);

                List<Record> setValueRecList = buildValueRecList(dto);
                commandRec.set("setValueList", setValueRecList);

                commandRecList.add(commandRec);
            }

            record.set("event_script", commandRecList);
        }
        return event_list;
    }

    private List<Record> buildValueRecList(Map dto) {
        List<Record> setValueRecList = new ArrayList<Record>();
        List<Map> setValueList = (List) dto.get("SETVALUELIST");
        if(setValueList == null){
        	return setValueRecList;
        }
        for (Map expMap : setValueList) {
            Record expRec = new Record();
            String exp = (String)expMap.get("EXP");
            String exp_key = (String)expMap.get("EXP");
            exp_key = exp_key.replaceAll("<-", "=");
            String target_field = exp_key.split("=")[0];// 报关商品.总价
            Record field = getFieldByName(target_field);
            String transFieldName = "t_" + field.getLong("structure_id") + ".f"
                    + field.getLong("id") + "_" + field.getStr("field_name");
            exp_key = exp_key.replaceAll(target_field, transFieldName);

            //String exp_post = exp_key.split("=")[1];// "报关商品.数量*报关商品.单价"
            // Stack<String> paramStack =
            // ExpCalculator.getParamsStack(exp_post);
            // for (String paramFieldName : paramStack) {
            // if(paramFieldName.indexOf(".")==-1)
            // continue;
            // Record paramField = getFieldByName(paramFieldName);
            // String transParamFieldName =
            // "t_"+paramField.getLong("structure_id")+".f"+paramField.getLong("id")+"_"+paramField.getStr("field_name");
            // exp_key = exp_key.replaceAll(paramFieldName,
            // transParamFieldName);
            // }
            expRec.set("exp", exp);
            expRec.set("exp_key", exp_key);
            setValueRecList.add(expRec);
        }
        return setValueRecList;
    }

    private List<Record> getPermissionList(String module_id) {
        List<Record> authRecs = Db.find(
                "select * from permission where module_id=?", module_id);
        return authRecs;
    }

    private List<Record> getAuthList(String module_id) {
        List<Record> authRecs = Db.find(
                "select mr.*, r.code, r.name from module_role mr, role r where mr.module_id=? and mr.role_id=r.id", module_id);
        for (Record record : authRecs) {
            long module_role_id = record.getLong("id");
            List<Record> pRecs = Db
                    .find("select rp.id, rp.permission_id, rp.permission_code, p.name permission_name, rp.is_authorize from role_permission rp, permission p"
                            + " where rp.module_role_id=? and rp.permission_id=p.id",
                            module_role_id);
            record.set("permission_list", pRecs);
        }
        return authRecs;
    }

    private Record getStructureRecs(String module_id) {
        Record mRec = Db.findFirst(
                "select * from eeda_module where module_id=?", module_id);
        return mRec;
    }

    // 针对字段设置生成预览页面
    // @Before(EedaMenuInterceptor.class)
    public void preview() {
        String module_id = getPara();
        setAttr("module_id", module_id);

        Record module = Db.findFirst("select * from eeda_modules where id=?",
                module_id);
        setAttr("module_version", module.get("version"));
        render("/profile/module/editOrder.html");
    }

    public void getSysSetting() {
        String code = getPara("code");
        List<Record> recs = Db.find("select * from eeda_setting where code =?",
                code);
        if (recs == null)
            recs = Collections.EMPTY_LIST;
        renderJson(recs);
    }

    public void getSysVar() {
        List<Record> recs = Db
                .find("select id, name from eeda_setting where code ='sys_var_default'");
        renderJson(recs);
    }

    public void getSysAutoNum() {
        List<Record> recs = Db.find("select id, name from eeda_auto_no");
        renderJson(recs);
    }

    public void getSysDataSource() {
        List<Record> recs = Db.find("select id, name from eeda_structure");
        renderJson(recs);
    }

    public void getFieldsByStructureId() {
        String structure_id = getPara("structure_id").trim();
        List<Record> fieldsRecs = Db.find(
                "select * from eeda_field where structure_id=?", structure_id);

        if (fieldsRecs == null)
            fieldsRecs = Collections.EMPTY_LIST;
        renderJson(fieldsRecs);
    }

    public void getStructureByName() {
        String name = getPara("name").trim();
        Record rec = getStructureByName(name);
        renderJson(rec);
    }

    public static Record getStructureByName(String name) {
        return Db.findFirst(
                "select id, name, module_id from eeda_structure where name =?",
                name);
    }

    /*
     * 传进来的参数应该是：运输单.单号
     */
    public void getFieldByName() {
        String name = getPara("name").trim();

        Record field = getFieldByName(name);
        if (field == null)
            field = new Record();
        renderJson(field);
    }

    public static Record getFieldByName(String name) {
        String structure_name = name.split("[.]")[0];
        String field_name = name.split("[.]")[1];

        Record structure = getStructureByName(structure_name);
        Record field = Db
                .findFirst(
                        "select * from eeda_field where structure_id=? and field_display_name =?",
                        structure.get("id"), field_name);

        return field;
    }

    public static String getFieldSqlNameByName(String name) {
        Record rec = getFieldByName(name);
        String str = "f" + rec.getLong("id") + "_" + rec.getStr("field_name");
        return str;
    }

    public void getRoleList() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user == null)
   			return;
        Long office_id = user
                .get("office_id");
        List<Record> recs = Db.find("select * from role where office_id=?",
                office_id);
        renderJson(recs);
    }

    /**
     * 数据列表的json
     */
    public void getDataListStructureByNameId() {
        String name = getPara("name").trim();
        String id = getPara("id").trim();
        Record dto = getDataListStructureByNameId(name, id);

        renderJson(dto);
    }

    private Record getDataListStructureByNameId(String name, String id) {
        Record rec = getStructureByName(name);

        String sql = "select * from t_" + rec.getLong("id") + " where id=?";
        Record sRec = Db.findFirst(sql, id);

        String ds = sRec.getStr("F47_SJY");// TODO: how to dynamic?
        Record dsRec = getStructureByName(ds);
        String structure_id = dsRec.getLong("id").toString();

        String searchStr = sRec.getStr("F50_CXZD");
        String[] searchArr = searchStr.split(";");

        List conditionList = new ArrayList();
        for (String fieldName : searchArr) {
            Record field = getFieldByName(fieldName);
            conditionList.add(field);
        }

        String colStr = sRec.getStr("F48_XSZD");
        String[] colArr = colStr.split(";");
        List colList = new ArrayList();
        for (String fieldName : colArr) {
            Record field = getFieldByName(fieldName);
            colList.add(field);
        }

        Record dto = new Record();
        dto.set("structure_id", structure_id);
        dto.set("condition_list", conditionList);
        dto.set("col_list", colList);
        return dto;
    }

    public void getCustomizeView() {
        String viewName = getPara("name");
        Record rec = Db.findFirst("select * from eeda_sql_views where name=?",
                viewName);
        if (rec != null) {
            String sqlViewName = rec.getStr("sql_name");
            Record viewRec = Db.findFirst("select * from " + sqlViewName
                    + " where id=1");
            String[] cols = viewRec.getColumnNames();
            // Record colsRec = new Record();
            // colsRec.set("colArr", cols);
            renderJson(cols);
        } else {
            renderJson(Collections.EMPTY_LIST);
        }
    }

    public void orderDelete() {
        String sId = getPara("structure_id");
        String orderId = getPara("order_id");
        int count = Db.update("update t_" + sId
                + " set eeda_delete='Y' where id=?", orderId);
        if (count == 1) {
            renderText("OK");
        } else {
            renderText("failed");
        }
    }
}
