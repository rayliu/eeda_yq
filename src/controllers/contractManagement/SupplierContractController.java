package controllers.contractManagement;

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
import models.SpAirTransport;
import models.SpAirTransportItem;
import models.SpBulkCargo;
import models.SpBulkCargoItem;
import models.SpCargoInsurance;
import models.SpCustom;
import models.SpInternalTrade;
import models.SpLandTransport;
import models.SpLandTransportItem;
import models.SpOceanCargo;
import models.SpOceanCargoItem;
import models.SpPickingCrane;
import models.SpStorage;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.yh.profile.ProviderChargeType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.LogInterceptor;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class SupplierContractController extends Controller {

    private Logger logger = Logger.getLogger(SupplierContractController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/eeda/contractManagement/supplierContractList.html");
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
        setAttr("user", LoginUserController.getLoginUser(this));
            render("/eeda/contractManagement/supplierContractEdit.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        Party party = Party.dao.findById(id);
        
        String code = party.get("location");

        Record re = Db.findFirst("select get_loc_full_name('"+code+"') as loc_name");
        setAttr("location", re.getStr("loc_name"));

        setAttr("party", party);
        
        Record order = new Record();
        order.set("oceanCargoList", getItems("oceanCargo",id));
        order.set("oceanCargoItemList", getItems("oceanCargoItem",id));
        order.set("internalTradeList", getItems("internalTrade",id));
        order.set("bulkCargoItemList", getItems("bulkCargoItem",id));
        order.set("pickingCraneList", getItems("pickingCrane",id));
        order.set("landTransportList", getItems("landTransport",id));
        order.set("landTransportItemList", getItems("landTransportItem",id));
        order.set("storageList", getItems("storage",id));
        order.set("airTransportList", getItems("airTransport",id));
        order.set("airTransportItemList", getItems("airTransportItem",id));
        order.set("customList", getItems("custom",id));
        order.set("bulkCargoList", getItems("bulkCargo",id));
        order.set("cargoInsuranceList", getItems("cargoInsurance",id));
        
        setAttr("order", order);
        setAttr("user", LoginUserController.getLoginUser(this));
     
        render("/eeda/contractManagement/supplierContractEdit.html");
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
    public void save() throws InstantiationException, IllegalAccessException {
        String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        Party party = new Party();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
       
        if (StringUtils.isNotEmpty(id)) {
            party = Party.dao.findById(id);
            
            DbUtils.setModelValues(dto, party);
            party.set("last_updated_stamp", new Date());
           
            //setContact(party);
            party.update();
        } else {
            //判断供应商简称
        	party = Party.dao.findFirst("select * from party where abbr=? and office_id=?", getPara("abbr"), pom.getCurrentOfficeId());
            if(party!=null){
            	renderText("abbrError");
            	return ;
            }
            //判断供应商全称
            party = Party.dao.findFirst("select * from party where company_name=? and office_id=?", getPara("company_name"), pom.getCurrentOfficeId()); 
            if(party!=null){
            	renderText("companyError");
            	return ;
            }
            
            //create 
   			DbUtils.setModelValues(dto, party);

            party.set("type", Party.PARTY_TYPE_SERVICE_PROVIDER);
            party.set("creator", LoginUserController.getLoginUserId(this));
            party.set("create_date", new Date());
            party.set("office_id", pom.getCurrentOfficeId());
            party.save();
        }
        
        List<Map<String, String>> oceanCargoList = (ArrayList<Map<String, String>>)dto.get("oceanCargo");
		DbUtils.handleList(oceanCargoList, id, SpOceanCargo.class, "order_id");
		List<Map<String, String>> oceanCargoItemList = (ArrayList<Map<String, String>>)dto.get("oceanCargoItem");
		DbUtils.handleList(oceanCargoItemList, id, SpOceanCargoItem.class, "order_id");
		List<Map<String, String>> internalTradeList = (ArrayList<Map<String, String>>)dto.get("internalTrade");
		DbUtils.handleList(internalTradeList, id, SpInternalTrade.class, "order_id");
		List<Map<String, String>> bulkCargoList = (ArrayList<Map<String, String>>)dto.get("bulkCargo");
		DbUtils.handleList(bulkCargoList, id, SpBulkCargo.class, "order_id");
		List<Map<String, String>> bulkCargoItemList = (ArrayList<Map<String, String>>)dto.get("bulkCargoItem");
		DbUtils.handleList(bulkCargoItemList, id, SpBulkCargoItem.class, "order_id");
		List<Map<String, String>> landTransportList = (ArrayList<Map<String, String>>)dto.get("landTransport");
		DbUtils.handleList(landTransportList, id, SpLandTransport.class, "order_id");
		List<Map<String, String>> landTransportItemList = (ArrayList<Map<String, String>>)dto.get("landTransportItem");
		DbUtils.handleList(landTransportItemList, id, SpLandTransportItem.class, "order_id");
		List<Map<String, String>> storageList = (ArrayList<Map<String, String>>)dto.get("storage");
		DbUtils.handleList(storageList, id, SpStorage.class, "order_id");
		List<Map<String, String>> airTransportList = (ArrayList<Map<String, String>>)dto.get("airTransport");
		DbUtils.handleList(airTransportList, id, SpAirTransport.class, "order_id");
		List<Map<String, String>> airTransportItemList = (ArrayList<Map<String, String>>)dto.get("airTransportItem");
		DbUtils.handleList(airTransportItemList, id, SpAirTransportItem.class, "order_id");
		List<Map<String, String>> customList = (ArrayList<Map<String, String>>)dto.get("custom");
		DbUtils.handleList(customList, id, SpCustom.class, "order_id");
		List<Map<String, String>> pickingCraneList = (ArrayList<Map<String, String>>)dto.get("pickingCrane");
		DbUtils.handleList(pickingCraneList, id, SpPickingCrane.class, "order_id");
		List<Map<String, String>> cargoInsuranceList = (ArrayList<Map<String, String>>)dto.get("cargoInsurance");
		DbUtils.handleList(cargoInsuranceList, id, SpCargoInsurance.class, "order_id");
		
        
     
        setAttr("saveOK", true);
        //redirect("/serviceProvider");
        renderJson(party);
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
    						+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,10",parentID,parentID);
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
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,10",parentID,parentID);
		} else {
			locationList = Db
					.find("select p.*,c.*,p.id as pid from party p,contact c,office o where o.id = p.office_id and p.contact_id = c.id and"
					        + " p.party_type = '"
							+ Party.PARTY_TYPE_INSURANCE_PARTY + "'  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)",parentID,parentID);
		}
		renderJson(locationList);
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
    
    //查询结算公司下拉,包括供应商和客户
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchCompany(){
    	String input = getPara("input");
    	long userId = LoginUserController.getLoginUserId(this);	
		Long parentID = pom.getParentOfficeId();
		
		List<Record> spList = Collections.EMPTY_LIST;
		if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr name from user_query_history h, party p "
                    + "where h.ref_id=p.id and h.type='ARAP_COM' and h.user_id=?";
            spList = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);
            if(spList.size()==0){
                spList = Db.find(" select p.id,p.abbr name from party p, office o where o.id = p.office_id "
                        + " and (p.company_name like '%"
                        + input
                        + "%' or p.abbr like '%"
                        + input
                        + "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) "
                        + " order by convert(p.abbr using gb2312) asc limit 10", parentID, parentID);
            }
            renderJson(spList);
        }else{
            if (input !=null && input.trim().length() > 0) {
                spList = Db
                        .find(" select p.id,p.abbr name from party p, office o where o.id = p.office_id "
                                + " and (p.company_name like '%"
                                + input
                                + "%' or p.abbr like '%"
                                + input
                                + "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) "
                                + " order by convert(p.abbr using gb2312) asc limit 10",parentID,parentID);
            } else {
                spList = Db
                        .find("select p.id,p.abbr name from party p, office o where o.id = p.office_id "
                                + " and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?) "
                                + " order by convert(p.abbr using gb2312) asc limit 10", parentID, parentID);
            }
            renderJson(spList);
        }
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
    
    public List<Record> getItems(String type,String order_id){
    	String sql = null;
    	if(type.equals("oceanCargo")){
    		sql = "select soc.*,ul.user_name creator_name from sp_ocean_cargo soc"
    				+ " left join user_login ul on ul.id = soc.creator where soc.order_id = ?";
    	}else if(type.equals("oceanCargoItem")){
    	    		sql = " select soc.*,c1.name doc_crc_name,c2.name tlx_crc_name,c3.name eir_crc_name,c4.name hss_crc_name "
    	    				+" ,c5.name o_f_20gp_crc_name,c6.name o_f_40gp_crc_name,c7.name o_f_40hq_crc_name,c8.name "  
    	    				+" thc_20gp_crc_name,c9.name thc_40gp_crc_name "
    	    				+" ,c10.name thc_40hq_crc_name,c11.name ebs_20gp_crc_name,c12.name ebs_40gp_crc_name,c13.name " 
    	    				+" ebs_40hq_crc_name,c14.name ams_afr_20gp_crc_name "
    	    				+" ,c15.name ams_afr_40gp_crc_name,c16.name ams_afr_40hq_crc_name,c17.name vat_20gp_crc_name,c18.name "
    	    				+" vat_40gp_crc_name,c19.name vat_40hq_crc_name "
    	    				+" ,c20.name isps_20gp_crc_name,c21.name isps_40gp_crc_name,c22.name isps_40hq_crc_name,c23.name  "
    	    				+" truck_20gp_crc_name,c24.name truck_40gp_crc_name "
    	    				+" ,c25.name truck_40hq_crc_name,c26.name total_20gp_crc_name,c27.name total_40gp_crc_name,c28.name "
    	    				+" total_40hq_crc_name  ,ul.c_name creator_name from sp_ocean_cargo_item soc "
    	    				+"     				  left join user_login ul on ul.id = soc.creator  "
    	    				+" left join currency c1 on c1.id=soc.doc_crc "
    	    				+" left join currency c2 on c2.id=soc.tlx_crc "
    	    				+" left join currency c3 on c3.id=soc.eir_crc "
    	    				+" left join currency c4 on c4.id=soc.hss_crc "
    	    				+" left join currency c5 on c5.id=soc.o_f_20gp_crc "
    	    				+" left join currency c6 on c6.id=soc.o_f_40gp_crc "
    	    				+" left join currency c7 on c7.id=soc.o_f_40hq_crc "
    	    				+" left join currency c8 on c8.id=soc.thc_20gp_crc "
    	    				+" left join currency c9 on c9.id=soc.thc_40gp_crc "
    	    				+" left join currency c10 on c10.id=soc.thc_40hq_crc "
    	    				+" left join currency c11 on c11.id=soc.ebs_20gp_crc "
    	    				+" left join currency c12 on c12.id=soc.ebs_40gp_crc "
    	    				+" left join currency c13 on c13.id=soc.ebs_40hq_crc "
    	    				+" left join currency c14 on c14.id=soc.ams_afr_20gp_crc "
    	    				+" left join currency c15 on c15.id=soc.ams_afr_40gp_crc "
    	    				+" left join currency c16 on c16.id=soc.ams_afr_40hq_crc "
    	    				+" left join currency c17 on c17.id=soc.vat_20gp_crc "
    	    				+" left join currency c18 on c18.id=soc.vat_40gp_crc "
    	    				+" left join currency c19 on c19.id=soc.vat_40hq_crc "
    	    				+" left join currency c20 on c20.id=soc.isps_20gp_crc "
    	    				+" left join currency c21 on c21.id=soc.isps_40gp_crc "
    	    				+" left join currency c22 on c22.id=soc.isps_40hq_crc "
    	    				+" left join currency c23 on c23.id=soc.truck_20gp_crc "
    	    				+" left join currency c24 on c24.id=soc.truck_40gp_crc "
    	    				+" left join currency c25 on c25.id=soc.truck_40hq_crc "
    	    				+" left join currency c26 on c26.id=soc.total_20gp_crc "
    	    				+" left join currency c27 on c27.id=soc.total_40gp_crc "
    	    				+" left join currency c28 on c28.id=soc.total_40hq_crc "
    				+ " left join user_login ul on ul.id = soc.creator where soc.order_id = ?";
    	}else if(type.equals("internalTrade")){
    		sql = "select * from sp_internal_trade where order_id = ?";
    	}else if(type.equals("bulkCargo")){
    		sql = "select * from sp_bulk_cargo where order_id = ?";
    	}else if(type.equals("bulkCargoItem")){
    		sql = " select sbc.*,ul.c_name creator_name ,c1.name port_charge_crc_name,c2.name doc_crc_name,c3.name vgm_crc_name,c4.name  d_o_crc_name "
    				+" ,c5.name o_f_crc_name,c6.name handling_crc_name,c7.name cfs_crc_name,c8.name thc_crc_name,c9.name  "+" baf_crc_name "
    				+" ,c10.name cic_crc_name,c11.name loading_fee_crc_name,c12.name total_crc_name "
    				+"  from sp_bulk_cargo_item sbc "
    				+"     				  left join user_login ul on ul.id = sbc.creator  "
    				+" left join currency c1 on c1.id=sbc.port_charge_crc "
    				+" left join currency c2 on c2.id=sbc.doc_crc "
    				+" left join currency c3 on c3.id=sbc.vgm_crc "
    				+" left join currency c4 on c4.id=sbc.d_o_crc "
    				+" left join currency c5 on c5.id=sbc.o_f_crc "
    				+" left join currency c6 on c6.id=sbc.handling_crc "
    				+" left join currency c7 on c7.id=sbc.cfs_crc "
    				+" left join currency c8 on c8.id=sbc.thc_crc "
    				+" left join currency c9 on c9.id=sbc.baf_crc "
    				+" left join currency c10 on c10.id=sbc.cic_crc "
    				+" left join currency c11 on c11.id=sbc.loading_fee_crc "
    				+" left join currency c12 on c12.id=sbc.total_crc "
    				+"     				  where sbc.order_id = ? ";
    	}else if(type.equals("landTransport")){
    		sql = "select * from sp_land_transport where order_id = ?";
    	}else if(type.equals("landTransportItem")){
    		sql = " select	slt.*,ul.c_name creator_name , c1. name document_fee_crc_name, "
    				+" 	c2. name tally_fee_crc_name, "
    				+" 	c3. name cea_custom_fee_crc_name, "
    				+" 	c4. name dia_custom_fee_crc_name, "
    				+" 	c5. name tariff_fee_crc_name, "
    				+" 	c6. name exchange_fee_crc_name, "
    				+" 	c7. name storage_fee_crc_name, "
    				+" 	c8. name delivery_fee_crc_name, "
    				+" 	c9. name customs_inspection_fee_crc_name, "
    				+" 	c10. name price_crc_name, "
    				+" 	c11. name tax_price_crc_name, "
    				+" 	c12. name notax_price_crc_name "
    				+" from	sp_land_transport_item slt "
    				+" left join user_login ul on ul.id = slt.creator "
    				+" left join currency c1 on c1.id = slt.document_fee_crc "
    				+" left join currency c2 on c2.id = slt.tally_fee_crc "
    				+" left join currency c3 on c3.id = slt.cea_custom_fee_crc "
    				+" left join currency c4 on c4.id = slt.dia_custom_fee_crc "
    				+" left join currency c5 on c5.id = slt.tariff_fee_crc "
    				+" left join currency c6 on c6.id = slt.exchange_fee_crc "
    				+" left join currency c7 on c7.id = slt.storage_fee_crc "
    				+" left join currency c8 on c8.id = slt.delivery_fee_crc "
    				+" left join currency c9 on c9.id = slt.customs_inspection_fee_crc "
    				+" left join currency c10 on c10.id = slt.price_crc "
    				+" left join currency c11 on c11.id = slt.tax_price_crc "
    				+" left join currency c12 on c12.id = slt.notax_price_crc "
    				+" where	slt.order_id = ? ";
    	}else if(type.equals("storage")){
    		sql = "select * from sp_storage where order_id = ?";
    	}else if(type.equals("airTransport")){
    		sql = "select * from sp_air_transport where order_id = ?";
    	}else if(type.equals("airTransportItem")){
    		sql = " select	sati.*, ul.c_name creator_name, "
    				+" c1.name a45kg_crc_name,c2.name a100kg_crc_name,c3.name a300kg_crc_name,c4.name a500kg_crc_name "
    				+" from	sp_air_transport_item sati "
    				+" left join user_login ul on ul.id = sati.creator "
    				+" 	 left join currency c1 on c1.id = sati.45kg_crc  "
    				+" 	 left join currency c2 on c2.id = sati.100kg_crc  "
    				+" 	 left join currency c3 on c3.id = sati.300kg_crc  "
    				+" 	 left join currency c4 on c4.id = sati.500kg_crc  "
    				+" where	sati.order_id = ?  ";
    	}else if(type.equals("custom")){
    		sql = " SELECT	sc.*, ul.c_name creator_name, "
    				+" 	c1. NAME df_reimbursement_crc_name, "
    				+" 	c2. NAME iq_reimbursement_crc_name, "
    				+" 	c3. NAME customs_entry_fee_crc_name, "
    				+" 	c4. NAME inspection_entry_fee_crc_name, "
    				+" 	c5. NAME customs_agent_fee_crc_name, "
    				+" 	c6. NAME total_fee_crc_name "
    				+" FROM 	sp_custom sc "
    				+" LEFT JOIN user_login ul ON ul.id = sc.creator "
    				+" LEFT JOIN currency c1 ON c1.id = sc.df_reimbursement_crc "
    				+" LEFT JOIN currency c2 ON c2.id = sc.iq_reimbursement_crc "
    				+" LEFT JOIN currency c3 ON c3.id = sc.customs_entry_fee_crc "
    				+" LEFT JOIN currency c4 ON c4.id = sc.inspection_entry_fee_crc "
    				+" LEFT JOIN currency c5 ON c5.id = sc.customs_agent_fee_crc "
    				+" LEFT JOIN currency c6 ON c6.id = sc.total_fee_crc "
    				+" WHERE	sc.order_id = ? ";
    	}else if(type.equals("pickingCrane")){
    		sql = "select * from sp_picking_crane where order_id = ?";
    	}else if(type.equals("cargoInsurance")){
    		sql = "select * from sp_cargo_insurance where order_id = ?";
    	}
    	
    	
    	List<Record> re = Db.find(sql,order_id);
    	return re;
    }
    
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	List<Record> list = null;
    	list = getItems(type,order_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
}
