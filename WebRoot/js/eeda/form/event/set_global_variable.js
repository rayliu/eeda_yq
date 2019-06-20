define(['jquery'], function ($) {
    console.log('enter set_global_variable.js');

    var handle = function(action){
        var event_action_setting=action.event_action_setting;
        var checked_var=event_action_setting.checked_var;
        //把列表的列表结构也要传过去，否则下页不知道如何取字段名
        var variable_define = sessionStorage.getItem(checked_var+'_define');
        if(!variable_define){
            var checked_var_define=$('#field_list_json').val();
            sessionStorage.setItem(checked_var+'_define', checked_var_define);
        }

        var checked_var_option=event_action_setting.checked_var_option;
        //往sessionStorage写变量
        if(checked_var_option=='值'){
            var variable = sessionStorage.getItem(checked_var);
            //现在只是清空
            sessionStorage.removeItem(checked_var);
            sessionStorage.removeItem(checked_var+'_define');
        }else{//选中项的值
            var variable = sessionStorage.getItem(checked_var);
            if(!variable){
                variable = [];
            }else{
                variable=JSON.parse(variable);
            }
                
            var data = dataTable.row(tr).data();
            console.log(data);
            variable.push(data);
            sessionStorage.setItem(checked_var, JSON.stringify(variable));
        }
        
    };

    return {
        handle: handle
    }
});