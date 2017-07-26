package controllers.webadmin.data;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ProductController extends Controller {

	private Logger logger = Logger.getLogger(ProductController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render(getRequest().getRequestURI()+"/list.html");
	}
	
	@Before(EedaMenuInterceptor.class)
	 public void edit(){
	        String id = getPara("id");
//	      String title = getPara("edit_radioTitle");
//	      String content = getPara("edit_radioContent");
//	      Record r= Db.findById("msg_board", id);
//	      r.set("title", title);
//	      r.set("content", content);
//	      r.set("update_stamp", new Date());
//	      r.set("updator", LoginUserController.getLoginUserId(this));
//	      Db.update("msg_board", r);
	        render(getRequest().getRequestURI()+"/edit.html");
	    }
	 
    @Before(Tx.class)
   	public void save() throws Exception {
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
        redirect("/");
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
    	
    	UserLogin user = LoginUserController.getLoginUser(this);
    	Long user_id = LoginUserController.getLoginUserId(this);

        long office_id=user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select ul.c_name productor,wp.* from wc_product wp "
        		+ "LEFT JOIN user_login ul on ul.id = wp.creator order by create_time asc ";
    	
    	String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    @Before(Tx.class)
    public void whetherCarriage(){
 	   String status=getPara("status");
 	   String info="";
 	   if(status.equals("toUp")){
 		   info="Y";
 	   }else if(status.equals("toDown")){
 		   info="N";
 	   }
 	   String id = getPara("id");
 	   String sql = "update wc_product set is_active = '"+info+"' where id="+id+""; 
 	   Db.update(sql);
 	   renderJson(true);
    }
    
    
    public void detail(){
    	String id = getPara("id");
    	String sql="select * from wc_product where id = "+id;
    	String pic="select * from wc_product_pic where order_id="+id;
    	Record  re = Db.findFirst(sql);
    	List  pictures = Db.find(pic);
    	setAttr("product",re);
    	setAttr("pictures",pictures);
    	render("/WebAdmin/tao_manage/product/modify/edit.html");
    }
    
    @Before(Tx.class)
	public void update(){
		//String rootPath = PathKit.getWebRootPath();
		String jsonStr = getPara("jsonStr");
        Long userId = LoginUserController.getLoginUserId(this);
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);  
        String order_id = (String) dto.get("id");
        String name = (String) dto.get("name");
        String category = (String) dto.get("category");
        String price_type = (String) dto.get("price_type");
        String price =  (String)dto.get("price");
        String unit = (String) dto.get("unit");
        String content = (String) dto.get("content");
        String cover = "".equals((String) dto.get("cover"))?null:(String) dto.get("cover");
        Double img_num = (Double) dto.get("img_num");
        Record order = null;
        if(StringUtils.isNotBlank(order_id)){
        	order = Db.findById("wc_product", order_id);
        	order.set("category", category);
        	order.set("name", name);
        	order.set("price_type", price_type);
        	order.set("price", price);
        	order.set("unit", unit);
        	order.set("is_active", "Y");
        	order.set("content", content);
        	order.set("cover", cover);
        	Db.update("wc_product", order);
        	
        	List<Record> orderItem = Db.find("select * from wc_product_pic where order_id = ? order by seq asc",order_id);
        	for (int i = 1; i <= img_num; i++) {
        		int order_num = orderItem.size();
        		Record re = null;
        		if(i > order_num){
        			re = new Record();
        			re.set("photo", (String) dto.get("photo"+i));
            		re.set("seq", i);
            		re.set("order_id", order.get("id"));
                	Db.save("wc_product_pic", re);
        		}
    		}
        }else{
        	order = new Record();
        	order.set("name", name);
        	order.set("category", category);
        	order.set("price_type", price_type);
        	order.set("price", price);
        	order.set("unit", unit);
        	order.set("content", content);
        	order.set("cover", cover);
        	order.set("creator", userId);
        	order.set("create_stamp", new Date());
        	Db.save("wc_product", order);
        	
        	Record orderItem = null;
        	for (int i = 1; i <= img_num; i++) {
        		orderItem = new Record();
        		orderItem.set("photo", (String) dto.get("photo"+i));
        		orderItem.set("seq", i);
        		orderItem.set("order_id", order.get("id"));
            	Db.save("wc_product_pic", orderItem);
    		}
        }
        renderJson(order);
	}
	
    
	@Before(Tx.class)
	public void deletePicture(){
		String id=getPara("id");
		Db.deleteById("wc_product_pic", id);
		renderJson(true);
	}
	
    public void seeMsgBoardDetail(){
    	String id = getPara("id");
    	Record r= Db.findById("msg_board", id);
    	renderJson(r);
    }
    
    @Before(Tx.class)
    public void saveFile() throws Exception{
    	Record re = new Record();
    	try {
            UploadFile file = getFile();
            re.set("name", file.getFileName());
        } catch (Exception e) {
            e.getMessage();
        }
    	renderJson(re);
    }
    
}
