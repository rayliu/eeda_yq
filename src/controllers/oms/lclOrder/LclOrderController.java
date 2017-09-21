package controllers.oms.lclOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Office;
import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.bookOrder.BookOrder;
import models.eeda.oms.bookOrder.BookingOrder;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.lclOrder.LclOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.eeda.SysInfoController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class LclOrderController extends Controller {

	private Logger logger = Logger.getLogger(LclOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/clcOrder");
        setAttr("listConfigList", configList);
        
		render("/oms/LclOrder/LclOrderList.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
		String ids = getPara("ids");
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		Office office = Office.dao.findById(office_id);
   		setAttr("office", office);
   		
   		String itemSql = "SELECT"
   				+ " jor.id, jor.order_no,jor.transport_type, p.abbr customer_name, jor.type order_type, jor.order_export_date,"
   				+ " jos.pol, jos.pod, jor.pieces, jor.gross_weight, jor.volume, jos.cargo_desc cargo_name,"
   				+ " c_p.abbr carrier, v_p.abbr vessel, jos.voyage, jos.eta,"
   				+ " jos.etd, jos.SONO so_number, "
   				+ " jos.net_weight, jos.vgm, l.name por"
   				+ " FROM"
   				+ " job_order jor"
   				+ " LEFT JOIN job_order_shipment jos ON jos.order_id = jor.id"
   				+ " LEFT JOIN party p ON p.id = jor.customer_id"
   				+ " LEFT JOIN party c_p on c_p.id = jos.carrier"
   				+ " LEFT JOIN party v_p on v_p.id = jos.vessel"
   				+ " left join location l on l.id =  jos.por"
   				+ " where jor.id in ("+ids+")";
		List<Record> itemList = Db.find(itemSql);
   		setAttr("itemList", itemList);
   		
   		String transportType = "";
   		for(Record re : itemList){
   			String transport_type = re.getStr("transport_type");
   			if(StringUtils.isBlank(transport_type)){
   				continue;
   			}
   			if(transport_type.contains("land")){
   				if(!transportType.contains("陆运")){
   					transportType += "陆运,";
   				}
   			}
   			if(transport_type.contains("ocean")){
   				if(!transportType.contains("海运")){
   					transportType += "海运,";
   				}
   			}
   			if(transport_type.contains("air")){
   				if(!transportType.contains("空运")){
   					transportType += "空运,";
   				}
   			}
    		if(transport_type.contains("custom")){
    			if(!transportType.contains("报关")){
   					transportType += "报关,";
   				}
   			}
    	   	if(transport_type.contains("insurance")){
    	   		if(!transportType.contains("保险")){
   					transportType += "保险,";
   				}
   			}
    	    if(transport_type.contains("trade")){
    	    	if(!transportType.contains("贸易")){
   					transportType += "贸易,";
   				}
   			}
    	    if(transport_type.contains("express")){
    	    	if(!transportType.contains("快递")){
   					transportType += "快递,";
   				}
   			}
   		}
   		if(StringUtils.isNotBlank(transportType)){
   			transportType = transportType.substring(0,transportType.length()-1);
   		}
   		
   		Record order = new Record();
   		order.set("transport_type", transportType);
   		setAttr("order", order);
   		
        render("/oms/LclOrder/lclOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
       	Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        LclOrder order = new LclOrder();
   		String id = (String) dto.get("id");
   		String action_type = "";
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		if (StringUtils.isNotEmpty(id)) {
   			action_type = "update";
   			order = LclOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, order);

   			//需后台处理的字段
   			order.set("updator", user.getLong("id"));
   			order.set("update_time", new Date());
   			order.update();
   		} else {
   			//create 
   			action_type = "create";
   			DbUtils.setModelValues(dto, order);
   			String orderNo = OrderNoGenerator.getNextOrderNo("LCL", office_id);
   			
   			order.set("order_no", orderNo);
   			order.set("creator", user.getLong("id"));
   			order.set("create_time", new Date());
   			order.set("office_id", office_id);
   			order.save();
   			
   			id = order.getLong("id").toString();
   		}
   		
   	
   		if("create".equals(action_type)){
   			List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("itemList");
   			//DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");
   	   		for(Map<String, String> map : itemList){
   	   			String job_id = map.get("id");
   	   			Record jobRe = Db.findById("job_order", job_id);
   	   			jobRe.set("lcl_order_flag", "Y");
   	   			Db.update("job_order",jobRe);
   	   			
   	   			Record item = new Record();
   	   			item.set("job_order_id", job_id);
   	   			item.set("order_id", id);
   	   			Db.save("lcl_order_item", item);
   	   		}
   		}
   		
		
		//保存下拉列表使用历史
		//主单据
//		List<Record> orderRes = new ArrayList<Record>();
//		orderRes.add(new Record().set("type", "ARAP_COM").set("param", "to_party_id"));
//		saveParamHistory(dto,orderRes,user.getLong("id")); 

		SysInfoController.saveLog(jsonStr, id, user, action_type, "计划单", "");

		long creator = order.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = order.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    @Before(Tx.class)
    public void createBookOrder(PlanOrderItem item,String id){
    	Long item_id = item.getLong("id");
    	UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		String job_order_type =item.getStr("job_order_type");
   		String delivery = item.get("delivery");
   		String pickup_addr = item.get("pickup_addr");
   		String pol_id = item.get("pol");
   		
   		String pod_id = item.get("pod");
   		String container_type = item.get("container_type");
   		String truck_type = item.get("truck_type");
   		
   		String cargo_name = item.get("cargo_name");
   		String customs_type = item.get("customs_type");
   		String transport_type = "";
   		if(job_order_type.contains("出口柜货")||job_order_type.contains("出口散货")){
   			if(pol_id!=null||pod_id!=null){
	   			if(StringUtils.isEmpty(transport_type)){
	   				transport_type+="ocean";
	   			}else{
	   				transport_type+=",ocean";
	   			}
   			}
   		}
   		if(job_order_type.contains("空运")){
   			if(pol_id!=null||pod_id!=null){
   				if(StringUtils.isEmpty(transport_type)){
	   				transport_type+="air";
	   			}else{
	   				transport_type+=",air";
	   			}
   			}
   		}
   		String land_type ="";
   		if(StringUtils.isNotEmpty(pickup_addr)){
   			if(StringUtils.isEmpty(transport_type)){
   				transport_type+="land";
   			}else{
   				transport_type+=",land";
   			}
   			if(StringUtils.isEmpty(land_type)){
   				land_type+="land_take";
   			}else{
   				land_type+=",land_take";
   			}	
   		}
   		if("代理报关".equals(customs_type)){
   			if(StringUtils.isEmpty(transport_type)){
   				transport_type+="custom";
   			}else{
   				transport_type+=",custom";
   			}
   		}
   		
   		//新booking
   		BookingOrder bookingOrder  = BookingOrder.dao.findFirst("select * from booking_order where plan_item_id = ? ",item_id);
   		if(bookingOrder==null){
    		PlanOrder re = PlanOrder.dao.findById(id);
    		bookingOrder  = new BookingOrder();
    		bookingOrder.set("booking_no", OrderNoGenerator.getNextOrderNo("BK", office_id));
    		bookingOrder.set("creator", user.getLong("id"));
    		bookingOrder.set("transport_type", transport_type);
    		bookingOrder.set("create_stamp", new Date());
    		bookingOrder.set("updator", user.getLong("id"));
    		bookingOrder.set("update_stamp", new Date());
    		bookingOrder.set("office_id", office_id);
    		bookingOrder.set("type", job_order_type);
    		bookingOrder.set("order_export_date", item.get("factory_loading_time"));
    		bookingOrder.set("plan_order_no", re.getStr("order_no"));
    		bookingOrder.set("plan_order_id", id);
    		bookingOrder.set("plan_item_id", item_id);
    		bookingOrder.set("entrust", re.getLong("to_party_id"));
    		bookingOrder.set("shipper", re.getLong("self_party_id"));
    		bookingOrder.set("plan_item_id", item.getLong("id"));
    		bookingOrder.set("pieces", item.get("pieces"));
    		bookingOrder.set("net_weight", item.get("net_weight"));
    		bookingOrder.set("gross_weight", item.get("gross_weight"));
    		bookingOrder.set("volume", item.get("volume"));
    		bookingOrder.set("gargo_name", cargo_name);
    		bookingOrder.set("booking_submit_flag", "Y");
    		bookingOrder.set("status", "已提交");
    		bookingOrder.save();
    		
    		Long order_id = bookingOrder.getLong("id");
    		if(transport_type.contains("ocean")){
            	Record ocean_detail  = Db.findFirst("select * from booking_ocean_detail where order_id = ? ",order_id);
                if(ocean_detail==null){
                	ocean_detail = new Record();
                	ocean_detail.set("order_id", order_id);
                	ocean_detail.set("eta", delivery);             	
                	ocean_detail.set("pol_id", pol_id);
                	ocean_detail.set("pod_id", pod_id);
                	
                	Db.save("booking_ocean_detail", ocean_detail);
                }
            }
    		if(transport_type.contains("air")){
            	Record air_detail  = Db.findFirst("select * from booking_air_detail where order_id = ? ",order_id);
                if(air_detail==null){
                	air_detail = new Record();
                	air_detail.set("order_id", order_id);
                	air_detail.set("air_eta", delivery); 
                	air_detail.set("air_pol_id", pol_id);
                	air_detail.set("air_pod_id", pod_id);
                	Db.save("booking_air_detail", air_detail);
                }
            }
    		//提货
            if(transport_type.contains("land")){
            	Record take_land  = Db.findFirst("select * from booking_land_detail where order_id = ? ",order_id);
                if(take_land==null){
                	take_land = new Record();
                	take_land.set("order_id", order_id);               	
                	take_land.set("take_address", pickup_addr);
                	take_land.set("land_type", land_type);
                	if(StringUtils.isNotEmpty(truck_type)){
                		String[] array = truck_type.split(",");
                    	for (int i = 0; i < array.length; i++) {
                    		String[] ctypeMsg = array[i].split("X");
                    		String tr_type = ctypeMsg[0];
                    		String number = ctypeMsg[1];
                    		take_land.set("truck_type", tr_type);
                    	}
                	}
                	                	
                	Db.save("booking_land_detail", take_land);
                }
            }
    		
    	}else{
    		bookingOrder.set("updator", user.getLong("id"));
    		bookingOrder.set("update_stamp", new Date());
    		bookingOrder.set("office_id", office_id);
    		bookingOrder.set("type", item.getStr("job_order_type"));
    		bookingOrder.set("order_export_date", item.get("factory_loading_time"));
    		bookingOrder.set("transport_type", item.getStr("transport_type"));
    		bookingOrder.set("pieces", item.get("pieces"));
    		bookingOrder.set("net_weight", item.get("net_weight"));
    		bookingOrder.set("gross_weight", item.get("gross_weight"));
    		bookingOrder.set("volume", item.get("volume"));
    		bookingOrder.update();
    	}
   		
   		//旧booking逻辑
   		BookOrder order  = BookOrder.dao.findFirst("select * from book_order where plan_item_id = ? ",item_id);
    	if(order==null){
    		PlanOrder re = PlanOrder.dao.findById(id);
       		order  = new BookOrder();
        	order.set("order_no", OrderNoGenerator.getNextOrderNo("BK", office_id));
        	order.set("creator", user.getLong("id"));
        	order.set("create_stamp", new Date());
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            Long to_party_id = re.getLong("to_party_id");
            Long to_ref_office_id = null ;
            if(StringUtils.isNotBlank(to_party_id.toString())){
            	Record to_party = Db.findFirst("select * from party where id = ? ",to_party_id);
            	if(to_party.getLong("ref_office_id")!=null){
            		 to_ref_office_id = to_party.getLong("ref_office_id");
            	}
            }
            order.set("ref_office_id", to_ref_office_id);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("plan_order_no", re.getStr("order_no"));
            order.set("plan_order_id", id);
            order.set("plan_item_id", item_id);            
            order.set("plan_item_id", item.getLong("id"));
            order.set("pieces", item.get("pieces"));
            order.set("net_weight", item.get("net_weight"));
            order.set("gross_weight", item.get("gross_weight"));
            order.set("volume", item.get("volume"));
            order.save();
    		
    	}else{
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("pieces", item.get("pieces"));
            order.set("net_weight", item.get("net_weight"));
            order.set("gross_weight", item.get("gross_weight"));
            order.set("volume", item.get("volume"));
            
            order.update();
    	}
    }

    
    private List<Record> getOrderItems(String orderId) {
        String itemSql = "SELECT"
   				+ " jor.id, jor.order_no,jor.transport_type, p.abbr customer_name, jor.type order_type, jor.order_export_date,"
   				+ " jos.pol, jos.pod, jor.pieces, jor.gross_weight, jor.volume, jos.cargo_desc cargo_name,"
   				+ " c_p.abbr carrier, v_p.abbr vessel, jos.voyage, jos.eta,"
   				+ " jos.etd, jos.SONO so_number, "
   				+ " jos.net_weight, jos.vgm, l.name por"
   				+ " FROM lcl_order_item loi"
   				+ " left join job_order jor on jor.id = loi.job_order_id"
   				+ " LEFT JOIN job_order_shipment jos ON jos.order_id = jor.id"
   				+ " LEFT JOIN party p ON p.id = jor.customer_id"
   				+ " LEFT JOIN party c_p on c_p.id = jos.carrier"
   				+ " LEFT JOIN party v_p on v_p.id = jos.vessel"
   				+ " left join location l on l.id =  jos.por"
   				+ " where loi.order_id = ?"
   				+ " group by loi.id ";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");

    	//获取明细表信息
    	setAttr("itemList", getOrderItems(id));
    	
    	//回显客户信息
    	Record order = Db.findFirst("select lor.*, p1.abbr MBLshipperAbbr , p2.abbr MBLconsigneeAbbr, p3.abbr MBLnotify_partyAbbr"
				+ " from lcl_order lor "
				+ " left join party p1 on p1.id=lor.MBLshipper"
				+ " left join party p2 on p2.id=lor.MBLconsignee"
				+ " left join party p3 on p3.id=lor.MBLnotify_party"
				+ " where lor.id = ?",id);

    	//用户信息
    	long creator = order.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	
    	order.set("creator_name", user.get("c_name"));
    	setAttr("order", order);

        render("/oms/LclOrder/LclOrderEdit.html");
    }
    

    
    public void list() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	String type = getPara("type_");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "order_export_date";
        	sort = "desc";
        }
        
        
        String sql = "";
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jor.office_id in ("+relist.getStr("office_id")+")";
        }
        
     sql = 		"SELECT * from (select jor.*, loc.name as pod_name,jos.sono,jos.mbl_no,jos.hbl_no,concat(ifnull(jos.sono, \"\"),ifnull(concat(\" / \",jos.mbl_no), \"\")) AS sono_mbl,if(jor.office_id != "+office_id+",'other','self') other_flag,"
     			+ " (SELECT  count(jod0.id) FROM job_order_doc jod0 WHERE  jod0.order_id =jor.id and (jod0.type='one' or jod0.type='three') and   jod0.send_status='已发送' ) new_count,"
     			+" (SELECT GROUP_CONCAT(josi.container_no SEPARATOR '<br>' ) "
    		 	+" FROM  job_order_shipment_item josi  "
    		 	+" LEFT JOIN job_order jo on jo.id=josi.order_id "
    		 	+" WHERE josi.order_id =jor.id) container_no, "
    		 	+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
				+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
				+ " LEFT JOIN currency c ON c.id = joa.currency_id "
				+ " WHERE joa.order_id=jor.id and joa.order_type='cost'  group by joa.order_type ) as char) cost, "
				+ " cast( (SELECT GROUP_CONCAT(CONCAT(fi.name,':',joa.currency_total_amount,' ',c.name)) from job_order_arap joa"
				+ " LEFT JOIN fin_item fi on fi.id = joa.charge_id "
				+ " LEFT JOIN currency c ON c.id = joa.currency_id "
				+ " WHERE joa.order_id=jor.id and joa.order_type='charge'  group by joa.order_type) as char) charge, "
				+ " ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p.company_name,p.code customer_code,ifnull(u1.c_name, u1.user_name) updator_name"
         		+ "	from job_order jor"
         		+ " LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
         		+ " left join job_order_custom_china_self_item jocc on jocc.order_id = jor.id"
         		+ " left join job_order_land_item joli on joli.order_id = jor.id"
         		+ " LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
         		+ "	left join job_order_shipment jos on jos.order_id = jor.id"
         		+ " left join location loc on jos.pod=loc.id"
         		+ "	left join party p on p.id = jor.customer_id"
         		+ "	left join user_login u on u.id = jor.creator"
         		+ " left join user_login u1 ON u1.id = jor.updator"
         		+ " WHERE (jor.office_id="+office_id+ ref_office+ ")"
         	    + " and jor.delete_flag = 'N'"
         	    + " and jor.status = '新建'"
         	    + " and ifnull(jor.lcl_order_flag,'') != 'Y'"
         	    + " GROUP BY jor.id "
         	    + " ) A where 1 = 1 ";
        
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        if (getPara("start") != null  && getPara("length") != null) {
        	if(Long.parseLong(getPara("start")) <= rec.getLong("total")){
        		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        	}else{
        		sLimit = " LIMIT 0, " + getPara("length");
        		pageIndex = "1";
        	}
            
            if("lock".equals(type)){
            	sLimit = "";
            }
        }
        
        List<Record> orderList = Db.find(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        System.out.println(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    public void orderList() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
    	String type = getPara("type_");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "order_export_date";
        	sort = "desc";
        }
        
        
        String sql = "";
        String ref_office = "";
        Record relist = Db.findFirst("select DISTINCT CAST(group_concat(ref_office_id) AS char) office_id from party where type='CUSTOMER' and ref_office_id is not null and office_id=?",office_id);
        if(relist!=null){
        	ref_office = " or jor.office_id in ("+relist.getStr("office_id")+")";
        }
        
        String condition = DbUtils.buildConditions(getParaMap());
        sql = "select lor.*,ul.c_name creator_name,"
         		+ " p1.abbr MBLshipper_name,"
         		+ " p2.abbr MBLconsignee_name,"
         		+ " p3.abbr MBLnotify_party_name,"
         		+ " GROUP_CONCAT(jor.order_no) job_order_no, "
         		+ " GROUP_CONCAT(jos.pol) pol,"
         		+ " GROUP_CONCAT(jos.pod) pod,"
         		+ " GROUP_CONCAT(jos.mbl_no) mbl_no"
         		+ " from lcl_order lor"
         		+ " LEFT JOIN lcl_order_item loi on loi.order_id = loi.id"
         		+ " LEFT JOIN job_order jor on jor.id = loi.job_order_id"
         		+ " LEFT JOIN job_order_shipment jos on jos.order_id = jor.id"
		        + " left join party p1 on p1.id = lor.MBLshipper" 
		        + " left join party p2 on p2.id = lor.MBLconsignee " 
		        + " left join party p3 on p3.id = lor.MBLnotify_party "
		        + " left join user_login ul on ul.id = lor.creator "
		        +  condition
		        + " group by lor.id"; 


        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        if (getPara("start") != null  && getPara("length") != null) {
        	if(Long.parseLong(getPara("start")) <= rec.getLong("total")){
        		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        	}else{
        		sLimit = " LIMIT 0, " + getPara("length");
        		pageIndex = "1";
        	}
        }
        
        List<Record> orderList = Db.find("select * from (" +sql + ") A order by " + sName +" "+ sort +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	list = getOrderItems(order_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
   
    
    //确认已完成计划单
    @Before(Tx.class)
    public void confirmCompleted(){
    	String id = getPara("id");
    	PlanOrder order = PlanOrder.dao.findById(id);
    	order.set("status", "已完成");
    	renderJson("{\"result\":true}");
    }
    
    //删除单据，设置为已删
    @Before(Tx.class)
    public void deleteOrder(){
    	String id = getPara("id");
    	String delete_reason = getPara("delete_reason");
    	Long deletor = LoginUserController.getLoginUserId(this);
    	Date date = new Date();
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String delete_stamp = sf.format(date);
    	Db.update("update plan_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
    			+ " delete_reason='"+delete_reason+"' where id = ?  ",id);
    	renderJson("{\"result\":true}");
    }
    

    @Before(Tx.class)
    public void confirmShipment(){
    	String item_id = getPara("item_id");
    	
    	String[] idArray = item_id.split(",");
    	for (int i = 0; i < idArray.length; i++) {
    		PlanOrderItem item = PlanOrderItem.dao.findById(idArray[i]);
    		item.set("confirm_shipment", "Y");
    		item.update();
    		
    		createBookOrder(item,item.getLong("order_id").toString());
    		createJobOrder(item);
		}

    	renderJson("{\"result\":true}");
    }
    
    
    @Before(Tx.class)
    public void createJobOrder(PlanOrderItem item){
    	Long plan_order_item_id = item.getLong("id");
    	Long plan_order_id = item.getLong("order_id");
    	JobOrder order  = JobOrder.dao.findFirst("select * from job_order where plan_order_item_id = ? ",plan_order_item_id);
    	UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		String transport_type = "";
    	String truct_type = item.getStr("truck_type");
    	String container_type = item.getStr("container_type");
    	String customs_type = item.getStr("customs_type");
    	String pickup_addr = item.get("pickup_addr");
    	if(StringUtils.isNotBlank(truct_type)||StringUtils.isNotEmpty(pickup_addr)){
    		if(StringUtils.isBlank(transport_type)){
    			transport_type += "land";
    		}else{
    			transport_type += ",land";
    		}
    	}
    	if(StringUtils.isNotBlank(container_type)){
    		if(StringUtils.isBlank(transport_type)){
    			transport_type += "ocean";
    		}else{
    			transport_type += ",ocean";
    		}
    	}
    	if(StringUtils.isNotBlank(container_type)||StringUtils.isNotEmpty((String) item.get("pol"))||StringUtils.isNotEmpty((String) item.get("pod"))){
    		if(StringUtils.isBlank(transport_type)){
    			transport_type += "ocean";
    		}else{
    			transport_type += ",ocean";
    		}
    	}
    	
    	if("代理报关".equals(customs_type)){
   			if(StringUtils.isEmpty(transport_type)){
   				transport_type+="custom";
   			}else{
   				transport_type+=",custom";
   			}
   		}
   		
    	if(order==null){
    		PlanOrder re = PlanOrder.dao.findById(plan_order_id);
    		//BookingOrder bookingOrder = BookingOrder.dao.findById(plan_order_id);
    		BookingOrder bookingOrder = BookingOrder.dao.findFirst("select * from booking_order where plan_item_id = ?",plan_order_item_id);
       		order  = new JobOrder();
       		
            String newDateStr = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yy");//转换后的格式
            Date date= item.get("factory_loading_time");
            newDateStr=sdf.format(date);
            
            Long officeNo = null;//生成单号要用到的office_id
            Record office = Db.findFirst("select office_id from party where ref_office_id = ?",office_id);
            if(office != null){
            	officeNo = office.getLong("office_id");
            }else{
            	try {
					throw new Exception("不存在ref_customer");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            	
       		String order_no = OrderNoGenerator.getNextOrderNo("EKYZH", newDateStr, officeNo==null?office_id:officeNo);
   			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
   			sb.insert(5, JobOrderController.generateJobPrefix(item.getStr("job_order_type")));//在指定的位置1，插入指定的字符串
   			order_no = sb.toString();
   			order.set("order_no", order_no);
        	order.set("creator", user.getLong("id"));
        	order.set("create_stamp", new Date());
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            
            Long to_party_id = re.getLong("to_party_id");
            Long booking_id = bookingOrder.getLong("id");
            String booking_no = bookingOrder.getStr("booking_no");
            if(StringUtils.isNotBlank(to_party_id.toString())){
            	Record to_party = Db.findFirst("select * from party where id = ? ",to_party_id);
            	Long to_ref_office_id = null ;
            	if(to_party.getLong("ref_office_id")!=null){
            		 to_ref_office_id = to_party.getLong("ref_office_id");
            		 Record customer_party = Db.findFirst("select * from party where office_id = ? and ref_office_id=? ",to_ref_office_id,office_id);
                 	if(customer_party!=null){
                 		Long customer_id = customer_party.getLong("id");
                 		order.set("customer_id", customer_id);
                 	}
            	}
            }
            order.set("from_order_type", "booking");
            order.set("from_order_id", booking_id);
            order.set("from_order_no", booking_no);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("plan_order_no", re.getStr("order_no"));
            order.set("plan_order_id", plan_order_id);
            order.set("plan_order_item_id", plan_order_item_id);
            order.set("pieces", item.get("pieces"));
            order.set("net_weight", item.get("net_weight"));
            order.set("gross_weight", item.get("gross_weight"));
            order.set("volume", item.get("volume"));
            order.set("transport_type", transport_type);
            
            //-----------默认
            order.set("billing_method", "perWeight");
            order.set("trans_clause", "CFS-CFS");
            order.set("trade_type", "FOB");

            order.save();
            
            if(bookingOrder!=null){
            	bookingOrder.set("to_order_id", order.get("id"));
            	bookingOrder.update();
            }
            
            //从表默认选项
            if(StringUtils.isNotBlank(container_type)){
            	String[] array = container_type.split(",");
            	for (int i = 0; i < array.length; i++) {
            		String[] ctypeMsg = array[i].split("X");
            		String con_type = ctypeMsg[0];
            		String number = ctypeMsg[1];
            		for (int j = 0; j < Integer.parseInt(number); j++) {
            			Record oceanItem = new Record();
            			oceanItem.set("order_id", order.get("id"));
            			oceanItem.set("container_type", con_type);
            			Db.save("job_order_shipment_item", oceanItem);
					}
            		
            	}
        	}
            
        	if(StringUtils.isNotBlank(truct_type)||StringUtils.isNotEmpty(pickup_addr)){
        		if(StringUtils.isNotBlank(truct_type)){
        			String[] array = truct_type.split(",");
                	for (int i = 0; i < array.length; i++) {
                		String[] ctypeMsg = array[i].split("X");
                		String tr_type = ctypeMsg[0];
                		String number = ctypeMsg[1];
                		for (int j = 0; j < Integer.parseInt(number); j++) {
                			Record landItem = new Record();
                			landItem.set("order_id", order.get("id"));
                			landItem.set("status", "待发车");
                			landItem.set("truck_type", tr_type);
                			landItem.set("take_address", pickup_addr);
                			Db.save("job_order_land_item", landItem);
    					}
                	}
        		}else{
        			Record landItem = new Record();
        			landItem.set("order_id", order.get("id"));
        			landItem.set("status", "待发车");
        			landItem.set("take_address", pickup_addr);
        			Db.save("job_order_land_item", landItem);
        		}
        		
        	}	
            if(StringUtils.isNotEmpty((String) item.get("pol"))||StringUtils.isNotEmpty((String) item.get("pod"))){
            	Record oceanDetail = new Record();
            	oceanDetail.set("order_id", order.get("id"));
            	oceanDetail.set("pol", item.get("pol"));
            	oceanDetail.set("pod", item.get("pod"));
            	
            	oceanDetail.set("carrier", item.get("carrier"));
            	oceanDetail.set("vessel", item.get("vessel"));
            	oceanDetail.set("voyage", item.get("voyage"));
            	oceanDetail.set("eta", item.get("eta"));
            	oceanDetail.set("etd", item.get("etd"));
            	//oceanDetail.set("SONO", item.get("SONO"));
    			Db.save("job_order_shipment", oceanDetail);
            }
    	}
    }
    
    //常用下拉字段保存进入历史记录（非明细表）
    @Before(Tx.class)
    public static void saveParamHistory(Map<String, ?> dto,List<Record> listRes,Long user_id){
    	if(dto != null ){
    		if(dto.size() <= 0){
    			return;
    		}
    		List<String> paramlist = new ArrayList<String>();//缓存到本地的数据，校验是否重复，是则跳过校验
    		for(Record listRe : listRes){
    			String type = listRe.getStr("type");//保存到user_query_history的类型
    			String param = listRe.getStr("param");//表单中对应字段的ID
    			
        		type = type.toUpperCase();
        		//param = param.toUpperCase();
        		
    			if(dto.get(param) != null){
    				String param_id = (String)dto.get(param);
    				if(paramlist.contains(param_id) || StringUtils.isBlank(param_id)){
    					continue;
    				}
    					
    				Record rec = Db.findFirst("select * from user_query_history where type=? and ref_id=? and user_id=?",type, param_id, user_id);
    		        if(rec == null){
    		            rec = new Record();
    		            rec.set("ref_id", param_id);
    		            rec.set("type", type);
    		            rec.set("user_id", user_id);
    		            rec.set("query_stamp", new Date());
    		            Db.save("user_query_history", rec);
    		        }else{
    		            rec.set("query_stamp", new Date());
    		            Db.update("user_query_history", rec);
    		        }
    		        paramlist.add(param_id);
    			}
    		}
    	}
    }
    
    //常用明细下拉列表字段保存进入历史记录
    @Before(Tx.class)
    public static void saveItemParamHistory(List<Map<String, String>> list,List<Record> listRes ,Long user_id){
    	if(list != null ){
    		if(list.size() <= 0){
    			return;
    		}
    		for(Record listRe : listRes){
    			String type = listRe.getStr("type");
    			String param = listRe.getStr("param");
    			
        		type = type.toUpperCase();
        		//param = param.toUpperCase();
        		
        		List<String> paramlist = new ArrayList<String>();
        		for(Map<String, String> map : list){
        			if(map.get(param) != null){
        				String param_id = map.get(param);
        				if(paramlist.contains(param_id) || StringUtils.isBlank(param_id)){
        					continue;
        				}
        					
        				Record rec = Db.findFirst("select * from user_query_history where type=? and ref_id=? and user_id=?",type, param_id, user_id);
        		        if(rec == null){
        		            rec = new Record();
        		            rec.set("ref_id", param_id);
        		            rec.set("type", type);
        		            rec.set("user_id", user_id);
        		            rec.set("query_stamp", new Date());
        		            Db.save("user_query_history", rec);
        		        }else{
        		            rec.set("query_stamp", new Date());
        		            Db.update("user_query_history", rec);
        		        }
        		        paramlist.add(param_id);
        			}
        		}
    		}
    	}
    }
    
    
    @Before(Tx.class)
    public void submitOrder(){
    	String order_id = getPara("order_id");
    	PlanOrder po = PlanOrder.dao.findById(order_id);
    	po.set("submit_flag", "Y");
    	po.set("new_submit_flag", "Y");
    	po.update();
    	
    	renderJson(true);
    }

}
