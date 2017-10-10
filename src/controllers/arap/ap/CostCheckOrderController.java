package controllers.arap.ap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostItem;
import models.ArapCostOrder;
import models.RateContrast;
import models.UserLogin;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.profile.Currency;

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
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostCheckOrderController extends Controller {
	private Log logger = Log.getLog(CostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/costCheckOrder");
        setAttr("listConfigList", configList);
		render("/eeda/arap/CostCheckOrder/CostCheckOrder.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	public void create(){
		
		String ids = getPara("itemId");//job_order_arap ids
		String totalAmount = getPara("totalAmount");
		String cny_totalAmount = getPara("cny_totalAmount");
		String usd_totalAmount = getPara("usd_totalAmount");
		String hkd_totalAmount = getPara("hkd_totalAmount");
		String jpy_totalAmount = getPara("jpy_totalAmount");
		String exchange_totalAmount = getPara("exchange_totalAmount");
		String exchange_cny_totalAmount = getPara("exchange_cny_totalAmount");
		String exchange_usd_totalAmount = getPara("exchange_usd_totalAmount");
		String exchange_hkd_totalAmount = getPara("exchange_hkd_totalAmount");
		String exchange_jpy_totalAmount = getPara("exchange_jpy_totalAmount");
		
		String strAry[] = ids.split(",");
		String id = strAry[0];
		String sql = " select joa.sp_id,p.abbr company_abbr,p.company_name company_name from job_order_arap joa "
				   + " left join party p on p.id = joa.sp_id "
				   + "  where joa.id = ? ";
		Record spRec = Db.findFirst(sql,id);
		Record order = new Record();
		order.set("sp_id", spRec.get("sp_id"));
		order.set("company_name", spRec.get("company_name"));
		order.set("company_abbr",spRec.get("company_abbr"));
		order.set("total_amount",exchange_totalAmount);
		order.set("jpy", exchange_jpy_totalAmount);
		order.set("cny", exchange_cny_totalAmount);
		order.set("usd", exchange_usd_totalAmount);
		order.set("hkd",exchange_hkd_totalAmount);
		
		order.set("ids",ids);
		order.set("creator_name", LoginUserController.getLoginUserName(this));
		order.set("item_list", getItemList(ids,"",""));
		order.set("currencyList", getCurrencyList(ids,""));
		setAttr("order", order);
		
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
	
	public List<Record> getCurrencyList(String ids,String order_id){
    	String sql = "SELECT "
    			+ " (select rc.id from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"') rate_id,"
    			+ " cur.id ,cur.name currency_name ,group_concat(distinct cast(joa.exchange_rate as char) SEPARATOR ';') exchange_rate ,"
    			+ " ifnull((select rc.new_rate from rate_contrast rc "
    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),cast(joa.exchange_rate as char)) new_rate"
				+ " FROM job_order_arap joa"
				+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
				+ " WHERE joa.id in("+ ids +") and cur.name!='CNY' group by cur.id" ;
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
			sql = " select if(joa.contract_amount != joa.total_amount and joa.cus_contract_flag = 'Y','Y','N') diff_flag,joa.id,joa.type,joa.sp_id, joa.order_type, joa.total_amount, joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),cast(joa.exchange_rate as char)) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = '"+order_id+"'),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
	                + " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
	                + " cur.name currency_name,"
	                + " ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
                    + " ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
                    + " ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag ,"
                    + " fi.name fin_name "
	                + " from job_order_arap joa "
	                + " left join job_order jo on jo.id=joa.order_id "
	                + " left join job_order_shipment jos on jos.order_id=joa.order_id "
	                + " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
	                + " left join job_order_air_item joai on joai.order_id=joa.order_id "
	                + " left join party p on p.id=joa.sp_id "
	                + " left join party p1 on p1.id=jo.customer_id "
	                + " left join location l on l.id=jos.fnd "
	                + " left join currency cur on cur.id=joa.currency_id "
	                + " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	                +" left join fin_item fi on joa.charge_id = fi.id "
	                + " where joa.audit_flag='Y' and joa.bill_flag='N' and joa.id in("+ids+") "
	                + " and jo.delete_flag = 'N'"
	                + " GROUP BY joa.id"
	                + " ORDER BY jo.order_no";	
		}else{
			sql = " select if(joa.contract_amount != joa.total_amount and joa.cus_contract_flag = 'Y','Y','N') diff_flag,joa.id,joa.type,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
			        + " aco.order_no check_order_no, jo.order_no,jo.id job_order_id,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
	                + " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
	                + " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
	    			+ " (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
	    			+ " ifnull((select rc.new_rate from rate_contrast rc "
	    			+ " where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
	                + " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
	                + " cur.name currency_name, "
	                + " ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
	                + " ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
	                + " fi.name fin_name ,"
	                + " ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag "
	                + " from job_order_arap joa "
	                + " left join job_order jo on jo.id=joa.order_id "
	                + " left join job_order_shipment jos on jos.order_id=joa.order_id "
	                + " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
	                + " left join job_order_air_item joai on joai.order_id=joa.order_id "
	                + " left join party p on p.id=joa.sp_id "
	                + " left join party p1 on p1.id=jo.customer_id "
	                + " left join location l on l.id=jos.fnd "
	                + " left join currency cur on cur.id=joa.currency_id "
	                + " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	                + " left join arap_cost_item aci on aci.ref_order_id = joa.id"
					+ " left join arap_cost_order aco on aco.id = aci.cost_order_id "
					+ " left join fin_item fi on joa.charge_id = fi.id "
					+ " where joa.id = aci.ref_order_id and aco.id in ("+order_id+")"+currency_code
					+ " and jo.delete_flag = 'N'"
	                + " GROUP BY joa.id "
	                + " ORDER BY aco.order_no, jo.order_no ";
		}
    	
    	List<Record> re = Db.find(sql);
    	return re;
    }
	
	public List<Record> getCostItemList(String order_ids,String bill_flag,String code,String exchange_currency,String fin_name,String order_no,String currency_name){
    	String sql = null;
    	String currency_code="";
    	String query_exchange_currency="";
    	String query_fin_name="";
    	String query_order_no="";
    	String query_currency_name="";
		if(StringUtils.isNotEmpty(code)){
			currency_code=" and cur. NAME="+"'"+code+"'";
		}
		if(StringUtils.isNotEmpty(exchange_currency)){
			String sql2="select id from currency where currency.name='"+exchange_currency+"'";
			List<Record> re=Db.find(sql2);
			query_exchange_currency=" and joa. exchange_currency_id="+re.get(0).get("id");
		}
		if(StringUtils.isNotEmpty(fin_name)){
			query_fin_name=" and fi.id in ("+fin_name+")";
		}
		if(StringUtils.isNotBlank(order_no)){
			query_order_no = " and jo.order_no='"+order_no+"'";
		}
		if(StringUtils.isNotBlank(currency_name)){
			query_currency_name = " and cur.name='"+currency_name+"'";
		}
			if("create".equals(bill_flag)){
				sql = " select joa.id,joa.create_flag,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no,jo.id job_order_id, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+" GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount,"
							
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from job_order jo"
							+" left join job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join job_order_shipment jos on jos.order_id=joa.order_id"
							+" left join job_order_shipment_item josi on josi.order_id=joa.order_id"
							+" left join job_order_air_item joai on joai.order_id=joa.order_id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join location l on l.id=jos.fnd"
							+" left join currency cur on cur.id=joa.currency_id"
							+" left join currency cur1 on cur1.id=joa.exchange_currency_id"
							+" left join cost_application_order_rel caol on caol.job_order_arap_id  = joa.id"
							+" left join arap_cost_application_order acao on caol.application_order_id = acao.id"
							 +" left join arap_cost_order aco on aco.id=caol.cost_order_id"
						  +" where acao.id="+order_ids+query_fin_name+query_order_no+query_currency_name
						  + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
				
			}else{
				sql = " select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
						+" aco.order_no check_order_no,jo.id job_order_id, jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
							+" p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination,"
							+" ifnull((select rc.new_rate from rate_contrast rc"
							    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
							    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
							    +"  ifnull((select rc.new_rate from rate_contrast rc"
							    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
							+" GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount,"
							+ " fi.name fin_name,"
							+" cur.name currency_name,"
							+" ifnull(cur1.NAME, cur.NAME) exchange_currency_name,"
							+" ifnull(joa.exchange_currency_rate, 1) exchange_currency_rate,"
							+" ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount, joa.pay_flag"
							+" from job_order jo"
							+" left join job_order_arap joa on jo.id=joa.order_id"
							+" left join fin_item fi on joa.charge_id = fi.id"
							+" left join job_order_shipment jos on jos.order_id=joa.order_id"
							+" left join job_order_shipment_item josi on josi.order_id=joa.order_id"
							+" left join job_order_air_item joai on joai.order_id=joa.order_id"
							+" left join party p on p.id=joa.sp_id"
							+" left join party p1 on p1.id=jo.customer_id"
							+" left join location l on l.id=jos.fnd"
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
		String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
		String pageIndex = getPara("draw");
		if (getPara("start") != null && getPara("length") != null) {
			sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
		}
		
		String sql = " select joa.id,joa.type,joa.sp_id,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,jo.order_no,jo.create_stamp,jo.customer_id,jo.volume,jo.net_weight, "
				+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination, "
				+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
				+ " cur.name currency_name "
				+ " from job_order_arap joa "
				+ " left join job_order jo on jo.id=joa.order_id "
				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
				+ " left join party p on p.id=joa.sp_id "
				+ " left join party p1 on p1.id=jo.customer_id "
				+ " left join location l on l.id=jos.fnd "
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
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date,order_no":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "order_export_date,order_no";
        	sort ="desc";
        }
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jo.office_id in ("+relist.getStr("office_id")+")";
        }
        String condition = DbUtils.buildConditions(getParaMap());
        String sql = "";
        if(checked!=null&&!"".equals(checked)&&checked.equals("Y")){
        	sql = "select * from(  "
        			+ " select if(joa.contract_amount != joa.total_amount and joa.cus_contract_flag = 'Y','Y','N') diff_flag,joa.order_type sql_type, joa.id,joa.type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
              		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.order_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
              		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
              		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
              		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type ,ifnull(joa.exchange_rate,1) exchange_rate,"
              		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
              		+ " ) after_total,"
              		+ " ifnull( ( SELECT rc.new_rate FROM rate_contrast rc "
              		+ " WHERE rc.currency_id = joa.currency_id AND rc.order_id = '' ), ifnull(joa.exchange_rate, 1) ) * ifnull(joa.total_amount, 0)"
              		+ " after_rate_total,ifnull(f.name,f.name_eng) fee_name,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
      				+ " from job_order_arap joa "
      				+ " left join job_order jo on jo.id=joa.order_id "
      				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
      				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
      				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
      				+ " left join party p on p.id=joa.sp_id "
      				+ " left join party p1 on p1.id=jo.customer_id "
      				+ " left join location l on l.id=jos.fnd "
      				+ " left join currency cur on cur.id=joa.currency_id "
      				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
      				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
      				+ " left join fin_item f on f.id = joa.charge_id"
      				+ " where joa.audit_flag='Y' and joa.bill_flag='N'  and (jo.office_id = "+office_id+ ref_office+")"
      				+ " and jo.delete_flag = 'N'"
      				+ " GROUP BY joa.id "
    				+ " ) B where 1=1 ";
       	 
       	}else{
	         sql = "select * from(  "
	         		+ " select if(joa.contract_amount != joa.total_amount and joa.cus_contract_flag = 'Y','Y','N') diff_flag,joa.id,joa.type,joa.sp_id,ifnull(joa.total_amount,0) total_amount,ifnull(joa.currency_total_amount,0) currency_total_amount,"
	         		+ " jo.id jobid,jo.order_no,jo.create_stamp,jo.order_export_date, jo.customer_id,jo.volume,jo.net_weight,jo.ref_no, "
	         		+ " p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,jos.hbl_no,l.name fnd,joai.destination, "
	         		+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount, "
	         		+ " ifnull(cur.name,'CNY') currency_name,joli.truck_type,ifnull(joa.exchange_rate, 1) exchange_rate,"
	         		+ " ( ifnull(joa.total_amount, 0) * ifnull(joa.exchange_rate, 1)"
	         		+ " ) after_total ,ifnull(f.name,f.name_eng) fee_name,cur1.name exchange_currency_name,joa.exchange_currency_rate,joa.exchange_total_amount"
	 				+ " from job_order_arap joa "
	 				+ " left join job_order jo on jo.id=joa.order_id "
	 				+ " left join job_order_shipment jos on jos.order_id=joa.order_id "
	 				+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
	 				+ " left join job_order_air_item joai on joai.order_id=joa.order_id "
	 				+ " left join party p on p.id=joa.sp_id "
	 				+ " left join party p1 on p1.id=jo.customer_id "
	 				+ " left join location l on l.id=jos.fnd "
	 				+ " left join currency cur on cur.id=joa.currency_id "
	 				+ " left join currency cur1 on cur1.id=joa.exchange_currency_id "
	 				+ " left join job_order_land_item joli on joli.order_id=joa.order_id "
	 				+ " left join fin_item f on f.id = joa.charge_id"
	 				+ " where joa.order_type='cost' and joa.audit_flag='Y' and joa.bill_flag='N' and (jo.office_id = "+office_id+ ref_office+")"
	 				+ " and jo.delete_flag = 'N'"
	 				+ " GROUP BY joa.id "
	 				+ " ) B where 1=1 ";
       	}
		
        String sqlTotal = "select count(1) total from ("+sql+ condition+") C";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by "+ sName +" "+ sort );
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
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select aco.*,IFNULL(aco.audit_status,aco.status) toStatus,p.abbr sp_name"
				+ " from arap_cost_order aco "
				+ " left join party p on p.id=aco.sp_id "
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
        
        ArapCostOrder aco = new ArapCostOrder();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		String action_type = "add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type = "update";
   			//update
   			aco = ArapCostOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, aco);
   			aco.update();
   			
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, aco);
	   		String orderPrefix = OrderNoGenerator.getNextOrderNo("YFDZ", office_id);
	        aco.set("order_no", orderPrefix);
	        aco.set("order_type", "应付对账单");
			aco.set("create_by", user.getLong("id"));
			aco.set("create_stamp", new Date());
			aco.set("office_id", office_id);
			aco.save();
			id = aco.getLong("id").toString();
			
			
			//设置已创建过对账单flag
			String idAttr[] = ids.split(",");
			for(int i=0 ; i<idAttr.length ; i++){
				JobOrderArap joa = JobOrderArap.dao.findById(idAttr[i]);
				joa.set("bill_flag", "Y");
				String hedge_order_type = joa.getStr("order_type");
				if("charge".equals(hedge_order_type)){
					joa.set("hedge_flag", "Y");
                }
				joa.update();
				ArapCostItem arapCostItem = new ArapCostItem();
				arapCostItem.set("ref_order_id", idAttr[i]);
				arapCostItem.set("cost_order_id", id);
				arapCostItem.save();
			}
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("currency_list");
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
   		
		saveLog(jsonStr, id, user, action_type);
		
   		String sql = " select aco.*, p.company_name company_name, u.c_name creator_name from arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by"
   				+ " where aco.id = ? ";
   		Record r = Db.findFirst(sql,id);
   		renderJson(r);
	}
	
	private void saveLog(String json, String order_id, UserLogin user, String action_type) {
        Record rec = new Record();
        rec.set("log_type", "action");
        rec.set("operation_obj", "应付对账单");
        rec.set("action_type", action_type);
        rec.set("create_stamp", new Date());
        rec.set("user_id", user.get("id"));
        rec.set("order_id", order_id);
        rec.set("json", json);
        rec.set("sys_type", "forwarder");
        rec.set("office_id", user.getLong("office_id"));
        logger.debug("save log...");
        Db.save("sys_log", rec);
    }
	
	@Before(EedaMenuInterceptor.class)
	public void edit(){
	    
		String id = getPara("id");//arap_cost_order id
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getOfficeId();
        //判断单office_id与登陆用户的office_id是否一致
        if(!OrderCheckOfficeUtil.checkOfficeEqual("arap_cost_order", Long.valueOf(id), office_id)){
            renderError(403);// no permission
            return;
        }
        
		String sql = " select aco.*,p.id company_id,p.company_name,p.abbr company_abbr,u.c_name creator_name,u1.c_name confirm_by_name from arap_cost_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record order = Db.findFirst(sql,id);
		
//		String condition = "select ref_order_id from arap_cost_item where cost_order_id ="+id;
//		order.set("currencylist", getCurrencyList(condition,id));
		order.set("item_list", getItemList("",id,""));
		setAttr("order", order);
		render("/eeda/arap/CostCheckOrder/CostCheckOrderEdit.html");
	}
	
	@Before(Tx.class)
	public void exchange_currency(){
	    String costOrderId = getPara("cost_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=("+rate+"*total_amount)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
		renderJson(exchangeTotalMap);
	}

    private Map<String, Double> updateExchangeTotal(String costOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in(select aci.ref_order_id from arap_cost_item aci where aci.cost_order_id="+costOrderId+")";
		
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
		
		Record order = Db.findById("arap_cost_order", costOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("arap_cost_order", order);
		return exchangeTotalMap;
    }
	
	@Before(Tx.class)
	public void confirm(){
		String id = getPara("id");
		ArapCostOrder aco = ArapCostOrder.dao.findById(id);
		aco.set("status","已确认");
		aco.set("confirm_stamp", new Date());
		aco.set("confirm_by", LoginUserController.getLoginUserId(this));
		aco.update();
		
		//设置y，已生成对账单o
		String sql="UPDATE job_order_arap joa set billConfirm_flag='Y' "
					+"where joa.id in (select aci.ref_order_id FROM arap_cost_item aci where cost_order_id="+id+" )";
		Db.update(sql);
		Record r = aco.toRecord();
		r.set("confirm_by_name", LoginUserController.getUserNameById(aco.getLong("confirm_by")));
		renderJson(r);
	}
	
	public void cancelConfirm(){
		String order_id = getPara("order_id");
   	 	long office_id = LoginUserController.getLoginUser(this).getLong("office_id");
   	 	Date action_time = new Date();
	   	String action = "cancelConfirm";
	   	String order_type = "CostCheckOrder";
	    
	   	//保存进状态审核表
	   	Record re = new Record();
	    re.set("order_id", order_id);
	    re.set("user_id", office_id);
	    re.set("action_time", action_time);
	    re.set("action",action);
	    re.set("order_type", order_type);
	    Db.save("status_audit", re);
	   
	    //更新arap_charge_order表的状态
	    ArapCostOrder aco = ArapCostOrder.dao.findById(order_id);
		aco.set("status","取消确认");
		aco.update();
		
		//更新job_order_arap表的billConfirm_flag设为'N'(变回未确认状态)
		String sql="UPDATE job_order_arap joa set billConfirm_flag='N' "
				+"where joa.id in (select aci.ref_order_id FROM arap_cost_item aci where cost_order_id="+order_id+" )";
		Db.update(sql);
		
		renderJson(true);
	}
	
	//编辑获取明细
	public void costEdit(){
    	String jor_id = getPara("jor_id");
	    String itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name,"
    				+ " c1.name exchange_currency_id_name"
    				+ " from job_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " left join currency c1 on c1.id=jor.exchange_currency_id"
    		        + " where jor.id=? order by jor.id";
	    List<Record> list = Db.find(itemSql,jor_id);
		Map map = new HashMap();
		map.put("sEcho",1);
		map.put("iTotalRecords", list.size());
		map.put("iTotalDisplayRecords", list.size());
		map.put("aaData", list);
		renderJson(map); 
    }
	
	//保存编辑明细
	public void costSave(){
    	String jsonStr = getPara("params");
    	
    	Gson gson = new Gson();
    	Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
    	JobOrderArap joa = new JobOrderArap().findById(dto.get("jor_id"));
    	String costOrderId=(String)dto.get("order_id");
    	String price = (String)dto.get("price");
    	if(price.isEmpty()){
    		joa.set("price",0);
    	}else{
    		joa.set("price",price);
    	}    	
    	
    	String amount = (String)dto.get("amount");
    	if(amount.isEmpty()){
    		joa.set("amount",0);
    	}else{
    		joa.set("amount",amount);
    	}
    	joa.set("unit_id",(String)dto.get("unit_id"));
    	
    	String total_amount = (String)dto.get("total_amount");
    	if(total_amount.isEmpty()){
    		joa.set("total_amount",0);
    	}else{
    		joa.set("total_amount",total_amount);
    	}
    	
    	String exchange_currency_rate = (String)dto.get("exchange_currency_rate");
    	if(exchange_currency_rate.isEmpty()){
    		joa.set("exchange_currency_rate",0);
    	}else{
    		joa.set("exchange_currency_rate",exchange_currency_rate);
    	}
    	
    	String exchange_total_amount = (String)dto.get("exchange_total_amount");
    	if(exchange_total_amount.isEmpty()){
    		joa.set("exchange_total_amount",0);
    	}else{
    		joa.set("exchange_total_amount",exchange_total_amount);
    	}
    	
    	String exchange_currency_rate_rmb = (String)dto.get("exchange_currency_rate_rmb");
    	if(exchange_currency_rate_rmb.isEmpty()){
    		joa.set("exchange_currency_rate_rmb",0);
    	}else{
    		joa.set("exchange_currency_rate_rmb",exchange_currency_rate_rmb);
    	}
    	
    	String exchange_total_amount_rmb = (String)dto.get("exchange_total_amount_rmb");
    	if(exchange_total_amount_rmb.isEmpty()){
    		joa.set("exchange_total_amount_rmb",0);
    	}else{
    		joa.set("exchange_total_amount_rmb",exchange_total_amount_rmb);
    	}
    	joa.set("sp_id",(String)dto.get("sp_id"));
    	joa.set("charge_id",(String)dto.get("charge_id"));
    	joa.set("currency_id",(String)dto.get("currency_id"));
    	joa.set("exchange_currency_id",(String)dto.get("exchange_currency_id"));
    	joa.set("rmb_difference",(String)dto.get("rmb_difference"));
    	joa.set("remark",(String)dto.get("remark"));
    	joa.update();
    	//计算结算汇总
    	Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
		exchangeTotalMap.put("costOrderId", Double.parseDouble(costOrderId));
		
    	renderJson(exchangeTotalMap);
    }
	
	
	//异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String ids = getPara("ids");
    	String order_ids = getPara("order_ids");
    	String appliction_id = getPara("appApplication_id");
    	String bill_flag = getPara("bill_flag");
    	String order_no = getPara("order_no1");
    	String currency_name = getPara("currency_name");
    	String  currency_code=getPara("query_currency");
    	//查询结算币制
    	String  exchange_currency=getPara("query_exchange_currency");
    	String  fin_name=getPara("query_fin_name");
    	List<Record> list = null;
    	if("N".equals(order_id)){
    		if(StringUtils.isNotEmpty(appliction_id)){
    			list = getCostItemList(appliction_id,bill_flag,currency_code,exchange_currency,fin_name,order_no,currency_name);
        	}else{
	    		if("".equals(order_ids)){
	    			order_ids=null;
	    				}
	    		list = getCostItemList(order_ids,"",currency_code,exchange_currency,fin_name,order_no,currency_name);
	    		}
    	}else{
    		list = getItemList(ids,order_id,currency_code);
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
    	ArapCostItem aci = null;
    	
    	if(costOrderId != null){
    		for(String itemId:itemArray){
    			aci = new ArapCostItem();
	    		 JobOrderArap jobOrderArap = JobOrderArap.dao.findById(itemId);
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
    		 JobOrderArap jobOrderArap = JobOrderArap.dao.findById(itemid);
    		 jobOrderArap.set("bill_flag", "N");
    		 jobOrderArap.set("hedge_flag", "N");
             jobOrderArap.update();
//             String sql="delete from  where ref_order_id="+itemid+"and cost_order_id="+costOrderId;
             Db.deleteById("arap_cost_item","ref_order_id,cost_order_id",itemid,costOrderId);
    	}
    	//计算结算汇总
    			Map<String, Double> exchangeTotalMap = updateExchangeTotal(costOrderId);
    			exchangeTotalMap.put("costOrderId", Double.parseDouble(costOrderId));
    	    	renderJson(exchangeTotalMap);
    }
    
    //导出excel对账单
	public void downloadExcelList(){
		String order_id = getPara("id");
		String sp_name = getPara("sp_name");
		
		String sqlExport = " SELECT l. NAME pod,l1. NAME pol,aco.begin_time,aco.end_time,o.eng_office_name,p.company_name,p.abbr sp_abbr,p.contact_person,p.phone,p.fax,p1.abbr customer_abbr,  "
				+" jo.order_export_date,jo.order_no,jo.type, "
				+"  jos.mbl_no MBL, jos.hbl_no HBL,jos.SONO so_no, "
				+" fi.name fee_name,cur. NAME currency_name,joa.amount,joa.price,ut.name unit_name, "
				+" if(joa.order_type='cost', "
				+" 	(0-joa.total_amount), "
				+" 	joa.total_amount "
				+" 	) total_amount, "
				+" (SELECT IF ( "
				+"   joa.order_type='cost', "
				+" 	joa.total_amount, "
				+" 	(0 - joa.total_amount) "
				+" ) from job_order_arap joa WHERE joa.currency_id = 3 and joa.id = aci.ref_order_id ) cny, "
				+" (SELECT IF ( "
				+"   joa.order_type='cost', "
				+" 	joa.total_amount, "
				+" 	(0 - joa.total_amount) "
				+" ) from job_order_arap joa WHERE joa.currency_id = 6 and joa.id = aci.ref_order_id ) usd, "
				+"  "
				+" (SELECT IF ( "
				+"   joa.order_type='cost', "
				+" 	joa.total_amount, "
				+" 	(0 - joa.total_amount) "
				+" ) from job_order_arap joa WHERE joa.currency_id = 8 and joa.id = aci.ref_order_id ) jpy, "
				+" (SELECT IF ( "
				+"   joa.order_type='cost', "
				+" 	joa.total_amount, "
				+" 	(0 - joa.total_amount) "
				+" ) from job_order_arap joa WHERE joa.currency_id = 9 and joa.id = aci.ref_order_id ) hkd, "
				+" GROUP_CONCAT(josi.container_no) container_no, "
				+" GROUP_CONCAT(josi.container_type) container_amount "
				+" from arap_cost_order aco "
				+" left join party p on p.id=aco.sp_id "
				+" LEFT JOIN office o on o.id = aco.office_id "
				+" left join arap_cost_item aci on aci.cost_order_id = aco.id "
				+" left join job_order_arap joa on joa.id = aci.ref_order_id "
				+" LEFT JOIN currency cur ON cur.id = joa.currency_id "
				+" LEFT JOIN job_order_shipment_item josi ON josi.order_id = joa.order_id "
				+" LEFT JOIN fin_item fi on fi.id = joa.charge_id "
				+" LEFT JOIN unit ut on ut.id = joa.unit_id "
				+" left join job_order jo on jo.id = joa.order_id "
				+" LEFT JOIN party p1 ON p1.id = jo.customer_id"
				+" left join job_order_shipment jos on jos.order_id = jo.id "
				+" LEFT JOIN location l ON l.id = jos.pod "
				+" LEFT JOIN location l1 ON l1.id = jos.pol"
				+" where aco.id ="+order_id
				+" GROUP BY aci.id" ;
		String total_name_header = "申请单号, 出货日期, 客户, 结算公司, 类型, 提单号(MBL), 提单号(HBL), SO号, 起运港,目的港, 箱号, 箱量类型, 费用名称, 币制, 金额";// 目的港,体积,件数,毛重,发票号,
		String[] headers = total_name_header.split(",");
		
		String head_id_sql_total = "ORDER_NO, ORDER_EXPORT_DATE, CUSTOMER_ABBR, SP_ABBR, TYPE, MBL, HBL, SO_NO, POL, POD, CONTAINER_NO, CONTAINER_AMOUNT, FEE_NAME, CURRENCY_NAME, TOTAL_AMOUNT";// 目的港,体积,件数,毛重,发票号,
		String[] fields = {"ORDER_NO", "ORDER_EXPORT_DATE", "CUSTOMER_ABBR", "SP_ABBR", "TYPE", "MBL", "HBL", "SO_NO", "POL", "POD", "CONTAINER_NO", "CONTAINER_AMOUNT", "FEE_NAME", "CURRENCY_NAME", "TOTAL_AMOUNT"};
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,sp_name);
		renderText(fileName);
	}     
 }
