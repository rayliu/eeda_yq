package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.PartyMark;
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

import controllers.eeda.ListConfigController;
import controllers.util.DbUtils;
import controllers.util.OrderCheckOfficeUtil;
import controllers.util.OrderNoGenerator;
import controllers.util.ParentOffice;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class SupplierRatingController extends Controller {

    private Logger logger = Logger.getLogger(SupplierRatingController.class);
    Subject currentUser = SecurityUtils.getSubject();
    
    ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
    
    @Before(EedaMenuInterceptor.class)
    public void index() {
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/supplierRating");
        setAttr("listConfigList", configList);
        setAttr("user", LoginUserController.getLoginUser(this));
        render("/eeda/profile/serviceProvider/serviceProviderMarkList.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void create() { 
    	Record re = new Record();
        setAttr("party_mark", re);
    	render("/eeda/profile/serviceProvider/serviceProviderMarkEdit.html");
    }
    
    public void list() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        
        String sql = "SELECT A.* FROM (SELECT pm.id,pm.order_no,pm. STATUS,pm.sp_id,p.abbr sp_name,pm.item_name,"
        		+ " CAST(IF (pm.score_type = 'plus',pm.score,'') AS CHAR) plus_score,CAST(IF (pm.score_type = 'reduce',pm.score,'') AS CHAR) reduce_score,"
        		+ " ul.c_name creator_name,pm.create_stamp,(SELECT (SUM(if(p_m.score_type='plus',IFNULL(p_m.score,0),(0-IFNULL(p_m.score,0))))+80) "
        		+ " from party_mark p_m where p_m.status ='已审核' and p_m.sp_id=pm.sp_id) SUM_scores FROM party_mark pm"
        		+ "  LEFT JOIN party p ON p.id = pm.sp_id LEFT JOIN user_login ul ON ul.id = pm.creator "
        		+ " WHERE pm.office_id = "+office_id+" ORDER BY create_stamp DESC ) A WHERE 1 = 1 ";



        
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        System.out.println(sql+ condition +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	String id = getPara("id");
	    UserLogin user = LoginUserController.getLoginUser(this);
	    if (user==null) {
            return;
        }
        long office_id=user.getLong("office_id");
        //判断与登陆用户的office_id是否一致
        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("party_mark", Long.valueOf(id), office_id)){
        	renderError(403);// no permission
            return;
        }
        //供应商
        String sql = "SELECT pm.*,p.abbr sp_name,ul.c_name creator_name,ul1.c_name auditor_name,"
        		+ " (SELECT (SUM(if(p_m.score_type='plus',IFNULL(p_m.score,0),(0-IFNULL(p_m.score,0))))+80) "
        		+ " from party_mark p_m where p_m.status ='已审核' and p_m.sp_id=pm.sp_id) SUM_scores from party_mark pm LEFT JOIN party p on p.id = pm.sp_id "
        		   + " LEFT JOIN user_login ul ON ul.id = pm.creator"
        		   + " LEFT JOIN user_login ul1 ON ul1.id = pm.auditor"
        		   + "  WHERE pm.id = "+id+" and pm.office_id = "+office_id;
        Record re = Db.findFirst(sql);
        setAttr("docList", getItemDetail(id, "docItem"));
        if(re == null){
        	re = new Record();
        }
        setAttr("party_mark", re);
        
   	    render("/eeda/profile/serviceProvider/serviceProviderMarkEdit.html");
     }
    
    public void save(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
    	String jsonStr=getPara("params");
    	Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String id = (String) dto.get("id");
        PartyMark party_mark = new PartyMark();
        if(StringUtils.isEmpty(id)){
        	party_mark.set("office_id",office_id);
        	party_mark.set("order_no",OrderNoGenerator.getNextOrderNo("PF", user.getLong("office_id")));
            party_mark.set("sp_id",dto.get("sp_id"));
            party_mark.set("status","新建");
            party_mark.set("creator",user.getLong("id"));
            party_mark.set("create_stamp",new Date());
            party_mark.set("audit_suggestion",(String)dto.get("audit_suggestion"));
            party_mark.set("score_type",(String)dto.get("score_type"));
            if(StringUtils.isNotEmpty((String)dto.get("event_time"))){
            	party_mark.set("event_time",(String)dto.get("event_time"));
            }
            party_mark.set("item_name",(String)dto.get("item_name"));
            if(StringUtils.isNotEmpty((String)dto.get("score"))){
            	party_mark.set("score",dto.get("score"));
            }
            party_mark.set("participant",(String)dto.get("participant"));
            party_mark.set("about",(String)dto.get("about"));
            if(((String)dto.get("score_type")).equals("reduce")){
            	party_mark.set("review",(String)dto.get("review"));
                party_mark.set("improvement",(String)dto.get("improvement"));
            }
            party_mark.save();
        }else{
        	party_mark = PartyMark.dao.findById(id);
            party_mark.set("sp_id",dto.get("sp_id"));
            party_mark.set("audit_suggestion",(String)dto.get("audit_suggestion"));
            party_mark.set("score_type",(String)dto.get("score_type"));
            if(StringUtils.isNotEmpty((String)dto.get("event_time"))){
            	party_mark.set("event_time",(String)dto.get("event_time"));
            }
            party_mark.set("item_name",(String)dto.get("item_name"));
            if(StringUtils.isNotEmpty((String)dto.get("score"))){
            	party_mark.set("score",dto.get("score"));
            }
            party_mark.set("participant",(String)dto.get("participant"));
            party_mark.set("about",(String)dto.get("about"));
            if(((String)dto.get("score_type")).equals("reduce")){
            	party_mark.set("review",(String)dto.get("review"));
                party_mark.set("improvement",(String)dto.get("improvement"));
            }
            party_mark.update();
        }
        
        //相关文档
      	List<Map<String, String>> doc_list = (ArrayList<Map<String, String>>)dto.get("doc_list");
      	DbUtils.handleList(doc_list, id, "party_mark_doc", "order_id");
        Record r = party_mark.toRecord();
   		r.set("creator_name", user.get("c_name"));
   		
        renderJson(r);
    }
    
    public void submitMethod(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
        String id = getPara("id");
        Record re = Db.findById("party_mark", id);
        re.set("status", "待审核");
        re.set("submitter", user.getLong("id"));
        re.set("submitter_time", new Date());
        Db.update("party_mark",re);
        renderJson(re); 
    }
    
    public void checkMethod(){
    	UserLogin user = LoginUserController.getLoginUser(this);
    	if (user==null) {
            return;
        }
    	String id = getPara("id");
    	String type = getPara("type");
    	Record re = Db.findById("party_mark", id);
    	if(type.equals("check")){
    		re.set("status", "已审核");
            re.set("auditor", user.getLong("id"));
            re.set("audit_stamp", new Date());
            Db.update("party_mark",re);
    	}else if(type.equals("notPassCheck")){
    		re.set("status", "审核不通过");
            re.set("auditor", user.getLong("id"));
            re.set("audit_stamp", new Date());
            Db.update("party_mark",re);
    	}
    	String sql = "SELECT (SUM(if(p_m.score_type='plus',IFNULL(p_m.score,0),(0-IFNULL(p_m.score,0))))+80) SUM_scores"
		+ " from party_mark p_m where p_m.status ='已审核' and p_m.sp_id="+re.getLong("sp_id");
    	Record re1 = Db.findFirst(sql);
    	re.set("auditor_name", user.get("c_name"));
    	re.set("SUM_scores", re1.getDouble("SUM_scores"));
        renderJson(re); 
    }
  //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
    	String id = getPara("order_id");
    	String type = getPara("type");
    	String remark = getPara("remark");
    	List<UploadFile> fileList = getFiles("serviceProvider_doc");
    	
		for (int i = 0; i < fileList.size(); i++) {
    		File file = fileList.get(i).getFile();
    		String fileName = file.getName();
    		
    		Record r = new Record();
    		r.set("order_id", id);
			r.set("uploader", LoginUserController.getLoginUserId(this));
			r.set("doc_name", fileName);
			r.set("upload_stamp", new Date());
			r.set("remark", remark);
			Db.save("party_mark_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("party_mark_doc", id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\serviceProvider_doc\\"+fileName;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("party_mark_doc", r);
            resultMap.put("result", result);
        }else{
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }
    
    //异步刷新字表
    public void tableList(){
    	String order_id = getPara("order_id");
    	String type = getPara("type");
    	List<Record> list = null;
//    	list = Db.find("SELECT * FROM fin_account WHERE order_id = ?",order_id);
    	
    	list=getItemDetail(order_id, type);
    	
    	Map map = new HashMap();
        map.put("sEcho", 1);
        map.put("iTotalRecords", list.size());
        map.put("iTotalDisplayRecords", list.size());
        map.put("aaData", list);
        renderJson(map); 
    }
    
  //返回对象	
    private List<Record> getItemDetail(String id,String type){
     	String itemSql = "";
    	List<Record> itemList = null;
    	if("contacts".equals(type)){
    		itemSql = "SELECT * FROM contacts_item WHERE party_id=?";
    		itemList = Db.find(itemSql, id);
    	}else if("cars".equals(type)){
    		itemList = Db.find("SELECT * FROM carinfo WHERE parent_id = ?",id);
		}else if("dock".equals(type)){
    		itemSql = "SELECT d.* FROM dockinfo d WHERE  d.party_type='serviceProvider' and d.party_id=? order by d.id";
    		itemList = Db.find(itemSql, id);
		}else if("docItem".equals(type)){
			itemSql = "select pm.*,u.c_name from party_mark_doc pm left join user_login u on pm.uploader=u.id "
        			+ " where pm.order_id=? order by pm.id";
    		itemList = Db.find(itemSql, id);
    	}else {
    		itemList = Db.find("SELECT fa.*,cy.code currency_name FROM fin_account fa "
    				+ "LEFT JOIN currency cy on cy.id = fa.currency_id  WHERE order_id = ?",id);
		}
		return itemList;
    }
}
