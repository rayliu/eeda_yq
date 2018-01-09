package controllers.importOrder;

import interceptor.SetAttrLoginUserInterceptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Product;
import models.eeda.oms.SalesOrder;
import models.eeda.profile.Unit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.OrderNoGenerator;

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
	 * 邮箱校验
	 */
	public static boolean checkEmail (String email){
		boolean flag = true;
		if (!email.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) {
			flag = false;
	    }
		return flag;
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
	 * upc条码校验
	 * @param value
	 * @return
	 */
	public static boolean checkUpc(String value){
		boolean flag = true;
		Product p = Product.dao.findFirst("select * from product where serial_no = ?",value);
		if(p == null){
			flag = false;  
		}
	    return flag;
	}
	

	
	/**
	 * 订单编号重复校验
	 */
	public static boolean checkRefOrderNo (String value){
		boolean flag = true;
		
		SalesOrder so = SalesOrder.dao.findFirst("select * from sales_order where order_no = ?",value);
		if(so != null){
			flag = false;
		}
	    return flag;
	}
	
	/**
	 * 单位校验
	 * @param value
	 * @return
	 */
	public static boolean checkUnit (String value){
		boolean flag = true;
		
		Unit goo = Unit.dao.findFirst("select * from unit where name = ?",value);
		if(goo == null){
			flag = false;
		}
	    return flag;
	}
	
	/**
	 * 单位Code校验
	 * @param value
	 * @return
	 */
	public static boolean checkUnitCode (String value){
		boolean flag = true;
		
		Unit goo = Unit.dao.findFirst("select * from unit where code = ?",value);
		if(goo == null){
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
	 *  收货人（转化后的）地区编码
	 * @param value
	 * @return
	 */
	public boolean checkLocation (String value){
		boolean flag = true;
		if(value.length()<20){
			flag = false;
		}
	    return flag;
	}



	
	
	/**
	 * 数据校验
	 * @param lines
	 * @return
	 */
	@Before(Tx.class)
	public Record importTJCheck( List<Map<String, String>> lines, long office_id) {
		Record result = new Record();
		result.set("result",true);
		//if ("true".equals(importResult.get("result"))) {
			int rowNumber = 2;//最左边的编码行
			String error_msg = "";
			try {
				for (Map<String, String> line :lines) {
					if(line != null){
					String customer_name = line.get("客户简称").trim();
					String type = line.get("类型").trim();
					String container_no = line.get("柜号").trim();
					String so_no = line.get("SO号").trim();
					String cabinet_type = line.get("柜型").trim();
					String head_carrier_name = line.get("头程船公司").trim();
					String lading_no = line.get("提单号").trim();
					String customer_salesman = line.get("客户业务员").trim();
					String contract_no = line.get("合同号").trim();
					String toca_no = line.get("拖卡号").trim();
					String cross_border_travel_name = line.get("跨境").trim();
					String take_wharf_name = line.get("提柜码头").trim();
					String back_wharf_name = line.get("还柜码头").trim();
					String charge_time = line.get("结算时间").trim();
					String loading_wharf1_name = line.get("装货地点1").trim();
					String loading_wharf2_name = line.get("装货地点2").trim();
					String loading_platform = line.get("装货平台").trim();
					String cabinet_date = line.get("提柜/货日期").trim();
					String closing_date = line.get("收柜日期").trim();
					String tj_car_no = line.get("提吉车牌（含 拖卡重量、拖头重量、司机、司机电话）").trim();
					String all_car_no = line.get("全程车牌").trim();
					String yg_car_no = line.get("移柜车牌").trim();
					String sz_car_no = line.get("收重车牌").trim();
					String trans_fee = line.get("运费").trim();
					String call_fee = line.get("打单费").trim();
					String weighing_fee = line.get("过磅费").trim();
					String high_speed_fee = line.get("高速费").trim();
					String remark = line.get("备注").trim();
					
					if(StringUtils.isNotBlank(customer_name)){
						Record customer = Db.findFirst("select * from party where abbr = ?  and type = 'CUSTOMER'  and office_id = ?",customer_name,office_id);
			   			if(customer == null){
			   				error_msg += "第"+rowNumber+"行【客户简称】，系统无此数据，请核对是否有误<br/>";
			   			}
					}else{
						error_msg += "第"+rowNumber+"行【客户简称】，字段不能为空<br/>";
					}
					
					if(StringUtils.isNotBlank(head_carrier_name)){
						Record carrier = Db.findFirst("select * from party where abbr = ? and type = 'SP'  and office_id = ?",head_carrier_name,office_id);
			   			if(carrier == null){
			   				error_msg += "第"+rowNumber+"行【头程船公司】，系统无此数据，请核对是否有误<br/>";
			   			}
					}else{
						error_msg += "第"+rowNumber+"行【头程船公司】，字段不能为空<br/>";
					}
		   			
					if(StringUtils.isNotBlank(back_wharf_name)){
			   			Record bf = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",back_wharf_name,office_id);
			   			if(bf == null){
			   				error_msg += "第"+rowNumber+"行【还柜码头】，系统无此数据，请核对是否有误<br/>";
			   			}
					}else{
						error_msg += "第"+rowNumber+"行【还柜码头】，字段不能为空<br/>";
					}
		   			
					if(StringUtils.isNotBlank(take_wharf_name)){
						Record tw = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",take_wharf_name,office_id);
			   			if(tw == null){
			   				error_msg += "第"+rowNumber+"行【提柜码头】，系统无此数据，请核对是否有误<br/>";
			   			}
					}else{
						error_msg += "第"+rowNumber+"行【提柜码头】，字段不能为空<br/>";
					}
		   			
					if(StringUtils.isNotBlank(cross_border_travel_name)){
			   			Record cbt = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",cross_border_travel_name,office_id);
			   			if(cbt == null){
			   				error_msg += "第"+rowNumber+"行【跨境】，系统无此数据，请核对是否有误<br/>";
			   			}
					}

					if(StringUtils.isNotBlank(loading_wharf1_name)){
			   			Record lw1 = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",loading_wharf1_name,office_id);
			   			if(lw1 == null){
			   				error_msg += "第"+rowNumber+"行【装货地点1】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
		   			
					if(StringUtils.isNotBlank(loading_wharf2_name)){
			   			Record lw2 = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",loading_wharf2_name,office_id);
			   			if(lw2 == null){
			   				error_msg += "第"+rowNumber+"行【装货地点2】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
					
					if(StringUtils.isNotBlank(tj_car_no)){
						Record car = Db.findFirst("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
								+ " where c.type='OWN' and c.office_id = ? and c.car_no = ? ", office_id,tj_car_no);
			   			if(car == null){
			   				error_msg += "第"+rowNumber+"行【提吉车牌（含 拖卡重量、拖头重量、司机、司机电话）】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
					
					if(StringUtils.isNotBlank(all_car_no)){
						Record car = Db.findFirst("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
								+ " where c.type='OWN' and c.office_id = ? and c.car_no = ? ", office_id,all_car_no);
			   			if(car == null){
			   				error_msg += "第"+rowNumber+"行【全程车牌】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
					
					if(StringUtils.isNotBlank(yg_car_no)){
						Record car = Db.findFirst("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
								+ " where c.type='OWN' and c.office_id = ? and c.car_no = ? ", office_id,yg_car_no);
			   			if(car == null){
			   				error_msg += "第"+rowNumber+"行【移柜车牌】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
					
					if(StringUtils.isNotBlank(sz_car_no)){
						Record car = Db.findFirst("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
								+ " where c.type='OWN' and c.office_id = ? and c.car_no = ? ", office_id,sz_car_no);
			   			if(car == null){
			   				error_msg += "第"+rowNumber+"行【收重车牌】，系统无此数据，请核对是否有误<br/>";
			   			}
					}
		   			
					if(StringUtils.isNotBlank(charge_time)){
						if(!checkDate(charge_time)){
							error_msg += "第"+rowNumber+"行【结算时间】，日期格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(cabinet_date)){
						if(!checkDate(cabinet_date)){
							error_msg += "第"+rowNumber+"行【提柜/货日期】，日期格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(closing_date)){
						if(!checkDate(closing_date)){
							error_msg += "第"+rowNumber+"行【收柜日期】，日期格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(trans_fee)){
						if(!checkDouble(trans_fee)){
							error_msg += "第"+rowNumber+"行【运费】，数字格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(call_fee)){
						if(!checkDouble(call_fee)){
							error_msg += "第"+rowNumber+"行【打单费】，数字格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(weighing_fee)){
						if(!checkDouble(weighing_fee)){
							error_msg += "第"+rowNumber+"行【过磅费】，数字格式不正确<br/>";
						}
					}
					
					if(StringUtils.isNotBlank(high_speed_fee)){
						if(!checkDouble(high_speed_fee)){
							error_msg += "第"+rowNumber+"行【高速费】，数字格式不正确<br/>";
						}
					}
					rowNumber++;
					}
				}
				
				if(StringUtils.isNotBlank(error_msg)){
					throw new Exception();
				}
			} catch (Exception e) {
				result.set("result", false);
				result.set("cause", error_msg);
				return result;
			} 
		return result;
	}
	
	
	
	@Before(Tx.class)
	public Record importTJValue( List<Map<String, String>> lines, long user_id, long office_id) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);

		int rowNumber = 1;
		
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			for (Map<String, String> line :lines) {
				if(line != null){
				String customer_name = line.get("客户简称").trim();
				String type = line.get("类型").trim();
				String container_no = line.get("柜号").trim();
				String so_no = line.get("SO号").trim();
				String cabinet_type = line.get("柜型").trim();
				String head_carrier_name = line.get("头程船公司").trim();
				String lading_no = line.get("提单号").trim();
				String customer_salesman = line.get("客户业务员").trim();
				String contract_no = line.get("合同号").trim();
				String toca_no = line.get("拖卡号").trim();
				String cross_border_travel_name = line.get("跨境").trim();
				//String  unload_type = line.get("提柜类型").trim();
				String take_wharf_name = line.get("提柜码头").trim();
				String back_wharf_name = line.get("还柜码头").trim();
				String charge_time = line.get("结算时间").trim();
				
				String loading_wharf1_name = line.get("装货地点1").trim();
				String loading_wharf2_name = line.get("装货地点2").trim();
				String loading_platform = line.get("装货平台").trim();
				
				String cabinet_date = line.get("提柜/货日期").trim();
				String closing_date = line.get("收柜日期").trim();
				String tj_car_no = line.get("提吉车牌（含 拖卡重量、拖头重量、司机、司机电话）").trim();
				String all_car_no = line.get("全程车牌").trim();
				String yg_car_no = line.get("移柜车牌").trim();
				String sz_car_no = line.get("收重车牌").trim();
				String trans_fee = line.get("运费").trim();
				String call_fee = line.get("打单费").trim();
				String weighing_fee = line.get("过磅费").trim();
				String high_speed_fee = line.get("高速费").trim();
				String remark = line.get("备注").trim();
				
				
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//转换后的格式
		        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyyMMdd");//分析日期
				Date date = sdf.parse(cabinet_date);
				String jobOrderDate = parseFormat.format(date).toString();
				
				Record order = new Record();
	   			String order_no = OrderNoGenerator.getNextOrderNo("HT", office_id);
	   			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
	   			sb.replace(2, 5, jobOrderDate);
	   			order_no =sb.toString();
	   			Long customer_id = null;
	   			Record customer = Db.findFirst("select * from party where abbr = ?  and type = 'CUSTOMER'  and office_id = ?",customer_name,office_id);
	   			if(customer != null){
	   				customer_id = customer.getLong("id");
	   				order.set("customer_id", customer_id);
	   			}
	   			
	   			Long head_carrier = null;
	   			Record carrier = Db.findFirst("select * from party where abbr = ? and type = 'SP'  and office_id = ?",head_carrier_name,office_id);
	   			if(carrier != null){
	   				head_carrier = carrier.getLong("id");
	   			}
	   			
	   			Long back_wharf = null;
	   			Record bf = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",back_wharf_name,office_id);
	   			if(bf != null){
	   				back_wharf = bf.getLong("id");
	   				order.set("head_carrier", head_carrier);
	   			}
	   			
	   			Long take_wharf = null;
	   			Record tw = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",take_wharf_name,office_id);
	   			if(tw != null){
	   				take_wharf = tw.getLong("id");
	   				order.set("take_wharf", take_wharf);
	   			}
	   			
	   			Long cross_border_travel = null;
	   			Record cbt = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",cross_border_travel_name,office_id);
	   			if(cbt != null){
	   				if(tw != null){
	   					cross_border_travel = tw.getLong("id");
	   				}
	   				order.set("cross_border_travel", cross_border_travel);
	   			}
	   			
	   			order.set("order_no", order_no);
	   			order.set("type", type);
	   			if(StringUtils.isBlank(container_no)){
	   				container_no = "无";
	   			}
	   			order.set("container_no", container_no);
	   			if(StringUtils.isBlank(so_no)){
	   				so_no = "无";
	   			}
	   			order.set("so_no", so_no);
	   			order.set("cabinet_type", cabinet_type);
	   			order.set("lading_no", lading_no);
	   			order.set("customer_salesman", customer_salesman);
	   			order.set("contract_no", contract_no);
	   			order.set("toca_no", toca_no);
	   			order.set("back_wharf", back_wharf);
	   			if(StringUtils.isNotBlank(charge_time)){
	   				order.set("charge_time", charge_time);
	   			}
	   			order.set("back_wharf", back_wharf);
	   			
	   			order.set("creator", user_id);
	   			order.set("trade_type", "FOB");
	   			order.set("transport_type", "land");
	   			order.set("create_stamp", new Date());
	   			order.set("office_id", office_id);
	   			order.set("remark", remark);
	   			Db.save("trans_job_order", order);
	   			
	   			Long order_id = order.getLong("id");
	   			List list = new ArrayList();
				if(StringUtils.isNotBlank(tj_car_no)){
					Record map = new Record();
		   			map.set("car_no", tj_car_no);
		   			map.set("unload_type", "提吉柜");
		   			list.add(map);
	   			}
				if(StringUtils.isNotBlank(all_car_no)){
					Record map = new Record();
		   			map.set("car_no", all_car_no);
		   			map.set("unload_type", "全程");
		   			list.add(map);
	   			}
				if(StringUtils.isNotBlank(yg_car_no)){
					Record map = new Record();
		   			map.set("car_no", yg_car_no);
		   			map.set("unload_type", "移柜");
		   			list.add(map);
	   			}
				if(StringUtils.isNotBlank(sz_car_no)){
					Record map = new Record();
		   			map.set("car_no", sz_car_no);
		   			map.set("unload_type", "收重柜");
		   			list.add(map);
	   			}
				
				

				for (int i = 0; i < list.size(); i++) {
					String car_no_name = ((Record)list.get(i)).getStr("car_no");
					String unload_type = ((Record)list.get(i)).getStr("unload_type");
					Record item = new Record();
					Long loading_wharf1 = null;
		   			Record lw1 = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",loading_wharf1_name,office_id);
		   			if(lw1 != null){
		   				loading_wharf1 = lw1.getLong("id");
		   				item.set("loading_wharf1", loading_wharf1);//searchLoading
		   			}
		   			Long loading_wharf2 = null;
		   			Record lw2 = Db.findFirst("select p.* from dockinfo p where p.dock_name = ? and  p.office_id=?",loading_wharf2_name,office_id);
		   			if(lw2 != null){
		   				loading_wharf2 = lw2.getLong("id");
		   				item.set("loading_wharf2", loading_wharf2);//searchLoading
		   			}
		   			
					item.set("loading_platform", loading_platform);
					
					
					if("全程".equals(unload_type)){
						if(StringUtils.isNotBlank(cabinet_date)){
							item.set("cabinet_date", cabinet_date);
						}
						
						if(StringUtils.isNotBlank(closing_date)){
							item.set("closing_date", closing_date);
						}
					}else if("提吉柜".equals(unload_type)){
						if(StringUtils.isNotBlank(cabinet_date)){
							item.set("cabinet_date", cabinet_date);
						}
					}else if("收重柜".equals(unload_type)){
						if(StringUtils.isNotBlank(closing_date)){
							item.set("closing_date", closing_date);
						}
					}
					
					//车牌带出
					Long car_no = null;
					if(StringUtils.isNotBlank(car_no_name)){
						Record car = Db.findFirst("select c.*,p.abbr sp_name from carinfo c left join party p on p.id=c.parent_id "
								+ " where c.type='OWN' and c.office_id = ? and c.car_no = ? ", office_id,car_no_name);
						
						if(car != null){
							car_no = car.getLong("id");
							item.set("car_no", car_no);
							
							//item.set("truck_type", car.get("cartye"));
							item.set("truck_type", car.get("truck_type"));
							item.set("toca_weight", car.get("toca_weight"));
							item.set("head_weight", car.get("head_weight"));
							item.set("driver", car.get("driver"));
							item.set("driver_tel", car.get("phone"));
						}
					}
					
					item.set("unload_type", unload_type);
					item.set("item_type", "shipment");
					item.set("status", "待发车");
					item.set("order_id", order_id);
					Db.save("trans_job_order_land_item", item);
				}
				
				
				//费用明细
				List arapList = new ArrayList();
				if(StringUtils.isNotBlank(trans_fee)){
					Record map = new Record();
		   			map.set("price", trans_fee);
		   			Record finItem = Db.findFirst("SELECT f.* FROM fin_item f WHERE f.office_id = ? and f.name = ?", office_id,"运费");
		   			if(finItem != null){
		   				map.set("charge_id",finItem.get("id"));
		   			}
		   			arapList.add(map);
	   			}
				if(StringUtils.isNotBlank(call_fee)){
					Record map = new Record();
		   			map.set("price", call_fee);
		   			Record finItem = Db.findFirst("SELECT f.* FROM fin_item f WHERE f.office_id = ? and f.name = ?", office_id,"打单费");
		   			if(finItem != null){
		   				map.set("charge_id",finItem.get("id"));
		   			}
		   			arapList.add(map);
	   			}
				if(StringUtils.isNotBlank(weighing_fee)){
					Record map = new Record();
		   			map.set("price", weighing_fee);
		   			Record finItem = Db.findFirst("SELECT f.* FROM fin_item f WHERE f.office_id = ? and f.name = ?", office_id,"过磅费");
		   			if(finItem != null){
		   				map.set("charge_id",finItem.get("id"));
		   			}
		   			arapList.add(map);
	   			}
				if(StringUtils.isNotBlank(high_speed_fee)){
					Record map = new Record();
		   			map.set("price", high_speed_fee);
		   			Record finItem = Db.findFirst("SELECT f.* FROM fin_item f WHERE f.office_id = ? and f.name = ?", office_id,"高速费");
		   			if(finItem != null){
		   				map.set("charge_id",finItem.get("id"));
		   			}
		   			arapList.add(map);
	   			}
				
				for (int i = 0; i < arapList.size(); i++) {
					String price = ((Record)arapList.get(i)).get("price").toString();
					Long charge_id = ((Record)arapList.get(i)).getLong("charge_id");
					
					Record arap = new Record();
					arap.set("order_type", "charge");
					arap.set("type", "陆运");
					arap.set("sp_id", customer_id);
					arap.set("charge_id", charge_id);
					arap.set("price", price);
					arap.set("amount", 1);
					arap.set("unit_id", 33);
					arap.set("currency_id", 3);
					arap.set("total_amount", price);
					arap.set("exchange_rate", 1);
					arap.set("currency_total_amount", price);
					arap.set("order_id", order_id);
					Db.save("trans_job_order_arap", arap);
					
				}
				 

				rowNumber++;
				}
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
			
			result.set("cause", "导入失败<br/>数据导入至第" + (rowNumber)
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
	
}
