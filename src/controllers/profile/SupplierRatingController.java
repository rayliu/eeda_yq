package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Location;
import models.ParentOfficeModel;
import models.Party;
import models.PartyMark;
import models.UserLogin;
import models.yh.profile.ProviderChargeType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

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
        render("/eeda/profile/serviceProvider/serviceProviderMarkList.html");
    }
    
    public void list() {
    	Long parentID = pom.getParentOfficeId();
    	String sp_id=getPara("sp_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql="select * (select pm.*,p.abbr,ul.c_name creator FROM party_mark pm "
				+" LEFT JOIN party p on p.id=pm.sp_id "
				+" LEFT JOIN user_login ul on ul.id=pm.creator "
				+" WHERE pm.sp_id = " +sp_id
				+" )A where 1=1 ";
      
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        List<Record> orderList = Db.find(sql+ condition +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);}
    
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
