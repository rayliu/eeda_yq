package controllers.arap.ap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.SpCostCompareOrder;
import models.SpCostCompareOrderSps;
import models.UserLogin;

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
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.ToDBCUtil;
//import models.ChargeAppOrderRel;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostComparisonController extends Controller {
    private Log logger = Log.getLog(CostComparisonController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/CostComparison");
        setAttr("listConfigList", configList);
    	render("/oms/CostComparison/CostComparisonList.html");
    }
    
    @Before(EedaMenuInterceptor.class) 
    public void create() {
		render("/oms/CostComparison/costComparisonEdit.html");
	}
    
 	
  	
  	
  	@Before(Tx.class)
	public void save() throws InstantiationException, IllegalAccessException {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        SpCostCompareOrder order = new SpCostCompareOrder();
   		String id = (String) dto.get("id");
   		String status = (String) dto.get("status");   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
   		String action_type = "add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type = "update";
   			//update
   			order = SpCostCompareOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order); 
   			order.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, order);
   			
   			//需后台处理的字段
   			order.set("order_no", OrderNoGenerator.getNextOrderNo("CBDB", user.getLong("office_id")));
   			order.set("creator", user.getLong("id"));
   			order.set("create_stamp", new Date());
   			order.set("office_id", office_id);
   			order.save();
   			id = order.getLong("id").toString();  
		
	   }
   		
   		//供应商明细
		List<Map<String, String>> SupplierItem_list = (ArrayList<Map<String, String>>)dto.get("SupplierItem_list");
		String userId = user.getLong("id").toString();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String dateString = formatter.format(date);
		if(SupplierItem_list!=null){
			for (Map<String, String> rowMap : SupplierItem_list) {//获取每一行
	    		Model<?> model = (Model<?>) SpCostCompareOrderSps.class.newInstance();
	    		
	    		String rowId = rowMap.get("id");
	    		String action = rowMap.get("action");
	    		if(StringUtils.isEmpty(rowId)){
	    			if(!"DELETE".equals(action)){
	    				DbUtils.setModelValues(rowMap, model);
		    			model.set("order_id", id);
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
		
		//报价明细保存
//		List<Map<String, String>> quotationItem_list = (ArrayList<Map<String, String>>)dto.get("quotationItem_list");
//		DbUtils.handleList(quotationItem_list, id, SpCostCompareOrderItem.class, "order_id");
		
		
		
   		saveLog(jsonStr, id, user, action_type);
		long creator = order.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
	}
  	
  	private void saveLog(String json, String order_id, UserLogin user, String action_type) {
        Record rec = new Record();
        rec.set("log_type", "action");
        rec.set("operation_obj", "供应商成本对比");
        rec.set("action_type", action_type);
        rec.set("create_stamp", new Date());
        rec.set("user_id", user.get("id"));
        rec.set("order_id", order_id);
        rec.set("json", json);
        rec.set("sys_type", "forwarder");
        rec.set("office_id", user.getLong("office_id"));
        Db.save("sys_log", rec);
    }
  	
  	
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	
    	String sp_id =getPara("sp_id");
        if(StringUtils.isNotEmpty(getPara("sp_id"))){
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,sp_id,"ARAP_COM");
    	}
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String sql = " SELECT 	spCom.*,lor. NAME por_name, "
				+" IF(service_type = 'doorToPort',ifnull(dockp.dock_name,concat(lpol.name,' -',lpol.code)), "
				+" IFNULL(lol. NAME,ifnull(dockp.dock_name,concat(lpol.name,' -',lpol.code)))) pol_name, "
				+" IFNULL(lod. NAME,ifnull(dockd.dock_name,concat(lpod.name,' -',lpod.code))) pod_name, "
				+" 	ul.c_name creator_name,	cy. NAME currency_name "
				+" FROM sp_cost_compare_order spCom "
				+" LEFT JOIN dockinfo dockp on (dockp.id = spCom.pickup_loc and spCom.pickup_loc_type='land') "
				+" LEFT JOIN dockinfo dockd on (dockd.id = spCom.delivery_loc and spCom.delivery_loc_type='land') "
				+" LEFT JOIN location lpol  on (lpol.id = spCom.pickup_loc and spCom.pickup_loc_type='port') "
				+" LEFT JOIN location lpod  on (lpod.id = spCom.delivery_loc and spCom.delivery_loc_type='port') "
				+" LEFT JOIN location lor on lor.id = spCom.por "
				+" LEFT JOIN location lol on lol.id = spCom.pol "
				+" LEFT JOIN location lod on lod.id = spCom.pod "
				+" LEFT JOIN user_login ul ON ul.id = spCom.creator "
				+" LEFT JOIN currency cy ON cy.id = spCom.target_currency "
				+" WHERE spCom.office_id ="+office_id;
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit );
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    } 
    
    //海、空条件
    @SuppressWarnings("unchecked")
    private String searchCondition(Map<String,?> dto){
    	String condition = "";
    	
		if(dto!=null){
			String service_typeRadio = (String) dto.get("service_typeRadio");
				for (Entry<String, ?> entry : dto.entrySet()) { 
					String key = entry.getKey();
		            	String preValue = String.valueOf(entry.getValue()).trim();
		            	String value = ToDBCUtil.ToDBC(preValue);//全角转半角函数
//		            		logger.debug(key+":"+value);
		            	    	if(key.contains("service_typeRadio")){
		            	    		if("doorToPort".contains(service_typeRadio)){
		            	    			condition+=StringUtils.isNotBlank(value)?" and scl.type like '%ocean%'":" and scl.type like '%%'";
		            	    		}else{
		            	    			condition+=StringUtils.isNotBlank(value)?" and scl.type like '%"+value+"%'":" and scl.type like '%%'";
		            	    		}
			                       }else if(key.contains("trans_clause")){
			                    	   condition+=" and spCon.trans_clause ='"+value+"'";
			                       }else if(key.contains("trade_type")){
			                    	   condition+=" and spCon.trade_type ='"+value+"'";
			                       }else if(key.contains("pol")){
			                    	   condition+=StringUtils.isNotBlank(value)?" and scl.pol_id ="+value:" and scl.pol_id is null";
			                       }else if(key.contains("pod")){
			                    	   condition+=StringUtils.isNotBlank(value)?" and scl.pod_id ="+value:" and scl.pod_id is null";
			                       }
			                       if("ocean,doorToPort".contains(service_typeRadio)){
			                    	   if(key.contains("por")){
				                    	   condition+=StringUtils.isNotBlank(value)?" and scl.por_id ="+value:" and scl.por_id is null";
				                       }  
			                       }                   
				}

		}
    	return condition;
    }
    //陆运条件
    @SuppressWarnings("unchecked")
    private String searchLandCondition(Map<String,?> dto){
    	String condition = "";
		if(dto!=null){
				for (Entry<String, ?> entry : dto.entrySet()) { 
					String key = entry.getKey();
		            	String preValue = String.valueOf(entry.getValue()).trim();
		            	String value = ToDBCUtil.ToDBC(preValue);//全角转半角函数
//		            		logger.debug(key+":"+value);		            	    
		            	    	if(key.contains("service_typeRadio")){		                    	   
			                    	   condition+=StringUtils.isNotBlank(value)?" and scl.type like '%land%'":" and scl.type like '%%'";
			                       }else if(key.contains("trans_clause")){
			                    	   condition+=" and spCon.trans_clause ='"+value+"'";
			                       }else if(key.contains("trade_type")){
			                    	   condition+=" and spCon.trade_type ='"+value+"'";
			                       }else if(key.contains("pickup_loc")){
			                    	   condition+=" AND (IF (scl.pol_id_type = 'land',dpol.id = '"+value+"',lpol.id = '"+value+"'))";
			                       }else if(key.contains("delivery_loc")){
			                    	   condition+=" AND (IF (scl.pod_id_type = 'land',dpod.id = '"+value+"',lpod.id = '"+value+"'))";
			                       }
		            	    }                          

		}
    	return condition;
    }
    
    
    
    
    
    
    
    
    
    
    
  	
  	//获取供应商
    @SuppressWarnings("unchecked")
    public void searchSpcomparison(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	String condition_json = getPara("paraJson");
    	String input =getPara("input");
    	
    	Gson gson = new Gson();   	
		
		Map<String,?> dto = gson.fromJson(condition_json,HashMap.class);
    	
    	if(StringUtils.isNotEmpty(input)){
    		input = " and py.abbr like '%"+getPara("input")+"%'";
    	}
    	
    	String service_typeRadio = (String)dto.get("service_typeRadio");
    	String condition="";
    	if("ocean,air,doorToPort".contains(service_typeRadio)){
    		condition = searchCondition(dto);
    	}
    	
    	String land_condition="";
    	if("land,doorToPort".contains(service_typeRadio)){
    		land_condition=searchLandCondition(dto);
    	}
    	String sp_sql ="";
    	String land_sp_sql ="";
    	
    	if("ocean,air".contains(service_typeRadio)){
    		sp_sql="SELECT py.id,py.abbr name,py.sp_type from supplier_contract spCon"
        			+ " LEFT JOIN supplier_contract_location scl on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " where spCon.office_id ="+office_id+condition+input
        			+ " GROUP BY spCon.customer_id";
    	}else if("land".contains(service_typeRadio)){
    		sp_sql="SELECT py.id,py.abbr name,py.sp_type from supplier_contract spCon"
        			+ " LEFT JOIN supplier_contract_location scl on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " LEFT JOIN dockinfo dpol ON (dpol.id = scl.pol_id AND scl.pol_id_type = 'land')"
        			+ "	LEFT JOIN dockinfo dpod ON (dpod.id = scl.pod_id AND scl.pod_id_type = 'land')"
        			+ "	LEFT JOIN location lpol ON (lpol.id = scl.pol_id AND scl.pol_id_type = 'port')"
        			+ " LEFT JOIN location lpod ON (lpod.id = scl.pod_id AND scl.pod_id_type = 'port')"
        			+ " where spCon.office_id ="+office_id+land_condition+input
        			+ " GROUP BY spCon.customer_id";
    	}else if("doorToPort".contains(service_typeRadio)){
    		sp_sql="SELECT py.id,py.abbr name,py.sp_type from supplier_contract spCon"
        			+ " LEFT JOIN supplier_contract_location scl on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " where spCon.office_id ="+office_id+condition+input
        			+ " GROUP BY spCon.customer_id";
    		land_sp_sql="SELECT py.id,py.abbr name,py.sp_type from supplier_contract spCon"
        			+ " LEFT JOIN supplier_contract_location scl on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " LEFT JOIN dockinfo dpol ON (dpol.id = scl.pol_id AND scl.pol_id_type = 'land')"
        			+ "	LEFT JOIN dockinfo dpod ON (dpod.id = scl.pod_id AND scl.pod_id_type = 'land')"
        			+ "	LEFT JOIN location lpol ON (lpol.id = scl.pol_id AND scl.pol_id_type = 'port')"
        			+ " LEFT JOIN location lpod ON (lpod.id = scl.pod_id AND scl.pod_id_type = 'port')"
        			+ " where spCon.office_id ="+office_id+land_condition+input
        			+ " GROUP BY spCon.customer_id";
    		
    		sp_sql=sp_sql+" union "+land_sp_sql;
    	}
    	
    	List<Record> rec = Db.find(sp_sql);
    	renderJson(rec);   	
    }
    
    
    @SuppressWarnings("unchecked")
    public void searchShowItem(){    	
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }        			
        UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	String conditionJson = getPara("paraJson");
    	Gson gson = new Gson();    	
		
		Map<String,?> dto = gson.fromJson(conditionJson,HashMap.class);
    	
    	String service_typeRadio = (String)dto.get("service_typeRadio");
    	String sp_id_string = " and spCon.customer_id in("+getPara("sp_id_string")+")"; 
    	if(StringUtils.isEmpty(getPara("sp_id_string"))){
    		sp_id_string="";
    	}
    	String condition="";
    	if("ocean,air,doorToPort".contains(service_typeRadio)){
    		condition = searchCondition(dto);
    	}
    	
    	String land_condition="";
    	if("land,doorToPort".contains(service_typeRadio)){
    		land_condition=searchLandCondition(dto);
    	}
    	
    	String item_sql="";
    	String land_item_sql="";
    	
        if("ocean,air".contains(service_typeRadio)){
        	item_sql = "SELECT py.id item_sp_id,scl.carrier_id,spCon.contract_no,spCon.trans_clause,spCon.trade_type,"
        			+ " scl.por_id,scl.pol_id,scl.pod_id,sci.fee_id,sci.price,sci.currency_id,py.abbr sp_abbr,"
        			+ " ifnull(pcar.abbr,pair.abbr) carrier_abbr,lpor.name por_name,lpol.name pol_name,lpod.name pod_name,fi.name fee_name,  sci.uom uom,"
        			+ "	ut.name uom_name,sci.contract_type,	sci.volume1, sci.volume2, sci.gross_weight1,sci.gross_weight2,"
        			+ "	sci.container_type,	sci.truck_type,"
        			+ " cy.name currency_name,spCon.id con_id,spCon.id contract_id,scl.id contact_loc_id,sci.id contact_item_id,py.sp_type"
        			+ " from  supplier_contract_item sci"
        			+ " LEFT JOIN supplier_contract_location scl on sci.supplier_loc_id = scl.id"
        			+ " LEFT JOIN supplier_contract spCon on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " LEFT JOIN party pcar ON pcar.id = scl.carrier_id"
        			+ " LEFT JOIN party pair ON pair.id = scl.air_company"
        			+ " LEFT JOIN location lpor ON lpor.id = scl.por_id"
        			+ " LEFT JOIN location lpol ON lpol.id = scl.pol_id"
        			+ " LEFT JOIN location lpod ON lpod.id = scl.pod_id"
        			+ " LEFT JOIN fin_item fi ON fi.id = sci.fee_id"
        			+ " LEFT JOIN unit ut on ut.id = sci.uom"
        			+ " LEFT JOIN currency cy on cy.id = sci.currency_id"    			
        			+ " where spCon.office_id ="+office_id+ condition+sp_id_string 
        			+ " order by fi.name";
        }else if("land".contains(service_typeRadio)){
        	item_sql=" SELECT	py.id item_sp_id,scl.carrier_id,spCon.contract_no,spCon.trans_clause,spCon.trade_type,"
        			+ "	null carrier_abbr,scl.por_id,	scl.pol_id,	scl.pod_id,	sci.fee_id,	sci.price,	sci.currency_id,"
        			+ " py.abbr sp_abbr,null por_name,IFNULL(dpol.dock_name,lpol.name) pol_name,	IFNULL(dpod.dock_name,lpod.name) pod_name,"
        			+ "	fi. NAME fee_name,sci.uom uom,ut. NAME uom_name,sci.contract_type,sci.volume1,sci.volume2,"
        			+ "	sci.gross_weight1,sci.gross_weight2,sci.container_type,	sci.truck_type,	cy. NAME currency_name,"
        			+ "	spCon.id con_id,spCon.id contract_id,scl.id contact_loc_id,	sci.id contact_item_id,	py.sp_type"
        			+ " FROM	supplier_contract_item sci"
        			+ " LEFT JOIN supplier_contract_location scl ON sci.supplier_loc_id = scl.id"
        			+ " LEFT JOIN supplier_contract spCon ON spCon.id = scl.contract_id "
        			+ " LEFT JOIN party pcar ON pcar.id = scl.carrier_id"
        			+ " LEFT JOIN party py ON py.id = spCon.customer_id"
        			+ " LEFT JOIN dockinfo dpol ON (dpol.id = scl.pol_id and scl.pol_id_type = 'land')"
        			+ " LEFT JOIN dockinfo dpod ON (dpod.id = scl.pod_id and scl.pod_id_type = 'land')"
        			+ " LEFT JOIN location lpol ON (lpol.id = scl.pol_id and scl.pol_id_type = 'port')"
        			+ " LEFT JOIN location lpod ON (lpod.id = scl.pod_id and scl.pod_id_type = 'port')"
        			+ " LEFT JOIN fin_item fi ON fi.id = sci.fee_id"
        			+ " LEFT JOIN unit ut ON ut.id = sci.uom"
        			+ " LEFT JOIN currency cy ON cy.id = sci.currency_id"
        			+ " WHERE	spCon.office_id ="+office_id+land_condition+sp_id_string			
        			+ " ORDER BY fi. NAME";
        }else if("doorToPort".contains(service_typeRadio)){
        	item_sql = "SELECT py.id item_sp_id,scl.carrier_id,spCon.contract_no,spCon.trans_clause,spCon.trade_type,"
        			+ " scl.por_id,scl.pol_id,scl.pod_id,sci.fee_id,sci.price,sci.currency_id,py.abbr sp_abbr,"
        			+ " ifnull(pcar.abbr,pair.abbr) carrier_abbr,lpor.name por_name,lpol.name pol_name,lpod.name pod_name,fi.name fee_name,  sci.uom uom,"
        			+ "	ut.name uom_name,sci.contract_type,	sci.volume1, sci.volume2, sci.gross_weight1,sci.gross_weight2,"
        			+ "	sci.container_type,	sci.truck_type,"
        			+ " cy.name currency_name,spCon.id con_id,spCon.id contract_id,scl.id contact_loc_id,sci.id contact_item_id,py.sp_type"
        			+ " from  supplier_contract_item sci"
        			+ " LEFT JOIN supplier_contract_location scl on sci.supplier_loc_id = scl.id"
        			+ " LEFT JOIN supplier_contract spCon on spCon.id = scl.contract_id"
        			+ " LEFT JOIN party py on py.id = spCon.customer_id"
        			+ " LEFT JOIN party pcar ON pcar.id = scl.carrier_id"
        			+ " LEFT JOIN party pair ON pair.id = scl.air_company"
        			+ " LEFT JOIN location lpor ON lpor.id = scl.por_id"
        			+ " LEFT JOIN location lpol ON lpol.id = scl.pol_id"
        			+ " LEFT JOIN location lpod ON lpod.id = scl.pod_id"
        			+ " LEFT JOIN fin_item fi ON fi.id = sci.fee_id"
        			+ " LEFT JOIN unit ut on ut.id = sci.uom"
        			+ " LEFT JOIN currency cy on cy.id = sci.currency_id"    			
        			+ " where spCon.office_id ="+office_id+ condition+sp_id_string ;
        	
        	land_item_sql=" SELECT	py.id item_sp_id,scl.carrier_id,spCon.contract_no,spCon.trans_clause,spCon.trade_type,"
        			+ "	scl.por_id,	scl.pol_id,	scl.pod_id,	sci.fee_id,	sci.price,	sci.currency_id,"
        			+ " py.abbr sp_abbr,null carrier_abbr,null por_name,IFNULL(dpol.dock_name,lpol.name) pol_name,	IFNULL(dpod.dock_name,lpod.name) pod_name,"
        			+ "	fi. NAME fee_name,sci.uom uom,ut. NAME uom_name,sci.contract_type,sci.volume1,sci.volume2,"
        			+ "	sci.gross_weight1,sci.gross_weight2,sci.container_type,	sci.truck_type,	cy. NAME currency_name,"
        			+ "	spCon.id con_id,spCon.id contract_id,scl.id contact_loc_id,	sci.id contact_item_id,	py.sp_type"
        			+ " FROM	supplier_contract_item sci"
        			+ " LEFT JOIN supplier_contract_location scl ON sci.supplier_loc_id = scl.id"
        			+ " LEFT JOIN supplier_contract spCon ON spCon.id = scl.contract_id "
        			+ " LEFT JOIN party pcar ON pcar.id = scl.carrier_id"
        			+ " LEFT JOIN party py ON py.id = spCon.customer_id"
        			+ " LEFT JOIN dockinfo dpol ON (dpol.id = scl.pol_id and scl.pol_id_type = 'land')"
        			+ " LEFT JOIN dockinfo dpod ON (dpod.id = scl.pod_id and scl.pod_id_type = 'land')"
        			+ " LEFT JOIN location lpol ON (lpol.id = scl.pol_id and scl.pol_id_type = 'port')"
        			+ " LEFT JOIN location lpod ON (lpod.id = scl.pod_id and scl.pod_id_type = 'port')"
        			+ " LEFT JOIN fin_item fi ON fi.id = sci.fee_id"
        			+ " LEFT JOIN unit ut ON ut.id = sci.uom"
        			+ " LEFT JOIN currency cy ON cy.id = sci.currency_id"
        			+ " WHERE	spCon.office_id ="+office_id+land_condition+sp_id_string;
        	item_sql ="SELECT * FROM ( " + item_sql +" union "+land_item_sql+" ) A ORDER BY fee_name ";
        }
    	
        
        String sqlTotal = "select count(1) total from ("+item_sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(item_sql);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap);
  	}
    
    
  	
  	
  	@Before(EedaMenuInterceptor.class)
  	public void edit(){
		String id = getPara("id");
		
		UserLogin user = LoginUserController.getLoginUser(this);
		if (user==null) {
            return;
        }
		long office_id = user.getLong("office_id");
		
		String Sql = "SELECT spCom.*,ifnull(dockp.dock_name,concat(lpol.name,' -',lpol.code)) pickup_loc_name,ifnull(dockd.dock_name,concat(lpod.name,' -',lpod.code)) delivery_loc_name,concat(lor.name,' -',lor.code) por_name,"
				+ " lol.name pol_name,lod.name pod_name,ul.c_name creator_name,cy.name currency_name"
				+ " from sp_cost_compare_order spCom"
				+ " LEFT JOIN dockinfo dockp on (dockp.id = spCom.pickup_loc and spCom.pickup_loc_type='land')"
				+ " LEFT JOIN dockinfo dockd on (dockd.id = spCom.delivery_loc and spCom.delivery_loc_type='land')"
				+ " LEFT JOIN location lpol  on (lpol.id = spCom.pickup_loc and spCom.pickup_loc_type='port')"
				+ " LEFT JOIN location lpod  on (lpod.id = spCom.delivery_loc and spCom.delivery_loc_type='port')"
				+ " LEFT JOIN location lor on lor.id = spCom.por"
				+ " LEFT JOIN location lol on lol.id = spCom.pol"
				+ " LEFT JOIN location lod on lod.id = spCom.pod"
				+ " LEFT JOIN user_login ul on ul.id = spCom.creator"
				+ " LEFT JOIN currency cy on cy.id = spCom.target_currency"
				+ " WHERE spCom.office_id = ? and spCom.id = ?";

		Record order = Db.findFirst(Sql,office_id,id);
		setAttr("order", order);
		setAttr("Supplier_list",getItems(id,"supplierItem"));
//		setAttr("quotation_list",getItems(id,"quotationItem"));
		render("/oms/CostComparison/costComparisonEdit.html");
	}
  	
    
    //返回供应商list
    private List<Record> getItems(String orderId,String para) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("supplierItem".equals(para)){
    		itemSql = "SELECT sps.*,py.abbr sp_name FROM sp_cost_compare_order_sps sps"
        			+ " LEFT JOIN party py on py.id = sps.sp_id"
        			+ " WHERE sps.order_id = ?";
    	}else if("quotationItem".equals(para)){
    		itemSql = "SELECT	scoi.id,py.id item_sp_id,scl.carrier_id,spCon.contract_no,spCon.trans_clause,spCon.trade_type,scl.por_id,scl.pol_id,scl.pod_id,sci.fee_id,sci.price,sci.currency_id,"
    				+ "  py.abbr sp_abbr,pcar.abbr carrier_abbr,lpor.name por_name,lpol.name pol_name,lpod.name pod_name,fi.name fee_name,cy.name currency_name,spCon.id contract_id,"
    				+ "  scl.id contact_loc_id,sci.id contact_item_id,py.sp_type"
    				+ " FROM	sp_cost_compare_order_item scoi "
    				+ " LEFT JOIN sp_cost_compare_order scco ON scoi.order_id = scco.id "
    				+ " LEFT JOIN supplier_contract spCon ON spCon.id = scoi.contract_id"
    				+ " LEFT JOIN supplier_contract_item sci on sci.id = scoi.contact_item_id"
    				+ " LEFT JOIN supplier_contract_location scl on scl.id = scoi.contact_loc_id"
    				+ " LEFT JOIN party py ON py.id = scoi.item_sp_id"
    				+ " LEFT JOIN party pcar ON pcar.id = scl.carrier_id"
    				+ " LEFT JOIN location lpor ON lpor.id = scl.por_id"
    				+ " LEFT JOIN location lpol ON lpol.id = scl.pol_id"
    				+ " LEFT JOIN location lpod ON lpod.id = scl.pod_id"
    				+ " LEFT JOIN fin_item fi ON fi.id = sci.fee_id"
    				+ " LEFT JOIN currency cy on cy.id = sci.currency_id"
    				+ " WHERE scco.id = ?";
    	}
    	itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
  //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("table_type");
    	
    	List<Record> list = null;
    	list = getItems(order_id,type);
    	
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
    }
    
    //提交供应商成本评比单
    public void submit(){
    	String order_id = getPara("order_id");
    	String thisId = getPara("thisId");
    	String orderSql = "select * from sp_cost_compare_order where id =?";
    	
    	Record orderRec = Db.findFirst(orderSql,order_id);
		if("submitBtn".equals(thisId)){
			orderRec.set("status", "已提交");		
		}else if("auditBtn".equals(thisId)){
			orderRec.set("status", "审核通过");	
		}else if("auditBtnDeny".equals(thisId)){
			orderRec.set("status", "审核不通过");
		}   	
    	Db.update("sp_cost_compare_order", orderRec);
    	renderJson(orderRec);
    }
}
