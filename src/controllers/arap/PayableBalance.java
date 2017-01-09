package controllers.arap;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PayableBalance extends Controller {
	private Log logger = Log.getLog(PayableBalance.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/eeda/arap/PayableBalance/PayableBalance.html");
	}
	
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = " SELECT * FROM ("
        		+" SELECT acao.sp_id sp," 
        		+"  p.abbr,"
        		+"  acao.begin_time,"
        		+"  acao.end_time,"
        		+"   'CNY' currency,"
        		+" 	SUM(modal_cny) cost_total,"
        		+"   IFNULL((SELECT SUM(modal_cny)"
        		+" 	from arap_cost_application_order aca LEFT JOIN party p on aca.sp_id = p.id "
        		+" 	WHERE aca.status = '已付款' and aca.sp_id = sp "+ condition
        		+" 	),0) cost_confirm "
        		+" from arap_cost_application_order acao LEFT JOIN party p on acao.sp_id = p.id "
        		+" WHERE acao.office_id = 1 "
        		+" GROUP BY acao.sp_id"
        		+" union "
        		+" SELECT acao.sp_id sp," 
        		+"   p.abbr,"
        		+"  acao.begin_time,"
        		+"  acao.end_time,"
        		+"   'USD' currency,"
        		+" 	SUM(modal_usd) cost_total,"
        		+"   IFNULL((SELECT SUM(modal_usd)"
        		+" 	from arap_cost_application_order aca LEFT JOIN party p on aca.sp_id = p.id "
        		+" 	WHERE aca.status = '已付款' and aca.sp_id = sp "+ condition
        		+" 	),0) cost_confirm"
        		+" from arap_cost_application_order acao LEFT JOIN party p on acao.sp_id = p.id "
        		+" WHERE acao.office_id = 1 "
        		+" GROUP BY acao.sp_id"
        		+" UNION"
        		+" SELECT acao.sp_id sp," 
        		+"   p.abbr,"
        		+"  acao.begin_time,"
        		+"  acao.end_time,"
        		+"   'JPY' currency,"
        		+" 	SUM(modal_jpy) cost_total,"
        		+"   IFNULL((SELECT SUM(modal_jpy)"
        		+" 	from arap_cost_application_order aca LEFT JOIN party p on aca.sp_id = p.id "
        		+" 	WHERE aca.status = '已付款' and aca.sp_id = sp "+ condition
        		+" 	),0) cost_confirm"
        		+" from arap_cost_application_order acao LEFT JOIN party p on acao.sp_id = p.id "
        		+" WHERE acao.office_id = 1 "
        		+" GROUP BY acao.sp_id"
        		+" UNION"
        		+" SELECT acao.sp_id sp," 
        		+"   p.abbr,"
        		+"  acao.begin_time,"
        		+"  acao.end_time,"
        		+"   'HKD' currency,"
        		+" 	SUM(modal_hkd) cost_total,"
        		+"   IFNULL((SELECT SUM(modal_hkd)"
        		+" 	from arap_cost_application_order aca LEFT JOIN party p on aca.sp_id = p.id "
        		+" 	WHERE aca.status = '已付款' and aca.sp_id = sp "+ condition
        		+" 	),0) cost_confirm"
        		+" from arap_cost_application_order acao LEFT JOIN party p on acao.sp_id = p.id "
        		+" WHERE acao.office_id ="+ office_id + condition
        		+" GROUP BY acao.sp_id"
        		+" ) A where cost_total!=0"+ condition +" ORDER BY abbr";
		
        String sqlTotal = "select count(1) total from ("+sql+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
}
