define(['jquery', 'layer'], function ($) {
    $('.copy_btn').click(function(){
        
        var from_module_id = $(this).attr('module_id');
        var to_module_id = $('#module_id').val();
        $.post('/module/copyModule', {from_module_id:from_module_id, to_module_id:to_module_id}, function(data){
            if(data=='OK'){
                parent.layer.closeAll();
                $('#saveBtn').trigger('click');
            }else{
                layer.msg("出错了");
                parent.layer.closeAll();
            }
        });
        
    });
});