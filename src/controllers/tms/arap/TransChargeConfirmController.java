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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransChargeConfirmController extends Controller {

	private Logger logger = Logger.getLogger(TransChargeConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {	
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/transChargeConfirm");
        setAttr("listConfigList", configList);	
		render("/tms/arap/transChargeConfirm/transChargeConfirmList.html");
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
        		+ " select tjoa.*,tjo.order_no,tjo.id jobid,tjo.container_no,tjo.so_no,CONVERT(substring(GROUP_CONCAT(tjol.cabinet_date), 1, 10),,char) create_stamp,tjo.customer_id,tjo.cabinet_type,"
        		+ " CONVERT(substring(tjol.cabinet_date,1,10),char) cabinet_date,p.company_name customer,p1.company_name sp_name,f.name charge_name,u.name unit_name,c.name currency_name "
				+ " from trans_job_order_arap tjoa "
				+ " right join trans_job_order tjo on tjo.id=tjoa.order_id "
				+ " LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjoa.order_id"
				+ " left join party p on p.id=tjo.customer_id "
				+ " left join party p1 on p1.id=tjoa.sp_id "
				+ " left join fin_item f on f.id=tjoa.charge_id "
				+ " left join unit u on u.id=tjoa.unit_id "
				+ " left join currency c on c.id=tjoa.currency_id "
				+ " where ifnull(tjoa.order_type ,'')!='cost' and tjo.office_id = "+office_id
				 + " and tjo.delete_flag = 'N'"
				 + " group by tjoa.id"
					+ " ) A where 1=1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        //+sLimit
        List<Record> orderList = Db.find(sql+ condition  );
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
				Record re = Db.findFirst("select * from trans_job_order_arap tjoa where id = ?",idAttr[i]);
				long arapId = re.getLong("id");
				Db.update("UPDATE trans_job_order_arap SET audit_flag ='Y' WHERE id = ?",arapId);
			}
			renderJson("{\"result\":true}");
		}


}
