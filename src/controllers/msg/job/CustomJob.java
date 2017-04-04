package controllers.msg.job;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.oms.ebaySalesOrder.EbaySalesOrderController;
public class CustomJob implements Runnable {

	public static boolean isRunning=false;
	
	@Override
	@Before(Tx.class)
	public void run() {

	    if(isRunning){
	        return;
	    }
	    isRunning = true;
	    System.out.println("begin loading...");
	    EbaySalesOrderController ea = new EbaySalesOrderController();
	    
	    List<Record> settingList = Db.find("select * from ebay_seller_account where is_stop='Y'");
	    for (Record record : settingList) {
	        ea.importOrders(record.getLong("office_id"));
	    }
	    
	    isRunning= false;
	}

	
}
