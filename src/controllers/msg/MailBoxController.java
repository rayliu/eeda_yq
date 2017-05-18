package controllers.msg;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import models.UserLogin;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import controllers.eeda.ListConfigController;
import controllers.profile.LoginUserController;
import controllers.util.DbUtils;

@RequiresAuthentication
@Before(SetAttrLoginUserInterceptor.class)
public class MailBoxController extends Controller {

    private Logger logger = Logger.getLogger(MailBoxController.class);
    Subject currentUser = SecurityUtils.getSubject();

    @Before(EedaMenuInterceptor.class)
    public void index() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long user_id = user.getLong("id");
        List<Record> configList = ListConfigController.getConfig(user_id,
                "/mailBox");
        setAttr("listConfigList", configList);

        render("/eeda/mailBox/list.html");
    }
    
    @Before(EedaMenuInterceptor.class)
    public void config() {
        render("/eeda/mailBox/configList.html");
    }

    public void configList() {
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select * from mail_box_config where office_id=" + office_id;

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by create_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);
    }
    
    @Before(Tx.class)
    public void save() throws Exception {
        String title = getPara("radioTitle");
        String content = getPara("radioContent");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        Record r = new Record();
        r.set("title", title);
        r.set("content", content);
        r.set("office_id", office_id);
        r.set("create_stamp", new Date());
        r.set("creator", LoginUserController.getLoginUserId(this));
        Db.save("msg_board", r);
        redirect("/");
    }

    @Before(Tx.class)
    public void saveOfMsgBoard() throws Exception {
        String title = getPara("radioTitle");
        String content = getPara("radioContent");
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        Record r = new Record();
        r.set("title", title);
        r.set("content", content);
        r.set("office_id", office_id);
        r.set("create_stamp", new Date());
        r.set("creator", LoginUserController.getLoginUserId(this));
        Db.save("msg_board", r);
        redirect("/msgBoard");
    }

    public void list() {

        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }

        String sql = "select * from mail_box where office_id=" + office_id;

        String condition = DbUtils.buildConditions(getParaMap());

        String sqlTotal = "select count(1) total from (" + sql + condition
                + ") B";
        Record rec = Db.findFirst(sqlTotal);
        logger.debug("total records:" + rec.getLong("total"));

        List<Record> orderList = Db.find(sql + condition
                + " order by create_time desc " + sLimit);
        Map map = new HashMap();
        map.put("draw", pageIndex);
        map.put("recordsTotal", rec.getLong("total"));
        map.put("recordsFiltered", rec.getLong("total"));
        map.put("data", orderList);
        renderJson(map);

    }

    public void reply() {
        String id = getPara("edit_id");
        String title = getPara("edit_radioTitle");
        String content = getPara("edit_radioContent");
        Record r = Db.findById("msg_board", id);
        r.set("title", title);
        r.set("content", content);
        r.set("update_stamp", new Date());
        r.set("updator", LoginUserController.getLoginUserId(this));
        Db.update("msg_board", r);
        redirect("/msgBoard");
    }

    public void receivceMail() {
        
        UserLogin user = LoginUserController.getLoginUser(this);
        long office_id = user.getLong("office_id");
        
        List<Record> configList = Db.find("select * from mail_box_config where office_id=?", office_id);
        for (Record config : configList) {
            recieveMail(office_id, config);
        }
        
        renderText("OK");
    }

    private void recieveMail(long office_id, Record config) {
        String imapserver = config.getStr("smtp"); // 邮件服务器
        String userName = config.getStr("user");
        String pwd = config.getStr("pwd"); // 根据自已的密码修改
        
        // 获取默认会话
        Properties prop = System.getProperties();
        prop.put("mail.imap.host", imapserver);

        prop.put("mail.imap.auth.plain.disable", "true");
        Session mailsession = Session.getInstance(prop, null);
        mailsession.setDebug(false); // 是否启用debug模式
        IMAPFolder folder = null;
        IMAPStore store = null;
        int total = 0;
        try {
            store = (IMAPStore) mailsession.getStore("imap"); // 使用imap会话机制，连接服务器
            store.connect(imapserver, userName, pwd);
            folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
            
            // 使用只读方式打开收件箱
            folder.open(Folder.READ_ONLY);
            // 获取总邮件数
            total = folder.getMessageCount();
            System.out.println(imapserver+"-----------------" + userName + "邮箱共有邮件：" + total
                    + " 封--------------");

            Calendar cal = null;
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1); // add 1 date
            Date maxDate = new Date(cal.getTimeInMillis());
            
            cal.add(Calendar.DAY_OF_MONTH, -7); // sub 7 dates
            Date minDate = new Date(cal.getTimeInMillis());
            
            SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LT, maxDate);
            SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, minDate);
            SearchTerm andTerm = new AndTerm(olderThan, newerThan);
            Message[] msgs = folder.search(andTerm);
            // 得到收件箱文件夹信息，获取邮件列表
            //Message[] msgs = folder.getMessages();
            System.out.println("\t过去一周收件箱的总邮件数：" + msgs.length);
            System.out.println("----------------End------------------");

            int count = msgs.length;
            ReceiveMail rm = null;
            for (int i = 0; i < count; i++) {
                MimeMessage msg = (MimeMessage) msgs[i];
                rm = new ReceiveMail(msg);
                rm.recive(msg, i, office_id, userName);
            }
        } catch (MessagingException ex) {
            System.err.println("不能以读写方式打开邮箱!");
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            try {
                if (folder != null)
                    folder.close(true); // 退出收件箱时,删除做了删除标识的邮件
                if (store != null)
                    store.close();
            } catch (Exception bs) {
                bs.printStackTrace();
            }
        }
    }

}
