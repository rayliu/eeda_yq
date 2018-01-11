package controllers.arap.ar.costRequest;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AppInvoiceDoc;
import models.ArapAccountAuditLog;
import models.ArapCostApplication;
import models.ArapCostOrder;
//import models.CostAppOrderRel;
import models.CostApplicationOrderRel;
import models.Office;
import models.Party;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.RequestInvoices;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostRequestController extends Controller {
    private Log logger = Log.getLog(CostRequestController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	String back =getPara("back");
    	setAttr("back",back);
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if (user==null) {
            return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/costRequest");
        setAttr("listConfigList", configList);
    	render("/oms/CostRequest/CostRequestList.html");
    }
    
    @Before(EedaMenuInterceptor.class) 
    public void create() {
		render("/oms/CostRequest/create.html");
	}
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        String sql = " select * from ("
        		+ " select  aco.*, p.abbr sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount,"
        		+ " sum(ifnull(c.paid_usd,0)) paid_usd,"
        		+ " sum(ifnull(c.paid_cny,0)) paid_cny,"
        		+ " sum(ifnull(c.paid_hkd,0)) paid_hkd,"
        		+ " sum(ifnull(c.paid_jpy,0)) paid_jpy,"
        		+ " group_concat((select concat(order_no,'-',status) from arap_cost_application_order where id = c.application_order_id) SEPARATOR '<br/>') app_msg"
				+ " from arap_cost_order aco "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id"
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
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
        String sql = " select * from ("
        				+"select  aco.*, p.abbr sp_name, "
        				+" IFNULL("
        				+" 				("
        				+" 					SELECT"
        				+" 						SUM(joa.exchange_total_amount)-IFNULL("
        				+" 				("
        				+" 					SELECT"
        				+" 						SUM(joa.exchange_total_amount)"
        				+" 					FROM"
        				+" 						job_order_arap joa"
        				+" 					LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+" 					WHERE"
        				+" 						joa.create_flag = 'Y' AND joa.order_type = 'charge'"
        				+" 					AND joa.exchange_currency_id = 3"
        				+" 					AND aci.cost_order_id = aco.id"
        				+" 				),"
        				+" 				0"
        				+" 			)"
        				+" 					FROM"
        				+" 						job_order_arap joa"
        				+" 					LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+" 					WHERE"
        				+" 						joa.create_flag = 'Y' AND joa.order_type = 'cost'"
        				+" 					AND joa.exchange_currency_id = 3"
        				+" 					AND aci.cost_order_id = aco.id"
        				+" 				),"
        				+" 				0"
        				+" 			) paid_cny,"
        				+" IFNULL("
        				+"				("
        				+"					SELECT"
        				+"						SUM(joa.exchange_total_amount)-IFNULL(("
        				+"						  SELECT"
        				+"												SUM(joa.exchange_total_amount)"
        				+"						FROM"
        				+"						job_order_arap joa"
        				+"						LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+"						WHERE"
        				+"							joa.create_flag = 'Y' AND joa.order_type = 'charge'"
        				+"						AND joa.exchange_currency_id = 6"
        				+"						AND aci.cost_order_id = aco.id"
        				+"            ),0)"
        				+"					FROM"
        				+"						job_order_arap joa"
        				+"					LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+"					WHERE"
        				+"						joa.create_flag = 'Y' AND joa.order_type = 'cost'"
        				+"					AND joa.exchange_currency_id = 6"
        				+"					AND aci.cost_order_id = aco.id"
        				+"				),"
        				+"				0"
        				+"			) paid_usd,"
        				+"  IFNULL((SELECT SUM(joa.exchange_total_amount)-IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa "
        				+"  LEFT JOIN arap_cost_item aci on joa.id =  aci.ref_order_id"
        				+"         				where joa.create_flag = 'Y' and joa.order_type='charge' AND joa.exchange_currency_id =8 and aci.cost_order_id=aco.id"
        				+"         				 ),0)"
        				+"     from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
        				+"         				where joa.create_flag = 'Y' and joa.order_type='charge' AND joa.exchange_currency_id =8 and aci.cost_order_id=aco.id"
        				+"         				 ),0) paid_jpy," 
        				+" IFNULL("
        				+" 	("
        				+" 		SELECT"
        				+" 			SUM(joa.exchange_total_amount)-IFNULL("
        				+" 	("
        				+" 		SELECT"
        				+" 			SUM(joa.exchange_total_amount)"
        				+" 		FROM"
        				+" 			job_order_arap joa"
        				+" 		LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+" 		WHERE"
        				+" 			joa.create_flag = 'Y' and joa.order_type = 'charge'"
        				+" 		AND joa.exchange_currency_id = 9"
        				+" 		AND aci.cost_order_id = aco.id"
        				+" 	),"
        				+" 	0"
        				+" )"
        				+" 		FROM"
        				+" 			job_order_arap joa"
        				+" 		LEFT JOIN arap_cost_item aci ON joa.id = aci.ref_order_id"
        				+" 		WHERE"
        				+" 			joa.create_flag = 'Y' and joa.order_type = 'cost'"
        				+" 		AND joa.exchange_currency_id = 9"
        				+" 		AND aci.cost_order_id = aco.id"
        				+" 	),"
        				+" 	0"
        				+" ) paid_hkd,"
        				+" group_concat(DISTINCT (select concat(order_no,'-',status) from arap_cost_application_order where id = c.application_order_id) SEPARATOR '<br/>') app_msg"
        				+" from arap_cost_order aco"
        				+" left join cost_application_order_rel c on c.cost_order_id=aco.id"
        				+" left join party p on p.id=aco.sp_id "
        				+" where aco.status='已确认' and aco.office_id = "+office_id+" "
        				+" group by aco.id"
        				+ " ) A where ((CONVERT (ifnull(usd, 0), DECIMAL) - CONVERT (paid_usd, DECIMAL) !=0) OR convert(ifnull(cny, 0), decimal) > convert(IFNULL(paid_cny,0), decimal) OR convert(ifnull(hkd, 0), decimal) > convert(paid_hkd, decimal) OR (CONVERT (ifnull(jpy, 0), DECIMAL) - CONVERT (paid_jpy, DECIMAL)  !=0))" ;
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition +") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        String pageBoolean = getPara("pageBoolean");
        if((StringUtils.isEmpty(pageBoolean)||(pageBoolean.equals("false")))){
        	sLimit="";
        }
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
				+ " from arap_cost_order aco left join party p on p.id = aco.sp_id where aco.id = ?",orderArrId[0]);

		Record re = new Record();
		re.set("sp_id", r.getLong("SP_ID"));
		re.set("payee_unit", r.getStr("PAYEE_UNIT"));
		re.set("payee_name", r.getStr("PAYEE_NAME"));
		setAttr("order", re);
		
		String sql="select group_concat(cast(ref_order_id as char) SEPARATOR ',') selected_item_ids"
                + " from arap_cost_item where cost_order_id in("+ids+")";
        String selected_item_ids = Db.findFirst(sql).getStr("selected_item_ids");
        setAttr("selected_item_ids", selected_item_ids);
			
		render("/oms/CostRequest/costEdit_select_item.html");
	}
    
    
    
    
    public void applicationList() {
//    	String sLimit = "";
//        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
        
    	String sp_id =getPara("sp_id");
        if(StringUtils.isNotEmpty(getPara("sp_id"))){
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,sp_id,"ARAP_COM");
    	}
        
        String service_stamp_beginTime =getPara("service_stamp_beginTime");
    	String service_stamp_endTime =getPara("service_stamp_endTime");
    	String serviceStampBeginEnd ="";
    	if(StringUtils.isNotBlank(service_stamp_beginTime)){
    		serviceStampBeginEnd+=" and ('"+service_stamp_beginTime+"' <=acao.begin_time";
    	}else{
    		serviceStampBeginEnd+=" and ('1970-01-01'<=acao.begin_time ";
    	}
    	if(StringUtils.isNotBlank(service_stamp_endTime)){
    		serviceStampBeginEnd+=" and '"+service_stamp_endTime+"' >=acao.end_time )";
    	}else{
    		serviceStampBeginEnd+=" and '2050-12-31'>=acao.end_time )";
    	}
        
        
    	
        UserLogin user = LoginUserController.getLoginUser(this);
        if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select p.abbr payee_company,p.code,acao.*, acao.order_no application_order_no,CAST(CONCAT(acao.begin_time,'到',acao.end_time) AS CHAR) service_stamp, "
        		+ " '申请单' order_type,aco.order_no cost_order_no,u.c_name "
        		+ " ,EXISTS (SELECT	joa.order_type FROM job_order_arap joa "
        		+ " LEFT JOIN cost_application_order_rel caor ON caor.job_order_arap_id = joa.id"
        		+ " WHERE	caor.application_order_id = acao.id	AND joa.order_type = 'charge' ) hedge_flag,"        		
        		+ " (SELECT GROUP_CONCAT(cri.invoice_no SEPARATOR '<br>') FROM request_invoices cri "
        		+ " LEFT JOIN arap_charge_application_order acao_1 ON acao_1.id = cri.order_id WHERE order_type='costRequest' and cri.order_id = acao.id ) invoices_no"
				+ " from arap_cost_application_order acao "
				+ " left join cost_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join arap_cost_order aco on aco.id = caor.cost_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ " LEFT JOIN party p on p.id=acao.sp_id"
				+ "	where acao.office_id = "+office_id+serviceStampBeginEnd
				+ " group by acao.id"
				+ " order by acao.begin_time"
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
  	public void costOrderList() {
          String ids = getPara("ids");
          String application_id = getPara("application_id");
          String sql = "";
          
          if(StringUtils.isBlank(application_id)){
  	       
          	sql =  " SELECT aco.*, p.company_name payee_name, '应付对账单' order_type, "
          			+" p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
          			+" (aco.usd-ifnull(c.paid_usd, 0)) wait_usd,"
          			+" (aco.cny-ifnull(c.paid_cny, 0)) wait_cny,"
          			+"     (aco.jpy-ifnull(c.paid_jpy, 0)) wait_jpy,"
          			+"     (aco.hkd-ifnull(c.paid_hkd, 0)) wait_hkd,"
          			+"     case "
          			+"         when c.cost_order_id is not null then"
          			+"             ifnull("
          			+"                    ( SELECT sum(exchange_total_amount) FROM job_order_arap"
          			+"                                 WHERE id IN(SELECT ref_order_id FROM arap_cost_item aci WHERE cost_order_id IN(c.cost_order_id) ) "
          			+"                                     AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'USD' ) "
          			+"                                     AND pay_flag = 'Y' "
          			+"                     ),0) "
          			+"     else aco.usd "
          			+"     end  apply_pay_usd, "
          			+"     case  "
          			+"         when c.cost_order_id is not null then "
          			+"             ifnull( "
          			+"                    ( SELECT sum(exchange_total_amount) FROM job_order_arap "
          			+"                        WHERE id IN( SELECT ref_order_id FROM arap_cost_item WHERE cost_order_id IN(c.cost_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'CNY' ) "
          			+"                             AND pay_flag = 'Y' "
          			+"                     ),0) "
          			+"     else aco.cny "
          			+"     end  apply_pay_cny, "
          			+"     case  "
          			+"         when c.cost_order_id is not null then "
          			+"             ifnull( "
          			+"                     ( SELECT sum(exchange_total_amount) FROM job_order_arap "
          			+"                         WHERE id IN( SELECT ref_order_id FROM arap_cost_item WHERE cost_order_id IN(c.cost_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'JPY' ) "
          			+"                             AND pay_flag = 'Y' "
          			+"                     ), 0 ) "
          			+"     else aco.jpy "
          			+"     end  apply_pay_jpy, "
          			+"     case  "
          			+"         when c.cost_order_id is not null then "
          			+"             ifnull( "
          			+"                     ( SELECT sum(exchange_total_amount) FROM job_order_arap "
          			+"                         WHERE id IN( SELECT ref_order_id FROM arap_cost_item WHERE cost_order_id IN(c.cost_order_id) ) "
          			+"                             AND exchange_currency_id =( SELECT id FROM currency WHERE CODE = 'HKD' )" 
          			+"                             AND pay_flag = 'Y' "
          			+"                 ), 0 ) "
          			+"     else aco.hkd "
          			+"     end  apply_pay_hkd "
          			+" FROM arap_cost_order aco "
          			+" LEFT JOIN cost_application_order_rel c on c.cost_order_id = aco.id"
          			+" LEFT JOIN party p ON p.id = aco.sp_id"
          			+" LEFT JOIN user_login ul ON ul.id = aco.create_by"
          			+" WHERE aco.id in(" +ids +")"
          			+" group by aco.id ";

  		}else{
  			sql = " SELECT aco.*, p.company_name payee_name, '应付对账单' order_type,"
					+"  p.company_name cname, ifnull(ul.c_name, ul.user_name) creator_name,"
					+"  ifnull((SELECT sum(exchange_total_amount) from job_order_arap " 
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" ))"  
					+" and exchange_currency_id = (select id from currency where code='USD')"
					+" and pay_flag='Y' ),0) apply_pay_usd, "
					+" ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='CNY') "
					+" and pay_flag='Y'),0) apply_pay_cny,"
					+"  ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='JPY') "
					+" and pay_flag='Y'),0) apply_pay_jpy,"
					+"  ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" ))"  
					+" and exchange_currency_id = (select id from currency where code='HKD') "
					+" and pay_flag='Y'),0) apply_pay_hkd,"
					+"  ( "
					+"  aco.usd - ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='USD')"
					+" and pay_flag='Y' ),0) "
					+" ) wait_usd, "
					+" ( "
					+" aco.cny - ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='CNY') "
					+" and pay_flag='Y'),0) "
					+" ) wait_cny, "
					+" ( "
					+" aco.jpy - ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='JPY') "
					+" and pay_flag='Y'),0) "
					+" ) wait_jpy, "
					+" ( "
					+" aco.hkd - ifnull((SELECT sum(exchange_total_amount) from job_order_arap  "
					+" where id in(select ref_order_id from arap_cost_item where cost_order_id in ("+ids+" )) " 
					+" and exchange_currency_id = (select id from currency where code='HKD') "
					+" and pay_flag='Y'),0) "
					+" ) wait_hkd"
					+"  FROM arap_cost_order aco "
					+"  LEFT JOIN cost_application_order_rel caor on caor.cost_order_id = aco.id"
					+"  LEFT JOIN arap_cost_application_order acao on acao.id = caor.application_order_id"
					+"  LEFT JOIN party p ON p.id = aco.sp_id"
					+"  LEFT JOIN user_login ul ON ul.id = aco.create_by"
					+"  where acao.id="+application_id
					+"	GROUP BY aco.id";
  		}
  		
  		Map<String,Object> BillingOrderListMap = new HashMap<String,Object>();
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
            
        ArapCostApplication order = new ArapCostApplication();
   		String id = (String) dto.get("id");
   		String ids=(String) dto.get("ids");
   		String status = (String) dto.get("status");
		String selected_item_ids= (String) dto.get("selected_ids"); //获取申请单据的id,用于回显
		String sp_id=(String) dto.get("sp_id");
		String sp_name=(String) dto.get("sp_name");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		if (user==null) {
            return;
        }
   		long office_id=user.getLong("office_id");
   		
   		String action_type = "add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type = "update";
   			//update
   			order = ArapCostApplication.dao.findById(id);
   			DbUtils.setModelValues(dto, order); 
   			
   			//需后台处理的字段
   			if("复核不通过".equals(status)){
   				order.set("status", "新建");
   			}
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YFSQ", user.getLong("office_id")));
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
	   		CostApplicationOrderRel caor = null;
	   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
	   		if(itemList != null){
	   			for(Map<String, String> item :itemList){
					String action = item.get("action");
						   itemId = item.get("id");
					if("CREATE".equals(action)){
						caor = new CostApplicationOrderRel();
						caor.set("application_order_id", id);
						caor.set("job_order_arap_id", itemId);
						Record aci = Db.findFirst("select * from arap_cost_item where ref_order_id=?",itemId);
						Long cost_order_id=aci.getLong("cost_order_id");
						caor.set("cost_order_id", cost_order_id);
						caor.set("order_type", "应付对账单");
						caor.save();
						
						ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
						arapCostOrder.set("audit_status", "付款申请中").update();
				            
				          }
					}
	   		}
			
			//更新勾选的job_order_arap item creat_flag,改变创建标记位
		  String ySql ="update job_order_arap set create_flag='Y' where id in("+selected_item_ids+")";
		  Db.update(ySql);
	}
   		
   			//发票明细
   			List<Map<String, String>> InvoiceItem_list = (ArrayList<Map<String, String>>)dto.get("InvoiceItem_list");
   			String userId = user.getLong("id").toString();
   			Date date = new Date();
   			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
   			String dateString = formatter.format(date);
   			if(InvoiceItem_list!=null){
   				for (Map<String, String> rowMap : InvoiceItem_list) {//获取每一行
   		    		Model<?> model = (Model<?>) RequestInvoices.class.newInstance();
   		    		
   		    		String rowId = rowMap.get("id");
   		    		String action = rowMap.get("action");
   		    		if(StringUtils.isEmpty(rowId)){
   		    			if(!"DELETE".equals(action)){
   		    				DbUtils.setModelValues(rowMap, model);
   			    			model.set("order_id", id);
   			    			model.set("order_type", "costRequest");
   			    			model.set("creator", userId);
   			    			model.set("create_stamp", dateString);
   			    			model.save();	
   		    			}
   		    		}else{
   		    				if("DELETE".equals(action)  ){//delete
   		        				Model<?> deleteModel = model.findById(rowId);
   		            			deleteModel.delete();
   		            		}else{//UPDATE
   		            			Model<?> updateModel = model.findById(rowId);
   		            			DbUtils.setModelValues(rowMap, updateModel);
   		            			updateModel.update();
   		            		}
   		    		}
   				}
   			}   		
   		
   		
   		saveLog(jsonStr, id, user, action_type);
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		r.set("sp_name",sp_name);
   		r.set("idsArray", ids);
   		renderJson(r);
	}
    
  	private void saveLog(String json, String order_id, UserLogin user, String action_type) {
        Record rec = new Record();
        rec.set("log_type", "action");
        rec.set("operation_obj", "付款申请单");
        rec.set("action_type", action_type);
        rec.set("create_stamp", new Date());
        rec.set("user_id", user.get("id"));
        rec.set("order_id", order_id);
        rec.set("json", json);
        rec.set("sys_type", "forwarder");
        rec.set("office_id", user.getLong("office_id"));
        Db.save("sys_log", rec);
    }
  	
  	@Before(EedaMenuInterceptor.class)
  	public void edit() throws ParseException {
		String id = getPara("id");
		ArapCostApplication order = ArapCostApplication.dao.findById(id);
		
		Party p  = Party.dao.findById(order.getLong("sp_id"));
		if(p != null){
			setAttr("party", p);
		}
		String sql = "select group_concat(cast(cost_order_id as char) SEPARATOR ',') ids from cost_application_order_rel where application_order_id = ?";
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
		
		List<Record> list = getItems(id);
    	setAttr("docList", list);
    	
    	UserLogin user1 = LoginUserController.getLoginUser(this);
    	if (user1==null) {
            return;
        }
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("arap_cost_application_order", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
		Record r = order.toRecord();
		r.set("creator_name", creator_name);
		r.set("check_name", check_name);
		r.set("confirm_name", confirm_name);
		setAttr("order", r);

		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		String sql1 = "select cri.*,ul.id creator,ul.c_name creator_name,c.id currency_id,c.name currency_name from request_invoices cri "
				+ "	left join user_login ul on ul.id =cri.creator"
				+ " left join currency c on c.id =cri.currency_id"
				+ " where order_type='costRequest' and order_id="+id;
		List<Record> invoice_list = Db.find(sql1);
		setAttr("invoice_list", invoice_list);
		
		render("/oms/CostRequest/costEdit.html");
	}
  	
    @Before(Tx.class)
    public void sendMail(String order_id,String order_no,String creator_name) {
    	UserLogin userlogin = UserLogin.dao.findFirst("SELECT * from user_login where c_name='"+creator_name+"'");
    	String mailTitle = "您有一份复核不通过的付款申请单";
    	String mailContent = "付款申请单为<a href=\"http://www.esimplev.com/costRequest/edit?id="+order_id+"\">"+order_no+"</a>";
    	
    	Office office=Office.dao.findById(userlogin.get("office_id"));
        
        
        try{
    	MultiPartEmail email = new MultiPartEmail();  
        /*smtp.exmail.qq.com*/
        String HostName = office.getStr("host_name");
        int SmtpPort = office.getInt("smtp_port");
        email.setHostName(HostName);
        email.setSmtpPort(SmtpPort);
            //反查公司信息
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
        }catch(EmailException e){
        	e.printStackTrace();
        }
       
    }
  	
  	
  	
  	
  	
  	
    //复核
  	@Before(Tx.class)
    public void checkOrder(){
        String application_id=getPara("order_id");
        String selfId = getPara("selfId");
   		String ids = getPara("ids");
   		String order_no =getPara("order_no");
   		String creator_name =getPara("creator_name");
   		String invoice_no =getPara("invoice_no");
   		
        //更改原始单据状态
        List<Record> res = null;
        Record re1=new Record();
        if(StringUtils.isNotEmpty(application_id)){
		    	 ArapCostApplication order = ArapCostApplication.dao.findById(application_id);
		    	 if("cancelcheckBtn".equals(selfId)){
		    		 order.set("status", "复核不通过");
		    		 order.set("invoice_no", invoice_no);
		    		 sendMail(application_id,order_no,creator_name);
		    		 
		    	 }else{
		    		 order.set("status", "已复核");
		    	 }
		         order.set("check_by", LoginUserController.getLoginUserId(this));
		         order.set("check_stamp", new Date()).update();
        		 res = Db.find("select * from cost_application_order_rel where application_order_id = ?",application_id);
        		 
        		 long check_by = order.getLong("check_by");
    	   		 String user_name = LoginUserController.getUserNameById(check_by);
    	  		 re1 = order.toRecord();
    	  		 re1.set("check_name",user_name);
        }
        if(StringUtils.isNotEmpty(ids)){
        	String[] arr= ids.split(",");
        	for(int i=0;i<arr.length;i++){
        		String id=arr[i];
	        	ArapCostApplication order = ArapCostApplication.dao.findById(id);
		         order.set("status", "已复核");
		         order.set("check_by", LoginUserController.getLoginUserId(this));
		         order.set("check_stamp", new Date()).update();
		         
        	}
        	String str="select * from cost_application_order_rel where application_order_id in ( "+ids+" )";
    	   res = Db.find(str);
    	   re1.set("ids",ids);
        }
        if(res != null){
        	for (Record re : res) {
      			Long id = re.getLong("cost_order_id");
      			String order_type = re.getStr("order_type");

      			if("应付对账单".equals(order_type)){
    			    ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(id);
    				arapCostOrder.set("audit_status", "已复核").update();
    			}
      		}
        }
  		
  		renderJson(re1);
    }
  	
  	
    //付款确认
  	@Before(Tx.class)
	public void confirmOrder(){
 		UserLogin user = LoginUserController.getLoginUser(this);
 		if (user==null) {
            return;
        }
  		String jsonStr=getPara("params");
 
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String ids = getPara("ids");
  		String application_id=getPara("application_id");
  		
  		String confirmVal=getPara("confirmVal");
  		String pay_remark="";
  		try{
  			 pay_remark=(String) dto.get("pay_remark");
  		}catch(Exception e){
  			e.printStackTrace();
  		}
  		if(StringUtils.isNotEmpty(application_id)){
  			if("坏账确认".equals(confirmVal)){
  				ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
  				arapCostInvoiceApplication.set("status", "该笔为坏账");
  				arapCostInvoiceApplication.set("confirm_by", user.get("id"));
  				arapCostInvoiceApplication.set("confirm_stamp", new Date());
                if(StringUtils.isNotEmpty(pay_remark)){
		        	  arapCostInvoiceApplication.set("pay_remark", pay_remark);
		        }
		          arapCostInvoiceApplication.update();
              //坏账的标记位
        		String paySql ="update job_order_arap set pay_flag='B' "
	      				+ " where id in (SELECT job_order_arap_id FROM cost_application_order_rel WHERE application_order_id ="+application_id+")" ;  //chargeOrderId.substring(1) 去掉第一位
                Db.update(paySql);
	              //更改原始单据状态
	            List<Record> res = Db.find("select * from cost_application_order_rel where application_order_id = ?",application_id);
                for (Record re : res) {
	    			Long cost_order_id = re.getLong("cost_order_id");
	    			String order_type = re.getStr("order_type");
          			if("应付对账单".equals(order_type)){
		  				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
		                  Double usd = arapCostOrder.getDouble("usd");
		                  Double cny = arapCostOrder.getDouble("cny");
		                  Double hkd = arapCostOrder.getDouble("hkd");
		                  Double jpy = arapCostOrder.getDouble("jpy");

                        String sql = "SELECT "
		                  		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_cny,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_usd,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_jpy,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_hkd ";
		                     
		                     Record r = Db.findFirst(sql);
		                     Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
		                     Double paid_usd = r.getDouble("paid_usd");
		                     Double paid_jpy = r.getDouble("paid_jpy");
		                     Double paid_hkd = r.getDouble("paid_hkd");
                                           
        				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
        					arapCostOrder.set("audit_status", "部分坏账款").update();
        				}else{
        					arapCostOrder.set("audit_status", "坏账款").update();
        				}
        			}
          		}
        	}else{
	  			String receive_time = (String) dto.get("receive_time");
	        	String receive_bank_id = "";
	        	String payment_method = (String) dto.get("payment_method");
	        	
	        	if(dto.get("receive_bank_id")!=null){
	      			 receive_bank_id =  (String)dto.get("receive_bank_id");
	      		}else{
	      			String str2="select id from fin_account where bank_name='现金' and office_id="+user.get("office_id");
	      	        Record rec = Db.findFirst(str2);
	      	        if(rec!=null){
	      	        	receive_bank_id = rec.getLong("id").toString();
	      	        }
	      		}
	        	ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(application_id);
		          arapCostInvoiceApplication.set("status", "已付款");
		          arapCostInvoiceApplication.set("receive_time", receive_time);
		          arapCostInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
		          arapCostInvoiceApplication.set("confirm_stamp", new Date());
		          if(StringUtils.isNotEmpty(pay_remark)){
		        	  arapCostInvoiceApplication.set("pay_remark", pay_remark);
		          }
		          arapCostInvoiceApplication.update();
		        //已付款的标记位
		      		String paySql ="update job_order_arap set pay_flag='Y' "
		      				+ " where id in (SELECT job_order_arap_id FROM cost_application_order_rel WHERE application_order_id ="+application_id+")" ; 
		             Db.update(paySql);
		              //更改原始单据状态
		            List<Record> res = Db.find("select * from cost_application_order_rel where application_order_id = ?",application_id);
		    		for (Record re : res) {
		    			Long cost_order_id = re.getLong("cost_order_id");
		    			String order_type = re.getStr("order_type");
	
		    			if("应付对账单".equals(order_type)){
		  				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
		                  Double usd = arapCostOrder.getDouble("usd");
		                  Double cny = arapCostOrder.getDouble("cny");
		                  Double hkd = arapCostOrder.getDouble("hkd");
		                  Double jpy = arapCostOrder.getDouble("jpy");
		                  if(usd==null)usd=0.0;
		     			 if(cny==null)cny=0.0;
		     			 if(hkd==null)hkd=0.0;
		     			 if(jpy==null)jpy=0.0;
		     			 
		                  String sql = "SELECT "
		                  		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_cny,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_usd,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_jpy,"
		          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
		          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.cost_order_id="+cost_order_id
		          				+" ),0) paid_hkd ";
		                     
		                     Record r = Db.findFirst(sql);
		                     Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
		                     Double paid_usd = r.getDouble("paid_usd");
		                     Double paid_jpy = r.getDouble("paid_jpy");
		                     Double paid_hkd = r.getDouble("paid_hkd");
		                     
		  				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
		  					arapCostOrder.set("audit_status", "部分已付款").update();
		  				}else{
		  					arapCostOrder.set("audit_status", "已付款").update();
		  				}
		  			}
		    		}
		    		//新建日记账表数据
		      		String cny_pay_amount ="0.0"; 
		      		if(arapCostInvoiceApplication.getDouble("modal_cny")!=null)
		      			cny_pay_amount=arapCostInvoiceApplication.getDouble("modal_cny").toString();
		      		if(!"0.0".equals(cny_pay_amount)&&StringUtils.isNotEmpty(cny_pay_amount)){
		      			createAuditLog(application_id, payment_method, receive_bank_id, receive_time, cny_pay_amount, "CNY");
		      		}
		            String usd_pay_amount = "0.0"; 
		            if(arapCostInvoiceApplication.getDouble("modal_usd")!=null)
		            	usd_pay_amount =arapCostInvoiceApplication.getDouble("modal_usd").toString();
		            if(!"0.0".equals(usd_pay_amount)&&StringUtils.isNotEmpty(usd_pay_amount)){
		            	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, usd_pay_amount, "USD");
		            }
		            
		            String jpy_pay_amount = "0.0";
		            if(arapCostInvoiceApplication.getDouble("modal_jpy")!=null)
		            	jpy_pay_amount =arapCostInvoiceApplication.getDouble("modal_jpy").toString();
		            if(!"0.0".equals(jpy_pay_amount)&&StringUtils.isNotEmpty(jpy_pay_amount)){
		            	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, jpy_pay_amount, "JPY");
		            }
		            String hkd_pay_amount = "0.0";
		            if(arapCostInvoiceApplication.getDouble("modal_hkd")!=null)
		            	hkd_pay_amount =arapCostInvoiceApplication.getDouble("modal_hkd").toString();
		            if(!"0.0".equals(hkd_pay_amount)&&StringUtils.isNotEmpty(hkd_pay_amount)){
		            	createAuditLog(application_id, payment_method, receive_bank_id, receive_time, hkd_pay_amount, "HKD");
		            }
        	}
  		}
  		if(StringUtils.isNotEmpty(ids)){
  			String[] arr= ids.split(",");
  	  		pay_remark=getPara("pay_remark");
	        for(int i=0;i<arr.length;i++){
	        	String id=arr[i];
	        	
	        	if("坏账确认".equals(confirmVal)){
	  				ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(id);
	  				arapCostInvoiceApplication.set("status", "该笔为坏账");
	  				arapCostInvoiceApplication.set("confirm_by", user.get("id"));
	  				arapCostInvoiceApplication.set("confirm_stamp", new Date());
	                if(StringUtils.isNotEmpty(pay_remark)){
			        	  arapCostInvoiceApplication.set("pay_remark", pay_remark);
			        }
			          arapCostInvoiceApplication.update();
	              //坏账的标记位
	        		String paySql ="update job_order_arap set pay_flag='B' "
		      				+ " where id in (SELECT job_order_arap_id FROM cost_application_order_rel WHERE application_order_id ="+id+")" ;  //chargeOrderId.substring(1) 去掉第一位
	                Db.update(paySql);
		              //更改原始单据状态
		            List<Record> res = Db.find("select * from cost_application_order_rel where application_order_id = ?",id);
	                for (Record re : res) {
		    			Long cost_order_id = re.getLong("cost_order_id");
		    			String order_type = re.getStr("order_type");
	          			if(order_type.equals("应付对账单")){
			  				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
			                  Double usd = arapCostOrder.getDouble("usd");
			                  Double cny = arapCostOrder.getDouble("cny");
			                  Double hkd = arapCostOrder.getDouble("hkd");
			                  Double jpy = arapCostOrder.getDouble("jpy");

	                        String sql = "SELECT "
			                  		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_cny,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_usd,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_jpy,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_hkd ";
			                     
			                     Record r = Db.findFirst(sql);
			                     Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
			                     Double paid_usd = r.getDouble("paid_usd");
			                     Double paid_jpy = r.getDouble("paid_jpy");
			                     Double paid_hkd = r.getDouble("paid_hkd");
	                                           
	        				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
	        					arapCostOrder.set("audit_status", "部分坏账款").update();
	        				}else{
	        					arapCostOrder.set("audit_status", "坏账款").update();
	        				}
	        			}
	          		}
	        	}else{
			        	String receive_time =getPara("receive_time");
			        	String receive_bank_id = "";
		        	
			        	ArapCostApplication arapCostInvoiceApplication = ArapCostApplication.dao.findById(id);
			        	String payment_method = arapCostInvoiceApplication.get("payment_method") ;
			        	
			        	if(arapCostInvoiceApplication.get("deposit_bank")!=null){
			        		receive_bank_id = arapCostInvoiceApplication.getLong("deposit_bank").toString();
			        	}
			        	if(StringUtils.isEmpty(receive_bank_id)){
			      			String str2="select id from fin_account where bank_name='现金' and office_id="+user.get("office_id");
			      	        Record rec = Db.findFirst(str2);
			      	        if(rec!=null){
			      	        	receive_bank_id = rec.getLong("id").toString();
			      	        }
			      		}
				          arapCostInvoiceApplication.set("status", "已付款");
				          arapCostInvoiceApplication.set("receive_time", receive_time);
				          arapCostInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
				          arapCostInvoiceApplication.set("confirm_stamp", new Date());
				          arapCostInvoiceApplication.set("pay_remark", pay_remark);
				          arapCostInvoiceApplication.update();
			        	//已付款的标记位
			      		String paySql ="update job_order_arap set pay_flag='Y' "
		      				+ " where id in (SELECT job_order_arap_id FROM cost_application_order_rel WHERE application_order_id ="+id+")" ; 
			      		Db.update(paySql);
		              //更改原始单据状态
			      		List<Record> res = Db.find("select * from cost_application_order_rel where application_order_id = ?",id);
			    		for (Record re : res) {
			    			Long cost_order_id = re.getLong("cost_order_id");
			    			String order_type = re.getStr("order_type");
			
			    			if(order_type.equals("应付对账单")){
			  				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
			                  Double usd = arapCostOrder.getDouble("usd");
			                  Double cny = arapCostOrder.getDouble("cny");
			                  Double hkd = arapCostOrder.getDouble("hkd");
			                  Double jpy = arapCostOrder.getDouble("jpy");
			                  if(usd==null)usd=0.0;
			     			 if(cny==null)cny=0.0;
			     			 if(hkd==null)hkd=0.0;
			     			 if(jpy==null)jpy=0.0;
			     			 
			                  String sql = "SELECT "
			                  		+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =3 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_cny,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =6 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_usd,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =8 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_jpy,"
			          				+" IFNULL((SELECT SUM(joa.exchange_total_amount) from  job_order_arap joa LEFT JOIN arap_cost_item aci on joa.id = aci.ref_order_id"
			          				+" where joa.create_flag = 'Y' AND joa.exchange_currency_id =9 and aci.cost_order_id="+cost_order_id
			          				+" ),0) paid_hkd ";
			                     
			                     Record r = Db.findFirst(sql);
			                     Double paid_cny = r.getDouble("paid_cny");//greate_flay=Y的arap item 汇总金额
			                     Double paid_usd = r.getDouble("paid_usd");
			                     Double paid_jpy = r.getDouble("paid_jpy");
			                     Double paid_hkd = r.getDouble("paid_hkd");
			                     
			  				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
			  					arapCostOrder.set("audit_status", "部分已付款").update();
			  				}else{
			  					arapCostOrder.set("audit_status", "已付款").update();
			  				}
			  			}
			    	 }
			            //新建日记账表数据
			      		String cny_pay_amount ="0.0"; 
			      		if(arapCostInvoiceApplication.getDouble("modal_cny")!=null)
			      			cny_pay_amount=arapCostInvoiceApplication.getDouble("modal_cny").toString();
			      		if(!"0.0".equals(cny_pay_amount)&&StringUtils.isNotEmpty(cny_pay_amount)){
			      			createAuditLog(id, payment_method, receive_bank_id, receive_time, cny_pay_amount, "CNY");
			      		}
			            String usd_pay_amount = "0.0"; 
			            if(arapCostInvoiceApplication.getDouble("modal_usd")!=null)
			            	usd_pay_amount =arapCostInvoiceApplication.getDouble("modal_usd").toString();
			            if(!"0.0".equals(usd_pay_amount)&&StringUtils.isNotEmpty(usd_pay_amount)){
			            	createAuditLog(id, payment_method, receive_bank_id, receive_time, usd_pay_amount, "USD");
			            }
			            
			            String jpy_pay_amount = "0.0";
			            if(arapCostInvoiceApplication.getDouble("modal_jpy")!=null)
			            	jpy_pay_amount =arapCostInvoiceApplication.getDouble("modal_jpy").toString();
			            if(!"0.0".equals(jpy_pay_amount)&&StringUtils.isNotEmpty(jpy_pay_amount)){
			            	createAuditLog(id, payment_method, receive_bank_id, receive_time, jpy_pay_amount, "JPY");
			            }
			            String hkd_pay_amount = "0.0";
			            if(arapCostInvoiceApplication.getDouble("modal_hkd")!=null)
			            	hkd_pay_amount =arapCostInvoiceApplication.getDouble("modal_hkd").toString();
			            if(!"0.0".equals(hkd_pay_amount)&&StringUtils.isNotEmpty(hkd_pay_amount)){
			            	createAuditLog(id, payment_method, receive_bank_id, receive_time, hkd_pay_amount, "HKD");
			            }
			        }
	        }
  		}
        Record r = new Record();
		r.set("confirm_name", LoginUserController.getUserNameById(LoginUserController.getLoginUserId(this)));
		r.set("status", "已付款");
		r.set("ids", ids);
        renderJson(r);
    }
  	
  	private void createAuditLog(String application_id, String payment_method,
            String receive_bank_id, String receive_time, String pay_amount, String currency_code) {
        //新建日记账表数据\
  		UserLogin user = LoginUserController.getLoginUser(this);
  		if (user==null) {
            return;
        }
        long office_id = user.getLong("office_id");
		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", payment_method);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_COST);
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
        auditLog.set("source_order", "应付申请单");
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
			order.set("type", "cost");
			order.set("upload_time", new Date());
			order.save();
		}

    	renderJson(order);
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	
    	List<Record> list = getItems(order_id);
    	
    	Map<String,Object> map = new HashMap<String,Object>();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());

        map.put("aaData", list);

        renderJson(map); 
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
        +"       from  job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select caor.job_order_arap_id from cost_application_order_rel caor where caor.application_order_id="+appOrderId+")";
		
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
                if("cost".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=exchange_amount);
                }else{
                    exchangeTotalMap.put(name, 0-rec.getDouble("exchange_total_amount"));
                }
            }else{
                if("cost".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=rec.getDouble("exchange_total_amount"));
                }else{
                    exchangeTotalMap.put(name, exchange_amount-=rec.getDouble("exchange_total_amount"));
                }        
            }
        }
		
		Record order = Db.findById("arap_cost_application_order", appOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		if(order.get("return_confirm_stamp")!=null){
			order.set("return_confirm_stamp", order.get("return_confirm_stamp"));
		}else{
			order.set("return_confirm_stamp", new Date());
		}
		Db.update("arap_cost_application_order", order);
		return exchangeTotalMap;
    }
    //添加明细的查询
    public void itemList(){
    	String checked = getPara("checked");
        //String sLimit = "";
        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	 sql = "select * from(  "
        			+ " select joa.order_type sql_type, joa.id,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
              		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.order_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no,jo.type, "
              		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
              		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
              		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
              		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
              		+ " ) after_total,"
              		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
              		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
              		+ " after_rate_total,ifnull(f.name,f.name_eng) fee_name,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
      				+ " from job_order jo "
      				+ " left join job_order_arap joa on jo.id=joa.order_id "
      				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
      				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
      				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
      				+ " left join party p on p.id=joa.sp_id "
      				+ " left join party p1 on p1.id=jo.customer_id "
      				+ " left join location l on l.id=jos.fnd "
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
                 		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
                 		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
                 		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
                 		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
                 		+ " ) after_total,"
                 		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
                 		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
                 		+ " after_rate_total,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
         				+ " from job_order jo "
         				+ " left join job_order_arap joa on jo.id=joa.order_id "
         				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
         				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
         				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
         				+ " left join party p on p.id=joa.sp_id "
         				+ " left join party p1 on p1.id=jo.customer_id "
         				+ " left join location l on l.id=jos.fnd "
         				+ " left join currency cur on cur.id=joa.currency_id "
         				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
         				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
         				+ " left join fin_item f on f.id = joa.charge_id"
         				+ " LEFT JOIN arap_cost_item aci ON aci.ref_order_id = joa.id "
         				+ " LEFT JOIN arap_cost_order aco ON aco.id = aci.cost_order_id "
         				+ " where joa.audit_flag='Y' and joa.billconfirm_flag = 'Y' and joa.create_flag='N' and aco.office_id = "+office_id
         				+ " and jo.delete_flag = 'N'"
         				+ " GROUP BY joa.id "
         				+ " ) B where 1=1 ";
        			}
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition);
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    public void insertCostItem(){
    	String itemList= getPara("cost_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String appOrderId=getPara("order_id");
    	//ArapCostApplication order = ArapCostApplication.dao.findById(appOrderId);
    	
   		CostApplicationOrderRel caor = null;
		for(String item :itemArray){
				caor = new CostApplicationOrderRel();
				caor.set("application_order_id", appOrderId);
				caor.set("job_order_arap_id", item);
				Record re = Db.findFirst("select * from arap_cost_item where ref_order_id=?",item);
				Long cost_order_id=re.getLong("cost_order_id");
				caor.set("cost_order_id", cost_order_id);
				caor.set("order_type", "应付对账单");
				caor.save();
				
				ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
				arapCostOrder.set("audit_status", "付款申请中").update();
				//更新job_order_arap的create_flag
				JobOrderArap joa=JobOrderArap.dao.findById(item);
				joa.set("create_flag", "Y");
				joa.update();
		}
    	//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(appOrderId);
		exchangeTotalMap.put("appOrderId", Double.parseDouble(appOrderId));
    	renderJson(exchangeTotalMap);
    }
    public void deleteCostItem(){
    	String appOrderId=getPara("order_id");
    	String itemid=getPara("cost_itemid");
    	if(itemid !=null&& appOrderId!=null){
    		 Db.deleteById("cost_application_order_rel","job_order_arap_id,application_order_id",itemid,appOrderId);
    		 Record re = Db.findFirst("select * from arap_cost_item where ref_order_id=?",itemid);
			 Long cost_order_id=re.getLong("cost_order_id");
    		 ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(cost_order_id);
			 //arapCostOrder.set("audit_status", "").update();
    		 
    		 JobOrderArap jobOrderArap = JobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("create_flag", "N");
             jobOrderArap.update();
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(appOrderId);
    			exchangeTotalMap.put("appOrderId", Double.parseDouble(appOrderId));
    	    	renderJson(exchangeTotalMap);
    }

}
