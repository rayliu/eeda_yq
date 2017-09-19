package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.profile.CurrencyRate;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.ParentOffice;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CurrencyRateController extends Controller {
    private Log logger = Log.getLog(CurrencyRateController.class);
    Subject currentUser = SecurityUtils.getSubject();
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/profile/currencyRate/list.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/currencyRate/edit.html");
    }

    // 编辑
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("currency_rate", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
        if (id != null) {
            CurrencyRate currency = CurrencyRate.dao.findById(id);
            setAttr("order", currency);
        }
        render("/profile/currencyRate/edit.html");

    }

    // 添加金融账户
    @Before(Tx.class)
    public void save() {
        String jsonStr=getPara("params");
        
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
      //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
        CurrencyRate order = new CurrencyRate();
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            //update
            order = CurrencyRate.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.set("office_id",office_id);
            order.update();
        } else {
            //create 
            DbUtils.setModelValues(dto, order);
            order.set("creator",LoginUserController.getLoginUserId(this));
            order.set("create_stamp",new Date());
            order.set("office_id",office_id);
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
    }

    // 删除
    public void delete() {
        String id = getPara("id");
        CurrencyRate currency = null;
        if (id != null) {
        	currency = CurrencyRate.dao.findById(id);
        	String is_stop = currency.getStr("is_stop");
        	if("Y".equals(is_stop)){
        		currency.set("is_stop", "N");
        	}else{
        		currency.set("is_stop", "Y");
        	}
        	currency.update(); 
        }
        renderJson(currency);
    }
    
    //查找相关货币的信息
    public void searchCurrency(){
    	String currency_id = getPara("currency_id");
    	if(StringUtils.isNotBlank(currency_id)){
    		String sql = "select * from currency_rate where currency_id = "+currency_id+" "
    				+ "and to_stamp in ( select max(to_stamp) "
    				+ "from currency_rate GROUP BY currency_id)";
    		Record re = Db.findFirst(sql);
    		renderJson(re);
    	}else{
    		renderJson(false);
    	}
    }

    // 列出
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
      //获取office_id
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }

        String sql = "select cu.*,ul.c_name creator_name from currency_rate cu"
        		+ " left join user_login ul on ul.id = cu.creator"
        		+ " where 1 = 1 and cu.office_id = "+office_id;
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orders = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", rec.getLong("total"));
        orderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderListMap.put("aaData", orders);

        renderJson(orderListMap); 
    }
    
}
