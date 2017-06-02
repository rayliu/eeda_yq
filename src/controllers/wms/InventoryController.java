package controllers.wms;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.wms.GateIn;
import models.wms.GateOut;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class InventoryController extends Controller {

	private Logger logger = Logger.getLogger(InventoryController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type=getPara("type");
		setAttr("type", type);
		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
		Record re = Db.findFirst("select count(1) total from gate_in gi where gi.out_flag = 'N' "
				+ " and error_flag='N' and office_id = ?",office_id);
		if(re != null){
			setAttr("totalLabel", re.get("total"));
		}
		
		
		render("/wms/inventory/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
        render("/wms/inventory/edit.html");
    }
    
    @Before(Tx.class)
   	public void save() throws Exception {		
   		String jsonStr=getPara("params");
       	
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
            
        PlanOrder planOrder = new PlanOrder();
   		String id = (String) dto.get("id");
   		
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		if (StringUtils.isNotBlank(id)) {
   			//update
   			planOrder = PlanOrder.dao.findById(id);
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("updator", user.getLong("id"));
   			planOrder.set("update_stamp", new Date());
   			planOrder.update();
   		} else {
   			//create 
   			DbUtils.setModelValues(dto, planOrder);
   			
   			//需后台处理的字段
   			planOrder.set("order_no", OrderNoGenerator.getNextOrderNo("JH", office_id));
   			planOrder.set("creator", user.getLong("id"));
   			planOrder.set("create_stamp", new Date());
   			planOrder.set("office_id", office_id);
   			planOrder.save();
   			
   			id = planOrder.getLong("id").toString();
   		}
   		
   		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("item_list");
		DbUtils.handleList(itemList, id, PlanOrderItem.class, "order_id");

		long creator = planOrder.getLong("creator");
   		String user_name = LoginUserController.getUserNameById(creator);
		Record r = planOrder.toRecord();
   		r.set("creator_name", user_name);
   		renderJson(r);
   	}
    
    
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
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
    	PlanOrder planOrder = PlanOrder.dao.findById(id);
    	setAttr("order", planOrder);
    	
    	//获取明细表信息
    	setAttr("itemList", getPlanOrderItems(id));
    	
    	//回显客户信息
    	Party party = Party.dao.findById(planOrder.getLong("customer_id"));
    	setAttr("party", party);

    	//用户信息
    	long creator = planOrder.getLong("creator");
    	UserLogin user = UserLogin.dao.findById(creator);
    	setAttr("user", user);
    	
        render("/wms/inventory/edit.html");
    }
    

    
    public void list() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       

        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class); 
            String item_no = dto.get("item_no");
            String part_no = dto.get("part_no");
            String shelves = dto.get("shelves");
            
            if(StringUtils.isNotBlank(item_no)){
            	condition += " and pro.item_no like '%"+item_no+"%'";
            }
            
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and pro.part_no like '%"+part_no+"%'";
            }
            
            if(StringUtils.isNotBlank(shelves)){
            	condition += " and gi.shelves like '%"+shelves+"%'";
            }
            
  
            
            
    	}
    	
    	String sqlTotal = "select count(1) total from (select pro.id "
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ condition 
			+ " group by pro.item_no) B";
    	
        
    	sql = "select pro.item_no,pro.item_name "
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ condition 
			+ " group by pro.item_no";
    	
        
        
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    
    public void partList() {
    	String sql = "";
        String condition="";
        String proCondition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
        
        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class); 
            String shelves = dto.get("shelves");
            String part_no = dto.get("part_no");
            if(StringUtils.isNotBlank(part_no)){
            	condition += " and pro.part_no = '"+part_no+"'";
            	proCondition += " and pro.part_no = '"+part_no+"'";
            }
            if(StringUtils.isNotBlank(shelves)){
            	condition += " and gi.shelves like '"+shelves+"'";
            }
            
            //处理无法识别item_no的数据
            String item_no = getPara("item_no");
            if(StringUtils.isNotBlank(item_no) && !"null".equals(item_no)){
            	condition += " and pro.item_no = '"+item_no+"'";
            	proCondition += " and pro.item_no = '"+item_no+"'";
            }else{
            	if(StringUtils.isBlank(part_no)){
            		condition += " and pro.item_no is null";
            		proCondition += " and pro.item_no is null";
            	}
            }
            
    	}
    	
    	String sqlTotal = "select count(1) total from (select A.id from (select gi.id,gi.part_no"
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ condition 
			+ " group by gi.id "
			+ " union"
			+ " select pro.id,pro.part_no from wmsproduct pro"
			+ " where amount>0 and pro.office_id="+office_id
			+ proCondition 
			+ " ) A group by A.part_no) B";
        
    	sql = "select A.*,count(IF (quantity = 0, null,A.id)) totalBox,sum(A.quantity) totalPiece from ("
    	    + "select gi.id, gi.quantity,gi.part_no,"
    		+ " pro.item_name,ifnull(pro.item_no,'') item_no,pro.part_name part_name ,"
    		+ " (select GROUP_CONCAT(item_no SEPARATOR ' , ') from wmsproduct where part_no = gi.part_no) usefor"
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ condition 
			+ " group by gi.id "
			+ " union"
			+ " select pro.id,0 quantity,pro.part_no,pro.item_name,pro.item_no,pro.part_name,"
			+ " (select GROUP_CONCAT(item_no SEPARATOR ' , ') from wmsproduct where part_no = pro.part_no) usefor"
			+ " from wmsproduct pro"
			+ " where amount>0 and pro.office_id="+office_id
			+ proCondition 
			+ " ) A group by A.part_no order by A.part_no";
    	
        
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));
        orderListMap.put("data", orderList);

        renderJson(orderListMap); 
    }
    
    
    public void itemDetailList() {
    	String sql = "";
        String condition="";
        String sLimit = "";
        String pageIndex = getPara("draw");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
       
        String part_no = getPara("part_no");
        if(StringUtils.isNotBlank(part_no)){
        	condition += " and gi.part_no = '"+part_no+"'";
        }
        
        String jsonStr = getPara("jsonStr");
    	if(StringUtils.isNotBlank(jsonStr)){
    		Gson gson = new Gson(); 
            Map<String, String> dto= gson.fromJson(jsonStr, HashMap.class); 
            
            String shelves = dto.get("shelves");
            if(StringUtils.isNotBlank(shelves)){
            	condition += " and gi.shelves like '%"+shelves+"%'";
            }
    	}
        
    	sql = "select gi.*, ifnull(u.c_name, u.user_name) creator_name,"
    		+ " pro.item_name,pro.item_no,pro.part_name part_name "
			+ " from gate_in gi "
			+ " left join user_login u on u.id = gi.creator"
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ condition
			+ " group by gi.id ";
    	
        
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);
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
   
    @Before(Tx.class)
    public void gateOut(){
    	String id = getPara("id");
    	
    	GateIn gi = GateIn.dao.findById(id);
    	gi.set("out_flag", "Y");
    	gi.set("self_out_flag", "Y");
    	gi.set("out_time",new Date());
    	gi.update();
    	
    	
    	renderJson(gi);
    }
    
    @Before(Tx.class)
    public void downloadList(){
    	String sql = null;
    	String conditions = "";
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
        
        String excelName = "ALL";
        String item_no = getPara("item_no");
    	if(StringUtils.isNotBlank(item_no)){
    		
    		excelName = item_no;
    		conditions += " and pro.item_no = '"+item_no+"'";
    	}
    	
    	
		sql = "select A.*,sum(A.quantity) total_quantity from ("
		    + " select gi.part_no,pro.part_name,gi.quantity "
			+ " from gate_in gi "
			+ " left join wmsproduct pro on pro.part_no = gi.part_no"
			+ " where gi.office_id="+office_id
			+ " and out_flag = 'N' and error_flag = 'N'"
			+ conditions
			+ " group by gi.id "
			+ " union all"
			+ " select pro.part_no,pro.part_name,0 quantity"
			+ " from wmsproduct pro"
			+ " where amount>0 and pro.office_id="+office_id
			+ conditions 
			+ " ) A group by A.part_no "; 
    	
        String exportSql = sql;
        String[] headers = new String[]{"part_no", "part_name","total_quantity","A&P"};
        String[] fields = new String[]{"part_no", "part_name", "total_quantity",""};
        String fileName = generateExcel(headers, fields, exportSql,excelName);
        renderText(fileName);
    }
    
    @SuppressWarnings("deprecation")
    public String generateExcel(String[] headers, String[] fields, String sql,String name){
	    String fileName="";
	    try {
	        System.out.println("generateExcel begin...");
	        String filePath = getRequest( ).getServletContext().getRealPath("/") + "/download/inventory";
	        File file = new File(filePath);
	        if(!file.exists()){ 
	            file.mkdir();
	        }
	        Date date = new Date();
	        SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
	        String outFileName = "库存统计-"+ name +"-" + formatDate.format(date) + ".xls";
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");  

            HSSFRow rowhead = sheet.createRow((short)0);
            for (int i = 0; i < headers.length; i++) {
                rowhead.createCell((short)i).setCellValue(headers[i]);
            }

            // 设置字体
//            HSSFFont font = workbook.createFont();
//            //font.setFontHeightInPoints((short) 40); //字体高度
//            font.setColor(HSSFFont.COLOR_NORMAL); //字体颜色
//            font.setFontName("宋体"); //字体
//            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); //加粗
//            font.setItalic(false); //是否使用斜体
//            
//            
//          
//	          // 设置单元格类型
//	        HSSFCellStyle cellStyle = workbook.createCellStyle();
//	        //cellStyle.setFont(font);
//	        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); //水平布局：居中
//	        cellStyle.setWrapText(true);
            
            List<Record> recs = Db.find(sql);
            System.out.println("sql finish!");
            HSSFRow row = null;
            HSSFCell cell = null;
            if(recs!=null){
                for (int j = 1; j <= recs.size(); j++) {
                	//HSSFDataFormat format = workbook.createDataFormat();
                	row = sheet.createRow((short)j);
                    Record rec = recs.get(j-1);
                    for (int k = 0; k < fields.length;k++){
                        Object obj = rec.get(fields[k]);
                        String strValue = "";
                        if(obj != null){
                            strValue =obj.toString();
                        }
                        cell = row.createCell((short) k);
                        //cell.setCellStyle(cellStyle);
                        //sheet.autoSizeColumn((short)k);
                        cell.setCellValue(strValue);
                    }
                }
            }

            fileName = filePath+"/"+outFileName;
            System.out.println("fileName: "+fileName);
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");
            fileName = "download/inventory/"+outFileName;
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	    return fileName;
	}
    

}
