package controllers.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.profile.OfficeController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ManualReportController extends Controller {
	private Log logger = Log.getLog(ManualReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		Long office_id = user.getLong("office_id");
		Long user_id = user.getLong("id");
		Record order = Db.findFirst("select * from manual_report where order_type = '费用明细' and creator = ? and office_id = ?",user_id,office_id);
		if(order == null){
			order = new Record();
		}else{
			//设置表格宽度
			String show_line = order.getStr("show_line");
			int lineSize = show_line.split(",").length;
			int tableWidth = 1200;
			if(lineSize > 6){
				tableWidth += (lineSize - 6) * 120;
			}
			setAttr("tableWidth", tableWidth);
		}
		setAttr("manual", order);
		render("/eeda/arap/manualReport/list.html");
	}
	
	
	@Before(Tx.class)
    public void save(){
		String jsonStr = getPara("jsonStr");
		UserLogin user = LoginUserController.getLoginUser(this);
		Long office_id = user.getLong("office_id");
		Long user_id = user.getLong("id");
		
		Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String show_line = dto.get("show_line").toString();
        show_line = show_line.substring(1, show_line.length()-1);
        
        String summary = dto.get("summary").toString();
        summary = summary.substring(1, summary.length()-1);
        
        String summary_show = dto.get("summary_show").toString();
        summary_show = summary_show.substring(1, summary_show.length()-1);
        
        String search_condition = dto.get("search_condition").toString();
        search_condition = search_condition.substring(1, search_condition.length()-1);
        
		
		Record order = Db.findFirst("select * from manual_report where order_type = '费用明细' and creator = ?",user_id);
		if(order == null){
			order = new Record();
			order.set("jsonStr", jsonStr);
			order.set("show_line", show_line);
			order.set("summary", summary);
			order.set("summary_show", summary_show);
			order.set("search_condition", search_condition);
			order.set("order_type", "费用明细");
			order.set("creator", user_id);
			order.set("create_time", new Date());
			order.set("office_id", office_id);
			Db.save("manual_report", order);
		} else {
			order.set("jsonStr", jsonStr);
			order.set("show_line", show_line);
			order.set("summary", summary);
			order.set("summary_show", summary_show);
			order.set("search_condition", search_condition);
			order.set("update_time", new Date());
			Db.update("manual_report", order);
		}
		
		renderJson(order);
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        
        String customer_id = getPara("customer_id");
    	if(StringUtils.isNotBlank(getPara("customer_id"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,customer_id,"CUSTOMER");
    	}
    	String sp_id = getPara("sp_id");
    	String order_no = getPara("order_no");
    	String export_date_begin_time = getPara("export_date_begin_time");
    	String export_date_end_time = getPara("export_date_end_time");
    	String create_stamp_begin_time = getPara("create_stamp_begin_time");
    	String create_stamp_end_time = getPara("create_stamp_end_time");
    	String summary = getPara("summary");
    	
    	//查询条件构造
    	String conditions = "";
    	if(StringUtils.isNotBlank(customer_id)){
    		conditions += " and jor.customer_id = "+customer_id;
    	}
    	if(StringUtils.isNotBlank(sp_id)){
    		conditions += " and joa.sp_id = "+sp_id;
    	}
		if(StringUtils.isNotBlank(order_no)){
			conditions += " and jor.order_no = '"+order_no+"'";
		}
		if(StringUtils.isNotBlank(export_date_begin_time) || StringUtils.isNotBlank(export_date_end_time)){
			if(StringUtils.isBlank(export_date_begin_time)){
				export_date_begin_time = "2000-01-01";
			}
			if(StringUtils.isBlank(export_date_end_time)){
				export_date_end_time = "2037-01-01";
			}else{
				export_date_end_time += " 23:59:59";
			}
			conditions += " and jor.order_export_date between '"+export_date_begin_time+"' and '"+export_date_end_time+"'";
		}
		if(StringUtils.isNotBlank(create_stamp_begin_time) || StringUtils.isNotBlank(create_stamp_end_time)){
			if(StringUtils.isBlank(create_stamp_begin_time)){
				create_stamp_begin_time = "2000-01-01";
			}
			if(StringUtils.isBlank(create_stamp_end_time)){
				create_stamp_end_time = "2037-01-01";
			}else{
				create_stamp_end_time += " 23:59:59";
			}
			conditions += " and jor.create_stamp between '"+create_stamp_begin_time+"' and '"+create_stamp_end_time+"'";
		}

        String sql = "select jor.id,jor.order_no ,jor.customer_id,p.abbr customer_name,jor.order_export_date export_date,ul.c_name creator_name,"
        		+ " jor.create_stamp,joa.sp_id,joa_p.abbr sp_name,joa.charge_id,fi.name fin_name,"
        		+ " ifnull(joa.total_amount,0) check_amount,ifnull(joa.currency_total_amount,0) total_amount,"
        		+ " jos.booking_agent,"
        		+ " (select abbr from party where id = jos.booking_agent) booking_agent_name,"
        		+ " (select abbr from party where id = jos.carrier) carrier_name,"
        		+ " (select abbr from party where id = jos.head_carrier) head_carrier_name,"
        		+ " jos.sono,jos.hbl_no,jos.mbl_no,jos.vessel,jos.voyage,jos.route,jos.etd,jos.eta"
        		+ " from job_order_arap joa"
        		+ " LEFT JOIN job_order jor on jor.id = joa.order_id"
        		+ " LEFT JOIN job_order_shipment jos on jos.order_id = jor.id"
        		+ " LEFT JOIN party p on p.id = jor.customer_id"
        		+ " LEFT JOIN party joa_p on joa_p.id = joa.sp_id"
        		+ " LEFT JOIN user_login ul on ul.id = jor.creator"
        		+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id"
        		+ " where 1 = 1 and jor.office_id = " + office_id
        		+ conditions
        		+ " group by joa.id";
        
        if(StringUtils.isNotBlank(summary)){
        	String[] array = summary.split(",");
        	String summary_eng = "";
        	for (int i = 0; i < array.length; i++) {
				if(array[i].trim().equals("客户")){
					summary_eng += "A.customer_id,";
				}
				if(array[i].trim().equals("工作单")){
					summary_eng += "A.order_no,";
				}
				if(array[i].trim().equals("出货日期")){
					summary_eng += "A.export_date,";
				}
				if(array[i].trim().equals("结算公司")){
					summary_eng += "A.sp_id,";
				}
				if(array[i].trim().equals("费用名称")){
					summary_eng += "A.charge_id,";
				}
				if(array[i].trim().equals("订舱代理")){
					summary_eng += "A.booking_agent_name,";
				}
				if(array[i].trim().equals("船公司")){
					summary_eng += "A.carrier_name,";
				}
				if(array[i].trim().equals("头程公司")){
					summary_eng += "A.head_carrier_name,";
				}
				if(array[i].trim().equals("SO NO")){
					summary_eng += "A.sono,";
				}
				if(array[i].trim().equals("HBL号")){
					summary_eng += "A.hbl_no,";
				}
				if(array[i].trim().equals("MBL号")){
					summary_eng += "A.mbl_no,";
				}
				if(array[i].trim().equals("船名")){
					summary_eng += "A.vessel,";
				}
				if(array[i].trim().equals("航次")){
					summary_eng += "A.voyage,";
				}
				if(array[i].trim().equals("航线")){
					summary_eng += "A.route,";
				}
				if(array[i].trim().equals("ETD")){
					summary_eng += "A.etd,";
				}
				if(array[i].trim().equals("ETA")){
					summary_eng += "A.eta,";
				}
			}
        	summary_eng = summary_eng.substring(0, summary_eng.length()-1);
        	sql = "select group_concat(distinct id separator '<br/>') id,"
        	        + " group_concat(distinct order_no separator '<br/>') order_no,"
        			+ " group_concat(distinct customer_name separator '<br/>') customer_name,"
        			+ " group_concat(distinct export_date separator '<br/>') export_date,"
        			+ " group_concat(distinct sp_name separator '<br/>') sp_name,"
        			+ " group_concat(distinct creator_name separator '<br/>') creator_name,"
        			+ " group_concat(distinct create_stamp separator '<br/>') create_stamp,"
        			+ " group_concat(distinct fin_name separator '<br/>') fin_name,"
        			+ " ifnull(sum(check_amount),0) check_amount,"
        			+ " ifnull(sum(total_amount),0) total_amount,"
        			+ " group_concat(distinct booking_agent_name separator '<br/>') booking_agent_name,"
        			+ " group_concat(distinct carrier_name separator '<br/>') carrier_name,"
        			+ " group_concat(distinct head_carrier_name separator '<br/>') head_carrier_name,"
        			+ " group_concat(distinct sono separator '<br/>') sono,"
        			+ " group_concat(distinct hbl_no separator '<br/>') hbl_no,"
        			+ " group_concat(distinct mbl_no separator '<br/>') mbl_no,"
        			+ " group_concat(distinct vessel separator '<br/>') vessel,"
        			+ " group_concat(distinct voyage separator '<br/>') voyage,"
        			+ " group_concat(distinct route separator '<br/>') route,"
        			+ " group_concat(distinct etd separator '<br/>') etd,"
        			+ " group_concat(distinct eta separator '<br/>') eta"
        			+ " from (" + sql + ") A  group by " + summary_eng;
        }
		
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
	}
	

	
	
	public void exportExcel(){
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        
        String customer_id = getPara("customer_id");
    	if(StringUtils.isNotBlank(getPara("customer_id"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,customer_id,"CUSTOMER");
    	}
    	String sp_id = getPara("sp_id");
    	String order_no = getPara("order_no");
    	String export_date_begin_time = getPara("export_date_begin_time");
    	String export_date_end_time = getPara("export_date_end_time");
    	String create_stamp_begin_time = getPara("create_stamp_begin_time");
    	String create_stamp_end_time = getPara("create_stamp_end_time");
    	String summary = getPara("summary");
    	String show_line = getPara("show_line");
    	
    	//查询条件构造
    	String conditions = "";
    	if(StringUtils.isNotBlank(customer_id)){
    		conditions += " and jor.customer_id = "+customer_id;
    	}
    	if(StringUtils.isNotBlank(sp_id)){
    		conditions += " and joa.sp_id = "+sp_id;
    	}
    	if(StringUtils.isNotBlank(order_no)){
			conditions += " and jor.order_no = '"+order_no+"'";
		}
		if(StringUtils.isNotBlank(export_date_begin_time) || StringUtils.isNotBlank(export_date_end_time)){
			if(StringUtils.isBlank(export_date_begin_time)){
				export_date_begin_time = "2000-01-01";
			}
			if(StringUtils.isBlank(export_date_end_time)){
				export_date_end_time = "2037-01-01";
			}else{
				export_date_end_time += " 23:59:59";
			}
			conditions += " and jor.order_export_date between '"+export_date_begin_time+"' and '"+export_date_end_time+"'";
		}
		if(StringUtils.isNotBlank(create_stamp_begin_time) || StringUtils.isNotBlank(create_stamp_end_time)){
			if(StringUtils.isBlank(create_stamp_begin_time)){
				create_stamp_begin_time = "2000-01-01";
			}
			if(StringUtils.isBlank(create_stamp_end_time)){
				create_stamp_end_time = "2037-01-01";
			}else{
				create_stamp_end_time += " 23:59:59";
			}
			conditions += " and jor.create_stamp between '"+create_stamp_begin_time+"' and '"+create_stamp_end_time+"'";
		}

		String sql = "select jor.id,jor.order_no ,jor.customer_id,p.abbr customer_name,jor.order_export_date export_date,ul.c_name creator_name,"
        		+ " jor.create_stamp,joa.sp_id,joa_p.abbr sp_name,joa.charge_id,fi.name fin_name,"
        		+ " ifnull(joa.total_amount,0) check_amount,ifnull(joa.currency_total_amount,0) total_amount,"
        		+ " jos.booking_agent,"
        		+ " (select abbr from party where id = jos.booking_agent) booking_agent_name,"
        		+ " (select abbr from party where id = jos.carrier) carrier_name,"
        		+ " (select abbr from party where id = jos.head_carrier) head_carrier_name,"
        		+ " jos.sono,jos.hbl_no,jos.mbl_no,jos.vessel,jos.voyage,jos.route,jos.etd,jos.eta"
        		+ " from job_order_arap joa"
        		+ " LEFT JOIN job_order jor on jor.id = joa.order_id"
        		+ " LEFT JOIN job_order_shipment jos on jos.order_id = jor.id"
        		+ " LEFT JOIN party p on p.id = jor.customer_id"
        		+ " LEFT JOIN party joa_p on joa_p.id = joa.sp_id"
        		+ " LEFT JOIN user_login ul on ul.id = jor.creator"
        		+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id"
        		+ " where 1 = 1 and jor.office_id = " + office_id
        		+ conditions
        		+ " group by joa.id";
        
        if(StringUtils.isNotBlank(summary)){
        	String[] array = summary.split(",");
        	String summary_eng = "";
        	for (int i = 0; i < array.length; i++) {
				if(array[i].trim().equals("客户")){
					summary_eng += "A.customer_id,";
				}
				if(array[i].trim().equals("工作单")){
					summary_eng += "A.order_no,";
				}
				if(array[i].trim().equals("出货时间")){
					summary_eng += "A.export_date,";
				}
				if(array[i].trim().equals("结算公司")){
					summary_eng += "A.sp_id,";
				}
				if(array[i].trim().equals("费用名称")){
					summary_eng += "A.charge_id,";
				}
				if(array[i].trim().equals("订舱代理")){
					summary_eng += "A.booking_agent_name,";
				}
				if(array[i].trim().equals("船公司")){
					summary_eng += "A.carrier_name,";
				}
				if(array[i].trim().equals("头程公司")){
					summary_eng += "A.head_carrier_name,";
				}
				if(array[i].trim().equals("SO NO")){
					summary_eng += "A.sono,";
				}
				if(array[i].trim().equals("HBL号")){
					summary_eng += "A.hbl_no,";
				}
				if(array[i].trim().equals("MBL号")){
					summary_eng += "A.mbl_no,";
				}
				if(array[i].trim().equals("船名")){
					summary_eng += "A.vessel,";
				}
				if(array[i].trim().equals("航次")){
					summary_eng += "A.voyage,";
				}
				if(array[i].trim().equals("航线")){
					summary_eng += "A.route,";
				}
				if(array[i].trim().equals("ETD")){
					summary_eng += "A.etd,";
				}
				if(array[i].trim().equals("ETA")){
					summary_eng += "A.eta,";
				}
			}
        	summary_eng = summary_eng.substring(0, summary_eng.length()-1);
        	sql = "select group_concat(distinct id separator '<br/>') id,"
        	        + " group_concat(distinct order_no separator '<br/>') order_no,"
        			+ " group_concat(distinct customer_name separator '<br/>') customer_name,"
        			+ " group_concat(distinct export_date separator '<br/>') export_date,"
        			+ " group_concat(distinct sp_name separator '<br/>') sp_name,"
        			+ " group_concat(distinct creator_name separator '<br/>') creator_name,"
        			+ " group_concat(distinct create_stamp separator '<br/>') create_stamp,"
        			+ " group_concat(distinct fin_name separator '<br/>') fin_name,"
        			+ " ifnull(sum(check_amount),0) check_amount,"
        			+ " ifnull(sum(total_amount),0) total_amount,"
        			+ " group_concat(distinct booking_agent_name separator '<br/>') booking_agent_name,"
        			+ " group_concat(distinct carrier_name separator '<br/>') carrier_name,"
        			+ " group_concat(distinct head_carrier_name separator '<br/>') head_carrier_name,"
        			+ " group_concat(distinct sono separator '<br/>') sono,"
        			+ " group_concat(distinct hbl_no separator '<br/>') hbl_no,"
        			+ " group_concat(distinct mbl_no separator '<br/>') mbl_no,"
        			+ " group_concat(distinct vessel separator '<br/>') vessel,"
        			+ " group_concat(distinct voyage separator '<br/>') voyage,"
        			+ " group_concat(distinct route separator '<br/>') route,"
        			+ " group_concat(distinct etd separator '<br/>') etd,"
        			+ " group_concat(distinct eta separator '<br/>') eta"
        			+ " from (" + sql + ") A  group by " + summary_eng;
        }
		

		String exportName = "自定义报表";
		String total_name_header = show_line;
		String[] headers = total_name_header.split(",");
		String[] fields = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			String value = headers[i].trim();
			if(value.equals("工作单")){
				fields[i] = "ORDER_NO";
			}
			if(value.equals("客户")){
				fields[i] = "CUSTOMER_NAME";
			}
			if(value.equals("出货日期")){
				fields[i] = "EXPORT_DAE";
			}
			if(value.equals("创建人")){
				fields[i] = "CREATOR_NAME";
			}
			if(value.equals("创建时间")){
				fields[i] = "CREATE_STAMP";
			}
			if(value.equals("结算公司")){
				fields[i] = "SP_NAME";
			}
			if(value.equals("费用名称")){
				fields[i] = "FIN_NAME";
			}
			if(value.equals("对账金额")){
				fields[i] = "CHECK_AMOUNT";
			}
			if(value.equals("结算金额")){
				fields[i] = "TOTAL_AMOUNT";
			}
		}
		
		String fileName = PoiUtils.generateExcel(headers, fields, sql ,exportName);
		renderText(fileName);
	} 
	
}
