define(['jquery', 'metisMenu', 'sb_admin','./edit_item_table','./edit_receiptItem_table',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

$(document).ready(function() {

    $(function(){
            var status=$('#status').val();
            charge_confirmBtn(status);
    });
    
    //构造主表json
    var buildOrder = function(){
    	var item = {};
    	var orderForm = $('#orderForm input,select,textarea');
    	for(var i = 0; i < orderForm.length; i++){
    		var name = orderForm[i].id;
        	var value =orderForm[i].value;
        	if(name){
        		if(name.indexOf("begin_time") != -1){
        			name = "begin_time";
        		}else if(name.indexOf("end_time") != -1){
        			name = "end_time"
        		}
        		item[name] = value;
        	}
    	}
        return item;
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
    		item.order_type = 'charge';
    		items_array.push(item);
    	})
    	return items_array;
    }
    
    //------------save
    $('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }
        
        $(this).attr('disabled', true);

        var order = buildOrder();
        order.have_invoice = $('input[type="radio"]:checked').val();
        order.id = $('#order_id').val();
        order.item_list = itemOrder.buildItemDetail();
        order.currency_list = buildCurJson();
        
        //异步向后台提交数据
        $.post('/transChargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                $("#order_no").val(order.ORDER_NO);
                $("#order_id").val(order.ID);
                $("#status").val(order.STATUS);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                if($('#status').val()=='新建'&&$('#order_id').val()){
                     $('#add_charge').attr('disabled', false);
                }
                $('#saveBtn').attr('disabled', false);
                $('#confirmBtn').attr('disabled', false);
                $('#printTotaledBtn').attr('disabled', false);
                $('#printBtn').attr('disabled', false);                 
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


    //按钮控制
    var order_id = $("#order_id").val();
    var status = $("#status").val()
    if(order_id==""){
    	$('#saveBtn').attr('disabled', false);
    	
    }else{
    	if(status=='新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confirmBtn').attr('disabled', false);
    	}else if(status=='已确认'){
    		$('#saveBtn').attr('disabled', true);
    		$('#confirmBtn').attr('disabled', true);
    		$('#cancelConfirmBtn').attr('disabled', false);
        }else if(status=='取消确认'){
        	$('#cancelConfirmBtn').attr('disabled', true);
        	$('#saveBtn').attr('disabled', false);
    		$('#confirmBtn').attr('disabled', false);
    		$('#add_charge').attr('disabled', false);
    		$('.delete').attr('disabled', false);
        }
    }
    
    //确认单据
    $('#confirmBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	 $.post('/transChargeCheckOrder/confirm', {id:id}, function(data){
    		 if(data){
    			 $('#saveBtn').attr('disabled', true);
                 $('.delete').attr('disabled', true);
                 $('#add_charge').attr('disabled', true);
                 $('#cancelConfirmBtn').attr('disabled', false);
                 $("#status").val('已确认');
                 $('#confirmOrder_div').show();
                 $('#costCheckreceipt').show();
                 //赋值给本次付款，收款余额
                 var cny=$('#cny').val().trim();
                 $('#receive_cny').val(cny);
                 $('#residual_cny').val(cny);
                 $('#charge_confirmBtn').attr('disabled',false);

                 $('#printTotaledBtn').attr('disabled', false);
    			 $("#confirm_name").val(data.CONFIRM_BY_NAME);
    			 $("#confirm_stamp").val(data.CONFIRM_STAMP); 

                 $("#total_amount").val($('#cny').val());
                 $("#receive_cny").val($('#cny').val());
                 $("#residual_cny").val($('#cny').val());
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
 			 
    		 }
         },'json').fail(function() {
        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    });
  
  //取消确认单据
    $('#cancelConfirmBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	var action = "cancelConfirm";
    	 $.post('/transChargeCheckOrder/confirm', {id:id,action:action}, function(data){
    		 if(data){
    			 $('#saveBtn').attr('disabled', false);
    			 $('#confirmBtn').attr('disabled', false);
                 $('.delete').attr('disabled', false);
                 $('#add_charge').attr('disabled', false);

                 $("#status").val('取消确认');
                 $('#confirmOrder_div').hide();
                 $('#costCheckreceipt').hide();

    			 $("#confirm_name").val("");
    			 $("#confirm_stamp").val(""); 

                 $("#total_amount").val("");
                 $("#receive_cny").val("");
                 $("#residual_cny").val("");
    			 $.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
 			 
    		 }
         },'json').fail(function() {
        	 $.scojs_message('取消确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    });

    //下载应收对账明细
    $('#exportBtn').click(function(){
    	var order_id = $('#order_id').val();
    	var company_name = $('#company_name').val();
    	$.post('/transChargeCheckOrder/downloadList',{order_id:order_id,company_name:company_name},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    //打印应收对账明细
    $('#printTotaledBtn').click(function(){
    	var order_id = $('#order_id').val();
    	var company_name = $('#company_name').val();
    	$.post('/jobOrderReport/printTotaledReceiveDetailPDF',{order_id:order_id,company_name:company_name},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    

    
    $('input[type=radio]').on('click',function(){
    	var checked = $(this).val();
    	
    	if(checked=='Y'){
    		$('#invoice_flag').show();
    	}else{
    		$('#invoice_flag').hide();
    	}
    });
  
    //付款方式回显（1）
    $('#payment_method').change(function(){
        var type = $(this).val();
        var status=$('#status').val();
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
            // $("#badBtn").attr("disabled", true);  
            // $("#charge_confirmBtn").attr("disabled", true);  
           
            var formRequired=0;
            var receive_cny=parseFloat($('#receive_cny').val());//本次收款CNY大于0
            var residual_cny=parseFloat($('#residual_cny').val());//未收余额CNY大于0
            if(receive_cny<=0 ){
                $.scojs_message('收款金额应大于0', $.scojs_message.TYPE_ERROR);
                return;
            }
            if(receive_cny> residual_cny){
                $.scojs_message('本次收款金额CNY大于未收的余额CNY', $.scojs_message.TYPE_ERROR);
                return;
            }
            if(residual_cny==0 ){
                $.scojs_message('该账单已完成收款', $.scojs_message.TYPE_ERROR);
                return;
            }
            if($('#receive_time').val()==''){
                 formRequired++;
            }
            if(formRequired>0){
                $.scojs_message('收款时间为必填字段', $.scojs_message.TYPE_ERROR);
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
            order.itemids=buildItemIDs();
            $.get("/transChargeCheckOrder/confirmOrder", {params:JSON.stringify(order),application_id:$('#order_id').val(),confirmVal:confirmVal}, function(data){
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
                        $.scojs_message('确认收款成功', $.scojs_message.TYPE_OK);
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
        item.total_amount=$('#cny').val();
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
           $('#add_charge').attr('disabled', false);
       }else if(status=='已确认'){
           $('#charge_confirmBtn').attr('disabled',false);
           $('#add_charge').attr('disabled', true);
           $('#confirmOrder_div').show();
           $('#costCheckreceipt').show();
       }else if(status=='部分已收款'){
           $('#charge_confirmBtn').attr('disabled',false);
           $('#add_charge').attr('disabled', true);
           $('#confirmOrder_div').show();
           $('#costCheckreceipt').show();
       }else if(status=='已收款'){
           $('#charge_confirmBtn').attr('disabled',true);
           $('#add_charge').attr('disabled',true );
           $('#confirmOrder_div').show();
           $('#costCheckreceipt').show();
       }
    }

    var buildItemIDs=function(){
        var item =[];
        var str="";
        $('#eeda-table tbody tr').each(function(){
            var id=$(this).attr('id');
            if(id){
                item.push(id);
            }
        })
        str=item.join(',');
         return str;
    }
  
});
});
