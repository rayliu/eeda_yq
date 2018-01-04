define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
  $(document).ready(function() {
	  //上传公司logo
	  $("#logo").on('click',function(){
		  $(this).fileupload({
				validation: {allowedExtensions: ['*']},
				autoUpload: true, 
			    url: '/BusinessAdmin/account/saveFile',
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
			        if(data.originalFiles[0]['size'] > 1024000) {
			            $.scojs_message('文件不能大于1000K', $.scojs_message.TYPE_ERROR);
			            return;
			        }else{
			        	data.submit();
			        }
				},
		        done: function (e, data) {
	        		if(data){
			    		$('#img_logo').attr('value', data.result.NAME);
			    		var imgPre =Id("img_logo");
			  		    imgPre.src = '/upload/'+data.result.NAME;

			    	}else{
			    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
			    	}
			     },error: function () {
		            alert('上传的时候出现了错误！');
		        }
		   });
	  })
	  
	  //定义id选择器
	  function Id(id){
		  return document.getElementById(id);
	  }
	  
	  
	  jQuery.validator.addMethod("isPhone", function(value,element) { 
		  var length = value.length; 
		  var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
		  var tel = /^\d{3,4}-?\d{7,9}$/; 
		  return this.optional(element) || (tel.test(value) || mobile.test(value)); 

	  }, "请正确填写您的联系电话"); 
	  
	  $('#orderForm').validate({
		  rules: {
			  shop_telephone: {
		    	  isPhone: true,
		      },
		      phone: {
		    	  isPhone: true,
		      },
		      qq: {
		    	  number:true,
		    	  rangelength:[5,15]
		      }
		    },
		  messages: {
		    	rangelength: $.validator.format("请输入长度在 {0} 到 {1} 之间的字符串dd"),
		  }
	  });
  
	  
	  
	 $('#updateBtn').on('click',function(){
		 if(!$('#orderForm').valid()){
			 return false;
		 }
		 
		 var p_c_d = $('#p_c_d').val();
		 var province = '';
		 var city = '';
		 var district = '';
		 if(p_c_d!=''){
			 var array = p_c_d.split("-");
			 for (var i = 0; i < array.length; i++) {
				 if(i == 0){
					 province = array[i];
				 }else if(i == 1){
					 city = array[i];
				 }else if(i == 2){
					 district = array[i];
				 }
			 }
		 }
		 
		 var order = {};
		 order.id = $('#order_id').val();
		 order.c_name = $('#c_name').val();
		 order.contact = $('#contact_person').val();
		 order.telephone = $('#tel_phone').val();
		 order.province = province;
		 order.city = city;
		 order.district = district;
		 order.address = $('#address').val();
		 order.shop_telephone = $('#shop_telephone').val();
		 order.qq = $('#qq').val();
		 order.about = $('#intro').val();
		 order.logo = $('#img_logo').attr('value');
		 
		 
		 var self = this;
		 $(self).attr('disabled',true);
		 $.post('/BusinessAdmin/account/save_info',{jsonStr:JSON.stringify(order)},function(data){
			 if(data){
				 $('#order_id').val(data.ID);
				 $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				 $(self).attr('disabled',false);
			 }else{
				 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
	 });
	 
	 $('#address').click(function(){
		 var replaceStr = '-';//要替换的字符串
		 var str = $('#p_c_d_input').val();//要被替换的字符串
		 
		 if($('#address').val().trim() == ''){
			 $('#address').val(str.replace(new RegExp(replaceStr,'gm'),' ') + ' ');
		 }
	 });
  });
});