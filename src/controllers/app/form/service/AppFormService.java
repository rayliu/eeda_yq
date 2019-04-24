package controllers.app.form.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.form.FormService;
import controllers.form.TemplateService;
import controllers.form.event.EventService;

public class AppFormService {
    private Controller cont = null;
    public AppFormService(Controller cont){
        this.cont = cont;
    } 
    
    public void edit(Long form_id, Long order_id, Record formRec) throws Exception{
        Long officeId = formRec.getLong("office_id");
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=?", form_id);
        
        
        List<Record> eventsList = Db.find("select * from eeda_form_event where form_id=?", form_id);
        formRec.set("events", eventsList);
        
        String form_name = formRec.getStr("name");
        String template_content = formRec.getStr("app_template");
        TemplateService ts = TemplateService.getInstance();
        template_content = ts.processTab(template_content);
        template_content = ts.processCharts(template_content, officeId);

        for (Record fieldRec : fieldList) {
            String fieldDisplayName=fieldRec.getStr("field_display_name");
            String fieldName=fieldRec.getStr("field_name");
            String replaceNameOrigin = "#{"+form_name+"."+fieldDisplayName+"}";
            String fieldType = fieldRec.getStr("field_type");
            String read_only = fieldRec.getStr("read_only");
            String replaceNameDest ="";
            String inputId = "form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase();
            
            String requiredStr = "";
            if("Y".equals(fieldRec.getStr("REQUIRED"))){
                requiredStr = "<span style='float:left;color:red;line-height: 31px;font-size: 16px;margin-left: -10px;'>*</span>";
            }
            String disabled = "";
            if("Y".equals(read_only)){
                disabled = "disabled";
            }
            if("自动编号".equals(fieldType)){
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='formControls col-xs-8 col-sm-8'>"
                        + "  <input type='text' name='"+inputId+"' class='input-text' autocomplete='off'  placeholder='系统自动生成' disabled>"
                        + "</div>"+requiredStr;
            }else if("文本".equals(fieldType)||"网址".equals(fieldType)){
                
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='formControls col-xs-8 col-sm-8'>"
                        + "  <input type='text' name='"+inputId+"' class='input-text "+disabled+"' autocomplete='off' "+disabled+" >"
                        + "</div>"+requiredStr;
            }else if("全国城市".equals(fieldType)){
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<div class='col-xs-8 col-sm-8'>"+
                        "    <input id='"+inputId+"_province' type='text' class='province' field_type='list' value='' style='display:none;'/>"+
                        "    <input id='"+inputId+"' type='text' field_type='list' value='' style='display:none;'/>"+
                        "    <input type='text' class='input-text city_input "+disabled+"'"+
                        "    name='"+inputId+"' autocomplete='new-password'"+
                        "    placeholder='请选择城市' "+disabled+" >"+
                        "    <div id='"+inputId+"_list' class='area-list pull-right dropDown-menu default dropdown-scroll' tabindex='-1'  "+
                        "    style='top: 35%; left: 2%; display: none;'>"+
                        "        <div class='area-list-title'>"+
                        "            <input data-id='0' data-level='0' type='button' value='省份' class='this'>"+
                        "            <input data-id='0' data-level='1' type='button' value='城市'>"+
                        "            <input data-id='0' data-level='2' type='button' value='县区'>"+
                        "            <span class='tips'>如不需选县区，请点击外面空白区域</span>"+
                        "        </div>"+
                        "        <div class='area-list-content' style='clear:both;'>"+
                        "            "+
                        "        </div>"+
                        "    </div>"+
                        "        "+
                        "    <ul id='"+inputId+"_list——1' class='pull-right dropDown-menu default dropdown-scroll' tabindex='-1' style='top: 35%; left: 2%;'>"+
                        "    </ul>"+
                        "</div>"+requiredStr;
            }else if("日期".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <input type='text' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd\"})' name='"+inputId+"' class='input-text Wdate'>"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("日期时间".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <input type='text' onfocus='WdatePicker({dateFmt:\"yyyy-MM-dd HH:mm:ss\"})' name='"+inputId+"' class='input-text Wdate "+disabled+"' "+disabled+">"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("多行文本".equals(fieldType)||"网页HTML".equals(fieldType)){
                replaceNameDest = "<div id='"+inputId+"_div'>"
                        + "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + " <div class='formControls col-xs-8 col-sm-8'>"
                        + "    <textarea class='textarea valid "+disabled+"' placeholder='' name='"+inputId+"' "+disabled+"></textarea>"
                        + " </div> "
                        + "</div> "+requiredStr;
            }else if("复选框".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_checkbox(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
//            }else if("从表引用".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_detail(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
//            }else if("字段引用".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_ref(form_name, fieldRec, fieldRec.getLong("id"), officeId);
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div' style='height:0px;'>"+replaceNameDest+"</div> "+requiredStr;
//            }else if("按钮".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_btn(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
//            }else if("下拉列表".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_dropdown(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
//            }else if("附件".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_fileUpload(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> "+requiredStr;
//            }else if("图片".equals(fieldType)){
//                FormService fs = new FormService(this);
//                replaceNameDest = fs.processFieldType_imgUpload(form_name, fieldRec, fieldRec.getLong("id"));
//                replaceNameDest="<div id='"+form_name+"-"+fieldDisplayName+"_div'>"+replaceNameDest+"</div> ";
            }else{
                replaceNameDest = "<label class='search-label'>"+fieldDisplayName+"</label>"
                        + "<input type='text' name='"+inputId+"' class='form-control'>";
            }
            template_content = template_content.replace(replaceNameOrigin, replaceNameDest);
        }
        
        //不需要在这里替换值, 应该是把UI组件+id 显示出来, 再通过JSON set value进去
//        GroupTemplate gt = getGroupTemplate();
//        Template t = gt.getTemplate(template_content);
//        t.binding("t_"+form_id, orderRec); 
//        String form_content = t.render(); 
        
        formRec.set("fields", fieldList);
        formRec.set("template_content", "");
        cont.setAttr("form_define", JsonKit.toJson(formRec));
        cont.setAttr("order_id", order_id);
        cont.setAttr("form_content", template_content);
        cont.setAttr("btnList", getFormBtns(form_id, "edit"));
        cont.setAttr("charts", getFormCharts(form_id));
    }
    
    private List<Record> getFormBtns(Long formId, String type) {
        List<Record> recList = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?", formId, type);
        return recList;
    }
    
    private Record getFormCharts(Long formId) {
        Record rec = Db.findFirst(
                "select * from eeda_form_charts where form_id=?", formId);
        return rec;
    }
}
