package controllers.tms.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapAccountAuditLog;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.RateContrast;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.profile.Currency;
import models.eeda.tms.TransArapChargeOrder;
import models.eeda.tms.TransArapChargeReceiveItem;
import models.eeda.tms.TransArapCostItem;
import models.eeda.tms.TransArapCostOrder;
import models.eeda.tms.TransArapCostReceiveItem;
import models.eeda.tms.TransJobOrderArap;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransCostCheckOrderController extends Controller {
	private Log logger = Log.getLog(TransCostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/transCostCheckOrder");
        setAttr("listConfigList", configList);	
		render("/tms/arap/transCostCheckOrder/tmsCostCheckOrder.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void create(){
		
		String ids = getPara("itemId");//job_order_arap ids
		String totalAmount = getPara("totalAmount");
		String cny_totalAmount = getPara("cny_totalAmount");
		String usd_totalAmount = getPara("usd_totalAmount");
		String hkd_totalAmount = getPara("hkd_totalAmount");
		String jpy_totalAmount = getPara("jpy_totalAmount");
//		String exchange_totalAmount = getPara("exchange_totalAmount");
//		String exchange_cny_totalAmount = getPara("exchange_cny_totalAmount");
//		String exchange_usd_totalAmount = getPara("exchange_usd_totalAmount");
//		String exchange_hkd_totalAmount = getPara("exchange_hkd_totalAmount");
//		String exchange_jpy_totalAmount = getPara("exchange_jpy_totalAmount");
		
		String strAry[] = ids.split(",");
		String id = strAry[0];
		String sql = " select joa.sp_id,joa.car_id,p.abbr company_abbr,p.company_name company_name from trans_job_order_arap joa "
				   + " left join party p on p.id = joa.sp_id "
				   + "  where joa.id = ? ";
		Record spRec = Db.findFirst(sql,id);
		Record order = new Record();
		order.set("sp_id", spRec.get("sp_id"));
		order.set("car_id", spRec.get("car_id"));
		order.set("company_name", spRec.get("company_name"));
		order.set("company_abbr",spRec.get("company_abbr"));
		order.set("total_amount",totalAmount);
		order.set("jpy", jpy_totalAmount);
		order.set("cny", cny_totalAmount);
		order.set("usd", usd_totalAmount);
		order.set("hkd",hkd_totalAmount);
		
		order.set("ids",ids);
		order.set("creator_name", LoginUserController.getLoginUserName(this));
		order.set("item_list", getItemList(ids,"",""));
		order.set("currencyList", getCurrencyList(ids,""));
		setAttr("order", order);
		UserLogin u3=LoginUserController.getLoginUser(this);
		if (u3==null) {
            return;
        }
		setAttr("user",u3);		
		render("/tms/arap/transCostCheckOrder/tmsCostCheckOrderEdit.html");
	}
	
	
	public List<Record> getCurrencyList(String ids,String order_id){
    	String sql = "SELECT "
    			+ " (select rc.id from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
    			+ " ifnull((select rc.new_rate from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),cast(joa.exchange_rate as char)) new_rate"
				+ " FROM trans_job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
    	List<Record> re = Db.find(sql);
    	
    	return re;
	}
	
	//报关例外，获取每次收款的记录
    public List<Record> getReceiveItemList(String order_id){
    	String sql = null;
   		 sql = "  SELECT caci.*,c.`name` currency_name ,ul.c_name receive_name"
   				+" from trans_arap_cost_order caco  "
   				+" left join trans_arap_cost_receive_item caci on caci.charge_order_id = caco.id "
   				+" LEFT JOIN currency c on c.id=caci.currency_id "
   				+ "  LEFT JOIN user_login ul ON ul.id = caci.confirm_by"
   				+" where caci.charge_order_id ="+order_id+" order by caci.id desc";
    	List<Record> re = Db.find(sql);
    	return re;
    }
	
	public List<Record> getItemList(String ids,String order_id, String code){
		String sql = null;
		String currency_code="";
		if(StringUtils.isNotEmpty(code)){
			currency_code=" and cur. NAME="+"'"+code+"'";
		}
		
		if(StringUtils.isEmpty(order_id)){
			sql = " select joa.id,joa.type,joa.sp_id, joa.order_type,joa.remark, joa.total_amount, joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name, jo.container_no,jo.so_no, "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),cast(joa.exchange_rate as char)) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
	                + " cur.name currency_name,"
	                + " ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
                    + " ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
                    + " ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount,cf.car_no, "
                    + " fi.name fin_name ,tjoli.cabinet_date"
	                + " from trans_job_order_arap joa "
	                + " left join trans_job_order jo on jo.id=joa.order_id "
	                + " left join party p on p.id=joa.sp_id "
	                + " left join party p1 on p1.id=jo.customer_id "
	                + " left join currency cur on cur.id=joa.currency_id "
	                + " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	                + " left join carinfo cf on cf.id=joa.car_id "
	                + " LEFT JOIN trans_job_order_land_item tjoli on tjoli.order_id=jo.id"
	                +" left join fin_item fi on joa.charge_id = fi.id "
	                + " where joa.audit_flag='Y' and joa.bill_flag='N' and joa.id in("+ids+") "
	                + " and jo.delete_flag = 'N'"
					+ " GROUP BY joa.id"
	                + " ORDER BY jo.order_no";	
		}else{
			sql = " select joa.id,joa.type,joa.sp_id,joa.remark,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
			        + " aco.order_no check_order_no, jo.order_no,jo.id job_order_id,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name,jo.container_no,jo.so_no,  "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
	                + " cur.name currency_name, "
	                + " ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
	                + " ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
	                + " fi.name fin_name ,"
	                + " ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount ,tjoli.cabinet_date,cf.car_no "
	                + " from trans_job_order_arap joa "
	                + " left join trans_job_order jo on jo.id=joa.order_id "
	                + " left join party p on p.id=joa.sp_id "
	                + " left join party p1 on p1.id=jo.customer_id "
	                + " left join currency cur on cur.id=joa.currency_id "
	                + " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	                + " left join trans_arap_cost_item aci on aci.ref_order_id = joa.id"
					+ " left join trans_arap_cost_order aco on aco.id = aci.cost_order_id "
	                + " left join carinfo cf on cf.id=joa.car_id "
					+ " left join fin_item fi on joa.charge_id = fi.id "
					+ " LEFT JOIN trans_job_order_land_item tjoli on tjoli.order_id=jo.id"
					+ " where joa.id = aci.ref_order_id and aco.id in ("+order_id+")"+currency_code
					 + " and jo.delete_flag = 'N'"
						+ " GROUP BY joa.id "
	                + " ORDER BY aco.order_no, jo.order_no ";
		}
    	
    	List<Record> re = Db.find(sql);
    	return re;
    }
	
	public List<Record> getCostItemList(String order_ids,String bill_flag,String code,String exchange_currency,String fin_name){
    	String sql = null;
    	String currency_code="";
    	String query_exchange_currency="";
    	String query_fin_name="";
		if(StringUtils.isNotEmpty(code)){
			currency_code=" and cur. NAME="+"'"+code+"'";
		}
		if(StringUtils.isNotEmpty(exchange_currency)){
			String sql2="select id from currency where currency.name='"+exchange_currency+"'";
			List<Record> re=Db.find(sql2);
			query_exchange_currency=" and joa. exchange_currency_id="+re.get(0).get("id");
		}
		if(StringUtils.isNotEmpty(fin_name)){
			query_fin_name=" and fi.id="+fin_name;
		}
			if("create".equals(bill_flag)){
				sql = " select joa.id,joa.create_flag,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, "
							+" from trans_job_order jo"
							+" left join trans_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							//tms申请单需新建rel表
							+" left join cost_application_order_rel caol on caol.job_order_arap_id  = joa.id"
							+" left join arap_cost_application_order acao on caol.application_order_id = acao.id"
							 +" left join arap_cost_order aco on aco.id=caol.cost_order_id"
						  +" where acao.id="+order_ids+query_fin_name
						  + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
				
			}else{
				sql = " select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, "
							+" from trans_job_order jo"
							+" left join trans_job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							
							+" left join arap_cost_item aci on aci.ref_order_id = joa.id"
						    +" left join arap_cost_order aco on aco.id = aci.cost_order_id"
						    +" where joa.id = aci.ref_order_id and joa.create_flag='N' and aco.id in ("+order_ids+")"
						    +currency_code
						    +query_exchange_currency+query_fin_name
						    + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
			}		
			

    	List<Record> re = Db.find(sql);
    	
    	return re;
    }
	
	
	
	
	
	
	
	public void createList() {
		String ids = getPara("itemIds");
		String order_id = getPara("order_id")==null?"":getPara("order_id");
		String sLimit = "";
		String pageIndex = getPara("draw");
		if (getPara("start") != null && getPara("length") != null) {
			sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
		}
		
		String sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
				+ " p.abbr sp_name,p1.abbr customer_name, "
				+ " cur.name currency_name "
				+ " from trans_job_order_arap joa "
				+ " left join trans_job_order jo on jo.id=joa.order_id "
				+ " left join party p on p.id=joa.sp_id "
				+ " left join party p1 on p1.id=jo.customer_id "
				+ " left join currency cur on cur.id=joa.currency_id "
				+ " where joa.id in ( "+ids+" ) "
				 + " and jo.delete_flag = 'N'"
					+ " GROUP BY joa.id ";
				
		
		String sqlTotal = "select count(1) total from ("+sql+") C";
		Record rec = Db.findFirst(sqlTotal);
		logger.debug("total records:" + rec.getLong("total"));
		
		List<Record> orderList = Db.find(sql + " order by create_stamp desc " +sLimit);
		Map map = new HashMap();
		map.put("draw", pageIndex);
		map.put("recordsTotal", rec.getLong("total"));
		map.put("recordsFiltered", rec.getLong("total"));
		map.put("data", orderList);
		renderJson(map); 
		
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
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = " SELECT	* FROM	"
        			+" 	(	"
        			+" 		SELECT	"
        			+" 			joa.order_type sql_type,joa.id,joa.remark,	"
        			+" 			joa.type,	"
        			+" 			joa.sp_id,	"
        			+" 			ifnull(joa.total_amount, 0) total_amount,	"
        			+" 			ifnull(	"
        			+" 				joa.currency_total_amount,	"
        			+" 				0	"
        			+" 			) currency_total_amount,	"
        			+" 			jo.id jobid,	"
        			+" 			jo.order_no,	"
        			+" 			jo.create_stamp,	"
        			+" 			jo.land_export_date,	"
        			+" 			jo.customer_id,	"
        			+" 			jo.volume,	"
        			+" 			jo.net_weight,	"
        			+" 			jo.ref_no,jo.so_no,jo.container_no,		"
        			+" 			p.abbr sp_name,	"
        			+" 			p1.abbr customer_name,	"
        			+" 			ifnull(cur. NAME, 'CNY') currency_name,	"
        			+" 			joli.truck_type,	"
        			+" 			ifnull(joa.exchange_rate, 1) exchange_rate,	"
        			+" 			(	"
        			+" 				ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)	"
        			+" 			) after_total,	"
        			+" 			ifnull(f. NAME, f.name_eng) fee_name,	"
        			+" 			cur1. NAME exchange_currency_name,	"
        			+" 			joa.exchange_currency_rate,	"
        			+" 			joa.exchange_total_amount,c.car_no	,joli.cabinet_date"
        			+" 		FROM	"
        			+" 			trans_job_order_arap joa	"
        			+" 		LEFT JOIN trans_job_order jo ON jo.id = joa.order_id	"
        			+" 		LEFT JOIN party p ON p.id = joa.sp_id	"
        			+" 		LEFT JOIN party p1 ON p1.id = jo.customer_id "
        			+" 		LEFT JOIN currency cur ON cur.id = joa.currency_id "
        			+" 		LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id "
        			+" 		LEFT JOIN trans_job_order_land_item joli ON joli.order_id = joa.order_id "
        			+" 		LEFT JOIN fin_item f ON f.id = joa.charge_id "
        			+ " 	LEFT JOIN carinfo c on c.id=joa.car_id "
        			+" 		WHERE "
        			+"   joa.audit_flag = 'Y' "
        			+" 		AND joa.bill_flag = 'N' "
        			+" 		AND jo.office_id = 4 "
        			 + " and jo.delete_flag = 'N'"
     				+" 		GROUP BY "
        			+" 			joa.id "
        			+" 	) B "
        			+" WHERE "
        			+" 	1 = 1 ";
       	 
       	}else{
	         sql = " SELECT	* FROM	"
	        		 +" 	(	"
	        		 +" 		SELECT	"
	        		 +" 			joa.order_type sql_type,joa.id,joa.remark,	"
	        		 +" 			joa.type,	"
	        		 +" 			joa.sp_id,	"
	        		 +" 			ifnull(joa.total_amount, 0) total_amount,	"
	        		 +" 			ifnull(	"
	        		 +" 				joa.currency_total_amount,	"
	        		 +" 				0	"
	        		 +" 			) currency_total_amount,	"
	        		 +" 			jo.id jobid,	"
	        		 +" 			jo.order_no,	"
	        		 +" 			jo.create_stamp,	"
	        		 +" 			jo.land_export_date,	"
	        		 +" 			jo.customer_id,	"
	        		 +" 			jo.volume,	"
	        		 +" 			jo.net_weight,	"
	        		 +" 			jo.ref_no,jo.so_no,jo.container_no,	"
	        		 +" 			p.abbr sp_name,	"
	        		 +" 			p1.abbr customer_name,	"
	        		 +" 			ifnull(cur. NAME, 'CNY') currency_name,	"
	        		 +" 			joli.truck_type,	"
	        		 +" 			ifnull(joa.exchange_rate, 1) exchange_rate,	"
	        		 +" 			(	"
	        		 +" 				ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)	"
	        		 +" 			) after_total,	"
	        		 +" 			ifnull(f. NAME, f.name_eng) fee_name,	"
	        		 +" 			cur1. NAME exchange_currency_name,	"
	        		 +" 			joa.exchange_currency_rate,	"
	        		 +" 			joa.exchange_total_amount,c.car_no	,joli.cabinet_date"
	        		 +" 		FROM	"
	        		 +" 			trans_job_order_arap joa	"
	        		 +" 		LEFT JOIN trans_job_order jo ON jo.id = joa.order_id	"
	        		 +" 		LEFT JOIN party p ON p.id = joa.sp_id	"
	        		 +" 		LEFT JOIN party p1 ON p1.id = jo.customer_id "
	        		 +" 		LEFT JOIN currency cur ON cur.id = joa.currency_id "
	        		 +" 		LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id "
	        		 +" 		LEFT JOIN trans_job_order_land_item joli ON joli.order_id = joa.order_id "
	        		 +" 		LEFT JOIN fin_item f ON f.id = joa.charge_id "
	        		 + " 		LEFT JOIN carinfo c on c.id=joa.car_id "
	        		 +" 		WHERE "
	        		 +" 			joa.order_type = 'cost' "
	        		 +" 		AND joa.audit_flag = 'Y' "
	        		 +" 		AND joa.bill_flag = 'N' "
	        		 +" 		AND jo.office_id = 4 "
	        		 + " and jo.delete_flag = 'N'"
	 				+" 		GROUP BY "
	        		 +" 			joa.id "
	        		 +" 	) B "
	        		 +" WHERE "
	        		 +" 	1 = 1 ";
       	}
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by land_export_date desc " );
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
		
	}
	public void orderList() {
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
        
        String sql = "select * from(  "
        		+ " select aco.*,"
        		+" IFNULL(aco.cny, 0) total_amount_cny, "
			    +" (SELECT IFNULL(SUM(t.receive_cny), 0) FROM trans_arap_cost_receive_item t WHERE t. charge_order_id=aco.id) total_receive_cny, "
			    +" (aco.cny-(SELECT IFNULL(SUM(t.receive_cny), 0)  FROM trans_arap_cost_receive_item t WHERE t.charge_order_id=aco.id)) total_RESIDUAL_CNY, "
			   + " p.abbr sp_name,c.car_no "
				+ " from trans_arap_cost_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " LEFT JOIN carinfo c ON c.id = aco.car_id "
				+ " where aco.office_id = "+ office_id
				+ " order by aco.id desc"
				+ " ) B where 1=1 ";
      
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
	}
	
	
	@Before(Tx.class)
	public void save() throws Exception{
		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String id = (String) dto.get("id");
        String ids = (String) dto.get("ids");
        
        TransArapCostOrder aco = new TransArapCostOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		if(user==null){
        	return;
        }
   		long office_id = user.getLong("office_id");
   		DateFormat dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			aco = TransArapCostOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, aco);
   			aco.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, aco);
	   		String orderPrefix = OrderNoGenerator.getNextOrderNo("YFDZ", office_id);
	        aco.set("order_no", orderPrefix);
	        aco.set("order_type", "应付对账单");
			aco.set("create_by", user.getLong("id"));
			aco.set("create_stamp",dateTimeformat.format( new Date()));
			aco.set("office_id", office_id);
			aco.save();
			id = aco.getLong("id").toString();
			
			
			//设置已创建过对账单flag
			if(ids != null){
				String idAttr[] = ids.split(",");
				for(int i=0 ; i<idAttr.length ; i++){
					TransJobOrderArap joa = TransJobOrderArap.dao.findById(idAttr[i]);
					joa.set("bill_flag", "Y");
					String hedge_order_type = joa.getStr("order_type");
					if("charge".equals(hedge_order_type)){
						joa.set("hedge_flag", "Y");
	                }
					joa.update();
					TransArapCostItem arapCostItem = new TransArapCostItem();
					arapCostItem.set("ref_order_id", idAttr[i]);
					arapCostItem.set("cost_order_id", id);
					arapCostItem.save();
				}
			}
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("currency_list");
   		if(itemList != null){
   			for(Map<String, String> item :itemList){
   				String new_rate = item.get("new_rate");
   				String rate = item.get("rate");
   				String order_type = item.get("order_type");
   				String currency_id = item.get("currency_id");
   				String rate_id = item.get("rate_id");
   				String order_id = (String) dto.get("id");
   				
   				RateContrast rc = null;
   				if(StringUtils.isEmpty(rate_id)){
   					rc = new RateContrast();
   					rc.set("order_id", id);
   					rc.set("new_rate", new_rate);
   					rc.set("rate", rate);
   					rc.set("currency_id", currency_id);
   					rc.set("order_type", order_type);
   					rc.set("create_by", LoginUserController.getLoginUserId(this));
   					rc.set("create_stamp", new Date());
   					rc.save();
   				}else{
   					rc = RateContrast.dao.findById(rate_id);
   					if(rc == null){
   						rc = RateContrast.dao.findFirst("select * from rate_contrast where order_id = ? and currency_id = ?",order_id,currency_id);
   					}
   					rc.set("new_rate", new_rate);
   					rc.set("update_by", LoginUserController.getLoginUserId(this));
   					rc.set("update_stamp", new Date());
   					rc.update();
   				}	
   			}
   		}
		
   		
   		String sql = " select aco.*, p.company_name company_name, u.c_name creator_name from trans_arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by"
   				+ " where aco.id = ? ";
   		
   		Record r = Db.findFirst(sql,id);
   		renderJson(r);
	}
	
	@Before(EedaMenuInterceptor.class)
	public void edit(){
		String id = getPara("id");//arap_cost_order id
		UserLogin user1 = LoginUserController.getLoginUser(this);
		if (user1==null) {
            return;
        }
	    long office_id=user1.getLong("office_id");
	    //判断与登陆用户的office_id是否一致
	    if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("trans_arap_cost_order", Long.valueOf(id), office_id)){
	    	renderError(403);// no permission
	        return;
	    }
		String sql = " select aco.*,p.id company_id,p.company_name,p.abbr company_abbr,c.car_no car_no_name,u.c_name creator_name,u1.c_name confirm_by_name from trans_arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " LEFT JOIN carinfo c ON c.id = aco.car_id "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record order = Db.findFirst(sql,id);
		UserLogin u3=LoginUserController.getLoginUser(this);
		order.set("user", u3);
		String sqlString="SELECT  (aco.cny-IFNULL(SUM(tacri.receive_cny),0))residual_cny FROM trans_arap_cost_order aco "
				 +" LEFT JOIN  trans_arap_cost_receive_item tacri  on aco.id=tacri.charge_order_id "
				 +" WHERE aco.id ="+id+" ORDER BY tacri.id DESC ";
		
		Record rec2 = Db.findFirst(sqlString);
		if(rec2!=null){
			order.set("residual_cny", rec2.get("residual_cny"));
		}
		order.set("receive_itemList", getReceiveItemList(id));
		order.set("item_list", getItemList("",id,""));
		setAttr("order", order);
		setAttr("user",u3);
		render("/tms/arap/transCostCheckOrder/tmsCostCheckOrderEdit.html");
	}
	
	@Before(Tx.class)
	public void exchange_currency(){
	    String costOrderId = getPara("cost_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update trans_job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
		renderJson(exchangeTotalMap);
	}

    private Map<String, Double> updateExchangeTotal(String costOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  trans_job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in(select aci.ref_order_id from trans_arap_cost_item aci where aci.cost_order_id="+costOrderId+")";
		
		Map<String, Double> exchangeTotalMap = new HashMap<String, Double>();
		exchangeTotalMap.put("CNY", 0d);
		exchangeTotalMap.put("USD", 0d);
		exchangeTotalMap.put("HKD", 0d);
		exchangeTotalMap.put("JPY", 0d);
		
		List<Record> resultList= Db.find(sql);
		for (Record rec : resultList) {
            String name = rec.get("exchange_currency_name");
            String type = rec.get("order_type");
            Double exchange_amount = exchangeTotalMap.get(name);
            if(exchangeTotalMap.get(name)==null){
                if("cost".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=exchange_amount);
                }else{
                    exchangeTotalMap.put(name, 0-rec.getDouble("exchange_total_amount"));
                }
            }else{
                if("cost".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=rec.getDouble("exchange_total_amount"));
                }else{
                    exchangeTotalMap.put(name, exchange_amount-=rec.getDouble("exchange_total_amount"));
                }        
            }
        }
		
		Record order = Db.findById("trans_arap_cost_order", costOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("trans_arap_cost_order", order);
		return exchangeTotalMap;
    }
	
	@Before(Tx.class)
	public void confirm(){
		String id = getPara("id");
		TransArapCostOrder aco = TransArapCostOrder.dao.findById(id);
		aco.set("status","已确认");
		aco.set("confirm_stamp", new Date());
		aco.set("confirm_by", LoginUserController.getLoginUserId(this));
		aco.update();
		
		//设置y，已生成对账单o
		String sql="UPDATE trans_job_order_arap joa set billConfirm_flag='Y' "
					+"where joa.id in (select aci.ref_order_id FROM trans_arap_cost_item aci where cost_order_id="+id+" )";
		Db.update(sql);
		Record r = aco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
		renderJson(r);
	}
	
	
	//异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String ids = getPara("ids");
    	String order_ids = getPara("order_ids");
    	String appliction_id = getPara("appApplication_id");
    	String bill_flag = getPara("bill_flag");
    	
    	String  currency_code=getPara("query_currency");
    	//查询结算币制
    	String  exchange_currency=getPara("query_exchange_currency");
    	String  fin_name=getPara("query_fin_name");
    	List<Record> list = null;
    	if("N".equals(order_id)){
    		if(StringUtils.isNotEmpty(appliction_id)){
    			list = getCostItemList(appliction_id,bill_flag,currency_code,exchange_currency,fin_name);
        	}else{
	    		if("".equals(order_ids)){
	    			order_ids=null;
	    				}
	    		list = getCostItemList(order_ids,"",currency_code,exchange_currency,fin_name);
	    		}
    	}else{
    		list = getItemList(ids,order_id,currency_code);
    	}
    	String  type=getPara("table_type");
    	if(order_id!=""&&"receive".equals(type)){
    		list=getReceiveItemList(order_id);
    		setAttr("receive_itemList",list);
    	}
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
    }
	
    public void insertCostItem(){
    	String itemList= getPara("cost_itemlist");
    	String[] itemArray =  itemList.split(",");
    	String costOrderId=getPara("order_id");
    	TransArapCostItem aci = null;
    	
    	if(costOrderId != null){
    		for(String itemId:itemArray){
    			aci = new TransArapCostItem();
    			TransJobOrderArap jobOrderArap = TransJobOrderArap.dao.findById(itemId);
	             jobOrderArap.set("bill_flag", "Y");
	             String hedge_order_type = jobOrderArap.getStr("order_type");
					if("charge".equals(hedge_order_type)){
						jobOrderArap.set("hedge_flag", "Y");
	                }
	             jobOrderArap.update();
				aci.set("ref_order_id", itemId);
				aci.set("cost_order_id", costOrderId);
				aci.save();
    		}
    		
    	}
    	//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
		exchangeTotalMap.put("costOrderId", Double.parseDouble(costOrderId));
		
    	renderJson(exchangeTotalMap);

    }
    public void deleteCostItem(){
    	String costOrderId=getPara("order_id");
    	String itemid=getPara("cost_itemid");
    	if(itemid !=null&& costOrderId!=null){
    		TransJobOrderArap jobOrderArap = TransJobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("bill_flag", "N");
    		 jobOrderArap.set("hedge_flag", "N");
             jobOrderArap.update();
//             String sql="delete from  where ref_order_id="+itemid+"and cost_order_id="+costOrderId;
             Db.deleteById("trans_arap_cost_item","ref_order_id,cost_order_id",itemid,costOrderId);
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
    			exchangeTotalMap.put("costOrderId", Double.parseDouble(costOrderId));
    	    	renderJson(exchangeTotalMap);
    }
     
    //收款确认
   	@Before(Tx.class)
 	public void confirmOrder(){
		   		 UserLogin user = LoginUserController.getLoginUser(this);
		   		if (user==null) {
		            return;
		        }
		   		String jsonStr=getPara("params");
		  
		        Gson gson = new Gson();  
		         Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
		         String id=(String)dto.get("charge_order_id");
			   	 String itemids= (String) dto.get("itemids");
		   		
		   		String pay_remark=(String) dto.get("pay_remark");
		   		
		   		TransArapCostReceiveItem cacritem=new TransArapCostReceiveItem();
		   		String receive_time = (String) dto.get("receive_time");
		   		String deposit_bank = "";
		     	String payment_method = (String) dto.get("payment_method");
		     	
		     	if(dto.get("deposit_bank")!=null && !"".equals(dto.get("deposit_bank"))){
		   			 deposit_bank = (String) dto.get("deposit_bank");
		   		}else{
		   			String str2="select id from fin_account where bank_name='现金' and office_id="+user.get("office_id");
		   	        Record rec = Db.findFirst(str2);
		   	        if(rec!=null){
		   	        	deposit_bank = rec.getLong("id").toString();
		   	        }
		   		}
		   			DbUtils.setModelValues(dto, cacritem); 
		   			//保存每次收款记录
		   			cacritem.save();
                
         				TransArapCostOrder arapChargeOrder = TransArapCostOrder.dao.findById(id);
         				//求每张应收对账单的每次收款金额记录表，求已收的总额
         				String sql1 =" SELECT	IFNULL( SUM(aci.receive_cny),0) paid_cny, "
         						+" 	IFNULL( SUM(aci.receive_jpy),0) paid_jpy, "
         						+" 	IFNULL( SUM(aci.receive_usd),0) paid_usd, "
         						+" 	IFNULL( SUM(aci.receive_hkd),0) paid_hkd "
         						+" FROM	trans_arap_cost_receive_item aci "
         						+" WHERE	aci.charge_order_id = "+id;
                            
                         Record r1 = Db.findFirst(sql1);                  
                         Double paid_cny = r1.getDouble("paid_cny");     
                         Double paid_usd = r1.getDouble("paid_usd");
                         Double paid_jpy = r1.getDouble("paid_jpy");
                         Double paid_hkd = r1.getDouble("paid_hkd");
                         
                         	//求每张应收对账单的总金额
                         String sql = "SELECT "
                         		+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_cost_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =3 and aci.cost_order_id="+id
                 				+" ),0) cny,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_cost_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =6 and aci.cost_order_id="+id
                 				+" ),0) usd,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_cost_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =8 and aci.cost_order_id="+id
                 				+" ),0) jpy,"
                 				+" IFNULL((SELECT SUM(joa.total_amount) from  trans_job_order_arap joa LEFT JOIN trans_arap_cost_item aci on joa.id = aci.ref_order_id"
                 				+" where  joa.currency_id =9 and aci.cost_order_id="+id
                 				+" ),0) hkd ";
                            
                            Record r = Db.findFirst(sql);
                            Double cny = r.getDouble("cny");//greate_flay=Y的arap item 汇总金额
                            Double usd = r.getDouble("usd");
                            Double jpy = r.getDouble("jpy");
                            Double hkd = r.getDouble("hkd");
                 
         				if(cny>paid_cny||usd>paid_usd||jpy>paid_jpy||hkd>paid_hkd){
         					arapChargeOrder.set("audit_status", "部分已付款").update();
         				}else{
         					arapChargeOrder.set("audit_status", "已付款").update();
         					//pay_flag为收付款标志
         					Db.update("update trans_job_order_arap set pay_flag = 'Y' where id in ("+itemids+")");
         				}
               //新建日记账表数据
           		if(!"0.0".equals(dto.get("receive_cny"))&&StringUtils.isNotEmpty((String) dto.get("receive_cny"))){
           			createAuditLog(id, payment_method, deposit_bank, receive_time, (String)dto.get("receive_cny"), "CNY");
           		}
	             if(!"0.0".equals(dto.get("receive_usd"))&&StringUtils.isNotEmpty((String)dto.get("receive_usd"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time,(String) dto.get("receive_usd"), "USD");
	             }
	             if(!"0.0".equals(dto.get("receive_jpy"))&&StringUtils.isNotEmpty((String) dto.get("receive_jpy"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time,(String) dto.get("receive_jpy"), "JPY");
	             }
	             if(!"0.0".equals(dto.get("receive_hkd"))&&StringUtils.isNotEmpty((String)dto.get("receive_hkd"))){
	             	createAuditLog(id, payment_method, deposit_bank, receive_time, (String)dto.get("receive_hkd"), "HKD");
	             }
         
         Record r11 = new Record();
 		r11.set("confirm_name", LoginUserController.getUserNameById(LoginUserController.getLoginUserId(this)));
 		r11.set("status", arapChargeOrder.get("audit_status"));
 		r11.set("ids", id);
         renderJson(r11);
  
     }
   	
	@Before(Tx.class)
  	private void createAuditLog(String application_id, String payment_method,
            String receive_bank_id, String receive_time, String pay_amount, String currency_code) {
        //新建日记账表数据
  		UserLogin user = LoginUserController.getLoginUser(this);
  		if (user==null) {
            return;
        }
        long office_id = user.getLong("office_id");
		ArapAccountAuditLog auditLog = new ArapAccountAuditLog();
        auditLog.set("payment_method", payment_method);
        auditLog.set("payment_type", ArapAccountAuditLog.TYPE_TRANSCOST);
        auditLog.set("currency_code", currency_code);
        auditLog.set("amount", pay_amount);
        auditLog.set("creator", LoginUserController.getLoginUserId(this));
        auditLog.set("create_date", receive_time);
        auditLog.set("office_id", office_id);
        if(receive_bank_id!=null && !("").equals(receive_bank_id)){
        		auditLog.set("account_id", receive_bank_id);
        	}
        auditLog.set("source_order", "运输应付对账单");
        auditLog.set("invoice_order_id", application_id);
        auditLog.save();
    }
    
}
