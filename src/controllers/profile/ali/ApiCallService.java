package controllers.profile.ali;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import controllers.util.ali.CommonUtil;

/**
 * api调用的服务类
 */
public class ApiCallService {

    /**
     * 调用api测试
     * 
     * @param urlHead
     *            请求的url到openapi的部分，如http://gw.open.1688.com/openapi/
     * @param urlPath
     *            protocol/version/namespace/name/appKey
     * @param appSecretKey
     *            测试的app密钥，如果为空表示不需要签名
     * @param params
     *            api请求参数map。如果api需要用户授权访问，那么必须完成授权流程，params中必须包含access_token参数
     * @return json格式的调用结果
     */
    public static String callApi(String urlHead, String urlPath,
            String appSecretKey, Map<String, String> params) {
        final HttpClient httpClient = HttpClients.createDefault();
        final HttpPost method = new HttpPost(urlHead + urlPath);
        method.setHeader("Content-type",
                "application/x-www-form-urlencoded; charset=UTF-8");
        List formParams = new ArrayList();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry
                        .getValue()));
            }
        }
        if (appSecretKey != null) {
            formParams.add(new BasicNameValuePair("_aop_signature", CommonUtil
                    .signatureWithParamsAndUrlPath(urlPath, params,
                            appSecretKey)));
        }
        String responseStr = "";
        try {
            HttpEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
            method.setEntity(entity);

            HttpResponse response = httpClient.execute(method);
            int status = response.getStatusLine().getStatusCode();

            responseStr = CommonUtil.parserResponse(response);
            if (status >= 300 || status < 200) {
                throw new RuntimeException("invoke api failed, urlPath:"
                        + urlPath + " status:" + status + " response:"
                        + responseStr);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            method.releaseConnection();
        }
        return responseStr;
    }

}
