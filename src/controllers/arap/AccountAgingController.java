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
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountAgingController extends Controller {
	private Log logger = Log.getLog(AccountAgingController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/accountAging");
		 setAttr("listConfigList", configList);
		render("/eeda/arap/AccountAging/AccountAgingList.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sp_id = getPara("sp_id");
    	if(StringUtils.isNotEmpty(getPara("sp_id"))){    		
    		//常用结算公司保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,sp_id,"ARAP_COM");
    	}
        String type = getPara("type")==null?"new":getPara("type").trim();
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = "";
        if(StringUtils.isNotEmpty(sp_id)){
        	condition = " and joa.sp_id = '"+sp_id+"'";
        }
        
        
        String sql =  " SELECT "
        		+ " 	abbr_name, "
        		+ " 	currency_name, "
        		+ " 	sum(total_amount) total_amount, "
        		+ " 	sum(currency_total_amount) currency_total_amount, "
        		+ " 	sum(three) three, "
        		+ " 	sum(six) six, "
        		+ " 	sum(nine) nine,"
        		+ "		after_nine "
        		+ " FROM "
        		+ " 	( "
        		+ " 		select "
        		+ " joa.sp_id ,joa.exchange_currency_id ,p.abbr abbr_name,cur. NAME currency_name, "
        		+ " joa.total_amount, "
        		+ " joa.currency_total_amount, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 0 MONTH),'%Y-%m') , joa.total_amount, 0 ) three,  "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') , joa.total_amount, 0 ) six, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 2 MONTH),'%Y-%m') , joa.total_amount, 0 ) nine, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 3 MONTH),'%Y-%m') , joa.total_amount, 0 ) after_nine "
        		+ " from job_order_arap joa  "
        		+ " LEFT JOIN job_order jor ON jor.id = joa.order_id "
        		+ " LEFT JOIN party p ON  p.id = joa.sp_id "
        		+ " LEFT JOIN currency cur ON cur.id = joa.currency_id "
        		+ " LEFT JOIN charge_application_order_rel caor on caor.job_order_arap_id = joa.id and joa.create_flag = 'Y' "
        		+ " LEFT JOIN arap_charge_application_order acao on acao.id = caor.application_order_id "
        		+ " where joa.order_type = 'charge' and ifnull(acao.status,'') !='已收款' "
        		+ " and jor.office_id = "+office_id+" and joa.type != '贸易' and jor.order_export_date<date_format(curdate(),'%Y-%m')"
        		+ condition
        		+ " and jor.delete_flag = 'N'"
				+ " GROUP BY joa.id "
        		+ " 	) a "
        		+ " GROUP BY "
        		+ " 	sp_id, "
        		+ " 	exchange_currency_id ";
        
        String sql2 =  " SELECT "
        		+ " 	abbr_name, "
        		+ " 	currency_name, "
        		+ " 	sum(total_amount) total_amount, "
        		+ " 	sum(currency_total_amount) currency_total_amount, "
        		+ " 	sum(three) three, "
        		+ " 	sum(six) six, "
        		+ " 	sum(nine) nine,"
        		+ "		after_nine "
        		+ " FROM "
        		+ " 	( "
        		+ " 		select "
        		+ " joa.sp_id ,joa.exchange_currency_id ,p.abbr abbr_name,cur. NAME currency_name, "
        		+ " joa.total_amount,joa.total_amount after_nine, "
        		+ " joa.currency_total_amount, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')>=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') , joa.total_amount, 0 ) three,  "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')>=date_format(DATE_SUB(curdate(), INTERVAL 2 MONTH),'%Y-%m') , joa.total_amount, 0 ) six, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')>=date_format(DATE_SUB(curdate(), INTERVAL 3 MONTH),'%Y-%m') , joa.total_amount, 0 ) nine "
        		+ " from job_order_arap joa  "
        		+ " LEFT JOIN job_order jor ON jor.id = joa.order_id "
        		+ " LEFT JOIN party p ON  p.id = joa.sp_id "
        		+ " LEFT JOIN currency cur ON cur.id = joa.currency_id "
        		+ " LEFT JOIN charge_application_order_rel caor on caor.job_order_arap_id = joa.id and joa.create_flag = 'Y' "
        		+ " LEFT JOIN arap_charge_application_order acao on acao.id = caor.application_order_id "
        		+ " where joa.order_type = 'charge' and ifnull(acao.status,'') !='已收款' "
        		+ " and jor.office_id = "+office_id+" and joa.type != '贸易' and jor.order_export_date<date_format(curdate(),'%Y-%m')"
        		+ condition
        		+ " and jor.delete_flag = 'N'"
				+ " GROUP BY joa.id "
        		+ " 	) a "
        		+ " GROUP BY "
        		+ " 	sp_id, "
        		+ " 	exchange_currency_id ";
        if("old".equals(type)){
        	sql = sql2;
        }
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
	// 导出excel对账单
	public void downloadExcelList() {
		UserLogin user = LoginUserController.getLoginUser(this);
		long office_id = user.getLong("office_id");
		String sp_id = getPara("sp_id");
		String type = getPara("type")==null?"new":getPara("type").trim();
		String spId = "";
		if (StringUtils.isBlank(sp_id)) {
			spId = "";
		} else {
			spId = " and joa.sp_id = " + sp_id;
		}

		String condition = spId;

		String sql =  " SELECT "
        		+ " 	abbr_name, "
        		+ " 	currency_name, "
        		+ " 	sum(total_amount) total_amount, "
        		+ " 	sum(currency_total_amount) currency_total_amount, "
        		+ " 	sum(three) three, "
        		+ " 	sum(six) six, "
        		+ " 	sum(nine) nine,"
        		+ "		after_nine "
        		+ " FROM "
        		+ " 	( "
        		+ " 		select "
        		+ " joa.sp_id ,joa.exchange_currency_id ,p.abbr abbr_name,cur. NAME currency_name, "
        		+ " joa.total_amount, "
        		+ " joa.currency_total_amount, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 0 MONTH),'%Y-%m') , joa.total_amount, 0 ) three,  "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') , joa.total_amount, 0 ) six, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 2 MONTH),'%Y-%m') , joa.total_amount, 0 ) nine, "
        		+ " IF (date_format(jor.order_export_date,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 3 MONTH),'%Y-%m') , joa.total_amount, 0 ) after_nine "
        		+ " from job_order_arap joa  "
        		+ " LEFT JOIN job_order jor ON jor.id = joa.order_id "
        		+ " LEFT JOIN party p ON  p.id = joa.sp_id "
        		+ " LEFT JOIN currency cur ON cur.id = joa.currency_id "
        		+ " LEFT JOIN charge_application_order_rel caor on caor.job_order_arap_id = joa.id and joa.create_flag = 'Y' "
        		+ " LEFT JOIN arap_charge_application_order acao on acao.id = caor.application_order_id "
        		+ " where joa.order_type = 'charge' and ifnull(acao.status,'') !='已收款' "
        		+ " and jor.office_id = "+office_id+" and joa.type != '贸易' and jor.order_export_date<date_format(curdate(),'%Y-%m')"
        		+ condition
        		+ " and jor.delete_flag = 'N'"
				+ " GROUP BY joa.id "
        		+ " 	) a "
        		+ " GROUP BY "
        		+ " 	sp_id, "
        		+ " 	exchange_currency_id ";

        String sqlExport = sql;
		String total_name_header = "结算公司,币制,欠款金额,转化成人民币金额,<=30天,<=60天,<=90天,<=120天";
		String[] headers = total_name_header.split(",");

		String[] fields = { "ABBR_NAME", "CURRENCY_NAME", "TOTAL_AMOUNT", "CURRENCY_TOTAL_AMOUNT",
				"THREE", "SIX", "NINE","AFTER_NINE"};
		
		Record r = Db.findFirst("select abbr from party where id ="+sp_id);
		String exportName = "";
		if(r!=null){
			exportName = r.getStr("abbr");
		}
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,
				exportName);
		renderText(fileName);
	}
	
}
