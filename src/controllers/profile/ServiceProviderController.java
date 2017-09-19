package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.PartyMark;
import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/serviceProvider");
        setAttr("listConfigList", configList);
        render("/eeda/profile/serviceProvider/serviceProviderList.html");
    }
    
    public void list() {
    	Long parentID = pom.getParentOfficeId();
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " select * from (select p.* from party p"
                    + " left join office o on o.id = p.office_id"
                    + " where p.type='SP' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")"
                    + " ) A where 1=1";
      
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
        setAttr("saveOK", false);
            render("/eeda/profile/serviceProvider/serviceProviderEdit.html");
    }
    
    
    //返回对象	
    private List<Record> getItemDetail(String id,String type){
     	String itemSql = "";
    	List<Record> itemList = null;
    	if("contacts".equals(type)){
    		itemSql = "SELECT * FROM contacts_item WHERE party_id=?";
    		itemList = Db.find(itemSql, id);
    	}else if("cars".equals(type)){
    		itemList = Db.find("SELECT * FROM carinfo WHERE parent_id = ?",id);
		}else {
    		itemList = Db.find("SELECT * FROM fin_account WHERE order_id = ?",id);
		}
		return itemList;
    }
    
    
    
    
    
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("party", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
        Party party = Party.dao.findById(id);
        
        String code = party.get("location");

//        Record re = Db.findFirst("select get_loc_full_name('"+code+"') as loc_name");
//        setAttr("location", re.getStr("loc_name"));
        
        setAttr("party", party);
        setAttr("user", LoginUserController.getLoginUser(this));
        setAttr("itemList", getItemDetail(id,""));
        setAttr("contacts_itemList", getItemDetail(id,"contacts"));
        render("/eeda/profile/serviceProvider/serviceProviderEdit.html");
    }
    
  //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	List<Record> list = null;
//    	list = Db.find("SELECT * FROM fin_account WHERE order_id = ?",order_id);
    	
    	list=getItemDetail(order_id, type);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
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
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_P_CREATE, PermissionConstant.PERMSSION_P_UPDATE}, logical=Logical.OR)
    @SuppressWarnings("unchecked")
	@Before(Tx.class)
    public void save() throws Exception { {
        String id = getPara("party_id");
        Party party = null;
        Party contact = null;
        Party contact1 = null;
        Party contact2 = null;
        Date createDate = Calendar.getInstance().getTime();
        if (id != null && !id.equals("")) {
            party = Party.dao.findById(id);
            party.set("last_updated_stamp", createDate);
            party.set("remark", getPara("remark"));
            party.set("receipt", getPara("receipt"));
            party.set("payment", getPara("payment"));
            String status = getPara("status");
            if(status==null){
            	party.set("status", "新建");
            }
            party.set("receiver", getPara("receiver"));
            party.set("bank_no", getPara("bank_no"));
            party.set("bank_name", getPara("bank_name"));           
            
            setParty(party);
            if(StringUtils.isBlank(party.getStr("code"))){
            	String code = OrderNoGenerator.getOrderNo("party",pom.getCurrentOfficeId());
            	if(StringUtils.isNotBlank(code)){
            		party.set("code", code.replace("P", "S"));
            	}
            }            
            party.update();
        } else {
            //判断供应商简称
            contact1 = Party.dao.findFirst("select * from party where abbr=? and office_id=?", getPara("abbr"), pom.getCurrentOfficeId());
            if(contact1!=null){
            	renderText("abbrError");
            	return ;
            }
          //判断供应商全称
            contact2 = Party.dao.findFirst("select * from party where company_name=? and office_id=?", getPara("company_name"), pom.getCurrentOfficeId()); 
            if(contact2!=null){
            	renderText("companyError");
            	return ;
            }
            
            party = new Party();
            party.set("type", Party.PARTY_TYPE_SERVICE_PROVIDER);
            party.set("creator", LoginUserController.getLoginUserId(this));
            party.set("create_date", createDate);
            
            party.set("status", "新建");
            party.set("receipt", getPara("receipt"));
            party.set("remark", getPara("remark"));
            party.set("payment", getPara("payment"));
            party.set("charge_type", getPara("chargeType"));
            party.set("office_id", pom.getCurrentOfficeId());
            setParty(party);
            String code = OrderNoGenerator.getOrderNo("party",pom.getCurrentOfficeId());
            if(StringUtils.isNotBlank(code)){
            	party.set("code", code.replace("P", "S"));
            }
            party.save();

        }
        String acount_json = getPara("acount_json");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(acount_json, HashMap.class);  
        //保存账户信息
        String order_id = party.get("id").toString();
        List<Map<String, String>> acount = (ArrayList<Map<String, String>>)dto.get("acount_json");
		DbUtils.handleList(acount, order_id, FinAccount.class, "order_id");
		//保存联系人信息
		List<Map<String, String>> contacts = (ArrayList<Map<String, String>>)dto.get("contacts_json");
		DbUtils.handleList(contacts, "contacts_item", order_id, "party_id");
		//保存公司车辆信息
		List<Map<String, String>> cars = (ArrayList<Map<String, String>>)dto.get("cars_json");
		DbUtils.handleList(cars, "carinfo", order_id, "parent_id");
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(party);
      }
    }

    private void setParty(Party contact) {
    	contact.set("code", getPara("code"));
    	contact.set("quick_search_code", getPara("quick_search_code"));
    	contact.set("fax", getPara("fax"));
    	contact.set("company_name", getPara("company_name"));
    	contact.set("bill_of_lading_info", getPara("bill_of_lading_info"));
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
				+ (getPara("sp_type_traders")==null?"":getPara("sp_type_traders")+";")
				+ (getPara("sp_type_port_supervision")==null?"":getPara("sp_type_port_supervision")+";")
				+ (getPara("sp_type_wharf")==null?"":getPara("sp_type_wharf")+";");
        
        contact.set("sp_type", sp_type);
        contact.set("mobile", getPara("mobile"));
        contact.set("phone", getPara("phone"));
        contact.set("address", getPara("address"));
        contact.set("address_eng", getPara("address_eng")==""?null:getPara("address_eng"));
        contact.set("introduction", getPara("introduction"));
        contact.set("city", getPara("city"));
        contact.set("postal_code", getPara("postal_code"));
        
        //新增字段
        contact.set("register_capital", getPara("register_capital"));
        contact.set("main_business", getPara("main_business"));
        contact.set("website", getPara("website"));
        contact.set("account_information", getPara("account_information"));
        if(StringUtils.isNotEmpty(getPara("establish_time"))){
        	contact.set("establish_time", getPara("establish_time"));
        }
        
        contact.set("enterprise_nature", getPara("enterprise_nature"));
        contact.set("taxpayer_category", getPara("taxpayer_category"));
        contact.set("sys_and_qua_certification", getPara("sys_and_qua_certification"));
        contact.set("this_year_salesamount", getPara("this_year_salesamount"));
        contact.set("last_year_salesamount", getPara("last_year_salesamount"));
        contact.set("beforelast_year_salesamount", getPara("beforelast_year_salesamount"));
        contact.set("sales_contact_information", getPara("sales_contact_information"));
        contact.set("sales_manager_information", getPara("sales_manager_information"));
        contact.set("operation_contact_information", getPara("operation_contact_information"));
        contact.set("customer_contact_information", getPara("customer_contact_information"));
        contact.set("financial_contact_information", getPara("financial_contact_information"));
        contact.set("response_problem_time", getPara("response_problem_time"));
        contact.set("solve_problem_time", getPara("solve_problem_time"));
        contact.set("pay_account_time", getPara("pay_account_time"));
        contact.set("business_scope", getPara("business_scope"));
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
            spList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId);
            if(spList.size()==0){
                sql = "select p.* from party p where office_id="+office_id;
                spList = Db.find(sql+" order by abbr limit 25");
            }
            renderJson(spList);
        }else{
            String sql = "select p.* from party p where office_id="+office_id;
                        
            if (input.trim().length() > 0) {
                sql +=" and (p.abbr like '%" + input + "%' or p.quick_search_code like '%" +input.toLowerCase()+ "%'"
                	+ " or p.quick_search_code like '%" + input.toUpperCase() + "%') ";
            }
            spList = Db.find(sql+" order by abbr limit 25");

            renderJson(spList);
        }
    }



    //只查询供应商
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search_sp() {
    	
    	String input = getPara("input");
    	
    	Long parentID = pom.getParentOfficeId();
    	List<Record> spList = Collections.EMPTY_LIST;
    	if (input !=null && input.trim().length() > 0) {
    		spList = Db
    				.find(" select p.* from party p, office o where o.id = p.office_id and p.type='SP' and"
    						+ " (p.company_name like '%"
    						+ input
    						+ "%' or p.abbr like '%"
    						+ input
    						+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,25",parentID,parentID);
    	} else {
    		spList = Db
    				.find("select p.* from party p, office o where o.id = p.office_id and p.type='SP' and "
    						+ " (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)", parentID, parentID);
    	}
    	renderJson(spList);
    }
    
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchInsurance() {
		String input = getPara("input");
		Long parentID = pom.getParentOfficeId();
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find(" select p.*,c.*,p.id as pid, p.payment from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " p.party_type = '"
							+ Party.PARTY_TYPE_INSURANCE_PARTY
							+ "' and (c.company_name like '%"
							+ input
							+ "%' or c.abbr like '%"
							+ input
							+ "%' or c.contact_person like '%"
							+ input
							+ "%' or c.email like '%"
							+ input
							+ "%' or c.mobile like '%"
							+ input
							+ "%' or c.phone like '%"
							+ input
							+ "%' or c.address like '%"
							+ input
							+ "%' or c.postal_code like '%"
							+ input
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,25",parentID,parentID);
		} else {
			locationList = Db
					.find("select p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " p.party_type = '"
							+ Party.PARTY_TYPE_INSURANCE_PARTY + "'  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)",parentID,parentID);
		}
		renderJson(locationList);
	}
//    public void chargeTypeList(){
//    	String id = getPara("typeId");
//    	
//    	String sLimit = "";
//        String pageIndex = getPara("sEcho");
//        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
//            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
//        }
//        Map chargeTypeMap = new HashMap();
//    	if(id == null || "".equals(id)){
//    		chargeTypeMap.put("sEcho", 0);
//    		chargeTypeMap.put("iTotalRecords", 0);
//    		chargeTypeMap.put("iTotalDisplayRecords", 0);
//    		chargeTypeMap.put("aaData", null);
//    	}else{
//    		String totalSql = "select count(*) as total from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = " + id;
//    		Record rec = Db.findFirst(totalSql);
//    		
//    		List<ProviderChargeType> list = ProviderChargeType.dao.find("select ct.id,ct.charge_type,ifnull(c.abbr,c.company_name) as customer_name,ct.remark from charge_type ct left join party p on p.id = ct.customer_id left join contact c on c.id = p.contact_id where ct.sp_id = ? " + sLimit,id);
//    		chargeTypeMap.put("sEcho", pageIndex);
//    		chargeTypeMap.put("iTotalRecords", rec.get("total"));
//    		chargeTypeMap.put("iTotalDisplayRecords", rec.get("total"));
//    		chargeTypeMap.put("aaData", list);
//    	}
//    	renderJson(chargeTypeMap);
//    	
//    }
    /*public void saveChargeType(){
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
    */
    //查询船公司下拉, 这里不用过滤office, 因为船公司是通用的
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCarrier(){
        String name = getPara("input");
        
        String condition = "";
        if(StringUtils.isNotBlank(name)){
        	condition = " and p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ";
        }
        
        List<Record> recs = null;
        String sql = "select  * from(SELECT p.id,p.abbr name"
				+ " FROM user_query_history uqh"
				+ " LEFT JOIN party p ON p.id = uqh.ref_id"
				+ " WHERE uqh.type = 'CARRIER'"
				+ condition
				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
				+ " ORDER BY uqh.query_stamp desc ) A"
				+ " UNION "
				+ " (select id,abbr name from party p where p.type = 'SP' and p.sp_type like '%carrier%' "
				+ condition+")";
       
        recs = Db.find(sql);
        renderJson(recs);
    }
    
    //查询结算公司下拉,包括供应商和客户
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCompany(){
    	String input = getPara("input");
    	String type = getPara("type");
    	String sp_type = getPara("sp_type");
    	String sql_type = "and p.type='"+type+"'";
    	String sql_sp_type = "and p.sp_type like '%"+sp_type+"%'";
    	if(StringUtils.isEmpty(type)){
    		sql_type="";
    	}
    	if(StringUtils.isEmpty(sp_type)){
    		sql_sp_type="";
    	}
    	long userId = LoginUserController.getLoginUserId(this);	
		Long parentID = pom.getParentOfficeId();
		
		List<Record> spList = Collections.EMPTY_LIST;
		if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr name,p.ref_office_id from user_query_history h, party p "
                    + " where h.ref_id=p.id and h.type='ARAP_COM' and h.user_id=?";
            spList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId);
            if(spList.size()==0){
            	String sql2 = "select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "+sql_type+sql_sp_type
                        + " and (p.abbr like '%"
                        + input
                        + "%' or p.code like '%"
                        + input
                        + "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) ";
            	
                spList = Db.find(sql2+" order by convert(p.abbr using gb2312) asc limit 25", parentID, parentID);
            }
            renderJson(spList);
        }else{
            if (input !=null && input.trim().length() > 0) {
            	String sql = " select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "+sql_type+sql_sp_type
                                + " and (p.abbr like '%"+input+"%' or p.code like '%"+ input
                                + "%' or p.quick_search_code like '%"+input+"%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) ";
                spList = Db.find(sql+ " order by convert(p.abbr using gb2312) asc limit 25",parentID,parentID);
                
                if(spList.size()==0){
                	String err = "无记录";
                	renderText(err);
                	return;                
                }
            } else {
                spList = Db.find("select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "+sql_type+sql_sp_type
                                + " and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?) "
                                + " order by convert(p.abbr using gb2312) asc limit 25", parentID, parentID);
            }
            renderJson(spList);
        }
    }
    
    //查询航空公司下拉
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchAirCompany(){
    	String input = getPara("input");
    	String sp_type = getPara("para");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	String conditions = "";
    	if(StringUtils.isNotBlank(input)){
    		conditions = " and (p.abbr like '%" + input + "%' or p.company_name like '%" + input + "%')";
    	}
    			
    	String sql = "select * from (SELECT p.id, p.abbr name "
    				+ " FROM user_query_history uqh"
    				+ " LEFT JOIN party p ON p.id = uqh.ref_id"
    				+ " and uqh.type = UPPER('"+sp_type+"')"
    				+ " WHERE uqh.type = UPPER('"+sp_type+"')"
    				+ conditions
    				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    				+ " ORDER BY uqh.query_stamp desc limit 0,25) A"
    				+ " UNION "
    				+ " (select p.id,p.abbr name from party p"
    				+ " where office_id="+office_id+" and p.type = 'SP'"
    				+ " and p.sp_type like '%"+sp_type+"%'"
    				+ conditions+") limit 0,25";
    	
    	List<Record> recs = Db.find(sql);
    	renderJson(recs);
    	
    }
    
    //查询运输公司下拉
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckCompany(){
    	String input = getPara("input");
    	String sp_type = getPara("para");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	String conditions = "";
    	if(StringUtils.isNotBlank(input)){
    		conditions = " and (p.abbr like '%" + input + "%' or p.company_name like '%" + input + "%')";
    	}
    			
    	String sql = "select * from (SELECT p.id, p.abbr name "
    				+ " FROM user_query_history uqh"
    				+ " LEFT JOIN party p ON p.id = uqh.ref_id"
    				+ " and uqh.type = UPPER('"+sp_type+"')"
    				+ " WHERE uqh.type = UPPER('"+sp_type+"')"
    				+ conditions
    				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    				+ " ORDER BY uqh.query_stamp desc limit 0,25) A"
    				+ " UNION "
    				+ " (select p.id,p.abbr name from party p"
    				+ " where office_id="+office_id+" and p.type = 'SP'"
    				+ " and p.sp_type like '%"+sp_type+"%'"
    				+ conditions+") limit 0,25";
    	
    	List<Record> recs = Db.find(sql);
    	renderJson(recs);
    	
    }
    
    // 获取指定运输公司
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckCompany_id() {
        String TRANSPORT_COMPANY_id = getPara("TRANSPORT_COMPANY_id");
       
        if(StringUtils.isEmpty(TRANSPORT_COMPANY_id)){
        	TRANSPORT_COMPANY_id = "";
        }
        Record resultFirst = new Record();
        String sql = "select p.id, p.abbr, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code from party p where  "
                + " p.type = 'SP' ";
                        
            if (TRANSPORT_COMPANY_id.trim().length() > 0) {
                sql +=" and p.id =" + TRANSPORT_COMPANY_id ;
            }
            resultFirst = Db.findFirst(sql);

            renderJson(resultFirst);
        
    }
    
    
    
    
    
    //查询单位下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchUnit(){
    	String input = getPara("input");
    	
    	String conditions = "";
    	if(StringUtils.isNotBlank(input)){
    		conditions = " and (u.name like '%" + input + "%' "+"or u.name_eng like '%"+input+"%')";
    	}
    			
    	String sql = "select  * from(SELECT u.id, CONCAT(u. NAME, u.name_eng) NAME, u.name_eng"
    				+ " FROM user_query_history uqh"
    				+ " LEFT JOIN unit u ON u.id = uqh.ref_id"
    				+ " and uqh.type = 'UNIT'"
    				+ " WHERE uqh.type = 'UNIT'"
    				+ conditions
    				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    				+ " ORDER BY uqh.query_stamp desc limit 0,25) A"
    				+ " UNION "
    				+ " (SELECT id, CONCAT(NAME, name_eng) NAME, name_eng"
    				+ " FROM unit u WHERE u.type = 'order'"
    				+ conditions+") limit 0,25";
    	
    	List<Record> recs = Db.find(sql);
    	renderJson(recs);
    }
    
    //查询工作单应收应付的单位下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchChargeUnit(){
    	String input = getPara("input");
    	
    	String conditions = "";
    	if(StringUtils.isNotBlank(input)){
    		conditions = " and (u.name like '%" + input + "%' "+"or u.name_eng like '%"+input+"%')";
    	}
    			
    	String sql = "select  * from(SELECT u.id, u. NAME , u.name_eng"
    				+ " FROM user_query_history uqh"
    				+ " LEFT JOIN unit u ON u.id = uqh.ref_id"
    				+ " WHERE uqh.type = 'CHARGE_UNIT'"
    				+ conditions
    				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    				+ " ORDER BY uqh.query_stamp desc limit 0,25) A"
    				+ " UNION "
    				+ " (SELECT id, NAME , name_eng"
    				+ " FROM unit u WHERE u.type = 'charge'"
    				+ conditions+") limit 0,25";
    	
    	List<Record> recs = Db.find(sql);
    	renderJson(recs);
    }
    
    //查询币制名下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCurrency(){
    	String input = getPara("input");
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	String d = sf.format(new Date());
    	List<Record> recs = null;
    	UserLogin user = LoginUserController.getLoginUser(this);
        String condition = "";
    	long office_id = user.getLong("office_id");
    	/*String sql = "select cr.office_id,c.id,c.code,c.name,cast( if(cr.from_stamp<'"+d+"' and cr.to_stamp>'"+d+"',cr.rate,'') as char ) rate from currency c"
    			+ " left join currency_rate cr on cr.currency_code = c.code  where 1=1 and cr.office_id= "+office_id+" group by name";*/
		
       if(!StringUtils.isBlank(input)){
    	   	condition+=" and (c.name like '%" + input + "%' or c.english_name like '%" + input + "%' or c.code like '%" + input + "%') ";
       }
        String sql = "SELECT"
    			+ "	to_stamp,"
    			+ "	from_stamp,"
    			+ "	cr.remark,"
    			+ "	cr.id,"
    			+ "	currency_code CODE,"
    			+ "	cr.NAME ,"
    			+ "	rate"
    			+ " FROM"
    			+ "	currency_rate c"
    			+ " left join currency cr on cr.code = c.currency_code"
    			+ " WHERE"
    			+ " 	1 = 1"
    			+ " AND NAME IS NOT NULL and c.is_stop = 'Y' and c.office_id = "+office_id+" and c.to_stamp in (select max(to_stamp) to_stamp "
    			+ " from currency_rate  where office_id="+office_id+" GROUP BY currency_code)"
    			+ "";

    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCurrency_create(){
    	String input = getPara("input");
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	String d = sf.format(new Date());
    	List<Record> recs = null;
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
    	/*String sql = "select cr.office_id,c.id,c.code,c.name,cast( if(cr.from_stamp<'"+d+"' and cr.to_stamp>'"+d+"',cr.rate,'') as char ) rate from currency c"
    			+ " left join currency_rate cr on cr.currency_code = c.code  where 1=1 and cr.office_id= "+office_id+" group by name";*/
    	String sql = "SELECT A.to_stamp,A.from_stamp,A.remark,c.id,c. CODE,c. NAME,A.rate"
    			+ " FROM "
    			+ " currency c"
    			+ " left join (select * from currency_rate crr where to_stamp in ( select max(to_stamp) from currency_rate where office_id = "+office_id+" GROUP BY currency_id)  and crr.is_stop = 'Y' ) A  on A.currency_id = c.id "
    			+ "where 1 = 1 and name is not null ";
        if(!StringUtils.isBlank(input)){
    		sql+=" and (c.name like '%" + input + "%' or c.english_name like '%" + input + "%' or c.code like '%" + input + "%') ";
    	}
    	recs = Db.find(sql+" group by name");
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
    	
    	String sql = "select * from fin_account fa where fa.bank_name!='现金' and (fa.is_stop is null or fa.is_stop !=1) and office_id="+office_id;
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (fa.bank_name like '%" + input + "%') ";
    	}
    	recs = Db.find(sql);
    	renderJson(recs);
    }
    
  //查询银行账户名下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchPartyAccount(){
    	UserLogin user = LoginUserController.getLoginUser(this);
//        long office_id = user.getLong("office_id");
    	String input = getPara("input");
    	String order_id = getPara("order_id");
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//    	String d = sf.format(new Date());
    	List<Record> recs = null;
    	
    	String sql = "select * from fin_account fa where order_id="+order_id;
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
        String addressInputStr = getPara("addressInputStr");
        String addStr = "";
        String conditon = "";
        if(StringUtils.isNotEmpty(addressInputStr)){
        	addStr=" and dock_name like '%"+addressInputStr+"%' ";
        }
        
        if(addressInputStr!=null){
        	conditon = "  and ( p.abbr ='" + name + "' or p.company_name = '" + name + "' ) ";
        }else{
        	if(!StringUtils.isBlank(name)){
        		conditon = "  and ( p.abbr like '%" + name + "%' or p.company_name like '%" + name + "%' ) ";
            }
        }
        
        List<Record> rec = null;
        String sql = "select  * from(SELECT p.id, p.abbr NAME,p.phone, "
        		+" 	p.address, "
        		+" 	p.contact_person, "
        		+" 	CONCAT( "
        		+" 		CONCAT(IFNULL(p.address, ''),':',IFNULL(p.contact_person, ''),':',IFNULL(p.phone, '')), "
        		+" 		',',IFNULL((SELECT GROUP_CONCAT(CONCAT(ifnull(di.dock_name,''),':',ifnull(di.land_contacts,''),':',ifnull(di.land_contact_phone,'')))"
        		+ " from dockinfo di WHERE di.party_id=p.id "+addStr+"),'') "
        		+" 	) dock_names "
    			+ " FROM user_query_history uqh"
    			+ " LEFT JOIN party p ON p.id = uqh.ref_id"
    			+ " WHERE uqh.type = 'TRUCKOUT'"
    			+ conditon
    			+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    			+ " ORDER BY uqh.query_stamp desc ) A"
    			
    			+ " UNION"
    			
    			+ " (SELECT "
        		+" 	p.id,p.abbr NAME,p.phone, "
        		+" 	p.address, "
        		+" 	p.contact_person, "
        		+" 	CONCAT( "
        		+" 		CONCAT(IFNULL(p.address, ''),':',IFNULL(p.contact_person, ''),':',IFNULL(p.phone, '')), "
        		+" 		',',IFNULL((SELECT GROUP_CONCAT(CONCAT(ifnull(di.dock_name,''),':',ifnull(di.land_contacts,''),':',ifnull(di.land_contact_phone,'')))"
        		+ " from dockinfo di WHERE di.party_id=p.id "+addStr+"),'') "
        		+" 	) dock_names "
        		+" FROM party p "
        		+" WHERE "
        		+" p.office_id ="+office_id
        		+ conditon+")";
        
        
        rec = Db.find(sql);
        renderJson(rec);
    }
    
    //查询仓库地址
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void landAddress(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String name = getPara("input");
        String addressInputStr = getPara("addressInputStr");
        String addStr = "";
        String conditon = "";
        if(StringUtils.isNotEmpty(addressInputStr)){
        	addStr=" and dock_name like '%"+addressInputStr+"%' ";
        }
        
        List<Record> rec = null;
        String sql = " SELECT dock.* from dockinfo dock LEFT JOIN party p on dock.party_id = p.id"
        		+ " WHERE p.office_id = "+office_id+" and p.ref_office_id = "+office_id+ addStr;
        
        
        rec = Db.find(sql);
        renderJson(rec);
    }
    
    
    
    
    
    //查询收货人下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchTruckIn(){
    	String input = getPara("input");
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	String conditions = "";
    	if(StringUtils.isNotBlank(input)){
    		conditions = " and (p.abbr like '%" + input + "%' or p.company_name like '%" + input + "%')";
    	}
    			
    	String sql = "select * from (SELECT p.id, p.abbr name, p.phone, p.address,p.contact_person  "
    				+ " FROM user_query_history uqh"
    				+ " LEFT JOIN party p ON p.id = uqh.ref_id"
    				+ " WHERE uqh.type = UPPER('TruckIn')"
    				+ conditions
    				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
    				+ " ORDER BY uqh.query_stamp desc ) A"
    				+ " UNION "
    				+ " (select p.id,p.abbr name, p.phone, p.address,p.contact_person from party p"
    				+ " where office_id="+office_id
    				+ conditions+")";
    	
    	List<Record> recs = Db.find(sql);
    	renderJson(recs);
    	
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
        rec = Db.find(sql + " limit 25");
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
    	rec = Db.find(sql + " limit 25");
    	renderJson(rec);
    }
    
    //查询报关境内货源地下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchGoodsSupply(){
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(code,' ',name) name from custom_goods_supply where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (code like '%" + input + "%' "
    				+ " or name like '%" + input + "%'"
    				+ " or concat(code,' ',name) like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 25");
    	renderJson(rec);
    }
    
    //查询报关境内货源地下拉列表
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchExportPort(){
    	String input = getPara("input");
    	List<Record> rec = null;
    	String sql = "select id,concat(under_code,' ',under_port) name from custom_port where 1=1 ";
    	if(!StringUtils.isBlank(input)){
    		sql+=" and (code like '%" + input + "%' or under_port like '%" + input + "%' "
    				+ " or under_code like '%" + input + "%'"
    			    + " or concat(under_code,' ',under_port) like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 25");
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
    		sql+=" and (custom_registration like '%" + input + "%' "
    			+ " or abbr like '%" + input + "%' or company_name like '%" + input + "%'"
    			+ " or concat(abbr,' ',ifnull(custom_registration,'')) like '%" + input + "%') ";
    	}
    	rec = Db.find(sql + " limit 25");
    	renderJson(rec);
    }
    
    //打分
    public void markCustormerList(){
    	String sp_id=getPara("sp_id");
    	String office_id= pom.getCurrentOfficeId().toString();
        String sql="select pm.*,p.abbr,ul.c_name creator_name FROM party_mark pm "
					+" LEFT JOIN party p on p.id=pm.sp_id "
					+" LEFT JOIN user_login ul on ul.id=pm.creator "
					+" WHERE pm.sp_id = " +sp_id;
        
        List<Record> orderList = Db.find(sql);
        Map map = new HashMap();
        map.put("data", orderList);
        renderJson(map);
    }
    //保存打分
    public void markCustormerSave() throws InstantiationException, IllegalAccessException{
    	String jsonStr=getPara("params");
    	
    	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class); 
        
       	String  sp_id=(String) dto.get("sp_id");
       	UserLogin user = LoginUserController.getLoginUser(this);
       	long userId = LoginUserController.getLoginUserId(this);
		long office_id = user.getLong("office_id");
       	 
        
       	List<Map<String, String>> item_list = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(item_list, sp_id, PartyMark.class, "sp_id");
		
		renderJson("saveStatu","OK");
    }
    //删除某项打分
    public void markCustormerDelete(){
    	String itemid=getPara("id");
    	Db.deleteById("party_mark", itemid);
    	renderJson("deleteStatu","OK");
    }
    
    @Before(Tx.class)
    public void approvalOrder(){
    	String action = getPara("action");
    	String id = getPara("id");
    	
    	Party party = Party.dao.findById(id);
    	if("submit".equals(action)){
    		party.set("status", "待审核");
    	}else if("approval".equals(action)){
    		party.set("status", "审核通过");
    	}else if("disapproval".equals(action)){
    		party.set("status", "审核不通过");
    	}else if("verification".equals(action)){
    		party.set("status", "审批通过");
    	}else if("disVerification".equals(action)){
    		party.set("status", "审批不通过");
    	}
    	party.update();
    	renderJson(party);
    }
}
