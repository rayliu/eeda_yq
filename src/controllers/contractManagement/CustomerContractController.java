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
import models.eeda.contract.customer.CustomerContract;

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
public class CustomerContractController extends Controller {

    private Logger logger = Logger.getLogger(CustomerContractController.class);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/customerContract");
        setAttr("listConfigList", configList);
        
        render("/eeda/contractManagement/customer/list.html");
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
        		+ " from customer_contract cc "
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
        render("/eeda/contractManagement/customer/edit.html");
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
        
        setAttr("user", user);
        setAttr("charge_items", getItems(id,"ocean"));
        setAttr("ocean_locations", getItems(id,"ocean_loc"));
        setAttr("charge_air_items", getItems(id,"air"));
        setAttr("air_locations", getItems(id,"air_loc"));
        setAttr("charge_land_items", getItems(id,"land"));
        setAttr("land_locations", getItems(id,"land_loc"));
        setAttr("trade_charge_items", getItems(id,"trade"));
        setAttr("tour_charge_items", getItems(id,"tour"));
        setAttr("tour_locations", getItems(id,"tour_loc"));
        
   		setAttr("order", Db.findFirst("select cc.*,p.abbr  from customer_contract cc "
   				+ " LEFT JOIN party p on p.id = cc.customer_id  where cc.id = ? ",id));
        render("/eeda/contractManagement/customer/edit.html");
    }
    
    public void delete() {
       
        String id = getPara();
        
        CustomerContract customerContract = CustomerContract.dao.findById(id);
        
        String obj = customerContract.get("is_stop");
        if(obj == null || "".equals(obj) || "N".equals(obj) || obj.equals(0)){
        	customerContract.set("is_stop", "Y");
        }else{
        	customerContract.set("is_stop", "N");
        }
        customerContract.update();
        redirect("/customerContract");
    }
    
    
    public List<Record> getItems(String contract_id,String type){
    	String sql = "";
    	if("ocean".equals(type)){
    		sql = " SELECT cci.*, fi.name fee_name, "
    		        + " CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from customer_contract_item cci"
					+ " left join customer_contract_location ccl on ccl.id = cci.customer_loc_id"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE ccl.contract_id = ? and cci.contract_type='"+type+"' and ccl.is_select = 'Y'";
    	}else if("land_loc".equals(type) || "tour_loc".equals(type)){
            sql = " SELECT ccl.*, "
                    + " l.dock_name pol_name, "
                    + " l1.dock_name pod_name"
                    +" from customer_contract_location ccl"
                    +" LEFT JOIN dockinfo l on l.id = ccl.pol_id"
                    +" LEFT JOIN dockinfo l1 on l1.id = ccl.pod_id"
                    +" WHERE ccl.contract_id = ? and ccl.type='"+type+"' "; 
    	}else if(type.indexOf("_loc")>0){
                sql = " SELECT ccl.*, "
                        + " CONCAT(l.name,' -', l.code) pol_name, "
                        + " CONCAT(l1.name,' -', l1.code) pod_name"
                        +" from customer_contract_location ccl"
                        +" LEFT JOIN location l on l.id = ccl.pol_id"
                        +" LEFT JOIN location l1 on l1.id = ccl.pod_id"
                        +" WHERE ccl.contract_id = ? and ccl.type='"+type+"' ";	
    	}else if("air".equals(type)){
    		sql = " SELECT cci.*,fi.name fee_name, "
    		        + "CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from customer_contract_item cci"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE cci.contract_id = ? and cci.contract_type='air' ";
    	}else if("land".equals(type)){
    		sql = " SELECT cci.*,fi.name fee_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from customer_contract_item cci"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE cci.contract_id = ?  and cci.contract_type='land'";
    	}else if("trade".equals(type)){
            sql = " SELECT cci.*,fi.name fee_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
                    +" from customer_contract_item cci"
                    +" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
                    +" LEFT JOIN unit u on u.id = cci.uom"
                    +" LEFT JOIN currency c on c.id= cci.currency_id"
                    +" WHERE cci.contract_id = ?  and cci.contract_type='trade'";
        }else if("tour".equals(type)){
            sql = " SELECT cci.*,fi.name fee_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
                    +" from customer_contract_item cci"
                    +" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
                    +" LEFT JOIN unit u on u.id = cci.uom"
                    +" LEFT JOIN currency c on c.id= cci.currency_id"
                    +" WHERE cci.contract_id = ?  and cci.contract_type='tour'";
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
   		CustomerContract  customerContract= new CustomerContract();
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
   			customerContract = CustomerContract.dao.findById(id);
   			
   			String oldContract_no=customerContract.get("contract_no");
   			String oldType =customerContract.get("type");
   			DbUtils.setModelValues(dto, customerContract);
   			if(!type.equals(oldType)){
   				StringBuilder sb = new StringBuilder(oldContract_no);//构造一个StringBuilder对象
				sb.replace(2, 3, generateJobPrefix(type));
				oldContract_no =sb.toString();
				customerContract.set("contract_no", oldContract_no);
   			}
   			
   			customerContract.set("updator", user.getLong("id"));
   			customerContract.set("update_stamp", new Date());
            
   			customerContract.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, customerContract);
//   			String newOrder_on ="EKYZH"+generateJobPrefix(type);
   			if(office_id!=6){
   			//需后台处理的字段
   	   			String contract_no = OrderNoGenerator.getNextOrderNo("EK", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(contract_no);//构造一个StringBuilder对象
   	   			sb.insert(2, generateJobPrefix(type));//在指定的位置，插入指定的字符串（类型代表）
   	   			sb.insert(5, newDateStrMM);//在指定的位置，插入指定的字符串(月份)
   	   			contract_no = sb.toString();
   	   		customerContract.set("contract_no", contract_no);
   	   		customerContract.set("creator", user.getLong("id"));
   	   		customerContract.set("create_date", new Date());
   	   		customerContract.set("status", "新建");
   	   		customerContract.set("updator", user.getLong("id"));
   	   		customerContract.set("update_stamp", new Date());
   	   		customerContract.set("office_id", office_id);
   	   		customerContract.save();
   	   		id = customerContract.getLong("id").toString();
   			}
   			if(office_id==6){
   			//需后台处理的字段
   	   			String contract_no = OrderNoGenerator.getNextOrderNo("KF", newDateStr, office_id);
   	   			StringBuilder sb = new StringBuilder(contract_no);//构造一个StringBuilder对象
   	   			sb.insert(2, generateJobPrefix(type));//在指定的位置1，插入指定的字符串
   	   			contract_no = sb.toString();
   	   		customerContract.set("contract_no", contract_no);
   	   		customerContract.set("creator", user.getLong("id"));
   	   		customerContract.set("create_date", new Date());
   	   		customerContract.set("status", "新建");
   	   		customerContract.set("updator", user.getLong("id"));
   	   		customerContract.set("update_stamp", new Date());
   	   		customerContract.set("office_id", office_id);
   	   		customerContract.save();
   	   		id = customerContract.getLong("id").toString();
   	   			
   			}
   			
   		}
   		//海运费用明细保存
   		
   		List<Map<String,String>> oceanLocs = (ArrayList<Map<String, String>>) dto.get("itemOceanLocList");
        DbUtils.handleList(oceanLocs, "customer_contract_location", id,"contract_id");
        
        Record re = Db.findFirst("select * from customer_contract_location where contract_id = ? and type = 'ocean_loc' and is_select = 'Y' ",id);
        if(re != null){
        	List<Map<String,String>> charge_items = (ArrayList<Map<String, String>>) dto.get("itemOceanList");
       		DbUtils.handleList(charge_items, "customer_contract_item", re.get("id").toString(),"customer_loc_id");	
        }
        
   		List<Map<String,String>> charge_air_items = (ArrayList<Map<String, String>>) dto.get("itemAirList");
   		DbUtils.handleList(charge_air_items, "customer_contract_item", id,"contract_id");
        List<Map<String,String>> airLocs = (ArrayList<Map<String, String>>) dto.get("itemAirLocList");
        DbUtils.handleList(airLocs, "customer_contract_location", id,"contract_id");
   		
   		List<Map<String,String>> charge_land_items = (ArrayList<Map<String, String>>) dto.get("itemLandList");
   		DbUtils.handleList(charge_land_items, "customer_contract_item", id,"contract_id");
        List<Map<String,String>> landLocs = (ArrayList<Map<String, String>>) dto.get("itemLandLocList");
        DbUtils.handleList(landLocs, "customer_contract_location", id,"contract_id");
   		
        List<Map<String,String>> charge_trade_items = (ArrayList<Map<String, String>>) dto.get("itemTradeList");
        DbUtils.handleList(charge_trade_items, "customer_contract_item", id,"contract_id");
        
        List<Map<String,String>> charge_tour_items = (ArrayList<Map<String, String>>) dto.get("itemTourList");
        DbUtils.handleList(charge_tour_items, "customer_contract_item", id,"contract_id");
        List<Map<String,String>> tourLocs = (ArrayList<Map<String, String>>) dto.get("itemTourLocList");
        DbUtils.handleList(tourLocs, "customer_contract_location", id,"contract_id");
        
   		Record rcon = new Record();
   		rcon= Db.findFirst("select * from customer_contract joc where id = ? ",id);
//   		rcon.set("charge_items", getItems(id));
       
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(rcon);
        
    }


  //异步刷新字表
    public void clickItem(){
    	String contract_id = getPara("contract_id");
    	String type = getPara("type");
    	String costomer_loc_id = getPara("customer_loc_id");

    	String sql = "";
    	if("ocean".equals(type)){
    		sql = " SELECT cci.*, fi.name fee_name, "
    		        + " CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from customer_contract_item cci"
					+ " left join customer_contract_location ccl on ccl.id = cci.customer_loc_id"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE cci.contract_type='ocean' and cci.customer_loc_id = ?";
    	}
    	
    	List<Record> list = Db.find(sql,costomer_loc_id);

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
