package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.eeda.profile.Currency;
import models.eeda.profile.CurrencyRate;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.DbUtils;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CurrencyRateController extends Controller {
    private Log logger = Log.getLog(CurrencyRateController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_LIST})
    public void index() {
        render("/profile/currencyRate/list.html");
    }
    
    public void create() {
        render("/profile/currencyRate/edit.html");
    }

    // 编辑
    public void edit() {
        String id = getPara("id");
        if (id != null) {
            CurrencyRate currency = CurrencyRate.dao.findById(id);
            setAttr("order", currency);
        }
        render("/profile/currencyRate/edit.html");

    }

    // 添加金融账户
    @Before(Tx.class)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        CurrencyRate order = new CurrencyRate();
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            //update
            order = CurrencyRate.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.update();
        } else {
            //create 
            DbUtils.setModelValues(dto, order);
            order.set("creator",LoginUserController.getLoginUserId(this));
            order.set("create_stamp",new Date());
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
    }

    // 删除
    public void delete() {
        String id = getPara("id");
        CurrencyRate currency = null;
        if (id != null) {
        	currency = CurrencyRate.dao.findById(id);
        	currency.set("status", "del");
        	currency.update(); 
        }
        renderJson(currency);
    }

    // 列出
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "select cu.*,ul.c_name creator_name from currency_rate cu"
        		+ " left join user_login ul on ul.id = cu.creator"
        		+ " where 1 = 1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orders = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", rec.getLong("total"));
        orderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderListMap.put("aaData", orders);

        renderJson(orderListMap); 
    }
    
}
