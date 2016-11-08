package controllers.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

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
         		+ " select joa.id,joa.type,joa.bill_flag,joa.sp_id,ifnull(joa.total_amount,0) total_amount,joa.exchange_rate,ifnull(joa.currency_total_amount,0) currency_total_amount,"
         		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
         		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
         		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
         		+ " cur.name currency_name,joli.truck_type "
 				+ " from job_order_arap joa "
 				+ " left join job_order jo on jo.id=joa.order_id "
 				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
 				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
 				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
 				+ " left join party p on p.id=joa.sp_id "
 				+ " left join party p1 on p1.id=jo.customer_id "
 				+ " left join location l on l.id=jos.fnd "
 				+ " left join currency cur on cur.id=joa.currency_id "
 				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
 				+ " where joa.order_type='cost' and joa.audit_flag='Y' and joa.bill_flag='N' "
 				+ " and jo.office_id = "+office_id
 				+ " GROUP BY joa.id "
 				+ " ) B where 1=1 ";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
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
	
}
