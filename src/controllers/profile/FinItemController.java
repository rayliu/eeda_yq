package controllers.profile;

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

    public void searchAllFinItem() {
        List<Record> finItems = Db.find("select * from fin_item");
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
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT id,name,remark from fin_item where 1 =1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by id desc " +sLimit);
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
        String name = (String) dto.get("name");
        String remark = (String) dto.get("remark");
        
        if (StringUtils.isBlank(id)) {
        	r = new FinItem();

            r.set("name", name);
            r.set("remark", remark);            
            r.save();
        } else {
        	r = FinItem.dao.findById(id);

        	r.set("name", name);
            r.set("remark", remark);            
            r.update();
        }
        renderJson(r);
    }
}
