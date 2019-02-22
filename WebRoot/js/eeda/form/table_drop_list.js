define(['jquery', 'sco'], function ($) {


    $('table .eeda').on('keyup click', 'input[eeda_type=drop_down]', function(event) {
        var inputField = $(this);
        var inputField_name = inputField.attr('name');

        var hiddenField_name = inputField_name.substr(0, inputField_name.length-6);
        var hiddenField = $("[name="+hiddenField_name+"]");

        var drop_list = inputField.parent().find('ul');//$("[name="+inputField_name+"_list]");
        var span_drop = drop_list.closest('.dropDown');

        drop_list.attr({
            input_name: inputField_name,
            input_hidden_name: hiddenField_name
        });
        //处理中文输入法, 没完成前不触发查询
        var cpLock = false;
        inputField.on('compositionstart', function () {
            cpLock = true;
        });
        inputField.on('compositionend', function () {
            cpLock = false;
        });

        var inputStr = inputField.val();
        if(cpLock)
            return;
        
        if (event.keyCode == 40) {
            drop_list.find('li').first().focus();
            return false;
        }
        
        var target_form_id = inputField.attr('target_form');
        var target_field = inputField.attr('target_field_name');
        $.get('/form/'+target_form_id+'-doQuery?'+target_field+'_like='+inputStr, function(dto){
            if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                return;
            }
            drop_list.empty();

            var data = dto.data;

            for(var i = 0; i < data.length; i++){
                var html= "<li><a tabindex='-1' class='drop_list_item' target_field='"+data[i][target_field.toUpperCase()]
                        +"' id='"+data[i].ID+"' data='"+JSON.stringify(data[i])+"'>"+data[i][target_field.toUpperCase()]+"</a></li>";

                drop_list.append(html);
            }
            
            
            
            drop_list.css({ 
                position: "absolute",
                left: inputField.width()-28+"px", 
                top: inputField.height()+54+"px",
                width: inputField.width()+20+"px"
            });
            span_drop.addClass('open');
            //eeda.hidePopList();
            drop_list.css('display', 'block');
            
        },'json');
    });


    $('table .eeda').on('click', 'ul.dropDown-menu a', function(e){
        var item = $(this);
        var id = item.attr('id');
        var value = item.text();
        console.log(item);

        var ul = item.closest('ul');
        var inputField_name = ul.attr('input_name');
        var inputField = ul.parent().parent().find('input[name='+inputField_name+']');
        inputField.val(value);
        //处理引用字段
        var item_list_str = inputField.attr('item_list');
        if(item_list_str){
            var item_list = JSON.parse(item_list_str);
            var data_str = item.attr('data');
            var data= JSON.parse(data_str);
            $.each(item_list, function(index, item) {
                var origin_name = item.ORIGIN_FIELD_NAME;
                var target_name = item.TARGET_FIELD_NAME;
                $("input[name*='"+target_name+"']").val(data[origin_name.toUpperCase()]);
            });
        }
    });

    
});
