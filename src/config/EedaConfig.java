package config;

import handler.UrlHanlder;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.ArapAccountAuditLog;
import models.Category;
import models.Location;
import models.Office;
import models.Party;
import models.Permission;
import models.Product;
import models.Role;
import models.RolePermission;
import models.UserCustomer;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAir;
import models.eeda.oms.jobOrder.JobOrderAirCargoDesc;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderInsurance;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderSendMail;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;
import models.eeda.oms.truckOrder.TruckOrder;
import models.eeda.oms.truckOrder.TruckOrderArap;
import models.eeda.oms.truckOrder.TruckOrderCargo;
import models.eeda.profile.Account;
import models.eeda.profile.ContainerType;
import models.eeda.profile.Country;
import models.eeda.profile.Currency;
import models.eeda.profile.Custom;
import models.eeda.profile.FinItem;
import models.eeda.profile.Unit;
import models.eeda.profile.Warehouse;
import models.yh.profile.CustomizeField;
import models.yh.profile.OfficeCofig;
import models.yh.profile.Route;

import org.apache.log4j.Logger;
import org.bee.tl.ext.jfinal.BeetlRenderFactory;
import org.h2.tools.Server;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

import controllers.oms.customOrder.CustomOrderController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.oms.jobOrder.JobOrderReportController;
import controllers.oms.planOrder.PlanOrderController;
import controllers.oms.todo.TodoController;
import controllers.oms.truckOrder.TruckOrderController;
import controllers.profile.AccountController;
import controllers.profile.ContainerTypeController;
import controllers.profile.CountryController;
import controllers.profile.CurrencyController;
import controllers.profile.CustomController;
import controllers.profile.FinItemController;
import controllers.profile.PrivilegeController;
import controllers.profile.UnitController;
import controllers.yh.report.ReportController;

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
        
        me.setDevMode(getPropertyToBoolean("devMode", false));
        
    	// ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
    	ApiConfigKit.setDevMode(me.getDevMode());
        
    	

        BeetlRenderFactory templateFactory = new BeetlRenderFactory();
        me.setMainRenderFactory(templateFactory);

        BeetlRenderFactory.groupTemplate.setCharset("utf-8");// 没有这句，html上的汉字会乱码

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

        // me.add("/fileUpload", HelloController.class);
        setScmRoute(me, contentPath);
        
    }
    

	private void setScmRoute(Routes me, String contentPath) {
		// yh project controller
        me.add("/", controllers.eeda.MainController.class, contentPath);
       // me.add("/apidoc", controllers.eeda.DocController.class);
//        me.add("/debug", controllers.profile.LogController.class, contentPath);
        me.add("/warehouse",controllers.profile.WarehouseController.class,contentPath);
        me.add("/loginUser", controllers.profile.LoginUserController.class, contentPath);
        me.add("/unit", UnitController.class, contentPath);
        me.add("/country", CountryController.class, contentPath);
        me.add("/finItem", FinItemController.class, contentPath);
        me.add("/custom", CustomController.class, contentPath);
        me.add("/containerType", ContainerTypeController.class, contentPath);
        //register loginUser
//        me.add("/register",controllers.profile.RegisterUserController.class,contentPath);
        me.add("/reset",controllers.profile.ResetPassWordController.class,contentPath);
//        me.add("/role", controllers.profile.RoleController.class, contentPath);
        me.add("/userRole",controllers.profile.UserRoleController.class,contentPath);
        me.add("/customer", controllers.profile.CustomerController.class, contentPath);
        me.add("/serviceProvider", controllers.profile.ServiceProviderController.class, contentPath);
        me.add("/location", controllers.profile.LocationController.class, contentPath);
        me.add("/office", controllers.profile.OfficeController.class, contentPath);
        me.add("/product", controllers.profile.ProductController.class, contentPath);

//		me.add("/accountAuditLog", AccountAuditLogController.class, contentPath);
		me.add("/account", AccountController.class, contentPath);
		me.add("/privilege", PrivilegeController.class, contentPath);
		//oms管理系统
		me.add("/planOrder", PlanOrderController.class, contentPath);
		me.add("/todo", TodoController.class, contentPath);
		me.add("/jobOrder", JobOrderController.class, contentPath);
		me.add("/jobOrderReport", JobOrderReportController.class, contentPath);
		me.add("/report", ReportController.class, contentPath);
		me.add("/customOrder", CustomOrderController.class, contentPath);
		me.add("/truckOrder", TruckOrderController.class, contentPath);
		me.add("/currency", CurrencyController.class, contentPath);
		
		//ar= account revenue  应收条目处理
        me.add("/chargeConfirmList", controllers.arap.ar.ChargeItemConfirmController.class, contentPath);
        me.add("/chargeCheckOrder", controllers.arap.ar.ChargeCheckOrderController.class, contentPath);
        me.add("/chargePreInvoiceOrder", controllers.arap.ar.ChargePreInvoiceOrderController.class, contentPath);
        me.add("/chargeInvoiceOrder", controllers.arap.ar.ChargeInvoiceOrderController.class, contentPath);
        me.add("/chargeAdjustOrder", controllers.arap.ar.ChargeAdjustOrderController.class, contentPath);
        me.add("/chargeMiscOrder", controllers.arap.ar.chargeMiscOrder.ChargeMiscOrderController.class, contentPath);
        me.add("/chargeAcceptOrder", controllers.arap.ar.ChargeAcceptOrderController.class, contentPath);
        me.add("/chargeConfirm", controllers.arap.ar.ChargeConfirmController.class, contentPath);
        //ap 应付条目处理
        me.add("/costConfirmList", controllers.arap.ap.CostItemConfirmController.class, contentPath);
        me.add("/costCheckOrder", controllers.arap.ap.CostCheckOrderController.class, contentPath);
        me.add("/costPreInvoiceOrder", controllers.arap.ap.CostPreInvoiceOrderController.class, contentPath);
        me.add("/costAdjustOrder", controllers.arap.ap.CostAdjustOrderController.class, contentPath);
        me.add("/costAcceptOrder", controllers.arap.ap.CostAcceptOrderController.class, contentPath);
        me.add("/costConfirm", controllers.arap.ap.CostConfirmController.class, contentPath);
        //应付报销单
        //ßme.add("/costReimbursement", controllers.arap.ap.CostReimbursementOrder.class, contentPath);
        //财务转账单
        me.add("/transferAccountsOrder", controllers.arap.ap.TransferAccountsController.class, contentPath);
//        me.add("/reimbursementItem", controllers.yh.ReimbursementItemController.class, contentPath);
        //手工成本单
        me.add("/costMiscOrder", controllers.arap.ap.costMiscOrder.CostMiscOrderController.class, contentPath);
        
        me.add("/inOutMiscOrder", controllers.arap.financial.inOutOrder.InOutMiscOrderController.class, contentPath);
        
        //预付单
        me.add("/costPrePayOrder", controllers.arap.ap.PrePayOrderController.class, contentPath);
        //audit log
        me.add("/accountAuditLog", controllers.arap.AccountAuditLogController.class, contentPath);
	}

    @Override
	public void configPlugin(Plugins me) {
        // 加载Shiro插件, for backend notation, not for UI
    	me.add(new ShiroPlugin(routes));
    	
    	//job启动
//    	SchedulerPlugin sp = new SchedulerPlugin("job.properties");
//        me.add(sp);
    	
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
        arp.addMapping("unit", Unit.class);
        arp.addMapping("country", Country.class);
        arp.addMapping("fin_item", FinItem.class);
        arp.addMapping("custom", Custom.class);
        arp.addMapping("container_type", ContainerType.class);
        arp.addMapping("party", Party.class);
//        arp.addMapping("contact", Contact.class);       
        arp.addMapping("route", Route.class);
        arp.addMapping("product", Product.class);
        arp.addMapping("category", Category.class);
        arp.addMapping("location", Location.class);

        //基本数据用户网点
        arp.addMapping("user_office", UserOffice.class);
        arp.addMapping("user_customer", UserCustomer.class);
        
        arp.addMapping("customize_field", CustomizeField.class);
        arp.addMapping("office_config", OfficeCofig.class);
        
        //中转仓
        arp.addMapping("warehouse", Warehouse.class);
        arp.addMapping("fin_account", Account.class);
        arp.addMapping("arap_account_audit_log", ArapAccountAuditLog.class);
        

        //oms
        arp.addMapping("plan_order", PlanOrder.class);
        arp.addMapping("plan_order_item", PlanOrderItem.class);
        
        arp.addMapping("job_order", JobOrder.class);
        arp.addMapping("job_order_shipment_item", JobOrderShipmentItem.class);
        arp.addMapping("job_order_arap", JobOrderArap.class);
        arp.addMapping("job_order_shipment", JobOrderShipment.class);
        arp.addMapping("job_order_doc", JobOrderDoc.class);
        arp.addMapping("job_order_air", JobOrderAir.class);
        arp.addMapping("job_order_air_item", JobOrderAirItem.class);
        arp.addMapping("job_order_air_cargodesc", JobOrderAirCargoDesc.class);
        arp.addMapping("job_order_land_item", JobOrderLandItem.class);
        arp.addMapping("job_order_custom", JobOrderCustom.class);
        arp.addMapping("job_order_insurance", JobOrderInsurance.class);
        arp.addMapping("job_order_sendMail", JobOrderSendMail.class);
        
        arp.addMapping("truck_order", TruckOrder.class);
        arp.addMapping("truck_order_arap", TruckOrderArap.class);
        arp.addMapping("truck_order_cargo", TruckOrderCargo.class);
        arp.addMapping("currency", Currency.class);
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
    	}
        //me.add(new SetAttrLoginUserInterceptor());
    }

    @Override
	public void configHandler(Handlers me) {
        me.add(new UrlSkipHandler("/apidoc.*", false));
        me.add(new UrlHanlder());
        
    }
    
}
