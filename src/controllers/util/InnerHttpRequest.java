package controllers.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.codec.Base64;

import com.aliyun.oss.HttpMethod;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;

public class InnerHttpRequest {
	private static final Logger logger = LoggerFactory.getLogger(InnerHttpRequest.class);
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
//        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return new java.security.cert.X509Certificate[]{};
//            }
//
//            public void checkClientTrusted(X509Certificate[] chain, String authType) {
//
//            }
//
//            public void checkServerTrusted(X509Certificate[] chain, String authType) {
//
//            }
//        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String doHttpRequest(String url, String param) {
        trustAllHosts();
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");


            if (realUrl.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                https.setHostnameVerifier(DO_NOT_VERIFY);
                https.setRequestMethod(HttpMethod.GET.toString());
                https.connect();
                in = new BufferedReader(new InputStreamReader(https.getInputStream()));
            } else {
                connection.connect();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("InnerHttpRequest doHttpRequest发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public String doPostHttpRequest(String url, String param) {
        trustAllHosts();
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(param.length()));

            if (realUrl.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                https.setHostnameVerifier(DO_NOT_VERIFY);
                https.setRequestMethod(HttpMethod.POST.toString());
                https.connect();
                in = new BufferedReader(new InputStreamReader(https.getInputStream()));

            } else {
                HttpURLConnection http = (HttpURLConnection) connection;
                http.setRequestMethod(HttpMethod.POST.toString());
                http.connect();
                in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            }

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("InnerHttpRequest doHttpRequest发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public String prepareParamStrURLEncoder(String[] keys, Object[] values) {
        StringBuffer params = new StringBuffer();
        String param = "";
        try {
            for (int i = 0; i < keys.length; i++) {
                //logger.info(keys[i] + ":" + values[i]);
                if (values[i].equals("") || values[i] == null) {
                    continue;
                }
                params.append(getUtf8Encoder(keys[i]) + "=" + getUtf8Encoder(values[i].toString()) + "&");

            }
            param = params.substring(0, params.lastIndexOf("&"));

        } catch (Exception e) {
            logger.info("InnerHttpRequest prepareParamStr(String[] keys, String[] values)转换url编码异常！" + e);
            e.printStackTrace();
        }
        //System.out.println("-----prepareParamStrURLEncoder-----" + param);
        return param;
    }

    private String protocol = "https";
    private String host = "dm.aliyuncs.com";
    private String AccessKeyId = "LTAIqgdMfx0UfDTB";
    private String AccessKeySecret = "vx8taDAgJw8e1Hc4teoKKBKF2syghj";//---------
    private String AccountName = "no-replay@webservicemail.logclub.com";//---------
    private String Format = "JSON";
    private String ToAddress = "";//---------
    private String SignatureMethod = "HMAC-SHA1";
    private String SignatureVersion = "1.0";
    private String Version = "2015-11-23";
    private String AddressType = "1";
    private String RegionId = "cn-hangzhou";
    private Boolean ReplyToAddress = Boolean.TRUE;
    private String HtmlBody = "";
    private String Subject = "";
    private String TagName = ""; //标签
    private HttpMethod method = HttpMethod.GET;

    public String httpRequestSendEmail(String action, Record param_rec) {
    	String[] columns = param_rec.getColumnNames();
    	for (int i = 0; i < columns.length; i++) {
			String key = columns[i];
			String value = param_rec.getStr(key);
			if(StrKit.notBlank(value)){
				if("AccessKeyId".equals(key)){
					AccessKeyId = value;
				} else if("AccountName".equals(key)){
					AccountName = value;
				}else if("AccessKeySecret".equals(key)){
					AccessKeySecret = value;
				}else if("HtmlBody".equals(key)){
					HtmlBody = value;
				}else if("TagName".equals(key)){
					TagName = value;
				}else if("Subject".equals(key)){
					Subject = value;
				}else if("ToAddress".equals(key)){
					ToAddress = value;
				}
			}
		}
        logger.info("httpRequestSendEmail start | action : " + action);
        String result = null;
        String utcTime = getUTCTimeStr();
        String signatureNonce = UUID.randomUUID().toString();
        String[] keys = new String[]{"AccessKeyId", "AccountName", "Action", "AddressType", "Format", "HtmlBody", "RegionId", "ReplyToAddress", "SignatureMethod",
                "SignatureNonce", "SignatureVersion", "Subject", "TagName", "Timestamp", "ToAddress", "Version"};
        Object[] values = new Object[]{AccessKeyId, AccountName, action, AddressType, Format, HtmlBody, RegionId, ReplyToAddress, SignatureMethod, signatureNonce
                , SignatureVersion, Subject, TagName, utcTime, ToAddress, Version};

        String signature = null;
        try {
            signature = getSignature(prepareParamStrURLEncoder(keys, values), method);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("-----Signature-----" + signature);
        String[] keys1 = new String[]{"AccessKeyId", "AccountName", "Action", "AddressType", "Format", "HtmlBody", "RegionId", "ReplyToAddress", "Signature", "SignatureMethod",
                "SignatureNonce", "SignatureVersion", "Subject", "TagName", "Timestamp", "ToAddress", "Version"};
        Object[] values1 = new Object[]{AccessKeyId, AccountName, action, AddressType, Format, HtmlBody, RegionId, ReplyToAddress, signature, SignatureMethod, signatureNonce
                , SignatureVersion, Subject, TagName, utcTime, ToAddress, Version};

        String param = prepareParamStrURLEncoder(keys1, values1);

//        System.out.println("-----url-----" + protocol + "://" + host + "/?" + param);
        if (method == HttpMethod.GET) {
            result = doHttpRequest(protocol + "://" + host + "/", param);
        } else {
            result = doPostHttpRequest(protocol + "://" + host + "/", param);
        }

//        System.out.println("-----httpRequestSendEmail result-----" + result);
        return result;
    }

    /**
     * 获取签名
     *
     * @param param
     * @param method
     * @return
     * @throws Exception
     */
    private String getSignature(String param, HttpMethod method) throws Exception {
        String toSign = method + "&" + URLEncoder.encode("/", "utf8") + "&"
                + getUtf8Encoder(param);
//        System.out.println("-----StringToSign-----" + toSign);
        byte[] bytes = HmacSHA1Encrypt(toSign, AccessKeySecret + "&");
        return Base64.encode(bytes);
    }

    private String getUtf8Encoder(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "utf8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\*", "%2A")
                .replaceAll("%7E", "~");
    }

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    private static DateFormat daetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm:ss",成功返回"yyyy-MM-ddTHH:mm:ssZ"<br />
     * 如果获取失败，返回null
     *
     * @return
     */
    public static String getUTCTimeStr() {
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        String date = daetFormat.format(cal.getTime());
        System.out.println("时间------" + date);
        String[] strs = date.split(" ");
        return strs[0] + "T" + strs[1] + "Z";
    }

    public void testUTC() {
        String UTCTimeStr = getUTCTimeStr();
        System.out.println("时间------" + UTCTimeStr);
    }

    /**
     * https://dm.aliyuncs.com/?Action=SingleSendMail
     * &AccountName=test@example.com
     * &ReplyToAddress=true
     * &AddressType=1
     * &ToAddress=test1@example.com
     * &Subject=Subject
     * &HtmlBody=body
     * &<公共请求参数>
     * <p>
     * POST方式需要修改请求头 Content-Type: application/x-www-form-urlencoded
     */
    public void testSingleSendMail() {
        String action = "SingleSendMail";
        httpRequestSendEmail(action, new Record());
    }
    
    
}
