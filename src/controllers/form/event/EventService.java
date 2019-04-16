package controllers.form.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import bsh.EvalError;
import bsh.Interpreter;
import controllers.form.FormUtil;

public class EventService {

    public EventService() {}

    public boolean handleSetValue(Map settingMap, long form_id, long order_id) throws Exception {
        boolean result = false;
        // 订单数据
        Record order = Db.findFirst("select * from form_" + form_id + " where id=?", order_id);

        Record currentFormRec = Db.findFirst("select * from eeda_form_define where id=?", form_id);
        String currentFormName = currentFormRec.getStr("name");// 本表单中文名
        Long office_id = currentFormRec.getLong("office_id");
        String sourceFormName = (String)settingMap.get("source_table_name");// 数据源-表名
        Record dbSource = FormUtil.getFormOrField(sourceFormName, office_id);
        Record detailForm = new Record();
        if ("从表引用".equals(dbSource.getStr("field_type"))) {
            Record detailRef = Db.findFirst(
                    "select id,target_form_name from eeda_form_field_type_detail_ref where field_id = ?",
                    dbSource.getLong("id"));
            detailForm = Db.findFirst("select id,name from eeda_form_define where name = ?",
                    detailRef.getStr("target_form_name"));
        } else {
            detailForm = dbSource;
        }

        String detailFormName = "form_" + detailForm.getLong("id");// 数据源-表名
        
        String targetFormChineseName = (String)settingMap.get("target_table_name");//目标表-表名
        Record targetForm = FormUtil.getFormOrField(targetFormChineseName, office_id);
        String targetFormName = "form_" + targetForm.getLong("id");// 目标表-表名
        // 条件替换成字段 {库存表.货品代码}={入库单.明细表.货品代码}-> f386_hpdm=f69_hpdm
        String conditionStr = (String)settingMap.get("condition");
        String condition = replaceStr(conditionStr, office_id);
        String set_value_action_type = (String)settingMap.get("form_set_value_action_type");
        // 循环赋值操作list（可能存在赋多个值）
        String form_set_value_edit_field_data = (String)settingMap.get("form_set_value_edit_field_data"); 
        Gson gson = new Gson();
        List<Map<String,Object>> setValueJsonList = gson.fromJson(form_set_value_edit_field_data, 
                new TypeToken<List<Map<String,Object>>>() { }.getType());
        List<Record> setValueList = new LinkedList<Record>();
        int row_num = setValueJsonList.size()/2;//每两个值为一行，field_name, expression
        for (int i = 0; i < row_num; i++) {
            Map field1 = setValueJsonList.get(i*2);
            Map field2 = setValueJsonList.get(i*2+1);
            Record rowRec = new Record();
            if("field_name".equals(field1.get("name").toString())){
                rowRec.set("field_name", field1.get("value"));
                rowRec.set("expression", field2.get("value"));
            }else{
                rowRec.set("field_name", field2.get("value"));
                rowRec.set("expression", field1.get("value"));
            }
            setValueList.add(rowRec);
        }
        
        // 赋值操作List
        if ("set_value".equals(set_value_action_type)) {
            // 数据源主表跟从表关联条件
            Record refJoinCondition = Db.findFirst(
                    "select * from eeda_form_field_type_detail_ref_join_condition where field_id = ?",
                    dbSource.getLong("id"));
            // Record field_from =
            // getFormOrField(refJoinCondition.getStr("field_from"));//主表关联条件列
            // Record field_to =
            // getFormOrField(refJoinCondition.getStr("field_to"));//从表关联条件列

            String sql = "select * from " + targetFormName + " where 1=1";
            if (StrKit.notBlank(condition)) {
                sql += " and " + condition;
            }
            Record re = null;// 目标表 order
            if (currentFormName.equals(targetFormChineseName)) {// 如果是本表单，则加上ID
                sql += " and id=" + order_id;
                re = Db.findFirst(sql);
            } else {
                re = Db.findFirst(sql);
            }
            if (re == null)
                return false;
            
            List<Record> recList = new LinkedList<Record>();
            recList.add(re);
            recList.add(order);
            // 循环赋值操作list（可能存在赋多个值）
            for (Record item : setValueList) {
                Record targetField = FormUtil.getFormOrField(item.getStr("name"), office_id);// 获取需要赋值操作的列
                // 变成实际的值操作
                String targetFieldColumnName = "f" + targetField.getLong("id") + "_" + targetField.getStr("field_name");
                String evalString = "evalResult=" + replaceStrWithValue(item.getStr("value"), recList, office_id);
                // 调用beanshell进行计算
                String evalResult = "";
                Interpreter interpreter = new Interpreter();
                try {
                    // 设置输入参数：
                    System.out.println("evalString：" + evalString);
                    interpreter.eval(evalString);
                    System.out.println("evalResult=" + interpreter.get("evalResult"));
                    evalResult = interpreter.get("evalResult").toString();
                } catch (EvalError evalError) {// 报错说明不是表达式
                    evalError.printStackTrace();
                    evalResult = (String)item.get("value");
                }
                if (re != null)
                    re.set(targetFieldColumnName, evalResult);
            }
            if (re != null)
                Db.update(targetFormName, re);
        } else if ("loops_set_value".equals(set_value_action_type)) {
            // 数据源主表跟从表关联条件
            Record refJoinCondition = Db.findFirst(
                    "select * from eeda_form_field_type_detail_ref_join_condition where field_id = ?",
                    dbSource.getLong("id"));
            Record field_from = FormUtil.getFormOrField(refJoinCondition.getStr("field_from"), office_id);// 主表关联条件列
            Record field_to = FormUtil.getFormOrField(refJoinCondition.getStr("field_to"), office_id);// 从表关联条件列

            // 数据源表的集合
            List<Record> sourceList = Db.find("select * from " + detailFormName + " where "
                    + field_to.getStr("real_name") + "='" + order.get(field_from.getStr("real_name")) + "'");
            // 源表循环集合执行赋值操作
            for (Record record : sourceList) {
                // 目标表字段不变，循环的源表记录需替换为值，例如：select * from stock where f386_hpdm=f69_hpdm ->
                // f386_hpdm='123'
                String replaceFieldName = getReplaceFieldName(sourceFormName, conditionStr, office_id);//来源表字段，从表.货品代码 f69_hpdm
                String conditionAfterChange = condition.replace(replaceFieldName, "'" + record.get(replaceFieldName) + "'");
                Record re = Db.findFirst("select * from " + targetFormName + " where " + conditionAfterChange);// 目标表
                if (re == null) {
                    //如果目标表没记录，就新增一条
                    //此时要将关联的字段赋值，如 {库存表.货品代码} = {入库单.从表.货品代码}
                    // 货品代码需赋值到新数据行
                    re = new Record();
                    Db.save(targetFormName, re);
                }

                // 循环赋值操作list（可能存在赋多个值）
                for (Record item : setValueList) {
                    Record targetField = FormUtil.getFormOrField(item.getStr("field_name"), office_id);// 获取需要赋值操作的列
                    // 变成实际的值操作
                    String targetFieldColumnName = "f" + targetField.getLong("id") + "_"
                            + targetField.getStr("field_name");
                    String expression = item.getStr("expression");
                    
                    //多个取值的源记录
                    List<Record> recList = new LinkedList<Record>();
                    recList.add(re);
                    recList.add(record);
                    
                    String evalResult = replaceStrWithValue(expression, recList, office_id);
                    System.out.println("evalResult：" + evalResult);
                    if(expression.indexOf("+")>0 || expression.indexOf("-")>0||expression.indexOf("*")>0||expression.indexOf("/")>0) {
                        String evalString = "evalResult=" + evalResult;
                        // 调用beanshell进行计算
                        Interpreter interpreter = new Interpreter();
                        try {
                            System.out.println("evalString：" + evalString);
                            interpreter.eval(evalString);
                            System.out.println("evalResult=" + interpreter.get("evalResult"));
                            evalResult = interpreter.get("evalResult").toString();
                        } catch (EvalError evalError) {
                            evalError.printStackTrace();
                        }
                    }
                    re.set(targetFieldColumnName, evalResult);
                }
                if (re != null) {// 如果目标表没记录，就新增一条
                    Db.update(targetFormName, re);
                } else {
                    Db.save(targetFormName, re);
                }
            }
        }
        return true;
    }

    // 举例：sourceName=入库单.明细表， origCondition=‘{库存表.货品代码}={入库单.明细表.货品代码}’
    // 结果：返回 f69_hpdm
    // 即 f69_hpdm 需要被替换成真正的值
    private String getReplaceFieldName(String sourceName, String origCondition, Long office_id) {
        String fieldColumnName = "";
        Pattern pattern = Pattern.compile("(?<=\\{)[^\\}]+");// 匹配花括号
        Matcher matcher = pattern.matcher(origCondition);
        while (matcher.find()) {
            String fieldName = matcher.group(0);
            if (fieldName.indexOf(sourceName) >= 0) {
                Record field = FormUtil.getFormOrField(fieldName, office_id);
                fieldColumnName = "f" + field.getLong("id") + "_" + field.getStr("field_name");
                break;
            }
        }
        return fieldColumnName;
    }

    // 表达式里不止两个表参数如何处理？用list
    //private String replaceStrWithValue(String str, Record target, Record orig, Long office_id) throws Exception {
    private String replaceStrWithValue(String expressionStr, List<Record> fieldRecList, Long office_id) throws Exception {
        Pattern pattern = Pattern.compile("(?<=\\{)[^\\}]+");// 匹配花括号
        Matcher matcher = pattern.matcher(expressionStr);
        while (matcher.find()) {
            System.out.println(matcher.group(0));//匹配的变量名，如：入库单.数量
            String fieldStr = matcher.group(0);
            Record field = FormUtil.getFormOrField(fieldStr, office_id);
            if (field == null) {
                System.out.println("该字段不存在：" + matcher.group(0));
                throw new Exception("该字段不存在：" + matcher.group(0));
            }
            String fieldColumnName = "f" + field.getLong("id") + "_" + field.getStr("field_name");
            for(Record rec:fieldRecList) {
                if (rec != null && rec.getColumns().get(fieldColumnName) != null) {
                    expressionStr = expressionStr.replace("{" + fieldStr + "}", rec.getStr(fieldColumnName.toUpperCase()));
                }
            }
        }
        return expressionStr;
    }

    private String replaceStr(String str, Long office_id) {
        Pattern pattern = Pattern.compile("(?<=\\{)[^\\}]+");// 匹配花括号
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group(0));
            String newStr = matcher.group(0);
            Record field = FormUtil.getFormOrField(newStr, office_id);
            str = str.replace("{" + newStr + "}", field.getStr("real_name"));
        }
        return str;
    }
}
