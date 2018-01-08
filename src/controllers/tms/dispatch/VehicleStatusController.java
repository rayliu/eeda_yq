package controllers.tms.dispatch;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ParentOfficeModel;
import models.UserLogin;
import models.yh.profile.Carinfo;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.ParentOffice;
import controllers.util.PermissionConstant;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class VehicleStatusController extends Controller {
	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);
	private Logger logger = Logger.getLogger(VehicleStatusController.class);
	Subject currentUser = SecurityUtils.getSubject();
	private Object type;

	@Before(EedaMenuInterceptor.class)
	public void index() {
		String type = getPara("type");
		setAttr("type",type);
		render("/tms/Dispatch/vehicleStatusList.html");
	}

    
    //更新车辆状态
    private void updateCarStatus(String count_carId,String car_id,String id){
    	Carinfo re_car = Carinfo.dao.findById(car_id);
		if(re_car!=null){
			if(count_carId.contains(car_id)){
				Record re_land_car = Db.findFirst("select * from trans_job_order_land_item where order_id = ? and car_no=?",id,car_id);
				if(re_land_car.get("dispatch_date")!=null){
					re_car.set("dispatch_status","Y");
				}
				if(re_land_car.get("cabinet_date")!=null){
					re_car.set("cabinet_status","Y");
				}
				if(re_land_car.get("arrival_date")!=null){
					re_car.set("arrival_status","Y");
				}
				if(re_land_car.get("closing_date")!=null){
					re_car.set("closing_status","Y");
				}
			}else {
				re_car.set("dispatch_status","N");
				re_car.set("cabinet_status","N");
				re_car.set("arrival_status","N");
				re_car.set("closing_status","N");
			}
			re_car.update();
		}
    }
    
    //上传相关文档
    @Before(Tx.class)
    public void saveDocFile(){
			String order_id = getPara("order_id");
	    	List<UploadFile> fileList = getFiles("doc");
	    	
			for (int i = 0; i < fileList.size(); i++) {
	    		File file = fileList.get(i).getFile();
	    		String fileName = file.getName();
	    		
				Record r = new Record();
				r.set("order_id", order_id);
				r.set("uploader", LoginUserController.getLoginUserId(this));
				r.set("doc_name", fileName);
				r.set("upload_time", new Date());
				Db.save("trans_job_order_doc",r);
		}
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("result", true);
    	renderJson(resultMap);
    }


    
    //上传陆运签收文件描述
    @Before(Tx.class)
    public void uploadSignDesc(){
		String id = getPara("id");
		List<UploadFile> fileList = getFiles("doc");
		File file = fileList.get(0).getFile();
		String fileName = file.getName();
		
		Record r = new Record();
		r.set("land_id", id);
		r.set("doc_name", fileName);
		r.set("uploader", LoginUserController.getLoginUserId(this));
		r.set("upload_time", new Date());
		Db.save("trans_job_order_land_doc",r);
		renderJson("{\"result\":true}");
    }
    
    //删除相关文档
    @Before(Tx.class)
    public void deleteDoc(){
    	String id = getPara("docId");
    	Record r = Db.findById("trans_job_order_doc",id);
    	String fileName = r.getStr("doc_name");
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	
    	String path = getRequest().getServletContext().getRealPath("/");
    	String filePath = path+"\\upload\\doc\\"+fileName;
    	
    	
    	File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            Db.delete("trans_job_order_doc",r);
            resultMap.put("result", result);
        }else{
        	Db.delete("trans_job_order_doc",r);
        	resultMap.put("result", "文件不存在可能已被删除!");
        }
        renderJson(resultMap);
    }




    
     
    public void list() {    	
        UserLogin user = LoginUserController.getLoginUser(this);
        if(user==null){
        	return;
        }
        long office_id=user.getLong("office_id");
        
    	String type=getPara("type");

        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sort = getPara("order[0][dir]")==null?"desc":getPara("order[0][dir]");
        String sColumn =  getPara("order[0][column]");
        String sName =  getPara("columns["+sColumn+"][data]")==null?"create_stamp":getPara("columns["+sColumn+"][data]") ;
        if("0".equals(sName)){
        	sName = "car_no";
        	sort ="desc";
        }
        
        String sql = "";
        if("sowait".equals(type)){
        	sql=" ";        	
        }else{
		         sql = "select * from( SELECT *,CASE WHEN dispatch_status='N' and  cabinet_status ='N' and arrival_status='N' "
		         		+ "and closing_status = 'N' THEN '待命车辆' ELSE ' 已启动车辆' END sendcar_status"
		         		+ " from carinfo WHERE office_id ="+office_id+") A where 1=1";
         }
        
        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from ("+sql+ condition+") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));
        
        List<Record> orderList = Db.find(sql+ condition + " order by  " + sName +" "+ sort +sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    }

 // 更改车辆受控状态
    @RequiresPermissions(value = {PermissionConstant.PERMSSION_A_DELETE})
    public void changeMonitorStatus() {
        String id = getPara("selfId");
        if (id != null) {
        	Carinfo carinfo = Carinfo.dao.findById(id);
        	 Object obj = carinfo.get("monitor_status");
             if("Y".equals(obj)){
            	 carinfo.set("monitor_status", "N");
             }else{
            	 carinfo.set("monitor_status", "Y");
             }
             carinfo.update();
        }
        renderJson("{\"result\":true}");
    }
    
    public void saveVehicleStatus() {
        String id = getPara("id");
        String vehicle_status = getPara("vehicle_status");
        if (id != null) {
        	Carinfo carinfo = Carinfo.dao.findById(id);
            carinfo.set("vehicle_status", vehicle_status);
            carinfo.update();
        }
        renderJson("{\"result\":true}");
    }

}
