package controllers.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import cn.hutool.core.io.FileUtil;

import com.aliyun.oss.common.utils.DateUtil;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.ehcache.CacheKit;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

import java.text.ParseException;
import java.util.*;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.sf.json.JSONObject;

public class OssUtil {
    
    private static String endpoint = "img.meipaibox.com";//"http://oss-cn-shenzhen.aliyuncs.com";
    private static String accessKeyId = "";
    private static String accessKeySecret = "";
    private static String bucketName = "";
    //private static String key = "picbox_qrcode.txt";//文件名, 唯一值

    private static OSSClient getOSSClient(){
//        endpoint = PropKit.get("endpoint");
//        accessKeyId = PropKit.get("accessKeyId");
//        accessKeySecret = PropKit.get("accessKeySecret");
//        bucketName = PropKit.get("bucketName");
    	List<Record> ossList = CacheKit.get("sysDict","ossList");
    	if(CacheKit.get("sysDict","ossList")==null){
    		ossList = Db.find("SELECT * FROM t_sys_dict"
        			+ " WHERE type_code = 'system_global'"
        			+ " AND (`code` = 'oss_endpoint'"
        			+ " OR `code`= 'oss_bucketName'"
        			+ " OR `code` = 'oss_accessKeyId'"
        			+ " OR `code` = 'oss_accessKeySecret') order by id");
        	CacheKit.put("sysDict","ossList", ossList);
    	}
    	
        endpoint = ossList.get(0).get("value");
        bucketName = ossList.get(1).get("value");
        accessKeyId = ossList.get(2).get("value");
        accessKeySecret = ossList.get(3).get("value");
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        return ossClient;
    }

    @Before(Tx.class)
    public static boolean uploadFile(String id, File file) throws Exception{
        boolean isOK = true;
        OSSClient ossClient = getOSSClient();
        /*
         * Upload an object to your bucket
         */
        LogKit.debug("Uploading a new object to OSS from a file\n");
        try {
            ossClient.putObject(new PutObjectRequest(bucketName, id, file));
            //在本地www中做备份
			String targetFilePath = PropKit.get("user_template_root")+"/upload/"+file.getName();
			FileUtil.copy(file.getPath(), targetFilePath, true);
        } catch (OSSException oe) {
            isOK = false;
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
            throw oe;
        } catch (ClientException ce) {
            isOK = false;
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
            throw ce;
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        return isOK;
    }

    public static String getObjUrl(String key){
        return getObjUrl(key, false, false);
    }

    /*
        公有读
     */
    public static String getObjUrl(String key, boolean needWatermark, boolean isWebp){
        String style = "x-oss-process=image/quality,Q_80";///resize,w_850";//format,webp
        if(isWebp){
            style += "/format,webp";
        }
        if(key.endsWith(".gif") || key.endsWith(".GIF")){
            style = "";
        }
        //判断图片是否需要加水印
        if(needWatermark) {//
            //Record imgRec = Db.findFirst("select * from t_site_img where is_watermark='Y' and oss_key=?", key);
            //if (imgRec != null) {
            String imgWater = CacheKit.get("userSessionCache", "watermark_oss_key");
            if (imgWater == null) {
                Record rec = Db.findFirst("select * from t_sys_dict where code='watermark_img_oss_key'");
                imgWater = rec.getStr("value")+"?x-oss-process=image/resize,P_20"; // 按总图比例10%显示
                //imgWater = "iDqcoStxQoDs.png?x-oss-process=image/resize,P_10"; // 按总图比例10%显示
                CacheKit.put("userSessionCache", "watermark_oss_key", imgWater);
            }

            //URL安全base64编码
            String result = null;
            Base64 encoder = new Base64();
            byte[] encodedBytes = encoder.encode(imgWater.getBytes());
            result = new String(encodedBytes);

            style = style+"/watermark,t_80,g_se,x_10,y_10,image_" + result;
            //}
        }

        OSSClient ossClient = getOSSClient();
        ossClient.shutdown();
        String http = endpoint.replace("http://", "http://"+bucketName+".");
        String strUrl = http+"/"+key+"?"+style;

        return strUrl;
    }
    
    
    /**主要处理附件
     * 
     * @param key
     * @param needWatermark
     * @return
     */
    public static String getFileSignedUrl(String key, boolean needWatermark){
        String strUrl = null;
        OSSClient ossClient = getOSSClient();

        try {
            // 设置URL过期时间为5分钟
            Date expiration = new Date(new Date().getTime() + 300 * 1000);
 //           Date expiration = new Date(new Date().getTime() + 10000);// url超时时间
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);

            req.setExpiration(expiration);

            
            // 生成URL
            URL signedUrl = ossClient.generatePresignedUrl(req);
            strUrl = signedUrl.toString();
        } catch (OSSException oe) {

            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());

        } catch (ClientException ce) {

            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());

        }  finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        return strUrl;
    }

    /*
        私有读, 需要签名,过期时间
     */
    public static String getObjSignedUrl(String key, boolean needWatermark){
        String style = "image/quality,Q_80";//format,webp
        if(key.endsWith(".gif") || key.endsWith(".GIF")){
            style = "";
        }

        String strUrl = null;
        OSSClient ossClient = getOSSClient();

        try {
            // 图片处理样式

            //String style = "image/resize,m_fixed,w_100,h_100/rotate,90";
            //String style = "style/"+styleName;

            //判断图片是否需要加水印
            if(needWatermark) {//
                //Record imgRec = Db.findFirst("select * from t_site_img where is_watermark='Y' and oss_key=?", key);
                //if (imgRec != null) {
                    String imgWater = CacheKit.get("userSessionCache", "watermark_oss_key");
                    if (imgWater == null) {
                        Record rec = Db.findFirst("select * from t_sys_dict where code='watermark_img_oss_key'");
                        imgWater = rec.getStr("value")+"?x-oss-process=image/resize,P_50";// 按总图比例30%显示
                        CacheKit.put("userSessionCache", "watermark_oss_key", imgWater);
                    }

                    //URL安全base64编码
                    String result = null;
                    Base64 encoder = new Base64();
                    byte[] encodedBytes = encoder.encode(imgWater.getBytes());
                    result = new String(encodedBytes);

                    style = style+"/watermark,image_" + result+",t_80,g_se,x_10,y_10";
                //}
            }

            // 设置URL过期时间为1小时
//            Date expiration = new Date(new Date().getTime() + 3600 * 1000);

            // 设置URL过期时间为100年 3600l* 1000*24*365*100
            Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 *365*100);// url超时时间
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);

            req.setExpiration(expiration);

            Record img_rec = null;
            HashMap<String, Record> imgMap = CacheKit.get("FrontCache", "imgMap");
            if(imgMap==null) {
                img_rec = Db.findFirst("select id,doc_type from t_site_img where is_delete != 'Y' and oss_key=?", key);
                imgMap = new HashMap<String, Record>();
                imgMap.put(key, img_rec);
                CacheKit.put("FrontCache", "imgMap", imgMap);
            }else{
                Record imgRec = imgMap.get(key);
                if(imgRec==null){
                    img_rec = Db.findFirst("select id,doc_type from t_site_img where is_delete != 'Y' and oss_key=?", key);
                    imgMap.put(key, img_rec);
                }else{
                	img_rec = imgRec;
                }
            }

            if(img_rec != null){
            	String doc_type = img_rec.getStr("doc_type");
            	if(!"file".equals(doc_type)){
            		req.setProcess(style);
            	}
            }
            
            // 生成URL
            URL signedUrl = ossClient.generatePresignedUrl(req);
            strUrl = signedUrl.toString();
        } catch (OSSException oe) {

            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());

        } catch (ClientException ce) {

            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());

        }  finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        return strUrl;
    }

    public static boolean getObjFile(String key, String localFilePath){
    	boolean result = false;
        String strUrl = null;
        OSSClient ossClient = getOSSClient();
        File file = null;
        try {
//            OSSObject obj = ossClient.getObject(new GetObjectRequest(bucketName, key));
            // 下载object到文件
            //file = new File(localFilePath);
            ossClient.getObject(new GetObjectRequest(bucketName, key), new File(localFilePath));
            result = true;
        } catch (OSSException oe) {

            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());

        } catch (ClientException ce) {

            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());

        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        return result;
    }

    public static void deleteObj(String key){
        String strUrl = null;
        OSSClient ossClient = getOSSClient();
        File file = null;
        try {
            ossClient.deleteObject(bucketName, key);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());

        } catch (ClientException ce) {

            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());

        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
    }

    public static void main(String[] args) throws IOException {
        /*
         * Constructs a client instance with your account for accessing OSS
         */
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

        System.out.println("Getting Started with OSS SDK for Java\n");
        
        try {

            /*
             * Determine whether the bucket exists
             */
//            if (!ossClient.doesBucketExist(bucketName)) {
//                /*
//                 * Create a new OSS bucket
//                 */
//                System.out.println("Creating bucket " + bucketName + "\n");
//                ossClient.createBucket(bucketName);
//                CreateBucketRequest createBucketRequest= new CreateBucketRequest(bucketName);
//                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
//                ossClient.createBucket(createBucketRequest);
//            }
//
//            /*
//             * List the buckets in your account
//             */
            System.out.println("Listing buckets");

            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            listBucketsRequest.setMaxKeys(500);

            for (Bucket bucket : ossClient.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }
            System.out.println();
//
//            /*
//             * Upload an object to your bucket
//             */
//            System.out.println("Uploading a new object to OSS from a file\n");
//            ossClient.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));
//
//            /*
//             * Determine whether an object residents in your bucket
//             */
//            boolean exists = ossClient.doesObjectExist(bucketName, key);
//            System.out.println("Does object " + bucketName + " exist? " + exists + "\n");
//
//            ossClient.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
//            ossClient.setObjectAcl(bucketName, key, CannedAccessControlList.Default);
//
//            ObjectAcl objectAcl = ossClient.getObjectAcl(bucketName, key);
//            System.out.println("ACL:" + objectAcl.getPermission().toString());
            
            /*
             * Download an object from your bucket
             */
            System.out.println("Downloading an object");
//            OSSObject object = ossClient.getObject(bucketName, "key");
//            System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
//            displayTextInputStream(object.getObjectContent());

            //getObjFile("11_qr", "WebRoot/upload/ext.jpg");

//            /*
//             * List objects in your bucket by prefix
//             */
//            System.out.println("Listing objects");
//            ObjectListing objectListing = ossClient.listObjects(bucketName, "11");
//            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
//                System.out.println(" - " + objectSummary.getKey() + "  " +
//                                   "(size = " + objectSummary.getSize() + ")");
//            }
//            System.out.println();

            /*
             * Delete an object
             */
//            System.out.println("Deleting an object\n");
//            ossClient.deleteObject(bucketName, key);
            
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
    }
    
    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("oss-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("0123456789011234567890\n");
        writer.close();

        return file;
    }

    private static void saveInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        byte[] buf = new byte[1024];
        for (int n = 0; n != -1; ) {
            n = input.read(buf, 0, buf.length);
        }
        input.close();

    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
        
        reader.close();
    }

    public static Map getOSS_signature(){
//        String endpoint = "*";
//        String accessId = "*";
//        String accessKey = "*";
//        String bucket = "*";
        String dir = "";
        String host = "https://" + endpoint;// + bucketName + "."
        String callbackUrl = "https://www.meipaibox.com/wx_xcx/album/oss_upload_pic_callback";//只是callback, 告诉上传文件成功或失败
        OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);


        try {
            long expireTime = 60;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);



            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            String callbackBody = "orderId=${x:order_id}&index=${x:index}&filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}";
            String callbackBodyType = "application/x-www-form-urlencoded";
            Map<String, String> callbackMap = new LinkedHashMap<String, String>();
            callbackMap.put("callbackUrl", callbackUrl);
            callbackMap.put("callbackBody", callbackBody);
            callbackMap.put("callbackBodyType", callbackBodyType);
            JSONObject callbackJson = JSONObject.fromObject(callbackMap);
            byte[] callbackJson_binaryData = callbackJson.toString().getBytes("utf-8");
            String encodedCallback = BinaryUtil.toBase64String(callbackJson_binaryData);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            //respMap.put("expire", formatISO8601Date(expiration));
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("callback", encodedCallback);

            JSONObject ja1 = JSONObject.fromObject(respMap);
            System.out.println(ja1.toString());
//			response.setHeader("Access-Control-Allow-Origin", "*");
//			response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            return respMap;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
