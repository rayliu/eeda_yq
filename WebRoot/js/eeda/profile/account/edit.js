define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
	if(bank_name){
		document.title = bank_name +' | '+document.title;
	}
	$('#menu_profile').addClass('active').find('ul').addClass('in');

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
            bank_name: $('#bank_name').val(),
            bank_person: $('#bank_person').val(),
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
       	  bank_name: {
            required: true
          },
          bank_person: {
            required: true
          },
          account_no:{
            required: true
          },
          type:{
        	  required: true
          }
        },
        messages:{
        	bank_name: {
                required: "账户名称不能为空"
              },
              bank_person: {
                required: "开户人姓名不能为空"
              },
              account_no:{
                required: "银行账户号码不能为空"
              },
              type:{
            	  required: "账户类型不能为空"
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