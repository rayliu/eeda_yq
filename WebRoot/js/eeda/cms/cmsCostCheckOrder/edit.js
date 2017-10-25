define(['jquery', 'metisMenu', 'sb_admin','pageguide','./edit_item_table','./edit_receiptItem_table', 'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

$(document).ready(function() {
	tl.pg.init({
        pg_caption: '本页教程'
    });
    
	//校验
    $("#orderForm").validate({
    	rules:{
    		remark:{
    			maxlength:30
    		}
    	}
    })
    //校验
    $("#confirmForm").validate({
    	rules:{
    		account_no:{
    			number:true,
    			maxlength:30
    		},
    		account_name:{
    			maxlength:30
    		},
    		invoice_no:{
    			maxlength:255
    		}
    	},
    	messages: {
    		account_no:{
    			number:"请输入合法的银行账户",
    			maxlength: $.validator.format( "请输入长度最多是 {0} 银行账户" )
    		},
    	}
    })
	
    $(function(){
      if(!$('#receive_cny').val()){
            $('#receive_cny').val($('#total_amount').val());
            $('#residual_cny').val($('#total_amount').val());
        }
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
        var formRequired = 0;
        $('form').each(function(){
        	if(!$(this).valid()){
        		formRequired++;
            }
        })
        if(formRequired>0){
        	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
        	return;
        }
        
        $(this).attr('disabled', true);
        
        var order = buildOrder();
        order.have_invoice = $('input[type="radio"]:checked').val();
        order.id = $('#order_id').val();
        order.item_list = itemOrder.buildItemDetail();
        order.currency_list = buildCurJson();
        
        //异步向后台提交数据
        $.post('/cmsCostCheckOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_time").val(order.CREATE_STAMP);
                $("#order_no").val(order.ORDER_NO);
                $("#order_id").val(order.ID);
                $("#status").val(order.STATUS);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
                $('#confrimBtn').attr('disabled', false);
                $('#printTotaledBtn').attr('disabled', false);
                $('#printBtn').attr('disabled', false);   
                $('#add_cost').attr('disabled', false);
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
    	$('#add_cost').attr('disabled', true);
    }else{
    	if(status=='新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confrimBtn').attr('disabled', false);
    		$('#printTotaledBtn').attr('disabled', false);
    		$('#printBtn').attr('disabled', false); 
        	$('#add_cost').attr('disabled', false);
    	}
        if(status == "已退单"||status == "已确认"){
            $('#refuseBtn').attr('disabled', true);
        }
    }
    
    //确认单据
    $('#confrimBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	 $.post('/cmsCostCheckOrder/confirm', {id:id}, function(data){
    		 if(data){
    			 $('#saveBtn').attr('disabled', true);
    			 $('#confirmBtn').attr('disabled', false);
    			 $("#status").val('已确认');
    			 $("#confirm_name").val(data.CONFIRM_BY_NAME);
    			 $("#confirm_time").val(data.CONFIRM_STAMP); 
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
 			 
    		 }
         },'json').fail(function() {
        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    })
    
       //删除按钮动作
     $("#refuseBtn").click(function(){
         var id = $('#order_id').val();
         $('#delete_id').val(id);
         $('#deleteReasonDetailAlert').click();
     }) 
     $("#deleteReasonDetail").on('click', '.deleteReason', function(){
         $('#deleteReason').val($(this).val());
     })
      $("#deleteReasonDetail").on('click', '.confirm', function(){
          if(!$("#deleteReasonDetailForm").valid()){
              return;
          }
          var id = $('#delete_id').val();
          var deleteReason = $('#deleteReason').val();
         $.post('/cmsCostCheckOrder/returnOrder', {id:id,delete_reason:deleteReason}, function(data){
             $('#deleteReasonDetail .return').click();
             $.scojs_message('退单成功', $.scojs_message.TYPE_OK);
             $('#confrimBtn').attr('disabled', true);
             $('#refuseBtn').attr('disabled', true);
         },'json').fail(function() {
             $.scojs_message('退单失败', $.scojs_message.TYPE_ERROR);
         });
     });

    
    //打印应收对账明细
    $('#printBtn').click(function(){
    	var order_id = $('#order_id').val();
    	$.post('/jobOrderReport/printReceiveDetailPDF',{order_id:order_id},function(data){
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
    	$.post('/jobOrderReport/printTotaledReceiveDetailPDF',{order_id:order_id},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });



    //显示付款确认
    var status = $("#status").val();
    var audit_status = $("#audit_status").val();
      if(status!="新建"){
    	  if(audit_status!="已付款"){
                 $('#confirmBtn').attr('disabled',false);
              }else{
                  $('#confirmBtn').attr('disabled',true);
              }
    	  }
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
      $("#confirmBtn,#badBtn").on('click',function(){
            var confirmVal =$(this).text();
            if(confirmVal=='坏账确认'){
                var pay_remark =$('#pay_remark').val()+'\n 这笔为坏账'
                $('#pay_remark').html(pay_remark);              
              } 
            $("#badBtn").attr("disabled", true);  
            $("#confirmBtn").attr("disabled", true);  
           
            var formRequired=0;
            $('form').each(function(){
                if(!$(this).valid()){
                    formRequired++;
                }
            })
            if($('#receive_time').val()==''){
                 formRequired++;
            }
            if(formRequired>0){
                $.scojs_message('收款时间为必填字段', $.scojs_message.TYPE_ERROR);
                $("#confirmBtn").attr("disabled", false);
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
            $.get("/cmsCostCheckOrder/confirmOrder", {params:JSON.stringify(order),application_id:$('#order_id').val(),confirmVal:confirmVal}, function(data){
                if(data){
                    var residual_cny=$('#residual_cny').val();//未收
                    var receive_cny=$('#receive_cny').val();
                    $('#residual_cny').val(parseFloat(residual_cny - receive_cny).toFixed(2));
                    $('#receive_cny').val(parseFloat(residual_cny - receive_cny).toFixed(2));

                    $("#returnBtn").attr("disabled", true);
                    $("#returnConfirmBtn").attr("disabled", false);
                    $("#deleteBtn").attr("disabled", true);
                    $("#confirm_name").val(data.CONFIRM_NAME);
                    itemOrder.refleshReciveTable($('#order_id').val());
                    if(confirmVal=="坏账确认"){
                        $("#status").val('该笔为坏账');
                        $.scojs_message('确认坏账成功', $.scojs_message.TYPE_OK);
                    }else{
                        $("#status").val(data.STATUS);
                        $("#audit_status").val(data.STATUS);
                        $("#confirmBtn").attr("disabled", false);
                        $.scojs_message('确认付款成功', $.scojs_message.TYPE_OK);
                    }
                }else{
                    $("#confirmBtn").attr("disabled", false);
                    $("#badBtn").attr("disabled", false);
                    $.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
                }
            },'json');
        });
   

     //构造主表json
    var buildConfirmFormOrder = function(){
        var item = {};
        item.custom_charge_order_id = $('#order_id').val();
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
