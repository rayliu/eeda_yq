package controllers.arap.ar;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.eeda.ListConfigController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChargeConfirmController extends Controller {

	private Logger logger = Logger.getLogger(ChargeConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {		
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/chargeConfirmList");
        setAttr("listConfigList", configList);
		render("/eeda/arap/ChargeConfirm/ChargeConfirmList.html");
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
        
        String customer_id = getPara("customer_id");
        Long userId = LoginUserController.getLoginUserId(this);
        if(StringUtils.isNotEmpty(getPara("customer_id"))){
    		//常用party查询保存进入历史记录
          	JobOrderController.addHistoryRecord(userId,customer_id,"ARAP_COM");
    	}
        
        String sp_id = getPara("sp_id");
        if(StringUtils.isNotEmpty(getPara("sp_id"))){
    		//常用party查询保存进入历史记录
          	JobOrderController.addHistoryRecord(userId,sp_id,"ARAP_COM");
    	}
        
        
        
        String sql = "select * from( "
        		+ " select joa.*,joa.type type_name,jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id,p.abbr customer_name,p1.abbr sp_name,f.name charge_name,u.name unit_name,"
        		+ " c.name currency_name, c1. NAME exchange_currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join party p on p.id=jo.customer_id "
				+ " left join party p1 on p1.id=joa.sp_id "
				+ " left join fin_item f on f.id=joa.charge_id "
				+ " left join unit u on u.id=joa.unit_id "
				+ " left join currency c on c.id=joa.currency_id "
				+ " LEFT JOIN currency c1 ON c1.id = joa.exchange_currency_id"
				+ " where joa.order_type='charge' and (jo.office_id = "+office_id + ref_office+")"
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
	
	public void listTotal(){
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
        String sql_money = "select ifnull(sum(price),0) sum_price,A.currency_name from( "
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
				+ " where joa.order_type='charge' and (jo.office_id = "+office_id + ref_office+")"
				+ " and jo.delete_flag = 'N'"
				+ " ) A where 1=1 ";
        String condition = DbUtils.buildConditions(getParaMap());
        List<Record>total_money = Db.find(sql_money +condition+" group by currency_name");
        Map map = new HashMap();
        map.put("total_money", total_money);
        renderJson(map); 
	}
	
	
	
//    public void list() {
//        String sLimit = "";
//        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
//        
//        UserLogin user = LoginUserController.getLoginUser(this);
//        long office_id=user.getLong("office_id");
//     
//        String sql = "select * from( "
//        		+ " select joa.*,jo.order_no,jo.id jobid,jo.create_stamp,jo.customer_id,p.company_name customer,p1.company_name sp_name,f.name charge_name,u.name unit_name,c.name currency_name "
//				+ " from job_order_arap joa "
//				+ " left join job_order jo on jo.id=joa.order_id "
//				+ " left join party p on p.id=jo.customer_id "
//				+ " left join party p1 on p1.id=joa.sp_id "
//				+ " left join fin_item f on f.id=joa.charge_id "
//				+ " left join unit u on u.id=joa.unit_id "
//				+ " left join currency c on c.id=joa.currency_id "
//				+ " where joa.order_type='charge' and jo.office_id = "+office_id
//				+ " and jo.delete_flag = 'N'"
//				+ " ) A where 1=1 ";
//        
//        String condition = DbUtils.buildConditions(getParaMap());
//
//        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
//        Record rec = Db.findFirst(sqlTotal);
//        logger.debug("total records:" + rec.getLong("total"));
//        
//        List<Record> orderList = Db.find(sql+ condition  +sLimit);
//        Map orderListMap = new HashMap();
//        orderListMap.put("draw", pageIndex);
//        orderListMap.put("recordsTotal", rec.getLong("total"));
//        orderListMap.put("recordsFiltered", rec.getLong("total"));
//
//        orderListMap.put("data", orderList);
//
//        renderJson(orderListMap); 
//    }   
//	
//	public void listTotal(){
//		String sLimit = "";
//        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }
//        
//        UserLogin user = LoginUserController.getLoginUser(this);
//        long office_id=user.getLong("office_id");
//        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
//        String sColumn =  getPara("order[0][column]");
//        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
//        String ref_office = "";
//        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
//        if(relist!=null){
//        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
//        }
//        String sql_money = "select ifnull(sum(price),0) sum_price,A.currency_name from( "
//        		+ " select joa.*,jo.id jobid,jo.order_no,jo.order_export_date,jo.create_stamp,jo.customer_id,p.abbr customer_name,p1.abbr sp_name,f.name charge_name,u.name unit_name,"
//        		+ " c.name currency_name, c1. NAME exchange_currency_name "
//				+ " from job_order_arap joa "
//				+ " left join job_order jo on jo.id=joa.order_id "
//				+ " left join party p on p.id=jo.customer_id "
//				+ " left join party p1 on p1.id=joa.sp_id "
//				+ " left join fin_item f on f.id=joa.charge_id "
//				+ " left join unit u on u.id=joa.unit_id "
//				+ " left join currency c on c.id=joa.currency_id "
//				+ " LEFT JOIN currency c1 ON c1.id = joa.exchange_currency_id"
//				+ " where joa.order_type='cost' and (jo.office_id = "+office_id + ref_office+")"
//				+ " and jo.delete_flag = 'N'"
//				+ " ) A where 1=1 ";
//        String condition = DbUtils.buildConditions(getParaMap());
//        List<Record>total_money = Db.find(sql_money +condition+" group by currency_name");
//        Map map = new HashMap();
//        map.put("total_money", total_money);
//        renderJson(map); 
//	}
    
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
