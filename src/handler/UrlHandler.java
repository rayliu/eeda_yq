package handler;

import com.jfinal.handler.Handler;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlHandler extends Handler {

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		LogKit.debug("handle url: " + target);
		
        if(target.indexOf(".")==-1) {
            // actionMapping 根据 target 得到对应的 action

            Map<String, Record> apiMap = CacheKit.get("apiCache", "apiMap");
			if(PropKit.getBoolean("devMode", false)){
				apiMap = null;
			}
            if (apiMap == null){
                List<Record> apiList = Db.find("select * from t_eeda_api where is_delete != 'Y'");
                apiMap = new HashMap<>();
                for (Record rec: apiList){
                    apiMap.put(rec.getStr("url"), rec);
                }
                CacheKit.put("apiCache", "apiMap", apiMap);
            }

			Record rec = apiMap.get(target);
			if (rec != null) {
				LogKit.info("执行 动态配置 代码, originUrl: "+target);
				request.setAttribute("originUrl", target);
				target = target.replaceAll("/", "-").substring(1);
				target = "/eeda_api/" + target;
			} else {
				//判断是否是有template url
//				Record re = Db.findFirst("select * from t_cms_navibar where url = ? and template_id is not null",target);
//				if(re!=null){
//					if(re.getLong("template_id") != null){
//						target = "/template/" + re.getLong("id");
//					} else {
//						//模板二
//						target = "/template2/" + re.getLong("id");
//					}
//				}
				
				LogKit.debug("非动态, 正常执行 controller...");
			}
			
//			if(target.indexOf("/webadmin")==-1) {
//				String[] arr = target.split("/");
//
//				String two_url = "";
//				String three_url = "";
//				
//				if(arr.length>2){
//					two_url = "/"+arr[2];
//				}
//				
//				if(arr.length>3){
//					three_url = "/"+arr[3];
//				}
//				Record twoRe = Db.findFirst("select * from t_channel where url = ? and seq = '2' and is_delete != 'Y'", two_url);
//				
//				if (twoRe!=null) {
//					Record threeRe = Db.findFirst("select * from t_channel where url = ? and seq = '3' and is_delete != 'Y'", three_url);
//					if(threeRe!=null){
//						request.setAttribute("originUrl", "/"+twoRe.getStr("id")+"-"+threeRe.getStr("id"));
//						target = "/"+arr[1]+"/"+twoRe.getStr("id")+"-"+threeRe.getStr("id");
//					}else{
//						if(arr.length<4){
//							request.setAttribute("originUrl", "/"+twoRe.getStr("id"));
//							target = "/"+arr[1]+"/"+twoRe.getStr("id");
//						}
//					}
//				}
//			}
		}

		LogKit.debug("content path:"+target);
		next.handle(target, request, response, isHandled);
	}
}
