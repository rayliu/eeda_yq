package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.yh.profile.CustomizeField;

import org.apache.commons.lang.StringUtils;

/**
 * generate Order number
 * 
 * All use one counter
 */
public class OrderNoGenerator {
	
	
	private static String count = "000";
	private static String dateValue = "20110101";
	//如果服务器重启了，当前的序列号就从数据库找到最后的号码，然后接着计数
	//如果需要按每张单的前缀来生成序列号，可以多加一个Map来记录
	//远桥生成单号规则，年月001，201101001
	
	public synchronized static String getNextOrderNo(String orderPrefix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		String nowdate = sdf.format(new Date());
		if("20110101".equals(dateValue)){
			initCountFromDB(nowdate);
		}else{
			//每月从新计数
			if(!nowdate.equals(dateValue)){
			    dateValue=nowdate;
			    count = "000";
			}
		}
		String orderNo = orderPrefix +nowdate+ getNo(count,3);
		
		CustomizeField cf = CustomizeField.dao.findFirst("select * from customize_field where order_type='latestOrderNo'");
		if(cf!=null){
			cf.set("field_code", orderNo).update();
		}
		return orderNo;
	}
	
	public synchronized static void initCountFromDB(String nowdate) {
		CustomizeField cf = CustomizeField.dao.findFirst("select * from customize_field where order_type='latestOrderNo'");
		if(cf!=null){
			String previousNo = cf.get("field_code");
			//不管前缀长度，后面的数字长度是 13， 2011010100001
			String ymd = StringUtils.right(previousNo, 9).substring(0, 6); //获取年月
			dateValue=nowdate;
			if(ymd.equals(nowdate)){//如果年月日 =今天， 获取流水号
				count = StringUtils.right(previousNo, 3); // 获取流水号
			}
		}
        
	}

	/**
	 * 返回当天的订单数+1
	 */
	private static String getNo(String s,int seqLength) {
		String rs = s;
		int i = Integer.parseInt(rs);
		i += 1;
		rs = "" + i;
		//seqLength是序列号长度
		for (int j = rs.length(); j < seqLength; j++) {
			rs = "0" + rs;
		}
		count = rs;
		return rs;
	}

	public static void test() {
	    //模拟多线程访问
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(){
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " => " + getNextOrderNo("YS"));
                }
            };
            t.start();
        }
    }

}
