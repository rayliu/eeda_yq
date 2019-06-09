
package controllers.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;

/**
 * EedaHttpKit
 * 主要是处理文件下载
 */
public class EedaHttpKit {
    private static Log logger = Log.getLog(EedaHttpKit.class);
	private EedaHttpKit() {}
	
	public static String decodeHeadInfo(String info) {  
		String value = "";
		String afterInfo = info;
		while(afterInfo.length() > 0){
			if(afterInfo.contains("\\u")){
				int index = afterInfo.indexOf("\\u");
				if(index > 0){
					value += afterInfo.substring(0,index);
				}
				String m_code = afterInfo.substring(index+2, index+6);  //单个
				char ch = (char) Integer.parseInt(m_code, 16);  
				value += ch;
				afterInfo = afterInfo.substring(index+6,afterInfo.length());
			}else{
				value += afterInfo;
				break;
			}
		}
		return value;
    }  
	

	
	public String getValue(String info){
		int index = info.indexOf("\\u");
		if(index > 0){
			//value = info.substring(0,index);
		}
		String m_code = info.substring(index+2, index+6);  //单个
		
		String afterInfo = info.substring(index+6,info.length());
		return null;
	}
	
	/**
	 * https 域名校验
	 */
	private class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
	
	/**
	 * https 证书管理
	 */
	private class TrustAnyTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;  
		}
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}
	
	private static final String GET  = "GET";
	private static final String POST = "POST";
	private static final String CHARSET = "UTF-8";
	
	private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
	private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new EedaHttpKit().new TrustAnyHostnameVerifier();
	
	private static SSLSocketFactory initSSLSocketFactory() {
		try {
			TrustManager[] tm = {new EedaHttpKit().new TrustAnyTrustManager() };  
			SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");  
			sslContext.init(null, tm, new java.security.SecureRandom());  
			return sslContext.getSocketFactory();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
		URL _url = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
		if (conn instanceof HttpsURLConnection) {
			((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
			((HttpsURLConnection)conn).setHostnameVerifier(trustAnyHostnameVerifier);
		}
		
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		conn.setConnectTimeout(19000);
		conn.setReadTimeout(19000);
		
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
		
		if (headers != null && !headers.isEmpty())
			for (Entry<String, String> entry : headers.entrySet())
				conn.setRequestProperty(entry.getKey(), entry.getValue());
		
		return conn;
	}
	
	/**
	 * Send GET request
	 */
	public static String get(String url, Map<String, String> queryParas, Map<String, String> headers) {
		HttpURLConnection conn = null;
		try {
			conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, headers);
			conn.connect();
			return readResponseString(conn);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	public static String getFile(String url, Map<String, String> queryParas, String uploadFolder) {
	    HttpURLConnection conn = null;
	    String fileName = "";
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, null);
            conn.connect();
            
            InputStream inputStream = conn.getInputStream();
            String raw = conn.getHeaderField("Content-Disposition");
             // raw = "attachment; filename=abc.jpg"
             if(raw != null && raw.indexOf("=") != -1) {
                 // 把文件写到字节数组保存起来  
                 BufferedInputStream bis = new BufferedInputStream(inputStream);
                 ByteArrayOutputStream fos = new ByteArrayOutputStream();
                 byte[] buffer = new byte[1024];
                 int len = 0;
                 while ((len = bis.read(buffer)) != -1) {
                     fos.write(buffer, 0, len);
                 }  
                 byte[] imgBytes = fos.toByteArray();
                 bis.close();
                 fos.close();
                 // 保存图片
                 fileName = raw.split("=")[1]; //getting value after '='
                 fileName = fileName.replaceAll("\"", "");
                 
                 File file = new File(uploadFolder+fileName);

                 FileOutputStream fop = new FileOutputStream(file);
                 // if file doesn't exists, then create it
                 if (!file.exists()) {
                     file.createNewFile();
                 }
                 ByteArrayInputStream in = new ByteArrayInputStream(imgBytes);
                 BufferedOutputStream bos = new BufferedOutputStream(fop);  
                 byte[] outBuffer = new byte[1024];  
                 int length = 0;  
                 while ((length = in.read(outBuffer)) != -1) {  
                     bos.write(outBuffer, 0, length);  
                 }  
                 bos.close();  
                 in.close();  
                 logger.debug("Save file done");
             } else {
                 // fall back to random generated file name?
             }
            
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return fileName;
    }
	
	
	public static String get(String url, Map<String, String> queryParas) {
		return get(url, queryParas, null);
	}
	
	public static String get(String url) {
		return get(url, null, null);
	}
	
	/**
	 * Send POST request
	 */
	public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
		HttpURLConnection conn = null;
		try {
			conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), POST, headers);
			conn.connect();
			
			OutputStream out = conn.getOutputStream();
			out.write(data.getBytes(CHARSET));
			out.flush();
			out.close();
			
			return readResponseString(conn);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	public static String post(String url, Map<String, String> queryParas, String data) {
		return post(url, queryParas, data, null);
	}
	
	public static String post(String url, String data, Map<String, String> headers) {
		return post(url, null, data, headers);
	}
	
	public static String post(String url, String data) {
		return post(url, null, data, null);
	}
	
	private static String readResponseString(HttpURLConnection conn) {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		try {
			inputStream = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
			String line = null;
			while ((line = reader.readLine()) != null){
				sb.append(line).append("\n");
			}
			return sb.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Build queryString of the url
	 */
	private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
		if (queryParas == null || queryParas.isEmpty())
			return url;
		
		StringBuilder sb = new StringBuilder(url);
		boolean isFirst;
		if (url.indexOf("?") == -1) {
			isFirst = true;
			sb.append("?");
		}
		else {
			isFirst = false;
		}
		
		for (Entry<String, String> entry : queryParas.entrySet()) {
			if (isFirst) isFirst = false;	
			else sb.append("&");
			
			String key = entry.getKey();
			String value = entry.getValue();
			if (StrKit.notBlank(value))
				try {value = URLEncoder.encode(value, CHARSET);} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
			sb.append(key).append("=").append(value);
		}
		logger.debug("url:"+sb.toString());
		return sb.toString();
	}
	
	public static String readIncommingRequestData(HttpServletRequest request) {
		BufferedReader br = null;
		try {
			StringBuilder result = new StringBuilder();
			br = request.getReader();
			for (String line=null; (line=br.readLine())!=null;) {
				result.append(line).append("\n");
			}
			
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (br != null)
				try {br.close();} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	// 得到客户的IP
    public static String getClientIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                //根据网卡获取本机配置的IP地址
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    logger.debug("UnknownHostException: 未知的名称或服务");
                }
                ipAddress = inetAddress.getHostAddress();
            }
        }

        //对于通过多个代理的情况，第一个IP为客户端真实的IP地址，多个IP按照','分割
        if(null != ipAddress && ipAddress.length() > 15){
            //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",") > 0){
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;

    }
}






