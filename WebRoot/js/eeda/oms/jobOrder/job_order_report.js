define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	//未保存或数据不足不生成PDF
	if($('#order_id').val()==''){
		$("#oceanPDF").hide();
	}
	
	//生成海运SI
	$('#printOceanSI').click(function(){
		//数据不足提示
    	var alert = '';
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#SONO').val()==''){
    		alert+='SO NO<br><br>';
    	}
    	if($('#mbl_no').val()==''){
    		alert+='MBL号码<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#shipping_mark').val()==''){
    		alert+='唛头 <br><br>';
    	}
    	if($('#cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
	    	var order_id = $("#order_id").val();
	    	$.post('/jobOrderReport/printOceanSI', {order_id:order_id}, function(data){
	    		if(data){
	                window.open(data);
	             }else{
	               $.scojs_message('生成海运SI PDF失败', $.scojs_message.TYPE_ERROR);
	               }
	    	}); 
    	}
		
	})
	//生成海运HBL PDF
    $('#printOceanHBL').click(function(){
    	//数据不足提示
    	var alert = '';
    
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#route').val()==''){
    		alert+='航线<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#hub').val()==''){
    		alert+='转运港 HUB<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#shipping_mark').val()==''){
    		alert+='唛头 <br><br>';
    	}
    	if($('#cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
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
 
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#route').val()==''){
    		alert+='航线<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#hub').val()==''){
    		alert+='转运港 HUB<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#shipping_mark').val()==''){
    		alert+='唛头<br><br>';
    	}
    	if($('#cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
    	if(alert!=''){
    		$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
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
    
    //生成海运头程资料数据不足判断
    $('#oceanHeadDetailBtn').click(function(){
    	//数据不足提示
    	var alert = '';
    	if($('#SONO').val()==''){
    		alert+='SO NO<br><br>';
    	}
    	if($('#head_carrier').val()==''){
    		alert+='头程船公司<br><br>';
    	}
    	if($('#carrier').val()==''){
    		alert+='香港收货人<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#route').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的港<br><br>';
    	}
    	if($('#closing_date').val()==''){
    		alert+='截关日期<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#eta').val()==''){
    		alert+='ETA';
    	}
    	if(alert!=''){
    		$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
			$('#oceanHeadDetailBtn1').click();
		}
    })
    
    //生成海运头程资料
    $('#printOceanHead').click(function(){
    	var oceanHead = {}
    	var form = $('#oceanHeadForm input');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	if(name){
        		oceanHead[name] = value;
        	}
    	}
    	oceanHead.id = $('#oceanHeadId').val();
    	oceanHead.order_id = $('#order_id').val();
    	
		$.post('/jobOrderReport/printOceanHead', {params:JSON.stringify(oceanHead)}, function(data){
				$("#oceanHeadId").val(data.oceanHeadId);
				if(data){
	                window.open(data.down_url);
	             }else{
	               $.scojs_message('生成海运头程资料失败', $.scojs_message.TYPE_ERROR);
	               }
				
		},'json').fail(function(){
		    	$.scojs_message('失败', $.scojs_message.TYPE_ERROR);
		  });
		
    });
    
    
    //生成空运booking PDF
    $('#printAirBooking').click(function(){
    	$('#shipper_input').val()==''
    			//数据不足提示
    	    	var alert = '';
    	    	if($('#shipper_input').val()==''){
    	    		alert+='发货人Shipper<br><br>';
    	    	}
    	    	if($('#shipper_info').val()==''){
    	    		alert+='发货人备注<br><br>';
    	    	}
    	    	if($('#consignee_input').val()==''){
    	    		alert+='收货人Consignee<br><br>';
    	    	}
    	    	if($('#consignee_info').val()==''){
    	    		alert+='收货人备注<br><br>';
    	    	}
    	    	if($('#notify_party_input').val()==''){
    	    		alert+='通知人NotifyParty<br><br>';
    	    	}
    	    	if($('#notify_party_info').val()==''){
    	    		alert+='通知人备注<br><br>';
    	    	}
    	    	if( $('#air_gross_weight').val()==''){
    	    		alert+='毛重<br><br>';
    	    	}
    	    	if($('#air_net_weight').val()==''){
    	    		alert+='净重';
    	    	}
    	    	if($('#air_volume').val()==''){
    	    		alert+='体积<br><br>';
    	    	}
    	    	if($('#shipping_mark').val()==''){
    	    		alert+='唛头<br><br>';
    	    	}
    	    	
    	    	if(alert!=''){
    				$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
    				$('#pdfAlert').click();
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