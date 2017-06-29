package controllers.contractManagement;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.contract.customer.SupplierContract;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class SupplierContractController extends Controller {

    private Logger logger = Logger.getLogger(SupplierContractController.class);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/supplierContract");
        setAttr("listConfigList", configList);
        
        render("/eeda/contractManagement/sp/list.html");
    }
    
    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from ( select cc.*,p.abbr customer_name,u.c_name creator_name ,"
        		+ " cast(CONCAT(substring(cc.contract_begin_time,1,10),' 到 ',substring(cc.contract_end_time,1,10))  as char) contract_period"
        		+ " from supplier_contract cc "
        		+" LEFT JOIN party p ON p.id = cc.customer_id "
        		+" LEFT JOIN user_login u on u.id = cc.creator "
        		+ " where  cc.office_id ="+user.getOfficeId()+")A where 1=1";
      
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql+ condition + " order by create_date desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }
    
    @Before(EedaMenuInterceptor.class)
    public void add() {
        setAttr("user", LoginUserController.getLoginUser(this));
        render("/eeda/contractManagement/sp/edit.html");
    }
    
  //根据合同类型生成不同前缀
    public static String generateJobPrefix(String type){
    		String prefix = "";
			if(type.equals("出口柜货")||type.equals("进口柜货")||type.equals("出口散货")||type.equals("内贸海运")){
				prefix+="O";
			}
			else if(type.equals("出口空运")||type.equals("进口空运")){
				prefix+="A";
			}
			else if(type.equals("香港头程")||type.equals("香港游")||type.equals("进口散货")){
				prefix+="L";
			}
			else if(type.equals("加贸")||type.equals("园区游")){
				prefix+="P";
			}
			else if(type.equals("陆运")){
				prefix+="T";
			}
			else if(type.equals("报关")){
				prefix+="C";
			}
			else if(type.equals("快递")){
				prefix+="E";
			}
			else if(type.equals("贸易")){
				prefix+="B";
			}
			return prefix;
    }
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		SupplierContract  supplierContract= new SupplierContract();
        
        
        setAttr("user", LoginUserController.getLoginUser(this));
        setAttr("charge_items", getItems(id,"ocean"));
        setAttr("ocean_locations", getItems(id,"ocean_loc"));
        setAttr("charge_air_items", getItems(id,"air"));
        setAttr("air_locations", getItems(id,"air_loc"));
        setAttr("charge_land_items", getItems(id,"land"));
        setAttr("land_locations", getItems(id,"land_loc"));
        setAttr("trade_charge_items", getItems(id,"trade"));
        setAttr("tour_charge_items", getItems(id,"tour"));
        setAttr("tour_locations", getItems(id,"tour_loc"));
        
   		setAttr("order", Db.findFirst("select cc.*,p.abbr  from supplier_contract cc "
   				+ " LEFT JOIN party p on p.id = cc.customer_id  where cc.id = ? ",id));
        render("/eeda/contractManagement/sp/edit.html");
    }
    
    public void delete() {
       
        String id = getPara();
        
        SupplierContract supplierContract = SupplierContract.dao.findById(id);
        
        String obj = supplierContract.get("is_stop");
        if(obj == null || "".equals(obj) || "N".equals(obj) || obj.equals(0)){
        	supplierContract.set("is_stop", "Y");
        }else{
        	supplierContract.set("is_stop", "N");
        }
        supplierContract.update();
        redirect("/supplierContract");
    }
    
    
    public List<Record> getItems(String contract_id,String type){
    	String sql = "";
    	if("land_loc".equals(type) || "tour_loc".equals(type)){
            sql = " SELECT ccl.*, "
                    + " l.dock_name pol_name, "
                    + " l1.dock_name pod_name"
                    +" from supplier_contract_location ccl"
                    +" LEFT JOIN dockinfo l on l.id = ccl.pol_id"
                    +" LEFT JOIN dockinfo l1 on l1.id = ccl.pod_id"
                    +" WHERE ccl.contract_id = ? and ccl.type='"+type+"' "; 
    	}else if(type.indexOf("_loc")>0){
                sql = " SELECT ccl.*,p.abbr carrier_name, "
                        + " CONCAT(l.name,' -', l.code) pol_name, "
                        + " CONCAT(l1.name,' -', l1.code) pod_name,"
                        + " CONCAT(l2.name,' -', l2.code) hub_name,"
                        + " CONCAT(l3.name,' -', l3.code) por_name"
                        +" from supplier_contract_location ccl"
                        +" LEFT JOIN location l on l.id = ccl.pol_id"
                        +" LEFT JOIN location l1 on l1.id = ccl.pod_id"
                        +" LEFT JOIN location l2 on l2.id = ccl.hub_id"
                        +" LEFT JOIN location l3 on l3.id = ccl.por_id"
                        + " left join party p on p.id = ccl.carrier_id"
                        +" WHERE ccl.contract_id = ? and ccl.type='"+type+"' ";	
    	}else if("trade".equals(type)){
            sql = " SELECT cci.*,fi.name fee_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
                    +" from supplier_contract_item cci"
                    +" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
                    +" LEFT JOIN unit u on u.id = cci.uom"
                    +" LEFT JOIN currency c on c.id= cci.currency_id"
                    +" WHERE cci.contract_id = ?  and cci.contract_type='trade'";
        }else if("tour".equals(type)){
            sql = " SELECT cci.*,fi.name fee_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
                    +" from supplier_contract_item cci"
                    +" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
                    +" LEFT JOIN unit u on u.id = cci.uom"
                    +" LEFT JOIN currency c on c.id= cci.currency_id"
                    +" WHERE cci.contract_id = ?  and cci.contract_type='tour'";
        }else {  
    		sql = " SELECT cci.*,fi.name fee_name, "
    		        + "u.name uom_name,c.name currency_name"
					+" from supplier_contract_item cci"
					+ " left join supplier_contract_location ccl on ccl.id = cci.supplier_loc_id"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE ccl.contract_id = ? and cci.contract_type='"+type+"' and ccl.is_select = 'Y' ";
    	}
    	
    	
    	List<Record> re = Db.find(sql,contract_id);
    	return re;
    }
    
    
    @Before(Tx.class)
    public void save() throws InstantiationException, IllegalAccessException {
        String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
   		String type = (String) dto.get("type");//根据合同类型生成不同前缀
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		SupplierContract  supplierContract= new SupplierContract();
   		String id = (String) dto.get("contract_id");

        String newDateStr = "";
        	
        String newDateStrMM = "";
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");//分析日期
        SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
        SimpleDateFormat sdfMM = new SimpleDateFormat("MM");//转换后的格式
        	newDateStr = sdf.format(new Date());
        	newDateStrMM = sdfMM.format(new Date());
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			supplierContract = SupplierContract.dao.findById(id);
   			
   			String oldContract_no=supplierContract.get("contract_no");
   			String oldType =supplierContract.get("type");
   			DbUtils.setModelValues(dto, supplierContract);
   			if(!type.equals(oldType)){
   				StringBuilder sb = new StringBuilder(oldContract_no);//构造一个StringBuilder对象
				sb.replace(2, 3, generateJobPrefix(type));
				oldContract_no =sb.toString();
				supplierContract.set("contract_no", oldContract_no);
   			}
   			
   			supplierContract.set("updator", user.getLong("id"));
   			supplierContract.set("update_stamp", new Date());
            
   			supplierContract.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, supplierContract);
//   			String newOrder_on ="EKYZH"+generateJobPrefix(type);
   			if(office_id!=6){
   			//需后台处理的字段
   	   			String contract_no = OrderNoGenerator.getNextOrderNo("EK", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(contract_no);//构造一个StringBuilder对象
   	   			sb.insert(2, generateJobPrefix(type));//在指定的位置，插入指定的字符串（类型代表）
   	   			sb.insert(5, newDateStrMM);//在指定的位置，插入指定的字符串(月份)
   	   			contract_no = sb.toString();
   	   		supplierContract.set("contract_no", contract_no);
   	   		supplierContract.set("creator", user.getLong("id"));
   	   		supplierContract.set("create_date", new Date());
   	   		supplierContract.set("status", "新建");
   	   		supplierContract.set("updator", user.getLong("id"));
   	   		supplierContract.set("update_stamp", new Date());
   	   		supplierContract.set("office_id", office_id);
   	   		supplierContract.save();
   	   		id = supplierContract.getLong("id").toString();
   			}
   			if(office_id==6){
   			//需后台处理的字段
   	   			String contract_no = OrderNoGenerator.getNextOrderNo("KF", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(contract_no);//构造一个StringBuilder对象
   	   			sb.insert(2, generateJobPrefix(type));//在指定的位置1，插入指定的字符串
   	   			contract_no = sb.toString();
   	   		supplierContract.set("contract_no", contract_no);
   	   		supplierContract.set("creator", user.getLong("id"));
   	   		supplierContract.set("create_date", new Date());
   	   		supplierContract.set("status", "新建");
   	   		supplierContract.set("updator", user.getLong("id"));
   	   		supplierContract.set("update_stamp", new Date());
   	   		supplierContract.set("office_id", office_id);
   	   		supplierContract.save();
   	   		id = supplierContract.getLong("id").toString();
   	   			
   			}
   			
   		}
   		//海运路线、费用明细保存
   		List<Map<String,String>> oceanLocs = (ArrayList<Map<String, String>>) dto.get("itemOceanLocList");
        DbUtils.handleList(oceanLocs, "supplier_contract_location", id,"contract_id");
        Record re = Db.findFirst("select * from supplier_contract_location where contract_id = ? and type ="
        		                + "  'ocean_loc' and is_select = 'Y' ",id);
        if(re != null){
	   		List<Map<String,String>> charge_items = (ArrayList<Map<String, String>>) dto.get("itemOceanList");
	   		DbUtils.handleList(charge_items, "supplier_contract_item", re.get("id").toString(),"supplier_loc_id");
        }
        
        //空运路线、费用明细保存
        List<Map<String,String>> airLocs = (ArrayList<Map<String, String>>) dto.get("itemAirLocList");
        DbUtils.handleList(airLocs, "supplier_contract_location", id,"contract_id");
        
        Record reAir = Db.findFirst("select * from supplier_contract_location where contract_id = ? and type ="
                + "  'air_loc' and is_select = 'Y' ",id);
		if(reAir != null){
			List<Map<String,String>> charge_air_items = (ArrayList<Map<String, String>>) dto.get("itemAirList");
			DbUtils.handleList(charge_air_items, "supplier_contract_item", reAir.get("id").toString(),"supplier_loc_id");
		}
        
        //陆运路线、费用明细保存
        List<Map<String,String>> landLocs = (ArrayList<Map<String, String>>) dto.get("itemLandLocList");
        DbUtils.handleList(landLocs, "supplier_contract_location", id,"contract_id");
        
        Record reLand = Db.findFirst("select * from supplier_contract_location where contract_id = ? and type ="
                + "  'land_loc' and is_select = 'Y' ",id);
		if(reLand != null){
			List<Map<String,String>> charge_land_items = (ArrayList<Map<String, String>>) dto.get("itemLandList");
			DbUtils.handleList(charge_land_items, "supplier_contract_item", reLand.get("id").toString(),"supplier_loc_id");
		}

        
   		
        List<Map<String,String>> charge_trade_items = (ArrayList<Map<String, String>>) dto.get("itemTradeList");
        DbUtils.handleList(charge_trade_items, "supplier_contract_item", id,"contract_id");
        
        List<Map<String,String>> charge_tour_items = (ArrayList<Map<String, String>>) dto.get("itemTourList");
        DbUtils.handleList(charge_tour_items, "supplier_contract_item", id,"contract_id");
        List<Map<String,String>> tourLocs = (ArrayList<Map<String, String>>) dto.get("itemTourLocList");
        DbUtils.handleList(tourLocs, "supplier_contract_location", id,"contract_id");
   		
   		Record rcon = new Record();
   		rcon= Db.findFirst("select * from supplier_contract joc where id = ? ",id);
        //rcon.set("charge_items", getItems(id));
       
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(rcon);
        
    }
    
    
  //异步刷新字表
    public void clickItem(){
    	String contract_id = getPara("contract_id");
    	String type = getPara("type");
    	String supplier_loc_id = getPara("supplier_loc_id");

    	String sql = "";
    		sql = " SELECT cci.*, fi.name fee_name, "
    		        + " CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from supplier_contract_item cci"
					+ " left join supplier_contract_location ccl on ccl.id = cci.supplier_loc_id"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE cci.contract_type='"+type+"' and cci.supplier_loc_id = ?";

    	
    	List<Record> list = Db.find(sql,supplier_loc_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
    
    

    
    //异步刷新字表
    public void tableList(){
    	String contract_id = getPara("contract_id");
    	String type = getPara("type");
    	List<Record> list = null;
    	list = getItems(contract_id,type);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
}
