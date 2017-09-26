package controllers.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
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
    public String processFieldType_checkbox(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Long form_id = fieldRec.getLong("form_id");
        Record checkBox = Db.findFirst(
                "select * from eeda_form_field_type_checkbox where field_id=?", field_id);
        
        List<Record> list = Db.find(
                "select * from eeda_form_field_type_checkbox_item where field_id=?", field_id);
        returnStr = "<label class='label-margin'>"+fieldDisplayName+"</label> <div style='padding-top: 8px;'>";
        for (Record r : list) {
            String name = r.getStr("name");
            String code = r.getStr("code");
            String isDefault = r.getStr("is_default");
            String checked = "";
            if("Y".equals(isDefault)){
                checked = "checked";
            }
            String checkboxStr = "<label class='radio-inline'>"
                    + "<input type='radio' origin_name='"+form_name+"-"+fieldDisplayName
                    +"' name='form_"+form_id+"-f"+field_id+"_"+fieldName+"' id='"+fieldName+"' value='"+name+"' "+checked+"/>"
                    + name+"</label>";
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
        
        String target_form_name = ref.getStr("ref_form");
        Record refForm = Db.findFirst(
                "select * from eeda_form_define where name=?", target_form_name);
        
        String target_form_field = ref.getStr("ref_field");
        Record field_rec = FormService.getFieldName(target_form_field.split("\\.")[0], target_form_field.split("\\.")[1]);//获取数据库对应的名称: f59_xh
        String field_name = "f"+field_rec.get("id")+"_"+field_rec.getStr("field_name");
        
        String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
        returnStr = "<label class='search-label'>"+fieldDisplayName+"</label>"
                + "<input type='text' name='"+inputId+"' class='form-control' placeholder='请选择' eeda_type='drop_down'"
                + " target_form='"+refForm.getLong("id")+"' target_field_name='"+field_name+"' >"
                //+ "<input id='"+inputId+"' style='display: none;' name="+inputId+" />"
                + "<ul id='"+inputId+"_list' class='pull-right dropdown-menu default dropdown-scroll' tabindex='-1' style='top: 35%; left: 2%;'>";
        
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
                "select * from eeda_form_field_type_detail_ref_display_field where field_id=?", field_id);
        String fieldStr = "<th></th>";//默认第一列是放按钮的
        
        for (Record r : display_list) {
            String name = r.getStr("target_field_name");
            if(name.indexOf(".")>0){
                fieldStr+="<th>"+name.split("\\.")[1]+"</th>";
            }else{
                fieldStr+="<th>"+name+"</th>";
            }
        }
        returnStr = "<div class='row'>"
                +"    <div class='col-lg-12'>"
                +"        <div class='form-group button-bar' >"
                +"            <button type='button' class='btn btn-success btn-xs' name='add_detail_row_btn' target_table='detail_table_"+field_id+"'>添加</button>"
                +"        </div>"
                +"    </div>"
                +"</div>"
                +"<table id='detail_table_"+field_id+"' type='dynamic' class='table table-striped table-bordered table-hover display' style='width:100%;'>"
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
}
