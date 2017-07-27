package controllers.tms.arap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class outputScaleController extends Controller {

	private Logger logger = Logger.getLogger(outputScaleController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/tms/arap/transCostConfirm/outputScaleList.html");
	}
     
	public String list() {
		String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        String sql = "select * from( "
        		+" SELECT tjo.id tjoid,tjol.id ,tjo.office_id,tjo.delete_flag,tjo.order_no,tjo.lading_no,p.company_name customer_name,tjo.customer_id customer,"
        		+" IFNULL(CONVERT (substring(tjol.cabinet_date, 1, 10),CHAR),CONVERT (substring(tjol.closing_date, 1, 10),CHAR)) c_date" 
        		+" ,tjo.type,dock.dock_name take_wharf_name, "
        		+" dock1.dock_name back_wharf_name,dock2.dock_name loading_wharf1_name,dock3.dock_name " 
        		+" loading_wharf2_name,tjo.container_no,tjo.cabinet_type,tjol.unload_type,car.car_no,tjol.car_no car_id,tjo.remark, "
        		+" (CONVERT (substring(tjol.closing_date, 1, 10),CHAR)) create_stamp,tjol.driver,CONCAT(IFNULL(CONCAT(dock.dock_name,'-'),''),IFNULL(CONCAT(dock2.dock_name,'-'),''),IFNULL(CONCAT(dock3.dock_name,'-'),''),IFNULL(" 
        		+ "dock1.dock_name,'')) combine_wharf, "
        		+" (SELECT GROUP_CONCAT(car.car_no SEPARATOR '/') from trans_job_order tjo1  "
        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
        		+" WHERE tjo.id = tjo1.id) combine_car_no, "
        		+" (SELECT GROUP_CONCAT(tjol.unload_type SEPARATOR '/') from trans_job_order tjo1 "
        		+" LEFT JOIN trans_job_order_land_item tjol on tjol.order_id = tjo1.id "
        		+" WHERE tjo.id = tjo1.id) combine_unload_type ,"
        		+ " (SELECT SUM(tjoa.currency_total_amount) FROM trans_job_order_arap tjoa WHERE tjoa.order_id = tjo.id AND tjoa.order_type = 'CHARGE' AND tjoa.charge_id = ( SELECT id FROM "
        		+ " fin_item f WHERE f. NAME = '运费' AND f.office_id = 4) ) freight,tjol.export_flag,"
        		+ " cast(substring(tjo.charge_time, 1, 10) AS CHAR) charge_time"
        		+" from trans_job_order_land_item tjol  "
        		+" LEFT JOIN trans_job_order tjo on tjol.order_id = tjo.id "
        		+" LEFT JOIN trans_job_order_arap tjoa ON tjol.order_id = tjoa.order_id"
        		+" LEFT JOIN dockinfo dock on dock.id = tjo.take_wharf "
        		+" LEFT JOIN dockinfo dock1 on dock1.id = tjo.back_wharf "
        		+" LEFT JOIN dockinfo dock2 on dock2.id = tjol.loading_wharf1 "
        		+" LEFT JOIN dockinfo dock3 on dock3.id = tjol.loading_wharf2 "
        		+" LEFT JOIN carinfo car on car.id = tjol.car_no  "
        		+" LEFT JOIN party p ON p.id = tjo.customer_id "
        		+ " WHERE tjo.office_id = "+office_id
					+ " ) A where 1=1 and delete_flag ='N' ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by charge_time " +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
        return sql;
		
	}
	
	
	public void downloadList(){
		String car_no = getPara("car_no");
		String driver = getPara("driver");
		String sql = list();
		String sql_car_no ="";
		String sql_driver ="";
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			Record re = Db.findFirst("select * from trans_job_order_land_item tjol where id = ?",idAttr[i]);
			long arapId = re.getLong("id");
			Db.update("UPDATE trans_job_order_land_item SET export_flag = 'Y' WHERE id=?",arapId);
		}
		if(StringUtils.isNotBlank(car_no)){
			 sql_car_no = " and car_no='"+car_no+"'";
		}
		if(StringUtils.isNotBlank(driver)){
			 sql_driver = " and driver='"+driver+"'";
		}
		if(StringUtils.isBlank(car_no)){
			car_no=driver;
		}
		String sqlExport = sql+sql_car_no+sql_driver;
		String[] headers = new String[]{"提单号", "提/收柜日期", "客户", "类型", "拖柜地址", "柜号", "柜型", "提柜类型", "结算车牌", "产值","运费",
				"备注"};
		String[] fields = new String[]{"LADING_NO", "C_DATE", "CUSTOMER_NAME", "TYPE", "COMBINE_WHARF", "CONTAINER_NO", "CABINET_TYPE", "COMBINE_UNLOAD_TYPE", "COMBINE_CAR_NO", "",
						"FREIGHT","REMARK"};
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,car_no);
		renderText(fileName);
	}
	
	
	public void costConfirm(){
		String ids = getPara("itemIds");
		String idAttr[] = ids.split(",");
		for(int i=0 ; i<idAttr.length ; i++){
			Record re = Db.findFirst("select * from job_order_arap joa where id = ?",idAttr[i]);
			long arapId = re.getLong("id");
			Db.update("UPDATE trans_job_order_arap SET audit_flag = 'Y' WHERE id=?",arapId);
		}
		renderJson("{\"result\":true}");
	}
   
}
