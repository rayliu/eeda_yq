package controllers.util;

import java.util.List;

import models.Location;

public class LocationUtil {
	public static Location getLocation(String locCode) {
		Location loc = null;
		List<Location> provinces = Location.dao
				.find("select * from location where pcode ='1'");
		Location l = Location.dao
				.findFirst("select * from location where code = (select pcode from location where code = '"
						+ locCode + "')");
		if (provinces.contains(l)) {
			loc = Location.dao
					.findFirst("select l.name as city,l1.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code = '"
							+ locCode + "'");
		} else {
			loc = Location.dao
					.findFirst("select l.name as district, l1.name as city,l2.name as province,l.code from location l left join location  l1 on l.pcode =l1.code left join location l2 on l1.pcode = l2.code where l.code ='"
							+ locCode + "'");
		}
		return loc;
	}
}
