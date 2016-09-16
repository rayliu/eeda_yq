package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderShipment;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostCheckOrderController extends Controller {
	private Log logger = Log.getLog(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_LIST })
	public void index() {
		render("/eeda/arap/CostCheckOrder/CostCheckOrder.html");
	}
	
	public void create(){
		
		String ids = getPara("itemIds");//jobOrder arap ids
		String totalAmount = getPara("totalAmount");
		
		String strAry[] = ids.split(",");
		String id = strAry[0];
		String sql = " select joa.sp_id,p.company_name sp_name from job_order_arap joa "
				   + " left join party p on p.id = joa.sp_id "
				   + "  where joa.id = ? ";
		Record spRec = Db.findFirst(sql,id);
		Record order = new Record();
		order.set("sp_id", spRec.get("sp_id"));
		order.set("sp_name", spRec.get("sp_name"));
		order.set("total_amount",totalAmount);
		order.set("ids",ids);
		order.set("creator_name", LoginUserController.getLoginUserName(this));
		
		
		String detailSql = "select * from(  "
                + " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
                + " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
                + " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
                + " cur.name currency_name "
                + " from job_order_arap joa "
                + " left join job_order jo on jo.id=joa.order_id "
                + " left join job_order_shipment jos on jos.order_id=joa.order_id "
                + " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
                + " left join job_order_air_item joai on joai.order_id=joa.order_id "
                + " left join party p on p.id=joa.sp_id "
                + " left join party p1 on p1.id=jo.customer_id "
                + " left join location l on l.id=jos.fnd "
                + " left join currency cur on cur.id=joa.currency_id "
                + " where joa.order_type='cost' and joa.audit_flag='Y' and joa.bill_flag='N' and joa.id in("+ids+") "
                + " GROUP BY joa.id "
                + " ) B ";
		List<Record> jobOrderRecs = Db.find(detailSql);
		order.set("item_list", jobOrderRecs);
		setAttr("order", order);
		
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
	public void createList() {
		String ids = getPara("itemIds");
		String sLimit = "";
		String pageIndex = getPara("draw");
		if (getPara("start") != null && getPara("length") != null) {
			sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
		}
		String sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
				+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
				+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
				+ " cur.name currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
				+ " left join party p on p.id=joa.sp_id "
				+ " left join party p1 on p1.id=jo.customer_id "
				+ " left join location l on l.id=jos.fnd "
				+ " left join currency cur on cur.id=joa.currency_id "
				+ " where joa.id in ( "+ids+" ) "
				+ " GROUP BY joa.id ";
				
		
		String sqlTotal = "select count(1) total from ("+sql+") C";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		List<Record> orderList = Db.find(sql + " order by create_stamp desc " +sLimit);
		Map map = new HashMap();
		map.put("draw", pageIndex);
		map.put("recordsTotal", rec.getLong("total"));
		map.put("recordsFiltered", rec.getLong("total"));
		map.put("data", orderList);
		renderJson(map); 
		
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * from(  "
        		+ " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
        		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
        		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
        		+ " cur.name currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
				+ " left join party p on p.id=joa.sp_id "
				+ " left join party p1 on p1.id=jo.customer_id "
				+ " left join location l on l.id=jos.fnd "
				+ " left join currency cur on cur.id=joa.currency_id "
				+ " where joa.order_type='cost' and joa.audit_flag='Y' and joa.bill_flag='N' "
				+ " GROUP BY joa.id "
				+ " ) B where 1=1 ";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	public void orderList() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(  "
        		+ " select aco.id,aco.order_no,aco.create_stamp,aco.status,aco.total_amount,c.pay_amount paid_amount,p.abbr sp_name "
				+ " from arap_cost_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id order by aco.id desc"
				+ " ) B where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
	}
	
	
	@Before(Tx.class)
	public void save() throws Exception{
		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String sp_id = (String) dto.get("sp_id");
        String ids = (String) dto.get("ids");
        
        
        ArapCostOrder aco = new ArapCostOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			aco = ArapCostOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, aco);
   			aco.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, aco);
	   		String orderPrefix = OrderNoGenerator.getNextOrderNo("YFDZ");
	        aco.set("order_no", orderPrefix);
	        aco.set("order_type", "对账单");
			aco.set("create_by", user.getLong("id"));
			aco.set("create_stamp", new Date());
			aco.save();
			id = aco.getLong("id").toString();
			
			
			//设置已创建过对账单flag
			String idAttr[] = ids.split(",");
			for(int i=0 ; i<idAttr.length ; i++){
				JobOrderArap joa = JobOrderArap.dao.findById(idAttr[i]);
				joa.set("bill_flag", "Y");
				joa.update();
				
				ArapCostItem arapCostItem = new ArapCostItem();
				arapCostItem.set("ref_order_id", idAttr[i]);
				arapCostItem.set("cost_order_id", id);
				arapCostItem.save();
			}
   		}
   		
   		String sql = " select aco.*, p.abbr sp_name, u.c_name creator_name from arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by"
   				+ " where aco.id = ? ";
   		
   		Record r = Db.findFirst(sql,id);
   		renderJson(r);
	}
	
	public void edit(){
		String id = getPara("id");
		String sql = " select aco.*,p.abbr sp_name,u.c_name creator_name,u1.c_name confirm_by_name from arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record order = Db.findFirst(sql,id);
		
		String detailSql = " select joa.id,joa.type,joa.sp_id,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.total_costRMB, "
				+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
				+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
				+ " left join party p on p.id=joa.sp_id "
				+ " left join party p1 on p1.id=jo.customer_id "
				+ " left join location l on l.id=jos.fnd "
				+ " left join arap_cost_item aci on aci.ref_order_id = joa.id"
				+ " left join arap_cost_order aco on aco.id = aci.cost_order_id and aco.id"
				+ " where joa.id = aci.ref_order_id and aco.id = ? ";
		List<Record> itemList = Db.find(detailSql,id);
		order.set("item_list", itemList);
		
		setAttr("order", order);
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
	public void confirm(){
		String id = getPara("id");
		ArapCostOrder aco = ArapCostOrder.dao.findById(id);
		aco.set("status","已确认");
		aco.set("confirm_stamp", new Date());
		aco.set("confirm_by", LoginUserController.getLoginUserId(this));
		aco.update();
		Record r = aco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
		renderJson(r);
	}
	
}
