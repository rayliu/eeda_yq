define(['jquery', 'file_upload' ,'sco'], function ($, metisMenu) { 
	$(document).ready(function() {

		$('#fileupload').click(function(){
			var order_id = $('#order_id').val();
		
			$('#fileupload').fileupload({
					validation: {allowedExtensions: ['doc','docx']},
					autoUpload: true, 
				    url: '/trJobOrder/saveDocFile?order_id='+order_id,
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
		
		
		// 贸易商品信息表
		var order_id = $('#order_id').val();
	    $("#import_tradeItem").click(function(){
	    	order_id = $('#order_id').val();
	    	fileUpload(order_id);
	    	if($('#order_id').val() == ''){
	    		$.scojs_message('先保存订单才可导入', $.scojs_message.TYPE_ERROR);
	    		return false;
	    	}
	    	
	    	$("#importFileUpload").click();
	    	
	    });
	    
	    var fileUpload = function(order_id){
	    	var str = null;
		    var errCustomerNo = null;
		    var errCustomerNoArr = [];
			$('#importFileUpload').fileupload({
		        dataType: 'json',
		        url: '/importOrder?order_type=tradeJobOrder&order_id='+order_id,
		        done: function (e,data) {
		        	$("#footer").show();
		        	$("#msgLoad").empty().append('<h4>'+data.result.CAUSE+'</h4>');

		        	itemOrder.refleshTradeCostItemTable(order_id);
		        },  
		        progressall: function (e, data) {//设置上传进度事件的回调函数  
		        	str = null;
		            errCustomerNo = null;
		            errCustomerNoArr = [];
		        	$('#msgLoad').empty().append('<center><img src="/yh/image/loading5.gif" width="20%"><h4>导入过程可能需要一点时间，请勿退出页面！</h4></center>');
		        	$('#myModal').modal('show');
		        	$("#footer").hide();
		        } 
		    },'json').error(function (jqXHR, textStatus, errorThrown) {
		        alert("出错了，请刷新页面重新尝试。")
		        console.log(errorThrown);
		    });
	    }
	    
	});
});