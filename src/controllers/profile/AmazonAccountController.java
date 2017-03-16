package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.profile.Account;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.call.FetchTokenCall;
import com.ebay.sdk.call.GetSessionIDCall;
import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.oms.salesOrder.EbayApiContextUtil;
import controllers.util.DbUtils;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AmazonAccountController extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(AmazonAccountController.class);
    
    @Before(EedaMenuInterceptor.class)
    public void index() throws Exception {
        render("/profile/amazonAccount/amazonAccountList.html");
    }
    
    // 编辑金融账户信息
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("order", l);
        }
        render("/profile/ebayAccount/edit.html");

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

    // 停用账户
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
        render("/profile/ebayAccount/ebayAccountList.html");
    }

    // 列出账户信息
    public void list() {
        String sLimit = "";

        String pageIndex = getPara("draw");
        logger.debug(getPara("length"));
        if (getPara("start") != null && getPara("length") != null && getParaToInt("length") !=-1 ) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        UserLogin user = LoginUserController.getLoginUser(this);
        String sql = "select * from amazon_seller_account where office_id = "+ user.getLong("office_id");
        
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
