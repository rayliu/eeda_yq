package models.yh.arap;

import com.jfinal.plugin.activerecord.Model;

public class ReimbursementOrder extends Model<ReimbursementOrder> {
	public static final String ORDER_STATUS_NEW = "new";
	public static final String ORDER_STATUS_AUDIT = "audit";
	public static final ReimbursementOrder dao = new ReimbursementOrder();
}
