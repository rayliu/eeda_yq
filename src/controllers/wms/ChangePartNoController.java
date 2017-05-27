package controllers.wms;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Party;
import models.UserLogin;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.wms.GateIn;
import models.wms.GateOut;

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
import controllers.util.DbUtils;
import controllers.util.OrderNoGenerator;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class ChangePartNoController extends Controller {

	private Logger logger = Logger.getLogger(ChangePartNoController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
		render("/wms/dataMaintain/changePartNo.html");
	}
	
	
    
	@Before(Tx.class)
   	public void update(){	
   		String dataStr=getPara("part_no");
       	
   		String[] dataArray = dataStr.split(",");
   		String succ = "";
   		String errs = "";
   		boolean result = true;
       	for (int i = 0; i < dataArray.length; i++) {
			String part_no = dataArray[i].trim();
			Record re = Db.findFirst("select * from wmsproduct where part_no = ?",part_no+'A');
			if(re != null){
				succ += part_no+",";
				Db.update("update gate_in set part_no = CONCAT(part_no,'A'),add_flag = 'Y' where part_no = ?",part_no);
				Db.update("update gate_out set part_no = CONCAT(part_no,'A'),add_flag = 'Y' where part_no = ?",part_no);
				Db.update("update inv_check_order set part_no = CONCAT(part_no,'A'),add_flag = 'Y' where part_no = ?",part_no);
			}else{
				errs += part_no+",";
				result = false;
			}
		}
       	
       	String cause = "";
       	if(StringUtils.isNotBlank(succ)){
       		cause += succ +"更新成功;  ";
       	}
       	if(StringUtils.isNotBlank(errs)){
       		cause += errs +"更新失败";;
       	}
       	
       	
       	Record res = new Record();
       	res.set("result", result);
       	res.set("cause", cause);

   		renderJson(res);
   	}
      

}
