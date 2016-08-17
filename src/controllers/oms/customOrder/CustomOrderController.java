package controllers.oms.customOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;
import models.eeda.oms.jobOrder.JobOrderShipment;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomOrderController extends Controller {

	private Logger logger = Logger.getLogger(CustomOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/oms/CustomOrder/CustomOrderList.html");
	}

	public void customOrderlist(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select jo.*, ifnull(u.c_name, u.user_name) creator_name, p.abbr customer_name, joc.custom_order_no  from job_order jo "
                + " left join party p on p.id = jo.customer_id"
                + " left join user_login u on u.id = jo.creator"
                + " left join job_order_custom joc on order_id = jo.id and joc.custom_type='china' "
                + " where jo.transport_type like '%custom%' ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }

	public void editCustomOrder(){
		
		setAttr("loginUser",LoginUserController.getLoginUserName(this));
		
        String id = getPara("id");
        String order_no = getPara("order_no");
        setAttr("order_no", order_no);
        setAttr("order_id", id);
        
        String sql = "select joc.* from job_order_custom joc where order_id = ? and custom_type = 'china' ";
        setAttr("custom",Db.findFirst(sql,id));
        
        render("/oms/CustomOrder/CustomOrderEdit.html");
    }

	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
   		String id = (String) dto.get("id");
   		
	   	//报关
		List<Map<String, String>> chinaCustom = (ArrayList<Map<String, String>>)dto.get("chinaCustom");
		DbUtils.handleList(chinaCustom, id, JobOrderCustom.class, "order_id");
		renderJson("{\"result\":true}");
	}
	
}
