package controllers.wms;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PrintPatterns;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class GateOutOrderController extends Controller {

	private Logger logger = Logger.getLogger(GateOutOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private static String contextPath = null;
	private String getContextPath() {
		if(contextPath == null){
			contextPath = getRequest( ).getServletContext().getRealPath("/");
		}
		return contextPath;
	}
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/wms/gateOutOrder/list.html");
	}
	
	@Before(Tx.class)
    public void create() {
		String idArray = getPara("idArray");
		String item_no = getPara("item_no");
		String quantity = getPara("quantity");
		String kt_no = getPara("kt_no");
		
		
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
		
		Record order = new Record();
		order.set("order_no", OrderNoGenerator.getNextOrderNo("GO", office_id));
		order.set("kt_no", kt_no);
		order.set("item_no", item_no);
		order.set("item_no", item_no);
		order.set("quantity", quantity);
		order.set("office_id", office_id);
		order.set("creator", LoginUserController.getLoginUserId(this));
		order.set("create_time", new Date());
		Db.save("gate_out_order", order);
		
		String [] array = idArray.split(",");
		for (int i = 0; i < array.length; i++) {
			Record item = new Record();
			item.set("order_id", order.getLong("id"));
			item.set("item_id", array[i]);
			Db.save("gate_out_order_item", item);
			
			Db.update("update gate_in set out_order_flag = 'Y' where id = ?",array[i]);
		}
		
        renderJson(order);
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
   		if (StringUtils.isNotBlank(id)) {
   			//update
   			planOrder = PlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("updator", user.getLong("id"));
   			planOrder.set("update_time", new Date());
   			planOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("JH", office_id));
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.set("office_id", office_id);
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    
    private List<Record> getPlanOrderItems(String orderId) {
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,"
                + " p.abbr carrier_name "
                + " from plan_order_item pi "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
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

    	//用户信息
    	long creator = planOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/wms/gateOutOrder/edit.html");
    }
    

    
    public void orderList() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            String item_no = dto.get("item_no");
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and goo.item_no = '"+item_no+"'";
            }

            String begin_time = dto.get("create_time_begin_time");
            if(StringUtils.isBlank(begin_time)){
            	begin_time = "2000-01-01";
            }
            
            String end_time = dto.get("create_time_end_time");
            if(StringUtils.isBlank(end_time)){
            	end_time = "2037-01-01";
            }else{
            	end_time = end_time +" 23:59:59";
            }
            
            condition += " and goo.create_time between '"+begin_time+"' and '"+end_time+"'";
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
    	sql = "select goo.*, ifnull(u.c_name, u.user_name) creator_name"
    			+ " from gate_out_order goo "
    			+ " left join user_login u on u.id = goo.creator"
    			+ " where goo.office_id="+office_id
    			+ " group by goo.id "
    			+ condition ;
    	

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql + " order by goo.create_time desc " +sLimit);
   
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    public void  orderItemList() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");

        String order_id = getPara("order_id");
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
    	sql = "select gi.*,pro.part_name,pro.item_name from gate_out_order_item goi "
    			+ " LEFT JOIN gate_in gi on gi.id = goi.item_id"
    			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
    			+ " where order_id ='"+order_id+"'"
    			+ condition 
    			+" group by goi.id";
    	

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);

        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
   
    
    
    public void list() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            String quantity = dto.get("quantity");
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and pro.item_no = '"+item_no+"'";
            }
            
            if(StringUtils.isNotBlank(quantity)){
            	
            }
            
            if(StringUtils.isNotBlank(item_name)){
            	condition += " and item_name like '%"+item_name+"%'";
            }
            
            if(StringUtils.isNotBlank(part_name)){
            	condition += " and part_name like '%"+part_name+"%'";
            }
            
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and part_no like '%"+part_no+"%'";
            }
            
            
            String begin_time = dto.get("create_time_begin_time");
            if(StringUtils.isBlank(begin_time)){
            	begin_time = "2000-01-01";
            }
            
            String end_time = dto.get("create_time_end_time");
            if(StringUtils.isBlank(end_time)){
            	end_time = "2037-01-01";
            }else{
            	end_time = end_time +" 23:59:59";
            }
            
            //condition += " and gi.create_time between '"+begin_time+"' and '"+end_time+"'";
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
    	sql = "(select gi.id,gi.quantity,gi.shelves,gi.qr_code,gi.part_no,gi.create_time, ifnull(u.c_name, u.user_name) creator_name,pro.id product_id,pro.amount,pro.item_name,pro.item_no,pro.part_name part_name "
    			+ " from gate_in gi "
    			+ " left join user_login u on u.id = gi.creator"
    			+ " left join wmsproduct pro on pro.part_no = gi.part_no and pro.amount>0"
    			+ " where gi.office_id="+office_id
    			+ " and gi.error_flag = 'N' and gi.out_flag = 'N' and gi.out_order_flag = 'N'"
    			+ condition 
    			+ " order by gi.part_no,gi.create_time desc )"
    			+ " UNION "
    			+ " ( SELECT NULL id,0 quantity, NULL shelves, NULL qr_code,"
    			+ " part_no, NULL create_time,"
    			+ " NULL creator_name, id product_id, amount,"
    			+ " item_name, item_no, part_name "
    			+ " FROM wmsproduct pro"
    			+ " WHERE 1 = 1 and pro.amount>0"
    			+ condition +")";
        
        List<Record> orderList = Db.find(sql +sLimit);
        renderJson(orderList); 
    }
    
    //打印应付对账单PDF
  	public void printDetailPDF(){
  		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhMMss");
  		String c=sdf.format(new Date());
  		String order_id = getPara("order_id");
  		String fileName = "/report/wms/gateOutOrder.jasper";
  		String outFileName = "/download/出库单"+c;
  		HashMap<String, Object> hm = new HashMap<String, Object>();
  		hm.put("order_id", order_id);
  		fileName = getContextPath() + fileName;
  		outFileName = getContextPath() + outFileName + order_id;
  		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
  		renderText(file.substring(file.indexOf("download")-1));
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
    public void confirmCompleted(){
    	String id = getPara("id");
    	PlanOrder order = PlanOrder.dao.findById(id);
    	order.set("status", "已完成");
    	renderJson("{\"result\":true}");
    }
    

}
