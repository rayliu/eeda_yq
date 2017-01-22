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
public class ArapReportController extends Controller {
	private Log logger = Log.getLog(ArapReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/ArapReport/ArapReport.html");
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
        String sql = "select * from(  "
         		+ " select joa.id,joa.type,joa.order_type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.charge_id fin_item_id,"
         		+" (CASE WHEN joa.pay_flag='Y' THEN '费用已结算' WHEN joa.create_flag='Y' THEN '费用已创建申请单' WHEN joa.billConfirm_flag='Y' THEN '费用已创建对账单并已确认'" 
         		+" WHEN joa.bill_flag='Y' THEN '费用已创建对账单' WHEN joa.audit_flag='Y' THEN '费用已确认' ELSE '费用未确认' end) flag,"
         		+ " joa.exchange_rate,joa.exchange_currency_rate,ifnull(joa.currency_total_amount,0) currency_total_amount,ifnull(joa.exchange_total_amount,0) exchange_total_amount,"
         		+ " jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id, "
         		+ " p.abbr sp_name,p1.abbr customer_name, "
         		+ " f.name fin_name,cur1.name exchange_currency_name, "
         		+ " cur.name currency_name "
 				+ " from job_order_arap joa "
 				+ " left join job_order jo on jo.id=joa.order_id "
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.customer_id "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
 				+ " left join fin_item f on f.id=joa.charge_id "
 				+ " where  jo.office_id = "+office_id
 				+ " GROUP BY joa.id "
 				+ " ) B where 1=1 ";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by order_export_date desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}
