package controllers.tms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.ParentOfficeModel;
import models.Party;
import models.UserLogin;
import models.eeda.tms.TransJobOrder;
import models.eeda.tms.TransJobOrderArap;
import models.eeda.tms.TransJobOrderLandItem;
import models.yh.profile.Carinfo;

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

import controllers.profile.LoginUserController;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TransOrderShortCutController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(TransOrderShortCutController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		
		render("/tms/TransJobOrder/transOrderShortCut.html");
	}
	
	@SuppressWarnings("null")
	@Before(EedaMenuInterceptor.class)
    public void create() throws Exception {
		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String id = (String) dto.get("id");
        String indexs="";
		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("itemList");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//转换后的格式
        
		
		TransJobOrderController tjc=new TransJobOrderController();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		for(int i=0;i<itemList.size();i++){
   			TransJobOrder transJobOrder=new TransJobOrder();
//   			//create 
//			DbUtils.setModelValues(dto, transJobOrder);
   			Map<String, String> itemMap=itemList.get(i);
   			String jobOrderDate = null;
   			if(itemMap.get("CABINET_DATE")!=null){
   				Date date = sdf.parse(itemMap.get("CABINET_DATE"));
   				jobOrderDate = sdf.format(date).toString();
   			}else{
   				jobOrderDate = sdf.format(new Date()).toString();
   			}
   			
   			
			//需后台处理的字段
   			String order_no = OrderNoGenerator.getNextOrderNo("HT", office_id);
   			StringBuilder sb = new StringBuilder(order_no);//构造一个StringBuilder对象
   			sb.replace(2, 5, jobOrderDate);
   			order_no =sb.toString();
   			String customer_id =itemMap.get("CUSTOMER_ID");
   			String cabinet_type=itemMap.get("cabinet_type");
   			String take_wharf=itemMap.get("TAKE_WHARF");
   			String back_wharf=itemMap.get("BACK_WHARF");
   			String loading_wharf1=itemMap.get("LOADING_WHARF1");
   			String loading_wharf2=itemMap.get("LOADING_WHARF2");
			transJobOrder.set("customer_id", itemMap.get("CUSTOMER_ID"));
			if(StringUtils.isNotEmpty(itemMap.get("CHARGE_TIME"))){
				transJobOrder.set("charge_time", itemMap.get("CHARGE_TIME"));
			}
			
			if(StringUtils.isNotEmpty(itemMap.get("HEAD_CARRIER"))){
				transJobOrder.set("head_carrier", itemMap.get("HEAD_CARRIER"));
			}			
			transJobOrder.set("type", itemMap.get("type"));
			transJobOrder.set("status", "新建");
			transJobOrder.set("remark", itemMap.get("remark"));
			transJobOrder.set("container_no", itemMap.get("container_no"));
			transJobOrder.set("so_no", itemMap.get("so_no"));
			transJobOrder.set("lading_no", itemMap.get("lading_no"));
			transJobOrder.set("seal_no", itemMap.get("seal_no"));
			transJobOrder.set("cabinet_type", itemMap.get("cabinet_type"));
			transJobOrder.set("trade_type", itemMap.get("trade_type"));
			if(StringUtils.isNotEmpty(itemMap.get("TAKE_WHARF"))){
				transJobOrder.set("take_wharf", itemMap.get("TAKE_WHARF"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("BACK_WHARF"))){
				transJobOrder.set("back_wharf", itemMap.get("BACK_WHARF"));
			}

			transJobOrder.set("cross_border_travel", itemMap.get("CROSS_BORDER_TRAVEL"));
			transJobOrder.set("customer_salesman", itemMap.get("customer_salesman"));
			transJobOrder.set("contract_no", itemMap.get("contract_no"));
			transJobOrder.set("toca_no", itemMap.get("toca_no"));
			transJobOrder.set("remark", itemMap.get("remark"));
			transJobOrder.set("transport_type", "land");
			transJobOrder.set("order_no", order_no);
			transJobOrder.set("creator", user.getLong("id"));
			transJobOrder.set("create_stamp", new Date());
			transJobOrder.set("office_id", office_id);
			transJobOrder.save();
			id = transJobOrder.getLong("id").toString();
			indexs+=itemMap.get("index").toString()+',';
			System.out.println("test: "+ id);
			//获取结算公司id：charge_company_id
			Party party = Party.dao.findById(customer_id);		
			String charge_company_id = null;
			if(party.get("charge_company_id")!=null){
				charge_company_id=party.get("charge_company_id").toString();
			}else{
				charge_company_id=customer_id;
			}
			
			//调用生成合同费用方法
			if(StringUtils.isNotEmpty(itemMap.get("freight"))){
				Record reFee = Db.findFirst("SELECT	f.* FROM 	fin_item f 	WHERE	f.office_id = ? and f.name = '运费'",office_id);
				String price = itemMap.get("freight");
				String charge_id = reFee.getLong("id").toString();//费用名称id
				transJobArapSave(id,charge_company_id,price,charge_id);
			}else{
				checkCustomerQuotation(office_id,id,customer_id,charge_company_id,cabinet_type,take_wharf,back_wharf,loading_wharf1,loading_wharf2);
			}
			
			
			//费用保存
			
			//陆运 SHOUZHONGGUI_CAR_NO	TIJIGUI_CAR_NO
			
			if(StringUtils.isNotEmpty(itemMap.get("WHOLE_COURSE_CAR_NO"))){
				TransJobOrderLandItem tjol=new TransJobOrderLandItem();
				tjol.set("order_id", id);
				tjol.set("unload_type", "全程");
				tjol.set("item_type", "shipment");
				tjol.set("status", "待发车");
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("WHOLE_COURSE_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
				if(StringUtils.isNotEmpty(itemMap.get("CABINET_DATE"))){
					tjol.set("cabinet_date", itemMap.get("CABINET_DATE"));
				}
				if(StringUtils.isNotEmpty(itemMap.get("CLOSING_DATE"))){
					tjol.set("closing_date", itemMap.get("CLOSING_DATE"));
				}
				tjol.set("loading_platform", itemMap.get("loading_platform"));
				 loading_wharf1=itemMap.get("LOADING_WHARF1");
				if(!"".equals(loading_wharf1)){
					tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				}
				
				loading_wharf2=itemMap.get("LOADING_WHARF2");
				if(!"".equals(loading_wharf2)){
					tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
				}
				tjol.save();
			}
			
			
			if(StringUtils.isNotEmpty(itemMap.get("TIJIGUI_CAR_NO"))){
				TransJobOrderLandItem tjol=new TransJobOrderLandItem();
				tjol.set("order_id", id);
				tjol.set("unload_type", "提吉柜");
				tjol.set("item_type", "shipment");
				tjol.set("status", "待发车");
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("TIJIGUI_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
				tjol.set("take_wharf", itemMap.get("TAKE_WHARF"));
				if(StringUtils.isNotEmpty(itemMap.get("CABINET_DATE"))){
					tjol.set("cabinet_date", itemMap.get("CABINET_DATE"));
				}
				tjol.set("loading_platform", itemMap.get("loading_platform"));
				 loading_wharf1=itemMap.get("LOADING_WHARF1");
				if(!"".equals(loading_wharf1)){
					tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				}
				
				loading_wharf2=itemMap.get("LOADING_WHARF2");
				if(!"".equals(loading_wharf2)){
					tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
				}
				tjol.save();
			}
			if(StringUtils.isNotEmpty(itemMap.get("YIGUI_CAR_NO"))){
				TransJobOrderLandItem tjol=new TransJobOrderLandItem();
				tjol.set("order_id", id);
				tjol.set("unload_type", "移柜");
				tjol.set("item_type", "shipment");
				tjol.set("status", "待发车");
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("YIGUI_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
			    loading_wharf1=itemMap.get("LOADING_WHARF1");
				if(!"".equals(loading_wharf1)){
					tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				}
				
				loading_wharf2=itemMap.get("LOADING_WHARF2");
				if(!"".equals(loading_wharf2)){
					tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
				}
				tjol.save();
			}
			if(StringUtils.isNotEmpty(itemMap.get("SHOUZHONGGUI_CAR_NO"))){
				TransJobOrderLandItem tjol=new TransJobOrderLandItem();
				tjol.set("order_id", id);
				tjol.set("unload_type", "收重柜");
				tjol.set("item_type", "shipment");
				tjol.set("status", "待发车");
				if(StringUtils.isNotEmpty(itemMap.get("CLOSING_DATE"))){
					tjol.set("closing_date", itemMap.get("CLOSING_DATE"));
				}
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("SHOUZHONGGUI_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
				tjol.set("back_wharf", itemMap.get("BACK_WHARF"));
			    loading_wharf1=itemMap.get("LOADING_WHARF1");
				if(!"".equals(loading_wharf1)){
					tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				}
				
				loading_wharf2=itemMap.get("LOADING_WHARF2");
				if(!"".equals(loading_wharf2)){
					tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
				}
				tjol.save();
			}
			
			//保存费用
			Map<String, String> valueMap = new HashMap<String,String>();
			if(StringUtils.isNotEmpty(itemMap.get("high_speed_fee"))){
				valueMap.put("高速费", itemMap.get("high_speed_fee"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("call_fee"))){
				valueMap.put("打单费", itemMap.get("call_fee"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("night_fee"))){
				valueMap.put("压夜费", itemMap.get("night_fee"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("weighing_fee"))){
				valueMap.put("过磅费", itemMap.get("weighing_fee"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("ji_jinji_out_fee"))){
				valueMap.put("吉进吉出费", itemMap.get("ji_jinji_out_fee"));
			}
			if(StringUtils.isNotEmpty(itemMap.get("advance_fee"))){
				valueMap.put("代垫费", itemMap.get("advance_fee"));
			}
			if(valueMap!=null){
				for (Entry<String, String> entry : valueMap.entrySet()) {
					if(StringUtils.isNotEmpty(entry.getValue())){
						Record reFee = Db.findFirst("SELECT	f.* FROM 	fin_item f 	WHERE	f.office_id = ? and f.name = '"+entry.getKey()+"'",office_id);
						String price = entry.getValue();
						String charge_id = reFee.getLong("id").toString();//费用名称id
						transJobArapSave(id,charge_company_id,price,charge_id);
					}
				}
			}
   		}

   		Record r =new Record();
   		r.set("indexs",indexs);
   		renderJson(r);
    }
	@Before(Tx.class)
	 public static void checkCustomerQuotation(long office_id,String order_id,String customer_id,String charge_company_id,String cabinet_type,String take_wharf,String back_wharf,
			 String loading_wharf1,String loading_wharf2) {
		 	
		 
				String	takeWharf=" and pq.take_wharf is null";
				String	backWharf=" and pq.back_wharf is null";
				String	loadingWharf1=" and pq.loading_wharf1 is null";
				String	loadingWharf2=" and pq.loading_wharf2 is null";
		 		if(!"".equals(take_wharf)){
		 				takeWharf=" and pq.take_wharf= "+take_wharf;
		        }

		        if(!"".equals(back_wharf)){
		        		backWharf=" and pq.back_wharf= "+back_wharf;
		        }
		        
		        if(!"".equals(loading_wharf1)){
		        		loadingWharf1=" and pq.loading_wharf1= "+loading_wharf1;
		        }
		        
		        if(!"".equals(loading_wharf2)){
		        		loadingWharf2=" and pq.loading_wharf2= "+loading_wharf2;
		        }
		        String charge_id="";
		        Record fin_itemId = Db.findFirst("SELECT id from fin_item where  `name`='运费' and office_id = "+office_id);
		        if(fin_itemId!=null){
		        	charge_id =fin_itemId.getLong("id").toString();
		        }
		        
		        
		        Record rer= new Record();
		        
		        String sqlRer = "SELECT * FROM trans_job_contract_relation pq "
		        		+ " WHERE order_id="+order_id
		        		+ " and sp_id ="+charge_company_id
		        		+" and pq.truck_type= '"+cabinet_type+"'"
		        		+ takeWharf
						+ backWharf
						+ loadingWharf1
						+ loadingWharf2;        
		        rer=Db.findFirst(sqlRer);
		       
				String sqlString="SELECT A.*,c.name currency_name from( SELECT pq.* FROM party_quotation pq "
						+" WHERE pq.party_id = "+customer_id
						+" and pq.truck_type= '"+cabinet_type+"'"
						+takeWharf
						+backWharf
						+loadingWharf1
						+loadingWharf2
						+" )A left join currency c on c.id=A.currency_id";
				Record records=Db.findFirst(sqlString);
				Double freight=null;
				
				if("".equals(charge_id)&&records!=null){
					records.set("charge_id", charge_id);
				}
				if(records!=null){
					freight= records.get("price_tax");
					Db.update("update party_quotation set charge_id = ? where id = ?",charge_id,records.getLong("id"));
				}
		   		Record r =new Record();
//		   		r.set("ids",ids);
		   		
		   		
		   		Record rerOrderId= new Record();
		        
		        String sqlOrderId = "SELECT * FROM trans_job_contract_relation pq "
		        		+ " WHERE order_id="+order_id
		        		+ " and sp_id ="+charge_company_id;
		        rerOrderId = Db.findFirst(sqlOrderId);
		        if(rerOrderId!=null&&rer==null){
		        	long Arap_id = rerOrderId.get("arap_id");
		        	long contractId =rerOrderId.get("id");
		        	Db.deleteById("trans_job_order_arap", Arap_id);
		        	Db.deleteById("trans_job_contract_relation", contractId);
		        }
		   	 if(rer==null){
		   		//从客户合同中拿出合同费用
		   		if(freight!=null&&!"".equals(freight)){
					Record rec = new Record();
					rec.set("order_id", order_id);
					rec.set("order_type", "charge");
					rec.set("type", "陆运");
					rec.set("sp_id", charge_company_id);
					rec.set("charge_id", charge_id);
					rec.set("price", freight);
					rec.set("amount", 1);
					rec.set("unit_id", 33);
					rec.set("currency_id", 3);
					rec.set("total_amount", freight); 
					rec.set("exchange_rate", 1);
					rec.set("currency_total_amount", freight);
					if(records.get("street_vehicle_freight")!=null){
						rec.set("street_vehicle_freight", records.get("street_vehicle_freight"));
					}
					
					
					Db.save("trans_job_order_arap",rec);
					long arap_id = rec.getLong("id");
					Record record = new Record();
					record.set("order_id", order_id);
					record.set("arap_charge_id", charge_id);
					record.set("sp_id", charge_company_id);
					
					if(!"".equals(take_wharf)){
						record.set("take_wharf", take_wharf);
					}
					if(!"".equals(back_wharf)){
						record.set("back_wharf", back_wharf);
					}
					
					if(!"".equals(loading_wharf1)){
						record.set("loading_wharf1", loading_wharf1);
					}
					if(!"".equals(loading_wharf2)){
						record.set("loading_wharf2", loading_wharf2);
					}					
					if(!"".equals(cabinet_type)){
						record.set("truck_type", cabinet_type);
					}
					record.set("total_amount", freight);
					record.set("arap_id", arap_id);
					Db.save("trans_job_contract_relation",record);
				}
		 		
		 	}
	    }
	
	public static void transJobArapSave(String job_order_id,String charge_company_id,String charge_id,String price){
		TransJobOrderArap transJobArap = TransJobOrderArap.dao.findFirst("select * from trans_job_order_arap  "
				+ "where order_id = ? and order_type = 'charge' and charge_id = ?",job_order_id,charge_id);
		Record reUnit = Db.findFirst("select * from unit WHERE type = 'charge' and name = 'B/L' ");
		String unit_id = reUnit.getLong("id").toString();//费用名称id
		Record reCurrency = Db.findFirst("select * from currency WHERE  name = 'CNY' ");
		String currency_id = reCurrency.getLong("id").toString();//币制名称id
		if(transJobArap==null){	    			    		
			transJobArap =new TransJobOrderArap();
			transJobArap.set("order_id", job_order_id);
			transJobArap.set("sp_id", charge_company_id);
    		transJobArap.set("type", "陆运");
    		transJobArap.set("order_type", "cost");
    		transJobArap.set("charge_id", charge_id);
    		transJobArap.set("price", price);
    		transJobArap.set("amount", 1);
    		transJobArap.set("unit_id", unit_id);
    		transJobArap.set("total_amount", price);
    		transJobArap.set("currency_id", currency_id);
    		transJobArap.set("exchange_rate", 1);
    		transJobArap.set("currency_total_amount", price);
    		transJobArap.save();
		}else{
			transJobArap.set("price", price);
			transJobArap.set("total_amount", 1);
			transJobArap.set("currency_total_amount", price);
			transJobArap.update();
		}
	}
   

}
