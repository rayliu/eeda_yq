define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 


	$('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验必填
    	var formRequired = 0;
        $('#accountFrom').each(function(){
        	if(!$(this).valid()){
        		formRequired++;
            }
        })
        
        if(formRequired>0){
        	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
            return;
        }
        
        $(this).attr('disabled', true);

        var order = {
            id: $('#id').val(),
            code: $('#code').val(),
            english_name: $('#english_name').val(),
            name: $('#name').val(),
            remark: $('#remark').val(),
        };
        //异步向后台提交数据
        $.post('/currency/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$('#id').val(order.ID);
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

	$('#accountFrom').validate({
        rules: {	
        	code:{
        		required: true,
        		maxlength:10
        	},
        	english_name:{
        		required: true,
        		maxlength:100
        	},
        	name:{
        		required: true,
        		maxlength:100
        	},
        	remark:{
        		maxlength:100
        	}
        },
        messages:{	
        },
        highlight: function(element) {
            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        success: function(element) {
            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        }
    });


});