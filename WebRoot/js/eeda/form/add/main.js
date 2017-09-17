define(['jquery', './detail_table', '../btns', '../value_change','datetimepicker_CN'], function ($, tableCont) {
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
