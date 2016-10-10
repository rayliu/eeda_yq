package controllers.report;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.UserLogin;

import org.apache.commons.lang.StringUtils;
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

import controllers.profile.LoginUserController;
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
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String customer_id = getPara("customer_id");
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        String date_type = getPara("date_type");
        
        String condition = "";
        String group_condition="";
        if(StringUtils.isNotEmpty(customer_id)){
        	condition += " and jo.customer_id = "+customer_id;
        }
        
        if("year".equals(date_type)){
        	if(StringUtils.isNotEmpty(begin_date)){
            	begin_date = begin_date+"-01-01 00:00:00";
            }else{
            	begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
            	end_date = end_date+"-12-31 23:59:59";
            }else{
            	end_date = "2037-12-31 23:59:59";
            }
            
            group_condition = " cast(year(jo.create_stamp) as char)";
        }else {
        	if(StringUtils.isNotEmpty(begin_date)){
            	begin_date = begin_date+"-01 00:00:00";
            }else{
            	begin_date = "1970-01-01 00:00:00";
            }
            
            if(StringUtils.isNotEmpty(end_date)){
            	end_date = end_date+"-31 23:59:59";
            }else{
            	end_date = "2037-12-31 23:59:59";
            }
            
            if("season".equals(date_type)){
            	group_condition = " (case"
            			+ " when cast(month(jo.create_stamp) as char)"
            			+ " between 1 and 3 "
            			+ " then cast(CONCAT(year(jo.create_stamp),'-','春季') as char)"
            			+ " when cast(month(jo.create_stamp) as char)"
            			+ " between 4 and 6 "
            			+ " then cast(CONCAT(year(jo.create_stamp),'-','夏季') as char)"
            			+ " when cast(month(jo.create_stamp) as char)"
            			+ " between 7 and 9 "
            			+ " then cast(CONCAT(year(jo.create_stamp),'-','秋季') as char)"
            			+ " when cast(month(jo.create_stamp) as char)"
            			+ " between 10 and 12 "
            			+ " then cast(CONCAT(year(jo.create_stamp),'-','冬季') as char)"
            			+ " end)";
            }else{
            	group_condition = " cast(CONCAT(year(jo.create_stamp),'-',month(jo.create_stamp)) as char)";
            }
            
        }
        
        condition += " and jo.create_stamp between '"+begin_date+"' and '"+end_date+"' "; 
        
        
    	String sql = "select create_stamp,customer_id,"
    			+ " sum(pieces) pieces,"
    			+ " sum(gross_weight) gross_weight, "
    			+ " sum(volume) volume, "
    			+ " customer_name,"
    			+ " sum(ocean_cost) ocean_cost, "
    			+ " sum(load_cost) load_cost, "
    			+ " sum(ari_cost) ari_cost, "
    			+ " sum(custom_cost) custom_cost, "
    			+ " sum(insurance_cost) insurance_cost, "
    			+ " sum(total_cost) total_cost, "
    			+ " sum(ocean_charge) ocean_charge, "
    			+ " sum(load_charge) load_charge, "
    			+ " sum(ari_charge) ari_charge, "
    			+ " sum(custom_charge) custom_charge, "
    			+ " sum(insurance_charge) insurance_charge, "
    			+ " sum(total_charge) total_charge"
    			+ " from ("
    			+ " select "+group_condition+" create_stamp, jo.customer_id, jo.pieces,"
    			+ " jo.gross_weight, jo.volume,p.abbr customer_name,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'cost' and joa.type = '海运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) ocean_cost,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'cost' and joa.type = '陆运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) load_cost,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'cost' and joa.type = '空运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) ari_cost,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'cost' and joa.type = '报关'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) custom_cost,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'cost' and joa.type = '保险'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) insurance_cost,"
    			+ " (select sum(ifnull(joa.currency_total_amount,0)) from job_order_arap joa "
    			+ " where joa.order_type='cost' and joa.order_id = jo.id"
    			+ " ) total_cost,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'charge' and joa.type = '海运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) ocean_charge,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'charge' and joa.type = '陆运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) load_charge,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'charge' and joa.type = '空运'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) ari_charge,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'charge' and joa.type = '报关'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) custom_charge,"
    			+ " ( SELECT sum( ifnull( joa.currency_total_amount, 0 ) ) FROM job_order_arap joa"
    			+ " WHERE joa.order_type = 'charge' and joa.type = '保险'"
    			+ " AND joa.order_id = jo.id"
    			+ " ) insurance_charge,"
    			+ " (select sum(ifnull(joa.currency_total_amount,0)) from job_order_arap joa "
    			+ " where joa.order_type='charge' and joa.order_id = jo.id"
    			+ " ) total_charge"
    			+ " from job_order jo"
    			+ " left join job_order_arap joa on joa.order_id = jo.id"
    			+ " left join party p on p.id = jo.customer_id"
    			+ " WHERE jo.office_id="+office_id
    			+ condition
    			+ " )A where 1=1"
    			+ " GROUP BY A.create_stamp,A.customer_id"
    			+ " ORDER BY A.customer_id,A.create_stamp"
    			+ "  ";
    	
    	//String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
    }
    
    
}
