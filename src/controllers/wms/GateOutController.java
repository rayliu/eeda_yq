package controllers.wms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import controllers.profile.LoginUserController;
import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;
import models.UserLogin;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class GateOutController extends Controller {

	private Logger logger = Logger.getLogger(GateOutController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/wms/gateOut/list.html");
	}

    public void list() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String error_flag = getPara("error_flag");
        if(StringUtils.isNotBlank(error_flag)){
        	error_flag = " and error_flag = '"+error_flag+"'";
        }else{
        	error_flag = "";
        }
        String joinStr = " ";
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
            	condition += " and pro.item_no like '%"+item_no+"%'";
            	joinStr = " left join wmsproduct pro on pro.part_no = go.part_no";
            }
            
            if(StringUtils.isNotBlank(item_name)){
            	condition += " and pro.item_name like '%"+item_name+"%'";
            	joinStr = " left join wmsproduct pro on pro.part_no = go.part_no";
            }
            
            if(StringUtils.isNotBlank(part_name)){
            	condition += " and pro.part_name like '%"+part_name+"%'";
            	joinStr = " left join wmsproduct pro on pro.part_no = go.part_no";
            }
            
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and go.part_no like '%"+part_no+"%'";
            }
            
            
            String begin_time = dto.get("create_time_begin_time");
            if(StringUtils.isBlank(begin_time)){
            	begin_time = "2016-01-01";
            }
            
            String end_time = dto.get("create_time_end_time");
            if(StringUtils.isBlank(end_time)){
            	end_time = "2025-01-01";
            }else{
            	end_time = end_time +" 23:59:59";
            }
            
            condition += " and go.create_time between '"+begin_time+"' and '"+end_time+"'";
            
    	}
        int length = getParaToInt("length");
        int start = getParaToInt("start");
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + start + ", " + length;
        }
    	
    	String sqlTotal = "select count(1) total from (select distinct go.id"
    		+ " from gate_out go  "
    	    + joinStr
			+ " where go.office_id="+office_id
			+ error_flag
			+ condition
			+ " ) B";
       
    	
    	sql = "select go.*, ifnull(u.c_name, u.user_name) creator_name,pro.item_no,pro.id product_id,pro.item_name,pro.part_name part_name "
			+ " from gate_out go "
			+ " left join user_login u on u.id = go.creator"
			+ " left join wmsproduct pro on pro.part_no = go.part_no"
			+ " where go.office_id="+office_id
			+ error_flag
			+ condition
			+ " group by go.id"
			+ " order by go.id desc";
    	
        
//    	long start = System.currentTimeMillis();
//        Record rec = Db.findFirst(sqlTotal);
//        long end = System.currentTimeMillis();
//        System.out.println("sqlTotal cost:" + (end-start));
       
        long start1 = System.currentTimeMillis();
        List<Record> orderList = Db.find(sql +sLimit);
        long end1 = System.currentTimeMillis();
        
        System.out.println("sql cost:" + (end1-start1));
        int total = 0;
        if(orderList.size()==length) {
            int page = Math.round(start/length)+1;
            total = page*length+1;
        }
        
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", total);
        orderListMap.put("recordsFiltered", total);

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    
    public void getTotalQuantity(){
    	String sql = "";
        String condition="";
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String error_flag = getPara("error_flag");
        if(StringUtils.isNotBlank(error_flag)){
        	error_flag = " and error_flag = '"+error_flag+"'";
        }else{
        	error_flag = "";
        }

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            
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
            
            
            String begin_time = dto.get("create_time_begin_time");
            if(StringUtils.isBlank(begin_time)){
            	begin_time = "2016-01-01";
            }
            
            String end_time = dto.get("create_time_end_time");
            if(StringUtils.isBlank(end_time)){
            	end_time = "2025-01-01";
            }else{
            	end_time = end_time +" 23:59:59";
            }
            
            condition += " and go.create_time between '"+begin_time+"' and '"+end_time+"'";
            
    	}

        sql = "SELECT sum(A.quantity) totalPiece from("
            + " select "
            + " go.quantity "
            + " from gate_out go "
            + " left join wmsproduct pro on pro.part_no = go.part_no"
            + " where go.office_id="+office_id
            + error_flag
            + condition
            + " group by go.id ) A";
    	

        Record re = Db.findFirst(sql);

        renderJson(re); 
    }
 

}
