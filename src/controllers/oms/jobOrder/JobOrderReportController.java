package controllers.oms.jobOrder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderShipmentHead;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PrintPatterns;

public class JobOrderReportController extends Controller {

	private Logger logger = Logger.getLogger(JobOrderReportController.class);
	private static String contextPath = null;

	private String getContextPath() {
		if(contextPath == null){
			contextPath = getRequest( ).getServletContext().getRealPath("/");
		}
		return contextPath;
	}

	
	public void printOceanHBL() {
		
		String order_no = getPara("order_no");
		String jasper_name = getPara("order_no");
		String fileName = "/report/jobOrder/oceanHBL.jasper";
		String outFileName = "/download/工作单海运HBLPDF";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	public void printOceanBooking() {
		
		String order_no = getPara("order_no");
		String fileName = "/report/jobOrder/oceanBooking.jasper";
		String outFileName = "/download/工作单海运bookingPDF";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printOceanHead() {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        JobOrderShipmentHead jobOrder = new JobOrderShipmentHead();
   		String id = (String) dto.get("id");
   		Map<String,Object> resultMap = new HashMap<String,Object>();
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			jobOrder = JobOrderShipmentHead.dao.findById(id);
   			DbUtils.setModelValues(dto, jobOrder);
   			jobOrder.update();
   			resultMap.put("oceanHeadId", id);
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, jobOrder);
   			jobOrder.save();
   			resultMap.put("oceanHeadId", jobOrder.getLong("id"));
   		}
    	renderJson(resultMap);
	}
	
		
}
