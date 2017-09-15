package controllers.report;

import interceptor.EedaMenuInterceptor;
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
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class BalanceReportController extends Controller {
    private Log logger = Log.getLog(BalanceReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	render("eeda/statusReport/balanceReport.html");
    }

    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String begin_date = getPara("begin_date");
        String end_date = getPara("end_date");
        String date_type = getPara("date_type");
        String balance = getPara("balance");
        
        
        String condition = "";
        String group_condition=""; 
        
        if(!"total".equals(balance)){
        	condition += " and joa.order_type = '"+balance+"'";
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
            
            group_condition = " cast(year(jor.create_stamp) as char)";
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
            			+ " when cast(month(jor.create_stamp) as char)"
            			+ " between 1 and 3 "
            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第一季') as char)"
            			+ " when cast(month(jor.create_stamp) as char)"
            			+ " between 4 and 6 "
            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第二季') as char)"
            			+ " when cast(month(jor.create_stamp) as char)"
            			+ " between 7 and 9 "
            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第三季') as char)"
            			+ " when cast(month(jor.create_stamp) as char)"
            			+ " between 10 and 12 "
            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第四季') as char)"
            			+ " end)";
            }else{
            	group_condition = " cast(CONCAT(year(jor.create_stamp),'-',month(jor.create_stamp)) as char)";
            }
        }
        
        condition += " and jor.create_stamp between '"+begin_date+"' and '"+end_date+"' "; 
        
        String sp_names = getPara("sp_names");
        String sp_name_col = "";
        String sp_name_con = "";
        String [] names = sp_names.split(",");
        
        //构造列      
        for (int i = 0; i < 20; i++) {
        	if(i<names.length){
        		if(!"total".equals(balance)){
        			sp_name_col += " sum(IF ( p.abbr = '"+ names[i] +"' and order_type='"+ balance +"', joa.currency_total_amount, 0 )) sp_name"+(i+1)+",";
        		}else{
        			sp_name_col += " sum(IF ( p.abbr = '"+ names[i] +"', "
        					+ " ifnull((SELECT sum(ifnull(joap.currency_total_amount,0)) "
        					+ " FROM job_order_arap joap where joap.id = joa.id and joap.order_type = 'charge'),0)"
        					+ " - "
        					+ " ifnull((SELECT sum(ifnull(joap.currency_total_amount,0)) "
        					+ " FROM job_order_arap joap where joap.id = joa.id and joap.order_type = 'cost'),0)"
        					+ ", 0 )) sp_name"+(i+1)+",";
        		}
        	} else{
        		sp_name_col += "null sp_name"+(i+1)+",";
        	}	
		}
        
        //构造查询条件
        for (int i = 0; i < names.length; i++) {
        	if(i != names.length-1){
        		sp_name_con += "'" + names[i] + "',";
        	}else{
        		sp_name_con += "'" + names[i] + "'";
        	}
        }
        
        
    	String sql = "SELECT "
    			+group_condition+" create_stamp,"
    			+ sp_name_col
    			+ " joa.type "
    			+ " FROM `job_order_arap` joa"
    			+ " LEFT JOIN job_order jor ON jor.id = joa.order_id "
    			+ " LEFT JOIN party p ON p.id = joa.sp_id"
    			+ " WHERE p.abbr IN ("+ sp_name_con +")"
    			+ " and jor.office_id="+office_id
    			+ condition
    			+ " and jor.delete_flag = 'N'"
 				+ "  group by "
    			+ group_condition
    			//+ " , joa.type"
    			+ "	order by cast(CONCAT( YEAR (jor.create_stamp), '-', MONTH (jor.create_stamp) ) AS CHAR )";
    	
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
	public void downloadExcelList(){
		  UserLogin user = LoginUserController.getLoginUser(this);
	        long office_id=user.getLong("office_id");
	        String begin_date = getPara("begin_date");
	        String end_date = getPara("end_date");
	        String date_type = getPara("date_type");
	        String balance = getPara("balance");
	        
	        
	        String condition = "";
	        String group_condition=""; 
	        
	        if(!"total".equals(balance)){
	        	condition += " and joa.order_type = '"+balance+"'";
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
	            
	            group_condition = " cast(year(jor.create_stamp) as char)";
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
	            			+ " when cast(month(jor.create_stamp) as char)"
	            			+ " between 1 and 3 "
	            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第一季') as char)"
	            			+ " when cast(month(jor.create_stamp) as char)"
	            			+ " between 4 and 6 "
	            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第二季') as char)"
	            			+ " when cast(month(jor.create_stamp) as char)"
	            			+ " between 7 and 9 "
	            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第三季') as char)"
	            			+ " when cast(month(jor.create_stamp) as char)"
	            			+ " between 10 and 12 "
	            			+ " then cast(CONCAT(year(jor.create_stamp),'-','第四季') as char)"
	            			+ " end)";
	            }else{
	            	group_condition = " cast(CONCAT(year(jor.create_stamp),'-',month(jor.create_stamp)) as char)";
	            }
	        }
	        
	        condition += " and jor.create_stamp between '"+begin_date+"' and '"+end_date+"' "; 
	        
	        String sp_names = getPara("sp_names");
	        String sp_name_col = "";
	        String sp_name_con = "";
	        String [] names = sp_names.split(",");
	        
	        //构造列      
	        for (int i = 0; i < 20; i++) {
	        	if(i<names.length){
	        		if(!"total".equals(balance)){
	        			sp_name_col += " sum(IF ( p.abbr = '"+ names[i] +"' and order_type='"+ balance +"', joa.currency_total_amount, 0 )) sp_name"+(i+1)+",";
	        		}else{
	        			sp_name_col += " sum(IF ( p.abbr = '"+ names[i] +"', "
	        					+ " ifnull((SELECT sum(ifnull(joap.currency_total_amount,0)) "
	        					+ " FROM job_order_arap joap where joap.id = joa.id and joap.order_type = 'charge'),0)"
	        					+ " - "
	        					+ " ifnull((SELECT sum(ifnull(joap.currency_total_amount,0)) "
	        					+ " FROM job_order_arap joap where joap.id = joa.id and joap.order_type = 'cost'),0)"
	        					+ ", 0 )) sp_name"+(i+1)+",";
	        		}
	        	} else{
	        		sp_name_col += "null sp_name"+(i+1)+",";
	        	}	
			}
	        
	        //构造查询条件
	        for (int i = 0; i < names.length; i++) {
	        	if(i != names.length-1){
	        		sp_name_con += "'" + names[i] + "',";
	        	}else{
	        		sp_name_con += "'" + names[i] + "'";
	        	}
	        }
	        
	        
	    	String sql = "SELECT "
	    			+group_condition+" create_stamp,"
	    			+ sp_name_col
	    			+ " joa.type "
	    			+ " FROM `job_order_arap` joa"
	    			+ " LEFT JOIN job_order jor ON jor.id = joa.order_id "
	    			+ " LEFT JOIN party p ON p.id = joa.sp_id"
	    			+ " WHERE p.abbr IN ("+ sp_name_con +") and jor.office_id="+office_id
	    			+ condition
	    			 + " and jor.delete_flag = 'N'"
	 				+ "  group by "
	    			+ group_condition
	    			+ " , joa.type"
	    			+ "	order by cast(CONCAT( YEAR (jor.create_stamp), '-', MONTH (jor.create_stamp) ) AS CHAR ),joa.type";
	    	
        String sqlExport = sql;
        String total_name_header = "单据日期,费用条目";
        for(String name : names){
        	
        	total_name_header += ","+name;
        }
		String[] headers = total_name_header.split(",");

		String[] fields = {"CREATE_STAMP","TYPE",		
				"SP_NAME1","SP_NAME2","SP_NAME3","SP_NAME4","SP_NAME5","SP_NAME6","SP_NAME7","SP_NAME8","SP_NAME9",
				"SP_NAME10","SP_NAME11","SP_NAME12","SP_NAME13","SP_NAME14","SP_NAME15","SP_NAME16","SP_NAME17",
				"SP_NAME18","SP_NAME19","SP_NAME20"};
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
}
