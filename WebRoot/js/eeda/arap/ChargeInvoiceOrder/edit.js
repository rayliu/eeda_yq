define(['jquery', 'metisMenu', 'sb_admin','dataTablesBootstrap','sco','validate_cn','./edit_item_table'], function ($, metisMenu) { 

$(document).ready(function() {

	document.title = '创建开票单 | ' + document.title;

    $('#menu_order').addClass('active').find('ul').addClass('in');
    
    //------------save
    $('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }
        
        $(this).attr('disabled', true);

        var order = {};
        order.id = $('#id').val();
        order.contact_person = $('#contact_person').val();
        order.phone = $('#phone').val();
        order.address = $('#address').val();
        order.total_amount = $('#total_amount').val();
        order.item_list = itemOrder.buildItemDetail();
        
        //异步向后台提交数据
        $.post('/chargeInvoiceOrder/save', {params:JSON.stringify(order)}, function(data){
        	debugger
            var order = data;
            if(order.ID>0){
            	eeda.contactUrl("edit?id",order.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$("#id").val(order.ID);
            	$("#order_no").val(order.ORDER_NO);
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                $("#status").val(order.STATUS);
                $("#total_amount").val(order.TOTAL_AMOUNT);
                
                $("#address").val(order.ADDRESS);
                $("#contact_person").val(order.CONTACT_PERSON);
                $("#phone").val(order.PHONE);
                $('#saveBtn').attr('disabled', false);
                $('#confrimBtn').attr('disabled', false);
                //异步刷新明细表
                itemOrder.refleshTable(order.ID);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });  
    
    /**
     * confirm
     * 
     */
    $('#confrimBtn').click(function(e){
    	var self = this;
    	$(self).attr('disabled',true);
    	
    	//异步向后台提交数据
        $.post('/chargeInvoiceOrder/confirm', {id:$('#id').val()}, function(data){
            var order = data;
            if(order.ID>0){
            	$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
                $("#status").val(order.STATUS);
                $('#saveBtn').attr('disabled', true);
            }else{
                $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                $(self).attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
            $(self).attr('disabled', false);
        });
    	
    });
 
    //按钮控制
    var order_id = $("#id").val();
    var status = $("#status").val();
    if(order_id==""){
    	$('#saveBtn').attr('disabled', false);
    }else{
    	if(status=='新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confrimBtn').attr('disabled', false);
    	}
    }
});
});
