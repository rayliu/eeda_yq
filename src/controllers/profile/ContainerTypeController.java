package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.Toll;
import models.UserOffice;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.truckOrder.TruckOrder;
import models.eeda.profile.ContainerType;
import models.eeda.profile.Unit;

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
public class ContainerTypeController extends Controller {
    private Log logger = Log.getLog(ContainerTypeController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

    public void searchAll() {
        List<Record> units = Db.find("select * from container_type");
        renderJson(units);
    }

    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_LIST })
    public void index() {
        render("/profile/containerType/list.html");
    }
    
    public void create() {
        render("/profile/containerType/edit.html");
    }
    
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_LIST })
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT * from container_type where 1 =1 ";
        
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
        ContainerType u = ContainerType.dao.findById(id);
        setAttr("order", u);
        
        render("/profile/containerType/edit.html");
        
    }

    // 删除条目
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_DELETE })
    public void delete() {
        String id = getPara();
        if (id != null) {
            ContainerType l = ContainerType.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/containerType");
    }

    // 添加编辑保存
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        String id = (String) dto.get("id");
        String container_type = (String) dto.get("name");
        if (StringUtils.isBlank(id)) {
            ContainerType r = new ContainerType();

            boolean s = r.set("container_type", container_type).save();
            if (s == true) {
                renderJson(r);
            }
        } else {
            ContainerType toll = ContainerType.dao.findById(id);
            boolean b = toll.set("container_type", container_type).update();
            renderJson(toll);
        }

    }
}
