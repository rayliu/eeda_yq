define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	 $('#orderForm').validate({
             rules: {
            	 name: {
            		 required: true,
            		 maxlength:20
            	 }
             },
             messages:{
                     
             }
         });
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            var formRequired = 0;
            $('#orderForm').each(function(){
            	if(!$(this).valid()){
            		formRequired++;
                }
            });
            if(formRequired>0){
            	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
                return;
            }
            
            $(this).attr('disabled', true);

           
            var order = {
                id: $('#id').val(),
                name: $('#name').val(),
            };
            //异步向后台提交数据
            $.post('/containerType/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#name").val(order.NAME);
                    
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  

     });
});