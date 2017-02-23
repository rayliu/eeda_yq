package controllers.arap.cmsAr;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.cms.CustomArapChargeItem;
import models.eeda.cms.CustomArapChargeOrder;
import models.eeda.cms.CustomPlanOrderArap;
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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CmsChargeCheckOrderController extends Controller {

	private Logger logger = Logger.getLogger(CmsChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/cmsArap/cmsChargeCheckOrder/cmsChargeCheckOrderList.html");
	}
	
	@Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        CustomArapChargeOrder order = new CustomArapChargeOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = CustomArapChargeOrder.dao.findById(id);
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

   			CustomArapChargeItem aci = null;
   			List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
   			for(Map<String, String> item :itemList){
   					String item_order_id=item.get("order_check_box");
		   			String sql2="SELECT id FROM custom_plan_order_arap"
		   					+ " where order_id in("+ item_order_id+") and order_type='charge'";
		   			List<Record> ids=Db.find(sql2);
		   			
		   			for(Record re :ids ){
		   				aci = new CustomArapChargeItem();
						aci.set("ref_order_type", "工作单");
						aci.set("ref_order_id", re.get("ID"));
						aci.set("custom_charge_order_id", id);
						aci.save();
						CustomPlanOrderArap cmsOrderArap = CustomPlanOrderArap.dao.findById(re.get("ID"));
						cmsOrderArap.set("bill_flag", "Y");
						cmsOrderArap.update();
		   			}
   				}
   		}
//   		CustomArapChargeItem aci = null;
//   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
//		for(Map<String, String> item :itemList){
//			String action = item.get("action");
//			String itemId = item.get("id");
//			if("CREATE".equals(action)){
//				aci = new CustomArapChargeItem();
//				aci.set("ref_order_type", "工作单");
//				aci.set("ref_order_id", itemId);
//				aci.set("custom_charge_order_id", id);
//				aci.save();
//				CustomPlanOrderArap cmsOrderArap = CustomPlanOrderArap.dao.findById(itemId);
//				cmsOrderArap.set("bill_flag", "Y");
//				cmsOrderArap.update();
//
//			}
//		}
		
		
//		List<Map<String, String>> currencyList = (ArrayList<Map<String, String>>)dto.get("currency_list");
//		for(Map<String, String> item :currencyList){
//			String new_rate = item.get("new_rate");
//			String rate = item.get("rate");
//			String order_type = item.get("order_type");
//			String currency_id = item.get("currency_id");
//			String rate_id = item.get("rate_id");
//			String order_id = (String) dto.get("id");
//			
//			RateContrast rc = null;
//			if(StringUtils.isEmpty(rate_id) && StringUtils.isEmpty(order_id)){
//				rc = new RateContrast();
//				rc.set("order_id", id);
//				rc.set("new_rate", new_rate);
//				rc.set("rate", rate);
//				rc.set("currency_id", currency_id);
//				rc.set("order_type", order_type);
//				rc.set("create_by", LoginUserController.getLoginUserId(this));
//				rc.set("create_stamp", new Date());
//				rc.save();
//			}else{
//				rc = RateContrast.dao.findById(rate_id);
//				if(rc == null){
//					rc = RateContrast.dao.findFirst("select * from rate_contrast where order_id = ? and currency_id = ?",order_id,currency_id);
//				}
//				rc.set("new_rate", new_rate);
//				rc.set("update_by", LoginUserController.getLoginUserId(this));
//				rc.set("update_stamp", new Date());
//				rc.update();
//			}	
//		}
		
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
	   		 sql = "select B.* from(  "
						 +" SELECT cpoa .*,cpo.id cpoid,cpo.order_no order_no,cpo.create_stamp create_stamp,cpo.booking_no booking_no,p.abbr sp_name, "
		   				 +" cpo.custom_export_date ,"
						 +" sum(iF(cpoa.charge_id=164,cpoa.total_amount,0)) MTF, "
						 +" sum(iF(cpoa.charge_id=165,cpoa.total_amount,0)) YJ, "
						 +" sum(iF(cpoa.charge_id=166,cpoa.total_amount,0)) ZHF, "
						 +" sum(iF(cpoa.charge_id=167,cpoa.total_amount,0 )) GKF, "
						 +" sum(iF(cpoa.charge_id=168,cpoa.total_amount,0)) LHF, "
						 +" sum(iF(cpoa.charge_id=169,cpoa.total_amount,0)) SCF, "
						 +" sum(iF(cpoa.charge_id=175,cpoa.total_amount,0)) ZLSCF, "
						 +" sum(iF(cpoa.charge_id=176,cpoa.total_amount,0)) FTF, "
						 +" sum(iF(cpoa.charge_id=177,cpoa.total_amount,0)) PZF, "
						 +" sum(iF(cpoa.charge_id=178,cpoa.total_amount,0)) XDF, "
						 +" sum(iF(cpoa.charge_id=179,cpoa.total_amount,0)) WLDLF, "
						 +" sum(iF(cpoa.charge_id=180,cpoa.total_amount,0)) LXF, "
						 +" sum(iF(cpoa.charge_id=181,cpoa.total_amount,0)) AC,  "
						 +" sum(iF(cpoa.charge_id=182,cpoa.total_amount,0)) WJF, "
						 +" sum(iF(cpoa.charge_id=183,cpoa.total_amount,0)) RZF,  "
						 +" sum(iF(cpoa.charge_id=184,cpoa.total_amount,0)) YF, "
						 +" sum(iF(cpoa.charge_id=185,cpoa.total_amount,0)) BGF, "
						 +" sum(iF(cpoa.charge_id=186,cpoa.total_amount,0)) DTF "
						 +" FROM custom_plan_order_arap cpoa  "
						 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
						 +" LEFT JOIN party p on p.id = cpo.application_unit "
						 +" LEFT JOIN fin_item fi on cpoa.charge_id = fi.id  "
						 +" WHERE cpoa.audit_flag='Y' and cpoa.bill_flag='N' AND cpo.office_id = "+office_id
		 				 + " GROUP BY cpoa.order_id "
		 				 + " ) B "
		 				 +" where 1=1 ";
     			}else{
		   		 sql = "select B.* from(  "
		   				+" SELECT cpo.order_no,cpo.id cpoid,cpo.date_custom,cpo.booking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
		   				 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
		   				 +" from custom_plan_order_arap cpoa "
		   				 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
		   				 +" LEFT JOIN fin_item f on f.id = cpoa.currency_id "
		   				 +" LEFT JOIN party p on p.id = cpoa.sp_id "
		   				 +" where cpoa.order_type='charge' and cpoa.audit_flag='Y' and cpoa.bill_flag='N'  and cpo.office_id = "+office_id
		   				 +"  GROUP BY cpoa.id " 
		 				 + " ) B "
		 				 +" where 1=1 ";
        			}
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
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
        		+ " select aco.*, p.abbr sp_name "
				+ " from custom_arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.office_id = "+office_id
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
    
    
    public List<Record> getItemList(String ids,String order_id){
    	String sql = null;
		if(StringUtils.isEmpty(order_id)){
	   		 sql = "select * from(  "
					 +" SELECT cpoa .*,cpo.id cpoid,cpo.order_no order_no,cpo.create_stamp create_stamp,cpo.booking_no booking_no,p.abbr company_abbr,"
						 +" sum(iF(cpoa.charge_id=164,cpoa.total_amount,0)) MTF, "
						 +" sum(iF(cpoa.charge_id=165,cpoa.total_amount,0)) YJ, "
						 +" sum(iF(cpoa.charge_id=166,cpoa.total_amount,0)) ZHF, "
						 +" sum(iF(cpoa.charge_id=167,cpoa.total_amount,0 )) GKF, "
						 +" sum(iF(cpoa.charge_id=168,cpoa.total_amount,0)) LHF, "
						 +" sum(iF(cpoa.charge_id=169,cpoa.total_amount,0)) SCF, "
						 +" sum(iF(cpoa.charge_id=175,cpoa.total_amount,0)) ZLSCF, "
						 +" sum(iF(cpoa.charge_id=176,cpoa.total_amount,0)) FTF, "
						 +" sum(iF(cpoa.charge_id=177,cpoa.total_amount,0)) PZF, "
						 +" sum(iF(cpoa.charge_id=178,cpoa.total_amount,0)) XDF, "
						 +" sum(iF(cpoa.charge_id=179,cpoa.total_amount,0)) WLDLF, "
						 +" sum(iF(cpoa.charge_id=180,cpoa.total_amount,0)) LXF, "
						 +" sum(iF(cpoa.charge_id=181,cpoa.total_amount,0)) AC,  "
						 +" sum(iF(cpoa.charge_id=182,cpoa.total_amount,0)) WJF, "
						 +" sum(iF(cpoa.charge_id=183,cpoa.total_amount,0)) RZF,  "
						 +" sum(iF(cpoa.charge_id=184,cpoa.total_amount,0)) YF, "
						 +" sum(iF(cpoa.charge_id=185,cpoa.total_amount,0)) BGF, "
						 +" sum(iF(cpoa.charge_id=186,cpoa.total_amount,0)) DTF "
					 +" FROM custom_plan_order_arap cpoa  "
					 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
					 +" LEFT JOIN party p on p.id = cpo.application_unit "
					 +" LEFT JOIN fin_item fi on cpoa.charge_id = fi.id  "
					 +" WHERE cpoa.order_id in("+ids+")"
					 +" and cpoa.audit_flag='Y' "
	 				+ " GROUP BY cpoa.order_id "
	 				+ " ) B where 1=1 ";
				}else{
			   		 sql = "select * from(  "
							 +" SELECT cpoa .*,cpo.id cpoid,cpo.order_no order_no,cpo.create_stamp create_stamp,cpo.booking_no booking_no,p.abbr company_abbr, "
							 +" sum(iF(cpoa.charge_id=164,cpoa.total_amount,0)) MTF, "
							 +" sum(iF(cpoa.charge_id=165,cpoa.total_amount,0)) YJ, "
							 +" sum(iF(cpoa.charge_id=166,cpoa.total_amount,0)) ZHF, "
							 +" sum(iF(cpoa.charge_id=167,cpoa.total_amount,0 )) GKF, "
							 +" sum(iF(cpoa.charge_id=168,cpoa.total_amount,0)) LHF, "
							 +" sum(iF(cpoa.charge_id=169,cpoa.total_amount,0)) SCF, "
							 +" sum(iF(cpoa.charge_id=175,cpoa.total_amount,0)) ZLSCF, "
							 +" sum(iF(cpoa.charge_id=176,cpoa.total_amount,0)) FTF, "
							 +" sum(iF(cpoa.charge_id=177,cpoa.total_amount,0)) PZF, "
							 +" sum(iF(cpoa.charge_id=178,cpoa.total_amount,0)) XDF, "
							 +" sum(iF(cpoa.charge_id=179,cpoa.total_amount,0)) WLDLF, "
							 +" sum(iF(cpoa.charge_id=180,cpoa.total_amount,0)) LXF, "
							 +" sum(iF(cpoa.charge_id=181,cpoa.total_amount,0)) AC,  "
							 +" sum(iF(cpoa.charge_id=182,cpoa.total_amount,0)) WJF, "
							 +" sum(iF(cpoa.charge_id=183,cpoa.total_amount,0)) RZF,  "
							 +" sum(iF(cpoa.charge_id=184,cpoa.total_amount,0)) YF, "
							 +" sum(iF(cpoa.charge_id=185,cpoa.total_amount,0)) BGF, "
							 +" sum(iF(cpoa.charge_id=186,cpoa.total_amount,0)) DTF "
							 +" FROM custom_arap_charge_order caco  "
							 +" left join custom_arap_charge_item caci on caci.custom_charge_order_id=caco.id "
							 +" left join custom_plan_order_arap cpoa on cpoa.id=caci.ref_order_id"
							 +" LEFT JOIN custom_plan_order cpo on cpo.id=cpoa.order_id"
					         +" LEFT JOIN party p on p.id=cpo.application_unit " 
							 +" where caco.id ="+order_id
			 				+ " GROUP BY cpoa.order_id "
			 				+ " ) B where 1=1 ";
						}	
    	List<Record> re = Db.find(sql);
    	return re;
    }
    
//    public List<Record> getCurrencyList(String ids,String order_id){
//    	String sql = "SELECT "
//    			+ " (select rc.id from rate_contrast rc "
//    	    	+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
//    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
//    			+ "0ull((select rc.new_rate from rate_contrast rc "
//    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1)) new_rate"
//				+ " FROM job_order_arap joa"
//				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
//				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
//    	List<Record> re = Db.find(sql);
//    	
//    	return re;
//    }
    
    @Before(EedaMenuInterceptor.class)
	public void create(){
		String ids = getPara("idsArray");//job_order_arap ids
		String total_amount = getPara("totalAmount");
		String cny_totalAmount = getPara("cny_totalAmount");
		String usd_totalAmount = getPara("usd_totalAmount");
		String hkd_totalAmount = getPara("hkd_totalAmount");
		String jpy_totalAmount = getPara("jpy_totalAmount");
		
		String sql = "SELECT cur.name currency_name ,p.phone,p.contact_person,p.address,p.company_name,cpoa.sp_id,cpoa.order_id"
				+ " FROM custom_plan_order_arap cpoa"
				+ " LEFT JOIN currency cur on cur.id = cpoa.currency_id"
				+ " LEFT JOIN custom_plan_order cpo on cpo.id=cpoa.order_id "
				+ " left join party p on p.id = cpo.application_unit "
				+ " WHERE cpoa.order_id in("+ ids +")"
				+ " group by cpoa.order_id";
		Record rec =Db.findFirst(sql);
		rec.set("total_amount", total_amount);
		rec.set("jpy", jpy_totalAmount);
		rec.set("cny", cny_totalAmount);
		rec.set("usd", usd_totalAmount);
		rec.set("hkd", hkd_totalAmount);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("user", LoginUserController.getLoginUserName(this));
		rec.set("itemList", getItemList(ids,""));
//		rec.set("currencyList", getCurrencyList(ids,""));
		setAttr("order",rec);
		render("/eeda/cmsArap/cmsChargeCheckOrder/cmsChargeCheckOrderEdit.html");
	}
	
	
    @Before(EedaMenuInterceptor.class)
    public void edit(){
		String id = getPara("id");//custom_arap_charge_order id
		String condition = "select ref_order_id from custom_arap_charge_item where custom_charge_order_id ="+id;
		
		String sql = " select aco.*,p.company_name,p.contact_person,p.phone,p.address,u.c_name creator_name,u1.c_name confirm_by_name from custom_arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record rec =Db.findFirst(sql,id);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("itemList", getItemList(condition,id));
//		rec.set("currencyList", getCurrencyList(condition,id));
		setAttr("order",rec);
		render("/eeda/cmsArap/cmsChargeCheckOrder/cmsChargeCheckOrderEdit.html");
	}

    
    @Before(Tx.class)
	public void exchange_currency(){
	    String chargeOrderId = getPara("custom_charge_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		renderJson(exchangeTotalMap);
	}
    
    private Map<String, Double> updateExchangeTotal(String chargeOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select aci.ref_order_id from custom_arap_charge_item aci where aci.custom_charge_order_id="+chargeOrderId+")";
		
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
		
		Record order = Db.findById("custom_arap_charge_order", chargeOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("custom_arap_charge_order", order);
		return exchangeTotalMap;
    }
    
    
    
	//异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	String condition = "select ref_order_id from custom_arap_charge_item where custom_charge_order_id ="+order_id;
    	list = getItemList(condition,order_id);

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
		CustomArapChargeOrder caco = CustomArapChargeOrder.dao.findById(id);
		caco.set("status","已确认");
		caco.set("confirm_stamp", new Date());
		caco.set("confirm_by", LoginUserController.getLoginUserId(this));
		caco.update();
		Record r = caco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(caco.getLong("confirm_by")));
		renderJson(r);
	}
    
   

}
