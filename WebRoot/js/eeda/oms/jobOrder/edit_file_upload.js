define(['jquery', 'file_upload' ,'sco'], function ($, metisMenu) { 
	$(document).ready(function() {
	
		$('#fileupload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#fileupload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/jobOrder/saveDocFile?order_id='+order_id,
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data.result.result){
				    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
				    		//异步刷新显示上传的文档信息
				    		itemOrder.refleshDocTable(order_id);
				    	}else{
				    		$.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
				    	}
				     },
			        error: function () {
			            alert('上传的时候出现了错误！');
			        }
			});
		})
		
		
		$('#twoUpload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#twoUpload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/jobOrder/saveDocFile?order_id='+order_id+'&type=two',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data.result.result){
				    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
				    		//异步刷新显示上传的文档信息
				    		itemOrder.refleshTwoDocTable(order_id);
				    	}else{
				    		$.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
				    	}
				     },
			        error: function () {
			            alert('上传的时候出现了错误！');
			        }
			});
		})
		
		$('#fourUpload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#fourUpload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/jobOrder/saveDocFile?order_id='+order_id+'&type=four',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data.result.result){
				    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
				    		//异步刷新显示上传的文档信息
				    		itemOrder.refleshFourDocTable(order_id);
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