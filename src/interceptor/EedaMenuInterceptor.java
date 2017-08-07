package interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.UserLogin;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
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
    
    //这个map 作为登录用户的url cahce, 用于判断用户是否可以访问该URL模块
    public static Map<Long, List<String>> menuUrlCache = null;
    
    @Override
    public void intercept(Invocation ai) {
        
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            UserLogin user = UserLogin.getCurrentUser();
            loadMenu(ai.getController(), user);
            
            //判断用户是否可以访问该URL模块
            if(menuUrlCache.get(user.getLong("id"))!=null){
                List<String> userUrlList = menuUrlCache.get(user.getLong("id"));
                String actionKey = ai.getActionKey();
<<<<<<< HEAD
                
                String key = "/";
                if(actionKey.split("/").length > 0){
                    key = "/"+actionKey.split("/")[1];
                }
=======
                if("/".equals(actionKey)){
                	actionKey = "/dashBoard";
                }
                String key = "/"+actionKey.split("/")[1];
>>>>>>> 车队：托运工作单查询条件加一个结算车牌
                logger.debug("action key: "+key);
                if(!"/".equals(ai.getActionKey()) && !userUrlList.contains(key)){//actionKey是否存在于授权模块中？
                    ai.getController().renderError(403);
                }
            }
            
        }
        
        
        ai.invoke();
    }

    /*
     * 这个如果不放这里,那么每个controller的index凡是要render HTML的地方都要写调用
     */
    private void loadMenu(Controller controller, UserLogin user) {
        logger.debug("EedaInterceptor loadMenu...");
        
        Long user_id = user.getLong("id");
        Long office_id = user.getLong("office_id");
        
        List<Record> modules = Collections.EMPTY_LIST;
        if(menuCache == null){
            menuCache = new HashMap<Long, List<Record>>();
            menuUrlCache = new HashMap<Long, List<String>>();
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
                    + " and p.code like '%list' "
                    + " and m.parent_id=module.id and m.office_id=? and u.user_name=?"
                + " ) order by seq";
        List<Record> modules = Db.find(sql, office_id, username);
        getNextLevelModules(office_id, username, modules, user_id);//获取第二层的菜单
        
        if (modules == null){
            modules = Collections.EMPTY_LIST;
        }else{
            menuCache.put(user_id, modules);
        }
        return modules;
    }

    //获取第二层的菜单
    private void getNextLevelModules(Long office_id, String username,
            List<Record> modules, Long user_id) {
        List<String> modulesUrl = new LinkedList<String>();
        String sql;
        for (Record module : modules) {
            sql = "SELECT "
            		+"    DISTINCT m.module_name,"
                    +"    u.office_id,"
                    +"    u.user_name,"
                    +"    GROUP_CONCAT(DISTINCT r. NAME) role_name,"
                    +"    ur.role_id,"
                    
                    +"    m.id module_id,"
                    +"    p.code permission_code,"
                    +"    p.name permission_name,"
                    +"    rp.id rp_id,"
                    +"    rp.is_authorize,"
                    +"    p.url,"
                    +"    m.seq"
                    +" FROM"
                    +"    eeda_modules m"
                    +"    left join permission p on m.id = p.module_id"
                    +"    left join user_login u on u.office_id = m.office_id"
                    +"    left join user_role ur on u.user_name = ur.user_name"
                    +"    left join role r on ur.role_id = r.id"
                    +"     left join role_permission rp on rp.role_id = r.id "
                    +"        AND rp.permission_id = p.id"
                    +"        AND rp.module_id = m.id"
                    +" WHERE "
                    +"         rp.is_authorize = 1"
                    +"        AND p.code LIKE '%list'"
                    +"        and m.parent_id = ?"
                    +"        AND m.office_id = ?"
                    +"        AND u.user_name = ?"
                    +" GROUP BY module_name"
                    +" ORDER BY m.seq";//union 查folder
            //logger.debug("EedaInterceptor module_id:"+module.get("id")+", office_id:"+office_id+", username:"+username);
            List<Record> orders = Db.find(sql, module.get("id"), office_id,
                    username);
            
            
            for (Record order : orders) {//再查一层单据
                String urlStr = order.getStr("url");
                if(!urlStr.startsWith("http")){
                    order.set("url", "/"+urlStr);
                }
                modulesUrl.add("/"+urlStr);
            }
            if(orders.size()==1){
                module.set("is_one_module", "Y");
            }
            
            module.set("orders", orders);
        }//end of for
        menuUrlCache.put(user_id, modulesUrl);
    }
    
}//~
