package controllers.contractManagement;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Party;
import models.UserLogin;
import models.eeda.contract.customer.CustomerContract;
import models.yh.profile.ProviderChargeType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
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

    private void setContact(Party contact) {
    	contact.set("code", getPara("code"));
    	contact.set("quick_search_code", getPara("quick_search_code"));
    	contact.set("fax", getPara("fax"));
    	contact.set("company_name", getPara("company_name"));
        contact.set("company_name_eng", getPara("company_name_eng"));
        contact.set("contact_person", getPara("contact_person"));
        contact.set("contact_person_eng", getPara("contact_person_eng")==""?null:getPara("contact_person_eng"));
        contact.set("location", getPara("location"));
        contact.set("email", getPara("email"));
        contact.set("abbr", getPara("abbr"));
        String sp_type = (getPara("sp_type_line")==null?"":getPara("sp_type_line") +";")
                + (getPara("sp_type_delivery")==null?"":getPara("sp_type_delivery") +";")
                + (getPara("sp_type_pickup")==null?"":getPara("sp_type_pickup") +";")
                + (getPara("sp_type_personal")==null?"":getPara("sp_type_personal") +";")
                + (getPara("sp_type_carrier")==null?"":getPara("sp_type_carrier") +";")
                + (getPara("sp_type_air")==null?"":getPara("sp_type_air") +";")
                + (getPara("sp_type_broker")==null?"":getPara("sp_type_broker") +";")
                + (getPara("sp_type_head_car")==null?"":getPara("sp_type_head_car") +";")
                + (getPara("sp_type_oversea_agent")==null?"":getPara("sp_type_oversea_agent")+";")
        		+ (getPara("sp_type_booking_agent")==null?"":getPara("sp_type_booking_agent")+";")
        		+ (getPara("sp_type_cargo_agent")==null?"":getPara("sp_type_cargo_agent")+";")
        		+ (getPara("sp_type_truck")==null?"":getPara("sp_type_truck")+";")
		        + (getPara("sp_type_manufacturer")==null?"":getPara("sp_type_manufacturer")+";")
				+ (getPara("sp_type_traders")==null?"":getPara("sp_type_traders")+";");
        contact.set("sp_type", sp_type);
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("address_eng", getPara("address_eng")==""?null:getPara("address_eng"));
        contact.set("introduction", getPara("introduction"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
    }

    public void province() {
        List<Record> locationList = Db.find("select * from location where pcode ='1'");
        renderJson(locationList);
    }

    public void city() {
        String cityId = getPara("id");
        System.out.println(cityId);
        List<Record> locationList = Db.find("select * from location where pcode ='" + cityId + "'");
        renderJson(locationList);
    }

    public void area() {
        String areaId = getPara("id");
        System.out.println(areaId);
        List<Record> locationList = Db.find("select * from location where pcode ='" + areaId + "'");
        renderJson(locationList);
    }

    public void searchAllCity() {
        String province = getPara("province");
        List<Location> locations = Location.dao
                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"
                        + province + "'))");
        renderJson(locations);
    }

    public void searchAllDistrict() {
        String city = getPara("city");
        List<Location> locations = Location.dao
                .find("select * from location where pcode=(select code from location where name = '" + city + "')");
        renderJson(locations);
    }
    
    // 一次查出省份,城市,区
    public void searchAllLocation() {    	
    	List<Location> provinceLocations = Location.dao.find("select * from location where pcode ='1'");
    	
        String province = getPara("province");
        List<Location> cityLocations = Location.dao
                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"
                        + province + "'))");
        
        String city = getPara("city");
        List<Location> districtLocations = Location.dao
                .find("select * from location where pcode=(select code from location where name = '" + city + "')");
        Map<String, List<Location>> map = new HashMap<String, List<Location>>();
        map.put("provinceLocations", provinceLocations);
        map.put("cityLocations", cityLocations);
        map.put("districtLocations", districtLocations);
    	renderJson(map);
    }
    
    //查询结算公司
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchSp() {
		String input = getPara("input");
		
		if(StringUtils.isEmpty(input)){
			input = "";
        }
		
		long userId = LoginUserController.getLoginUserId(this);
		UserLogin user = LoginUserController.getLoginUser(this);
		long office_id = user.getLong("office_id");
		
		List<Record> spList = Collections.EMPTY_LIST;
		
		if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr from user_query_history h, party p "
                    + "where h.ref_id=p.id and h.type='ARAP_COM' and h.user_id=?";
            spList = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);
            if(spList.size()==0){
                sql = "select p.* from party p where office_id="+office_id;
                spList = Db.find(sql+" order by abbr limit 10");
            }
            renderJson(spList);
        }else{
            String sql = "select p.* from party p where office_id="+office_id;
                        
            if (input.trim().length() > 0) {
                sql +=" and (p.abbr like '%" + input + "%' or p.quick_search_code like '%" +input.toLowerCase()+ "%'"
                	+ " or p.quick_search_code like '%" + input.toUpperCase() + "%') ";
            }
            spList = Db.find(sql+" order by abbr limit 10");

            renderJson(spList);
        }
    }



   
   
    public void chargeTypeList(){
    	String id = getPara("typeId");
    	
    	String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        Map chargeTypeMap = new HashMap();
    	if(id == null || "".equals(id)){
    		chargeTypeMap.put("sEcho", 0);
    		chargeTypeMap.put("iTotalRecords", 0);
    		chargeTypeMap.put("iTotalDisplayRecords", 0);
    		chargeTypeMap.put("aaData", null);
    	}else{
    		String totalSql = "select count(*) as total from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = " + id;
    		Record rec = Db.findFirst(totalSql);
    		
    		List<ProviderChargeType> list = ProviderChargeType.dao.find("select ct.id,ct.charge_type,ifnull(c.abbr,c.company_name) as customer_name,ct.remark from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = ? " + sLimit,id);
    		chargeTypeMap.put("sEcho", pageIndex);
    		chargeTypeMap.put("iTotalRecords", rec.get("total"));
    		chargeTypeMap.put("iTotalDisplayRecords", rec.get("total"));
    		chargeTypeMap.put("aaData", list);
    	}
    	renderJson(chargeTypeMap);
    	
    }
    public void saveChargeType(){
    	String sp_id = getPara("sp_id");
    	String item_id = getPara("chargeTypeItemId");
    	String customer_id = getPara("customer_id");
    	String type = getPara("c_type");
    	String remark = getPara("chargeTypeRemark");
    	if(sp_id == null || "".equals(sp_id)){
    		renderJson();
    	}
    	ProviderChargeType p = ProviderChargeType.dao.findFirst("select * from charge_type where sp_id = ? and customer_id = ? ",sp_id,customer_id);
    	if(p != null){
    		p.set("remark", remark);
    		p.set("charge_type", type);
    		p.update();
    		renderJson(p);
    	}else{
    		ProviderChargeType pct = null;
        	if(item_id == null || "".equals(item_id)){
        		//保存数据
        		pct = new ProviderChargeType();
        		
        		pct.set("sp_id", sp_id);
        		pct.set("customer_id",customer_id);
        		pct.set("remark", remark);
        		pct.set("charge_type",type);
        		pct.save();
        	}else{
        		//更新数据
        		pct = ProviderChargeType.dao.findById(item_id);
        		//pct.set("customer_id", customer_id);
        		pct.set("remark", remark);
        		pct.set("charge_type", type);
        		pct.update();
        	}
        	renderJson(pct);
    	}
    	
    }
    public void delChargeType(){
    	String id = getPara("id");
    	if(id != null && !"".equals(id)){
    		ProviderChargeType.dao.deleteById(id);
    		renderJson("{\"success\":true}");
    	}else{
    		renderJson("{\"success\":false}");
    	}
    	
    }
    public void editChargeType(){
    	String id = getPara("id");
    	ProviderChargeType pct = ProviderChargeType.dao.findFirst("select ct.*,ifnull(c.abbr,c.company_name) as customer_name from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.id = ?",id);
    	renderJson(pct);
    }
    public void seachChargeType(){
    	String sp_id = getPara("sp_id");
    	String customer_id = getPara("customer_id");
    	ProviderChargeType pct = null;
    	if(sp_id != null && !"".equals(sp_id) && customer_id != null && !"".equals(customer_id)){
    		pct = ProviderChargeType.dao.findFirst("select charge_type from charge_type where sp_id = ? and customer_id = ?",sp_id,customer_id);
    		if(pct != null){
    			renderJson(pct);
    		}else{
    			Party p = Party.dao.findById(sp_id);
    			
    			renderJson("{\"charge_type\":" + p.getStr("charge_typ") + "}");
    		}
    	}else{
    		renderJson("{\"charge_type\":error}");
    	}
    	
    }
    
    //查询船公司下拉, 这里不用过滤office, 因为船公司是通用的
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCarrier(){
        UserLogin user = LoginUserController.getLoginUser(this);
        
        String name = getPara("input");
        List<Record> recs = null;
        String sql = "select id,abbr name from party p where p.type = 'SP' and p.sp_type like '%carrier%' ";
        if(!StringUtils.isBlank(name)){
        	sql+=" and p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ";
        }
        recs = Db.find(sql);
        renderJson(recs);
    }
    
   
    
    //查询航空公司下拉
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchAirCompany(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
    	String name = getPara("input");
    	String sp_type = getPara("para");
    	List<Record> recs = null;
    	String sql = "select p.id,p.abbr name from party p where office_id="+office_id+" and p.type = 'SP' and p.sp_type like '%"+sp_type+"%' ";
    	if(!StringUtils.isBlank(name)){
    		sql+=" and (p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%') ";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    //查询运输公司下拉
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckCompany(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
    	String name = getPara("input");
    	String sp_type = getPara("para");
    	List<Record> rec = null;
    	String sql = "select p.id,p.abbr name from party p where office_id="+office_id+" and p.type = 'SP' and p.sp_type like '%"+sp_type+"%' ";
    	if(!StringUtils.isBlank(name)){
    		sql+=" and p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ";
    	}
    	rec = Db.find(sql);
    	renderJson(rec);
    }
    
    //查询单位下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchUnit(){
    	String input = getPara("input");
    	List<Record> recs = null;
    	String sql = "select id,CONCAT(name,name_eng) name from unit where type='order'";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and name like '%" + input + "%' "+"or name_eng like '%"+input+"%'";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    //查询工作单应收应付的单位下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchChargeUnit(){
    	String input = getPara("input");
    	List<Record> recs = null;
    	String sql = "select id, name from unit where type='charge'";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and name like '%" + input + "%' "+"or name_eng like '%"+input+"%'";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    //查询币制名下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCurrency(){
    	String input = getPara("input");
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	String d = sf.format(new Date());
    	List<Record> recs = null;
    	
    	String sql = "select c.id,c.code,c.name,cast( if(cr.from_stamp<'"+d+"' and cr.to_stamp>'"+d+"',cr.rate,'') as char ) rate from currency c"
    			+ " left join currency_rate cr on cr.currency_code = c.code where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (c.name like '%" + input + "%' or c.english_name like '%" + input + "%' or c.code like '%" + input + "%') ";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    
  //查询银行账户名下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchAccount(){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
    	String input = getPara("input");
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	String d = sf.format(new Date());
    	List<Record> recs = null;
    	
    	String sql = "select * from fin_account fa where fa.bank_name!='现金' and office_id="+office_id;
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (fa.bank_name like '%" + input + "%') ";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }

    
    //查询发货人下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckOut(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String name = getPara("input");
        List<Record> rec = null;
        String sql = "select p.id,p.abbr name, p.phone, p.address from party p where office_id="+office_id;
        if(!StringUtils.isBlank(name)){
            sql+=" and p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ";
        }
        rec = Db.find(sql);
        renderJson(rec);
    }
    
    //查询收货人下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckIn(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String name = getPara("input");
        List<Record> rec = null;
        String sql = "select p.id,p.abbr name, p.phone, p.address from party p where office_id="+office_id;
        if(!StringUtils.isBlank(name)){
            sql+=" and p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ";
        }
        rec = Db.find(sql);
        renderJson(rec);
    }
    
    //查询贸易方式下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchSupervisionMethod(){
        String name = getPara("input");
        List<Record> rec = null;
        String sql = "select * from supervision_method s where 1=1 ";
        if(!StringUtils.isBlank(name)){
            sql+=" and s.name like '%" + name + "%' ";
        }
        rec = Db.find(sql + " limit 10");
        renderJson(rec);
    }
    
    //查询报关征免性质下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCustomExemptionNature(){
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(code,' ',name) name from custom_exemption_nature where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (code like '%" + input + "%' or name like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 10");
    	renderJson(rec);
    }
    
    //查询报关境内货源地下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchGoodsSupply(){
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(code,' ',name) name from custom_goods_supply where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (code like '%" + input + "%' or name like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 10");
    	renderJson(rec);
    }
    
    //查询报关境内货源地下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchExportPort(){
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(under_code,' ',under_port) name from custom_port where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (code like '%" + input + "%' or under_port like '%" + input + "%' or under_code like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 10");
    	renderJson(rec);
    }
    
    //查询企业海关代码
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCompanyCustomCode(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(abbr,' ',ifnull(custom_registration,'')) name,"
    			+ " concat(ifnull(ifnull(address_eng, address),''),'\n',ifnull(ifnull(contact_person_eng, contact_person),''),'\n',ifnull(phone,''),ifnull(fax,'')) str"
    			+ " from party where office_id="+office_id + " and type in('CUSTOMER', 'SP')";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (custom_registration like '%" + input + "%' or abbr like '%" + input + "%' or company_name like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 10");
    	renderJson(rec);
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
