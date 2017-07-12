package controllers.arap.ap;

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

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostItemConfirmController extends Controller {

	private Logger logger = Logger.getLogger(CostItemConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {

		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/costConfirmList");
        setAttr("listConfigList", configList);
		render("/eeda/arap/CostItemConfirm/CostItemConfirm.html");
	}
     
	public void list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String sql = "select * from( "
        		+ " select joa.*,jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id,p.abbr customer_name,p1.abbr sp_name,f.name charge_name,u.name unit_name,"
        		+ " c.name currency_name, c1. NAME exchange_currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join party p on p.id=jo.customer_id "
				+ " left join party p1 on p1.id=joa.sp_id "
				+ " left join fin_item f on f.id=joa.charge_id "
				+ " left join unit u on u.id=joa.unit_id "
				+ " left join currency c on c.id=joa.currency_id "
				+ " LEFT JOIN currency c1 ON c1.id = joa.exchange_currency_id"
				+ " where joa.order_type='cost' and (jo.office_id = "+office_id + ref_office+")"
				+ " and jo.delete_flag = 'N'"
				+ " ) A where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by "+ sName +" "+ sort);
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
			JobOrderArap joa = JobOrderArap.dao.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
			joa.set("audit_flag", "Y");
			joa.update();
		}
		renderJson("{\"result\":true}");
	}
   
}
