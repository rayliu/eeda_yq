package controllers.app.form.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;

import controllers.app.form.service.AppFormService;
import controllers.form.FormService;
import controllers.form.FormTableConfigService;
import controllers.form.FormUtil;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AppFormController extends Controller {

    private Logger logger = Logger.getLogger(AppFormController.class);
    
    public void index() throws Exception{
        String module_id = getPara(0);
        String action = getPara(1);
        Long order_id = getParaToLong(2);
        String detailFieldId = getPara(3);
        String showMenu = getPara("menu");
        setAttr("showSideMenu", "N");
        if("Y".equals(showMenu)) {
            setAttr("showSideMenu", "Y");
            setAttr("side_menu", getMenuList());
        }
        setAttr("action", action);
        setAttr("module_id", module_id);
        setAttr("order_id", order_id);
        setAttr("detail_field_id", detailFieldId);
        //以下为表单的标准 action
        
        //list 跳转到form 列表查询 页面;
        //doQuery ajax 列表查询 动作; 
        
        //add 跳转到新增页面; 
        //doAdd 新增动作
        
        //edit 跳转到编辑页面; 
        //doUpdate 编辑的保存动作
        
        if("doQuery".equals(action)){
            Map<String,Object> listMap = queryForm(Long.valueOf(module_id));
            renderJson(listMap);
            return;
        }
        
        //click 表单按钮的动作
        //valueChange 表单按钮的动作, 参数 {field_name, value, old_value}
        Record formRec = Db.findFirst("select * from eeda_form_define where "
                + " module_id=?", module_id);
        if(formRec ==null){
            logger.debug("-------------form 没有定义!---------------");
            redirect("/");
            return;
        }
        Long form_id = formRec.getLong("id");
        setAttr("form_id", form_id);
        setAttr("form_name", formRec.getStr("name"));
        
        
        if(!action.startsWith("do")){
            AppFormService afs = new AppFormService(this);
            if("edit".equals(action)){
                //edit(form_id, order_id, formRec);
            }else if("view".equals(action)){
                setAttr("order_id", order_id);
                List<Record> recList = Db.find(
                        "select * from eeda_form_btn where form_id=? and type=?", form_id, "app_btn_edit");
                setAttr("btnList", recList);
            }else if("add".equals(action)){
                afs.edit(form_id, null, formRec);
            }else if("list".equals(action)){
                if("Y".equals(formRec.getStr("is_single_record"))){//单页显示，不需要list
                    Record orderRec = Db.findFirst("select * from form_"+form_id);
                    if(orderRec!=null){//有一条数据，跳转去edit页面
                        redirect("/app/form/"+module_id+"-edit-"+orderRec.getLong("id")+"?menu=Y");
                    }else{//无数据，跳转去add页面，有可能是一个展示页面，不需要记录数据
                        redirect("/app/form/"+module_id+"-add?menu=Y");
                    }
                }else{
                    list(form_id);
                    render("/lego_app/form/list.html");
                }
                return;
            }else if("detailList".equals(action)){
                //最后一个参数3是从表的field.id, 通过它获取从表的form_id, 再关联出从表数据
                //getDetaiList(form_id, detailFiledId);
                render("/lego_app/form/form_detail_list.html");
                return;
            }
            render("/lego_app/form/edit.html");
        }else{
            Record rec = new Record();
            if("doGet".equals(action)){
                //获取APP 模板中的显示字段
                List<Record> fieldList = getForm(module_id, form_id, order_id, formRec);
                renderJson(fieldList);
                
                return;
            }else if ("doAdd".equals(action) || "doUpdate".equals(action)){
//                rec = saveForm();
//                renderJson(rec);
            }
            
        }
        
        render("/lego_app/form/edit.html");
    }
    
    //获取APP 模板中的显示字段
    private List<Record> getForm(String module_id, Long form_id, Long order_id, Record formRec){
        Long office_id=formRec.getLong("office_id");
        List<Record> fieldList = new LinkedList<Record>();
        
        Record orderRec = Db.findFirst("select * from form_"+form_id+" where id=?", order_id);
        String form_name = formRec.getStr("name");
        
        String app_template_content = formRec.getStr("app_template");
        
        //根据app_template_content往页面输出field_list，然后页面通过JS生成页面元素
        Document doc = Jsoup.parseBodyFragment(app_template_content);
        Element body = doc.body();
        Elements ps = body.getElementsByTag("p");
        for (Element p : ps) {
            Record fieldRec = new Record();
            String fieldName=p.text();
            fieldName = fieldName.replace("#{", "").replace("}", "");
            Record field_rec = FormService.getFieldName(fieldName.split("\\.")[0], fieldName.split("\\.")[1], office_id);//获取数据库对应的名称: f59_xh
            String field_name = "f"+field_rec.getLong("id")+"_"+field_rec.getStr("field_name");
            String value= "";
            if(fieldName.indexOf("明细表")>=0 || fieldName.indexOf("从表")>=0){
                fieldRec.set("display_type", "list");
                fieldRec.set("display_name", fieldName.split("\\.")[1]);
                //最后一个参数是从表的field.id, 通过它获取从表的form_id, 再关联出从表数据
                fieldRec.set("value", "/app/form/"+module_id+"-detailList-"+order_id+"-"+field_rec.getLong("id"));//link
                fieldList.add(fieldRec);
            }else if(orderRec.get(field_name.toUpperCase())!=null){
                value= orderRec.get(field_name.toUpperCase()).toString();
                fieldRec.set("display_type", "field");
                fieldRec.set("display_name", field_rec.getStr("field_display_name"));
                fieldRec.set("value", value);
                fieldList.add(fieldRec);
            }
        }
        return fieldList;
    }
    
    private Map<String,Object> queryForm(Long form_id){
        List<Record> orderList = new ArrayList<Record>();
        String str = (getPara("s")==null?"":getPara("s"));
        //获取APP查询列
        String condition = "";
        List<Record> colList = Db.find("select * from eeda_form_field where form_id=? and app_display_col='Y'", form_id);
        for (Record colRec : colList) {
            String colName = "f"+colRec.getLong("id")+"_"+colRec.getStr("field_name");
            String likeStr = colName+" like '%"+str+"%'";
            condition = " and "+likeStr;
        }
        
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        Record displayFieldRec = Db.findFirst("select * from eeda_form_field where "
                + " form_id=? and app_display_col='Y'", form_id);
        Long fieldId=displayFieldRec.getLong("id");
        String fieldName=displayFieldRec.getStr("field_name");
        String displayFieldName=displayFieldRec.getStr("field_display_name");
        String sql = "";
        Record define = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
        if("search_form".equals(define.get("type"))){
//            String lieNameStr = getLieNameStr(form_id);
//            String biaoNameStr = "";//getBiaoNameStr(form_id);//join
//            String joinStr = getJoinStr(form_id);//left join
//            String filterStr = getFilterStr(form_id);
//            sql = "select "+lieNameStr+" from "+biaoNameStr+" "+joinStr+" where 1=1 ";
//            int index = biaoNameStr.indexOf(" ");
//            //+ " order by "+biaoNameStr.substring(index)+".id desc " 
//            orderList = Db.find(sql+ condition+filterStr +sLimit);
        }else if("form".equals(define.get("type"))){
            sql = "select id, f"+fieldId+"_"+fieldName+" from form_"+form_id+" where 1=1 "; 
            orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        }

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        setAttr("queryTotal", sqlTotal);
        
        
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        return orderListMap;
    }
    
    
    private void list(Long form_id){
        //setAttr("btnList", getFormBtns(form_id, "list"));
        Record re = Db.findFirst("select * from eeda_form_define where id = ?",form_id);
        Record displayField = Db.findFirst("select * from eeda_form_field where "
                + " form_id=? and app_display_col='Y'", form_id);
        setAttr("form_id", form_id);
        setAttr("display_field", displayField);
        if(displayField==null) return;
        String field_col_name = "f"+displayField.getLong("id")+"_"+displayField.getStr("field_name");
        setAttr("field_col_name", field_col_name);
    }
    
    private List<Record> getMenuList() {
        String key = "appMenu";
        List<Record> menuList = CacheKit.get("formCache",key);
        if(menuList!=null) return menuList;
        
        menuList = new ArrayList<Record>();
        UserLogin user= LoginUserController.getLoginUser(this);
        Long office_id = user.get("office_id");
        String role_id=getPara("role_id");
        List<Record> level1List = Db.find("select *, 1 level from eeda_modules "
                + "where parent_id is null and delete_flag='N' and office_id=? order by seq", office_id);
        for(Record lvl1:level1List) {
            //查询后台设置中，开放出来的module
            List<Record> level2List = Db.find("select m.*, 2 level from eeda_modules m, eeda_form_define fd "
                    + " where fd.module_id=m.id and fd.is_public='Y'"
                    + " and m.parent_id=? and m.delete_flag='N' and m.office_id=? order by seq", lvl1.getLong("id"), office_id);
            if(level2List==null || level2List.size()==0)
                continue;
            //获取CRUD权限
            handleOpeationPermission(level2List, office_id, role_id);
            
            menuList.add(lvl1);
            for(Record lvl2:level2List) {
                //判断当前的角色role是否有该menu的权限
                String sql = "select rp.role_id, rp.permission_id, " + 
                        " p.permission_name, p.permission_type, pm.menu_id, m.module_name menu_name" +
                        " from t_rbac_ref_role_permission rp " +
                        " left join t_rbac_permission p on rp.permission_id = p.id" +
                        " left join t_rbac_ref_permission_menu pm on rp.permission_id = pm.permission_id and pm.office_id=?" +
                        " left join eeda_modules m on pm.menu_id = m.id"+
                        " where rp.role_id=? and m.id=?";
                Record menuRec = Db.findFirst(sql, office_id, role_id, lvl2.getLong("id"));
                if(menuRec!=null) {
                    lvl2.set("is_menu_open", "Y");
                }else {
                    lvl2.set("is_menu_open", "N");
                }
            }
            lvl1.set("lvl2_list", level2List);
        }
        CacheKit.put("formCache",key, menuList);
        return menuList;
    }
    
    private void handleOpeationPermission(List<Record> level2List, long office_id, String role_id) {
        for(Record lvl2:level2List) {
            Long moduleId = lvl2.getLong("id");
            //判断当前的角色role是否有CRUD operation的权限
            String sql = "select rp.role_id, rp.permission_id, " + 
                    "   p.permission_name, p.permission_type" + 
                    "   from t_rbac_ref_role_permission rp " + 
                    "   left join t_rbac_permission p on rp.permission_id = p.id" + 
                    " where p.permission_type='operation' and rp.office_id=? and rp.role_id=? and p.module_id=?";
            List<Record> permissionList = Db.find(sql, office_id, role_id, moduleId);
            lvl2.set("permission_list", permissionList);
        }
    }
    
    public void detailTable() {
        String form_id_str = getPara(0);
        String order_id_str = getPara(1);
        String field_id = getPara(2);
        
        Long form_id=0l;
        if(StrKit.notBlank(form_id_str)) {
            form_id=Long.valueOf(form_id_str);
        }
        Long order_id=0l;
        if(StrKit.notBlank(order_id_str)) {
            order_id=Long.valueOf(order_id_str);
        }
        
        UserLogin user= LoginUserController.getLoginUser(this);
        Long office_id = user.get("office_id");
        
        FormTableConfigService ets = new FormTableConfigService(this);
        //获取子表的字段设置属性
        ArrayList<String> fieldIdList = new ArrayList<String>();
        fieldIdList.add(field_id);
        List<Record> table_define_list = ets.getTableConfig(fieldIdList, office_id);
        //获取整个form的数据
        Record data_rec = FormUtil.getFormData(form_id, order_id, office_id);
        List<Record> data_list = new LinkedList<Record>();
        List<Record> detail_table_list =data_rec.get("DETAIL_TABLES");
        String table_id = "detail_table_"+field_id;
        for (Record record : detail_table_list) {
            if(table_id.equals(record.getStr("TABLE_ID"))) {
                data_list=record.get("DATA_LIST");
            }
        }
        
        Record returnRec = new Record();
        returnRec.set("table_define_list", table_define_list);
        returnRec.set("data_list", data_list);
        renderJson(returnRec); 
    }
    
    public void detailList(){
        render("/lego_app/form_detail_list.html");
    }
    
    //专门给Module做预览用
    public void preview(){
        render("/lego_app/module/formTemplate.html");
    }
}
