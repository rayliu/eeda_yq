package controllers.tms.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.RateContrast;
import models.UserLogin;
import models.eeda.profile.Currency;
import models.eeda.tms.TransArapChargeItem;
import models.eeda.tms.TransArapChargeOrder;
import models.eeda.tms.TransArapChargeReceiveItem;
import models.eeda.tms.TransJobOrder;
import models.eeda.tms.TransJobOrderArap;

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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransChargeCheckOrderController extends Controller {

	private Logger logger = Logger.getLogger(TransChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/transChargeCheckOrder");
        setAttr("listConfigList", configList);	
		render("/tms/arap/transChargeCheckOrder/transChargeCheckOrderList.html");
	}
	
	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        TransArapChargeOrder order = new TransArapChargeOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = TransArapChargeOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSDZ", user.getLong("office_id")));
   			order.set("order_type", "应收对账单");
   			order.set("create_by", user.getLong("id"));
   			DateFormat dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   			order.set("create_stamp", dateTimeformat.format(new Date()));   			
   			order.set("office_id", office_id);
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}

   		TransArapChargeItem aci = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String action = item.get("action");
			String itemId = item.get("id");
			if("CREATE".equals(action)){
				aci = new TransArapChargeItem();
				aci.set("ref_order_type", "工作单");
				aci.set("ref_order_id", itemId);
				aci.set("charge_order_id", id);
				aci.save();
                TransJobOrderArap jobOrderArap = TransJobOrderArap.dao.findById(itemId);
                jobOrderArap.set("bill_flag", "Y");
                String hedge_order_type = jobOrderArap.getStr("order_type");
                if("cost".equals(hedge_order_type)){
                	jobOrderArap.set("hedge_flag", "Y");
                }
                jobOrderArap.update();
			}
		}
		
		
		List<Map<String, String>> currencyList = (ArrayList<Map<String, String>>)dto.get("currency_list");
		for(Map<String, String> item :currencyList){
			String new_rate = item.get("new_rate");
			String rate = item.get("rate");
			String order_type = item.get("order_type");
			String currency_id = item.get("currency_id");
			String rate_id = item.get("rate_id");
			String order_id = (String) dto.get("id");
			
			RateContrast rc = null;
			if(StringUtils.isEmpty(rate_id) && StringUtils.isEmpty(order_id)){
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
		
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}


    public void list() {
    	String checked = getPara("checked");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	 sql = "select * from(  "
        			+ " select joa.order_type sql_type,joa.remark, joa.id,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
              		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.land_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no,jo.type,jo.so_no,jo.container_no,jo.cabinet_type, "
              		+ " p.abbr sp_name,p1.abbr customer_name, "
              		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
              		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
              		+ " ) after_total,"
              		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
              		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
              		+ " after_rate_total,ifnull(f.name,f.name_eng) fee_name,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount,joli.cabinet_date,convert(SUBSTR(jo.charge_time,1,10),char) charge_time,"
              		+ " CONCAT(IFNULL(CONCAT(dock0.dock_name, '-'),''),"
    				+ " IFNULL(CONCAT(GROUP_CONCAT(dock2.dock_name SEPARATOR '-'),'-'),''),"
    				+ " IFNULL(CONCAT(GROUP_CONCAT(dock3.dock_name SEPARATOR '-'),'-'),''),"
    				+ " IFNULL(dock1.dock_name, '')) combine_wharf"
      				+ " from trans_job_order jo "
      				+ " left join trans_job_order_arap joa on jo.id=joa.order_id "
      				+ " left join party p on p.id=joa.sp_id "
      				+ " left join party p1 on p1.id=jo.customer_id "
      				+ " left join currency cur on cur.id=joa.currency_id "
      				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
      				+ " left join trans_job_order_land_item joli on joli.order_id=joa.order_id "
      				+ " left join fin_item f on f.id = joa.charge_id"
      				+ " LEFT JOIN dockinfo dock0 ON dock0.id = tjo.take_wharf "
    				+ " LEFT JOIN dockinfo dock1 ON dock1.id = tjo.back_wharf "
    				+ " LEFT JOIN dockinfo dock2 ON dock2.id = tjol.loading_wharf1 "
    				+ " LEFT JOIN dockinfo dock3 ON dock3.id = tjol.loading_wharf2"
      				+ " where joa.audit_flag='Y' and joa.bill_flag='N'  and jo.office_id = "+office_id
      				 + " and jo.delete_flag = 'N'"
     				+ " GROUP BY joa.id "
    				+ " ) B where 1=1 ";
        	}else{
        		 sql = "select * from(  "
                 		+ " select ifnull(f.name,f.name_eng) fee_name,joa.remark, joa.id,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
                 		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.land_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no,jo.type,jo.so_no,jo.container_no,jo.cabinet_type, "
                 		+ " p.abbr sp_name,p1.abbr customer_name, "
                 		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
                 		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
                 		+ " ) after_total,"
                 		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
                 		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
                 		+ " after_rate_total,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount,joli.cabinet_date,convert(SUBSTR(jo.charge_time,1,10),char) charge_time,"
                 		+ " CONCAT(IFNULL(CONCAT(dock0.dock_name, '-'),''),"
        				+ " IFNULL(CONCAT(GROUP_CONCAT(dock2.dock_name SEPARATOR '-'),'-'),''),"
        				+ " IFNULL(CONCAT(GROUP_CONCAT(dock3.dock_name SEPARATOR '-'),'-'),''),"
        				+ " IFNULL(dock1.dock_name, '')) combine_wharf"
         				+ " from trans_job_order jo "
         				+ " left join trans_job_order_arap joa on jo.id=joa.order_id "
         				+ " left join party p on p.id=joa.sp_id "
         				+ " left join party p1 on p1.id=jo.customer_id "
         				+ " left join currency cur on cur.id=joa.currency_id "
         				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
         				+ " left join trans_job_order_land_item joli on joli.order_id=joa.order_id "
         				+ " left join fin_item f on f.id = joa.charge_id"
         				+ " LEFT JOIN dockinfo dock0 ON dock0.id = jo.take_wharf "
        				+ " LEFT JOIN dockinfo dock1 ON dock1.id = jo.back_wharf "
        				+ " LEFT JOIN dockinfo dock2 ON dock2.id = joli.loading_wharf1 "
        				+ " LEFT JOIN dockinfo dock3 ON dock3.id = joli.loading_wharf2"
         				+ " where joa.order_type='charge' and joa.audit_flag='Y' and joa.bill_flag='N'  and jo.office_id = "+office_id
         				 + " and jo.delete_flag = 'N'"
         				+ " GROUP BY joa.id "
         				+ " ) B where 1=1 ";
        			}
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +" order by charge_time");
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
	public void list2() {
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

		List<Record> BillingOrders = null;

		Map BillingOrderListMap = new HashMap();
		BillingOrderListMap.put("draw", pageIndex);
		BillingOrderListMap.put("data", BillingOrders);

		renderJson(BillingOrderListMap);
	}
    
    
    public void checkedList(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        			
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select aco.*, "
        		+" IFNULL(aco.cny, 0) total_amount_cny, "
			    +" (SELECT IFNULL(SUM(t.receive_cny), 0) FROM trans_arap_charge_receive_item t WHERE t. charge_order_id=aco.id) total_receive_cny, "
			    +" (aco.cny-(SELECT IFNULL(SUM(t.receive_cny), 0)  FROM trans_arap_charge_receive_item t WHERE t.charge_order_id=aco.id)) total_RESIDUAL_CNY, "
			    + " p.abbr sp_name,CAST(CONCAT(begin_time,'到',end_time) AS CHAR) service_stamp,u.c_name confirm_name,IFNULL(aco.audit_status,aco.status) toStatus"
				+ " from trans_arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " LEFT JOIN user_login u ON u.id = aco.confirm_by "
				+ " where aco.office_id = "+office_id+" order by aco.create_stamp DESC "
				+ " ) B where 1=1 ";
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap);
    	
    }
    
    
    public List<Record> getItemList(String ids,String order_id,String code){
    	String sql = null;
    	String currenry_code="";
    	if(StringUtils.isNotEmpty(code)){
    		 currenry_code=" and cur. NAME="+"'"+code+"'";
    	}
		if(StringUtils.isEmpty(order_id)){
			sql = " select joa.id, joa.order_type,jo.order_no,jo.create_stamp,jo.id job_order_id,jo.customer_id,jo.volume vgm,"
    			+ "IFNULL(cur1.name,cur.name) exchange_currency_name,"
    			+ "IFNULL(joa.exchange_currency_rate,1) exchange_currency_rate,IFNULL(joa.exchange_total_amount,joa.total_amount) exchange_total_amount,"
    			+ "joa.total_amount total_amount,joa.exchange_rate exchange_rate,joa.remark, " 
    			+ " jo.net_weight gross_weight,jo.so_no,jo.container_no,"
    			+ " cur.name currency_name,"
    			+ " jo.ref_no ref_no,"
    			+ " p1.company_name sp_name,jols.truck_type truck_type,jols.cabinet_date,"
    			+ " fi.name fin_name,"
    			+ " CONCAT(IFNULL(CONCAT(dock0.dock_name, '-'),''),"
				+ " IFNULL(CONCAT(GROUP_CONCAT(dock2.dock_name SEPARATOR '-'),'-'),''),"
				+ " IFNULL(CONCAT(GROUP_CONCAT(dock3.dock_name SEPARATOR '-'),'-'),''),"
				+ " IFNULL(dock1.dock_name, '')) combine_wharf"
    			+ " from trans_job_order_arap joa"
    			+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
    			+ " LEFT JOIN currency cur1 on cur1.id = joa.exchange_currency_id"
    			+ "	left join trans_job_order jo on jo.id=joa.order_id "
				+ " left join fin_item fi on joa.charge_id = fi.id "
    			+ " left join trans_job_order_land_item  jols on jols.order_id=joa.order_id "
    			+ "	left join party p1 on p1.id=joa.sp_id "
    			+ " LEFT JOIN dockinfo dock0 ON dock0.id = jo.take_wharf "
				+ " LEFT JOIN dockinfo dock1 ON dock1.id = jo.back_wharf "
				+ " LEFT JOIN dockinfo dock2 ON dock2.id = jols.loading_wharf1 "
				+ " LEFT JOIN dockinfo dock3 ON dock3.id = jols.loading_wharf2"
    			+ "	where joa.audit_flag='Y' "
    			+ " and joa.id in("+ids+")"
    			 + " and jo.delete_flag = 'N'"
 				+ " GROUP BY joa.id";
			}else{				
			sql = " select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,joa.remark,"
					+" aco.order_no check_order_no, jo.id job_order_id, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
					+ " jo.ref_no ref_no,jo.so_no,jo.container_no,"
						+" p.abbr sp_name,p1.abbr customer_name, "
						+" ifnull((select rc.new_rate from rate_contrast rc"
						    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
						    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
						    +"  ifnull((select rc.new_rate from rate_contrast rc"
						    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
						+ " fi.name fin_name,"
						+ " cur.name currency_name,"
						+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
						+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
						+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount ,tjoli.cabinet_date,"
						+ " CONCAT(IFNULL(CONCAT(dock0.dock_name, '-'),''),"
						+ " IFNULL(CONCAT(GROUP_CONCAT(dock2.dock_name SEPARATOR '-'),'-'),''),"
						+ " IFNULL(CONCAT(GROUP_CONCAT(dock3.dock_name SEPARATOR '-'),'-'),''),"
						+ " IFNULL(dock1.dock_name, '')) combine_wharf"
						+" from trans_job_order jo"
						+" left join trans_job_order_arap joa on jo.id=joa.order_id"
						+" left join fin_item fi on joa.charge_id = fi.id"
						+" left join party p on p.id=joa.sp_id"
						+" left join party p1 on p1.id=jo.customer_id"
						+" left join currency cur on cur.id=joa.currency_id"
						+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
						+" left join trans_arap_charge_item aci on aci.ref_order_id = joa.id"
						+ " left join trans_job_order_land_item  jols on jols.order_id=jo.id "
						+ " LEFT JOIN dockinfo dock0 ON dock0.id = jo.take_wharf "
	    				+ " LEFT JOIN dockinfo dock1 ON dock1.id = jo.back_wharf "
	    				+ " LEFT JOIN dockinfo dock2 ON dock2.id = jols.loading_wharf1 "
	    				+ " LEFT JOIN dockinfo dock3 ON dock3.id = jols.loading_wharf2"
					 +" left join trans_arap_charge_order aco on aco.id = aci.charge_order_id"
					 + " LEFT JOIN trans_job_order_land_item tjoli on tjoli.order_id=jo.id"
					 +" where joa.id = aci.ref_order_id and aco.id = ("+order_id+")" +currenry_code
					 + " and jo.delete_flag = 'N'"
						+" GROUP BY joa.id"
						+" ORDER BY aco.order_no, jo.order_no";
				
				
			}	
    	List<Record> re = Db.find(sql);
    	return re;
    }
    
    public List<Record> getChargeItemList(String order_ids,String bill_flag,String code,String exchange_currency,String fin_name){
    	String sql = null;
    	String currency_code="";
    	String query_exchange_currency="";
    	String query_fin_name="";
		if(StringUtils.isNotEmpty(code)){
			currency_code=" and cur. NAME="+"'"+code+"'";
		}
		if(StringUtils.isNotEmpty(exchange_currency)){
			String sql2="select id from currency where currency.name='"+exchange_currency+"'";
			List<Record> re=Db.find(sql2);
			query_exchange_currency=" and joa. exchange_currency_id="+re.get(0).get("id");
		}
		if(StringUtils.isNotEmpty(fin_name)){
			query_fin_name=" and fi.id="+fin_name;
		}
			if("create".equals(bill_flag)){
				sql = " select joa.id,joa.create_flag,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.remark,joa.currency_total_amount,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name, "
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+" GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from trans_job_order jo"
							+" left join trans_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							+" left join charge_application_order_rel caol on caol.job_order_arap_id  = joa.id"
							+" left join arap_charge_application_order acao on caol.application_order_id = acao.id"
							 +" left join trans_arap_charge_order aco on aco.id=caol.charge_order_id"
						  +" where acao.id="+order_ids+query_fin_name
						  + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
				
			}else{
				sql = "select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,joa.remark,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from trans_job_order jo"
							+" left join trans_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							+" left join trans_arap_charge_item aci on aci.ref_order_id = joa.id"
						 +" left join trans_arap_charge_order aco on aco.id = aci.charge_order_id"
						 +" where joa.id = aci.ref_order_id and joa.create_flag='N' and aco.id in ("+order_ids+")"
							+currency_code
							+query_exchange_currency+query_fin_name
							 + " and jo.delete_flag = 'N'"
								+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
			}		
			

    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    
  //报关例外，获取每次收款的记录
    public List<Record> getReceiveItemList(String order_id){
    	String sql = null;
   		 sql = "  SELECT caci.*,c.`name` currency_name ,ul.c_name receive_name"
   				+" from trans_arap_charge_order caco  "
   				+" left join trans_arap_charge_receive_item caci on caci.charge_order_id = caco.id "
   				+" LEFT JOIN currency c on c.id=caci.currency_id "
   				+ "  LEFT JOIN user_login ul ON ul.id = caci.confirm_by"
   				+" where caci.charge_order_id ="+order_id+" order by caci.id desc";
    	List<Record> re = Db.find(sql);
    	return re;
    }
    
    
    public List<Record> getCurrencyList(String ids,String order_id){
    	String sql = "SELECT "
    			+ " (select rc.id from rate_contrast rc "
    	    	+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
    			+ " ifnull((select rc.new_rate from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1)) new_rate"
				+ " FROM trans_job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    
    @Before(EedaMenuInterceptor.class)
	public void create(){
		String ids = getPara("idsArray");//job_order_arap ids
		String total_amount = getPara("totalAmount");
		String cny_totalAmount = getPara("cny_totalAmount");
		String usd_totalAmount = getPara("usd_totalAmount");
		String hkd_totalAmount = getPara("hkd_totalAmount");
		String jpy_totalAmount = getPara("jpy_totalAmount");
		
		String sql = "SELECT cur.name currency_name ,joa.exchange_rate ,p.phone,p.abbr company_abbr,p.contact_person,p.address,p.company_name,joa.sp_id,joa.order_id"
				+ " FROM trans_job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " left join party p on p.id = joa.sp_id "
				+ " WHERE joa.id in("+ ids +")"
				+ " group by joa.order_id";
		Record rec =Db.findFirst(sql);
		rec.set("total_amount", total_amount);
		//对账
		rec.set("jpy_duizhang", jpy_totalAmount);
		rec.set("cny_duizhang", cny_totalAmount);
		rec.set("usd_duizhang",usd_totalAmount);
		rec.set("hkd_duizhang", hkd_totalAmount);
		
		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
//		rec.set("user", LoginUserController.getLoginUserName(this));
		rec.set("itemList", getItemList(ids,"",""));
//		rec.set("currencyList", getCurrencyList(ids,""));
		rec.set("company_abbr", rec.get("company_abbr"));
		setAttr("order",rec);
		UserLogin u3=LoginUserController.getLoginUser(this);
		setAttr("user",u3);
		render("/tms/arap/transChargeCheckOrder/transChargeCheckOrderEdit.html");
	}
	
	
    @Before(EedaMenuInterceptor.class)
    public void edit(){
		String id = getPara("id");//trans_arap_charge_order id
	    UserLogin user1 = LoginUserController.getLoginUser(this);
	    long office_id=user1.getLong("office_id");
	    //判断与登陆用户的office_id是否一致
	    if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("trans_arap_charge_order", Long.valueOf(id), office_id)){
	    	renderError(403);// no permission
	        return;
	    }
		String condition = "select ref_order_id from trans_arap_charge_item where charge_order_id ="+id;
		
		String sql = " select aco.*,p.company_name,p.contact_person,p.id company_id,p.abbr company_abbr,p.phone,p.address,u.c_name creator_name,u1.c_name confirm_by_name from trans_arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record rec =Db.findFirst(sql,id);

		UserLogin u3=LoginUserController.getLoginUser(this);
		rec.set("user", u3);
		String sqlString="SELECT  (aco.cny-IFNULL(SUM(tacri.receive_cny),0))residual_cny FROM trans_arap_charge_order aco  "
				 +" LEFT JOIN trans_arap_charge_receive_item tacri on aco.id=tacri.charge_order_id "
				 +" WHERE aco.id ="+id+" ORDER BY tacri.id DESC ";
		Record rec2 = Db.findFirst(sqlString);
		
		Record rec3 = Db.findFirst("SELECT tacri.id,tacri.deposit_bank_input,tacri.receive_time,fa.account_name,fa.account_no from trans_arap_charge_receive_item tacri "
				+ " LEFT JOIN fin_account fa on fa.id=tacri.deposit_bank where create_stamp=(SELECT max(create_stamp)FROM trans_arap_charge_receive_item where charge_order_id = ?)",id);
		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("itemList", getItemList(condition,id,""));
//		rec.set("currencyList", getCurrencyList(condition,id));
		rec.set("company_id", rec.get("company_id"));
		rec.set("company_abbr", rec.get("company_abbr"));
		rec.set("receive_itemList", getReceiveItemList(id));
		if(rec3!=null){
			rec.set("deposit_bank_input", rec3.get("deposit_bank_input"));
			rec.set("receive_time", rec3.get("receive_time"));
			rec.set("account_name", rec3.get("account_name"));
			rec.set("account_no", rec3.get("account_no"));
		}
		
		if(rec2!=null){
			rec.set("residual_cny", rec2.get("residual_cny"));
		}
		setAttr("order",rec);
		setAttr("user",u3);
		render("/tms/arap/transChargeCheckOrder/transChargeCheckOrderEdit.html");
	}

    
    @Before(Tx.class)
	public void exchange_currency(){
	    String chargeOrderId = getPara("charge_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update trans_job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		renderJson(exchangeTotalMap);
	}
    
    private Map<String, Double> updateExchangeTotal(String chargeOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  trans_job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select aci.ref_order_id from trans_arap_charge_item aci where aci.charge_order_id="+chargeOrderId+")";
		
		Map<String, Double> exchangeTotalMap = new HashMap<String, Double>();
		exchangeTotalMap.put("CNY", 0d);
		exchangeTotalMap.put("USD", 0d);
		exchangeTotalMap.put("HKD", 0d);
		exchangeTotalMap.put("JPY", 0d);
		
		List<Record> resultList= Db.find(sql);
		for (Record rec : resultList) {
            String name = rec.get("exchange_currency_name");
            String type = rec.get("order_type");
            Double exchange_amount = exchangeTotalMap.get(name);
            if(exchangeTotalMap.get(name)==null){
                if("charge".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=exchange_amount);
                }else{
                    exchangeTotalMap.put(name, 0-rec.getDouble("exchange_total_amount"));
                }
            }else{
                if("charge".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=rec.getDouble("exchange_total_amount"));
                }else{
                    exchangeTotalMap.put(name, exchange_amount-=rec.getDouble("exchange_total_amount"));
                }        
            }
        }
		
		Record order = Db.findById("trans_arap_charge_order", chargeOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("trans_arap_charge_order", order);
		return exchangeTotalMap;
    }
    
    
    
	//异步刷新字表
    public void tableList(){
    	String order_ids = getPara("order_ids");
    	String order_id = getPara("order_id");
    	String appliction_id = getPara("appApplication_id");
    	String bill_flag = getPara("bill_flag");
    	String currency_code=getPara("query_currency");
    	//查询结算币制
    	String  exchange_currency=getPara("query_exchange_currency");
    	String  fin_name=getPara("query_fin_name");
    	List<Record> list = null;
    	String condition = "select ref_order_id from trans_arap_charge_item where charge_order_id in ("+order_ids+") ";
    	
    	if("N".equals(order_id)){//应收申请单
    		if(StringUtils.isNotEmpty(appliction_id)){
    			list = getChargeItemList(appliction_id,bill_flag,currency_code,exchange_currency,fin_name);
        	}else{
	    		if("".equals(order_ids)){
	    			order_ids=null;
	    				}
	    		list = getChargeItemList(order_ids,"",currency_code,exchange_currency,fin_name);
	    		}
    	}else{//应收对账单
    		    list = getItemList(condition,order_id,currency_code);
    	}
    	String  type=getPara("table_type");
    	if(order_id!=""&&"receive".equals(type)){
    		list=getReceiveItemList(order_id);
    		setAttr("receive_itemList",list);
    	}

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }

    @Before(Tx.class)
    public void confirm(){
		String id = getPara("id");
		String action = getPara("action");
		TransArapChargeOrder aco = TransArapChargeOrder.dao.findById(id);
		if(action==null){
			action="";
		}
		if(action.equals("cancelConfirm")){
			//
			aco.set("status","取消确认");
			aco.update();
			
			//
			String sql="UPDATE trans_job_order_arap joa set billConfirm_flag='N' "
						+"where joa.id in (select aci.ref_order_id FROM trans_arap_charge_item aci where charge_order_id="+id+" )";
			Db.update(sql);
			
			//保存这次取消确认记录进status_audit表
			Record re = new Record(); 
			re.set("order_type", "transChargeCheckOrder");
			re.set("order_id", id);
			re.set("user_id", LoginUserController.getLoginUser(this).getLong("office_id"));
			re.set("action_time",new Date());
			re.set("action",action);
			Db.save("status_audit", re);
			renderJson(re);
		}else{
			
			aco.set("status","已确认");
			aco.set("confirm_stamp", new Date());
			aco.set("confirm_by", LoginUserController.getLoginUserId(this));
			aco.update();
			
			//设置y，已生成对账单o
			String itemList=aco.get("ref_order_id");
			String sql="UPDATE trans_job_order_arap joa set billConfirm_flag='Y' "
						+"where joa.id in (select aci.ref_order_id FROM trans_arap_charge_item aci where charge_order_id="+id+" )";
			Db.update(sql);
			
			Record r = aco.toRecord();
			r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
			renderJson(r);
		}
		
	}
    
    public void insertChargeItem(){
    	String itemList= getPara("charge_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String chargeOrderId=getPara("order_id");
    	TransArapChargeItem aci = null;
    	
    	if(chargeOrderId != null){
    		for(String itemId:itemArray){
    			aci = new TransArapChargeItem();
	    		 TransJobOrderArap jobOrderArap = TransJobOrderArap.dao.findById(itemId);

	             jobOrderArap.set("bill_flag", "Y");
	             String hedge_order_type = jobOrderArap.getStr("order_type");
	             if("cost".equals(hedge_order_type)){
	                	jobOrderArap.set("hedge_flag", "Y");
	               }
	             jobOrderArap.update();
				aci.set("ref_order_id", itemId);
				aci.set("charge_order_id", chargeOrderId);
				aci.save();
//        	String sql="INSERT into trans_arap_charge_item (ref_order_id,charge_order_id) "
//        				+ "VALUES ("+itemId+","+order_id+")";
    		}
    		
    	}
    	//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		exchangeTotalMap.put("chargeOrderId", Double.parseDouble(chargeOrderId));
		
    	renderJson(exchangeTotalMap);

    }
    
    public void deleteChargeItem(){
    	String chargeOrderId=getPara("order_id");
    	String itemid=getPara("charge_itemid");
    	if(itemid !=null&& chargeOrderId!=null){
    		TransJobOrderArap jobOrderArap = TransJobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("bill_flag", "N");
             jobOrderArap.update();
//             String sql="delete from  where ref_order_id="+itemid+"and charge_order_id="+chargeOrderId;
             Db.deleteById("trans_arap_charge_item","ref_order_id,charge_order_id",itemid,chargeOrderId);
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
    			exchangeTotalMap.put("chargeOrderId", Double.parseDouble(chargeOrderId));
    	    	renderJson(exchangeTotalMap);
    }  
    
    public void downloadList(){
    	String order_id = getPara("order_id");
    	String company_name = getPara("company_name");
//        UserLogin user = LoginUserController.getLoginUser(this);
        String exportSql =  "SELECT (@rowNO := @rowNo + 1) AS rowno,a.* from( "
        		+ " SELECT tjo.id,tjo.order_no,tjo.lading_no lading_no,tjo.type,taco.invoice_no invoice_no,"
        		 +" 	( SELECT GROUP_CONCAT(tjoli.cabinet_date) "
        		 +" 		FROM  trans_job_order_land_item tjoli "
        		 +" 		LEFT JOIN trans_job_order tjor ON tjoli.order_id = tjor.id "
        		 +" 		WHERE tjor.id = tjo.id ) cabinet_date, "
        		 +"   	py.abbr  customer_name, "
        		 +" 	( SELECT GROUP_CONCAT(tjoli.unload_type) "
        		 +" 		FROM trans_job_order_land_item tjoli "
        		 + "        LEFT JOIN trans_job_order tjor ON tjoli.order_id = tjor.id "
        		 +" 		WHERE tjor.id = tjo.id ) unload_type, "
        		 +" 	d.dock_name, tjo.container_no, tjo.cabinet_type, "
        		 +" 	( SELECT GROUP_CONCAT(co.car_no) "
        		 +" 		FROM trans_job_order_land_item tjoli "
        		 +" 		LEFT JOIN trans_job_order tjor ON tjoli.order_id = tjor.id "
        		 +" 		LEFT JOIN carinfo co ON co.id = tjoli.car_no "
        		 +" 		WHERE tjor.id = tjo.id ) car_no, "
        		 +" 	if(tjoa.charge_id = 173,IFNULL(round(tjoa.total_amount,2),''),'') freight, "
        		 +"   cy. NAME  currency_name, "
        		 +" 	if(tjoa.charge_id = 264,IFNULL(round(tjoa.total_amount,2),''),'') weighing_fee, "
        		 +" 	if(tjoa.charge_id = 263,IFNULL(round(tjoa.total_amount,2),''),'') night_fee, "
        		 +" 	if(tjoa.charge_id = 262,IFNULL(round(tjoa.total_amount,2),''),'') high_speed_fee, "
        		 +" 	if(tjoa.charge_id = 174,IFNULL(round(tjoa.total_amount,2),''),'') call_fee, "
        		 +" 	if(tjoa.charge_id = 302,IFNULL(round(tjoa.total_amount,2),''),'') empty_in_out_fee, "
        		 +" 	if(tjoa.charge_id = 497,IFNULL(round(tjoa.total_amount,2),''),'') advance_fee, "
        		 +" 	tjo.remark "
        		 +" FROM trans_arap_charge_order taco "
        		 +" LEFT JOIN trans_arap_charge_item taci ON taco.id = taci.charge_order_id  "
        		 +" LEFT JOIN trans_job_order_arap tjoa ON taci.ref_order_id = tjoa.id "
        		
        		 +" LEFT JOIN currency cy ON cy.id = tjoa.currency_id "
        		 +" LEFT JOIN trans_job_order tjo ON tjo.id = tjoa.order_id "
        		 +" LEFT JOIN party py ON py.id = tjo.customer_id "
        		 +" LEFT JOIN dockinfo d ON d.id = tjo.take_wharf "
        		 +" WHERE "
        		 +" 	tjoa.id = taci.ref_order_id "
        		 +" AND taco.id = ("+order_id+") "
        		 +" AND tjo.delete_flag = 'N' "
        		 +" GROUP BY "
        		 +" 	tjoa.id "
        		 +" ORDER BY "
        		 +" 	taco.order_no, "
        		 +" 	tjo.order_no ) a,(SELECT @rowNO := 0) b";
        
        //List<String> headers = new ArrayList<String>();
        String[] headers = new String[]{"序号","提单号", "提柜日期", "客户", "方式", "提柜地址", "柜号", "尺码", "拖车号", "应收运费", "币制",
        								"打单费","高速费","过磅费","压夜费","吉进吉出","代垫费", "发票号"};
        String[] fields = new String[]{"ROWNO","LADING_NO", "CABINET_DATE", "CUSTOMER_NAME", "TYPE", "DOCK_NAME", "CONTAINER_NO", "CABINET_TYPE", "CAR_NO", "FREIGHT", "CURRENCY_NAME",
        								"CALL_FEE","HIGH_SPEED_FEE","WEIGHING_FEE","NIGHT_FEE","EMPTY_IN_OUT_FEE","ADVANCE_FEE",  "INVOICE_NO"};
        String fileName = PoiUtils.generateExcel(headers, fields, exportSql,company_name);
        renderText(fileName);
    }
    
    //收款确认
   	@Before(Tx.class)
 	public void confirmOrder(){
		   		 UserLogin user = LoginUserController.getLoginUser(this);
		   		 String jsonStr=getPara("params");
		  
		         Gson gson = new Gson();  
		         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class); 
		         String id=(String)dto.get("charge_order_id");
			   	 String itemids= (String) dto.get("itemids");
		   		
		   		String pay_remark=(String) dto.get("pay_remark");
		   		
		   		TransArapChargeReceiveItem cacritem=new TransArapChargeReceiveItem();
		   		String receive_time = (String) dto.get("receive_time");
		   		String deposit_bank = "";
		     	String payment_method = (String) dto.get("payment_method");
		     	
		     	if(dto.get("deposit_bank")!=null && !"".equals(dto.get("deposit_bank"))){
		   			 deposit_bank =  dto.get("deposit_bank").toString();
		   		}else{
		   			String str2="select id from fin_account where bank_name='现金' and office_id="+user.get("office_id");
		   	        Record rec = Db.findFirst(str2);
		   	        if(rec!=null){
		   	        	deposit_bank = rec.getLong("id").toString();
		   	        }
		   		}
		   		DbUtils.setModelValues(dto, cacritem); 
		   			//保存每次收款记录
		   			cacritem.save();
                
         				TransArapChargeOrder arapChargeOrder = TransArapChargeOrder.dao.findById(id);
         				//求每张应收对账单的每次收款金额记录表，求已收的总额
         				String sql1 =" SELECT	IFNULL( SUM(aci.receive_cny),0) paid_cny, "
         						+" 	IFNULL( SUM(aci.receive_jpy),0) paid_jpy, "
         						+" 	IFNULL( SUM(aci.receive_usd),0) paid_usd, "
         						+" 	IFNULL( SUM(aci.receive_hkd),0) paid_hkd "
         						+" FROM	trans_arap_charge_receive_item aci "
         						+" WHERE	aci.charge_order_id = "+id;
                            
                         Record r1 = Db.findFirst(sql1);                  
                         Double paid_cny = r1.getDouble("paid_cny");     
                         Double paid_usd = r1.getDouble("paid_usd");
                         Double paid_jpy = r1.getDouble("paid_jpy");
                         Double paid_hkd = r1.getDouble("paid_hkd");
                         
                         	//求每张应收对账单的总金额
                         String sql = "SELECT "
                         		+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_charge_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =3 and aci.charge_order_id="+id
                 				+" ),0) cny,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_charge_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =6 and aci.charge_order_id="+id
                 				+" ),0) usd,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_charge_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =8 and aci.charge_order_id="+id
                 				+" ),0) jpy,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_charge_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =9 and aci.charge_order_id="+id
                 				+" ),0) hkd ";
                            
                            Record r = Db.findFirst(sql);
                            Double cny = r.getDouble("cny");//greate_flay=Y的arap item 汇总金额
                            Double usd = r.getDouble("usd");
                            Double jpy = r.getDouble("jpy");
                            Double hkd = r.getDouble("hkd");
                 
         				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
         					arapChargeOrder.set("audit_status", "部分已收款").update();
         				}else{
         					arapChargeOrder.set("audit_status", "已收款").update();
         					//pay_flag为收付款标志
         					Db.update("update trans_job_order_arap set pay_flag = 'Y' where id in ("+itemids+")");
         				}
               //新建日记账表数据
           		if(!"0.0".equals(dto.get("receive_cny"))&&StringUtils.isNotEmpty((String) dto.get("receive_cny"))){
           			createAuditLog(id, payment_method, deposit_bank, receive_time, (String)dto.get("receive_cny"), "CNY");
           		}
	             if(!"0.0".equals(dto.get("receive_usd"))&&StringUtils.isNotEmpty((String)dto.get("receive_usd"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time,(String) dto.get("receive_usd"), "USD");
	             }
	             if(!"0.0".equals(dto.get("receive_jpy"))&&StringUtils.isNotEmpty((String) dto.get("receive_jpy"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time,(String) dto.get("receive_jpy"), "JPY");
	             }
	             if(!"0.0".equals(dto.get("receive_hkd"))&&StringUtils.isNotEmpty((String)dto.get("receive_hkd"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time, (String)dto.get("receive_hkd"), "HKD");
	             }
         
         Record r11 = new Record();
 		r11.set("confirm_name", LoginUserController.getUserNameById(LoginUserController.getLoginUserId(this)));
 		r11.set("status", arapChargeOrder.get("audit_status"));
 		r11.set("ids", id);
         renderJson(r11);
  
     }
   	
	@Before(Tx.class)
  	private void createAuditLog(String application_id, String payment_method,
            String receive_bank_id, String receive_time, String pay_amount, String currency_code) {
        //新建日记账表数据
  		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", payment_method);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_TRANSCHARGE);
        auditLog.set("currency_code", currency_code);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("office_id", office_id);
        if(receive_bank_id!=null && !("").equals(receive_bank_id)){
        		auditLog.set("account_id", receive_bank_id);
        	}
        auditLog.set("source_order", "运输应收对账单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
    }
    
	public void chargeEdit(){
    	String tjor_id = getPara("tjor_id");
	    String itemSql = "SELECT  tjor.id,tjo.id tjo_id,tjo.container_no container_no,tjo.so_no,tjo.charge_time charge_time,"
	    		+ " tjo.customer_id customer_id,p1.abbr customer_name,tjor.sp_id,p.abbr sp_name,"
	    		+ " tjor.charge_id charge_id,f. NAME fin_name,tjor.currency_id currency_id,c.name currency_name,tjor.total_amount total_amount,"
	    		+ " tjor.exchange_rate exchange_rate,(ifnull(tjor.total_amount,0)*ifnull(tjor.exchange_rate,1)) after_total,tjor.remark"
	    		+ " FROM trans_job_order_arap tjor"
	    		+ " LEFT JOIN trans_job_order tjo ON tjo.id = tjor.order_id"
	    		+ " LEFT JOIN party p ON p.id = tjor.sp_id"
	    		+ " LEFT JOIN party p1 ON p1.id = tjo.customer_id"
	    		+ " LEFT JOIN fin_item f ON f.id = tjor.charge_id" 
	    		+ " LEFT JOIN unit u ON u.id = tjor.unit_id"
	    		+ " LEFT JOIN currency c ON c.id = tjor.currency_id"
	    		+ " LEFT JOIN currency c1 ON c1.id = tjor.exchange_currency_id"
	    		+ " WHERE tjor.id = ? ORDER BY tjor.id";
	    List<Record> list = Db.find(itemSql,tjor_id);
		Map map = new HashMap();
		map.put("sEcho",1);
		map.put("iTotalRecords", list.size());
		map.put("iTotalDisplayRecords", list.size());
		map.put("aaData", list);
		renderJson(map); 
    }
    
	public void chargeSave(){
    	String jsonStr = getPara("params");
    	
    	Gson gson = new Gson();
    	Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
    	TransJobOrderArap tjoa = new TransJobOrderArap().findById(dto.get("tjor_id"));
    	TransJobOrder tjo = new TransJobOrder().findById(dto.get("tjo_id"));
    	String chargeOrderId=(String)dto.get("order_id");
    	tjoa.set("sp_id",(String)dto.get("sp_id"));
    	
    	String container_no = (String)dto.get("container_no");
    	if(container_no.isEmpty()){
    		tjo.set("container_no","");
    	}else{
    		tjo.set("container_no",container_no);
    	}
    	String so_no = (String)dto.get("so_no");
    	if(so_no.isEmpty()){
    		tjo.set("so_no","");
    	}else{
    		tjo.set("so_no",so_no);
    	}
    	
    	String customer_id = (String)dto.get("customer_id");
    	if(customer_id.isEmpty()){
    		tjo.set("customer_id","");
    	}else{
    		tjo.set("customer_id",customer_id);
    	}
    	
    	String charge_id = (String)dto.get("charge_id");
    	if(charge_id.isEmpty()){
    		tjoa.set("charge_id","");
    	}else{
    		tjoa.set("charge_id",charge_id);
    	}
    	
    	String total_amount = (String)dto.get("total_amount");
    	if(total_amount.isEmpty()){
    		tjoa.set("total_amount","");
    	}else{
    		tjoa.set("total_amount",total_amount);
    	}
    	
    	String exchange_rate = (String)dto.get("exchange_rate");
    	if(exchange_rate.isEmpty()){
    		tjoa.set("exchange_rate","");
    	}else{
    		tjoa.set("exchange_rate",exchange_rate);
    	}
    	
    	tjoa.set("remark",(String)dto.get("remark"));
    	tjoa.update();
    	tjo.update();
    	//计算结算汇总
    	Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		exchangeTotalMap.put("chargeOrderId", Double.parseDouble(chargeOrderId));
		
    	renderJson(exchangeTotalMap);
    }
    
}
