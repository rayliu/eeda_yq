package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.UserLogin;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.StringTemplateResourceLoader;

import com.google.gson.Gson;
//import cache.EedaServiceCache;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.form.FormService;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FormController extends Controller {
    private Log logger = Log.getLog(FormController.class);
    private static GroupTemplate gt=null;
    
    private GroupTemplate getGroupTemplate() throws IOException{
        if(gt==null){
          StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader(); 
          Configuration cfg = Configuration.defaultConfiguration();
          gt = new GroupTemplate(resourceLoader, cfg);
        }
        return gt;
    }
    @Before({EedaMenuInterceptor.class, Tx.class})
    @SuppressWarnings("unchecked")
    public void index() throws IOException {
        logger.debug("thread["+Thread.currentThread().getName()+
                "] -------------Eeda form---------------");
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_id = getPara(0);
        String action = getPara(1);
        Long order_id = getParaToLong(2);
        //以下为表单的标准 action
        
        //list 跳转到form 列表查询 页面;
        //doQuery ajax 列表查询 动作; 
        
        //add 跳转到新增页面; 
        //doAdd 新增动作
        
        //edit 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        
        //doDelete 表单删除的动作
        
        //click 表单按钮的动作
        //valueChange 表单按钮的动作, 参数 {field_name, value, old_value}
        //tableConfig 从表表单的配置, 参数 {field_id_list}
        
        setAttr("action", action);
        setAttr("module_id", module_id);
        
        if("doQuery".equals(action)){
            
            Map listMap = queryForm(Long.valueOf(module_id));
            renderJson(listMap);
            return;
        }
        
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return;
        }
        Long form_id = formRec.getLong("id");
        logger.debug("-------------Eeda module:"+module_id+", form_id:"+form_id+", action: "+action+"---------------");
        
        if("eventConfig".equals(action)){
            List<Record> itemList = Db.find("select * from eeda_form_event where menu_type='default_event_add_after_open'"
                    + " and form_id=?", form_id);
            for (Record record : itemList) {
                String type = record.getStr("type");
                if("set_value".equals(type)){
                    Record cssRec = Db.findFirst("select * from eeda_form_event_set_value where event_id=?", record.getLong("id"));
                    record.set("set_value", cssRec);
                    
                    List<Record> list = Db.find("select * from eeda_form_event_set_value_item where "
                            + " event_id=?", record.getLong("id"));
                    for (Record rec : list) {
                        String name = rec.getStr("name");
                        Record field_rec = FormService.getFieldName(name.split("\\.")[0], name.split("\\.")[1]);//获取数据库对应的名称: f59_xh
                        String field_name = "form_"+field_rec.getLong("form_id")+"-f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
                        rec.set("field_name", field_name);
                        
                        String value = rec.getStr("value");
                        if("系统变量.当前用户名".equals(value)){
                            String userName=user.getStr("c_name");
                            rec.set("value", userName);
                        }
                    }
                    record.set("set_value_item", list);
                }
            }
            
            renderJson(itemList);
        }else if("tableConfig".equals(action)){
            String jsonStr = getPara("field_id_list");
            Gson gson = new Gson();
            ArrayList<String> fieldIdList = gson.fromJson(jsonStr, ArrayList.class);
            List<Record> list = new ArrayList<Record>();
            for (String fieldId : fieldIdList) {
                Record rec = new Record();
                List<Record> itemList = Db.find("select * from eeda_form_field_type_detail_ref_display_field where "
                        + " field_id=?", fieldId);
                for (Record record : itemList) {
                    String target_field_name = record.getStr("target_field_name");
                    String form_name = target_field_name.split("\\.")[0];
                    String field_display_name = target_field_name.split("\\.")[1];
                    Record field_rec = FormService.getFieldName(form_name, field_display_name);//获取数据库对应的名称: f59_xh
                    record.set("field_name", "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name"));
                    record.set("field_display_name", field_rec.getStr("field_display_name"));
                    record.set("field_type", field_rec.getStr("field_type"));
                    
                    if("字段引用".equals(field_rec.getStr("field_type"))){
                        Record ref = Db.findFirst(
                                "select * from eeda_form_field_type_ref where field_id=?", field_rec.getLong("id"));
                        record.set("ref", ref);
                        
                        String target_form_name = ref.getStr("ref_form");
                        Record refForm = Db.findFirst(
                                "select * from eeda_form_define where name=?", target_form_name);
                        
                        String target_form_field = ref.getStr("ref_field");
                        Record target_field_rec = FormService.getFieldName(target_form_field.split("\\.")[0], target_form_field.split("\\.")[1]);//获取数据库对应的名称: f59_xh
                        String field_name = "f"+target_field_rec.get("id")+"_"+target_field_rec.getStr("field_name");
//                        + " target_form='"++"' target_field_name='"+field_name+"'"
                        ref.set("target_form_id", refForm.getLong("id"));
                        ref.set("target_field_name", field_name);
                    }
                }
                rec.set("display_field_list", itemList);
                list.add(rec);
            }
            renderJson(list);
        }else if("valueChange".equals(action)){
            List<Record> recList = Db.find("select * from eeda_form_event where "
                    + " menu_type='value_change' and form_id=?", form_id);
            for (Record event : recList) {
                if("set_css".equals(event.getStr("type"))){
                    Record cssRec = Db.findFirst("select * from eeda_form_event_css where event_id=?", event.getLong("id"));
                    List<Record> cssItemList = Db.find("select * from eeda_form_event_css_item where "
                            + " event_id=?", event.getLong("id"));
                    cssRec.set("set_field_list", cssItemList);
                    event.set("set_css", cssRec);
                }
            }
            renderJson(recList);
        }else if("click".equals(action)){
            Long btn_id = order_id;
            List<Record> recList = Db.find("select * from eeda_form_event where "
                    + " btn_id=?", btn_id);
            for (Record event : recList) {
                if("open".equals(event.getStr("type"))){
                    Record rec = Db.findFirst("select * from eeda_form_event_open where event_id=?", event.getLong("id"));
                    event.set("open", rec);
                }else if("print".equals(event.getStr("type"))){
                    List<Record> template_list = Db.find("select * from eeda_form_print_template where form_id=?", form_id);
                    event.set("template_list", template_list);
                }
            }
            renderJson(recList);
        }else if(!action.startsWith("do")){
            if("edit".equals(action)){
                edit(form_id, order_id, formRec);
            }else if("view".equals(action)){
                
            }else if("add".equals(action)){
                edit(form_id, null, formRec);
            }else if("list".equals(action)){
                list(form_id);
                render("/eeda/form/listTemplate.html");
                return;
            }
            
            render("/eeda/form/template.html");
        }else{
            Record rec = new Record();
            if("doGet".equals(action)){
                rec = getForm(form_id, order_id);
                renderJson(rec);
            }else if ("doAdd".equals(action) || "doUpdate".equals(action)){
                rec = saveForm();
                renderJson(rec);
            }
            
        }
    }
    
    private Record saveForm(){
        Record rec = new Record();
        String jsonStr=getPara("data");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String module_id = (String) dto.get("module_id");
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return rec;
        }
        Long form_id = formRec.getLong("id");
        for (Entry<String, ?> entry : dto.entrySet()) { 
            String key = entry.getKey();
            if(key.startsWith("form_"+form_id)){
                String colName = key.split("-")[1];
                String value = String.valueOf(entry.getValue()).trim();
                rec.set(colName, value);
            }
        }
        
        String order_id = (String) dto.get("order_id");
        if (StrKit.isBlank(order_id)) {
            Db.save("form_"+form_id, rec);
        }else{
            rec.set("id", order_id);
            Db.update("form_"+form_id, rec);
        }
        
        //处理从表保存
        List<Map<String, ?>> detailList = (ArrayList<Map<String, ?>>)dto.get("detail_tables");
        for (Map<String, ?> detail : detailList) {
             String table_id = (String) detail.get("table_id");
             String field_id = table_id.split("_")[2];
             //1.通过field_id 找到对应的明细表 form_id
             Record detailRec = Db.findFirst("select distinct form.* from eeda_form_field f, eeda_form_field_type_detail_ref ref, eeda_form_define form "
                +" where f.id = ref.field_id "
                +" and ref.target_form_name = form.name"
                +" and f.id = ?", field_id);
             //2.通过field_id 找到对应的明细表 的 关联字段, 现在先做单个//TODO
             Record detailConditionRec = Db.findFirst("select ref.* from eeda_form_field f, eeda_form_field_type_detail_ref_join_condition ref "
                     +" where f.id = ref.field_id "
                     +" and f.id = ?", field_id);
             String field_from = detailConditionRec.getStr("field_from");
             String field_to = detailConditionRec.getStr("field_to");
             //主表关联值
             Record field_rec = FormService.getFieldName(field_from.split("\\.")[0], field_from.split("\\.")[1]);//获取数据库对应的名称: f59_xh
             String field_from_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
             Object from_field_value = rec.get(field_from_name);
             //从表关联值
             Record field_to_rec = FormService.getFieldName(field_to.split("\\.")[0], field_to.split("\\.")[1]);//获取数据库对应的名称: f59_xh
             String field_to_name = "f"+field_to_rec.getLong("id")+"_"+field_to_rec.getStr("field_name");
             
             Long detail_form_id = detailRec.getLong("id");
             List<Map<String, ?>> detailDataList = (List<Map<String, ?>>)detail.get("data_list");
             for (Map<String, ?> rowMap : detailDataList) {
                 Record rowRec = new Record();
                 for (Entry<String, ?> entry : rowMap.entrySet()) { 
                     String colName = entry.getKey();
                     String value = String.valueOf(entry.getValue()).trim();
                     if("id".equals(colName)){
                         if(!StrKit.isBlank(value)){
                             rowRec.set(colName, Long.valueOf(value));
                         }
                     }else{
                         rowRec.set(colName, value);
                     }
                 }
                 rowRec.set(field_to_name, from_field_value);//关联字段 赋值
                 if(rowRec.get("id")==null){
                     Db.save("form_"+detail_form_id, rowRec);
                 }else{
                     Db.update("form_"+detail_form_id, rowRec);
                 }
            }
        }
        return rec;
    }
    private List<Record> list(Long form_id){
        setAttr("btnList", getFormBtns(form_id, "list"));
        
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
        setAttr("form_id", form_id);
        setAttr("field_list", fieldList);
        setAttr("field_list_json", JsonKit.toJson(fieldList));
        return fieldList;
    }
    
    private List<Record> getFormBtns(Long formId, String type) {
        List<Record> recList = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?", formId, type);
        return recList;
    }
    
    private Record getForm(Long form_id, Long order_id){
        Record rec = Db.findFirst("select * from form_"+form_id+" where "
                + " id=?", order_id);
        List<Record> detailList= new ArrayList<Record>();
        
        List<Record> fieldList = Db.find("select distinct field.id field_id, form.id form_id, form.name, cond.field_from, cond.field_to "
                    +"from eeda_form_field field, eeda_form_field_type_detail_ref ref,"
                    + " eeda_form_field_type_detail_ref_join_condition cond,"
                    +"    eeda_form_define form"
                    +" where "
                    +" field.id = ref.field_id"
                    +" and field.id = cond.field_id"
                    +" and ref.target_form_name = form.name"
                    +" and field.field_type='从表引用' "
                    +" and field.form_id=?", form_id);
        for (Record record : fieldList) {
            Long d_form_id = record.getLong("form_id");
            Long field_id = record.getLong("field_id");
            
            String field_from = record.getStr("field_from");
            String field_to = record.getStr("field_to");
            //主表关联值
            Record field_rec = FormService.getFieldName(field_from.split("\\.")[0], field_from.split("\\.")[1]);//获取数据库对应的名称: f59_xh
            String field_from_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
            Object from_field_value = rec.get(field_from_name);
            //从表关联值
            Record field_to_rec = FormService.getFieldName(field_to.split("\\.")[0], field_to.split("\\.")[1]);//获取数据库对应的名称: f59_xh
            String field_to_name = "f"+field_to_rec.getLong("id")+"_"+field_to_rec.getStr("field_name");
            
            List<Record> dataList = Db.find("select * from form_"+d_form_id+" where "+field_to_name+"=?", from_field_value);
            
            Record table_record = new Record(); 
            table_record.set("table_id", "detail_table_"+field_id);
            table_record.set("data_list", dataList);
            
            detailList.add(table_record);
        }
        rec.set("detail_tables", detailList);
        return rec;
    }
    
    private Map queryForm(Long form_id){
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from form_"+form_id+" where 1=1 "; 
        List<Record> list = Db.find(sql);
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        return orderListMap;
    }

    private void edit(Long form_id, Long order_id, Record formRec) throws IOException{
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=?", form_id);
        
        Record orderRec = Db.findFirst("select * from form_"+form_id+" where id=?", order_id);
        String form_name = formRec.getStr("name");
        
        String template_content = formRec.getStr("template_content");
        for (Record fieldRec : fieldList) {
            String fieldDisplayName=fieldRec.getStr("field_display_name");
            String fieldName=fieldRec.getStr("field_name");
            String replaceNameOrigin = "#{"+form_name+"."+fieldDisplayName+"}";
            String fieldType = fieldRec.getStr("field_type");
            String read_only = fieldRec.getStr("read_only");
            String replaceNameDest ="";
            String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
            
            if("编码".equals(fieldType)){
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<input type='text' name='"+inputId+"' class='form-control'  placeholder='系统自动生成'>";//
            }else if("文本".equals(fieldType)){
                String disabled = "";
                if("Y".equals(read_only)){
                    disabled = "disabled";
                }
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<input type='text' name='"+inputId+"' class='form-control' "+disabled+" >";
            }else if("全国城市".equals(fieldType)){
                String disabled = "";
                if("Y".equals(read_only)){
                    disabled = "disabled";
                }
                
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class=''>"+
                        "    <input id='"+inputId+"_province' type='text' class='province' field_type='list' value='' style='display:none;'/>"+
                        "    <input id='"+inputId+"' type='text' field_type='list' value='' style='display:none;'/>"+
                        "    <input type='text' class='form-control city_input'"+
                        "    name='"+inputId+"' "+
                        "    placeholder='请选择城市' >"+
                        "    <div id='"+inputId+"_list' class='area-list pull-right dropdown-menu default dropdown-scroll' tabindex='-1'  "+
                        "    style='top: 35%; left: 2%; display: none;'>"+
                        "        <div class='area-list-title'>"+
                        "            <input data-id='0' data-level='0' type='button' value='省份' class='this'>"+
                        "            <input data-id='0' data-level='1' type='button' value='城市'>"+
                        "            <input data-id='0' data-level='2' type='button' value='县区'>"+
                        "            <span class='tips'>如不需选县区，请点击外面空白区域</span>"+
                        "        </div>"+
                        "        <div class='area-list-content' style='clear:both;'>"+
                        "            "+
                        "        </div>"+
                        "    </div>"+
                        "        "+
                        "    <ul id='"+inputId+"_list——1' class='pull-right dropdown-menu default dropdown-scroll' tabindex='-1' style='top: 35%; left: 2%;'>"+
                        "    </ul>"+
                        "</div>";
            }else if("日期".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + " <label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<span class='add-on'>"
                        + " <i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar'></i>"
                        +"</span> "
                        + " <input id='"+inputId+"' name='"+inputId+"' class='form-control' type='text' data_type='date'/>"
                        + "</div> ";
            }else if("日期时间".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + " <label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<span class='add-on'>"
                        + " <i class='fa fa-calendar' data-time-icon='icon-time' data-date-icon='icon-calendar'></i>"
                        +"</span> "
                        + " <input id='"+inputId+"' name='"+inputId+"' class='form-control' type='text' data_type='date_time'/>"
                        + "</div> ";
            }else if("复选框".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_checkbox(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else if("从表引用".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_detail(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else if("字段引用".equals(fieldType)){
                FormService fs = new FormService(this);
                replaceNameDest = fs.processFieldType_ref(form_name, fieldRec, fieldRec.getLong("id"));
                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else{
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<input type='text' name='"+inputId+"' class='form-control'>";
            }
            template_content = template_content.replace(replaceNameOrigin, replaceNameDest);
        }
        
        //不需要在这里替换值, 应该是把UI组件+id 显示出来, 再通过JSON set value进去
//        GroupTemplate gt = getGroupTemplate();
//        Template t = gt.getTemplate(template_content);
//        t.binding("t_"+form_id, orderRec); 
//        String form_content = t.render(); 
        
        formRec.set("fields", fieldList);
        formRec.set("template_content", "");
        setAttr("form_define", JsonKit.toJson(formRec));
        
        setAttr("order_id", order_id);
        
        setAttr("form_content", template_content);
        
        setAttr("btnList", getFormBtns(form_id, "edit"));
    }
    
}
