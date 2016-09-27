package controllers.arap.ar;

import interceptor.SetAttrLoginUserInterceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapChargeApplication;
import models.ArapChargeInvoice;
import models.ArapChargeOrder;
//import models.ChargeAppOrderRel;
import models.ChargeApplicationOrderRel;
import models.Party;
import models.UserLogin;
import models.yh.arap.chargeMiscOrder.ArapMiscChargeOrder;
import models.yh.arap.inoutorder.ArapInOutMiscOrder;
import models.yh.damageOrder.DamageOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
public class ChargeAcceptOrderController extends Controller {
    private Log logger = Log.getLog(ChargeAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COLLECTIONCONFIRM_LIST})
    public void index() {
    	render("/eeda/arap/ChargeAcceptOrder/ChargeAcceptOrderList.html");
    }
    
    
    public void create() {
		String idsArray = getPara("idsArray");
		setAttr("idsArray", idsArray);
		
		String sp_id = "";
		String[] orderArrId=idsArray.split(",");
		for (int i=0;i<orderArrId.length;i++) {
			String[] array = orderArrId[0].split(":");
			String id = array[0];
			String order_type = array[1];
			if("应收对账单".equals(order_type)){
				ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				//sp_id = arapChargeOrder.getLong("sp_id").toString();
			}else if("应收开票单".equals(order_type)){
				ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				//sp_id = arapChargeInvoice.getLong("sp_id").toString();
			}
		}

		setAttr("sp_id", sp_id);
			
		List<Record> Account = null;
		Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/ChargeAcceptOrder/chargeEdit.html");
	}
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " select * from (SELECT "
        		+ " aco.id, aco.order_no, '应收对账单' order_type, aco.check_amount total_amount, "
        		+ " sum(ifnull(caor.receive_amount,0)) receive_amount, aco.status,"
        		+ " aco.invoice_no, p.abbr payee_name, aco.remark "
        		+ " FROM"
        		+ " arap_charge_order aco"
        		+ " LEFT JOIN party p ON p.id = aco.sp_id"
        		+ " LEFT JOIN charge_application_order_rel caor ON caor.charge_order_id = aco.id and caor.order_type = '应收对账单'"
        		+ " where aco.have_invoice = 'N' and aco.status != '新建'"
        		+ " GROUP BY aco.id"
        		+ " union"
        		+ " SELECT"
        		+ " aci.id, aci.order_no, '应收开票单' order_type, aci.total_amount total_amount,"
        		+ " sum(ifnull(caor.receive_amount,0)) receive_amount, aci.status, aco.invoice_no, p.abbr payee_name,"
        		+ " aci.remark "
        		+ " FROM arap_charge_invoice aci"
        		+ " LEFT JOIN party p ON p.id = aci.sp_id"
        		+ " LEFT JOIN arap_charge_order aco on aco.invoice_order_id = aci.id"
        		+ " LEFT JOIN charge_application_order_rel caor ON caor.charge_order_id = aci.id and caor.order_type = '应收开票单'"
        		+ " where aci.status != '新建'"
        		+ " GROUP BY aci.id"
        		+ " ) A where total_amount>receive_amount ";
		
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
    
    
    public void applicationList() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(  "
        		+ " select acao.id,acao.order_no,acao.status,'申请单' order_type,acao.total_amount,acao.create_stamp, "
        		+ " acao.remark,p.abbr payee_name,ul.c_name create_name "
				+ " from arap_charge_application_order acao "
				+ " left join user_login ul on ul.id = acao.create_by "
				+ " left join party p on p.id = acao.sp_id "
				+ " ) A where 1=1 ";
		
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

  	//新逻辑
  	public void chargeOrderList() {
          String ids = getPara("idsArray");
          String application_id = getPara("application_id");
          String dz_id ="" ;//应收对账单
          String kpjl_id = "";//应收开票单
          String sql = "";
          
          if(application_id.equals("")){
          	if(!application_id.equals(ids)){
          		String[] orderArrId=ids.split(",");
   				for (int i=0;i<orderArrId.length;i++) {
   					String[] one=orderArrId[i].split(":");
   					String id = one[0];
   					String orderType = one[1];
   					
   					
   					if("应收对账单".equals(orderType)){
   						dz_id += id+",";
   					}else if("应收开票单".equals(orderType)){
   						kpjl_id += id+",";
   					}
   				}
   				if(!dz_id.equals(""))
   					dz_id = dz_id.substring(0, dz_id.length()-1);
   				else
   					dz_id = "''";
   				if(!kpjl_id.equals(""))
   					kpjl_id = kpjl_id.substring(0, kpjl_id.length()-1);
   				else
   					kpjl_id = "''";
          	}
  	       
  			
  		
  			sql = " SELECT aco.id,aco.sp_id,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
  					+ " p.abbr sp_name, ifnull(ul.c_name, ul.user_name) creator_name, aco.check_amount total_amount,"
  					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单' "
  					+ " ) receive_amount,"
  					+ " (aco.check_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
  					+ " FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单'"
  					+ " )) noreceive_amount"
  					+ " FROM arap_charge_order aco "
  					+ " LEFT JOIN party p ON p.id = aco.sp_id"
  					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
  					+ " WHERE "
  					+ " aco.id in(" + dz_id +")"
  					+ " union "
  					+ " SELECT aci.id, aci.sp_id sp_id,aci.order_no, '应收开票单' order_type, aci. STATUS, aci.remark, "
  					+ " aci.create_stamp create_stamp, p.abbr sp_name, ifnull(ul.c_name, ul.user_name) creator_name, aci.total_amount, "
  					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
  					+ "  FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '应收开票单' ) receive_amount, "
  					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
  					+ " charge_application_order_rel caor"
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '应收开票单' ) ) noreceive_amount"
  					+ " FROM arap_charge_invoice aci "
  					+ " LEFT OUTER JOIN party p ON aci.sp_id = p.id "
  					+ " LEFT OUTER JOIN contact c ON c.id = p.contact_id "
  					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
  					+ " LEFT OUTER JOIN office o ON ul.office_id = o.id "
  					+ " WHERE aci.id in(" + kpjl_id +")";

  		}else{
  			sql = " select *  from (SELECT caor.id,aco.sp_id,aco.order_no, '应收对账单' order_type, aco.STATUS, aco.remark, aco.create_stamp,"
  					+ " p.abbr sp_name, ifnull(ul.c_name, ul.user_name) creator_name, aco.check_amount total_amount,"
  					+ " ( SELECT ifnull(sum(caor.receive_amount),0) FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id and caor.application_order_id = aciao.id AND caor.order_type = '应收对账单' ) receive_amount,"
  					+ " (aco.check_amount - (SELECT ifnull(sum(caor.receive_amount), 0) "
  					+ " FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aco.id AND caor.order_type = '应收对账单'  )) noreceive_amount, aciao.id app_id "
  					+ " FROM arap_charge_order aco "
  					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aco.id"
  					+ " LEFT JOIN arap_charge_application_order aciao on aciao.id = caor.application_order_id"
  					+ " LEFT JOIN party p ON p.id = aco.sp_id"
  					+ " LEFT JOIN user_login ul ON ul.id = aco.create_by"
  					+ " where caor.order_type = '应收对账单'"
  					+ " union "
  					+ " SELECT caor.id, aci.sp_id sp_id,  aci.order_no, '应收开票单' order_type, aci. STATUS, aci.remark, "
  					+ " aci.create_stamp create_stamp, p.abbr sp_name, ifnull(ul.c_name, ul.user_name) creator_name, aci.total_amount total_amount, "
  					+ " ( SELECT ifnull(sum(caor.receive_amount), 0)"
  					+ "  FROM charge_application_order_rel caor "
  					+ " WHERE caor.charge_order_id = aci.id and caor.application_order_id = aciao.id AND caor.order_type = '应收开票单' ) receive_amount, "
  					+ " ( aci.total_amount - ( SELECT ifnull(sum(caor.receive_amount), 0) FROM "
  					+ " charge_application_order_rel caor"
  					+ " WHERE caor.charge_order_id = aci.id AND caor.order_type = '应收开票单' ) ) noreceive_amount, aciao.id app_id  "
  					+ " FROM arap_charge_invoice aci "
  					+ " LEFT JOIN charge_application_order_rel caor on caor.charge_order_id = aci.id"
  					+ " LEFT JOIN arap_charge_application_order aciao on aciao.id = caor.application_order_id"
  					+ " LEFT OUTER JOIN party p ON aci.sp_id = p.id "
  					+ " LEFT OUTER JOIN user_login ul ON aci.create_by = ul.id"
  					+ " where caor.order_type = '应收开票单'"
  					+ " ) A where app_id ="+application_id ;	    
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
	public void save() {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        ArapChargeApplication order = new ArapChargeApplication();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
		
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			order = ArapChargeApplication.dao.findById(id);
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("update_by", user.getLong("id"));
   			order.set("update_stamp", new Date());
   			order.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("YSSQ"));
   			order.set("create_by", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}
		
   		
   		ChargeApplicationOrderRel caor = null;
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		for(Map<String, String> item :itemList){
			String action = item.get("action");
			String itemId = item.get("id");
			String amount = item.get("amount");
			String order_type = item.get("order_type");
			if("CREATE".equals(action)){
				caor = new ChargeApplicationOrderRel();
				caor.set("application_order_id", id);
				caor.set("charge_order_id", itemId);
				caor.set("order_type", order_type);
				caor.set("receive_amount", amount);
				caor.save();
				
                if("应收对账单".equals(order_type)){
					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(itemId);
					arapChargeOrder.set("status", "收款申请中").update();
				}else if("应收开票单".equals(order_type)){
					ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(itemId);
					arapChargeInvoice.set("status", "收款申请中").update();
				}
			}else{
				caor = ChargeApplicationOrderRel.dao.findById(itemId);
				caor.set("receive_amount", amount);
				caor.update();
			}
		}
		
		long create_by = order.getLong("create_by");
   		String user_name = LoginUserController.getUserNameById(create_by);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
	}
  	
  	public void edit() throws ParseException {
		String id = getPara("id");
		ArapChargeApplication order = ArapChargeApplication.dao.findById(id);
		
		Party p  = Party.dao.findById(order.getLong("sp_id"));
		if(p != null){
			setAttr("party", p);
		}
		
		UserLogin userLogin = null;
		userLogin = UserLogin.dao .findById(order.get("create_by"));
		String creator_name = userLogin.get("c_name");
		
		userLogin = UserLogin.dao .findById(order.get("check_by"));
		String check_name = null;
		if(userLogin != null){
			check_name = userLogin.get("c_name");
		}
		
		Record r = order.toRecord();
		r.set("creator_name", creator_name);
		r.set("check_name", check_name);
		setAttr("order", r);

		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/ChargeAcceptOrder/chargeEdit.html");
	}
  	
  	
    //复核
  	@Before(Tx.class)
    public void checkOrder(){
        String application_id=getPara("order_id");
          
        ArapChargeApplication order = ArapChargeApplication.dao.findById(application_id);
        order.set("status", "已复核");
        order.set("check_by", LoginUserController.getLoginUserId(this));
        order.set("check_stamp", new Date()).update();
     
        //更改原始单据状态
        List<Record> res = Db.find("select * from charge_application_order_rel where application_order_id = ?",application_id);
  		for (Record re : res) {
  			Long id = re.getLong("charge_order_id");
  			String order_type = re.getStr("order_type");

  			if("应收对账单".equals(order_type)){
			    ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(id);
				arapChargeOrder.set("status", "已复核").update();
			}else if("应收开票单".equals(order_type)){
			    ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(id);
				arapChargeInvoice.set("status", "已复核").update();
			}
  		}
  		  
  		long check_by = order.getLong("check_by");
   		String user_name = LoginUserController.getUserNameById(check_by);
  		  
  		Record re = order.toRecord();
  		re.set("check_name",user_name);
  	    renderJson(re);
    }
  	
  	
    //收款确认
  	@Before(Tx.class)
	public void confirmOrder(){
  		String jsonStr=getPara("params");
  		int receive_bank = 4;
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
   		String id = (String) dto.get("id");
   		String receive_type = (String) dto.get("receive_type");
   		if(StringUtils.isNotEmpty((String)dto.get("receive_bank"))){
   			receive_bank = Integer.parseInt((String)dto.get("receive_bank")) ;
   		}
   		
   		String receive_time = (String) dto.get("receive_time");
   		String receive_amount = (String) dto.get("receive_amount");
   		
        ArapChargeApplication arapChargeInvoiceApplication = ArapChargeApplication.dao.findById(id);
        arapChargeInvoiceApplication.set("status", "已收款");
        arapChargeInvoiceApplication.set("receive_type", receive_type);
        arapChargeInvoiceApplication.set("receive_bank_id", receive_bank);
        arapChargeInvoiceApplication.set("receive_time", receive_time);
        arapChargeInvoiceApplication.set("confirm_by", LoginUserController.getLoginUserId(this));
        arapChargeInvoiceApplication.set("confirm_stamp", new Date());
        arapChargeInvoiceApplication.update();
          
        //更改原始单据状态
        List<Record> res = Db.find("select * from charge_application_order_rel where application_order_id = ?",id);
  		for (Record re : res) {
  			Long charge_order_id = re.getLong("charge_order_id");
  			String order_type = re.getStr("order_type");

  			if("应收对账单".equals(order_type)){
			    ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(charge_order_id);
				arapChargeOrder.set("status", "已收款").update();
			}else if("应收开票单".equals(order_type)){
			    ArapChargeInvoice arapChargeInvoice = ArapChargeInvoice.dao.findById(charge_order_id);
				arapChargeInvoice.set("status", "已收款").update();
			}
  		}
          
        //新建日记账表数据
  		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", receive_type);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_CHARGE);
        auditLog.set("amount", receive_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("account_id", receive_bank);
        auditLog.set("source_order", "应收申请单");
        auditLog.set("invoice_order_id", id);
        auditLog.save();
                  
        renderJson("{\"success\":true}");  
    }
}
