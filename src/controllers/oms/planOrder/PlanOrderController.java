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
import controllers.oms.jobOrder.JobOrderController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
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
   		
   		Record re = Db.findFirst("select * from party where type='SP' and ref_office_id is not null and office_id = ?",office_id);
   		if(re!=null){
   			Long ref_office_id = re.getLong("ref_office_id");
   	   		Office ref_office = Office.dao.findById(ref_office_id);
   	   		setAttr("ref_office", ref_office);
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
   		if (StringUtils.isNotEmpty(id)) {
   			//update
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
   			if(office_id==8){
   				planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("XXJH", office_id));
   			}else if (office_id==9){
   				planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("XXHKJH", office_id));
   			}else if(office_id==1){
   				planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("YQJH", office_id));
   			}
   			
   			
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.set("office_id", office_id);
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");
		
		
//		List<PlanOrderItem> reList = PlanOrderItem.dao.find("select * from plan_order_item where order_id = ?",id);
//		for(PlanOrderItem item:reList){
//			createBookOrder(item,id);
//		}
		

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    @Before(Tx.class)
    public void createBookOrder(PlanOrderItem item,String id){
    	Long item_id = item.getLong("id");
    	BookOrder order  = BookOrder.dao.findFirst("select * from book_order where plan_item_id = ? ",item_id);
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
    	if(order==null){
    		PlanOrder re = PlanOrder.dao.findById(id);
       		order  = new BookOrder();
        	order.set("order_no", OrderNoGenerator.getNextOrderNo("BK", office_id));
        	order.set("creator", user.getLong("id"));
        	order.set("create_stamp", new Date());
        	order.set("updator", user.getLong("id"));
        	order.set("update_stamp", new Date());
            order.set("office_id", office_id);
            order.set("type", item.getStr("job_order_type"));
            order.set("order_export_date", item.get("factory_loading_time"));
            order.set("transport_type", item.getStr("transport_type"));
            order.set("plan_order_no", re.getStr("order_no"));
            order.set("plan_order_id", id);
            order.set("plan_item_id", item_id);
            order.set("customer_id", re.getLong("customer_id"));
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
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,bor.id book_order_id,bor.order_no book_order_no,"
                + " p.abbr carrier_name "
                + " from plan_order_item pi "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
                + " left join book_order bor on bor.plan_item_id = pi.id"
                +" left join party p on pi.carrier=p.id"
                +" left join unit u on u.id=pi.unit_id"
                +" where order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
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

    	
    	//用户信息
    	long creator = planOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
    	//当前
    	long office_id = user.getLong("office_id");
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

    	String type=getPara("type");
    	
        String sLimit = "";
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"create_stamp":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "create_stamp";
        }
        
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        if("todo".equals(type)){
        	sql =" SELECT "
        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name "
        			+ " FROM plan_order po "
        			+ " LEFT JOIN plan_order_item poi ON po.id = poi.order_id "
        			+ " left join party p on p.id = po.customer_id "
        			+ " left join user_login u on u.id = po.creator "
        			+ " WHERE (po.office_id="+office_id+" or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y')) and is_gen_job='N' AND factory_loading_time is not NULL "
        			+ " AND datediff(factory_loading_time, now())<=5"
        			+ " and po.delete_flag = 'N'";
        }else if ("customwaitPlan".equals(type)){
        	sql =" SELECT "
        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p. CODE"
        			+ " FROM"
        			+ "	plan_order po"
        			+ " LEFT JOIN plan_order_item poi ON poi.order_id = po.id"
        			+ " LEFT JOIN user_login u ON u.id = po.creator"
        			+ " LEFT JOIN party p ON p.id = po.customer_id"
        			+ " WHERE"
        			+ " (po.office_id="+office_id+" or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y'))  and poi.customs_type = '自理报关'"
        			+ " AND poi.is_gen_job = 'N'"
        			+ " and po.delete_flag = 'N'"
        			+ " GROUP BY poi.id ";
        }else{
        	sql = "SELECT * from (select"
        			+ " (GROUP_CONCAT(CONCAT(ifnull(cast(poi.factory_loading_time as char),'<span style=\"color:red;\">无出货时间</span>'), "
        			+ " IFNULL(if((poi.CARRIER is not null or poi.VESSEL is not null or poi.VOYAGE is not null)and poi.confirm_shipment = 'N',' <span style=\"color:#4caf50;\">已定仓</span>',null),IF (poi.confirm_shipment = 'Y',' 已确认出货',' 新建'))) SEPARATOR '<br/>')) item_status,"
        			+ " if(((select count(1) from plan_order_item "
        			+ " where order_id = po.id and confirm_shipment = 'Y')=count(poi.id) and count(poi.id)!=0),'已完成',"
        			+ " if(po.submit_flag='N' and "
        			+ " (select count(1) from plan_order_item where order_id = po.id and confirm_shipment = 'Y')=0,"
        			+ " '新建','处理中')) order_status,"
        			+ " (GROUP_CONCAT((poi.job_order_type) SEPARATOR '<br>')) job_order_type,"
        			+ " po.*, ifnull(u.c_name, u.user_name) creator_name ,o.office_abbr sp_name"
    			+ " from plan_order po "
    			+ " LEFT JOIN plan_order_item poi on poi.order_id = po.id"
    			+ " LEFT JOIN office o ON o.id = po.to_entrusted_id "
    			+ " left join user_login u on u.id = po.creator"
    			+ " where (po.office_id="+office_id
    			+ " or (ifnull(po.to_entrusted_id,'')="+office_id+" and po.submit_flag='Y')) and po.delete_flag = 'N'"
    			+ "	group by po.id"
    			+ " ) A where 1=1 ";
        }
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
    	if(StringUtils.isNotBlank(truct_type)){
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
   		
    	if(order==null){
    		PlanOrder re = PlanOrder.dao.findById(plan_order_id);
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
            
            Long entrusted_id = re.getLong("entrusted_id");
            if(StringUtils.isNotBlank(entrusted_id.toString())){
            	Record customer = Db.findFirst("select * from party where type='CUSTOMER' and ref_office_id = ? ",entrusted_id);
            	if(customer!=null){
            		Long customer_id = customer.getLong("id");
            		order.set("customer_id", customer_id);
            	}
            }
            
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
            
            //从表默认选项
            if(StringUtils.isNotBlank(container_type)){
            	String[] array = container_type.split(",");
            	for (int i = 0; i < array.length; i++) {
            		String[] ctypeMsg = array[i].split("X");
            		String con_type = ctypeMsg[0];
            		String number = ctypeMsg[1];
            		for (int j = 0; j < Integer.parseInt(number); j++) {
            			Record landItem = new Record();
            			landItem.set("order_id", order.get("id"));
            			landItem.set("container_type", con_type);
            			Db.save("job_order_shipment_item", landItem);
					}
            		
            	}
        		
        		
        		
        	}
        	if(StringUtils.isNotBlank(truct_type)){
        		String[] array = truct_type.split(",");
            	for (int i = 0; i < array.length; i++) {
            		String[] ctypeMsg = array[i].split("X");
            		String tr_type = ctypeMsg[0];
            		String number = ctypeMsg[1];
            		for (int j = 0; j < Integer.parseInt(number); j++) {
            			Record oceanItem = new Record();
            			oceanItem.set("order_id", order.get("id"));
            			oceanItem.set("status", "待发车");
            			oceanItem.set("truck_type", tr_type);
            			Db.save("job_order_land_item", oceanItem);
					}
            	}
            	
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
