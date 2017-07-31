define(['jquery', 'file_upload' ,'sco'], function ($, metisMenu) { 
	$(document).ready(function() {
	
		$('#oneUpload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#oneUpload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/bookOrder/saveDocFile?order_id='+order_id+"&type=one",
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data.result.result){
				    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
				    		//异步刷新显示上传的文档信息
				    		itemOrder.refleshOneDocTable(order_id);
				    	}else{
				    		$.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
				    	}
				     },
			        error: function () {
			            alert('上传的时候出现了错误！');
			        }
			});
		})
		
		
		$('#threeUpload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#threeUpload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/bookOrder/saveDocFile?order_id='+order_id+"&type=three",
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data.result.result){
				    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
				    		//异步刷新显示上传的文档信息
				    		itemOrder.refleshThreeDocTable(order_id);
				    	}else{
				    		$.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
				    	}
				     },
			        error: function () {
			            alert('上传的时候出现了错误！');
			        }
			});
		})
		
	});
});