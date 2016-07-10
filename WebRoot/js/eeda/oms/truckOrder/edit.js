define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	
    	document.title = order_no + ' | ' + document.title;
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

            var items_array = salesOrder.buildCargoDetail();
            var order = {
                id: $('#order_id').val(),
                customer_id: $('#customer_id').val(),
                type: $('#type').val(),
                status: $('#status').val()==''?'新建':$('#status').val(),
                item_list:items_array
            };

            var status = $('#status').val();
            //异步向后台提交数据
            $.post('/truckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#creator_name").val(order.CREATOR_NAME);
                    $("#create_stamp").val(order.CREATE_STAMP);
                    $("#order_id").val(order.ID);
                    $("#order_no").val(order.ORDER_NO);
                    $("#status").val(order.STATUS);
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

     });
});