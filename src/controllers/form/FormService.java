package controllers.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

public class FormService {
    private Controller cont = null;
    public FormService(Controller cont){
        this.cont = cont;
    } 
    
    public static Record getFieldName(String form_name, String feild_display_name){
        Record rec = Db.findFirst("select f.* from eeda_form_define form, eeda_form_field f where "
                +" form.id = f.form_id and "
                +"form.name=? and f.field_display_name=?", form_name, feild_display_name);

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
    public String processFieldType_ref(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        Record ref = Db.findFirst(
                "select * from eeda_form_field_type_ref where field_id=?", field_id);
        String displayType = ref.getStr("display_type");
        String target_form_name = ref.getStr("ref_form");
        Record refForm = Db.findFirst(
                "select * from eeda_form_define where name=?", target_form_name);
        //回填字段
//        String target_form_field = ref.getStr("ref_field");
//        Record field_rec = FormService.getFieldName(target_form_field.split("\\.")[0], target_form_field.split("\\.")[1]);//获取数据库对应的名称: f59_xh
        
        
        String inputId = "form_"+form_id+"-f"+field_id+"_"+fieldName.toLowerCase();
        String target_search_field_name = "";
        List<Record> itemList = Db.find(
                "select * from eeda_form_field_type_ref_item where field_id=?", field_id);
        for (Record record : itemList) {
            String from_name = record.getStr("from_name");
            Record rec = FormService.getFieldName(from_name.split("\\.")[0], from_name.split("\\.")[1]);
            String t_field_name = "f"+rec.get("id")+"_"+rec.getStr("field_name");//获取数据库对应的名称: f59_xh
            
            target_search_field_name += (","+t_field_name);//查询的字段
            
            record.set("from_field_name", t_field_name);
            //回填字段
            String to_name = record.getStr("to_name");
            if(StrKit.notBlank(to_name)){
                if(to_name.indexOf("\\.")==-1){
                    to_name=form_name+"."+to_name;
                }
                Record value_rec = FormService.getFieldName(to_name.split("\\.")[0], to_name.split("\\.")[1]);
                String v_field_name = "f"+value_rec.get("id")+"_"+value_rec.getStr("field_name");//获取数据库对应的名称: f59_xh
                record.set("to_field_name", v_field_name);
            }
        }
        
        String listJson = JsonKit.toJson(itemList);
        target_search_field_name=target_search_field_name.substring(1);//去掉第一个，
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
            String value = r.getStr("value");
            String name = r.getStr("name");
            dropdownStr += "<option value='"+value+"'>"+name+"</option>";
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
        		+ "<span style='width:30%;' class='btn-upload'><a href='javascript:void();' class='btn btn-primary radius'><i class='iconfont'>&#xf0020;</i> 上传图片</a>"
        				+ "<input type='file' id='fileupload"+fieldRec.get("id")+"' multiple name='img_files' class='input-file'></span>"
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
        		  + "<div class='formControls col-xs-8 col-sm-8'>"
        		  + "<span class='btn-upload form-group' style='float:left;'>"
        		  + "<a href='javascript:void();' class='btn btn-primary size-S radius'>上传文件</a>"
        		  + "<input type='file' id='fileupload"+fieldRec.get("id")+"' multiple name='files' class='input-file'>"
        		  + "</span>"
        		  + "<span name='"+inputId+"' class='col-sm-8 file_name' style='overflow: hidden; text-overflow: ellipsis;white-space: nowrap;'></span>"
                  + " </div>";
        return returnStr;
    }
}
