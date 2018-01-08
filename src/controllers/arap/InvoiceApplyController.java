package controllers.arap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.ArapChargeApplication;
import models.ArapChargeOrder;
import models.ChargeApplicationOrderRel;
import models.ChargeInvoiceOrder;
import models.ChargeInvoiceOrderItem;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;

import org.apache.commons.lang.StringUtils;
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

import controllers.arap.ar.chargeRequest.ChargeRequestController;
import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InvoiceApplyController  extends Controller {
	private Log logger = Log.getLog(ChargeRequestController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
	public void index() {
		render("/oms/InvoiceApply/list.html");
	}
    
    @Before(EedaMenuInterceptor.class) 
    public void create() {
		render("/oms/InvoiceApply/create.html");
	}
    
    @Before(EedaMenuInterceptor.class)
  	public void edit() throws ParseException {
    	String id = getPara("id");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
    	String sql = "select cio.*,p.abbr party_name,u.c_name creator_name,c.name currency_name,fa.bank_name bank_name from charge_invoice_order cio "
    			   + " LEFT JOIN party p on p.id = cio.party_id "
    			   + " LEFT JOIN user_login u on u.id = cio.creator"
    			   + " LEFT JOIN currency c on c.id = cio.currency "
    			   + " LEFT JOIN fin_account fa on fa.id=cio.bank_id"
    			   + " where cio.office_id = "+office_id
    			   + " and cio.id = "+id;
    	 Record order = Db.findFirst(sql);
    	 Record sp = Db.findFirst("select identification_no from party where id="+order.get("party_id"));
    	 order.set("identification_no", sp.get("identification_no"));
         setAttr("order", order);
         
    	render("/oms/InvoiceApply/edit.html");
    }
    
    public void list() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
			return;
		}
        long office_id=user.getLong("office_id");
    	String sql = "select cio.id,cio.order_no,cio.party_id,p.abbr party_name,cio.invoice_no,cio.status,cio.creator,u.c_name creator_name,cio.create_time from charge_invoice_order cio "
    			   + " LEFT JOIN party p on p.id=cio.party_id "
    			   + " LEFT JOIN user_login u on u.id=cio.creator"
    			   + " where cio.office_id="+office_id;
    	String condition = DbUtils.buildConditions(getParaMap());
    	List<Record> orderList = Db.find(sql+ condition);
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("data", orderList);
        renderJson(map); 
    }
    
    public void save() {
    	String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
   		String ids = (String) dto.get("ids");
		String selected_item_ids= (String) dto.get("selected_item_ids"); //获取申请单据的id,用于回显
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
			return;
		}
   		long office_id=user.getLong("office_id");
   		
   		ChargeInvoiceOrder order = new ChargeInvoiceOrder();
   		if (StringUtils.isNotBlank(id)) {
   			//update
   			order = ChargeInvoiceOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order); 
   			order.update();
   			
			//保存发票明细
   	   		List<Map<String, String>> invoiceList = (ArrayList<Map<String, String>>)dto.get("invoiceList");
   			handleList(invoiceList,id);
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("FPH", user.getLong("office_id")));
   			order.set("creator", user.getLong("id"));
   			order.set("create_time", new Date());
   			order.set("office_id", office_id);
   			order.set("status", "开票中");
   			order.save();
   			
   			String[] itemId = ids.split(",");
   			String[] joaId = selected_item_ids.split(",");
   			if(StringUtils.isNotBlank(selected_item_ids)){
   	   			for(int i=0;i<joaId.length;i++){
   	   				Record re = new Record();
   	   				Record caor = Db.findFirst("select charge_order_id from arap_charge_item where ref_order_id = "+joaId[i]);
   	   				re.set("order_id", order.get("id"));
   	   				re.set("charge_order_id",caor.get("charge_order_id"));
   	   				re.set("job_order_arap_item_id",joaId[i]);
   	   				Db.save("charge_invoice_order_item_charge_item", re);
   	   			}
   			}
   			
   			if(StringUtils.isNotBlank(ids)){
   	   			for(int i=0;i<itemId.length;i++){
   	   				String item_id = itemId[i];
   	   					ArapChargeOrder arapChargeOrder = ArapChargeOrder.dao.findById(item_id);
   	   					arapChargeOrder.set("audit_status", "开票中").update();
   	   			}
   			}
   			
   			//更新勾选的job_order_arap item creat_flag,改变创建标记位
   			String ySql ="update job_order_arap set invoice_create_flag='Y' where id in("+selected_item_ids+")";
   	        Db.update(ySql);
   		}
   		
   		Record r = order.toRecord();
   		r.set("idsArray", ids);
   		renderJson(r);
    }
    
    //提交方法
    public void submitMethod(){
    	String order_id = getPara("order_id");
    	ChargeInvoiceOrder cio = ChargeInvoiceOrder.dao.findById(order_id);
    	cio.set("status","已提交");
    	cio.update();
    	renderJson(cio);
    }
    
    //复核方法
    public void checkMethod(){
    	String order_id = getPara("order_id");
    	String chargeItemIds = getPara("chargeItemIds");
    	String charge_order_id = "";
    	ChargeInvoiceOrder cio = ChargeInvoiceOrder.dao.findById(order_id);
    	cio.set("status","已开票");
    	cio.set("check_flag","Y");
    	cio.update();
    	String[] chargeItemId;	
    	if(chargeItemIds==null){
    		List<Record> re = Db.find("select job_order_arap_item_id,charge_order_id from charge_invoice_order_item_charge_item where order_id="+order_id);
    		for(int i = 0;i<re.size();i++){
    			Record re1 = re.get(i);
    			JobOrderArap joa = JobOrderArap.dao.findById(re1.get("job_order_arap_item_id"));
    			joa.set("invoice_check_flag", "Y");
        		joa.update();
        		charge_order_id = re1.getLong("charge_order_id").toString();
    		}
    	}else{
    		chargeItemId = chargeItemIds.split(",");
        	for(int i = 1;i<chargeItemId.length;i++){
        		Record re = Db.findById("charge_invoice_order_item_charge_item", chargeItemId[i]);
        		JobOrderArap joa = JobOrderArap.dao.findById(re.get("job_order_arap_item_id"));
        		joa.set("invoice_check_flag", "Y");
        		joa.update();
        		charge_order_id = re.getLong("charge_order_id").toString();
        	}
    	}
    	
    	/*Record aco = Db.findById("charge_invoice_order_item_charge_item", charge_order_id);
    	aco.set("audit_status","已开票");*/
    	List<Record> ref_order_ids = Db.find("select ref_order_id from arap_charge_item where charge_order_id="+charge_order_id);
    	int num = 0;
    	for(int i = 0;i<ref_order_ids.size();i++){
    		JobOrderArap joa = JobOrderArap.dao.findById(ref_order_ids.get(i).get("ref_order_id"));
    		if(joa.get("invoice_check_flag").equals("Y")){
    			num++;
    		}
    	}
    	if(num>0&&ref_order_ids.size()>num){
    		Record aco = Db.findById("arap_charge_order", charge_order_id);
        	aco.set("audit_status","部分已开票");
        	Db.update("arap_charge_order",aco);
    	}else if(num>0&&ref_order_ids.size()==num){
    		Record aco = Db.findById("arap_charge_order", charge_order_id);
    		aco.set("audit_status","已开票");
    		Db.update("arap_charge_order",aco);
    	}
    	renderJson(cio);
    }
    
  //复核不通过方法
    public void cancelcheckMethod(){
    	String order_id = getPara("order_id");
    	ChargeInvoiceOrder cio = ChargeInvoiceOrder.dao.findById(order_id);
    	cio.set("status","复核不通过");
    	cio.update();
    	renderJson(cio);
    }
    
    public void requestList(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
			return;
		}
        long office_id=user.getLong("office_id");
        String sql = " SELECT "
        		+" 	* "
        		+" FROM "
        		+" 	( "
        		+" 		SELECT "
        		+" 			aco.*, p.abbr party_name, "
        		+" 			IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount)-(IFNULL( "
        		+" 				( "
        		+" 					SELECT "
        		+" 						SUM(joa.exchange_total_amount) "
        		+" 					FROM "
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 3 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'charge' "
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
        		+" 						job_order_arap joa  "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 6 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'charge' "
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
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 8 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y' AND joa.order_type = 'charge' "
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
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y'  AND joa.order_type = 'cost' "
        		+" 					AND joa.exchange_currency_id = 9 "
        		+" 					AND aci.charge_order_id = aco.id "
        		+" 				), "
        		+" 				0 "
        		+" 			)) "
        		+" 					FROM "
        		+" 						job_order_arap joa "
        		+" 					LEFT JOIN arap_charge_item aci ON joa.id = aci.ref_order_id "
        		+" 					WHERE "
        		+" 						joa.invoice_create_flag = 'Y'  AND joa.order_type = 'charge' "
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
        		+" 						arap_charge_application_order "
        		+" 					WHERE "
        		+" 						id = c.application_order_id "
        		+" 				) SEPARATOR '<br/>' "
        		+" 			) app_msg "
        		+" 		FROM "
        		+" 			arap_charge_order aco "
        		+" 		LEFT JOIN charge_application_order_rel c ON c.charge_order_id = aco.id "
        		+" 		LEFT JOIN party p ON p.id = aco.sp_id "
        		+" 		WHERE "
        		+" 			aco. STATUS = '已确认' and aco.newProcessFlag='Y'"
        		+" 		AND aco.office_id = "+office_id+" "
        		+" 		GROUP BY "
        		+" 			aco.id "
        		+" 	) A "
        		+" WHERE "
        		+" 	( "
        		+" 		(ifnull(usd, 0)-paid_usd > 0.02) "
        		+" 		OR (ifnull(cny, 0)-paid_cny > 0.02) "
        		+" 		OR (ifnull(hkd, 0)-paid_hkd > 0.01) "
        		+" 		OR (ifnull(jpy, 0)-paid_jpy > 0.01) "
        		+" 	) ";
    	 String condition = DbUtils.buildConditions(getParaMap());
    	 String begin_time =getPara("begin_time_begin"); 
         String end_time =getPara("end_time_end"); 
         if(StringUtils.isNotEmpty(begin_time)){
         	condition+= " and ('"+begin_time+"' <= begin_time";
         }else if(StringUtils.isNotEmpty(end_time)){
         	condition+= " and ('1970-01-01' <= begin_time";
         }
         if(StringUtils.isNotEmpty(end_time)){
         	condition+= " and end_time<='"+end_time+"')";
         }else if(StringUtils.isNotEmpty(begin_time)){
         	condition+= " and end_time<='2030-12-31')";
         }
    	List<Record> orderList = Db.find(sql+condition);
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("data", orderList);
    	renderJson(map);
    }
    
    public void itemList(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
			return;
		}
        long office_id=user.getLong("office_id");
    	String order_id = getPara("order_ids");
    	String cioiciIds = getPara("cioiciIds");
    	String ids = getPara("ids");
    	String new_process_flag = getPara("new_process_flag");
    	String condition = "";
    	if(StringUtils.isNotBlank(order_id)){
    		order_id = " and cioici.order_id IN ("+order_id+")";
    	}
    	/*if(StringUtils.isNotBlank(cioiciIds)){
    		condition += " and (cioici.id IN ("+cioiciIds+") or cioici.order_item_id is null)";
    	}*/
    	if(StringUtils.isNotBlank(ids)){
    		condition += " and cioici.id not in ("+ids+")";
    	}
    	String sql = "";
    	if(StringUtils.isBlank(new_process_flag)||!new_process_flag.equals("Y")){
    		sql = "SELECT cioici.id,jo.id joId,aco.order_no check_order_no,jo.order_no order_no,p.abbr sp_name,f.name fin_name,cur.id currency_id,cur.name currency_name ,joa.exchange_total_amount"
    				+ " FROM charge_invoice_order cio "
    				+ " LEFT JOIN charge_invoice_order_item_charge_item cioici on cioici.order_id = cio.id"
    				+ " LEFT JOIN job_order_arap joa on joa.id = cioici.job_order_arap_item_id "
    				+ " LEFT JOIN job_order jo on jo.id = joa.order_id "
    				+ " LEFT JOIN party p on p.id = joa.sp_id "
    				+ " LEFT JOIN fin_item f on f.id = joa.charge_id "
    				+ " LEFT JOIN currency cur ON cur.id = joa.exchange_currency_id "
    				+ " LEFT JOIN arap_charge_order aco on aco.id = cioici.charge_order_id "
    				+ " where joa.invoice_create_flag = 'Y' and jo.delete_flag = 'N'"+order_id+condition
    				+ " and jo.office_id = "+office_id
    				+ " GROUP BY cioici.id ORDER BY cioici.id ";
    	}else{
    		sql = "SELECT cioici.id,jo.id joId,aco.order_no check_order_no,jo.order_no order_no,jo.type type,p.abbr customer_name,sp_p.abbr sp_name,f. NAME fin_name,"
    				+ " cur1.name currency_name,joa.total_amount total_amount,cur2. NAME exchange_currency_name,ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount"
    				+ " FROM charge_invoice_order cio "
    				+ " LEFT JOIN charge_invoice_order_item_charge_item cioici on cioici.order_id = cio.id"
    				+ " LEFT JOIN job_order_arap joa ON joa.id = cioici.job_order_arap_item_id"
    				+ " LEFT JOIN job_order jo ON jo.id = joa.order_id"
    				+ " LEFT JOIN party sp_p ON sp_p.id = joa.sp_id"
    				+ " LEFT JOIN party p ON p.id = jo.customer_id"
    				+ " LEFT JOIN fin_item f ON f.id = joa.charge_id"
    				+ " LEFT JOIN currency cur1 ON cur1.id = joa.currency_id"
    				+ " LEFT JOIN currency cur2 ON cur2.id = joa.exchange_currency_id"
    				+ " LEFT JOIN arap_charge_order aco ON aco.id = cioici.charge_order_id"
    				+ " WHERE joa.create_flag = 'N' AND jo.delete_flag = 'N'"+order_id+" AND jo.office_id ="+office_id
    				+ " GROUP BY cioici.id ORDER BY cioici.id";
    	}
    	List<Record> orderList = new ArrayList<Record>();
		if(StringUtils.isNotBlank(order_id)){
			orderList = Db.find(sql);
		}
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("data", orderList);
    	renderJson(map);
    }
    
    public void addItemList(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
    	String condition = DbUtils.buildConditions(getParaMap());
    	 
		String sql = "SELECT joa.id,joa.sp_id,aco.order_no check_order_no,jo.order_no,p.abbr sp_name,fi.id charge_id,fi.name fin_name,cur.name currency_name,joa.price,joa.pay_flag "
				+ " FROM job_order jo "
				+ " LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
				+ " LEFT JOIN fin_item fi ON joa.charge_id = fi.id "
				+ " LEFT JOIN party p ON p.id = joa.sp_id "
				+ " LEFT JOIN currency cur ON cur.id = joa.currency_id "
				+ " LEFT JOIN arap_charge_item aci ON aci.ref_order_id = joa.id "
				+ " LEFT JOIN arap_charge_order aco ON aco.id = aci.charge_order_id"
				+ " WHERE joa.id = aci.ref_order_id AND joa.invoice_create_flag = 'N' AND jo.delete_flag = 'N' "+condition
				+ " and jo.office_id = "+office_id
				+ " GROUP BY joa.id ORDER BY aco.order_no,jo.order_no";
    	
    	List<Record> orderList = Db.find(sql);
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("data", orderList);
    	renderJson(map);
    }
    
    //发票明细list
    public void invoiceList(){
    	String id = getPara("id");
    	String sql = "SELECT cioi.*,c.name currency_name,fi.name fee_name,"
    			   + " (select CAST(GROUP_CONCAT(cioici.id) AS char) from charge_invoice_order_item_charge_item cioici where cioici.order_item_id = cioi.id) charge_ids "
    			   + " FROM charge_invoice_order_item cioi "
    			   + " LEFT JOIN fin_item fi on fi.id = cioi.fee_id "
    			   + " LEFT JOIN currency c on c.id = cioi.currency_id "
    			   + " where cioi.order_id="+id
    			   + " ORDER BY cioi.id";
    	List<Record> orderList = Db.find(sql);
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("data", orderList);
    	renderJson(map);
    }
    
    //删除对账单明细
    public void deleteChargeItem(){
    	String cioici_id = getPara("id");
    	if(StringUtils.isNotBlank(cioici_id)){
    		Record re = Db.findById("charge_invoice_order_item_charge_item", cioici_id);
    		JobOrderArap jobOrderArap = JobOrderArap.dao.findById(re.get("job_order_arap_item_id"));
    		jobOrderArap.set("invoice_create_flag", "N");
            jobOrderArap.update();
            Db.delete("charge_invoice_order_item_charge_item", re);
    	}
    }
    
    //添加对账单明细
    public void addChargeItem(){
    	String itemid = getPara("charge_itemlist");
    	String order_id = getPara("order_id");
    	if(StringUtils.isNotBlank(itemid)){
    		String[] itemids = itemid.split(",");
    		for(int i = 0;i<itemids.length;i++){
    			Record re = new Record();
    			JobOrderArap jobOrderArap = new JobOrderArap();
    			jobOrderArap = JobOrderArap.dao.findById(itemids[i]);
       		 	jobOrderArap.set("invoice_create_flag", "Y");
                jobOrderArap.update();
                Record aci = Db.findFirst("select charge_order_id from arap_charge_item where ref_order_id = "+itemids[i]);
	   			re.set("order_id", order_id);
	   			re.set("charge_order_id",aci.get("charge_order_id"));
	   			re.set("job_order_arap_item_id",itemids[i]);
	   			Db.save("charge_invoice_order_item_charge_item", re);
    		}
    	}
    	Record re = new Record();
    	renderJson(re);
    }
    
    //删除发票明细
    public void deleteInvoiceItem(){
    	String rowId = getPara("rowId");
    	String ids = getPara("ids");
    	//删除发票明细
    	if(StringUtils.isNotBlank(rowId)){
        	ChargeInvoiceOrderItem cioi = ChargeInvoiceOrderItem.dao.findById(rowId);
        	cioi.delete();
    	}
    	//清空关联发票明细的对账单费用明细
    	if(StringUtils.isNotBlank(ids)){
    		String[] cioiciId = ids.split(",");
    		for(int i = 0;i<cioiciId.length;i++){
        		Record cioici = Db.findById("charge_invoice_order_item_charge_item", cioiciId[i]);
        		cioici.set("order_item_id", null);
        		Db.update("charge_invoice_order_item_charge_item",cioici);
        	}
    	}
    }
    
    
    //保存发票明细方法
    public void handleList(List<Map<String, String>> invoiceList,String order_id){
    	for (Map<String, String> rowMap : invoiceList){
    		String[] invoice_ids = (rowMap.get("invoice_ids")).split(",");
    		String rowId = rowMap.get("id");
    		if(StringUtils.isBlank(rowId)){
    			ChargeInvoiceOrderItem cioi = new ChargeInvoiceOrderItem();
    			DbUtils.setModelValues(rowMap, cioi);
    			cioi.set("order_id", order_id);
    			cioi.save();
    			
    			//更新charge_invoice_order_item_charge_item表
    			for(int i = 0;i<invoice_ids.length;i++){
    				Record cioici = Db.findById("charge_invoice_order_item_charge_item", invoice_ids[i]);
    				if(cioici!=null){
    					cioici.set("order_item_id", cioi.get("id"));
        				Db.update("charge_invoice_order_item_charge_item", cioici);
    				}
    			}
    		}else if(StringUtils.isNotBlank(rowId)){
    			ChargeInvoiceOrderItem cioi = new ChargeInvoiceOrderItem();
    			DbUtils.setModelValues(rowMap, cioi);
    			cioi.update();
    			
    			Db.update("update charge_invoice_order_item_charge_item set order_item_id=null where order_item_id="+cioi.get("id"));
				
    			//重新保存charge_invoice_order_item_charge_item表的order_item_id列
    			for(int i = 0;i<invoice_ids.length;i++){
    				Record cioici = Db.findById("charge_invoice_order_item_charge_item", invoice_ids[i]);
    				if(cioici!=null){
        				cioici.set("order_item_id", cioi.get("id"));
        				Db.update("charge_invoice_order_item_charge_item", cioici);
    				}
    			}
    		}
		}
    }
    
   
}
