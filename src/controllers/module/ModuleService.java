package controllers.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.PingYinUtil;

public class ModuleService {
    private Controller cont = null;
    public ModuleService(Controller cont){
        this.cont = cont;
    } 
    
    public void processFieldType(List<Map<String, String>> field_list, long form_id){
        for (Map<String, ?> field : field_list) {
            Object id = field.get("id".toUpperCase());
            String field_display_name = (String)field.get("field_display_name".toUpperCase());
            Object seq = field.get("seq".toUpperCase());
            String field_type = (String)field.get("field_type".toUpperCase());
            Record itemRec = new Record();
            if(!(id instanceof java.lang.Double)){
                itemRec.set("form_id", form_id);
                itemRec.set("field_display_name", field_display_name);
                itemRec.set("field_name", PingYinUtil.getFirstSpell(field_display_name));
                itemRec.set("field_type", field_type);
                if(seq instanceof java.lang.Long)
                    itemRec.set("seq", seq);
                Db.save("eeda_form_field", itemRec);
                id = itemRec.get("id");
            }else{
                itemRec = Db.findById("eeda_form_field", id);
                itemRec.set("field_display_name", field_display_name);
                itemRec.set("field_name", PingYinUtil.getFirstSpell(field_display_name));
                itemRec.set("field_type", field_type);
                if(seq!=null)
                    itemRec.set("seq", seq);
                Db.update("eeda_form_field", itemRec);
            }
            
            Long field_id = null;
            if(id instanceof java.lang.Double){
                field_id = ((Double)id).longValue();
            }else{
                field_id = (Long)id;
            }
            //处理字段类型(例如, 复选框: 男, 女)
            processFieldType(field, field_id);
        }
    }
    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    private void processFieldType(Map<String, ?> field, Long field_id){
            String fieldType = (String)field.get("field_type".toUpperCase());
            if("复选框".equals(fieldType)){
                Long fieldId = field_id;
                
                Map<String, ?> fieldTypeObj = (Map<String, ?>)field.get("check_box".toUpperCase());
                Object checkId = fieldTypeObj.get("id".toUpperCase());
                String is_single_check = (String)fieldTypeObj.get("is_single_check".toUpperCase());
                String line_display_num = (String)fieldTypeObj.get("line_display_numbers".toUpperCase());
                Record rec = new Record();
                if(!(checkId instanceof java.lang.Double)){
                    rec.set("field_id", fieldId);
                    rec.set("is_single_check", is_single_check);
                    rec.set("line_display_num", line_display_num);
                    Db.save("eeda_form_field_type_checkbox", rec);
                }else{
                    rec = Db.findById("eeda_form_field_type_checkbox", checkId);
                    rec.set("field_id", fieldId);
                    rec.set("is_single_check", is_single_check);
                    rec.set("line_display_num", line_display_num);
                    Db.update("eeda_form_field_type_checkbox", rec);
                }
                
                List<Map<String, ?>> list = (ArrayList<Map<String, ?>>)fieldTypeObj.get("item_list".toUpperCase());
                for (Map<String, ?> checkBox : list) {
                    Object id = checkBox.get("id".toUpperCase());
                    String name = (String)checkBox.get("name".toUpperCase());
                    String code = (String)checkBox.get("code".toUpperCase());
                    Object seq = checkBox.get("seq".toUpperCase());
                    String is_default = (String)checkBox.get("is_default".toUpperCase());
                    
                    Record itemRec = new Record();
                    if(!(id instanceof java.lang.Double)){
                        itemRec.set("field_id", fieldId);
                        itemRec.set("seq", seq);
                        itemRec.set("name", name);
                        itemRec.set("code", code);
                        itemRec.set("is_default", is_default);
                        Db.save("eeda_form_field_type_checkbox_item", itemRec);
                    }else{
                        itemRec = Db.findById("eeda_form_field_type_checkbox_item", id);
                        itemRec.set("field_id", fieldId);
                        itemRec.set("name", name);
                        itemRec.set("code", code);
                        itemRec.set("seq", seq);
                        itemRec.set("is_default", is_default);
                        Db.update("eeda_form_field_type_checkbox_item", itemRec);
                    }
                }
            }

    }
}
