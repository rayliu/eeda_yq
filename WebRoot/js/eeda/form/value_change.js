define(['jquery', 'sco'], function ($) {
    $('input[type=radio]').change(function(event) {
        var module_id = $('#module_id').val();
        var origin_name =$(this).attr('origin_name');
        var field_name =$(this).attr('name');
        var value = $(this).val();
        console.log('radio '+field_name+':'+value);
        $.post('/form/'+module_id+'-valueChange', {field_name: field_name, value: value}, function(data){
            console.log(data);
            for (var i = 0; i < data.length; i++) {
                var event = data[i];
                if(event.MENU_TYPE=='value_change' && event.TYPE == 'set_css'){
                    var set_css = event.SET_CSS;
                    var target_field = set_css.TARGET_FIELD.replace(/\./g, "-");
                    var condition = set_css.CONDITION;
                    console.log('value_change condition:'+condition);

                    var chinese_field_list = [];
                    var chinese_field = "";
                    var is_coming = false;
                    for(var k=0; k<condition.length; k++){ 
                        console.log(condition.charAt(k));
                        var char = condition.charAt(k);
                        //escape对字符串进行编码，字符值大于 255 的以 %u**** 格式存储，而字符值大于 255 的恰好是非英文字符（一般是中文字符，非中文字符也可以当作中文字符考虑
                        if (escape(char).indexOf("%u") >= 0 || char == '.') { //字符串 str 中含有汉字 
                            chinese_field += char;
                            is_coming = true;
                        }else{
                            is_coming = false;
                            if(chinese_field.length>0){
                                chinese_field_list.push(chinese_field);
                                chinese_field ="";
                            }
                        }
                    }
                    $(chinese_field_list).each(function(i, item) { 
                        var item_new = item.replace(/\./g, "-");
                        var target_str = "$('[origin_name="+item_new+"]:checked').val()";
                        condition = condition.replace(item, target_str);
                    });
                    console.log('new condition :'+condition +", "+eval(condition));
                    if(origin_name == target_field && eval(condition)){
                        var set_list = set_css.SET_FIELD_LIST;
                        for (var j = 0; j < set_list.length; j++) {
                            var item = set_list[j];
                            var name = item.NAME.replace(/\./g, "-");
                            if(item.VALUE == '隐藏'){
                                $('#'+name+'_div').hide();
                            }
                            if(item.VALUE == '显示'){
                                $('#'+name+'_div').show();
                            }
                        }
                    }
                }
            }
        });
    });

    
});
