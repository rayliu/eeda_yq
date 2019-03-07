package controllers.form;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class FormTableConfigService {
    private Controller cont = null;
    public FormTableConfigService(Controller cont){
        this.cont = cont;
    } 
    
    public List<Record> getTableConfig(ArrayList<String> fieldIdList) {
        List<Record> list = new ArrayList<Record>();
        for (String fieldId : fieldIdList) {
            Record rec = new Record();
            List<Record> itemList = Db.find("select * from eeda_form_field_type_detail_ref_display_field where "
                    + " field_id=? order by sort_no", fieldId);
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
                }else if("下拉列表".equals(field_rec.getStr("field_type"))){
                    List<Record> dropdown_list = Db.find("select * from eeda_form_field_type_dropdown where field_id=?", field_rec.getLong("id"));
                    record.set("dropdown_list",dropdown_list);
                }
            }
            rec.set("display_field_list", itemList);
            list.add(rec);
        }
        return list;
    }
}
