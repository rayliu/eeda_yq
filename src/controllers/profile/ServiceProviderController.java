package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.yh.profile.ProviderChargeType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ServiceProviderController extends Controller {

    private Logger logger = Logger.getLogger(ServiceProviderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
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
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        Party party = Party.dao.findById(id);
        
        String code = party.get("location");

        Record re = Db.findFirst("select get_loc_full_name('"+code+"') as loc_name");
        setAttr("location", re.getStr("loc_name"));

        setAttr("party", party);
     
        render("/eeda/profile/serviceProvider/serviceProviderEdit.html");
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
    public void save() {
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
            
            party.set("receiver", getPara("receiver"));
            party.set("bank_no", getPara("bank_no"));
            party.set("bank_name", getPara("bank_name"));
            setContact(party);
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
            party.set("receipt", getPara("receipt"));
            party.set("remark", getPara("remark"));
            party.set("payment", getPara("payment"));
            party.set("charge_type", getPara("chargeType"));
            party.set("office_id", pom.getCurrentOfficeId());
            
            setContact(party);
            party.save();

        }
     
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
        		+ (getPara("sp_type_truck")==null?"":getPara("sp_type_truck")+";");
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
    
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void searchSp() {
    	
		String input = getPara("input");
	
		Long parentID = pom.getParentOfficeId();
		List<Record> spList = Collections.EMPTY_LIST;
		if (input !=null && input.trim().length() > 0) {
		    spList = Db
					.find(" select p.*, p.id as pid, p.payment from party p, office o where o.id = p.office_id and"
					        + " (p.company_name like '%"
							+ input
							+ "%' or p.abbr like '%"
							+ input
							+ "%' or p.contact_person like '%"
							+ input
							+ "%' or p.email like '%"
							+ input
							+ "%' or p.mobile like '%"       
							+ input
							+ "%' or p.phone like '%"
							+ input
							+ "%' or p.address like '%"
							+ input
							+ "%' or p.postal_code like '%"
							+ input
							+ "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) limit 0,10",parentID,parentID);
		} else {
		    spList = Db
					.find("select p.*, p.id as pid from party p, office o where o.id = p.office_id and "
					        + " (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?)", parentID, parentID);
		}
		renderJson(spList);
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
}
