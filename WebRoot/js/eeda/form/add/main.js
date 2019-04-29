define(['jquery', 'hui', './detail_table', '../event/events', '../btns', '../value_change', '../drop_list_change', 
    '../table_drop_list', 'datetimepicker_CN', '../city_list', '../charts/form_chart'], 
    function ($, huiCont, tableCont, eventCont) {
    
    $(document).ready(function(){ 
        document.title = '新增 | ' + document.title;

        $(".HuiTab").Huitab({index:0});

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

        var form_define_json = JSON.parse($("#form_define").text());
        eventCont.handle('event_add_page_onload', form_define_json);//处理 新增页面打开后 载入时的处理事件

        //判断是否有需要后台执行的defaut-event:   新增-打开表单
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
});
