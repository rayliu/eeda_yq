package controllers.tradeArap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AppInvoiceDoc;
//import models.ChargeAppOrderRel;
import models.Office;
import models.Party;
import models.UserLogin;
import models.eeda.tr.tradeJoborder.TradeArapAccountAuditLog;
import models.eeda.tr.tradeJoborder.TradeArapChargeApplicationOrder;
import models.eeda.tr.tradeJoborder.TradeArapChargeOrder;
import models.eeda.tr.tradeJoborder.TradeChargeApplicationOrderRel;
import models.eeda.tr.tradeJoborder.TradeJobOrderArap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.eeda.SysInfoController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TradeChargeRequestController extends Controller {
    private Log logger = Log.getLog(TradeChargeRequestController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	String back =getPara("back");
    	setAttr("back",back);
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/tradeChargeRequest");
        setAttr("listConfigList", configList);
    	render("/tradeArap/ChargeRequest/ChargeRequestList.html");
    }
    
    @Before(EedaMenuInterceptor.class) 
    public void create() {
		render("/tradeArap/ChargeRequest/create.html");
	}
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = " select * from ("
        		+ " select  aco.*, p.company_name sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount,"
        		+ " sum(ifnull(c.paid_usd,0)) paid_usd,"
        		+ " sum(ifnull(c.paid_cny,0)) paid_cny,"
        		+ " sum(ifnull(c.paid_hkd,0)) paid_hkd,"
        		+ " sum(ifnull(c.paid_jpy,0)) paid_jpy,"
        		+ " group_concat((select concat(order_no,'-',status) from trade_arap_charge_application_order where id = c.application_order_id) SEPARATOR '<br/>') app_msg"
				+ " from trade_arap_charge_order aco "
				+ " left join trade_charge_application_order_rel c on c.charge_order_id=aco.id"
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.status!='新建' and aco.office_id = "+office_id
				+ " group by aco.id"
				+ " ) A where (ifnull(usd,0)>paid_usd or ifnull(cny,0)>paid_cny or ifnull(hkd,0)>paid_hkd or ifnull(jpy,0)>paid_jpy)";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition +") B";
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
    
    @Before(EedaMenuInterceptor.class)
    public void OrderList() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = " SELECT "
        		+" 	* "
        		+" FROM "
        		+" 	( "
        		+" 		SELECT "
        		+" 			aco.*, p.abbr sp_name, "
        		+" 			IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount)-(IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 3 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'charge' "
        		+" 					AND joa.exchange_currency_id = 3 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			) paid_cny, "
        		+" 			IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount)-(IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa  "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 6 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'charge' "
        		+" 					AND joa.exchange_currency_id = 6 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			) paid_usd, "
        		+" 			IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount)-(IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 8 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y' AND joa.order_type = 'charge' "
        		+" 					AND joa.exchange_currency_id = 8 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			) paid_jpy, "
        		+" 			IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount)-(IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y'  AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 9 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						trade_job_order_arap joa "
        		+" 					LEFT JOIN trade_arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.create_flag = 'Y'  AND joa.order_type = 'charge' "
        		+" 					AND joa.exchange_currency_id = 9 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			) paid_hkd, "
        		+" 			group_concat( "
        		+" 				DISTINCT ( "
        		+" 					SELECT "
        		+" 						concat(order_no, '-', STATUS) "
        		+" 					FROM "
        		+" 						trade_arap_charge_application_order "
        		+" 					WHERE "
        		+" 						id = c.application_order_id "
        		+" 				) SEPARATOR '<br/>' "
        		+" 			) app_msg "
        		+" 		FROM "
        		+" 			trade_arap_charge_order aco "
        		+" 		LEFT JOIN trade_charge_application_order_rel c ON c.charge_order_id = aco.id "
        		+" 		LEFT JOIN party p ON p.id = aco.sp_id "
        		+" 		WHERE "
        		+" 			aco. STATUS != '新建' "
        		+" 		AND aco.office_id = "+office_id+" "
        		+" 		GROUP BY "
        		+" 			aco.id "
        		+" 	) A "
        		+" WHERE "
        		+" 	( "
        		+" 		(ifnull(usd, 0)-paid_usd > 0.01) "
        		+" 		OR (ifnull(cny, 0)-paid_cny > 0.01) "
        		+" 		OR (ifnull(hkd, 0)-paid_hkd > 0.01) "
        		+" 		OR (ifnull(jpy, 0)-paid_jpy > 0.01) "
        		+" 	) ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition +") B";
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
    
    
    @Before(EedaMenuInterceptor.class) 
    public void billDetailed() {
		String ids = getPara("idsArray");
		setAttr("ids", ids);
		
		String[] orderArrId=ids.split(",");
		Record r = Db.findFirst("select aco.sp_id,ifnull(company_name,company_name_eng) payee_unit,ifnull(contact_person,contact_person_eng) payee_name"
				+ " from trade_arap_charge_order aco left join party p on p.id = aco.sp_id where aco.id = ?",orderArrId[0]);

		Record re = new Record();
		re.set("sp_id", r.getLong("SP_ID"));
		re.set("payee_unit", r.getStr("PAYEE_UNIT"));
		re.set("payee_name", r.getStr("PAYEE_NAME"));
		setAttr("order", re);
		
		String sql="select group_concat(cast(ref_order_id as char) SEPARATOR ',') selected_item_ids"
                + " from trade_arap_charge_item where charge_order_id in("+ids+")";
        String selected_item_ids = Db.findFirst(sql).getStr("selected_item_ids");
        setAttr("selected_item_ids", selected_item_ids);
			
		render("/tradeArap/ChargeRequest/chargeEdit_select_item.html");
	}
    
    
    
    
    public void applicationList() {
//    	String sLimit = "";
//        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select p.abbr payee_company,acao.*,uche.c_name check_name,ucom.c_name confirm_name, acao.order_no application_order_no,CAST(CONCAT(acao.begin_time,'到',acao.end_time) AS CHAR) service_stamp, "
        		+ " '申请单' order_type,aco.order_no charge_order_no,u.c_name "
				+ " from trade_arap_charge_application_order acao "
				+ " left join trade_charge_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join trade_arap_charge_order aco on aco.id = caor.charge_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ " left join user_login uche on uche.id = acao.check_by"
				+ " left join user_login ucom on ucom.id = acao.confirm_by"
				+ " LEFT JOIN party p on p.id=acao.sp_id"
				+ "	where (acao.office_id = "+office_id+ " or acao.office_id in (select office_id from user_office where user_name='"+ user.getStr("user_name")+ "' ))"
				+ " group by acao.id"
				+ " order by acao.create_stamp desc"
				+ " ) B where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition );//+sLimit
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }

  	//新逻辑
  	public void chargeOrderList() {
          String ids = getPara("ids");
          String application_id = getPara("application_id");
          String sql = "";
          
          if("".equals(application_id)||application_id==null){
  	       
          	sql =  " SELECT aco.*, p.company_name payee_name, '应收对账单' order_type, "
          			+" p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
          			+" (aco.usd-ifnull(c.paid_usd, 0)) wait_usd,"
          			+" (aco.cny-ifnull(c.paid_cny, 0)) wait_cny,"
          			+"     (aco.jpy-ifnull(c.paid_jpy, 0)) wait_jpy,"
          			+"     (aco.hkd-ifnull(c.paid_hkd, 0)) wait_hkd,"
          			+"     case "
          			+"         when c.charge_order_id is not null then"
          			+"             ifnull("
          			+"                    ( SELECT sum(exchange_total_amount) FROM trade_job_order_arap"
          			+"                                 WHERE id IN(SELECT ref_order_id FROM trade_arap_charge_item aci WHERE charge_order_id IN(c.charge_order_id) ) "
          			+"                                     AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'USD' ) "
          			+"                                     AND pay_flag = 'Y' "
          			+"                     ),0) "
          			+"     else aco.usd "
          			+"     end  apply_pay_usd, "
          			+"     case  "
          			+"         when c.charge_order_id is not null then "
          			+"             ifnull( "
          			+"                    ( SELECT sum(exchange_total_amount) FROM trade_job_order_arap "
          			+"                        WHERE id IN( SELECT ref_order_id FROM trade_arap_charge_item WHERE charge_order_id IN(c.charge_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'CNY' ) "
          			+"                             AND pay_flag = 'Y' "
          			+"                     ),0) "
          			+"     else aco.cny "
          			+"     end  apply_pay_cny, "
          			+"     case  "
          			+"         when c.charge_order_id is not null then "
          			+"             ifnull( "
          			+"                     ( SELECT sum(exchange_total_amount) FROM trade_job_order_arap "
          			+"                         WHERE id IN( SELECT ref_order_id FROM trade_arap_charge_item WHERE charge_order_id IN(c.charge_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'JPY' ) "
          			+"                             AND pay_flag = 'Y' "
          			+"                     ), 0 ) "
          			+"     else aco.jpy "
          			+"     end  apply_pay_jpy, "
          			+"     case  "
          			+"         when c.charge_order_id is not null then "
          			+"             ifnull( "
          			+"                     ( SELECT sum(exchange_total_amount) FROM trade_job_order_arap "
          			+"                         WHERE id IN( SELECT ref_order_id FROM trade_arap_charge_item WHERE charge_order_id IN(c.charge_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'HKD' )" 
          			+"                             AND pay_flag = 'Y' "
          			+"                 ), 0 ) "
          			+"     else aco.hkd "
          			+"     end  apply_pay_hkd "
          			+" FROM trade_arap_charge_order aco "
          			+" LEFT JOIN trade_charge_application_order_rel c on c.charge_order_id = aco.id"
          			+" LEFT JOIN party p ON p.id = aco.sp_id"
          			+" LEFT JOIN user_login ul ON ul.id = aco.create_by"
          			+" WHERE aco.id in(" +ids +")"
          			+" group by aco.id ";

  		}else{
  			sql = " SELECT aco.*, p.company_name payee_name, '应收对账单' order_type,"
					+"  p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
					+"  ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap " 
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" ))"  
					+" and exchange_currency_id = (select id from currency where code='USD')"
					+" and pay_flag='Y' ),0) apply_pay_usd, "
					+" ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='CNY') "
					+" and pay_flag='Y'),0) apply_pay_cny,"
					+"  ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='JPY') "
					+" and pay_flag='Y'),0) apply_pay_jpy,"
					+"  ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" ))"  
					+" and exchange_currency_id = (select id from currency where code='HKD') "
					+" and pay_flag='Y'),0) apply_pay_hkd,"
					+"  ( "
					+"  aco.usd - ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='USD')"
					+" and pay_flag='Y' ),0) "
					+" ) wait_usd, "
					+" ( "
					+" aco.cny - ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='CNY') "
					+" and pay_flag='Y'),0) "
					+" ) wait_cny, "
					+" ( "
					+" aco.jpy - ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='JPY') "
					+" and pay_flag='Y'),0) "
					+" ) wait_jpy, "
					+" ( "
					+" aco.hkd - ifnull((SELECT sum(exchange_total_amount) from trade_job_order_arap  "
					+" where id in(select ref_order_id from trade_arap_charge_item where charge_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='HKD') "
					+" and pay_flag='Y'),0) "
					+" ) wait_hkd"
					+"  FROM trade_arap_charge_order aco "
					+"  LEFT JOIN trade_charge_application_order_rel caor on caor.charge_order_id = aco.id"
					+"  LEFT JOIN trade_arap_charge_application_order acao on acao.id = caor.application_order_id"
					+"  LEFT JOIN party p ON p.id = aco.sp_id"
					+"  LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+"  where acao.id="+application_id
					+"	GROUP BY aco.id";
  		}
  		
  		Map BillingOrderListMap = new HashMap();
  		List<Record> recordList= Db.find(sql);
        BillingOrderListMap.put("draw", recordList.size());
        BillingOrderListMap.put("recordsTotal", recordList.size());
        BillingOrderListMap.put("recordsFiltered", recordList.size());
        BillingOrderListMap.put("data", recordList);
        

        renderJson(BillingOrderListMap);
  	}
  	
  	
  	
  	@Before(Tx.class)
	public void save() throws InstantiationException, IllegalAccessException {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        TradeArapChargeApplicationOrder order = new TradeArapChargeApplicationOrder();
   		String id = (String) dto.get("id");
   		String ids=(String) dto.get("ids");
   		String status = (String) dto.get("status");
		String selected_item_ids= (String) dto.get("selected_ids"); //获取申请单据的id,用于回显
		String sp_id=(String) dto.get("sp_id");
		String sp_name=(String) dto.get("sp_name");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
   		
   		String action_type="add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type="update";
   			order = TradeArapChargeApplicationOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order); 
   			
   		//需后台处理的字段
   			if("复核不通过".equals(status)){
   				order.set("status", "新建");
   			}
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSSQ", user.getLong("office_id")));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.set("office_id", office_id);
   			order.set("sp_id", sp_id);
   			if(!"".equals(selected_item_ids)){
   				order.set("selected_item_ids", selected_item_ids);
   			}
   			order.save();
   			id = order.getLong("id").toString();
   			
		String itemId="";
   		TradeChargeApplicationOrderRel caor = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String action = item.get("action");
				   itemId = item.get("id");
			if("CREATE".equals(action)){
				caor = new TradeChargeApplicationOrderRel();
				caor.set("application_order_id", id);
				caor.set("job_order_arap_id", itemId);
				Record aci = Db.findFirst("select * from trade_arap_charge_item where ref_order_id=?",itemId);
				Long charge_order_id=aci.getLong("charge_order_id");
				caor.set("charge_order_id", charge_order_id);
				caor.set("order_type", "应收对账单");
				caor.save();
				
				TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
				arapChargeOrder.set("audit_status", "收款申请中").update();
				
			}
		}
		
		//selected_item_ids,改变创建标记位
//		if("".equals(selected_item_ids)){
//			String textError="您创建的申请单中选中没有明细";
//			renderJson(textError);
//			return ;
//        }
		//更新勾选的job_order_arap item creat_flag,改变创建标记位
		String ySql ="update trade_job_order_arap set create_flag='Y' where id in("+selected_item_ids+")";
        Db.update(ySql);
	        
		
	}
   		SysInfoController.saveLog(jsonStr, id, user, action_type, "收款申请单", "trade");
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		r.set("idsArray", ids);
   		r.set("sp_name",sp_name);
   		renderJson(r);
	}
  	
  	@Before(EedaMenuInterceptor.class)
  	public void edit() throws ParseException {
		String id = getPara("id");
		UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("trade_arap_charge_application_order", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
		TradeArapChargeApplicationOrder order = TradeArapChargeApplicationOrder.dao.findById(id);
		
		Party p  = Party.dao.findById(order.getLong("sp_id"));
		if(p != null){
			setAttr("party", p);
		}
		String sql = "select group_concat(cast(charge_order_id as char) SEPARATOR ',') ids from trade_charge_application_order_rel where application_order_id = ?";
		Record rec = Db.findFirst(sql,id);
		setAttr("ids", rec.getStr("ids"));
		
		UserLogin userLogin = null;
		userLogin = UserLogin.dao .findById(order.get("create_by"));
		String creator_name = userLogin.get("c_name");
		
		userLogin = UserLogin.dao .findById(order.get("check_by"));
		String check_name = null;
		if(userLogin != null){
			check_name = userLogin.get("c_name");
		}
		
		userLogin = UserLogin.dao .findById(order.get("confirm_by"));
		String confirm_name = null;
		if(userLogin != null){
			confirm_name = userLogin.get("c_name");
		}
		
		List<Record> list = null;
    	list = getItems(id);
    	setAttr("docList", list);
		
		Record r = order.toRecord();
		r.set("creator_name", creator_name);
		r.set("check_name", check_name);
		r.set("confirm_name", confirm_name);
		setAttr("order", r);

		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/tradeArap/ChargeRequest/chargeEdit.html");
	}
  	
  	
  	@Before(Tx.class)
    public void sendMail(String order_id,String order_no,String creator_name) throws Exception {
    	UserLogin userlogin = UserLogin.dao.findFirst("SELECT * from user_login where c_name='"+creator_name+"'");
    	String mailTitle = "您有一份复核不通过的收款申请单";
    	String mailContent = "收款申请单为<a href=\"http://www.esimplev.com/chargeRequest/edit?id="+order_id+"\">"+order_no+"</a>";
    	
    	Office office=Office.dao.findById(userlogin.get("office_id"));
        MultiPartEmail email = new MultiPartEmail();  
        /*smtp.exmail.qq.com*/
        String HostName = office.getStr("host_name");
        int SmtpPort = office.getInt("smtp_port");
        email.setHostName(HostName);
        email.setSmtpPort(SmtpPort);
        //反查公司信息
        
        try{
        /*输入公司的邮箱和密码*/
        email.setAuthenticator(new DefaultAuthenticator(office.getStr("email"), office.getStr("emailPassword")));        
        email.setSSLOnConnect(true);
        email.setFrom(office.getStr("email"),office.getStr("office_name"));//设置发信人
        //设置收件人，邮件标题，邮件内容
//    	email.addTo("1063203104@qq.com");
    	email.addTo(userlogin.getStr("email"));
//        email.addTo("864358232@qq.com");
        email.setSubject(mailTitle);
        email.setContent(mailContent, "text/html;charset=gb2312");
//        //抄送
//        email.addCc("1063203104@qq.com");
//       //密送
//        email.addBcc("1063203104@qq.com");
        
        	//email.setCharset("UTF-8"); 
        	email.send();
        }catch(Exception e){
        	e.printStackTrace();
        }
       
    }
  	
  	
    //复核
  	@Before(Tx.class)
    public void checkOrder() throws Exception{
        String application_id=getPara("order_id");
        String selfId = getPara("selfId");
        String ids = getPara("ids");
        String order_no =getPara("order_no");
   		String creator_name =getPara("creator_name");
   		String invoice_no =getPara("invoice_no");
        //更改原始单据状态
        List<Record> res = null;

   		  
  		Record re = new Record();
  		if(StringUtils.isNotEmpty(application_id)){
  			TradeArapChargeApplicationOrder order = TradeArapChargeApplicationOrder.dao.findById(application_id);
  			if("cancelcheckBtn".equals(selfId)){
	    		 order.set("status", "复核不通过");
	    		 order.set("invoice_no", invoice_no);
	    		 sendMail(application_id,order_no,creator_name);
	    		 
	    	 }else{
	    		 order.set("status", "已复核");
	    	 }

  			order.set("check_by", LoginUserController.getLoginUserId(this));
	        order.set("check_stamp", new Date()).update();
	        res = Db.find("select * from trade_charge_application_order_rel where application_order_id = ?",application_id);
	  		
	        long check_by = order.getLong("check_by");
	   		 String user_name = LoginUserController.getUserNameById(check_by);
	  		 re = order.toRecord();
	  		 re.set("check_name",user_name);
  		}  		
  		if(StringUtils.isNotEmpty(ids)){
  			String[] arr= ids.split(",");
  			for(int i=0;i<arr.length;i++){
  				String id=arr[i];
  				TradeArapChargeApplicationOrder order = TradeArapChargeApplicationOrder.dao.findById(id);
  		        order.set("status", "已复核");
  		        order.set("check_by", LoginUserController.getLoginUserId(this));
  		        order.set("check_stamp", new Date()).update();
  			}
  			String str="select * from trade_charge_application_order_rel where application_order_id in ( "+ids+" )";
     	   res = Db.find(str);
     	   re.set("ids",ids);
  		}
  		for (Record re1 : res) {
  			Long id = re1.getLong("charge_order_id");
  			String order_type = re1.getStr("order_type");

  			if("应收对账单".equals(order_type)){
			    TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(id);
				arapChargeOrder.set("audit_status", "已复核").update();
			}
  		}
  	    renderJson(re);
    }
  	
  	
    //收款确认
  	@Before(Tx.class)
	public void confirmOrder(){
  		UserLogin user = LoginUserController.getLoginUser(this);
  		String jsonStr=getPara("params");
 
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String ids = getPara("ids");
  		String application_id=getPara("application_id");
  		String confirmVal=getPara("confirmVal");
  		String pay_remark=getPara("pay_remark");
  		String receive_time =getPara("receive_time");
  		String application_office_id ="";
  		
  		if(dto !=null && dto.get("pay_remark") != null){
  		    pay_remark=(String) dto.get("pay_remark");
  		}
  		if(dto !=null && dto.get("receive_time") != null){
  			receive_time=(String) dto.get("receive_time");
  		}
  		
        if(StringUtils.isNotEmpty(application_id)){
            TradeArapChargeApplicationOrder arapChargeInvoiceApplication = TradeArapChargeApplicationOrder.dao.findById(application_id);
            application_office_id = arapChargeInvoiceApplication.get("office_id").toString();
            
        	if("坏账确认".equals(confirmVal)){
        		badRevenueHandle(user, arapChargeInvoiceApplication, pay_remark); 
        	}else{
        		normalConfirmHandle(user, dto, application_id, pay_remark,
                        application_office_id, arapChargeInvoiceApplication);
        	}
        }
        if(StringUtils.isNotEmpty(ids)){
  			String[] arr= ids.split(",");
	        for(int i=0;i<arr.length;i++){
	        	String id=arr[i];
	        	String receive_bank_id = "";
	        	
	        	TradeArapChargeApplicationOrder arapChargeInvoiceApplication = TradeArapChargeApplicationOrder.dao.findById(id);
	        	application_office_id = arapChargeInvoiceApplication.get("office_id").toString();
	        	String payment_method = arapChargeInvoiceApplication.get("payment_method");
	        	
	        	if(arapChargeInvoiceApplication.get("deposit_bank")!=null){
	        		receive_bank_id = arapChargeInvoiceApplication.getLong("deposit_bank").toString();
	        	}
	        	if(StringUtils.isEmpty(receive_bank_id)){
	      			String str2="select id from fin_account where bank_name='现金' and office_id="+application_office_id;
	      	        Record rec = Db.findFirst(str2);
	      	        if(rec!=null){
	      	        	receive_bank_id = rec.getLong("id").toString();
	      	        }
	      		}
	            arapChargeInvoiceApplication.set("status", "已收款");
	            arapChargeInvoiceApplication.set("receive_time", receive_time);
	            arapChargeInvoiceApplication.set("confirm_by", user.get("id"));
	            arapChargeInvoiceApplication.set("confirm_stamp", new Date());
	            arapChargeInvoiceApplication.set("pay_remark", pay_remark);
	            arapChargeInvoiceApplication.update();
	            //已收款的标记位
	    		String paySql ="update trade_job_order_arap set pay_flag='Y' "
	    				+ " where id in (SELECT job_order_arap_id FROM trade_charge_application_order_rel WHERE application_order_id ="+id+")" ; //chargeOrderId.substring(1) 去掉第一位
	            Db.update(paySql);
	          //更改原始单据状态
	            List<Record> res = Db.find("select * from trade_charge_application_order_rel where application_order_id = ?",id);
	           
	            for (Record re : res) {
	      			Long charge_order_id = re.getLong("charge_order_id");
	      			String order_type = re.getStr("order_type");
	      			if(order_type.equals("应收对账单")){
	    				TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
	                    Double usd = arapChargeOrder.getDouble("usd");
	                    Double cny = arapChargeOrder.getDouble("cny");
	                    Double hkd = arapChargeOrder.getDouble("hkd");
	                    Double jpy = arapChargeOrder.getDouble("jpy");
	
	                    String sql = "SELECT "
	                    		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
	            				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.charge_order_id="+charge_order_id
	            				+" ),0) paid_cny,"
	            				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
	            				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.charge_order_id="+charge_order_id
	            				+" ),0) paid_usd,"
	            				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
	            				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.charge_order_id="+charge_order_id
	            				+" ),0) paid_jpy,"
	            				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
	            				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.charge_order_id="+charge_order_id
	            				+" ),0) paid_hkd ";
	                       
	                       Record r = Db.findFirst(sql);
	                       Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
	                       Double paid_usd = r.getDouble("paid_usd");
	                       Double paid_jpy = r.getDouble("paid_jpy");
	                       Double paid_hkd = r.getDouble("paid_hkd");
	    				
	    				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
	    					arapChargeOrder.set("audit_status", "部分已收款").update();
	    				}else{
	    					arapChargeOrder.set("audit_status", "已收款").update();
	    				}
	    			}
	      		}
	            //新建日记账表数据
	      		String cny_pay_amount = "0.0"; 
	      		if(arapChargeInvoiceApplication.getDouble("modal_cny")!=null)
	      			cny_pay_amount =arapChargeInvoiceApplication.getDouble("modal_cny").toString();
	      		if(!"0.0".equals(cny_pay_amount)&&StringUtils.isNotEmpty(cny_pay_amount)){
	      			createAuditLog(id, payment_method, receive_bank_id, receive_time, cny_pay_amount, "CNY", application_office_id);
	      		}
	      		
	            String usd_pay_amount ="0.0"; 
	            if(arapChargeInvoiceApplication.getDouble("modal_usd")!=null)
	            	usd_pay_amount =arapChargeInvoiceApplication.getDouble("modal_usd").toString();
	            if(!"0.0".equals(usd_pay_amount)&&StringUtils.isNotEmpty(usd_pay_amount)){
	            	createAuditLog(id, payment_method, receive_bank_id, receive_time, usd_pay_amount, "USD", application_office_id);
	            }
	            
	            String jpy_pay_amount ="0.0"; 
	            if(arapChargeInvoiceApplication.getDouble("modal_jpy")!=null)
	            	jpy_pay_amount =arapChargeInvoiceApplication.getDouble("modal_jpy").toString();
	            if(!"0.0".equals(jpy_pay_amount)&&StringUtils.isNotEmpty(jpy_pay_amount)){
	            	createAuditLog(id, payment_method, receive_bank_id, receive_time, jpy_pay_amount, "JPY", application_office_id);
	            }
	            
	            String hkd_pay_amount ="0.0"; 
	            if(arapChargeInvoiceApplication.getDouble("modal_hkd")!=null)
	            	hkd_pay_amount =arapChargeInvoiceApplication.getDouble("modal_hkd").toString();
	            if(!"0.0".equals(hkd_pay_amount)&&StringUtils.isNotEmpty(hkd_pay_amount)){
	            	createAuditLog(id, payment_method, receive_bank_id, receive_time, hkd_pay_amount, "HKD", application_office_id);
	            }
	            
	        }
        }
        
        Record r = new Record();
		r.set("confirm_name", LoginUserController.getUserNameById(LoginUserController.getLoginUserId(this)));
		r.set("status", "已收款");
		r.set("ids", ids);
        renderJson(r);
 
    }

    private void normalConfirmHandle(UserLogin user, Map<String, ?> dto,
            String application_id, String pay_remark,
            String application_office_id,
            TradeArapChargeApplicationOrder arapChargeInvoiceApplication) {
        String receive_time = (String) dto.get("receive_time");
        String receive_bank_id = "";
        String payment_method = (String) dto.get("payment_method");
        
        if(dto.get("receive_bank_id")!=null){
        	 receive_bank_id =  dto.get("receive_bank_id").toString();
        }else{
        	String str2="select id from fin_account where bank_name='现金' and office_id="+ application_office_id;
            Record rec = Db.findFirst(str2);
            if(rec!=null){
            	receive_bank_id = rec.getLong("id").toString();
            }
        }
        
        
        arapChargeInvoiceApplication.set("status", "已收款");
        if(StringUtils.isNotEmpty(receive_time)){
        	arapChargeInvoiceApplication.set("receive_time", receive_time);
        }
        arapChargeInvoiceApplication.set("confirm_by", user.get("id"));
        arapChargeInvoiceApplication.set("confirm_stamp", new Date());
        if(StringUtils.isNotEmpty(pay_remark)){
        	arapChargeInvoiceApplication.set("pay_remark",pay_remark );
        }
        arapChargeInvoiceApplication.update();
        //已收款的标记位
        String paySql ="update trade_job_order_arap set pay_flag='Y' "
        		+ " where id in (SELECT job_order_arap_id FROM trade_charge_application_order_rel WHERE application_order_id ="+application_id+")" ; //chargeOrderId.substring(1) 去掉第一位
        Db.update(paySql);
        //更改原始单据状态
        List<Record> res = Db.find("select * from trade_charge_application_order_rel where application_order_id = ?",application_id);
        for (Record re : res) {
        	Long charge_order_id = re.getLong("charge_order_id");
        	String order_type = re.getStr("order_type");
        	if(order_type.equals("应收对账单")){
        		TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
                Double usd = arapChargeOrder.getDouble("usd");
                Double cny = arapChargeOrder.getDouble("cny");
                Double hkd = arapChargeOrder.getDouble("hkd");
                Double jpy = arapChargeOrder.getDouble("jpy");

                String sql = "SELECT "
                		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_cny,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_usd,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_jpy,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_hkd ";
                   
                   Record r = Db.findFirst(sql);
                   Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
                   Double paid_usd = r.getDouble("paid_usd");
                   Double paid_jpy = r.getDouble("paid_jpy");
                   Double paid_hkd = r.getDouble("paid_hkd");
        
        		if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
        			arapChargeOrder.set("audit_status", "部分已收款").update();
        		}else{
        			arapChargeOrder.set("audit_status", "已收款").update();
        		}
        	}
        }
           //新建日记账表数据
        String cny_pay_amount = "0.0"; 
        if(arapChargeInvoiceApplication.getDouble("modal_cny")!=null)
        	cny_pay_amount =arapChargeInvoiceApplication.getDouble("modal_cny").toString();
        if(!"0.0".equals(cny_pay_amount)&&StringUtils.isNotEmpty(cny_pay_amount)){
        	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, cny_pay_amount, "CNY", application_office_id);
        }
        
        String usd_pay_amount ="0.0"; 
        if(arapChargeInvoiceApplication.getDouble("modal_usd")!=null)
        	usd_pay_amount =arapChargeInvoiceApplication.getDouble("modal_usd").toString();
        if(!"0.0".equals(usd_pay_amount)&&StringUtils.isNotEmpty(usd_pay_amount)){
        	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, usd_pay_amount, "USD", application_office_id);
        }
        
        String jpy_pay_amount ="0.0"; 
        if(arapChargeInvoiceApplication.getDouble("modal_jpy")!=null)
        	jpy_pay_amount =arapChargeInvoiceApplication.getDouble("modal_jpy").toString();
        if(!"0.0".equals(jpy_pay_amount)&&StringUtils.isNotEmpty(jpy_pay_amount)){
        	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, jpy_pay_amount, "JPY", application_office_id);
        }
        
        String hkd_pay_amount ="0.0"; 
        if(arapChargeInvoiceApplication.getDouble("modal_hkd")!=null)
        	hkd_pay_amount =arapChargeInvoiceApplication.getDouble("modal_hkd").toString();
        if(!"0.0".equals(hkd_pay_amount)&&StringUtils.isNotEmpty(hkd_pay_amount)){
        	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, hkd_pay_amount, "HKD", application_office_id);
        }
    }

    private void badRevenueHandle(UserLogin user, TradeArapChargeApplicationOrder arapChargeInvoiceApplication,
            String pay_remark) {
        String application_id = arapChargeInvoiceApplication.get("id");
        arapChargeInvoiceApplication.set("status", "该笔为坏账");
        arapChargeInvoiceApplication.set("confirm_by", user.get("id"));
        arapChargeInvoiceApplication.set("confirm_stamp", new Date());
        if(StringUtils.isNotEmpty(pay_remark)){
        	arapChargeInvoiceApplication.set("pay_remark",pay_remark );
        }
        arapChargeInvoiceApplication.update();
           //坏账的标记位
        String paySql ="update trade_job_order_arap set pay_flag='B' "
        		+ " where id in (SELECT job_order_arap_id FROM trade_charge_application_order_rel WHERE application_order_id ="+application_id+")" ; //chargeOrderId.substring(1) 去掉第一位
        Db.update(paySql);
           //更改原始单据状态
        List<Record> res = Db.find("select * from trade_charge_application_order_rel where application_order_id = ?",application_id);
        for (Record re : res) {
        	Long charge_order_id = re.getLong("charge_order_id");
        	String order_type = re.getStr("order_type");
        	if(order_type.equals("应收对账单")){
        		TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
                Double usd = arapChargeOrder.getDouble("usd");
                Double cny = arapChargeOrder.getDouble("cny");
                Double hkd = arapChargeOrder.getDouble("hkd");
                Double jpy = arapChargeOrder.getDouble("jpy");

                String sql = "SELECT "
                		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_cny,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_usd,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_jpy,"
        				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  trade_job_order_arap joa LEFT JOIN trade_arap_charge_item aci on joa.id = aci.ref_order_id"
        				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.charge_order_id="+charge_order_id
        				+" ),0) paid_hkd ";
                   
                   Record r = Db.findFirst(sql);
                   Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
                   Double paid_usd = r.getDouble("paid_usd");
                   Double paid_jpy = r.getDouble("paid_jpy");
                   Double paid_hkd = r.getDouble("paid_hkd");
        
        		if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
        			arapChargeOrder.set("audit_status", "部分坏账款").update();
        		}else{
        			arapChargeOrder.set("audit_status", "坏账款").update();
        		}
        	}
        }
    }
  	
  	
  	private void createAuditLog(String application_id, String payment_method,
            String receive_bank_id, String receive_time, String pay_amount, 
            String currency_code, String office_id) {
        //新建日记账表数据
  		
		TradeArapAccountAuditLog auditLog = new TradeArapAccountAuditLog();
        auditLog.set("payment_method", payment_method);
        auditLog.set("payment_type", TradeArapAccountAuditLog.TYPE_CHARGE);
        auditLog.set("currency_code", currency_code);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("office_id", office_id);
        if(receive_bank_id!=null && !("").equals(receive_bank_id)){
        		auditLog.set("account_id", receive_bank_id);
        	}else{
        		auditLog.set("account_id", 4);
        	}
        auditLog.set("source_order", "应收申请单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
    }

    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
    	String order_id = getPara("order_id");
    	List<UploadFile> fileList = getFiles("doc");
    	
    	AppInvoiceDoc order = new AppInvoiceDoc();
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();

			order.set("order_id", order_id);
			order.set("uploader", LoginUserController.getLoginUserId(this));
			order.set("doc_name", fileName);
			order.set("type", "charge");
			order.set("upload_time", new Date());
			order.save();
		}

    	renderJson(order);
    }
    
    
  //返回list
    private List<Record> getItems(String orderId) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	
    	itemSql = "select aid.*,u.c_name from app_invoice_doc aid left join user_login u on aid.uploader=u.id "
    			+ " where aid.order_id=? order by aid.id desc";
    	itemList = Db.find(itemSql, orderId);
	    
		return itemList;
	}
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	AppInvoiceDoc order = AppInvoiceDoc.dao.findById(id);
    	String fileName = order.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            order.delete();
            resultMap.put("result", result);
        }else{
        	order.delete();
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    private Map<String, Double> updateExchangeTotal(String appOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  trade_job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select caor.job_order_arap_id from trade_charge_application_order_rel caor where caor.application_order_id="+appOrderId+")";
		
		Map<String, Double> exchangeTotalMap = new HashMap<String, Double>();
		exchangeTotalMap.put("MODAL_CNY", 0d);
		exchangeTotalMap.put("MODAL_USD", 0d);
		exchangeTotalMap.put("MODAL_HKD", 0d);
		exchangeTotalMap.put("MODAL_JPY", 0d);
		
		List<Record> resultList= Db.find(sql);
		for (Record rec : resultList) {
            String name = "MODAL_"+rec.get("exchange_currency_name");
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
		
		Record order = Db.findById("trade_arap_charge_application_order", appOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		if(order.get("return_confirm_stamp")!=null){
			order.set("return_confirm_stamp", order.get("return_confirm_stamp"));
		}else{
			order.set("return_confirm_stamp", new Date());
		}
		Db.update("trade_arap_charge_application_order", order);
		return exchangeTotalMap;
    }
    //添加明细的查询
    public void itemList(){
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
        			+ " select joa.order_type sql_type, joa.id,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
              		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.order_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no,jo.type, "
              		+ " p.abbr sp_name,p1.abbr customer_name,"
//              		+ " jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
//              		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
              		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
              		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
              		+ " ) after_total,"
              		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
              		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
              		+ " after_rate_total,ifnull(f.name,f.name_eng) fee_name,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
      				+ " from trade_job_order jo "
      				+ " left join trade_job_order_arap joa on jo.id=joa.order_id "
//      				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
//      				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
//      				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
      				+ " left join party p on p.id=joa.sp_id "
      				+ " left join party p1 on p1.id=jo.customer_id "
//      				+ " left join location l on l.id=jos.fnd "
      				+ " left join currency cur on cur.id=joa.currency_id "
      				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
      				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
      				+ " left join fin_item f on f.id = joa.charge_id"
      				+ " where  joa.audit_flag='Y' and joa.billconfirm_flag = 'Y'  and joa.create_flag='N'  and jo.office_id = "+office_id
      				 + " and jo.delete_flag = 'N'"
     				+ " GROUP BY joa.id "
    				+ " ) B where 1=1 ";
        	}else{
        		 sql = "select * from(  "
                 		+ " select ifnull(f.name,f.name_eng) fee_name, joa.id,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
                 		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.order_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no,jo.type, "
                 		+ " p.abbr sp_name,p1.abbr customer_name,"
//                 		+ " jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
//                 		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
                 		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
                 		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
                 		+ " ) after_total,"
                 		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
                 		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
                 		+ " after_rate_total,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
         				+ " from trade_job_order jo "
         				+ " left join trade_job_order_arap joa on jo.id=joa.order_id "
//         				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
//         				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
//         				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
         				+ " left join party p on p.id=joa.sp_id "
         				+ " left join party p1 on p1.id=jo.customer_id "
//         				+ " left join location l on l.id=jos.fnd "
         				+ " left join currency cur on cur.id=joa.currency_id "
         				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
         				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
         				+ " left join fin_item f on f.id = joa.charge_id"
         				+ " where joa.order_type='charge' and joa.audit_flag='Y' and joa.billconfirm_flag = 'Y' and joa.create_flag='N' and jo.office_id = "+office_id
         				 + " and jo.delete_flag = 'N'"
         				+ " GROUP BY joa.id "
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
    	 
//    	String sql="select * from	trade_job_order_arap joa where sp_id ="+sp_id
//    						+"and billconfirm_flag = 'Y'"
//							+"and create_flag='N' "
//							+"and order_type = 'charge' ";
    }
    
    public void insertChargeItem(){
    	String itemList= getPara("charge_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String appOrderId=getPara("order_id");
    	TradeArapChargeApplicationOrder order = TradeArapChargeApplicationOrder.dao.findById(appOrderId);
    	
   		TradeChargeApplicationOrderRel caor = null;
		for(String item :itemArray){
				caor = new TradeChargeApplicationOrderRel();
				caor.set("application_order_id", appOrderId);
				caor.set("job_order_arap_id", item);
				Record re = Db.findFirst("select * from trade_arap_charge_item where ref_order_id=?",item);
				Long charge_order_id=re.getLong("charge_order_id");
				caor.set("charge_order_id", charge_order_id);
				caor.set("order_type", "应收对账单");
				caor.save();
				
				TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
				arapChargeOrder.set("audit_status", "收款申请中").update();
				//更新trade_job_order_arap的create_flag
				TradeJobOrderArap joa=TradeJobOrderArap.dao.findById(item);
				joa.set("create_flag", "Y");
				joa.update();
		}
    	//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(appOrderId);
		exchangeTotalMap.put("appOrderId", Double.parseDouble(appOrderId));
    	renderJson(exchangeTotalMap);
    }
    //删除明细
    public void deleteChargeItem(){
    	String appOrderId=getPara("order_id");
    	String itemid=getPara("charge_itemid");
    	if(itemid !=null&& appOrderId!=null){
    		 Db.deleteById("trade_charge_application_order_rel","job_order_arap_id,application_order_id",itemid,appOrderId);
    		 //Record re = Db.findFirst("select * from trade_arap_charge_item where ref_order_id=?",itemid);
			 //Long charge_order_id=re.getLong("charge_order_id");
    		 //TradeArapChargeOrder arapChargeOrder = TradeArapChargeOrder.dao.findById(charge_order_id);
    		 //arapChargeOrder.set("audit_status", "新建").update();
    		 
    		 TradeJobOrderArap jobOrderArap = TradeJobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("create_flag", "N");
             jobOrderArap.update();
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(appOrderId);
    			exchangeTotalMap.put("appOrderId", Double.parseDouble(appOrderId));
    	    	renderJson(exchangeTotalMap);
    }

}
