package controllers.backend.module;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class CopyModuleService {
    private Controller cont = null;

    public CopyModuleService(Controller cont) {
        this.cont = cont;
    }

    public void copyModule(String fromModuleId, String toModuleId) {
       //复制 form
       Long fromFormId = 0l;
       Record fromFormRec = Db.findFirst("select * from eeda_form_define where module_id=?", fromModuleId);
       Record toFormRec = Db.findFirst("select * from eeda_form_define where module_id=?", toModuleId);
       if(fromFormRec!=null) {
           fromFormId = fromFormRec.getLong("id");
           if(toFormRec==null) {//没有form，新建
               fromFormId=fromFormRec.getLong("id");
               fromFormRec.remove("id").set("module_id", toModuleId);
               Db.save("eeda_form_define", fromFormRec);
           }else {//有form，update
               fromFormRec.set("id", toFormRec.getLong("id")).set("module_id", toModuleId);
               Db.update("eeda_form_define", fromFormRec);
           }
       }
       
       //复制字段
       Long toFormId = fromFormRec.getLong("id");
       //form_fields, 先删除旧的字段
       Db.update("delete from eeda_form_field where form_id=?", toFormId);
       
       ArrayList<Record> fieldsList = (ArrayList<Record>) Db.find("select * from eeda_form_field where form_id=?", fromFormId);
       ArrayList<Record> copyFieldsList = (ArrayList<Record>) fieldsList.clone();
       for (Record fieldRec : copyFieldsList) {
           String fieldType = fieldRec.getStr("field_type");
           
           fieldRec.remove("id").set("form_id", toFormId);
           Db.save("eeda_form_field", fieldRec);
           
           //复制复选框选项
           if("复选框".equals(fieldType)) {
               copyCheckBox(fieldRec, fieldRec.getLong("id"));
           }
       }
       
       //复制按钮，先删除旧的按钮记录
       Db.update("delete from eeda_form_btn where form_id=?", toFormId);
       List<Record> fromFormBtnList = Db.find("select * from eeda_form_btn where form_id=?", fromFormId);
       for (Record btnRec : fromFormBtnList) {
           btnRec.remove("id").set("form_id", toFormId);
           Db.save("eeda_form_btn", btnRec);
       }
    }
    
    //复制复选框选项
    private void copyCheckBox(Record originRec, long toFieldId) {
        Record fromFieldTypeRec = Db.findById("eeda_form_field_type_checkbox", originRec.getLong("id"));
        Record toFieldTypeRec = Db.findById("eeda_form_field_type_checkbox", toFieldId);
        if(fromFieldTypeRec!=null) {
            if(toFieldTypeRec==null) {//save
                fromFieldTypeRec.set("id", toFieldId);
                Db.save("eeda_form_field_type_checkbox", fromFieldTypeRec);
            }else {//update
                fromFieldTypeRec.set("id", toFieldId);
                Db.update("eeda_form_field_type_checkbox", fromFieldTypeRec);
            }
        }
        //先删除旧的字段记录
        Db.update("delete from eeda_form_field_type_checkbox_item where field_id=?", toFieldId);
        List<Record> fromFieldTypeItemList = Db.find("select * from eeda_form_field_type_checkbox_item where field_id=?", originRec.getLong("id"));
        for (Record fromFieldTypeItem : fromFieldTypeItemList) {
            fromFieldTypeItem.set("id", toFieldId);
            Db.save("eeda_form_field_type_checkbox_item", fromFieldTypeItem);
        }
    }
}
