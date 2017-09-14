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
    

    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    public String processFieldType_checkbox(String form_name, Record fieldRec, Long field_id){
        String returnStr = "";
        String fieldDisplayName=fieldRec.getStr("field_display_name");
        String fieldName=fieldRec.getStr("field_name");
        Record checkBox = Db.findFirst(
                "select * from eeda_form_field_type_checkbox where field_id=?", field_id);
        
        List<Record> list = Db.find(
                "select * from eeda_form_field_type_checkbox_item where field_id=?", field_id);
        returnStr = "<label class='label-margin'>"+fieldDisplayName+"</label> ";
        for (Record r : list) {
            String name = r.getStr("name");
            String code = r.getStr("code");
            String isDefault = r.getStr("is_default");
            String checked = "";
            if("Y".equals(isDefault)){
                checked = "checked";
            }
            String checkboxStr = "<label class='radio-inline'>"
                    + "<input type='radio' origin_name='"+form_name+"-"+fieldDisplayName+"' name='"+fieldName+"' id='"+fieldName+"' value='"+code+"' "+checked+">"
                    + name+"</label>";
            returnStr+=checkboxStr;
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
                "select * from eeda_form_field_type_detail_ref_display_field where field_id=?", field_id);
        String fieldStr = "";
        
        for (Record r : display_list) {
            String name = r.getStr("target_field_name");
            fieldStr+="<th>"+name+"</th>";
        }
        returnStr = "<div class='row'>"
                +"    <div class='col-lg-12'>"
                +"        <div class='form-group button-bar' >"
                +"            <button type='button' class='btn btn-success btn-xs' name='add_detail_row_btn' target_table='detail_table_"+field_id+"'>添加</button>"
                +"        </div>"
                +"    </div>"
                +"</div>"
                +"<table id='detail_table_"+field_id+"' class='table table-striped table-bordered table-hover display' style='width:100%;'>"
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
