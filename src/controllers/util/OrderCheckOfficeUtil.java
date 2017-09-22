package controllers.util;


import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class OrderCheckOfficeUtil {
	public static boolean checkOfficeEqual(String tableName, Long orderId, Long userOfficeId) {
		Record rec = Db.findById(tableName, orderId);
		Long officeId = rec.getLong("office_id");
		Long toOfficeId = (long)897984654;
		if(rec.getStr("to_office_id")==null){
			toOfficeId = (long)897984654;
		}else{
			toOfficeId = Long.parseLong(rec.getStr("to_office_id"));
		}
		
		if(officeId == userOfficeId||userOfficeId == toOfficeId)
		    return true;
		return false;
	}
}
