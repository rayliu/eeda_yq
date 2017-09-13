package controllers.tradeReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import models.UserLogin;
import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.arap.BillProfitAndPaymentController;
import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class tradeBillProfitAndPaymentController extends Controller{
	private Log logger = Log.getLog(tradeBillProfitAndPaymentController.class);
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/billProfitAndPayment");
		setAttr("listConfigList", configList);
		render("/tr/tradeReport/tradeBillProfitAndPaymentList.html");
	}
	
	public long list(){
		String checked = getPara("checked");
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = "SELECT*,ROUND(charge_rmb-cost_rmb,2) profit,ROUND(((charge_rmb-cost_rmb)/charge_rmb)*100,2) profit_rate "
           	 	 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr customer_name,tjo.order_export_date,"
           	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
           	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
           	 	 + " FROM trade_job_order tjo"
           	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
           	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
           	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id
           	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1 "+condition+" AND (charge_rmb - cost_rmb) < 0 ORDER BY customer_name";
        }else{
        	 sql = "SELECT*,ROUND(charge_rmb-cost_rmb,2) profit,ROUND(((charge_rmb-cost_rmb)/charge_rmb)*100,2) profit_rate "
        	 	 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr customer_name,tjo.order_export_date,"
        	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
        	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
        	 	 + " FROM trade_job_order tjo"
        	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
        	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
        	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id
        	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1  "+condition+" ORDER BY customer_name";
        }
        
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        long total = rec.getLong("total");
        List<Record> orderList = Db.find(sql+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
        return total;
	}
	
	//合计方法
	public void listTotal(){
		String checked = getPara("checked");
		String customer_name= getPara("customer_name");
		String order_export_date_begin_time = getPara("order_export_date_begin_time");
		String order_export_date_end_time = getPara("order_export_date_end_time");
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
		String customerName = "";
        if(customer_name==null||StringUtils.isBlank(customer_name)){
        	customerName="";
        }else{
        	customerName =" and customer_name like '%"+customer_name+"%'";
        }
		
		String order_export_date =  " and (order_export_date between '"+order_export_date_begin_time+" 00:00:00' and '"+order_export_date_end_time+" 23:59:59')";

		if(StringUtils.isBlank(order_export_date_begin_time)||StringUtils.isBlank(order_export_date_end_time)){
			order_export_date="";
		}
		
		String condition = customerName+order_export_date;
		
		String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	 sql = "SELECT*,ROUND(SUM(charge_rmb),2) charge_total,"
        	 	 + " ROUND(SUM(cost_rmb),2) cost_total,"
        	 	 + " ROUND(SUM(charge_rmb)-SUM(cost_rmb),2) total_profit,"
        	 	 + " ROUND(((SUM(charge_rmb)-SUM(cost_rmb))/SUM(charge_rmb))*100,2) total_profit_rate"
           	 	 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr customer_name,tjo.order_export_date,"
           	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
           	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
           	 	 + " FROM trade_job_order tjo"
           	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
           	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
           	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id
           	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1 "+condition+" AND (charge_rmb - cost_rmb) < 0 ORDER BY customer_name";
        }else{
        	 sql = "SELECT*,ROUND(SUM(charge_rmb),2) charge_total,"
        	 	 + " ROUND(SUM(cost_rmb),2) cost_total,"
        	 	 + " ROUND(SUM(charge_rmb)-SUM(cost_rmb),2) total_profit,"
        	 	 + " ROUND(((SUM(charge_rmb)-SUM(cost_rmb))/SUM(charge_rmb))*100,2) total_profit_rate"
    			 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr customer_name,tjo.order_export_date,"
        	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
        	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
        	 	 + " FROM trade_job_order tjo"
        	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
        	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
        	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id
        	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1 "+condition+" ORDER BY customer_name";
        }
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
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
		
		if(StringUtils.isBlank(customer_id)){
			customerId = "";
		}
		
		
		String order_export_date = " and (order_export_date between '"+order_export_date_begin_time+" 00:00:00' and '"+order_export_date_end_time+" 23:59:59' )";
		String exportName = order_export_date_begin_time+"~"+order_export_date_end_time;
		if(StringUtils.isBlank(order_export_date_begin_time)||StringUtils.isBlank(order_export_date_end_time)){
			order_export_date = "";
		}
	    String condition = customerId+order_export_date;
		
	    String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = "SELECT*,ROUND(charge_rmb-cost_rmb,2) profit,ROUND(((charge_rmb-cost_rmb)/charge_rmb)*100,2) profit_rate "
              	 	 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr,tjo.order_export_date,"
              	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
              	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
              	 	 + " FROM trade_job_order tjo"
              	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
              	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
              	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id+" "+condition
              	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1 AND (charge_rmb - cost_rmb) < 0 ORDER BY abbr";
        }else{
        	sql = "SELECT*,ROUND(charge_rmb-cost_rmb,2) profit,ROUND(((charge_rmb-cost_rmb)/charge_rmb)*100,2) profit_rate "
           	 	 + " FROM (SELECT tjo.id,tjo.customer_id,tjo.order_no,p.abbr,tjo.order_export_date,"
           	 	 + " SUM(IF(order_type='charge',currency_total_amount,0)) charge_rmb,"
           	 	 + " SUM(IF(order_type='cost',currency_total_amount,0)) cost_rmb"
           	 	 + " FROM trade_job_order tjo"
           	 	 + " LEFT JOIN trade_job_order_arap tjoa ON tjoa.order_id = tjo.id"
           	 	 + " LEFT JOIN party p ON p.id = tjo.customer_id"
           	 	 + " WHERE tjo.delete_flag = 'N' and tjo.office_id = "+office_id+" "+condition
           	 	 + " GROUP BY tjo.id ) A WHERE 1 = 1 ORDER BY abbr";
        }
        
		
		String total_name_header = "单号,客户,出货时间,应收折合金额(CNY),应付折合金额(CNY),利润(RMB),利润率(%)";
		String[] headers = total_name_header.split(",");
		
		
		String[] fields = {"ORDER_NO", "ABBR", "ORDER_EXPORT_DATE", "CHARGE_RMB", "COST_RMB", "PROFIT","PROFIT_RATE"};
		String fileName = PoiUtils.generateExcel(headers, fields, sql,exportName);
		renderText(fileName);
	}

}
