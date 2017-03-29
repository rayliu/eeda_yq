package controllers.msg.job;

import com.jfinal.aop.Before;
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
	    ea.importOrders();
	    
	    isRunning= false;
	}

	
}
