define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN', './edit_doc_table'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
        
     
        
             	
    	//已报关行按钮状态
    	$('#confirmCompleted,#passBtn,#refuseBtn').click(function(){
    		var btnId = $(this).attr("id");
    		$(this).attr('disabled', true);
    		id = $('#order_id').val();
    		var plan_order_no = $('#order_no').val();
    		var customer_id = $('#application_company').val();
    		$.post('/customPlanOrder/confirmCompleted', {id:id,btnId:btnId,plan_order_no:plan_order_no,customer_id:customer_id}, function(order){
    				$('#status').val(order.STATUS);
    				var status = order.STATUS;
					//提交报关行按钮状态
    				if(status=="处理中"){
						$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', true);
						//审核按钮状态
						$('#passBtn').attr('disabled',false);
			        	$('#refuseBtn').attr('disabled',false);
			        	$.scojs_message('申请单提交成功', $.scojs_message.TYPE_OK);
		        	}
    				if(status=="审核通过"){
		        		$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', true);
						//审核按钮状态
						$('#passBtn').attr('disabled',true);
			        	$('#refuseBtn').attr('disabled',true);
			        	$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
			        	if(confirm('确定要前往工作单？')){
			        		location.href="/customJobOrder/edit?id="+order.JOB_ORDER_ID;
			        	}
			        	
		        	}
    				if(status=="审核不通过"){
    					$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', true);
						//审核按钮状态
						$('#passBtn').attr('disabled',true);
			        	$('#refuseBtn').attr('disabled',true);
			        	$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
    				}
		    	    },'json').fail(function() {
		    	    	if(status=="新建"){
			    	        $.scojs_message('申请单提交失败', $.scojs_message.TYPE_ERROR);
			    	        $('#confirmCompleted').attr('disabled', false);
			    	        $('#passBtn').attr('disabled',true);
				        	$('#refuseBtn').attr('disabled',true);
		    	        }
		    	    	if(status=="处理中"){
		    	    		$.scojs_message('申请单提交失败', $.scojs_message.TYPE_ERROR);
			    	        $('#confirmCompleted').attr('disabled', true);
			    	        $('#saveBtn').attr('disabled', true);
			    	        $('#passBtn').attr('disabled',false);
				        	$('#refuseBtn').attr('disabled',false);
		    	    	}
		    	      });
    	})
    	
    	
    	
    	 //提交报关行按钮状态
    	var id = $('#order_id').val();
    	var status = $('#status').val();
        if(id!=''){
        	$('#confirmCompleted').attr('disabled', false);
        }
		if(status=='处理中'){
			//提交报关行按钮状态
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled', true);
			//审核按钮状态
			$('#passBtn').attr('disabled',false);
        	$('#refuseBtn').attr('disabled',false);
        }
		
		if(status=="审核通过"||status=="审核不通过"){
			//提交报关行按钮状态
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled',true);
			//审核按钮状态
			$('#passBtn').attr('disabled',true);
        	$('#refuseBtn').attr('disabled',true);
		}
    	
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验页面中必填字段
            if(!$("#customForm").valid()){
                return;
            }
            
            $(this).attr('disabled', true);
            
            
       //获取页面数据，构造json 
            var items_array = salesOrder.buildCargoDetail();
        	var order = {};
        	order['id'] = $('#order_id').val();
        	order['note'] = $('#note').val();
        	var customForm = $('#customForm input,#customForm select,#customForm textarea');
        	for(var i = 0; i < customForm.length; i++){
        		var name = customForm[i].id;
            	var value =customForm[i].value;
            if(name){
            	if(name=="status"){
            		value =customForm[i].value==""?"新建":customForm[i].value;
            	}
            		order[name] = value;
            	
        	  }
        	}
        	order['item_list'] = items_array;
        	order.doc_list = salesOrder.buildDocItem();

//            var order = {
//            		    id: $('#order_id').val(),
//            	carrier_id: $('#carrier').val(),
//            	 deal_mode: $('#deal_mode').val(),
//                      type: $('#type').val(),
//                    remark: $('#note').val(),
//                    status: $('#status').val()==''?'新建':$('#status').val(),
//                 item_list:items_array
//                
//            };
            //异步向后台提交数据
            $.post('/customPlanOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                    $("#create_stamp").val(order.CREATE_STAMP);
                    $("#creator_name").val(order.CREATOR_NAME);
                    $("#order_id").val(order.ID);
                    $("#order_no").val(order.ORDER_NO);
                    $("#status").val(order.STATUS);
                    $("#note").val(order.NOTE);
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmCompleted').attr('disabled', false);
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