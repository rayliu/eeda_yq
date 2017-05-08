package controllers.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.ParentOfficeModel;
import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.ParentOffice;


@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomerRemindController extends Controller{

	private Log logger = Log.getLog(CustomReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/customerRemind");
        setAttr("listConfigList", configList);
    	render("/eeda/report/customerRemindReport/list.html");
    }
    public void list1() {
    	String ADD = getPara();
    	setAttr("add",ADD);
        Long parentID = pom.getParentOfficeId();
        
        String sLimit = "";
        String customer_remind=getPara("customer_remind");
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " SELECT * FROM ( 	SELECT p.customer_remind days,  "
        		+" 			GROUP_CONCAT(DISTINCT p.abbr SEPARATOR '<br/>') group_abbr "
        		+" 		FROM job_order jor "
        		+" 		LEFT JOIN party p ON p.id = jor.customer_id "
        		+" 		LEFT JOIN office o ON o.id = p.office_id "
        		+" 		WHERE "
        		+" 		TIMESTAMPDIFF(DAY, (SELECT max(jor.create_stamp) from job_order  where id=jor.id),NOW()) >="+customer_remind
        		+" 		AND p.type = 'CUSTOMER' "
        		+" 		and p.customer_remind = "+customer_remind
        		+" 		and (o.id = " + parentID + " or o.belong_office = " + parentID + ") "
        		+" 	) A "
        		+" WHERE 1 = 1 ";
      
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
}
