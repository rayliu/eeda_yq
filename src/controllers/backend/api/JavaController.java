package controllers.backend.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.jfinal.plugin.ehcache.CacheKit;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class JavaController extends Controller{
	public void index() {
		setAttr("getListUrl", "/webadmin/java/getList");
		LogKit.info(getAttr("getListUrl").toString());
		
		render("/eeda/api/list.html");
	}
	
	public void edit(){
	   
		String id = getPara("id");
		Record order = Db.findFirst("select tja.*, u.c_name creator_name from t_eeda_api tja " +
				" left join user_login u on tja.creator=u.id where tja.id =?",id);

		setAttr("order", order);
		render("/eeda/api/edit.html");
	}
	
	
	public void delete() {
		Record login_user = getAttr("user");
		String order_id = getPara("id");
		boolean result = false;
		Record re = new Record();
		re = Db.findFirst("select * from t_eeda_api where id = "+order_id);
		if(re!=null){
			re.set("is_delete","Y");
			result = Db.update("t_jfa_api", re);
		}
		re.set("result", result);
		renderJson(re);
	}
	
	public void getList() {
		
		String condition = " ";         
		String name= getPara("input");         
		        
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
		if(StrKit.notBlank(name)){             
		    condition+=" and tja.name like '%"+name+"%'";         
		}         
		        
		Record login_user = getAttr("user");         
		String sql = "SELECT tja.id, tja.name, url, tja.type, creator, tja.create_time, version, is_active, schedule, " +
				"CHAR_LENGTH(codes) code_length, u.c_name creator_name "
				+ " FROM t_eeda_api tja "
				+ " left join user_login u on tja.creator=u.id"
				+ " where tja.is_delete='N' "+condition;
		List orderList = Db.find(sql+" order by tja.id desc "+sLimit);          
		String sqlTotal = "select count(1) total from (" + sql + condition + ") B";         
		Record rec = Db.findFirst(sqlTotal);          
		Map<String, Object> orderListMap = new HashMap<String, Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        renderJson(orderListMap);
		renderJson(orderListMap);
	}
	
	
	public void save() {
		Record login_user = getAttr("login_user");
		String id = getPara("id");
		String jsonStr = getPara("params");
		Gson gson = new Gson();
		Map dto = gson.fromJson(jsonStr, HashMap.class);
		String order_id = (String)dto.get("id");

		Record re = new Record();  	
		Iterator it = dto.keySet().iterator();
		while(it.hasNext()){
		    // 获得key
		    String key = (String) it.next(); 
		    String value = (String)dto.get(key);    
		    if(StrKit.isBlank(value)){
		        value = null;
		    }
		    re.set(key, value);
		}
		//test
		boolean result = false;
		if(StrKit.notBlank(order_id)){
		    result = Db.update("t_eeda_api", re);
		}else{
		    re.set("creator", login_user.getLong("id"));
		    re.set("create_time", new Date());
		    result = Db.save("t_eeda_api",re);
		}

		//如果是job, 保存时重新loadJobs, TODO: 需优化为load指定的job
		if(re.getStr("type").equals("job")){
//			RedisUtil.reloadJobs();
		}
		//保存成功后,清楚一次缓存
		CacheKit.removeAll("apiCache");

		re.set("result", result);
		renderJson(re);
	}

}
