define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	//未保存或数据不足不生成PDF
	if($('#order_id').val()==''){
		$("#oceanPDF").hide();
	}
	
	//生成HBL PDF
    $('#printHBL').click(function(){
    	//数据不足提示
    	if( $('#gross_weight').val()==''||$('#volume').val()==''||$('#pieces').val()==''
    		||$('#ocean_shipper_input').val()==''||$('#ocean_shipper_info').val()==''||$('#ocean_consignee_input').val()==''
    		||$('#ocean_consignee_info').val()==''||$('#ocean_consignee_info').val()==''||$('#ocean_notify_party_input').val()==''
    		||$('#ocean_notify_party_info').val()==''||$('#hbl_no').val()==''||$('#vessel').val()==''
    		||$('#route').val()==''||$('#voyage').val()==''||$('#por').val()==''||$('#pol').val()==''
    		||$('#pod').val()==''||$('#fnd').val()==''||$('#hub').val()==''
    		||$('#etd').val()==''||$('#shipping_mark').val()==''||$('#cargo_desc').val()==''
    	){
    		$.scojs_message('数据不足,不能打印PDF', $.scojs_message.TYPE_ERROR);
    		
    	}else{
	    	var order_no = $("#order_no").val();
	    	$.post('/jobOrderReport/printOceanHBL', {order_no:order_no}, function(data){
	    		if(data){
	                window.open(data);
	             }else{
	               $.scojs_message('生成HBL PDF失败', $.scojs_message.TYPE_ERROR);
	               }
	    	}); 
    	}
    });
    
    //生成booking PDF
    $('#printBooking').click(function(){
    	//数据不足提示
    	if( $('#gross_weight').val()==''||$('#volume').val()==''||$('#pieces').val()==''
    		||$('#ocean_shipper_input').val()==''||$('#ocean_shipper_info').val()==''||$('#ocean_consignee_input').val()==''
    		||$('#ocean_consignee_info').val()==''||$('#ocean_consignee_info').val()==''||$('#ocean_notify_party_input').val()==''
    		||$('#ocean_notify_party_info').val()==''||$('#hbl_no').val()==''||$('#vessel').val()==''
    		||$('#route').val()==''||$('#voyage').val()==''||$('#por').val()==''||$('#pol').val()==''
    		||$('#pod').val()==''||$('#fnd').val()==''||$('#hub').val()==''
    		||$('#etd').val()==''||$('#shipping_mark').val()==''||$('#cargo_desc').val()==''
    	){
    		$.scojs_message('数据不足,不能打印PDF', $.scojs_message.TYPE_ERROR);
    	}else{
	    	var order_no = $("#order_no").val();
	    	$.post('/jobOrderReport/printOceanBooking', {order_no:order_no}, function(data){
	    		if(data){
	    			window.open(data);
	    		}else{
	    			$.scojs_message('生成bookingPDF失败', $.scojs_message.TYPE_ERROR);
	    		}
	    	});    	
    	}
    });

    
});
});