define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 
        'validate_cn', 'sco', 'datetimepicker_CN', 'jq_blockui', 'dtColReorder'], function ($, metisMenu) { 
    // $.blockUI({ 
    //     message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/></h4>' 
    // });
    $("#breadcrumb_li").text('计划订单');

    $(document).ready(function() {

    	document.title = order_no + ' | ' + document.title;
        $('#menu_order').addClass('active').find('ul').addClass('in');

      //按钮状态
    	var id = $('#order_id').val();
    	var status = $('#status').val();
    	var submit_flag = $('#submit_flag').val();
        if(id==''){
        	$('#confirmCompleted').attr('disabled', true);
        }else{
    		if(status=='已完成'){
    			$('#confirmCompleted').attr('disabled', true);
    			$('#saveBtn').attr('disabled', true);
    		}
    		if(submit_flag=='Y'){
    			$('#saveBtn').attr('disabled', true);
    			$('#submitBtn').attr('disabled', true);
    		}
        }


    	//已完成计划单确认
    	$('#confirmCompleted').click(function(){
            $.blockUI({ 
                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
            });
    		$('#confirmCompleted').attr('disabled', true);
    		id = $('#order_id').val();
    		$.post('/planOrder/confirmCompleted', {id:id}, function(data){
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
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $.blockUI({ 
                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
            });
            $(this).attr('disabled', true);

            var items_array = salesOrder.buildCargoDetail();
            var order = {
                id: $('#order_id').val(),
                customer_id: $('#customer_id').val(),
                type: $('#type').val(),
                remark: $('#note').val(),
                entrusted_id: $('#entrusted_id').val(),
                to_entrusted_id: $('#to_entrusted_id').val(),
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
                    $('#confirmCompleted').attr('disabled', false);

                    $.unblockUI();
                    //异步刷新明细表
                    salesOrder.refleshTable(order.ID);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);

                    $.unblockUI();
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            });
        });  
        
        
        //计划单跳转到工作单
        $('#create_jobOrder').click(function(){
        	this.diabled = true;
        	var order_id = $('#order_id').val();
        	var itemIds=[];
        	$('#cargo_table input[type="checkbox"]').each(function(){
        		var checkbox = $(this).prop('checked');
        		if(checkbox){
        			var itemId = $(this).parent().parent().attr('id');
        			itemIds.push(itemId);
        		}
        	});
        	if(itemIds.length>1){
        		$.scojs_message('只能勾选一条明细创建工作单', $.scojs_message.TYPE_ERROR);
        		this.diabled = false;
        		return false;
        	}
        		
        	
        	location.href ="/jobOrder/create?order_id="+order_id+"&itemIds="+itemIds;
        })
        
        
         $('#submitBtn').click(function(){
        	 this.disabled = true;
         	 var order_id = $('#order_id').val();
         	 var to_entrusted_id =  $('#to_entrusted_id').val();
         	 if(to_entrusted_id==''){
         		 $.scojs_message('被委托方不能为空', $.scojs_message.TYPE_ERROR);
         		 this.disabled = false;
         		 return false;
         	 }
         	 
         	 $.blockUI({ 
                 message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
             });
         	 $.post('/planOrder/submitOrder',{order_id:order_id},function(data){
         		 if(data){
         			 $('#saveBtn').attr('disabled', true);
         			 $.scojs_message('提交成功', $.scojs_message.TYPE_OK);
         			 $.unblockUI();
         		 }
         	 }).fail(function() {
                 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                 $('#submitBtn').attr('disabled', false);
                 $.unblockUI();
             });
        	 
         })


     });
});