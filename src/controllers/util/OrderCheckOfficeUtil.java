package controllers.util;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class OrderCheckOfficeUtil {
	public static boolean checkOfficeEqual(String tableName, Long orderId, Long userOfficeId) {
		Record rec = Db.findById(tableName, orderId);
		Long officeId = rec.getLong("office_id");
		if(officeId == userOfficeId)
		    return true;
		return false;
	}
}
