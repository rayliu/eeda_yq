package controllers.oms.ebaySalesOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.sdk.ApiContext;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;

//@RequiresAuthentication

public class AllOrderControllerForMobile extends Controller {
    private ApiContext apiContext = null;
    private Log logger = Log.getLog(AllOrderControllerForMobile.class);

    public void allOrderList() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }else{
            sLimit = " LIMIT 0, 20";
        }
        String condition = DbUtils.buildConditions(getParaMap());
        
        String sql = "select * from ebay_order where 1=1 ";
        List<Record> orderList = Db.find(sql + condition
                + " order by created_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", 1);
        map.put("recordsTotal", 10);
        map.put("recordsFiltered", 10);
        map.put("data", orderList);
        renderJson(map);
    }
    
    public void ebayOrderList() {
//        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = 5;//user.getLong("office_id");

        String type = getPara("type");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }else{
            sLimit = " LIMIT 0, 20";
        }
        String sql = "select * from ebay_order where 1=1 ";

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by created_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }

    public void amazonOrderList() {
//      UserLogin user = LoginUserController.getLoginUser(this);
      long office_id = 5;//user.getLong("office_id");

      String type = getPara("type");

      String sLimit = "";
      String pageIndex = getPara("draw");
      if (getPara("start") != null && getPara("length") != null) {
          sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
      }else{
          sLimit = " LIMIT 0, 20";
      }
      String sql = "select * from amazon_sales_order where 1=1 ";

      String condition = DbUtils.buildConditions(getParaMap());

      String sqlTotal = "select count(1) total from (" + sql + condition
              + ") B";
      Record rec = Db.findFirst(sqlTotal);
      logger.debug("total records:" + rec.getLong("total"));

      List<Record> orderList = Db.find(sql + condition
              + " order by purchase_date desc " + sLimit);
      Map map = new HashMap();
      map.put("draw", pageIndex);
      map.put("recordsTotal", rec.getLong("total"));
      map.put("recordsFiltered", rec.getLong("total"));
      map.put("data", orderList);
      renderJson(map);
  }
}
