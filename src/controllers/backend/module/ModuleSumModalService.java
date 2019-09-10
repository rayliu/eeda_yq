package controllers.backend.module;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

public class ModuleSumModalService {
    private Controller cont = null;

    public ModuleSumModalService(Controller cont) {
        this.cont = cont;
    }

    public List<Record> getSumModalTree(List<String> formList, Long officeId) {
        List<Record> list= new ArrayList<Record>();
        for (String formName : formList) {
            Record rec = new Record();
            List<Record> recList = getFormFieldsWithDetail(formName, officeId);
            rec.set("form_name", formName);
            rec.set("field_list", recList);
            list.add(rec);
        }
        return list;
    }
    
    private List<Record> getFormFieldsWithDetail(String formName, Long officeId){
        List<Record> fieldList = Db.find("select ef.name form_name, eff.id, eff.form_id, eff.field_name, eff.field_display_name, "
                + "eff.field_type, em.office_id from eeda_form_define ef, eeda_form_field eff, eeda_modules em "
                + "where ef.id=eff.form_id and ef.module_id=em.id and em.office_id=? and ef.name=?", officeId, formName);
        for(Record rec: fieldList) {
            String type = rec.getStr("field_type");
            if("从表引用".equals(type)) {
                List<Record> dFieldList = Db.find("select * from eeda_form_field_type_detail_ref_display_field where field_id=? order by sort_no", rec.getLong("id"));
                rec.set("field_list", dFieldList);
            }
        }
        
        return fieldList;
    }
    
}
