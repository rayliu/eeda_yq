package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.eeda.profile.Account;

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
public class AccountController extends Controller {
    private Log logger = Log.getLog(LoginUserController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/profile/account/list.html");
    }
    // 链接到添加金融账户页面
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/account/edit.html");
    }

    // 编辑金融账户信息
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("order", l);
        }
        render("/profile/account/edit.html");

    }

    // 添加金融账户
    @Before(Tx.class)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        Account order = new Account();
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            //update
            order = Account.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.update();
        } else {
            //create 
            DbUtils.setModelValues(dto, order);
            order.set("office_id", LoginUserController.getLoginUser(this).get("office_id"));
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
    }

    // 删除金融账户
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_DELETE})
    public void del() {
        String id = getPara();
        if (id != null) {
            /*Db.deleteById("fin_account", id);*/
        	Account account = Account.dao.findById(id);
        	 Object obj = account.get("is_stop");
             if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	 account.set("is_stop", true);
             }else{
            	 account.set("is_stop", false);
             }
             account.update();
        }
        render("/profile/account/list.html");
    }

    // 列出金融账户信息
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "select * from fin_account where office_id = "+ pom.getCurrentOfficeId();
        
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
