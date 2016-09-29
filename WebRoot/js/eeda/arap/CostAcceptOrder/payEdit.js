define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '付款申请单 | '+document.title;
	$('#pay_date').val(eeda.getDate());
	$('#menu_finance').addClass('active').find('ul').addClass('in');
		//datatable, 动态处理
	    var ids = $("#ids").val();
        var total = 0.00;
        var nopay = 0.00;
        var pay = 0.00;

		var dataTable = eeda.dt({
		    id: 'CostOrder-table',
		    paging: true,
		    serverSide: true, //不打开会出现排序不对
		    ajax: "/costPreInvoiceOrder/costOrderList?ids="+ids+"&application_id="+$("#application_id").val(),
            createdRow: function ( row, data, index ) {
                $(row).attr('id', data.ID);
                $(row).attr('item_ids', data.ITEM_IDS);
                $(row).attr('payee_unit', data.SP_ID);//收款单位
            },
		    columns:[
             {"data":"ORDER_TYPE","width": "100px","sClass":"order_type"},
             {"data":"ORDER_NO","width": "120px",
            	"render": function(data, type, full, meta) {
            		return '<a href="/costCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
        		}
             },
        	{"data":"CNAME","width": "250px"},
    		{"data":"TOTAL_AMOUNT","width": "100px",
    			"render": function(data, type, full, meta) {
					var str = parseFloat(data).toFixed(2);
					total = total + parseFloat(data);
					$("#total").html(total.toFixed(2));
					return str;
    			}
    		},
    		{"data":"PAID_AMOUNT","width": "100px",
    			"render": function(data, type, full, meta) {
					var str = parseFloat(full.TOTAL_AMOUNT-data).toFixed(2);
					nopay = nopay + parseFloat(full.TOTAL_AMOUNT-data);
					$("#nopay").html(nopay.toFixed(2));
					return str;
    			}
    		},
    		{"data":"PAID_AMOUNT","width": "100px",
    			"render": function(data, type, full, meta) {
    				if($('#application_id').val()==''){
    					var str = parseFloat(full.TOTAL_AMOUNT-data).toFixed(2);
    					pay = pay + parseFloat(full.TOTAL_AMOUNT-data);
    				}else{
    					var str = parseFloat(full.APPLICATION_AMOUNT).toFixed(2);
    					pay = pay + parseFloat(full.APPLICATION_AMOUNT);
    				}
    				$("#pay").html(pay.toFixed(2));
					$("#pay_amount").val(pay.toFixed(2));
					return "<input type ='text' name='amount' style='width:80px' id ='amount' value='"+str+"'>";
    			}
    		},
    		{"data":"CREATOR_NAME","width": "120px"},
    		{"data":"CREATE_STAMP","width": "150px"},
    		{"data":"REMARK","width": "150px"}
        ]      
    });	
    
    var orderjson = function(){
    	var array=[];
    	var sum=0.0;
    	$("#CostOrder-table input[name='amount']").each(function(){
    		var obj={};
    		obj.id = $(this).parent().parent().attr('id');
    		obj.order_type = $(this).parent().parent().find('.order_type').text();
    		obj.value = $(this).val();
    		obj.item_ids = $(this).parent().parent().attr('item_ids');
    		obj.payee_unit = $(this).parent().parent().attr('payee_unit');
    		sum+=parseFloat(obj.value);
    		array.push(obj);
    	});
    	
    	$("#total_amount").val(sum);
    	var str_JSON = JSON.stringify(array);
    	$("#detailJson").val(str_JSON);
    };
    
    
    //保存
	$("#saveBtn").on('click',function(){
		$("#saveBtn").attr("disabled", true);
		$("#printBtn").attr("disabled", true);
	
		orderjson();
	
		if($("#payment_method").val()=='transfers'){
			if($("#deposit_bank").val()=='' && $("#bank_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		$.post('/costPreInvoiceOrder/save',$("#checkForm").serialize(), function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#application_id").val(data.ID);
				$("#status").val(data.STATUS);
				$("#application_no").val(data.ORDER_NO);
				$("#application_date").val(data.CREATE_STAMP);
				$("#saveBtn").attr("disabled", false);
				$("#printBtn").attr("disabled", false);
				$("#checkBtn").attr('disabled',false);
				$("#deleteBtn").attr("disabled", false);
				eeda.contactUrl("edit?id",data.ID);
				total = 0.00;
				nopay = 0.00;
				pay = 0.00;
				
				//var url = "/costPreInvoiceOrder/costOrderList?application_id="+$("#application_id").val();
				//$('#CostOrder-table').dataTable().fnDraw();
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
		
	});
	
	
	  $("#checkBtn").on('click',function(){
		  	$("#checkBtn").attr("disabled", true);
		  	$("#saveBtn").attr("disabled", true);
		  	
		  	orderjson();
		  	
			$.get("/costPreInvoiceOrder/checkStatus", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
				if(data.ID>0){
					$("#check_name").val(data.CHECK_NAME);
					$("#check_stamp").val(data.CHECK_STAMP);
					$("#status").val(data.STATUS);
					$.scojs_message('复核成功', $.scojs_message.TYPE_OK);
					$("#returnBtn").attr("disabled", false);
					$("#confirmBtn").attr("disabled", false);
				}else{
					$("#checkBtn").attr("disabled", false);
					$.scojs_message('复核失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  
	  //撤回
	  $("#returnBtn").on('click',function(){
		  	$("#returnBtn").attr("disabled", true);
		  	if(confirm("确定撤回未复核状态？")){
		  		orderjson();
				$.get("/costPreInvoiceOrder/returnOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
					if(data.success){
						$.scojs_message('退回成功', $.scojs_message.TYPE_OK);
						$("#checkBtn").attr("disabled", false);
					  	$("#saveBtn").attr("disabled", false);
					  	$("#confirmBtn").attr("disabled", true);
					}else{
						$("#returnBtn").attr("disabled", false);
						$.scojs_message('退回失败', $.scojs_message.TYPE_FALSE);
					}
				},'json');
		  	}else{
		  		$("#returnBtn").attr("disabled", false);
		  	}
		});
	  
	  
	  //付款确认
	  $("#confirmBtn").on('click',function(){
		  	$("#confirmBtn").attr("disabled", true);
		  	orderjson();
			$.get("/costPreInvoiceOrder/confirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val(),pay_time:$('#pay_date').val(),pay_type:$('#pay_type').val(),pay_bank:$('#pay_bank').val()}, function(data){
				if(data.success){
					$("#returnBtn").attr("disabled", true);
					$("#deleteBtn").attr("disabled", true);
					$("#status").val(data.STATUS);
					$("#returnConfirmBtn").attr("disabled", false);
					$.scojs_message('付款成功', $.scojs_message.TYPE_OK);
				}else{
					$("#confirmBtn").attr("disabled", false);
					$.scojs_message('付款失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  //付款确认撤回未确认状态
	  $("#returnConfirmBtn").on('click',function(){
		  	$("#returnConfirmBtn").attr("disabled", true);
		  	if(confirm("确定撤回未付款确认状态？")){
		  		orderjson();
				$.get("/costPreInvoiceOrder/returnConfirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
					if(data.success){
						$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
					  	$("#confirmBtn").attr("disabled", false);
					  	$("#returnBtn").attr("disabled", false);
					  	$("#deleteBtn").attr("disabled", false);
					}else{
						$("#returnConfirmBtn").attr("disabled", false);
						$("#returnBtn").attr("disabled", false);
						$.scojs_message('撤回失败', $.scojs_message.TYPE_FALSE);
					}
				},'json');
		  	}else{
		  		$("#returnConfirmBtn").attr("disabled", false);
		  	}
		});
	  
	  
	  
	  //撤销单据
	  $("#deleteBtn").on('click',function(){
		  	$("#deleteBtn").attr("disabled", true);
		  	if(confirm("确定撤撤销此单据？返回到上一步重新做单？")){
		  		orderjson();
				$.get("/costPreInvoiceOrder/deleteOrder", {application_id:$('#application_id').val()}, function(data){
					if(data.success){
						$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
						setTimeout(function(){
							location.href="/costAcceptOrder";
						}, 1000);
					}else{
						$("#deleteBtn").attr("disabled", false);
						$.scojs_message('撤销失败', $.scojs_message.TYPE_FALSE);
					}
				},'json');
		  	}else{
		  		$("#deleteBtn").attr("disabled", false);
		  	}
		});
	
	
	  
	 //异步显示总金额
    $("#CostOrder-table").on('input', 'input', function(e){
		e.preventDefault();
		var value = 0.00;
		var currentValue = $(this).val();
		var $totalAmount = $(this).parent().parent().find('.yufu_amount').text();
		if(parseFloat($totalAmount)-parseFloat(currentValue)<0){
			$(this).val(0);
			$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
			return false;
		}
		$("input[name='amount']").each(function(){
			if($(this).val()!=null&&$(this).val()!=''){
				value = value + parseFloat($(this).val());
			}else{
				$("#InvorceApplication-table").on('blur', 'input', function(e){
					$(this).val(0);
				});
			}
	    });		
		$("#pay").html(value);
		$("#pay_amount").val(value);
	});	
    
   
    //按钮控制
	if($('#application_id').val()==''){
		$("#saveBtn").attr('disabled',false);
	}else{
		if($('#status').val()=='新建'){
			$("#saveBtn").attr('disabled',false);
			$("#checkBtn").attr('disabled',false);
		}else if($('#status').val()=='已复核'){
			$("#confirmBtn").attr('disabled',false);
		}
	}
	
	
	//付款方式回显（1）
	$('#payment_method').change(function(){
		var type = $(this).val();
		if(type == 'cash'){
			$('#transfers_massage').hide();
		}else{
			$('#transfers_massage').show();
		}
	})
	
	//发票类型（1）
	$('#invoice_type').change(function(){
		var type = $(this).val();
		if(type == 'wbill'){
			$('#invoiceDiv').hide();
		}else{
			$('#invoiceDiv').show();
		}
	})
	
	//付款方式回显（2）
	$('#pay_type').change(function(){
		var type = $(this).val();
		if(type == 'cash'){
			$('#pay_type_massage').hide();
		}else{
			$('#pay_type_massage').show();
		}
	})
	
	

});
});