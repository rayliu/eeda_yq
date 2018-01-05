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
			this.disabled = true;
			var type = $("#register_type").val();
			var id_card = '';
			var company_pic = '';
			var company_name = '';
			if(type == '1'){
				id_card = $("#img_id_card").attr('value');
			}else if(type=="2"){
				company_pic = $("#img_company_pic_2").attr("value")
				company_name = $(":input[name=company_name]").val()
				
			}
			var user_name = $("#user_name").val();
			var password = $("#user_pass").val();
			var phone = $("#user_phone").val();
			var contact = $(":input[name=contact_"+type+"]").val();
			var telephone = $(":input[name=telephone_"+type+"]").val();
			var trade_type = $(":input[name=trade_type_"+type+"]").val();
			var shop_address = $(":input[name=shop_address_"+type+"]").val();
			var shop_telephone = $(":input[name=shop_telephone_"+type+"]").val();
			var qq = $(":input[name=qq_"+type+"]").val();
			var about = $(":input[name=about_"+type+"]").val();
			var logo = $("#img_logo_"+type+"").attr("value");
			
			var p_c_d = $("#p_c_d_"+type+"").val();
			var address = p_c_d.split('-'); 
			
			var shop_province = address[0];
			var shop_city = address[1];
			var shop_district = '';
			if(p_c_d.length > 15){
				shop_district = address[2];
			}
			
			window.location.href="/BusinessAdmin/register/done?id_card="+encodeURI(encodeURI(id_card))
								+"&type="+type
								+"&company_pic="+encodeURI(encodeURI(company_pic))
								+"&company_name="+encodeURI(encodeURI(company_name))
								+"&user_name="+encodeURI(encodeURI(user_name))
								+"&password="+password
								+"&phone="+phone
								+"&contact="+encodeURI(encodeURI(contact))
								+"&telephone="+telephone
								+"&trade_type="+trade_type
								+"&shop_address="+encodeURI(encodeURI(shop_address))
								+"&shop_telephone="+shop_telephone
								+"&qq="+qq
								+"&about="+encodeURI(encodeURI(about))
								+"&logo="+encodeURI(encodeURI(logo))
								+"&shop_province="+shop_province
								+"&shop_city="+shop_city
								+"&shop_district="+shop_district;
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
				    add: function(e, data) {
				        var uploadErrors = [];
				        var acceptFileTypes = /^image\/(jpe?g)$/i;

						//文件类型判断
				        if(data.originalFiles[0]['type'] && !acceptFileTypes.test(data.originalFiles[0]['type'])) {
				        	//uploadErrors.push('图片类型不对');
				        	$.scojs_message('图片类型不对', $.scojs_message.TYPE_ERROR);
				        	return;
				        }

						//文件大小判断
				        if(data.originalFiles[0]['size'] > 2048000) {
				            $.scojs_message('文件不能大于2M', $.scojs_message.TYPE_ERROR);
				            return;
				        }else{
				        	data.submit();
				        }
					},
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