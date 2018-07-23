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
public class InviterController extends Controller {

	private Logger logger = Logger.getLogger(InviterController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		 //获取地区
        String sql_loc="select loc.name city,lm.* from location_management lm "
        				+ "left join location loc on lm.code = loc.code";
        List<Record> locations = Db.find(sql_loc);
        setAttr("locations",locations);
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
        String location = getPara("location");
        
        String condition = "";
        if(StringUtils.isNotBlank(begin_date) || StringUtils.isNotBlank(end_date)){
        	if(StringUtils.isBlank(begin_date)){
        		begin_date = "2000-01-01" ;
        	}else{
        		begin_date = begin_date+"-01" ;
        	}
        	
        	if(StringUtils.isNotBlank(end_date)){
        		end_date = end_date + "-31";
        	}else{
        		end_date = "2037-01-01";
        	}
        	
        	condition = " and u.create_time between '" + begin_date + "' and '" + end_date + "'";
        }
        if(StringUtils.isNotBlank(location)){
        	condition += " and u.location like '%"+location+"%'";
        }
         
        String sql = "SELECT iv.*, com.company_name,"
        		+ " (select count(u.id) from user_login u "
        		+ " left join wc_company wc on wc.creator = u.id"
        		+ " where u.invitation_code = iv.invite_code "+condition+") invite_amount"
        		+ " FROM `wc_inviter` iv "
        		+ " LEFT JOIN user_login ul on ul.id = iv.user_id"
        		+ " LEFT JOIN wc_company com on com.creator = ul.id"
        		+ " where ul.system_type = '商家后台' and ul.is_delete != 'Y'"
        		+ " and iv.is_delete != 'Y'"
        		+ " group by iv.id";
    	

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
    
    @Before(Tx.class)
    public void update_remark(){
    	Record result = new Record();
    	String id = getPara("id");
    	String remark = getPara("remark");
    	
    	if(StringUtils.isNotBlank(id)){
    		Record order = Db.findById("wc_inviter", id);
    		order.set("remark", remark);
    		Db.update("wc_inviter",order);
    		
    		result.set("result", true);
    	}else{
    		result.set("result", false);
    	}
    	
    	renderJson(result);
    }

}
