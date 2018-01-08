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
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class profitController extends Controller {
	private Log logger = Log.getLog(profitController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/ProfitAndPaymentRate/Profit.html");
	}
	
	public long list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
			return 0;
		}
        long office_id=user.getLong("office_id");
        
        String customer_id =getPara("customer_id");
    	if(StringUtils.isNotEmpty(getPara("customer_id"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,customer_id,"CUSTOMER");
    	}
        
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.id,A.customer_id,A.abbr,A.user_name,A.royalty_rate,A.user_id,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb FROM ("
        		+" SELECT jo.id,jo.customer_id,jo.order_export_date,p.abbr,ul.c_name user_name,ul.id user_id,cs.royalty_rate,"
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
        		+" LEFT JOIN customer_salesman cs on cs.party_id = jo.customer_id"
        		+" LEFT JOIN user_login ul on ul.id = cs.salesman_id"
        		+"  WHERE 1=1 and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ")"
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 "+condition+" GROUP BY A.customer_id  ORDER BY abbr";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
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
		String user_id =(String) getPara("user_id");
		String customer = getPara("customer");
		String customer_name = getPara("abbr_like");
        String begin_time = getPara("order_export_date_begin_time");
        String end_time = getPara("order_export_date_end_time");
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
			return;
		}
        long office_id=user.getLong("office_id");
		
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String conditions = "";
        
        if(StringUtils.isNotBlank(customer)){
        	conditions += " and jo.customer_id = "+ customer;
        }
        
        if(StringUtils.isNotBlank(customer_name)){
        	conditions += " and p.abbr like '%"+ customer_name + "%'";
        }
        
        if(StringUtils.isNotBlank(user_id)){
        	conditions += " and ul.id = "+ user_id;
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
        		+" LEFT JOIN customer_salesman cs on cs.party_id = jo.customer_id"
        		+" LEFT JOIN user_login ul on ul.id = cs.salesman_id"
        		+"  WHERE 1=1 and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ")"
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
		UserLogin user = LoginUserController.getLoginUser(this);
		if (user==null) {
            return;
        }
	    long office_id=user.getLong("office_id");
		String customer_id = getPara("customer_id");
		String order_export_date_begin_time = getPara("begin_time");
		String order_export_date_end_time = getPara("end_time");
		String customerId = "";
		
		String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
		
		if(customer_id == ""||customer_id.equals("")){
			customerId = "";
		}else{
			customerId = " and customer_id = "+customer_id;
		}
		
		String order_export_date = "";
		String exportName = "";
		if(StringUtils.isNotBlank(order_export_date_begin_time)||StringUtils.isNotBlank(order_export_date_end_time)){
			order_export_date = "  and (order_export_date between '"+order_export_date_begin_time+"' and '"+order_export_date_end_time+"' )";
			exportName = order_export_date_begin_time+"~"+order_export_date_end_time;
		}
	    String condition = customerId+order_export_date;
		
		String sqlExport = " SELECT A.id,A.customer_id,A.abbr,A.user_name,A.royalty_rate,A.user_id,sum(charge_cny) charge_cny,SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,SUM(cost_cny) cost_cny,SUM(cost_usd) cost_usd,"
        		+" sum(cost_jpy) cost_jpy,SUM(cost_hkd) cost_hkd,SUM(charge_rmb) charge_rmb,sum(cost_rmb) cost_rmb,(SUM(charge_rmb)-sum(cost_rmb)) profit,FORMAT((((SUM(charge_rmb)-sum(cost_rmb))/(sum(cost_rmb)))*100),2) profit_rate FROM ("
        		+" SELECT jo.id,jo.customer_id,jo.order_export_date,p.abbr,ul.c_name user_name,ul.id user_id,cs.royalty_rate,"
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
        		+" LEFT JOIN customer_salesman cs on cs.party_id = jo.customer_id"
        		+ " LEFT JOIN user_login ul on ul.id = cs.salesman_id"
        		+"  WHERE p.office_id ="+office_id+" and joa.pay_flag!='B' and (jo.office_id="+office_id+ ref_office+ ")"
        		+ " and jo.delete_flag = 'N'"
    			+" ) A where 1=1 "+condition+" GROUP BY A.customer_id  ORDER BY abbr";
		
		String total_name_header = "客户,业务员,应收折合(CNY),应付折合(CNY),利润(CNY),利润率(%),业务员提成";
		String[] headers = total_name_header.split(",");
		
		
		String[] fields = {"ABBR", "EMPLOYEE_NAME", "CHARGE_RMB", "COST_RMB", "PROFIT", "PROFIT_RATE", ""};
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	} 
	
}
