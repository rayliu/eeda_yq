define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	//未保存或数据不足不生成PDF
	if($('#order_id').val()==''){
		$("#oceanPDF").hide();
	}
	
	//生成海运HBL PDF
    $('#printOceanHBL').click(function(){
    	//数据不足提示
    	var alert = '';
    	if( $('#gross_weight').val()==''){
    		alert+='毛重,';
    	}
    	if($('#volume').val()==''){
    		alert+='体积 ,';
    	}
    	if($('#pieces').val()==''){
    		alert+='件数 ,';
    	}
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper,';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注,';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee,';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注,';
    	}
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty,';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注,';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码,';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名,';
    	}
    	if($('#route').val()==''){
    		alert+='航线,';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次,';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR,';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL,';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD,';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND,';
    	}
    	if($('#hub').val()==''){
    		alert+='转运港 HUB,';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD,';
    	}
    	if($('#shipping_mark').val()==''){
    		alert+='唛头 ,';
    	}
    	if($('#cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlert').show();
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
		}else{
	    	var order_id = $("#order_id").val();
	    	$.post('/jobOrderReport/printOceanHBL', {order_id:order_id}, function(data){
	    		if(data){
	                window.open(data);
	             }else{
	               $.scojs_message('生成海运HBL PDF失败', $.scojs_message.TYPE_ERROR);
	               }
	    	}); 
    	}
    		
    });
    
    //生成海运booking PDF
    $('#printOceanBooking').click(function(){
    	//数据不足提示
    	var alert = '';
    	if( $('#gross_weight').val()==''){
    		alert+='毛重,';
    	}
    	if($('#volume').val()==''){
    		alert+='体积 ,';
    	}
    	if($('#pieces').val()==''){
    		alert+='件数 ,';
    	}
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper,';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注,';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee,';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注,';
    	}
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty,';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注,';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码,';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名,';
    	}
    	if($('#route').val()==''){
    		alert+='航线,';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次,';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR,';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL,';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD,';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND,';
    	}
    	if($('#hub').val()==''){
    		alert+='转运港 HUB,';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD,';
    	}
    	if($('#shipping_mark').val()==''){
    		alert+='唛头 ,';
    	}
    	if($('#cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
    	if(alert!=''){
			$('#pdfAlert').show();
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
		}else{
	    	var order_no = $("#order_no").val();
	    	$.post('/jobOrderReport/printOceanBooking', {order_no:order_no}, function(data){
	    		if(data){
	    			window.open(data);
	    		}else{
	    			$.scojs_message('生成海运booking PDF失败', $.scojs_message.TYPE_ERROR);
	    		}
	    	});    	
    	}
    });
    
    //生成空运booking PDF
    $('#printAirBooking').click(function(){
    	$('#shipper_input').val()==''
    			//数据不足提示
    	    	var alert = '';
    	    	if($('#shipper_input').val()==''){
    	    		alert+='发货人Shipper,';
    	    	}
    	    	if($('#shipper_info').val()==''){
    	    		alert+='发货人备注,';
    	    	}
    	    	if($('#consignee_input').val()==''){
    	    		alert+='收货人Consignee,';
    	    	}
    	    	if($('#consignee_info').val()==''){
    	    		alert+='收货人备注,';
    	    	}
    	    	if($('#notify_party_input').val()==''){
    	    		alert+='通知人NotifyParty,';
    	    	}
    	    	if($('#notify_party_info').val()==''){
    	    		alert+='通知人备注,';
    	    	}
    	    	if( $('#air_gross_weight').val()==''){
    	    		alert+='毛重,';
    	    	}
    	    	if($('#air_volume').val()==''){
    	    		alert+='体积 ,';
    	    	}
    	    	if($('#shipping_mark').val()==''){
    	    		alert+='唛头 ,';
    	    	}
    	    	if($('#air_net_weight').val()==''){
    	    		alert+='净重 ,';
    	    	}
    	    	
    	    	if(alert!=''){
    				$('#pdfAlert').show();
    				$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
    			}else{
		    		var order_no = $("#order_no").val();
		    		$.post('/jobOrderReport/printAirBooking', {order_no:order_no}, function(data){
		    			if(data){
		    				window.open(data);
		    			}else{
		    				$.scojs_message('生成空运booking PDF失败', $.scojs_message.TYPE_ERROR);
		    			}
		    		});    	
    			}
    	});

    
});
});