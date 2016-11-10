package controllers.tms.jobOrder;

import java.util.HashMap;
import java.util.Map;

import models.eeda.oms.jobOrder.JobOrderShipmentHead;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.DbUtils;
import controllers.util.PrintPatterns;

public class TransJobOrderReportController extends Controller {

	private Logger logger = Logger.getLogger(TransJobOrderReportController.class);
	private static String contextPath = null;

	private String getContextPath() {
		if(contextPath == null){
			contextPath = getRequest( ).getServletContext().getRealPath("/");
		}
		return contextPath;
	}
	
	//海运电放保涵word
	public void printOceanWord(){
		
		String order_id = getPara("order_id");
		String fileName = "/report/jobOrder/guaranteeLetter.jasper";
		String outFileName = "/download/生成电放保函word";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().printDoc(fileName,outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}

	
	public void printOceanHBL() {
		
		String order_id = getPara("order_id");
		String fileName = "/report/jobOrder/oceanHBL.jasper";
		String outFileName = "/download/工作单海运HBL";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
        fileName = getContextPath() + fileName;
        outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printOceanBooking() {
		
		String order_no = getPara("order_no");
		String fileName = "/report/jobOrder/oceanBooking.jasper";
		String outFileName = "/download/工作单海运booking";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printOceanSI() {
		
		String order_id = getPara("order_id");
		String fileName = "/report/jobOrder/oceanSI.jasper";
		String outFileName = "/download/工作单海运MBLSI";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	public void printOceanHBLSI() {
		
		String order_id = getPara("order_id");
		String fileName = "/report/jobOrder/oceanHBLSI.jasper";
		String outFileName = "/download/工作单海运HBLSI";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	@Before(Tx.class)
	public void printOceanHead() {
		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
   		String id = (String) dto.get("id");
   		String order_id = (String) dto.get("order_id");
   		
   		JobOrderShipmentHead jsh = new JobOrderShipmentHead();
   		
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			jsh = JobOrderShipmentHead.dao.findById(id);
   			DbUtils.setModelValues(dto, jsh);
   			jsh.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, jsh);
   			jsh.save();
   		}
    	
    	String fileName = "/report/jobOrder/oceanHead.jasper";
		String outFileName = "/download/工作单海运头程资料";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		
		Record rec =new Record();
		rec.set("oceanHeadId", jsh.get("id"));
		rec.set("down_url", file.substring(file.indexOf("download")-1));
		renderJson(rec);
	}
	
	@Before(Tx.class)
	public void printCabinetTruck() {
		String jsonStr=getPara("params");
		
		Gson gson = new Gson();  
		Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
		
		String id = (String) dto.get("id");
		String order_id = (String) dto.get("order_id");
		
		
		Record r =new Record();
		if (StringUtils.isNotEmpty(id)) {
			//update
		    r = Db.findById("job_order_land_cabinet_truck", id);
			DbUtils.setModelValues(dto, r, "job_order_land_cabinet_truck");
			Db.update("job_order_land_cabinet_truck", r);
		} else {
			//create 
			DbUtils.setModelValues(dto, r, "job_order_land_cabinet_truck");
			Db.save("job_order_land_cabinet_truck", r);
			id = r.getLong("id").toString();
		}
		
		String fileName = "/report/jobOrder/cabinetTruckOrder.jasper";
		String outFileName = "/download/工作单陆运柜货派车单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		
		r.set("truckHeadId", id);
		r.set("down_url", file.substring(file.indexOf("download")-1));
		renderJson(r);
	}
	
	
	//空运booking
	public void printAirBooking() {
		
		String order_no = getPara("order_no");
		String fileName = "/report/jobOrder/airBooking.jasper";
		String outFileName = "/download/工作单空运booking";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_no", order_no);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_no;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	//路运派车单打印
	public void printTruckOrderPDF() {
		
		String itemId = getPara("itemId");
		String fileName = "/report/jobOrder/truckOrder.jasper";
		String outFileName = "/download/陆运派车单";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("itemId", itemId);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + itemId;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	//打印debitNote中文
	public void printDebitNotePDF() {
		String debit_note = getPara("debit_note");
		String order_id = getPara("itemIds");
		String [] orderIdArr= order_id.split(",");
		String fileName;
		String outFileName;
		if("debitNote".equals(debit_note)){
			fileName = "/report/jobOrder/debitNote.jasper";
			outFileName = "/download/debitNote中文";
		}else if ("debit_note_eng".equals(debit_note)){
			fileName = "/report/jobOrder/debitNote_eng.jasper";
			outFileName = "/download/debitNote英文";
		}else {
			fileName = "/report/jobOrder/INVOICE.jasper";
			outFileName = "/download/Invoice英文";
		}
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", orderIdArr);		
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	//打印应收对账单
	public void printReceiveDetailPDF(){
		String order_id = getPara("order_id");
		String fileName = "/report/checkOrder/ReceivableDetails.jasper";
		String outFileName = "/download/应收对账单PDF";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	//打印应付对账单PDF
	public void payableDetailPDF(){
		String order_id = getPara("order_id");
		String fileName = "/report/checkOrder/payableDetails.jasper";
		String outFileName = "/download/应付对账单PDF";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
    //打印托运申报单printConsignmentBill
	public void printConsignmentBill(){
		String order_id = getPara("id");
		String fileName = "/report/jobOrder/consignmentBill.jasper";
		String outFileName = "/download/托运申报单 PDF";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("order_id", order_id);
		fileName = getContextPath() + fileName;
		outFileName = getContextPath() + outFileName + order_id;
		String file = PrintPatterns.getInstance().print(fileName, outFileName,hm);
		renderText(file.substring(file.indexOf("download")-1));
	}
	
	
	
}
