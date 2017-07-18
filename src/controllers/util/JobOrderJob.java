package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
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
	}

}
