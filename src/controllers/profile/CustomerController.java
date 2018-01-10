 package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.ParentOfficeModel;
import models.Party;
import models.UserCustomer;
import models.UserLogin;
import models.UserRole;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.eeda.ListConfigController;
import controllers.importOrder.CheckOrder;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomerController extends Controller {

    private Log logger = Log.getLog(CustomerController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class) 
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/customer");
        setAttr("listConfigList", configList);
            render("/eeda/profile/customer/CustomerList.html");
    }
    
    public void list() {
    	String ADD = getPara();
    	setAttr("add",ADD);
        Long parentID = pom.getParentOfficeId();
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " select * from (select p.*,p.id as pid from party p"
                    + " left join office o on o.id = p.office_id"
                    + " where p.type='CUSTOMER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")"
                    + " ) A where 1=1";
      
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql+ condition + " order by create_date desc " +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    @Before(EedaMenuInterceptor.class)
    public void add() {
        setAttr("saveOK", false);
            render("/eeda/profile/customer/CustomerEdit.html");
    }
    
    
    //返回明细对象	
//    private List<Record> getItemDetail(String id,String type){
//     	String itemSql = "";
//    	List<Record> itemList = null;
//    	if("contacts".equals(type)){
//    		itemSql = "SELECT * FROM contacts_item WHERE party_id=?";
//    		itemList = Db.find(itemSql, id);
//    	}else if("account".equals(type)){
//    		itemList = Db.find("SELECT * FROM fin_account WHERE order_id = ?",id);
//		}
//		return itemList;
//    }
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        String type = getPara("type");
        setAttr("type",type);
        Party party =null;
        
        UserLogin user1 = LoginUserController.getLoginUser(this);
        if (user1==null) {
            return;
        }
        long office_id=user1.getLong("office_id");
        
        if("OWN".equals(type)){
        	party = Party.dao.findFirst("select p.*,p1.abbr charge_company_abbr from party p "
            		+ " LEFT JOIN party p1 on p1.id = p.charge_company_id"
            		+ " where p.office_id=? and p.type='OWN'",office_id);
        }else{
        	party = Party.dao.findFirst("select p.*,p1.abbr charge_company_abbr from party p "
            		+ " LEFT JOIN party p1 on p1.id = p.charge_company_id"
            		+ " where p.id=?",id);
        }
        
        id = party.getLong("id").toString();
        
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("party", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
        setAttr("party", party);
        String sql = "select jod.*,u.c_name from party_doc jod left join user_login u on jod.uploader=u.id "
    			+ " where party_id=? order by jod.id";
        setAttr("docList", Db.find(sql,id));
        setAttr("customerQuotationList", getItems(id, "customerQuotationItem"));
        setAttr("itemList", getItems(id, "dock"));
        setAttr("contacts_itemList", getItems(id,"contacts"));
        setAttr("account_itemList", getItems(id,"account"));
        setAttr("salesman_itemList", getItems(id,"salesman"));
        setAttr("mailSet",getDetail(id,"mailSet"));
        render("/eeda/profile/customer/CustomerEdit.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_DELETE})
    public void delete() {
        long id = getParaToLong();
        Party party = Party.dao.findById(id);
        Object obj = party.get("is_stop");
   
        if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
        	party.set("is_stop", true);
        }else{
        	party.set("is_stop", false);
        }
        party.update();
        redirect("/customer");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_C_CREATE, PermissionConstant.PERMSSION_C_UPDATE}, logical=Logical.OR)
    @Before(Tx.class)
    public void save() throws InstantiationException, IllegalAccessException {

    	String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        Long office_id = pom.getCurrentOfficeId();
        Party party = null;
        Long userId = LoginUserController.getLoginUserId(this);
        Date createDate = Calendar.getInstance().getTime();
        
        if (!"".equals(id) && id != null) {
        	
            party = Party.dao.findById(id);
            party.set("last_modified_by", userId);
            party.set("last_updated_stamp", createDate);
            DbUtils.setModelValues(dto, party);
            if(StringUtils.isBlank(party.getStr("code"))){
            	String code = OrderNoGenerator.getOrderNo("party",office_id);
            	if(StringUtils.isNotBlank(code)){
            		party.set("code", code.replace("P", "C"));
            	}
            }
            party.update();

        } else {
            
            party = new Party();
            party.set("office_id", pom.getCurrentOfficeId());
            party.set("type", Party.PARTY_TYPE_CUSTOMER);
            party.set("creator", userId);
            party.set("create_date", createDate);
            DbUtils.setModelValues(dto, party);
            String code = OrderNoGenerator.getOrderNo("party",office_id);
            if(StringUtils.isNotBlank(code)){
            	party.set("code", code.replace("P", "C"));
            }
            party.save();
            id = party.getLong("id").toString();
            
            Long parentID = pom.getParentOfficeId();
            //判断当前是否是系统管理员，是的话将当前的客户默认给
            List<UserRole> urList = UserRole.dao.find("select * from user_role ur left join user_login ul on ur.user_name = ul.user_name left join office o on o.id = ul.office_id  where role_code = 'admin' and (o.id = ? or o.belong_office = ?)",parentID,parentID);
            if(urList.size()>0){
            	for (UserRole userRole : urList) {
                	UserCustomer uc = new UserCustomer();
                	uc.set("user_name", userRole.get("user_name"));
                	uc.set("customer_id",party.get("id"));
                	uc.save();
    			}
            }
            
            //同步到用户
            List<UserLogin> ulList = UserLogin.dao.find("select * from user_login where (all_customer = 'Y' and office_id = ?) or id = ?",pom.getCurrentOfficeId(),LoginUserController.getLoginUserId(this));
            for(UserLogin ul :ulList){
            	Record re = Db.findFirst("select * from user_role ur left join user_login ul on ur.user_name = ul.user_name "
            			+ " left join office o on o.id = ul.office_id  "
            			+ " where role_code = 'admin' and (o.id = ? or o.belong_office = ?) and ur.user_name = ?",parentID,parentID,ul.getStr("user_name")) ;
            	if(re!=null)
            		continue;
            	String user_name = ul.getStr("user_name");
            	UserCustomer uc = new UserCustomer();
            	uc.set("user_name", user_name);
            	uc.set("customer_id", party.getLong("id"));
            	uc.save();
            }
            
        }
        
        //回填charge_company_id
        if(party.get("charge_company_id")==null){
        	party.set("charge_company_id", id);
        	party.update();
        }
        
        
        
        //文档上传保存
        List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("docItem");
		DbUtils.handleList(itemList, "party_doc", id, "party_id");
		//车队客户合同价
		List<Map<String, String>> customerQuotationItemList = (ArrayList<Map<String, String>>)dto.get("customer_quotationItem");
		DbUtils.handleList(customerQuotationItemList,  "party_quotation", id, "party_id");
		//保存账户信息
        List<Map<String, String>> acount = (ArrayList<Map<String, String>>)dto.get("acount_json");
		DbUtils.handleList(acount, id, FinAccount.class, "order_id");
		//保存联系人信息
		List<Map<String, String>> contacts = (ArrayList<Map<String, String>>)dto.get("contacts_json");
		DbUtils.handleList(contacts, "contacts_item", id, "party_id");
		//保存联系人信息
		List<Map<String, String>> salesman = (ArrayList<Map<String, String>>)dto.get("salesman_json");
		DbUtils.handleList(salesman, "customer_salesman", id, "party_id");
		//客户的工厂地点dock
		List<Map<String, String>> dock_Item = (ArrayList<Map<String, String>>)dto.get("dock_Item");
		DbUtils.handleList(dock_Item,  "dockinfo", id, "party_id");
		//保存邮箱设置
		List<Map<String, String>> mailSet = (ArrayList<Map<String, String>>)dto.get("mailSet_json");
		DbUtils.handleList(mailSet, "send_mail_set", id,"party_id");
		
		party = Party.dao.findFirst("select p.*,p1.abbr charge_company_abbr,sms.id mail_id from party p "
        		+ " LEFT JOIN party p1 on p1.id = p.charge_company_id"
        		+ " left join send_mail_set sms on sms.party_id = p.id"
        		+ " where p.id=?",id);
    	renderJson(party);
    }


    /*
     * public void location() { String id = getPara(); Contact contact =
     * Contact.dao.findById(id); String code = contact.get("location"); if (code
     * != null) { List<Record> transferOrders = Db .find(
     * "SELECT trim(concat(l2.name, ' ', l1.name,' ',l.name)) as dname,l.code FROM LOCATION l left join lOCATION  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code="
     * + code); renderJson(transferOrders); } else { renderJson(0); }
     * 
     * }
     */
    public void checkCustomerNameExist(){
 		String company_name= getPara("company_name");
 		boolean checkObjectExist;
 		Long parentID = pom.getParentOfficeId();
 		Party p = Party.dao.findFirst("select p.* from party p where p.company_name =? and p.type='CUSTOMER' and p.office_id = ?",company_name,parentID);
 		
 		if(p == null){
 			checkObjectExist=true;
 		}else{
 			checkObjectExist=false;
 		}
 		renderJson(checkObjectExist);
 	}
    public void checkCustomerAbbrExist(){
    	String abbr= getPara("abbr");
 		boolean checkObjectExist;
 		Long parentID = pom.getParentOfficeId();
 		Party p = Party.dao.findFirst("select p.* from party p where p.abbr =? and p.type='CUSTOMER' and p.office_id = ?",abbr,parentID);
 		if(p == null){
 			checkObjectExist=true;
 		}else{
 			checkObjectExist=false;
 		}
 		renderJson(checkObjectExist);
 	}
    public void searchPartCustomer() {
		String input = getPara("input");
		List<Record> locationList = Collections.EMPTY_LIST;
		if (input.trim().length() > 0) {
			locationList = Db
					.find("select *,p.id as pid,p.payment from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER
							+ "' and (company_name like '%"
							+ input
							+ "%' or contact_person like '%"
							+ input
							+ "%' or email like '%"
							+ input
							+ "%' or mobile like '%"
							+ input
							+ "%' or phone like '%"
							+ input
							+ "%' or address like '%"
							+ input
							+ "%' or postal_code like '%"
							+ input
							+ "%') and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') limit 0,25");
		} else {
			locationList = Db
					.find("select *,p.id as pid from party p,contact c where p.contact_id = c.id and p.party_type = '"
							+ Party.PARTY_TYPE_CUSTOMER + "' and (p.is_stop is null or p.is_stop = 0) and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"')");
		}
		renderJson(locationList);
	}
    
    
    public void getCustomerRoute(){
        String id = getPara("route_id");
        String sql = "select rp.*, get_loc_full_name(rp.location_from) location_from_name, get_loc_full_name(rp.location_to) location_to_name, sp.abbr sp_name from customer_route_provider rp"
                +" left join contact sp on rp.sp_id = sp.id"
                +" where rp.id = ?";
        Record route = Db.findFirst(sql, id);
        renderJson(route);
    }
    
    public void routeList(){
        String draw = getPara("draw");
        String start = getPara("start");
        String length = getPara("length");
        
        String customer_id = getPara("customer_id");
        
        String sql = "select rp.*, l1.name location_from_name, l2.name location_to_name, sp.abbr sp_name from customer_route_provider rp"
                +" left join location l1 on rp.location_from = l1.code"
                +" left join location l2 on rp.location_to = l2.code"
                +" left join contact sp on rp.sp_id = sp.id"
                +" where customer_id = ?";
        
        String totalSql = "select count(1) total from (" + sql + ") A";
        
        Record rec = Db.findFirst(totalSql, customer_id);
        String sLimit = " limit " + start + ", " +length; 
        
        List<Record> locationList = Db.find(sql +" order by id desc " + sLimit, customer_id);
        if(locationList == null)
            locationList = Collections.EMPTY_LIST;
        
        Map orderListMap = new HashMap();
        orderListMap.put("draw", draw);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", locationList);
        renderJson(orderListMap);
    }
    
    // 列出客户公司名称
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search() {
        String customerName = getPara("customerName");

        if(StringUtils.isEmpty(customerName)){
            customerName = "";
        }
        long userId = LoginUserController.getLoginUserId(this);
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id = user.getLong("office_id");
        
        List<Record> resultList = Collections.EMPTY_LIST;
        if(StrKit.isBlank(customerName)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr from user_query_history h, party p "
                    + "where h.ref_id=p.id and h.type='CUSTOMER' and h.user_id=? and p.office_id=?";
            resultList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId,office_id);
            if(resultList.size()==0){
                sql = "select p.id, p.abbr from party p where p.type = 'CUSTOMER' "
                        + " and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
                resultList = Db.find(sql+" order by abbr limit 25");
            }
            renderJson(resultList);
        }else{
            String sql = "select p.id, p.abbr,p.code from party p where (p.type = 'CUSTOMER' or p.type = 'OWN') ";
            String sql_contition =" and (p.abbr like '%"
            		+ customerName 
            		+ "%' or p.company_name like '%" 
            		+ customerName 
            		+ "%' or p.quick_search_code like '%" 
            		+ customerName.toLowerCase()+ "%'"
                	+ " or p.quick_search_code like '%" 
            		+ customerName.toUpperCase() 
            		+ "%'"
                	+ " or p.code like '%" 
            		+ customerName.toUpperCase() 
            		+ "%'"
                	+ " or p.code like '%" 
            		+ customerName.toLowerCase()
            		+ "%'"
            		+ " or p.company_name_eng like '%" 
            		+ customerName.toUpperCase() 
            		+ "%'"
                	+ " or p.company_name_eng like '%" 
            		+ customerName.toLowerCase()
            		+ "%'"
                    + " or concat(p.abbr,' - ',p.code) like '%" +customerName+ "%') ";
            if (customerName.trim().length() > 0) {
                sql +=sql_contition;
            }
            resultList = Db.find(sql+" order by abbr limit 25");
            
            if(resultList.size()==0){
            	String err = "无记录";
            	renderText(err);
            	return;
            }else{
            	 sql+=" and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
            	 if (customerName.trim().length() > 0) {
                     sql +=sql_contition;
                 }
            	 resultList = Db.find(sql+" order by abbr limit 25");
            	 renderJson(resultList);
            	 return;
            }
        }
    }
    
 // 列出客户公司名称
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search_company_title() {
        String customerName = getPara("customerName");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
   		long office_id = user.getLong("office_id");

        if(StringUtils.isEmpty(customerName)){
            customerName = "";
        }
        long userId = LoginUserController.getLoginUserId(this);
        
        List<Record> resultList = Collections.EMPTY_LIST;
            String sql = "select p.id, p.company_name,p.company_name_eng,p.phone,p.code from party p LEFT JOIN office_ref oref on sub_party_id = p.id  where oref.main_office_id = "+office_id;
            String sql_contition =" and (p.abbr like '%" + customerName + "%' or p.quick_search_code like '%" +customerName.toLowerCase()+ "%'"
                	+ " or p.quick_search_code like '%" + customerName.toUpperCase() + "%'"
                	+ " or p.code like '%" + customerName.toUpperCase() + "%'"
                	+ " or p.code like '%" +customerName.toLowerCase()+ "%' "
                	+ " or p.company_name like '%" +customerName+ "%' "
                    + " or concat(p.abbr,' - ',p.code) like '%" +customerName+ "%') ";
            if (customerName.trim().length() > 0) {
                sql +=sql_contition;
            }
            resultList = Db.find(sql+" order by abbr limit 25");
            
            if(resultList.size()==0){
            	String err = "无记录";
            	renderText(err);
            	return;
            }else{
            	 if (customerName.trim().length() > 0) {
                     sql +=sql_contition;
                 }
            	 resultList = Db.find(sql+" order by abbr limit 25");
            	 renderJson(resultList);
            	 return;
            }
        
    }
    
    
    
    
    // 列出所有客户名称
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search_customer() {
        String customerName = getPara("customerName");
       
        if(StringUtils.isEmpty(customerName)){
            customerName = "";
        }
        long userId = LoginUserController.getLoginUserId(this);
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id = user.getLong("office_id");
        List<Record> resultList = Collections.EMPTY_LIST;
        if(StrKit.isBlank(customerName)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr,ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info from user_query_history h, party p "
                    + "where h.ref_id=p.id and (h.type='CUSTOMER' OR h.type='OWN') and h.user_id=? and p.office_id =?";
            resultList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId,office_id);
            if(resultList.size()==0){
                sql = "select p.id, p.abbr, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                    + " from party p where (p.type = 'CUSTOMER' OR p.type = 'OWN') "
                    + " and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
                resultList = Db.find(sql+" order by abbr limit 25");
            }
            renderJson(resultList);
        }else{
            String sql = "select p.id, p.abbr, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                    + " from party p where  "
                    + " p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
                        
            if (customerName.trim().length() > 0) {
                sql +=" and (p.abbr like '%" + customerName + "%' or p.quick_search_code like '%" + customerName.toLowerCase() +"%'"
                	+ " or p.quick_search_code like '%" + customerName.toUpperCase() + "%') ";
            }
            resultList = Db.find(sql+" limit 25");

            renderJson(resultList);
        }
    }
    
    
    // 列出所有party名称
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search_party() {
        String partyName = getPara("customerName");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
   		long office_id = user.getLong("office_id");
       
        if(StringUtils.isEmpty(partyName)){
        	partyName = "";
        }
        long userId = LoginUserController.getLoginUserId(this); 
        List<Record> resultList = Collections.EMPTY_LIST;
        if(StrKit.isBlank(partyName)){//从历史记录查找
            String sql = "select h.ref_id, p.id, p.abbr,ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                    + " from user_query_history h, party p "
                    + "where h.ref_id=p.id  and h.user_id=? and p.office_id=?";
            resultList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId,office_id);
            if(resultList.size()==0){
                sql = "select p.id, p.abbr, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                    + " from party p where 1=1 "
                    + " and p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
                resultList = Db.find(sql+" order by abbr limit 25");
            }
            renderJson(resultList);
        }else{
            String sql = "select p.id, p.abbr, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                    + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                    + " from party p where  "
                    + " 1=1 and p.office_id ="+office_id;
                        
            if (partyName.trim().length() > 0) {
                sql +=" and (p.abbr like '%" + partyName + "%' or p.quick_search_code like '%" + partyName.toLowerCase() +"%'"
                	+ " or p.quick_search_code like '%" + partyName.toUpperCase() + "%') ";
            }
            resultList = Db.find(sql+" limit 25");

            renderJson(resultList);
        }
    }
    
    
    
    // 列出指定party名称，客户
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
    public void search_party_id() {
        String customer_id = getPara("customer_id");
       
        if(StringUtils.isEmpty(customer_id)){
        	customer_id = "";
        }
        Record resultFirst = new Record();
        String sql = "select p.id, p.abbr,p.company_name , ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                + " ifnull(p.address_eng, p.address) address, p.phone ,p.fax,p.zip_code,p.bill_of_lading_info"
                + " from party p where  "
                + " p.id in (select customer_id from user_customer where user_name='"+currentUser.getPrincipal()+"') ";
                        
            if (customer_id.trim().length() > 0) {
                sql +=" and p.id =" + customer_id ;
            }
            resultFirst = Db.findFirst(sql);

            renderJson(resultFirst);
        
    }

    
    // 列出所有party名称,供应商
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})
    public void searchParty() {
        String partyName = getPara("input")==null?getPara("partyName"):getPara("input");
        String type = getPara("type")==null?getPara("para"):getPara("type");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
   		long office_id = user.getLong("office_id");
        
   		String officeConditon = "";
   		if(!"carrier".equals(type)){
   			officeConditon = " and office_id="+office_id;
        }
   		
   		String condition = "";
        if (partyName.trim().length() > 0) {
        	condition = " and (p.abbr like '%" + partyName + "%' or p.quick_search_code like '%" + partyName.toUpperCase() + "%') ";
        }

        String sql = "select  * from(SELECT p.id, p.abbr,p.abbr name, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                + " ifnull(p.address_eng, p.address) address, p.phone,p.ref_office_id,p.bill_of_lading_info"
				+ " FROM user_query_history uqh"
				+ " LEFT JOIN party p ON p.id = uqh.ref_id"
				+ " and uqh.type = upper('"+type+"')"
				+ " WHERE uqh.type = upper('"+type+"')"
				+ condition
				+ " and uqh.user_id = "+LoginUserController.getLoginUserId(this)
				+ " ORDER BY uqh.query_stamp desc ) A"
				+ " UNION "
				+ " (select p.id, p.abbr,p.abbr name, ifnull(p.contact_person_eng, p.contact_person) contact_person, "
                + " ifnull(p.address_eng, p.address) address, p.phone,p.bill_of_lading_info,p.ref_office_id"
                + " from party p where sp_type like '%"+type+"%' "
                + condition+officeConditon+")";
        
        List<Record>  partyList = Db.find(sql);

        renderJson(partyList);
    }
    
    
  //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
    	String id = getPara("order_id");
    	//String type = getPara("type");
    	List<UploadFile> fileList = getFiles("customer_doc");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		
    		Record r = new Record();
    		r.set("party_id", id);
			r.set("uploader", LoginUserController.getLoginUserId(this));
			r.set("doc_name", fileName);
			r.set("upload_time", new Date());
			Db.save("party_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    //上传公司LOGO
    @Before(Tx.class)
    public void uploadLogo(){
    	String id = getPara("order_id");
    	String src_path = getPara("src_path");
    	String self_btn_id = getPara("self_btn_id");
    	Record party = Db.findFirst("select * from party where id =?",id);
    	
    	if("changeFileuploadLogoSpan".equals(self_btn_id)){
			String path = getRequest().getServletContext().getRealPath("/");
	    	File deleteLogo = new File(path+src_path);
	    	if (deleteLogo.exists() && deleteLogo.isFile()) {
	    		deleteLogo.delete();
	    	}
		}
    	party.set("logo_path", null);
    		List<UploadFile> fileList = getFiles("partyLOGO");

		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();

    		party.set("logo_path", fileName);
    		
			Db.update("party",party);
		}
		
    	renderJson(party);
    }
    
    //删除公司LOGO
    @Before(Tx.class)
    public void deleteLogo(){
    	String id = getPara("order_id");
    	String src_path = getPara("src_path");
    	String path = getRequest().getServletContext().getRealPath("/");
    	File deleteLogo = new File(path+src_path);

    	if (deleteLogo.exists() && deleteLogo.isFile()) {
    		deleteLogo.delete();
    	}
    	
    	Record party = Db.findFirst("select * from party where id =?",id);
    	party.set("logo_path", null);
    	Db.update("party",party);
    	HttpServletRequest url = getRequest();
    	
    	renderJson("{\"result\":true}");
    }
    
    //获取访问链接
//    HttpServletRequest url = getRequest();
//	StringBuffer URL = url.getRequestURL();
    //String URL = url.getServerName();域名
    
    //上传陆运图片
    @Before(Tx.class)
    public void saveLandDocFile(){
    	String id = getPara("id");
    	//String type = getPara("type");
    	Record rLandDoc = Db.findFirst("select * from dockinfo where id =?",id);
    	
    	
    	rLandDoc.set("picture_link", null);
    		List<UploadFile> fileList = getFiles("dockinfo");

		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		rLandDoc.set("uploader", LoginUserController.getLoginUserId(this));
    		rLandDoc.set("upload_time", new Date());
    		rLandDoc.set("picture_link", fileName);
    		
			Db.update("dockinfo",rLandDoc);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
  //删除陆运图片
    @Before(Tx.class)
    public void delectLandDocFile(){
    	String id = getPara("id");
    	Record rLandDoc = Db.findFirst("select * from dockinfo where id =?",id);
    	rLandDoc.set("picture_link", null);
		Db.update("dockinfo",rLandDoc);
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    
    
    
  //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("party_doc", id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\customer_doc\\"+fileName;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("party_doc", r);
            resultMap.put("result", result);
        }else{
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    
  //异步刷新字表
    public void tableList(){
//    	String order_id = getPara("order_id");
//    	String sql = "select jod.*,u.c_name from party_doc jod left join user_login u on jod.uploader=u.id "
//    			+ " where party_id=? order by jod.id";
//    	List<Record> list = Db.find(sql,order_id);
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	
    	List<Record> list = getItems(order_id,type);
    	Map<String,Object> map = new HashMap<String,Object>();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list != null?list.size():0);
        map.put("iTotalDisplayRecords", list != null?list.size():0);
        map.put("aaData", list);
        renderJson(map); 
    }
    
    private List<Record> getItems(String orderId,String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	 if("docItem".equals(type)){
    		itemSql = "select jod.*,u.c_name from party_doc jod left join user_login u on jod.uploader=u.id "
        			+ " where jod.party_id=? order by jod.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("customerQuotationItem".equals(type)){
    		itemSql = " SELECT pq.*,c.`name` currency_name,d1.dock_name take_address_name,d2.dock_name delivery_address_name,d3.dock_name loading_wharf1_name "
    				+" ,d4.dock_name loading_wharf2_name FROM party_quotation pq "
    				+ " LEFT JOIN currency c ON c.id = pq.currency_id"
    				+" LEFT JOIN dockinfo d1 on d1.id=pq.take_wharf "
    				+" LEFT JOIN dockinfo d2 on d2.id=pq.back_wharf "
    				+" LEFT JOIN dockinfo d3 on d3.id=pq.loading_wharf1 "
    				+" LEFT JOIN dockinfo d4 on d4.id=pq.loading_wharf2 "
    				+ " where pq.party_id=? order by pq.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("dock".equals(type)){
    		itemSql = "SELECT d.* FROM dockinfo d WHERE  d.party_type='customer' and d.party_id=? order by d.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("contacts".equals(type)){
    		itemSql = "SELECT * FROM contacts_item WHERE party_id=?";
    		itemList = Db.find(itemSql, orderId);
    	}else if("account".equals(type)){
    		itemList = Db.find("SELECT fa.*,cy.code currency_name FROM fin_account fa "
    				+ " LEFT JOIN currency cy on cy.id = fa.currency_id  WHERE order_id =?",orderId);
		}else if("salesman".equals(type)){
    		itemList = Db.find("SELECT cs.*,ul.c_name salesman_name FROM customer_salesman cs "
    				+ " LEFT JOIN user_login ul on cs.salesman_id = ul.id WHERE party_id =?",orderId);
		}else if("mailSet".equals(type)){
    		itemList = Db.find("SELECT * from send_mail_set where party_id =?",orderId);
		}
		return itemList;
	}
    public Record getDetail(String orderId,String type){
    	Record re =null;
    	String sql = "";
    	if("mailSet".equals(type)){
    		sql = "select * from send_mail_set where party_id =?";
    		re = Db.findFirst(sql,orderId);
    	}
    	return re;
    };
    
    
    @Before(Tx.class)
	public static Record importCheck(List<Map<String, String>> lines, long office_id) {
		Record result = new Record();
		result.set("result",true);
		//if ("true".equals(importResult.get("result"))) {
			int rowNumber = 2;//最左边的编码行
			String error_msg = "";
			try {
				for (Map<String, String> line :lines) {
					if(line != null){
					String quick_search_code = line.get("助记码").trim();
					String company_name = line.get("公司中文名称").trim();
					String abbr = line.get("公司简称").trim();
					String company_name_eng = line.get("公司英文名称").trim();
					String address = line.get("中文地址").trim();
					String address_eng = line.get("英文地址").trim();
					String contact_person = line.get("联系人").trim();
					String contact_person_eng = line.get("联系人(英文)").trim();
					String phone = line.get("联系人电话").trim();
					String fax = line.get("传真").trim();
					String skype = line.get("视屏通话帐号(skype)").trim();
					String email = line.get("邮箱").trim();
					String zip_code = line.get("邮编").trim();
					String company_type = line.get("企业类型").trim();
					String receipt = line.get("开票单位").trim();
					String registration = line.get("商检企业备案号").trim();
					String custom_registration = line.get("海关备案号").trim();
					String insurance_rates = line.get("保险费率").trim();
					String remark = line.get("备注").trim();
					
					if(StringUtils.isBlank(quick_search_code)){
						error_msg += "第"+rowNumber+"行【助记码】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(company_name)){
						Party party = Party.dao.findFirst("select * from party where company_name =? and type='CUSTOMER' and office_id = ?",company_name,office_id);
						if(party != null){
							error_msg += "第"+rowNumber+"行【公司中文名称】，系统已存在【"+company_name+"】此客户<br/>";
						}
					}else{
						error_msg += "第"+rowNumber+"行【公司中文名称】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(abbr)){
						Record customer = Db.findFirst("select * from party where abbr = ?  and type = 'CUSTOMER'  and office_id = ?",abbr,office_id);
			   			if(customer != null){
			   				error_msg += "第"+rowNumber+"行【公司简称】，系统已存在【"+abbr+"】此客户<br/>";
			   			}
					}else{
						error_msg += "第"+rowNumber+"行【公司简称】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(email)){
						if(!CheckOrder.checkEmail(email)){
							error_msg += "第"+rowNumber+"行【邮箱】，【"+abbr+"】邮箱格式不合法<br/>";
						}
					}
		   			
					rowNumber++;
				}
				}
				
				if(StringUtils.isNotBlank(error_msg)){
					throw new Exception();
				}
			} catch (Exception e) {
				result.set("result", false);
				result.set("cause", error_msg);
				return result;
			} 
		return result;
	}
	
	
	
	@Before(Tx.class)
	public static Record importValue( List<Map<String, String>> lines, long user_id, long office_id) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);

		int rowNumber = 1;
		
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			for (Map<String, String> line :lines) {
				if(line != null){
				String quick_search_code = line.get("助记码").trim();
				String company_name = line.get("公司中文名称").trim();
				String abbr = line.get("公司简称").trim();
				String company_name_eng = line.get("公司英文名称").trim();
				String address = line.get("中文地址").trim();
				String address_eng = line.get("英文地址").trim();
				String contact_person = line.get("联系人").trim();
				String contact_person_eng = line.get("联系人(英文)").trim();
				String phone = line.get("联系人电话").trim();
				String fax = line.get("传真").trim();
				String skype = line.get("视屏通话帐号(skype)").trim();
				String email = line.get("邮箱").trim();
				String zip_code = line.get("邮编").trim();
				String company_type = line.get("企业类型").trim();
				String receipt = line.get("开票单位").trim();
				String registration = line.get("商检企业备案号").trim();
				String custom_registration = line.get("海关备案号").trim();
				String insurance_rates = line.get("保险费率").trim();
				String remark = line.get("备注").trim();
				
				Record order = new Record();
				String code = OrderNoGenerator.getOrderNo("party",office_id);
	            if(StringUtils.isNotBlank(code)){
	            	order.set("code", code.replace("P", "C"));
	            }
	            order.set("quick_search_code",quick_search_code );
	            order.set("company_name",company_name );
	            order.set("abbr",abbr );
	            order.set("company_name_eng",company_name_eng );
	            order.set("address", address);
	            order.set("address_eng",address_eng );
	            order.set("contact_person",contact_person );
	            order.set("contact_person_eng",contact_person_eng );
	            order.set("phone",phone );
	            order.set("fax",fax );
	            order.set("skype",skype );
	            order.set("email",email );
	            order.set("zip_code",zip_code );
	            order.set("company_type", company_type);
	            order.set("receipt", receipt);
	            order.set("registration", registration);
	            order.set("custom_registration",custom_registration );
	            order.set("insurance_rates", insurance_rates);
	            order.set("remark",remark );
	            
	            order.set("office_id", office_id);
	            order.set("type", Party.PARTY_TYPE_CUSTOMER);
	            order.set("creator", user_id);
	            order.set("create_date", new Date());
	            
	   			Db.save("party", order);

//				for (int i = 0; i < list.size(); i++) {
//
//				}

				rowNumber++;
				}
			}
			conn.commit();
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！");
		} catch (Exception e) {
			System.out.println("导入操作异常！");
			System.out.println(e.getMessage());
			e.printStackTrace();
			
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			result.set("result", false);
			
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
			
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		
		return result;
	}
    
}
