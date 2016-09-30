package controllers.msg;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MsgBoardController extends Controller {

	private Logger logger = Logger.getLogger(MsgBoardController.class);
	Subject currentUser = SecurityUtils.getSubject();

//	@RequiresPermissions(value = { PermissionConstant.PERMISSION_TO_LIST })
	public void index() {
		render("/eeda/msgBoard/msgBoardList.html");
	}
	
    @Before(Tx.class)
   	public void save() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	Record r= new Record();
        r.set("title", title);
        r.set("content", content);
        r.set("create_stamp", new Date());
        r.set("creator", LoginUserController.getLoginUserId(this));
        Db.save("msg_board", r);
        renderJson("{\"result\":true}");
   	}
    
    public void list(){
    	 String sLimit = "";
         String pageIndex = getPara("draw");
         if (getPara("start") != null && getPara("length") != null) {
             sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
         }
         
    	String sql = "select * from ("
    			+ " select m.*, u.c_name create_name, u1.c_name update_name"
        		+ " from msg_board m "
        		+ " left join user_login u on u.id = m.creator"
        		+ " left join user_login u1 on u1.id = m.updator"
        		+ " ) A where 1=1 ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    public void edit(){
    	
    }

}
