define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {

      //校验是否已存在
        var order_id = $('#order_id').val();
        if(!order_id){
        	$('#orderForm').validate({
	            rules: {
	            	employee_name: {
	                	remote:{
		                    url: "/employeeFiling/checkCodeExist",
		                    type: "post",
		                    data:  {
		                        code: function() { 
		                              return $("#employee_name").val();
		                        }
	                    	}
	                	}
	                }
	            },
	            messages:{
	            	employee_name:{
	                    remote:"此员工名字已存在"
	                }
	            },
	            highlight: function(element) {
	                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
	            },
	            success: function(element) {
	                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
	            }
            });
        }
        
        
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
                id: $('#order_id').val(),
                c_name: $('#c_name').val(),
                entry_time: $('#entry_time').val(),
                user_tel: $('#user_tel').val(),
                user_phone: $('#user_phone').val(),
            	user_fax: $('#user_fax').val(),
            	email:$("#email").val()
            };
            //异步向后台提交数据
            $.post('/employeeFiling/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#order_id").val(order.ID);
                	$("#c_name").val(order.C_NAME);
                	$("#entry_time").val(order.ENTRY_TIME);
                	$("#create_time").val(order.CREATE_TIME);
                	$("#user_tel").val(order.USER_TEL);
                	$("#user_phone").val(order.USER_PHONE);
                	$("#user_fax").val(order.USER_FAX);
                	$("#email").val(order.EMAIL);
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