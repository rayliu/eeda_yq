package controllers.profile;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;
import models.eeda.profile.Account;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.call.FetchTokenCall;
import com.ebay.sdk.call.GetSessionIDCall;
import com.google.gson.Gson;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import controllers.oms.ebaySalesOrder.EbayApiContextUtil;
import controllers.profile.ali.ClientAuthService;
import controllers.util.DbUtils;
import controllers.util.ali.CommonUtil;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class AliexpressAccountController extends Controller {
    private String host = "gw.api.alibaba.com";
    private Log logger = Log.getLog(AliexpressAccountController.class);

    @Before(EedaMenuInterceptor.class)
    public void index() throws Exception {
        render("/profile/aliexpressAccount/aliexpressAccountList.html");
    }

    // 添加账户页面
    @Before(EedaMenuInterceptor.class)
    public void create() {
        render("/profile/ebayAccount/edit.html");
    }

    @Before(EedaMenuInterceptor.class)
    public void edit() {
        String id = getPara("id");
        if (id != null) {
            Account l = Account.dao.findById(id);
            setAttr("order", l);
        }
        render("/profile/ebayAccount/edit.html");

    }

    @Before(Tx.class)
    public void save() {
        String jsonStr = getPara("params");

        Gson gson = new Gson();
        Map<String, ?> dto = gson.fromJson(jsonStr, HashMap.class);

        Account order = new Account();
        String id = (String) dto.get("id");
        if (StringUtils.isNotEmpty(id)) {
            // update
            order = Account.dao.findById(id);
            DbUtils.setModelValues(dto, order);
            order.update();
        } else {
            // create
            DbUtils.setModelValues(dto, order);
            order.set("office_id",
                    LoginUserController.getLoginUser(this).get("office_id"));
            order.save();
            id = order.getLong("id").toString();
        }
        renderJson(order);
    }

    // 停用账户
    public void del() {
        String id = getPara();
        if (id != null) {
            /* Db.deleteById("fin_account", id); */
            Account account = Account.dao.findById(id);
            Object obj = account.get("is_stop");
            if (obj == null || "".equals(obj) || obj.equals(false)
                    || obj.equals(0)) {
                account.set("is_stop", true);
            } else {
                account.set("is_stop", false);
            }
            account.update();
        }
        render("/profile/ebayAccount/ebayAccountList.html");
    }

    // 列出账户信息
    public void list() {
        String sLimit = "";

        String pageIndex = getPara("draw");
        logger.debug(getPara("length"));
        if (getPara("start") != null && getPara("length") != null
                && getParaToInt("length") != -1) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        UserLogin user = LoginUserController.getLoginUser(this);
        String sql = "select * from aliexpress_seller_account where office_id = "
                + user.getLong("office_id");

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orders = Db.find(sql + condition + " order by id desc "
                + sLimit);
        Map orderListMap = new HashMap();
        orderListMap.put("sEcho", pageIndex);
        orderListMap.put("iTotalRecords", rec.getLong("total"));
        orderListMap.put("iTotalDisplayRecords", rec.getLong("total"));

        orderListMap.put("aaData", orders);

        renderJson(orderListMap);
    }

    /**
     * getloginurl
     * 
     * @param redirect_uri
     * @return
     */
    public String buildSignInUrl(String client_id, String secret_key) {
        // 第一步 获取速卖通api的常量
        String redirect_uri = "https://wms.eeda123.com/aliexpressAccount/auth_accepted";
        String site = "aliexpress";// 国际交易请用"aliexpress";

        String param = (new StringBuilder("client_id=")).append(client_id)
                .append("&site=aliexpress&redirect_uri=").append(redirect_uri)
                .toString();
        String param1 = (new StringBuilder("client_id")).append(client_id)
                .append("redirect_uri").append(redirect_uri).append("site")
                .append(site).toString();
        logger.debug("签名因子:" + param1);

        // String sign = CommonUtil.signatureWithParamsAndUrlPath();
        String sign = HmacUtils.hmacSha1Hex(param1, secret_key).toUpperCase();
        logger.debug("签名结果:" + sign);

        String url = new StringBuilder(
                "http://gw.api.alibaba.com/auth/authorize.htm?").append(param)
                .append("&_aop_signature=").append(sign).toString();
        logger.debug("签名后的url:" + url);
        return url;
    }

    /*
     * 1. 获取临时令牌code 2. 获取code后继续跳转到授权页
     */
    public void aliexpressAuth() throws Exception {
        
        String user_name = getPara("user_name");
        String account_code = getPara("account_code");
        String client_id = getPara("user_key");
        String secret_key = getPara("secret_key");

        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", client_id);// appKey
        params.put("client_secret", secret_key); // appSecret
        params.put("site", "aliexpress");
        params.put("redirect_uri",
                "https://wms.eeda123.com/aliexpressAccount/getCode");

        String sessionId = getRequest().getSession().getId();
        setSessionAttr(sessionId + "_code", client_id);
        // String signInUrl = buildSignInUrl(client_id, secret_key);
        // 返回客户端和Web端授权时获取临时令牌code的url
        String signInUrl = ClientAuthService.getClientAuthUrl(host, params,
                secret_key);

        Record rec = new Record();
        rec.set("code", account_code);
        rec.set("client_id", client_id);
        rec.set("secret", secret_key);
        rec.set("account_name", user_name);
        UserLogin user = LoginUserController.getLoginUser(this);
        rec.set("creator_id", user.get("id"));
        rec.set("create_time", new Date());
        rec.set("office_id", user.get("office_id"));
        
        Record oldRec = Db
                .findFirst(
                        "select * from aliexpress_seller_account where office_id=? and client_id=?",
                        user.get("office_id"), client_id);
        if(oldRec!=null){
            rec.set("id", oldRec.get("id"));
            Db.update("aliexpress_seller_account", rec);
        }else{
            Db.save("aliexpress_seller_account", rec);
        }

        redirect(signInUrl);
    }

    // 响应：获取临时令牌code
    public void getCode() throws Exception {
        UserLogin user = LoginUserController.getLoginUser(this);
        String code = getPara("code");
        String sessionId = getRequest().getSession().getId();
        String client_id = getSessionAttr(sessionId + "_code");
        Record rec = Db
                .findFirst(
                        "select * from aliexpress_seller_account where office_id=? and client_id=?",
                        user.get("office_id"), client_id);
        //去获取token
        String secret_key = rec.getStr("secret");
        logger.debug("client_id:"+client_id);
        logger.debug("client_secret:"+secret_key);
        logger.debug("code:"+code);

        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", client_id);// appKey
        params.put("client_secret", secret_key); // appSecret
        params.put("code", code); // appSecret
        params.put("redirect_uri",
                "https://wms.eeda123.com/aliexpressAccount");
        boolean needRefreshToken = true;
        
        String jsonStr = ClientAuthService.getToken(host, params, needRefreshToken);
        logger.debug(jsonStr);
        Map json = new Gson().fromJson(jsonStr, Map.class);
        if(json.get("access_token")!=null){
            rec.set("access_token", json.get("access_token"));
        }
        rec.set("refresh_token", json.get("refresh_token"));

        DateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss'-0700'");
        Date d = sdf.parse(json.get("refresh_token_timeout").toString());
        rec.set("refresh_token_timeout", d);
        Db.update("aliexpress_seller_account", rec);
        
        redirect("/aliexpressAccount");
    }


    
    // ebay callback this auth_accepted
    @Before(EedaMenuInterceptor.class)
    public void auth_accepted() {
        String user_name = getPara("username");
        String account_code = getSessionAttr(user_name + "_code");
        String sessionID = getSessionAttr(user_name);
        logger.debug("sessionID:" + sessionID);

        /*
         * String strEbayAuthToken = null; try { strEbayAuthToken =
         * fetchTokenCall.fetchToken(); Date expireDate =
         * fetchTokenCall.getHardExpirationTime().getTime();
         * logger.debug("strEbayAuthToken:" + strEbayAuthToken);
         * logger.debug("expireDate:" + expireDate); Record rec = Db
         * .findFirst("select * from ebay_seller_account where account_name='" +
         * user_name + "'"); if (rec != null) { rec.set("token",
         * strEbayAuthToken); rec.set("expire_date", expireDate);
         * Db.update("seller_account", rec); } else { UserLogin user =
         * LoginUserController.getLoginUser(this); Record newRec = new Record();
         * newRec.set("account_name", user_name); newRec.set("code",
         * account_code); newRec.set("token", strEbayAuthToken);
         * newRec.set("expire_date", expireDate); newRec.set("created_time", new
         * Date()); newRec.set("creator", user.getLong("id"));
         * newRec.set("office_id", user.getLong("office_id"));
         * Db.save("seller_account", newRec); }
         * 
         * } catch (Exception e) { e.printStackTrace(); }
         */
        redirect("/aliexpressAccount");
    }
}
