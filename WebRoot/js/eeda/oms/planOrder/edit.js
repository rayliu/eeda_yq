define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	document.title = order_no + ' | ' + document.title;

        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $(this).attr('disabled', true);

            var items_array = salesOrder.buildCargoDetail();
            var order = {
                id: $('#order_id').val(),
                customer_id: $('#customer_id').val(),
                type: $('#type').val(),
                remark: $('#note').val(),
                status: $('#status').val()==''?'新建':$('#status').val(),
                item_list:items_array
                
            };

            //异步向后台提交数据
            $.post('/planOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#creator_name").val(order.CREATOR_NAME);
                    $("#create_stamp").val(order.CREATE_STAMP);
                    $("#order_id").val(order.ID);
                    $("#order_no").val(order.ORDER_NO);
                    $("#status").val(order.STATUS);
                    $("#note").val(order.REMARK);
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    
                    //异步刷新明细表
                    salesOrder.refleshTable(order.ID);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  
        
        
        //计划单跳转到工作单
        $('#create_jobOrder').click(function(){
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
        	location.href ="/jobOrder/create?order_id="+order_id+"&itemIds="+itemIds;
        })
        

     });
});