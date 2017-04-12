package controllers.wms.importOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.wms.GateIn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import au.com.bytecode.opencsv.CSVReader;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.ReaderXLS;
import controllers.util.ReaderXlSX;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ImportOrder extends Controller {

	private Logger logger = Logger.getLogger(ImportOrder.class);
	Subject currentUser = SecurityUtils.getSubject();


	//导入CSV
	@Before(Tx.class)
	public void importMac() throws Exception {   
		String order_type = getPara("order_type");
		UploadFile uploadFile = getFile();
		File file = uploadFile.getFile();
		String fileName = file.getName();
		UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
		Record resultMap = new Record();
		CSVReader csvReader = null;  
	    try {  
	        csvReader = new CSVReader(new FileReader(file),',');//importFile为要导入的文本格式逗号分隔的csv文件，提供getXX/setXX方法    
	        if(fileName.endsWith(".csv")){  
	        	
	        	CheckOrder checkOrder = new CheckOrder();
	        	if("gateIn".equals(order_type)){
	        		if(fileName.indexOf("入库")>-1){
	        			resultMap = checkOrder.importGateInValue(csvReader, officeId);
	        		}else{
	        			throw new Exception("文件《"+fileName+"》中未检测到\"入库\"关键字<br/>请核查此文件是否为入库记录表");
	        		}
	        	}else if("gateOut".equals(order_type)){
	        		if(fileName.indexOf("出库")>-1){
	        			resultMap = checkOrder.importGateOutCheck(csvReader);
		        		if(resultMap.get("result")){
		        			resultMap = checkOrder.importGateOutValue(new CSVReader(new FileReader(file),','), officeId);
		        		}else{
		        			throw new Exception(resultMap.getStr("cause")+"<br/>请先导入入库信息表");
		        		}
	        		}else{
	        			throw new Exception("文件《"+fileName+"》中未检测到\"出库\"关键字<br/>请核查此文件是否为出库记录表");
	        		}
	        	}else if("invCheck".equals(order_type))
	        		if(fileName.indexOf("盘点")>-1){
	        			resultMap = checkOrder.importInvCheckValue(csvReader, officeId);
	        		}else{
	        			throw new Exception("文件《"+fileName+"》中未检测到\"盘点\"关键字<br/>请核查此文件是否为盘点单表");
	        		}
	        }else{
	        	throw new Exception("导入格式有误，请导入正确的csv格式");
	        }
	    } catch (Exception e) {  
	        e.printStackTrace(); 
	        resultMap.set("result", false);
	        resultMap.set("cause", e.getMessage());
	    } 
	    renderJson(resultMap);
	}  
	
	// 导入单据
	public void importOrder() {
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
			if (fileName.endsWith(".xls")) {
				title = ReaderXLS.getXlsTitle(file);
				content = ReaderXLS.getXlsContent(file);
			} else if (fileName.endsWith(".xlsx")) {
				title = ReaderXlSX.getXlsTitle(file);
				content = ReaderXlSX.getXlsContent(file);
			} else {
				resultMap.set("result", false);
				resultMap.set("cause", "导入失败，请选择正确的excel文件（xls/xlsx）");
			}
			
			//导入模板表头（标题）校验
			if (title != null && content.size() > 0) {
				CheckOrder checkOrder = new CheckOrder();
				if (checkOrder.checkoutExeclTitle(title, order_type)) {
					if("product".equals(order_type)){
						// 内容校验
						//resultMap = checkOrder.importProductCheck(content);
						
						// 内容开始导入
						//if(resultMap.getBoolean("result")){
							resultMap = checkOrder.importProductValue(content, userId, officeId);
						//}
					}
				} else {
					resultMap.set("result", false);
					resultMap.set("cause", "导入失败，excel标题列与模板excel标题列不一致");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.set("result", false);
			resultMap.set("cause","导入失败，请检测excel内容是否填写规范<br/>（建议使用Microsoft Office Excel软件操作数据）");
		}
		logger.debug("result:" + resultMap.get("result") + ",cause:"
				+ resultMap.get("cause"));

		renderJson(resultMap);
	}

}
