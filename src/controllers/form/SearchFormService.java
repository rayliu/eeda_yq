package controllers.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.util.PingYinUtil;

/**
 * 构造 查询表的SQL
 * 表单来源block：  采购入库单, 退货入库单, 
 *  
 * 关联字段：[采购入库单.仓库, 采购入库单.从表.货品名称]， [退货入库单.仓库, 退货入库单.从表.货品名称]
 * 
 * 显示字段（以第一张表为准）:  采购入库单.仓库   采购入库单.从表.货品名称
 * 统计字段:  库存= 采购入库单.从表.货品数量 + 退货入库单.从表.货品数量
 *           货损库存= 采购入库单.从表.货损数量 + 退货入库单.从表.货损数量
 * 构造 内部表 的统计列查询：
 *     第一个表 cgrkdcb.f3_sl, 0 f6_sl
 *     第二个表 thrkdcb.f6_sl, 0 f3_sl
 *     
 * 构造 内部表 的查询：
 * basicSql = select cgrkd.f1_ck ck, cgrkdcb.f2_hpmc hpmc, cgrkdcb.f3_sl, 0 f6_sl from form_1 cgrkd, form_2 cgrkdcb where cgrkd.no=cgrkdcb.no
 *            union all
 *            select thrkd.f4_ck ck, thrkdcb.f5_hpmc hpmc, 0 f3_sl, thrkdcb.f6_sl from form_3 thrkd, form_4 thrkdcb where thrkd.no=thrkdcb.no
 *            
 * 处理后basicSql的结果列:  ck, hpmc, f3_sl, f6_sl 
 * 
 * 
 * 最终外部普通列 outerShowCols = ck, hpmc
 * 最终外部统计列 outerSumCols： (sum(f3_sl)+sum(f6_sl)) sl
 * 最终外部的列   outerCols = ck, hpmc, (sum(f3_sl)+sum(f6_sl)) sl
 * 
 * 最终外部的group:  group by ck, hpmc
 * 最终外部的sql:  select ck, hpmc, (sum(f3_sl)+sum(f6_sl)) sl from(basicSql) group by ck, hpmc
 * 
 * @author a13570610691
 *
 */

public class SearchFormService {
    private Controller cont = null;
    public SearchFormService(Controller cont){
        this.cont = cont;
    } 
    
    public static void main(String[] args) {
        String expression = "采购入库单.采购入库单明细.数量 + B.采购入库单明细.数量 ";
//                + "- C.采购入库单明细.数量+ D.采购入库单明细.数量-E.采购入库单明细.数量";
        SearchFormService s = new SearchFormService(null);
        String result = s.getExpressionStr(expression, 1l);
        LogKit.info(result);
    }
    
    public List<Record> getDisplayCols(Long form_id, Long office_id){
        List<Record> fieldList = new ArrayList<Record>();
        //查数据列表
        List<Record> custom_search_cols_list = Db.find("select * from eeda_form_custom_search_cols where form_id = ?",form_id);
        for(int j = 0;j<custom_search_cols_list.size();j++){
            String fieldName = custom_search_cols_list.get(j).get("field_name");
            
            Record field = FormUtil.getFormOrField(fieldName, office_id);
            field.set("listed","Y");
            field.set("custom_search","Y");
            fieldList.add(field);
        }
        
        List<Record> custom_search_sum_cols_list = Db.find("select * from eeda_form_custom_search_sum_col where form_id = ?",form_id);
        for(int j = 0;j<custom_search_sum_cols_list.size();j++){
            String fieldDisplayName = custom_search_sum_cols_list.get(j).get("field_display_name");
            String expression = custom_search_sum_cols_list.get(j).get("expression");
            //从公式中获取第一个block的字段名
            Record source = Db.findFirst("select * from eeda_form_custom_search_source where form_id = ? order by seq",form_id);
            String firstFormName = source.getStr("form_name");
            
            String real_name = "";
            String replaceStr =expression.replace("+", "@").replace("-", "@");
            String[] fieldArr=replaceStr.split("@");
            List<String> exFieldList = Arrays.asList(fieldArr);
            for (String fieldName : exFieldList) {
                if(fieldName.contains(firstFormName)) {
                    Record field = FormUtil.getFormOrField(fieldName, office_id);
                    real_name = field.getStr("real_name");
                    break;
                }
            }
            Record field = new Record();
            field.set("field_name", real_name);
            field.set("field_display_name", fieldDisplayName);
            field.set("listed","Y");
            field.set("custom_search","Y");
            fieldList.add(field);
        }
        return fieldList;
    }

    public String buildQuerySql(Long form_id, long office_id) {
        String strBasicSql = buildBasicSql(form_id, office_id);
        String strOuterShowCols = buildOuterShowCols(form_id, office_id);
        String strOuterSumCols = buildOuterSumCols(form_id, office_id);
        String strGroupSumCols = " group by "+strOuterShowCols;
        if(StrKit.isBlank(strOuterSumCols)) {
        	return "select "+strOuterShowCols+" from ("+strBasicSql+") A "+strGroupSumCols;
        }
        return "select "+strOuterShowCols+", "+strOuterSumCols+" from ("+strBasicSql+") A "+strGroupSumCols;
    }
    
    /**
     * 构造最外部非聚合的列名: 取第一个表的关联字段 的缩写
     * @param form_id
     * @param office_id
     * @return
     */
    private String buildOuterShowCols(Long form_id, long office_id) {
        List<String> strList = new LinkedList<String>();
        Record source = Db.findFirst("select * from eeda_form_custom_search_source where form_id = ? order by seq",form_id);
        String formName = source.getStr("form_name");
        return getColsName(formName, form_id, office_id, false);
    }
    
    private String buildOuterSumCols(Long form_id, long office_id) {
        List<String> strList = new LinkedList<String>();
        List<Record> list = Db.find("select * from eeda_form_custom_search_sum_col where form_id = ? order by seq",form_id);
        for (Record rec : list) {
            String fieldColName = rec.getStr("field_display_name");
            String aliasName = PingYinUtil.getFirstSpell(fieldColName);
            
            String expression = rec.getStr("expression");//需要聚合的字段, 取出来的时候是中文, 要转换成拼音
            String expressionStr = getExpressionStr(expression, office_id);
            strList.add(expressionStr);
        }
        return String.join(", ", strList);
    }
    
    private String buildBasicSql(Long form_id, long office_id) {
        String strBasicSql = "";
        
        List<String> sourceSqlList = new LinkedList<String>();
        List<Record> sourcelist = getSource(form_id);
        for (Record sourceRec : sourcelist) {
            String formName = sourceRec.getStr("form_name");
            String colsName = getColsName(formName, form_id, office_id, true);
            String sumColsName = getInnerSumColsName(formName, form_id, office_id);
            String fromStr =  buildFromStr(formName, form_id, office_id);
            String sql = "select "+colsName+", "+sumColsName+" from "+fromStr;
            if(StrKit.isBlank(sumColsName)) {
            	sql = "select "+colsName+" from "+fromStr;
            }
            sourceSqlList.add(sql);
        }
        strBasicSql=String.join(" union all ", sourceSqlList);
        
        return strBasicSql;
    }
    
    private List<Record> getSource(Long form_id){
        List<Record> list = Db.find("select * from eeda_form_custom_search_source where form_id = ?",form_id);
        return list;
    }
    
    /**
     * 1.  内部sql: 获取非聚合字段列名, 需要加上表名的缩写, 有可能是从表的字段
     * 2.  外部给group 用: 获取非聚合字段列名, 不需要加上表名的缩写
     * @param form_id
     * @return
     */
    private String getColsName(String master_form_name, Long form_id, Long office_id, boolean needPrefix){
        List<String> strList = new LinkedList<String>();
        List<Record> list = Db.find("select * from eeda_form_custom_search_source_col where form_id = ? and form_name=? order by seq",form_id, master_form_name);
        for (Record rec : list) {
            String field_display_name = master_form_name+"."+rec.getStr("field_name");
            Record fieldRec = FormUtil.getFormOrField(field_display_name, office_id);
            if(needPrefix) {
            	String form_name=master_form_name;
                if(field_display_name.split("\\.").length==3) {//从表的字段
                    form_name = field_display_name.split("\\.")[1];
                }
                String py = PingYinUtil.getFirstSpell(form_name);
                String field_name = py+"."+fieldRec.getStr("real_name")+" "+fieldRec.getStr("field_name"); //例如:  rkd.f12_ck ck
                strList.add(field_name);
            }else {
                String field_name = fieldRec.getStr("field_name"); //例如:  ck
                strList.add(field_name);
            }
        }
        
        return String.join(", ", strList);
    }
    
    /**
     * 获取 内部 聚合字段列名
     *  构造 内部表 的统计列查询：
     *     第一个表 cgrkdcb.f3_sl, 0 f6_sl
     *     第二个表 thrkdcb.f6_sl, 0 f3_sl
     * @param form_id
     * @return
     */
    private String getInnerSumColsName(String current_form_name, Long form_id, long office_id){
        List<String> strList = new LinkedList<String>();
        List<Record> list = Db.find("select * from eeda_form_custom_search_sum_col where form_id = ? order by seq",form_id);
        for (Record rec : list) {
            //String field_display_name = rec.getStr("field_display_name");//需要显示的字段, 取出来的时候是中文, 要转换成拼音
            String expression = rec.getStr("expression");//需要聚合的字段, 取出来的时候是中文, 要转换成拼音
            String replaceStr =expression.replace("+", "@").replace("-", "@");
            String[] fieldArr=replaceStr.split("@");
            List<String> fieldList = Arrays.asList(fieldArr);
            for(String fieldName : fieldList) {
                Record fieldRec = FormUtil.getFormOrField(fieldName, office_id);
                String formName = fieldName.split("\\.")[0];
                String py = "";
                if(current_form_name.equals(formName)) {
                    py = PingYinUtil.getFirstSpell(formName)+".";
                    if(fieldName.split("\\.").length==3) {
                        py = PingYinUtil.getFirstSpell(fieldName.split("\\.")[1])+".";
                    }
                }else {
                    py = "0 ";
                }
                String field_name = py+fieldRec.getStr("real_name"); //例如:  rkd.f12_ck
                strList.add(field_name);
            }
        }
        
        return String.join(", ", strList);
    }
    
    /**
     * 构造from 后的语句
     * @param form_id
     * @return
     */
    private String buildFromStr(String form_name, Long form_id, Long office_id){
        //1. 显示字段中是否包含从表
        boolean isIncludeDetail = false;
        String strMasterFormName = form_name, strDetailFormName = "", strDetailRefField = "";
        List<Record> list = Db.find("select * from eeda_form_custom_search_source_col where form_id = ? and form_name=? order by seq",form_id, form_name);
        for (Record rec : list) {
            String fieldName = rec.getStr("field_name");
            if(fieldName.split("\\.").length == 2) {
                isIncludeDetail=true;
                strDetailRefField = fieldName;//显示字段
                strDetailFormName = fieldName.split("\\.")[0];
                break;
            }
        }
        String biaoNameStr = "";
        //2. 根据主表的从表引用找到关联字段，构造 from A，B where A.no=B.no
        if(isIncludeDetail) {
            //显示字段
            Record refFieldRec = FormUtil.getFormOrField(strDetailRefField, office_id);
            //找主表
            Record masterRec = FormUtil.getFormOrField(strMasterFormName, office_id);
            //找从表
            Record detailRec = FormUtil.getFormOrField(strDetailFormName, office_id);
            
            String pyMaster = PingYinUtil.getFirstSpell(masterRec.getStr("name"));
            String pyDetail = PingYinUtil.getFirstSpell(detailRec.getStr("name"));
            biaoNameStr = "form_"+masterRec.get("id")+" "+pyMaster+
                    ", "+"form_"+detailRec.get("id")+" "+pyDetail;//TODO: 如果有多个从表, 需优化
            
            String whereStr = "";
            //找从表引用字段
            Record refField = Db.findFirst("select * from eeda_form_field where form_id=? and field_type='从表引用' and field_display_name=?",
                    masterRec.getLong("id"), strDetailFormName);
            //找从表关联字段， 构造 where 
            List<Record> joinList = Db.find("select * from eeda_form_field_type_detail_ref_join_condition where field_id=? ", 
                    refField.getLong("id"));
            for(Record joinField : joinList) {
                Record fromField = FormUtil.getFormOrField(joinField.getStr("field_from"), office_id);
                Record toField = FormUtil.getFormOrField(joinField.getStr("field_to"), office_id);
                whereStr += " and "+pyMaster+".f"+fromField.getLong("id")+"_"+fromField.getStr("field_name")+
                        "="+pyDetail+".f"+toField.getLong("id")+"_"+toField.getStr("field_name");
            }
            biaoNameStr += " where 1=1 "+whereStr;
        }
        return biaoNameStr;
    }
    /**
     * 构造sum 语句
     * @param expresstion
     * @param office_id
     * @return （sum(A)+sum(B)-sum(C)） A
     */
    public String getExpressionStr(String expression,  Long office_id){
        expression= expression.replace(" ", "");
        String replaceStr =expression.replace("+", "@").replace("-", "@");
        String[] fieldArr=replaceStr.split("@");
        List<String> list = Arrays.asList(fieldArr);
        
        List<String> addList = new LinkedList<String>();
        List<String> minusList = new LinkedList<String>();
        for (String fieldName : list) {
            String realDbName = getRealDbName(fieldName, office_id);
            String operator = getOperator(fieldName, expression);
            switch (operator) {
            case "+":
                addList.add(realDbName);
                LogKit.info("+"+realDbName);
                break;
            case "-":
                minusList.add(fieldName);
                LogKit.info("-"+realDbName);
                break;
            }
        }
        String resultStr = buildSumStr(addList, minusList);
        return resultStr;
    }
    
    private String buildSumStr(List<String> addList,  List<String> minusList) {
        String addStr = "", minusStr="";
        addStr = "sum("+String.join(")+sum(", addList)+")";
        minusStr = "sum("+String.join(")-sum(", minusList)+")";
        String str = "";
        if(minusList.size()>0) {
            str = "("+addStr+"-"+minusStr+") "+addList.get(0); 
        }else {
            str = "("+addStr+") "+addList.get(0); 
        }
        LogKit.info(str);
        return str;
    }
    
    private String getOperator(String fieldName, String expression) {
        String operator = "+";
        int index = expression.indexOf(fieldName);
        if(index==0) {
            
        }else {
            operator = expression.substring(index-1, index);
        }
        return operator;
    }
    
    private String getRealDbName(String fieldName, Long office_id) {
        Record rec = FormUtil.getFormOrField(fieldName, office_id);
        return rec.getStr("real_name");
    }
}
