package controllers.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;

public class PhoneAddress {
	public static Record check2record(String phone){
		// TODO Auto-generated method stub
		Record rec = new Record();
		String host = "https://api04.aliyun.venuscn.com";
	    String path = "/mobile";
	    String method = "GET";
	    String appcode = "c55e6a37345948c69b974fbb708d61bd";
	    Map<String, String> headers = new HashMap<String, String>();
	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
	    headers.put("Authorization", "APPCODE " + appcode);
	    Map<String, String> querys = new HashMap<String, String>();
	    querys.put("mobile", phone);
	
	    String city = null;
	    try {
	    	/**
	    	* 重要提示如下:
	    	* HttpUtils请从
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
	    	* 下载
	    	*
	    	* 相应的依赖请参照
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
	    	*/
	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
	    	//System.out.println(response.toString());
	    	////获取response的body
	    	String josnStr = EntityUtils.toString(response.getEntity());
	    	System.out.println(josnStr);
	
	    	Gson gson = new Gson();  
	        Map<String, ?> dto= gson.fromJson(josnStr, HashMap.class);  
	        String msg = (String) dto.get("msg");
	        String ret = (String) dto.get("ret");
	        rec.set("status", ret);
	        rec.set("msg", msg);
	        
	    	if("success".equals(msg)){
	    		Map<String, Object> result =  (Map<String, Object>) dto.get("data");
	    		rec = rec.setColumns(result);
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return rec;
	}
	
	
	public static String check2str(String phone){
		// TODO Auto-generated method stub
		String result = null;
		String host = "https://jisushouji.market.alicloudapi.com";
	    String path = "/shouji/query";
	    String method = "GET";
	    String appcode = "c55e6a37345948c69b974fbb708d61bd";
	    Map<String, String> headers = new HashMap<String, String>();
	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
	    headers.put("Authorization", "APPCODE " + appcode);
	    Map<String, String> querys = new HashMap<String, String>();
	    querys.put("shouji", phone);
	
	    try {
	    	/**
	    	* 重要提示如下:
	    	* HttpUtils请从
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
	    	* 下载
	    	*
	    	* 相应的依赖请参照
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
	    	*/
	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
	    	//System.out.println(response.toString());
	    	////获取response的body
	    	result = EntityUtils.toString(response.getEntity());
	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return result;
	}
	
	public static String check2city(String phone){
		// TODO Auto-generated method stub
		Record rec = new Record();
		String host = "https://api04.aliyun.venuscn.com";
	    String path = "/mobile";
	    String method = "GET";
	    String appcode = "c55e6a37345948c69b974fbb708d61bd";
	    Map<String, String> headers = new HashMap<String, String>();
	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
	    headers.put("Authorization", "APPCODE " + appcode);
	    Map<String, String> querys = new HashMap<String, String>();
	    querys.put("mobile", phone);
	
	    String city = null;
	    try {
	    	/**
	    	* 重要提示如下:
	    	* HttpUtils请从
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
	    	* 下载
	    	*
	    	* 相应的依赖请参照
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
	    	*/
	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
	    	//System.out.println(response.toString());
	    	////获取response的body
	    	String josnStr = EntityUtils.toString(response.getEntity());
	    	System.out.println(josnStr);
	
	    	Gson gson = new Gson();  
	        Map<String, ?> dto= gson.fromJson(josnStr, HashMap.class);  
	        String msg = (String) dto.get("msg");
	        String status = (String) dto.get("ret");
	        rec.set("status", status);
	        rec.set("msg", msg);
	    	if("success".equals(msg)){
	    		Map<String, Object> result =  (Map<String, Object>) dto.get("data");
	    		rec = rec.setColumns(result);
	    		city = rec.getStr("city");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return city;
	}
}
