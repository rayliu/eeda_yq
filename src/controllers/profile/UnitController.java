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
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class UnitController extends Controller {
    private Log logger = Log.getLog(UnitController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

    public void searchAllUnit() {
        List<Record> units = Db.find("select * from unit");
        renderJson(units);
    }

    @Before(EedaMenuInterceptor.class)
    public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/unit");
        setAttr("listConfigList", configList);
        render("/eeda/profile/unit/unitList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/eeda/profile/unit/unitEdit.html");
    }
    
    public void list() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "SELECT id,code,name_eng,name,type from unit where 1=1 "+condition+" and office_id="+office_id+" or office_id is null";

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
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        Unit u = Unit.dao.findById(id);
        setAttr("order", u);
        
        render("/eeda/profile/unit/unitEdit.html");
        
    }

    // 删除条目
    public void delete() {
        String id = getPara();
        if (id != null) {
            Unit l = Unit.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/unit");
    }

    // 添加编辑保存
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String jsonStr = getPara("params");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        Unit u = new Unit();
        if (StringUtils.isBlank(id)) {
        	//create
   			DbUtils.setModelValues(dto, u);
   			u.set("office_id", office_id);
            u.save();
            id = u.getLong("id").toString();
        } else {
            u = Unit.dao.findById(id);
            DbUtils.setModelValues(dto, u);
            u.update();
        }
        u.toRecord();
        renderJson(u);
    }
    
    //校验是否存在此单位
    public void checkCodeExist(){
    	String para= getPara("code");
    	String sql = "select * from unit where code = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此单位
    public void checkNameExist(){
    	String para= getPara("name");
    	String sql = "select * from unit where name = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此单位
    public void checkNameEngExist(){
    	String para= getPara("name_eng");
    	String sql = "select * from unit where name_eng = ?";
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
