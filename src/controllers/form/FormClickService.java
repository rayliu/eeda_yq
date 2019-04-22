package controllers.form;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.FormController;
import controllers.util.DbUtils;
import controllers.util.PingYinUtil;
import controllers.util.PoiUtils;

public class FormClickService {
    private Controller cont = null;
    public FormClickService(Controller cont){
        this.cont = cont;
    } 
    
    public List<Record> handleClickAction(Record title, Long form_id,
            Long btn_id, Long office_id) throws NumberFormatException, Exception {
        FormService fs = new FormService(cont);
        List<Record> recList = Db.find("select * from eeda_form_event where btn_id=?", btn_id);
        for (Record event : recList) {
            String eventJson=event.getStr("event_json");
            if(StrKit.isBlank(eventJson)) continue;
            Gson gson = new Gson();
            List<Map<String,Object>> jsonList = gson.fromJson(eventJson,new TypeToken<List<Map<String,Object>>>() { }.getType());
            List<Map<String,Object>> childrenList= (List)jsonList.get(0).get("children");
            for (Map<String, Object> map : childrenList) {
                String actionType=(String)map.get("action_type");
                String orderId = cont.getPara("order_id");
                Map<String, Object> event_action_setting=null;
                switch (actionType) {
                    case "print":
                        List<Record> template_list = fs.getPrintTemplate(form_id,Long.valueOf(orderId));
                        event.set("template_list", template_list);
                        break;
                    case "form_set_value"://表单赋值
                        event_action_setting=(Map)map.get("event_action_setting");
                        fs.setValue(event_action_setting,form_id,Long.valueOf(orderId));
                        break;
                    case "element_set_droplist"://本表单改变某个字段的值
                        event_action_setting=(Map)map.get("event_action_setting");
                        fs.setDroplist(event_action_setting,form_id,Long.valueOf(orderId));
                        break;
                    default:
                        break;
                }
            }
            //旧逻辑
            if("open".equals(event.getStr("type"))){
//                Record rec = Db.findFirst("select * from eeda_form_event_open where event_id=?", event.getLong("id"));
//                event.set("open", rec);
            }else if("print".equals(event.getStr("type"))){
                 
            }else if("list_add_row".equals(event.getStr("type"))){
                Record rec = Db.findFirst("select * from eeda_form_event_list_add_row where event_id=?", event.getLong("id"));
                String field = rec.getStr("target_field_name");
                Record target_field_rec = FormService.getFieldName(field.split("\\.")[0], field.split("\\.")[1], office_id);
                rec.set("field_id", target_field_rec.getLong("id"));
                event.set("list_add_row", rec);
            }else if("set_value".equals(event.getStr("type"))){
                
            }
            //导出excel
            else if("export_excel".equals(event.getStr("type"))){  
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                String fileName = title.get("level2") + format.format(date) + ".xls";
                String filePath = "/download/download_order";
                
                Record define = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
                String condition = DbUtils.buildConditions(cont.getParaMap());
                if("search_form".equals(define.get("type"))){
                    //自定义查询
                    String lieNameStr = FormController.getLieNameStr(form_id, office_id);
                    String biaoNameStr = "";//getBiaoNameStr(form_id);//join
                    String joinStr = FormController.getJoinStr(form_id, office_id);//left join
                    String filterStr = FormController.getFilterStr(form_id);
                    String sql = "select "+lieNameStr+" from "+biaoNameStr+" "+joinStr+" where 1=1 ";

                    //做生成excel处理
                    String sqlExport = sql+ condition+filterStr;
                    List<Record> list = FormController.getDisplayCols(form_id, office_id);
                    String[] title_name = new String[list.size()];
                    for(Record item : list){
                        String name = item.getStr("FIELD_DISPLAY_NAME");
                        title_name[list.indexOf(item)] = name;
                    }
                    
                    String lieNameStrWithoutTable = getLieNameStrWithoutTable(form_id);
                    String[] content = lieNameStrWithoutTable.toUpperCase().split(",");
                    
                    boolean result = PoiUtils.generateExcel(title_name, content, sqlExport,PathKit.getWebRootPath()+filePath, fileName);
                    if(result){
                        System.out.println("ok");
                    }else{
                        System.out.println("error");
                    }  
                } else {
//                      表单列表查询
                    List<Record> fieldList = Db.find("select * from eeda_form_field where "
                            + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
                    String[] title_name = new String[fieldList.size()];
                    String[] content = new String[fieldList.size()];
                    for(Record item : fieldList){
                        String name = item.getStr("FIELD_DISPLAY_NAME");
                        String text = item.getStr("FIELD_NAME").toUpperCase();
                        String item_id = item.get("ID").toString();
                        title_name[fieldList.indexOf(item)] = name;
                        content[fieldList.indexOf(item)] = "F"+item_id+ "_"+text;
                    }
                    String sql = "select * from form_"+form_id+" where 1=1 "; 
                    String sqlExport = sql+ condition + " order by id desc ";
                    
                    boolean result = PoiUtils.generateExcel(title_name, content, sqlExport,PathKit.getWebRootPath()+filePath, fileName);
                    if(result){
                        System.out.println("ok");
                    }else{
                        System.out.println("error");
                    }  
                }
                event.set("template_name", filePath+ "/" + fileName);
            }
            //下载导入excel模板
            else if("download_template".equals(event.getStr("type"))){
                String fileName = title.get("level2") + ".xls";
                String filePath = "/download/download_template";
                File file = new File(PathKit.getWebRootPath() + filePath + fileName);
                Record define = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
                //做生成excel处理
                String sqlExport = "select 1 = 1;";
                List<Record> list = null;
                if("search_form".equals(define.get("type"))){
                    list = FormController.getDisplayCols(form_id, office_id);
                }else if("form".equals(define.get("type"))){
                    list = Db.find("select * from eeda_form_field where "
                            + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
                }
                String[] title_name = new String[list.size()];
                for(Record item : list){
                    String name = item.getStr("FIELD_DISPLAY_NAME");
                    title_name[list.indexOf(item)] = name;
                }
                String[] content = {};
                
                boolean result = PoiUtils.generateExcel(title_name, content, sqlExport,PathKit.getWebRootPath()+filePath, fileName);
                if(result){
                    System.out.println("ok");
                }else{
                    System.out.println("error");
                }  
                
                event.set("template_name", filePath+ "/" + fileName);
            }else if("import_excel".equals(event.getStr("type"))){
                event.set("form_id", form_id);
            }
        }
        return recList;
    }
    
    private String getLieNameStrWithoutTable(Long form_id){
        String lieNameStr = "";
        List<Record> colsList = Db.find("select * from eeda_form_custom_search_cols where form_id = ?",form_id);
        for(int i = 0;i<colsList.size();i++){
            String expression = colsList.get(i).get("expression");
            int index = expression.indexOf(".");
            String lie_name = expression.substring(index+1);
            String biao_name = expression.substring(0,index);
            String biao_name_py = PingYinUtil.getFirstSpell(expression.substring(0,index));
            Record define = Db.findFirst("select * from eeda_form_define where name = ?",biao_name);
            Record field = Db.findFirst("select * from eeda_form_field where field_display_name = ? and form_id = ?",lie_name,define.get("id"));
            String str = "";
            if(lieNameStr.length()>0){
                str = ",";
            }
            if("ID".equals(lie_name)){
                lieNameStr += "id";
            }else{
                lieNameStr += str+"f"+field.get("id")+"_"+field.get("field_name");
            }
        }
        return lieNameStr;
    }
}
