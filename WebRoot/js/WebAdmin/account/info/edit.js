define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
  $(document).ready(function() {
	  //上传公司logo
	  $("#logo").on('click',function(){
		  $(this).fileupload({
				validation: {allowedExtensions: ['*']},
				autoUpload: true, 
			    url: '/WebAdmin/account/saveFile',
			    dataType: 'json',
			    add: function(e, data) {
			        var uploadErrors = [];
			        var acceptFileTypes = /^image\/(gif|jpe?g|png)$/i;

					//文件类型判断
			        if(data.originalFiles[0]['type'] && !acceptFileTypes.test(data.originalFiles[0]['type'])) {
			        	//uploadErrors.push('图片类型不对');
			        	$.scojs_message('图片类型不对', $.scojs_message.TYPE_ERROR);
			        	return;
			        }

					//文件大小判断
			        if(data.originalFiles[0]['size'] > 512000) {
			            $.scojs_message('文件不能大于500K', $.scojs_message.TYPE_ERROR);
			            return;
			        }else{
			        	data.submit();
			        }
				},
		        done: function (e, data) {
	        		if(data){
			    		$('#img_logo').val(data.result.NAME);
			    	
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
	  
	 $('#updateBtn').on('click',function(){
		 var self = this;
		 var order = {};
		 order.id = $('#user_id').val();
		 order.contact = $('#contact').val();
		 order.phone = $('#phone').val();
		 order.address = $('#address').val();
		 order.telephone = $('#telephone').val();
		 order.qq = $('#qq').val();
		 order.about = $('#about').val();
		 order.logo = $('#img_logo').prop('value')==undefined?$('#img_logo').attr('value'):$('#img_logo').prop('value');
		 $(self).attr('disabled',true);
		 $.post('/WebAdmin/account/updateInfo',{jsonStr:JSON.stringify(order)},function(data){
			 if(data){
				 $('#user_id').val(data.ID);
				 $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				 $(self).attr('disabled',false);
			 }else{
				 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
		 
		 
	 });
  });
});