package controllers.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.form.event.EventService;

public class FormService {
    private Controller cont = null;
    public FormService(Controller cont){
        this.cont = cont;
    } 
    
    public static Record getFieldName(String form_name, String feild_display_name, Long office_id){
        Record rec = Db.findFirst("select f.* from eeda_form_define form, eeda_form_field f "
        		+ "where form.id = f.form_id and form.name=? and f.field_display_name=? and form.office_id=?", form_name, feild_display_name, office_id);

        return rec;
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_btn(String form_name, Record fieldRec, Long field_id){
        String returnStr = "<button type='button' class='btn btn-success' id='form_"+fieldRec.getLong("form_id")+"-btn_"+field_id+"'>"+
                fieldRec.getStr("field_display_name")+"</button>";
       
        return returnStr;
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_checkbox(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        Record checkBox = Db.findFirst(
                "select * from eeda_form_field_type_checkbox where field_id=?", field_id);
        
        List<Record> list = Db.find(
                "select * from eeda_form_field_type_checkbox_item where field_id=?", field_id);
        returnStr = "<label class='form-label'>"+fieldDisplayName+"</label>"
                + " <div class='formControls skin-minimal col-xs-8 col-sm-8'>";
        int index = 0;
        for (Record r : list) {
            index++;
            String name = r.getStr("name");
            String code = r.getStr("code");
            String isDefault = r.getStr("is_default");
            String checked = "";
            if("Y".equals(isDefault)){
                checked = "checked";
            }
            String checkboxStr = "";
            if("Y".equals(checkBox.get("is_single_check"))){
            	checkboxStr = "<label class='radio-inline'>"
                         + "<input type='radio' class='ml-10' origin_name='"+form_name+"-"+fieldDisplayName
                         +"' name='form_"+form_id+"-f"+field_id+"_"+fieldName+"' id='"+fieldName+"' value='"+name+"' "+checked+"/>"
                         + name+"</label>";
            }else{
            	checkboxStr = "<div class='radio-box'>"
                        + "     <input type='checkbox' origin_name='"+form_name+"-"+fieldDisplayName
                        +"'          name='form_"+form_id+"-f"+field_id+"_"+fieldName+"' id='"+fieldName+index+"' value='"+name+"' "+checked+"/>"
                        + "     <label for='"+fieldName+index+"'>"+name+"</label>"
                        +"</div>";
            }
            returnStr+=checkboxStr;
        }
        return returnStr+"</div>";
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_ref(String form_name, Record fieldRec, Long field_id, Long office_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        Record ref = Db.findFirst(
                "select * from eeda_form_field_type_ref where field_id=?", field_id);
        String displayType = ref.getStr("display_type");
        String target_form_name = ref.getStr("ref_form");
        Record refForm = Db.findFirst(
                "select * from eeda_form_define where name=? and office_id=?", target_form_name, office_id);
        //回填字段
//        String target_form_field = ref.getStr("ref_field");
//        Record field_rec = FormService.getFieldName(target_form_field.split("\\.")[0], target_form_field.split("\\.")[1]);//获取数据库对应的名称: f59_xh
        
        
        String inputId = "form_"+form_id+"-f"+field_id+"_"+fieldName.toLowerCase();
        String target_search_field_name = "";
        List<Record> itemList = Db.find(
                "select * from eeda_form_field_type_ref_item where field_id=?", field_id);
        for (Record record : itemList) {
            String from_name = record.getStr("from_name");
            if(from_name==null) continue;
            Record rec = FormService.getFieldName(from_name.split("\\.")[0], from_name.split("\\.")[1], office_id);
            String t_field_name = "f"+rec.get("id")+"_"+rec.getStr("field_name");//获取数据库对应的名称: f59_xh
            
            target_search_field_name += (","+t_field_name);//查询的字段
            
            record.set("from_field_name", t_field_name);
            //回填字段
            String to_name = record.getStr("to_name");
            if(StrKit.notBlank(to_name)){
                if(to_name.indexOf("\\.")==-1){
                    to_name=form_name+"."+to_name;
                }
                Record value_rec = FormService.getFieldName(to_name.split("\\.")[0], to_name.split("\\.")[1], office_id);
                String v_field_name = "f"+value_rec.get("id")+"_"+value_rec.getStr("field_name");//获取数据库对应的名称: f59_xh
                record.set("to_field_name", v_field_name);
            }
        }
        
        String listJson = JsonKit.toJson(itemList);
        if(StrKit.notBlank(target_search_field_name)){
            target_search_field_name=target_search_field_name.substring(1);//去掉第一个，
        }
        if("dropdown".equals(displayType)){
            returnStr = "<label class='search-label'>"+fieldDisplayName+"</label>"
                    + "<div class='formControls col-xs-8 col-sm-8'>"
                    + " <input type='text' name='"+inputId+"' class='input-text' autocomplete='off' placeholder='请选择' eeda_type='drop_down'"
                    + "    target_form='"+refForm.getLong("id")+"' target_field_name='"+target_search_field_name+"'"
                    + "    item_list='"+listJson+"'>"
                    + "</div>"
                    + "<div class='dropDown'>"
                    + "     <ul id='"+inputId+"_list' class='dropDown-menu menu radius box-shadow'>"
                    + "</div>";
        }else{
            returnStr = "<label class='search-label'>"+fieldDisplayName+"</label>"
                    + "<div class='formControls col-xs-8 col-sm-8'>"
                    + " <input type='text' name='"+inputId+"' class='input-text' autocomplete='off' placeholder='请选择' eeda_type='pop'"
                    + "    target_form='"+refForm.getLong("id")+"' target_field_name='"+target_search_field_name+"'"
                    + "    item_list='"+listJson+"'>"
                    + "</div>";
        }
        return returnStr;
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_detail(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Record ref = Db.findFirst(
                "select * from eeda_form_field_type_detail_ref where field_id=?", field_id);
        
        List<Record> condition_list = Db.find(
                "select * from eeda_form_field_type_detail_ref_join_condition where field_id=?", field_id);
        
        List<Record> display_list = Db.find(
                "select * from eeda_form_field_type_detail_ref_display_field where field_id=? order by sort_no", field_id);
        String fieldStr = "<th></th>";//默认第一列是放按钮的
        
        for (Record r : display_list) {
            String name = r.getStr("target_field_name");
            if(name.indexOf(".")>0){
                fieldStr+="<th>"+name.split("\\.")[1]+"</th>";
            }else{
                fieldStr+="<th>"+name+"</th>";
            }
        }
        returnStr = "<table id='detail_table_"+field_id+"' type='dynamic' class='table table-striped table-bordered table-hover display' style='width:100%;'>"
                +"    <thead class='eeda'>"
                +"        <tr>"
                + fieldStr
                +"        </tr>"
                +"    </thead>"
                +"    <tbody>"
                +"      "
                +"    </tbody>"
                +"</table>";
        return returnStr;
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_dropdown(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName = fieldRec.getStr("field_display_name");
        String fieldName = fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
        List<Record> dropdown_list = Db.find(
                "select * from eeda_form_field_type_dropdown where field_id=? order by sequence", field_id);
        
        returnStr = "<label class='form-label'>"+fieldDisplayName+"</label>"
                + " <div class='formControls skin-minimal col-xs-8 col-sm-8'>"
                + " <select id='"+inputId+"' name='"+inputId+"' class='form-control input-text'>";
        String dropdownStr = "";
        for (Record r : dropdown_list) {
            String name = r.getStr("name");
            dropdownStr += "<option value='"+name+"'>"+name+"</option>";
        }
        returnStr+=dropdownStr;
        return returnStr+"</select></div>";
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_imgUpload(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName = fieldRec.getStr("field_display_name");
        String fieldName = fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
        returnStr = "<label class='form-label'>"+fieldDisplayName+"</label>"
            + "<span style='width:30%;' class='btn-upload'>"
            + "  <a href='javascript:void();' class='btn btn-primary size-S radius'><i class='Hui-iconfont'>&#xe642;</i>图片</a>"
            + "  <input type='file' id='fileupload"+fieldRec.get("id")+"' multiple name='img_files' class='input-file'>"
            + "</span>"
            + "<div id='f"+fieldRec.get("id")+"' name='upload' style='margin-top:1%;'></div>";
        return returnStr;
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_fileUpload(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName = fieldRec.getStr("field_display_name");
        String fieldName = fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
        returnStr = "<label class='form-label'>"+fieldDisplayName+"</label>"
                + "  <span style='width:30%;' class='btn-upload'>"
                + "    <a href='javascript:void();' class='btn btn-primary size-S radius'><i class='Hui-iconfont'>&#xe642;</i>上传文件</a>"
                + "    <input type='file' id='fileupload"+fieldRec.get("id")+"' multiple name='files' class='input-file'>"
                + "  </span>"
                + "<div id='f"+fieldRec.get("id")+"' name='file_upload' class='file_list' style='margin-top:1%;'></div>";
        return returnStr;
    }
    
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public List<Record> getPrintTemplate(Long form_id,Long order_id){
    	Record formRec = Db.findFirst("select id, name, office_id from eeda_form_define where id = ?",form_id);
    	Long office_id=formRec.getLong("office_id");
    	List<Record> fieldList = Db.find("select id,field_name,field_display_name,field_type from eeda_form_field where form_id=?", form_id);
    	List<Record> template_list = Db.find("select id,name,content from eeda_form_print_template where form_id=?", form_id);
    	String form_name = formRec.getStr("name");
    	
    	String tableName = "form_"+form_id;
    	Record order = Db.findFirst("select * from "+tableName+" where id=?",order_id);
        for(Record field : fieldList){
        	String fieldDisplayName = field.getStr("field_display_name");
        	String fieldType = field.getStr("field_type");
            String replaceNameOrigin = "#{"+form_name+"."+fieldDisplayName+"}";
            String columnName = "f"+field.getLong("id")+"_"+field.getStr("field_name");
            
            String html = "";
            if("从表引用".equals(fieldType)){
            	 List<Record> display_list = Db.find(
                         "select id,target_field_name from eeda_form_field_type_detail_ref_display_field where field_id= ? order by sort_no", field.getLong("id"));
                 String fieldStr = "";
                 
                 for (Record r : display_list) {
                     String name = r.getStr("target_field_name");
                     if(name.indexOf(".")>0){
                         fieldStr+="<th>"+name.split("\\.")[1]+"</th>";
                     }else{
                         fieldStr+="<th>"+name+"</th>";
                     }
                 }
                 String tbodyStr = formDetail(form_id,order_id, office_id);
                 html = "<table id='detail_print_table_"+field.getLong("id")+"' type='dynamic' class='table detail_table table-striped table-bordered table-hover display' style='width:100%;'>"
                         +"    <thead class='eeda'>"
                         +"        <tr>"
                         + fieldStr
                         +"        </tr>"
                         +"    </thead>"
                         +"    <tbody>"
                         +tbodyStr
                         +"    </tbody>"
                         +"</table>";
            }else{
            	html = "<div class='print-form'><label style='float:left;'>"+fieldDisplayName+"：</label><div>"+order.get(columnName)+"</div></div>";
            }
            template_list.get(0).set("content",template_list.get(0).getStr("content").replace(replaceNameOrigin, html));
        }
        
        return template_list;
    }
    
    private String formDetail(Long form_id, Long order_id, Long office_id){
    	Record rec = Db.findFirst("select * from form_"+form_id+" where "
                + " id=?", order_id);
    	List<Record> refFieldList = Db.find("select distinct field.id field_id, form.id form_id, form.name, cond.field_from, cond.field_to "
                +"from eeda_form_field field, eeda_form_field_type_detail_ref ref,"
                + " eeda_form_field_type_detail_ref_join_condition cond,"
                +"    eeda_form_define form"
                +" where "
                +" field.id = ref.field_id"
                +" and field.id = cond.field_id"
                +" and ref.target_form_name = form.name"
                +" and field.field_type='从表引用' "
                +" and field.form_id=?", form_id);
    	
    	StringBuffer tbodySb = new StringBuffer();
    	for (Record fieldRec : refFieldList) {
            Long d_form_id = fieldRec.getLong("form_id");
            List<Record> fieldList = Db.find("select id,field_name,field_display_name,field_type from eeda_form_field where form_id=?", d_form_id);
            
            String field_from = fieldRec.getStr("field_from");
            String field_to = fieldRec.getStr("field_to");
            //主表关联值
            Record field_rec = FormService.getFieldName(field_from.split("\\.")[0], field_from.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
            String field_from_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
            Object from_field_value = rec.get(field_from_name);
            //从表关联值
            Record field_to_rec = FormService.getFieldName(field_to.split("\\.")[0], field_to.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
            String field_to_name = "f"+field_to_rec.getLong("id")+"_"+field_to_rec.getStr("field_name");
            
            List<Record> dataList = Db.find("select * from form_"+d_form_id+" where "+field_to_name+"=?", from_field_value);
            StringBuffer trSb = new StringBuffer();
            for(Record data :dataList){
            	trSb = new StringBuffer().append("<tr>");
            	for(Record field : fieldList){
            		String columnName = "f"+field.getLong("id")+"_"+field.getStr("field_name");
            		trSb.append("<td>"+data.get(columnName)+"</td>");
            	}
            	trSb.append("</tr>");
            	tbodySb.append(trSb);
            }
        }
        return tbodySb.toString();
    }
    
    /**
     * 保存系统日志
     * @param action
     * @param jsonStr
     * @param form_id
     * @param order_id
     * @param user_id
     * @param office_id
     * @param ip
     * @return
     */
    public boolean saveSysLog(String action,String jsonStr,long form_id,long order_id,long user_id,long office_id,String ip){
        Record sysLog = new Record();
        sysLog.set("log_type", "operate");
        sysLog.set("operation_obj", jsonStr);
        sysLog.set("action_type", action);
        sysLog.set("create_stamp", new Date());
        sysLog.set("user_id", user_id);
        sysLog.set("ip",ip);
        sysLog.set("office_id", office_id);
        sysLog.set("form_id", form_id);
        sysLog.set("order_id", order_id);
        return Db.save("sys_log", sysLog);
    }
    
    public boolean setValue(Map setting,long form_id,long order_id) throws Exception{
        EventService es = new EventService();
        boolean result = es.handleSetValue(setting, form_id, order_id);
        return result;
    }

    public void setDroplist(Map setting,long form_id,long order_id)  {
        // 订单数据
        Record order = Db.findFirst("select * from form_" + form_id + " where id=?", order_id);
//        Record currentFormRec = Db.findFirst("select * from eeda_form_define where id=?", form_id);
//        String currentFormName = currentFormRec.getStr("name");// 本表单中文名
//        Long office_id = currentFormRec.getLong("office_id");
        
        String treeJsonStr = (String)setting.get("tree_json");
        List<Map<String,Object>> selected_nodes=getCheckedNodes(treeJsonStr);
        for (Map<String,Object> node : selected_nodes) {
            String selectValue=(String)node.get("select_value");
            long fieldId=((Double)node.get("id")).longValue();
            Record fieldRec = Db.findFirst("select * from eeda_form_field where id=?", fieldId);
            String colName = "f"+fieldId+"_"+fieldRec.getStr("field_name");
            order.set(colName, selectValue);
            Db.update("form_"+form_id, order);
        }
    }

    private List<Map<String,Object>> getCheckedNodes(String treeJsonStr) {
        List<Map<String,Object>> selected_nodes=new LinkedList<>();
        Gson gson = new Gson();
        List<Map<String,Object>> nodeList = gson.fromJson(treeJsonStr, 
                new TypeToken<List<Map<String,Object>>>() { }.getType());
        for (Map<String,Object> node : nodeList) {
            List<Map<String,Object>> subNodeList= (List<Map<String, Object>>) node.get("children");
            for (Map<String,Object> subNode : subNodeList) {
                if((Boolean)subNode.get("checked")) {
                    selected_nodes.add(subNode);
                }
            }
        }
        return selected_nodes;
    }
}
