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
import models.wms.GateIn;
import models.wms.GateOut;

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
public class ErrorReportController extends Controller {

	private Logger logger = Logger.getLogger(ErrorReportController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		
		String sql = "(select distinct error_msg"
				+ " from gate_in gi "
				+ " where gi.error_flag = 'Y'"
				+ " group by gi.id)"
				+ " union"
				+ " (select distinct error_msg "
				+ " from gate_out gi "
				+ " where gi.error_flag = 'Y' "
				+ " group by gi.id)";

	    List<Record> orderList = Db.find(sql);
	    setAttr("errorList", orderList);
		
		render("/wms/report/error_report.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
        render("/wms/report/edit.html");
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
            //condition = DbUtils.buildConditions(dto);
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            String error_msg = dto.get("error_msg");
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and pro.item_no like '%"+item_no+"%'";
            }
            
            if(StringUtils.isNotBlank(item_name)){
            	condition += " and pro.item_name like '%"+item_name+"%'";
            }
            
            if(StringUtils.isNotBlank(part_name)){
            	condition += " and pro.part_name like '%"+part_name+"%'";
            }
            
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and pro.part_no like '%"+part_no+"%'";
            }
            
            if(StringUtils.isNotBlank(error_msg)){
            	condition += " and gi.error_msg = '"+error_msg+"'";
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
            
            condition += " and gi.create_time between '"+begin_time+"' and '"+end_time+"'";
            
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
    	sql = "(select '入库记录' order_type ,gi.id,gi.error_msg,gi.qr_code,gi.shelves,gi.quantity,gi.move_flag,gi.create_time, ifnull(u.c_name, u.user_name) creator_name,pro.item_no,pro.id product_id,pro.item_name,pro.part_name part_name,pro.part_no "
			+ " from gate_in gi "
			+ " left join user_login u on u.id = gi.creator"
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.error_flag = 'Y' and gi.office_id="+office_id
			+ condition+" group by gi.id)"
			+ " union"
			+ " (select '出库记录' order_type  ,gi.id,gi.error_msg,gi.qr_code,gi.shelves,gi.quantity,gi.move_flag,gi.create_time, ifnull(u.c_name, u.user_name) creator_name,pro.item_no,pro.id product_id,pro.item_name,pro.part_name part_name,pro.part_no "
			+ " from gate_out gi "
			+ " left join user_login u on u.id = gi.creator"
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.error_flag = 'Y' and  gi.office_id="+office_id
			+ condition+"  group by gi.id)";
    	
        
        String sqlTotal = "select count(1) total from ("+sql+") A";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find("select * from(" + sql + ") A order by create_time desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    public void gateIn(){
    	String idArray = getPara("idArray");
    	
    	List<GateOut> reList = GateOut.dao.find("select * from gate_out where id in ("+idArray+")");
    	for (GateOut re :reList) {
			GateIn gi = new GateIn();
			gi.set("office_id", re.getLong("office_id"));
			gi.set("qr_code", re.getStr("qr_code"));
			gi.set("part_no", re.getStr("part_no"));
			gi.set("quantity", re.get("quantity"));
			gi.set("shelves", re.getStr("shelves"));
			gi.set("move_flag", re.getStr("move_flag"));
			gi.set("creator", re.get("creator"));
			gi.set("creator_code", re.getStr("creator_code"));
			gi.set("create_time", new Date());
			gi.set("self_in_flag", "Y");
			gi.set("out_flag", "Y");
			gi.save();
			
			re.set("error_flag", "N").update();
		}
    	
    	renderJson(true);
    }

}
