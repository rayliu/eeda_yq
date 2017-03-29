package controllers.oms.aliexpressSalesOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AliexpressSalesOrderController extends Controller {

    private Log logger = Log.getLog(AliexpressSalesOrderController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() {
        String type = getPara("type");
        setAttr("type", type);
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
        String sql = "select * from aliexpress_sales_order where office_id="+office_id;

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by modified_time desc " + sLimit);
        
        for (Record r : orderList) {
            List<Record> productList =Db.find("select * from aliexpress_sales_order_product where order_id=?", r.get("order_id"));
            r.set("product_list", productList);
        }
        
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void importOrders() throws IOException {
        List<Record> recList = ListOrdersApi.findOrderListQuery();
        
        renderJson(recList);
    }

   
}
