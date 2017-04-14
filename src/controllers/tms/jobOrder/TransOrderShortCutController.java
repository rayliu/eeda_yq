package controllers.tms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.tms.TransJobOrder;
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
	
	@Before(EedaMenuInterceptor.class)
    public void create() {
		String jsonStr=getPara("params");
       	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String id = (String) dto.get("id");
        String ids="";
		List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get("itemList");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//转换后的格式
        String jobOrderDate = sdf.format(new Date()).toString();
		
		TransJobOrderController tjc=new TransJobOrderController();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		for(int i=0;i<itemList.size();i++){
   			TransJobOrder transJobOrder=new TransJobOrder();
//   			//create 
//			DbUtils.setModelValues(dto, transJobOrder);
   			Map<String, String> itemMap=itemList.get(i);
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
			transJobOrder.set("type", itemMap.get("type"));
			transJobOrder.set("status", "新建");
			transJobOrder.set("remark", itemMap.get("remark"));
			transJobOrder.set("container_no", itemMap.get("container_no"));
			transJobOrder.set("so_no", itemMap.get("so_no"));
			transJobOrder.set("lading_no", itemMap.get("lading_no"));
			transJobOrder.set("cabinet_type", itemMap.get("cabinet_type"));
//			transJobOrder.set("carriage_fee", itemMap.get("carriage_fee"));
//			transJobOrder.set("bill_fee", itemMap.get("bill_fee"));
//			transJobOrder.set("trans_clause", itemMap.get("trans_clause"));
			transJobOrder.set("trade_type", itemMap.get("trade_type"));
//			transJobOrder.set("land_export_date", itemMap.get("land_export_date"));
			transJobOrder.set("take_wharf", itemMap.get("TAKE_WHARF"));
			transJobOrder.set("back_wharf", itemMap.get("BACK_WHARF"));
			transJobOrder.set("remark", itemMap.get("remark"));
//			transJobOrder.set("transport_type", itemMap.get("transport_type"));
			transJobOrder.set("order_no", order_no);
			transJobOrder.set("creator", user.getLong("id"));
			if(StringUtils.isNotEmpty(itemMap.get("CREATE_STAMP"))){
				transJobOrder.set("create_stamp", itemMap.get("CREATE_STAMP"));
			}else{
				transJobOrder.set("create_stamp", new Date());
			}
			transJobOrder.set("office_id", office_id);
			transJobOrder.save();
			id = transJobOrder.getLong("id").toString();
			ids+=id+',';
			System.out.println("test: "+ id);
			
			
			//调用生成合同费用方法
			checkCustomerQuotation(office_id,id,customer_id,cabinet_type,take_wharf,back_wharf,loading_wharf1,loading_wharf2);
			
			//陆运 SHOUZHONGGUI_CAR_NO	TIJIGUI_CAR_NO	
			if(StringUtils.isNotEmpty(itemMap.get("TIJIGUI_CAR_NO"))){
				TransJobOrderLandItem tjol=new TransJobOrderLandItem();
				tjol.set("order_id", id);
				tjol.set("unload_type", "提吉柜");
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("TIJIGUI_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
				tjol.set("take_address", itemMap.get("TAKE_WHARF"));
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
				//车牌对应司机；
				Carinfo ci=Carinfo.dao.findById(itemMap.get("SHOUZHONGGUI_CAR_NO"));
				tjol.set("car_no", ci.get("id"));
				tjol.set("truck_type", ci.get("cartype"));
				tjol.set("driver", ci.get("driver"));
				tjol.set("driver_tel", ci.get("phone"));
				tjol.set("delivery_address", itemMap.get("BACK_WHARF"));
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
			
//			List<Map<String, String>> land_item = (ArrayList<Map<String, String>>)dto.get("land_list");
//			DbUtils.handleList(land_item, id, TransJobOrderLandItem.class, "order_id");
			
//			//费用明细，应收应付      CHARGE_ID    CURRENCY_ID total_amount exchange_rate  currency_total_amount
//			List<Map<String, String>> charge_list = (ArrayList<Map<String, String>>)dto.get("charge_list");
//			DbUtils.handleList(charge_list, id, TransJobOrderArap.class, "order_id");
//			List<Map<String, String>> chargeCost_list = (ArrayList<Map<String, String>>)dto.get("chargeCost_list");
//			DbUtils.handleList(chargeCost_list, id, TransJobOrderArap.class, "order_id");
			//记录结算公司使用历史	
//			saveAccoutCompanyQueryHistory(charge_list);
//			saveAccoutCompanyQueryHistory(chargeCost_list);
//			//记录结算费用使用历史  
//			saveFinItemQueryHistory(charge_list);
//			saveFinItemQueryHistory(chargeCost_list);
   		}
	
   		
   		Record r =new Record();
   		r.set("ids",ids);
   		renderJson(r);
    }
	@Before(Tx.class)
	 public static void checkCustomerQuotation(long office_id,String order_id,String customer_id,String cabinet_type,String take_wharf,String back_wharf,
			 String loading_wharf1,String loading_wharf2) {
		 	
		 
				String	takeWharf="";
				String	backWharf="";
				String	loadingWharf1="";
				String	loadingWharf2="";
		 		if(!"".equals(take_wharf)){
		 				takeWharf=" and pq.take_address= "+take_wharf;
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
		        long charge_id;
		        Record fin_itemId = Db.findFirst("SELECT id from fin_item where  `name`='运费' and office_id = "+office_id);
		        charge_id =fin_itemId.getLong("id");
		        
		        Record rer= new Record();
		        
		        String sqlRer = "SELECT * FROM trans_job_contract_relation pq "
		        		+ " WHERE order_id="+order_id
		        		+ " and sp_id ="+customer_id
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
		        		+ " and sp_id ="+customer_id;
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
					rec.set("sp_id", customer_id);
					rec.set("charge_id", charge_id);
					rec.set("price", freight);
					rec.set("amount", 1);
					rec.set("unit_id", 33);
					rec.set("currency_id", 3);
					rec.set("total_amount", freight);
					rec.set("exchange_rate", 1);
					rec.set("currency_total_amount", freight);
					
					Db.save("trans_job_order_arap",rec);
					long arap_id = rec.getLong("id");
					Record record = new Record();
					record.set("order_id", order_id);
					record.set("arap_charge_id", charge_id);
					record.set("sp_id", customer_id);
					if(!"".equals(loading_wharf1)){
						record.set("loading_wharf1", loading_wharf1);
					}
					if(!"".equals(loading_wharf2)){
						record.set("loading_wharf2", loading_wharf2);
					}
					if(!"".equals(back_wharf)){
						record.set("back_wharf", back_wharf);
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
   

}
