define(['jquery', 'sco', 'file_upload',"validate_cn",'dataTablesBootstrap'], function ($, metisMenu) {
	
	$(document).ready(function() {
		$('.addr').click(function(){
			 var replaceStr = '-';//要替换的字符串
			 var type = $("#register_type").val();
			 var str = $('#p_c_d_'+type+'_input').val();//要被替换的字符串
			 
			 if($(this).val().trim() == ''){
				 $('[name=shop_address_'+type+']').val(str.replace(new RegExp(replaceStr,'gm'),' ') + ' ');
			 }
		});
		
		//校验规则
		$("#two_form").validate({
			rules:{
				contact_1 :{
					required:true
				},
				contact_2 :{
					required:true
				},
				telephone_1:{
					required:true,
					isPhone:true
				},
				telephone_2:{
					required:true,
					isPhone:true
				},
				shop_telephone_1:{
					isPhone:true
				},
				shop_telephone_2:{
					isPhone:true
				},
				qq_1: {
			    	  number:true,
			    	  rangelength:[5,15]
			    },
			    qq_2: {
			    	  number:true,
			    	  rangelength:[5,15]
			    }
			 },
			 messages: {
			    rangelength: $.validator.format("请输入长度在 {0} 到 {1} 之间的字符串dd"),
			 }
		});
		
		jQuery.validator.addMethod("isPhone", function(value,element) { 
			  var length = value.length; 
			  var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
			  var tel = /^\d{3,4}-?\d{7,9}$/; 
			  return this.optional(element) || (tel.test(value) || mobile.test(value)); 

		}, "请正确填写您的联系电话"); 
		
		$('.hornav a').click(function(){
	        $('.hornav li').removeClass('current');
	        $(this).parent().addClass('current');
	        $("#personal").hide();
	        $("#company").hide();

	        var curShowType = $(this).data('type');
	        $("#"+curShowType).show();

	        if(curShowType=="personal"){
	            $("#register_type").val("1");
	        }else{
	            $("#register_type").val("2");
	        }
	    });
		
		//点击下一步
		$("#nextBtn").click(function(){
			if(!$("#two_form").valid()){
				return false;
			}
			
			var type=$("#register_type").val();
			var user={};
			user.type=type;
			if(type=='1'){
				user.id_card=$("#img_id_card").attr('value');
			}else if(type=="2"){
				user.company_pic=$("#img_company_pic_2").attr("value")
				user.company_name=$(":input[name=company_name]").val()
				
			}
			user.user_name = $("#user_name").val();
			user.password = $("#user_pass").val();
			user.phone = $("#user_phone").val();
			user.contact = $(":input[name=contact_"+type+"]").val();
			user.telephone = $(":input[name=telephone_"+type+"]").val();
			user.trade_type = $(":input[name=trade_type_"+type+"]").val();
			user.shop_address = $(":input[name=shop_address_"+type+"]").val();
			user.shop_telephone = $(":input[name=shop_telephone_"+type+"]").val();
			user.qq = $(":input[name=qq_"+type+"]").val();
			user.about = $(":input[name=about_"+type+"]").val();
			user.logo = $("#img_logo_"+type+"").attr("value");
			var p_c_d = $("#p_c_d_"+type+"").val();
			var address = p_c_d.split('-'); 
			user.shop_province = address[0];
			user.shop_city = address[1];
			user.shop_district = address[2];
			window.location.href="/BusinessAdmin/register/done?jsonStr="+JSON.stringify(user);
		});
		
		  //定义id选择器
		  function Id(id){
			  return document.getElementById(id);
		  };
		  
		//保存图片
		$(":input[type=file]").click(function(){
			var id=$(this).attr("id");
			var self=$(this);  
			self.fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/BusinessAdmin/register/saveFile',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data){
				    		$.scojs_message('已选择', $.scojs_message.TYPE_OK);
				    		$("#img_"+id).attr('value',data.result.NAME)
				    		var imgPre =Id("img_"+id+"");
				  		    imgPre.src ='/upload/'+data.result.NAME; 
				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			 });
		});
		
		
		 
		
	});
});