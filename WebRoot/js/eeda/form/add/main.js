define(['jquery', '../btns', 'datetimepicker_CN'], function ($) {
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
});
