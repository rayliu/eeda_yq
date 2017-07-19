package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sun.org.mozilla.javascript.internal.regexp.SubString;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderShipment;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class JobOrderJob implements Runnable {
	private Log logger = Log.getLog(JobOrderJob.class);

	@Override
	public void run() {
		String sql = "SELECT id,etd,atd,eta,ata FROM job_order_shipment where ((atd is  null or atd ='') and (etd is not null or etd!='')) or ((ata is  null or ata ='') and (eta is not null or eta!='')) ;";

		JobOrderShipment jobOrderShipment = new JobOrderShipment();
		List<JobOrderShipment> jobOrderShipmentList = jobOrderShipment.dao.find(sql);
		
		for(int i = 0;i<jobOrderShipmentList.size();i++){
			Date etd = jobOrderShipmentList.get(i).getDate("etd");
			Date atd = jobOrderShipmentList.get(i).getDate("atd");
			Date eta = jobOrderShipmentList.get(i).getDate("eta");
			Date ata = jobOrderShipmentList.get(i).getDate("ata");
			
			Date time = new Date();
			if(atd==null||atd.equals("")){
				if(etd!=null&&!"".equals(etd)){
					if(time.getTime()>etd.getTime()){
						atd=etd;
						Long id = jobOrderShipmentList.get(i).getLong("id");
						jobOrderShipment.findById(id).set("atd", atd).update();					
					}
				}
			}
			
			if(ata==null||ata.equals("")){
				if(eta!=null&&!"".equals(eta)){
					if(time.getTime()>eta.getTime()){
						ata=eta;
						Long id = jobOrderShipmentList.get(i).getLong("id");
						jobOrderShipment.findById(id).set("ata", ata).update();					
					}
				}
			}
		}
		System.out.println("-------------------job----------------------");
		
		Calendar c = Calendar.getInstance();
		//int current_day=c.get(Calendar.DATE);//获取当前日
		int current_month = c.get(Calendar.MONTH )+1;//获取当前月份
		int month_int = 0;
		int current_day = 20;
		if(current_day==20){
			JobOrder jobOrder = new JobOrder();
			List<JobOrder> jobOrederList = jobOrder.find("select id,status,order_export_date from job_order where office_id=1 and status!='已完成' and Date(order_export_date)>='2017-01-01'; ");
			for(int i = 0; i<jobOrederList.size(); i++){
				month_int = Integer.parseInt((jobOrederList.get(i).get("order_export_date").toString()).substring(6,7));
				if(current_month-month_int==1){
					jobOrederList.get(i).set("status","已完成");
					jobOrederList.get(i).update();
				}
			}
				
		}
	}

}
