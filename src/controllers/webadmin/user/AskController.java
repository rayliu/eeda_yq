package controllers.webadmin.user;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AskController extends Controller {

	private Logger logger = Logger.getLogger(AskController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
	    //对应action
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void details(){
	    String id = getPara("id");
	    String question = "SELECT wq.id,wq.title,SUBSTR(wq.create_time,1,19) create_time,ul.user_name"
	    		+ " FROM wc_question wq "
	    		+ " left join user_login ul on ul.id = wq.creator"
	    		+ " where wq.id = ?";
	    Record questionRe = Db.findFirst(question, id);
	    
	    String response = "SELECT wr.id,SUBSTR(wr.create_time,1,19) create_time,wr.value,ul.user_name,ul.wedding_date"
	    		+ " FROM wc_response wr "
	    		+ " left join user_login ul on ul.id = wr.creator"
    			+ " where wr.question_id =?";
	    List<Record> responseList = Db.find(response, id);
	    setAttr("question", questionRe);
	    setAttr("responseList", responseList);
	    render("/WebAdmin/user/ask/details.html");
	}
	 
    public void list(){
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select * from ("
    			+ " select wc_q.id,wc_q.create_time,wc_q.title,"
    			+ " (select COUNT(id) from wc_response wc_r where wc_r.question_id=wc_q.id) reply_number "
    			+ " from wc_question wc_q) A "
    			+ " where 1=1 ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_time desc " +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    public void deleteQuestion(){
    	String id = getPara("id");
    	Record  question = Db.findById("wc_question", id);
    	List<Record>  responseList = Db.find("select*from wc_response where question_id = ?",id);
    	Db.delete("wc_question", question);
    	for (Record record : responseList) {
			Db.delete("wc_response", record);
		}
    	renderJson("{\"result\":true}");
    }

    public void deleteResponse(){
    	String id = getPara("id");
    	Record r= Db.findById("wc_response", id);
    	boolean result = Db.delete("wc_response", r);
    	renderJson("{\"result\":"+result+"}");
    }
}
