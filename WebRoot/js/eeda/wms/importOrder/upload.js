define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function() {
		var isImporting = import_status;
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
	        	 
	        	 var impor = window.setInterval(function(){
	 				if(isImporting){
	 					$.post('/importOrder/getImportStatus',function(data){
	 						if(!data){
	 							$("#uploadBtn").attr('disabled',false);
	 				        	$("#uploadBtn").text('导入单据');
	 				        	isImporting = false;
	 				        	order.refleshTable();
	 				        	alert('导入成功');
	 				        	clearInterval(impor);
	 						}
	 					});
	 				}
	 			},10000)
	        } 
	    },'json').error(function (jqXHR, textStatus, errorThrown) {
	        alert("出错了，请刷新页面重新尝试。")
	        console.log(errorThrown);
	    });
		
		
		var interval = window.setInterval(function(){
			if(isImporting){
				$.post('/importOrder/getImportStatus',function(data){
					if(!data){
						$("#uploadBtn").attr('disabled',false);
			        	$("#uploadBtn").text('导入单据');
			        	isImporting = false;
			        	order.refleshTable();
			        	alert('导入成功');
			        	clearInterval(interval);
					}
				});
			}
		},10000)
		
	});
});

