package controllers.webadmin.ad;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class TaoController extends Controller {

	private Logger logger = Logger.getLogger(TaoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		//获取广告价格
        Record re1 = Db.findFirst("select id,type,price from price_maintain where type ='第一张广告'");
        Record re2 = Db.findFirst("select id,type,price from price_maintain where type ='第二张广告'");
        Record re3 = Db.findFirst("select id,type,price from price_maintain where type ='第三张广告'");
        Record re4 = Db.findFirst("select id,type,price from price_maintain where type ='第四张广告'");
		if(re1==null){
        	re1 = new Record();
        	re1.set("type", "第一张广告");
        	re1.set("price",0.0);
        	Db.save("price_maintain", re1);
        }else{
        	setAttr("first",re1);
        }
		
		if(re2 == null){
        	re2 = new Record();
        	re2.set("type", "第二张广告");
        	re2.set("price",0.0);
        	Db.save("price_maintain", re2);
        }else{
        	setAttr("second",re2);
        }
		
		if(re3 == null){
        	re3 = new Record();
        	re3.set("type", "第三张广告");
        	re3.set("price",0.0);
        	Db.save("price_maintain", re3);
        }else{
        	setAttr("third",re3);
        }
		
		if(re4 == null){
        	re4 = new Record();
        	re4.set("type", "第四张广告");
        	re4.set("price", 0.0);
        	Db.save("price_maintain", re4);
        }else{
        	setAttr("fourth",re4);
        }
		
		Record one = Db.findFirst("select *,ul.user_name user_name,wcp.name product_name,ul2.user_name default_user_name,wcp2.name default_product_name from wc_ad_banner_photo wabp"
					+ " LEFT JOIN user_login ul2 on ul2.id=wabp.default_user_id"
					+ " LEFT JOIN wc_product wcp2 on wcp2.id=wabp.default_product_id "
					+ " LEFT JOIN wc_product wcp on wabp.product_id=wcp.id "
					+ " LEFT JOIN user_login ul on ul.id=wabp.user_id where ad_index = 1");
        Record two = Db.findFirst("select *,ul.user_name user_name,wcp.name product_name,ul2.user_name default_user_name,wcp2.name default_product_name from wc_ad_banner_photo wabp "
        			+ " LEFT JOIN user_login ul2 on ul2.id=wabp.default_user_id"
        			+ " LEFT JOIN wc_product wcp2 on wcp2.id=wabp.default_product_id "
					+ " LEFT JOIN wc_product wcp on wabp.product_id=wcp.id "
					+ " LEFT JOIN user_login ul on ul.id=wabp.user_id where ad_index = 2");
        Record three =Db.findFirst("select *,ul.user_name user_name,wcp.name product_name,ul2.user_name default_user_name,wcp2.name default_product_name from wc_ad_banner_photo wabp "
        			+ " LEFT JOIN user_login ul2 on ul2.id=wabp.default_user_id"
        			+ " LEFT JOIN wc_product wcp2 on wcp2.id=wabp.default_product_id "
					+ " LEFT JOIN wc_product wcp on wabp.product_id=wcp.id "
					+ " LEFT JOIN user_login ul on ul.id=wabp.user_id where ad_index = 3");
        Record four = Db.findFirst("select *,ul.user_name user_name,wcp.name product_name,ul2.user_name default_user_name,wcp2.name default_product_name from wc_ad_banner_photo wabp "
        			+ " LEFT JOIN user_login ul2 on ul2.id=wabp.default_user_id"
        			+ " LEFT JOIN wc_product wcp2 on wcp2.id=wabp.default_product_id "
					+ " LEFT JOIN wc_product wcp on wabp.product_id=wcp.id "
					+ " LEFT JOIN user_login ul on ul.id=wabp.user_id where ad_index = 4");
        setAttr("one", one);
        setAttr("two", two);
        setAttr("three", three);
        setAttr("four", four);
        
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){
	        String id = getPara("id");
	       
	        render(getRequest().getRequestURI()+"/edit.html");
	    }
	
	

	 
    @Before(Tx.class)
   	public void save() throws Exception {
    	String user_id = getPara("user_id");
    	String product_id = getPara("product_id");
    	String begin_date = getPara("begin_date");
    	String end_date = getPara("end_date");
    	String photo = getPara("photo");
    	String index = getPara("index");
    	String default_flag = getPara("default_flag");
    	
    	Record order = Db.findFirst("select * from wc_ad_banner_photo where ad_index =?", index);
    	if(order == null){
    		order = new Record();
    		order.set("ad_index", index);
    		if("Y".equals(default_flag)){
    			order.set("default_photo", photo);
    			if(StringUtils.isNotBlank(user_id)){
    				order.set("default_user_id", user_id);
    			}
    			if(StringUtils.isNotBlank(product_id)){
    				order.set("default_product_id", product_id);
    			}
    	    }else{
    	    	order.set("photo", photo);
    	    	if(StringUtils.isNotBlank(user_id)){
    				order.set("user_id", user_id);
    			}
    			if(StringUtils.isNotBlank(product_id)){
    				order.set("product_id", product_id);
    			}
    			if(StringUtils.isNotBlank(begin_date)){
    				order.set("begin_date", begin_date);
    			}
    			if(StringUtils.isNotBlank(end_date)){
    				order.set("end_date", end_date);
    			}
    	    }
    		order.set("creator", LoginUserController.getLoginUserId(this));
    		order.set("create_time", new Date());
    		Db.save("wc_ad_banner_photo", order);
    	}else{
    		if("Y".equals(default_flag)){
    			order.set("default_photo", photo);
    			order.set("default_user_id", user_id);
        		order.set("default_product_id", product_id);
    	    }else{
    	    	order.set("photo", photo);
    	    	order.set("user_id", user_id);
        		order.set("product_id", product_id);
        		order.set("begin_date", begin_date);
        		order.set("end_date", end_date);
    	    }
    		order.set("update_by", LoginUserController.getLoginUserId(this));
    		order.set("update_time", new Date());
    		Db.update("wc_ad_banner_photo", order);
    	}

        renderJson(true);
   	}
    
    @Before(Tx.class)
    public void saveOfMsgBoard() throws Exception {
    	String title = getPara("radioTitle");
    	String content = getPara("radioContent");
    	UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	Record r= new Record();
    	r.set("title", title);
    	r.set("content", content);
    	r.set("office_id", office_id);
    	r.set("create_stamp", new Date());
    	r.set("creator", LoginUserController.getLoginUserId(this));
    	Db.save("msg_board", r);
    	redirect("/msgBoard");
    }
    
    public void list(){
    	String sLimit = "";
   		String pageIndex = getPara("draw");
   	 	if (getPara("start") != null && getPara("length") != null) {
        		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
    	}
   	 	Long user_id = LoginUserController.getLoginUserId(this);
		String sqlTotal = "select count(1) total from wc_ad_banner where creator = "+user_id ;
		Record rec = Db.findFirst(sqlTotal);
		String sql = "select * from wc_ad_banner where  creator = "+user_id+" order by begin_date desc " +sLimit;
		List<Record> orderList =  Db.find(sql);
		Map map = new HashMap();
        map.put("draw", pageIndex);
   	 	map.put("recordsTotal", rec.getLong("total"));
    	map.put("recordsFiltered", rec.getLong("total"));
   		 map.put("data", orderList);
    	renderJson(map);
    }
    
   @Before(Tx.class)
   public void updatePrice(){
	   String id = getPara("id");
	   String price = getPara("price") == null?"0":getPara("price");
	   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   String sql="update price_maintain set price ="+price+" ,  update_time='"+df.format(new Date())+"' where id="+id;
	   Db.update(sql);
   }
   
   @Before(Tx.class)
   public void whetherApprove(){
	   String status=getPara("status");
	   String info="";
	   if(status.equals("Y")){
		   info="已审批";
	   }else if(status.equals("N")){
		   info="已拒绝";
	   }
	   String id = getPara("id");
	   String sql = "update wc_ad_banner set status = '"+info+"' where id="+id+""; 
	   Db.update(sql);
	   renderJson(true);
   }
   
    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
    //查询商家下拉
    @Clear({SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class})// 清除指定的拦截器, 这个不需要查询个人和菜单信息
//    public void searchUser(){
//    	String input = getPara("input");
//    	long userId = LoginUserController.getLoginUserId(this);	
//		Long parentID = pom.getParentOfficeId();
//		
//		List<Record> spList = Collections.EMPTY_LIST;
//		if(StrKit.isBlank(input)){//从历史记录查找
//            String sql = "select h.ref_id, p.id, p.abbr name,p.ref_office_id from user_query_history h, party p "
//                    + "where h.ref_id=p.id and h.type='ARAP_COM' and h.user_id=?";
//            spList = Db.find(sql+" ORDER BY query_stamp desc limit 25", userId);
//            if(spList.size()==0){
//                spList = Db.find(" select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "
//                        + " and (p.company_name like '%"
//                        + input
//                        + "%' or p.abbr like '%"
//                        + input
//                        + "%' or p.code like '%"
//                        + input
//                        + "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) "
//                        + " order by convert(p.abbr using gb2312) asc limit 25", parentID, parentID);
//            }
//            renderJson(spList);
//        }else{
//            if (input !=null && input.trim().length() > 0) {
//                spList = Db
//                        .find(" select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "
//                                + " and (p.company_name like '%"
//                                + input
//                                + "%' or p.abbr like '%"
//                                + input
//                                + "%' or p.code like '%"
//                                + input
//                                + "%')  and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office=?) "
//                                + " order by convert(p.abbr using gb2312) asc limit 25",parentID,parentID);
//            } else {
//                spList = Db
//                        .find("select p.id,p.abbr name,p.ref_office_id from party p, office o where o.id = p.office_id "
//                                + " and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office =?) "
//                                + " order by convert(p.abbr using gb2312) asc limit 25", parentID, parentID);
//            }
//            renderJson(spList);
//        }
//    }
    public void searchUser(){
    	//String input = getPara("input");
    	List<Record> userList = new ArrayList<Record>();
    	
    	userList = Db.find("select id,user_name from user_login");
    	
    	renderJson(userList);
    }
    
    public void searchProduct(){
    	//String input = getPara("input");
    	String user_id = getPara("user_id");
    	List<Record> userList = new ArrayList<Record>();
    	String sql = "select wcp.id,wcp.name from wc_product wcp";
    	String condition = " where creator = "+user_id;
    	if(StringUtils.isBlank(user_id)){
    		condition = "";
    	}
    	sql = sql+condition;
    	userList = Db.find(sql);
    	renderJson(userList);
    }
    
    public void searchWcCompany(){
    	String user_id = getPara("user_id");
    	String sql = "select wcp.name from wc_product wcp left join user_login ul on ul.id = wcp.creator where ul.id="+user_id;
    	Record re = Db.findFirst(sql);
    	renderJson(re);
    }
}
