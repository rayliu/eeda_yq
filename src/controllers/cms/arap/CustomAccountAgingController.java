package controllers.cms.arap;

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
import controllers.profile.LoginUserController;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomAccountAgingController extends Controller {
	private Log logger = Log.getLog(CustomAccountAgingController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/accountAging");
		 setAttr("listConfigList", configList);
		render("/eeda/cmsArap/customAccountAging/AccountAgingList.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sp_id = getPara("sp_id");
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
        		+ " 	sum(three) three, "
        		+ " 	sum(six) six, "
        		+ " 	sum(nine) nine,"
        		+ "		after_nine "
        		+ " FROM "
        		+ " 	( "
        		+ " 		select "
        		+ " joa.sp_id ,joa.currency_id ,p.abbr abbr_name,cur. NAME currency_name, "
        		+ " joa.total_amount, "
        		+ " IF (date_format(caco.confirm_stamp,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 0 MONTH),'%Y-%m') , joa.total_amount, 0 ) three,  "
        		+ " IF (date_format(caco.confirm_stamp,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') , joa.total_amount, 0 ) six, "
        		+ " IF (date_format(caco.confirm_stamp,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 2 MONTH),'%Y-%m') , joa.total_amount, 0 ) nine, "
        		+ " IF (date_format(caco.confirm_stamp,'%Y-%m')<date_format(DATE_SUB(curdate(), INTERVAL 3 MONTH),'%Y-%m') , joa.total_amount, 0 ) after_nine "
        		+ " from custom_plan_order_arap joa  "
        		+ " LEFT JOIN custom_plan_order jor ON jor.id = joa.order_id "
        		+ " LEFT JOIN party p ON  p.id = joa.sp_id "
        		+ " LEFT JOIN currency cur ON cur.id = joa.currency_id "
        		+ " LEFT JOIN custom_arap_charge_item caci on caci.ref_order_id=joa.id "
        		+ "  LEFT JOIN  custom_arap_charge_order caco on caco.id = caci.custom_charge_order_id and caco.`status`='已确认' "
        		+ "  LEFT JOIN custom_arap_charge_receive_item cacri on cacri.custom_charge_order_id=caco.id "
        		+ " where joa.order_type = 'charge' and ifnull(caco.audit_status,'') !='已收款' "
        		+ " and jor.office_id = "+office_id+"  and caco.confirm_stamp<date_format(curdate(),'%Y-%m')"
        		+ condition
        		+ " and jor.delete_flag = 'N'"
				+ " GROUP BY joa.id "
        		+ " 	) a "
        		+ " GROUP BY "
        		+ " 	sp_id, "
        		+ " 	currency_id ";
        
		
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
	
}
