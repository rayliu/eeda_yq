package controllers.eeda;import java.io.IOException;import java.util.ArrayList;import java.util.Calendar;import java.util.Date;import java.util.HashMap;import java.util.LinkedHashMap;import java.util.List;import java.util.Map;import models.Location;import models.eeda.Order;import models.eeda.OrderItem;import org.apache.shiro.SecurityUtils;import org.apache.shiro.subject.Subject;import com.fasterxml.jackson.core.JsonParseException;import com.fasterxml.jackson.databind.JsonMappingException;import com.fasterxml.jackson.databind.ObjectMapper;import com.jfinal.aop.Before;import com.jfinal.core.Controller;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Db;import com.jfinal.plugin.activerecord.Record;import com.jfinal.plugin.activerecord.tx.Tx;public class LocationController extends Controller {	private Log logger = Log.getLog(LocationController.class);	Subject currentUser = SecurityUtils.getSubject();	public void province() {        List<Record> locationList = Db.find("select * from location where pcode ='1'");        renderJson(locationList);    }    public void city() {        String cityId = getPara("id");        System.out.println(cityId);        List<Record> locationList = Db.find("select * from location where pcode ='" + cityId + "'");        renderJson(locationList);    }    public void area() {        String areaId = getPara("id");        System.out.println(areaId);        List<Record> locationList = Db.find("select * from location where pcode ='" + areaId + "'");        renderJson(locationList);    }    public void searchAllCity() {        String province = getPara("province");        List<Location> locations = Location.dao                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"                        + province + "'))");        renderJson(locations);    }    public void searchAllDistrict() {        String city = getPara("city");        List<Location> locations = Location.dao                .find("select * from location where pcode=(select code from location where name = '" + city + "')");        renderJson(locations);    }        // 一次查出省份,城市,区    public void searchAllLocation() {    	    	List<Location> provinceLocations = Location.dao.find("select * from location where pcode ='1'");    	        String province = getPara("province");        List<Location> cityLocations = Location.dao                .find("select * from location where id in (select id from location where pcode=(select code from location where name = '"                        + province + "'))");                String city = getPara("city");        List<Location> districtLocations = Location.dao                .find("select * from location where pcode=(select code from location where name = '" + city + "')");        Map<String, List<Location>> map = new HashMap<String, List<Location>>();        map.put("provinceLocations", provinceLocations);        map.put("cityLocations", cityLocations);        map.put("districtLocations", districtLocations);    	renderJson(map);    }}