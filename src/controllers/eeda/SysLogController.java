package controllers.eeda;

import interceptor.EedaMenuInterceptor;
import interceptor.SetAttrLoginUserInterceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import models.ParentOfficeModel;
import models.UserLogin;
import models.eeda.profile.OfficeConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.CaptchaRender;

import controllers.profile.LoginUserController;
import controllers.util.DbUtils;
import controllers.util.EedaCommonHandler;
import controllers.util.MD5Util;
import controllers.util.ParentOffice;
import controllers.util.getCurrentPermission;

@Before({ SetAttrLoginUserInterceptor.class, EedaMenuInterceptor.class })
public class SysLogController extends Controller {
    private static final String RANDOM_CODE_KEY = "eeda";
    private Log logger = Log.getLog(SysLogController.class);
    Subject currentUser = SecurityUtils.getSubject();

    public void index() {
        Record login_user = getAttr("login_user");
        List<Record> emailList = Db.find("select * from email_setting where office_id = ?",
                login_user.getLong("office_id"));
        for (Record email : emailList) {
            setAttr(email.getStr("type"), email);
        }
        render("/eeda/profile/sysLog/sysLog.html");
    }

    public void save() {
        String params = getPara("params");
        Gson gson = new Gson();
        List<List<Map<String, Object>>> list = gson.fromJson(params, new TypeToken<List<List<Map<String, Object>>>>() {
        }.getType());
        if (list.size() > 0) {
            Record login_user = getAttr("login_user");
            Db.update("delete from email_setting where office_id = ?", login_user.getLong("office_id"));
            for (List<Map<String, Object>> i : list) {
                Record re = new Record();
                for (Map<String, Object> map : i) {
                    re.set((String) map.get("name"), map.get("value"));
                }
                re.set("office_id", login_user.getLong("office_id"));
                Db.save("email_setting", re);
            }
        }
        renderJson("{\"result\":" + true + "}");
    }

    public void getLoginLog() {
        Record login_user = getAttr("login_user");
        Long officeId = login_user.getLong("office_id");
        // 两种参数写法, 第一种为空就换第二种再试
        String condition = DbUtils.buildConditions(getParaMap());
        if (StrKit.isBlank(condition) && StrKit.notBlank(getPara("target_field"))
                && StrKit.notBlank(getPara("like_str"))) {
            String fields = getPara("target_field");
            String like_str = getPara("like_str");
            String[] fieldsArr = fields.split(",");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < fieldsArr.length; i++) {
                String fieldName = fieldsArr[i];
                String likeStr = fieldName + " like '%" + like_str + "%'";
                list.add(likeStr);
            }
            condition = " and (" + StringUtils.join(list.toArray(), " or ") + ")";
        }
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select sl.id,sl.ip,sl.create_stamp,ul.user_name,o.office_name" + " from sys_log sl"
                + " left join user_login ul on ul.id = sl.user_id" + " left join office o on o.id = sl.office_id"
                + " where sl.log_type='登录' and sl.office_id="+officeId;

        List<Record> orderList = Db.find(sql + condition + " order by sl.id desc " + sLimit);
        String sqlTotal = "select count(1) total from (" + sql + condition + ") B";
        Record rec = Db.findFirst(sqlTotal);
        setAttr("queryTotal", sqlTotal);
        Map<String, Object> orderListMap = new HashMap<String, Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        renderJson(orderListMap);
    }

    public void getOperateLog() {
        Record login_user = getAttr("login_user");
        Long officeId = login_user.getLong("office_id");
        // 两种参数写法, 第一种为空就换第二种再试
        String condition = DbUtils.buildConditions(getParaMap());
        if (StrKit.isBlank(condition) && StrKit.notBlank(getPara("target_field"))
                && StrKit.notBlank(getPara("like_str"))) {
            String fields = getPara("target_field");
            String like_str = getPara("like_str");
            String[] fieldsArr = fields.split(",");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < fieldsArr.length; i++) {
                String fieldName = fieldsArr[i];
                String likeStr = fieldName + " like '%" + like_str + "%'";
                list.add(likeStr);
            }
            condition = " and (" + StringUtils.join(list.toArray(), " or ") + ")";
        }
        String sLimit = "";
        String pageIndex = getPara("draw");
        if (getPara("start") != null && getPara("length") != null) {
            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");
        }
        String sql = "select sl.id,sl.order_id,action_type,sl.ip,sl.create_stamp,ul.user_name,o.office_name,efd.name form_name"
                + " from sys_log sl" + " left join user_login ul on ul.id = sl.user_id"
                + " left join office o on o.id = sl.office_id"
                + " left join eeda_form_define efd on efd.id = sl.form_id"
                + " where sl.log_type = 'operate' and sl.office_id="+officeId;
        List<Record> orderList = Db.find(sql + condition + " order by sl.id desc " + sLimit);
        String sqlTotal = "select count(1) total from (" + sql + condition + ") B";
        Record rec = Db.findFirst(sqlTotal);
        setAttr("queryTotal", sqlTotal);
        Map<String, Object> orderListMap = new HashMap<String, Object>();
        orderListMap.put("draw", pageIndex);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", orderList);
        renderJson(orderListMap);
    }
}
