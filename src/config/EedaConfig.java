package config;

import handler.UrlHanlder;
import interceptor.ActionCostInterceptor;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

import models.AppInvoiceDoc;
import models.ArapAccountAuditLog;
import models.ArapChargeApplication;
import models.ArapChargeInvoice;
import models.ArapChargeItem;
import models.ArapChargeOrder;
import models.ArapCostApplication;
import models.ArapCostItem;
import models.ArapCostOrder;
import models.ArapMiscCostOrder;
import models.Category;
import models.ChargeApplicationOrderRel;
import models.CostApplicationOrderRel;
import models.CustomArapCostItem;
import models.CustomArapCostOrder;
import models.Location;
import models.Office;
import models.Party;
import models.PartyMark;
import models.Permission;
import models.Product;
import models.RateContrast;
import models.Role;
import models.RolePermission;
import models.SpAirTransport;
import models.SpAirTransportItem;
import models.SpBulkCargo;
import models.SpBulkCargoItem;
import models.SpCargoInsurance;
import models.SpCustom;
import models.SpInternalTrade;
import models.SpLandTransport;
import models.SpLandTransportItem;
import models.SpOceanCargo;
import models.SpOceanCargoItem;
import models.SpPickingCrane;
import models.SpStorage;
import models.TradeItem;
import models.UserCustomer;
import models.UserLogin;
import models.UserOffice;
import models.UserRole;
import models.eeda.cms.CustomArapChargeItem;
import models.eeda.cms.CustomArapChargeOrder;
import models.eeda.cms.CustomPlanOrder;
import models.eeda.cms.CustomPlanOrderArap;
import models.eeda.cms.CustomPlanOrderItem;
import models.eeda.oms.PlanOrder;
import models.eeda.oms.PlanOrderItem;
import models.eeda.oms.jobOrder.JobOrder;
import models.eeda.oms.jobOrder.JobOrderAir;
import models.eeda.oms.jobOrder.JobOrderAirCargoDesc;
import models.eeda.oms.jobOrder.JobOrderAirItem;
import models.eeda.oms.jobOrder.JobOrderArap;
import models.eeda.oms.jobOrder.JobOrderCustom;
import models.eeda.oms.jobOrder.JobOrderDoc;
import models.eeda.oms.jobOrder.JobOrderExpress;
import models.eeda.oms.jobOrder.JobOrderInsurance;
import models.eeda.oms.jobOrder.JobOrderLandItem;
import models.eeda.oms.jobOrder.JobOrderOceanTemplate;
import models.eeda.oms.jobOrder.JobOrderSendMail;
import models.eeda.oms.jobOrder.JobOrderSendMailTemplate;
import models.eeda.oms.jobOrder.JobOrderShipment;
import models.eeda.oms.jobOrder.JobOrderShipmentHead;
import models.eeda.oms.jobOrder.JobOrderShipmentItem;
import models.eeda.oms.truckOrder.TruckOrder;
import models.eeda.oms.truckOrder.TruckOrderArap;
import models.eeda.oms.truckOrder.TruckOrderCargo;
import models.eeda.profile.Account;
import models.eeda.profile.ContainerType;
import models.eeda.profile.Country;
import models.eeda.profile.Currency;
import models.eeda.profile.CurrencyRate;
import models.eeda.profile.Custom;
import models.eeda.profile.FinItem;
import models.eeda.profile.Module;
import models.eeda.profile.ModuleRole;
import models.eeda.profile.OrderNoSeq;
import models.eeda.profile.Unit;
import models.eeda.profile.Warehouse;
import models.eeda.tms.TransJobOrder;
import models.eeda.tms.TransJobOrderArap;
import models.eeda.tms.TransJobOrderLandItem;
import models.yh.profile.Carinfo;
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
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

import controllers.app.AppControllerForMobile;
import controllers.cms.jobOrder.CustomJobOrderController;
import controllers.cms.jobOrder.CustomPlanOrderController;
import controllers.eeda.ModuleController;
import controllers.msg.YqDashBoardController;
import controllers.oms.customOrder.CustomOrderController;
import controllers.oms.jobOrder.JobOrderController;
import controllers.oms.jobOrder.JobOrderControllerForMobile;
import controllers.oms.jobOrder.JobOrderReportController;
import controllers.oms.planOrder.PlanOrderController;
import controllers.oms.planOrder.PlanOrderControllerForMobile;
import controllers.oms.todo.TodoController;
import controllers.oms.truckOrder.TruckOrderController;
import controllers.profile.AccountController;
import controllers.profile.ContainerTypeController;
import controllers.profile.CountryController;
import controllers.profile.CurrencyController;
import controllers.profile.CurrencyRateController;
import controllers.profile.CustomController;
import controllers.profile.FinItemController;
import controllers.profile.PrivilegeController;
import controllers.profile.TradeItemController;
import controllers.profile.UnitController;
import controllers.report.OrderStatusController;
import controllers.tms.jobOrder.TransJobOrderController;
import controllers.tms.jobOrder.TransOrderShortCutController;
import controllers.tms.planOrder.TransPlanOrderController;

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
        
    	// 微信 ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
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
        me.add("/", controllers.eeda.MainController.class, contentPath);
        me.add("/module", ModuleController.class, contentPath);
        me.add("/dashBoard", YqDashBoardController.class);
        
        me.add("/tradeItem", TradeItemController.class, contentPath);
        me.add("/sys", controllers.eeda.SysInfoController.class, contentPath);
        me.add("/warehouse",controllers.profile.WarehouseController.class,contentPath);
        me.add("/loginUser", controllers.profile.LoginUserController.class, contentPath);
        me.add("/unit", UnitController.class, contentPath);
        me.add("/country", CountryController.class, contentPath);
        me.add("/finItem", FinItemController.class, contentPath);
        me.add("/custom", CustomController.class, contentPath);
        me.add("/carInfo", controllers.profile.CarinfoController.class, contentPath);
        me.add("/containerType", ContainerTypeController.class, contentPath);
        //register loginUser
//        me.add("/register",controllers.profile.RegisterUserController.class,contentPath);
        me.add("/reset",controllers.profile.ResetPassWordController.class,contentPath);
        me.add("/role", controllers.profile.RoleController.class, contentPath);
        me.add("/userRole",controllers.profile.UserRoleController.class,contentPath);
        me.add("/customer", controllers.profile.CustomerController.class, contentPath);
        me.add("/serviceProvider", controllers.profile.ServiceProviderController.class, contentPath);
        me.add("/supplierRating", controllers.profile.SupplierRatingController.class, contentPath);
        me.add("/location", controllers.profile.LocationController.class, contentPath);
        me.add("/office", controllers.profile.OfficeController.class, contentPath);
        me.add("/product", controllers.profile.ProductController.class, contentPath);
        me.add("/customerRemind", controllers.report.CustomerRemindController.class, contentPath);

//		me.add("/accountAuditLog", AccountAuditLogController.class, contentPath);
		me.add("/account", AccountController.class, contentPath);
		me.add("/privilege", PrivilegeController.class, contentPath);
		//oms管理系统
		me.add("/planOrder", PlanOrderController.class, contentPath);
		me.add("/todo", TodoController.class, contentPath);
		me.add("/jobOrder", JobOrderController.class, contentPath);
		
		me.add("/jobOrderReport", JobOrderReportController.class, contentPath);
//		me.add("/report", ReportController.class, contentPath);
		me.add("/customOrder", CustomOrderController.class, contentPath);
		me.add("/truckOrder", TruckOrderController.class, contentPath);
		me.add("/currency", CurrencyController.class, contentPath);
		me.add("/currencyRate", CurrencyRateController.class, contentPath);
		
		
		//cms 报关管理
		me.add("/customJobOrder", CustomJobOrderController.class, contentPath);
		me.add("/customPlanOrder", CustomPlanOrderController.class, contentPath);
		me.add("/cmsChargeConfirm", controllers.arap.cmsAr.CmsChargeConfirmController.class, contentPath);
		me.add("/cmsChargeCheckOrder", controllers.arap.cmsAr.CmsChargeCheckOrderController.class, contentPath);
		me.add("/cmsCostCheckOrder", controllers.arap.cmsAr.CmsCostCheckOrderController.class, contentPath);
		me.add("/expenseEntry", controllers.cms.expenseEntry.ExpenseEntryController.class, contentPath);
		
		
		//tms 车队系统
		me.add("/transJobOrder", TransJobOrderController.class, contentPath);
		me.add("/transPlanOrder", TransPlanOrderController.class, contentPath);
		me.add("/transOrderShortCut", TransOrderShortCutController.class, contentPath);
		
		//ar= account revenue  应收条目处理
		me.add("/chargeRequest", controllers.arap.ar.chargeRequest.ChargeReuqestrController.class, contentPath);
		me.add("/costRequest", controllers.arap.ar.costRequest.CostReuqestrController.class, contentPath);
        me.add("/chargeConfirmList", controllers.arap.ar.ChargeItemConfirmController.class, contentPath);
        me.add("/chargeCheckOrder", controllers.arap.ar.ChargeCheckOrderController.class, contentPath);
        me.add("/chargeCheckOrderList", controllers.arap.ar.ChargeCheckOrderController.class, contentPath);
        me.add("/chargePreInvoiceOrder", controllers.arap.ar.ChargePreInvoiceOrderController.class, contentPath);
        me.add("/chargeInvoiceOrder", controllers.arap.ar.ChargeInvoiceOrderController.class, contentPath);
        me.add("/chargeAdjustOrder", controllers.arap.ar.ChargeAdjustOrderController.class, contentPath);
        me.add("/chargeMiscOrder", controllers.arap.ar.chargeMiscOrder.ChargeMiscOrderController.class, contentPath);
        me.add("/chargeAcceptOrder", controllers.arap.ar.ChargeAcceptOrderController.class, contentPath);
        me.add("/chargeConfirm", controllers.arap.ar.ChargeConfirmController.class, contentPath);
        
        //ap 应付条目处理
        me.add("/costConfirmList", controllers.arap.ap.CostItemConfirmController.class, contentPath);
        me.add("/costCheckOrder", controllers.arap.ap.CostCheckOrderController.class, contentPath);
        me.add("/costAdjustOrder", controllers.arap.ap.CostAdjustOrderController.class, contentPath);
        me.add("/costAcceptOrder", controllers.arap.ap.CostAcceptOrderController.class, contentPath);
        me.add("/costConfirm", controllers.arap.ap.CostConfirmController.class, contentPath);
        
        //运营报表
        me.add("/arapReport", controllers.arap.ArapReportController.class, contentPath);
        me.add("/profitReport", controllers.report.ProfitReportController.class, contentPath);
        me.add("/balanceReport", controllers.report.BalanceReportController.class, contentPath);
        me.add("/customReport", controllers.report.CustomReportController.class, contentPath);
        me.add("/chargeBalanceReport",controllers.arap.ChargeBalanceReportController.class, contentPath);
        me.add("/costBalanceReport",controllers.arap.CostBalanceReportController.class, contentPath);
        me.add("/profitAndPaymentRate",controllers.arap.ProfitAndPaymentRateController.class, contentPath);
        me.add("/accountAging",controllers.arap.AccountAging.class, contentPath);
        me.add("/billProfitAndPayment",controllers.arap.BillProfitAndPaymentController.class, contentPath);
        me.add("/oceanRouteReport", controllers.report.OceanRouteReportController.class, contentPath); 
        me.add("/airRouteReport", controllers.report.AirRouteReportController.class, contentPath); 
        
        //合同管理
        me.add("/supplierContract", controllers.contractManagement.SupplierContractController.class, contentPath);
        
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
        
        //发布公告
        me.add("/msgBoard", controllers.msg.MsgBoardController.class, contentPath);
        me.add("/orderStatus", OrderStatusController.class, contentPath);
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
        arp.addMapping("eeda_modules", Module.class);
        arp.addMapping("module_role", ModuleRole.class);
        arp.addMapping("unit", Unit.class);
        arp.addMapping("country", Country.class);
        arp.addMapping("trade_item", TradeItem.class);
        arp.addMapping("fin_item", FinItem.class);
        arp.addMapping("custom", Custom.class);
        arp.addMapping("container_type", ContainerType.class);
        arp.addMapping("party", Party.class);
        arp.addMapping("party_mark", PartyMark.class);
//        arp.addMapping("contact", Contact.class);       
        arp.addMapping("route", Route.class);
        arp.addMapping("product", Product.class);
        arp.addMapping("category", Category.class);
        arp.addMapping("location", Location.class);
        arp.addMapping("order_no_seq", OrderNoSeq.class);
        arp.addMapping("carinfo", Carinfo.class);
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
        arp.addMapping("job_order_shipment_head", JobOrderShipmentHead.class);
        arp.addMapping("job_order_ocean_template", JobOrderOceanTemplate.class);
        arp.addMapping("job_order_sendmail_template", JobOrderSendMailTemplate.class);
        arp.addMapping("job_order_express", JobOrderExpress.class);
        
        arp.addMapping("truck_order", TruckOrder.class);
        arp.addMapping("truck_order_arap", TruckOrderArap.class);
        arp.addMapping("truck_order_cargo", TruckOrderCargo.class);
        arp.addMapping("currency", Currency.class);
        arp.addMapping("currency_rate", CurrencyRate.class);
        
        //财务
        arp.addMapping("arap_cost_order", ArapCostOrder.class);
        arp.addMapping("arap_cost_item", ArapCostItem.class);
        arp.addMapping("arap_misc_cost_order", ArapMiscCostOrder.class);
        arp.addMapping("arap_cost_application_order", ArapCostApplication.class);
        arp.addMapping("cost_application_order_rel", CostApplicationOrderRel.class);
        arp.addMapping("arap_charge_order", ArapChargeOrder.class);
        arp.addMapping("arap_charge_item", ArapChargeItem.class);
        arp.addMapping("arap_charge_invoice", ArapChargeInvoice.class);
        arp.addMapping("arap_charge_application_order", ArapChargeApplication.class);
        arp.addMapping("charge_application_order_rel", ChargeApplicationOrderRel.class);
        arp.addMapping("app_invoice_doc", AppInvoiceDoc.class);
        arp.addMapping("rate_contrast", RateContrast.class);
        
        //cms 报关管理		
        arp.addMapping("custom_plan_order", CustomPlanOrder.class);
        arp.addMapping("custom_plan_order_item", CustomPlanOrderItem.class);
        arp.addMapping("custom_arap_charge_order", CustomArapChargeOrder.class);
        arp.addMapping("custom_arap_charge_item", CustomArapChargeItem.class);
        arp.addMapping("custom_arap_cost_order", CustomArapCostOrder.class);
        arp.addMapping("custom_arap_cost_item", CustomArapCostItem.class);
        arp.addMapping("custom_plan_order_arap", CustomPlanOrderArap.class);
        
        //tms 车队管理
        arp.addMapping("trans_job_order", TransJobOrder.class);
        arp.addMapping("trans_job_order_land_item", TransJobOrderLandItem.class);
        arp.addMapping("trans_job_order_arap", TransJobOrderArap.class);
        
        //保险供应商
        arp.addMapping("sp_ocean_cargo", SpOceanCargo.class);
        arp.addMapping("sp_ocean_cargo_item", SpOceanCargoItem.class);
        arp.addMapping("sp_internal_trade", SpInternalTrade.class);
        arp.addMapping("sp_bulk_cargo_item", SpBulkCargoItem.class);
        arp.addMapping("sp_bulk_cargo", SpBulkCargo.class);  
        
        arp.addMapping("sp_land_transport", SpLandTransport.class); 
        arp.addMapping("sp_land_transport_item", SpLandTransportItem.class); 
        arp.addMapping("sp_storage", SpStorage.class);  
        arp.addMapping("sp_air_transport", SpAirTransport.class);  
        arp.addMapping("sp_air_transport_item", SpAirTransportItem.class);  
        arp.addMapping("sp_custom", SpCustom.class);  
        arp.addMapping("sp_picking_crane", SpPickingCrane.class);  
        arp.addMapping("sp_cargo_insurance", SpCargoInsurance.class); 
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
