package controllers.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AccountAuditLogController extends Controller {
    private Log logger = Log.getLog(AccountAuditLogController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	List<ArapAccountAuditLog> list = ArapAccountAuditLog.dao.find("SELECT DISTINCT source_order FROM arap_account_audit_log");
    	setAttr("List", list);
    	
    	List<ArapAccountAuditLog> accountlist = ArapAccountAuditLog.dao.find("SELECT DISTINCT a.bank_name FROM arap_account_audit_log aaa left join fin_account a on a.id = aaa.account_id");
    	setAttr("accountList", accountlist);
    	render("/tradeArap/AccountAuditLog/AccountAuditLogList.html");
    }

    public void list() {
    	String ids = getPara("ids");
    	String beginTime = getPara("beginTime");
    	String endTime = getPara("beginTime");
    	String sourceOrder = getPara("source_order");
    	String orderNo = getPara("orderNo");
    	String begin = getPara("begin");
    	String end = getPara("end");
    	String bankName = getPara("bankName");
    	String money = getPara("money");
    	String condiction = "";
    	//升降序
    	String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
		
		String orderByStr = " order by A.create_date desc ";
//        if(!StrKit.isBlank(colName)){
//        	orderByStr = " order by A."+colName+" "+sortBy;
//        }
    	if(ids != null && !"".equals(ids)){
    		condiction += " and account_id in("+ids+") ";
    	}
    	if(beginTime == null || "".equals(beginTime)){
    		beginTime = "1970-01-01";
    	}else{
    		beginTime = getPara("beginTime")+"-01";
    	}
    	if(endTime == null || "".equals(endTime)){
    		endTime = "2037-12-31";
    	}else{
    		endTime = getPara("beginTime")+"-31 23:59:59";
    	}
    	
    	
    	if(sourceOrder != null && !sourceOrder.equals("")){
    		condiction +=" and A.source_order ='" + sourceOrder + "' ";
    	}
    	if(orderNo != null && !orderNo.equals("")){
    		condiction +=" and A.order_no like '%" + orderNo + "%' ";
    	}
    	if(bankName != null && !bankName.equals("")){
    		condiction +=" and A.bank_name = '" + bankName + "' ";
    	}
    	if(money != null && !money.equals("")){
    		condiction +=" and A.amount like '%" + money + "%' ";
    	}
    	if(begin == null || "".equals(begin)){
    		condiction += " and A.create_date between '" + beginTime + "' ";
    	}else{
    		condiction += " and A.create_date between '" + begin + "' ";
    	}
    	if(end == null || "".equals(end)){
    		condiction += " and '" + endTime + "' ";
    	}else{
    		condiction += " and '" + end + "' ";
    	}
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " limit " + getPara("start") + ", " + getPara("length");
        }
        
        String sql = "";
        if(true){
        	sql = " select * from (select aaal.*,ifnull(ul.c_name, ul.user_name) user_name, fa.bank_name,"
        			+ " (CASE "
        			+ " WHEN aaal.source_order = '应收申请单' "
				    + " THEN "
				    + " (select aco.order_no FROM arap_charge_application_order aco where aco.id = aaal.invoice_order_id )"
				    + " WHEN aaal.source_order = '应付申请单' "
				    + " THEN "
				    + " (select aco.order_no FROM arap_cost_application_order aco where aco.id = aaal.invoice_order_id )"
				    + " end ) order_no,"
				    + " (select amount from arap_account_audit_log where id=aaal.id and payment_type='cost') cost_amount,"
				    + " (select amount from arap_account_audit_log where id=aaal.id and payment_type='charge') charge_amount"
				    + " from arap_account_audit_log aaal"
        			+ " left join user_login ul on ul.id = aaal.creator"
        			+ " left join fin_account fa on aaal.account_id = fa.id "
        			+ " ) A where office_id="+office_id;        	
        }

        Record rec = Db.findFirst("select count(*) total from ("+sql + condiction + ") B ");
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orders = Db.find(sql + condiction + orderByStr + sLimit);

        
        
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orders);
        renderJson(map);
    }
    
    //出纳日记帐：所有账户的按月期初期末总计
    public void accountList() {
    	String beginTime = getPara("beginTime");
    	int year = 0;
    	int month = 0;
    	if(beginTime == null || "".equals(beginTime)){
    		
    	}else{
    		year = Integer.parseInt(beginTime.substring(0, 4));
    		month = Integer.parseInt(beginTime.substring(5));
    	}
    	
    	String sLimit = "";
    	String pageIndex = getPara("sEcho");
    	if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
    		sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
    	}
    	UserLogin user = LoginUserController.getLoginUser(this);
    	long office_id = user.getLong("office_id");
    	
    	String sqlTotal = "select count(1) total from fin_account where office_id=?";
    	Record rec = Db.findFirst(sqlTotal, office_id);
    	 
    	String sql = " SELECT fa.office_id,fa.id,(select bank_name from fin_account where id = fa.id) bank_name,'"+ beginTime +"' date, "
    			+ " ( ( SELECT ROUND(ifnull(sum(amount), 0), 2)"
    			+ " FROM arap_account_audit_log aa"
    			+ " WHERE aa.account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '2015-01-01' AND '"+year+"-"+(month-1)+"-31 23:59:59' AND aa.payment_type = 'CHARGE' ) "
    			+ " - "
    			+ " ( SELECT ROUND(ifnull(sum(amount), 0), 2)"
    			+ " FROM arap_account_audit_log aa"
    			+ " WHERE account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '2015-01-01' AND '"+year+"-"+(month-1)+"-31 23:59:59' AND aa.payment_type = 'COST'"
    			+ " ) ) init_amount,"
    			+ " ( ( SELECT ROUND(ifnull(sum(amount), 0), 2)"
    			+ " FROM arap_account_audit_log aa "
    			+ " WHERE aa.account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '"+year+"-"+month+"-01' AND '"+year+"-"+month+"-31 23:59:59' AND aa.payment_type = 'CHARGE'"
    			+ " ) ) total_charge,"
    			+ " ( SELECT ROUND(ifnull(sum(amount), 0), 2)"
    			+ " FROM arap_account_audit_log aa"
    			+ " WHERE aa.account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '"+year+"-"+month+"-01' AND '"+year+"-"+month+"-31 23:59:59' AND aa.payment_type = 'COST'"
    			+ " ) total_cost,"
    			+ " ( ( SELECT ROUND(ifnull(sum(amount), 0), 2)"
    			+ " FROM arap_account_audit_log aa"
    			+ " WHERE aa.account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '2015-01-01' AND '"+year+"-"+month+"-31 23:59:59' AND aa.payment_type = 'CHARGE'  )"
    			+ "  - "
    			+ " ( SELECT ROUND(ifnull(sum(amount), 0), 2) FROM arap_account_audit_log aa  "
    			+ " WHERE aa.account_id = fa.id and aa.office_id = "+office_id+" AND aa.create_date BETWEEN '2015-01-01' AND '"+year+"-"+month+"-31 23:59:59' AND aa.payment_type = 'COST'"
    			+ " ) ) balance_amount"
    			+ " FROM arap_account_audit_log aal"
    			+ " right JOIN fin_account fa ON fa.id = aal.account_id"
    			+ " where fa.office_id="+office_id
    			+ " GROUP BY fa.id ";
    	
    	List<Record> BillingOrders = Db.find(sql+sLimit);
    	
    	Map BillingOrderListMap = new HashMap();
    	BillingOrderListMap.put("sEcho", pageIndex);
    	BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
    	BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));
    	
    	BillingOrderListMap.put("aaData", BillingOrders);
    	
    	renderJson(BillingOrderListMap);
    }
}
