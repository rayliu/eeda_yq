package controllers.bizadmin.weddingcase;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.SetAttrLoginUserInterceptor;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CaseController extends Controller {

	private Logger logger = Logger.getLogger(CaseController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	public void list(){
	    String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
		Long user_id = LoginUserController.getLoginUserId(this);
		String sqlTol="select count(1) total from wc_case where creator = "+user_id+" order by create_time desc "+sLimit;
		Record rec=Db.findFirst(sqlTol);
		String sql="select * from wc_case where creator = "+user_id;
		List example_list=Db.find(sql);
		Map map=new HashMap();
	    map.put("recordsTotal", rec.getLong("total"));
	    map.put("recordsFiltered", rec.getLong("total"));
		map.put("data", example_list);
		renderJson(map);
	}
	public void modify_info(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void modify_pwd(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	public void saveFile(){
		Record re = new Record();
    	try {
            UploadFile file = getFile();
            re.set("name", file.getFileName());
        } catch (Exception e) {
            e.getMessage();
        }
    	renderJson(re);
	}
	public void save(){
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);
        Record example = new Record();
        example.set("name", dto.get("name"));
        example.set("picture_name", dto.get("picture"));
        example.set("creator",userId);
        example.set("create_time",new Date());
        Db.save("wc_case", example);
        renderJson(example);
	}
	public void delete(){
		String id=getPara("id");
		Db.deleteById("wc_case", id);
		renderJson(true);
	}
}
