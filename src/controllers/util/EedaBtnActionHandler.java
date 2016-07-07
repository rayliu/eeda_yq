package controllers.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * //action处理：针对 保存，审核，撤销 等按钮动作做相应的赋值，
 * 
 * 类型有：
 * 1. 固定值 赋值
 * 2. 单据号码 自动生成
 * 3. ？撤销时，判断是否有下级单据
 * 
 * @author Ray Liu
 *
 */
public class EedaBtnActionHandler {
    private static Logger logger = Logger.getLogger(EedaBtnActionHandler.class);
    
    public static void handleBtnAction(String moduleId, String btnAction, String orderId){
        Record action = Db.findFirst("select * from structure_action where module_id=? and action_name=?"
                , moduleId, btnAction);
        if(action == null)
            return;
        
        String actionScript = action.getStr("action_script");
        Gson gson = new Gson();  
        List<LinkedTreeMap> list = gson.fromJson(actionScript, new TypeToken<List<LinkedTreeMap>>(){}.getType()); 
        for (Map commandMap: list) {
            String commandStr = commandMap.get("command").toString();
            Map<String, ?> commandDto = gson.fromJson(commandStr, HashMap.class);
            String condition = commandDto.get("condition").toString();
            logger.debug(condition);
            processCondition(orderId, commandDto, condition, moduleId);
        }
    }

    private static void processCondition(String orderId,
            Map<String, ?> commandDto, String condition, String moduleId) {
        if("ID为空".equals(condition) || "ID不为空".equals(condition)){
            List<Map<String, String>> fieldList = (List)commandDto.get("setValueList");
            for (Map<String, String> fieldMap: fieldList) {
                for (Entry<String, String> entry: fieldMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    logger.debug(key+":"+value);
                    String[] keys= key.split(",");
                    String structureId = keys[0].split(":")[1];
                    String fieldName = keys[1].split(":")[1];
                    if(("自动生成单号").equals(value)){
                        //判断单号是否已经生成
                        Record rec = Db.findFirst("select " + fieldName + " from t_" + structureId + " where id="+orderId);
                        if(rec != null && rec.getStr(fieldName) == null){
                            Db.update("update t_" + structureId + " set " + fieldName + " = " + 
                                    OrderNoGenerator.getNextOrderNo("") + " where id=" + orderId);
                        }
                    }else{
                        Db.update("update t_"+structureId+" set "+fieldName+"='" + value + "' where id="+orderId);
                    }
                }
            }
        }else if("列表中存在上级单据".equals(condition)){
            String structure_id = commandDto.get("structure_id").toString();
            Record rec = Db.findFirst("select * from structure where module_id="+moduleId+" and add_btn_type='弹出列表, 从其它数据表选取' "
                    + " and add_btn_setting like '%\"structure_id\":\"3\"%' ");
            if(rec ==null)
                return;
            String order_detail_structure_id = rec.get("id").toString();
            List<Map<String, String>> fieldList = (List)commandDto.get("setValueList");
            for (Map<String, String> fieldMap: fieldList) {
                for (Entry<String, String> entry: fieldMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    logger.debug(key+":"+value);
                    String[] keys= key.split(",");
                    String structureId = keys[0].split(":")[1];
                    String fieldName = keys[1].split(":")[1];
                    Db.update("update t_"+structureId+" set "+fieldName+"='"+value
                            +"' where id in(select ref_t_id from t_"+order_detail_structure_id+" where parent_id="+orderId+")");
                }
            }
        }
    }
    
}
