define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	//未保存或数据不足不生成PDF
	if($('#order_id').val()==''){
		$("#oceanPDF").hide();
	}
	
	//生成HBL PDF
    $('#printHBL').click(function(e){
    	e.preventDefault();
    	$(this).attr('disabled',true);
    	var order_no = $("#order_no").val();
    	$.post('/jobOrderReport/printOceanHBL', {order_no:order_no}, function(data){
    		if(data){
                window.open(data);
             }else{
               $.scojs_message('生成HBL PDF失败', $.scojs_message.TYPE_ERROR);
               }
    	});    	
    	$(this).attr('disabled',false);
    });
	
});
});