package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.Office;
import models.ParentOfficeModel;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;
import models.eeda.profile.OfficeConfig;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.utils.Base64Utils;

import controllers.util.MD5Util;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class OfficeController extends Controller {
    private Log logger = Log.getLog(LoginUserController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
    public void index() {
    	OfficeConfig officeConfig = OfficeConfig.dao.findFirst("select * from office_config where office_id = ?",pom.getParentOfficeId());
    	List<Office> list = Office.dao.find("select * from office where belong_office = " + pom.getParentOfficeId());
    	setAttr("officeConfig", officeConfig);
    	if(list.size()>0){
    		setAttr("amount", list.size());
    	}else{
    		setAttr("amount", 0);
    	}
    	
        render("/yh/profile/office/office.html");
    }

    @Before(EedaMenuInterceptor.class)
    public void setting() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long officeId = user.getLong("office_id");
        Record officeRec = Db.findFirst("select * from office where  id=?", officeId);
        Record officeConfigRec = Db.findFirst("select * from office_config where office_id=?", officeId);
        setAttr("officeConfig", officeConfigRec);
        setAttr("office", officeRec);
        render("/eeda/profile/office/edit.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void department() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long officeId = user.getLong("office_id");
        List<Record> group1_list = Db.find("select trg.*, (select count(user_id) from t_rbac_ref_group_user trrgu where trrgu.group_id = trg.id) ppl_count"
                + " from t_rbac_group trg"
                + " where trg.is_delete!='Y' and trg.office_id=? and trg.parent_id is null",officeId);
        for(int i = 0;i<group1_list.size();i++){
            List<Record> group2_list = Db.find("select trg.*, (select count(user_id) from t_rbac_ref_group_user trrgu where trrgu.group_id = trg.id) ppl_count"
                    + " from t_rbac_group trg"
                    + " where trg.is_delete!='Y' and trg.office_id=? and trg.parent_id = ?",officeId,group1_list.get(i).getLong("id"));
            group1_list.get(i).set("group2_list", group2_list);
            for(int j = 0;j<group2_list.size();j++){
                List<Record> group3_list = Db.find("select trg.*,(select count(user_id) from t_rbac_ref_group_user trrgu where trrgu.group_id = trg.id) ppl_count"
                        + " from t_rbac_group trg"
                        + " where trg.is_delete!='Y' and trg.office_id=? and trg.parent_id = ?",officeId,group2_list.get(j).getLong("id"));
                group2_list.get(j).set("group3_list", group3_list);
                for(int k=0;k<group3_list.size();k++){
                    List<Record> group4_list = Db.find("select * from t_rbac_group where trg.is_delete!='Y' and trg.office_id=? and parent_id=?",officeId,group3_list.get(k).getLong("id"));
                    group3_list.get(k).set("group4_list", group4_list);
                }
            }
        }
        setAttr("group1_list", group1_list);
//        setAttr("user_type",user_type);
        render("/eeda/profile/office/department.html");
    }

    //保存公司信息
    public void save() {
        String id = getPara("id");
        
        String officeName = getPara("office_name");
        String officeDesc = getPara("office_desc");
        String officeSupport = getPara("office_support");
        Office office = Office.dao.findById(id);
        if(office!=null) {
            office.set("office_name", officeName);
            office.set("company_intro", officeDesc);
            office.set("office_support", officeSupport);
            office.update();
        }
        String systemTitle = getPara("system_title");
        String systemSubTitle = getPara("system_sub_title");
        Record officeConfig = Db.findFirst("select * from office_config where office_id=?", id);
        
        if(officeConfig==null) {
            officeConfig = new Record();
            officeConfig.set("system_title", systemTitle);
            officeConfig.set("system_sub_title", systemSubTitle);
            officeConfig.set("office_id", id);
            Db.save("office_config", officeConfig);
        }else {
            officeConfig.set("system_title", systemTitle);
            officeConfig.set("system_sub_title", systemSubTitle);
            Db.update("office_config", officeConfig);
        }
        
        renderText("ok");
    }
    
    //updateDepartmentName
    public void updateGroupName() {
        String group_id = getPara("group_id");
        String group_name = getPara("group_name");
        Record group = new Record();
        boolean result = false;
        if(StrKit.notBlank(group_id)){
            group = Db.findById("t_rbac_group", group_id);
            group.set("name", group_name);
            result = Db.update("t_rbac_group",group);
        }
        renderText("ok");
    }
    
    public void createGroup(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long officeId = user.getLong("office_id");
        String group_id = getPara("group_id");
        String group_name = getPara("group_name");
        Record group = new Record();
        boolean result = false;
        if(StrKit.notBlank(group_id)){
            group.set("name", group_name);
            group.set("parent_id", group_id);
            group.set("office_id", officeId);
            result = Db.save("t_rbac_group",group);
            group.set("result", result);
        }
        renderJson(group);
    }
    @Before(Tx.class)
    public void deleteGroup(){
        String id = getPara("id");
        
        boolean group_result = false;
        Db.update("delete from t_rbac_ref_group_user where group_id = ?", id);
        //删除部门前，先删除部门下的子部门
        int deleteGroupNum = 0;//删除部门下，子部门的数量
        List<Record> groupList = Db.find("select * from t_rbac_group where parent_id = ?", id);
        for(Record group : groupList){
            Db.update("delete from t_rbac_ref_group_user where group_id = ?",group.getLong("id"));
            Db.delete("t_rbac_group",group);
            deleteGroupNum++;
        }
        logger.debug("删除子部门数量: "+deleteGroupNum);
        Record group = Db.findById("t_rbac_group", id);
        group_result = Db.delete("t_rbac_group",group);
        renderJson("{\"result\":"+group_result+"}");
    }
    
    public void roleList(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long officeId = user.getLong("office_id");
        List<Record> roleList = Db.find("select * from t_rbac_role where is_delete!='Y' and office_id=?", officeId);
        List<Record> departmentList = Db.find("select * from t_rbac_group where is_delete!='Y' and office_id=?", officeId);
        Record rec = new Record();
        rec.set("role_list", roleList);
        rec.set("department_list", departmentList);
        renderJson(rec);
    }
    
    @Before(Tx.class)
    public void saveUser(){
        UserLogin userLogin = LoginUserController.getLoginUser(this);
        long officeId = userLogin.getLong("office_id");
        String user_id = getPara("user_id");
        String group_id = getPara("group_id");
        String new_group_id = getPara("department_id");
        String user_name = getPara("user_name");
        String password =  MD5Util.encode("SHA1",getPara("pwd"));
        String c_name = getPara("c_name");
//        String mobile = (String)dto.get("mobile");
        String role_id = getPara("role_id");
        
        Record user = new Record();
        Record user_role = new Record();
        
        boolean user_result = false;
        if(StrKit.notBlank(user_id)){
            user = Db.findById("user_login",user_id);
            user.set("user_name", user_name);
            user.set("c_name", c_name);
//            user.set("type", "system");
//            user.set("mobile", mobile);
            user_result = Db.update("user_login", user);
            
            Db.update("delete from t_rbac_ref_user_role where user_name = ?", user_name);
            
            user_role = new Record();
            user_role.set("role_id", role_id);
            user_role.set("user_name", user_name);
            Db.save("t_rbac_ref_user_role",user_role);
        }else{
          //查找用户名是否存在
            Record suerRe = Db.findFirst("select * from user_login where user_name = ?", user_name);
            if(suerRe!=null) {
                suerRe.set("reuslt", false);
                suerRe.set("msg", "用户名"+user_name+"已存在");
                renderJson(suerRe);
                return;
            }
            user.set("user_name", user_name);
            user.set("password", password);
            user.set("c_name", c_name);
//            user.set("mobile", mobile);
//            user.set("type", "system");
            user.set("office_id", officeId);
            user.set("create_time",new Date());
            user_result = Db.save("user_login", user);
            
            user_role.set("role_id", role_id);
            user_role.set("user_name", user_name);
            Db.save("t_rbac_ref_user_role",user_role);
        }
        boolean result = false;
        if(user_result){
            Db.update("delete from t_rbac_ref_group_user where user_id = ?", user.getLong("id"));
            Record re = new Record();
            if(StrKit.isBlank(new_group_id)) {
                re.set("group_id", group_id);
            }else {
                re.set("group_id", new_group_id);
            }
            re.set("user_id", user.getLong("id"));
            result = Db.save("t_rbac_ref_group_user", re);
        }
        Record resultRe = new Record();
        resultRe.set("result", result);
        renderJson(resultRe);
    }
    
    @Before(Tx.class)
    public void deleteUser(){
        String id = Base64Utils.decode(getPara("id"));
        if(id.indexOf("-")>0){
            id = id.substring(0,id.indexOf("-"));
        }
        boolean user_result = false;
        Db.update("delete from t_rbac_ref_group_user where user_id = ?", id);
        Db.update("delete from t_rbac_ref_user_role where user_id = ?", id);
        Record user = Db.findById("t_rbac_user", id);
        user_result = Db.delete("t_rbac_user",user);
        renderJson("{\"result\":"+user_result+"}");
    }
    
    public void updateUserPwd(){
        String user_id = Base64Utils.decode(getPara("user_id"));
        if(user_id.indexOf("-")>0){
            user_id = user_id.substring(0,user_id.indexOf("-"));
        }
        String new_pwd = getPara("new_pwd");
        String password =  MD5Util.encode("SHA1",new_pwd);
        int resultNumber = Db.update("update t_rbac_user set password=? where id = ?",password,user_id);
        renderJson("{\"resultNumber\":"+resultNumber+"}");
    }
    
    public void getDepartmentUser() {
        String group_id = getPara("group_id");
        List<Record> userList = Db.find("select ul.*, g.id group_id, g.name group_name, r.id role_id, r.name role_name from t_rbac_ref_group_user gu "
                + "left join user_login ul on gu.user_id=ul.id "
                + "left join t_rbac_group g on gu.group_id=g.id "
                + "left join t_rbac_ref_user_role ur on ur.user_name=ul.user_name "
                + "left join t_rbac_role r on ur.role_id=r.id "
                + "where gu.group_id=?", group_id);
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        
        orderListMap.put("draw", 0);
        orderListMap.put("recordsTotal", userList.size());
        orderListMap.put("recordsFiltered", userList.size());

        orderListMap.put("data", userList);

        renderJson(orderListMap);
    }
    // 添加分公司
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_CREATE, PermissionConstant.PERMSSION_O_UPDATE}, logical=Logical.OR)
    @Before(Tx.class)
    public void saveOffice() {
        /*
         * if (!isAuthenticated()) return;
         */
        String id = getPara("officeId");
        if (id != "") {
            UserLogin user = UserLogin.dao.findById(id);
        }
        Record office;
        if(id != null && id !=""){
        	office = Db.findById("office", id);
        }else{
        	office = new Record();
        }
        office.set("office_code", getPara("office_code"));
        office.set("office_name", getPara("office_name"));
        office.set("office_person", getPara("office_person"));
        office.set("phone", getPara("phone"));
        office.set("address", getPara("address"));
        office.set("email", getPara("email"));
        office.set("type", getPara("type"));
        office.set("company_intro", getPara("company_intro"));
        office.set("location", getPara("location"));
        office.set("abbr", getPara("abbr"));
        //判断当前是更新还是新建
        if (id != "") {
            Db.update("office", office);
        } else {
            //记录分公司的总公司
			String name = (String) currentUser.getPrincipal();
			//根据登陆用户获取公司的的父公司的ID
			UserOffice user_office = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",name,true);
			Office parentOffice = Office.dao.findFirst("select * from office where id = ?",user_office.get("office_id"));
			if(parentOffice.get("belong_office") != null && !"".equals(parentOffice.get("belong_office"))){
				office.set("belong_office",parentOffice.get("belong_office"));
			}else{
				office.set("belong_office",parentOffice.get("id"));
			}
			
			
	 		//创建用户是网点用户
	 		//Record rec = Db.findFirst("select belong_office  from office  where id = " + users.get(0).get("office_id"));
	 		//user.set("office_id", rec.get("belong_office"));
            
            Db.save("office", office);
            //自动将新的公司给是管理员的用户
            
            List<UserRole> urList = UserRole.dao.find("select * from t_rbac_ref_user_role ur left join user_login ul on ur.user_name = ul.user_name left join office o on o.id = ul.office_id  where role_code = 'admin' and (o.id = ? or o.belong_office = ?)",pom.getParentOfficeId(),pom.getParentOfficeId());
            if(urList.size()>0){
            	for (UserRole userRole : urList) {
                	UserOffice uo = new UserOffice();
                	uo.set("user_name", userRole.get("user_name"));
                	uo.set("office_id",office.get("id"));
                	uo.save();
    			}
            }
        }
        
        OfficeConfig officeConfig = OfficeConfig.dao.findFirst("select * from office_config where office_id = ?",pom.getParentOfficeId());
    	List<Office> list = Office.dao.find("select * from office where belong_office = " + pom.getParentOfficeId());
    	setAttr("officeConfig", officeConfig);
    	if(list.size()>0){
    		setAttr("amount", list.size());
    	}else{
    		setAttr("amount", 0);
    	}
        render("/yh/profile/office/office.html");

    }

    // 删除分公司
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_DELETE})
    public void del() {
        /*
         * UserLogin.dao.find("select * from user_login");
         * UserLogin.dao.deleteById(getParaToInt());
         */
        String id = getPara();
        if (id != null) {
        	
        	Office office = Office.dao.findById(id);
        	Object obj = office.get("is_stop");
            if(obj == null || "".equals(obj) || obj.equals(false) || obj.equals(0)){
            	office.set("is_stop", true);
            }else{
            	office.set("is_stop", false);
            }
            office.update();
        	
        }
        OfficeConfig officeConfig = OfficeConfig.dao.findFirst("select * from office_config where office_id = ?",pom.getParentOfficeId());
    	List<Office> list = Office.dao.find("select * from office where belong_office = " + pom.getParentOfficeId());
    	setAttr("officeConfig", officeConfig);
    	if(list.size()>0){
    		setAttr("amount", list.size());
    	}else{
    		setAttr("amount", 0);
    	}
        render("/yh/profile/office/office.html");
    }

    // 列出分公司信息
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_O_LIST})
    public void listOffice() {
        /*
         * Paging
         */
    	String address=getPara("address");
    	String person=getPara("person");
    	String name=getPara("name");
    	String type=getPara("type");
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null
                && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", "
                    + getPara("iDisplayLength");
        }
        
        ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
        
        Long parentID = pom.getParentOfficeId();
        Long parent_id = pom.getBelongOffice();
        
        String sql ="";
        String list_sql= "";
		// 获取总条数
        String totalWhere = "";
        
        if(parent_id == null || "".equals(parent_id) || currentUser.hasRole("admin")){
        	
        	sql = "select count(1) total from office where belong_office = " + parentID + " or id = " + parentID;
        	list_sql= "select * from office"
        	 		+ " where office_name  like '%"+name+"%'  and "
        	 		+ "office_person like '%"+person+"%' "
        	 		+ "and type  like '%"+type+"%' "
        	 		+ "and address  like '%"+address+"%' and (belong_office = " + parentID + " or id = " + parentID +") order by id desc " + sLimit;
        }else{
        	
        	sql = "select count(1) total from office where belong_office = " + parentID + " ";
        	list_sql= "select * from office"
        	 		+ " where office_name  like '%"+name+"%'  and "
        	 		+ "office_person like '%"+person+"%' "
        	 		+ "and type  like '%"+type+"%' "
        	 		+ "and address  like '%"+address+"%' and belong_office = " + parentID + " order by id desc " + sLimit;
        }
        Record rec = Db.findFirst(sql + totalWhere);
        logger.debug("total records:" + rec.getLong("total"));
        // 获取当前页的数据
       
        List<Record> orders = null;
        if(type==null&&name==null&&address==null&&person==null){
        	if(parent_id == null || "".equals(parent_id) || currentUser.hasRole("admin")){
        		orders = Db.find("select o.*,lc.name dname from office o left join location lc on o.location = lc.code where (o.belong_office = " + parentID + " or o.id = " + parentID + ") order by o.id desc" + sLimit);
        	}else{
        		orders = Db.find("select o.*,lc.name dname from office o left join location lc on o.location = lc.code where o.belong_office = " + parentID + " order by o.id desc" + sLimit);
        	}
        	
        }else{
        	 orders = Db.find(list_sql);
        }
       
        Map orderMap = new HashMap();
        orderMap.put("sEcho", pageIndex);
        orderMap.put("iTotalRecords", rec.getLong("total"));
        orderMap.put("iTotalDisplayRecords", rec.getLong("total"));
        orderMap.put("aaData", orders);

        renderJson(orderMap);
    }
    
    //查询分公司所有仓库
    public void findOfficeWarehouse(){
    	String office_id = getPara();// 调车单id
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
        String sqlTotal = "";
        Record rec = null;
        String sql = "";
    	if(office_id != ""){
	        sqlTotal = "select count(0) total from warehouse where office_id ="+office_id+";";
	        logger.debug("sql :" + sqlTotal);
	        rec = Db.findFirst(sqlTotal);
	        logger.debug("total records:" + rec.getLong("total"));
	
	        //String sql = "select * from warehouse where office_id ="+office_id+";";
	        sql = "select w.*,lc.name dname from warehouse w"
			+ " left join location lc on w.location = lc.code "
			+ " where w.office_id = "+office_id+" order by w.id desc " + sLimit;
    	}
    	List<Record> warehouseList = Db.find(sql);
    	Map Map = new HashMap();
    	Map.put("sEcho", pageIndex);
    	Map.put("iTotalRecords", rec.getLong("total"));
    	Map.put("iTotalDisplayRecords", rec.getLong("total"));
    	Map.put("aaData", warehouseList);
    	renderJson(Map); 
    }
    public void checkOfficeNameExist(){
    	boolean result = true;
    	String officeName= getPara("office_name");
    	String[] str =officeName.split(",");
    	Office office= Office.dao.findFirst("select * from office where office_name = '" + str[0] + "'");
    	if(office != null){
    		if(str.length == 1){
    			result = false;
    		}else{
    			logger.debug("1:"+str[0]+","+str[1]);
    			if(!str[0].equals(str[1])){
    				result = false;
    			}
    		}
    	}
    	renderJson(result);
    }
    

}
