package controllers.wms;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileOutputStream;
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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
        String orderBy = " order by gi.id desc";
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            //condition = DbUtils.buildConditions(dto);
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            String order_type = dto.get("order_type");
            
            if(StringUtils.isNotBlank(order_type)){
            	if("gateOut".equals(order_type)){
            		condition += " and gi.out_flag = 'Y' ";
            	}
            }
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and pro.item_no like '%"+item_no+"%'";
            }
            
            if(StringUtils.isNotBlank(item_name)){
            	condition += " and pro.item_name like '%"+item_name+"%'";
            }
            
            if(StringUtils.isNotBlank(part_name)){
            	condition += " and pro.part_name like '%"+part_name+"%'";
            	orderBy = " order by gi.qr_code";
            }
            
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and gi.part_no like '%"+part_no+"%'";
            	orderBy = " order by gi.qr_code";
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
            
            String out_begin_time = dto.get("out_time_begin_time");
            String out_end_time = dto.get("out_time_end_time");
            if(StringUtils.isNotBlank(out_begin_time) || StringUtils.isNotBlank(out_end_time)){
            	if(StringUtils.isBlank(out_begin_time)){
                	out_begin_time = "2000-01-01";
                }
                if(StringUtils.isBlank(out_end_time)){
                	out_end_time = "2037-01-01";
                }else{
                	out_end_time = out_end_time +" 23:59:59";
                }
                condition += " and gi.out_time between '"+out_begin_time+"' and '"+out_end_time+"'";
            }
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	
    	String sqlTotal= "select  count(1) total from ( "
			+ " select gi.id from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ error_flag
			+ inv_flag
			+ condition
			+ " group by gi.id ) B ";
       
    	sql = "select gi.*, ifnull(u.c_name, u.user_name) creator_name,pro.item_no,pro.id product_id,pro.item_name,pro.part_name part_name "
			+ " from gate_in gi "
			+ " left join user_login u on u.id = gi.creator"
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ error_flag
			+ inv_flag
			+ condition
			+ " group by gi.id  ";
    	
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql + orderBy + sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

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
        }

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            String order_type = dto.get("order_type");
            
            if(StringUtils.isNotBlank(order_type)){
            	if("gateOut".equals(order_type)){
            		condition += " and gi.out_flag = 'Y' ";
            	}
            }
            
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

    	sql = "SELECT sum(A.quantity) totalPiece from("
    		+ " select "
    		+ " gi.quantity "
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ error_flag
			+ condition
			+ " group by gi.id ) A";

        Record re = Db.findFirst(sql);

        renderJson(re); 
    }
    
    @Before(Tx.class)
    public void downloadList(){
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");

        String sql = "";
        String condition="";
        
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
        String order_type = null;
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class);  
            String item_no = dto.get("item_no");
            String item_name = dto.get("item_name");
            String part_name = dto.get("part_name");
            String part_no = dto.get("part_no");
            order_type = dto.get("order_type");
            
            if(StringUtils.isNotBlank(order_type)){
            	if("gateOut".equals(order_type)){
            		condition += " and gi.out_flag = 'Y' ";
            	}
            }
            
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
            	condition += " and gi.part_no like '%"+part_no+"%'";
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
            
            String out_begin_time = dto.get("out_time_begin_time");
            String out_end_time = dto.get("out_time_end_time");
            if(StringUtils.isNotBlank(out_begin_time) || StringUtils.isNotBlank(out_end_time)){
            	if(StringUtils.isBlank(out_begin_time)){
                	out_begin_time = "2000-01-01";
                }
                if(StringUtils.isBlank(out_end_time)){
                	out_end_time = "2037-01-01";
                }else{
                	out_end_time = out_end_time +" 23:59:59";
                }
                condition += " and gi.out_time between '"+out_begin_time+"' and '"+out_end_time+"'";
            }
            
    	}
    
    	sql = "select"
    		+ " A.part_no ,"
    		+ " A.part_name,"
    		+ " group_concat(distinct A.shelves SEPARATOR '  ;  ') shelves,"
    		+ " sum(A.quantity) total_quantity "
    		+ " from "
    		+ " (select gi.*,pro.item_no,pro.item_name,pro.part_name part_name "
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ error_flag
			+ inv_flag
			+ condition
			+ " group by gi.id) A group by A.part_no  ";
    	
    	String operate_time = "create_time";
    	String excelName = "入库明细";
    	if("gateOut".equals(order_type)){
    		operate_time = "out_time";
    		excelName = "出库明细";
    	}
    	
        String exportSql = sql;
        String[] headers = new String[]{"组件编码", "组件名称","货架","总数"};
        String[] fields = new String[]{"part_no", "part_name","shelves", "total_quantity"};
        String fileName = generateExcel(headers, fields, exportSql,excelName);
        renderText(fileName);
    }
    
    @SuppressWarnings("deprecation")
    public String generateExcel(String[] headers, String[] fields, String sql,String name){
	    String fileName="";
	    try {
	        System.out.println("generateExcel begin...");
	        String filePath = getRequest( ).getServletContext().getRealPath("/") + "/download/inventory";
	        File file = new File(filePath);
	        if(!file.exists()){ 
	            file.mkdir();
	        }
	        Date date = new Date();
	        SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
	        String outFileName = name +"-" + formatDate.format(date) + ".xls";
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");  

            HSSFRow rowhead = sheet.createRow((short)0);
            for (int i = 0; i < headers.length; i++) {
                rowhead.createCell((short)i).setCellValue(headers[i]);
            }
            
            List<Record> recs = Db.find(sql);
            System.out.println("sql finish!");
            HSSFRow row = null;
            HSSFCell cell = null;
            if(recs!=null){
                for (int j = 1; j <= recs.size(); j++) {
                	//HSSFDataFormat format = workbook.createDataFormat();
                	row = sheet.createRow((short)j);
                    Record rec = recs.get(j-1);
                    for (int k = 0; k < fields.length;k++){
                        Object obj = rec.get(fields[k]);
                        String strValue = "";
                        if(obj != null){
                            strValue =obj.toString();
                        }
                        cell = row.createCell((short) k);
                        //cell.setCellStyle(cellStyle);
                        //sheet.autoSizeColumn((short)k);
                        cell.setCellValue(strValue);
                    }
                }
            }

            fileName = filePath+"/"+outFileName;
            System.out.println("fileName: "+fileName);
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");
            fileName = "download/inventory/"+outFileName;
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	    return fileName;
	}

}
