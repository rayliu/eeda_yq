package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.DepartOrder;
import models.InsuranceOrder;
import models.Party;
import models.yh.arap.ArapMiscCostOrder;
import models.yh.delivery.DeliveryOrder;
import models.yh.profile.Contact;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostItemConfirmController extends Controller {
    private Log logger = Log.getLog(CostItemConfirmController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void index() {
    	    render("/eeda/arap/CostItemConfirm/CostItemConfirmList.html");
    }


    public void confirm() {
        String ids = getPara("ids");
        String[] idArray = ids.split(",");
        logger.debug(String.valueOf(idArray.length));

        String customerId = getPara("customerId");
        Party party = Party.dao.findById(customerId);

        Contact contact = Contact.dao.findById(party.get("contact_id").toString());
        setAttr("customer", contact);
    	setAttr("type", "CUSTOMER");
    	setAttr("classify", "receivable");
        render("/eeda/arap/CostAcceptOrder/CostCheckOrderEdit.html");
    }

    // 应付条目列表
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void list() {
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sortColIndex = getPara("iSortCol_0");
		String sortBy = getPara("sSortDir_0");
		String colName = getPara("mDataProp_"+sortColIndex);
        
        String orderNo = getPara("orderNo");
        String sp = getPara("sp");
        String no = getPara("no");
        String beginTime = getPara("beginTime");
        String plantime = getPara("plantime");
        String arrivaltime = getPara("arrivaltime");
        String endTime = getPara("endTime");
        String status = getPara("status");
        String type = getPara("type");
        String booking_note_number = getPara("booking_note_number");
        String route_from =getPara("route_from");
        String route_to=getPara("route_to");
        String customer_name = getPara("customer_name");
        String serial_no=getPara("serial_no");
        String sign_no = getPara("sign_no");
       
        String user_name = currentUser.getPrincipal().toString();
        
        String sqlTotal = "";
        String sql = "select cast(planning_time as CHAR) planning_time1, A.* from (select distinct dor.id,"
        		+ " dor.order_no order_no,"
        		+ " dor.status,"
        		+ " ifnull((SELECT NAME FROM location WHERE CODE = dor.route_from),"
        		+ " (SELECT NAME FROM location WHERE CODE = dor.route_from )) route_from,"
        		+ " ifnull((SELECT NAME FROM location WHERE CODE = dor.route_to),"
        		+ " (SELECT NAME FROM location WHERE CODE = dor.route_to)) route_to,"
        		+ " IFNULL(c3.address,IFNULL(toid.notify_party_company,'')) receivingunit, c.abbr spname,"
        		+ " (SELECT sum(doi1.amount) FROM delivery_order_item doi1 WHERE doi1.delivery_id = dor.id ) amount,"
        		+ " round(ifnull(prod.volume,toi.volume)*toi.amount,2) volume,"
        		+ " round(ifnull(prod.weight,toi.weight)*toi.amount,2) weight,"
        		+ " DATE(dor.create_stamp) create_stamp,"
        		+ " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
        		+ " dor.ref_no ref_no,"
        		+ " '配送' business_type, "
        		+ " null as booking_note_number,"
        		+ " GROUP_CONCAT((SELECT toid.serial_no from transfer_order_item_detail toid WHERE  doi.transfer_item_detail_id=toid.id) SEPARATOR '<br/>') serial_no,"
        		+ " (select ifnull(sum(dofi.amount),0) from delivery_order_fin_item dofi where dofi.order_id = dor.id and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount,"
        		+ " (SELECT ifnull(sum(amount),0) FROM delivery_order_fin_item dofi WHERE dofi.order_id = dor.id) change_amount,"
        		+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = doi.transfer_order_id group by tor.id) separator '\r\n') transfer_order_no,"
        		+ " dor.sign_status return_order_collection,"
        		+ " dor.remark,"
        		+ " DATE(dor.depart_stamp) as depart_time,"
        		+ " group_concat(distinct(SELECT CAST(tor.planning_time AS CHAR) FROM transfer_order tor WHERE tor.id = doi.transfer_order_id GROUP BY tor.id) SEPARATOR '\r\n') planning_time,"
        		+ " oe.office_name office_name,"
        		+ " c1.abbr cname,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='运输费') as transport_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='搬运费') as carry_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='上楼费') as climb_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='保险费') as insurance_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='提货费') as take_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='安装费') as anzhuang_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='仓储费') as cangchu_cost,"
//        		+ " (select sum(dofi.amount) from delivery_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.order_id = dor.id and fi.name='其他费用') as other_cost"
				+ " 0 AS transport_cost,"
				+ " 0 AS carry_cost,"
				+ " 0 AS climb_cost,"
				+ " 0 AS insurance_cost,"
				+ " 0 AS take_cost,"
				+ " 0 AS anzhuang_cost,"
				+ " 0 AS cangchu_cost,"
				+ " 0 AS other_cost"
				+ " from delivery_order dor"
				+ " left join return_order ror on ror.delivery_order_id = dor.id"
				+ " left join party_old p on dor.sp_id = p.id "
				+ " left join contact c on p.contact_id = c.id "
				+ " left join delivery_order_item doi on doi.delivery_id = dor.id "
				+ " left join delivery_order_fin_item dofi on dor.id = dofi.order_id "
				+ " left join transfer_order_item toi on doi.transfer_item_id = toi.id "
				+ " LEFT JOIN transfer_order_item_detail toid ON toid.id = doi.transfer_item_detail_id"
				+ " left join product prod on toi.product_id = prod.id "
				+ " left join transfer_order tor ON tor.id = doi.transfer_order_id"
				+ " left join party_old p1 on tor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
				+ " LEFT JOIN party_old p3 ON dor.notify_party_id = p3.id"
				+ " LEFT JOIN contact c3 ON p3.contact_id = c3.id"
				+ " left join user_login ul on dor.create_by = ul.id "
				+ " left join warehouse w on dor.from_warehouse_id = w.id "
				+ " left join office oe on w.office_id = oe.id "
				+ " where ror.transaction_status != '新建' and unix_timestamp(dor.appointment_stamp) > unix_timestamp('2015-06-01 10:34:36') and  dor.audit_status='新建'and (dor.status !='新建' or dor.status !='计划中' or dor.status != '初始化') and p.party_type='SERVICE_PROVIDER' "
				+ " and dor.customer_id in(select customer_id from user_customer where user_name='"+user_name+"')"
		        + " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"') "
		        + " or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dor.id "
				+ " union"
				+ " select distinct dpr.id,"
				+ " dpr.depart_no order_no,"
				+ " dpr.status,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),"
        		+ " (SELECT NAME FROM location WHERE CODE = dpr.route_from )) route_from,"
        		+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),"
        		+ " (SELECT NAME FROM location WHERE CODE = dpr.route_to)) route_to,"
				+ " (CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit, c.abbr spname,"
				+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM 	transfer_order_item_detail toid WHERE toid.depart_id = dpr.id ) WHEN tor.cargo_nature = 'cargo'  "
				+ " THEN ( SELECT sum(toi.amount) FROM "
				+ "	depart_order dpr2 LEFT JOIN depart_transfer dt on dt.depart_id = dpr2.id LEFT JOIN transfer_order_item toi on toi.order_id = dt.order_id "
				+ "	where dpr2.id = dpr.id ) END ) amount,"
				//+ " (select count(*) from transfer_order_item_detail where depart_id =dpr.id ) as amount,"
				+ " round((select count(id) * volume as volume from transfer_order_item_detail where depart_id =dpr.id),2) volume,"
				+ " round((select count(id) * weight as weight from transfer_order_item_detail where depart_id =dpr.id),2) weight,"
				+ " DATE(dpr.create_stamp) create_stamp,"
				+ " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
				+ " null as ref_no,"
				+ " dpr.transfer_type business_type, "
				+ " dpr.booking_note_number as booking_note_number,"
				+ " null as customer_delivery_no,"
				+ " (select ifnull(sum(dofi.amount),0) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.depart_order_id = dpr.id  and fi.type = '应付' and IFNULL(dofi.cost_source,'') != '对账调整金额') pay_amount, "
				+ " (SELECT sum(amount) FROM depart_order_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.depart_order_id = dpr.id AND fi.type = '应付') change_amount,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,"
				+ " dpr.sign_status return_order_collection,"
				+ " dpr.remark,"
				+ " DATE(dpr.departure_time) as depart_time, "
				+ " group_concat( distinct(select cast(tor.planning_time AS CHAR) from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') planning_time,"
				+ " oe.office_name office_name,"
				+ " c1.abbr cname,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='运输费' and fi.type = '应付') transport_cost, "
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='搬运费' and fi.type = '应付') carry_cost,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='上楼费' and fi.type = '应付') climb_cost,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='保险费' and fi.type = '应付') insurance_cost,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='提货费' and fi.type = '应付') take_cost, "
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='安装费' and fi.type = '应付') anzhuang_cost,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='仓储费' and fi.type = '应付') cangchu_cost,"
//				+ " (select sum(dofi.amount) from depart_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where depart_order_id = dpr.id and fi.name='其他费用' and fi.type = '应付') other_cost"
				+ " 0 AS transport_cost,"
				+ " 0 AS carry_cost,"
				+ " 0 AS climb_cost,"
				+ " 0 AS insurance_cost,"
				+ " 0 AS take_cost,"
				+ " 0 AS anzhuang_cost,"
				+ " 0 AS cangchu_cost,"
				+ " 0 AS other_cost"
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.depart_id = dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join party_old p1 on tor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
//				+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ "	left join depart_order_fin_item dofi on dofi.depart_order_id = dpr.id "
//				+ " left join product prod on toi.product_id = prod.id "
				+ " left join user_login ul on ul.id = dpr.create_by left join party_old p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
				+ " left join office oe on oe.id = tor.office_id"
				+ "  where  unix_timestamp(dpr.create_stamp) > unix_timestamp('2015-06-01 10:34:36') and (ifnull(dtr.depart_id, 0) > 0) and dpr.audit_status='新建'  and dpr.combine_type='DEPART'"
				+ " 	and p.party_type='SERVICE_PROVIDER' "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
				+ " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"')"
				        + " or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dpr.id"
				+ " union "
				+ " select distinct dpr.id,"
				+ " dpr.depart_no order_no,"
				+ " dpr.status,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_from),"
        		+ " (SELECT NAME FROM location WHERE CODE = dpr.route_from )) route_from,"
        		+ " ifnull((SELECT NAME FROM location WHERE CODE = dpr.route_to),"
        		+ " (SELECT NAME FROM location WHERE CODE = dpr.route_to)) route_to,"
				+ " (CASE tor.arrival_mode WHEN 'gateIn' THEN w.warehouse_name WHEN 'delivery' THEN tor.receiving_address WHEN 'deliveryToFactory' THEN tor.receiving_address WHEN 'deliveryToWarehouse' OR 'deliveryToFachtoryFromWarehouse' THEN w.warehouse_name ELSE tor.receiving_address END) receivingunit, c.abbr spname,"
				+ " ( SELECT CASE WHEN tor.cargo_nature = 'ATM' THEN ( SELECT count(toid.id) FROM transfer_order_item_detail toid"
				+ " WHERE  toid.pickup_id = dpr.id ) WHEN tor.cargo_nature = 'cargo' THEN ( SELECT sum(toi.amount) FROM "
				+ " depart_order dpr2 LEFT JOIN depart_transfer dt on dt.pickup_id = dpr2.id LEFT JOIN transfer_order_item toi on toi.order_id = dt.order_id "
				+ " where dpr2.id = dpr.id ) END ) amount,"
				//+ " (select count(id) from transfer_order_item_detail where pickup_id = pofi.pickup_order_id)as amount,"
				+ " round((SELECT sum(volume)	FROM transfer_order_item_detail	WHERE	pickup_id = dtr.pickup_id),2) volume,"
				+ " round((SELECT	sum(weight)	FROM transfer_order_item_detail	WHERE pickup_id = dtr.pickup_id ),2) weight,"
				+ " DATE(dpr.create_stamp) create_stamp, ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
				+ " null as ref_no,"
				+ " '提货' business_type, "
				+ " null as booking_note_number,"
				+ " null as customer_delivery_no,"
				+ " (select ifnull(sum(pofi.amount),0) from pickup_order_fin_item pofi  where pofi.pickup_order_id = dpr.id and fin_item_id!=7 and IFNULL(pofi.cost_source,'') != '对账调整金额') pay_amount, "
				+ " (select sum(amount) from pickup_order_fin_item dofi left join fin_item fi on fi.id = dofi.fin_item_id where dofi.pickup_order_id = dpr.id and fi.type = '应付') change_amount,"
				+ " group_concat(distinct (select tor.order_no from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') transfer_order_no,"
				+ " dpr.sign_status return_order_collection,"
				+ " dpr.remark,"
				+ " DATE(dpr.turnout_time) as depart_time,"
				+ " group_concat(distinct(select cast(tor.planning_time AS CHAR) from transfer_order tor where tor.id = dtr.order_id) separator '\r\n') planning_time,"
				+ " oe.office_name office_name,"
				+ " c1.abbr cname,"
//				+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='运输费') as transport_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='搬运费') as carry_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='上楼费') as climb_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='保险费') as insurance_cost,"
//				+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='提货费') as take_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='安装费') as anzhuang_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='仓储费') as cangchu_cost,"
//        		+ " (select sum(pofi.amount) from pickup_order_fin_item pofi left join fin_item fi on fi.id = pofi.fin_item_id where pofi.pickup_order_id = dpr.id and fi.name='其他费用') as other_cost"
				+ " 0 AS transport_cost,"
				+ " 0 AS carry_cost,"
	            + " 0 AS climb_cost,"
	            + " 0 AS insurance_cost,"
	            + " 0 AS take_cost,"
	            + " 0 AS anzhuang_cost,"
	            + " 0 AS cangchu_cost,"
	            + " 0 AS other_cost"
				+ " from depart_order dpr "
				+ " left join depart_transfer dtr on dtr.pickup_id = dpr.id"
				+ " left join transfer_order tor on tor.id = dtr.order_id "
				+ " left join party_old p1 on tor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
//				+ " left join transfer_order_item toi on toi.order_id = tor.id "
//				+ " left join transfer_order_item_detail toid on toid.order_id = tor.id "
//				+ " left join product prod on toi.product_id = prod.id "
				//+ " left join pickup_order_fin_item pofi on pofi.pickup_order_id = dtr.pickup_id"
				+ " left join user_login ul on ul.id = dpr.create_by left join party_old p on p.id = dpr.sp_id left join contact c on c.id = p.contact_id "
				+ " LEFT JOIN warehouse w ON w.id = tor.warehouse_id"
				+ " left join office oe on oe.id = tor.office_id "
				+ " where unix_timestamp(dpr.create_stamp) > unix_timestamp('2015-06-01 10:34:36') "
				+ " and (ifnull(dtr.pickup_id, 0) > 0) and dpr.audit_status='新建' "
				+ " and p.party_type='SERVICE_PROVIDER' and dpr.combine_type='PICKUP' "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
                + " and (w.id in (select w.id from user_office uo, warehouse w where uo.office_id = w.office_id and uo.user_name='"+user_name+"')"
                + "      or tor.arrival_mode in ('delivery','deliveryToWarehouse','deliveryToFactory','deliveryToFachtoryFromWarehouse'))"
				+ " group by dpr.id"
				+ " union "
				+ " select distinct ior.id,"
				+ " ior.order_no order_no,"
				+ " ior.status,"
				+ " ifnull((SELECT NAME FROM location WHERE CODE = tor.route_from),"
        		+ " (SELECT NAME FROM location WHERE CODE = tor.route_from )) route_from,"
        		+ " ifnull((SELECT NAME FROM location WHERE CODE = tor.route_to),"
        		+ " (SELECT NAME FROM location WHERE CODE = tor.route_to)) route_to,"
				+ " '' receivingunit, c.abbr spname,"
//				+ " (select sum(toi.amount) from transfer_order_item toi where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) amount,"
//				+ " (select round(sum(ifnull(prod.volume,toi.volume)*amount),2) from transfer_order_item toi left join product prod on toi.product_id = prod.id  where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) volume,"
//				+ " (select round(sum(ifnull(prod.weight,toi.weight)*amount),2) from transfer_order_item toi left join product prod on toi.product_id = prod.id  where toi.order_id in(select tro.id from transfer_order tro where tro.insurance_id  = ior.id )) weight,"
				+ " 0 amount,"
				+ " 0 volume,"
				+ " 0 weight,"
				+ " ior.create_stamp create_stamp,"
				+ " ifnull(ul.c_name, ul.user_name) creator,tor.customer_order_no customer_order_no,"
				+ " null as ref_no,"
				+ " '保险' business_type, "
				+ " null as booking_note_number,"
				+ " null as customer_delivery_no,"
				+ " (select ifnull(sum(ifi.insurance_amount),0) from insurance_fin_item ifi  where ifi.insurance_order_id = ior.id and IFNULL(ifi.cost_source,'') != '对账调整金额') pay_amount, "
				+ " round((SELECT sum(insurance_amount) FROM insurance_fin_item dofi LEFT JOIN fin_item fi ON fi.id = dofi.fin_item_id WHERE dofi.insurance_order_id = ior.id AND fi.type = '应付' ),2) change_amount,"
				+ " group_concat(distinct tor.order_no separator '\r\n') transfer_order_no,"
				+ " ior.sign_status return_order_collection,"
				+ " ior.remark,"
				+ " ior.create_stamp as depart_time,"
				+ " cast(tor.planning_time AS CHAR) planning_time,"
				+ " oe.office_name office_name,"
				+ " c1.abbr cname,"
				+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='运输费') as transport_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='搬运费') as carry_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='上楼费') as climb_cost,"
        		+ " (select sum(ifi.insurance_amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='保险费') as insurance_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='提货费') as take_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='安装费') as anzhuang_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='仓储费') as cangchu_cost,"
        		+ " (select sum(ifi.amount) from insurance_fin_item ifi left join fin_item fi on fi.id = ifi.fin_item_id where ifi.insurance_order_id = ior.id and fi.name='其他费用') as other_cost"
				+ " from insurance_order ior "
				+ " left join insurance_fin_item ifit on ifit.insurance_order_id = ior.id"
				+ " left join transfer_order tor on ior.id = tor.insurance_id "
				+ " left join party_old p1 on tor.customer_id = p1.id"
				+ " left join contact c1 on p1.contact_id = c1.id"
				//+ " left join transfer_order_item toi on toi.order_id = tor.id "
				+ " left join user_login ul on ul.id = ior.create_by"
				+ " left join party_old p on ior.insurance_id = p.id "
				+ " left join contact c on p.contact_id = c.id "
				+ " left join office oe on oe.id = tor.office_id "
				+ " where unix_timestamp(ior.create_stamp) > unix_timestamp('2015-06-01 10:34:36') and ior.audit_status='新建' and p.party_type = 'INSURANCE_PARTY' and ifit.id is not null "
				+ " and tor.customer_id in (select customer_id from user_customer where user_name='"+user_name+"')"
				+ " and ior.office_id in (select office_id from user_office where user_name='"+user_name+"')"
				+ "group by ior.id "
				+ " union "
				+ " SELECT DISTINCT	amco.id,amco.order_no, amco.STATUS, l.name route_from,"
				+ " l1.name route_to,'' receivingunit,c.abbr spname,NULL as amount,NULL as volume,NULL as weight,"
				+ " amco.create_stamp, ifnull(ul.c_name, ul.user_name) creator,'' customer_order_no,null as ref_no,"
				+ " '成本单' business_type,"
				+ " NULL as booking_note_number,null as customer_delivery_no,"
				+ " ifnull(amco.total_amount,0) pay_amount,"
				+ " (SELECT sum(amount) FROM arap_misc_cost_order_item amcoi LEFT JOIN fin_item fi ON fi.id = amcoi.fin_item_id WHERE amcoi.misc_order_id = amco.id AND fi.type = '应付') change_amount,"
				+ " NULL as transfer_order_no,NULL as return_order_collection,amco.remark remark,"
				+ " NULL as depart_time, NULL as planning_time, o.office_name,c1.abbr cname,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '运输费') transport_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id "
				+ " AND fi.NAME = '搬运费') carry_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '上楼费') climb_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '提货费') insurance_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '保险费') take_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '安装费') anzhuang_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '仓库费') cangchu_cost,"
				+ " (SELECT sum(amcoi.amount) FROM arap_misc_cost_order_item amcoi"
				+ " LEFT JOIN fin_item fi ON amcoi.fin_item_id = fi.id"
				+ " WHERE amcoi.misc_order_id = amco.id"
				+ " AND fi.NAME = '其他费用') other_cost"
				+ " FROM arap_misc_cost_order amco"
				+ " LEFT JOIN user_login ul ON ul.id = amco.create_by"
				+ " LEFT JOIN party_old p1 ON amco.customer_id = p1.id"
				+ " LEFT JOIN contact c1 ON p1.contact_id = c1.id"
				+ " LEFT JOIN party_old p ON amco.sp_id = p.id"
				+ " LEFT JOIN contact c ON p.contact_id = c.id"
				+ " LEFT JOIN location l ON amco.route_from=l.code"
				+ " LEFT JOIN location l1 ON amco.route_to=l1.code"
				+ " LEFT JOIN office o ON o.id=amco.office_id"
				+ " where amco.audit_status = '新建' and amco.type = 'biz' and amco.total_amount!=0"
				+ " and amco.office_id in (select office_id from user_office where user_name='"+user_name+"')"
				+ " GROUP BY amco.id) as A ";
        String condition = "";
      
        if(orderNo != null || sp != null || no != null || beginTime != null
        	|| endTime !=null || status != null || type != null){
        	if (beginTime == null || "".equals(beginTime)) {
				beginTime = "1970-01-01";
			}
			if (endTime == null || "".equals(endTime)) {
				endTime = "2037-12-31";
			}
			if (plantime == null || "".equals(plantime)) {
				plantime = "1970-01-01";
			}
			if (arrivaltime == null || "".equals(arrivaltime)) {
				arrivaltime = "2037-12-31";
			}
			 
        	condition =  " where ifnull(order_no,'') like '%" + no + "%' "
        			+ " and ifnull(transfer_order_no,'') like '%" + orderNo + "%' "
        			+ " and ifnull(status,'') like '%" + status + "%' "
        			+ " and ifnull(spname,'') like '%" + sp + "%' "
        			+ " and ifnull(depart_time, '1970-01-01') between '" + beginTime + "' and '" + endTime + " 23:59:59' "
        			+ " and ifnull(business_type,'') like '%" + type + "%'"
        			+ " and ifnull(route_from,'') like '%" + route_from + "%'"
        			+ " and ifnull(route_to,'') like '%" + route_to + "%'"
        			+ " and ifnull(booking_note_number,'') like '%" + booking_note_number + "%'"
        			+ " and ifnull(planning_time, '1970-01-01') between '" + plantime + "' and '" + arrivaltime + " 23:59:59' "
        	        + " and ifnull(cname,'') like '%" + customer_name + "%'"
        	        + " and ifnull(status, '') != '手动删除'";
        	if (StringUtils.isNotEmpty(serial_no)){
        		condition += " and serial_no like '%" + serial_no + "%'";
			}
        	if (StringUtils.isNotEmpty(sign_no)){
        		condition += " and ref_no like '%" + sign_no + "%'";
			}
        }
        if (condition == "" ){
        	condition=" where ifnull(status, '') != '手动删除' ";
        }
        sqlTotal = " select count(1) total from (" + sql + condition + ") as B"; 
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        long sTime = Calendar.getInstance().getTimeInMillis();
        
        String orderByStr = " order by A.depart_time desc ";
        if(colName.length()>0){
        	orderByStr = " order by A."+colName+" "+sortBy;
        }
        List<Record> BillingOrders = Db.find(sql + condition + orderByStr + sLimit);
        long eTime = Calendar.getInstance().getTimeInMillis();
        logger.debug("time cost:" + (eTime-sTime));
        
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap);
    }
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CTC_AFFIRM})
    public void costConfiremReturnOrder(){
    	String ids = getPara("ids");
    	String orderNos = getPara("orderNos");
    	String[] idArr = ids.split(",");
    	String[] orderNoArr = orderNos.split(",");
    	for(int i=0 ; i<idArr.length ; i++){
    		if("提货".equals(orderNoArr[i])){
    			DepartOrder pickupOrder = DepartOrder.dao.findById(idArr[i]);
    			pickupOrder.set("audit_status", "已确认");
    			pickupOrder.update();
    		}else if("零担".equals(orderNoArr[i]) || "整车".equals(orderNoArr[i])){
    			DepartOrder departOrder = DepartOrder.dao.findById(idArr[i]);
    			departOrder.set("audit_status", "已确认");
    			departOrder.update();
    		}else if("配送".equals(orderNoArr[i])){
    			DeliveryOrder deliveryOrder = DeliveryOrder.dao.findById(idArr[i]);
    			deliveryOrder.set("audit_status", "已确认");
    			deliveryOrder.update();
    		}else if("成本单".equals(orderNoArr[i])){
    			ArapMiscCostOrder arapmisccostOrder = ArapMiscCostOrder.dao.findById(idArr[i]);
    			arapmisccostOrder.set("audit_status", "已确认");
    			arapmisccostOrder.update();
    		}else{
    			InsuranceOrder insuranceOrder = InsuranceOrder.dao.findById(idArr[i]);
    			insuranceOrder.set("audit_status", "已确认");
    			insuranceOrder.update();
    		}
    	}
        renderJson("{\"success\":true}");
    }
}
