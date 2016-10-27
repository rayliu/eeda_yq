package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.eeda.profile.OrderNoSeq;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

/**
 * generate Order number
 * 
 * All use one counter
 */
public class OrderNoGenerator {
	//如果服务器重启了，当前的序列号就从数据库找到最后的号码，然后接着计数
	//TODO：如果需要按每张单的前缀来生成序列号，可以多加一个Map来记录
	
	public synchronized static String getNextOrderNo(String orderPrefix, long officeId) {
	    String orderNo = "";
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String nowdate = sdf.format(new Date());
        
		//1. 从数据库获取orderPrefix的最后号码, 没有该orderPrefix则新增一行记录
        //2. 判断是否日期相同, 相同就累加, 不同就重新计数
	    Record orderSeq = Db.findFirst("select * from order_no_seq where prefix=? and office_id=?", orderPrefix, officeId);
        if(orderSeq!=null){
            String seq = "000";
            String lastOrderNo = orderSeq.get("last_order_no");
            //不管前缀长度，后面的数字长度是 13， 201101001
            String ym = StringUtils.right(lastOrderNo, 9).substring(0, 6); // 获取年月字符串
            if(ym.equals(nowdate)){//如果年月 =今天， 获取流水号
                seq = StringUtils.right(lastOrderNo, 3); // 获取流水号
            }
            orderNo = orderPrefix +nowdate+ getNo(seq);
            orderSeq.set("last_order_no", orderNo);
            Db.update("order_no_seq", orderSeq);
        }else{
            orderSeq = new Record();
            orderNo = orderPrefix +nowdate+ getNo("000");
            orderSeq.set("prefix", orderPrefix);
            orderSeq.set("last_order_no", orderNo);
            orderSeq.set("office_id", officeId);
            Db.save("order_no_seq", orderSeq);
        }
		
		return orderNo;
	}

	/**
	 * 返回当天的订单数+1
	 */
	private static String getNo(String s) {
		String rs = s;
		int i = Integer.parseInt(rs);
		i += 1;
		rs = "" + i;
		int seqLength = 3;//序列号长度001
		for (int j = rs.length(); j < seqLength; j++) {
			rs = "0" + rs;
		}
		return rs;
	}


}
