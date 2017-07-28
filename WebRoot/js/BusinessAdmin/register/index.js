define(['jquery', 'sco', 'file_upload',"validate_cn",'dataTablesBootstrap'], function ($, metisMenu) {
	
	$(document).ready(function() {
	
		//校验规则
		$("#first_form").validate({
			rules:{
				login_name:{
					required:true,
					maxlength:20,
					remote:{
						type:"post",
						url:"/BusinessAdmin/register/exist",
						dataType:"html",
						dataFilter:function(data,type){
							if(data=="true"){
								return false;
							}else{
								return true;
							}
								
						}
					}
				},
				phone:{
					required:true,
					isMobile:true
				},
				password:{
					required:true,
				},
				passwordagain:{
					required:true,
					equalTo:"#password"
				}
			},
			messages:{
				login_name:{
					required:"请输入用户名！！！",
					remote:"已经存在了"
				},
				phone:{
					required:"必须填写手机号码！！",
					isMobile:"必须是手机号!!!"
				},
				password:{
					required:"请设置密码！！！"
				},
				passwordagain:{
					required:"请再次核对密码！！！",
					equalTo:"前后密码不一致！！！！！！"
				}
			}
		})
		 jQuery.validator.addMethod("isMobile", function(value, element) { 
			  var length = value.length; 
			  var mobile = /^1(3|4|5|7|8)\d{9}$/; 
			  return this.optional(element) || (length == 11 && mobile.test(value)); 
		  }, "请正确填写您的手机号码"); 
	});
});