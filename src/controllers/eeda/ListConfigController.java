package controllers.eeda;import java.util.HashMap;import java.util.List;import java.util.Map;import models.UserLogin;import com.google.gson.Gson;import com.jfinal.core.Controller;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import controllers.profile.LoginUserController;public class ListConfigController extends Controller {    private static Log logger = Log.getLog(ListConfigController.class);      public void list() {        UserLogin user = LoginUserController.getLoginUser(this);        long user_id = user.getLong("id");                String module_path = getPara("module_path");        String sLimit = "";        String pageIndex = getPara("draw");        if (getPara("start") != null && getPara("length") != null) {            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");        }                String sql="select * from eeda_order_cols_config "                + "where user_id=? and module_path=? order by seq";        logger.debug("user_id="+user_id+", module_id="+module_path);        List<Record> orderList = Db.find(sql, user_id, module_path);        if(orderList.size()==0){            orderList = Db.find(sql, 0, module_path);        }                String sqlTotal = "select count(1) total from ("+sql+") B";        Record rec = Db.findFirst(sqlTotal, user_id, module_path);                Map orderListMap = new HashMap();        orderListMap.put("draw", pageIndex);        orderListMap.put("recordsTotal", rec.getLong("total"));        orderListMap.put("recordsFiltered", rec.getLong("total"));        orderListMap.put("data", orderList);        renderJson(orderListMap);    }        public static List<Record> getConfig(long user_id, String module_path){        String sql="select * from eeda_order_cols_config "                + "where user_id=? and module_path=? order by seq";        logger.debug("user_id="+user_id+", module_path="+module_path);        List<Record> orderList = Db.find(sql, user_id, module_path);        if(orderList.size()==0){            orderList = Db.find(sql, 0, module_path);        }        logger.debug("orderList.size():"+orderList.size());        return orderList;    }    public void save(){        UserLogin user = LoginUserController.getLoginUser(this);        long user_id = user.getLong("id");                String jsonStr = getPara("data");        logger.debug("jsonStr="+jsonStr);        Gson gson = new Gson();          Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);        List<Map> fieldList = (List<Map>)dto.get("arr");        for (Map map : fieldList) {            Record rec = new Record();                        rec.set("COL_DISPLAY_NAME", map.get("COL_DISPLAY_NAME"));            rec.set("COL_FIELD", map.get("COL_FIELD"));            rec.set("IS_SHOW", map.get("IS_SHOW"));            rec.set("MODULE_PATH", map.get("MODULE_PATH"));            rec.set("MODULE_TABLE_ID", map.get("MODULE_TABLE_ID"));            rec.set("SEQ", map.get("SEQ"));            if((Double)map.get("USER_ID") == 0){                rec.set("USER_ID", user_id);                Db.save("eeda_order_cols_config", rec);            }else{                rec.set("ID", map.get("ID"));                Db.update("eeda_order_cols_config", rec);            }                    }                renderText("OK");    }}