package controllers.cms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.cms.CustomPlanOrder;
import models.eeda.cms.CustomPlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import config.ShiroExt;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CustomPlanOrderController extends Controller {

	private Logger logger = Logger.getLogger(CustomPlanOrderController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		
		render("/cms/customPlanOrder/CustomPlanOrderlist.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
        String jobId = getPara("jobOrderId");
        String to_office_id = getPara("to_office_id");
        JobOrder jo = JobOrder.dao.findById(jobId);
        setAttr("jobOrder", jo);
        setAttr("customTemplateInfo", getCustomTemplateInfo());
        setAttr("to_office_id", to_office_id);

        
        List<Record> re = Db.find("select null id ,joc.doc_name,joc.upload_time,joc.remark,ul.c_name c_name,joc.uploader from job_order_custom_doc joc"
        		+ " left join user_login ul on joc.uploader = ul.id"
        		+ " where joc.order_id = ? and joc.share_flag = 'Y'",jobId);
        setAttr("docList", re);
        
        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        CustomPlanOrder customPlanOrder = new CustomPlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotEmpty(id)) {
   			//update
   			customPlanOrder = CustomPlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("updator", user.getLong("id"));
   			customPlanOrder.set("update_stamp", new Date());
   			customPlanOrder.update();
   			saveCustomTemplate(dto);
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, customPlanOrder);
   			
   			//需后台处理的字段
   			customPlanOrder.set("order_no", OrderNoGenerator.getNextOrderNo("BGSQ", office_id));
   			customPlanOrder.set("creator", user.getLong("id"));
   			customPlanOrder.set("create_stamp", new Date());
   			customPlanOrder.set("office_id", office_id);
   			customPlanOrder.save();
   			id = customPlanOrder.getLong("id").toString();
   			saveCustomTemplate(dto);
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, CustomPlanOrderItem.class, "order_id");
		
		List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
		DbUtils.handleList(doc_list, "custom_plan_order_doc", id, "order_id");
		
		List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
		DbUtils.handleList(charge_list, "custom_plan_order_arap", id, "order_id");
		
		List<Map<String, String>> cost_list = (ArrayList<Map<String, String>>)dto.get("cost_list");
		DbUtils.handleList(cost_list, "custom_plan_order_arap", id, "order_id");
		
		List<Map<String, String>> shipping_item = (ArrayList<Map<String, String>>)dto.get("shipping_item");
		DbUtils.handleList(shipping_item, "custom_plan_order_shipping_item", id, "order_id");

		long creator = customPlanOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = customPlanOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
   
    private void saveCustomTemplate(Map<String, ?> dto) {
    	Long creator_id = LoginUserController.getLoginUserId(this);
    	
    	String receive_sent_consignee = (String) dto.get("receive_sent_consignee");
    	String production_and_sales = (String) dto.get("production_and_sales");
    	String application_unit = (String) dto.get("application_unit");
    	String export_port = (String) dto.get("export_port");
    	String transport_type = (String) dto.get("transport_type");
    	String supervision_mode = (String) dto.get("supervision_mode");
    	String nature_of_exemption = (String) dto.get("nature_of_exemption");
    	String record_no = (String) dto.get("record_no");
    	String trading_country = (String) dto.get("trading_country");
    	String destination_country = (String) dto.get("destination_country");
    	String destination_port = (String) dto.get("destination_port");
    	String supply_of_goods = (String) dto.get("supply_of_goods");
    	String license_no = (String) dto.get("license_no");
    	String contract_agreement_no = (String) dto.get("contract_agreement_no");
    	String deal_mode = (String) dto.get("deal_mode");
    	String note = (String) dto.get("note");
    	
    	String sql = "select 1 from custom_plan_order_template where"
                + " creator_id = "+creator_id;
      
        if(StringUtils.isNotEmpty(receive_sent_consignee)){
        	sql+=" and receive_sent_consignee= '"+receive_sent_consignee+"'";
        }
        if(StringUtils.isNotEmpty(production_and_sales)){
        	sql+=" and production_and_sales= '"+production_and_sales+"'";
        }
        if(StringUtils.isNotEmpty(application_unit)){
        	sql+=" and application_unit= '"+application_unit+"'";
        }
        if(StringUtils.isNotEmpty(export_port)){
        	sql+=" and export_port= '"+export_port+"'";
        }
        if(StringUtils.isNotEmpty(transport_type)){
        	sql+=" and transport_type= '"+transport_type+"'";
        }
        if(StringUtils.isNotEmpty(supervision_mode)){
        	sql+=" and supervision_mode= '"+supervision_mode+"'";
        }
        if(StringUtils.isNotEmpty(nature_of_exemption)){
        	sql+=" and nature_of_exemption= '"+nature_of_exemption+"'";
        }
        if(StringUtils.isNotEmpty(record_no)){
        	sql+=" and record_no= '"+record_no+"'";
        }
        if(StringUtils.isNotEmpty(trading_country)){
        	sql+=" and trading_country= '"+trading_country+"'";
        }
        if(StringUtils.isNotEmpty(destination_country)){
        	sql+=" and destination_country= '"+destination_country+"'";
        }
        if(StringUtils.isNotEmpty(destination_port)){
        	sql+=" and destination_port= '"+destination_port+"'";
        }
        if(StringUtils.isNotEmpty(supply_of_goods)){
        	sql+=" and supply_of_goods= '"+supply_of_goods+"'";
        }
        if(StringUtils.isNotEmpty(license_no)){
        	sql+=" and license_no= '"+license_no+"'";
        }
        if(StringUtils.isNotEmpty(contract_agreement_no)){
        	sql+=" and contract_agreement_no= '"+contract_agreement_no+"'";
        }
        if(StringUtils.isNotEmpty(deal_mode)){
        	sql+=" and deal_mode= '"+deal_mode+"'";
        }
        if(StringUtils.isNotEmpty(note)){
        	sql+=" and note= '"+note+"'";
        }
  
        Record checkRec = Db.findFirst(sql);
    	if(checkRec==null){
    		Record r= new Record();
    		r.set("creator_id", creator_id);
    		r.set("receive_sent_consignee", receive_sent_consignee);
    		r.set("production_and_sales", production_and_sales);
    		r.set("application_unit", application_unit);
    		r.set("export_port", export_port);
    		r.set("transport_type", transport_type);
    		r.set("supervision_mode", supervision_mode);
    		r.set("nature_of_exemption", nature_of_exemption);
    		r.set("record_no", record_no);
    		r.set("trading_country", trading_country);
    		r.set("destination_country", destination_country);
    		r.set("destination_port", destination_port);
    		r.set("supply_of_goods", supply_of_goods);
    		r.set("license_no", license_no);
    		r.set("contract_agreement_no", contract_agreement_no);
    		r.set("deal_mode", deal_mode);
    		r.set("note", note);
    		Db.save("custom_plan_order_template", r);
    	}
	}

	//返回list
    private List<Record> getItems(String orderId, String type) {
    	String itemSql = "";
    	List<Record> itemList = null;
    	if("cargo".equals(type)){
    		itemSql = " SELECT cpo.*,cur.name currency_name,l.name destination_country_item_name, concat(cen.code,' ',cen.name) exemption_name"
    				+ " FROM custom_plan_order_item cpo"
    				+ " left join currency cur on cur.id =cpo.currency"
    				+ "	LEFT JOIN location l on l.id = cpo.destination_country_item"
    				+ " left join custom_exemption_nature cen on cen.id = cpo.exemption"
    				+ " where cpo.order_id=? order by cpo.id";
    		itemList = Db.find(itemSql, orderId);
    	}else if("doc".equals(type)){
    		itemSql = "select cpo.ref_job_order_id, null id ,jocd.doc_name,jocd.upload_time,jocd.remark,ul.c_name c_name,jocd.uploader from job_order_custom_doc jocd"
    				+ " LEFT JOIN user_login ul on ul.id = jocd.uploader"
    				+ " LEFT JOIN custom_plan_order cpo on cpo.ref_job_order_id = jocd.order_id and jocd.share_flag = 'Y'"
    				+ " where cpo.id =?"
    				+ " union all"
    				+ " select  cpo.ref_job_order_id, jod.id ,jod.doc_name,jod.upload_time,jod.remark,u.c_name c_name,jod.uploader "
    				+ " from custom_plan_order_doc jod "
    				+ " left join custom_plan_order cpo on cpo.id = jod.order_id"
    				+ " left join user_login u on jod.uploader=u.id "
	    			+ " where order_id=?";
    		itemList = Db.find(itemSql, orderId,orderId);
    	}else if("charge".equals(type)){
    	  //shiroExt 去判断权限
            ShiroExt shiro = new ShiroExt();
            boolean isShowHideRow = shiro.hasPermission("customPlanOrder.fin_item_hide");
    		itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name from custom_plan_order_arap jor "
    		        + " left join party pr on pr.id=jor.sp_id"
    		        + " left join fin_item f on f.id=jor.charge_id"
    		        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
    		        + " where order_id=? and order_type=? ";
            if(!isShowHideRow){
                itemSql += " and jor.hide_flag='N'";
            }
    		itemList = Db.find(itemSql +" order by jor.id", orderId, "charge");
    	}else if("cost".equals(type)){
    	    ShiroExt shiro = new ShiroExt();
            boolean isShowHideRow = shiro.hasPermission("customPlanOrder.fin_item_hide");
	    	itemSql = "select jor.*, pr.abbr sp_name, f.name charge_name,f.name_eng charge_name_eng,u.name unit_name,c.name currency_name from custom_plan_order_arap jor"
	    	        + " left join party pr on pr.id=jor.sp_id"
	    	        + " left join fin_item f on f.id=jor.charge_id"
	    	        + " left join unit u on u.id=jor.unit_id"
    		        + " left join currency c on c.id=jor.currency_id"
	    	        + " where order_id=? and order_type=?";
	    	if(!isShowHideRow){
                itemSql += " and jor.hide_flag='N'";
            }
	    	itemList = Db.find(itemSql + " order by jor.id", orderId, "cost");
    	}else if("shipping".equals(type)){
	    	itemSql = "select * from custom_plan_order_shipping_item "
	    	        + " where order_id=? order by id";
	    	itemList = Db.find(itemSql, orderId);
    	}
    	return itemList;
    }
    
    @Before(EedaMenuInterceptor.class)
    public void edit() {
    	String id = getPara("id");
    	String sql = "select cpo.*,l.name trading_country_name,l1.name destination_country_name,l2.name destination_port_name,sm.name supervision_mode_name,p.abbr hongkong_consignee_input"
    			+ "	,p1.abbr head_carrier_name,l3.name aim_port_name,l4.name shipment_port_name,concat(ce.code,' ',ce.name) nature_of_exemption_name,"
    			+ " concat(cp.under_code,' ',cp.under_port) export_port_name, concat(cgs.code,' ',cgs.name) supply_of_goods_name,"
    			+ " l5.name appointed_port_name, p2.abbr boat_company_name, p3.abbr shipping_men_name, p4.abbr consignee_name, p5.abbr notice_man_name,"
    			+ " concat(p6.abbr,' ',ifnull(p6.custom_registration,'')) customs_code_name "
    			+ " from custom_plan_order cpo"
    			+ " left join location l on l.id=cpo.trading_country"
    			+ " left join location l1 on l1.id=cpo.destination_country"
    			+ " left join location l2 on l2.id=cpo.destination_port"
    			+ "	LEFT JOIN location l3 on l3.id = cpo.aim_port"
    			+ "	LEFT JOIN location l4 on l4.id = cpo.shipment_port"
    			+ " left join location l5 on l5.id=cpo.appointed_port"
    			+ "	LEFT JOIN party p on p.id = cpo.hongkong_consignee"
    			+ "	LEFT JOIN party p1 on p1.id = cpo.head_carrier"
    			+ "	LEFT JOIN party p2 on p2.id = cpo.boat_company"
    			+ "	LEFT JOIN party p3 on p3.id = cpo.shipping_men"
    			+ "	LEFT JOIN party p4 on p4.id = cpo.consignee"
    			+ "	LEFT JOIN party p5 on p5.id = cpo.notice_man"
    			+ "	LEFT JOIN party p6 on p6.id = cpo.customs_code"
    			+ " left join supervision_method sm on sm.id = cpo.supervision_mode"
    			+ " left join custom_exemption_nature ce on ce.id = cpo.nature_of_exemption"
    			+ " left join custom_goods_supply cgs on cgs.id = cpo.supply_of_goods"
    			+ " left join custom_port cp on cp.id = cpo.export_port"
    			+ " where cpo.id = ?";
    	Record r = Db.findFirst(sql,id);
    	setAttr("order", r);
    	setAttr("shippingItemList", getItems(id,"shipping"));
    	setAttr("itemList", getItems(id,"cargo"));
    	setAttr("docList", getItems(id,"doc"));
    	
    	setAttr("chargeList", getItems(id,"charge"));
    	setAttr("costList", getItems(id,"cost"));
    	setAttr("customTemplateInfo", getCustomTemplateInfo());
    	
    	//用户信息
    	long creator = r.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/cms/customPlanOrder/CustomPlanOrderEdit.html");
    }
    
    
    private Object getCustomTemplateInfo() {
    	String sql = "select t.*, p.abbr receive_sent_consignee_abbr, concat(ifnull(p.address_eng, p.address), '\r', ifnull(p.contact_person_eng, p.contact_person), '\r', ifnull(p.phone,'')) receive_sent_consignee_info,"
    			+ " p1.abbr production_and_sales_abbr, concat(ifnull(p1.address_eng, p1.address), '\r', ifnull(p1.contact_person_eng, p1.contact_person), '\r', ifnull(p1.phone,'')) production_and_sales_info,"
    			+ " p2.abbr application_unit_abbr, concat(ifnull(p2.address_eng, p2.address), '\r', ifnull(p2.contact_person_eng, p2.contact_person), '\r', ifnull(p2.phone,'')) application_unit_info,"
    			+ " s.name supervision_mode_name, l.name trading_country_name, l1.name destination_country_name, l2.name destination_port_name,"
    			+ " s1.name nature_of_exemption_name "
    			+ " from custom_plan_order_template t "
    			+ " left join party p on p.id = t.receive_sent_consignee"
    			+ " left join party p1 on p1.id = t.production_and_sales"
    			+ " left join party p2 on p2.id = t.application_unit"
    			+ " left join supervision_method s on s.id = t.supervision_mode"
    			+ " left join supervision_method s1 on s1.code = t.nature_of_exemption"
    			+ " left join location l on l.id = t.trading_country"
    			+ " left join location l1 on l1.id = t.destination_country"
    			+ " left join location l2 on l2.id = t.destination_port"
    			+ " where t.creator_id=? order by t.id";
    	List<Record> t = Db.find(sql, LoginUserController.getLoginUserId(this));
		return t;
	}

	public void list() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "";
        String condition="";
        
    	sql = "SELECT * from (SELECT cpo.id,cpo.order_no,cpo.type,cpo.production_and_sales_input application_company_name,ul.c_name creator_name,cpo.booking_no,"
    			+ " cpo.create_stamp,cpo.status,cpo.custom_state,(SELECT COUNT(0) from custom_plan_order cpo WHERE cpo.custom_state = '放行' and cpo.office_id="+office_id+") pass,"
    			+ " (SELECT COUNT(1) from custom_plan_order cpo WHERE cpo.custom_state = '查验' and cpo.office_id="+office_id+") checked,"
    			+ "	(SELECT COUNT(2) from custom_plan_order cpo WHERE cpo.custom_state = '异常待处理' and cpo.office_id="+office_id+") handling,"
    			+ " (SELECT COUNT(3) from custom_plan_order cpo WHERE cpo.custom_state = '异常' and cpo.office_id="+office_id+") abnormal,"
    			+ " (SELECT COUNT(4) from custom_plan_order cpo WHERE cpo.status = '待审核' and cpo.office_id="+office_id+") waitAuditing"
    			+ " FROM custom_plan_order cpo"
    			+ " LEFT JOIN user_login ul on ul.id = cpo.creator"
    			+ " where cpo.office_id="+office_id+" or cpo.to_office_id ="+office_id+")A"
		        + " where 1 =1 ";

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
    
  //异步刷新子表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	Boolean showHide = Boolean.valueOf(getPara("showHide"));
    	
    	List<Record> list = null;
    	list = getItems(order_id, type);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
    }
   
    
    //提交申请单给报关行
    public void confirmCompleted(){
    	String id = getPara("id");
    	String plan_order_no = getPara("plan_order_no");
    	String customer_id= getPara("customer_id");
    	String btnId = getPara("btnId");
    	CustomPlanOrder order = CustomPlanOrder.dao.findById(id);
    	if("confirmCompleted".equals(btnId)){
    		order.set("status","待审核");
    		order.set("fill_by",LoginUserController.getLoginUserId(this));
    		order.set("fill_stamp",new Date());
    	}
    	if("passBtn".equals(btnId)){
    		order.set("status","审核通过");
    		Record r = new Record();
    		r.set("plan_order_id", id);
    		r.set("plan_order_no", plan_order_no);
    		r.set("customer_id", customer_id);
    		
       		UserLogin user = LoginUserController.getLoginUser(this);
       		long office_id = user.getLong("office_id");
       		r.set("creator", user.getLong("id"));
   			r.set("create_stamp", new Date());
   			r.set("office_id", office_id);
    		r.set("order_no", OrderNoGenerator.getNextOrderNo("BGGZD", office_id));
    		Db.save("custom_job_order", r);
    		id = r.getLong("id").toString();
    		
    		
    	}
    	if("refuseBtn".equals(btnId) || "cancelAuditBtn".equals(btnId)){
    		order.set("status","审核不通过");
    	}
    	order.update();
    	
    	Record rec = order.toRecord();
    	rec.set("job_order_id", id);
    	renderJson(rec);
    }
    
  //上传相关文档
    @Before(Tx.class)
    public void uploadDocFile(){
    	String order_id = getPara("order_id");
    	List<UploadFile> fileList = getFiles("doc");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		
			Record r = new Record();
			r.set("order_id", order_id);
			r.set("uploader", LoginUserController.getLoginUserId(this));
			r.set("doc_name", fileName);
			r.set("upload_time", new Date());
			Db.save("custom_plan_order_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("custom_plan_order_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("custom_plan_order_doc",r);
            resultMap.put("result", result);
        }else{
        	Db.delete("custom_plan_order_doc",r);
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    
    //删除常用模版
    @Before(Tx.class)
    public void deleteCustomTemplate(){
    	String id = getPara("id");
    	Db.update("delete from custom_plan_order_template where id = ?",id);
    	renderJson("{\"result\":true}");
    }
    
    //费用明细确认
    @Before(Tx.class)
    public void feeConfirm(){
		String id = getPara("id");
		Db.update("update custom_plan_order_arap set audit_flag = 'Y' where id = ?", id);
		renderJson("{\"result\":true}");
	 }
    

}
