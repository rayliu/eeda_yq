package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.UserLogin;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;











import com.google.gson.Gson;
//import cache.EedaServiceCache;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.json.Json;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FormController extends Controller {
    private Log logger = Log.getLog(FormController.class);
    private static GroupTemplate gt=null;
    
    private GroupTemplate getGroupTemplate() throws IOException{
        if(gt==null){
          StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader(); 
          Configuration cfg = Configuration.defaultConfiguration();
          gt = new GroupTemplate(resourceLoader, cfg);
        }
        return gt;
    }
    @Before(EedaMenuInterceptor.class)
    @SuppressWarnings("unchecked")
    public void index() throws IOException {
        logger.debug("thread["+Thread.currentThread().getName()+
                "] -------------Eeda form---------------");
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        long office_id = user.getLong("office_id");
        
        String module_id = getPara(0);
        String action = getPara(1);
        Long order_id = getParaToLong(2);
        //以下为表单的标准 action
        
        //list 跳转到form 列表查询 页面;
        //doQuery ajax 列表查询 动作; 
        
        //add 跳转到新增页面; 
        //doAdd 新增动作
        
        //edit 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        
        //doDelete 表单删除的动作
        
        //click 表单按钮的动作
        
        setAttr("action", action);
        setAttr("module_id", module_id);
        
        if("doQuery".equals(action)){
            
            Map listMap = queryForm(Long.valueOf(module_id));
            renderJson(listMap);
            return;
        }
        
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return;
        }
        Long form_id = formRec.getLong("id");
        logger.debug("-------------Eeda module:"+module_id+", form_id:"+form_id+", action: "+action+"---------------");
        
        if("click".equals(action)){
            Long btn_id = order_id;
            List<Record> recList = Db.find("select * from eeda_form_event where "
                    + " btn_id=?", btn_id);
            for (Record event : recList) {
                if("open".equals(event.getStr("type"))){
                    Record rec = Db.findFirst("select * from eeda_form_event_open where event_id=?", event.getInt("id"));
                    event.set("open", rec);
                }
            }
            renderJson(recList);
        }else if(!action.startsWith("do")){
            if("edit".equals(action)){
                edit(form_id, order_id, formRec);
            }else if("view".equals(action)){
                
            }else if("add".equals(action)){
                edit(form_id, -1l, formRec);
            }else if("list".equals(action)){
                list(form_id);
                render("/eeda/form/listTemplate.html");
                return;
            }
            
            render("/eeda/form/template.html");
        }else{
            Record rec = new Record();
            if("doGet".equals(action)){
                rec = getForm(form_id, order_id);
                renderJson(rec);
            }else if ("doAdd".equals(action)){
                rec = saveForm();
                renderJson(rec);
            }
            
        }
    }
    
    private Record saveForm(){
        Record rec = new Record();
        String jsonStr=getPara("data");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String module_id = (String) dto.get("module_id");
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return rec;
        }
        Long form_id = formRec.getLong("id");
        for (Entry<String, ?> entry : dto.entrySet()) { 
            String key = entry.getKey();
            if(key.startsWith("form_"+form_id)){
                String colName = key.split("-")[1];
                String value = String.valueOf(entry.getValue()).trim();
                rec.set(colName, value);
            }
        }
        Db.save("form_"+form_id, rec);
        
        return rec;
    }
    private List<Record> list(Long form_id){
        setAttr("btnList", getFormBtns(form_id, "list"));
        
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=? order by if(isnull(seq),1,0), seq", form_id);
        setAttr("form_id", form_id);
        setAttr("field_list", fieldList);
        setAttr("field_list_json", JsonKit.toJson(fieldList));
        return fieldList;
    }
    
    private List<Record> getFormBtns(Long formId, String type) {
        List<Record> recList = Db.find(
                "select * from eeda_form_btn where form_id=? and type=?", formId, type);
        return recList;
    }
    
    private Record getForm(Long form_id, Long order_id){
        Record rec = Db.findFirst("select * from form_"+form_id+" where "
                + " id=?", order_id);
        return rec;
    }
    
    private Map queryForm(Long form_id){
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from form_"+form_id+" where 1=1 "; 
        List<Record> list = Db.find(sql);
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        return orderListMap;
    }

    private void edit(Long form_id, Long order_id, Record formRec) throws IOException{
        List<Record> fieldList = Db.find("select * from eeda_form_field where "
                + " form_id=?", form_id);
        
        Record orderRec = Db.findFirst("select * from form_"+form_id+" where id=?", order_id);
        String form_name = formRec.getStr("name");
        
        String template_content = formRec.getStr("template_content");
        for (Record fieldRec : fieldList) {
            String fieldDisplayName=fieldRec.getStr("field_display_name");
            String fieldName=fieldRec.getStr("field_name");
            String replaceNameOrigin = "#{"+form_name+"."+fieldDisplayName+"}";
            String replaceNameDest = "<input type='text' name='form_"+form_id+"-f"+fieldRec.get("id")+"_"+fieldName.toLowerCase()+"'>";
            template_content = template_content.replace(replaceNameOrigin, replaceNameDest);
        }
        
        //不需要在这里替换值, 应该是把UI组件+id 显示出来, 再通过JSON set value进去
//        GroupTemplate gt = getGroupTemplate();
//        Template t = gt.getTemplate(template_content);
//        t.binding("t_"+form_id, orderRec); 
//        String form_content = t.render(); 
        
        formRec.set("fields", fieldList);
        formRec.set("template_content", "");
        setAttr("form_define", JsonKit.toJson(formRec));
        
        setAttr("order_id", order_id);
        
        setAttr("form_content", template_content);
        
        setAttr("btnList", getFormBtns(form_id, "edit"));
    }
    
}
