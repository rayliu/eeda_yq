package controllers.oms.jobOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.DbUtils;

//@RequiresAuthentication
//@Before(SetAttrLoginUserInterceptor.class)
public class JobOrderControllerForMobile extends Controller {
	
	private Logger logger = Logger.getLogger(JobOrderControllerForMobile.class);
	
	public void list() {       
        long office_id=1;
        
        String type=getPara("type");
        
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }else{
            sLimit = " LIMIT 50 ";
        }
        
        String sql = "";
        if("sowait".equals(type)){
            sql=" SELECT jor.*,ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order jor "
                    + " LEFT JOIN job_order_shipment jos on jor.id = jos.order_id "
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator "
                    + " WHERE jor.office_id="+office_id
                    + " and jor.type = '出口柜货' AND jos.SONO IS NULL AND jor.transport_type LIKE '%ocean%'"
                    + " and jor.delete_flag = 'N'";         
        }else if("truckorderwait".equals(type)){
             sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order_land_item joli"
                    + " left join job_order jor on jor.id = joli.order_id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " WHERE jor.office_id="+office_id
                    + " and datediff(joli.eta, now()) <= 3 AND (joli.truckorder_flag != 'Y' OR joli.truckorder_flag IS NULL)"
                    + " AND jor.transport_type LIKE '%land%'"
                    + " and jor.delete_flag = 'N'";
            
            
        } else if("siwait".equals(type)){
             sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order_shipment jos"
                    + " left join job_order jor on jos.order_id = jor.id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator "
                    + " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(jos.export_date)=TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
            
        } else if("mblwait".equals(type)){
            sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order_shipment jos "
                    + " left join job_order jor on jos.order_id = jor.id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " WHERE jor.office_id="+office_id
                    + " and  jos.si_flag = 'Y' and (jos.mbl_flag != 'Y' or jos.mbl_flag is null)"
                    + " and jor.delete_flag = 'N'";
            
        } else if("customwait".equals(type)){
            sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " from job_order jor "
                    + " LEFT JOIN job_order_custom joc on joc.order_id = jor.id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " left join job_order_custom_china_self_item jocc on jocc.order_id = jor.id"
                    + " where jor.office_id="+office_id
                    + " and  jor.transport_type LIKE '%custom%'"
                    + " and isnull(joc.customs_broker) and isnull(jocc.custom_bank)"
                    + " and jor.delete_flag = 'N'"
                    + " GROUP BY jor.id";
            
        } else if("insurancewait".equals(type)){
            sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order jor LEFT JOIN job_order_insurance joi ON jor.id = joi.order_id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " WHERE jor.office_id="+office_id
                    + " and  jor.transport_type LIKE '%insurance%' and joi.insure_no is NULL"
                    + " and jor.delete_flag = 'N'";
        } else if("overseacustomwait".equals(type)){
            sql = "SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order_shipment jos "
                    + " LEFT JOIN job_order jor on jos.order_id = jor.id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " WHERE jor.office_id="+office_id
                    + " and (jos.afr_ams_flag !='Y' OR jos.afr_ams_flag is  NULL) and jos.wait_overseaCustom = 'Y' "
                    + " and timediff(now(),jos.etd)<TIME('48:00:00') "
                    + " and jor.delete_flag = 'N'";
        } else if("tlxOrderwait".equals(type)){
            sql = " SELECT jor.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name"
                    + " FROM job_order_shipment jos"
                    + " LEFT JOIN job_order jor on jos.order_id = jor.id"
                    + " left join party p on p.id = jor.customer_id"
                    + " left join user_login u on u.id = jor.creator"
                    + " WHERE jor.office_id="+office_id
                    + " and TO_DAYS(jos.etd)= TO_DAYS(now())"
                    + " and jor.delete_flag = 'N'";
        }
        else{
                 sql = "SELECT * from (select jo.*,"
                        + " ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p.company_name,p.code customer_code"
                        + " from job_order jo"
                        + " left join party p on p.id = jo.customer_id"
                        + " left join user_login u on u.id = jo.creator"
                        + " where jo.office_id="+office_id
                        + " and jo.delete_flag = 'N'"
                        + " ) A where 1 = 1 ";
         }
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }


}
