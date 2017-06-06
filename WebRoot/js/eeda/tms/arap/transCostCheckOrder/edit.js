define(['jquery', 'metisMenu', 'sb_admin', './edit_item_table','./edit_receiptItem_table', 'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	  
        var order_no = $('#order_no').val();
        if(order_no){
            document.title = order_no + ' | ' + document.title;
        }else{
            document.title = '创建应付对账单 | ' + document.title;
        }
        $("#breadcrumb_li").text('应付对账单');
        
        $(function(){
	            var status=$('#status').val();
	            charge_confirmBtn(status);
//	          if($('#receive_cny').val()==''){
//	                $('#receive_cny').val($('#cny').val());
//	                $('#residual_cny').val($('#cny').val());
//	            }
       
          });
        
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
                car_id: $('#car_id').val(),
                begin_time:$('#audit_begin_time').val(),
                end_time:$('#audit_end_time').val(),
                usd:$('#usd').val(),
                cny:$('#cny').val(),
                hkd:$('#hkd').val(),
                jpy:$('#jpy').val()
            };
            order.currency_list = buildCurJson();

            //异步向后台提交数据
            $.post('/transCostCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID){
                	$('#id').val(order.ID);
                	$('#order_id').val(order.ID);
                	$('#sp_id').val(order.SP_ID);
                	$('#order_no').val(order.ORDER_NO);
                	$('#status').val(order.STATUS);
                	$('#creator').val(order.CREATOR_NAME);
                	$('#create_stamp').val(order.CREATE_STAMP);
                	$('#sp_name').text(order.COMPANY_NAME);
                	$('#cost_amount').text(order.COST_AMOUNT);
                	$('#audit_begin_time').val(order.BEGIN_TIME);
                	$('#audit_end_time').val(order.END_TIME);
                    
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    $('#exchange').attr('disabled',false);
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
        
        //按钮状态
        var status = $('#status').val();
        var order_id = $("#order_id").val();
         if(order_id==""){
                $('#saveBtn').attr('disabled', false);
                
            }else{
                if(status=='新建'){
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    $('#printBtn').attr('disabled', false);         
                }else if(status=='已确认'){
                    $('#add_cost').attr("disabled",true);
                    $('.delete').attr("disabled",true);
                    }
            }
        
        $('#confirmBtn').click(function(){
        	$(this).attr('disabled', true);
        	var id = $('#id').val();
        	 $.post('/transCostCheckOrder/confirm', {id:id}, function(data){
        		 if(data){
	    			 $('#saveBtn').attr('disabled', true);
                     $('#printBtn').attr('disabled', true);
                     $('.delete').attr('disabled', true);
                     $('#add_cost').attr('disabled', true);

                     $("#status").val(data.STATUS);
	    			 $('#confirm_name').val(data.CONFIRM_BY_NAME);
	    			 $('#confirm_stamp').val(data.CONFIRM_STAMP);
                     $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
            		 $('#confirmOrder_div').show();
           		   	 $('#costCheckreceipt').show();
            		 //赋值给本次付款，收款余额
                     var cny=$('#cny').val().trim();
                     $('#receive_cny').val(cny);
                     $('#residual_cny').val(cny);
                     $('#charge_confirmBtn').attr('disabled',false);
        		 }
	         },'json').fail(function() {
	        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                 $(this).attr('disabled', false);
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

    //付款方式回显（1）
    $('#payment_method').change(function(){
        var type = $(this).val();
        if(type == 'cash'||type==""){
            $('#transfers_massage').hide();
            $('#receive_type_massage').hide();
            $('#transfers_massage_pay').hide();
        }else{
            $('#transfers_massage').show();
            $('#receive_type_massage').show();
            $('#transfers_massage_pay').show();
        }
    })

      //付款确认
      $("#charge_confirmBtn,#badBtn").on('click',function(){
            var confirmVal =$(this).text();
            if(confirmVal=='坏账确认'){
                var pay_remark =$('#pay_remark').val()+'\n 这笔为坏账'
                $('#pay_remark').html(pay_remark);              
              } 
//            $("#badBtn").attr("disabled", true);  
//            $("#charge_confirmBtn").attr("disabled", true);  
           
            var formRequired=0;
            var receive_cny=$('#receive_cny').val();//本次付款CNY大于0
            var residual_cny=$('#residual_cny').val();//未付余额CNY大于0
            if(receive_cny<=0 ){
            	$.scojs_message('付款金额应大于0', $.scojs_message.TYPE_ERROR);
            	return;
            }
            if(receive_cny> residual_cny){
            	$.scojs_message('本次付款金额CNY大于未付的余额CNY', $.scojs_message.TYPE_ERROR);
            	return;
            }
            if(residual_cny==0 ){
            	$.scojs_message('该账单已完成付款', $.scojs_message.TYPE_ERROR);
            	return;
            }
            if($('#receive_time').val()==''){
                 formRequired++;
            }
            if(formRequired>0){
                $.scojs_message('付款时间为必填字段', $.scojs_message.TYPE_ERROR);
                $("#charge_confirmBtn").attr("disabled", false);
                $("#badBtn").attr("disabled", false);
                return;
            }


            var order={};
            // // order.id=$('#order_id').val();
            // order.receive_time=$('#receive_time').val();
            // order.receive_bank_id=$('#deposit_bank').val();
            // order.payment_method = $('#payment_method').val();
            // order.pay_remark = $('#pay_remark').val();
            order=buildConfirmFormOrder();
            $.get("/transCostCheckOrder/confirmOrder", {params:JSON.stringify(order),application_id:$('#order_id').val(),confirmVal:confirmVal}, function(data){
                if(data){
                    var residual_cny=$('#residual_cny').val();//未收
                    var receive_cny=$('#receive_cny').val();
                    $('#residual_cny').val(parseFloat(residual_cny - receive_cny).toFixed(2));
                    $('#receive_cny').val(parseFloat(residual_cny - receive_cny).toFixed(2));

                    $("#returnBtn").attr("disabled", true);
                    $("#returnConfirmBtn").attr("disabled", false);
                    $("#deleteBtn").attr("disabled", true);
                    $("#charge_confirm_name").val(data.CONFIRM_NAME);
                    itemOrder.refleshReciveTable($('#order_id').val());
                    if(confirmVal=="坏账确认"){
                        $("#status").val('该笔为坏账');
                        $.scojs_message('确认坏账成功', $.scojs_message.TYPE_OK);
                    }else{
                        $("#status").val(data.STATUS);
                        $("#audit_status").val(data.STATUS);
                        $.scojs_message('确认付款成功', $.scojs_message.TYPE_OK);
                    }
            }else{
                    $("#charge_confirmBtn").attr("disabled", false);
                    $("#badBtn").attr("disabled", false);
                    $.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
                }
            },'json');
        });
    

     //构造主表json
    var buildConfirmFormOrder = function(){
        var item = {};
        item.charge_order_id = $('#order_id').val();
        item.total_amount=$('#total_amount').val();
        item.confirm_by=$('#user_id').val();
        // item.selected_ids = $('#selected_ids').val();
        item.status='新建';
        item.currency_id=3;
        var orderForm = $('#confirmForm input,select,textarea');
        for(var i = 0; i < orderForm.length; i++){
            var name = orderForm[i].id;
            var value =orderForm[i].value;
            if(name){
                if(name.indexOf("check_time_begin") != -1){
                    name = "begin_time";
                }else if(name.indexOf("check_time_end") != -1){
                    name = "end_time"
                }
                item[name] = value;
                if(name.indexOf("residual_cny")!=-1){
                    item[name]=$('#residual_cny').val()-$('#receive_cny').val();
                }
            }
        }
        return item;
    }

    var charge_confirmBtn=function(status){
 	   var cny=$('#receive_cny').val();
 	   if(status=='新建'){
 		   $('#charge_confirmBtn').attr('disabled',true);
 		   $('#add_cost').attr('disabled', false);
 	   }else if(status=='已确认'){
 		   $('#charge_confirmBtn').attr('disabled',false);
 		   $('#add_cost').attr('disabled', true);
 		   $('#confirmOrder_div').show();
 		   $('#costCheckreceipt').show();
 	   }else if(status=='部分已付款'){
 		   $('#charge_confirmBtn').attr('disabled',false);
  		   $('#add_cost').attr('disabled', true);
  		   $('#confirmOrder_div').show();
 		   $('#costCheckreceipt').show();
 	   }else if(status=='已付款'){
 		   $('#charge_confirmBtn').attr('disabled',true);
  		   $('#add_cost').attr('disabled',true );
  		   $('#confirmOrder_div').show();
 		   $('#costCheckreceipt').show();
 	   }
    }
});
});