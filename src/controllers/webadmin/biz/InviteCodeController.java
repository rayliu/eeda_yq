package controllers.webadmin.biz;

import freemarker.template.utility.StringUtil;
import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang3.StringUtils;
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
public class InviteCodeController extends Controller {

	private Logger logger = Logger.getLogger(InviteCodeController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}

    public void list(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        
        String condition = "";
        if(StringUtils.isNotBlank(begin_date) || StringUtils.isNotBlank(end_date)){
        	if(StringUtils.isBlank(begin_date)){
        		begin_date = "2000-01-01" ;
        	}
        	
        	if(StringUtils.isNotBlank(end_date)){
        		end_date += " 23:59:59";
        	}else{
        		end_date = "2037-01-01";
        	}
        	
        	condition = " and create_time between '" + begin_date + "' and '" + end_date + "'";
        }
         
        String sql = "SELECT  ul.influence invite_count,"
        		+ " cat.`name` category_name, "
        		+ " ifnull(com.c_name,com.company_name) compnay_name,"
        		+ " ul.invitation_code, "
        		+ " ul.phone "
        		+ " FROM `user_login` ul "
        		+ " LEFT JOIN wc_company com on com.creator = ul.id "
        		+ " LEFT JOIN category cat on cat.id = com.trade_type "
        		+ " where ul.system_type = '商家后台' and ul.is_delete != 'Y' order by ul.id desc";
    	

        String sqlTotal = "select count(1) total from ("+sql+ ") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }

}
