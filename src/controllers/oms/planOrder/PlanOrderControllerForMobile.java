package controllers.oms.planOrder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.oms.PlanOrder;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.app.AppControllerForMobile;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;


public class PlanOrderControllerForMobile extends Controller {

	private Logger logger = Logger.getLogger(PlanOrderControllerForMobile.class);
	Subject currentUser = SecurityUtils.getSubject();

	
    private List<Record> getPlanOrderItems(String orderId) {
        String itemSql = "select pi.*, l_por.name por_name, l_pol.name pol_name, l_pod.name pod_name,u.name unit_name,"
                + " p.abbr carrier_name "
                + " from plan_order_item pi "
                +" left join location l_por on pi.por=l_por.id"
                +" left join location l_pol on pi.pol=l_pol.id"
                +" left join location l_pod on pi.pod=l_pod.id"
                +" left join party p on pi.carrier=p.id"
                +" left join unit u on u.id=pi.unit_id"
                +" where order_id=?";

		List<Record> itemList = Db.find(itemSql, orderId);
		return itemList;
	}
    
   
    public void list() throws IOException {
        if (!AppControllerForMobile.checkHeaderAuth(getRequest())) {
            getResponse().setStatus(401);
            getResponse().setHeader("Cache-Control", "no-store");
            getResponse().setDateHeader("Expires", 0);
            getResponse().setHeader("WWW-authenticate", "Basic Realm=\"test\"");
            renderText("用户未登录!");
            return;
        }
//        UserLogin user = LoginUserController.getLoginUser(this);
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
        String condition="";
        if("todo".equals(type)){
        	sql =" SELECT po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name "
        			+ " FROM plan_order po "
        			+ " LEFT JOIN plan_order_item poi ON po.id = poi.order_id "
        			+ " left join party p on p.id = po.customer_id "
        			+ " left join user_login u on u.id = po.creator "
        			+ " WHERE po.office_id="+office_id+" and is_gen_job='N' AND factory_loading_time is not NULL "
        			+ " AND datediff(factory_loading_time, now())<=5"
        			+ " and po.delete_flag = 'N'";
        }else if ("customwaitPlan".equals(type)){
        	sql =" SELECT po.*, ifnull(u.c_name, u.user_name) creator_name,p.abbr customer_name,p. CODE"
        			+ " FROM"
        			+ "	plan_order po"
        			+ " LEFT JOIN plan_order_item poi ON poi.order_id = po.id"
        			+ " LEFT JOIN user_login u ON u.id = po.creator"
        			+ " LEFT JOIN party p ON p.id = po.customer_id"
        			+ " WHERE"
        			+ " po.office_id="+office_id+" and poi.customs_type = '自理报关'"
        			+ " AND poi.is_gen_job = 'N'"
        			+ " and po.delete_flag = 'N'"
        			+ " GROUP BY poi.id ";
        }else{
        	sql = "SELECT * from (select po.*, ifnull(u.c_name, u.user_name) creator_name ,p.abbr customer_name,p.code customer_code"
    			+ " from plan_order po "
    			+ " left join party p on p.id = po.customer_id "
    			+ " left join user_login u on u.id = po.creator"
    			+ " where po.office_id="+office_id
    			+ " and po.delete_flag = 'N'"
    			+ " ) A where 1=1 ";
        }
        condition = DbUtils.buildConditions(getParaMap());
        
        
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by create_stamp desc " +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	List<Record> list = null;
    	list = getPlanOrderItems(order_id);

    	Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", 1);
        BillingOrderListMap.put("iTotalRecords", list.size());
        BillingOrderListMap.put("iTotalDisplayRecords", list.size());

        BillingOrderListMap.put("aaData", list);

        renderJson(BillingOrderListMap); 
    }
   
    
    //确认已完成计划单
    public void confirmCompleted(){
    	String id = getPara("id");
    	PlanOrder order = PlanOrder.dao.findById(id);
    	order.set("status", "已完成");
    	renderJson("{\"result\":true}");
    }
    
    //删除单据，设置为已删
    @Before(Tx.class)
    public void deleteOrder(){
    	String id = getPara("id");
    	String delete_reason = getPara("delete_reason");
    	Long deletor = LoginUserController.getLoginUserId(this);
    	Date date = new Date();
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String delete_stamp = sf.format(date);
    	Db.update("update plan_order set delete_flag='Y', deletor='"+deletor+"', delete_stamp='"+delete_stamp+"',"
    			+ " delete_reason='"+delete_reason+"' where id = ?  ",id);
    	renderJson("{\"result\":true}");
    }
    

}
