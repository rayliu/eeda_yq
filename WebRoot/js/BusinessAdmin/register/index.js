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
						dataType:"json",
						dataFilter:function(data,type){
							return data;
						}
					}
				},
				phone:{
					required:true,
					isMobile:true
				},
				password:{
					required:true,
					minlength:6
				},
				passwordagain:{
					required:true,
					minlength:6,
					equalTo:"#password"
				}
			},
			messages:{
				login_name:{
					remote:"用户名已存在"
				},
				phone:{
					isMobile:"不合法手机号码"
				},
				passwordagain:{
					equalTo:"前后秘密不一致"
				}
			}
		});
		
		
		
		
		
		 jQuery.validator.addMethod("isMobile", function(value, element) { 
			  var length = value.length; 
			  var mobile = /^1(3|4|5|7|8)\d{9}$/; 
			  return this.optional(element) || (length == 11 && mobile.test(value)); 
		  }, "请正确填写您的手机号码"); 
		 
		 
	     $('#nextBtn').on('click',function(){
	    	 if(!$('#first_form').valid()){
	    		 return false;
	    	 }
	    	 var self = this;
	    	 var login_name = $('#login_name').val();
	    	 var phone = $('#phone').val();
	    	 var password = $('#password').val();
	    	 
	    	 self.disabled = true;
	    	 location.href="/BusinessAdmin/register/info?login_name="+encodeURI(encodeURI(login_name))
														    	 +'&phone='+encodeURI(encodeURI(phone))
														    	 +'&password='+encodeURI(encodeURI(password));
	    	 self.disabled = false;
	     });
	});
});