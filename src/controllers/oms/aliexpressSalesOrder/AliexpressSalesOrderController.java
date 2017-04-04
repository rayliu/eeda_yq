package controllers.oms.aliexpressSalesOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import net.sf.json.JSONObject;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.oms.ebaySalesOrder.EbayApiContextUtil;
import controllers.profile.LoginUserController;
import controllers.profile.ali.AuthService;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AliexpressSalesOrderController extends Controller {

    private Log logger = Log.getLog(AliexpressSalesOrderController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() {
        UserLogin user = LoginUserController.getLoginUser(this);
        String sql = "select * from aliexpress_seller_account where office_id = "
                + user.getLong("office_id");
        List<Record> ebayAcountList = Db.find(sql);
        setAttr("aliexpressAcountList", ebayAcountList);

        render("/oms/aliexpressSalesOrder/aliexpressSalesOrderList.html");
    }

    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");

        String type = getPara("type");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from aliexpress_sales_order where office_id="
                + office_id;

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by modified_time desc " + sLimit);

        for (Record r : orderList) {
            List<Record> productList = Db
                    .find("select * from aliexpress_sales_order_product where order_id=?",
                            r.get("order_id"));
            r.set("product_list", productList);
        }

        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void importOrders() throws Exception {
        UserLogin user = LoginUserController.getLoginUser(null);
        long office_id = user.getLong("office_id");
        
        List<Record> accountList = Db.find("select * from aliexpress_seller_account where office_id=?", office_id);
        for (Record setting : accountList) {
            long appKey = setting.getLong("client_id");
            String appSecret = setting.getStr("secret");
            String access_token = setting.getStr("access_token");
            String refresh_token = setting.getStr("refresh_token");
            
            Date access_token_timeout = setting.getDate("access_token_timeout");
            Date now = new Date();
            if(now.after(access_token_timeout)){
                String host = "gw.api.alibaba.com";
                Map<String, String> token_params = new HashMap<String, String>();
                token_params.put("client_id", String.valueOf(appKey));
                token_params.put("client_secret", appSecret);
                token_params.put("refresh_token", refresh_token);
                String refreshTokenResult = AuthService.refreshToken(host, token_params);
                System.out.println("refreshTokenResult: "+refreshTokenResult);
                Map jsonObject = new Gson().fromJson(refreshTokenResult.toString(), Map.class);
               
                String accessToken = (String) jsonObject.get("access_token");
                Record setRec = Db.findFirst("select * from aliexpress_seller_account where office_id=? and client_id=?"
                        , office_id, appKey);
                if(setRec!=null){
                    setRec.set("access_token", accessToken);
                    Calendar cd = Calendar.getInstance();
                    cd.add(Calendar.HOUR, 10);//10小时后过期
                    setRec.set("access_token_timeout", cd.getTime());
                    Db.update("aliexpress_seller_account", setRec);
                }else{
                    throw new Exception("not found setting");
                }
            }

            List<Record> recList = ListOrdersApi.findOrderListQuery(appKey, appSecret, access_token, refresh_token);
        }
        
        renderText("OK");
    }
}
