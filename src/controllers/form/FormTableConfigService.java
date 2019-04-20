package controllers.form;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;

public class FormTableConfigService {
    private Controller cont = null;
    public FormTableConfigService(Controller cont){
        this.cont = cont;
    } 
    
    public List<Record> getTableConfig(ArrayList<String> fieldIdList, Long office_id) {
        List<Record> list = new ArrayList<Record>();
       
        for (String fieldId : fieldIdList) {
            Record rec = new Record();
            
            String key = "tableConfig_"+fieldId;
            Record tableConfigRec = CacheKit.get("formCache",key);
            if(tableConfigRec!=null) {
                LogKit.info(key+" load from cache...");
                list.add(tableConfigRec);
                continue;
            }
            
            List<Record> itemList = Db.find("select * from eeda_form_field_type_detail_ref_display_field where "
                    + " field_id=? order by sort_no", fieldId);
            for (Record record : itemList) {
                String target_field_name = record.getStr("target_field_name");
                String form_name = target_field_name.split("\\.")[0];
                String field_display_name = target_field_name.split("\\.")[1];
                Record field_rec = FormService.getFieldName(form_name, field_display_name, office_id);//获取数据库对应的名称: f59_xh
                Long disFieldId = field_rec.getLong("id");
                record.set("field_name", "f"+disFieldId+"_"+field_rec.getStr("field_name"));
                record.set("field_display_name", field_rec.getStr("field_display_name"));
                record.set("field_type", field_rec.getStr("field_type"));
                
                if("字段引用".equals(field_rec.getStr("field_type"))){
                    Record ref = Db.findFirst(
                            "select * from eeda_form_field_type_ref where field_id=?", disFieldId);
                    record.set("ref", ref);
                    
                    String target_form_name = ref.getStr("ref_form");
                    Record refForm = Db.findFirst(
                            "select * from eeda_form_define where name=? and office_id=?", target_form_name, office_id);
                    ref.set("target_form_id", refForm.getLong("id"));
                    //下拉显示的字段和回填的字段
                    String target_search_field_name = "";//关联目标表需要查询的字段
                    List<Record> refItemList = Db.find(
                            "select * from eeda_form_field_type_ref_item where field_id=?", disFieldId);
                    if(refItemList!=null) {
                        for(Record refItemRec:refItemList) {
                            String from_name = refItemRec.getStr("from_name");
                            Record fromFieldRec = FormService.getFieldName(from_name.split("\\.")[0], from_name.split("\\.")[1], office_id);
                            String t_field_name = "f"+fromFieldRec.get("id")+"_"+fromFieldRec.getStr("field_name");//获取数据库对应的名称: f59_xh
                            target_search_field_name += (","+t_field_name);//查询的字段
                            refItemRec.set("from_field_name", t_field_name);
                            //回填字段
                            String to_name = refItemRec.getStr("to_name");
                            if(StrKit.notBlank(to_name)){
                                if(to_name.indexOf("\\.")==-1){
                                    to_name=form_name+"."+to_name;
                                }
                                Record value_rec = FormService.getFieldName(to_name.split("\\.")[0], to_name.split("\\.")[1], office_id);
                                String v_field_name = "f"+value_rec.get("id")+"_"+value_rec.getStr("field_name");//获取数据库对应的名称: f59_xh
                                refItemRec.set("to_field_name", v_field_name);
                            }
                        }
                        ref.set("ref_item_list", refItemList);
                    }
                    if(StrKit.notBlank(target_search_field_name)){
                        target_search_field_name=target_search_field_name.substring(1);//去掉第一个，
                    }
                    ref.set("target_field_name", target_search_field_name);//关联目标表需要查询的字段
                }else if("下拉列表".equals(field_rec.getStr("field_type"))){
                    List<Record> dropdown_list = Db.find("select * from eeda_form_field_type_dropdown where field_id=?", field_rec.getLong("id"));
                    record.set("dropdown_list",dropdown_list);
                }
            }
            rec.set("display_field_list", itemList);
            CacheKit.put("formCache", key, rec);
            list.add(rec);
        }
        return list;
    }
}
