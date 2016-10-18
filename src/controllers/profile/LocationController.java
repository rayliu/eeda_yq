package controllers.profile;import interceptor.SetAttrLoginUserInterceptor;import java.util.Collections;import java.util.HashMap;import java.util.List;import java.util.Map;import org.apache.commons.lang.StringUtils;import org.apache.shiro.authz.annotation.RequiresAuthentication;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.kit.StrKit;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;@RequiresAuthentication@Before(SetAttrLoginUserInterceptor.class)public class LocationController extends Controller {    private Log logger = Log.getLog(LocationController.class);    // in config route已经将路径默认设置为/yh    // me.add("/yh", controllers.yh.AppController.class, "/yh");    public void index() {        render("/eeda/profile/location.html");    }    // 列出城市信息    public void listLocation() {    	    	String sLimit = "";        String pageIndex = getPara("draw");        if (getPara("start") != null && getPara("length") != null) {            sLimit = " LIMIT " + getPara("start") + ", " + getPara("length");        }        String sSearch = getPara("sSearch");        String columsSql = " select concat(t.name, ' ',t2.name,' ',t3.name) as name,t.name as province,t2.name as city,t3.name as district ";                String fromSql =" from location t left join location t2 on t.code = t2.pcode left join location t3 on t3.pcode=t2.code where t.pcode = 1";           String conditions=" where 1=1  ";        if (StringUtils.isNotEmpty(sSearch)){        	conditions+=" and UPPER(A.province) like '%"+sSearch.toUpperCase()+"%' "        			+ " or UPPER(A.city) like '%"+sSearch.toUpperCase()+"%' "        			+ " or UPPER(A.district) like '%"+sSearch.toUpperCase()+"%' ";        }                        List<Record> rec = Db.find("select * from (" + columsSql + fromSql+") A "+ conditions + sLimit);        //总数数量        Record recTotal = Db.findFirst("select count(*) total from(select * from (" + columsSql + fromSql+") A "+ conditions+") B");        Long total = recTotal.getLong("total");        logger.debug("total records:" + total);        Map orderMap = new HashMap();        orderMap.put("sEcho", pageIndex);        orderMap.put("iTotalRecords", total);        orderMap.put("iTotalDisplayRecords", total);        orderMap.put("aaData", rec);        renderJson(orderMap);    }        //查询港口和城市    public void searchPortAndCity(){        String portName = getPara("input");        long userId = LoginUserController.getLoginUserId(this);        List<Record> portList = Collections.EMPTY_LIST;        if(StrKit.isBlank(portName)){//从历史记录查找            String sql = "select h.ref_id, l.* from user_query_history h, location l "                    + "where h.ref_id=l.id and h.type='port' and h.user_id=?";            portList = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);            if(portList.size()==0){                sql = "select * from location where (type='port' or city_type='city') ";                portList = Db.find(sql+" ORDER BY name limit 10");            }            renderJson(portList);        }else{            String sql = "select * from location where (type='port' or city_type='city') ";                                    if (portName.trim().length() > 0 && portName!=null) {                sql +=" and (code like '%" + portName.toUpperCase() + "%' or name like '%" + portName + "%') ";            }            portList = Db.find(sql+"ORDER BY id desc");            renderJson(portList);        }    }          //查询港口    public void searchPort(){    	String portName = getPara("input");    	long userId = LoginUserController.getLoginUserId(this);    	if(StrKit.isBlank(portName)){//从历史记录查找    	    String sql = "select h.ref_id, l.* from user_query_history h, location l "    	            + "where h.ref_id=l.id and h.type='port' and h.user_id=?";    	    List<Record> portList = Collections.EMPTY_LIST;    	    portList = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);    	    if(portList.size()==0){    	        sql = "select * from location where type='port' ";    	        portList = Db.find(sql+" ORDER BY name limit 10");    	    }            renderJson(portList);    	}else{            List<Record> portList = Collections.EMPTY_LIST;            String sql = "select * from location where type='port' ";                        if (portName.trim().length() > 0 && portName!=null) {                sql +=" and (code like '%" + portName.toUpperCase() + "%' or name like '%" + portName + "%') ";            }            portList = Db.find(sql+"ORDER BY name limit 10");            renderJson(portList);    	}    }        //查询港口    public void searchCountry(){        String portName = getPara("input");        long userId = LoginUserController.getLoginUserId(this);        if(StrKit.isBlank(portName)){//从历史记录查找            String sql = "select h.ref_id, l.* from user_query_history h, location l "                    + "where h.ref_id=l.id and h.type='country' and h.user_id=?";            List<Record> portList = Collections.EMPTY_LIST;            portList = Db.find(sql+" ORDER BY query_stamp desc limit 10", userId);            if(portList.size()==0){                sql = "select * from location where type='country' ";                portList = Db.find(sql+" ORDER BY code limit 10");            }            renderJson(portList);        }else{            List<Record> portList = Collections.EMPTY_LIST;            String sql = "select * from location where type='country' ";                        if (portName.trim().length() > 0 && portName!=null) {                sql +=" and (code like '%" + portName.toUpperCase() + "%' or name like '%" + portName + "%') ";            }            portList = Db.find(sql+"ORDER BY code limit 10");            renderJson(portList);        }    }}