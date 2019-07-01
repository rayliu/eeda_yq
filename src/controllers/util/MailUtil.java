package controllers.util;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.Resource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class MailUtil {
	public static void main(String[] args) {
		
		String host = "smtp.exmail.qq.com";//邮件服务器主机host，目前只支持SMTP协议(可以是163或者qq)pop3 smtp
		int port = 465;//邮件服务器端口 995
		String username = "log@logclub.com";//登录邮件服务器的账号
		String password = "QAZwsx123";//登录邮件服务器的密码，该密码通常是通过短信动态授权第三方登录的密码
		int timeout = 2500; 
		
		String mailFrom = "log@logclub.com";
		String to = "992827305@qq.com";
		String subject = "测试标题";
		String text = "测试内容";
		
		try{
			//邮件测试
			//sendMailForTest( host, port, username, password, from, to);
			sendTextMail( host, port, username, password, timeout, mailFrom,
		    		 to, subject, text);
			
		}catch (MailAuthenticationException e) {
            e.printStackTrace();
            System.out.println("邮件认证异常：authentication failure(认证失败)");
        }catch(MailSendException e){
            e.printStackTrace();
            System.out.println("邮件发送异常：failure when sending the message(发送消息失败)");
        }catch(MailParseException  e){
            e.printStackTrace();
            System.out.println("邮件消息解析异常：failure when parsing the message(消息解析失败)");
        }
	}

    private static JavaMailSenderImpl createMailSender(String host,int port,String username,String password,int timeout){
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("Utf-8");
        // 创建邮件配置
        Properties p = new Properties();
        p.setProperty("mail.smtp.timeout",timeout+"");
        p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        p.setProperty("mail.smtp.auth","true");
        p.setProperty("mail.smtp.ssl.enable", "true");// 开启ssl
        p.setProperty("mail.debug", "true");
        sender.setJavaMailProperties(p);
        
        return sender;
    }
    
    //发送测试的邮件
    public static void sendMailForTest(String host,int port,String username,String password,String from,
            String to){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setSubject("这是测试邮件，请勿回复！");
        mail.setSentDate(new Date());// 邮件发送时间
        mail.setText("这是一封测试邮件。如果您已收到此邮件，说明您的邮件服务器已设置成功，请勿回复。");
        JavaMailSenderImpl sender = createMailSender(host, port, username, password,25000);
        sender.send(mail);
    }
    
    public static void sendTextMail(String host,int port,String username,String password,int timeout,String mailFrom,
    		String to,String subject,String text){
    	JavaMailSenderImpl mailSender = createMailSender(host,port,username,
    			password,timeout);
    	
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setSentDate(new Date());// 邮件发送时间
        mail.setText(text);
        mailSender.send(mail);
    }
    public static void sendHtmlMail(String host,int port,String username,String password,int timeout,String mailFrom,
    		String to,String subject,String html) throws MessagingException {
    	JavaMailSenderImpl mailSender = createMailSender(host,port,username,
    			password,timeout);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // 设置utf-8或GBK编码，否则邮件会有乱码
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        messageHelper.setFrom(mailFrom);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(html, true);
        mailSender.send(mimeMessage);
    }
    
    public static void sendHtmlMail2(String to,String subject,String html) throws MessagingException {
    	String host = "smtp.exmail.qq.com";//邮件服务器主机host，目前只支持SMTP协议(可以是163或者qq)pop3 smtp
		int port = 465;//邮件服务器端口 995
		String username = "log@logclub.com";//登录邮件服务器的账号
		String password = "QAZwsx123";//登录邮件服务器的密码，该密码通常是通过短信动态授权第三方登录的密码
		int timeout = 2500; 
		List<Record> config = Db.find("SELECT * FROM `t_sys_dict` where type_code = 'mails_config' and is_delete = 'N'");
		for(Record item : config){
			String code = item.getStr("code");
			String value = item.getStr("value");
			if(StrKit.notBlank(value)){
				if("host".equals(code)){
					host = value;
				} else if("port".equals(code)){
					port = Integer.parseInt(value);
				}else if("username".equals(code)){
					username = value;
				}else if("password".equals(code)){
					password = value;
				}else if("timeout".equals(code)){
					timeout = Integer.parseInt(value);
				}
			}
		}
		
		String mailFrom = "log@logclub.com";
    	JavaMailSenderImpl mailSender = createMailSender(host,port,username,
    			password,timeout);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // 设置utf-8或GBK编码，否则邮件会有乱码
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        messageHelper.setFrom(mailFrom);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(html, true);
        mailSender.send(mimeMessage);
    }

    public static void sendFileMail(String host,int port,String username,String password,int timeout,String mailFrom,
    		String to,String subject,String html,String contentId,Resource resource) throws MessagingException {
    	JavaMailSenderImpl mailSender = createMailSender(host,port,username,
    			password,timeout);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // 设置utf-8或GBK编码，否则邮件会有乱码
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        messageHelper.setFrom(mailFrom);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(html, true);
        //FileSystemResource img = new FileSystemResource(new File("c:/350.jpg"));
        messageHelper.addInline(contentId, resource);
        // 发送
        mailSender.send(mimeMessage);
    }
    
    
}