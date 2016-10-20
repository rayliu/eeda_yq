package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapChargeItem;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.RateContrast;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;

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
		
		String ids = getPara("itemId");//job_order_arap ids
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
		order.set("item_list", getItemList(ids,""));
		order.set("currencyList", getCurrencyList(ids,""));
		setAttr("order", order);
		
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
	
	public List<Record> getCurrencyList(String ids,String order_id){
    	String sql = "SELECT "
    			+ " (select rc.id from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
    			+ " ifnull((select rc.new_rate from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),exchange_rate) new_rate"
				+ " FROM job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
    	List<Record> re = Db.find(sql);
    	
    	return re;
	}
	
	
	public List<Record> getItemList(String ids,String order_id){
		String sql = null;
		if(StringUtils.isEmpty(order_id)){
			sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),joa.exchange_rate) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
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
	                + " GROUP BY joa.id";	
		}else{
			sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),joa.exchange_rate) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
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
	                + " left join arap_cost_item aci on aci.ref_order_id = joa.id"
					+ " left join arap_cost_order aco on aco.id = aci.cost_order_id "
					+ " where joa.id = aci.ref_order_id and aco.id =  '"+order_id+"'"
	                + " GROUP BY joa.id ";
		}
    	
    	List<Record> re = Db.find(sql);
    	return re;
    }
	
	
	public void createList() {
		String ids = getPara("itemIds");
		String order_id = getPara("order_id")==null?"":getPara("order_id");
		String sLimit = "";
		String pageIndex = getPara("draw");
		if (getPara("start") != null && getPara("length") != null) {
			sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
		}
		
		String sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
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
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * from(  "
         		+ " select joa.id,joa.type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
         		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
         		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
         		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
         		+ " cur.name currency_name,joli.truck_type "
 				+ " from job_order_arap joa "
 				+ " left join job_order jo on jo.id=joa.order_id "
 				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
 				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
 				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.customer_id "
 				+ " left join location l on l.id=jos.fnd "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
 				+ " where joa.order_type='cost' and joa.audit_flag='Y' and joa.bill_flag='N' and jo.office_id = "+office_id
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
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select aco.id,aco.order_no,aco.create_stamp,aco.status,aco.total_amount,c.pay_amount paid_amount,p.abbr sp_name "
				+ " from arap_cost_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id"
				+ " where aco.office_id = "+ office_id
				+ " order by aco.id desc"
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
        String ids = (String) dto.get("ids");
        
        ArapCostOrder aco = new ArapCostOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
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
	        aco.set("order_type", "应付对账单");
			aco.set("create_by", user.getLong("id"));
			aco.set("create_stamp", new Date());
			aco.set("office_id", office_id);
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
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("currency_list");
		for(Map<String, String> item :itemList){
			String new_rate = item.get("new_rate");
			String rate = item.get("rate");
			String order_type = item.get("order_type");
			String currency_id = item.get("currency_id");
			String rate_id = item.get("rate_id");
			String order_id = (String) dto.get("id");
			
			RateContrast rc = null;
			if(StringUtils.isEmpty(rate_id)){
				rc = new RateContrast();
				rc.set("order_id", id);
				rc.set("new_rate", new_rate);
				rc.set("rate", rate);
				rc.set("currency_id", currency_id);
				rc.set("order_type", order_type);
				rc.set("create_by", LoginUserController.getLoginUserId(this));
				rc.set("create_stamp", new Date());
				rc.save();
			}else{
				rc = RateContrast.dao.findById(rate_id);
				if(rc == null){
					rc = RateContrast.dao.findFirst("select * from rate_contrast where order_id = ? and currency_id = ?",order_id,currency_id);
				}
				rc.set("new_rate", new_rate);
				rc.set("update_by", LoginUserController.getLoginUserId(this));
				rc.set("update_stamp", new Date());
				rc.update();
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
		String id = getPara("id");//arap_cost_order id
		String sql = " select aco.*,p.abbr sp_name,u.c_name creator_name,u1.c_name confirm_by_name from arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record order = Db.findFirst(sql,id);
		
		String condition = "select ref_order_id from arap_cost_item where cost_order_id ="+id;
		order.set("currencylist", getCurrencyList(condition,id));
		order.set("item_list", getItemList("",id));
		
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
