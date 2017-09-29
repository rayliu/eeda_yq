package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.eeda.ListConfigController;
import controllers.util.DbUtils;
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
        long user_id = user.getLong("id");
		List<Record> configList = ListConfigController.getConfig(user_id, "/supplierRating");
        setAttr("listConfigList", configList);
        setAttr("user", LoginUserController.getLoginUser(this));
        render("/eeda/profile/serviceProvider/serviceProviderMarkList.html");
    }
    
    
    
    public void list() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id=user.getLong("office_id");
    	
        String sLimit = "";
        String pageIndex = getPara("draw");
        
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"order_export_date":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "order_export_date";
        	sort = "desc";
        }
        
        
        String sql = "SELECT * from( SELECT p.abbr sp_name,p.id spId, SUM(score) sum_score,GROUP_CONCAT(CONCAT(pm.item,' : ',pm.score) SEPARATOR ' <br> ') item_smg from party p"
        		+ " LEFT JOIN party_mark pm on pm.sp_id = p.id  WHERE p.office_id = "+office_id
        		+ " GROUP BY p.id ) A where 1=1 ";

        
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        if (getPara("start") != null  && getPara("length") != null) {
        	if(Long.parseLong(getPara("start")) <= rec.getLong("total")){
        		sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        	}else{
        		sLimit = " LIMIT 0, " + getPara("length");
        		pageIndex = "1";
        	}
        }
        
        List<Record> orderList = Db.find(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        System.out.println(sql+ condition + " order by " + sName +" "+ sort +sLimit);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }
    
    
    
    @Before({EedaMenuInterceptor.class, Tx.class})
    public void edit() {
    	Long parentID = pom.getParentOfficeId();
    	String sp_id=getPara("sp_id");
        //供应商
        String sql = "SELECT * from party WHERE id = "+sp_id;
        
        //评分明细
        List<Record> partyItem = null;
        String itemSql = "select * from party_mark pm where pm.sp_id ="+sp_id;
        partyItem = Db.find(itemSql);
        setAttr("partyItemList", partyItem);
        
        //打分页面        
   	    setAttr("partyMark", Db.findFirst(sql));
   	    render("/eeda/profile/serviceProvider/serviceProviderMarkEdit.html");
     }
    
  //打分
    public void checkedList(){
    	String office_id= pom.getCurrentOfficeId().toString();
        String sql="select * from( SELECT "
        		+"	pm.*, p.abbr sp_name,sum(pm.score) total_score, "
        		+"	ul.c_name creator_name  "
        		+"FROM	party_mark pm "
        		+"LEFT JOIN party p ON p.id = pm.sp_id "
        		+"LEFT JOIN user_login ul ON ul.id = pm.creator "
        		+"WHERE	p.office_id = 2 "
        		+"GROUP BY pm.sp_id "
        		+ " order by mark_date desc "
        		+ " )A where 1=1 ";
        String condition = DbUtils.buildConditions(getParaMap());
        List<Record> orderList = Db.find(sql+condition);
        Map map = new HashMap();
        map.put("data", orderList);
        renderJson(map);
    }

   
}
