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

import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TotalProfitController extends Controller {
	private Log logger = Log.getLog(TotalProfitController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/ProfitAndPaymentRate/TotalProfit.html");
	}
	
	public long list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String conditions = "";
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String customer_id =getPara("customer");
    	if(StringUtils.isNotEmpty(getPara("customer"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,customer_id,"CUSTOMER");
    	}
        
        String customer = getPara("customer");
        String begin_time = getPara("order_export_date_begin_time");
        String end_time = getPara("order_export_date_end_time");
        if(StringUtils.isNotBlank(customer)){
        	conditions += " and jo.customer_id = "+ customer;
        }
        
        if(StringUtils.isBlank(begin_time)){
        	begin_time = " 2000-1-1";
        }
        if(StringUtils.isBlank(end_time)){
        	end_time = " 2037-1-1";
        }else{
        	end_time = end_time + " 23:59:59";
        }
        conditions += " and (jo.order_export_date between '"+begin_time+"' and '"+end_time+"')";
        	
        
        String sql = " SELECT A.date_time,"
        		+ " ifnull(SUM(charge_rmb),0) charge_rmb,"
        		+ " ifnull(sum(cost_rmb),0) cost_rmb "
        		+ " FROM ("
        		+" SELECT cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char) date_time,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from job_order jo "
        		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+"  WHERE p.office_id ="+office_id+" and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ") " 
        		+ conditions
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1  GROUP BY A.date_time";
		
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        long total = rec.getLong("total");
        List<Record> orderList = Db.find(sql);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		return total;
	}
	
	public void listTotal() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
		
        String conditions = "";
        String customer = getPara("customer");
        String begin_time = getPara("order_export_date_begin_time");
        String end_time = getPara("order_export_date_end_time");
        if(StringUtils.isNotBlank(customer)){
        	conditions += " and jo.customer_id = "+ customer;
        }
        
        if(StringUtils.isBlank(begin_time)){
        	begin_time = " 2000-1-1";
        }
        if(StringUtils.isBlank(end_time)){
        	end_time = " 2037-1-1";
        }else{
        	end_time = end_time + " 23:59:59";
        }
        conditions += " and (jo.order_export_date between '"+begin_time+"' and '"+end_time+"')";

        String sql = " SELECT "
        		+ " ifnull(SUM(charge_rmb),0) total_charge,"
        		+ " ifnull(sum(cost_rmb),0) total_cost "
        		+ " FROM ("
        		+" SELECT cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char) date_time,"
	    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
	    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
        		+"  from job_order jo "
        		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
        		+"  LEFT JOIN party p on p.id = jo.customer_id"
        		+"  WHERE p.office_id ="+office_id+" and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ") " 
        		+ conditions
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 ";
		
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}
	
	//导出excel对账单
		public void downloadExcelList(){
			String exportName = "";
		    UserLogin user = LoginUserController.getLoginUser(this);
	        long office_id=user.getLong("office_id");
	        
	        String ref_office = "";
	        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
	        if(relist!=null){
	        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
	        }
	        String conditions = "";
	        String customer = getPara("customer");
	        String begin_time = getPara("order_export_date_begin_time");
	        String end_time = getPara("order_export_date_end_time");
	        if(StringUtils.isNotBlank(customer)){
	        	conditions += " and jo.customer_id = "+ customer;
	        	Record re = Db.findById("party", customer);
	        	String customer_name = re.getStr("abbr");
	        	exportName = customer_name + "-";
	        }
	        
	        if(StringUtils.isBlank(begin_time)){
	        	begin_time = " 2000-1-1";
	        }
	        if(StringUtils.isBlank(end_time)){
	        	end_time = " 2037-1-1";
	        }else{
	        	end_time = end_time + " 23:59:59";
	        }
	        conditions += " and (jo.order_export_date between '"+begin_time+"' and '"+end_time+"')";
	        
	        

	        
	        String sqlExport = " SELECT A.id,A.date_time,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,"
	        		+ " sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
	        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb "
	        		+ " FROM ("
	        		+" SELECT jo.id,cast(CONCAT(year(jo.order_export_date),'-',month(jo.order_export_date)) as char) date_time,"
	        		+" IF(joa.order_type='charge' and joa.currency_id = 3,total_amount,0) charge_cny,"
	        		+"	IF(joa.order_type='charge' and joa.currency_id = 6,total_amount,0) charge_usd,"
	        		+"	IF(joa.order_type='charge' and joa.currency_id = 8,total_amount,0) charge_jpy,"
		    		+"	IF(joa.order_type='charge' and joa.currency_id = 9,total_amount,0) charge_hkd,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 3,total_amount,0) cost_cny,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 6,total_amount,0) cost_usd,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 8,total_amount,0) cost_jpy,"
		    		+"	IF(joa.order_type='cost' and joa.currency_id = 9,total_amount,0) cost_hkd,"
		    		+"	if(joa.order_type='charge',currency_total_amount,0) charge_rmb,"
		    		+"	if(joa.order_type='cost',currency_total_amount,0) cost_rmb"
	        		+"  from job_order jo "
	        		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
	        		+"  LEFT JOIN party p on p.id = jo.customer_id"
	        		+"  WHERE p.office_id ="+office_id+" and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ") " 
	        		+ conditions
	        		+ " and jo.delete_flag = 'N'"
	    			+" ) A where 1=1  GROUP BY A.date_time";
			
			String total_name_header = "日期,应收折合(CNY),应付折合(CNY),利润(CNY),利润率(%)";
			String[] headers = total_name_header.split(",");
			
			
			String[] fields = { "DATE_TIME", "CHARGE_RMB", "COST_RMB", "PROFIT", "PROFIT_RATE"};
			String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName + "总利润表");
			renderText(fileName);
		} 
	
}
