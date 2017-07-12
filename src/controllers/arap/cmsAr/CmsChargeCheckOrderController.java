package controllers.arap.cmsAr;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.Party;
import models.UserLogin;
import models.eeda.cms.CustomArapChargeItem;
import models.eeda.cms.CustomArapChargeOrder;
import models.eeda.cms.CustomArapChargeReceiveItem;
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
import controllers.util.PoiUtils;

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
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("BGYSDZ", user.getLong("office_id")));
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
			+" SELECT cpo.order_no,cpoa.order_type ,cpoa.id arap_id,cpo.id order_id,cpo.date_custom,cpo.tracking_no,p.abbr sp_name,f.name fin_name,cpoa.amount, cpoa.price, "
			 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
			 +" from custom_plan_order_arap cpoa "
			 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
			 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
			 +" LEFT JOIN party p on p.id = cpoa.sp_id "
			 +" where 1 = 1 "
			 + checkCondition
			 +" and cpo.delete_flag='N' "
			 + " and cpoa.audit_flag='Y' and cpoa.bill_flag='N'  and (cpo.office_id = "+office_id+ " or cpo.to_office_id="+office_id+")"
			 +"  GROUP BY cpoa.id " 
			 + " ) B "
			 +" where 1=1 ";

        
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  );
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
        		+ " select if(aco.audit_status IS NULL ,aco.status,aco.audit_status) as real_status,"
        		+ "aco.*, p.abbr sp_name,ul.c_name creator_name,ul2.c_name confirm_name "
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
        String ordersql=sql+ condition  +sLimit;
        List<Record> orderList = Db.find(ordersql);
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
			 sql = "SELECT cpo.order_no,cpoa.id id,cpo.id order_id,cpo.date_custom,cpo.tracking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
		   				 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
		   				 +" from custom_plan_order_arap cpoa "
		   				 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
		   				 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
		   				 +" LEFT JOIN party p on p.id = cpoa.sp_id "
		   				 +" where cpoa.id in("+ids+")"
		   				 +" and cpo.delete_flag='N' "
		   				 + "";
				}else{
			   		 sql = "SELECT cpo.order_no,cpoa.id id,cpo.id order_id,cpo.date_custom,cpo.tracking_no,p.abbr abbr_name,f.name fin_name,cpoa.amount, cpoa.price, "
		   				 +" IF(cpoa.currency_id = 3,'人民币','') currency_name,cpoa.total_amount,cpoa.remark,cpo.customs_billCode,cpo.create_stamp "
		   				 +" from custom_plan_order_arap cpoa "
		   				 +" left join custom_arap_charge_item caci on caci.ref_order_id = cpoa.id"
		   				 +" LEFT JOIN custom_plan_order cpo on cpo.id = cpoa.order_id "
		   				 +" LEFT JOIN fin_item f on f.id = cpoa.charge_id "
		   				 +" LEFT JOIN party p on p.id = cpoa.sp_id "
		   				 +" where caci.custom_charge_order_id ="+order_id
		   				 +" and cpo.delete_flag='N' "
		   				 ;
						}	
    	List<Record> re = Db.find(sql);
    	return re;
    }
    //报关例外，获取每次收款的记录
    public List<Record> getReceiveItemList(String order_id){
    	String sql = null;
   		 sql = "  SELECT caci.*,c.`name` currency_name ,ul.c_name receive_name"
   				+" from custom_arap_charge_order caco  "
   				+" left join custom_arap_charge_receive_item caci on caci.custom_charge_order_id = caco.id "
   				+" LEFT JOIN currency c on c.id=caci.currency_id "
   				+ "  LEFT JOIN user_login ul ON ul.id = caci.confirm_by"
   				+" where caci.custom_charge_order_id ="+order_id+" order by caci.id desc";
    	List<Record> re = Db.find(sql);
    	return re;
    }
    
    @Before(EedaMenuInterceptor.class)
	public void create(){
		String ids = getPara("idsArray");//custom_plan_order_arap ids
		String total_amount = getPara("totalAmount");
		
		String sql = "SELECT p.phone,p.contact_person,p.address,p.company_name ,cpoa.sp_id"
				+ " FROM custom_plan_order_arap cpoa"
				+ " LEFT JOIN custom_plan_order cpo on cpo.id=cpoa.order_id "
				+ " left join party p on p.id = cpoa.sp_id "
				+ " WHERE cpoa.id in("+ ids +")"
				 +" and cpo.delete_flag='N' "
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
		Long sp_id = order.getLong("sp_id");
		UserLogin ul = UserLogin.dao.findById(create_by);
		UserLogin ul2 = UserLogin.dao.findById(confirm_by);
		UserLogin u3=LoginUserController.getLoginUser(this);
		
		Party sp = Party.dao.findById(sp_id);
		
		String sqlString="SELECT  residual_cny FROM custom_arap_charge_receive_item WHERE custom_charge_order_id="+id+" ORDER BY id DESC";
		Record rec2 = Db.findFirst(sqlString);
		Record rec = order.toRecord(); 
		rec.set("user", u3);
		rec.set("creator_name", ul.getStr("c_name"));
		rec.set("confirm_name", ul2==null?"":ul2.getStr("c_name"));//
		rec.set("company_name", sp==null?"":sp.getStr("company_name"));//
		rec.set("sp_name", sp==null?"":sp.getStr("abbr"));//
		rec.set("itemList", getItemList("",id));
		
		rec.set("receive_itemList", getReceiveItemList(id));
		if(rec2!=null){
			rec.set("residual_cny", rec2.get("residual_cny"));
		}
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
		Db.update("update custom_plan_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		renderJson(exchangeTotalMap);
	}
    
    @Before(Tx.class)
    private Map<String, Double> updateExchangeTotal(String chargeOrderId) {
        String sql="select cpoa.order_type,  cur.NAME exchange_currency_name, "
        +"       ifnull(cpoa.exchange_total_amount, cpoa.total_amount) exchange_total_amount "
        +"       from  custom_plan_order_arap cpoa "
        +"       LEFT JOIN currency cur ON cur.id = cpoa.currency_id"
        +"       where cpoa.id in (select aci.ref_order_id from custom_arap_charge_item aci where aci.custom_charge_order_id="+chargeOrderId+")";
		
		Map<String, Double> exchangeTotalMap = new HashMap<String, Double>();
		exchangeTotalMap.put("total_amount", 0d);
//		exchangeTotalMap.put("USD", 0d);
//		exchangeTotalMap.put("HKD", 0d);
//		exchangeTotalMap.put("JPY", 0d);
		
		List<Record> resultList= Db.find(sql);
		for (Record rec : resultList) {
            String name = "total_amount";
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
    	
     	List<Record> list = null;
    	String condition = "select ref_order_id from custom_arap_charge_item where custom_charge_order_id in ("+order_ids+") ";
    	
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
    		    list = getItemList(condition,order_id);
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
		CustomArapChargeOrder caco = CustomArapChargeOrder.dao.findById(id);
		caco.set("status","已确认");
		caco.set("confirm_stamp", new Date());
		caco.set("confirm_by", LoginUserController.getLoginUserId(this));
		caco.update();
		
		//设置y，已生成对账单o
		String itemList=caco.get("ref_order_id");
		String sql="UPDATE custom_plan_order_arap cpoa set billConfirm_flag='Y' "
					+"where cpoa.id in (select aci.ref_order_id FROM custom_arap_charge_item aci where custom_charge_order_id="+id+" )";
		Db.update(sql);
				
		Record r = caco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(caco.getLong("confirm_by")));
		renderJson(r);
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
			query_exchange_currency=" and cpoa. exchange_currency_id="+re.get(0).get("id");
		}
		if(StringUtils.isNotEmpty(fin_name)){
			query_fin_name=" and fi.id="+fin_name;
		}
			if("create".equals(bill_flag)){
				sql = " select cpoa.*,aco.order_no check_order_no,cpo.customs_billcode ,cpo.order_no,cpo.create_stamp,cpo.carrier customer_id,cpo.type,  "
						+" 							 p.abbr sp_name,p1.abbr customer_name, "
						+" 							 fi.name fin_name, "
						+" 							 cur.name currency_name "
						+" 							 from custom_plan_order cpo "
						+" 							 left join custom_plan_order_arap cpoa on cpo.id=cpoa.order_id "
						+" 							 left join fin_item fi on cpoa.charge_id = fi.id "
						+" 							 left join custom_plan_order_shipping_item josi on josi.order_id=cpoa.order_id "
						+" 							 left join party p on p.id=cpoa.sp_id "
						+" 							 left join party p1 on p1.id=cpo.carrier "
						+" 							 left join currency cur on cur.id=cpoa.currency_id "
						+" 							 left join custom_charge_application_order_rel caol on caol.custom_plan_order_arap_id  = cpoa.id "
						+" 							 left join custom_arap_charge_application_order acao on caol.application_order_id = acao.id "
						+" 							  left join custom_arap_charge_order aco on aco.id=caol.custom_charge_order_id "
						+" 						   where acao.id="+order_ids+query_fin_name
						 +" and cpo.delete_flag='N' "
						 +" 							 GROUP BY cpoa.id "
						+" 							 ORDER BY aco.order_no, cpo.order_no";
				
			}else{
				sql = "  select cpoa.*,aco.order_no check_order_no, jo.order_no,jo.customs_billcode , "
						+" jo.create_stamp,jo.carrier customer_id,jo.type,  "
						+" 							 p.abbr sp_name,p1.abbr customer_name, "
						+" 							  fi.name fin_name, "
						+" 							 cur.name currency_name "
						+" 							 from custom_plan_order jo "
						+" 							 left join custom_plan_order_arap cpoa on jo.id=cpoa.order_id "
						+" 							 left join fin_item fi on cpoa.charge_id = fi.id "
						+" 							 left join custom_plan_order_shipping_item josi on josi.order_id=cpoa.order_id "
						+" 							 left join party p on p.id=cpoa.sp_id "
						+" 							 left join party p1 on p1.id=jo.carrier "
						+" 							 left join currency cur on cur.id=cpoa.currency_id "
						+" 							 left join custom_arap_charge_item aci on aci.ref_order_id = cpoa.id "
						+" 						  left join custom_arap_charge_order aco on aco.id = aci.custom_charge_order_id "
						+" 						  where cpoa.id = aci.ref_order_id and cpoa.create_flag='N' and aco.id in ("+order_ids+")"
							+currency_code
							+query_exchange_currency+query_fin_name
							 +" and jo.delete_flag='N' "
							 +" GROUP BY cpoa.id"
							+" ORDER BY aco.order_no, jo.order_no";
			}		
			

    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
    
    
    //收款确认
   	@Before(Tx.class)
 	public void confirmOrder(){
		   		 UserLogin user = LoginUserController.getLoginUser(this);
		   		String jsonStr=getPara("params");
		  
		        	Gson gson = new Gson();  
		         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
		         String id=(String)dto.get("custom_charge_order_id");
		   		
		   		String pay_remark=(String) dto.get("pay_remark");
		   		String itemids= (String) dto.get("itemids");
		   		CustomArapChargeReceiveItem cacritem=new CustomArapChargeReceiveItem();
		   		String receive_time = (String) dto.get("receive_time");
		   		String deposit_bank = "";
		     	String payment_method = (String) dto.get("payment_method");
		     	String empty = (String) dto.get("deposit_bank");
		     	
		     	if(StringUtils.isNotEmpty(empty)){
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
                
         				CustomArapChargeOrder arapChargeOrder = CustomArapChargeOrder.dao.findById(id);
         				//求每张应收对账单的每次收款金额记录表，求已收的总额
         				String sql1 =" SELECT	IFNULL( SUM(aci.receive_cny),0) paid_cny, "
         						+" 	IFNULL( SUM(aci.receive_jpy),0) paid_jpy, "
         						+" 	IFNULL( SUM(aci.receive_usd),0) paid_usd, "
         						+" 	IFNULL( SUM(aci.receive_hkd),0) paid_hkd "
         						+" FROM	custom_arap_charge_receive_item aci "
         						+" WHERE	aci.custom_charge_order_id = "+id;
                            
                         Record r1 = Db.findFirst(sql1);                  
                         Double paid_cny = r1.getDouble("paid_cny");     
                         Double paid_usd = r1.getDouble("paid_usd");
                         Double paid_jpy = r1.getDouble("paid_jpy");
                         Double paid_hkd = r1.getDouble("paid_hkd");
                         
                         	//求每张应收对账单的总金额
                         String sql = "SELECT "
                         		+" IFNULL((SELECT SUM(cpoa.total_amount) from  custom_plan_order_arap cpoa LEFT JOIN custom_arap_charge_item aci on cpoa.id = aci.ref_order_id"
                 				+" where  cpoa.currency_id =3 and aci.custom_charge_order_id="+id
                 				+" ),0) cny,"
                 				+" IFNULL((SELECT SUM(cpoa.total_amount) from  custom_plan_order_arap cpoa LEFT JOIN custom_arap_charge_item aci on cpoa.id = aci.ref_order_id"
                 				+" where  cpoa.currency_id =6 and aci.custom_charge_order_id="+id
                 				+" ),0) usd,"
                 				+" IFNULL((SELECT SUM(cpoa.total_amount) from  custom_plan_order_arap cpoa LEFT JOIN custom_arap_charge_item aci on cpoa.id = aci.ref_order_id"
                 				+" where  cpoa.currency_id =8 and aci.custom_charge_order_id="+id
                 				+" ),0) jpy,"
                 				+" IFNULL((SELECT SUM(cpoa.total_amount) from  custom_plan_order_arap cpoa LEFT JOIN custom_arap_charge_item aci on cpoa.id = aci.ref_order_id"
                 				+" where  cpoa.currency_id =9 and aci.custom_charge_order_id="+id
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
         					Db.update("update custom_plan_order_arap set pay_flag = 'Y' where id in ("+itemids+")");
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
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CUSTOMCHARGE);
        auditLog.set("currency_code", currency_code);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("office_id", office_id);
        if(receive_bank_id!=null && !("").equals(receive_bank_id)){
        		auditLog.set("account_id", receive_bank_id);
        	}else{
        		Record rLog = Db.findFirst("select * from fin_account where bank_name = '现金' and office_id = "+office_id);
        		if(rLog!=null){
        			String account_id = rLog.getLong("id").toString();
	        		if(StringUtils.isNotEmpty(account_id)){
	        			auditLog.set("account_id", account_id);
	        		}
        		}
        		
        	}
        auditLog.set("source_order", "报关应收对账单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
    }
	
	@Before(Tx.class)
	public void deleteChargeItem(){
    	String chargeOrderId=getPara("order_id");
    	String itemid=getPara("charge_itemid");
    	if(itemid !=null&& chargeOrderId!=null){
    		CustomPlanOrderArap cusPlanOrderArap = CustomPlanOrderArap.dao.findById(itemid);
    		cusPlanOrderArap.set("bill_flag", "N");
//    		cusPlanOrderArap.set("hedge_flag", "N");
    		cusPlanOrderArap.update();
             Db.deleteById("custom_arap_charge_item","ref_order_id,custom_charge_order_id",itemid,chargeOrderId);
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
    			exchangeTotalMap.put("customChargeOrderId", Double.parseDouble(chargeOrderId));
    	    	renderJson(exchangeTotalMap);
    } 
	
	public void insertChargeItem(){
    	String itemList= getPara("charge_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String customChargeOrderId=getPara("order_id");
    	CustomArapChargeItem aci = null;
    	
    	if(customChargeOrderId != null){
    		for(String itemId:itemArray){
    			aci = new CustomArapChargeItem();
    			CustomPlanOrderArap cusPlanArap = CustomPlanOrderArap.dao.findById(itemId);

    			cusPlanArap.set("bill_flag", "Y");
	             String hedge_order_type = cusPlanArap.getStr("order_type");
	             if("cost".equals(hedge_order_type)){
	            	 cusPlanArap.set("hedge_flag", "Y");
	               }
	             cusPlanArap.update();
				aci.set("ref_order_id", itemId);
				aci.set("custom_charge_order_id", customChargeOrderId);
				aci.save();
//        	String sql="INSERT into arap_charge_item (ref_order_id,charge_order_id) "
//        				+ "VALUES ("+itemId+","+order_id+")";
    		}
    		
    	}
    	//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(customChargeOrderId);
		exchangeTotalMap.put("customChargeOrderId", Double.parseDouble(customChargeOrderId));
		
    	renderJson(exchangeTotalMap);

    }
	
	//导出excel对账单
		public void downloadExcelList(){
			String order_id = getPara("id");
			String sp_name = getPara("sp_name");
//			String sql = list();
			String sql_fin_name =" SELECT GROUP_CONCAT(DISTINCT f. NAME,':',f.id)  fin_name_id "
					+" FROM  custom_plan_order_arap cpoa "
					+" LEFT JOIN custom_arap_charge_item caci ON caci.ref_order_id = cpoa.id  "
					+" LEFT JOIN custom_plan_order cpo ON cpo.id = cpoa.order_id "
					+" LEFT JOIN fin_item f ON f.id = cpoa.charge_id "
					+" WHERE caci.custom_charge_order_id = "+order_id
					+" AND cpo.delete_flag = 'N'  ";
			Record re_fin_name_id = Db.findFirst(sql_fin_name);
			String fin_name_id_stirng = re_fin_name_id.getStr("fin_name_id");
			String [] fin_name_id = fin_name_id_stirng.split(",");
			String sql_name_combina = "";
			ArrayList<String> list = new ArrayList<String>();
			ArrayList<String> list_id = new ArrayList<String>();
			ArrayList<String> list_name = new ArrayList<String>();

			list_id.add("DATE_CUSTOM");
			list_id.add("ORDER_NO");
			list_id.add("ABBR_NAME");
			list_id.add("CUSTOMS_BILLCODE");
			
			for(String fin_name_id_single : fin_name_id){
				 String [] fin_name_id_separate = fin_name_id_single.split(":");
				 sql_name_combina += "if(f.name = '"+fin_name_id_separate[0] +"' and cpoa.order_type = 'charge',cpoa.total_amount,0) "+fin_name_id_separate[1]+"_total_amount,";
				 				 
			     list_name.add(fin_name_id_separate[0]);
	 
				 list.add("SUM("+fin_name_id_separate[1]+"_total_amount) SUM_"+fin_name_id_separate[1]+"_total_amount");
				 list_id.add("SUM_"+fin_name_id_separate[1]+"_total_amount");
				 
			}
			
//			//去除重复值  
//			HashSet list_remove_repeat=new HashSet(list);
//			HashSet list_id_remove_repeat=new HashSet(list_id);
//			HashSet list_name_remove_repeat=new HashSet(list_name);
			
			
			String sum_sql_total = "";
			String head_id_sql_total = "";
			String head_name_sql_total = "";
			
			sum_sql_total = StringUtils.join(list.toArray(), ",");
			head_id_sql_total = StringUtils.join(list_id.toArray(), ",");
			head_name_sql_total = StringUtils.join(list_name.toArray(), ",");
	
			
			String sqlExport = "SELECT "
							+"	date_custom, "
							+"	order_no, "
							+"	abbr_name, "
							+"	customs_billCode, "
							+"	create_stamp, "
							+sum_sql_total
							+" FROM "
							+"	( "
							+"		SELECT "
							+"			cpo.date_custom, "
							+"			cpo.order_no, "
							+"			p.abbr abbr_name, "
							+"			cpo.customs_billCode, "							
							+sql_name_combina
							+"      cpo.create_stamp "
							+" FROM "
							+"	custom_plan_order_arap cpoa "
							+" LEFT JOIN custom_arap_charge_item caci ON caci.ref_order_id = cpoa.id  "
							+" LEFT JOIN custom_plan_order cpo ON cpo.id = cpoa.order_id "
							+" LEFT JOIN fin_item f ON f.id = cpoa.charge_id "
							+" LEFT JOIN party p ON p.id = cpoa.sp_id "
							+" WHERE "
							+"	caci.custom_charge_order_id = "+order_id
							+" AND cpo.delete_flag = 'N' "
							+" GROUP BY "
							+"	cpoa.id "
							+"	) A "
							+" GROUP BY "
							+"	order_no  ";
			String total_name_header = "报关日期, 申请单号, 公司名称, 报关单号,"+head_name_sql_total;
			String[] headers = total_name_header.split(",");
			
			String[] fields = head_id_sql_total.split(",");
			String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,sp_name);
			renderText(fileName);
		}
}
