package controllers.module.custom_search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.form.FormUtil;

public class CustomSearchService {
    public void handleCustomSearch(Map<String, ?> dto, Long form_id){
        Map<String, Object> customSearch = (Map<String, Object>) dto.get("customSearch");
        handle_search_source(form_id, customSearch);
        handleCols(form_id, customSearch);
        handleFilter(form_id, customSearch);
        handleFilterCondition(form_id, customSearch);
    }
    private void handleFilterCondition(Long form_id, Map<String, Object> customSearch) {
        String filter_condition = (String)customSearch.get("custom_filter_condition");
        if(StrKit.notBlank(filter_condition)) {
            Record re = Db.findFirst("select * from eeda_form_custom_search_filter_condition where form_id=?", form_id);
            if(re!=null) {
                re.set("condition", filter_condition);
                Db.update("eeda_form_custom_search_filter_condition", re);
            }else {
                re = new Record();
                re.set("condition", filter_condition);
                re.set("form_id", form_id);
                Db.save("eeda_form_custom_search_filter_condition", re);
            }
        }
    }
    private void handleFilter(Long form_id, Map<String, Object> customSearch) {
        List<Map<String, String>> custom_search_filter = (ArrayList<Map<String, String>>)customSearch.get("custom_search_filter");
        if(custom_search_filter.size()>0){
            for(int i = 0;i<custom_search_filter.size();i++){
                String id = FormUtil.getId(custom_search_filter.get(i).get("ID")).toString() ;
                String param_name = custom_search_filter.get(i).get("PARAM_NAME");
                String data_type = custom_search_filter.get(i).get("DATA_TYPE");
                String must_flag = custom_search_filter.get(i).get("MUST_FLAG");
                String default_value = custom_search_filter.get(i).get("DEFAULT_VALUE");
                String is_delete = custom_search_filter.get(i).get("IS_DELETE");
                Record re = new Record();
                if(StringUtils.isBlank(id)){
                    re.set("form_id", form_id);
                    re.set("param_name",param_name);
                    re.set("data_type",data_type);
                    re.set("must_flag",must_flag);
                    re.set("default_value",default_value);
                    Db.save("eeda_form_custom_search_filter", re);
                }else{
                    re = Db.findById("eeda_form_custom_search_filter", id);
                    if(re!=null){
                        if("Y".equals(is_delete)){
                            Db.delete("eeda_form_custom_search_filter", re);
                        }else{
                            re.set("param_name",param_name);
                            re.set("data_type",data_type);
                            re.set("must_flag",must_flag);
                            re.set("default_value",default_value);
                            Db.update("eeda_form_custom_search_filter", re);
                        }
                    }
                }
            }
        }
    }

    private void handleCols(Long form_id, Map<String, Object> customSearch) {
        List<Map<String, String>> custom_search_cols = (ArrayList<Map<String, String>>)customSearch.get("custom_search_cols");
        if(custom_search_cols.size()>0){
            for(int i = 0;i<custom_search_cols.size();i++){
                String id = FormUtil.getId(custom_search_cols.get(i).get("ID")).toString();
                String field_name = custom_search_cols.get(i).get("FIELD_NAME");
                String expression = custom_search_cols.get(i).get("EXPRESSION");
                String sort = custom_search_cols.get(i).get("SORT");
                String width = custom_search_cols.get(i).get("WIDTH");
                String hidden_flag = custom_search_cols.get(i).get("HIDDEN_FLAG");
                String is_delete = custom_search_cols.get(i).get("IS_DELETE");
                Record re = new Record();
                if(StringUtils.isBlank(id)){
                    re.set("form_id", form_id);
                    re.set("field_name",field_name);
                    re.set("expression",expression);
                    re.set("sort",sort);
                    re.set("width",width);
                    re.set("hidden_flag",hidden_flag);
                    Db.save("eeda_form_custom_search_cols", re);
                }else{
                    re = Db.findById("eeda_form_custom_search_cols", id);
                    if(re!=null){
                        if("Y".equals(is_delete)){
                            Db.delete("eeda_form_custom_search_cols", re);
                        }else{
                            re.set("field_name",field_name);
                            re.set("expression",expression);
                            re.set("sort",sort);
                            re.set("width",width);
                            re.set("hidden_flag",hidden_flag);
                            Db.update("eeda_form_custom_search_cols", re);
                        }
                    }
                }
            }
        }
    }

    private void handle_search_source(Long form_id, Map<String, Object> customSearch) {
        Map<String, Object> custom_search_source = (Map<String, Object>)customSearch.get("custom_search_source");
        if(custom_search_source!=null){
            List<Map<String, String>> block_arr = (ArrayList<Map<String, String>>)custom_search_source.get("block_arr");
            if(block_arr!=null && block_arr.size()>0){
                for(int i = 0;i<block_arr.size();i++){
                    String id = FormUtil.getId(block_arr.get(i).get("ID")).toString();
                    String form_name = block_arr.get(i).get("FORM_NAME");
                    String connect_type = block_arr.get(i).get("CONNECT_TYPE");
                    String is_delete = block_arr.get(i).get("IS_DELETE");
                    Record re = new Record();
                    if(StringUtils.isBlank(id)){
                        re.set("form_id", form_id);
                        re.set("form_name", form_name);
                        re.set("connect_type", connect_type);
                        Db.save("eeda_form_custom_search_source", re);
                    }else{
                        re = Db.findById("eeda_form_custom_search_source", id);
                        if(re!=null){
                            if("Y".equals(is_delete)){
                                Db.delete("eeda_form_custom_search_source", re);
                            }else{
                                re.set("form_name", form_name);
                                re.set("connect_type", connect_type);
                                Db.update("eeda_form_custom_search_source", re);
                            }
                        }
                    }
                }
            }
            List<Map<String, String>> join_list = (ArrayList<Map<String, String>>)custom_search_source.get("join_list");
            if(join_list.size()>0){
                for(int i =0;i<join_list.size();i++){
                    String id = FormUtil.getId(join_list.get(i).get("ID")).toString();
                    String form_left = join_list.get(i).get("FORM_LEFT");
                    String form_left_field = join_list.get(i).get("FORM_LEFT_FIELD");
                    String form_right = join_list.get(i).get("FORM_RIGHT");
                    String form_right_field = join_list.get(i).get("FORM_RIGHT_FIELD");
                    String operator = join_list.get(i).get("OPERATOR");
                    String is_delete = join_list.get(i).get("IS_DELETE");
                    Record re = new Record();
                    if(StringUtils.isBlank(id)){
                        re.set("form_id", form_id);
                        re.set("form_left", form_left);
                        re.set("form_left_field", form_left_field);
                        re.set("form_right", form_right);
                        re.set("form_right_field", form_right_field);
                        re.set("operator", operator);
                        Db.save("eeda_form_custom_search_source_condition", re);
                    }else{
                        re = Db.findById("eeda_form_custom_search_source_condition", id);
                        if(re!=null){
                            if("Y".equals(is_delete)){
                                Db.delete("eeda_form_custom_search_source_condition", re);
                            }else{
                                re.set("form_left", form_left);
                                re.set("form_left_field", form_left_field);
                                re.set("form_right", form_right);
                                re.set("form_right_field", form_right_field);
                                re.set("operator", operator);
                                Db.update("eeda_form_custom_search_source_condition", re);
                            }
                        }
                    }
                }
            }
        }
    }
}
