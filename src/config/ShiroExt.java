package config;import java.beans.BeanInfo;import java.beans.Introspector;import java.beans.PropertyDescriptor;import java.util.List;import java.util.Map;import models.Office;import models.RolePermission;import models.UserLogin;import models.UserOffice;import org.apache.shiro.SecurityUtils;import org.apache.shiro.subject.Subject;//import org.bee.tl.core.GroupTemplate;import org.beetl.core.GroupTemplate;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;/* gt.registerFunctionPackage("so",new ShiroExt ()); 你可以在模板里直接调用，譬如 @if(so.isGuest()) { */public class ShiroExt {	/**	 * The guest tag	 * 	 * @return	 */	public boolean isGuest() {		return getSubject() == null || getSubject().getPrincipal() == null;	}	/**	 * The user tag	 * 	 * @return	 */	public boolean isUser() {		return getSubject() != null && getSubject().getPrincipal() != null;	}	/**	 * The authenticated tag	 * 	 * @return	 */	public boolean isAuthenticated() {		return getSubject() != null && getSubject().isAuthenticated();	}	public boolean isNotAuthenticated() {		return !isAuthenticated();	}	/**	 * The principal tag	 * 	 * @param map	 * @return	 */	public String principal(Map map) {		String strValue = null;		if (getSubject() != null) {			// Get the principal to print out			Object principal;			String type = map != null ? (String) map.get("type") : null;			if (type == null) {				principal = getSubject().getPrincipal();			} else {				principal = getPrincipalFromClassName(type);			}			String property = map != null ? (String) map.get("property") : null;			// Get the string value of the principal			if (principal != null) {				if (property == null) {					strValue = principal.toString();				} else {					strValue = getPrincipalProperty(principal, property);				}			}		}		if (strValue != null) {			return strValue;		} else {			return null;		}	}	/**	 * The hasRole tag	 * 	 * @param roleName	 * @return	 */	public boolean hasRole(String roleName) {		return getSubject() != null && getSubject().hasRole(roleName);	}	/**	 * The lacksRole tag	 * 	 * @param roleName	 * @return	 */	public boolean lacksRole(String roleName) {		boolean hasRole = getSubject() != null && getSubject().hasRole(roleName);		return !hasRole;	}	/**	 * The hasAnyRole tag	 * 	 * @param roleNames	 * @return	 */	public boolean hasAnyRole(String roleNames) {		boolean hasAnyRole = false;		Subject subject = getSubject();		if (subject != null) {			// Iterate through roles and check to see if the user has one of the			// roles			for (String role : roleNames.split(",")) {				if (subject.hasRole(role.trim())) {					hasAnyRole = true;					break;				}			}		}		return hasAnyRole;	}	/**	 * The hasPermission tag	 * 这个是针对eeda权限体系改过的   Ray 2016-11-15	 * @param p	 * @return	 */	public boolean hasPermission(String p) {//		return getSubject() != null && getSubject().isPermitted(p);	    String userName=getSubject().getPrincipal().toString();	    String sql = "select u.office_id, u.user_name, r.name role_name, ur.role_id, m.module_name, m.id module_id, "            +" p.code permission_code, p.name permission_name, rp.id rp_id, rp.is_authorize "            +"    from user_login u, user_role ur, role r, eeda_modules m, permission p, role_permission rp "            +"where u.user_name=ur.user_name and ur.role_id=r.id "            +"and u.office_id=m.office_id and m.id=p.module_id  "            +"and rp.role_id=r.id and rp.permission_id=p.id and rp.module_id=m.id and rp.is_authorize=1 "            +"and u.user_name=? and p.code=? ";	    Record rec = Db.findFirst(sql, userName, p);	    boolean isHas=false;	    if(rec!=null){	        isHas = true;	    }	    //System.out.println("userName:"+userName+", permission_code:"+p+", isHas:"+isHas);	    return isHas;	}		/**	 * The hasPermission tag	 * 	 * @param p	 * @return	 */	public boolean hasPermission(String p, String userName) {		//TODO 判断当前用户的office_id		boolean is_hasPermission = false;		UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name =?",userName);		//UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);		Office parentOffice = Office.dao.findFirst("select * from office where id = ?",user.get("office_id"));		Long parentID = parentOffice.get("belong_office");		if(parentID == null || "".equals(parentID)){			parentID = parentOffice.getLong("id");		}				List<RolePermission> list = 				RolePermission.dao.find("select rp.* from user_login ul "									+"	left join user_role ur on ur.user_name = ul.user_name "									+"	left join role_permission rp on rp.role_code = ur.role_code where ul.user_name = ? and rp.permission_code =? and rp.office_id = ? and (rp.is_authorize != ?)",userName,p,parentID,false);				if(list.size()>0){			is_hasPermission = true;		}					/*return getSubject() != null && getSubject().isPermitted(p);*/		return is_hasPermission && getSubject() != null && getSubject().isPermitted(p);	}	/**	 * The lacksPermission tag	 * 	 * @param p	 * @return	 */	public boolean lacksPermission(String p) {		return !hasPermission(p);	}	@SuppressWarnings({ "unchecked" })	private Object getPrincipalFromClassName(String type) {		Object principal = null;		try {			Class cls = Class.forName(type);			principal = getSubject().getPrincipals().oneByType(cls);		} catch (ClassNotFoundException e) {		}		return principal;	}	private String getPrincipalProperty(Object principal, String property) {		String strValue = null;		try {			BeanInfo bi = Introspector.getBeanInfo(principal.getClass());			// Loop through the properties to get the string value of the			// specified property			boolean foundProperty = false;			for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {				if (pd.getName().equals(property)) {					Object value = pd.getReadMethod().invoke(principal, (Object[]) null);					strValue = String.valueOf(value);					foundProperty = true;					break;				}			}			if (!foundProperty) {				final String message = "Property [" + property + "] not found in principal of type [" + principal.getClass().getName()				        + "]";				throw new RuntimeException(message);			}		} catch (Exception e) {			final String message = "Error reading property [" + property + "] from principal of type [" + principal.getClass().getName()			        + "]";			throw new RuntimeException(message, e);		}		return strValue;	}	protected Subject getSubject() {		return SecurityUtils.getSubject();	}	public static void main(String[] args) {		GroupTemplate gt = new GroupTemplate();		gt.registerFunctionPackage("shiro", new ShiroExt());	}}