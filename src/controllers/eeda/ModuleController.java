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
import models.eeda.FormBtn;
import models.eeda.profile.Module;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.module.ModuleService;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PingYinUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ModuleController extends Controller {

    private Log logger = Log.getLog(ModuleController.class);
    Subject currentUser = SecurityUtils.getSubject();

    // ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    @RequiresRoles("admin")
    @Before(EedaMenuInterceptor.class)
    public void index() {
        List<UserLogin> users = UserLogin.dao.find(
                "select u.* from user_login u "
                + " left join t_rbac_ref_group_user gu on gu.user_id=u.id"
                + " left join t_rbac_group g on gu.group_id=g.id"
                + " where g.office_id=?",
                LoginUserController.getLoginUser(this).getLong("office_id"));
        setAttr("users", users);
        render("/profile/module/moduleList.html");
    }

    public void getActiveModules() {
        String sql = "select id, module_name, parent_id, office_id, seq from eeda_modules "
                + "where status = '启用' and sys_only ='N' and office_id="
                + LoginUserController.getLoginUser(this).getLong("office_id");

        List<Record> modules = Db.find(sql);
        if (modules == null) {
            modules = Collections.EMPTY_LIST;
        } else {
            for (Record module : modules) {
                String fieldSql = "select f.* from eeda_structure s, eeda_field f where  f.structure_id = s.id and s.parent_id is null and s.module_id=?";
                List<Record> fields = Db.find(fieldSql, module.getLong("id"));
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
        String fieldName = "F" + field.getLong("id").toString() + "_"
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
        String parent_id = getPara("id");
        String cons = "";
        String sql = "select id, module_name, parent_id, office_id, seq, version, url from eeda_modules where delete_flag!='Y' and office_id="
                + LoginUserController.getLoginUser(this).getLong("office_id");

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
        String id = getPara("id");
        String parent_id = getPara("parent_id");
        String module_name = getPara("name");
        Long office_id = LoginUserController.getLoginUser(this)
                .getLong("office_id");

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
            Db.update("update eeda_form_define set name='"+module_name+"' where module_id ='"+module.getLong("id")+"'");
        }

        renderJson(module);
    }

    @Before(Tx.class)
    public void deleteModuleTree(){
    	String id = getPara("id");
    	String nodeName = getPara("nodeName");
    	int result1 = Db.update("update eeda_modules set delete_flag = 'Y' where id = ? and module_name = ?",id,nodeName);
    	int result2 = Db.update("update eeda_form_define set delete_flag = 'Y' where module_id = ? and name = ?",id,nodeName);
    	if(result1>0||result2>0){
    		renderJson("{\"result\":"+true+"}");
    	}
    }
    
    @Before(Tx.class)
    public void updateModuleSeq() {
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
                + LoginUserController.getLoginUser(this).getLong("office_id")
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
        Long module_id = Long.valueOf(dto.get("module_id").toString());
        String url = (String) dto.get("url");
        UserLogin user = LoginUserController.getLoginUser(this);

        Db.update(" update eeda_modules set url = ? where id=?", url, module_id);

        // handle info
        Long form_id = handleInfo(dto, module_id, user.getLong("office_id"));
        // handle fields and build/update table
        List<Map<String, String>> field_list = (ArrayList<Map<String, String>>) dto
                .get("fields");
        // DbUtils.handleList(field_list, form_id, Field.class, "form_id");
        ModuleService ms = new ModuleService(this);
        ms.processFieldType(field_list, form_id);

        //根据字段更新去更新表字段, 或没字段也要建空表
        Record formExistRec=Db.findFirst("SHOW TABLES LIKE 'form_"+form_id+"'");
        if("Y".equals(dto.get("field_update_flag"))||formExistRec==null){
            buildFormTable(form_id);
        }
        //处理自定义查询
        handleCustomSearch(dto, form_id);
        
        // 处理按钮
        if("Y".equals(dto.get("btn_update_flag"))){
        	handleBtns(dto, form_id);
        }
        
        // 处理事件
        if("Y".equals(dto.get("event_update_flag"))||"Y".equals(dto.get("editEvent_update_flag"))){
        	handleEvents(dto, form_id);
        }
        
        // 处理权限点
        List<Map<String, String>> permission_list = (ArrayList<Map<String, String>>) dto
                .get("permission_list");
        handlePermission(permission_list,module_id);
        // 处理岗位权限
        handleAuth(dto, module_id);
        // 处理打印模板
        handlePrintTemplate(dto, form_id);
        // 处理数据接口
        //ms.saveInterface(dto, form_id);
        // 处理图表
        ms.handleCharts(dto, form_id);
        
        Record orderRec = Db.findById("eeda_modules", module_id);
        List<Record> form_field_list = getFormFields(form_id);
//      List<Record> form_field_list = Db.find("select * from eeda_form_field where form_id = ?", form_id);
        orderRec.set("form_field_list", form_field_list);
        orderRec.set("permission_list", getPermissionList(module_id.toString()));
        orderRec.set("module_role_list", getAuthList(module_id.toString()));
        
        List<Record> btn_list_query = getFormBtns(form_id, "list");
        orderRec.set("btn_list_query", btn_list_query);
        List<Record> btn_list_edit = getFormBtns(form_id, "edit");
        orderRec.set("btn_list_edit", btn_list_edit);
        orderRec.set("custom_search_source", getCustomSearchSource(form_id));
        orderRec.set("custom_search_source_Condition", getCustomSearchCondition(form_id));
        orderRec.set("custom_search_cols", getCustomSearchCols(form_id));
        orderRec.set("custom_search_filter", getCustomSearchFilter(form_id));
        List<Record> editEventList = Db.find("select essvi.* from eeda_form_event_save_set_value_item essvi "
        									+ "left join eeda_form_event efe on essvi.event_id = efe.id where efe.form_id = ?", form_id);
        orderRec.set("editEventList", editEventList);
        renderJson(orderRec);
    }
    @SuppressWarnings("null")
    private void handlePrintTemplate(Map<String, ?> dto, Long form_id) {
        List<Map<String, String>> template_list = (ArrayList<Map<String, String>>) dto.get("print_template");
        
        for (Map<String, ?> field : template_list) {
            String id = field.get("id".toUpperCase()).toString();
            String is_delete = (String) field.get("is_delete");
            if("Y".equals(is_delete)){
                Db.deleteById("eeda_form_print_template", id);
                continue;
            }
            String name = (String) field.get("name"
                    .toUpperCase());
            String desc = (String) field.get("desc".toUpperCase());
            String content = (String) field.get("content".toUpperCase());
            
            Document doc = Jsoup.parseBodyFragment(content);
            Element body = doc.body();
            Elements tds = body.getElementsByTag("td");
            for (Element td : tds) {
            	td.html(td.text());
            }
            content = doc.body().html();
            Record itemRec = new Record();
            if (StrKit.isBlank(id)) {
                itemRec.set("form_id", form_id);
                itemRec.set("name", name);
                itemRec.set("desc", desc);
                itemRec.set("content", content);
                Db.save("eeda_form_print_template", itemRec);
            } else {
                itemRec = Db.findById("eeda_form_print_template", id);
                itemRec.set("name", name);
                itemRec.set("desc", desc);
                itemRec.set("content", content);
                Db.update("eeda_form_print_template", itemRec);
            }
        }
    }
    
    private void handleEvents(Map<String, ?> dto, Long form_id)
            throws InstantiationException, IllegalAccessException {
        Map<String, List> map = (LinkedTreeMap) dto.get("events");
        List<Double > deleted_event_list = map.get("node_delete_ids");
        for (Double  id : deleted_event_list) {
            Db.deleteById("eeda_form_event", id.longValue());
            //TODO:还要删掉子表的数据
        }
        List<Map<String, ?>> event_list = map.get("node_list");
        for (Map<String, ?> event : event_list) {
            if (event.get("id") != null) {
                Long id = ((Double) event.get("id")).longValue();
                Record rec = Db.findFirst(
                        "select * from eeda_form_event where id=?", id);
                rec.set("name", event.get("name"));
                rec.set("type", event.get("type"));
                rec.set("menu_type", event.get("menu_type".toUpperCase()));
                
                String type = (String) event.get("type");
                if ("open".equals(type)) {
                    saveEventOpen(event, id);
                }else if("set_css".equals(type)){
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventSetCss(event, id);
                } else if ("set_value".equals(type)) {
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventSetValue(event, rec.getLong("id"));
                } else if ("list_add_row".equals(type)) {
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventListAddRow(event, rec.getLong("id"));
                }else if("save".equals(type)){
                	ModuleService ms = new ModuleService(this);
                	ms.saveEventSaveSetValue(event, rec.getLong("id"));
                }
                Db.update("eeda_form_event", rec);
            } else {

                Record rec = new Record();
                rec.set("name", event.get("name"));
                rec.set("type", event.get("type"));
                rec.set("form_id", form_id);
                rec.set("btn_id", event.get("btn_id"));
                
                rec.set("menu_type", event.get("menu_type"));
               
                Db.save("eeda_form_event", rec);

                String type = (String) event.get("type");
                if ("open".equals(type)) {
                    saveEventOpen(event, rec.getLong("id"));
                } else if ("set_css".equals(type)) {
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventSetCss(event, rec.getLong("id"));
                } else if ("set_value".equals(type)) {
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventSetValue(event, rec.getLong("id"));
                } else if ("list_add_row".equals(type)) {
                    ModuleService ms = new ModuleService(this);
                    ms.saveEventListAddRow(event, rec.getLong("id"));
                }else if("save".equals(type)){
                	ModuleService ms = new ModuleService(this);
                	ms.saveEventSaveSetValue(event, rec.getLong("id"));
                }
            }
        }
    }

    private void saveEventOpen(Map<String, ?> event, Long event_id) {
        Long office_id = LoginUserController.getLoginUser(this)
                .getLong("office_id");
        Map<String, ?> openDto = (Map<String, ?>) event.get("openForm");
        if (openDto == null) {
            openDto = (Map<String, ?>) event.get("OPEN_FORM");
        }
        Record eventOpen = Db
                .findFirst(
                        "select * from eeda_form_event_open where event_id=?",
                        event_id);
        if (eventOpen != null) {
            eventOpen.set("condition",
                    openDto.get("condition") == null ? openDto.get("CONDITION")
                            : openDto.get("condition"));
            String moduleName = openDto.get("module_name") == null ? openDto
                    .get("MODULE_NAME").toString() : openDto.get("module_name").toString();
            eventOpen.set("module_name",moduleName);

            Record rec = Db.findFirst("select * from eeda_form_define fd, eeda_modules em"
                    + " where fd.module_id=em.id and fd.delete_flag!='Y' and fd.name=?"
                    + " and em.office_id=?", moduleName, office_id);
            eventOpen.set("module_id", rec.get("module_id"));
            eventOpen.set("open_type",
                    openDto.get("open_type") == null ? openDto.get("OPEN_TYPE")
                            : openDto.get("open_type"));
            Db.update("eeda_form_event_open", eventOpen);
            
            
        } else {
            eventOpen = new Record();
            eventOpen.set("event_id", event_id);
            eventOpen.set("condition", openDto.get("condition"));
            String moduleName = openDto.get("module_name").toString();
            eventOpen.set("module_name", moduleName);
            
            Record rec = Db.findFirst("select * from eeda_form_define fd, eeda_modules em"
                    + " where fd.module_id=em.id and fd.delete_flag!='Y' and fd.name=?"
                    + " and em.office_id=?", moduleName, office_id);
            eventOpen.set("module_id", rec.get("module_id"));
            eventOpen.set("open_type", openDto.get("open_type"));
            Db.save("eeda_form_event_open", eventOpen);
        }
    }

    private void handleCustomSearch(Map<String, ?> dto, Long form_id){
    	  Map<String, Object> customSearch = (Map<String, Object>) dto.get("customSearch");
    	  Map<String, Object> custom_search_source = (Map<String, Object>)customSearch.get("custom_search_source");
    	  if(custom_search_source!=null){
    		  List<Map<String, String>> block_arr = (ArrayList<Map<String, String>>)custom_search_source.get("block_arr");
    		  if(block_arr!=null && block_arr.size()>0){
    			  for(int i = 0;i<block_arr.size();i++){
    				  String id = block_arr.get(i).get("ID");
    				  String form_name = block_arr.get(i).get("FORM_NAME");
    				  String connect_type = block_arr.get(i).get("CONNECT_TYPE");
    				  String is_delete = block_arr.get(i).get("IS_DELETE");
    				  Record re = new Record();
    				  if(StringUtils.isBlank(id)){
    					  re.set("form_id", form_id);
    					  re.set("form_name", form_name);
    					  re.set("connect_type", connect_type);
    					  Db.save("eeda_form_custom_search_source", re);
    				  }else{
    					  re = Db.findById("eeda_form_custom_search_source", id);
    					  if(re!=null){
    						  if("Y".equals(is_delete)){
        						  Db.delete("eeda_form_custom_search_source", re);
        					  }else{
            					  re.set("form_name", form_name);
            					  re.set("connect_type", connect_type);
            					  Db.update("eeda_form_custom_search_source", re);
        					  }
    					  }
    				  }
    			  }
    		  }
    		  List<Map<String, String>> join_list = (ArrayList<Map<String, String>>)custom_search_source.get("join_list");
    		  if(join_list.size()>0){
    			  for(int i =0;i<join_list.size();i++){
    				  String id = join_list.get(i).get("ID");
    				  String form_left = join_list.get(i).get("FORM_LEFT");
    				  String form_left_field = join_list.get(i).get("FORM_LEFT_FIELD");
    				  String form_right = join_list.get(i).get("FORM_RIGHT");
    				  String form_right_field = join_list.get(i).get("FORM_RIGHT_FIELD");
    				  String operator = join_list.get(i).get("OPERATOR");
    				  String is_delete = join_list.get(i).get("IS_DELETE");
    				  Record re = new Record();
    				  if(StringUtils.isBlank(id)){
    					  re.set("form_id", form_id);
    					  re.set("form_left", form_left);
    					  re.set("form_left_field", form_left_field);
    					  re.set("form_right", form_right);
    					  re.set("form_right_field", form_right_field);
    					  re.set("operator", operator);
    					  Db.save("eeda_form_custom_search_source_condition", re);
    				  }else{
    					  re = Db.findById("eeda_form_custom_search_source_condition", id);
    					  if(re!=null){
    						  if("Y".equals(is_delete)){
        						  Db.delete("eeda_form_custom_search_source_condition", re);
        					  }else{
        						  re.set("form_left", form_left);
            					  re.set("form_left_field", form_left_field);
            					  re.set("form_right", form_right);
            					  re.set("form_right_field", form_right_field);
            					  re.set("operator", operator);
            					  Db.update("eeda_form_custom_search_source_condition", re);
        					  }
    					  }
    				  }
    			  }
    		  }
    	  }
    	  List<Map<String, String>> custom_search_cols = (ArrayList<Map<String, String>>)customSearch.get("custom_search_cols");
    	  if(custom_search_cols.size()>0){
    		  for(int i = 0;i<custom_search_cols.size();i++){
    			  String id = custom_search_cols.get(i).get("ID");
    			  String field_name = custom_search_cols.get(i).get("FIELD_NAME");
    			  String expression = custom_search_cols.get(i).get("EXPRESSION");
    			  String width = custom_search_cols.get(i).get("WIDTH");
    			  String hidden_flag = custom_search_cols.get(i).get("HIDDEN_FLAG");
    			  String is_delete = custom_search_cols.get(i).get("IS_DELETE");
    			  Record re = new Record();
    			  if(StringUtils.isBlank(id)){
    				  re.set("form_id", form_id);
    				  re.set("field_name",field_name);
    				  re.set("expression",expression);
    				  re.set("width",width);
    				  re.set("hidden_flag",hidden_flag);
    				  Db.save("eeda_form_custom_search_cols", re);
    			  }else{
    				  re = Db.findById("eeda_form_custom_search_cols", id);
    				  if(re!=null){
    					  if("Y".equals(is_delete)){
        					  Db.delete("eeda_form_custom_search_cols", re);
        				  }else{
            				  re.set("field_name",field_name);
            				  re.set("expression",expression);
            				  re.set("width",width);
            				  re.set("hidden_flag",hidden_flag);
            				  Db.update("eeda_form_custom_search_cols", re);
        				  }
    				  }
    			  }
    		  }
    	  }
    	  List<Map<String, String>> custom_search_filter = (ArrayList<Map<String, String>>)customSearch.get("custom_search_filter");
    	  if(custom_search_filter.size()>0){
    		  for(int i = 0;i<custom_search_filter.size();i++){
    			  String id = custom_search_filter.get(i).get("ID");
    			  String param_name = custom_search_filter.get(i).get("PARAM_NAME");
    			  String data_type = custom_search_filter.get(i).get("DATA_TYPE");
    			  String must_flag = custom_search_filter.get(i).get("MUST_FLAG");
    			  String default_value = custom_search_filter.get(i).get("DEFAULT_VALUE");
    			  String is_delete = custom_search_filter.get(i).get("IS_DELETE");
    			  Record re = new Record();
    			  if(StringUtils.isBlank(id)){
    				  re.set("form_id", form_id);
    				  re.set("param_name",param_name);
    				  re.set("data_type",data_type);
    				  re.set("must_flag",must_flag);
    				  re.set("default_value",default_value);
    				  Db.save("eeda_form_custom_search_filter", re);
    			  }else{
    				  re = Db.findById("eeda_form_custom_search_filter", id);
    				  if(re!=null){
    					  if("Y".equals(is_delete)){
        					  Db.delete("eeda_form_custom_search_filter", re);
        				  }else{
            				  re.set("param_name",param_name);
            				  re.set("data_type",data_type);
            				  re.set("must_flag",must_flag);
            				  re.set("default_value",default_value);
            				  Db.update("eeda_form_custom_search_filter", re);
        				  }
    				  }
    			  }
    		  }
    	  }
    }
    
    private void handleBtns(Map<String, ?> dto, Long form_id)
            throws InstantiationException, IllegalAccessException {
        List<Map<String, String>> btn_list = (ArrayList<Map<String, String>>) dto
                .get("btns");
        DbUtils.handleList(btn_list, form_id, FormBtn.class, "form_id");
    }
    
    @SuppressWarnings("null")
    private Long handleInfo(Map<String, ?> dto, Long module_id, Long office_id) {
        String tempalteContent = dto.get("template_content").toString();
        String appTempalteContent = dto.get("app_template_content").toString();
        Map<String, String> infoMap = (Map) dto.get("info");
        Record formRec = Db.findFirst(
                "select * from eeda_form_define where module_id=?", module_id);
        if("Y".equals(infoMap.get("is_home_index"))){
        	Db.update("update eeda_form_define set is_home_index = 'N' where is_home_index = 'Y'");
        }
        Document doc = Jsoup.parseBodyFragment(tempalteContent);
        Element body = doc.body();
        Elements tds = body.getElementsByTag("td");
        for (Element td : tds) {
            String tdHtml = td.html();
            if(tdHtml.indexOf("<a")>=0){
                td.html(tdHtml);
            }else{
                td.html(td.text());
            }
        }
        
        tempalteContent = doc.body().html();
        if (formRec != null) {
            formRec.set("name", infoMap.get("name"));
            formRec.set("code", infoMap.get("code"));
            formRec.set("type", infoMap.get("type"));
            formRec.set("is_public", infoMap.get("is_public"));
            formRec.set("is_home_index", infoMap.get("is_home_index"));
            formRec.set("is_single_record", infoMap.get("is_single_record"));
            formRec.set("template_content", tempalteContent);
            formRec.set("app_template", appTempalteContent);
            formRec.set("module_id", module_id);
            formRec.set("office_id", office_id);
            Db.update("eeda_form_define", formRec);
        } else {
            formRec = new Record();
            formRec.set("name", infoMap.get("name"));
            formRec.set("code", infoMap.get("code"));
            formRec.set("type", infoMap.get("type"));
            formRec.set("is_public", infoMap.get("is_public"));
            formRec.set("is_home_index", infoMap.get("is_home_index"));
            formRec.set("is_single_record", infoMap.get("is_single_record"));
            formRec.set("template_content", tempalteContent);
            formRec.set("app_template", appTempalteContent);
            formRec.set("module_id", module_id);
            formRec.set("office_id", office_id);
            Db.save("eeda_form_define", formRec);
        }
        return formRec.getLong("id");
    }

    private void handlePermission(List<Map<String, String>> list, Long module_id){
    	if(list!=null){
    		for (Map<String, String> rowMap : list) {//获取每一行
    			Permission permission = new Permission();
    			String rowId = (rowMap.get("id")==null?String.valueOf(rowMap.get("ID")):rowMap.get("id"));
    			String action = rowMap.get("action");
    			if(StringUtils.isEmpty(rowId) || "null".equals(rowId)){
	    			if(!"DELETE".equals(action)){
	    				DbUtils.setModelValues(rowMap, permission);
	    				permission.set("module_id", module_id);
	    				permission.save();	
	    			}
	    		}else{
	    				if("DELETE".equals(action)  ){//delete
	        				Model<?> deleteModel = permission.findById(rowId);
	        				//删除关联的role_permission表中的数据
	            			List<Record> role_permission_list = Db.find("select * from role_permission where permission_id = ?",rowId);
	            			if(role_permission_list.size()>0){
	            				for(Record re : role_permission_list){
	            					Db.update("delete from role_permission where id = ?", re.getLong("id"));
	            				}
	            			}
	            			//最后才删除 permission表的数据
	            			deleteModel.delete();
	            		}else{//UPDATE
	            			Model<?> updateModel = permission.findById(rowId);
	            			DbUtils.setModelValues(rowMap, updateModel);
	            			updateModel.update();
	            		}
	    		}
    		}
    	}
    }
    
    private void handleAuth(Map<String, ?> dto, Long module_id) {
        List<Map<String, ?>> auth_list = (ArrayList<Map<String, ?>>) dto
                .get("auth_list");
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
            } else {
                if ("DELETE".equals(action)) {// delete
                    Db.update("delete from module_role where id=?", rowId);

                    Db.update(
                            "delete from role_permission where module_role_id=?",
                            rowId);
                } else {// UPDATE
                    Record mr = Db.findById("module_role", rowId);
                    if(mr!=null){
                    	String mr_role_id = mr.getLong("role_id").toString();
                        if (!mr_role_id.equals(role_id)) {
                        	mr.set("role_id",role_id);
                        	Db.update("module_role", mr);
                        }
                        String mr_id = mr.getLong("id").toString();
                        List<Map<String, String>> permissionList = (List) map
                                .get("permission_list");
                        for (Map<String, ?> p : permissionList) {
                            String row_id = (String) p.get("id");
                            String permissionId = (String) p.get("permission_id");
                            String permissionCode = (String) p
                                    .get("permission_code");
                            boolean is_auth = (Boolean) p.get("is_authorize");
                            Record permission = Db.findById("permission", permissionId);
                            if(permission!=null){
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
    }

    public void searchFormBtns() {
        Long form_id = getParaToLong("form_id");
        String type = getPara("type");
        List<Record> list = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?",
                form_id, type);
        
        List<Record> page_btn_list = Db.find(
                "select * from eeda_form_field where form_id=? and field_type='按钮'",
                form_id);
        
        Record rec = new Record();
        rec.set("tool_bar_btns", list);
        rec.set("page_btns", page_btn_list);
        renderJson(rec);
    }

    public void searchFormBtnEvents() {

        Long form_id = getParaToLong("formId");
        List<Record> list = null;
        if ("值变化".equals(getPara("name"))) {
            list = Db
                    .find("select * from eeda_form_event where form_id=? and menu_type='value_change'",
                            form_id);
        }else if ("新增-打开表单后".equals(getPara("name"))) {
            list = Db
                    .find("select * from eeda_form_event where form_id=? and menu_type='default_event_add_after_open'",
                            form_id);
        }else{
            Long btn_id = getParaToLong("id");
            list = Db.find(
                    "select * from eeda_form_event where form_id=? and btn_id=?",
                    form_id, btn_id);
        }
        
        for (Record event : list) {
            String eventType = event.getStr("type");
            if ("open".equals(eventType)) {
                Record openRec = Db.findFirst(
                        "select * from eeda_form_event_open where event_id=?",
                        event.getLong("id"));
                event.set("open_form", openRec);
            }else if("set_css".equals(eventType)){
                Record cssRec = Db.findFirst(
                        "select * from eeda_form_event_css where event_id=?",
                        event.getLong("id"));
                List<Record> itemList = Db.find(
                        "select * from eeda_form_event_css_item where event_id=?",
                        event.getLong("id"));
                cssRec.set("set_field_list", itemList);
                event.set("set_css", cssRec);
            }else if("set_value".equals(eventType)){
                Record cssRec = Db.findFirst(
                        "select * from eeda_form_event_set_value where event_id=?",
                        event.getLong("id"));
                List<Record> itemList = Db.find(
                        "select * from eeda_form_event_set_value_item where event_id=?",
                        event.getLong("id"));
                if(cssRec!=null){
                    cssRec.set("set_field_list", itemList);
                    event.set("set_value", cssRec);
                }
            }else if("list_add_row".equals(eventType)){
                Record rec = Db.findFirst(
                        "select * from eeda_form_event_list_add_row where event_id=?",
                        event.getLong("id"));
                
                event.set("list_add_row", rec);
            }else if("save".equals(eventType)){
            	Record cssRec = Db.findFirst(
                        "select * from eeda_form_event_save_set_value where event_id=?",
                        event.getLong("id"));
            	List<Record> itemList = Db.find(
                         "select * from eeda_form_event_save_set_value_item where event_id=?",
                         event.getLong("id"));
            	cssRec.set("set_field_list", itemList);
                event.set("save", cssRec);
            }
        }

        renderJson(list);
    }

    private void searchHandle(Map<String, ?> dto, Long module_id) {
        String searchStr = (String) dto.get("search_obj");
        Map<String, ?> searchDto = new Gson()
                .fromJson(searchStr, HashMap.class);
        String viewName = searchDto.get("view_name").toString();
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
        if (event_list.size() == 0)
            return;

        // 先处理删除
        List<String> deleteIds = new ArrayList<String>();
        for (Map<String, ?> row : event_list) {
            if (row.get("id") == null)
                continue;
            String event_id = row.get("id").toString();
            deleteIds.add(event_id);
        }
        if (deleteIds.size() > 0)
            Db.update(
                    "delete from eeda_module_event where module_id=? and id not in(?)",
                    module_id, StringUtils.join(deleteIds, ","));

        for (Map<String, ?> row : event_list) {
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
                int updateCount = Db.update(sql, event_name, event_type,
                        event_script, id);
            } else {// insert
                String sql = "insert into eeda_module_event (module_id, event_name, event_type, event_script) values(?, ?, ?, ?)";
                Db.update(sql, module_id, event_name, event_type, event_script);
            }
        }
    }

    private void authHandle(Map<String, ?> dto, String module_id) {
        List<Map<String, ?>> auth_list = (ArrayList<Map<String, ?>>) dto
                .get("auth_list");
        if (auth_list.size() == 0)
            return;
        List<String> roleIds = new ArrayList<String>();
        for (Map<String, ?> row : auth_list) {
            if (row.get("role_id") == null)
                continue;
            String role_id = row.get("role_id").toString();
            roleIds.add(role_id);
        }
        if (roleIds.size() > 0)
            Db.update(
                    "delete from eeda_module_permission where module_id=? and role_id not in(?)",
                    module_id, StringUtils.join(roleIds, ","));

        for (Map<String, ?> row : auth_list) {

            List<Map<String, String>> role_auth = (List<Map<String, String>>) row
                    .get("role_auth_list");
            if (row.get("role_id") == null && role_auth.size() == 0)
                continue;
            String role_id = row.get("role_id").toString();
            logger.debug("role_id=" + role_id);

            for (Map<String, ?> role_auth_map : role_auth) {
                String permission_id = role_auth_map.get("id").toString();
                if (StringUtils.isEmpty(permission_id)) {
                    String action_name = role_auth_map.get("name").toString();
                    String sql = "select * from eeda_structure_action where module_id=? and action_name=?";
                    Record rec = Db.findFirst(sql, module_id, action_name);
                    if (rec != null) {
                        permission_id = rec.getLong("id").toString();
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
                    int updateCount = Db.update(sql, permission_id, is_auth,
                            null, rec.getLong("id"));
                } else {// insert
                    String sql = "insert into eeda_module_permission (module_id, role_id, permission_id, is_auth, office_id) values(?, ?, ?, ?, ?)";
                    Db.update(sql, module_id, role_id, permission_id, is_auth,
                            null);
                }
            }
        }
    }

    @Before(Tx.class)
    private void buildFormTable(Long form_id) {
        // module 运输单 id=13，那么table_name 生成： form_13
        String tableName = "form_" + form_id;

        // 每个子表中默认有ID, PARENT_form_ID, eeda_delete字段，请勿添加同名字段。
        String createTableSql = "CREATE TABLE if not exists `" + tableName
                + "` (" + " `id` BIGINT(20) NOT NULL AUTO_INCREMENT,"
//                + " `parent_form_id` BIGINT(20) NULL,"
                + " `eeda_delete` char(1) NOT NULL DEFAULT 'N',"
                + "PRIMARY KEY (`id`)) ENGINE = InnoDB";
        Db.update(createTableSql);

        
        
        String fieldSql = "select * from eeda_form_field where form_id=?";
        List<Record> fieldList = Db.find(fieldSql, form_id);
        for (Record field : fieldList) {
            String fieldName = "f" + field.getLong("id").toString() + "_"
                    + field.getStr("field_name");
            String createField = "";
            // 根据ID判断字段是否已存在
            Record oldFieldRec = Db.findFirst("show columns from " + tableName
                    + " like '" + "f" + field.getLong("id").toString() + "_%'");
            if ("日期".equals(field.getStr("field_type"))) {
                if (oldFieldRec != null) {
                    createField = "ALTER TABLE `" + tableName + "` "
                            + "CHANGE COLUMN `" + oldFieldRec.getStr("field")
                            + "` `" + fieldName
                            + "` date NULL DEFAULT NULL COMMENT ''";
                } else {
                    createField = "ALTER TABLE `" + tableName
                            + "` ADD COLUMN `" + fieldName
                            + "` date NULL COMMENT ''";
                }
            } else if ("日期时间".equals(field.getStr("field_type"))) {
                if (oldFieldRec != null) {
                    createField = "ALTER TABLE `" + tableName + "` "
                            + "CHANGE COLUMN `" + oldFieldRec.getStr("field")
                            + "` `" + fieldName
                            + "` TIMESTAMP NULL DEFAULT NULL COMMENT ''";
                } else {
                    createField = "ALTER TABLE `" + tableName
                            + "` ADD COLUMN `" + fieldName
                            + "` TIMESTAMP NULL COMMENT ''";
                }
            } else {
                if (oldFieldRec != null) {
                    if("varchar(255)".equals(oldFieldRec.getStr("TYPE")))
                        continue;
                    createField = "ALTER TABLE `" + tableName + "` "
                            + "CHANGE COLUMN `" + oldFieldRec.getStr("field")
                            + "` `" + fieldName
                            + "` VARCHAR(255) NULL DEFAULT NULL COMMENT ''";
                } else {
                    createField = "ALTER TABLE `" + tableName
                            + "` ADD COLUMN `" + fieldName
                            + "` VARCHAR(255) NULL COMMENT ''";
                }
            }
            Db.update(createField);
        }
        //删除字段
        String descSql = "desc form_"+form_id;
        List<Record> existFieldList = Db.find(descSql);
        for (Record existFieldRec : existFieldList) {
            boolean is_exist = false;
            String existFieldName=existFieldRec.getStr("field");
            if("id".equals(existFieldName)||"eeda_delete".equals(existFieldName))
                continue;
            for (Record field : fieldList) {
                String fieldName = "f" + field.getLong("id").toString() + "_"
                        + field.getStr("field_name");
                if(existFieldName.equals(fieldName)){
                    is_exist = true;
                    break;
                }
            }
            if(!is_exist){
                String sql = "ALTER TABLE `" + tableName
                        + "` DROP COLUMN `" + existFieldName
                        + "`";
                Db.update(sql);
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

        List<Record> structure_list = null;// getStructureRecs(module_id);
        Record formRec = getForm(module_id);

        List<Record> permission_list = getPermissionList(module_id);
        List<Record> auth_list = getAuthList(module_id);
        // String search_obj = getSearchObj(module_id);

        Record rec = new Record();
        rec.set("module_id", module_id);
        if (formRec != null) {
            Long form_id = formRec.getLong("id");
            rec.set("form", formRec);
            rec.set("form_fields", getFormFields(form_id));
            rec.set("custom_search_source",getCustomSearchSource(form_id));
            rec.set("custom_search_source_Condition",getCustomSearchCondition(form_id));
            rec.set("custom_search_cols",getCustomSearchCols(form_id));
            rec.set("custom_search_filter",getCustomSearchFilter(form_id));
            List<Record> btn_list_query = getFormBtns(form_id, "list");
            rec.set("btn_list_query", btn_list_query);
            List<Record> btn_list_edit = getFormBtns(form_id, "edit");
            rec.set("btn_list_edit", btn_list_edit);
            List<Record> print_template_list = getPrintTemplate(form_id);
            rec.set("print_template_list", print_template_list);
            List<Record> interface_list = getInterface(form_id);
            rec.set("interface_list", interface_list);
            Record charts = getCharts(form_id);
            rec.set("charts", charts);
        }else {
            rec.set("module_name_py", PingYinUtil.getFirstSpell(module.getStr("module_name")));
        }
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
    
    private Record getCharts(Long formId) {
        Record rec = Db.findFirst(
                "select * from eeda_form_charts where form_id=?",formId);
        return rec;
    }
    
    private List<Record> getInterface(Long formId) {
        List<Record> recList = Db.find(
                "select * from eeda_form_interface where form_id=?",
                formId);
        for (Record intFace : recList) {
            long id = intFace.getLong("id");
            Record rec = new Record();
            List<Record> sourceList = Db.find(
                    "select * from eeda_form_interface_source where interface_id=?",
                    id);
            rec.set("block_list", sourceList);
            
            List<Record> joinList = Db.find(
                    "select * from eeda_form_interface_source_join where interface_id=?",
                    id);
            rec.set("join_list", joinList);
            intFace.set("source", rec);
            
            List<Record> colList = Db.find(
                    "select * from eeda_form_interface_cols where interface_id=?",
                    id);
            intFace.set("cols", colList);
            
            List<Record> filterList = Db.find(
                    "select * from eeda_form_interface_filter where interface_id=?",
                    id);
            intFace.set("filter", filterList);
        }
        return recList;
    }
    
    private List<Record> getPrintTemplate(Long formId) {
        List<Record> recList = Db.find(
                "select * from eeda_form_print_template where form_id=?",
                formId);
        return recList;
    }

    private Record getForm(String module_id) {
        Record rec = Db.findFirst(
                "select * from eeda_form_define where module_id=?", module_id);
        return rec;
    }

    private List<Record> getFormFields(Long formId) {
        List<Record> recList = Db.find(
                "select * from eeda_form_field where form_id=?", formId);
        // 获取字段类型的对应record
        for (Record field : recList) {
            String type = field.getStr("field_type");
            if ("复选框".equals(type)) {
                buildCheckBox(field);
            }else if ("自动编号".equals(type)) {
                Record ref = Db
                        .findFirst(
                                "select * from eeda_form_field_type_auto_no where field_id=?",
                                field.getLong("id"));
                List<Record> item_list = Db
                        .find("select * from eeda_form_field_type_auto_no_item where field_id=?",
                                field.getLong("id"));
                if (item_list.size() > 0)
                    ref.set("item_list", item_list);
                field.set("auto_no", ref);
            }else if ("从表引用".equals(type)) {
                Record ref = Db
                        .findFirst(
                                "select * from eeda_form_field_type_detail_ref where field_id=?",
                                field.getLong("id"));

                List<Record> condition_list = Db
                        .find("select * from eeda_form_field_type_detail_ref_join_condition where field_id=?",
                                field.getLong("id"));
                if (condition_list.size() > 0)
                    ref.set("join_condition", condition_list);
                
                List<Record> field_list = Db
                        .find("select * from eeda_form_field_type_detail_ref_display_field where field_id=? order by sort_no",
                                field.getLong("id"));
                if (field_list.size() > 0)
                    ref.set("display_field", field_list);
                field.set("detail_ref", ref);
            }else if ("字段引用".equals(type)) {
                Record ref = Db
                        .findFirst(
                                "select * from eeda_form_field_type_ref where field_id=?",
                                field.getLong("id"));
                field.set("ref", ref);
                
                List<Record> field_list = Db
                        .find("select * from eeda_form_field_type_ref_item where field_id=?",
                                field.getLong("id"));
                if (field_list.size() > 0)
                    ref.set("item_list", field_list);
                
            }else if("下拉列表".equals(type)){
            	 List<Record> field_list = Db.find("select * from eeda_form_field_type_dropdown where field_id=?",field.getLong("id"));
            	 if (field_list.size() > 0)
            		 field.set("dropdown_list", field_list);
            }
        }
        return recList;
    }
    
    private List<Record> getCustomSearchSource(Long form_id){
    	List<Record> sourceList = Db.find("select * from eeda_form_custom_search_source where form_id = ?",form_id);
    	return sourceList;
    }
    
    private List<Record> getCustomSearchCondition(Long form_id){
    	List<Record> sourceConditionList = Db.find("select * from eeda_form_custom_search_source_condition where form_id = ?",form_id);
    	return sourceConditionList;
    }
    
    private List<Record> getCustomSearchCols(Long form_id){
    	List<Record> colsList = Db.find("select * from eeda_form_custom_search_cols where form_id = ?",form_id);
    	return colsList;
    }
    
    private List<Record> getCustomSearchFilter(Long form_id){
    	List<Record> filterList = Db.find("select * from eeda_form_custom_search_filter where form_id = ?",form_id);
    	return filterList;
    }

    private void buildCheckBox(Record field) {
        Record checkBox = Db
                .findFirst(
                        "select * from eeda_form_field_type_checkbox where field_id=?",
                        field.getLong("id"));

        List<Record> list = Db
                .find("select * from eeda_form_field_type_checkbox_item where field_id=?",
                        field.getLong("id"));
        if (list.size() > 0)
            checkBox.set("item_list", list);

        field.set("CHECK_BOX", checkBox);
    }

    private List<Record> getFormBtns(Long formId, String type) {
        List<Record> recList = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?",
                formId, type);
        return recList;
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
        UserLogin user = LoginUserController.getLoginUser(this);
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
                    + " from eeda_structure_action sa, eeda_module_permission mp, t_rbac_role er, eeda_user_role ur "
                    + " where sa.module_id = mp.module_id and sa.id = mp.permission_id and mp.role_id =er.id and mp.role_id = ur.role_id"
                    + " and sa.module_id=? and ur.user_name=?";

            action_list = Db.find(authSql, module_id, LoginUserController
                    .getLoginUser(this).getStr("user_name"));

        }
        return action_list;
    }

    private List<Record> getEventList(Long form_id) {
        List<Record> event_list = Db.find(
                "select * from eeda_form_event where form_id=?", form_id);
        if (event_list == null) {
            event_list = Collections.EMPTY_LIST;
        }

        for (Record event : event_list) {
            String eventType = event.getStr("type");
            if ("open".equals(eventType)) {
                Record openRec = Db.findFirst(
                        "select * from eeda_form_event_open where event_id=?",
                        event.getInt("id"));
                event.set("open_form", openRec);
            }
        }
        return event_list;
    }

    private List<Record> buildValueRecList(Map dto) {
        List<Record> setValueRecList = new ArrayList<Record>();
        List<Map> setValueList = (List) dto.get("SETVALUELIST");
        for (Map expMap : setValueList) {
            Record expRec = new Record();
            String exp = expMap.get("EXP").toString();
            String exp_key = expMap.get("EXP").toString();
            exp_key = exp_key.replaceAll("<-", "=");
            String target_field = exp_key.split("=")[0];// 报关商品.总价
            Record field = getFieldByName(target_field);
            String transFieldName = "t_" + field.getLong("structure_id") + ".f"
                    + field.getLong("id") + "_" + field.getStr("field_name");
            exp_key = exp_key.replaceAll(target_field, transFieldName);

            String exp_post = exp_key.split("=")[1];// "报关商品.数量*报关商品.单价"
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
        List<Record> authRecs = Db
                .find("select mr.*, r.code, r.name from module_role mr, t_rbac_role r where mr.module_id=? and mr.role_id=r.id",
                        module_id);
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
                        structure.getLong("id"), field_name);

        return field;
    }

    public static String getFieldSqlNameByName(String name) {
        Record rec = getFieldByName(name);
        String str = "f" + rec.getLong("id") + "_" + rec.getStr("field_name");
        return str;
    }

    public void getRoleList() {
        Long office_id = LoginUserController.getLoginUser(this)
                .get("office_id");
        List<Record> recs = Db.find("select * from t_rbac_role where office_id=?",
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
    
    /**
     * 检查是否有设为首页的
     */
    public void checkExistIndex(){
    	String module_id = getPara("module_id");
    	List<Record> existIndexList = Db.find("select id,name from eeda_form_define where is_home_index='Y' and module_id!=?",module_id);
    	renderJson(existIndexList);
    }
    /**
     * 获取所有forms
     */
    public void getAllForms(){
        Long officeId = LoginUserController.getLoginUser(this).getLong("office_id");
        List<Record> formList = Db.find("select ef.id, ef.code, ef.name from eeda_modules em, eeda_form_define ef "
                + "where em.id=ef.module_id and em.parent_id is not null and em.delete_flag ='N' and em.office_id=?", officeId);
        renderJson(formList);
        
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", 0);
        orderListMap.put("recordsTotal", formList.size());
        orderListMap.put("recordsFiltered", formList.size());

        orderListMap.put("data", formList);
        renderJson(orderListMap);
    }
    
    public void getFormFields(){
        Long officeId = LoginUserController.getLoginUser(this).getLong("office_id");
        String form_name = getPara("form_name");
        List<Record> formList = Db.find("select ef.name form_name, eff.id, eff.form_id, eff.field_name, eff.field_display_name, "
                + "eff.field_type, em.office_id from eeda_form_define ef, eeda_form_field eff, eeda_modules em "
                + "where ef.id=eff.form_id and ef.module_id=em.id and em.office_id=? and ef.name=?", officeId, form_name);
        renderJson(formList);
        
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", 0);
        orderListMap.put("recordsTotal", formList.size());
        orderListMap.put("recordsFiltered", formList.size());

        orderListMap.put("data", formList);
        renderJson(orderListMap);
    }
}
