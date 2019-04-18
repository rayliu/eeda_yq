define(['jquery'], function ($) {
    console.log('enter event_check_condition.js')
    function check_condition(node){
        var action_type = node.action_type
        var condition_json = node.condition_json;
        var condition_obj_arr = {};
        
        if(condition_json){
            condition_obj_arr = JSON.parse(condition_json);
        }else{
            return true;
        }
        var form_define_json = JSON.parse($("#form_define").text()); 
        var order_id = $('#order_id').val();
        var form_id = form_define_json.ID;
        var fields = form_define_json.FIELDS;

        var condition_pass = false;
        console.log(condition_obj_arr);
        var formular_conditon_match_type=node.formular_conditon_match_type;
        
        var bool_arr = [];
        for (let index = 0; index < condition_obj_arr.length; index++) {
            const element = condition_obj_arr[index];
            var condition_field1 = element.condition_field1;
            var condition_operator = element.condition_operator;
            var condition_field2 = element.condition_field2;

            var condition_field1_value=getFieldValue(condition_field1, form_id, fields);
            var condition_field2_value=getFieldValue(condition_field2, form_id, fields);
            
            var expression = condition_field1_value+condition_operator+condition_field2_value;
            console.log('expression: '+expression);
            bool_arr.push(eval(expression));
        }
        if(formular_conditon_match_type=='and'){
            condition_pass=isArrAllTrue(bool_arr);
        }else{
            condition_pass=isArrContainsTrue(bool_arr);
        }
        
        return condition_pass;
    }

    function isArrAllTrue(bool_arr){
        var true_arr = [], false_arr = [];
        bool_arr.forEach(element => {
            if(element){
                true_arr.push(element);
            }else{
                false_arr.push(element);
            }
        });
        if(true_arr.length == bool_arr.length){
            return true;
        }
        return false;
    }

    function isArrContainsTrue(arr){
        var true_arr = [], false_arr = [];
        arr.forEach(element => {
            if(element){
                true_arr.push(element);
            }else{
                false_arr.push(element);
            }
        });
        if(true_arr.length >0){
            return true;
        }
        return false;
    }

    function getFieldValue(condition_field, form_id, fields){
        var condition_field_value = condition_field;
        var regex = /\{(.+?)\}/g;  // {} 花括号，大括号
        var field_array = condition_field.match(regex);
        console.log(field_array);

        if(!field_array ){
            condition_field_value="'"+condition_field_value+"'"
            return condition_field_value;
        } 
        field_array.forEach(el => {
            var field_display_name = el.split('.')[1].replace('}','');
            var field_id="";
            var field_name="";
            fields.forEach(f => {
                if(f.FIELD_DISPLAY_NAME==field_display_name){
                    field_id=f.ID;
                    field_name=f.FIELD_NAME;
                    return;
                }
            });
            var element_name = "[name=form_"+form_id+"-f"+field_id+"_"+field_name+"]";
            var element_value="'"+$(element_name).val()+"'";
            console.log(element_value);
            condition_field_value=condition_field.replace(el, element_value);
        });
        return condition_field_value;
    }

	return {
        check_condition:check_condition
    }
});
