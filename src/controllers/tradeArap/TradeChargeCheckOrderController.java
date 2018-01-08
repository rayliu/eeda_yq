package controllers.tradeArap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.eeda.tr.tradeJoborder.TradeArapChargeItem;
import models.eeda.tr.tradeJoborder.TradeArapChargeOrder;
import models.eeda.tr.tradeJoborder.TradeArapCostOrder;
import models.RateContrast;
import models.UserLogin;
import models.eeda.tr.tradeJoborder.TradeJobOrderArap;
import models.eeda.cms.CustomArapChargeOrder;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.profile.Currency;

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
import controllers.eeda.SysInfoController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TradeChargeCheckOrderController extends Controller {

	private Logger logger = Logger.getLogger(TradeChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {

		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/tradeChargeCheckOrder");
        setAttr("listConfigList", configList);
		render("/tradeArap/ChargeCheckOrder/ChargeCheckOrderList.html");
	}
	
	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
       TradeArapChargeOrder order = new TradeArapChargeOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		String action_type="add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type="update";
   			order = TradeArapChargeOrder.dao.findById(id);
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
   			order.set("create_stamp", new Date());
   			order.set("office_id", office_id);
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}

   		TradeArapChargeItem aci = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
   		if(itemList != null){
   			for(Map<String, String> item :itemList){
   				String action = item.get("action");
   				String itemId = item.get("id");
   				if("CREATE".equals(action)){
   					aci = new TradeArapChargeItem();
   					aci.set("ref_order_type", "工作单");
   					aci.set("ref_order_id", itemId);
   					aci.set("charge_order_id", id);
   					aci.save();
   	                TradeJobOrderArap jobOrderArap = TradeJobOrderArap.dao.findById(itemId);
   	                jobOrderArap.set("bill_flag", "Y");
   	                String hedge_order_type = jobOrderArap.getStr("order_type");
   	                if("cost".equals(hedge_order_type)){
   	                	jobOrderArap.set("hedge_flag", "Y");
   	                }
   	                jobOrderArap.update();
   				}
   			}
   		}
		
		List<Map<String, String>> currencyList = (ArrayList<Map<String, String>>)dto.get("currency_list");
		if(currencyList != null){
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
		}
		
		SysInfoController.saveLog(jsonStr, id, user, action_type, "应收对账单", "trade");
		
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
        	       
      				+"(SELECT "
      				+"            joa.order_type sql_type,"
      				+"            joa.id,"
      				+"            joa.sp_id,"
      				+"            IFNULL(joa.total_amount, 0) total_amount,"
      				+"            IFNULL(joa.currency_total_amount, 0) currency_total_amount,"
      				+"            jo.id jobid,"
      				+"            jo.order_no,"
      				+"            jo.create_stamp,"
      				+"            jo.order_export_date,"
      				+"            jo.customer_id,"
      				+"            jo.volume,"
      				+"            jo.net_weight,"
      				+"            jo.ref_no,"
      				+"            jo.type,"
      				+"            p.abbr sp_name,"
      				+"            p1.abbr customer_name,"
      				+"            '' mbl_no,"
      				+"            '' hbl_no,"
      				+"            ''  fnd,"
      				+"            '' destination,"
      				+"            ''  container_no,"
      				+"            ''  container_amount,"
      				+"            IFNULL(cur.name, 'CNY') currency_name,"
      				+"            '' truck_type,"
      				+"            IFNULL(joa.exchange_rate, 1) exchange_rate,"
      				+"            (IFNULL(joa.total_amount, 0) * IFNULL(joa.exchange_rate, 1)) after_total,"
      				+"            IFNULL((SELECT "
      				+"                    rc.new_rate"
      				+"                FROM"
      				+"                    rate_contrast rc"
      				+"                WHERE"
      				+"                    rc.currency_id = joa.currency_id"
      				+"                        AND rc.order_id = ''), IFNULL(joa.exchange_rate, 1)) * IFNULL(joa.total_amount, 0) after_rate_total,"
      				+"            IFNULL(f.name, f.name_eng) fee_name,"
      				+"            cur1.name exchange_currency_name,"
      				+"            joa.exchange_currency_rate,"
      				+"            joa.exchange_total_amount"
                    +"    FROM"
                    +"        trade_job_order jo"
                    +"    LEFT JOIN trade_job_order_arap joa ON jo.id = joa.order_id"
                    +"    LEFT JOIN party p ON p.id = joa.sp_id"
                    +"    LEFT JOIN party p1 ON p1.id = jo.customer_id"
                    +"    LEFT JOIN currency cur ON cur.id = joa.currency_id"
                    +"    LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
                    +"    LEFT JOIN fin_item f ON f.id = joa.charge_id"
                    +"    WHERE"
                    +"            joa.audit_flag = 'Y'"
                    +"            AND joa.bill_flag = 'N'"
                    +"            AND jo.office_id ="+office_id
                    + " and jo.delete_flag = 'N'"
    				+"    GROUP BY joa.id)"
      				+ ") B where 1=1 ";
        	}else{
        		 sql = "select * from( "
         				+"(SELECT "
         				+"        IFNULL(f.name, f.name_eng) fee_name,"
         				+"            joa.id,"
         				+"            joa.sp_id,"
         				+"            IFNULL(joa.total_amount, 0) total_amount,"
         				+"            IFNULL(joa.currency_total_amount, 0) currency_total_amount,"
         				+"            jo.id jobid,"
         				+"            jo.order_no,"
         				+"            jo.create_stamp,"
         				+"            jo.order_export_date,"
         				+"            jo.customer_id,"
         				+"            jo.volume,"
         				+"            jo.net_weight,"
         				+"            jo.ref_no,"
         				+"            jo.type,"
         				+"            p.abbr sp_name,"
         				+"            p1.abbr customer_name,"
         				+"            '' mbl_no,"
         				+"            '' hbl_no,"
         				+"            '' fnd,"
         				+"            '' destination,"
         				+"            '' container_no,"
         				+"            '' container_amount,"
         				+"            IFNULL(cur.name, 'CNY') currency_name,"
         				+"            '' truck_type,"
         				+"            IFNULL(joa.exchange_rate, 1) exchange_rate,"
         				+"            (IFNULL(joa.total_amount, 0) * IFNULL(joa.exchange_rate, 1)) after_total,"
         				+"            IFNULL((SELECT "
         				+"                    rc.new_rate"
         				+"                FROM"
         				+"                    rate_contrast rc"
         				+"                WHERE"
         				+"                    rc.currency_id = joa.currency_id"
         				+"                        AND rc.order_id = ''), IFNULL(joa.exchange_rate, 1)) * IFNULL(joa.total_amount, 0) after_rate_total,"
         				+"            cur1.name exchange_currency_name,"
         				+"            joa.exchange_currency_rate,"
         				+"            joa.exchange_total_amount"
         				+"    FROM"
         				+"        trade_job_order jo"
         				+"    LEFT JOIN trade_job_order_arap joa ON jo.id = joa.order_id"
         				+"    LEFT JOIN party p ON p.id = joa.sp_id"
         				+"    LEFT JOIN party p1 ON p1.id = jo.customer_id"
         				+"    LEFT JOIN currency cur ON cur.id = joa.currency_id"
         				+"    LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
         				+"    LEFT JOIN fin_item f ON f.id = joa.charge_id"
         				+"    WHERE"
         				+"        joa.order_type = 'charge'"
         				+"            AND joa.audit_flag = 'Y'"
         				+"            AND joa.bill_flag = 'N'"
         				+"            AND jo.office_id ="+office_id
         				 + " and jo.delete_flag = 'N'"
         				+"    GROUP BY joa.id)"
         				+ " ) B where 1=1 ";
        			}
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition);
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
        		+ " select aco.*,IFNULL(aco.audit_status,aco.status) toStatus, p.abbr sp_name "
				+ " from trade_arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
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
			sql = " select joa.id, joa.order_type,jo.order_no,jo.order_export_date,jo.customer_id,jo.volume vgm,"
    			+ "IFNULL(cur1.name,cur.name) exchange_currency_name,"
    			+ "IFNULL(joa.exchange_currency_rate,1) exchange_currency_rate,IFNULL(joa.exchange_total_amount,joa.total_amount) exchange_total_amount,"
    			+ "joa.total_amount total_amount,joa.exchange_rate exchange_rate," 
    			+ " jo.net_weight gross_weight,"
    			+ " cur.name currency_name,"
    			+ " jo.ref_no ref_no,"
    			+ " p1.company_name sp_name,"
    			+ " '' mbl_no, '' fnd, '' destination, '' hbl_no, '' truck_type,"
    			+ " '' container_no, '' container_amount ,"
    			+ " fi.name fin_name "
    			+ " from trade_job_order_arap joa"
    			+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
    			+ " LEFT JOIN currency cur1 on cur1.id = joa.exchange_currency_id"
    			+ "	left join trade_job_order jo on jo.id=joa.order_id "
				+ " left join fin_item fi on joa.charge_id = fi.id "
    			/*+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
    			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
    			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
    			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "*/
    			+ "	left join party p1 on p1.id=joa.sp_id "
    			//+ "	left join location l on l.id=jos.fnd "
    			+ "	where joa.audit_flag='Y' "
    			+ " and joa.id in("+ids+")"
    			 + " and jo.delete_flag = 'N'"
 				+ " GROUP BY joa.id";
			}else{				
			sql = " select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
					+" aco.order_no check_order_no, jo.id job_order_id, jo.order_no,jo.order_export_date,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
					+ " jo.ref_no ref_no,"
						+" p.abbr sp_name,p1.abbr customer_name,"
						+ " '' mbl_no, '' fnd, '' destination,"
						+" ifnull((select rc.new_rate from rate_contrast rc"
						    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
						    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
						    +"  ifnull((select rc.new_rate from rate_contrast rc"
						    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
						+" '' container_no, '' container_amount,"
						+ " fi.name fin_name,"
						+ " cur.name currency_name,"
						+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
						+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
						+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
						+" from trade_job_order jo"
						+" left join trade_job_order_arap joa on jo.id=joa.order_id"
						+" left join fin_item fi on joa.charge_id = fi.id"
						/*+" left join job_order_shipment jos on jos.order_id=joa.order_id"
						+" left join job_order_shipment_item josi on josi.order_id=joa.order_id"
						+" left join job_order_air_item joai on joai.order_id=joa.order_id"*/
						+" left join party p on p.id=joa.sp_id"
						+" left join party p1 on p1.id=jo.customer_id"
						//+" left join location l on l.id=jos.fnd"
						+" left join currency cur on cur.id=joa.currency_id"
						+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
						+" left join trade_arap_charge_item aci on aci.ref_order_id = joa.id"
					 +" left join trade_arap_charge_order aco on aco.id = aci.charge_order_id"
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
				sql = " select joa.id,joa.create_flag,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name, '' mbl_no, '' fnd, '' destination,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+" '' container_no, '' container_amount,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from trade_job_order jo"
							+" left join trade_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							+" left join trade_charge_application_order_rel caol on caol.job_order_arap_id  = joa.id"
							+" left join trade_arap_charge_application_order acao on caol.application_order_id = acao.id"
							 +" left join trade_arap_charge_order aco on aco.id=caol.charge_order_id"
						  +" where acao.id="+order_ids+query_fin_name
						  + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
				
			}else{
				sql = "select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name, '' mbl_no, '' fnd, '' destination,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+" '' container_no, '' container_amount,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from trade_job_order jo"
							+" left join trade_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							+" left join trade_arap_charge_item aci on aci.ref_order_id = joa.id"
						 +" left join trade_arap_charge_order aco on aco.id = aci.charge_order_id"
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
    
    
    public List<Record> getCurrencyList(String ids,String order_id){
    	String sql = "SELECT "
    			+ " (select rc.id from rate_contrast rc "
    	    	+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
    			+ " ifnull((select rc.new_rate from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1)) new_rate"
				+ " FROM trade_job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    
    @Before(EedaMenuInterceptor.class)
	public void create(){
		String ids = getPara("idsArray");//job_order_arap ids
		//String total_amount = getPara("totalAmount");
		String cny_totalAmount = getPara("cny_totalAmount");
		String usd_totalAmount = getPara("usd_totalAmount");
		String hkd_totalAmount = getPara("hkd_totalAmount");
		String jpy_totalAmount = getPara("jpy_totalAmount");
		String exchange_total_amount = getPara("exchange_totalAmount");
		String exchange_cny_totalAmount = getPara("exchange_cny_totalAmount");
		String exchange_usd_totalAmount = getPara("exchange_usd_totalAmount");
		String exchange_hkd_totalAmount = getPara("exchange_hkd_totalAmount");
		String exchange_jpy_totalAmount = getPara("exchange_jpy_totalAmount");
		
		String sql = "SELECT cur.name currency_name ,joa.exchange_rate ,p.phone,p.abbr company_abbr,p.contact_person,p.address,p.company_name,joa.sp_id,joa.order_id"
				+ " FROM trade_job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " left join party p on p.id = joa.sp_id "
				+ " WHERE joa.id in("+ ids +")"
				+ " group by joa.order_id";
		Record rec =Db.findFirst(sql);
		//rec.set("total_amount", total_amount);
		//对账
		rec.set("jpy_duizhang", jpy_totalAmount);
		rec.set("cny_duizhang", cny_totalAmount);
		rec.set("usd_duizhang",usd_totalAmount);
		rec.set("hkd_duizhang", hkd_totalAmount);
		//结算
		rec.set("exchange_total_amount", exchange_total_amount);
		rec.set("jpy", exchange_jpy_totalAmount);
		rec.set("cny", exchange_cny_totalAmount);
		rec.set("usd",exchange_usd_totalAmount);
		rec.set("hkd", exchange_hkd_totalAmount);
		

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("user", LoginUserController.getLoginUserName(this));
		rec.set("itemList", getItemList(ids,"",""));
		rec.set("currencyList", getCurrencyList(ids,""));
		rec.set("company_abbr", rec.get("company_abbr"));
		setAttr("order",rec);
		render("/tradeArap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}
	
	
    @Before(EedaMenuInterceptor.class)
    public void edit(){
		String id = getPara("id");//trade_arap_charge_order id
	    UserLogin user1 = LoginUserController.getLoginUser(this);
	    long office_id=user1.getLong("office_id");
	    //判断与登陆用户的office_id是否一致
	    if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("trade_arap_charge_order", Long.valueOf(id), office_id)){
	    	renderError(403);// no permission
	        return;
	    }
		String condition = "select ref_order_id from trade_arap_charge_item where charge_order_id ="+id;
		
		String sql = " select aco.*,p.company_name,p.contact_person,p.id company_id,p.abbr company_abbr,p.phone,p.address,u.c_name creator_name,u1.c_name confirm_by_name from trade_arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record rec =Db.findFirst(sql,id);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("itemList", getItemList(condition,id,""));
		rec.set("currencyList", getCurrencyList(condition,id));
		rec.set("company_id", rec.get("company_id"));
		rec.set("company_abbr", rec.get("company_abbr"));
		setAttr("order",rec);
		render("/tradeArap/ChargeCheckOrder/ChargeCheckOrderEdit.html");
	}

    
    @Before(Tx.class)
	public void exchange_currency(){
	    String chargeOrderId = getPara("charge_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update trade_job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		renderJson(exchangeTotalMap);
	}
    
    //退掉单据
    @Before(Tx.class)
    public void returnOrder(){
        String id = getPara("id");
        String delete_reason = getPara("delete_reason");
        TradeArapChargeOrder order = TradeArapChargeOrder.dao.findById(id);
        order.set("status","已退单");
        order.set("update_stamp", new Date());
        order.set("return_reason", delete_reason);
        order.set("update_by", LoginUserController.getLoginUserId(this));
        order.update();
        renderJson("{\"result\":true}");
    }
    
    private Map<String, Double> updateExchangeTotal(String chargeOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  trade_job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select aci.ref_order_id from trade_arap_charge_item aci where aci.charge_order_id="+chargeOrderId+")";
		
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
		
		Record order = Db.findById("trade_arap_charge_order", chargeOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("trade_arap_charge_order", order);
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
    	String condition = "select ref_order_id from trade_arap_charge_item where charge_order_id in ("+order_ids+") ";
    	
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
		logger.debug("TradeArapChargeOrder id:"+id);
		TradeArapChargeOrder aco = TradeArapChargeOrder.dao.findById(id);
		aco.set("status","已确认");
		aco.set("confirm_stamp", new Date());
		aco.set("confirm_by", LoginUserController.getLoginUserId(this));
		aco.update();
		
		//设置y，已生成对账单o
		String itemList=aco.get("ref_order_id");
		String sql="UPDATE trade_job_order_arap joa set billConfirm_flag='Y' "
					+"where joa.id in (select aci.ref_order_id FROM trade_arap_charge_item aci where charge_order_id="+id+" )";
		Db.update(sql);
		
		Record r = aco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
		renderJson(r);
	}
    
    public void cancelConfirm(){
   	 String id = getPara("id");
   	 long office_id = LoginUserController.getLoginUser(this).getLong("office_id");
   	 Date action_time = new Date();
   	 String action = "cancelConfirm";
   	 String order_type = "tradeChargeCheckOrder";
   	 //保存进状态审核表
   	 Record re = new Record();
   	 re.set("order_id", id);
   	 re.set("user_id", office_id);
   	 re.set("action_time", action_time);
   	 re.set("action",action);
   	 re.set("order_type", order_type);
   	 Db.save("status_audit", re);
   	 //更新arap_charge_order表的状态
   	 TradeArapChargeOrder aco = TradeArapChargeOrder.dao.findById(id);
		 aco.set("status","取消确认");
		 aco.update();
		//更新job_order_arap表的billConfirm_flag设为'N'(变回未确认状态)
		 String sql="UPDATE trade_job_order_arap joa set billConfirm_flag='N' "
					+"where joa.id in (select aci.ref_order_id FROM trade_arap_charge_item aci where charge_order_id="+id+" )";
		 Db.update(sql);
		 
		 renderJson(true);
   }
    
    //编辑
    public void chargeEdit(){
    	String tjor_id = getPara("tjor_id");
	    String itemSql = "select tjor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from trade_job_order_arap tjor "
    		        + " left join party pr on pr.id=tjor.sp_id"
    		        + " left join fin_item f on f.id=tjor.charge_id"
    		        + " left join unit u on u.id=tjor.unit_id"
    		        + " left join currency c on c.id=tjor.currency_id"
    		        + " left join currency c1 on c1.id=tjor.exchange_currency_id"
    		        + " where tjor.id=? order by tjor.id";
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
    	TradeJobOrderArap tjoa = new TradeJobOrderArap().findById(dto.get("tjor_id"));
    	String chargeOrderId=(String)dto.get("order_id");
    	String price = (String)dto.get("price");
    	if(price.isEmpty()){
    		tjoa.set("price",0);
    	}else{
    		tjoa.set("price",price);
    	}    	
    	
    	String amount = (String)dto.get("amount");
    	if(amount.isEmpty()){
    		tjoa.set("amount",0);
    	}else{
    		tjoa.set("amount",amount);
    	}
    	tjoa.set("unit_id",(String)dto.get("unit_id"));
    	
    	String total_amount = (String)dto.get("total_amount");
    	if(total_amount.isEmpty()){
    		tjoa.set("total_amount",0);
    	}else{
    		tjoa.set("total_amount",total_amount);
    	}
    	String currency_total_amount = (String)dto.get("currency_total_amount");
    	if(total_amount.isEmpty()){
    		tjoa.set("currency_total_amount",0);
    	}else{
    		tjoa.set("currency_total_amount",currency_total_amount);
    	}
    	
    	String exchange_currency_rate = (String)dto.get("exchange_currency_rate");
    	if(exchange_currency_rate.isEmpty()){
    		tjoa.set("exchange_currency_rate",0);
    	}else{
    		tjoa.set("exchange_currency_rate",exchange_currency_rate);
    	}
    	
    	String exchange_total_amount = (String)dto.get("exchange_total_amount");
    	if(exchange_total_amount.isEmpty()){
    		tjoa.set("exchange_total_amount",0);
    	}else{
    		tjoa.set("exchange_total_amount",exchange_total_amount);
    	}
    	
    	String exchange_currency_rate_rmb = (String)dto.get("exchange_currency_rate_rmb");
    	if(exchange_currency_rate_rmb.isEmpty()){
    		tjoa.set("exchange_currency_rate_rmb",0);
    	}else{
    		tjoa.set("exchange_currency_rate_rmb",exchange_currency_rate_rmb);
    	}
    	
    	String exchange_total_amount_rmb = (String)dto.get("exchange_total_amount_rmb");
    	if(exchange_total_amount_rmb.isEmpty()){
    		tjoa.set("exchange_total_amount_rmb",0);
    	}else{
    		tjoa.set("exchange_total_amount_rmb",exchange_total_amount_rmb);
    	}
    	tjoa.set("sp_id",(String)dto.get("sp_id"));
    	tjoa.set("charge_id",(String)dto.get("charge_id"));
    	tjoa.set("currency_id",(String)dto.get("currency_id"));
    	tjoa.set("exchange_currency_id",(String)dto.get("exchange_currency_id"));
    	tjoa.set("rmb_difference",(String)dto.get("rmb_difference"));
    	tjoa.set("remark",(String)dto.get("remark"));
    	tjoa.update();
    	//计算结算汇总
    	Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		exchangeTotalMap.put("chargeOrderId", Double.parseDouble(chargeOrderId));
		
    	renderJson(exchangeTotalMap);
    }
    
    public void insertChargeItem(){
    	String itemList= getPara("charge_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String chargeOrderId=getPara("order_id");
    	TradeArapChargeItem aci = null;
    	
    	if(chargeOrderId != null){
    		for(String itemId:itemArray){
    			aci = new TradeArapChargeItem();
	    		 TradeJobOrderArap jobOrderArap = TradeJobOrderArap.dao.findById(itemId);

	             jobOrderArap.set("bill_flag", "Y");
	             String hedge_order_type = jobOrderArap.getStr("order_type");
	             if("cost".equals(hedge_order_type)){
	                	jobOrderArap.set("hedge_flag", "Y");
	               }
	             jobOrderArap.update();
				aci.set("ref_order_id", itemId);
				aci.set("charge_order_id", chargeOrderId);
				aci.save();
//        	String sql="INSERT into trade_arap_charge_item (ref_order_id,charge_order_id) "
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
    		 TradeJobOrderArap jobOrderArap = TradeJobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("bill_flag", "N");
    		 jobOrderArap.set("hedge_flag", "N");
             jobOrderArap.update();
//             String sql="delete from  where ref_order_id="+itemid+"and charge_order_id="+chargeOrderId;
             Db.deleteById("trade_arap_charge_item","ref_order_id,charge_order_id",itemid,chargeOrderId);
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
    			exchangeTotalMap.put("chargeOrderId", Double.parseDouble(chargeOrderId));
    	    	renderJson(exchangeTotalMap);
    }  

}
