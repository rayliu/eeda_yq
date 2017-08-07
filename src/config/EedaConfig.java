package config;

import handler.UrlHanlder;
import interceptor.ActionCostInterceptor;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.Category;
import models.Location;
import models.Office;
import models.Party;
import models.Permission;
import models.Role;
import models.RolePermission;
import models.UserCustomer;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;
import models.eeda.profile.Country;
import models.eeda.profile.Module;
import models.eeda.profile.ModuleRole;
import models.eeda.profile.OfficeConfig;
import models.eeda.profile.OrderNoSeq;
import models.eeda.profile.Unit;
import models.wms.GateIn;
import models.wms.GateOut;
import models.wms.InvCheckOrder;
import models.wms.Wmsproduct;

import org.apache.log4j.Logger;
import org.beetl.ext.jfinal.BeetlRenderFactory;
import org.h2.tools.Server;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.scheduler.SchedulerPlugin;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

import controllers.app.AppControllerForMobile;
import controllers.eeda.MainController;
import controllers.eeda.ModuleController;
import controllers.msg.EwmsDashBoardController;
import controllers.oms.jobOrder.JobOrderControllerForMobile;
import controllers.oms.planOrder.PlanOrderControllerForMobile;
import controllers.profile.AccountController;
import controllers.profile.ContainerTypeController;
import controllers.profile.CountryController;
import controllers.profile.CustomController;
import controllers.profile.FinItemController;
import controllers.profile.PrivilegeController;
import controllers.profile.TradeItemController;
import controllers.profile.UnitController;
import controllers.wms.ChangePartNoController;
import controllers.wms.ErrorReportController;
import controllers.wms.GateInController;
import controllers.wms.GateOutController;
import controllers.wms.GateOutOrderController;
import controllers.wms.InvCheckOrderController;
import controllers.wms.InventoryController;
import controllers.wms.ProductController;
import controllers.wms.importOrder.ImportOrder;
import controllers.wx.WeixinApiController;
import controllers.wx.WeixinMsgController;
import controllers.wx.WxController;

public class EedaConfig extends JFinalConfig {
    private Logger logger = Logger.getLogger(EedaConfig.class);

    private static final String H2 = "H2";
    private static final String Mysql = "Mysql";
    private static final String ProdMysql = "ProdMysql";
      
    public static String mailUser;
    public static String mailPwd;
    
    /**
     * 
     * 供Shiro插件使用 。
     */
    Routes routes;

    C3p0Plugin cp;
    ActiveRecordPlugin arp;

    @Override
	public void configConstant(Constants me) {
        //加载配置文件    	
        loadPropertyFile("app_config.txt");
        PropKit.use("app_config.txt");
        
        me.setDevMode(getPropertyToBoolean("devMode", false));
        
    	// 微信 ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
    	ApiConfigKit.setDevMode(me.getDevMode());
        
    	

        BeetlRenderFactory templateFactory = new BeetlRenderFactory();
        me.setMainRenderFactory(templateFactory);

        // 注册后，可以使beetl html中使用shiro tag
        BeetlRenderFactory.groupTemplate.registerFunctionPackage("shiro", new ShiroExt());

        //没有权限时跳转到login
        me.setErrorView(401, "/eeda/noLogin.html");//401 authenticate err
        me.setErrorView(403, "/eeda/noPermission.html");// authorization err
        
        //内部出错跳转到对应的提示页面，需要考虑提供更详细的信息。
        me.setError404View("/eeda/err404.html");
        me.setError500View("/eeda/err500.html");
        
        // me.setErrorView(503, "/login.html");
        // get name representing the running Java virtual machine.
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
        // get pid
        String pid = name.split("@")[0];
        logger.info("Pid is: " + pid);
    }

    @Override
	public void configRoute(Routes me) {
        this.routes = me;

        //TODO: 为之后去掉 yh做准备
        String contentPath="/";//"yh";

        setAppRoute(me, contentPath);
        setScmRoute(me, contentPath);
        
        
    }
    private void setAppRoute(Routes me, String contentPath) {
        me.add("/app", AppControllerForMobile.class);
        me.add("/app/jobOrder", JobOrderControllerForMobile.class);
        me.add("/app/planOrder", PlanOrderControllerForMobile.class);
    }

	private void setScmRoute(Routes me, String contentPath) {
		// yh project controller
        me.add("/", MainController.class, contentPath);
        me.add("/module", ModuleController.class, contentPath);
       // me.add("/apidoc", controllers.eeda.DocController.class);基础数据
        
        me.add("/eWmsDashBoard", EwmsDashBoardController.class, contentPath);
        me.add("/tradeItem", TradeItemController.class, contentPath);
        me.add("/sys", controllers.eeda.SysInfoController.class, contentPath);
        me.add("/warehouse",controllers.profile.WarehouseController.class,contentPath);
        me.add("/loginUser", controllers.profile.LoginUserController.class, contentPath);
        me.add("/unit", UnitController.class, contentPath);
        me.add("/country", CountryController.class, contentPath);
        me.add("/finItem", FinItemController.class, contentPath);
        me.add("/custom", CustomController.class, contentPath);
        me.add("/containerType", ContainerTypeController.class, contentPath);
        //register loginUser
//        me.add("/register",controllers.profile.RegisterUserController.class,contentPath);
        //me.add("/reset",controllers.profile.ResetPassWordController.class,contentPath);
        me.add("/role", controllers.profile.RoleController.class, contentPath);
        me.add("/userRole",controllers.profile.UserRoleController.class,contentPath);
        me.add("/location", controllers.profile.LocationController.class, contentPath);
        me.add("/office", controllers.profile.OfficeController.class, contentPath);
        me.add("/product", controllers.profile.ProductController.class, contentPath);
        me.add("/customerRemind", controllers.report.CustomerRemindController.class, contentPath);

//		me.add("/accountAuditLog", AccountAuditLogController.class, contentPath);
		me.add("/account", AccountController.class, contentPath);
		me.add("/privilege", PrivilegeController.class, contentPath);
		
        
        //仓库管理模块wms
        me.add("/importOrder", ImportOrder.class, contentPath);
        me.add("/gateIn", GateInController.class, contentPath);
        me.add("/gateOut", GateOutController.class, contentPath);
        me.add("/gateOutOrder", GateOutOrderController.class, contentPath);
        me.add("/invCheckOrder", InvCheckOrderController.class, contentPath);
        me.add("/wmsproduct", ProductController.class, contentPath);
        me.add("/inventory", InventoryController.class, contentPath);
        me.add("/errorReport", ErrorReportController.class, contentPath);
        me.add("/changePartNo", ChangePartNoController.class, contentPath);
  
        me.add("/wx", WxController.class);
        
        me.add("/msg", WeixinMsgController.class);
        me.add("/api", WeixinApiController.class, "/api");
	}

    @Override
	public void configPlugin(Plugins me) {
        // 加载Shiro插件, for backend notation, not for UI
    	me.add(new ShiroPlugin(routes));
    	
    	//job启动
    	SchedulerPlugin sp = new SchedulerPlugin("import_job.properties");
        me.add(sp);
    	
        mailUser = getProperty("mail_user_name");
        mailPwd = getProperty("mail_pwd");
        // H2 or mysql
        initDBconnector();

        me.add(cp);

        arp = new ActiveRecordPlugin(cp);
        arp.setShowSql(true);// 控制台打印Sql
//        SqlReporter.setLog(true);// log4j 打印Sql
        me.add(arp);

        arp.setDialect(new MysqlDialect());
        // 配置属性名(字段名)大小写不敏感容器工厂
        arp.setContainerFactory(new CaseInsensitiveContainerFactory(false));

        setTableMapping();
        
    }

    private void setTableMapping() {
        arp.addMapping("office", Office.class);
        arp.addMapping("user_login", UserLogin.class);
        arp.addMapping("role", Role.class);
        arp.addMapping("permission", Permission.class);
        arp.addMapping("user_role", UserRole.class);
        arp.addMapping("role_permission", RolePermission.class);
        arp.addMapping("eeda_modules", Module.class);
        arp.addMapping("module_role", ModuleRole.class);
        arp.addMapping("unit", Unit.class);
        arp.addMapping("country", Country.class);
       
        arp.addMapping("party", Party.class);

//        arp.addMapping("route", Route.class);
        arp.addMapping("category", Category.class);
        arp.addMapping("location", Location.class);
        arp.addMapping("order_no_seq", OrderNoSeq.class);
        
        //基本数据用户网点
        arp.addMapping("user_office", UserOffice.class);
        arp.addMapping("user_customer", UserCustomer.class);
        
        arp.addMapping("office_config", OfficeConfig.class);
      
        
        //仓库管理模块wms
        arp.addMapping("wmsproduct", Wmsproduct.class); 
        arp.addMapping("gate_in", GateIn.class); 
        arp.addMapping("gate_out", GateOut.class); 
        arp.addMapping("inv_check_order", InvCheckOrder.class); 
    }

    private void initDBconnector() {
        String dbType = getProperty("dbType");
        String url = getProperty("dbUrl");
        String username = getProperty("username");
        String pwd = getProperty("pwd");

        if (H2.equals(dbType)) {
            connectH2();
        } else {
        	logger.info("DB url: " + url);
            cp = new C3p0Plugin(url, username, pwd);
            //DataInitUtil.initH2Tables(cp);

        }

    }

    private void connectH2() {
        // 这个启动web console以方便通过localhost:8082访问数据库
        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cp = new C3p0Plugin("jdbc:h2:mem:eeda;", "sa", "");
        // cp = new C3p0Plugin("jdbc:h2:data/sample;IFEXISTS=TRUE;", "sa", "");
        cp.setDriverClass("org.h2.Driver");
//        DataInitUtil.initH2Tables(cp);
    }

    @Override
	public void configInterceptor(Interceptors me) {
    	if("Y".equals(getProperty("is_check_permission"))){
    		logger.debug("is_check_permission = Y");
         	me.add(new ShiroInterceptor());
         	//针对shiro 设置错误页面
            ShiroKit.setUnauthorizedUrl("/noPermission");
    	}
    	// 添加控制层全局拦截器, 每次进入页面时构造菜单项
        //me.addGlobalActionInterceptor(new EedaMenuInterceptor());
        me.add(new ActionCostInterceptor());
    }

    @Override
	public void configHandler(Handlers me) {
        me.add(new UrlSkipHandler("/apidoc.*", false));
        me.add(new UrlHanlder());
        
    }
    
}
