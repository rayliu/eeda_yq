define(['jquery', 'metisMenu', 'sb_admin', 'validate_cn', 'file_upload', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	// 导入运输单
    $("#importBtn").click(function(){
    	$("#fileUpload").click();
    });
    
    
    var str = null;
    var errCustomerNo = null;
    var errCustomerNoArr = [];
	$('#fileUpload').fileupload({
        dataType: 'json',
        done: function (e,data) {
        	$("#footer").show();
        	$("#msgLoad").empty().append('<h4>'+data.result.CAUSE+'</h4>');
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
    });;
    
});
});