package controllers.profile;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.eeda.profile.Account;
import models.eeda.profile.Warehouse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import controllers.util.DbUtils;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class WarehouseController extends Controller{

    private Logger logger = Logger.getLogger(WarehouseController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    Long parentID = pom.getParentOfficeId();
    
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void index() {
		render("/profile/warehouse/list.html");
	}
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_W_LIST})
	public void list() {
		
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }

        String sql = "select w.*,(select trim(concat(l2.name, ' ', l1.name,' ',l.name)) from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code=w.location) dname,lc.name from warehouse w"
                        + " left join location lc on w.location = lc.code"
                        + " left join office o on o.id = w.office_id"
                        + " where (o.id = " + parentID + " or o.belong_office = " + parentID +")";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> warehouses = Db.find(sql);

        List<Record> orders = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderMap= new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap); 
	}
	
//	public void listContact(){
//		List<Contact> contactjson = Contact.dao.find("select * from contact");			
//        renderJson(contactjson);
//	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_CREATE})
	public void add() {
		setAttr("saveOK", false);
		render("/profile/warehouse/edit.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_UPDATE})
	public void edit() {
		long id = getParaToLong("id");

		Warehouse warehouse = Warehouse.dao.findById(id);
		setAttr("order", warehouse);

		if(warehouse.get("location") != null && !"".equals(warehouse.get("location"))){
	        String code = warehouse.get("location");
	        Record re = Db.findFirst("select get_loc_full_name('"+code+"') as locFrom");
//	        setAttr("default_loc_from", re.getStr("locFrom"));
	        setAttr("location_name", re.getStr("locFrom"));
		}
	
		Party sp = Party.dao.findFirst("select * from party where id = "+warehouse.get("sp_id"));
		setAttr("sp", sp);
		render("/profile/warehouse/edit.html");
	}
	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_DELETE})
	public void delete() {
		
		String id = getPara();
		Warehouse warehouse = Warehouse.dao.findById(id);
		/*warehouse.set("office_id", null);
		warehouse.set("sp_id", null);*/
		if(!"inactive".equals(warehouse.get("status"))){
			warehouse.set("status", "inactive");
		}else{
			warehouse.set("status", "active");
		}
		warehouse.update();
		//warehouse.delete();
		redirect("/warehouse");
	}

	@RequiresPermissions(value = {PermissionConstant.PERMSSION_W_CREATE, PermissionConstant.PERMSSION_W_UPDATE}, logical=Logical.OR)
	public void save() {
        String jsonStr=getPara("params");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        Warehouse order=null; 
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            //update
            order = Warehouse.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.update();
        } else {
            //create 
            order = new Warehouse();
            DbUtils.setModelValues(dto, order);
            order.set("office_id", LoginUserController.getLoginUser(this).get("office_id"));
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
	}
	
	public void findDocaltion(){
		String officeId = getPara("officeId");
		Office office = Office.dao.findById(officeId);
		String code = null;
		if(office.get("location") != null && !"".equals(office.get("location"))){
			code = office.get("location");
		}
		logger.debug("所在地："+code);
        renderJson(code);
	}
	
	
}
