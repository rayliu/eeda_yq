define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {

        //------------save
        $('#saveBtn').click(function(e){        	
        	
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $(this).attr('disabled', true);

           
            var order = {
                id: $('#id').val(),
                code: $('#code').val(),
                name: $('#name').val()
            };
            //异步向后台提交数据
            $.post('/custom/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#code").val(order.CODE);
                	$("#name").val(order.NAME);
                	$("#id").val(order.ID);
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