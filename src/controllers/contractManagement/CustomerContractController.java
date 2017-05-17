package controllers.contractManagement;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
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
        String sql = " select * from customer_contract where office_id="+user.getOfficeId();
      
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
   		long office_id = user.getLong("office_id");
   		CustomerContract  customerContract= new CustomerContract();
        
        
        setAttr("user", LoginUserController.getLoginUser(this));
        setAttr("charge_items", getItems(id));
   		setAttr("order", Db.findFirst("select cc.*,p.abbr  from customer_contract cc "
   				+ " LEFT JOIN party p on p.id = cc.customer_id  where cc.id = ? ",id));
        render("/eeda/contractManagement/customer/edit.html");
    }
    
    public void delete() {
       
        String id = getPara();
        
        Party party = Party.dao.findById(id);
        
        Object obj = party.get("is_stop");
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	party.set("is_stop", true);
        }else{
        	party.set("is_stop", false);
        }
        party.update();
        redirect("/serviceProvider");
    }
    
    
    public List<Record> getItems(String contract_id){
    	String sql = " SELECT cci.*,fi.name fee_name,l.name pol_name,l1.name pod_name,CONCAT(u.name,u.name_eng) uom_name,c.name currency_name"
					+" from customer_contract_item cci"
					+" LEFT JOIN fin_item fi on fi.id = cci.fee_id"
					+" LEFT JOIN location l on l.id = cci.pol_id"
					+" LEFT JOIN location l1 on l1.id = cci.pod_id"
					+" LEFT JOIN unit u on u.id = cci.uom"
					+" LEFT JOIN currency c on c.id= cci.currency_id"
					+" WHERE cci.contract_id = ? ";
    	
    	
    	
    	List<Record> re = Db.find(sql,contract_id);
    	return re;
    }
    
    

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyMM");//转换后的格式
        	newDateStr = sdf.format(new Date());
   		
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
   		//费用明细保存
   		List<Map<String,String>> charge_items = (ArrayList<Map<String, String>>) dto.get("itemList");
   		DbUtils.handleList(charge_items, "customer_contract_item", id,"contract_id");
   		
//		List<Map<String, String>> shipment_detail = (ArrayList<Map<String, String>>)dto.get("shipment_detail");
//		DbUtils.handleList(shipment_detail, id, JobOrderShipment.class, "order_id");
   		
   		Record rcon = new Record();
   		rcon= Db.findFirst("select * from customer_contract joc where id = ? ",id);
   		rcon.set("charge_items", getItems(id));
       
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(rcon);
        
    }


    
    //异步刷新字表
    public void tableList(){
    	String contract_id = getPara("contract_id");
    	
    	List<Record> list = null;
    	list = getItems(contract_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
}
