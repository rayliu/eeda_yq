package controllers.form;

import java.util.List;

import models.UserLogin;

import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;

public class FormEventConfigService {
    private Controller cont = null;
    public FormEventConfigService(Controller cont){
        this.cont = cont;
    } 
    
    public List<Record> getEventConfig(UserLogin user, Long form_id) {
        Long office_id = user.getLong("office_id");
        String key = "FormEventConfig_"+form_id;
        List<Record> itemList = CacheKit.get("formCache",key);
        if(itemList!=null) {
            LogKit.info(key+" load from cache...");
            return itemList;
        }
        
        
        itemList = Db.find("select * from eeda_form_event where menu_type='default_event_add_after_open'"
                + " and form_id=?", form_id);
        for (Record record : itemList) {
            String type = record.getStr("type");
            if("set_value".equals(type)){
                Record cssRec = Db.findFirst("select * from eeda_form_event_set_value where event_id=?", record.getLong("id"));
                record.set("set_value", cssRec);
                
                List<Record> list = Db.find("select * from eeda_form_event_set_value_item where "
                        + " event_id=?", record.getLong("id"));
                for (Record rec : list) {
                    String name = rec.getStr("name");
                    Record field_rec = FormService.getFieldName(name.split("\\.")[0], name.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
                    String field_name = "form_"+field_rec.getLong("form_id")+"-f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
                    rec.set("field_name", field_name);
                    
                    String value = rec.getStr("value");
                    if("系统变量.当前用户名".equals(value)){
                        String userName=user.getStr("c_name");
                        rec.set("value", userName);
                    }
                }
                record.set("set_value_item", list);
            }
        }
        CacheKit.put("formCache", key, itemList);
        return itemList;
    }
}
