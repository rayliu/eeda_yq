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
public class ChargeBalanceReportController extends Controller {
	private Log logger = Log.getLog(ChargeBalanceReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
			return;
		}
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/chargeBalanceReport");
        setAttr("listConfigList", configList);
		render("/eeda/arap/ChargeBalanceReport/ChargeBalanceReport.html");
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
        
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String sp_id =getPara("sp_id");
    	if(StringUtils.isNotEmpty(getPara("sp_id"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,sp_id,"ARAP_COM");
    	}
        
        
        
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,A.employee_name,A.employee_id,sum(charge_cny) charge_cny,"
        		+ " SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,"
        		+ " SUM(uncharge_cny) uncharge_cny,SUM(uncharge_usd) uncharge_usd,sum(uncharge_jpy) uncharge_jpy,"
        		+ " SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb,sum(uncharge_rmb) uncharge_rmb"
        		+ " FROM (SELECT jo.id,jo.customer_id,jo.order_export_date,p.abbr,joa.sp_id,em.employee_name,em.id employee_id,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 3,total_amount,0) charge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6,"
        		+ " total_amount,0) charge_usd,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 8,total_amount,0) charge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9,"
        		+ " total_amount,0) charge_hkd,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_usd,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_hkd,"
        		+ " IF (joa.order_type = 'charge',currency_total_amount,0) charge_rmb,"
        		+ " IF (joa.order_type = 'charge' AND pay_flag='N',currency_total_amount,0) uncharge_rmb"
        		+ " FROM job_order jo"
        		+ " LEFT JOIN job_order_arap joa ON jo.id = joa.order_id"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
        		+ " LEFT JOIN customer_salesman cs on cs.party_id = joa.sp_id"
        		+ " LEFT JOIN employee em on em.id = cs.salesman_id"
        		+ " WHERE 1=1 and (jo.office_id="+office_id+ ref_office+ ")"
        		+ " and jo.delete_flag = 'N'"
				+ " ) A"
        		+ " WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0"+condition
        		+ " GROUP BY A.sp_id"
        		+ " ORDER BY uncharge_rmb desc";
		
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
		String spid =(String) getPara("sp_id");
		String sp_name = (String) getPara("abbr_like");
		String order_export_date_begin_time =(String) getPara("order_export_date_begin_time");
		String order_export_date_end_time =(String) getPara("order_export_date_end_time");
		
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
		
		String sp_id =" and sp_id="+spid;
		if(StringUtils.isBlank(spid)){
			sp_id="";
		}
		String spName = "";
		if(StringUtils.isNotBlank(sp_name)){
			spName =" and abbr like '%"+sp_name+"%'";
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
		String condition = sp_id+spName+order_export_date;
		
		String sql=" SELECT "
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) charge_hkd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 3 "
			+"	  and joa.order_type = 'charge' and pay_flag='N'  "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_cny,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 6 "
			+"	  and joa.order_type = 'charge' and pay_flag='N' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_usd,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 8 "
			+"	  and joa.order_type = 'charge' and pay_flag='N' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_jpy,"
			+"	(SELECT "
			+"	IFNULL(SUM(joa.total_amount),0)"
			+"	  from job_order jo "
			+"	  LEFT JOIN job_order_arap joa on jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+" and joa.currency_id = 9 "
			+"	  and joa.order_type = 'charge' and pay_flag='N' "+condition
			+ " and jo.delete_flag = 'N'"
			+"	) uncharge_hkd, "
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+"	AND joa.order_type = 'charge' "+condition+""
			+ " and jo.delete_flag = 'N'"
			+ ") total_charge,"
			+"	(SELECT "
			+"		IFNULL(SUM(joa.currency_total_amount),	0) "
			+"	FROM  job_order jo "
			+"	LEFT JOIN job_order_arap joa ON jo.id = joa.order_id "
			+"  LEFT JOIN party p on p.id = joa.sp_id"
			+"	WHERE 	1=1 and (jo.office_id="+office_id+ ref_office+ ")"
			+"	AND joa.order_type = 'charge' and pay_flag='N' "+condition
			+ " and jo.delete_flag = 'N'"
			+") total_uncharge";
		
		
		Record re = Db.findFirst(sql);
		long total=list();
		re.set("total", total);
		renderJson(re);
	}
	
	public void downloadExcelList(){
		UserLogin user = LoginUserController.getLoginUser(this);
		if (user==null) {
            return;
        }
		long office_id = user.getLong("office_id");
		String sp_id = getPara("sp_id");
		String begin_time = getPara("begin_time");
		String end_time = getPara("end_time");
		
		String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
		
		String spId = "";
		String order_export_date = "";
		if (StringUtils.isBlank(sp_id)) {
			spId = "";
		} else {
			spId = " and A.sp_id = " + sp_id;
		}
		if (StringUtils.isBlank(begin_time)||StringUtils.isBlank(end_time)) {
			order_export_date = "";
		} else {
			order_export_date =  " and (order_export_date between '"+begin_time+"' and '"+end_time+"')";
		}

		String condition = spId+order_export_date;

		String sql = " SELECT A.id,A.customer_id,A.abbr,A.sp_id,A.employee_name,A.employee_id,sum(charge_cny) charge_cny,"
        		+ " SUM(charge_usd) charge_usd,SUM(charge_jpy) charge_jpy,sum(charge_hkd) charge_hkd,"
        		+ " SUM(uncharge_cny) uncharge_cny,SUM(uncharge_usd) uncharge_usd,sum(uncharge_jpy) uncharge_jpy,"
        		+ " SUM(uncharge_hkd) uncharge_hkd,SUM(charge_rmb) charge_rmb,sum(uncharge_rmb) uncharge_rmb, "
        		+ " round(((SUM(charge_rmb)-sum(uncharge_rmb))/sum(charge_rmb))*100,2)  payment_rate "
        		+ " FROM (SELECT jo.id,jo.customer_id,jo.order_export_date,p.abbr,joa.sp_id,em.employee_name,em.id employee_id,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 3,total_amount,0) charge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6,"
        		+ " total_amount,0) charge_usd,IF (joa.order_type = 'charge'"
        		+ " AND joa.currency_id = 8,total_amount,0) charge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9,"
        		+ " total_amount,0) charge_hkd,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 3 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_cny,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 6 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_usd,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 8 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_jpy,"
        		+ " IF (joa.order_type = 'charge' AND joa.currency_id = 9 AND pay_flag='N',"
        		+ " total_amount,0) uncharge_hkd,"
        		+ " IF (joa.order_type = 'charge',currency_total_amount,0) charge_rmb,"
        		+ " IF (joa.order_type = 'charge' AND pay_flag='N',currency_total_amount,0) uncharge_rmb"
        		+ " FROM job_order jo"
        		+ " LEFT JOIN job_order_arap joa ON jo.id = joa.order_id"
        		+ " LEFT JOIN party p ON p.id = joa.sp_id"
        		+ " LEFT JOIN customer_salesman cs on cs.party_id = joa.sp_id"
        		+ " LEFT JOIN employee em on em.id = cs.salesman_id"
        		+ " WHERE 1=1 and (jo.office_id="+office_id+ ref_office+ ")"
        		+ " and jo.delete_flag = 'N'"
				+ " ) A"
        		+ " WHERE A.sp_id IS NOT NULL AND A.charge_rmb!=0"+condition
        		+ " GROUP BY A.sp_id"
        		+ " ORDER BY uncharge_rmb desc";

        String sqlExport = sql;
		String total_name_header = "结算公司,CNY(应收),USD(应收),JPY(应收),HKD(应收),折合CNY(应收),CNY(未收),USD(未收),JPY(未收),HKD(未收),折合CNY(未收),回款率";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ABBR", "CHARGE_CNY", "CHARGE_USD", "CHARGE_JPY",
				"CHARGE_HKD", "CHARGE_RMB", "UNCHARGE_CNY","UNCHARGE_USD","UNCHARGE_JPY","UNCHARGE_HKD","UNCHARGE_RMB","PAYMENT_RATE"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
	
	
	
	
}
