package controllers.profile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.mail.internet.MimeUtility;

import models.UserLogin;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import config.EedaConfig;
import controllers.util.MD5Util;
import controllers.util.SmsSendUtil;

public class RegisterUserController extends Controller {
    // 这个是记录操作日志的类
    private Log logger = Log.getLog(LoginUserController.class);

    public void index() {
        render("/eeda/theme/h-ui/register.html");
    }

    @Before(Tx.class)
    public void getRegisterCode() {
        String mobile = getPara("mobile");

        Random rand = new Random();
        int code = rand.nextInt(9999) + 1;

        Record rec = new Record();
        rec.set("mobile", mobile);
        rec.set("code", code);

        Record oldRec = Db.findFirst("select * from user_login where user_phone=?", mobile.trim());
        if (oldRec != null) {
            Record result = new Record();
            result.set("status", "failed");
            result.set("msg", "该手机已注册");

            renderJson(result);
        } else {
            // send sms with code
            logger.debug("注册验证码 code : " + code);

            SmsSendUtil.send(mobile, code);

            Db.save("user_register", rec);
            Record result = new Record();
            result.set("status", "ok");
            result.set("msg", "短信已发送");
            renderJson(result);
        }
    }

    @Before(Tx.class)
    public void saveRegistrant() {
        String email = getPara("email");
        String mobile = getPara("mobile");
        String smsCode = getPara("smsCode");
        String pwd = getPara("password");
        String againPassword = getPara("retype_password");

        // 判断登陆email不能重复注册
        UserLogin mailUser = UserLogin.dao.findFirst("select * from user_login where user_name=?", email);
        if (mailUser != null) {
            Record errRec = new Record();
            errRec.set("status", "failed");
            errRec.set("msg", "该用户已存在，不能注册。");
            renderJson(errRec);
            return;
        }
        // 判断登陆mobile不能重复注册
//        UserLogin oldUser = UserLogin.dao.findFirst("select * from user_login where user_phone=?", mobile);
//        if (oldUser != null) {
//            Record errRec = new Record();
//            errRec.set("status", "failed");
//            errRec.set("msg", "用户已存在，不能注册。");
//            setAttr("err", errRec);
//            renderError(500);
//            return;
//        }
        // 判断smsCode不能重复注册
//        Record rec = Db.findFirst("select * from user_register where mobile=? order by create_date desc", mobile);
//        if (rec != null) {
//            String code = rec.getStr("code");
//            if (!code.equals(smsCode)) {
//                Record errRec = new Record();
//                errRec.set("status", "failed");
//                errRec.set("msg", "验证码不一致，不能注册。");
//
//                setAttr("err", errRec);
//                renderError(500);
//                return;
//            }
//        } else {
//            Record errRec = new Record();
//            errRec.set("status", "failed");
//            errRec.set("msg", "该手机号码未申请验证码，请重新申请。");
//
//            setAttr("err", errRec);
//            renderError(500);
//            return;
//        }
        //第一步，将公司的信息填入到office表中，获取到office id
        Record office = new Record();
        office.set("office_name", "未设置公司名称");
//        office.set("phone", mobile);
        office.set("create_stamp", new Date());
        Db.save("office", office);

        //将新注册公司的ID设值到注册用户中
        Record user = new Record();
        user.set("user_name", email);
        user.set("c_name", email);
        String sha1Pwd = MD5Util.encode("SHA1", pwd);
        user.set("password", sha1Pwd);
        user.set("office_id", office.get("id"));
        Db.save("user_login", user);

        //创建管理员角色
        Record role = new Record();
        role.set("code", "admin");
        role.set("name", "系统管理员");
        role.set("remark", "拥有所有权限");
        role.set("office_id", office.get("id"));
        Db.save("role", role);
        
        //关联user-role
        Record userRole = new Record();
        userRole.set("user_name", email);
        userRole.set("role_id", role.getLong("id"));
        userRole.set("role_code", "admin");
        Db.save("user_role", userRole);
        
        //user_office
        Record userCompany = new Record();
        userCompany.set("user_name", email);
        userCompany.set("office_id", office.get("id"));
        userCompany.set("is_main", 1);
        Db.save("user_office", userCompany);
        
        try {
            // 注册成功后发一封通知邮件给易达管理员
            notifyAdmin(email);

            // 保存成功后，跳转登录页面
            Record errRec = new Record();
            errRec.set("status", "ok");
            errRec.set("msg", "注册成功");

            renderJson(errRec);
        } catch (Exception e) {
            e.printStackTrace();
            Record errRec = new Record();
            errRec.set("status", "failed");
            errRec.set("msg", e.getMessage());

            renderJson(errRec);
        }

    }

    // 注册成功后发一封通知邮件给易达管理员
    private void notifyAdmin(String mobile) throws EmailException {
        // 注册成功
        Email emailTo = new SimpleEmail();
        emailTo.setHostName("smtp.exmail.qq.com");
        emailTo.setSmtpPort(465);

        /* 输入公司的邮箱和密码 */
        /* EedaConfig.mailUser, EedaConfig.mailPwd */
        emailTo.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));
        emailTo.setSSLOnConnect(true);
        /* EedaConfig.mailUser */
        emailTo.setFrom(EedaConfig.mailUser);// 设置发信人
        emailTo.setSubject("新用户注册信息:" + mobile);
        Date date = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String newDate = sf.format(date);
        String basePath = newDate + " 用户: " + mobile + ", 在“易得系统”注册了新账号";

        emailTo.setMsg(basePath);

        /* 添加邮件收件人 */
        emailTo.addTo("ray.liu@eeda123.com");// 设置收件人
        emailTo.send();
    }

    private void sendToUser(String email) throws EmailException, UnsupportedEncodingException {
        MultiPartEmail emailToUser = new MultiPartEmail();
        emailToUser.setHostName("smtp.exmail.qq.com");
        emailToUser.setSmtpPort(465);

        // 输入公司的邮箱和密码
        emailToUser.setAuthenticator(new DefaultAuthenticator(EedaConfig.mailUser, EedaConfig.mailPwd));
        emailToUser.setSSLOnConnect(true);

        // 设置发信人
        emailToUser.setFrom(EedaConfig.mailUser);
        // 设置主题
        emailToUser.setSubject("快速掌握易达物流系统"); // 要发送的附件
        String basePath = "尊敬的" + email
                + "用户：\r \n \r \n \r \n \t \t\t \t\t \t感谢您注册易达TMS，您的账号已激活。为了使你快速的了解我们的系统，请您参考3 minutes to know Eeda-TMS.pdf文档。\r \n \r \n如果有问题，请联系我们创诚易达团队\r \n";

        emailToUser.setMsg(basePath);

        EmailAttachment attachment = new EmailAttachment();

        File file = new File(System.getProperty("user.dir") + "\\WebRoot\\download\\3分钟认识易达TMS.pdf");

        attachment.setPath(file.getPath());

        attachment.setName(MimeUtility.encodeText(file.getName()));

        // 设置附件描述
        attachment.setDescription("三分钟了解系统");
        // 设置附件类型
        attachment.setDisposition(EmailAttachment.ATTACHMENT);

        emailToUser.attach(attachment);

        emailToUser.addTo(email);
        emailToUser.send();
    }

    public void checkUserNameExist() {
        String userName = getPara("username");
        boolean checkObjectExist;

        UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name=?", userName);
        if (user == null) {
            checkObjectExist = true;
        } else {
            checkObjectExist = false;
        }
        renderJson(checkObjectExist);
    }

    public void checkOfficeNameExist() {
        String officeName = getPara("officeName");
        Record office = Db.findFirst("select * from office where office_name=?", officeName);
        boolean checkObjectExist;
        if (office == null) {
            checkObjectExist = true;
        } else {
            checkObjectExist = false;
        }
        renderJson(checkObjectExist);
    }
}
