package controllers.webadmin.user;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

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

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.base.payload.APNPayload.DictionaryAlertMsg;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.gexin.rp.sdk.template.style.Style0;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class CloudpushController extends Controller {

	private Logger logger = Logger.getLogger(CloudpushController.class);
	Subject currentUser = SecurityUtils.getSubject();

	@Before(EedaMenuInterceptor.class)
	public void index() {
	    //对应action
		render(getRequest().getRequestURI()+"/list.html");
	}
	
    public void list(){
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
        	sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
         
    	String sql = "select  fee.* from cloud_push fee"
    			+ " left join user_login ul on ul.id = fee.creator "
    			+ " where 1=1 "
    			+ " order by fee.create_time desc";
    	
        String sqlTotal = "select count(1) total from ("+sql+") B";
        Record rec = Db.findFirst(sqlTotal);
        
        List<Record> orderList = Db.find(sql +sLimit);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map); 
    	
    }
    
    
    static String appId = "RgMlGTp3Kq6uZOWQuIWA55";
    static String appkey = "shVPfqzmIt6swSOhLJT1d";
    static String masterSecret = "wZB5s6w0PS7e8s7rGqaFK7";
    static String host = "http://sdk.open.api.igexin.com/apiex.htm";
    
    @Before(Tx.class)
    public void sendMsg(){
    	Long user_id = LoginUserController.getLoginUserId(this);
    	Record result = new Record();
    	
    	String action = getPara("action");
    	String target = getPara("target");
    	String target_value = getPara("target_value");
    	String title = getPara("title");
    	String body = getPara("body");
    	Record order = new Record();
    	order.set("action", action);
    	order.set("target", target);
    	order.set("target_value", target_value);
    	order.set("title", title);
    	order.set("body", body);
    	order.set("creator", user_id);
    	order.set("create_time", new Date());
    	Db.save("cloud_push", order);
    	
    	IGtPush push = new IGtPush(host, appkey, masterSecret);
        //LinkTemplate template = linkTemplateDemo();
    	
    	if("ANDROID".equals(action)) {
    		 //打开应用
    	   	NotificationTemplate template = notificationTemplate("", title, body);
    	   	//透传模板
    	   	 //TransmissionTemplate template = transmissionTemplateDemo();
    	   	AppMessage message = new AppMessage();
    		message.setData(template);
    		// 设置消息离线，并设置离线时间
    		message.setOffline(true);
    		// 离线有效时间，单位为毫秒，可选
    		message.setOfflineExpireTime(24 * 1000 * 3600);
    		// 设置推送目标条件过滤
    		List<String> appIdList = new ArrayList<String>();
    		List<String> provinceList = new ArrayList<String>();
    		appIdList.add(appId);
    		// 设置省份
    		//provinceList.add("广东省");
    		message.setAppIdList(appIdList);
    		//message.setProvinceList(provinceList);
    		List<String> phoneTypes = new ArrayList<String>();
    	    phoneTypes.add("ANDROID");
    	    //phoneTypes.add("IOS");
    	    message.setPhoneTypeList(phoneTypes);
    		
            IPushResult ret = null;
            try {
                ret = push.pushMessageToApp(message);
            } catch (RequestException e) {
                e.printStackTrace();
               // ret = push.pushMessageToSingle(message, target, e.getRequestId());
            }
            if (ret != null) {
            	result.set("result", ret.getResponse().get("result").toString());
            	result.set("data", ret.getResponse().toString());
                System.out.println(ret.getResponse().toString());
                
                order.set("status", result.get("result"));
            	Db.update("cloud_push", order);
            } else {
                System.out.println("服务器响应异常");
            }
    	} else {
    	   //ios透传模板
    	   	TransmissionTemplate template = IosTransTemplate("", title, body);

    	   	AppMessage message = new AppMessage();
    		message.setData(template);
    		// 设置消息离线，并设置离线时间
    		message.setOffline(true);
    		// 离线有效时间，单位为毫秒，可选
    		message.setOfflineExpireTime(24 * 1000 * 3600);
    		// 设置推送目标条件过滤
    		List<String> appIdList = new ArrayList<String>();
    		List<String> provinceList = new ArrayList<String>();
    		appIdList.add(appId);
    		// 设置省份
    		//provinceList.add("广东省");
    		message.setAppIdList(appIdList);

            IPushResult ret = null;
            try {
                ret = push.pushMessageToApp(message);
            } catch (RequestException e) {
                e.printStackTrace();
               // ret = push.pushMessageToSingle(message, target, e.getRequestId());
            }
            if (ret != null) {
            	result.set("result", ret.getResponse().get("result").toString());
            	result.set("data", ret.getResponse().toString());
            	System.out.println(ret.getResponse().toString());
            	
            	order.set("status", result.get("result"));
            	Db.update("cloud_push", order);
            } else {
                System.out.println("服务器响应异常");
            }
    	}
	   	renderJson(result);
    }
    
    
  //开网页模板
    public LinkTemplate linkTemplateDemo() {
        LinkTemplate template = new LinkTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appkey);
        // 设置通知栏标题与内容
        template.setTitle("请输入通知栏标题");
        template.setText("请输入通知栏内容");
        // 配置通知栏图标
        template.setLogo("icon.png");
        // 配置通知栏网络图标，填写图标URL地址
        template.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        template.setIsRing(true);
        template.setIsVibrate(true);
        template.setIsClearable(true);
        // 设置打开的网址地址
        template.setUrl("http://www.baidu.com");
        
        return template;
    }
    
    //打开应用模板
    public NotificationTemplate notificationTemplate(String tc_value, String title, String body) {
        NotificationTemplate template = new NotificationTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appkey);

        Style0 style = new Style0();
        // 设置通知栏标题与内容
        style.setTitle(title);
        style.setText(body);
        // 配置通知栏图标
        style.setLogo("icon.png");
        // 配置通知栏网络图标
        style.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        style.setRing(true);
        style.setVibrate(true);
        style.setClearable(true);
        template.setStyle(style);

        // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
        template.setTransmissionType(2);
        template.setTransmissionContent("");
        return template;
    }
    
    //穿透消息模板
    public TransmissionTemplate transmissionTemplateDemo() {
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appkey);
        // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
        template.setTransmissionType(2);
        template.setTransmissionContent("请输入需要透传的内容");
        // 设置定时展示时间
        // template.setDuration("2015-01-16 11:40:00", "2015-01-16 12:24:00");
        return template;
    }
    
    //IOS模板
    public TransmissionTemplate IosTransTemplate(String tc_value, String title, String body){
    	TransmissionTemplate template = new TransmissionTemplate();
		template.setAppId(appId);
		template.setAppkey(appkey);
		template.setTransmissionContent(tc_value);
		template.setTransmissionType(2);
		
		APNPayload payload = new APNPayload();
		payload.setBadge(0);//应用图片上显示的数字
		payload.setContentAvailable(1);
		payload.setSound("default");
		// payload.setCategory("$由客户端定义");
		//payload.setAlertMsg(new APNPayload.SimpleAlertMsg("优惠大酬宾"));
		// 字典模式使用下者
		//payload.setAlertMsg(getDictionaryAlertMsg());
		
		DictionaryAlertMsg msg = new APNPayload.DictionaryAlertMsg();
		msg.setTitle(title);
		msg.setBody(body);
		payload.setAlertMsg(msg);
		
		template.setAPNInfo(payload);
		
		return template;
    }
 
 
}
