package controllers.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountAging extends Controller {
	private Log logger = Log.getLog(AccountAging.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/AccountAging/AccountAgingList.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sp_id = getPara("sp_id");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = "";
        if(StringUtils.isNotEmpty(sp_id)){
        	condition = " and acao.sp_id = '"+sp_id+"'";
        }
        
        
        String sql = " select abbr_name,currency_name,sum(total_amount) total_amount,"
        		+" sum(three) three,sum(six) six,sum(nine) nine "
        		+" from(SELECT"
        		+" 		acao.order_no,acao.sp_id,p.abbr abbr_name,cur.name currency_name,"
        		+" joa.total_amount,"
        		+" if(datediff(curdate(),jor.order_export_date)<=30,joa.total_amount,0) three,"
        		+" if(datediff(curdate(),jor.order_export_date)<=60,joa.total_amount,0) six,"
        		+" if(datediff(curdate(),jor.order_export_date)<=90,joa.total_amount,0) nine"
        		+" 		FROM"
        		+" 			arap_charge_application_order acao"
        		+" 		LEFT JOIN charge_application_order_rel caor on caor.application_order_id = acao.id"
        		+" 		LEFT JOIN job_order_arap joa on joa.id = caor.job_order_arap_id"
        		+"      LEFT JOIN job_order jor on jor.id = joa.order_id"
        		+" 		LEFT JOIN party p ON acao.sp_id = p.id"
        		+" 		LEFT JOIN currency cur on  cur.id = joa.currency_id"
        		+" 		WHERE"
        		+" 			acao.office_id = "+office_id
        		+" 		and acao.status != '已收款'"
        		+ condition
        		+" 		GROUP BY joa.id"
        		+"    ) a"
        		+" GROUP BY "
        		+" abbr_name,currency_name";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}
