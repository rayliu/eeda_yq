package controllers.oms.bookingOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.profile.Currency;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class BookingCostCheckOrderController extends Controller {

	private Logger logger = Logger.getLogger(BookingCostCheckOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
		if(user==null){
			return;
		}
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/bookingCostCheckOrder");
        setAttr("listConfigList", configList);
		render("/eeda/bookingArap/BookingCostCheckOrderList.html");
	}
	

    
	public void list2() {
        //String sLimit = "";
        String pageIndex = getPara("draw");
//        if (getPara("start") != null && getPara("length") != null) {
//            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
//        }

		//List<Record> BillingOrders = null;

		Map<String,Object> BillingOrderListMap = new HashMap<String,Object>();
		BillingOrderListMap.put("draw", pageIndex);
		BillingOrderListMap.put("data", null);

		renderJson(BillingOrderListMap);
	}
    
    
    public void checkedList(){
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        			
        UserLogin user = LoginUserController.getLoginUser(this);
        if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        
        String sql = "select * from(  "
        		+ " select aco.*,IFNULL(aco.audit_status,aco.status) toStatus, "
        		+ "  (SELECT abbr from party where office_id = "+office_id+" and ref_office_id = aco.office_id) sp_name "
				+ " from arap_charge_order aco "
				+ " left join party p on p.id=aco.sp_id "
				+ " where p.ref_office_id = "+office_id+" order by aco.create_stamp DESC "
				+ " ) B where 1=1 ";
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap);
    	
    }
    
    
    public List<Record> getItemList(String ids,String order_id,String code){
    	String sql = null;
    	String currenry_code="";
    	if(StringUtils.isNotEmpty(code)){
    		 currenry_code=" and cur. NAME="+"'"+code+"'";
    	}
		if(StringUtils.isEmpty(order_id)){
			sql = " select joa.id, joa.order_type,jo.order_no,jo.order_export_date,jo.customer_id,jo.volume vgm,"
    			+ "IFNULL(cur1.name,cur.name) exchange_currency_name,"
    			+ "IFNULL(joa.exchange_currency_rate,1) exchange_currency_rate,IFNULL(joa.exchange_total_amount,joa.total_amount) exchange_total_amount,"
    			+ "joa.total_amount total_amount,joa.exchange_rate exchange_rate," 
    			+ " jo.net_weight gross_weight,"
    			+ " cur.name currency_name,"
    			+ " jo.ref_no ref_no,"
    			+ " p1.company_name sp_name,jos.mbl_no,l.name fnd,joai.destination,jos.hbl_no,jols.truck_type truck_type,"
    			+ " GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount ,"
    			+ " fi.name fin_name "
    			+ " from job_order_arap joa"
    			+ " LEFT JOIN currency cur on cur.id = joa.currency_id"
    			+ " LEFT JOIN currency cur1 on cur1.id = joa.exchange_currency_id"
    			+ "	left join job_order jo on jo.id=joa.order_id "
				+ " left join fin_item fi on joa.charge_id = fi.id "
    			+ "	left join job_order_shipment jos on jos.order_id=joa.order_id "
    			+ " left join job_order_shipment_item josi on josi.order_id=joa.order_id "
    			+ "	left join job_order_air_item joai on joai.order_id=joa.order_id "
    			+ " left join job_order_land_item  jols on jols.order_id=joa.order_id "
    			+ "	left join party p1 on p1.id=joa.sp_id "
    			+ "	left join location l on l.id=jos.fnd "
    			+ "	where joa.audit_flag='Y' "
    			+ " and joa.id in("+ids+")"
    			+ " and jo.delete_flag = 'N'"
    			+ " GROUP BY joa.id";
			}else{				
			sql = " select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
					+" aco.order_no check_order_no, jo.id job_order_id, jo.order_no,jo.order_export_date,jo.customer_id,jo.volume,jo.net_weight,jo.type," 
					+ " jo.ref_no ref_no,"
						+" p.abbr sp_name,p1.abbr customer_name,jos.mbl_no,l.name fnd,joai.destination,"
						+" ifnull((select rc.new_rate from rate_contrast rc"
						    +"  where rc.currency_id = joa.currency_id and rc.order_id = aco.id),cast(joa.exchange_rate as char)) new_rate,"
						    +" (ifnull(joa.total_amount,0)*ifnull(joa.exchange_rate,1)) after_total,"
						    +"  ifnull((select rc.new_rate from rate_contrast rc"
						    +" where rc.currency_id = joa.currency_id and rc.order_id = aco.id),ifnull(joa.exchange_rate,1))*ifnull(joa.total_amount,0) after_rate_total,"
						+" GROUP_CONCAT(josi.container_no) container_no,GROUP_CONCAT(josi.container_type) container_amount,"
						+ " fi.name fin_name,"
						+ " cur.name currency_name,"
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
						+" left join arap_charge_item aci on aci.ref_order_id = joa.id"
					 +" left join arap_charge_order aco on aco.id = aci.charge_order_id"
					 +" where joa.id = aci.ref_order_id and aco.id = ("+order_id+")" +currenry_code
					 + " and jo.delete_flag = 'N'"
					 +" GROUP BY joa.id"
						+" ORDER BY aco.order_no, jo.order_no";
				
				
			}	
    	List<Record> re = Db.find(sql);
    	return re;
    }
    
    public List<Record> getChargeItemList(String order_ids,String bill_flag,String code,String exchange_currency,String fin_name){
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
							+" left join charge_application_order_rel caol on caol.job_order_arap_id  = joa.id"
							+" left join arap_charge_application_order acao on caol.application_order_id = acao.id"
							 +" left join arap_charge_order aco on aco.id=caol.charge_order_id"
						  +" where acao.id="+order_ids+query_fin_name
						  + " and jo.delete_flag = 'N'"
							+" GROUP BY joa.id"
							+" ORDER BY aco.order_no, jo.order_no";
				
			}else{
				sql = "select joa.id,joa.sp_id,joa.order_type,joa.total_amount,joa.exchange_rate,joa.currency_total_amount,"
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
							+" left join arap_charge_item aci on aci.ref_order_id = joa.id"
						    +" left join arap_charge_order aco on aco.id = aci.charge_order_id"
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

    @Before(EedaMenuInterceptor.class)
    public void edit(){
		String id = getPara("id");//arap_charge_order id
		String condition = "select ref_order_id from arap_charge_item where charge_order_id ="+id;
		
		String sql = " select aco.*,p.company_name,p.contact_person,p.id company_id,"
				+ " p.abbr company_abbr,p.phone,p.address,u.c_name creator_name,u1.c_name confirm_by_name "
				+ " from arap_charge_order aco "
   				+ " left join party p on p.id=aco.sp_id "
   				+ " left join user_login u on u.id=aco.create_by "
   				+ " left join user_login u1 on u1.id=aco.confirm_by "
   				+ " where aco.id = ? ";
		Record rec =Db.findFirst(sql,id);

		rec.set("address", rec.get("address"));
		rec.set("customer", rec.get("contact_person"));
		rec.set("phone", rec.get("phone"));
		rec.set("itemList", getItemList(condition,id,""));
		rec.set("company_id", rec.get("company_id"));
		rec.set("company_abbr", rec.get("company_abbr"));
		setAttr("order",rec);
		render("/eeda/bookingArap/BookingCostCheckOrderEdit.html");
	}

    
    @Before(Tx.class)
	public void exchange_currency(){
	    String chargeOrderId = getPara("charge_order_id");
		String ids = getPara("ids");
		String ex_currency_name = getPara("ex_currency_name");
		Currency c = Currency.dao.findFirst("select id from currency where code = ?", ex_currency_name);
		Long ex_currency_id = c.getLong("id");
		String rate = getPara("rate");
		Db.update("update job_order_arap set exchange_currency_id="+ex_currency_id+" , exchange_currency_rate="+rate+","
				+ " exchange_total_amount=truncate(("+rate+"*total_amount),4)  where id in ("+ids+") and total_amount!=''");
		
		//计算结算汇总
		Map<String, Double> exchangeTotalMap = updateExchangeTotal(chargeOrderId);
		renderJson(exchangeTotalMap);
	}
    
    private Map<String, Double> updateExchangeTotal(String chargeOrderId) {
        String sql="select joa.order_type, ifnull(cur1.NAME, cur.NAME) exchange_currency_name, "
        +"       ifnull(joa.exchange_total_amount, joa.total_amount) exchange_total_amount "
        +"       from  job_order_arap joa "
        +"       LEFT JOIN currency cur ON cur.id = joa.currency_id"
        +"       LEFT JOIN currency cur1 ON cur1.id = joa.exchange_currency_id"
        +"       where joa.id in (select aci.ref_order_id from arap_charge_item aci where aci.charge_order_id="+chargeOrderId+")";
		
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
                if("charge".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=exchange_amount);//Double.ParseDouble(df.format(result_value))转取两位小数
                }else{
                    exchangeTotalMap.put(name, 0-rec.getDouble("exchange_total_amount"));
                }
            }else{
                if("charge".equals(type)){
                    exchangeTotalMap.put(name, exchange_amount+=rec.getDouble("exchange_total_amount"));
                }else{
                    exchangeTotalMap.put(name, exchange_amount-=rec.getDouble("exchange_total_amount"));
                }        
            }
        }
		
		Record order = Db.findById("arap_charge_order", chargeOrderId);
		for (Map.Entry<String, Double> entry : exchangeTotalMap.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		    order.set(entry.getKey(), entry.getValue());
		}
		Db.update("arap_charge_order", order);
		return exchangeTotalMap;
    }
    
    
    
	//异步刷新字表
    public void tableList(){
    	String order_ids = getPara("order_ids");
    	String order_id = getPara("order_id");
    	String appliction_id = getPara("appApplication_id");
    	String bill_flag = getPara("bill_flag");
    	String currency_code=getPara("query_currency");
    	//查询结算币制
    	String  exchange_currency=getPara("query_exchange_currency");
    	String  fin_name=getPara("query_fin_name");
    	List<Record> list = null;
    	String condition = "select ref_order_id from arap_charge_item where charge_order_id in ("+order_ids+") ";
    	
    	if("N".equals(order_id)){//应收申请单
    		if(StringUtils.isNotEmpty(appliction_id)){
    			list = getChargeItemList(appliction_id,bill_flag,currency_code,exchange_currency,fin_name);
        	}else{
	    		if("".equals(order_ids)){
	    			order_ids=null;
	    				}
	    		list = getChargeItemList(order_ids,"",currency_code,exchange_currency,fin_name);
	    		}
    	}else{//应收对账单
    		    list = getItemList(condition,order_id,currency_code);
    	}

    	Map<String,Object> BillingOrderListMap = new HashMap<String,Object>();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
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
  					+"   joa.order_type='charge', "
  					+" 	joa.total_amount, "
  					+" 	(0 - joa.total_amount) "
  					+" ) from job_order_arap joa WHERE joa.currency_id = 3 and joa.id = aci.ref_order_id ) cny, "
  					+" (SELECT IF ( "
  					+"   joa.order_type='charge', "
  					+" 	joa.total_amount, "
  					+" 	(0 - joa.total_amount) "
  					+" ) from job_order_arap joa WHERE joa.currency_id = 6 and joa.id = aci.ref_order_id ) usd, "
  					+"  "
  					+" (SELECT IF ( "
  					+"   joa.order_type='charge', "
  					+" 	joa.total_amount, "
  					+" 	(0 - joa.total_amount) "
  					+" ) from job_order_arap joa WHERE joa.currency_id = 8 and joa.id = aci.ref_order_id ) jpy, "
  					+" (SELECT IF ( "
  					+"   joa.order_type='charge', "
  					+" 	joa.total_amount, "
  					+" 	(0 - joa.total_amount) "
  					+" ) from job_order_arap joa WHERE joa.currency_id = 9 and joa.id = aci.ref_order_id ) hkd, "
  					+" GROUP_CONCAT(josi.container_no) container_no, "
  					+" GROUP_CONCAT(josi.container_type) container_amount "
  					+" from arap_charge_order aco "
  					+" left join party p on p.id=aco.sp_id "
  					+" LEFT JOIN office o on o.id = aco.office_id "
  					+" left join arap_charge_item aci on aci.charge_order_id = aco.id "
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
  			
  			//String head_id_sql_total = "ORDER_NO, ORDER_EXPORT_DATE, CUSTOMER_ABBR, SP_ABBR, TYPE, MBL, HBL, SO_NO, POL, POD, CONTAINER_NO, CONTAINER_AMOUNT, FEE_NAME, CURRENCY_NAME, TOTAL_AMOUNT";// 目的港,体积,件数,毛重,发票号,
  			String[] fields = {"ORDER_NO", "ORDER_EXPORT_DATE", "CUSTOMER_ABBR", "SP_ABBR", "TYPE", "MBL", "HBL", "SO_NO", "POL", "POD", "CONTAINER_NO", "CONTAINER_AMOUNT", "FEE_NAME", "CURRENCY_NAME", "TOTAL_AMOUNT"};
  			String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,sp_name);
  			renderText(fileName);
  		}  
    
}
