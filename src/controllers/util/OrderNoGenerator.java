package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * generate Order number
 * 
 * All use one counter
 */
public class OrderNoGenerator {
	//如果服务器重启了，当前的序列号就从数据库找到最后的号码，然后接着计数
	//TODO：如果需要按每张单的前缀来生成序列号，可以多加一个Map来记录

    //根据日期生成当月序列号
    public synchronized static String getNextOrderNo(String orderPrefix, String dateStr, long officeId) {
        String orderNo = "";
        orderNo = generateNo(orderPrefix, officeId, dateStr);
        return orderNo;
    }
    
	public synchronized static String getNextOrderNo(String orderPrefix, long officeId) {
	    String orderNo = "";
	    SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String nowdate = sdf.format(new Date());
        
		orderNo = generateNo(orderPrefix, officeId, nowdate);
		
		return orderNo;
	}
	
	public synchronized static String getOrderNo(String order_type, long officeId) {
	    String orderNo = null;
	    Record re = Db.findFirst("select * from code_produce where order_type = ? and office_id = ?",order_type,officeId);
	    
	    if(re != null){
	    	String date_type = re.getStr("date_type");
	    	String first_name = re.getStr("first_name");
	    	int serial_length = re.getInt("serial_length");
	    	int last_serial_no = re.getInt("last_serial_no")+1;
	    	
	    	SimpleDateFormat sdf = null;
	    	sdf = new SimpleDateFormat(date_type);
	        String middle_name = sdf.format(new Date());  //中间
	        
	        String serial = String.valueOf(last_serial_no);   //构造后的序号
	        int length = String.valueOf(last_serial_no).length();
        	if(length < serial_length){
        		int c = serial_length - length;
        		String sero = "";
        		for (int j = 0; j < c; j++) {
        			sero += "0";
				}
        		serial = sero+serial;
        		
        	}
        	re.set("last_serial_no", last_serial_no);
        	Db.update("code_produce",re);
	        orderNo = first_name + middle_name + serial;
	    }
		return orderNo;
	}
	
	
	
	
	//按照年流水号生成单号
    private static String generateNo(String orderPrefix, long officeId,
            String dateStr) {
        String orderNo;
        //1. 从数据库获取orderPrefix的最后号码, 没有该orderPrefix则新增一行记录
        //2. 判断是否日期相同, 相同就累加, 不同就重新计数
	    Record orderSeq = Db.findFirst("select * from order_no_seq where prefix=? and office_id=?", orderPrefix+dateStr, officeId);
        if(orderSeq!=null){
            String seq = "0000";
            String lastOrderNo = orderSeq.get("last_order_no");
            //不管前缀长度，后面的数字长度是 11， 1101001
            String ym = StringUtils.right(lastOrderNo, 6).substring(0, 2); // 获取年字符串
            if(ym.equals(dateStr)){//如果年 =今年， 获取流水号
                seq = StringUtils.right(lastOrderNo, 4); // 获取流水号
            }
            orderNo = orderPrefix +dateStr+ getNo(seq);
            orderSeq.set("last_order_no", orderNo);
            Db.update("order_no_seq", orderSeq);
        }else{
            orderSeq = new Record();
            orderNo = orderPrefix +dateStr+ getNo("0000");
            orderSeq.set("prefix", orderPrefix+dateStr);
            orderSeq.set("last_order_no", orderNo);
            orderSeq.set("office_id", officeId);
            Db.save("order_no_seq", orderSeq);
        }
        return orderNo;
    }

	/**
	 * 返回当前的订单数+1
	 */
	private static String getNo(String s) {
		String rs = s;
		int i = Integer.parseInt(rs);
		i += 1;
		rs = "" + i;
		int seqLength = 4;//序列号长度0001
		for (int j = rs.length(); j < seqLength; j++) {
			rs = "0" + rs;
		}
		return rs;
	}
	
	
	


}
