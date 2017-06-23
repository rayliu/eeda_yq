define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function() {
		var isImporting = false;
		// 导入运输单
	    $("#uploadBtn").click(function(){
	    	$("#toFileUpload").click();
	    });
	    
	    var str=null;
	    var errCustomerNo=null;
	    var errCustomerNoArr=[];
		$('#toFileUpload').fileupload({
	        dataType: 'json',
	        done: function (e,data) {
	        	$("#uploadBtn").attr('disabled',false);
	        	$("#uploadBtn").text('导入单据');
	        	isImporting = false;
	        	order.refleshTable();
	        	alert(data.result.CAUSE);
	        },
	        progressall: function (e, data) {//设置上传进度事件的回调函数  
	        	 $("#uploadBtn").attr('disabled',true);
	        	 $("#uploadBtn").html("<img src='/images/loading.gif' style='height: 25px;'/>正在导入...");
	        	 isImporting = true;
	        } 
	    },'json').error(function (jqXHR, textStatus, errorThrown) {
	        alert("出错了，请刷新页面重新尝试。")
	        console.log(errorThrown);
	    });
		
		
//		if(true){
//			$.scojs_message('in', $.scojs_message.OK);
//			window.setInterval(updateStatus(),3000)
//		}
//		
//		function updateStatus(){
//			$.scojs_message('d', $.scojs_message.TYPE_FALSE);
//			$.post('/importOrder/getImportStatus',function(data){
//				if(data){
//					$("#uploadBtn").attr('disabled',false);
//		        	$("#uploadBtn").text('导入单据');
//		        	isImporting = false;
//		        	order.refleshTable();
//		        	alert('导入成功');
//				}
//			});
//		}
		
	});
});

