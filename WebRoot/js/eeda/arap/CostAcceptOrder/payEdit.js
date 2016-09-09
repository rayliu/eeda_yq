define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '付款申请单 | '+document.title;
    if(type!=""){
		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
		  $('#menu_finance').addClass('active').find('ul').addClass('in');
	  }else{
		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		$('#menu_finance').addClass('active').find('ul').addClass('in');
	  }
   

	//datatable, 动态处理
    var ids = $("#ids").val();
    var total = 0.00;
    var nopay = 0.00;
    var pay = 0.00;
    var datatable = $('#CostOrder-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bPaginate" : false, //显示分页器
        "bSort": false, // 不要排序
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costPreInvoiceOrder/costOrderList?ids="+ids+"&application_id="+$("#application_id").val(),
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID});
			$(nRow).attr({item_ids:aData.ITEM_IDS});
			$(nRow).attr({payee_unit:aData.PAYEE_UNIT});
		},
        "aoColumns": [   
                      
             {"mDataProp":"ORDER_TYPE","sWidth": "100px","sClass":'order_type'},
             {"mDataProp":"ORDER_NO","sWidth": "120px",
            	"fnRender": function(obj) {
            		return eeda.getUrlByNo(obj.aData.ID,obj.aData.ORDER_NO);
        		}
             },
        	{"mDataProp":"CNAME","sWidth": "250px"},
        	{"mDataProp":"PAYEE_NAME","sWidth": "120px"},
    		{"mDataProp":"COST_AMOUNT","sWidth": "100px",
    			"fnRender": function(obj) {
					total = total + parseFloat(obj.aData.COST_AMOUNT) ;
					$("#total").html(total);
					return obj.aData.COST_AMOUNT;
    			}
    		},
    		{"mDataProp":"YUFU_AMOUNT","sWidth": "100px","sClass":'yufu_amount',
    			"fnRender": function(obj) {
					nopay = nopay + parseFloat(obj.aData.YUFU_AMOUNT) ;
					$("#nopay").html(nopay);
					return obj.aData.YUFU_AMOUNT;
    			}
    		},
    		{"mDataProp":null,"sWidth": "100px",
    			"fnRender": function(obj) {
					if($('#application_id').val()==''){
						pay = pay + parseFloat(obj.aData.YUFU_AMOUNT) ;
						$("#pay").html(pay);
						$("#pay_amount").val(pay);
						return "<input type ='text' name='amount' style='width:80px' id ='amount' value='"+obj.aData.YUFU_AMOUNT+"'>";
					}
					else{
						if(obj.aData.PAY_AMOUNT==0){
	    					obj.aData.PAY_AMOUNT = obj.aData.YUFU_AMOUNT;
	    				}
						pay = pay + parseFloat(obj.aData.PAY_AMOUNT) ;
						$("#pay").html(pay);
						$("#pay_amount").val(pay);
						return "<input type ='text' name='amount' style='width:80px' id ='amount' value='"+obj.aData.PAY_AMOUNT+"'>";
					}
    			}
    		},
    		{"mDataProp":"CREATOR_NAME","sWidth": "120px"},
    		{"mDataProp":"CREATE_STAMP","sWidth": "150px"},
    		{"mDataProp":"REMARK","sWidth": "150px"}
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
    
    
    
    

    //申请保存
  //付款确认
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
		$.get('/costPreInvoiceOrder/save',$("#checkForm").serialize(), function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#application_id").val(data.ID);
				$("#application_no").val(data.ORDER_NO);
				$("#application_date").val(data.CREATE_STAMP);
				$("#saveBtn").attr("disabled", false);
				$("#printBtn").attr("disabled", false);
				$("#checkBtn").attr('disabled',false);
				$("#deleteBtn").attr("disabled", false);
				contactUrl("edit?id",data.ID);
				total = 0.00;
				nopay = 0.00;
				pay = 0.00;
				datatable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costOrderList?application_id="+$("#application_id").val();;
				datatable.fnDraw();
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
		
	});
	
    //打印
	 $("#printBtn").on('click',function(){
	    	var order_no = $("#application_no").val();
	    	if(order_no != null && order_no != ""){
		    	$.post('/report/printPayMent', {order_no:order_no}, function(data){
		    		if(data.indexOf(",")>=0){
						var file = data.substr(0,data.length-1);
		    			var str = file.split(",");
		    			for(var i = 0 ;i<str.length;i++){
		    				window.open(str[i]);
		    			}
					}else{
						window.open(data);
					}
		    	});
	    	}else{
	    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
	    	}
	    	

	    });
	  $("#checkBtn").on('click',function(){
		  	$("#checkBtn").attr("disabled", true);
		  	$("#saveBtn").attr("disabled", true);
		  	
		  	orderjson();
		  	
			$.get("/costPreInvoiceOrder/checkStatus", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
				if(data.ID>0){
					$("#check_name").val();
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
    
 
    
    var payment = function(){
    	if($('#payment_method').val()=='transfers'){
    		$("#transfers_massage").show();
    	}else if($('#payment_method').val()=='cash'){
    		$("#deposit_bank").val('');
    		$("#bank_no").val('');
    		$("#account_name").val('');
    		$("#transfers_massage").hide();
    	}
    }; 	
    
  //付款方式文本框控制
    $('#payment_method').on('change',function(){
    	payment();
    });
    
    
    
    var payType = function(){
    	if($('#pay_type').val()=='cash'){
    		$("#pay_bank").val('');
    		$("#pay_type_massage").hide();
    	}else{
    		$("#pay_type_massage").show();
    	}
    };
    //付款方式（付款确认）控制
    $('#pay_type').on('change',function(){
    	payType();
    });
    
   
    //按钮控制
	if($('#status').val()=='new'){
		$("#saveBtn").attr('disabled',false);
		$("#deleteBtn").attr('disabled',true);
	}else if($('#status').val()=='新建' || $('#status').val()=='已审批'){
		if($('#application_id').val()!=''){
			$("#saveBtn").attr('disabled',false);
			$("#printBtn").attr('disabled',false);
			$("#checkBtn").attr('disabled',false);
		}
	}else if($('#status').val()=='已复核'){
		$("#printBtn").attr('disabled',false);
		$("#returnBtn").attr('disabled',false);
		$("#confirmBtn").attr('disabled',false);
	}else if($('#status').val()=='已付款'){
		$("#deleteBtn").attr('disabled',true);
		$("#printBtn").attr('disabled',false);
		$("#returnConfirmBtn").attr('disabled',false);
	}

	//开票类型控制
	 var clean = function(){
		$('#billing_unit').val('');
 		$('#payee_unit').val('');
 		$('#payee_name').val('');
 		$('#account_name').val('');
 		$('#payee_name').attr('disabled',false);
 		$('#account_name').attr('disabled',false);
	 };
	 $('#payment_method').on('change',function(){
		 var value = $('#payment_method').val();
		 if(value=="cash"){
			 $('#payee_name').attr("readonly",false);
		 }else{
			 $('#payee_name').attr("readonly","readonly");
		 }
	 });
	 $('#invoice_type').on('change',function(){
		var value = $('#invoice_type').val();
    	if(value=='wbill'){
    		clean();
    		$('#account_name').attr("readonly",false);
	    	$('#payee_unit').attr("readonly",false);
	    	$('#payee_name').attr("readonly",false);
    		$('#payment_method').val('cash');
    	}else if(value=='mbill'){
    		clean();
    		$('#payment_method').val('transfers');
   		 	payment();
    		$('#account_name').val($('#payee_filter').val());
    		$('#payee_unit').val($('#payee_filter').val());
    		$('#payee_name').val("");
    		$('#account_name').attr("readonly","readonly");
    		$('#payee_unit').attr("readonly","readonly");
    		var method = $('#payment_method').val();
    		if(method=="cash"){
   			 $('#payee_name').attr("readonly",false);
   		    }else{
   			 $('#payee_name').attr("readonly","readonly");
   		 }
    	}else if(value=='dbill'){
    		clean();
    		$('#payee_unit').val($('#account_name').val());
    		$('#account_name').attr("readonly","readonly");
	    	$('#payee_unit').attr("readonly","readonly");
	    	var method = $('#payment_method').val();
	    	if(method=="cash"){
				 $('#payee_name').attr("readonly",false);
			 }else{
				 $('#payee_name').attr("readonly","readonly");
			 }
    		$('#billing_unit').on('input',function(){
    			 $('#payment_method').val('transfers');
    			 payment();
    		 });
    	}
	 });
	 $('#billing_unit').on('input',function(){
		 if($('#invoice_type').val()=='dbill'){
			$('#payment_method').val('transfers');
			payment();
			$('#payee_unit').val($('#billing_unit').val());
			$('#account_name').val($('#billing_unit').val());
			$('#payee_name').val(""); 
		 } 
	 });
	 
	 

	


    //回显
    //付款类型
    $('#payment_method').val($('#payment_method_show').val());
    
    //开票类型
    if($('#invoice_type_show').val()=='')
    	$('#invoice_type').val('wbill');
    else{
    	$('#invoice_type').val($('#invoice_type_show').val());
    }
    if($('#invoice_type').val()=='wbill'){
 		$('#account_name').attr("readonly",false);
	    $('#payee_unit').attr("readonly",false);
	    $('#payee_name').attr("readonly",false);
 	}else if($('#invoice_type').val()=='mbill'){
 		$('#account_name').attr("readonly","readonly");
 		$('#payee_unit').attr("readonly","readonly");
 		$('#payee_name').attr("readonly","readonly");
 	}else if($('#invoice_type').val()=='dbill'){
 		$('#account_name').attr("readonly","readonly");
	    $('#payee_unit').attr("readonly","readonly");
	    $('#payee_name').attr("readonly","readonly");
 	}
    ////付款方式（付款确认）回显控制
    if($('#pay_type_show').val()==''){
    	 $('#pay_type').val('cash');
    }else{
    	 $('#pay_type').val($('#pay_type_show').val());
    }
    payType();
    
    
    //付款银行回显
    $('#pay_bank').val($('#pay_banks').val());
    
    if($('#deposit_bank').val()!='' || $('#bank_no').val()!='' || $('#account_name').val()!=''){
    	$('#payment_method').val('transfers');
    }else{
    	$('#payment_method').val('cash');
    	payment();
    }
});
});