package controllers.tms.arap;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class outputScaleController extends Controller {

	private Logger logger = Logger.getLogger(outputScaleController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/tms/arap/transCostConfirm/outputScaleList.html");
	}
     
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = "select * from( "
        		+ " select tjoa.*,tjo.id jobid,tjo.order_no,tjo.create_stamp,tjo.customer_id,p.company_name customer,p1.company_name sp_name,f.name charge_name,u.name unit_name,"
        		+ " CONVERT(substring(tjol.cabinet_date,1,10),char) cabinet_date,c.name currency_name, c1. NAME exchange_currency_name,tjo.container_no,tjo.so_no,cf.car_no "
				+ " from trans_job_order_arap tjoa "
				+ " right join trans_job_order tjo on tjo.id=tjoa.order_id "
				+ " LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjoa.order_id"
				+ " left join party p on p.id=tjo.customer_id "
				+ " left join party p1 on p1.id=tjoa.sp_id "
				+ " left join fin_item f on f.id=tjoa.charge_id "
				+ " left join carinfo cf on cf.id=tjoa.car_id "
				+ " left join unit u on u.id=tjoa.unit_id "
				+ " left join currency c on c.id=tjoa.currency_id "
				+ " LEFT JOIN currency c1 ON c1.id = tjoa.exchange_currency_id"
				+ " where ifnull(tjoa.order_type ,'')!='charge' and tjo.office_id = "+office_id
				 + " and tjo.delete_flag = 'N'"
					+ " ) A where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	
	public void costConfirm(){
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			Record re = Db.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
			long arapId = re.getLong("id");
			Db.update("UPDATE trans_job_order_arap SET audit_flag = 'Y' WHERE id=?",arapId);
		}
		renderJson("{\"result\":true}");
	}
   
}
