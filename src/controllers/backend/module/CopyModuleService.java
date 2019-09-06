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

    //copy system, need change office_id
    public void copyModuleWithEvent(String fromModuleId, String toModuleId, String toOfficeId) {
        // 复制 form
        Record formRec = copyForm(fromModuleId, toModuleId, toOfficeId);
        if(formRec==null) return;
        Long fromFormId = formRec.getLong("fromFormId");
        Long toFormId = formRec.getLong("toFormId");
        // 复制字段
        copyFields(fromFormId, toFormId);
        // 复制 非btn的事件
        copyEvents(fromFormId, toFormId);
        // 复制 btn 和 btn事件
        copyBtnsWithEvent(fromFormId, toFormId);
    }

    //in system copy module
    public void copyModule(String fromModuleId, String toModuleId) {
        // 复制 form
        Record formRec = copyForm(fromModuleId, toModuleId, null);
        if(formRec==null) return;
        Long fromFormId = formRec.getLong("fromFormId");
        Long toFormId = formRec.getLong("toFormId");
        // 复制字段
        copyFields(fromFormId, toFormId);
        // 复制 btn 
        copyBtns(fromFormId, toFormId);
    }
    
    private void copyEvents(Long fromFormId, Long toFormId) {
        // 先删除旧的事件记录
        Db.update("delete from eeda_form_event where form_id=?", toFormId);
        //需要根据fromForm 的 btn来生成新btn的事件
        List<Record> fromEventList = Db.find("select * from eeda_form_event where form_id=? and btn_id is null", fromFormId);
        for (Record eventRec : fromEventList) {
            eventRec.remove("id").set("form_id", toFormId);
            Db.save("eeda_form_event", eventRec);
        }
    }
    
    /**
     * 复制按钮, 顺便复制事件
     * @param fromFormId
     * @param toFormId
     */
    private void copyBtns(Long fromFormId, Long toFormId) {
        // 先删除旧的按钮记录
        Db.update("delete from eeda_form_btn where form_id=?", toFormId);
        List<Record> fromFormBtnList = Db.find("select * from eeda_form_btn where form_id=?", fromFormId);
        for (Record btnRec : fromFormBtnList) {
            btnRec.remove("id").set("form_id", toFormId);
            Db.save("eeda_form_btn", btnRec);
        }
    }
    
    /**
     * 复制按钮, 顺便复制事件
     * @param fromFormId
     * @param toFormId
     */
    private void copyBtnsWithEvent(Long fromFormId, Long toFormId) {
        // 先删除旧的按钮记录
        Db.update("delete from eeda_form_btn where form_id=?", toFormId);
        List<Record> fromFormBtnList = Db.find("select * from eeda_form_btn where form_id=?", fromFormId);
        for (Record btnRec : fromFormBtnList) {
            Long oldBtnId = btnRec.getLong("id");
            btnRec.remove("id").set("form_id", toFormId);
            Db.save("eeda_form_btn", btnRec);
            Long newBtnId = btnRec.getLong("id");
            //需要根据fromForm 的 btn来生成新btn的事件
            List<Record> fromEventList = Db.find("select * from eeda_form_event where form_id=? and btn_id =?", fromFormId, oldBtnId);
            for (Record eventRec : fromEventList) {
                eventRec.remove("id").set("form_id", toFormId).set("btn_id", newBtnId);
                Db.save("eeda_form_event", eventRec);
            }
        }
    }

    private void copyFields(Long fromFormId, Long toFormId) {
        // form_fields, 先删除旧的字段
        Db.update("delete from eeda_form_field where form_id=?", toFormId);

        ArrayList<Record> fieldsList = (ArrayList<Record>) Db.find("select * from eeda_form_field where form_id=?",
                fromFormId);
        ArrayList<Record> copyFieldsList = (ArrayList<Record>) fieldsList.clone();
        for (Record fieldRec : copyFieldsList) {
            String fieldType = fieldRec.getStr("field_type");

            fieldRec.remove("id").set("form_id", toFormId);
            Db.save("eeda_form_field", fieldRec);

            // 复制复选框选项
            if ("复选框".equals(fieldType)) {
                copyCheckBox(fieldRec, fieldRec.getLong("id"));
            }
        }
    }

    private Record copyForm(String fromModuleId, String toModuleId, String toOfficeId) {
        Long fromFormId = 0l;
        Record fromFormRec = Db.findFirst("select * from eeda_form_define where module_id=?", fromModuleId);
        Record toFormRec = Db.findFirst("select * from eeda_form_define where module_id=?", toModuleId);
        if (fromFormRec != null) {
            fromFormId = fromFormRec.getLong("id");
            if (toFormRec == null) {// 没有form，新建
                fromFormId = fromFormRec.getLong("id");
                String formName = fromFormRec.getStr("name") + "_拷贝";
                if(toOfficeId!=null) {//copy to 新系统,不需要加 拷贝 二字
                    formName = fromFormRec.getStr("name");
                }
                Db.update("update eeda_modules set module_name='" + formName + "' where id=?", toModuleId);
                long sourceFormId= fromFormRec.getLong("id");
                fromFormRec.remove("id").set("module_id", toModuleId).set("name", formName);
                if(toOfficeId!=null) {//copy to 新系统,需要设置新的office_id
                    fromFormRec.set("office_id", toOfficeId);
                }
                Db.save("eeda_form_define", fromFormRec);
                long toFormId=fromFormRec.getLong("id");
                //拷贝表
                String sql = "CREATE TABLE form_"+toFormId+" LIKE form_"+sourceFormId;
                Db.update(sql);
            } else {// 有form，update
                String formName = fromFormRec.getStr("name") + "_拷贝";
                Db.update("update eeda_modules set module_name='" + formName + "' where id=?", toModuleId);
                fromFormRec.set("id", toFormRec.getLong("id")).set("module_id", toModuleId).set("name", formName);
                Db.update("eeda_form_define", fromFormRec);
            }
            Record rec = new Record();
            rec.set("fromFormId", fromFormId);
            rec.set("toFormId", fromFormRec.getLong("id"));
            return rec;
        }
        return null;
    }

    // 复制复选框选项
    private void copyCheckBox(Record originRec, long toFieldId) {
        Record fromFieldTypeRec = Db.findById("eeda_form_field_type_checkbox", originRec.getLong("id"));
        Record toFieldTypeRec = Db.findById("eeda_form_field_type_checkbox", toFieldId);
        if (fromFieldTypeRec != null) {
            if (toFieldTypeRec == null) {// save
                fromFieldTypeRec.set("id", toFieldId);
                Db.save("eeda_form_field_type_checkbox", fromFieldTypeRec);
            } else {// update
                fromFieldTypeRec.set("id", toFieldId);
                Db.update("eeda_form_field_type_checkbox", fromFieldTypeRec);
            }
        }
        // 先删除旧的字段记录
        Db.update("delete from eeda_form_field_type_checkbox_item where field_id=?", toFieldId);
        List<Record> fromFieldTypeItemList = Db
                .find("select * from eeda_form_field_type_checkbox_item where field_id=?", originRec.getLong("id"));
        for (Record fromFieldTypeItem : fromFieldTypeItemList) {
            fromFieldTypeItem.set("id", toFieldId);
            Db.save("eeda_form_field_type_checkbox_item", fromFieldTypeItem);
        }
    }
}
