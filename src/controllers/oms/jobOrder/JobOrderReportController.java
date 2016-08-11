package controllers.oms.jobOrder;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;

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
	
	
		
}
