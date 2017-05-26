package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.TradeItem;
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
public class TradeItemController extends Controller {
    private Log logger = Log.getLog(TradeItemController.class);
    
    //查询费用中文名称
    public void search() {
        String input = getPara("input");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
        List<Record> finItems = Collections.EMPTY_LIST;
        if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "select h.ref_id, f.id, f.name from user_query_history h, fin_item f "
                    + "where h.ref_id=f.id and h.type='ARAP_FIN' and h.user_id=?";
            finItems = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);
            if(finItems.size()==0){
                finItems = Db.find("select * from fin_item f where f.office_id=? and f.name like '%"+input+"%' "
                        + " order by convert(f.name using gb2312) asc limit 10", officeId);
            }
            renderJson(finItems);
        }else{
            if (input !=null && input.trim().length() > 0) {
                finItems = Db.find("select * from fin_item f where f.office_id=? and f.name like '%"+input+"%' "
                        + " order by convert(name using gb2312) asc limit 10", officeId);
            }else{
                finItems = Db.find("select * from fin_item f where f.office_id=? "
                        + "order by convert(name using gb2312) asc limit 10", officeId);
            }
            renderJson(finItems);
        }
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
        render("/eeda/profile/tradeItem/tradeItemList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/eeda/profile/tradeItem/tradeItemEdit.html");
    }
    

    public void list() {
    	
        String code = getPara("code");
        String name = getPara("name");
        String name_eng = getPara("name_eng");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "";
        if(code==null&&name==null&&name_eng==null){
        	sql = "SELECT * from trade_item t where 1 =1 and t.office_id="+officeId;
        }else{
        	sql = "SELECT id,code,name_eng,name,remark from fin_item f where 1 =1 and f.office_id="+officeId
        			+ " and code like '%"+code
        			+"%' and name like '%"+name
        			+"%' and name_eng like '%"+name_eng+"%'";
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
        Record tradeItem = Db.findFirst("SELECT * from trade_item t where id="+id);
        setAttr("order", tradeItem);
        
        render("/eeda/profile/tradeItem/tradeItemEdit.html");
        
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
        
        TradeItem r = null;
        
        String id = (String) dto.get("id");
        String commodity_name = (String) dto.get("commodity_name");
        String unit_name = (String) dto.get("unit_name");
        String unit_name_eng = (String) dto.get("unit_name_eng");
        String VAT_rate = (String) dto.get("VAT_rate");
        String rebate_rate = (String) dto.get("rebate_rate");
        String remark = (String) dto.get("remark");
        
        if (StringUtils.isBlank(id)) {
        	r = new TradeItem();
        	r.set("commodity_name", commodity_name);
        	r.set("unit_name", unit_name);
            r.set("unit_name_eng", unit_name_eng);
            if(!VAT_rate.equals("")){
            	Long.parseLong(VAT_rate);
            }
            if(!rebate_rate.equals("")){
            	Long.parseLong(rebate_rate);
            }
            r.set("VAT_rate", VAT_rate);
            r.set("rebate_rate",rebate_rate);
            r.set("remark", remark);
            r.set("office_id", officeId);
            r.save();
        } else {
        	r = TradeItem.dao.findById(id);
        	r.set("commodity_name", commodity_name);
        	r.set("unit_name", unit_name);
            r.set("unit_name_eng", unit_name_eng);
            r.set("VAT_rate", VAT_rate);
            r.set("rebate_rate", rebate_rate);
            r.set("remark", remark);           
            r.update();
        }
        renderJson(r);
    }
    
    //校验是否存在此商品名称
    public void checkCommodityNameExist(){
    	String para= getPara("commodity_name");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        long officeId = user.getLong("office_id");
        
    	String sql = "select * from trade_item where commodity_name = ? and office_id=?";
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
