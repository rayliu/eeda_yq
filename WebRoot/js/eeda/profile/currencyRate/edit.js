define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
	
	document.title = '汇率 | '+document.title;
	

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
            currency_code: $('#cost_currency_input').val(),
            currency_id: $('#cost_currency').val(),
            to_currency_code: $('#to_currency_input').val(),
            to_currency_id: $('#to_currency').val(),
            rate: $('#rate').val(),
            from_stamp: $('#peroid_begin_time').val(),
            to_stamp: $('#peroid_end_time').val(),
            remark: $('#remark').val(),
        };
        //异步向后台提交数据
        $.post('/currencyRate/save', {params:JSON.stringify(order)}, function(data){
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
	
/*	$("#cost_currency_list").on("mousedown",function(){
		self = $(this);
		debugger
		var currency_id = $("#cost_currency").val();
		$.post("/currencyRate/searchCurrency",{currency_id:currency_id},function(data){
			result = data;
			if(result !=false){
				$("#rate").val(result.RATE);
				$("#peroid_begin_time").val(result.FROM_STAMP);
				$("#peroid_end_time").val(result.TO_STAMP);
				$("#remark").val(result.REMARK);
			}
		})
	})*/
	
	$('#accountFrom').validate({
        rules: {
        	rate:{
        		number:true,
        		maxlength:18
        	},
        	remark:{
        		maxlength:100
        	}
        },
        messages:{
        	rate:{
        		maxlength:$.validator.format("请输入长度最多是 {0} 个数字"),
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