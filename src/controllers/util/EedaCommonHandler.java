package controllers.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class EedaCommonHandler {
    private static Log logger = Log.getLog(EedaCommonHandler.class);
    
    /**
     * editOrder页面: save传回来的data结构, 查询order传出去的结构按以下格式构造
     * orderDto{
     *    id:1，                  //单据的id，
     *    module_id: 13，         //对应的模块id，
     *    action: "保存",         //对应的按钮动作
     *    fields_list:[{         //类型为“字段”的数据，只有一行
     *          id:1,            //对应表名T_3.id
     *          structure_id:3,  //对应表名T_3
     *          F1_DH: "YS1001", //对应表中字段T_3.F1_DH
     *          F2_KH: "1",      //对应表中字段T_3.F2_KH
     *          F3_GYS: "8"      //对应表中字段T_3.F3_GYS
     *      }，{}],
     *    table_list:[           //类型为“列表”的数据，相当于从表
     *      {
     *         structure_id: 5,         //对应表名T_5
     *         structure_parent_id: 3,  //此从表对应主表为structure_id:3,
     *         row_list:[               //对应表名T_5中的多行
     *              {
     *                  id: 1,          //对应表名T_5.id
     *                  parent_id:1,    //对应表名T_3.id=1
     *                  ref_t_id: 3,    //对应其它关联表 id =3
     *                  F13_XH: "533",  //对应表中字段T_5.F13_XH
     *                  F14_SL: ""
     *                  F15_TJ: ""
     *                  F16_ZL: ""
     *                  F17_DZ: ""
     *                  table_list:[           //类型为“单品列表”的数据，相当于从表的从表
     *                      {
     *                          structure_id: 6,         //对应表名T_6
     *                          structure_parent_id: 5,  //此从表对应主表为structure_id:5,
     *                          row_list:[
     *                              {
     *                                  id: 1,          //对应表名T_6.id
     *                                  parent_id:1,    //对应表名T_5.id=1
     *                                  ref_t_id: 0,    //无对应其它关联表
     *                                  F18_XLH: "A33", //对应表中字段T_6.F18_XLH
     *                                  F19_BZ: ""      //对应表中字段T_6.F18_BZ
     *                              }
     *                          ]
     *                      }
     *                  ]
     *              }
     *         ] 
     *      },{}
     *    ],  
     * }
     */
    public static Record getOrderDto(String jsonStr){
        Record orderRec = new Record();
        Gson gson = new Gson();  
        Map<String, ?> dto= gson.fromJson(jsonStr, HashMap.class);
        String order_id = dto.get("id").toString();
        
        orderRec.set("id", order_id);
        orderRec.set("module_id", dto.get("MODULE_ID"));
        
        List fieldsList = new ArrayList();
        List tableList  = new ArrayList();
        orderRec.set("fields_list", fieldsList);
        orderRec.set("table_list", tableList);
        
        List<Map<String, ?>> strctureList = (ArrayList<Map<String, ?>>)dto.get("STRUCTURE_LIST");
        Map<String, ?> rootStructure = null;
        for (Map<String, ?> structure : strctureList) {
            if("字段".equals(structure.get("STRUCTURE_TYPE"))){
                Record fieldRec = buildFieldRec(order_id, structure);
                fieldsList.add(fieldRec);
                rootStructure = structure;
            }
        }
        
        Record rootRec = new Record();
        long structureId = ((Double)rootStructure.get("ID")).longValue();
        rootRec.set("ID", structureId);
        rootRec.set("PARENT_ID", 0l);
        buildTableDto(order_id, tableList, rootRec);
        logger.debug(orderRec.toJson());
        return orderRec;
    }

    /*
     * 递归构造table数据
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    private static void buildTableDto(String parent_id,
            List parentTableList, Record structureRec) {
        
        long structureId = structureRec.getLong("ID");
        long parentStructureId = structureRec.getLong("PARENT_ID");
        
        //查找下级从表
        List<Record> childStructureList = Db.find("select * from structure where parent_id="+ structureId);
        
        for (Record childStructure : childStructureList) {
            Record tableRec = new Record();
            tableRec.set("structure_id", childStructure.getLong("ID"));
            tableRec.set("structure_parent_id", childStructure.getLong("PARENT_ID"));
            
            String originStructureId = childStructure.get("id").toString();
            String tableName = "t_" + originStructureId;
            String refCon = "";
            String subCol = "";
            //获取field定义, 判断是否需要针对特殊列表转换ID -> 名字
            subCol = getSubCol(originStructureId);
            
            if("弹出列表, 从其它数据表选取".equals(childStructure.get("ADD_BTN_TYPE"))){
                String settingJson = childStructure.get("ADD_BTN_SETTING").toString();
                Gson gson = new Gson();  
                Map<String, ?> settingDto= gson.fromJson(settingJson, HashMap.class);
                String targetStructureId = settingDto.get("structure_id").toString();
                tableName += ", t_"+targetStructureId;
                refCon = " and t_"+originStructureId+".ref_t_id = t_"+targetStructureId+".id";
                subCol += getSubCol(targetStructureId);
            }
            List<Record> rowList = Db.find("select *, t_"+originStructureId+".id "//这里多写一个ID，因为DB中取最后一个列的ID
                    +subCol+" from "+tableName+" where t_"+originStructureId+".parent_id="+parent_id+refCon);
            
            //构造下级从表的结构
            if(rowList.size()>0){
                tableRec.set("row_list", rowList);
                for (Record record : rowList) {
                    List childTableList = new ArrayList();
                    record.set("table_list", childTableList);
                    buildTableDto(record.get("id").toString(), childTableList, childStructure);
                }
            }
            
            parentTableList.add(tableRec);
        }
    }

    private static String getSubCol(String structureId) {
        String subCol = "";
        String fieldSql = "select * from field where structure_id = ?";
        List<Record> fieldDefineList = Db.find(fieldSql, structureId);
        for (Record fieldDefine : fieldDefineList) {
            if("下拉列表".equals(fieldDefine.get("field_type"))) {
                String key = "F"+fieldDefine.getLong("id")+"_"+fieldDefine.getStr("field_name");
            
                if ("客户列表".equals(fieldDefine.get("field_type_ext_type")) 
                        || "供应商列表".equals(fieldDefine.get("field_type_ext_type"))){
                    subCol += ", (select abbr from contact where id=t_"+structureId+"."+key+") "+ key +"_INPUT";
                }else if("产品列表".equals(fieldDefine.get("field_type_ext_type"))){
                   subCol += ", (select item_no from product where id=t_"+structureId+"."+key+") "+ key +"_INPUT";
                }else if("城市列表".equals(fieldDefine.get("field_type_ext_type"))){
                    subCol += ", (get_loc_full_name(t_"+structureId+"."+key+")) "+ key +"_INPUT";
                }
            }
        }
        return subCol;
    }
    
    public static Map searchOrder(Enumeration<String>  paraNames, HttpServletRequest request){
        Map orderListMap = new HashMap();
        String structur_id = "";
        
        String draw = "";
        String start = "";
        String length = "";
        
        String colCondition = "";
        String subCol = "";
        
        //获取field定义
        List<String> fieldIdList = new ArrayList<String>();
        List<String> fieldNameList = new ArrayList<String>();
        for (Enumeration<String> e = paraNames; paraNames.hasMoreElements();){
            String paraName = e.nextElement();
            String paraValue = request.getParameter(paraName);
            System.out.println(paraName + "=" + paraValue);
            
            if("structure_id".equals(paraName)){
                structur_id = paraValue;
            }
            if("draw".equals(paraName)){
                draw = paraValue;
            }
            if("start".equals(paraName)){
                start = paraValue;
            }
            if("length".equals(paraName)){
                length = paraValue;
            }
            if(paraName.startsWith("F") && !paraName.endsWith("_INPUT")){//获取field定义
                String id = paraName.split("_")[0].replace("F", "");
                fieldIdList.add(id);
                fieldNameList.add(paraName);
            }
            
        }

        List<Record> resultList = null;
        //获取field定义, 判断是否需要针对特殊列表转换ID -> 名字
        String fieldSql = "select * from field where id in(" + StringUtils.join(fieldIdList, ", ")+")";
        List<Record> fieldDefineList = Db.find(fieldSql);
        
        Map<String, Map> dateMap = new HashMap<String, Map>();
        for (String fieldName : fieldNameList) {
                String key = fieldName;
                String value = request.getParameter(key);
                
                logger.debug("key="+key);
                if(key.endsWith("_begin_time")){
                    String dateFieldKey = key.substring(0, key.indexOf("_begin_time"));
                    Map dateValueMap = new HashMap();
                    dateValueMap = dateMap.get(dateFieldKey);
                    if(dateValueMap==null){
                        dateValueMap = new HashMap();
                    }
                    dateValueMap.put("begin_time", value);
                }else if(key.endsWith("_end_time")){
                    String dateFieldKey = key.substring(0, key.indexOf("_end_time"));
                    Map dateValueMap = new HashMap();
                    dateValueMap = dateMap.get(dateFieldKey);
                    if(dateValueMap==null){
                        dateValueMap = new HashMap();
                    }
                    dateValueMap.put("end_time", value);
                }else{
                    Record fieldDefine = getFieldDefine(key, fieldDefineList);
                    if("下拉列表".equals(fieldDefine.get("field_type"))) {
                           if( "客户列表".equals(fieldDefine.get("field_type_ext_type")) || "供应商列表".equals(fieldDefine.get("field_type_ext_type"))){
                               subCol += ", (select abbr from contact where id="+key+") "+ key +"_INPUT";
                               if(StringUtils.isNotEmpty(value)){
                                    colCondition += " and " + key + " = '" + value + "'";
                               }
                           }else if("城市列表".equals(fieldDefine.get("field_type_ext_type"))){
                               subCol += ", (get_loc_full_name("+key+")) "+ key +"_INPUT";
                           }
                    }else{
                        if(StringUtils.isNotEmpty(value))
                            colCondition += " and " + key + " like '%" + value + "%'";
                    }
                }
        }
        
        //处理日期
        for (Entry<String, Map> entry: dateMap.entrySet()) {
            String key = entry.getKey();
            Map valueMap = entry.getValue();
            colCondition += "and " + key + " between '" + valueMap.get("begin_time") + " 00:00:00' and " + valueMap.get("end_time") +" 23:59:59";
        }
        
        String sql = "select t.* "+ subCol +" from T_" + structur_id +" t where 1=1 " + colCondition;
        
        String totalSql = "select count(1) total from (" + sql + ") A";
        
        Record rec = Db.findFirst(totalSql);
        String sLimit = " limit " + start + ", " +length; 
        resultList = Db.find(sql +" order by id desc " + sLimit);
        if(resultList == null)
            resultList = Collections.EMPTY_LIST;
        
        
        orderListMap.put("draw", draw);
        orderListMap.put("recordsTotal", rec.getLong("total"));
        orderListMap.put("recordsFiltered", rec.getLong("total"));

        orderListMap.put("data", resultList);

        return orderListMap;
    }

    private static Record getFieldDefine(String fieldName, List<Record> fieldDefineList){
        Record rec = null;
        for (Record record : fieldDefineList) {
            String recFieldName = "F" + record.get("id") + "_" + record.get("field_name");
            if(fieldName.equals(recFieldName)){
                rec = record;
                break;
            }
        }
        return rec;
    }
    
    private static Record buildFieldRec(String order_id, Map<String, ?> structure) {
        long structureId = ((Double)structure.get("ID")).longValue();
        Record orderRec;
        String subCol = getSubCol(String.valueOf(structureId));
        if(structure.get("PARENT_ID") == null){
            String tableName = "T_" + structureId;
            List<Map> fieldList = (ArrayList<Map>)structure.get("FIELDS_LIST");
            orderRec = Db.findFirst("select * "+subCol+" from "+tableName+ " where id=?", order_id);
            
            orderRec.set("structure_id", structureId);
            logger.debug(orderRec.toJson());
            
        }else{
            int parentId = ((Double)structure.get("PARENT_ID")).intValue();
            String tableName = "T_" + structureId;
            orderRec = Db.findFirst("select * "+subCol+" from "+tableName+ " where parent_id=?", order_id);
            if(orderRec != null)
                orderRec.set("structure_id", structureId);
            logger.debug(orderRec.toJson());
        }
        return orderRec;
    }
    
    @SuppressWarnings("unchecked")
    public static void commonUpdate(Map<String, ?> dto) {
        String orderId = dto.get("id").toString();
        List<Map<String, String>> fields_list = (ArrayList<Map<String, String>>)dto.get("fields_list");
        for (Map<String, String> tableMap : fields_list) {//获取每一个主表+主表从属表
            String structure_id = tableMap.get("structure_id");
            String colSet = "";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key) || "structure_id".equals(key) || key.endsWith("_INPUT")){
                    continue;
                }
                colSet += ","+key + "='"+value+"'";
            }
            
            String sql = "update T_"+structure_id+" set " + colSet.substring(1) + " where id=" + orderId;
            logger.debug(sql);
            Db.update(sql);
        }
        
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        tablesUpdate(orderId, table_list);
        
        //action 保存，审核，撤销 等按钮动作
        EedaBtnActionHandler.handleBtnAction(dto.get("module_id").toString(), 
                dto.get("action").toString(), orderId);
    }

    private static void tablesUpdate(String parentId,
            List<Map<String, ?>> table_list) {
        for (Map<String, ?> tableMap : table_list) {//获取每一个从表
            String structure_id = tableMap.get("structure_id").toString();
            
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            
            //先处理删除
            tableRowDelete(parentId, structure_id, rowFieldsList);
            
            for (Map<String, ?> rowMap : rowFieldsList) {//表中每一行, update or insert
                String rowId = rowMap.get("id").toString();
                if(StringUtils.isEmpty(rowId)){
                    rowId = tableRowInsert(parentId, structure_id, rowMap).toString();
                }else{
                    tableRowUpdate(structure_id, rowMap, rowId);
                }
                //从表的行中是否还有从表
                if(rowMap.get("table_list") != null){
                    List<Map<String, ?>> tableList = (ArrayList<Map<String, ?>>)rowMap.get("table_list");
                    tablesUpdate(rowId, tableList);
                }
            }
        }
    }

    private static void tableRowDelete(String orderId, String structure_id,
            List<Map> rowFieldsList) {
        List<String> idList = new ArrayList<String>();
        for (Map<String, String> rowMap : rowFieldsList) {
            String rowId = rowMap.get("id").toString();
            if(StringUtils.isNotEmpty(rowId)){
                idList.add(rowId);
            }
        }
        
        if(idList.size()>0){
            String deleteSql = "delete from T_" + structure_id + " where parent_id=" + orderId + " and id not in (" + StringUtils.join(idList, ", ") + ")";
            logger.debug(deleteSql);
            Db.update(deleteSql);
        }else{
            String deleteSql = "delete from T_" + structure_id + " where parent_id=" + orderId;
            logger.debug(deleteSql);
            Db.update(deleteSql);
        }
    }

    private static void tableRowUpdate(String structure_id,
            Map<String, ?> rowMap, String rowId) {
        String colSet = "";
        for (Entry<String, ?> entry: rowMap.entrySet()) {//行的每个字段
            String key = entry.getKey();
            String value = entry.getValue().toString();
            if("id".equals(key) || key.endsWith("_INPUT") || "table_list".equals(key)){
                continue;
            }
            colSet += ","+key + "='"+value+"'";
        }
        String sql = "update T_" + structure_id + " set " + colSet.substring(1) + " where id=" +rowId;
        logger.debug(sql);
        Db.update(sql);
    }

    private static String tableRowInsert(String parentId, String structure_id,
            Map<String, ?> rowMap) {
        String rowId = "";
        String colName = "";
        String colValue = "";
        
        for (Entry<String, ?> entry: rowMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            if("id".equals(key) || key.endsWith("_INPUT") || "table_list".equals(key)){
                continue;
            }
            //嵌套的从表有可能上级ID是新增的，所以此时row本身parent_id 为空
            if("parent_id".equals(key) && StringUtils.isEmpty(value)){
                value = parentId;
            }
            colName += ","+key;
            colValue+= ",'"+value+"'";
        }
        
        String sql = "insert into T_"+structure_id+"("+colName.substring(1)+") values("+colValue.substring(1)+")";
        logger.debug(sql);
        Db.update(sql);
        rowId = getLastInsertID("T_"+structure_id).toString();
        return rowId;
    }
    
    public static String commonInsert(Map<String, ?> dto) {
        BigInteger order_id = new BigInteger("0");
        List<Map<String, String>> fields_list = (ArrayList<Map<String, String>>)dto.get("fields_list");
        for (Map<String, String> tableMap : fields_list) {//获取每一行
            String tableName = tableMap.get("structure_id");
            
            String colName = "parent_id";
            String colValue = "null";
            for (Entry<String, String> entry: tableMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if("id".equals(key) || key.endsWith("_INPUT") || "structure_id".equals(key)){
                    continue;
                }
                colName += ","+key;
                colValue+= ",'"+value+"'";
            }
            
            String sql = "insert into T_"+tableName+"("+colName+") values("+colValue+")";
            logger.debug(sql);
            Db.update(sql);
            order_id = getLastInsertID(tableName);
        }
        List<Map<String, ?>> table_list = (ArrayList<Map<String, ?>>)dto.get("table_list");
        tablesInsert(order_id, table_list);
        
        //action 保存，审核，撤销 等按钮动作
        EedaBtnActionHandler.handleBtnAction(dto.get("module_id").toString(), 
                dto.get("action").toString(), order_id.toString());
        return order_id.toString();
    }

    /*
     * 递归对从表进行新增插入
     */
    private static void tablesInsert(BigInteger parent_id,
            List<Map<String, ?>> table_list) {
        for (Map<String, ?> tableMap : table_list) {//获取每一行
            String tableName = tableMap.get("structure_id").toString();
            
            List<Map> rowFieldsList = (ArrayList<Map>)tableMap.get("row_list");
            for (Map<String, ?> rowMap : rowFieldsList) {
                String colName = "";
                String colValue = "";
                for (Entry<String, ?> entry: rowMap.entrySet()) {
                    String key = entry.getKey();
                    if("id".equals(key) || key.endsWith("_INPUT") || "structure_id".equals(key) || "table_list".equals(key)){
                        continue;
                    }
                    
                    String value = entry.getValue().toString();
                    if("parent_id".equals(key) && StringUtils.isEmpty(value)){
                        value = parent_id.toString();
                    }
                    colName += ","+key;
                    colValue+= ",'"+value+"'";
                }
                String sql = "insert into T_"+tableName+"("+colName.substring(1)+") values("+colValue.substring(1)+")";
                logger.debug(sql);
                Db.update(sql);
                List next_table_list = (List) rowMap.get("table_list");
                if(next_table_list!=null){
                    parent_id = getLastInsertID("T_"+tableName);
                    tablesInsert(parent_id, next_table_list);
                }
            }
        }
    }

    private static BigInteger getLastInsertID(String tableName) {
        BigInteger order_id;
        Record idRec = Db.findFirst("select LAST_INSERT_ID() id");
        BigInteger big = new BigInteger(idRec.getLong("id").toString());
        order_id = big;
        logger.debug(tableName+" last insert order_id = "+order_id);
        return order_id;
    }
}
