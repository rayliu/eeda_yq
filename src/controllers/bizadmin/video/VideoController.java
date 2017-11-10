package controllers.bizadmin.video;

import java.io.File;
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
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class VideoController extends Controller {

	private Logger logger = Logger.getLogger(VideoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	public void modify_info(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	
	public void modify_pwd(){
        String id = getPara("id");
        render(getRequest().getRequestURI()+"/edit.html");
    }
	public void list(){
	    String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
		Long user_id = LoginUserController.getLoginUserId(this);
		String sqlTotal = "select count(1) total from video_case where creator = "+user_id ;
		Record rec = Db.findFirst(sqlTotal);
		String sql = "select * from video_case where  creator = "+user_id+" order by create_time desc "+sLimit;
		List<Record> orderList =  Db.find(sql);
	   	Map map = new HashMap();
	   	map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
	}
	
	public boolean deletePicture(String pic_name){
		String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\"+pic_name;
		File file = new File(filePath);
		boolean result = false;
		if(file.exists()&&file.isFile()){
			result = file.delete();
			result = true;
		}
		return result;
	}
	
	@Before(Tx.class)
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
	
	@Before(Tx.class)
	public void save(){
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);
        Record video = new Record();
        video.set("name", dto.get("name"));
        video.set("cover", dto.get("cover"));
        video.set("video_url", dto.get("video_url"));
        video.set("creator", userId);
        video.set("create_time", new Date());
        Db.save("video_case", video);
        renderJson(video);
	}
	
	@Before(Tx.class)
	public void delete(){
		String id=getPara("id");
		deletePicture(Db.findFirst("select * from video_case where id = "+id).getStr("cover"));
		Db.deleteById("video_case", id);
		renderJson(true);
	}
}
