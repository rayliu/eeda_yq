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
		String jsonStr = getPara("jsonStr");
		Gson gson = new Gson(); 
        Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
        String item_no = dto.get("item_no");
        String quantity = dto.get("quantity");
        String kt_no = dto.get("kt_no");

		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
		
		Record order = new Record();
		order.set("order_no", OrderNoGenerator.getNextOrderNo("GO", office_id));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String timeString = sdf.format(new Date());
		
		if(StringUtils.isBlank(kt_no)){
			order.set("kt_no", timeString);
		}else{
			order.set("kt_no", kt_no);
		}
		order.set("item_no", item_no);
		order.set("quantity", quantity);
		order.set("office_id", office_id);
		order.set("creator", LoginUserController.getLoginUserId(this));
		order.set("create_time", new Date());
		Db.save("gate_out_order", order);
        renderJson(order);
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
    			+ " group by goo.id ";
    	

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
    
    
    public void actualList() {
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
       
    	sql = "SELECT goo.*,go.date_no, ifnull(u.c_name, u.user_name) creator_name "
    			+ " FROM `gate_out` go"
    			+ " LEFT JOIN gate_out_order goo on goo.order_no = go.order_no"
    			+ " left join user_login u on u.id = go.creator"
    			+ " GROUP BY go.date_no";
    	

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql  +sLimit);
   
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
       
    	sql = "select gi.*,pro.part_name,pro.item_no,pro.item_name from gate_in gi "
    			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
    			+ " where out_order_id ='"+order_id+"'"
    			+ condition 
    			+" group by gi.id";
    	

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
		Gson gson = new Gson(); 
        Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  

        String quantity = dto.get("quantity");
        if(StringUtils.isNotBlank(quantity)){
        	quantity = dto.get("quantity");
        }else{
        	quantity = "0";
        }
        
        String item_no = dto.get("item_no");
        if(StringUtils.isNotBlank(item_no)){
        	condition = " and pro.item_no = '"+item_no+"'";
        }else{
        	condition = " and pro.item_no = 'empty'";
        }
    	
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	
    	 sql = "SELECT "
    	    		+ "	A.*, sum(ifnull(A.quantity,0)) shelves_total_piece "
    				+ " FROM "
    				+ "	( "
    				+ "		SELECT "
    				+ "			gi.part_no, "
    				+ "			gi.quantity, "
    				+ "			pro.part_name, "
    				+ "			pro.item_no, "
    				+ "			pro.item_name, "
    				+ "			pro.amount, "
    				+ "			("+quantity+" * pro.amount) act_quantity "
    				+ "		FROM "
    				+ "			gate_in gi "
    				+ "		LEFT JOIN wmsproduct pro ON pro.part_no = gi.part_no "
    				+ "		WHERE "
    				+ "		 gi.out_flag = 'N' "
    				+ "		AND gi.error_flag = 'N' "
    				+ "		and gi.office_id="+office_id
    				+ condition
    				+ "		GROUP BY gi.id "
    				+ "		UNION ALL "
    				+ "			SELECT "
    				+ "				pro.part_no, "
    				+ "				NULL quantity, "
    				+ "				pro.part_name, "
    				+ "				pro.item_no, "
    				+ "				pro.item_name, "
    				+ "				pro.amount, "
    				+ "				("+quantity+" * pro.amount) act_quantity "
    				+ "			FROM "
    				+ "				wmsproduct pro "
    				+ "			WHERE "
    				+ "				 pro.office_id="+office_id
    				+ condition
    				+ "	) A "
    				+ " GROUP BY "
    				+ "	A.part_no";
    	
        
        Record rec = Db.findFirst("select count(1) total from ("+sql+")B");
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    
    //打印应付对账单PDF
  	public void printDetailPDF(){
  		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhMMss");
  		String c=sdf.format(new Date());
  		String order_id = getPara("order_id");
  		Record re = Db.findById("gate_out_order", order_id);
  		
  		String fileName = "/report/wms/gateOutOrder.jasper";
  		String outFileName = "/download/出库单"+c;
  		HashMap<String, Object> hm = new HashMap<String, Object>();
  		hm.put("order_id", order_id);
  		hm.put("itemNo", re.getStr("item_no"));
  		hm.put("totalQuantity", re.get("quantity"));
  		fileName = getContextPath() + fileName;
  		outFileName = getContextPath() + outFileName + order_id;
  		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
  		renderText(file.substring(file.indexOf("download")-1));
  	}
  
    public void searchKT(){
    	String kt_no = getPara("kt_no");
    	
    	Record re = Db.findFirst("select * from gate_out_order where kt_no = ?",kt_no);
    	
    	if(re==null){
    		renderJson(true);
    	}else{
    		renderJson(re);
    	}
    	
    }
    
}
