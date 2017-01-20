package controllers.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostBalanceReportController extends Controller {
	private Log logger = Log.getLog(CostBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/CostBalanceReport/CostBalanceReport.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT * FROM ("
        		+" SELECT p.abbr ,joa.sp_id sp, ifnull(SUM(total_amount),0) cost_total,'CNY' currency,jo.order_export_date, "
        		+" 		ifnull((SELECT SUM(total_amount) "
        		+" 			from job_order jo  "
        		+" 			LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 			LEFT JOIN party p on p.id = joa.sp_id "
        		+" 			WHERE  exchange_currency_id = 3 AND joa.order_type = 'cost' AND pay_flag='Y'  "
        		+"             and joa.sp_id =sp and jo.office_id ="+ office_id + condition
        		+"  ),0) cost_confirm  from job_order jo  "
        		+" 	LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 	LEFT JOIN party p on p.id = joa.sp_id "
        		+" 	WHERE  exchange_currency_id = 3 AND joa.order_type = 'cost' and jo.office_id = "+ office_id + condition
        		+"          "
        		+"   GROUP BY sp_id "
        		+" union "
        		+" SELECT p.abbr ,joa.sp_id sp, ifnull(SUM(total_amount),0) cost_total,'USD' currency,jo.order_export_date, "
        		+" 		ifnull((SELECT SUM(total_amount) "
        		+" 			from job_order jo  "
        		+" 			LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 			LEFT JOIN party p on p.id = joa.sp_id "
        		+" 			WHERE  exchange_currency_id = 6 AND joa.order_type = 'cost' AND pay_flag='Y'  "
        		+"             and joa.sp_id =sp and jo.office_id = "+ office_id + condition
        		+"  ),0) cost_confirm  from job_order jo  "
        		+" 	LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 	LEFT JOIN party p on p.id = joa.sp_id "
        		+" 	WHERE  exchange_currency_id = 6 AND joa.order_type = 'cost' and jo.office_id ="+ office_id + condition
        		+"          "
        		+"   GROUP BY sp_id "
        		+" union "
        		+" SELECT p.abbr ,joa.sp_id sp, ifnull(SUM(total_amount),0) cost_total,'JPY' currency,jo.order_export_date, "
        		+" 		ifnull((SELECT SUM(total_amount) "
        		+" 			from job_order jo  "
        		+" 			LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 			LEFT JOIN party p on p.id = joa.sp_id "
        		+" 			WHERE  exchange_currency_id = 8 AND joa.order_type = 'cost' AND pay_flag='Y'  "
        		+"             and joa.sp_id =sp and jo.office_id = "+ office_id + condition
        		+"  ),0) cost_confirm  from job_order jo  "
        		+" 	LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
        		+" 	LEFT JOIN party p on p.id = joa.sp_id "
        		+" 	WHERE  exchange_currency_id = 8 AND joa.order_type = 'cost' and jo.office_id = "+ office_id + condition
        		+"          "
        		+"   GROUP BY sp_id "
        		+" union "
        		+" SELECT p.abbr ,joa.sp_id sp, ifnull(SUM(total_amount),0) cost_total,'HKD' currency,jo.order_export_date, "
        		+" 		ifnull((SELECT SUM(total_amount) "
        		+" 			from job_order jo  "
        		+" 			LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 			LEFT JOIN party p on p.id = joa.sp_id "
        		+" 			WHERE  exchange_currency_id = 9 AND joa.order_type = 'cost' AND pay_flag='Y'  "
        		+"             and joa.sp_id =sp and jo.office_id = "+ office_id + condition
        		+"  ),0) cost_confirm  from job_order jo  "
        		+" 	LEFT JOIN job_order_arap joa on jo.id=joa.order_id "
        		+" 	LEFT JOIN party p on p.id = joa.sp_id "
        		+" 	WHERE  exchange_currency_id = 9 AND joa.order_type = 'cost' and jo.office_id ="+ office_id + condition
        		+"   GROUP BY sp_id "	 
        		+" ) A where cost_total!=0 ORDER BY abbr";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition );
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}
