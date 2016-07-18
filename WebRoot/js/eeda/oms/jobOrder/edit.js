define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;
	
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
        var items_array = itemOrder.buildItemDetail();
        var charge_array = itemOrder.buildChargeDetail();
        var shipment_array = itemOrder.buildShipmentDetail();
        var order={}
        order.id = $('#order_id').val();
        order.customer_id = $('#customer_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.type = $('#type').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#note').val();
        order.transport_type = transport_type.toString();
        order.item_list = items_array; 
        order.charge_list = charge_array;
        order.shipment_detail = shipment_array;
        
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
                //异步刷新明细表
                itemOrder.refleshTable(order.ID);
                //异步刷新明细表
                itemOrder.refleshChargeTable(order.ID);
                
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
    $('#create_truckOrder').click(function(){
    	$(this).attr('disabled', true);
    	var order_id = $('#order_id').val();
    	var itemIds=[];
    	$('#cargo_table input[type="checkbox"]').each(function(){
    		var checkbox = $(this).prop('checked');
    		if(checkbox){
    			var itemId = $(this).parent().parent().attr('id');
    			itemIds.push(itemId);
    		}
    	});
    	location.href ="/truckOrder/create?order_id="+order_id+"&itemIds="+itemIds;
    })
    
    //运输方式checkbox回显
    var checkArray =$('#hiddenTransports').val().split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#transport_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);
	        }
	    })
    }
    
});
});