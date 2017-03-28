package controllers.msg.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import models.eeda.oms.LogisticsOrder;
import models.eeda.oms.SalesOrder;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.oms.ebaySalesOrder.EbaySalesOrderController;
import controllers.profile.EbayAccountController;
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
