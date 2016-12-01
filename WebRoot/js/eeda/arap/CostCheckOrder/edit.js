define(['jquery', 'metisMenu', 'sb_admin', './edit_item_table', 'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	  
        var order_no = $('#order_no').val();
        if(order_no){
            document.title = order_no + ' | ' + document.title;
        }else{
            document.title = '创建应付对账单 | ' + document.title;
        }
        
        var buildCurJson = function(){
        	var items_array=[];
        	$('#currencyDiv ul li').each(function(){
        		var item={};
        		var new_rate = $(this).find('[name=new_rate]').val();
        		var rate = $(this).find('[name=rate]').val();
        		var currency_id = $(this).find('[name=new_rate]').attr('currency_id');
        		var rate_id = $(this).find('[name=new_rate]').attr('rate_id');
        		if(new_rate==''){
        			new_rate = rate;
        		}
        		
        		item.new_rate = new_rate;
        		item.rate = rate;
        		item.rate_id = rate_id;
        		item.currency_id = currency_id;
        		item.order_type = 'cost';
        		items_array.push(item);
        	})
        	return items_array;
        }
        
       
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                total_amount: 0,//parseFloat($('#total_amount').val()).toFixed(2),
                cost_amount: 0,//$('#cost_amount').val(),
                sp_id: $('#sp_id').val(),
                begin_time:$('#audit_begin_time').val(),
                end_time:$('#audit_end_time').val(),
                usd:$('#usd').val(),
                cny:$('#cny').val(),
                hkd:$('#hkd').val(),
                jpy:$('#jpy').val()
            };
            order.currency_list = buildCurJson();

            //异步向后台提交数据
            $.post('/costCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID){
                	$('#id').val(order.ID);
                	$('#sp_id').val(order.SP_ID);
                	$('#order_no').val(order.ORDER_NO);
                	$('#status').val(order.STATUS);
                	$('#creator').val(order.CREATOR_NAME);
                	$('#create_stamp').val(order.CREATE_STAMP);
                	$('#company').text(order.SP_NAME);
                	$('#cost_amount').text(order.COST_AMOUNT);
                	$('#audit_begin_time').val(order.BEGIN_TIME);
                	$('#audit_end_time').val(order.END_TIME);
                    
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    $('#exchange').attr('disabled',false);
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
        var status = $('#status').val();
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
        		 if(data){
	    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
	    			 $('#saveBtn').attr('disabled', true);
	    			 $(this).attr('disabled', true);
	    			 $('#deleteBtn').attr('disabled', false);
	    			 $('#confirm_name').val(data.CONFIRM_BY_NAME);
	    			 $('#confirm_stamp').val(data.CONFIRM_STAMP);
        		 }
	         },'json').fail(function() {
	        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                 $(this).attr('disabled', false);
                 $('#saveBtn').attr('disabled', false);
                 $('#deleteBtn').attr('disabled', true);
	           });
        })
        
        //应付对账单打印明细
        $('#printBtn').click(function(){
        	var order_id = $('#order_id').val();
        	$.post('/jobOrderReport/payableDetailPDF',{order_id:order_id},function(data){
        		if(data){
        			window.open(data);
        		}else{
        			$.scojs_message('生成应付对账单 PDF失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        	
        });
        
        
});
});