package controllers.cms.expenseEntry;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.arap.cmsAr.CmsChargeCheckOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ExpenseEntryController extends Controller {

	private Logger logger = Logger.getLogger(CmsChargeCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/cmsArap/expenseEntry/ExpenseEntryList.html");
	}
	
	public void list() {
    	String checked = getPara("checked");
    	
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
        String sql = "";
        String checkCondition = "";
        if(!"Y".equals(checked)){
        	checkCondition = "and cpoa.order_type='charge'";
        }

		sql = " SELECT * from ( "
				+" SELECT "
				+" 	cpo.receive_sent_consignee_input customer_name, "
				+" 	cpo.order_no, "
				+" 	cpo.create_stamp,"
				+ " cpo.date_custom, "
				+" 	cpo.tracking_no, "
				+" 	cpoa.*, GROUP_CONCAT( "
				+" 		( "
				+" 			SELECT "
				+" 				CONCAT( "
				+" 					f.`name`, "
				+" 					':', "
				+" 					( "
				+" 						SELECT "
				+" 							FORMAT(cpoa1.total_amount, 2) "
				+" 					) "
				+" 				) "
				+" 			FROM "
				+" 				custom_plan_order_arap cpoa1 "
				+" 			WHERE "
				+" 				cpoa1.order_type = 'charge' "
				+" 			AND cpoa1.id = cpoa.id "
				+" 		) SEPARATOR '<br/>' "
				+" 	) charge_msg, "
				+" 	GROUP_CONCAT( "
				+" 		( "
				+" 			SELECT "
				+" 				CONCAT( "
				+" 					f.`name`, "
				+" 					':', "
				+" 					( "
				+" 						SELECT "
				+" 							FORMAT(cpoa2.total_amount, 2) "
				+" 					) "
				+" 				) "
				+" 			FROM "
				+" 				custom_plan_order_arap cpoa2 "
				+" 			WHERE "
				+" 				cpoa2.order_type = 'cost' "
				+" 			AND cpoa2.id = cpoa.id "
				+" 		) SEPARATOR '<br/>' "
				+" 	) cost_msg "
				+" FROM "
				+" 	custom_plan_order_arap  cpoa"
				
				+" LEFT JOIN custom_plan_order cpo ON cpo.id = cpoa.order_id "
				+" LEFT JOIN fin_item f ON f.id = cpoa.charge_id "
				+" WHERE "
				+" tracking_no is not null and (cpo.office_id = "+office_id+" or to_office_id="+office_id+")"
				 +" and cpo.delete_flag='N' "
				 +" GROUP BY "
				+" 	cpoa.order_id "
				+" ORDER BY create_stamp desc "
				+" )A where 1=1 ";

        
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
}
