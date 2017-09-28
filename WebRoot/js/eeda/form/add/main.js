define(['jquery', './detail_table', '../btns', '../value_change', '../drop_list_change', 'datetimepicker_CN', '../city_list'], function ($, tableCont) {
    document.title = '新增 | ' + document.title;

    var date_inputs = $('input[data_type=date]');
    $.each(date_inputs, function(i, item){
        var id= $(item).attr('id');
        $('#'+id+'_div').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();   
            $(item).trigger('keyup');
        });
    });

    //判断是否有defaut-event:   新增-打开表单
    var module_id = $('#module_id').val();
    $.post('/form/'+module_id+'-eventConfig', function(data, textStatus, xhr) {
        console.log("eventConfig.....");
        console.log(data);
        $.each(data, function(index, item) {
            var condition = item.SET_VALUE.CONDITION;

            var item_list = item.SET_VALUE_ITEM;
            $.each(item_list, function(index, field) {
                 var field_name = field.FIELD_NAME;
                 var value = field.VALUE;
                 $('[name='+ field_name+']').val(value);
            });
        });
    });
});
