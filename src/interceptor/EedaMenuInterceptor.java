package interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class EedaMenuInterceptor implements Interceptor {
    private Log logger = Log.getLog(EedaMenuInterceptor.class);
    
    //这个map 作为登录用户的cahce, 免得每次都查很多次DB，消耗connection
    public static Map<Long, List<Record>> menuCache = null;
    
    @Override
    public void intercept(Invocation ai) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            loadMenu(ai.getController());
        }
        ai.invoke();
    }

    /*
     * 这个如果不放这里,那么每个controller的index凡是要render HTML的地方都要写调用
     */
    private void loadMenu(Controller controller) {
        logger.debug("EedaInterceptor loadMenu...");
        UserLogin user = UserLogin.getCurrentUser();
        Long user_id = user.getLong("id");
        Long office_id = user.getLong("office_id");
        
        List<Record> modules = Collections.EMPTY_LIST;
        if(menuCache == null){
            menuCache = new HashMap<Long, List<Record>>();
            modules = buildMenu(user, user_id, office_id);
        }else{
            if(menuCache.get(user_id)==null){
                modules = buildMenu(user, user_id, office_id);
            }else{
                modules = menuCache.get(user_id);
                logger.debug("EedaInterceptor loadMenu from cache...");
            }
        }
        controller.setAttr("modules", modules);
    }

    private List<Record> buildMenu(UserLogin user, Long user_id, Long office_id) {
        String username = user.getStr("user_name");
        // 查询当前用户菜单
        String sql = "select distinct module.* from eeda_modules module"
                + " where exists ("
                + "     select u.office_id, u.user_name, r.name role_name, ur.role_id, m.module_name, m.id module_id, "
                    + " p.code permission_code, p.name permission_name, rp.id rp_id, rp.is_authorize, p.url, m.seq"
                    + " from user_login u, user_role ur, role r, eeda_modules m, permission p, role_permission rp"
                    + " where u.user_name=ur.user_name and ur.role_id=r.id"
                    + " and u.office_id=m.office_id and m.id=p.module_id "
                    + " and rp.role_id=r.id and rp.permission_id=p.id and rp.module_id=m.id and rp.is_authorize=1"
                    + " and rp.permission_code like '%list' "
                    + " and m.parent_id=module.id and m.office_id=? and u.user_name=?"
                + " ) order by seq";
        List<Record> modules = Db.find(sql, office_id, username);
        for (Record module : modules) {
            sql = "select u.office_id, u.user_name, r.name role_name, ur.role_id, m.module_name, m.id module_id, "
                    + " p.code permission_code, p.name permission_name, rp.id rp_id, rp.is_authorize, p.url, m.seq"
                    + " from user_login u, user_role ur, role r, eeda_modules m, permission p, role_permission rp"
                    + " where u.user_name=ur.user_name and ur.role_id=r.id"
                    + " and u.office_id=m.office_id and m.id=p.module_id "
                    + " and rp.role_id=r.id and rp.permission_id=p.id and rp.module_id=m.id and rp.is_authorize=1"
                    + " and rp.permission_code like '%list' "
                    + " and m.parent_id=? and m.office_id=? and u.user_name=?"
                    + " order by m.seq";

            List<Record> orders = Db.find(sql, module.get("id"), office_id,
                    username);
            for (Record order : orders) {
                String urlStr = order.getStr("url");
                if(!urlStr.startsWith("http")){
                    order.set("url", "/"+urlStr);
                }
            }
            module.set("orders", orders);
        }

        if (modules == null){
            modules = Collections.EMPTY_LIST;
        }else{
            menuCache.put(user_id, modules);
        }
        return modules;
    }
}
