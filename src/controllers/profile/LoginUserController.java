package controllers.profile;import interceptor.EedaMenuInterceptor;import interceptor.SetAttrLoginUserInterceptor;import java.util.ArrayList;import java.util.Arrays;import java.util.Date;import java.util.HashMap;import java.util.List;import java.util.Map;import models.Office;import models.ParentOfficeModel;import models.Party;import models.UserCustomer;import models.UserLogin;import models.UserOffice;import models.UserRole;import models.eeda.profile.Employee;import org.apache.commons.lang.StringUtils;import org.apache.shiro.SecurityUtils;import org.apache.shiro.authz.annotation.RequiresAuthentication;import org.apache.shiro.subject.Subject;import com.google.gson.Gson;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.kit.StrKit;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import com.jfinal.plugin.activerecord.tx.Tx;import controllers.util.CompareStrList;import controllers.util.DbUtils;import controllers.util.MD5Util;import controllers.util.OrderCheckOfficeUtil;import controllers.util.ParentOffice;@RequiresAuthentication@Before(SetAttrLoginUserInterceptor.class)public class LoginUserController extends Controller {    private Log logger = Log.getLog(LoginUserController.class);    Subject currentUser = SecurityUtils.getSubject();        public static Long getLoginUserId(Controller controller) {    	Subject currentUserNew = SecurityUtils.getSubject();                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='"+currentUserNew.getPrincipal().toString()+"'");        if (user!=null) {            return user.getLong("id");        }        return -1L;    }        public static UserLogin getLoginUser(Controller controller) {    	Subject currentUserNew = SecurityUtils.getSubject();                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='"+currentUserNew.getPrincipal().toString()+"'");        if (user!=null) {            return user;        }        return null;    }        public static Office getLoginUserOffice(Controller controller) {    	Subject currentUserNew = SecurityUtils.getSubject();                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='"+currentUserNew.getPrincipal().toString()+"'");        Office office=Office.dao.findFirst("select * from office where id="+user.getOfficeId());        if (office!=null) {            return office;        }        return null;    }        public static String getLoginUserName(Controller controller) {    	Subject currentUserNew = SecurityUtils.getSubject();                UserLogin user = UserLogin.dao.findFirst("select * from user_login where user_name='"+currentUserNew.getPrincipal().toString()+"'");        String user_name = "";        if (user!=null) {        	user_name = user.get("c_name");    		if(user_name==null || "".equals(user_name))    			user_name = user.get("user_name");        }        return user_name;    }        public static String getUserNameById(String id) {        UserLogin user = UserLogin.dao.findFirst("select * from user_login where id="+id);        String user_name = "";        if (user!=null) {        	user_name = user.get("c_name");    		if(user_name==null || "".equals(user_name))    			user_name = user.get("user_name");        }        return user_name;    }        public static String getUserNameById(Long id) {        UserLogin user = UserLogin.dao.findFirst("select * from user_login where id="+id);        String user_name = "";        if (user!=null) {            user_name = user.get("c_name");            if(user_name==null || "".equals(user_name))                user_name = user.get("user_name");        }        return user_name;    }        @Before(EedaMenuInterceptor.class)    public void index() {        render("/eeda/profile/loginUser/loginUser.html");    }    // show增加用户页面    @Before(EedaMenuInterceptor.class)    public void addUser() {        render("/eeda/profile/loginUser/editUser.html");    }    // show编辑用户    @Before(EedaMenuInterceptor.class)    public void edit() {    	//String username = currentUser.getPrincipal().toString();        String id = getPara("id");        UserLogin user1 = LoginUserController.getLoginUser(this);        long office_id=user1.getLong("office_id");        //判断与登陆用户的office_id是否一致        if(office_id !=1 && !OrderCheckOfficeUtil.checkOfficeEqual("user_login", Long.valueOf(id), office_id)){        	renderError(403);// no permission            return;        }        if (id != null) {                     Record user  = Db.findFirst("select CAST(substr(ul.create_time,1,19) AS char) create_time,"            		+ " CAST(substr(ul.entry_time,1,10) AS char) entry_time,ul.*,GROUP_CONCAT(re.name) user_station"            		+ " from  user_login ul"            		+ " LEFT JOIN user_role ur on ur.user_name = ul.user_name"            		+ " LEFT JOIN role re on re.id = ur.role_id "            		+ "  WHERE ul.id = ?",id);            setAttr("lu", user);            render("/eeda/profile/loginUser/editUser.html");        }    }            @Before(EedaMenuInterceptor.class)    public void editPerson(){    	String username = currentUser.getPrincipal().toString();        String id = getPara();        if (username != null && id == null) {            UserLogin user = UserLogin.dao.findFirst(                    "select * from user_login where user_name=?", username);            setAttr("lu", user);            setAttr("status", "");            render("/eeda/profile/loginUser/EditSelf.html");        }    }    @Before({EedaMenuInterceptor.class, Tx.class})    public void savePerson(){    	//String username=getPara("username");        String id = getPara("userId");        String name = getPara("chineseName");        String pass = getPara("password");        String user_phone = getPara("user_phone");                if (id != "") {        	UserLogin user = UserLogin.dao.findById(id);        	if(pass != null && !"".equals(pass)){        	    String sha1Pwd = MD5Util.encode("SHA1", pass);        		user.set("password", sha1Pwd);        	}            user.set("password_hint", getPara("pw_hint"));            user.set("user_tel", getPara("user_tel"));            user.set("user_fax", getPara("user_fax"));            user.set("c_name", name);            user.set("user_phone",user_phone);            user.update();            setAttr("lu", user);            setAttr("status", "ok");            render("/eeda/profile/loginUser/EditSelf.html");        }    }    // 添加登陆用户    @Before({EedaMenuInterceptor.class, Tx.class})    public void saveUser() throws Exception { {    	UserLogin user = null;    	String username=getPara("username");        String id = getPara("userId");        String name = getPara("name");        //String officeids = getPara("officeIds");        String customerids = getPara("customerIds");        String user_phone = getPara("user_phone");        //String wechat_no = getPara("wechat_no");        if (id != "") {            user = UserLogin.dao.findById(id);            if(!StrKit.isBlank(getPara("password"))){                String sha1Pwd = MD5Util.encode("SHA1", getPara("password"));                user.set("password", sha1Pwd);            }        }else{        	user = new UserLogin();        	String sha1Pwd = MD5Util.encode("SHA1", getPara("password"));            user.set("password", sha1Pwd);        	user.set("user_name", username);        }        String all_customer = getPara("all_customer");        if("on".equals(all_customer)){        	user.set("all_customer", "Y");        }else{        	user.set("all_customer", "N");        }        user.set("user_phone",user_phone);        user.set("password_hint", getPara("pw_hint"));                user.set("user_tel",getPara("user_tel"));        user.set("email", getPara("email"));                user.set("user_fax", getPara("user_fax"));        if(StringUtils.isNotBlank(getPara("entry_time"))){        	user.set("entry_time", getPara("entry_time"));        }                                user.set("c_name", name);                user.set("user_name", username);        //user.set("wechat_no", wechat_no);        UserLogin current_user = LoginUserController.getLoginUser(this);        long office_id = current_user.getLong("office_id");                if (id != "") {            user.update();        } else {            user.set("office_id",office_id);            user.set("create_time",new Date());            user.save();        }        //saveOffice(user,officeids);        saveUserCustomer(user,customerids);                        id = user.getLong("id").toString();                String employee_json = getPara("employee_json");        String employee_id = getPara("employee_id");       	Gson gson = new Gson();          Map<String, ?> dto= gson.fromJson(employee_json, HashMap.class);          //保存员工建档信息        List<Map<String, String>> employee = (ArrayList<Map<String, String>>)dto.get("employee_json");		DbUtils.handleList(employee,"employee",id,"order_id");//        Record em = Db.findFirst("select * from employee where order_id = ",id);       //        if (StringUtils.isBlank(employee_id)) {//        	//create//        	employee_id = em.getLong("id").toString();//        	em = Employee.dao.findById(employee_id);//   			em.set("create_stamp",new Date());//   			em.set("office_id",office_id);//   			em.update();//   			employee_id = em.getLong("id").toString();//        } else {//        	em = Employee.dao.findById(employee_id);//            em.set("update_stamp",new Date());//            em.update();//        }                //addDefaultOffice(user, id);        setAttr("lu", user);        Record r = user.toRecord();        r.set("employee", Employee.dao.findById(employee_id));                //render("/eeda/profile/loginUser/EditUser.html");        renderJson(r);      }    }        @Before(Tx.class)	private void addDefaultOffice(UserLogin user, String id) {		String userName = currentUser.getPrincipal().toString();        UserOffice user_office = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);        UserOffice u_o = UserOffice.dao.findFirst("select * from user_office where user_name = ? and office_id = ?",user.get("user_name"),user_office.get("office_id"));        if(id == ""){        	 //将当前用户的网点给当前添加的用户            UserOffice new_user_office = null;            if(u_o != null){            	new_user_office = u_o;            	new_user_office.set("is_main", true);                new_user_office.update();                user.set("office_id", new_user_office.get("office_id"));            }else{            	new_user_office = new UserOffice();            	new_user_office.set("user_name",user.get("user_name"));                new_user_office.set("office_id",user_office.get("office_id"));                new_user_office.set("is_main", true);                new_user_office.save();                user.set("office_id", new_user_office.get("office_id"));                }             user.update();        }	}    // 删除用户    @Before(EedaMenuInterceptor.class)    public void del() {        String id = getPara();        if (id != null) {        	UserLogin u = UserLogin.dao.findFirst("select * from user_login where id = ?",id);        	if(u.get("is_stop") == null || "".equals(u.get("is_stop")) || u.get("is_stop").equals(false)        			|| u.get("is_stop").equals(0)){        		u.set("is_stop", true);        	}else{        		u.set("is_stop", false);        	}        	u.update();        }        render("/eeda/profile/loginUser/loginUser.html");    }    // 列出用户信息//    @RequiresPermissions(value = {PermissionConstant.PERMSSION_U_LIST})    public void listUser() {        ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);        // 获取总条数        String sql = "";        String querySQL= "";        Long parentID = pom.getBelongOffice();        //判断当前用户是否为管理员        if(parentID == null || "".equals(parentID)){        	sql = "select count(1) total from user_login ul "        			+ " left join office o on ul.office_id = o.id "        			+ " where o.id = " + pom.getParentOfficeId() + " or o.belong_office = "+ pom.getParentOfficeId()        			+ " ";        	querySQL = " SELECT "        			+" 	ul.*, r.id role_id, r. NAME position_name,GROUP_CONCAT((SELECT "        			+" CONCAT(em.module_name,' : ',GROUP_CONCAT( per.name)) msg "        			+" FROM "        			+"  eeda_modules em  "        			+" LEFT JOIN permission per on per.module_id = em.id "        			+" WHERE em.id=mr.module_id "        			+" GROUP BY em.id "        			+" ORDER BY em.id "        			+" ) SEPARATOR '  /  ') role_msg "        			+" FROM "        			+" 	user_login ul "        			+" LEFT JOIN office o ON ul.office_id = o.id "        			+" LEFT JOIN user_role ur ON ul.user_name = ur.user_name "        			+" LEFT JOIN module_role mr ON mr.role_id = ur.role_id "        			+" LEFT JOIN role r ON r.id = ur.role_id "        			+ " where o.id = " + pom.getParentOfficeId()+ " or o.belong_office = "+ pom.getParentOfficeId()        			+ " GROUP BY ul.id"        			+ " order by ul.id";        }else{        	sql = "select count(1) total from user_login ul  "        			+ " where office_id = "+ pom.getCurrentOfficeId()        			+ " ";        			        	querySQL = " SELECT "        			+" 	ul.*,r. NAME position_name,GROUP_CONCAT((SELECT "        			+" CONCAT(em.module_name,' : ',GROUP_CONCAT( per.name)) msg "        			+" FROM "        			+"  eeda_modules em  "        			+" LEFT JOIN permission per on per.module_id = em.id "        			+" WHERE em.id=mr.module_id "        			+" GROUP BY em.id "        			+" ORDER BY em.id "        			+" ) SEPARATOR '  /  ') role_msg "        			+" FROM "        			+" 	user_login ul "        			+" LEFT JOIN user_role ur ON ul.user_name = ur.user_name "        			+" LEFT JOIN module_role mr ON mr.role_id = ur.role_id "        			+" LEFT JOIN role r ON r.id = ur.role_id "        			+ " where office_id = "+ pom.getCurrentOfficeId()        			+ " GROUP BY ul.id"        			+ " order by ul.id";        }        // 获取当前页的数据        Record rec = Db.findFirst(sql);                List<Record> orders = Db.find(querySQL);        Map map = new HashMap();        map.put("draw", 1);        map.put("recordsTotal", rec.getLong("total"));        map.put("recordsFiltered", rec.getLong("total"));        map.put("data", orders);        renderJson(map);     }    public void officeList(){    	String id = getPara("userId");    	if(id==null || "".equals(id)){			 Map<String, List> map = new HashMap<String, List>();			 map.put("userOffice", null);			 renderJson(map);             return;    	}    	UserLogin user= UserLogin.dao.findById(id);    	List<UserOffice> ulist = UserOffice.dao.find("select uo.user_name,uo.office_id,o.office_name,uo.is_main from user_office uo left join office o on o.id = uo.office_id"    			+ "  where user_name =?",user.get("user_name"));		 Map<String, List> map = new HashMap<String, List>();		 map.put("userOffice", ulist);        renderJson(map);    	    }    @Before(Tx.class)    public void saveOffice(UserLogin user,String ids){    	List<UserOffice> ulist = UserOffice.dao.find("select * from user_office where user_name=?",user.get("user_name"));    	    	if(ids!=null&&!"".equals(ids)){    		String[] officeIds=ids.split(",");    		if(ulist.size()>0){    			List<Object> oldList = new ArrayList<Object>();    			for (UserOffice userOffice : ulist) {					oldList.add(userOffice.get("office_id"));				}     			List<String> strList = new ArrayList(Arrays.asList(officeIds));    	           			List<Object> temp = new ArrayList<Object>();    			for(int i=0;i<oldList.size();i++){    	        	for(int j=0;j<strList.size();j++){    	        		 if(oldList.get(i).toString().equals(strList.get(j).toString())){    	                     temp.add(oldList.get(i));                        	                 }    	        	}    	        }    			 for(int i = 0;i<temp.size();i++){    				 oldList.remove(temp.get(i));    				     	             strList.remove(temp.get(i).toString());    	        }    			if(oldList.size()>0){    				for (Object object : oldList) {    					UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",user.get("user_name"),object);    					uo.delete();    				}    			}    			    			    			for (Object object : strList) {    				if(object!=null&&!"".equals(object)){    					UserOffice userOffice= new UserOffice();        				userOffice.set("user_name", user.get("user_name"));        				userOffice.set("office_id", object);        				userOffice.save();    				}    								}    		}else{    			for (String string : officeIds) {    				if(string!=null&&!"".equals(string)){	    				UserOffice userOffice= new UserOffice();	    				userOffice.set("user_name", user.get("user_name"));	    				userOffice.set("office_id", string);	    				userOffice.save();    				}    			}    		}    	}else{    		for (UserOffice userOffice : ulist) {    			userOffice.delete();    		}    	}    	    	renderJson(user);    }    public void searchAllOffice() {    	String userName = currentUser.getPrincipal().toString();    	UserOffice currentoffice = UserOffice.dao.findFirst("select * from user_office where user_name = ? and is_main = ?",userName,true);    	Office parentOffice = Office.dao.findFirst("select * from office where id = ?",currentoffice.get("office_id"));    	Long parentID = parentOffice.get("belong_office");    	if(parentID == null || "".equals(parentID)){    		parentID = parentOffice.getLong("id");    	}    	List<Office> offices = Office.dao.find("select id,office_name,is_stop from office where id = ? or belong_office = ?",parentID,parentID);		renderJson(offices);	}    @Before(Tx.class)    public void saveIsmain(){    	UserLogin ul = UserLogin.dao.findById(getPara("id"));    	UserRole ur = UserRole.dao.findFirst("select * from user_role where user_name=? and role_code='admin'",ul.get("user_name"));    	UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",ul.get("user_name"),getPara("office_id"));    	/*判断当前用户不是管理员*/    	if(ur==null||"".equals(ur)){    		UserOffice user_office = UserOffice.dao.findFirst("select * from user_office where user_name=? and is_main=?",ul.get("user_name"),true);        	if(user_office!=null&&!"".equals(user_office)&&!user_office.equals(false)){        		user_office.set("is_main", false);        		user_office.update();        	}        	String office_id=getPara("office_id");        	boolean is_mian;        	if(office_id==null||"".equals(office_id)){        		is_mian=false;        	}else{        		is_mian=true;        	}        	if(uo==null){        		uo=new UserOffice();        		uo.set("user_name", ul.get("user_name"));        		uo.set("office_id", getPara("office_id"));        		uo.set("is_main",is_mian);        		uo.save();        	}else{        		        		uo.set("is_main",is_mian);        		uo.update();        	}        	/*ul.set("office_id",getPara("office_id"));*/        	ul.update();    	}    	renderJson();    }    public void searchAllCustomer(){    	    	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);		Long parentID = pom.getParentOfficeId();    	    	List<Record> partys =Db.find("select p.id pid, p.company_name,p.is_stop from party p left join office o on p.office_id = o.id where p.type = 'CUSTOMER' and (p.is_stop is null or p.is_stop = 0) and (o.id = ? or o.belong_office = ?)",parentID,parentID);		renderJson(partys);    }    @Before(Tx.class)    public void saveUserCustomer(UserLogin user,String customers){    	    	List<UserCustomer> uclist = UserCustomer.dao.find("select *from user_customer where user_name=?",user.get("user_name"));    	    	if(customers!=null&&!"".equals(customers)){    		String[] ids = customers.split(",");    		if(uclist==null||"".equals(uclist)){    			for (String string : ids) {    				if(string!=null&&!"".equals(string)){    					UserCustomer uc= new UserCustomer();        				uc.set("user_name", user.get("user_name"));        				uc.set("customer_id", string);        				uc.save();    				}    				    			}    		}else{    			List<Object> uc_ids = new ArrayList<Object>();    			for (UserCustomer u_c: uclist) {					uc_ids.add( u_c.get("customer_id"));				}    			CompareStrList com = new CompareStrList();    			List<Object> list = com.compare(uc_ids, ids);    			List<Object> removerList = new ArrayList<Object>();    			removerList=(List<Object>) list.get(0);    			if(removerList.size()>0){    				for (Object str : removerList) {    					UserCustomer uc = UserCustomer.dao.findFirst("select * from user_customer where user_name=? and customer_id=?",user.get("user_name"),str);    					uc.delete();    				}    			}    			    			List<Object> addList = (List<Object>) list.get(1);    			for (Object str : addList) {    				if(str!=null&&!"".equals(str)){    					UserCustomer uc= new UserCustomer();        				uc.set("user_name", user.get("user_name"));        				uc.set("customer_id", str);        				uc.save();    				}    								}    			    		}    		    	}else{    		for (UserCustomer u_c: uclist) {				u_c.delete();			}    	}    	renderJson();    }    public void delOffice(){    	String id= getPara("id");    	String office_id = getPara("office_id");    	UserLogin user= UserLogin.dao.findById(id);    	UserOffice uo = UserOffice.dao.findFirst("select * from user_office where user_name=? and office_id=?",user.get("user_name"),office_id);    	    	UserRole userRole = UserRole.dao.findFirst("select * from user_role where user_name=? and role_code='admin'",user.get("user_name"));    			if(uo.get("is_main")!=null&&!"".equals(uo.get("is_main"))&&!"0".equals(uo.get("is_main"))){    		user.set("office_id", null);    		user.update();    		if(userRole==null||"".equals(userRole)){    			uo.delete();    		}    	}else{    		uo.delete();    	}    	    	renderJson();    }    public void delCustomer(){    	String id= getPara("id");    	String customer_id = getPara("customer_id");    	UserLogin user= UserLogin.dao.findById(id);    	UserCustomer uc = UserCustomer.dao.findFirst("select * from user_customer where user_name=? and customer_id=?",user.get("user_name"),customer_id);    	uc.delete();    	renderJson();    }    public void customerList(){    	String id = getPara("userId");    	if(id==null || "".equals(id)){			 Map<String, List> map = new HashMap<String, List>();			 map.put("customerlist", null);			 renderJson(map);             return;    	}    	UserLogin user= UserLogin.dao.findById(id);    	List<UserCustomer> ulist = UserCustomer.dao.find("select uc.id,uc.customer_id, p.company_name,p.is_stop from user_customer uc left join party p on uc.customer_id = p.id where user_name=? ",user.get("user_name"));		Map<String, List> map = new HashMap<String, List>();		map.put("customerlist", ulist);        renderJson(map);    }    //网点全选和全不选    @Before(Tx.class)    public void OfficeAllSelect(){    	String is_check = getPara("is_check");    	String id = getPara("userId");    	UserLogin user = UserLogin.dao.findById(id);    	    	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);		Long parentID = pom.getParentOfficeId();    	    	if("true".equals(is_check)){    		//添加全部    		List<UserOffice> ulist= UserOffice.dao.find("select * from user_office where user_name = ?",user.get("user_name"));    		String str="";    		if(ulist.size()>0){    			StringBuffer buffer = new StringBuffer();        		for (UserOffice userOffice : ulist) {    				buffer.append(userOffice.get("office_id"));    				buffer.append(",");    			}        		str = buffer.substring(0, buffer.length()-1).toString();    		}    		    		String sql = "select * from office where (id = " + parentID + " or belong_office = "  + parentID +")";    		String condition = " ";    		if(str.length()>0){    			condition = " and id not in ( " +str+ " ) ";    		}    		List<Office> list = Office.dao.find(sql + condition);    		for (Office office : list) {				UserOffice userOffice = new UserOffice();				userOffice.set("user_name", user.get("user_name"));				userOffice.set("office_id", office.get("id"));				userOffice.save();					}    		    	}else{    		List<UserOffice> ulist= UserOffice.dao.find("select * from user_office where user_name = ? and (is_main is null or is_main = 0)",user.get("user_name"));    		for (UserOffice userOffice : ulist) {				userOffice.delete();			}    	}    	    	renderJson();    }  //客户全选和全不选    @Before(Tx.class)    public void selectAllCustomer(){    	String is_check = getPara("is_check");    	String id = getPara("userId");    	UserLogin user = UserLogin.dao.findById(id);    	    	    	ParentOfficeModel pom = ParentOffice.getInstance().getOfficeId(this);		Long parentID = pom.getParentOfficeId();    	    	if("true".equals(is_check)){    		user.set("all_customer", "Y");    		Db.update("delete from user_customer where user_name = ?",user.get("user_name"));    		String sql = "select p.* from party p left join office o on o.id = p.office_id where p.type ='CUSTOMER' and (o.id= " + parentID +" or o.belong_office = " + parentID +")";    		List<Party> list = Party.dao.find(sql );    		for (Party party : list) {    			Db.update("insert into `user_customer`(`CUSTOMER_ID`, `USER_NAME`) values(?, ?)",party.get("id"),user.get("user_name"));				}    		    	}else{    		user.set("all_customer", "N");    		Db.update("delete from user_customer where user_name = ?",user.get("user_name"));    	}    	user.update();    	    	renderJson("{\"result\":true}");    }    public void isSelectAll(){    	String id = getPara("userId");    	    	if(id !=null && !"".equals(id)){    		UserLogin user = UserLogin.dao.findById(id);    		Office office = Office.dao.findFirst("select count(*) as total from office");        	UserOffice uo = UserOffice.dao.findFirst("select count(*) as total from user_office  where user_name = ?",user.get("user_name"));        	if(office.get("total").equals(uo.get("total"))){        		renderText("checked");        	}else{        		renderText("nochecked");        		        	}    	}else{    		renderText("nochecked");    	}    	    }    public void isSelectAllCustomer(){    	String id = getPara("userId");    	    	if(id !=null && !"".equals(id)){    		UserLogin user = UserLogin.dao.findById(id);    		Party party = Party.dao.findFirst("select count(*) as total from party where type='CUSTOMER'");        	UserCustomer uo = UserCustomer.dao.findFirst("select count(*) as total from user_customer  where user_name = ?",user.get("user_name"));        	if(party.get("total").equals(uo.get("total"))){        		renderText("checked");        	}else{        		renderText("nochecked");        		        	}    	}else{    		renderText("nochecked");    	}    	    }    public void checkNameExist(){		String userName= getPara("username");		boolean checkObjectExist;				UserLogin user = UserLogin.dao.findFirst(                "select * from user_login ul where ul.user_name=? ", userName);		if(user == null){			checkObjectExist=true;		}else{			checkObjectExist=false;		}		renderJson(checkObjectExist);	}        public void checkOldPwd(){        String user_name= getPara("user_name");        String old_pwd= getPara("old_pwd");        String sha1Pwd = MD5Util.encode("SHA1", old_pwd);        boolean checkOldPwdExist;                UserLogin user = UserLogin.dao.findFirst(                "select * from user_login ul where ul.user_name=? and ul.password=?", user_name, sha1Pwd);        if(user != null){            checkOldPwdExist=true;        }else{            checkOldPwdExist=false;        }        renderJson(checkOldPwdExist);    }}