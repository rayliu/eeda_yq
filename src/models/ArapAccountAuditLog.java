package models;import com.jfinal.plugin.activerecord.Model;@SuppressWarnings("serial")public class ArapAccountAuditLog extends Model<ArapAccountAuditLog> {    public static final String TYPE_CHARGE="CHARGE";    public static final String TYPE_COST="COST";    public static final String TYPE_CUSTOMCHARGE="CUSTOMCHARGE";    public static final String TYPE_CUSTOMCOST="CUSTOMCOST";    	public static final ArapAccountAuditLog dao = new ArapAccountAuditLog();}