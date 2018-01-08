package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.Office;
import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.yh.profile.Carinfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CarinfoController extends Controller {
    private Log logger = Log.getLog(CarinfoController.class);
    // in config route已经将路径默认设置为/yh
    // me.add("/yh", controllers.yh.AppController.class, "/yh");
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_LIST})
    
    public void search() {
        
        String input = getPara("input");
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        Long office_id = user.getLong("office_id");
      
        
        List<Record> orders = Collections.EMPTY_LIST;
        if(StrKit.isBlank(input)){
            orders = Db.find("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
                    + " where c.type='OWN' and c.office_id = ? order by id desc limit 10 ", office_id);
        }else{
            orders = Db.find("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
                    + " where c.type='OWN' and c.office_id = ? and c.car_no like '%"+input+"%' order by car_no limit 10 ", office_id);
        }

        renderJson(orders);
    }
    
    
    @Before(EedaMenuInterceptor.class)
    public void index() {	
	    render("/eeda/profile/carinfo/carlist.html"); 
    }

//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_LIST})
    public void driverinfoIndex(){
        render("/eeda/profile/carinfo/driverlist.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PC_LIST})
    public void spcarinfoIndex(){
        render("/eeda/profile/carinfo/spcarList.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PD_LIST})
    public void spdriverinfoIndex(){
		render("/eeda/profile/carinfo/spDriverList.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_LIST})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        Long office_id = user.getLong("office_id");
        
        // 获取总条数
        String totalWhere = "";
//        String sql = "select count(1) total from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_OWN +"' and (o.id = " + office_id + " or o.belong_office = " + office_id + ")";
        String sql = "select * from (select c.* from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_OWN +"' "
        		+ " and (o.id = " + office_id + " or o.belong_office = " + office_id + ")"
        		 + " ) A where 1=1";
        
        String condition = DbUtils.buildConditions(getParaMap());
        
//        Record rec = Db.findFirst(sql + totalWhere);
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);

        // 获取当前页的数据
        List<Record>  orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
//        String sql2 = "select c.* from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_OWN +"' and (o.id = " + office_id + " or o.belong_office = " + office_id + ")" + sLimit;
       
        Map map = new HashMap();
//        orderMap.put("sEcho", pageIndex);
//        orderMap.put("iTotalRecords", rec.getLong("total"));
//        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
//        orderMap.put("aaData", orderList);
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);

        renderJson(map);
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_LIST})
    public void driverlist() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        
        //获取当前用户的总公司ID
        Long parentID = pom.getParentOfficeId();
        
        
        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from party p left join office o on o.id = p.office_id where p.party_type='DRIVER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select c.license license,c.contact_person,c.phone,c.identification,p.id as pid,p.is_stop from party p left join contact c on p.contact_id =c.id left join office o on o.id = p.office_id where p.party_type='DRIVER' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")" + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }

    // 发车记录单list
    public void carmanageList() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        // 获取总条数
        String totalWhere = "";
        String sql = "select count(1) total from depart_order dor " + ""
                    + " left join carinfo c on dor.driver_id = c.id " + " where dor.status!='取消' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own'";
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));

        // 获取当前页的数据
        List<Record> orders = Db.find("select dor.*,ct.contact_person,ct.phone,c.car_no,c.cartype,c.status cstatus,c.length, (select group_concat(dt.transfer_order_no separator '\r\n')  from depart_transfer dt where depart_id = dor.id)  as transfer_order_no  from depart_order dor "
                    + " left join carinfo c on dor.carinfo_id = c.id "
                    + " left join party p on dor.driver_id = p.id "
                    + " left join contact ct on p.contact_id = ct.id "
                    + " where dor.status!='取消' and combine_type = '"
                    + DepartOrder.COMBINE_TYPE_PICKUP + "' and combine_type = 'PICKUP' and (dor.status = '已入货场' or dor.status = '已入库' ) and dor.pickup_mode = 'own' order by dor.create_stamp desc " + sLimit);
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_CREATE})
    @Before(EedaMenuInterceptor.class)
    public void add() {
            render("/eeda/profile/carinfo/edit.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_CREATE})
    public void driveradd() {
            render("/eeda/profile/carinfo/driveredit.html");
    }

    // 添加车辆
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_CREATE,PermissionConstant.PERMSSION_CI_UPDATE},logical=Logical.OR)
    public void save() {
        String id = getPara("carId");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        Long office_id = user.getLong("office_id");
        Carinfo carinfo = null;
        Carinfo contact = null;
        if (id != null && !"".equals(id)) {
            carinfo = Carinfo.dao.findById(id);
            setCarifo(carinfo);
            carinfo.set("type", Carinfo.CARINFO_TYPE_OWN);
            carinfo.set("hundred_fuel_standard", getPara("hundred_fuel_standard") == "" ? 0 : getPara("hundred_fuel_standard"));
            carinfo.set("rated_load", getPara("rated_load") == "" ? 0 : getPara("rated_load"));
            carinfo.set("rated_cube", getPara("rated_cube") == "" ? 0 : getPara("rated_cube"));
            carinfo.set("cartype", getPara("cartype") == "" ? 0 : getPara("cartype"));
            carinfo.set("length", getPara("length") == "" ? 0 : getPara("length"));
            carinfo.set("toca_weight", getPara("toca_weight") == "" ? 0 : getPara("toca_weight"));
            carinfo.set("head_weight", getPara("head_weight") == "" ? 0 : getPara("head_weight"));
            carinfo.set("toca_num", getPara("toca_num") == "" ? 0 : getPara("toca_num"));
            carinfo.set("short_phone", getPara("short_phone") == "" ? 0 : getPara("short_phone"));
            carinfo.set("initial_mileage", getPara("initial_mileage") == "" ? 0 : getPara("initial_mileage"));
            carinfo.set("parent_id", getPara("parent_id") == "" ? null : getPara("parent_id"));
            carinfo.set("vehicle_status", getPara("vehicle_status") == "" ? null : getPara("vehicle_status"));
            if(getPara("gongsiche")!=""&&getPara("gongsiche")!=null){
            	carinfo.set("car_owned", getPara("gongsiche") == "" ? 0 : getPara("gongsiche"));
            }else if(getPara("jieche")!=""&&getPara("jieche")!=null){
            	carinfo.set("car_owned", getPara("jieche") == "" ? 0 : getPara("jieche"));
            }
            System.out.println("");
            
            carinfo.set("office_id", office_id);
            
            carinfo.update();
        } else {
        	//判断供应商简称
        	contact = Carinfo.dao.findFirst("select * from carinfo where car_no=? and office_id=?", getPara("car_no"), pom.getCurrentOfficeId());
            if(contact!=null){
            	renderText("abbrError");
            	return ;
            }
            carinfo = new Carinfo();
            setCarifo(carinfo);
            carinfo.set("type", Carinfo.CARINFO_TYPE_OWN);
            carinfo.set("hundred_fuel_standard", getPara("hundred_fuel_standard") == "" ? 0 : getPara("hundred_fuel_standard"));
            carinfo.set("rated_load", getPara("rated_load") == "" ? 0 : getPara("rated_load"));
            carinfo.set("rated_cube", getPara("rated_cube") == "" ? 0 : getPara("rated_cube"));
            carinfo.set("toca_weight", getPara("toca_weight") == "" ? 0 : getPara("toca_weight"));
            carinfo.set("head_weight", getPara("head_weight") == "" ? 0 : getPara("head_weight"));
            carinfo.set("toca_num", getPara("toca_num") == "" ? 0 : getPara("toca_num"));
            carinfo.set("short_phone", getPara("short_phone") == "" ? 0 : getPara("short_phone"));
            carinfo.set("initial_mileage", getPara("initial_mileage") == "" ? 0 : getPara("initial_mileage"));
            carinfo.set("parent_id", getPara("parent_id") == "" ? null : getPara("parent_id"));
            
            if(getPara("gongsiche")!=""&&getPara("gongsiche")!=null){
            	carinfo.set("car_owned", getPara("gongsiche") == "" ? 0 : getPara("gongsiche"));
            }else if(getPara("jieche")!=""&&getPara("jieche")!=null){
            	carinfo.set("car_owned", getPara("jieche") == "" ? 0 : getPara("jieche"));
            }
            carinfo.set("office_id", office_id);
            carinfo.save();
        }
        setAttr("saveOK", true);
        // redirect("/carInfo");
        renderJson(carinfo);
    }

    // 添加司机保存
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_CREATE, PermissionConstant.PERMSSION_D_UPDATE}, logical=Logical.OR)
    public void driversave() {
        String id = getPara("driverId");
        Party party = null;
        /*Contact contact = null;
        Date createDate = Calendar.getInstance().getTime();
        if (!id.isEmpty()) {
            party = Party.dao.findById(id);
            party.set("last_update_date", createDate);
            party.set("office_id", getPara("officeSelect"));
            party.update();

            contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id=" + id);
            setContact(contact);
            contact.update();
        } else {
            contact = new Contact();
            setContact(contact);
            contact.save();
            party = new Party();
            party.set("party_type", Party.PARTY_TYPE_DRIVER);
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", currentUser.getPrincipal());
            party.set("create_date", createDate);
            party.set("office_id", getPara("officeSelect"));
            party.save();

        }
        */
        setAttr("saveOK", true);
        	redirect("/driverinfo/driverinfoIndex");
    }

    

    public void setCarifo(Carinfo carinfo) {
        carinfo.set("driver", getPara("driver")== "" ? 0 : getPara("driver"));
        carinfo.set("cartype", getPara("ctype")== "" ? 0 : getPara("ctype"));
        carinfo.set("car_no", getPara("car_no")== "" ? 0 : getPara("car_no"));
        carinfo.set("phone", getPara("phone")== "" ? 0 : getPara("phone"));
        carinfo.set("length", getPara("length")== "" ? 0 : getPara("length"));
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMISSION_TO_DELETE,PermissionConstant.PERMSSION_PC_DELETE},logical=Logical.OR)
    public void delect() {
        String id = getPara();
        if (id != null) {
        	//基础数据不能删除
        	Carinfo info = Carinfo.dao.findById(id);
        	 Object obj = info.get("is_stop");
             if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	 info.set("is_stop", true);
             }else{
            	 info.set("is_stop", false);
             }
             info.update();
            /*Carinfo.dao.deleteById(id);*/
        }
        renderJson("{\"success\":true}");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_DELETE})
    public void driverdelect() {
        String id = getPara();

        if(id != null && !"".equals(id)){
    		Party party = Party.dao.findById(id);
    		Object obj = party.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	party.set("is_stop", true);
            }else{
            	party.set("is_stop", false);
            }
            party.update();
    	}
        renderJson("{\"success\":true}");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CI_UPDATE})
    @Before(EedaMenuInterceptor.class)
    public void edit() {
    	String id = getPara("id");
	    UserLogin user1 = LoginUserController.getLoginUser(this);
	    if (user1==null) {
            return;
        }
	    long office_id=user1.getLong("office_id");
	    //判断与登陆用户的office_id是否一致
	    if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("carinfo", Long.valueOf(id), office_id)){
	    	renderError(403);// no permission
	        return;
	    }
//        Carinfo carinfo = Carinfo.dao.findById(id);
       String sqlString="SELECT c.*,p.abbr parent_id_input FROM carinfo c LEFT JOIN party p on p.id=c.parent_id "
    		   			+" WHERE c.id="+id;
       Record record=Db.findFirst(sqlString);
        setAttr("lu", record);
        render("/eeda/profile/carinfo/edit.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_D_UPDATE})
    public void driveredit() {
        String id = getPara();
        Party party = Party.dao.findById(id);
//        Contact contact = Contact.dao.findById(party.get("contact_id"));
//        
//        setAttr("lu", party);
//        setAttr("lu2", contact);
        render("/eeda/profile/carinfo/driveredit.html");
    }
    
    // 供应商车辆信息
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PC_LIST})
    public void spCarInfoList(){
    	String sLimit = "";
    	String sSearch = getPara("sSearch"); 
    	String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        //获取当前用户的总公司ID
        Long parentID = pom.getParentOfficeId();
        
        String limit = " and (c.driver like '%"+sSearch+"%' or c.car_no like '%"+sSearch+"%' or c.cartype like '%"+sSearch+"%' or c.phone like '%"+sSearch+"%' or c.length like '%"+sSearch+"%') ";
        
        // 获取总条数
        String totalWhere = "";
        Record rec = null;
        List<Record> orders = null;
        if(sSearch==null || "".equals(sSearch)){
        	String sql = "select count(1) total from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_SP +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
             rec = Db.findFirst(sql + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
             orders = Db.find("select c.* from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_SP +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")" + sLimit);
        }else{
        	String sql = "select count(1) total from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_SP +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
            rec = Db.findFirst(sql + limit + totalWhere);
            logger.debug("total records:" + rec.getLong("total"));

            // 获取当前页的数据
            orders = Db.find("select c.* from carinfo c left join office o on c.office_id = o.id where c.type = '" + Carinfo.CARINFO_TYPE_SP +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")" + limit + sLimit);
         }
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PC_CREATE})
    public void addSpCarInfo() {
            render("/eeda/profile/carinfo/spCarInfoEdit.html");
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PC_CREATE, PermissionConstant.PERMSSION_PC_UPDATE}, logical=Logical.OR)
    public void saveSpCarInfo() {
        String id = getPara("carId");
        Carinfo carinfo = null;
        if (id != "" && !"".equals(id)) {
            carinfo = Carinfo.dao.findById(id);
            setCarifo(carinfo);
            carinfo.set("type", Carinfo.CARINFO_TYPE_SP);
            carinfo.set("identity_number", getPara("identity_number"));
            carinfo.set("family_contact", getPara("family_contact"));
            carinfo.set("mobile", getPara("mobile"));
            carinfo.set("remark", getPara("remark"));
            carinfo.set("office_id", getPara("officeSelect"));
            carinfo.update();
        } else {
            carinfo = new Carinfo();
            setCarifo(carinfo);
            carinfo.set("type", Carinfo.CARINFO_TYPE_SP);
            carinfo.set("identity_number", getPara("identity_number"));
            carinfo.set("family_contact", getPara("family_contact"));
            carinfo.set("mobile", getPara("mobile"));
            carinfo.set("remark", getPara("remark"));
            //carinfo.set("office_id", getPara("hideOfficeId"));
            carinfo.set("office_id", getPara("officeSelect"));
            carinfo.save();
        }
        redirect("/spcarinfo/spcarinfoIndex");
    }    
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PC_UPDATE})
    public void editSpCarInfo() {
        String id = getPara();
        Carinfo carinfo = Carinfo.dao.findById(id);
        System.out.println(carinfo);
        setAttr("lu", carinfo);
        render("/eeda/profile/carinfo/spCarInfoEdit.html");
    }
    
    public void deleteSpCarInfo() {
    	String id = getPara();
    	if(id != null && !"".equals(id)){
    		Carinfo carinfo = Carinfo.dao.findById(id);
        	Object obj = carinfo.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	carinfo.set("is_stop", true);
            }else{
            	carinfo.set("is_stop", false);
            }
            carinfo.update();
    	}
    	
    	redirect("/spcarinfo");
    }    
    
    // 供应商司机信息
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PD_LIST})
    public void spDriverList(){
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	
    	 //获取当前用户的总公司ID
         Long parentID = pom.getParentOfficeId();
         
    	
    	// 获取总条数
    	String totalWhere = "";
    	String sql = "select count(1) total from party p left join office o on o.id = p.office_id where p.party_type = '" + Party.PARTY_TYPE_SP_DRIVER +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")";
    	Record rec = Db.findFirst(sql + totalWhere);
    	logger.debug("total records:" + rec.getLong("total"));
    	
    	// 获取当前页的数据
    	List<Record> orders = Db.find("select p.*,p.id pid,c.* from party p left join contact c on c.id = p.contact_id left join office o on o.id = p.office_id where party_type = '" + Party.PARTY_TYPE_SP_DRIVER +"' and (o.id = " + parentID + " or o.belong_office = " + parentID + ")" + sLimit);
    	Map orderMap = new HashMap();
    	orderMap.put("sEcho", pageIndex);
    	orderMap.put("iTotalRecords", rec.getLong("total"));
    	orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
    	orderMap.put("aaData", orders);
    	
    	renderJson(orderMap);
    }
//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PD_CREATE})
    public void addSpDriver() {
    	render("/eeda/profile/carinfo/spDriverEdit.html");
    }

    public void saveSpDriver() {
    	String id = getPara("driverId");
        Party party = null;
        /*Contact contact = null;
        if (!"".equals(id) && id != null) {
            party = Party.dao.findById(id);
            party.set("last_update_date", new Date());
            party.update();

            contact = Contact.dao.findFirst("select c.* from contact c,party p where c.id=p.contact_id and p.id=" + id);
            setContact(contact);
            contact.update();
        } else {
        	
        	 //获取当前用户的总公司ID
            Long parentID = pom.getParentOfficeId();
             
             
            contact = new Contact();
            setContact(contact);
            contact.save();
            party = new Party();
            party.set("contact_id", contact.getLong("id"));
            party.set("creator", currentUser.getPrincipal());
            party.set("create_date", new Date());
            party.set("party_type", Party.PARTY_TYPE_SP_DRIVER);
            party.set("office_id", parentID);
            party.save();
        }
        */
        setAttr("saveOK", true);
        	redirect("/spdriverinfo/spdriverinfoIndex");
    }    

//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PD_DELETE})
    public void deleteSpDriver() {
    	String id = getPara();
    	/*Party.dao.deleteById(id);*/
    	if(id != null && !"".equals(id)){
    		Party party = Party.dao.findById(id);
    		Object obj = party.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	party.set("is_stop", true);
            }else{
            	party.set("is_stop", false);
            }
            party.update();
    	}
    	redirect("/spdriverinfo/spdriverinfoIndex");
    }
    // 查出所有的office
 	public void searchAllOffice() {
 		ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
		Long parentID = pom.getParentOfficeId();
		
		//List<Record> offices = Db.find("select o.id,o.office_name,o.is_stop from office o where o.id = " + parentID +" or o.belong_office = " + parentID);
 		List<Office> offices = Office.dao.find("select * from office o where o.id = " + parentID +" or o.belong_office = " + parentID);
 		renderJson(offices);
 	}
 	
 	public void searchOfficebyUser() {
 		String userName = currentUser.getPrincipal().toString();
 		
 		List<Office> offices = Office.dao.find("select o.* from user_office uo left join office o on uo.office_id = o.id where uo.user_name = '" + userName + "'");
 		renderJson(offices);
 	}
 	
}
