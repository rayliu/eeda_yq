package controllers.tr.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TradeArapReportController extends Controller {
	private Log logger = Log.getLog(TradeArapReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/tradeArapReport");
		setAttr("listConfigList", configList);
		render("/tr/arap/ArapReport/ArapReport.html");
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
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "	SELECT"
        		+"		*"
        		+"	FROM"
        		+"		("
        		+"			SELECT"
        		+"				tjoa.id,"
        		+"				tjoa.type,"
        		+"				tjoa.order_type,"
        		+"				tjoa.sp_id,"
        		+"				ifnull(tjoa.total_amount, 0) total_amount,"
        		+"				tjoa.charge_id fin_item_id,"
        		+"				("
        		+"					CASE"
        		+"					WHEN tjoa.pay_flag = 'Y' THEN"
        		+"						'费用已结算'"
        		+"					WHEN tjoa.create_flag = 'Y' THEN"
        		+"						'费用已创建申请单'"
        		+"					WHEN tjoa.billConfirm_flag = 'Y' THEN"
        		+"						'费用已创建对账单并已确认'"
        		+"					WHEN tjoa.bill_flag = 'Y' THEN"
        		+"						'费用已创建对账单'"
        		+"					WHEN tjoa.audit_flag = 'Y' THEN"
        		+"						'费用已确认'"
        		+"					ELSE"
        		+"						'费用未确认'"
        		+"					END"
        		+"				) flag,"
        		+"				tjoa.exchange_rate,"
        		+"				tjoa.exchange_currency_rate,"
        		+"				ifnull("
        		+"					tjoa.currency_total_amount,"
        		+"					0"
        		+"				) currency_total_amount,"
        		+"				ifnull("
        		+"					tjoa.exchange_total_amount,"
        		+"					0"
        		+"				) exchange_total_amount,"
        		+"				tjo.id jobid,"
        		+"				tjo.order_no,"
        		+"				tjo.order_export_date,"
        		+"				tjo.create_stamp,"
        		+"				tjo.customer_id,"
        		+"				p.abbr sp_name,"
        		+"				p1.abbr customer_name,"
        		+"				f. NAME fin_name,"
        		+"				cur1. NAME exchange_currency_name,"
        		+"				cur. NAME currency_name,"
        		+"				IFNULL("
        		+"					taoCharge.order_no,"
        		+"					taoCost.order_no"
        		+"				) check_order_no,"
        		+"				IFNULL("
        		+"					tcaoCharge.order_no,"
        		+"					tcaoCost.order_no"
        		+"				) application_order_no,"
        		+"				taoCharge.id charge_order_id,"
        		+"				taoCost.id cost_order_id,"
        		+"				tcaoCharge.id charge_application_order_id,"
        		+"				tcaoCost.id cost_charge_application_order_id"
        		+"			FROM"
        		+"				trade_job_order_arap tjoa"
        		+"			LEFT JOIN trade_job_order tjo ON tjo.id = tjoa.order_id"
        		+"			LEFT JOIN party p ON p.id = tjoa.sp_id"
        		+"			LEFT JOIN party p1 ON p1.id = tjo.customer_id"
        		+"			LEFT JOIN currency cur ON cur.id = tjoa.currency_id"
        		+"			LEFT JOIN currency cur1 ON cur1.id = tjoa.exchange_currency_id"
        		+"			LEFT JOIN fin_item f ON f.id = tjoa.charge_id"
        		+"			LEFT JOIN trade_arap_charge_item taCharge ON taCharge.ref_order_id = tjoa.id"
        		+"			LEFT JOIN trade_arap_charge_order taoCharge ON taoCharge.id = taCharge.charge_order_id"
        		+"			LEFT JOIN trade_arap_cost_item taCost ON taCost.ref_order_id = tjoa.id"
        		+"			LEFT JOIN trade_arap_cost_order taoCost ON taoCost.id = taCost.cost_order_id"
        		+"			LEFT JOIN trade_charge_application_order_rel tcaCharge ON tcaCharge.job_order_arap_id = tjoa.id"
        		+"			LEFT JOIN trade_arap_charge_application_order tcaoCharge ON tcaoCharge.id = tcaCharge.application_order_id"
        		+"			LEFT JOIN trade_cost_application_order_rel tcaCost ON tcaCost.job_order_arap_id = tjoa.id"
        		+"			LEFT JOIN trade_arap_cost_application_order tcaoCost ON tcaoCost.id = tcaCost.application_order_id"
        		+"			WHERE"
        		+"			tjo.office_id = 7 AND tjo.delete_flag = 'N' AND tjo.delete_flag = 'N'"
        		+"			GROUP BY tjoa.id"
        		+"		) B"
        		+"			WHERE 1 = 1";
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by order_export_date desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}
