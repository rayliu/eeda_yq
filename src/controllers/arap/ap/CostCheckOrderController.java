package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.eeda.oms.jobOrder.JobOrderArap;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostCheckOrderController extends Controller {
	private Log logger = Log.getLog(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@RequiresPermissions(value = { PermissionConstant.PERMSSION_CCOI_LIST })
	public void index() {
		render("/eeda/arap/CostCheckOrder/CostCheckOrder.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "select * from(  "
        		+ " select joa.*,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.total_costRMB, "
        		+ " p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination, "
        		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
				+ " left join party p1 on p1.id=joa.sp_id "
				+ " left join location l on l.id=jos.fnd "
				+ " where joa.order_type='cost' and joa.audit_flag='Y' "
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
	
	public void costCheckConfirm(){
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
			joa.set("bill_flag", "Y");
			joa.update();
		}
		renderJson("{\"result\":true}");
	}
	
	public void edit(){
		
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
}
