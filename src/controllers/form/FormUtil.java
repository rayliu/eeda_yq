package controllers.form;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import models.UserLogin;

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
    
    public static Record getFormData(Long form_id, Long order_id, Long office_id ){
        Record rec = Db.findFirst("select * from form_"+form_id+" where "
                + " id=?", order_id);
        List<Record> detailList= new ArrayList<Record>();
        
        List<Record> fieldList = Db.find("select distinct field.id field_id, form.id form_id, form.name, cond.field_from, cond.field_to "
                    +"from eeda_form_field field, eeda_form_field_type_detail_ref ref,"
                    + " eeda_form_field_type_detail_ref_join_condition cond,"
                    +"    eeda_form_define form"
                    +" where "
                    +" field.id = ref.field_id"
                    +" and field.id = cond.field_id"
                    +" and ref.target_form_name = form.name"
                    +" and field.field_type='从表引用' "
                    +" and field.form_id=?", form_id);
        for (Record fieldRec : fieldList) {
            Long d_form_id = fieldRec.getLong("form_id");
            Long field_id = fieldRec.getLong("field_id");
            
            String field_from = fieldRec.getStr("field_from");
            String field_to = fieldRec.getStr("field_to");
            //主表关联值
            Record field_rec = FormService.getFieldName(field_from.split("\\.")[0], field_from.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
            String field_from_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
            Object from_field_value = rec.get(field_from_name);
            //从表关联值
            Record field_to_rec = FormService.getFieldName(field_to.split("\\.")[0], field_to.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
            String field_to_name = "f"+field_to_rec.getLong("id")+"_"+field_to_rec.getStr("field_name");
            
            List<Record> dataList = Db.find("select * from form_"+d_form_id+" where "+field_to_name+"=?", from_field_value);
            
            Record table_record = new Record(); 
            table_record.set("table_id", "detail_table_"+field_id);
            table_record.set("data_list", dataList);
            
            detailList.add(table_record);
        }
        
        List<Record> imgFieldList = Db.find("select * from  eeda_form_field field"
                + " where field.field_type='图片' "
                +" and field.form_id=?", form_id);
        for (Record imgFieldRec : imgFieldList) {
            List<Record> imgList = Db.find("select * from eeda_form_field_type_img where order_id = ? and field_id = ?",order_id,imgFieldRec.get("id"));
            imgFieldRec.set("imgList", imgList);
        }
        
        rec.set("detail_tables", detailList);
        rec.set("imgFieldList", imgFieldList);
        return rec;
    }
}
