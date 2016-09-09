define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	document.title = ' | ' + document.title;
        $('#menu_charge').addClass('active').find('ul').addClass('in');
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                cost_amount: $('#charge_amount').text(),
                sp_id: $('#sp_id').val(),
                begin_time:$('#begin_time').val(),
                end_time:$('#end_time').val()
            };

            //异步向后台提交数据
            $.post('/chargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.CHARGE.ID){
                	$('#id').val(order.CHARGE.ID);
                	$('#sp_id').val(order.CHARGE.SP_ID);
                	$('#order_no').text(order.CHARGE.ORDER_NO);
                	$('#status').text(order.CHARGE.STATUS);
                	$('#create_stamp').text(order.CHARGE.CREATE_STAMP);
                	$('#remark').text(order.CHARGE.REMARK);
                	$('#company').text(order.CHARGE.SP_NAME);
                	$('#cost_amount').text(order.CHARGE.CHARGE_AMOUNT);
                	$('#begin_time').val(order.CHARGE.BEGIN_TIME);
                	$('#end_time').val(order.CHARGE.END_TIME);
                	$('#login_user').text(order.CHARGE.LOGINUSER);
                    
                    eeda.contactUrl("edit?id",order.CHARGE.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    
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