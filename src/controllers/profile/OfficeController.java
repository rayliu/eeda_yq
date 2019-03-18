package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

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
            
            List<UserRole> urList = UserRole.dao.find("select * from user_role ur left join user_login ul on ur.user_name = ul.user_name left join office o on o.id = ul.office_id  where role_code = 'admin' and (o.id = ? or o.belong_office = ?)",pom.getParentOfficeId(),pom.getParentOfficeId());
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
