package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.profile.Currency;

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
public class CurrencyController extends Controller {
    private Log logger = Log.getLog(LoginUserController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/profile/currency/list.html");
    }
    // 链接到添加金融账户页面
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/currency/edit.html");
    }

    // 编辑金融账户信息
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        if (id != null) {
            Currency currency = Currency.dao.findById(id);
            setAttr("order", currency);
        }
        render("/profile/currency/edit.html");

    }

    // 添加金融账户
    @Before(Tx.class)
    public void save() {
        String jsonStr=getPara("params");
      //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        Currency order = new Currency();
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            //update
            order = Currency.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.set("updator",LoginUserController.getLoginUserId(this));
            order.set("update_stamp",new Date());
            order.set("office_id",office_id);
            order.update();
        } else {
            //create 
            DbUtils.setModelValues(dto, order);
            order.set("creator",LoginUserController.getLoginUserId(this));
            order.set("create_stamp",new Date());
            order.set("office_id",office_id);
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
    }

    // 删除金融账户
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_DELETE})
    public void delete() {
        String id = getPara("id");
        Currency currency = null;
        if (id != null) {
        	currency = Currency.dao.findById(id);
        	currency.delete(); 
        }
        renderJson(currency);
    }

    // 列出金融账户信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
      
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "select cu.*,ul.c_name creator_name from currency cu"
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
