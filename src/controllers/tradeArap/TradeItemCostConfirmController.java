package controllers.tradeArap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;
import models.eeda.tr.tradeJoborder.TradeJobOrderArap;

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
public class TradeItemCostConfirmController extends Controller {
	private Logger logger = Logger.getLogger(TradeItemCostConfirmController.class);
	Subject currentUser = SecurityUtils.getSubject();
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/tradeItemCostConfirmList");
        setAttr("listConfigList", configList);
		render("/tradeArap/ItemCostConfirm/list.html");
	}
	
	public void list() {
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or tjo.office_id in ("+relist.getStr("office_id")+")";
        }
        String sql = "SELECT*FROM(SELECT tjoa.*,tjo.order_no,tjo.order_export_date,tjo.create_stamp,tjo.customer_id,"
        		   + " p.abbr customer_name,p1.abbr sp_name,f. NAME charge_name,u. NAME unit_name,c. NAME currency_name,"
        		   + " c1. NAME exchange_currency_name FROM trade_job_order_arap tjoa"
        		   + " LEFT JOIN trade_job_order tjo ON tjo.id = tjoa.order_id"
        		   + " LEFT JOIN party p ON p.id = tjo.customer_id"
        		   + " LEFT JOIN party p1 ON p1.id = tjoa.sp_id"
        		   + " LEFT JOIN fin_item f ON f.id = tjoa.charge_id"
        		   + " LEFT JOIN unit u ON u.id = tjoa.unit_id"
        		   + " LEFT JOIN currency c ON c.id = tjoa.currency_id"
        		   + " LEFT JOIN currency c1 ON c1.id = tjoa.exchange_currency_id"
        		   + " WHERE tjoa.order_type = 'cost' and (tjo.office_id = "+office_id + ref_office+")"
        		   + " AND tjo.delete_flag = 'N') A WHERE 1 = 1 ";
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        List<Record> orderList = Db.find(sql+ condition + " order by "+ sName +" "+ sort);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
	}
	
	public void listTotal(){
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
//        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
//        String sColumn =  getPara("order[0][column]");
//        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
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
				+ " where joa.order_type='cost' and (jo.office_id = "+office_id + ref_office+")"
				+ " and jo.delete_flag = 'N'"
				+ " ) A where 1=1 ";
        String condition = DbUtils.buildConditions(getParaMap());
        List<Record>total_money = Db.find(sql_money +condition+" group by currency_name");
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("total_money", total_money);
        renderJson(map); 
	}
	
	public void costConfirm(){
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			TradeJobOrderArap joa = TradeJobOrderArap.dao.findFirst("select * from trade_job_order_arap tjoa where id = ?",idAttr[i]);
			joa.set("audit_flag", "Y");
			joa.update();
		}
		renderJson("{\"result\":true}");
	}
}
