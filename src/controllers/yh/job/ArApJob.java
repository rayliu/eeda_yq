package controllers.yh.job;

import java.util.Calendar;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;

public class ArApJob implements Runnable {
	private Log logger = Log.getLog(ArApJob.class);

	@Override
	public void run() {
		Calendar cal = Calendar.getInstance();

		Db.update(
				"insert into arap_account_audit_summary"
						+ "(account_id, year, month, total_charge, total_cost, init_amount, balance_amount)"
						+ "select account_id, "
						+ "case month+1"
						+ "	when 13 then year+1"
						+ "    else year"
						+ "    end,  "
						+ "  case month+1"
						+ "	when 13 then 1"
						+ "    else month+1"
						+ "    end "
						+ ", 0, 0, balance_amount, balance_amount "
						+ "from arap_account_audit_summary where year=? and month=?",
						cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1);
	}

}
