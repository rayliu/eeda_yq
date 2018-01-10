package controllers.arap.cmsAr;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.cms.CustomPlanOrderArap;

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
public class CmsCostConfirmController extends Controller {

	private Logger logger = Logger.getLogger(CmsCostConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {		
		render("/eeda/cmsArap/cmsCostConfirm/cmsCostConfirmList.html");
	}
	
    public void list() {
        //String sLimit = "";
        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
   			return;
   		}
        long office_id=user.getLong("office_id");
        
//        String ref_office = "";
//        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
//        if(relist!=null){
//        	ref_office = " or cpo.office_id in ("+relist.getStr("office_id")+")";
//        }
     
        String sql = "select * from( "
        		+"  select cpoa.*,cpo.order_no,cpo.id cpobid,cpo.create_stamp,p1.company_name sp_name,cpo.type job_type,f.name charge_name,"
        		+"  cpo.date_custom,c.name currency_name  "
				+" 	from custom_plan_order_arap cpoa  "
				+" 	right join custom_plan_order cpo on cpo.id=cpoa.order_id  "
				+" 	left join party p1 on p1.id=cpoa.sp_id  "
				+" 	left join fin_item f on f.id=cpoa.charge_id  "
				+" 	left join currency c on c.id=cpoa.currency_id  "
				+" 	where cpoa.order_type ='cost' and (cpo.office_id="+office_id+ " or cpo.to_office_id ="+office_id +") and cpo.delete_flag = 'N'"				
				+ " ) A where 1=1 ";
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition  );//不分页+sLimit
        Map<String,Object> orderListMap = new HashMap<String,Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }   
    

	  public void chargeConfirm(){
		  String ids = getPara("itemIds");
			String idAttr[] = ids.split(",");
			for(int i=0 ; i<idAttr.length ; i++){
				CustomPlanOrderArap joa = CustomPlanOrderArap.dao.findFirst("select * from custom_plan_order_arap joa where id = ?",idAttr[i]);
				joa.set("audit_flag", "Y");
				joa.update();
			}
			renderJson("{\"result\":true}");
		}


}
