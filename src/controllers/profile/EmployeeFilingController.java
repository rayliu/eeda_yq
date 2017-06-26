package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.ParentOfficeModel;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;
import models.eeda.profile.Employee;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.DbUtils;
import controllers.util.ParentOffice;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class EmployeeFilingController extends Controller {
	private Log logger = Log.getLog(PrivilegeController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);

	
	
	@Before(EedaMenuInterceptor.class)
	public void index(){
		render("/eeda/profile/employee/employeeFilingList.html");
	}
	
	@Before(Tx.class)
	public void save() {
        String jsonStr=getPara("params");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
        Employee em = new Employee();
        if (StringUtils.isBlank(id)) {
        	//create
   			DbUtils.setModelValues(dto, em);
   			em.set("create_stamp",new Date());
   			em.set("office_id",office_id);
   			em.save();
            id = em.getLong("id").toString();
        } else {
        	em = Employee.dao.findById(id);
            DbUtils.setModelValues(dto, em);
            em.set("update_stamp",new Date());
            em.update();
        }
        em.toRecord();
        renderJson(em);
    }
	
	public void list() {
		
		String confirmFee = getPara("confirmFee");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        String ref_office = "";

        sql = " SELECT em.*,r.name station_name from employee em  LEFT JOIN role r on em.station = r.id where 1 =1 and em.office_id= "+office_id;
        

        condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by   create_stamp desc " );
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
    }
	
	/*编辑*/
	@Before(EedaMenuInterceptor.class)
	public void edit(){
		String id = getPara("id");
		Record em = new Record();
		String sql = "SELECT em.*,r.name station_name from employee em  LEFT JOIN role r on em.station = r.id"
				+ " WHERE em.id = ?";
		em = Db.findFirst(sql,id);
        setAttr("order", em);		
		render("/eeda/profile/employee/addEmployeeFiling.html");
	}
	
	/*给新用户分配角色*/
	@Before(EedaMenuInterceptor.class)
	public void create(){
		render("/eeda/profile/employee/addEmployeeFiling.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void addOrUpdate(){
		String id = getPara("id");
		UserLogin user = UserLogin.dao.findFirst("select * from user_login where id = ?",id);
		List<UserRole> list = UserRole.dao.find("select * from user_role where user_name = ?",user.get("user_name"));
		if(list.size()>0){
			setAttr("user_name", user.get("user_name"));		
			render("/eeda/profile/userRole/assigning_roles.html");
		}else{
			render("/eeda/profile/userRole/addRole.html");
		}
		
	}
	/*列出没有角色的用户*/
	public void userList(){
		String sql = "";
		Long parentID = pom.getBelongOffice();
		//系统管理员
		if(parentID == null || "".equals(parentID)){
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_id is null and (o.id = " + pom.getParentOfficeId() +" or o.belong_office= "+ pom.getParentOfficeId() +")";
		}else{
			sql = "select u.*, ur.role_code from user_login u left join office o on u.office_id = o.id left join user_role ur on u.user_name = ur.user_name where ur.role_id is null and o.id = " + pom.getCurrentOfficeId();
		}
		
		List<Record> orders = Db.find(sql);
        renderJson(orders);
	}
	
	public void updateRole(){
		String userName = getPara("name");
		String r = getPara("roles");
		
		if(StrKit.isBlank(r)){
		    Db.update("delete from user_role where user_name= ? ", userName);
		}else{
		    Db.update("delete from user_role where user_name= ? and role_id not in("+r+")", userName);
		
            String[] role_ids = r.split(",");
            //要添加的role
            
            if(role_ids.length>0){
            	for (Object role_id : role_ids) {
                    UserRole ur = new UserRole();
                    ur.set("user_name", userName);
                    logger.debug("role_id:"+role_id);
                    UserRole role = UserRole.dao.findFirst("select * from user_role where user_name=? and role_id=?", userName, role_id);
                    if(role == null){
                    	ur.set("role_id", role_id);
                        ur.save();
                    }
                }
            }
		}
		clearMenuCache(userName);
		renderJson();
	}
	
	//清除菜单缓存
	private void clearMenuCache(String userName) {
	    Record user = Db.findFirst("select * from user_login where user_name=?", userName);
	    if(user!=null){
            long user_id = user.getLong("id");
            Map cache = EedaMenuInterceptor.menuCache;
            if(cache!=null && cache.get(user_id)!=null){
                cache.remove(user_id);
            }
	    }
    }
	
	
	
	@Before(EedaMenuInterceptor.class)
	public void userPermissionRender(){
		String username = getPara("username");
		setAttr("username", username);
		render("/eeda/profile/userRole/userPermission.html");
	}
	
	//新建校验是否存在此员工名称
    public void checkCodeExist(){
    	String para= getPara("employee_name");
    	String sql = "select * from employee where employee_name = ?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }

	private Office getCurrentUserOffice() {
		String userName = currentUser.getPrincipal().toString();
		UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);
		Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));
		return parentOffice;
	}
	//查询员工名字
	public void searchEmployee(){
		String employee_name= getPara("employee_name");
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		List<Record> employeeList = Collections.EMPTY_LIST;
   		String sql = "SELECT *  FROM employee em WHERE office_id="+office_id;
   		if (employee_name.trim().length() > 0) {
            sql +=" and (em.employee_name like '%" + employee_name + "%' ) ";
        }
   		employeeList = Db.find(sql);
   		renderJson(employeeList);
	}
	
}
