define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',
    './edit_shipment_table','./edit_land_table', './edit_charge_table','./edit_air_table', './edit_shipment_detail','./edit_air_detail'], function ($, metisMenu) {
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;
	
	//构造海运信息json
	var oseanJson = function(){
	}
	
    //------------save
    $('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数
        
        $(this).attr('disabled', true);
        var transport_type = [];
        $('#transport_type input[type="checkbox"]:checked').each(function(){
        	transport_type.push($(this).val()); 
        });
        //var items_array = itemOrder.buildItemDetail();
        //var charge_array = itemOrder.buildChargeDetail();
        
        //海运
        var shipment_detail = itemOrder.buildShipmentDetail();
        var shipment_item = itemOrder.buildOseanItem();
        //空运
        var air_detail = itemOrder.buildAirDetail();
        var air_item = itemOrder.buildAirItem();
        //陆运
        var air_item = itemOrder.buildLoadItem();
        
        var order={}
        order.id = $('#order_id').val();
        order.customer_id = $('#customer_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.type = $('#type').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#note').val();
        order.transport_type = transport_type.toString();
        //order.item_list = items_array; 
        //order.charge_list = charge_array;
        //海运
        order.shipment_detail = shipment_detail;
        order.shipment_list = shipment_item;
        //空运
        order.air_detail = air_detail;
        order.air_list = air_item;
        //陆运
        order.land_list = air_item;
        
        //异步向后台提交数据
        $.post('/jobOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
                $("#order_id").val(order.ID);
                $("#item_id").val(order.SHIPMENT.ID);
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
                //异步刷新海运明细表
                itemOrder.refleshOseanTable(order.ID);
                //异步刷新空运明细表
                itemOrder.refleshAirItemTable(order.ID);
                //异步刷新路运明细表
                itemOrder.refleshLandItemTable(order.ID);
                //异步刷新明细表
                //itemOrder.refleshChargeTable(order.ID);
                
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });  
    
    //创建派车单URL跳转传参
//    $('#create_truckOrder').click(function(){
//    	$(this).attr('disabled', true);
//    	var order_id = $('#order_id').val();
//    	var itemIds=[];
//    	$('#cargo_table input[type="checkbox"]').each(function(){
//    		var checkbox = $(this).prop('checked');
//    		if(checkbox){
//    			var itemId = $(this).parent().parent().attr('id');
//    			itemIds.push(itemId);
//    		}
//    	});
//    	location.href ="/truckOrder/create?order_id="+order_id+"&itemIds="+itemIds;
//    });
    
    var showServiceTab=function(service){
        switch (service){
          case 'ocean':
            $('#oceanDetailTab').show();
            break;
          case 'air':
            $('#airDetailTab').show();
            break;
          case 'domestic':
            $('#domesticDetailTab').show();
            break;
          case 'custom':
            $('#customDetailTab').show();
            break;
          case 'insur':
            $('#insurDetailTab').show();
            break;
        }
    };

    var hideServiceTab=function(service){
        switch (service){
          case 'ocean':
            $('#oceanDetailTab').hide();
            break;
          case 'air':
            $('#airDetailTab').hide();
            break;
          case 'domestic':
            $('#domesticDetailTab').hide();
            break;
          case 'custom':
            $('#customDetailTab').hide();
            break;
          case 'insur':
            $('#insurDetailTab').hide();
            break;
        }
    };

    $('#transport_type input[type="checkbox"]').change(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
    //运输方式checkbox回显
    var checkArray =$('#hiddenTransports').val().split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#transport_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);

                showServiceTab(checkValue);
	        }
	    })
    }
    
});
});