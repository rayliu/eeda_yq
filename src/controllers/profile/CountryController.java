package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.eeda.profile.Country;

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
public class CountryController extends Controller {
    private Log logger = Log.getLog(CountryController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

    public void searchAllCountry() {
        List<Record> countrys = Db.find("select * from country");
        renderJson(countrys);
    }

    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/profile/country/countryList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/country/countryEdit.html");
    }
    
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "SELECT * from country where 1 =1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map<String,Object> BillingOrderListMap = new HashMap<String,Object>();
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
        Country u = Country.dao.findById(id);
        setAttr("order", u);
        
        render("/profile/country/countryEdit.html");
        
    }

    // 删除条目
    public void delete() {
        String id = getPara("id");
        if (id != null) {
        	Country l = Country.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals("0")) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/country");
    }

    // 添加编辑保存
    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        Country r = null;
        
        String id = (String) dto.get("id");
        String code = (String) dto.get("code");
        String english_name = (String) dto.get("english_name");
        String chinese_name = (String) dto.get("chinese_name");
        
        if (StringUtils.isBlank(id)) {
        	r = new Country();
            r.set("code", code);
            r.set("english_name", english_name);
            r.set("chinese_name", chinese_name);
            r.save();
        } else {
        	r = Country.dao.findById(id);
        	r.set("code", code);
        	r.set("english_name", english_name);
            r.set("chinese_name", chinese_name);
            r.update();
        }
        renderJson(r);
    }
}
