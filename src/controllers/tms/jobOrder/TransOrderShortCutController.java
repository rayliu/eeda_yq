package controllers.tms.jobOrder;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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
		
		TransJobOrderController tjc=new TransJobOrderController();
   		UserLogin user = LoginUserController.getLoginUser(this);
   		long office_id = user.getLong("office_id");
   		for(int i=0;i<itemList.size();i++){
   			TransJobOrder transJobOrder=new TransJobOrder();
//   			//create 
//			DbUtils.setModelValues(dto, transJobOrder);
   			Map<String, String> itemMap=itemList.get(i);
			//需后台处理的字段
			String order_no = OrderNoGenerator.getNextOrderNo(tjc.generateJobPrefix(itemMap.get("type")), office_id);

//			transJobOrder.set("plan_order_id", itemMap.get("plan_order_id"));
//			transJobOrder.set("plan_order_item_id", itemMap.get("plan_order_item_id"));
			transJobOrder.set("customer_id", itemMap.get("CUSTOMER_ID"));
//			transJobOrder.set("plan_order_no", itemMap.get("plan_order_no"));
			transJobOrder.set("type", itemMap.get("type"));
			transJobOrder.set("status", "新建");
			transJobOrder.set("remark", itemMap.get("remark"));
			transJobOrder.set("container_no", itemMap.get("container_no"));
			transJobOrder.set("so_no", itemMap.get("so_no"));
			transJobOrder.set("lading_no", itemMap.get("lading_no"));
			transJobOrder.set("cabinet_type", itemMap.get("cabinet_type"));
//			transJobOrder.set("head_carrier", itemMap.get("head_carrier"));
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
				tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
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
				tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
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
				tjol.set("loading_wharf1", itemMap.get("LOADING_WHARF1"));
				tjol.set("loading_wharf2", itemMap.get("LOADING_WHARF2"));
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

	 public void checkCustomerQuotation() {
			String jsonStr=getPara("params");
	       	Gson gson = new Gson();  
	        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);

	   		UserLogin user = LoginUserController.getLoginUser(this);
	   		long office_id = user.getLong("office_id");
	        String CUSTOMER_ID = (String) dto.get("CUSTOMER_ID");
	        String TAKE_WHARF = (String) dto.get("TAKE_WHARF");
	        String BACK_WHARF = (String) dto.get("BACK_WHARF");
	        String LOADING_WHARF1 = (String) dto.get("LOADING_WHARF1");
	        String LOADING_WHARF2 = (String) dto.get("LOADING_WHARF2");
	        String CHARGE_ID ="";
	        	if(StringUtils.isNotEmpty((String) dto.get("CHARGE_ID")))
	        		CHARGE_ID=" and "+dto.get("CHARGE_ID")+" = ( SELECT f.id FROM fin_item f WHERE f.office_id="+office_id
	    					+"		and f.name ='运费' ) ";
	        String truck_type = (String) dto.get("truck_type");

			String sqlString="SELECT A.*,c.name currency_name from( SELECT pq.* FROM party_quotation pq "
					+" WHERE pq.party_id = "+CUSTOMER_ID
					+" and pq.loading_wharf1= "+LOADING_WHARF1
					+" and pq.loading_wharf2= "+LOADING_WHARF2
					+" and pq.take_address= "+TAKE_WHARF
					+" and pq.delivery_address= "+BACK_WHARF
					+" and pq.truck_type= '"+truck_type+" ' "
					+CHARGE_ID
					+" )A left join currency c on c.id=A.currency_id";
			List<Record> records=Db.find(sqlString);
	   		Record r =new Record();
//	   		r.set("ids",ids);
	   		renderJson(records);
	    }
   

}
