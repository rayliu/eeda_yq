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

    public ModuleService(Controller cont) {
        this.cont = cont;
    }
    
    public void saveEventSetValue(Map<String, ?> event, Long event_id) {
        Map<String, ?> dto = (Map<String, ?>) event.get("SET_VALUE");
        if (dto != null) {
            String condition = (String) dto.get("CONDITION");
            String id = (String) dto.get("id".toUpperCase());
            Record itemRec = new Record();
            if (StrKit.isBlank(id)) {
                itemRec.set("event_id", event_id);
                itemRec.set("condition", condition);
                Db.save("eeda_form_event_set_value", itemRec);
            } else {
                itemRec = Db.findById("eeda_form_event_set_value", id);
                itemRec.set("condition", condition);
                Db.update("eeda_form_event_set_value", itemRec);
            }

            List<Map<String, String>> field_list = (ArrayList<Map<String, String>>) dto
                    .get("SET_FIELD_LIST");
            for (Map<String, String> field : field_list) {
                String name = (String) field.get("NAME");
                String value = (String) field.get("VALUE");
                String field_id = (String)field.get("id".toUpperCase());
                Record item = new Record();
                if (StrKit.isBlank(field_id)) {
                    item.set("event_id", event_id);
                    item.set("name", name);
                    item.set("value", value);
                    Db.save("eeda_form_event_set_value_item", item);
                } else {
                    item = Db.findById("eeda_form_event_set_value_item", field_id);
                    item.set("name", name);
                    item.set("value", value);
                    Db.update("eeda_form_event_set_value_item", item);
                }
            }
        }
    }
    
    public void saveEventSetCss(Map<String, ?> event, Long event_id) {
        Map<String, ?> dto = (Map<String, ?>) event.get("SET_CSS");
        if (dto != null) {
            String condition = (String) dto.get("CONDITION");
            String target_field = (String) dto.get("TARGET_FIELD");
            Object id = dto.get("id".toUpperCase());
            Record itemRec = new Record();
            if (!(id instanceof java.lang.String)) {
                itemRec.set("event_id", event_id);
                itemRec.set("condition", condition);
                itemRec.set("target_field", target_field);
                Db.save("eeda_form_event_css", itemRec);
                id = itemRec.get("id");
            } else {
                itemRec = Db.findById("eeda_form_event_css", id);
                itemRec.set("condition", condition);
                itemRec.set("target_field", target_field);
                Db.update("eeda_form_event_css", itemRec);
            }

            List<Map<String, String>> field_list = (ArrayList<Map<String, String>>) dto
                    .get("SET_FIELD_LIST");
            for (Map<String, String> field : field_list) {
                String name = (String) field.get("NAME");
                String value = (String) field.get("VALUE");
                Object field_id = field.get("id".toUpperCase());
                Record item = new Record();
                if (!(field_id instanceof java.lang.String)) {
                    item.set("event_id", event_id);
                    item.set("name", name);
                    item.set("value", value);
                    Db.save("eeda_form_event_css_item", item);
                } else {
                    item = Db.findById("eeda_form_event_css_item", field_id);
                    item.set("name", name);
                    item.set("value", value);
                    Db.update("eeda_form_event_css_item", item);
                }
            }
        }
    }

    public void processFieldType(List<Map<String, String>> field_list,
            long form_id) {
        for (Map<String, ?> field : field_list) {
            Object id = field.get("id".toUpperCase());
            String is_delete = (String) field.get("is_delete");
            if("Y".equals(is_delete)){
                Db.deleteById("eeda_form_field", id);
                continue;
            }
            String field_display_name = (String) field.get("field_display_name"
                    .toUpperCase());
            Object seq = field.get("seq".toUpperCase());
            String field_type = (String) field.get("field_type".toUpperCase());
            String read_only = (String) field.get("read_only".toUpperCase());
            
            Record itemRec = new Record();
            if (!(id instanceof java.lang.Double)) {
                itemRec.set("form_id", form_id);
                itemRec.set("field_display_name", field_display_name);
                itemRec.set("field_name",
                        PingYinUtil.getFirstSpell(field_display_name));
                itemRec.set("field_type", field_type);
                itemRec.set("read_only", read_only);
                if (seq instanceof java.lang.Long)
                    itemRec.set("seq", seq);
                Db.save("eeda_form_field", itemRec);
                id = itemRec.get("id");
            } else {
                itemRec = Db.findById("eeda_form_field", id);
                itemRec.set("field_display_name", field_display_name);
                itemRec.set("field_name",
                        PingYinUtil.getFirstSpell(field_display_name));
                itemRec.set("field_type", field_type);
                if (seq != null && !StrKit.isBlank(seq.toString()))
                    itemRec.set("seq", seq);
                itemRec.set("read_only", read_only);
                Db.update("eeda_form_field", itemRec);
            }

            Long field_id = null;
            if (id instanceof java.lang.Double) {
                field_id = ((Double) id).longValue();
            } else {
                field_id = (Long) id;
            }
            // 处理字段类型(例如, 复选框: 男, 女)
            saveFieldType(field, field_id);
        }
        
    }

    @SuppressWarnings("unchecked")
    @Before(Tx.class)
    private void saveFieldType(Map<String, ?> field, Long field_id) {
        String fieldType = (String) field.get("field_type".toUpperCase());
        if ("复选框".equals(fieldType)) {
            saveCheckBox(field, field_id);
        }else if ("从表引用".equals(fieldType)) {
            saveDetailRef(field, field_id);
        }else if ("字段引用".equals(fieldType)) {
            Map<String, ?> fieldTypeObj = (Map<String, ?>) field
                    .get("REF");
            String refId = (String) fieldTypeObj.get("id".toUpperCase());
            String ref_form = (String) fieldTypeObj
                    .get("ref_form".toUpperCase());
            String ref_field = (String) fieldTypeObj
                    .get("ref_field".toUpperCase());
            String is_dropdown = (String) fieldTypeObj
                    .get("is_dropdown".toUpperCase());
            Record rec = new Record();
            if (StrKit.isBlank(refId)) {
                rec.set("field_id", field_id);
                rec.set("ref_form", ref_form);
                rec.set("ref_field", ref_field);
                rec.set("is_dropdown", is_dropdown);
                Db.save("eeda_form_field_type_ref", rec);
            } else {
                rec = Db.findById("eeda_form_field_type_ref", refId);
                rec.set("field_id", field_id);
                rec.set("ref_form", ref_form);
                rec.set("ref_field", ref_field);
                rec.set("is_dropdown", is_dropdown);
                Db.update("eeda_form_field_type_detail_ref", rec);
            }
        }
        
    }

    private void saveDetailRef(Map<String, ?> field, Long field_id) {
        Long fieldId = field_id;

        Map<String, ?> fieldTypeObj = (Map<String, ?>) field
                .get("DETAIL_REF");
        String refId = fieldTypeObj.get("id".toUpperCase()).toString();
        String target_form_name = (String) fieldTypeObj
                .get("target_form_name".toUpperCase());
        Record rec = new Record();
        if (StrKit.isBlank(refId)) {
            rec.set("field_id", fieldId);
            rec.set("target_form_name", target_form_name);
            Db.save("eeda_form_field_type_detail_ref", rec);
        } else {
            rec = Db.findById("eeda_form_field_type_detail_ref", refId);
            rec.set("field_id", fieldId);
            rec.set("target_form_name", target_form_name);
            Db.update("eeda_form_field_type_detail_ref", rec);
        }

        List<Map<String, ?>> join_condition_list = (ArrayList<Map<String, ?>>) fieldTypeObj
                .get("join_condition".toUpperCase());
        for (Map<String, ?> condition : join_condition_list) {
            String id = condition.get("id".toUpperCase()).toString();;
            String field_from = (String) condition.get("field_from".toUpperCase());
            String field_to = (String) condition.get("field_to".toUpperCase());

            Record itemRec = new Record();
            if (StrKit.isBlank(id)) {
                itemRec.set("field_id", fieldId);
                itemRec.set("field_from", field_from);
                itemRec.set("field_to", field_to);
                Db.save("eeda_form_field_type_detail_ref_join_condition", itemRec);
            } else {
                itemRec = Db.findById("eeda_form_field_type_detail_ref_join_condition",
                        id);
                itemRec.set("field_id", fieldId);
                itemRec.set("field_from", field_from);
                itemRec.set("field_to", field_to);
                Db.update("eeda_form_field_type_detail_ref_join_condition", itemRec);
            }
        }
        
        List<Map<String, ?>> display_list = (ArrayList<Map<String, ?>>) fieldTypeObj
                .get("display_field".toUpperCase());
        for (Map<String, ?> display_field : display_list) {
            String id = display_field.get("id".toUpperCase()).toString();;
            String target_field_name = (String) display_field.get("target_field_name".toUpperCase());
            String value = (String) display_field.get("value".toUpperCase());

            Record itemRec = new Record();
            if (StrKit.isBlank(id)) {
                itemRec.set("field_id", fieldId);
                itemRec.set("target_field_name", target_field_name);
                itemRec.set("value", value);
                Db.save("eeda_form_field_type_detail_ref_display_field", itemRec);
            } else {
                itemRec = Db.findById("eeda_form_field_type_detail_ref_display_field",
                        id);
                itemRec.set("field_id", fieldId);
                itemRec.set("target_field_name", target_field_name);
                itemRec.set("value", value);
                Db.update("eeda_form_field_type_detail_ref_display_field", itemRec);
            }
        }
    }

    private void saveCheckBox(Map<String, ?> field, Long field_id) {
        Long fieldId = field_id;

        Map<String, ?> fieldTypeObj = (Map<String, ?>) field
                .get("check_box".toUpperCase());
        Object checkId = fieldTypeObj.get("id".toUpperCase());
        String is_single_check = (String) fieldTypeObj
                .get("is_single_check".toUpperCase());
        String line_display_num = (String) fieldTypeObj
                .get("line_display_numbers".toUpperCase());
        Record rec = new Record();
        if (!(checkId instanceof java.lang.Double)) {
            rec.set("field_id", fieldId);
            rec.set("is_single_check", is_single_check);
            rec.set("line_display_num", line_display_num);
            Db.save("eeda_form_field_type_checkbox", rec);
        } else {
            rec = Db.findById("eeda_form_field_type_checkbox", checkId);
            rec.set("field_id", fieldId);
            rec.set("is_single_check", is_single_check);
            rec.set("line_display_num", line_display_num);
            Db.update("eeda_form_field_type_checkbox", rec);
        }

        List<Map<String, ?>> list = (ArrayList<Map<String, ?>>) fieldTypeObj
                .get("item_list".toUpperCase());
        for (Map<String, ?> checkBox : list) {
            Object id = checkBox.get("id".toUpperCase());
            String name = (String) checkBox.get("name".toUpperCase());
            String code = (String) checkBox.get("code".toUpperCase());
            Object seq = checkBox.get("seq".toUpperCase());
            String is_default = (String) checkBox.get("is_default"
                    .toUpperCase());

            Record itemRec = new Record();
            if (!(id instanceof java.lang.Double)) {
                itemRec.set("field_id", fieldId);
                itemRec.set("seq", seq);
                itemRec.set("name", name);
                itemRec.set("code", code);
                itemRec.set("is_default", is_default);
                Db.save("eeda_form_field_type_checkbox_item", itemRec);
            } else {
                itemRec = Db.findById("eeda_form_field_type_checkbox_item",
                        id);
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
