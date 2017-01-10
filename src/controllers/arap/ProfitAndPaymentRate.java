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
public class ProfitAndPaymentRate extends Controller {
	private Log logger = Log.getLog(ProfitAndPaymentRate.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/ProfitAndPaymentRate/ProfitAndPaymentRate.html");
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
        		+" SELECT jo.id,jo.customer_id,p.abbr,SUM(currency_total_amount) charge_rmb,"
        		+" (SELECT SUM(currency_total_amount) from  job_order_arap joa "
        		+" LEFT JOIN job_order jor on joa.order_id = jor.id "
        		+" WHERE joa.order_type = 'cost' and jor.customer_id = jo.customer_id "+condition
        		+" ) cost_rmb"
        		+"  from job_order jo "
        		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+"  WHERE jo.office_id ="+office_id+" and joa.order_type = 'charge' "+condition
        		+" GROUP BY jo.customer_id"
        		+" ) A where 1=1  ORDER BY abbr";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
	public void listTotal() {
		String customer_id =(String) getPara("customer");
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		String order_export_date=order_export_date_begin_time.replaceAll("_begin_time", "");
//		String condition = 
//				SELECT 
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 3 and jo.customer_id = 35533 
//				  and joa.order_type = 'charge' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) charge_cny,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 6 and jo.customer_id = 35533 
//				  and joa.order_type = 'charge' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) charge_usd,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 8 and jo.customer_id = 35533 
//				  and joa.order_type = 'charge' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) charge_jpy,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 9 and jo.customer_id = 35533 
//				  and joa.order_type = 'charge' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) charge_hkd,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 3 and jo.customer_id = 35533 
//				  and joa.order_type = 'cost' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) cost_cny,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 6 and jo.customer_id = 35533 
//				  and joa.order_type = 'cost' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) cost_usd,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 8 and jo.customer_id = 35533 
//				  and joa.order_type = 'cost' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) cost_jpy,
//				(SELECT 
//				IFNULL(SUM(joa.exchange_total_amount),0)
//				  from job_order jo 
//				  LEFT JOIN job_order_arap joa on jo.id = joa.order_id 
//				  LEFT JOIN party p on p.id = jo.customer_id
//				  WHERE jo.office_id = 1 and joa.exchange_currency_id = 9 and jo.customer_id = 35533 
//				  and joa.order_type = 'cost' and (jo.order_export_date BETWEEN '2016-11-01' AND '2016-11-31' )
//				) cost_hkd

		
		
		
	}
	
}
