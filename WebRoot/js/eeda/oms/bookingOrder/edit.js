define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN',
        	'jq_blockui','./edit_ocean_detail','./edit_land_detail','./edit_custom_detail','./edit_air_detail','./edit_doc_table','./cost_table_edit'], function ($, metisMenu) {
$(document).ready(function() {

    
    $('#collapseGoodsInfo').on('show.bs.collapse', function () {
        $('#collapseGoodsIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
    });
    $('#collapseGoodsInfo').on('hide.bs.collapse', function () {
        $('#collapseGoodsIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
    
    $('#collapseOrderInfo').on('show.bs.collapse', function () {
        $('#collapseOrderIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
    });
    $('#collapseOrderInfo').on('hide.bs.collapse', function () {
        $('#collapseOrderIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });

	 //按钮状态
	var id = $('#order_id').val();
	var status = $('#status').val();
    if(id==''){
    	$('#confirmCompleted').attr('disabled', true);
    }else{
		if(status=='已完成'){
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled', true);
		}
    }
	
	//已完成工作单确认
	$('#confirmCompleted').click(function(){
        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
		$('#confirmCompleted').attr('disabled', true);
		id = $('#order_id').val();
		$.post('/bookOrder/confirmCompleted', {id:id}, function(data){
	            $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
	            $('#saveBtn').attr('disabled', true);
	            $.unblockUI();
	    },'json').fail(function() {
	        $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
	        $('#confirmCompleted').attr('disabled', false);
            $.unblockUI();
	   });
	})
	
	
	var showServiceTab=function(service){
        switch (service){
            case 'ocean':
                $('#oceanDetail').show();
                $('#ocean_status').show();
                break;
            case 'air':
                $('#airDetail').show();
                $('#air_status').show();
                break;
            case 'land':
                $('#landDetail').show();
                break;
            case 'custom':
                $('#customDetail').show();
//                $('#custom_status').show();
                break;            
        }
    };

    var hideServiceTab=function(service){
        switch (service){
            case 'ocean':
                $('#oceanDetail').hide();
                $('#ocean_status').hide();
                break;
            case 'air':
                $('#airDetail').hide();
                $('#air_status').hide();
                break;
            case 'land':
                $('#landDetail').hide();
                break;
            case 'custom':
                $('#customDetail').hide();
                $('#custom_status').hide();
                break;            
        }
    };
	
	
	//委托类型checkbox回显,transport_type是用js拿值
    var checkArray = transport_type_hidden.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#transport_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	this.checked = true;
                showServiceTab(checkValue);
	        }
	    })
    }
	
    //单击时，tab的显示隐藏
    $('#transport_type input[type="checkbox"]').change(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
	
	
    //------------save
	$('#saveBtn').click(function(e){
        //提交前，校验数据
        var formRequired = 0;
        $('form').each(function(){
        	if(!$(this).valid()){
        		formRequired++;
            }
        })
        if(formRequired>0){
        	$.scojs_message('货物名称和出货时间为必填字段', $.scojs_message.TYPE_ERROR);
        	return;
        }

        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
        $('#saveBtn').attr('disabled', true);
        //服务项目checkbox遍历取值
        var transport_type = [];
        $('#transport_type input[type="checkbox"]:checked').each(function(){
        	transport_type.push($(this).val()); 
        });
        var transport_type_str = transport_type.toString();

        var order={}
        order.id = $('#order_id').val();
        order.office_id = $('#office_id').val();
        order.to_office_id = $('#to_office_id').val();

        order.shipper = $('#shipper').val();     
        order.shipper_info = $('#shipper_info').val();
        
        order.consignee = $('#consignee').val();     
        order.consignee_info = $('#consignee_info').val();
        
        order.notify = $('#notify').val();     
        order.notify_info = $('#notify_info').val();
        
        order.transport_type = transport_type_str;
        order.booking_no = $('#booking_no').val();
        order.outer_order_no = $('#outer_order_no').val();
        order.relation_no = $('#relation_no').val();
        order.type = $('#type').val();        
        order.entrust_type = $('#entrust_type').val();
        order.entrust = $('#entrust').val();
        order.gargo_name = $('#gargo_name').val();
        order.pieces = $('#pieces').val();
        order.order_unit = $('#order_unit').val();
        order.order_export_date = $('#order_export_date').val();
        order.volume = $('#volume').val();
        order.gross_weight = $('#gross_weight').val();
        
        if(transport_type_str.indexOf('ocean')>-1){
        	//海运信息
            order.ocean_detail=itemOrder.buildOceanDetail();
        }
        if(transport_type_str.indexOf('air')>-1){
        	//空运信息
            order.air_detail=itemOrder.buildAirDetail();
        }
        if(transport_type_str.indexOf('land')>-1){
        	//陆运信息
            order.land_detail=itemOrder.buildLandDetail();
        }
        if(transport_type_str.indexOf('custom')>-1){
        	//报关信息
            order.custom_detail=itemOrder.buildCustomDetail();
        }       
        
        //相关文档
        order.doc_list = eeda.buildTableDetail("doc_table","");
        //异步向后台提交数据
        $.post('/bookingOrder/save', {params:JSON.stringify(order)}, function(data){
            var server_back_order = data;
            if(server_back_order.ID){
            	eeda.contactUrl("edit?id",server_back_order.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$('#saveBtn').attr('disabled', false);
            	$('#confirmCompleted').attr('disabled', false);
                $("#order_id").val(server_back_order.ID);
                if(server_back_order.OCEAN != null){
                	$("#ocean_id").val(server_back_order.OCEAN.ID);
                }
                if(server_back_order.AIR != null){
                	$("#air_id").val(server_back_order.AIR.ID);
                }
                if(server_back_order.LAND != null){
                	$("#land_id").val(server_back_order.LAND.ID); 
                }
                if(server_back_order.CUSTOM != null){
                	$("#custom_id").val(server_back_order.CUSTOM.ID);
                }
                $("#booking_no").val(server_back_order.BOOKING_NO);
                $("#creator_name").val(server_back_order.CREATOR_NAME);
                
                //异步刷新明细表
                $.unblockUI();
            }else{
                if(data.ERR_CODE == 'update_stamp_not_equal'){
                    saveOrderToLocalstorage(order);
                    $.scojs_message(data.ERR_MSG, $.scojs_message.TYPE_ERROR);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                }
                
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
            $.unblockUI();
        });
    	
	});
	$.unblockUI();

    
    
	
	$('#submitBtn').click(function(){
			var order_id = $('#order_id').val();
			$.post('/bookingOrder/submitBooking',{order_id:order_id},function(data){
				if(data.result){
				    $.scojs_message('提交成功', $.scojs_message.TYPE_OK);
				    //异步刷新明细表
	                $('#submitBtn').attr('disabled',true);
	                $('#saveBtn').attr('disabled',true);
			    }else if(data){
			    	$.scojs_message(data, $.scojs_message.TYPE_ERROR);
				    self.disabled = false;
			    }else{
				    $.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
				    self.disabled = false;
			    }
			    $.unblockUI();
			}).fail(function() {
			    $.unblockUI();
			    self.disabled = false;
	            $.scojs_message('后台出错', $.scojs_message.TYPE_ERROR);
	        });
		});
	if($('#booking_submit_flag').val()=='Y'){
		$('#submitBtn').attr('disabled',true);
        $('#saveBtn').attr('disabled',true);
	}
  });
});