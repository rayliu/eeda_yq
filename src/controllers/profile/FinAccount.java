package controllers.profile;

import models.eeda.oms.jobOrder.JobOrderShipmentItem;

import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class FinAccount extends Model<JobOrderShipmentItem> {
	public static final FinAccount dao = new FinAccount();
}