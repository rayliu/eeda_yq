define(['jquery', 'metisMenu', 'sb_admin','./edit_item_table',  'dataTablesBootstrap','sco','pageguide','validate_cn'], function ($, metisMenu) { 

$(document).ready(function() {
	tl.pg.init({
        pg_caption: '本页教程'
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
    
    //退单按钮动作
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
        $.post('/tradeChargeCheckOrder/returnOrder', {id:id,delete_reason:deleteReason}, function(data){
            $('#deleteReasonDetail .return').click();
            $('#return_reason').val($("#deleteReason").val());
            $('#status').val("已退单");
            $('#confirmBtn,#refuseBtn').attr('disabled', true);
            $('#saveBtn').attr('disabled', false);
            $.scojs_message('退单成功', $.scojs_message.TYPE_OK);
        },'json').fail(function() {
            $.scojs_message('退单失败', $.scojs_message.TYPE_ERROR);
        });
    });
    
    //------------save
    $('#saveBtn').click(function(e){
    	$("#status").val("新建");
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
        $.post('/tradeChargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                $("#order_no").val(order.ORDER_NO);
                $("#order_id").val(order.ID);
                $("#status").val(order.STATUS);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
                $('#confirmBtn').attr('disabled', false);
                $('#refuseBtn').attr('disabled', false);
                $('#add_charge').attr('disabled', false);  
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
    	$('#query_listCurrency').prop('disabled',true);
        $('#printBtn,#printTotaledBtn').attr('disabled', true);
    }else{
    	if(status=='新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confirmBtn').attr('disabled', false); 
    		$('#refuseBtn').attr('disabled', false);
    		$('.itemEdit,.delete,#add_charge,#exchange,#query_listCurrency').attr('disabled', false);
    	}else if(status=='已退单'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confirmBtn').attr('disabled', true);
    		$('#refuseBtn').attr('disabled', true);
    		$('.itemEdit,.delete,#add_charge,#exchange,#query_listCurrency').attr('disabled', false);
    	}else if(status == "已确认"){
    		$('#cancelConfirmBtn').attr('disabled', false);
    		$('#saveBtn').attr('disabled', true);
    		$('#confirmBtn').attr('disabled', true); 
            $('#refuseBtn').attr('disabled', true);
            $('.itemEdit,.delete,#add_charge,#exchange').attr('disabled', true);
        }else if(status == "取消确认"){
        	$('#cancelConfirmBtn').attr('disabled', true);
    		$('#saveBtn').attr('disabled', false);
    		$('#confirmBtn').attr('disabled', false); 
            $('#refuseBtn').attr('disabled', false);
            $('.itemEdit,.delete,#add_charge,#exchange,#query_listCurrency').attr('disabled', false);
        }else{
        	$('.delete,.itemEdit,#add_charge,#refuseBtn,#exchange').prop('disabled',true);
        	$('#saveBtn,#confirmBtn,#cancelConfirmBtn').prop('disabled',true);
    		$('#query_listCurrency').prop('disabled',false);
        }
    }
    
    //确认单据
    $('#confirmBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	 $.post('/tradeChargeCheckOrder/confirm', {id:id}, function(data){
    		 if(data){
    			 $('#saveBtn').attr('disabled', true);
                 $('.delete,.itemEdit,#add_charge,#refuseBtn,#exchange').attr('disabled', true);
                 $("#status").val('已确认');
                 $('#cancelConfirmBtn').attr('disabled', false);
    			 $("#confirm_name").val(data.CONFIRM_BY_NAME);
    			 $("#confirm_stamp").val(data.CONFIRM_STAMP); 
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
 			 
    		 }
         },'json').fail(function() {
        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    })
    
    //取消确认单据
        $("#cancelConfirmBtn").click(function(){
    	var id = $("#order_id").val();
    	$.post("/tradeChargeCheckOrder/cancelConfirm",{id:id},function(data){
    		if(data){
    			$("#status").val('取消确认');
    			$.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
    			$('#cancelConfirmBtn').attr('disabled', true);
    			$('#confirmBtn').attr('disabled', false);
    			$('#saveBtn').attr('disabled', false);
    			$('.delete,.itemEdit,#refuseBtn,#add_charge,#exchange').attr('disabled', false);
    		}else{
    			$.scojs_message('取消确认失败', $.scojs_message.TYPE_ERROR);
    		}
    	})
    });
    //打印应收对账明细
    $('#printBtn').click(function(){
    	var order_id = $('#order_id').val();
    	$.post('/tradeJobOrderReport/printReceiveDetailPDF',{order_id:order_id},function(data){
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
    	$.post('/tradeJobOrderReport/printTotaledReceiveDetailPDF',{order_id:order_id,company_name:company_name},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    
    //结算金额汇总取两位小数
    var refleshNum = function(numValue){
		var numbleValue = parseFloat(numValue).toFixed(2);
		return numbleValue;
	}
	var currency=new Array('cny','usd','jpy','hkd')
		for(var i=0;i<currency.length;i++){
			var cujh=currency[i];
			var stringNum=cujh;
			var cujh= $('#'+stringNum).val();
			$('#'+stringNum).val(refleshNum(cujh));
		}
    

    
    $('input[type=radio]').on('click',function(){
    	var checked = $(this).val();
    	
    	if(checked=='Y'){
    		$('#invoice_flag').show();
    	}else{
    		$('#invoice_flag').hide();
    	}
    });
   
  
});
});
