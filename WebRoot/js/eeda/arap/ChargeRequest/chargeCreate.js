define(['jquery', 'metisMenu', 'sb_admin','./createStep1', './chargeEdit_select_item','dataTablesBootstrap', 
        'validate_cn', 'sco'], function ($, metisMenu, sb, createStep1Contr, selectContr) {
$(document).ready(function() {
	document.title = '收款申请单 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    $('#receive_time').val(eeda.getDate());
    
    //构造主表json
    var buildOrder = function(){
    	var item = {};
    	item.id = $('#order_id').val();
    	item.selected_ids = $('#selected_ids').val();
    	item.status='新建';
    	var orderForm = $('#orderForm input,select,textarea');
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
        	}
    	}
        return item;
    }
    //默认填上的字段
    if($('#billing_unit').val()==''){
    	$('#billing_unit').val('珠海横琴远桥供应链管理有限公司')
    }
    
    
    //第二步里面的单
    var buildItem = function(){
    	var items_array=[];
        $('#select_item_table input[type="checkbox"]:checked').each(function(){
  			var id = $(this).parent().parent().attr('id');
  			var item={};
            item.id = id;
            item.action = 'CREATE';
            items_array.push(item);
        });
        return items_array;
    }
    
	//datatable, 动态处理
    var ids = $("#ids").val();
    var selected_item_ids = $("#selected_ids").val();

    
	
    //申请保存
	$("#createSave").on('click',function(){
	
		$("#createSave").attr("disabled", true);
		if($('#check_time_begin_time').val()==""||$('#check_time_end_time').val()==""){
				$.scojs_message('业务发生月不能为空', $.scojs_message.TYPE_FALSE);
				$("#createSave").attr("disabled", false);
				return false;
				
		}
		
		if($("#payment_method").val()=='transfers'||$("#payment_method").val()=='checkTransfers'){
			if($("#deposit_bank").val()=='' && $("#account_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				$("#createSave").attr("disabled", false);
				return false;
				
			}
		}
		
		var order = buildOrder();
		order.item_list = buildItem();
		order.selected_item_ids=$("#selected_ids").val();
		order.ids=$('#ids').val();
		order.sp_id=$('#sp_id').val();
		order.sp_name=$('#sp_id_input').val();
		$.post('/chargeRequest/save',{params:JSON.stringify(order)}, function(data){
			$("#createSave").attr("disabled", false);
			if(data.ID>0){
				$.scojs_message('创建新申请成功', $.scojs_message.TYPE_OK);
				$("#order_no").val(data.ORDER_NO);
				$("#sp_id").val(data.sp_id);
				$("#sp_id_input").val(data.sp_name);
				if(confirm('刚生成的新申请单号'+data.ORDER_NO+':是否前往该申请单？')){
					self.location='/chargeRequest/edit?id='+data.ID; 
				 }

				selectContr.refleshSelectTable(data.IDSARRAY);
				createStep1Contr.refleshStep1Table();
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		 },'json').fail(function() {
	            $.scojs_message('创建新申请失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	        });
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
	 
	 //复核
	  $("#checkBtn").on('click',function(){
		  	$("#checkBtn").attr("disabled", true);
		  	$("#saveBtn").attr("disabled", true);
		  
			$.get("/chargeAcceptOrder/checkOrder", {order_id:$('#order_id').val(),}, function(data){
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
	  
	  
	  //退回
	  $("#returnBtn").on('click',function(){
		  	$("#returnBtn").attr("disabled", true);
		  	orderjson();
			$.get("/chargePreInvoiceOrder/returnOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
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
		});
	  
	  
	//撤销单据
	  $("#deleteBtn").on('click',function(){
		  	$("#deleteBtn").attr("disabled", true);
		  	if(confirm("确定撤撤销此单据？返回到上一步重新做单？")){
		  		orderjson();
				$.get("/chargePreInvoiceOrder/deleteOrder", {application_id:$('#application_id').val()}, function(data){
					if(data.success){
						$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
						setTimeout(function(){
							location.href="/chargeAcceptOrder";
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
	  
	  
	  //收款确认
	  $("#confirmBtn").on('click',function(){
		  	$("#confirmBtn").attr("disabled", true);
		  	
//		  	if($("#receive_type").val()=='transfers'){
//				if($("#receive_bank").val()==''){
//					$.scojs_message('收入银行不能为空', $.scojs_message.TYPE_FALSE);
//					return false;
//				}
//			}
			var order={};
			order.id=$('#order_id').val();
			order.receive_time=$('#receive_time').val();
			order.payment_method = $('#payment_method').val();
			order.order_type="应收对账单";
			$.get("/chargeRequest/confirmOrder", {params:JSON.stringify(order)}, function(data){
				if(data){
					$("#status").val('已收款');
					$("#returnBtn").attr("disabled", true);
					$("#returnConfirmBtn").attr("disabled", false);
					$("#deleteBtn").attr("disabled", true);
					$("#confirm_name").val(data.CONFIRM_NAME);
					$.scojs_message('收款成功', $.scojs_message.TYPE_OK);
				}else{
					$("#confirmBtn").attr("disabled", false);
					$.scojs_message('收款失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  
	  
	  
	  
	//收款确认撤回未确认状态
	  $("#returnConfirmBtn").on('click',function(){
		  	$("#returnConfirmBtn").attr("disabled", true);
		  	if(confirm("确定撤回未收款确认状态？")){
		  		orderjson();
				$.get("/chargePreInvoiceOrder/returnConfirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
					if(data.success){
						$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
					  	$("#confirmBtn").attr("disabled", false);
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
	  
	  
	  $('#select_item_table').on('click',"input[type=checkbox]",function () {
	        var table = $('#select_item_table').DataTable();
	        var row = $(this).parent().parent();
	        var cell = table.cell($(this).parent());//  td
	        
	        selectContr.calcTotal();

	        var selected_ids=[];
	        table.data().each(function(item, index) {
				var id=item.ID;
	            if(!$('#checkbox_'+id).prop('checked'))
	                return;
	            selected_ids.push(item.ID);
	        });
	        $('#selected_ids').val(selected_ids);	        
	        //获取对账单号
	    });  

	//异步显示总金额
    
 
  //按钮控制
	if($('#order_id').val()==''){
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
		if(type == 'cash'||type==""){
			$('#transfers_massage').hide();
			$('#receive_type_massage').hide();
		}else{
			$('#transfers_massage').show();
			$('#receive_type_massage').show();
		}
	})
	
	//发票类型（1）
	$('#bill_type').change(function(){
		var type = $(this).val();
		if(type == 'wbill'||type==""){
			$('#invoiceDiv').hide();
			$('#projectFee').hide();
		}else{
			$('#invoiceDiv').show();
			if(type=='ordinarybill'||type=='specialbill'){
				$('#projectFee').show();
			}else{
				$('#projectFee').hide();
			}
		}
	})
	
	
	var projectFeeType = $('#bill_type').val();
	if(projectFeeType=='ordinarybill'||projectFeeType=='specialbill'){
		$('#projectFee').show();
	}else{
		$('#projectFee').hide();
	}
	
	
	var invoice_type = $('#bill_type').val();
	if(invoice_type == 'wbill'||invoice_type==""){
			$('#invoiceDiv').hide();
		}else{
			$('#invoiceDiv').show();
		}
	
	$('#deposit_bank_list').on('mousedown','a',function(){
	   $('#account_no').val( $(this).attr('account_no'));
 	   $('#account_name').val( $(this).attr('account_name'));
 	   
    })
    
    
    

	
	
	//付款方式回显（2）
//	$('#receive_type').change(function(){
//		var type = $(this).val();
//		if(type == 'cash'){
//			$('#receive_type_massage').hide();
//		}else{
//			$('#receive_type_massage').show();
//		}
//	})
	
//	var ids = [];
//	var applied_arap_id = [];
//	var itemTable = eeda.dt({
//        id: 'charge-table',
//        columns:[
//	        {"data": "ID",
//	        	"render": function ( data, type, full, meta ) {
//	        		var str = '<input type="checkbox" style="width:30px">';
//	        		for(var i=0;i<ids.length;i++){
//	                    if(ids[i]==data){
//	                   	 str = '<input type="checkbox" style="width:30px" checked>';
//	                    }
//	                }
//	        		return str;
//			    }
//	        },
//	        { "data": "ORDER_NO"},
//	        { "data": "TYPE"},
//	        { "data": "CREATE_STAMP"},
//	        { "data": "SP_NAME"},
//	        { "data": "CURRENCY_NAME","class":"currency_name"},
//	        { "data": "TOTAL_AMOUNT","class":"total_amount",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "EXCHANGE_RATE"},
//	        { "data": "AFTER_TOTAL",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "NEW_RATE"},
//	        { "data": "AFTER_RATE_TOTAL",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "EXCHANGE_CURRENCY_NAME","class":"EXCHANGE_CURRENCY_NAME"},
//	        { "data": "EXCHANGE_CURRENCY_RATE"},
//	        { "data": "EXCHANGE_TOTAL_AMOUNT","class":"EXCHANGE_TOTAL_AMOUNT"},
//	        { "data": "ORDER_TYPE", "visible": false,
//	            "render": function ( data, type, full, meta ) {
//	                if(!data)
//	                    data='';
//	                return data;
//	            }
//	        },
//	      ]
//	});
	
	
//	$('#eeda-table').on('click','td',function(){
//		
//		$('#chargeAlert').click();
//		var order_id = $(this).parent().attr('id');
//		$('#chargeAlert').attr('name',order_id);
//		var url = "/chargeCheckOrder/tableList?order_id="+order_id;
//    	itemTable.ajax.url(url).load();
//	})
	
	
	
	

});
});