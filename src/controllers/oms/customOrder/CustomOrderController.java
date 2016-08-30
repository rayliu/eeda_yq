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

	public void create() {
		setAttr("loginUser",LoginUserController.getLoginUserName(this));
		render("/oms/CustomOrder/CustomOrderChina.html");
    }
	
	public void customOrderlist(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select jo.order_no,jo.create_stamp, ifnull(u.c_name, u.user_name) creator_name, p.abbr customer_name,joc.id, joc.custom_order_no,joc.custom_type,joc.status"
        		+ " from job_order jo "
                + " left join party p on p.id = jo.customer_id"
                + " left join user_login u on u.id = jo.creator"
                + " left join job_order_custom joc on joc.order_id = jo.id"
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

	//有工作单号的报关
	public void edit(){
        String id = getPara("id");
        String order_no = getPara("order_no");
        String sql = "select joc.* from job_order_custom joc"
        		   + " where id=? ";
        setAttr("order_no",order_no);
        setAttr("custom",Db.findFirst(sql,id));
        setAttr("loginUser",LoginUserController.getLoginUserName(this));
        render("/oms/CustomOrder/CustomOrderEdit.html");
    }
	
	//无工作单号的报关
	public void editOfCreate(){
		String id = getPara("id");
		String sql = "select joc.* from job_order_custom joc"
				+ " where id=? ";
		setAttr("custom",Db.findFirst(sql,id));
		setAttr("loginUser",LoginUserController.getLoginUserName(this));
		render("/oms/CustomOrder/CustomOrderChina.html");
	}

	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
   		String id = (String) dto.get("id");
   		JobOrderCustom joc = new JobOrderCustom();
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			joc = JobOrderCustom.dao.findById(id);
   			DbUtils.setModelValues(dto, joc);
   			joc.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, joc);
//   			joc.set("custom_order_no", OrderNoGenerator.getNextOrderNoForYQ("BG"));
   			joc.save();
   			id = joc.getLong("id").toString();
   		}
   		Record r = joc.toRecord();
   		r.set("result", true);
		renderJson(r);
	}
	
}
