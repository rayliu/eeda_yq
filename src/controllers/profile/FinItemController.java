package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.eeda.profile.FinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FinItemController extends Controller {
    private Log logger = Log.getLog(FinItemController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

    //查询费用中文名称
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})
    public void search() {
        String input = getPara("input");
        
        List<Record> finItems = null;
        if (input !=null && input.trim().length() > 0) {
            finItems = Db.find("select * from fin_item where name like '%"+input+"%' limit 10");
        }else{
            finItems = Db.find("select * from fin_item limit 10");
        }
        renderJson(finItems);
    }
    
    //查询费用英文名称
    public void search_eng() {
    	String input = getPara("input");
    	
    	List<Record> finItems = null;
    	if (input !=null && input.trim().length() > 0) {
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f where f.name_eng like '%"+input+"%'");
    	}else{
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f");
    	}
    	renderJson(finItems);
    }

    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_LIST })
    public void index() {
        render("/profile/finItem/finItemList.html");
    }
    
    public void create() {
        render("/profile/finItem/finItemEdit.html");
    }
    

    public void list() {
    	
        String code = getPara("code");
        String name = getPara("name");
        String name_eng = getPara("name_eng");
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "";
        if(code==null&&name==null&&name_eng==null){
        	sql = "SELECT id,code,name_eng,name,remark from fin_item where 1 =1 ";
        }else{
        	sql = "SELECT id,code,name_eng,name,remark from fin_item where 1 =1 "
        			+ " and code like '%"+code
        			+"%' and name like '%"+name
        			+"%' and name_eng like '%"+name_eng+"%'";
        }

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql + " order by id desc " +sLimit);
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap); 
    }

    // 编辑条目按钮
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void edit() {
        String id = getPara("id");
        FinItem u = FinItem.dao.findById(id);
        setAttr("order", u);
        
        render("/profile/finItem/finItemEdit.html");
        
    }

    // 删除条目
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_DELETE })
    public void delete() {
        String id = getPara();
        if (id != null) {
        	FinItem l = FinItem.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/finItem");
    }

    // 添加编辑保存
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        FinItem r = null;
        
        String id = (String) dto.get("id");
        String code = (String) dto.get("code");
        String name = (String) dto.get("name");
        String name_eng = (String) dto.get("name_eng");
        String remark = (String) dto.get("remark");
        
        if (StringUtils.isBlank(id)) {
        	r = new FinItem();
        	r.set("code", code);
        	r.set("name", name);
            r.set("name_eng", name_eng);
            r.set("remark", remark);            
            r.save();
        } else {
        	r = FinItem.dao.findById(id);
        	r.set("code", code);
        	r.set("name", name);
            r.set("name_eng", name_eng);
            r.set("remark", remark);            
            r.update();
        }
        renderJson(r);
    }
    
    //校验是否存在此费用
    public void checkCodeExist(){
    	String para= getPara("code");
    	String sql = "select * from fin_item where code = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameExist(){
    	String para= getPara("name");
    	String sql = "select * from fin_item where name = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameEngExist(){
    	String para= getPara("name_eng");
    	String sql = "select * from fin_item where name_eng = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    
}
