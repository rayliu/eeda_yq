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

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class GateInController extends Controller {

	private Logger logger = Logger.getLogger(GateInController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/wms/gateIn/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
        render("/wms/gateIn/edit.html");
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
   			planOrder.set("update_stamp", new Date());
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
    	
        render("/wms/gateInOrder/edit.html");
    }
    

    
    public void list() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String error_flag = getPara("error_flag");
        String out_flag = getPara("out_flag");
        String inv_flag = getPara("inv_flag");
        if(StringUtils.isNotBlank(inv_flag)){
        	inv_flag = " and inv_flag = '"+inv_flag+"'";
        }else{
        	inv_flag = "";
        }
        if(StringUtils.isNotBlank(out_flag)){
        	out_flag = " and out_flag = '"+out_flag+"'";
        }else{
        	out_flag = "";
        }
        if(StringUtils.isNotBlank(error_flag)){
        	error_flag = " and error_flag = '"+error_flag+"'";
        }else{
        	error_flag = "";
        }

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            //condition = DbUtils.buildConditions(dto);
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and item_no like '%"+item_no+"%'";
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
            
            condition += " and create_time between '"+begin_time+"' and '"+end_time+"'";
            
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
    	sql = "SELECT * from (select gi.*, ifnull(u.c_name, u.user_name) creator_name,pro.item_no,pro.id product_id,pro.item_name,pro.part_name part_name "
			+ " from gate_in gi "
			+ " left join user_login u on u.id = gi.creator"
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ out_flag
			+ error_flag
			+ inv_flag
			+ " ) A where 1=1 ";
    	
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_time desc " +sLimit);
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
   

}
