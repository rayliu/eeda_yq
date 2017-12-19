package controllers.alipay;  

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {
	
//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    //test 2016082000295782
    //2016022401159400
	public static String app_id = "2016022401159400";
	
	// 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCzSFOEzGj+W6lK"+
            "XOE0iuN5mfLPjVCjIEj59FK9PURXrbea9jOIjjGptT8RiBBXiqI7rwOz+hfXOMW7"+
            "48512v9YXgMZ4Q+aAi5yae3L+ox/KZXZKb1dvnTJQMhdwdKKRZsfdpTKgWAxpLWG"+
            "irAXYZJotcw3Qf48XiKsg9sTYTTDJnc0bFI10gtbytuD+OHMXNIEBrJd3wI7wBkW"+
            "etfcq9aSif6wg6RB9eblR4WHXFqIQ2aRmPa/hCwj212jjt7no6t5twbtjG8kn06p"+
            "pvIL/LPn5oapyRRlBYg6EEsd2ByaBkgBTussSE5ckKL0NGQPrt8vJtQo5b18URTP"+
            "H24VOai7AgMBAAECggEAcb+o5e//iarEfV7ysDCS5TtM+GzTxbwdMSHYQSMXMn4Q"+
            "qSweRDzbu644ZroqjDB0gGOpeM5rw+zMYErIWrRpIDr9wrSt/mv15jfZXXsJG0Fa"+
            "Rt+Zu1m0pWAKAJqSJ5LEOjE1dEqjqruzRLrHwP3yM6ds+35OfSTI4SYDoIcjP9Ed"+
            "1YP/JRkz551F88ASGbqKO1yNgUVaLEVU5kfCXCGBs2cLNN0/aaKtKfXIyEuh9Oct"+
            "IafhiMDbmYKYhcZNvG/7n9XEHtHJ3f1N8ichHjn4PQ4KkDDyDrmE0eVXaSTyzkH2"+
            "+l4yHaBtY587iXaGkclyh6qxf9ktX7EbaZS5hdfKSQKBgQDsT0Pn2VLT7ACMSBVZ"+
            "kpYIc4wnYAzsIp3kQPkqjt/ktku9RrEcDvU0mr5i2QXzJSvihlsnBPYxFAfAbMcV"+
            "0JxiCh7ygrgch/cnCGzwXH4+9ddge3jkhHgtqxITgYCgjMuScpNyXRq9PDPykQNI"+
            "5az6QqFP+aeww/0WAIH4mBGtXQKBgQDCOJzHRVbwjmNEqBqvNGj9rMg8EHBziLBs"+
            "lCsEn9wNNwS+D8dJy8q9oLeHCLb2nL3y0lqWJD/spymHs0mewJDrJ2rKqcBBAVkS"+
            "q3I4qx9IyjMCA+jA7hfgDQM+zgrPU0sjzxY306zX6MHo3tqkTT3p3fQZ6T/T3TWt"+
            "54SV3P609wKBgEMAfoqAbiG0artbvOH21B91U6nebAuQDSCo430ToZvhrDvwjUsd"+
            "Wp0duxmf2zYDthu2aKX5nCxyN3hx30jqK00I/ZlZQwaj7oncs7a7IviLEBRFLhPt"+
            "E34vYSKyt1vOT/IoFnJ4v5okNb5zK3FB3fffaImaiddu2pWgczmfNHPpAoGAPOB9"+
            "08AaNiCLYPPIGW62Ef1cbedBOBV6Jy5yJWruEH5UUDal4q2sCLAdlofWwYnzx+7D"+
            "UhheqRDkZZJ+2vHE+tBKGugGSNnhT4bJsJWCotAuGM2c4QxAE4xAnij7Hm0fOkQo"+
            "1KrWnH5qeVGQ+rrzUyCs6IpB5WiK8M2Gs2qns5UCgYA6C63Ntb0rfn8g8SRHTTqe"+
            "aiVTSwtmzc/841lFGnNzltRrZx+VFPed5eqHQseeG6r9YLxkd6Uenmy3wGpR356W"+
            "SsxvnhnvYNihbgrKpnY8Ofe42bXRX8P20fAvK9z1V84+7s82pcTQ6VrvH9+10BmY"+
            "alQOKE0DoPtPXjWhPQo9YQ==";
	
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    //test 
    public static String alipay_public_key =  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAviQlu+eD/UoM+l0n6IZsbrDEGzGiZ4C1Abk7JPQctdkmWI0R9bnPEtof55u+R5p0QP+5GjPrFanIBNg5kZ4r5qT7OHLE/9wC9ONPEuLW5/U3msykacHQYLnrWtgpPG0L+UXXw5CVQ4tVTIfhuxNeIohvdoYzDbuJxtSVim1JkU+/FvWU3xwTnj4VEr/+LeOcMVqMRJgHgAUoongQ929XYac4CAELJhnLyWw8XBoHa9mUyaRWIoFNzhkxbQZt4poKPVSUe7F5yEccDaH2xYVy7IH5h6RySkEbrX+N9F/JApg2WpP7+zEpvjW79p/ffCIOPnCu/dkWIWLU6+Qi0eGI6QIDAQAB";  
            //"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhWV2Zf8EP1T6ojbEhsGt+MDgHaS1PpmgiBFBLi4fh+Itnwz+rKgOWe4mwZxQSBmDjJgtCgsTw2Y8xTw8QcJb8fuFMbjYbJRodIJfyzxAvsrsjej9G4f7pXL9ZYRZyrGhu35sly8I4Uix5GEOgKAdLmwLC30DiSYO7uiHyG/Gs8NA9t+Y5DRg6l4Q9HtdUBOr5a1DwJX4fDrrq7i4G6fJMLq556kvM2HNhtMHaDsZeu3+SlgkSbkSU4Bd5o9R/LsbRTWX1yJj7GQIVvNf1FldPnetj09G22iUy/Ygiv9jJr2LT5gVymltJw44FudmiOduwMmdSlMExG8tU/N9GEiUDwIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String notify_url = "http://www.iwedclub.com/alipay/ali_notify";//"http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String return_url = "http://www.iwedclub.com/alipay/ali_return";//"http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";

	// 签名方式
	public static String sign_type = "RSA2";
	
	// 字符编码格式
	public static String charset = "utf-8";
	
	// 支付宝网关     
	public static String gatewayUrl = "https://openapi.alipay.com/gateway.do";
//	        "https://openapi.alipaydev.com/gateway.do";
	
	// 支付宝网关
	public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /** 
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

