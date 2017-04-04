package controllers.oms.ebaySalesOrder;

import models.UserLogin;

import org.apache.log4j.Logger;

import com.ebay.sdk.ApiAccount;
import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiCredential;
import com.ebay.sdk.CallRetry;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

public class EbayApiContextUtil {
    private ApiContext apiContext = new ApiContext();
    static final String CONFIG_XML_NAME = "ebayConfig.xml";
    static final String CONFIG_SANDBOX = "ebay_sandbox";
    static final String CONFIG_PROD = "ebay";
    private Logger logger = Logger.getLogger(EbayApiContextUtil.class);
    
    public static String configStr = CONFIG_PROD;
    public static String ruName = "";
    public static String signInUrl = "";
    
    public EbayApiContextUtil(long office_id) {
        // Enable Call-Retry.
        CallRetry cr = new CallRetry();
        cr.setMaximumRetries(3);
        cr.setDelayTime(1000); // Wait for one second between each retry-call.
        String[] apiErrorCodes = new String[] { "10007", // "Internal error to the application."
                "931", // "Validation of the authentication token in API request failed."
                "521", // Test of Call-Retry:
                       // "The specified time window is invalid."
                "124" // Test of Call-Retry: "Developer name invalid."
        };
        cr.setTriggerApiErrorCodes(apiErrorCodes);

        // Set trigger exceptions for CallRetry.
        // Build a dummy SdkSoapException so that we can get its Class.
        java.lang.Class[] tcs = new java.lang.Class[] { com.ebay.sdk.SdkSoapException.class };
        cr.setTriggerExceptions(tcs);

        apiContext.setCallRetry(cr);

        apiContext.setTimeout(180000);

        try {
            this.loadConfiguration(office_id);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ApiContext getApiContext() {

        return apiContext;
    }

    public void loadConfiguration(long office_id) throws Exception {
        Record ebayConfig = Db.findFirst("select * from ebay_platform_config where sell_platform=?", configStr);
        
        this.apiContext.setApiServerUrl(ebayConfig.getStr("server_url"));
        this.apiContext.setEpsServerUrl(ebayConfig.getStr("eps_server_url"));
        this.apiContext.setSignInUrl(ebayConfig.getStr("signin_url"));

        
        ApiCredential apiCred = new ApiCredential();
        this.apiContext.setApiCredential(apiCred);

        ApiAccount ac = new ApiAccount();
        apiCred.setApiAccount(ac);

        ac.setDeveloper(ebayConfig.getStr("dev_id"));
        ac.setApplication(ebayConfig.getStr("app_id"));
        ac.setCertificate(ebayConfig.getStr("cert_id"));
        
        logger.debug("ebay config:"+configStr);
        logger.debug("ebay ApiServerUrl:"+ebayConfig.getStr("server_url"));
        logger.debug("ebay EpsServerUrl:"+ebayConfig.getStr("eps_server_url"));
        logger.debug("ebay SignInUrl:"+ebayConfig.getStr("signin_url"));
        logger.debug("ebay DeveloperID:"+ebayConfig.getStr("dev_id"));
        logger.debug("ebay ApplicationID:"+ebayConfig.getStr("app_id"));
        logger.debug("ebay CertID:"+ebayConfig.getStr("cert_id"));
        logger.debug("ebay RuName:"+ebayConfig.getStr("ru_name"));
        
        Record ebayAccount = Db.findFirst("select * from ebay_seller_account where type='"+configStr+"' and office_id=?", office_id);
        if(ebayAccount!=null){
            logger.debug("ebay token:"+ebayAccount.getStr("token"));
            apiCred.seteBayToken(ebayAccount.getStr("token"));
        }

        ruName = ebayConfig.getStr("ru_name");
        this.apiContext.setRuName(ebayConfig.getStr("ru_name"));

    }
}
