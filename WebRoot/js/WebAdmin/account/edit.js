define(['jquery', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	$('#orderForm').validate({
		rules: {
			old_password: {
			    required: true,
			    minlength: 6,
			},
			password: {
		        required: true,
		        minlength: 6
		    },
		    confirm_password: {
		        required: true,
		        minlength: 6,
		        equalTo: "#password"
		    },
		    user_name{
		    	required:true
		    }
		},
	    messages: {
	    	user_name{
	    		required:"用户名不能为空"
	    	},
	        old_password: {
		        required: "不能为空",
		        minlength: "密码长度不能小于 6 个字母",
		        remote:"密码错误"
		    },
	        password: {
	            required: "不能为空",
	            minlength: "密码长度不能小于 6 个字母"
	        },
	        confirm_password: {
	            required: "不能为空",
	            minlength: "密码长度不能小于 6 个字母",
	            equalTo: "两次密码输入不一致"
	        }
	    }
	});
	  
	  
	  
	  
	  
	 $('#updateBtn').on('click',function(){
		 if(!$('#orderForm').valid()){
			 return;
		 }
		 var user_name=$("#user_name").val();
		 var password = $('#password').val();
		 $.post('/WebAdmin/account/update',{password:password,user_name:user_name},function(data){
			 if(data){
				 $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
			 }else{
				 $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
		 
	 });
});
});