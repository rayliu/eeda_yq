package interceptor;

import java.util.ArrayList;
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
import com.jfinal.kit.LogKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

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
            modules = buildMenu(user);
        }else{
            if(menuCache.get(user_id)==null){
                modules = buildMenu(user);
            }else{
                modules = menuCache.get(user_id);
                logger.debug("EedaInterceptor loadMenu from cache...");
            }
        }
        controller.setAttr("modules", modules);
    }

    private List<Record> buildMenu(UserLogin user) {
        Long user_id = user.getLong("id");
        // 查询当前用户菜单
        List<Record> menuList= new ArrayList<Record>();
        Long office_id = user.get("office_id");
        List<Record> level1List = Db.find("select *, 1 level from eeda_modules "
                + "where parent_id is null and delete_flag='N' and office_id=? order by seq", office_id);
        for(Record lvl1:level1List) {
            //查询后台设置中，开放出来的module
            List<Record> level2List = Db.find("select m.*, 2 level, "
                    + " concat('form/', CAST(m.id as CHAR), '-list') url"
                    + " from eeda_modules m, eeda_form_define fd "
                    + " where fd.module_id=m.id and fd.is_public='Y'"
                    + " and m.parent_id=? and m.delete_flag='N' and m.office_id=? order by seq", lvl1.getLong("id"), office_id);
            if(level2List==null || level2List.size()==0)
                continue;

            List<Record> menu2List= new ArrayList<Record>();
            for(Record lvl2:level2List) {
                //判断当前用户的角色role是否有该menu的权限
                String sql = "select ur.role_id role_id,ur.role_code, rp.permission_id, " + 
                        " p.permission_name, p.permission_type, pm.menu_id, m.module_name menu_name" +
                        " from user_login u" + 
                        " left join t_rbac_ref_user_role ur on u.user_name=ur.user_name" +
                        " left join t_rbac_ref_role_permission rp on ur.role_id=rp.role_id" +
                        " left join t_rbac_permission p on rp.permission_id = p.id" +
                        " left join t_rbac_ref_permission_menu pm on rp.permission_id = pm.permission_id and pm.office_id=?" +
                        " left join eeda_modules m on pm.menu_id = m.id"+
                        " where u.id=? and m.id=?";
                Record menuRec = Db.findFirst(sql, office_id, user.getLong("id"), lvl2.getLong("id"));
                if(menuRec!=null) {
//                    lvl2.set("is_menu_open", "Y");
                    menu2List.add(lvl2);
                }
            }
            //如果menu2List存在，加到lvl1中
            if(menu2List.size()>0) {
                lvl1.set("orders", menu2List);
                menuList.add(lvl1);
            }
        }

        if (menuList == null){
            menuList = Collections.EMPTY_LIST;
        }else{
            menuCache.put(user_id, menuList);
        }
        return menuList;
    }
}
