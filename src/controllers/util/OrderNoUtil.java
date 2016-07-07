package controllers.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
/**
 *
 * @deprecated use OrderNoGenerator.getNextOrderNo(String orderPrefix) instead.  
 */
@Deprecated
public class OrderNoUtil {
	
	/**
	 *
	 * @deprecated use OrderNoGenerator.getNextOrderNo(String orderPrefix) instead.  
	 */
	@Deprecated
	public synchronized static String getOrderNo(String sql,String head){
		String orderNo = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String format = sdf.format(new Date().getDate());
		Record order = Db.findFirst(sql);
		if (order != null) {
			String num = (String) (order.get("order_no") != null ? order.get("order_no") : order.get("depart_no"));
			String str = num.substring(num.length() - 13);
			String time = format + "00001";
			Long oldTime = Long.parseLong(str);
			Long newTime = Long.parseLong(time);
			orderNo = (oldTime >= newTime) ? String.valueOf((oldTime + 1)) : String.valueOf(newTime);
		} else {
			orderNo = format + "00001";
		}
		return head+orderNo;
	}
}
