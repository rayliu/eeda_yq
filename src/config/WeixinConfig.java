package config;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

import controllers.wx.WeixinApiController;
import controllers.wx.WeixinMsgController;

public class WeixinConfig extends JFinalConfig {
    public void configConstant(Constants me) {
        PropKit.use("app_config.txt");
        me.setDevMode(PropKit.getBoolean("devMode", false));

        // ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
        ApiConfigKit.setDevMode(me.getDevMode());
        // 默认使用的jackson，下面示例是切换到fastJson
//      me.setJsonFactory(new FastJsonFactory());
    }

    public void configRoute(Routes me) {
        me.add("/msg", WeixinMsgController.class);
        me.add("/api", WeixinApiController.class, "/api");
//        me.add("/pay", WeixinPayController.class);
    }

    public void configPlugin(Plugins me) {
        // 1.5 之后支持redis存储access_token、js_ticket，需要先启动RedisPlugin
        // RedisPlugin redisPlugin = new RedisPlugin("weixin", "127.0.0.1");
        // me.add(redisPlugin);
    }
    public void configInterceptor(Interceptors me) {}
    public void configHandler(Handlers me) {}

    public void afterJFinalStart() {
        // 1.5 之后支持redis存储access_token、js_ticket，需要先启动RedisPlugin
//      ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache());
        // 1.6新增的2种初始化
//      ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache(Redis.use("weixin")));
//      ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache("weixin"));
    }
}
