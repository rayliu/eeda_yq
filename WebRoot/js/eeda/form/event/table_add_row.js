define(['jquery'], function ($) {
    console.log('enter table_add_row.js');

    var handle = function(action){
        var event_action_setting=action.event_action_setting;
        var target_table_field_id = event_action_setting.target_table_field_id;
        var dataTable=window['dataTable_'+target_table_field_id];
        var dataTable_cols= window['dataTable_'+target_table_field_id+'_cols'];
        var item_list = event_action_setting.item_list;

        var pass_var;
        var pass_var_define;
        for (let index = 0; index < item_list.length; index++) {
            var item = item_list[index];
            var expression=item.expression;
            if(expression.length>0){
                var regex3 = /\{(.+?)\}/g;  // {} 花括号，大括号
                var variable = expression.match(regex3)[0];
                var variableName=variable.split('.')[0].substr(1);
                pass_var = JSON.parse(sessionStorage.getItem(variableName));
                pass_var_define = JSON.parse(sessionStorage.getItem(variableName+'_define'));
            }
        }

        for (let i = 0; i < pass_var.length; i++) {
            var pass_var_item = pass_var[i];
            var target_item = {};
            //从event定义循环找出赋值字段
            for (let index = 0; index < item_list.length; index++) {
                var item = item_list[index];
                var expression=item.expression;
                if(expression.length>0){
                    var regex3 = /\{(.+?)\}/g;  // {} 花括号，大括号
                    var variable = expression.match(regex3)[0];
                    var variableName=variable.split('.')[0].substr(1);
                    var from_variableName=variable.split('.')[1].replace("}", "");
                    //循环从 表定义转换中文字段
                    var from_field_name= getFromField(from_variableName, pass_var_define);
                    if(!from_field_name) continue;
                    //循环从 目标表列定义 转换中文字段
                    var to_field_name= getToField(item.field_name, dataTable_cols);
                    target_item[to_field_name]=pass_var_item[from_field_name.toUpperCase()]
                }
            }
            dataTable.row.add(target_item).draw(true);
        }
    }

    var getFromField= function(field_name, pass_var_define){
        for (let j = 0; j < pass_var_define.length; j++) {
            var field = pass_var_define[j];
            if(field_name==field.FIELD_DISPLAY_NAME){
                var from_field='F'+field.ID+'_'+field.FIELD_NAME;
                return from_field;
            }
        }
    }
    var getToField= function(field_name, pass_var_define){
        for (let j = 0; j < pass_var_define.length; j++) {
            var field = pass_var_define[j];
            if(field.data.indexOf(field_name.toUpperCase())>0){
                return field.data;
            }
        }
    }
    
	return {
        handle:handle
    }
});
