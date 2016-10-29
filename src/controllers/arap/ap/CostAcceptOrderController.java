package controllers.arap.ap;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.AppInvoiceDoc;
import models.ArapCostApplication;
import models.ArapCostOrder;
import models.Party;
import models.UserLogin;

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
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CostAcceptOrderController extends Controller {
    private Log logger = Log.getLog(CostAcceptOrderController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    @Before(EedaMenuInterceptor.class)
    public void index() {   
    	render("/eeda/arap/CostAcceptOrder/CostAcceptOrderList.html");
    }
    
    public void list() {
    	String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        
        UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
        String sql = " select * from ("
        		+ " select  aco.id, aco.order_no, aco.order_type, aco.status, aco.create_stamp, aco.total_amount totalCostAmount, aco.sp_id, p.company_name sp_name, "
        		+ " sum(ifnull(c.pay_amount,0)) paid_amount"
				+ " from arap_cost_order aco "
				+ " left join cost_application_order_rel c on c.cost_order_id=aco.id"
				+ " left join party p on p.id=aco.sp_id "
				+ " where aco.status!='新建' and aco.office_id = "+office_id
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
        UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id=user.getLong("office_id");
        String sql = "select * from(  "
        		+ " select acao.id,acao.order_no application_order_no,acao.status,acao.payment_method,acao.create_stamp,acao.check_stamp,acao.pay_time, "
        		+ " acao.remark,acao.payee_unit,acao.payee_name, "
        		+ " '申请单' order_type,acao.total_amount,aco.order_no cost_order_no,u.c_name "
				+ " from arap_cost_application_order acao "
				+ " left join cost_application_order_rel caor on caor.application_order_id = acao.id "
				+ " left join arap_cost_order aco on aco.id = caor.cost_order_id"
				+ " left join user_login u on u.id = acao.create_by"
				+ "	where acao.office_id = "+office_id
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
    
    @Before(EedaMenuInterceptor.class)
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
    
    @Before(EedaMenuInterceptor.class) 
	public void edit() throws ParseException {
		String id = getPara("id");
		ArapCostApplication aca = ArapCostApplication.dao.findById(id);
		setAttr("invoiceApplication", aca);
		
		Party con  = Party.dao.findFirst("select * from party  where id =?",aca.get("payee_id"));
		if(con!=null)
		    setAttr("payee_filter", con.get("company_name"));
		UserLogin userLogin = UserLogin.dao .findById(aca.get("create_by"));
		setAttr("submit_name", userLogin.get("c_name"));
		
		Long check_by = aca.getLong("check_by");
		if( check_by != null){
			userLogin = UserLogin.dao .findById(check_by);
			String check_name = userLogin.get("c_name");
			setAttr("check_name", check_name);
		}
		
		List<Record> list = null;
    	list = getItems(id);
    	setAttr("docList", list);
		
		List<Record> Account = Db.find("select * from fin_account where bank_name != '现金'");
		setAttr("accountList", Account);
		
		render("/eeda/arap/CostAcceptOrder/payEdit.html");
	}
    
  
  //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
    	String order_id = getPara("order_id");
    	List<UploadFile> fileList = getFiles("doc");
    	
    	AppInvoiceDoc order = new AppInvoiceDoc();
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();

			order.set("order_id", order_id);
			order.set("uploader", LoginUserController.getLoginUserId(this));
			order.set("doc_name", fileName);
			order.set("type", "cost");
			order.set("upload_time", new Date());
			order.save();
		}

    	renderJson(order);
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	
    	List<Record> list = null;
    	list = getItems(order_id);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());

        map.put("aaData", list);

        renderJson(map); 
    }
    
  //返回list
    private List<Record> getItems(String orderId) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	
    	itemSql = "select aid.*,u.c_name from app_invoice_doc aid left join user_login u on aid.uploader=u.id "
    			+ " where aid.order_id=? and aid.type='cost' order by aid.id desc";
    	itemList = Db.find(itemSql, orderId);
	    
		return itemList;
	}
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	AppInvoiceDoc order = AppInvoiceDoc.dao.findById(id);
    	String fileName = order.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            order.delete();
            resultMap.put("result", result);
        }else{
        	order.delete();
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }

    
}
