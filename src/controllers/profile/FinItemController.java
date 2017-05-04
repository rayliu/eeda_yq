package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.profile.FinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class FinItemController extends Controller {
    private Log logger = Log.getLog(FinItemController.class);
    
    //查询费用中文名称
    public void search() {
        String input = getPara("input");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
        List<Record> finItems = Collections.EMPTY_LIST;
        if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "SELECT * from( SELECT	h.ref_id,h.query_stamp,	f.id,	f. NAME,c.id currency_id,c.`code` currency_code,cr.rate "
					+" FROM	user_query_history h "
					+" LEFT JOIN	fin_item f on h.ref_id = f.id "
					+" LEFT JOIN currency c on c.id=f.binding_currency and c.office_id=f.office_id "
					+" LEFT JOIN currency_rate cr on cr.currency_id =c.id and cr.office_id=c.office_id "
                    + " where h.ref_id=f.id and h.type='ARAP_FIN' and h.user_id=? ) B"
                    + " where currency_id is not null or currency_code is not null or rate is not null ";
            finItems = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);
            if(finItems.size()==0){
                finItems = Db.find("SELECT	f.*,c.id currency_id,c.`code` currency_code,cr.rate FROM 	fin_item f "
						+" LEFT JOIN currency c on c.office_id = f.office_id		AND c.`code` = f.binding_currency "
						+" LEFT JOIN currency_rate cr ON cr.currency_id = c.id		AND cr.office_id = c.office_id "
						+" WHERE	f.office_id = ?  "
                		+ " and f.name like '%"+input+"%' "
                        + " order by convert(f.name using gb2312) asc limit 10", officeId);
            }
        }else{
            if (input !=null && input.trim().length() > 0) {
                finItems = Db.find("SELECT	f.*,c.id currency_id,c.`code` currency_code,cr.rate FROM 	fin_item f "
						+" LEFT JOIN currency c on c.office_id = f.office_id		AND c.`code` = f.binding_currency "
						+" LEFT JOIN currency_rate cr ON cr.currency_id = c.id		AND cr.office_id = c.office_id "
						+" WHERE	f.office_id = ?  "
                		+ " and f.name like '%"+input+"%' "
                        + " order by convert(f.name using gb2312) asc limit 10", officeId);
            }else{
                finItems = Db.find("SELECT	f.*,c.id currency_id,c.`code` currency_code,cr.rate FROM 	fin_item f "
						+" LEFT JOIN currency c on c.office_id = f.office_id		AND c.`code` = f.binding_currency "
						+" LEFT JOIN currency_rate cr ON cr.currency_id = c.id		AND cr.office_id = c.office_id "
						+" WHERE	f.office_id = ?   "
                        + "order by convert(f.name using gb2312) asc limit 10", officeId);
            }
            
        }
        
        renderJson(finItems);
    }
    
    //查询费用英文名称
    public void search_eng() {
    	String input = getPara("input");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
    	List<Record> finItems = null;
    	if (input !=null && input.trim().length() > 0) {
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f"
    		        + " where f.office_id=? and f.name_eng like '%"+input+"%'", officeId);
    	}else{
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f where f.office_id=? ", officeId);
    	}
    	renderJson(finItems);
    }
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/eeda/profile/finItem/finItemList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
        
        List<Record> currency = Db.find("select * from currency where office_id=?",officeId);
		setAttr("currencyList", currency);
        render("/eeda/profile/finItem/finItemEdit.html");
    }
    

    public void list() {
    	
        String code = getPara("code");
        String name = getPara("name");
        String name_eng = getPara("name_eng");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        String	office_id="";
        String que_code="";
        String que_name="";
        String que_name_eng="";
        if(code !=null && code.trim().length() > 0){
        	que_code= " and code like '%"+code+"%' ";
        }
        if(name !=null && name.trim().length() > 0){
        	que_name= " and name like '%"+name+"%' ";
        }
        if(name_eng !=null && name_eng.trim().length() > 0){
        	que_name_eng= " and name_eng like '%"+name_eng+"%' ";
        }
        office_id="and f.office_id="+officeId;
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "";
        if(code==null&&name==null&&name_eng==null){
        	sql = "SELECT * from fin_item f where 1 =1 "+office_id;
        }else{
        	sql = "SELECT * from fin_item f where 1 =1 "+office_id
        			+que_code
        			+que_name
        			+que_name_eng;
        }

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql + " order by id desc " +sLimit);
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap); 
    }

    // 编辑条目按钮
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        FinItem u = FinItem.dao.findById(id);
        setAttr("order", u);
        UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
        
        List<Record> currency = Db.find("select * from currency where office_id=?",officeId);
		setAttr("currencyList", currency);
        render("/eeda/profile/finItem/finItemEdit.html");
        
    }

    // 删除条目
    public void delete() {
        String id = getPara();
        if (id != null) {
        	FinItem l = FinItem.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/finItem");
    }

    // 添加编辑保存
//    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
//            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
        String jsonStr=getPara("params");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        FinItem r = null;
        
        String id = (String) dto.get("id");
        String code = (String) dto.get("code");
        String name = (String) dto.get("name");
        String name_eng = (String) dto.get("name_eng");
        String binding_currency = (String) dto.get("binding_currency");
        String remark = (String) dto.get("remark");
        
        if (StringUtils.isBlank(id)) {
        	r = new FinItem();
        	r.set("code", code);
        	r.set("name", name);
            r.set("name_eng", name_eng);
            r.set("binding_currency", binding_currency);
            r.set("remark", remark);
            r.set("office_id", officeId);
            r.save();
        } else {
        	r = FinItem.dao.findById(id);
        	r.set("code", code);
        	r.set("name", name);
            r.set("name_eng", name_eng);
            r.set("binding_currency", binding_currency);
            r.set("remark", remark);            
            r.update();
        }
        renderJson(r);
    }
    
    //校验是否存在此费用
    public void checkCodeExist(){
    	String para= getPara("code");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
    	String sql = "select * from fin_item where code = ? and office_id=?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql,para, officeId);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameExist(){
    	String para= getPara("name");
    	UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
    	String sql = "select * from fin_item where name = ? and office_id=?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql, para, officeId);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameEngExist(){
    	String para= getPara("name_eng");
    	UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
    	String sql = "select * from fin_item where name_eng =? and office_id=?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql, para, officeId);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    
}
