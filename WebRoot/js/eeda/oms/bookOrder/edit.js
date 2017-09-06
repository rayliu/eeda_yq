define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN', 'jq_blockui'], function ($, metisMenu) {
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
     //两位小数处理
	var dealPoint = function(ids){
		var ids = ids.split(",")
		for(x in ids){
			id = $.trim(ids[x])
			var num = parseFloat($("#"+id+"").val()).toFixed(2);
			if(!isNaN(num)){
				$("#"+id+"").val(num)
			}
		}
	}
	
	dealPoint("gross_weight,volume");

	
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
        	$.scojs_message('客户和出货时间为必填字段', $.scojs_message.TYPE_ERROR);
        	return;
        }

        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
        $('#saveBtn').attr('disabled', true);

        var order={}
        order.id = $('#order_id').val();
        order.office_id = $('#office_id').val();
        order.ref_office_id = $('#ref_office_id').val();
        order.HBLconsignee = $('#HBLconsignee').val();
        order.outer_order_no = $('#outer_order_no').val();
        order.type = $('#type').val();
        order.order_export_date = $('#order_export_date').val();
        order.gargo_name = $('#gargo_name').val();        
        order.pickup_addr = $('#pickup_addr').val();
        order.delivery = $('#delivery').val();
        order.pieces = $('#pieces').val();
        order.gross_weight = $('#gross_weight').val();
        order.volume = $('#volume').val();
        order.pol_id = $('#pol_id').val();
        order.pod_id = $('#pod_id').val();
        order.remark = $('#remark').val();
        
        
        //相关文档
        order.doc_list = eeda.buildTableDetail("doc_table","");
        //异步向后台提交数据
        $.post('/bookOrder/save', {params:JSON.stringify(order)}, function(data){
            var server_back_order = data;
            if(server_back_order.ID){
            	eeda.contactUrl("edit?id",server_back_order.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$('#saveBtn').attr('disabled', false);
            	$('#confirmCompleted').attr('disabled', false);
                $("#order_id").val(server_back_order.ID);                
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
			$.post('/bookOrder/submitBooking',{order_id:order_id},function(data){
				if(data.result){
				    $.scojs_message('提交成功', $.scojs_message.TYPE_OK);
				    //异步刷新明细表
	                $('#submitBtn').attr('disabled',true);
	                $('#saveBtn').attr('disabled',true);
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