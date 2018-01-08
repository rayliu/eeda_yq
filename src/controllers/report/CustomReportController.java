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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PermissionConstant;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomReportController extends Controller {
    private Log logger = Log.getLog(CustomReportController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/customReport");
        setAttr("listConfigList", configList);
    	render("eeda/statusReport/customReport.html");
    }

    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");

        
    	String sql = "select * from(select p.abbr sp_name,date(jor.create_stamp) create_stamp,joa.sp_id sp_id,joa.order_type,ifnull(joa.currency_total_amount,0) total_amount,"
    			+ " (select GROUP_CONCAT(custom_order_no SEPARATOR '/n') from job_order_custom where order_id = joa.order_id) custom_order_no"
    			+ " from job_order_arap joa"
    			+ " LEFT JOIN party p on p.id = joa.sp_id"
    			+ " LEFT JOIN job_order jor on jor.id = joa.order_id" 
    			+ " where joa.type = '报关' "
    			+ " and jor.office_id="+office_id
    			 + " and jor.delete_flag = 'N'"
 				+ " )A where 1=1" ;
    	
    	String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlTotal = "select count(1) total from ("+sql+condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+condition +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);
        renderJson(orderListMap); 
    }
    
	public void downloadExcelList(){
	    UserLogin user = LoginUserController.getLoginUser(this);
	    if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
    	String sql = "select * from(select p.abbr sp_name,date(jor.create_stamp) create_stamp,joa.sp_id sp_id,joa.order_type,ifnull(joa.currency_total_amount,0) total_amount,"
    			+ " (select GROUP_CONCAT(custom_order_no SEPARATOR '/n') from job_order_custom where order_id = joa.order_id) custom_order_no"
    			+ " from job_order_arap joa"
    			+ " LEFT JOIN party p on p.id = joa.sp_id"
    			+ " LEFT JOIN job_order jor on jor.id = joa.order_id" 
    			+ " where joa.type = '报关' "
    			+ " and jor.office_id="+office_id
    			 + " and jor.delete_flag = 'N'"
 				+ " )A where 1=1" ;
    	
    	String condition = DbUtils.buildConditions(getParaMap());
        
        String sqlExport = sql+condition;
		String total_name_header = "公司名称,日期,提单号,报关单号,报关代理费,报关+报检单录入费,上传费,商检费,消毒费,入闸费,"
                                    +"综合费,单证更改费,港建费,码头费,堆存/电费/港务费,DOC/THC费,代理运费,合计（元）";
		String[] headers = total_name_header.split(",");

		String[] fields = { "SP_NAME", "CREATE_STAMP", "", "CUSTOM_ORDER_NO",
				"", "", "","","","","","","","","","","","TOTAL_AMOUNT"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
}
