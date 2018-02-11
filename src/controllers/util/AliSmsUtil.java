package controllers.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.BatchSmsAttributes;
import com.aliyun.mns.model.MessageAttributes;
import com.aliyun.mns.model.RawTopicMessage;
import com.aliyun.mns.model.TopicMessage;

public class AliSmsUtil {
    private static MNSClient client = null;
    private static AtomicLong totalCount = new AtomicLong(0);

    private static String endpoint = null;
    private static String accessId = null;
    private static String accessKey = null;

    private static String topicName = null;
    private static String queueName = "JavaSDKPerfTestQueue";
    private static int threadNum = 100;
    private static int totalSeconds = 180;
    
    private static String signName = null;
    private static String SMSTemplateCode = null;
    
    protected static boolean parseConf(String template) {
        String confFilePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "perf_test_config.properties";

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(confFilePath));
            if (bis == null) {
                System.out.println("ConfFile not opened: " + confFilePath);
                return false;
            }
        } catch (FileNotFoundException e) {
            System.out.println("ConfFile not found: " + confFilePath);
            return false;
        }

        // load file
        Properties properties = new Properties();
        try {
            properties.load(bis);
        } catch(IOException e) {
            System.out.println("Load ConfFile Failed: " + e.getMessage());
            return false;
        } finally {
            try {
                bis.close();
            } catch (Exception e) {
                // do nothing
            }
        }

        // init the member parameters
        endpoint = properties.getProperty("Endpoint");
        System.out.println("Endpoint: " + endpoint);
        accessId = properties.getProperty("AccessId");
        System.out.println("AccessId: " + accessId);
        accessKey = properties.getProperty("AccessKey");

        topicName = properties.getProperty("TopicName");
        System.out.println("topicName: " + topicName);
        signName = properties.getProperty("signName");
        System.out.println("signName: " + signName);
        
        if("sendMsg".equals(template)){
        	SMSTemplateCode = properties.getProperty("SMSTemplateCode");//发送信息
        }else if("sendCode".equals(template)){
        	SMSTemplateCode = properties.getProperty("SCODETemplateCode");//发送验证码
        }
        System.out.println("TemplateCode: " + SMSTemplateCode);
        
        queueName = properties.getProperty("QueueName", queueName);
        System.out.println("QueueName: " + queueName);
        threadNum = Integer.parseInt(properties.getProperty("ThreadNum", String.valueOf(threadNum)));
        System.out.println("ThreadNum: " + threadNum);
        totalSeconds = Integer.parseInt(properties.getProperty("TotalSeconds", String.valueOf(totalSeconds)));
        System.out.println("TotalSeconds: " + totalSeconds);

        return true;
    }
    
    public static void main(String[] args) {
        sendSms("1234", "18578200347","code");
    }

    public static void sendSms(String code, String mobile,String template) {
        if (!parseConf(template)) {
            return;
        }
        /**
         * Step 1. 获取主题引用
         */
        CloudAccount account = new CloudAccount(accessId, accessKey, endpoint);
        MNSClient client = account.getMNSClient();
        CloudTopic topic = client.getTopicRef(topicName);
        /**
         * Step 2. 设置SMS消息体（必须）
         *
         * 注：目前暂时不支持消息内容为空，需要指定消息内容，不为空即可。
         */
        RawTopicMessage msg = new RawTopicMessage();
        msg.setMessageBody("sms-message");
        /**
         * Step 3. 生成SMS消息属性
         */
        MessageAttributes messageAttributes = new MessageAttributes();
        BatchSmsAttributes batchSmsAttributes = new BatchSmsAttributes();
        // 3.1 设置发送短信的签名（SMSSignName）
        batchSmsAttributes.setFreeSignName(signName);
        // 3.2 设置发送短信使用的模板（SMSTempateCode）
        batchSmsAttributes.setTemplateCode(SMSTemplateCode);
        // 3.3 设置发送短信所使用的模板中参数对应的值（在短信模板中定义的，没有可以不用设置）
        BatchSmsAttributes.SmsReceiverParams smsReceiverParams = new BatchSmsAttributes.SmsReceiverParams();
        if(StringUtils.isNotBlank(code)){
        	smsReceiverParams.setParam("code", code);
        }
        // 3.4 增加接收短信的号码
        batchSmsAttributes.addSmsReceiver(mobile, smsReceiverParams);
        messageAttributes.setBatchSmsAttributes(batchSmsAttributes);
        try {
            /**
             * Step 4. 发布SMS消息
             */
            TopicMessage ret = topic.publishMessage(msg, messageAttributes);
            System.out.println("MessageId: " + ret.getMessageId());
            System.out.println("MessageMD5: " + ret.getMessageBodyMD5());
        } catch (ServiceException se) {
            System.out.println(se.getErrorCode() + se.getRequestId());
            System.out.println(se.getMessage());
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }
}
