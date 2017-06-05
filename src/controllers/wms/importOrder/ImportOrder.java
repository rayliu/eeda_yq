package controllers.wms.importOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.wms.GateIn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import au.com.bytecode.opencsv.CSVReader;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.ReaderXLS;
import controllers.util.ReaderXlSX;
import controllers.util.bigExcel.BigXlsxHandleUitl;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ImportOrder extends Controller {

	private Logger logger = Logger.getLogger(ImportOrder.class);
	Subject currentUser = SecurityUtils.getSubject();
	public static boolean isImporting = false;
	
	@Before(EedaMenuInterceptor.class)
	public void index() {
	    render("/wms/import/list.html");
	}
	
	//导入CSV
	@Before(Tx.class)
	public void importMac() {   
		String order_type = getPara("order_type");
		UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		String fileName = file.getName();
		UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
		Record resultMap = new Record();
		CSVReader csvReader = null;  
		
		String doc_name = null;
		String[] fileArray = fileName.split("_");
		doc_name =fileArray[0]+"_"+fileArray[1]+"_"+fileArray[2].substring(0, 6)+fileName.subSequence(fileName.length()-4, fileName.length());
		
	    try {  
	    	if(isImporting){
	    		throw new Exception("有文件正在上传，请稍等1~2分钟。。。");
	    	}
	    	
	    	Record import_log = Db.findFirst("select * from import_log where doc_name = ?",doc_name);
			if(import_log == null){
		        csvReader = new CSVReader(new FileReader(file),',');//importFile为要导入的文本格式逗号分隔的csv文件，提供getXX/setXX方法    
		        if(fileName.endsWith(".csv")){  
		        	
		        	Record re = new Record();
		        	re.set("office_id", officeId);
		        	re.set("doc_name", doc_name);
		        	re.set("creator", user.get("id"));
		        	re.set("create_time", new Date());
		        	long start = Calendar.getInstance().getTimeInMillis();
		        	isImporting = true;
		        	
		        	CheckOrder checkOrder = new CheckOrder();
		        	if(fileName.indexOf("入库记录")>-1){
		        		resultMap = checkOrder.importGateInValue(csvReader, officeId);
		        	}else if(fileName.indexOf("出库记录")>-1){
		        		resultMap = checkOrder.importGateOutValue(csvReader, officeId);
		        	}else if(fileName.indexOf("盘点单")>-1){
		        		resultMap = checkOrder.importInvCheckValue(csvReader, officeId);
		        	}else{
	                    throw new Exception("文件《"+fileName+"》中未检测到\"入库记录\"，\"出库记录\"，\"盘点单\"关键字<br/>请核查此文件是否为要导入的数据表");
		        	}
		        	
		        	if(resultMap.get("result")){
		        		long end = Calendar.getInstance().getTimeInMillis();
			            long time = (end- start)/1000;
			        	re.set("complete_time", new Date());
			        	re.set("import_time", time);
			        	Db.save("import_log", re);
			        	isImporting = false;
		        	}
		        	
		        }else{
		        	throw new Exception("导入格式有误，请导入正确的csv格式");
		        }
			}else{
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，相同文件不可重复导入");
			}
	    } catch (Exception e) {  
	        e.printStackTrace(); 
	        resultMap.set("result", false);
	        resultMap.set("cause", e.getMessage());
	        isImporting = false;
	    } 
	    renderJson(resultMap);
	}  
	
//	// 导入单据
//	public void importOrder() {
//		String order_type = getPara("order_type");
//		
//		UploadFile uploadFile = getFile();
//		File file = uploadFile.getFile();
//		String fileName = file.getName();
//		String strFile = file.getPath();
//
//		UserLogin user = LoginUserController.getLoginUser(this);
//        Long userId = user.getLong("id");
//        Long officeId = user.getLong("office_id");
//		Record resultMap = new Record();
//		try {
//			String[] title = null;
//			List<Map<String, String>> content = new ArrayList<Map<String, String>>();
//			//exel格式区分
//			if (fileName.endsWith(".xls")) {
//				title = ReaderXLS.getXlsTitle(file);
//				content = ReaderXLS.getXlsContent(file);
//			} else if (fileName.endsWith(".xlsx")) {
//				//title = ReaderXlSX.getXlsTitle(file);
//				//content = ReaderXlSX.getXlsContent(file);
//				System.out.println("read content successful!!!");
//			} else {
//				resultMap.set("result", false);
//				resultMap.set("cause", "导入失败，请选择正确的excel文件（xls/xlsx）");
//			}
//			
//			//导入模板表头（标题）校验
//			if (title != null && content.size() > 0) {
//				CheckOrder checkOrder = new CheckOrder();
//				if (checkOrder.checkoutExeclTitle(title, order_type)) {
//					if("product".equals(order_type)){
//						// 内容校验
//						//resultMap = checkOrder.importProductCheck(content);
//						
//						// 内容开始导入
//						//if(resultMap.getBoolean("result")){
//							resultMap = checkOrder.importProductValue(content, userId, officeId);
//						//}
//					}
//				} else {
//					resultMap.set("result", false);
//					resultMap.set("cause", "导入失败，excel标题列与模板excel标题列不一致");
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			resultMap.set("result", false);
//			resultMap.set("cause","导入失败，请检测excel内容是否填写规范<br/>（建议使用Microsoft Office Excel软件操作数据）");
//		}
//		logger.debug("result:" + resultMap.get("result") + ",cause:"
//				+ resultMap.get("cause"));
//
//		renderJson(resultMap);
//	}
	
	
	// 导入单据
	public void importOrder() {
		Connection conn = null;
		Record result = new Record();
		String order_type = getPara("order_type");
		
		UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		String fileName = file.getName();
		String strFile = file.getPath();

		UserLogin user = LoginUserController.getLoginUser(this);
        Long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
		Record resultMap = new Record();
		try {
			String[] title = null;
			List<Map<String, String>> content = new ArrayList<Map<String, String>>();
			//exel格式区分
			 conn = DbKit.getConfig().getDataSource().getConnection();
			 DbKit.getConfig().setThreadLocalConnection(conn);
			 conn.setAutoCommit(false);// 自动提交变成false
			 if (fileName.endsWith(".xlsx")) {
				 
				//导入前先清除掉表中数据
				Db.update("delete from wmsproduct");
				BigXlsxHandleUitl.processFile(strFile);
				conn.commit();
				resultMap.set("result", true);
				resultMap.set("cause","导入成功！");
			} else {
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，目前只支持（xlsx）格式文件");
			}

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.set("result", false);
			resultMap.set("cause","导入失败，请检测excel内容是否填写规范<br/>（建议使用Microsoft Office Excel软件操作数据）");
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		renderJson(resultMap);
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
            
            condition += " and create_time between '"+begin_time+"' and '"+end_time+"'";
            
    	}
        
    	if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
    	
    	String sqlTotal= "select  count(1) total from ( "
			+ " select * from import_log il where il.office_id="+office_id
			+ " ) B ";
       
    	sql = "select il.*,ul.c_name user_name from import_log il"
    		+ "	left join user_login ul on ul.id = il.creator"
			+ " where il.office_id="+office_id 
			+ " order by il.id desc";
    	
        
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
}
