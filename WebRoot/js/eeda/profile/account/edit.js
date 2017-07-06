define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 


	$('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#accountFrom").valid()){
            return;
        }
        
        $(this).attr('disabled', true);

       
        var order = {
            id: $('#id').val(),
            account_name: $('#account_name').val(),
            bank_name: $('#bank_name').val(),
            account_no: $('#account_no').val(),
            type: $('#type').val(),
            currency: $('#currency').val(),
            remark: $('#remark').val(),
        };
        //异步向后台提交数据
        $.post('/account/save', {params:JSON.stringify(order)}, function(data){
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

	$('#accountFrom').validate({
        rules: {
        	account_name: {
            required: true
          },
          bank_name: {
            required: true
          },
          account_no:{
            required: true
          }
        },
        messages:{
        	account_name: {
                required: "账户名称不能为空"
              },
              bank_name: {
                required: "开户行不能为空"
              },
              account_no:{
                required: "银行账户号码不能为空"
              }
        },
        highlight: function(element) {
            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
        },
        success: function(element) {
            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        }
    });


});