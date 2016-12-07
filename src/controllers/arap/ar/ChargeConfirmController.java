package controllers.arap.ar;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;

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
public class ChargeConfirmController extends Controller {

	private Logger logger = Logger.getLogger(ChargeConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {		
		render("/eeda/cmsArap/ChargeConfirm/ChargeConfirmList.html");
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
        		+ " select joa.*,jo.order_no,jo.id jobid,jo.create_stamp,jo.customer_id,p.company_name customer,p1.company_name sp_name,f.name charge_name,u.name unit_name,c.name currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join party p on p.id=jo.customer_id "
				+ " left join party p1 on p1.id=joa.sp_id "
				+ " left join fin_item f on f.id=joa.charge_id "
				+ " left join unit u on u.id=joa.unit_id "
				+ " left join currency c on c.id=joa.currency_id "
				+ " where joa.order_type='charge' and jo.office_id = "+office_id
				+ " ) A where 1=1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
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
    
//    public void chargeConfirm(){
//		String ids = getPara("itemIds");
//		String idAttr[] = ids.split(",");
//		for(int i=0 ; i<idAttr.length ; i++){
//			JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap where id = ?",idAttr[i]);
//			joa.set("audit_flag", "Y");
//			joa.update();
//		}
//		renderJson("{\"result\":true}");
//	}
	  public void chargeConfirm(){
		  String ids = getPara("itemIds");
			String idAttr[] = ids.split(",");
			for(int i=0 ; i<idAttr.length ; i++){
				JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
				joa.set("audit_flag", "Y");
				joa.update();
			}
			renderJson("{\"result\":true}");
		}


}
