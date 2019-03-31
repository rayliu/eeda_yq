package controllers.form;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class FormUtil {
    /**
     * 获取form或者field，最多支持3层
     * @param name 传入的name,如入库单、入库单.单号、入库单.从表.货品代码等
     * @return
     */
    public static Record getFormOrField(String name, Long office_id){
        Record rec = new Record();
        
        if(StrKit.notBlank(name)){
            String[] nameArry = name.split("\\.");
            if(nameArry.length==3){
                Record form = getFormOrField(nameArry[0], office_id);
                Record detailField = Db.findFirst("select id from eeda_form_field where form_id = ? and field_display_name = ?",form.getLong("id"),nameArry[1]);
                Record detailRef = Db.findFirst("select id,target_form_name from eeda_form_field_type_detail_ref where field_id = ?",detailField.getLong("id"));
                Record detailForm = Db.findFirst("select id,name from eeda_form_define where name = ? and office_id=?",detailRef.getStr("target_form_name"), office_id);
                rec = getFormOrField(detailForm.getStr("name")+"."+nameArry[2], office_id);
                if(rec!=null) {
                    rec.set("this_type", "field");
                    rec.set("real_name", "f"+rec.getLong("id")+"_"+rec.getStr("field_name"));
                }
            }else if(nameArry.length==2){
                Record form = getFormOrField(nameArry[0], office_id);
                rec = Db.findFirst("select * from eeda_form_field where form_id = ? and field_display_name = ?",form.getLong("id"),nameArry[1]);
                if(rec!=null) {
                    rec.set("this_type", "field");
                    rec.set("real_name", "f"+rec.getLong("id")+"_"+rec.getStr("field_name"));
                }
            }else{
                rec = Db.findFirst("select * from eeda_form_define where name = ? and office_id=?",name, office_id);
                if(rec!=null) {
                    rec.set("this_type", "form");
                    rec.set("real_name", "form_"+rec.getLong("id"));
                }
            }
        }

        return rec;
    } 
}
