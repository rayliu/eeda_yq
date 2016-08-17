package models.yh.arap;

import com.jfinal.plugin.activerecord.Model;

public class TransferAccountsOrder extends Model<TransferAccountsOrder> {
	public static final String ORDER_STATUS_NEW = "new";
	public static final String ORDER_STATUS_CONFIRM = "confirm";
	public static final TransferAccountsOrder dao = new TransferAccountsOrder();
}
