package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.Toll;
import models.UserLogin;
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

import controllers.eeda.ListConfigController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
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

    @Before(EedaMenuInterceptor.class)
    public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/containerType");
        setAttr("listConfigList", configList);
        render("/profile/containerType/list.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/containerType/edit.html");
    }
    
    public void list() {
        String sLimit = "";
        UserLogin user1 = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user1.getLong("office_id");
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT * from container_type where 1 =1 and office_id="+office_id;
        
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
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        UserLogin user1 = LoginUserController.getLoginUser(this);
        if (user1==null) {
            return;
        }
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("container_type", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
        ContainerType u = ContainerType.dao.findById(id);
        setAttr("order", u);
        
        render("/profile/containerType/edit.html");
        
    }

    // 删除条目
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
        UserLogin user1 = LoginUserController.getLoginUser(this);
        if(user1==null){
        	return;
        }
        long office_id=user1.getLong("office_id");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        String id = (String) dto.get("id");
        String container_type = (String) dto.get("name");
        if (StringUtils.isBlank(id)) {
            ContainerType r = new ContainerType();
            r.set("container_type", container_type);
            r.set("office_id", office_id);
            boolean s = r.save();
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
