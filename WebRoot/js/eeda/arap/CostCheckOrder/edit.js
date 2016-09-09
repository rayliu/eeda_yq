define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	document.title = ' | ' + document.title;
        $('#menu_cost').addClass('active').find('ul').addClass('in');
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                total_amount: parseFloat($('#total_amount').text()).toFixed(2),
                payee_id: $('#sp_id').val(),
                begin_time:$('#begin_time').val(),
                end_time:$('#end_time').val()
            };

            //异步向后台提交数据
            $.post('/costCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.COST.ID){
                	$('#id').val(order.COST.ID);
                	$('#sp_id').val(order.COST.SP_ID);
                	$('#order_no').text(order.COST.ORDER_NO);
                	$('#status').text(order.COST.STATUS);
                	$('#create_stamp').text(order.COST.CREATE_STAMP);
                	$('#remark').text(order.COST.REMARK);
                	$('#company').text(order.COST.SP_NAME);
                	$('#cost_amount').text(order.COST.COST_AMOUNT);
                	$('#begin_time').val(order.COST.BEGIN_TIME);
                	$('#end_time').val(order.COST.END_TIME);
                	$('#login_user').text(order.LOGINUSER);
                    
                    eeda.contactUrl("edit?id",order.COST.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    
                    //异步刷新明细表
                    
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  
        
        //按钮状态
        var status = $('#status').text();
        if(status=='新建'){
        	$('#confirmBtn').attr('disabled', false);
        }else if(status=='已确认'){
        	$('#saveBtn').attr('disabled', true);
        	$('#confirmBtn').attr('disabled', true);
        	$('#deleteBtn').attr('disabled', false);
        }
        
        $('#confirmBtn').click(function(){
        	$(this).attr('disabled', true);
        	var id = $('#id').val();
        	 $.post('/costCheckOrder/confirm', {id:id}, function(data){
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    			 $('#saveBtn').attr('disabled', true);
    			 $(this).attr('disabled', true);
    			 $('#deleteBtn').attr('disabled', false);
	         },'json').fail(function() {
	        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                 $(this).attr('disabled', false);
                 $('#saveBtn').attr('disabled', false);
                 $('#deleteBtn').attr('disabled', true);
	           });
        })
        
});
});