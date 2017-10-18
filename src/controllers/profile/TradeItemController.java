package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.TradeItem;
import models.UserLogin;
import models.eeda.profile.FinItem;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.util.OrderCheckOfficeUtil;
import controllers.util.PoiUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TradeItemController extends Controller {
    private Log logger = Log.getLog(TradeItemController.class);
    
    //查询费用中文名称
    public void search() {/*
        String input = getPara("input");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
        List<Record> finItems = Collections.EMPTY_LIST;
        if(StrKit.isBlank(input)){//从历史记录查找
            String sql = "select h.ref_id, f.id, f.name from user_query_history h, fin_item f "
                    + "where h.ref_id=f.id and h.type='ARAP_FIN' and h.user_id=?";
            finItems = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);
            if(finItems.size()==0){
                finItems = Db.find("select * from fin_item f where f.office_id=? and f.name like '%"+input+"%' "
                        + " order by convert(f.name using gb2312) asc limit 10", officeId);
            }
            renderJson(finItems);
        }else{
            if (input !=null && input.trim().length() > 0) {
                finItems = Db.find("select * from fin_item f where f.office_id=? and f.name like '%"+input+"%' "
                        + " order by convert(name using gb2312) asc limit 10", officeId);
            }else{
                finItems = Db.find("select * from fin_item f where f.office_id=? "
                        + "order by convert(name using gb2312) asc limit 10", officeId);
            }
            renderJson(finItems);
        }
    */
    	
    	  UserLogin user = LoginUserController.getLoginUser(this);
          long userId = user.getLong("id");
          Long officeId = user.getLong("office_id");
          
          String sLimit = "";
          String pageIndex = getPara("sEcho");
          if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
              sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
          }
    		String name = getPara("commodity_name");
    		String c = getPara("commodity_code");
    		String[]codes = c.split(",");
    		String condition = "";
    		if(StringUtils.isNotBlank(name)){
    			condition += "and commodity_name like '%"+name+"%' ";
    		}
    		if(codes.length > 0 && StringUtils.isNotBlank(c)){
    			for (int i = 0; i < codes.length; i++){
    				String code = codes[i];
    				code = code.trim();
    				if(i == 0){
    					condition += "and ( commodity_code like '%"+code+"%' ";
    				}else{
    					condition += "or commodity_code like '%"+code+"%' ";
    				}
    			}
    			condition += ")";
    		}
    		String sql = "select * from trade_item t where 1=1 and t.office_id="+officeId+" "+condition;
    		   String sqlTotal = "select count(1) total from ("+sql+") B";
    	        Record rec = Db.findFirst(sqlTotal);
    	        logger.debug("total records:" + rec.getLong("total"));
    	        List<Record> BillingOrders = Db.find(sql + " order by id desc " +sLimit);
    	        Map BillingOrderListMap = new HashMap();
    	        BillingOrderListMap.put("sEcho", pageIndex);
    	        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
    	        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

    	        BillingOrderListMap.put("aaData", BillingOrders);

    	        renderJson(BillingOrderListMap); 
    }
    
    //查询费用英文名称
    public void search_eng() {
    	String input = getPara("input");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
    	List<Record> finItems = null;
    	if (input !=null && input.trim().length() > 0) {
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f"
    		        + " where f.office_id=? and f.name_eng like '%"+input+"%'", officeId);
    	}else{
    		finItems = Db.find("select f.id,ifnull(f.name_eng,f.name) name from fin_item f where f.office_id=? ", officeId);
    	}
    	renderJson(finItems);
    }
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
        render("/eeda/profile/tradeItem/tradeItemList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/eeda/profile/tradeItem/tradeItemEdit.html");
    }
    

    public void list() {
    	
        String code = getPara("code");
        String name = getPara("name");
        String name_eng = getPara("name_eng");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        
        String sLimit = "";
        String pageIndex = getPara("sEcho");
        if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
            sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
        }
        
        String sql = "";
        if(code==null&&name==null&&name_eng==null){
        	sql = "SELECT * from trade_item t where 1 =1 and t.office_id="+officeId;
        }else{
        	sql = "SELECT id,code,name_eng,name,remark from fin_item f where 1 =1 and f.office_id="+officeId
        			+ " and code like '%"+code
        			+"%' and name like '%"+name
        			+"%' and name_eng like '%"+name_eng+"%'";
        }

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> BillingOrders = Db.find(sql + " order by id desc " +sLimit);
        Map BillingOrderListMap = new HashMap();
        BillingOrderListMap.put("sEcho", pageIndex);
        BillingOrderListMap.put("iTotalRecords", rec.getLong("total"));
        BillingOrderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        BillingOrderListMap.put("aaData", BillingOrders);

        renderJson(BillingOrderListMap); 
    }

    // 编辑条目按钮
    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
	    UserLogin user1 = LoginUserController.getLoginUser(this);
	    long office_id=user1.getLong("office_id");
	    //判断与登陆用户的office_id是否一致
	    if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("trade_item", Long.valueOf(id), office_id)){
	    	renderError(403);// no permission
	        return;
	    }
        Record tradeItem = Db.findFirst("SELECT * from trade_item t where id="+id);
        setAttr("order", tradeItem);
        
        render("/eeda/profile/tradeItem/tradeItemEdit.html");
        
    }

    // 删除条目
    public void delete() {
        String id = getPara();
        if (id != null) {
        	FinItem l = FinItem.dao.findById(id);
            Object obj = l.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                l.set("is_stop", true);
            } else {
                l.set("is_stop", false);
            }
            l.update();
        }
        redirect("/finItem");
    }

    // 添加编辑保存
//    @RequiresPermissions(value = { PermissionConstant.PERMSSION_T_CREATE,
//            PermissionConstant.PERMSSION_T_UPDATE }, logical = Logical.OR)
    public void save() {
        String jsonStr=getPara("params");
        UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        Long officeId = user.getLong("office_id");
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        
        TradeItem r = null;
        
        String id = (String) dto.get("id");
        String commodity_name = (String) dto.get("commodity_name");
        String commodity_code = (String) dto.get("commodity_code");
        String unit_name = (String) dto.get("unit_name");
        String unit_name_eng = (String) dto.get("unit_name_eng");
        String VAT_rate = (String) dto.get("VAT_rate");
        String rebate_rate = (String) dto.get("rebate_rate");
        String remark = (String) dto.get("remark");
        
        if (StringUtils.isBlank(id)) {
        	r = new TradeItem();
        	r.set("commodity_name", commodity_name);
        	r.set("commodity_code", commodity_code);
        	r.set("unit_name", unit_name);
            r.set("unit_name_eng", unit_name_eng);
            r.set("VAT_rate", VAT_rate);
            r.set("rebate_rate",rebate_rate);
            r.set("remark", remark);
            r.set("office_id", officeId);
            r.save();
        } else {
        	r = TradeItem.dao.findById(id);
        	r.set("commodity_name", commodity_name);
        	r.set("commodity_code", commodity_code);
        	r.set("unit_name", unit_name);
            r.set("unit_name_eng", unit_name_eng);
            r.set("VAT_rate", VAT_rate);
            r.set("rebate_rate", rebate_rate);
            r.set("remark", remark);           
            r.update();
        }
        renderJson(r);
    }
    
	public void downloadExcelList(){
  	  UserLogin user = LoginUserController.getLoginUser(this);
      long userId = user.getLong("id");
      Long officeId = user.getLong("office_id");
      
      String sLimit = "";
      String pageIndex = getPara("sEcho");
      if (getPara("iDisplayStart") != null && getPara("iDisplayLength") != null) {
          sLimit = " LIMIT " + getPara("iDisplayStart") + ", " + getPara("iDisplayLength");
      }
		String name = getPara("commodity_name");
		String c = getPara("commodity_code");
		String[]codes = c.split(",");
		String condition = "";
		if(StringUtils.isNotBlank(name)){
			condition += "and commodity_name like '%"+name+"%' ";
		}
		if(codes.length > 0 && StringUtils.isNotBlank(c)){
			for (int i = 0; i < codes.length; i++){
				String code = codes[i];
				code = code.trim();
				if(i == 0){
					condition += "and ( commodity_code like '%"+code+"%' ";
				}else{
					condition += "or commodity_code like '%"+code+"%' ";
				}
			}
			condition += ")";
		}
		String sql = "select * from trade_item t where 1=1 and t.office_id="+officeId+" "+condition;
        
		String sqlExport = sql;
		String total_name_header = "商品名称,商品编码,单位,增值税率,退税率,备注";
		String[] headers = total_name_header.split(",");

		String[] fields = {"COMMODITY_NAME","COMMODITY_CODE","UNIT_NAME","VAT_RATE","REBATE_RATE","REMARK"};
		
		String exportName = "";
		
		String fileName = PoiUtils.generateExcel(headers, fields, sqlExport,exportName);
		renderText(fileName);
	}
    
    //校验是否存在此商品名称
    public void checkCommodityNameExist(){
    	String para = getPara("commodity_name");
    	String order_id = getPara("order_id");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long userId = user.getLong("id");
        long officeId = user.getLong("office_id");
    	
    	boolean ifExist = true;
    	String sql = "select * from trade_item where id = ? and office_id=?";
		String sql1 = "select * from trade_item where commodity_name = ? and office_id=?";
		Record r = Db.findFirst(sql,order_id, officeId);
		Record r1 = Db.findFirst(sql1,para, officeId);
    	if(order_id!=null&&order_id!=""){
    		if(para.equals(r.get("commodity_name"))||r1==null){
    			ifExist = true;
    		}else{
        		ifExist = false;
        	}
    	}else{
    		if(r1==null){
        		ifExist = true;
        	}else{
        		ifExist = false;
        	}
    	}
    	
    	
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameExist(){
    	String para= getPara("name");
    	UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
    	String sql = "select * from fin_item where name = ? and office_id=?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql, para, officeId);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //校验是否存在此费用
    public void checkNameEngExist(){
    	String para= getPara("name_eng");
    	UserLogin user = LoginUserController.getLoginUser(this);
        Long officeId = user.getLong("office_id");
    	String sql = "select * from fin_item where name_eng =? and office_id=?";
    	boolean ifExist;
    	Record r = Db.findFirst(sql, para, officeId);
    	if(r==null){
    		ifExist = true;
    	}else{
    		ifExist = false;
    	}
    	renderJson(ifExist);
    }
    
    //贸易商品信息导入
	@Before(Tx.class)
	public Record importValue( List<Map<String, String>> lines, String order_id, long office_id) {
		Connection conn = null;
		Record result = new Record();
		result.set("result",true);

		int rowNumber = 1;
		
		try {
			conn = DbKit.getConfig().getDataSource().getConnection();
			DbKit.getConfig().setThreadLocalConnection(conn);
			conn.setAutoCommit(false);// 自动提交变成false
			
			for (Map<String, String> line :lines) {
				String commodity_name = line.get("商品名称").trim();
				String commodity_code = line.get("商品编码").trim();
				String legal_unit = line.get("单位").trim();
				String vat_rate = line.get("增值税率").trim();
				String rebate_rate = line.get("退税率").trim();
				String remark = line.get("备注").trim();
	   			Long commodity_id = null;
	   			Record commodity = Db.findFirst("select * from trade_item where commodity_name = ? and office_id = ?",commodity_name,office_id);
	   			Record order = new Record();
	   			if(commodity != null){
	   				commodity_id = commodity.getLong("id");
	   				commodity.set("unit_name", legal_unit);
	   				commodity.set("commodity_code", commodity_code);
	   				commodity.set("VAT_rate", vat_rate);
	   				commodity.set("rebate_rate", rebate_rate);
	   				commodity.set("remark", remark);
	   				commodity.set("office_id", office_id);
		   			Db.update("trade_item", commodity);
	   			}else{
	   				order.set("commodity_code", commodity_code);
		   			order.set("commodity_name", commodity_name);
		   			order.set("unit_name", legal_unit);
		   			order.set("VAT_rate", vat_rate);
		   			order.set("rebate_rate", rebate_rate);
		   			order.set("remark", remark);  
		   			order.set("office_id", office_id);
		   			Db.save("trade_item", order);
	   			}
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
