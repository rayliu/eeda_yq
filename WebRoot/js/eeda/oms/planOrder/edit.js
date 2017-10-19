define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'pageguide',
        'validate_cn', 'sco', 'datetimepicker_CN', 'jq_blockui', 'dtColReorder'], function ($, metisMenu) { 
    $(document).ready(function() {

        tl.pg.init({
            pg_caption: '本页教程'
        });
        
        //按钮状态
    	var id = $('#order_id').val();
    	var status = $('#status').val();
    	var submit_flag = $('#submit_flag').val();
        if(id==''){
        	$('#confirmCompleted').attr('disabled', true);
        }else{
    		if(status=='已完成'){
    			$('#confirmCompleted').attr('disabled', true);
//    			$('#saveBtn').attr('disabled', true);
    		}
    		if(submit_flag=='Y'){
//    			$('#saveBtn').attr('disabled', true);
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
    	            salesOrder.refleshTabl(id);
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
            var formRequired = 0;
            $('#orderForm').each(function(){
            	if(!$(this).valid()){
            		formRequired++;
                }
            })
            errorlength = $("[class=error_span]").length;
            var loc_id = $($(".error_span").get(0)).parent().parent().parent().parent().attr('id');
            
            if(formRequired>0||errorlength>0){
            	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
            	location.hash="#"+loc_id;
            	return;
            }
            /*//提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }*/
            
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
                begin_plan_time: $('#plan_time_begin_time').val(),
                end_plan_time: $('#plan_time_end_time').val(),
                to_entrusted_id: $('#to_entrusted_id').val(),
                
                self_party_id: $('#self_party_id').val(),
                to_party_id: $('#to_party_id').val(),
                
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
         			 salesOrder.refleshTable(order_id);
         		 }
         		$.unblockUI();
         	 }).fail(function() {
                 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                 $('#submitBtn').attr('disabled', false);
                 $.unblockUI();
             });
        	 
         })
         
         
         //按钮控制
         var login_office_type = $('#login_office_type').val();
         if(login_office_type == 'forwarderCompany'){
        	 $('#allShipmentBtn').hide();
        	 $('#submitBtn').hide();
             $('#confirmCompleted').hide();
             $('#add_cargo').hide();
         }
         
         //校验
         $('#note').on("blur",function(){
     		var data = $.trim($(this).val());
     		var len = data.length;
     		var re = /^.{255,}$/;
     		if(re.test(data)&&len!=0){
     			$(this).parent().append("<span style='color:red;' class='error_span'>请输入长度255以内的字符串</span>")
     		}
     	});
        $("#note").on("focus",function(){
     		$(this).parent().find("span").remove();
     	});

     });
});