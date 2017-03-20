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
   			order.set("check_amount", dto.get("total_amount"));
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSDZ", user.getLong("office_id")));
   			order.set("check_amount", dto.get("total_amount"));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.set("office_id", office_id);
   			order.save();
   			
   			id = order.getLong("id").toString();

   			CustomArapChargeItem aci = null;
   			List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
   			for(Map<String, String> item :itemList){
   				aci = new CustomArapChargeItem();
				aci.set("ref_order_type", "报关申请单");
				aci.set("ref_order_id", item.get("id"));
				aci.set("custom_charge_order_id", id);
				aci.save();
				
				Db.update("update custom_plan_order_arap set bill_flag = 'Y' where id = ?",item.get("id"));
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
        String checkCondition = "";
        if(!"Y".equals(checked)){
        	checkCondition = "and cpoa.order_type='charge'";
        }

		sql = "select B.* from(  "
			+" SELECT cpo.order_no,cpoa.order_type ,cpoa.id arap_id,cpo.id order_id,cpo.date_custom,cpo.booking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
			 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
			 +" from custom_plan_order_arap cpoa "
			 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
			 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
			 +" LEFT JOIN party p on p.id = cpoa.sp_id "
			 +" where 1 = 1 "
			 + checkCondition
			 + " and cpoa.audit_flag='Y' and cpoa.bill_flag='N'  and cpo.office_id = "+office_id
			 +"  GROUP BY cpoa.id " 
			 + " ) B "
			 +" where 1=1 ";

        
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
        		+ " select aco.*, p.abbr sp_name,ul.c_name creator_name,ul2.c_name confirm_name "
				+ " from custom_arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " left join user_login ul on ul.id = aco.create_by"
				+ " left join user_login ul2 on ul2.id = aco.confirm_by"
				+ " where aco.office_id = "+office_id
				+ " group by aco.id ORDER BY  aco.create_stamp desc ) B where 1=1 ";
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
			 sql = "SELECT cpo.order_no,cpoa.id id,cpo.id order_id,cpo.date_custom,cpo.booking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
		   				 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
		   				 +" from custom_plan_order_arap cpoa "
		   				 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
		   				 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
		   				 +" LEFT JOIN party p on p.id = cpoa.sp_id "
		   				 +" where cpoa.id in("+ids+")";
				}else{
			   		 sql = "SELECT cpo.order_no,cpoa.id id,cpo.id order_id,cpo.date_custom,cpo.booking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
		   				 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
		   				 +" from custom_plan_order_arap cpoa "
		   				 +" left join custom_arap_charge_item caci on caci.ref_order_id = cpoa.id"
		   				 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
		   				 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
		   				 +" LEFT JOIN party p on p.id = cpoa.sp_id "
		   				 +" where caci.custom_charge_order_id ="+order_id;
						}	
    	List<Record> re = Db.find(sql);
    	return re;
    }

    
    @Before(EedaMenuInterceptor.class)
	public void create(){
		String ids = getPara("idsArray");//job_order_arap ids
		String total_amount = getPara("totalAmount");
		
		String sql = "SELECT p.phone,p.contact_person,p.address,p.company_name declare_unit,cpo.application_unit declare_unit_id,cpoa.sp_id"
				+ " FROM custom_plan_order_arap cpoa"
				+ " LEFT JOIN custom_plan_order cpo on cpo.id=cpoa.order_id "
				+ " left join party p on p.id = cpo.application_unit "
				+ " WHERE cpoa.id in("+ ids +")"
				+ " group by cpoa.order_id";
		Record rec =Db.findFirst(sql);
		rec.set("total_amount", total_amount);
		rec.set("itemList", getItemList(ids,""));
		setAttr("order",rec);
		render("/eeda/cmsArap/cmsChargeCheckOrder/cmsChargeCheckOrderEdit.html");
	}
	
	
    @Before(EedaMenuInterceptor.class)
    public void edit(){
		String id = getPara("id");

		CustomArapChargeOrder order = CustomArapChargeOrder.dao.findById(id);
		Long create_by = order.getLong("create_by");
		Long confirm_by = order.getLong("confirm_by");
		UserLogin ul = UserLogin.dao.findById(create_by);
		UserLogin ul2 = UserLogin.dao.findById(confirm_by);
		
		Record rec = order.toRecord(); 
		rec.set("creator_name", ul.getStr("c_name"));
		rec.set("confirm_name", ul2==null?"":ul2.getStr("c_name"));
		rec.set("itemList", getItemList("",id));
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
    	String order_ids = getPara("order_ids");
    	String order_id = getPara("order_id");
    	String appliction_id = getPara("appApplication_id");
    	String bill_flag = getPara("bill_flag");
    	String currency_code=getPara("query_currency");
    	//查询结算币制
    	String  exchange_currency=getPara("query_exchange_currency");
    	String  fin_name=getPara("query_fin_name");
    	
//    	List<Record> list = null;
//    	String condition = "select ref_order_id from custom_arap_charge_item where custom_charge_order_id ="+order_id;
     	List<Record> list = null;
    	String condition = "select ref_order_id from custom_arap_charge_item where custom_charge_order_id in ("+order_ids+") ";
//    	list = getItemList(condition,order_id);
    	
    	if("N".equals(order_id)){//应收申请单
    		if(StringUtils.isNotEmpty(appliction_id)){
    			list = getItemList("",appliction_id);
        	}else{
	    		if("".equals(order_ids)){
	    			order_ids=null;
	    				}
	    		list = getItemList(condition,"");
	    		}
    	}else{//应收对账单
    		    list = getItemList(condition,order_id);
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
