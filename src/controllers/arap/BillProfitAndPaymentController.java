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

import controllers.eeda.ListConfigController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class BillProfitAndPaymentController extends Controller {
	private Log logger = Log.getLog(BillProfitAndPaymentController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/billProfitAndPayment");
		setAttr("listConfigList", configList);
		render("/eeda/arap/BillProfitAndPayment/BillProfitAndPayment.html");
	}
	
	public void list() {
		String checked = getPara("checked");
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        
        String customer_id = getPara("customer_id");
        Long userId = LoginUserController.getLoginUserId(this);
        if(StringUtils.isNotEmpty(getPara("customer_id"))){
    		//常用party查询保存进入历史记录
          	JobOrderController.addHistoryRecord(userId,customer_id,"ARAP_COM");
    	}
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = " SELECT * FROM ("
            		+" SELECT jo.id,jo.customer_id,jo.order_no,jo.order_export_date,p.abbr , "
            		+" SUM( IF (order_type = 'charge',currency_total_amount,0)) charge_rmb,"
            		+" SUM(IF (order_type = 'cost',currency_total_amount,0	)) cost_rmb"
            		+"  from job_order jo "
            		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
            		+"  LEFT JOIN party p on p.id = jo.customer_id"
            		+"  WHERE 1=1 and (jo.office_id="+office_id+ ref_office+ ") "+condition
            		+ " and jo.delete_flag = 'N'"
    				+" GROUP BY jo.id"
            		+" ) A where 1=1 and (charge_rmb-cost_rmb)<0";
        }else{
        	sql = " SELECT * FROM ("
            		+" SELECT jo.id,jo.customer_id,jo.order_no,jo.order_export_date,p.abbr ,"
            		+" SUM( IF (order_type = 'charge',currency_total_amount,0)) charge_rmb,"
            		+" SUM(IF (order_type = 'cost',currency_total_amount,0	)) cost_rmb"
            		+"  from job_order jo "
            		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
            		+"  LEFT JOIN party p on p.id = jo.customer_id"
            		+"  WHERE 1=1 and (jo.office_id="+office_id+ ref_office+ ") "+condition
            		+ " and jo.delete_flag = 'N'"
    				+" GROUP BY jo.id"
            		+" ) A where 1=1  ";
        }
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+" ORDER BY abbr "+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
        final long totalBack = rec.getLong("total");
		
	}
	
	public void listTotal() {
		String checked = getPara("checked");
		String customer =(String) getPara("customer");
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
		String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
		
        String customerName = "";
        if(customer==null||StringUtils.isBlank(customer)){
        	customerName="";
        }else{
        	customerName =" and customer_id ="+customer;
        }
		if(order_export_date_begin_time==null){
			order_export_date_begin_time="";
		}
		if(order_export_date_end_time==null){
			order_export_date_end_time="";
		}
		
		String order_export_date =  " and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"')";

		if(order_export_date_begin_time==""||order_export_date_begin_time==""){
			order_export_date="";
		}
		String condition = customerName+order_export_date;
		
		String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	 sql = "SELECT*,ROUND(SUM(charge_rmb),2) charge_total,"
        	 	 + " ROUND(SUM(cost_rmb),2) cost_total,"
        	 	 + " ROUND(SUM(charge_rmb)-SUM(cost_rmb),2) total_profit,"
        	 	 + " ROUND(((SUM(charge_rmb)-SUM(cost_rmb))/SUM(charge_rmb))*100,2) total_profit_rate"
           	 	 + " FROM (SELECT jo.id,jo.customer_id,jo.order_no,p.abbr customer_name,jo.order_export_date,"
           	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
           	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
           	 	 + " FROM job_order jo"
           	 	 + " LEFT JOIN job_order_arap joa ON joa.order_id = jo.id"
           	 	 + " LEFT JOIN party p ON p.id = jo.customer_id"
           	 	 + " WHERE jo.delete_flag = 'N' and (jo.office_id="+office_id+ ref_office+ ")"
           	 	 + " GROUP BY jo.id ) A WHERE 1 = 1 "+condition+" AND (charge_rmb - cost_rmb) < 0 ORDER BY customer_name";
        }else{
        	 sql = "SELECT*,ROUND(SUM(charge_rmb),2) charge_total,"
        	 	 + " ROUND(SUM(cost_rmb),2) cost_total,"
        	 	 + " ROUND(SUM(charge_rmb)-SUM(cost_rmb),2) total_profit,"
        	 	 + " ROUND(((SUM(charge_rmb)-SUM(cost_rmb))/SUM(charge_rmb))*100,2) total_profit_rate"
    			 + " FROM (SELECT jo.id,jo.customer_id,jo.order_no,p.abbr customer_name,jo.order_export_date,"
        	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
        	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
        	 	 + " FROM job_order jo"
        	 	 + " LEFT JOIN job_order_arap joa ON joa.order_id = jo.id"
        	 	 + " LEFT JOIN party p ON p.id = jo.customer_id"
        	 	 + " WHERE jo.delete_flag = 'N' and (jo.office_id="+office_id+ ref_office+ ")"
        	 	 + " GROUP BY jo.id ) A WHERE 1 = 1 "+condition+" ORDER BY customer_name";
        }
		
		Record re = Db.findFirst(sql);
		renderJson(re);
	}
	
	//导出excel对账单
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
	    long office_id=user.getLong("office_id");
		String customer_id = getPara("customer_id");
		String order_export_date_begin_time = getPara("begin_time");
		String order_export_date_end_time = getPara("end_time");
		String checked = getPara("checked");
		String customerId = " and customer_id = "+customer_id;
		
		String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jor.office_id in ("+relist.getStr("office_id")+")";
        }
		
		if(StringUtils.isBlank(customer_id)){
			customerId = "";
		}
		
		
		String order_export_date = "  and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"' )";
		String exportName = order_export_date_begin_time+"~"+order_export_date_end_time;
		if(StringUtils.isBlank(order_export_date_begin_time)||StringUtils.isBlank(order_export_date_end_time)){
			order_export_date = "";
		}
	    String condition = customerId+order_export_date;
		
	    String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = " SELECT *,charge_rmb-cost_rmb profit,FORMAT((((charge_rmb-cost_rmb)/cost_rmb)*100),2) profit_rat FROM ("
            		+" SELECT jo.id,jo.customer_id,jo.order_no,jo.order_export_date,p.abbr,SUM(currency_total_amount) charge_rmb,"
            		+" ifnull((SELECT SUM(currency_total_amount) from  job_order_arap joa "
            		+" LEFT JOIN job_order jor on joa.order_id = jor.id "
            		+" WHERE joa.order_type = 'cost' and (jor.office_id="+office_id+ ref_office+ ") "+condition
            		+ " and jor.delete_flag = 'N'"
    				+" ),0) cost_rmb"
            		+"  from job_order jo "
            		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
            		+"  LEFT JOIN party p on p.id = jo.customer_id"
            		+"  WHERE (jor.office_id="+office_id+ ref_office+ ") and joa.order_type = 'charge' "+condition
            		+ " and jo.delete_flag = 'N'"
    				+" GROUP BY jo.id"
            		+" ) A where 1=1 and (charge_rmb-cost_rmb)<0 ORDER BY abbr";
        }else{
        	sql = " SELECT *,charge_rmb-cost_rmb profit,FORMAT((((charge_rmb-cost_rmb)/cost_rmb)*100),2) profit_rat FROM ("
            		+" SELECT jo.id,jo.customer_id,jo.order_no,jo.order_export_date,p.abbr,SUM(currency_total_amount) charge_rmb,"
            		+" ifnull((SELECT SUM(currency_total_amount) from  job_order_arap joa "
            		+" LEFT JOIN job_order jor on joa.order_id = jor.id "
            		+" WHERE joa.order_type = 'cost' and (jor.office_id="+office_id+ ref_office+ ") "+condition
            		+ " and jor.delete_flag = 'N'"
    				+" ),0) cost_rmb"
            		+"  from job_order jo "
            		+"  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
            		+"  LEFT JOIN party p on p.id = jo.customer_id"
            		+"  WHERE (jor.office_id="+office_id+ ref_office+ ") and joa.order_type = 'charge' "+condition
            		+ " and jo.delete_flag = 'N'"
    				+" GROUP BY jo.id"
            		+" ) A where 1=1  ORDER BY abbr";
        }
        
		
		String total_name_header = "单号,客户,出货时间,应收折合金额(CNY),应付折合金额(CNY),利润(RMB),利润率(%)";
		String[] headers = total_name_header.split(",");
		
		
		String[] fields = {"ORDER_NO", "ABBR", "ORDER_EXPORT_DATE", "CHARGE_RMB", "COST_RMB", "PROFIT","PROFIT_RAT"};
		String fileName = PoiUtils.generateExcel(headers, fields, sql,exportName);
		renderText(fileName);
	}
	
}
