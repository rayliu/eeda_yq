define(['jquery', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	$('#orderForm').validate({
		rules: {
			old_password: {
			    required: true,
			    remote: {
			    	url: "/BusinessAdmin/account/check_pwd",
			    	type: "post",             
			    	dataType: "json",         
			    	data: {                    
			        	old_password: function() {
			            	return $("#old_password").val();
			            }
			        }
			    }
			},
			password: {
		        required: true,
		        minlength: 6
		    },
		    confirm_password: {
		        required: true,
		        equalTo: "#password"
		    }
		},
	    messages: {
	        old_password: {
		        required: "请填写原密码！！",
		        remote:"密码错误"
		    },
	        password: {
	            required: "不能为空",
	            minlength: "密码长度不能小于 6 个字母"
	        },
	        confirm_password: {
	            required: "请填写你刚才设定的密码！",
	            equalTo: "两次密码输入不一致"
	        }
	    }
	});
	  
	  
	  
	  
	  
	 $('#updateBtn').on('click',function(){
		 if(!$('#orderForm').valid()){
			 return;
		 }
		 var password = $('#password').val();
		 $.post('/BusinessAdmin/account/update_pwd',{password:password},function(data){
			 if(data){
				 $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
			 }else{
				 $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
		 
	 });
});
});