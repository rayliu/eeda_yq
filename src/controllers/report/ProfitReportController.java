package controllers.report;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ProfitReportController extends Controller {
    private Log logger = Log.getLog(ProfitReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_PCO_LIST})
    public void index() {
    	render("eeda/statusReport/profitReport.html");
    }

    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
    	String sql = "select * from ("
    			+ " select jo.id,jo.order_no, jo.customer_id, jo.pieces, jo.gross_weight, jo.volume,p.abbr,"
    			+ " (select sum(ifnull(joa.currency_total_amount,0)) from job_order_arap joa where joa.order_type='cost' and joa.order_id = jo.id) cost,"
    			+ " (select sum(ifnull(joa.currency_total_amount,0)) from job_order_arap joa where joa.order_type='charge' and joa.order_id = jo.id) charge"
    			+ " from job_order jo"
    			+ " left join job_order_arap joa on joa.order_id = jo.id"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " )A where 1=1 ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
    }
    
    
}
