package controllers.arap.ap;

import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ArapCostOrder;
import models.Party;

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

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostAcceptOrderController extends Controller {
    private Log logger = Log.getLog(CostAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_COSTCONFIRM_LIST})
    public void index() {   
    	render("/eeda/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = " select * from ("
        		+ " select  aco.id, aco.order_no, aco.order_type, aco.status, aco.create_stamp, aco.total_amount totalCostAmount, aco.sp_id, p.company_name sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount"
				+ " from arap_cost_order aco "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id"
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.status!='新建'"
				+ " group by aco.id"
				+ " ) A where totalCostAmount>paid_amount";

        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc "+sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    public void applicationList() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select * from(  "
        		+ " select acao.id,acao.order_no application_order_no,acao.status,acao.payment_method,acao.create_stamp,acao.check_stamp,acao.pay_time, "
        		+ " acao.remark,acao.payee_unit,acao.payee_name, "
        		+ " '申请单' order_type,acao.total_amount,aco.order_no cost_order_no,u.c_name "
				+ " from arap_cost_application_order acao "
				+ " left join cost_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join arap_cost_order aco on aco.id = caor.cost_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ " group by acao.id"
				+ " ) B where 1=1 ";
		
        String condition = DbUtils.buildConditions(getParaMap());
        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by id desc " + sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_CPO_CREATE})
    public void create() {
        String ids = getPara("itemIds");
        String[] idArr=ids.split(",");
        setAttr("ids",ids);
        
        String payee_id = "";
        String payee_filter = "";
        String payee_name = "";
        String deposit_bank = "";
        String bank_no = "";
        String account_name = "";

        ArapCostOrder arapCostOrder = ArapCostOrder.dao.findById(idArr[0]);
        payee_id = arapCostOrder.getLong("sp_id").toString();

        if(StringUtils.isNotEmpty(payee_id)){
            Party contact = Party.dao.findFirst("select * from  party where id = ?",payee_id);
            payee_filter = contact.getStr("company_name");
            deposit_bank = contact.getStr("bank_name");
            bank_no = contact.getStr("bank_no");
            account_name = contact.getStr("receiver");
        }
        setAttr("payee_filter", payee_filter);
        setAttr("deposit_bank", deposit_bank);
        setAttr("bank_no", bank_no);
        setAttr("account_name", account_name);
        
        setAttr("payee_id", payee_id);
        setAttr("payee_name", payee_name);
        
            
        List<Record> Account = null;
        Account = Db.find("select * from fin_account where bank_name != '现金'");
        setAttr("accountList", Account);
        
        setAttr("submit_name", LoginUserController.getLoginUserName(this));
        setAttr("saveOK", false);
        setAttr("status", "new");
        render("/eeda/arap/CostAcceptOrder/payEdit.html");
    } 
    
  
    
    
}
