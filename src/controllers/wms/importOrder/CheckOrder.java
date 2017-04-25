package controllers.wms.importOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.wms.GateIn;
import models.wms.GateOut;
import models.wms.InvCheckOrder;
import models.wms.Wmsproduct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import au.com.bytecode.opencsv.CSVReader;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;


@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CheckOrder extends Controller {
	private Logger logger = Logger.getLogger(CheckOrder.class);
	Subject currentUser = SecurityUtils.getSubject();
	//Long user_id = null;
	
	/**
	 * 校验表头是否和数据库的相符
	 * @param title
	 * @param execlType
	 * @return
	 */
	public boolean checkoutExeclTitle(String[] title, String execlType) {
		boolean flag = true;
		List<Record> dbTitleList = Db.find("select execl_title from execl_title where execl_type = ? ", execlType);
		if (dbTitleList != null) {
			// 判断总数是否相等
			if (dbTitleList.size() != title.length) {
				flag = false;
			}else{
				// 判断是否所有列标题一致
				List<String> titleList = new ArrayList<String>(dbTitleList.size());
				for (Record record : dbTitleList) {
					titleList.add(record.getStr("execl_title").trim());
				}
				
				for (int i = 0; i < title.length; i++) {
					String excelTitle = title[i];
					if (!titleList.contains(excelTitle.trim())) {
						flag = false;
					}
				}
			}
		}
		
		return flag;
	}
	

	
	/**
     * 四舍五入
     * @param number
     * @return
     */
	public static String changeNum(Double number){
		return String.format("%.2f", number);
	}

	/**
	 * 校验是否为double类型
	 * @param value
	 * @return
	 */
	public static boolean checkDouble(String value){
		boolean flag = true;
		for (int i = value.length();--i>=0;){    
		   if (!Character.isDigit(value.charAt(i)) && !String.valueOf(value.charAt(i)).equals(".")){  
			  flag = false;  
		   }  
	    }  
	    return flag;
	}
	
	
	/**
	 * 数字分割
	 * @param value
	 * @return
	 */
	public static String getDouble(String value){
		String number = null;
		for (int i = 0;i<value.length();i++){    
		   if (!Character.isDigit(value.charAt(i))){  
			   number = value.substring(i+1,value.length());  
		   }else{
			   return number==null?value:number;
		   }
	    }  
	    return number==null?value:number;
	}
	
	/**
	 * 注塑件编号重复校验
	 */
	public static boolean checkItemNo (String value){
		boolean flag = true;
		
		Wmsproduct so = Wmsproduct.dao.findFirst("select * from wmsproduct where item_no = ?",value);
		if(so != null){
			flag = false;
		}
	    return flag;
	}
	
	/**
	 * qr_code重复校验
	 */
	public static boolean checkQrCode (String table,String value){
		boolean flag = true;
		
		Record re = Db.findFirst("select * from "+table+" where qr_code = ?",value);
		if(re != null){
			flag = false;
		}
	    return flag;
	}

	/**
	 * 日期格式校验
	 * @param value
	 * @return
	 */
	public static boolean checkDate(String dateValue) {    
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
        try{  
            Date date = (Date)formatter.parse(dateValue);   
            return dateValue.equals(formatter.format(date));  
        }catch(Exception e){  
            return false;  
        }  
    }  
	
	
	/**
	 * 数据产品校验
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importProductCheck( List<Map<String, String>> lines) {
		System.out.println("一共要导入"+lines.size()+"行数据");
		Record result = new Record();
		result.set("result",true);
		String errorMsg = "";
		int rowNumber = 1;
		try {
			for (Map<String, String> line :lines) {
				String item_name = line.get("注塑件名称")==null?null:line.get("注塑件名称").trim();
				String item_no = line.get("注塑件编码")==null?null:line.get("注塑件编码").trim();
				String part_no = line.get("组件编码")==null?null:line.get("组件编码").trim();
				String part_name = line.get("组件名称")==null?null:line.get("组件名称").trim();
				String amount = line.get("数量")==null?null:line.get("数量").trim();
				String unit = line.get("Un")==null?null:line.get("Un").trim();
				String node = line.get("节点")==null?null:line.get("节点").trim();
				
				
				if(StringUtils.isNotBlank(item_no)){
					if(!checkItemNo(item_no)){
						errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:此【注塑件编码】("+item_no+")已存在，请核实是否有重复导入<br/><br/>");
					}
				}else{
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【注塑件编码】不能为空<br/><br/>");
				}
				
				if(StringUtils.isEmpty(item_name)){
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【注塑件名称】不能为空<br/><br/>");
				}
				
				if(StringUtils.isEmpty(part_no)){
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【组件编码】不能为空<br/><br/>");
				}
				
				if(StringUtils.isEmpty(part_name)){
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【组件名称】不能为空<br/><br/>");
				}
				
				if(StringUtils.isNotBlank(amount)){
					if(!checkDouble(amount)){
						errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【数量】("+amount+")格式类型有误<br/><br/>");
					}
				}else{
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【数量】不能为空<br/><br/>");
				}
				
				if(StringUtils.isEmpty(unit)){
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【Un】不能为空<br/><br/>");
				}
				
				if(StringUtils.isEmpty(node)){
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【节点】不能为空<br/><br/>");
				}
				
				rowNumber++;
				System.out.println("校验完"+(rowNumber)+"行");
			}
			
			if(StringUtils.isNotBlank(errorMsg)){
				throw new Exception(errorMsg);
			}
			
		} catch (Exception e) {
			System.out.println("校验操作异常！");
			System.out.println(e.getMessage());
			
			result.set("result", false);
			result.set("cause", e.getMessage());	
		} 
		return result;
	}
	
	
	/**
	 * 销售订单内容开始导入
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importProductValue( List<Map<String, String>> lines, long userId, long officeId) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);
		
		int rowNumber = 1;

		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			for (Map<String, String> line :lines) {
				String item_name = line.get("maktx")==null?null:line.get("maktx").trim();
				String item_no = line.get("matnr")==null?null:line.get("matnr").trim();
				String part_no = line.get("idnrk")==null?null:line.get("idnrk").trim();
				String part_name = line.get("ojtxp")==null?null:line.get("ojtxp").trim();
				String amount = line.get("bdmng")==null?null:line.get("bdmng").trim();
				String unit = line.get("meins")==null?null:line.get("meins").trim();
				String node = line.get("pid")==null?null:line.get("pid").trim();
		

				//默认值带入
				Wmsproduct order = new Wmsproduct();
				order.set("item_name", item_name);  
				order.set("item_no", item_no); 
				order.set("part_no",part_no);  
				order.set("part_name",part_name);  
				order.set("unit",unit);  
				order.set("node",node);  
				order.set("amount",amount);  
				order.set("creator", userId); 
				order.set("create_time", new Date());
				order.set("office_id", officeId); 
				order.save();	
				
				rowNumber++;
			}
			conn.commit();
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！");
		} catch (Exception e) {
			System.out.println("导入操作异常！");
			System.out.println(e.getMessage());
			e.printStackTrace();
			
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			result.set("result", false);
			
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber+1)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
			
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		
		return result;
	}
	
	/**
	 * 数据产品校验
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importGateInCheck( List<Map<String, String>> lines) {
		System.out.println("一共要导入"+lines.size()+"行数据");
		Record result = new Record();
		result.set("result",true);
		String errorMsg = "";
		int rowNumber = 1;
		try {
			for (Map<String, String> line :lines) {
				String qr_code = line.get("qr_code")==null?null:line.get("qr_code").trim();
				String part_no = line.get("part_no")==null?null:line.get("part_no").trim();
				String quantity = line.get("quantity")==null?null:line.get("quantity").trim();
				String shelves = line.get("shelves")==null?null:line.get("shelves").trim();
				String return_flag = line.get("return_flag")==null?null:line.get("return_flag").trim();
				String move_flag = line.get("move_flag")==null?null:line.get("move_flag").trim();
				String creator = line.get("creator")==null?null:line.get("creator").trim();
				String create_time = line.get("create_time")==null?null:line.get("create_time").trim();
				
				
				if(StringUtils.isNotBlank(qr_code)){
					if(!checkItemNo(qr_code)){
						errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:此【注塑件编码】("+qr_code+")已存在，请核实是否有重复导入<br/><br/>");
					}
				}else{
					errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:【注塑件编码】不能为空<br/><br/>");
				}
				
				rowNumber++;
				System.out.println("校验完"+(rowNumber)+"行");
			}
			
			if(StringUtils.isNotBlank(errorMsg)){
				throw new Exception(errorMsg);
			}
			
		} catch (Exception e) {
			System.out.println("校验操作异常！");
			System.out.println(e.getMessage());
			
			result.set("result", false);
			result.set("cause", e.getMessage());	
		} 
		return result;
	}
	
	
	/**
	 * 入库记录开始导入
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importGateInValue(CSVReader csvReader, long officeId) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);
		String repeatMsg = "";
		int rowNumber = 0;
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			String[] csvRow = null;//row  
            String[] title = null;
            
            while ((csvRow = csvReader.readNext()) != null){    
            	if(rowNumber == 0){
            		title = csvRow;
            		rowNumber++;
            		continue;
            	}

            	GateIn order = new GateIn();
                for (int i =0; i<csvRow.length; i++){
                	String titleName = StringUtils.isNotBlank(title[i])?title[i].trim():null;
                	String value = StringUtils.isNotBlank(csvRow[i])?csvRow[i].trim():null;;
                	if(StringUtils.isNotBlank(value)){
                		if(!"id".equals(titleName) && !"creator".equals(titleName) && !"qr_code".equals(titleName)){
		                    order.set(titleName, value);
	                	}else if("creator".equals(titleName)){
	                		UserLogin ul = UserLogin.dao.findFirst("select * from user_login where c_name = ?",value);
	                		if(ul != null)
	                			order.set(titleName, ul.getLong("id"));
	                		order.set("creator_code", value);
	                	}else if("qr_code".equals(titleName)){
	                		order.set(titleName, value);
	                		GateIn gi = GateIn.dao.findFirst("select * from gate_in where qr_code = ? and move_flag = ? and return_flag = ?",value,order.getStr("move_flag"),order.getStr("return_flag"));
	                		if(gi != null){
	                			order.set("error_flag", "Y");
	                			order.set("error_msg", "货架上已经存在此货品，不能重复入库");
	                		}
	                	}
                	}
                }    
                order.set("office_id", officeId);
                order.save();
                rowNumber++;
            }
            conn.commit();
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！<br/><br/>"+repeatMsg);
		} catch (Exception e) {
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
            
			result.set("result", false);
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber+1)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
			//throw new ActiveRecordException(e);
		}  finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		return result;
	}
	
	
	
	@Before(Tx.class)
	public Record importGateOutCheck(CSVReader csvReader) {

		Record result = new Record();
		result.set("result",true);
		String errorMsg = "";
		int rowNumber = 0;
		
		try {
			
			String[] csvRow = null;//row  
            String[] title = null;
            
            while ((csvRow = csvReader.readNext()) != null){    
            	if(rowNumber == 0){
            		title = csvRow;
            		rowNumber++;
            		continue;
            	}
                for (int i =0; i<csvRow.length; i++){
                	String titleName = StringUtils.isNotBlank(title[i])?title[i].trim():null;
                	String value = StringUtils.isNotBlank(csvRow[i])?csvRow[i].trim():null;;
                	if(StringUtils.isNotBlank(value)){
                		if("qr_code".equals(titleName)){
	                		GateIn gi = GateIn.dao.findFirst("select * from gate_in where error_flag='N' and qr_code = ?",value);
	                		if(gi == null){
	                			errorMsg += ("数据校验至第" + (rowNumber+1) + "行时出现异常:库存中没有此货品（未入库）<br/>");
	                		} 
	                	}
                	}
                }    
                rowNumber++;
            }
            if(StringUtils.isNotBlank(errorMsg)){
            	result.set("result", false);
            	result.set("cause", errorMsg);
            }
		} catch (Exception e) {
            
			result.set("result", false);
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
		} 
		return result;
	}

	
	/**
	 * 出库记录开始导入
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importGateOutValue(CSVReader csvReader, long officeId) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);
		String repeatMsg = "";
		int rowNumber = 0;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhMMss");
  		String c=sdf.format(new Date());   //临时单号
		try {
			
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			String[] csvRow = null;//row  
            String[] title = null;
            int arrayNum = 0;
            String this_order_no = null;
            List<Record> reList =null;
            while ((csvRow = csvReader.readNext()) != null){    
            	if(rowNumber == 0){
            		title = csvRow;
            		rowNumber++;
            		continue;
            	}
            	GateOut order = new GateOut();
                for (int i =0; i<csvRow.length; i++){
                	String titleName = StringUtils.isNotBlank(title[i])?title[i].trim():null;
                	String value = StringUtils.isNotBlank(csvRow[i])?csvRow[i].trim():null;;
                	if(StringUtils.isNotBlank(value)){
                		if(!"id".equals(titleName) && !"creator".equals(titleName) && !"qr_code".equals(titleName)){
		                    order.set(titleName, value);
	                	}else if("creator".equals(titleName)){
	                		UserLogin ul = UserLogin.dao.findFirst("select * from user_login where c_name = ?",value);
	                		if(ul != null)
	                			order.set(titleName, ul.getLong("id"));
	                		order.set("creator_code", value);
	                	}else if("qr_code".equals(titleName)){
	                		order.set(titleName, value);
	                		GateIn gi = GateIn.dao.findFirst("select * from gate_in where error_flag='N' and qr_code = ? and move_flag = ?",value,order.getStr("move_flag"));
	                		if(gi != null){
	                			String out_flag = gi.getStr("out_flag");
	                			if("Y".equals(out_flag)){
	                				order.set("error_flag", "Y");
		                			order.set("error_msg", "此货品已经出库");
	                			}else{
	                				gi.set("out_flag", "Y").update();
	                			}
	                		} else {
	                			order.set("error_flag", "Y");
	                			order.set("error_msg", "未入库");
	                		}
	                	}
                	}
                }    
                
                
                String order_no = order.getStr("order_no");
                String qr_code = order.getStr("qr_code");
                String part_no = order.getStr("part_no");
                
                
                Record goo = Db.findFirst("select * from gate_out_order where order_no = ?",order_no);
                if(goo != null){
                	Long out_order_id = goo.getLong("id");
                	GateIn gi = GateIn.dao.findFirst("SELECT"
    						+"	gi.*"
    						+"  FROM"
    						+"	gate_in gi"
    						+"  WHERE"
    						+"	gi.out_order_id =? and gi.qr_code = ? and gi.out_flag='N'"
    						+"  GROUP BY"
    						+"	gi.id",out_order_id,qr_code);
                    
                    if(gi==null){//查无此qr_code后，再查有没产品
                    	GateIn gi2 = GateIn.dao.findFirst("SELECT"
        						+"	gi.*"
        						+"  FROM"
        						+"	gate_in gi"
        						+"  WHERE"
        						+"	gi.out_order_id =? and gi.part_no = ? and gi.out_flag='N'"
        						+"  GROUP BY"
        						+"	gi.id",out_order_id,part_no);
                    	if(gi2==null){
                    		//说明拿错货品了
                    		order.set("import_msg", order_no+"里面没有此货品");
                    	}else{
                    		//货品对了，只是不是出库单里面的货品，要更新
                    		gi2.set("out_order_id", out_order_id).update();
                    		
                    		//把原来的替换为空
                    		GateIn re = GateIn.dao.findFirst("select * from gate_in where out_order_id = ? and out_flag='N' and part_no = ?",out_order_id,part_no);
                    		if(re!=null)
                    			re.set("out_order_id", null).update();
                    	}
                    }
                }else{
                	//不存在此出库单
                	throw new Exception("出库单号不存在");
                }
                
                order.set("office_id", officeId);
                order.set("date_no", c);
                order.save();
                rowNumber++;
            }
            
            conn.commit();
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！<br/><br/>"+repeatMsg);
		} catch (Exception e) {
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			result.set("result", false);
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber+1)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");

		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		return result;
	}
	
	
	/**
	 * 盘点单开始导入
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importInvCheckValue(CSVReader csvReader, long officeId) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);
		int rowNumber = 0;
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			String[] csvRow = null;//row  
            String[] title = null;
            String order_no = null;
            while ((csvRow = csvReader.readNext()) != null){    
            	if(rowNumber == 0){
            		title = csvRow;
            		rowNumber++;
            		continue;
            	}
            	InvCheckOrder order = new InvCheckOrder();
                for (int i =0; i<csvRow.length; i++){
                	String titleName = StringUtils.isNotBlank(title[i])?title[i].trim():null;
                	String value = StringUtils.isNotBlank(csvRow[i])?csvRow[i].trim():null;;
                	if(StringUtils.isNotBlank(value)){
                		if(!"id".equals(titleName) && !"creator".equals(titleName)){
		                    order.set(titleName, value);
	                	} else if("creator".equals(titleName)){
	                		UserLogin ul = UserLogin.dao.findFirst("select * from user_login where c_name = ?",value);
	                		if(ul != null)
	                			order.set(titleName, ul.getLong("id"));
	                		order.set("creator_code", value);
	                	} 
                	}
                }    
                
                GateIn gi = GateIn.dao.findFirst("select * from gate_in where qr_code = ? ",order.getStr("qr_code"));
                if(gi == null){
        			gi = new GateIn();
        			gi.set("office_id", officeId);
        			gi.set("qr_code", order.getStr("qr_code"));
        			gi.set("part_no", order.getStr("part_no"));
        			gi.set("quantity", order.getStr("quantity"));
        			gi.set("shelves", order.getStr("shelves"));
        			gi.set("creator", order.getLong("creator"));
        			gi.set("create_time", order.get("create_time"));
        			gi.set("inv_flag", "Y");
        			gi.set("inv_msg", "盘点单号："+order.getStr("order_no")+",盘点入库");
        			gi.save();
        		} else {
        			String this_shelves = order.getStr("shelves");
        			String order_shelves = gi.getStr("shelves");
        			if(!order_shelves.equals(this_shelves)){
        				gi.set("inv_msg", "盘点单号："+order.getStr("order_no")+","+order_shelves+"调整为"+this_shelves);
        				gi.set("shelves", this_shelves);
        				gi.set("inv_flag", "Y");
        				gi.update();
        			}
        		}
            	
                order.set("office_id", officeId);
                order.save();
                rowNumber++;
                
                order_no = order.getStr("order_no");    
            }
            
            //过滤掉库存中不存在的货品（多出来的货品）
            List<Record> invList = Db.find("SELECT shelves,GROUP_CONCAT(qr_code SEPARATOR ',') qr_codes,count(qr_code) amount FROM `inv_check_order` where order_no = ? GROUP BY shelves",order_no);
            List<Record> gateInList = Db.find("SELECT GROUP_CONCAT(qr_code SEPARATOR ',') qr_codes,shelves,count(qr_code) amount FROM `gate_in` where out_flag='N' and error_flag='N'"
        			+ "  GROUP BY shelves");
            for(Record invR : invList){
            	String invShelves = invR.getStr("shelves");
            	Long invQuantity = invR.getLong("amount");
            	
            	for(Record giR : gateInList){
            		String giShelves = giR.getStr("shelves");
                	Long giQuantity = giR.getLong("amount");
                	
                	if(invShelves.equals(giShelves)){
                		if(invQuantity != giQuantity){
                			String inv_qrCode = invR.getStr("qr_codes");
                			String [] invArray = inv_qrCode.split(",");
                			String gi_qrCode = giR.getStr("qr_codes");
                			String [] giArray = gi_qrCode.split(",");
                			
                			for (int i = 0; i < giArray.length; i++) {
                				if(!useList(invArray,giArray[i])){
                					String qr_code = giArray[i];//多出来的
            						Db.update("update gate_in set inv_flag = 'Y',out_flag='Y', inv_msg = '盘点单号:"+order_no+",盘点出库' where qr_code = ?",qr_code);
                				}
							}
                		}
                	}
            	}
            }
            
            
			result.set("cause","成功导入( "+(rowNumber-1)+" )条数据！<br/>");
			conn.commit();
		} catch (Exception e) {
			try {
				if (null != conn)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
            
			result.set("result", false);
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber)
						+ "行时出现异常:" + e.getMessage() + "<br/>导入数据已取消！");
			//throw new ActiveRecordException(e);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				DbKit.getConfig().removeThreadLocalConnection();
			}
		}
		return result;
	}
	
	public static boolean useList(String[] arr, String targetValue) {
	    return Arrays.asList(arr).contains(targetValue);
	}   
}
