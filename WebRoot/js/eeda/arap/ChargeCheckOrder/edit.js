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
        $.post('/chargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                $("#order_no").val(order.ORDER_NO);
                $("#order_id").val(order.ID);
                $("#status").val(order.STATUS);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn,#confirmBtn,#printTotaledBtn,#printBtn,#add_charge,#exchange,#query_listCurrency,.itemEdit,.delete').attr('disabled', false);
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
		$('#confirmBtn').attr('disabled', true);
    	$('#printTotaledBtn').attr('disabled', true);
		$('#printBtn').attr('disabled', true);
		$('#add_charge').attr('disabled', true);
		$('#query_listCurrency,.itemEdit,.delete').attr('disabled', true);
    }else{
    	if(status=='新建'){
    		$('#saveBtn,#confirmBtn,#printTotaledBtn,#printBtn,#add_charge,#exchange,#query_listCurrency,.itemEdit,.delete').attr('disabled', false);
    	}else if(status=='已确认'){
            $('#cancelConfirmBtn').attr('disabled', false);
            $('#add_charge,#saveBtn,#confirmBtn,#exchange').attr('disabled', true);
        }else if(status=='取消确认'){
        	$('#cancelConfirmBtn').attr('disabled', true);
        	$('#add_charge,#saveBtn,#confirmBtn,#exchange').attr('disabled', false);
        }else{
        	$('#add_charge,#saveBtn,#confirmBtn,#exchange,#cancelConfirmBtn').attr('disabled', true);
        }
    }
    
    //确认单据
    $('#confirmBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	 $.post('/chargeCheckOrder/confirm', {id:id}, function(data){
    		 if(data){
    			 $('#cancelConfirmBtn').attr('disabled', false);
                 $('.delete,.itemEdit,#add_charge,#saveBtn,#confirmBtn,#exchange').attr('disabled', true);
                 $("#status").val('已确认');
    			 $("#confirm_name").val(data.CONFIRM_BY_NAME);
    			 $("#confirm_stamp").val(data.CONFIRM_STAMP); 
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    			 $("#cancelConfirmBtn").attr('disabled', false);
    		 }
         },'json').fail(function() {
        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    })
    
    
    //生成应收对账明细
    $('#printBtn').click(function(){
    	var order_id = $('#order_id').val();
    	$.post('/jobOrderReport/printReceiveDetailPDF',{order_id:order_id},function(data){
    		if(data){
    			$.scojs_message('生成应收对账单PDF成功', $.scojs_message.TYPE_OK);
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    //生成应收对账PDF(合计)
    $('#printTotaledBtn').click(function(){
    	var order_id = $('#order_id').val();
    	var company_name = $('#company_name').val();
    	$.post('/jobOrderReport/printTotaledReceiveDetailPDF',{order_id:order_id,company_name:company_name},function(data){
    		if(data){
    			$.scojs_message('生成应收对账单PDF成功', $.scojs_message.TYPE_OK);
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    
    //导出excel对账单
    $('#exportExcel').click(function(){
        $(this).attr('disabled', true);
        var id = $('#order_id').val();
        var sp_name = $('#company_name').val();
        $.post('/chargeCheckOrder/downloadExcelList', {id:id,sp_name:sp_name}, function(data){
            $('#exportExcel').prop('disabled', false);
            $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
            window.open(data);
        }).fail(function() {
            $('#exportExcel').prop('disabled', false);
            $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
        });
    });
    //导出excel对账单(合计)
    $('#exportTotaledExcel').click(function(){
        $(this).attr('disabled', true);
        var id = $('#order_id').val();
        var sp_name = $('#company_name').val();
        $.post('/chargeCheckOrder/downloadExcelList1', {id:id,sp_name:sp_name}, function(data){
            $('#exportExcel').prop('disabled', false);
            $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
            window.open(data);
        }).fail(function() {
            $('#exportExcel').prop('disabled', false);
            $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
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
    
    $("#cancelConfirmBtn").click(function(){
    	var order_id = $("#order_id").val();
    	$.post("/chargeCheckOrder/cancelConfirm",{order_id:order_id},function(data){
    		if(data){
    			$("#status").val('取消确认');
    			$.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
    			$('#cancelConfirmBtn').attr('disabled', true);
    			$('.delete,.itemEdit,#add_charge,#saveBtn,#confirmBtn,#exchange').attr('disabled', false);
    		}else{
    			$.scojs_message('取消确认失败', $.scojs_message.TYPE_ERROR);
    		}
    	})
    });
    
    //账单公司信息回填
    $('#own_company_list').on('click', '.fromLocationItem', function(e){
		$('#own_company_name_eng').val($(this).attr('company_name_eng')=='null'?'':$(this).attr('company_name_eng'));
		$('#own_company_tel').val($(this).attr('phone')=='null'?'':$(this).attr('phone'));
	});
  
});
});
