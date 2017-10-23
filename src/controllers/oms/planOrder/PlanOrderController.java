package controllers.oms.planOrder;

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
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class PlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(PlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
   		long office_id = user.getLong("office_id");
   		Office office = Office.dao.findById(office_id);
   		setAttr("office", office);
        
		List<Record> configList = ListConfigController.getConfig(user_id, "/planOrder");
        setAttr("listConfigList", configList);
		render("/oms/PlanOrder/PlanOrderList.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		Office office = Office.dao.findById(office_id);
   		setAttr("office", office);
   		
   		Record re = Db.findFirst("select * from party where ref_office_id =? and office_id = ?",office_id,office_id);
   		if(re!=null){
   	   		setAttr("self_party", re);
   		}
   		
   		Record party_amount = Db.findFirst("select count(1) amount from party where ref_office_id is not null and ref_office_id!=? and office_id =?",office_id,office_id);
   		Long amount = party_amount.get("amount");
   		if(amount==1){
   			Party to_party = Party.dao.findFirst("select * from party where ref_office_id is not null and ref_office_id!=? and office_id =?",office_id,office_id);
   	   		setAttr("to_party", to_party);
   		}
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
       	Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        PlanOrder planOrder = new PlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		
   		String action_type="add";
   		if (StringUtils.isNotEmpty(id)) {
   		    action_type="update";
   			planOrder = PlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, planOrder);

   			//需后台处理的字段
   			planOrder.set("updator", user.getLong("id"));
   			planOrder.set("update_stamp", new Date());
   			planOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			Record office = Db.findFirst("select * from office_order_no_config where office_id=? and order_type='plan_order'", office_id);
   			String orderNo = "";
   			if(office!=null){
   			    orderNo = OrderNoGenerator.getNextOrderNo(office.getStr("prefix"), office_id);
   			}else {
   			    orderNo = OrderNoGenerator.getNextOrderNo("JH", office_id);
   			}
   			
   			planOrder.set("order_no", orderNo);
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.set("office_id", office_id);
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");
		
		//保存下拉列表使用历史
		//主单据
		List<Record> orderRes = new ArrayList<Record>();
		orderRes.add(new Record().set("type", "ARAP_COM").set("param", "to_party_id"));
		saveParamHistory(dto,orderRes,user.getLong("id")); 
		
		//明细表内容
		List<Record> planRes = new ArrayList<Record>();
		planRes.add(new Record().set("type", "port").set("param", "POL"));
		planRes.add(new Record().set("type", "port").set("param", "POD"));
		planRes.add(new Record().set("type", "carrier").set("param", "CARRIER"));
		planRes.add(new Record().set("type", "UNIT").set("param", "UNIT_ID"));
		planRes.add(new Record().set("type", "port").set("param", "POR"));
		saveItemParamHistory(itemList,planRes,user.getLong("id")); 
		
		SysInfoController.saveLog(jsonStr, id, user, action_type, "计划单", "");

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
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

    
    private List<Record> getPlanOrderItems(String orderId) {
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,bor.id book_order_id,"
        		+ "bor.booking_no book_order_no,pl.submit_flag, p.abbr carrier_name "
                + " from plan_order pl "
                + " LEFT JOIN plan_order_item pi  on pl.id = pi.order_id "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
                + " left join booking_order bor on bor.plan_item_id = pi.id"
                +" left join party p on pi.carrier=p.id"
                +" left join unit u on u.id=pi.unit_id"
                +" where order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	UserLogin user1 = LoginUserController.getLoginUser(this);
        long office_id=user1.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("plan_order", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
    	
    	PlanOrder planOrder = PlanOrder.dao.findById(id);
    	setAttr("order", planOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getPlanOrderItems(id));
    	
    	//回显客户信息
    	Party party = Party.dao.findById(planOrder.getLong("customer_id"));
    	setAttr("party", party);
    	Office entrusted = Office.dao.findById(planOrder.getLong("entrusted_id"));
    	setAttr("entrusted", entrusted);
    	Office toEntrusted = Office.dao.findById(planOrder.getLong("to_entrusted_id"));
    	setAttr("toEntrusted", toEntrusted);
    	
    	Record self_party = Db.findFirst("select * from party where id = ?",planOrder.getLong("self_party_id"));
    	setAttr("self_party", self_party);
    	Party to_party = Party.dao.findById(planOrder.getLong("to_party_id"));
    	setAttr("to_party", to_party);

    	
    	//用户信息
    	long creator = planOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
    	//当前
   		Office office = Office.dao.findById(office_id);
   		setAttr("office", office);
   		
		//forwarderCompany货代公司打开该单时，new_submit_flag标志为n
   		UserLogin login_user = LoginUserController.getLoginUser(this);
   		Office office2=Office.dao.findById(login_user.getLong("office_id"));
   		 setAttr("login_office", office2);
   		if(office2.getStr("type")!=null&&"forwarderCompany".equals(office2.getStr("type"))){
   			planOrder.set("new_submit_flag", "N");
   			planOrder.update();
   		}
   		
        render("/oms/PlanOrder/PlanOrderEdit.html");
    }
    

    
    public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");

    	String type=getPara("type_");
    	
        String sLimit = "";
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"create_stamp":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "create_stamp";
        	sort ="desc";
        }
        
        if(StringUtils.isNotEmpty(getPara("partyId"))){
    		String partyId =getPara("partyId");
    		//常用客户保存进入历史记录
          	Long userId = LoginUserController.getLoginUserId(this);
          	JobOrderController.addHistoryRecord(userId,partyId,"ARAP_COM");
    	}
        
        
        
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        //代办事项
        String dai_condition = "";
        if("todo".equals(type)){
//        	sql =" SELECT "
//        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name "
//        			+ " FROM plan_order po "
//        			+ " LEFT JOIN plan_order_item poi ON po.id = poi.order_id "
//        			+ " left join party p on p.id = po.customer_id "
//        			+ " left join user_login u on u.id = po.creator "
//        			+ " WHERE (po.office_id="+office_id+" or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y')) and is_gen_job='N' AND factory_loading_time is not NULL "
//        			+ " AND datediff(factory_loading_time, now())<=5"
//        			+ " and po.delete_flag = 'N'";
        	dai_condition = " and 'N' in (select group_concat(confirm_shipment) from plan_order_item where order_id = po.id) and ifnull(poi.id,'') != ''"
        			+ "  AND datediff(poi.factory_loading_time, now())<=5  ";
        }else if ("customwaitPlan".equals(type)){
//        	sql =" SELECT "
//        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p. CODE"
//        			+ " FROM"
//        			+ "	plan_order po"
//        			+ " LEFT JOIN plan_order_item poi ON poi.order_id = po.id"
//        			+ " LEFT JOIN user_login u ON u.id = po.creator"
//        			+ " LEFT JOIN party p ON p.id = po.customer_id"
//        			+ " WHERE"
//        			+ " (po.office_id="+office_id+" or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y'))  and poi.customs_type = '自理报关'"
//        			+ " AND poi.is_gen_job = 'N'"
//        			+ " and po.delete_flag = 'N'"
//        			+ " GROUP BY poi.id ";
        	dai_condition = " and 'N' in (select group_concat(confirm_shipment) from plan_order_item where order_id = po.id)  and ifnull(poi.id,'') != ''"
        			+ " and poi.customs_type = '自理报关'";
        }
        	sql = "SELECT * from (select"
        			+ " (GROUP_CONCAT(CONCAT(ifnull(cast(poi.factory_loading_time as char),'<span style=\"color:red;\">无出货时间</span>'), "
        			+ " IFNULL(if((poi.CARRIER is not null or poi.VESSEL is not null or poi.VOYAGE is not null)and poi.confirm_shipment = 'N',' <span style=\"color:#4caf50;\">已定仓</span>',null),IF (poi.confirm_shipment = 'Y',' 已确认出货',' 新建'))) SEPARATOR '<br/>')) item_status,"
        			+ " if(((select count(1) from plan_order_item "
        			+ " where order_id = po.id and confirm_shipment = 'Y')=count(poi.id) and count(poi.id)!=0),'已完成',"
        			+ " if(po.submit_flag='N' and "
        			+ " (select count(1) from plan_order_item where order_id = po.id and confirm_shipment = 'Y')=0,"
        			+ " '新建','处理中')) order_status,"
        			+ " (GROUP_CONCAT((poi.job_order_type) SEPARATOR '<br>')) job_order_type,"
        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name ,ifnull(o.office_abbr,p.abbr) sp_name"
    			+ " from plan_order po "
    			+ " LEFT JOIN plan_order_item poi on poi.order_id = po.id"
    			+ " LEFT JOIN office o ON o.id = po.to_entrusted_id "
    			+ " LEFT JOIN party p ON p.id = po.to_party_id "
    			+ " left join user_login u on u.id = po.creator"
    			+ " where (po.office_id="+office_id
    			+ " or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y')) and po.delete_flag = 'N'"
    			+ dai_condition
    			+ "	group by po.id"
    			+ " ) A where 1=1 ";
        
        condition = DbUtils.buildConditions(getParaMap());
        
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by  " + sName +" "+ sort + sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }

	//异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	list = getPlanOrderItems(order_id);

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
    	order.update();
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
