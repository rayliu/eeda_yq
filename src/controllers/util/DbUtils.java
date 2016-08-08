package controllers.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class DbUtils {
	private static Log logger = Log.getLog(DbUtils.class);
	
	public static String buildConditions(Map<String, String[]> paraMap) {
		String condition = "";
		Map<String, Map<String, String>> dateFieldMap = new HashMap<String, Map<String, String>>();

        for (Entry<String, String[]> entry : paraMap.entrySet()) {
            String key = entry.getKey();
            String filterValue = entry.getValue()[0];
            
            if(StringUtils.isNotEmpty(filterValue) && !"undefined".equals(filterValue)){
            	logger.debug(key + ":" + filterValue);
            	if(key.endsWith("_no") || key.endsWith("_name")){
            		condition += " and " + key + " like '%" + filterValue + "%' ";
            		continue;
            	}else if(key.endsWith("_id") || key.endsWith("_status")){
            		condition += " and " + key + " = '" + filterValue + "' ";
            		continue;
            	}else if(key.endsWith("_begin_time")){
            		key = key.replaceAll("_begin_time", "");
            		Map<String, String> valueMap = dateFieldMap.get(key)==null?new HashMap<String, String>():dateFieldMap.get(key);
            		valueMap.put("_begin_time", filterValue);
            		dateFieldMap.put(key, valueMap);
            		continue;
            	}else if(key.endsWith("_end_time")){
            		key = key.replaceAll("_end_time", "");
            		Map<String, String> valueMap = dateFieldMap.get(key)==null?new HashMap<String, String>():dateFieldMap.get(key);
            		valueMap.put("_end_time", filterValue);
            		dateFieldMap.put(key, valueMap);
            		continue;
            	}
            }	
        }
        
        //处理日期
        for (Entry<String, Map<String, String>> entry : dateFieldMap.entrySet()) {
        	String beginTime = "1970-1-1";
        	String endTime = "2037-12-31";
        	
        	String key = entry.getKey();
        	
        	Map<String, String> valueMap = entry.getValue();
        	for (Entry<String,String> valueEntry : valueMap.entrySet()) {
        		String subKey = valueEntry.getKey();
        		if(subKey.equals("_begin_time")){
        			beginTime = valueEntry.getValue();
            		continue;
            	}else if(subKey.equals("_end_time")){
            		endTime = valueEntry.getValue();
            		continue;
            	}
			}
        	condition += " and " + key + " between '" + beginTime + "' and '" + endTime+ " 23:59:59' ";
        }
        logger.debug("condition: "+condition);
        return condition;
	}
	
	public static void handleList(List<Map<String, String>> itemList,
	        String master_order_id, Class<?> clazz,
	        String master_col_name) 
			throws InstantiationException, IllegalAccessException {
    	for (Map<String, String> rowMap : itemList) {//获取每一行
    		Model<?> model = (Model<?>) clazz.newInstance();
    		
    		String rowId = rowMap.get("id");
    		String action = rowMap.get("action");
    		if(StringUtils.isEmpty(rowId)){
				setModelValues(rowMap, model);
    			model.set(master_col_name, master_order_id);
    			model.save();	
    		}else{
    			if("DELETE".equals(action)){//delete
    				Model<?> deleteModel = model.findById(rowId);
        			deleteModel.delete();
        		}else{//UPDATE
        			Model<?> updateModel = model.findById(rowId);
        			setModelValues(rowMap, updateModel);
        			updateModel.update();
        		}
    		}
    			
		}
	}
	
	public static void handleLists(List<Map<String, ?>> itemList,
	        String master_order_id, Class<?> clazz,Class<?> itemClazz,
	        String master_col_name) 
			throws InstantiationException, IllegalAccessException {
    	for (Map<String, ?> rowMap : itemList) {//获取每一行
    		Model<?> model = (Model<?>) clazz.newInstance();
    		Object model_id = null;
    		Object rowId = rowMap.get("id");
    		Object action = rowMap.get("action");
    		if(StringUtils.isEmpty(rowId.toString())){
				setModelValues(rowMap, model);
    			model.set(master_col_name, master_order_id);
    			model.save();	
    			model_id = model.get("id");
    		}else{
    			if("DELETE".equals(action)){//delete
    				Model<?> deleteModel = model.findById(rowId);
        			deleteModel.delete();
        		}else{//UPDATE
        			Model<?> updateModel = model.findById(rowId);
        			setModelValues(rowMap, updateModel);
        			updateModel.update();
        			model_id = updateModel.get("id");
        		}
    		}
    		
    		
    		for (Entry<String, ?> entry : rowMap.entrySet()) { 
    			String key = entry.getKey();
    			if(key.endsWith("_list")){
    				List<Map<String, ?>> list = (ArrayList<Map<String, ?>>)rowMap.get(key);
    				handleLists(list, model_id.toString(), itemClazz,itemClazz, "order_id");
    			}
    		}
    			
		}
	}
	
	public static void deleteHandle(List<Map<String, String>> itemList,
            Class<?> clazz, Map<String, String> master_ref_col){
	    List<String> idList = new ArrayList<String>();
	    for (Map<String, String> rowMap : itemList) {//获取每一行
	        String rowId = rowMap.get("id");
	        if(StringUtils.isNotEmpty(rowId))
	            idList.add(rowId);
	    }
	    
	    if(idList.size()>0){
	        String structureId = master_ref_col.get("structure_id");
            String deleteSql = "delete from field where structure_id=" + structureId + " and id not in (" + StringUtils.join(idList, ", ") + ")";
            logger.debug(deleteSql);
            Db.update(deleteSql);
	        
        }
	}
	public static void handleList(List<Map<String, String>> itemList,
            Class<?> clazz,
            Map<String, String> master_ref_col) 
            throws InstantiationException, IllegalAccessException {
	    Model<?> inModel = (Model<?>) clazz.newInstance();
	    String modelName = inModel.getClass().getSimpleName();
	    if("Field".equals(modelName)){//针对common Field的处理
	        deleteHandle(itemList, clazz, master_ref_col);
	    }
	    
        for (Map<String, String> rowMap : itemList) {//获取每一行
            Model<?> model = (Model<?>) clazz.newInstance();
            
            String rowId = rowMap.get("id");
            String action = rowMap.get("action");
            if(StringUtils.isEmpty(rowId)){//创建
                model.save();//先创建，获取id以方便创建从表时获取上级 id
                setModelValues(rowMap, model);
                for (String col_name : master_ref_col.keySet()) {
                    model.set(col_name, master_ref_col.get(col_name));
                }
                
                model.update();
            }else if("DELETE".equals(action)){//delete
                Model<?> deleteModel = model.findById(rowId);
                deleteModel.delete();
            }else{//UPDATE
                Model<?> updateModel = model.findById(rowId);
                setModelValues(rowMap, updateModel);
                updateModel.update();
            }
        }
    }

    //遇到 _list 是从表Map, 递归处理， model = ?_list
	public static void setModelValues(Map<String, ?> dto, Model<?> model) {
	    String modelName = model.getClass().getSimpleName();

		for (Entry<String, ?> entry : dto.entrySet()) { 
			String key = entry.getKey();
			if(!key.endsWith("_list")){
            	String value = String.valueOf(entry.getValue()) ;
            	//忽略  action 字段
            	if(StringUtils.isNotEmpty(value) && !"action".equals(key)){
            		logger.debug(key+":"+value);
            		if("null".equals(value)){
                        value=null;
                    }
            		try {
                        model.set(key, value);
                        if("Field".equals(modelName) && "field_display_name".equals(key)){//对field 的特殊处理
                            model.set("field_name", PingYinUtil.getFirstSpell(value).toUpperCase());
                            //需判断当前表是否有相同字段名
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
            	}
            }
//			else{
//                String modelClassName = key.substring(0, key.indexOf("_list"));
//                try {
//                    List<Map<String, String>> itemList = (ArrayList<Map<String, String>>)dto.get(key);
//                    Class c = Class.forName("models.yh.structure."+StringUtils.capitalize(modelClassName));
//                   
//                    Map<String, String> master_ref= new HashMap<String, String>();
//                    master_ref.put("structure_id", model.get("id").toString());
//                    handleList(itemList, c, master_ref);
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                } catch (InstantiationException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
		}
	}
}
