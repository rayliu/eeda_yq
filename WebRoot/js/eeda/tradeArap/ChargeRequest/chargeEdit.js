﻿define(['jquery', 'metisMenu', 'sb_admin','./createStep1', './chargeEdit_select_item', './edit_doc_table','dataTablesBootstrap', 
        'validate_cn', 'sco'], function ($, metisMenu, sb, createStep1Contr, selectContr) {
$(document).ready(function() {
	document.title = '收款申请单 | '+document.title;
	$("#breadcrumb_li").text('收款申请单');

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    if($('#receive_time').val()==""){
    	
    }
    $('.row.search_panel').hide();
    
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
    
    //刷createTable, 动态处理
    var order_id=$('#order_id').val();
    if(order_id!=''){
    	selectContr.refleshCreateTable(order_id);
    }
    
	//datatable, 动态处理
    var ids = $("#ids").val();
    var selected_item_ids = $("#selected_ids").val();
    
	//保留两位小数 , 要保留两位小数，要处理数据分割符，处理数据分割符不能保存
//	var refleshNum = function(numValue){
//		var numbleValue = eeda.numFormat(parseFloat(numValue).toFixed(2),3);
//		return numbleValue;
//	}
//	var currency=new Array('cny','usd','jpy','hkd')
//	var comfirm_modal=['modal_','comfirm_modal_'];
//	for(var j=0;j<comfirm_modal.length;j++ ){
//		var bianliang=comfirm_modal[j];
//		for(var i=0;i<currency.length;i++){
//			var cujh=currency[i];
//			var stringNum=bianliang+cujh;
//			var modal_cujh= $('#'+stringNum).val();
//			$('#'+stringNum).val(refleshNum(modal_cujh));
//		}
//	}
	
	
    //申请保存
	$("#saveBtn").on('click',function(){
		
		
		$("#createSave").attr("disabled", true);
		if($('#check_time_begin_time').val()==""||$('#check_time_end_time').val()==""){
				$.scojs_message('业务发生月不能为空', $.scojs_message.TYPE_FALSE);
				$("#createSave").attr("disabled", false);
				return false;
				
		}
		
		if($("#payment_method").val()=='transfers'||$("#payment_method").val()=='checkTransfers'){
			if($("#deposit_bank").val()=='' && $("#account_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		$("#saveBtn").attr("disabled", true);
		var order = buildOrder();
		order.item_list = buildItem();
		order.selected_item_ids=$("#selected_ids").val();
		order.ids=$('#ids').val();
		$.post('/tradeChargeRequest/save',{params:JSON.stringify(order)}, function(data){
			$("#saveBtn").attr("disabled", false);
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#order_id").val(data.ID);
				$("#order_no").val(data.ORDER_NO);
				$("#create_stamp").val(data.CREATE_STAMP);
				$("#creator_name").val(data.CREATOR_NAME);

//				eeda.contactUrl("edit?id",data.ID);
				//dataTable.ajax.url("/chargeAcceptOrder/chargeOrderList?application_id="+$("#order_id").val()).load();
//				itemOrder.refleshDocTable(data.ID);
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		 },'json').fail(function() {
	            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	        });
	});
	
	
    //打印
	 $("#printBtn").on('click',function(){
	    	var order_no = $("#application_no").val();
	    	if(order_no != null && order_no != ""){
		    	$.post('/tradeJobOrderReport/printPayMent', {order_no:order_no}, function(data){
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
	  $("#checkBtn,#cancelcheckBtn").on('click',function(){
		    var selfId=$(this).attr('id');
		  	var order_no = $("#order_no").val();
		  	var creator_name = $("#creator_name").val();
		  	var invoice_no = $("#invoice_no").val();
		  	
			$.get("/tradeChargeRequest/checkOrder", {order_id:$('#order_id').val(),selfId:selfId,order_no:order_no,creator_name:creator_name,invoice_no:invoice_no}, function(data){
				if(data.ID>0){
					$("#check_name").val(data.CHECK_NAME);
					$("#check_stamp").val(data.CHECK_STAMP);
					$("#status").val(data.STATUS);
					var status = data.STATUS;
					$("#status_cancel").val(status);
					if(status=="复核不通过"){
						$("#checkBtn").attr("disabled", false);
						$.scojs_message('复核不通过', $.scojs_message.TYPE_OK);
						$("#cancelcheckBtn").attr("disabled", true);
						$("#saveBtn").attr("disabled", false);
						$("#returnBtn").attr("disabled", true);
						$("#confirmBtn").attr("disabled", true);
						$("#badBtn").attr("disabled", true);
						$("#add_cost").attr("disabled", false);
						$('#select_item_table .delete').attr("disabled", false);
					}else{
						$("#checkBtn").attr("disabled", true);
						$.scojs_message('复核成功', $.scojs_message.TYPE_OK);
						$("#cancelcheckBtn").attr("disabled", false);
						$("#checkBtn").attr("disabled", true);
						$("#saveBtn").attr("disabled", true);
						$("#confirmBtn").attr("disabled", false);
						$("#badBtn").attr("disabled", false);
						$("#add_cost").attr("disabled", true);
						$('#select_item_table .delete').attr("disabled", true);
						$("#returnBtn").attr("disabled", false);
					}
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
				  	$("#badBtn").attr("disabled", true);
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
		            $.scojs_message('确认时间为必填字段', $.scojs_message.TYPE_ERROR);
		            $("#confirmBtn").attr("disabled", false);
		            $("#badBtn").attr("disabled", false);
		            return;
		        }

			var order={};
	  		order.id=$('#order_id').val();
			order.receive_time=$('#receive_time').val();
			order.receive_bank_id=$('#deposit_bank').val();
			order.payment_method = $('#payment_method').val();
			order.pay_remark = $('#pay_remark').val();
			$.get("/tradeChargeRequest/confirmOrder", {params:JSON.stringify(order),application_id:$('#order_id').val(),confirmVal:confirmVal}, function(data){
				if(data){
					$("#returnBtn").attr("disabled", true);
					$("#returnConfirmBtn").attr("disabled", false);
					$("#deleteBtn").attr("disabled", true);
					$("#confirm_name").val(data.CONFIRM_NAME);
					if(confirmVal=="坏账确认"){
						$("#status").val('该笔为坏账');
						$.scojs_message('确认坏账成功', $.scojs_message.TYPE_OK);
					}else{
						$("#status").val('已收款');
						$.scojs_message('收款成功', $.scojs_message.TYPE_OK);
					}
				}else{
					$("#confirmBtn").attr("disabled", false);
					$("#badBtn").attr("disabled", false);
					$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	//收款确认撤回未确认状态
	  $("#returnConfirmBtn").on('click',function(){
		  	$("#returnConfirmBtn").attr("disabled", true);
		  	if(confirm("确定撤回未收款确认状态？")){
		  		orderjson();
				$.post("/chargePreInvoiceOrder/returnConfirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
					if(data.success){
						$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
					  	$("#confirmBtn").attr("disabled", false);
					  	$("#badBtn").attr("disabled", false);
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
	        var pay_flag='N';
	        if($(this).prop('checked')==true){
	            pay_flag='Y';
	        }
	        //注意 - call draw() 更新table.data()中的数据
	        cell.data(pay_flag).draw();
	        selectContr.calcTotal();

	        var selected_ids=[];
	        table.data().each(function(item, index) {
	            if(item.PAY_FLAG == 'N')
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
			$("#cancelcheckBtn").attr("disabled", false);
		}else if($('#status').val()=='已复核'){
			$("#checkBtn").attr("disabled", true);
			$("#cancelcheckBtn").attr("disabled", false);
			$("#checkBtn").attr("disabled", true);
			$("#saveBtn").attr("disabled", true);
			$("#confirmBtn").attr("disabled", false);
			$("#badBtn").attr("disabled", false);
			$("#add_cost").attr("disabled", true);
			$('#select_item_table .delete').attr("disabled", true);
			$("#returnBtn").attr("disabled", false);
		}else if($('#status').val()=='复核不通过'){
			$("#checkBtn").attr("disabled", false);
			$("#cancelcheckBtn").attr("disabled", true);
			$("#saveBtn").attr("disabled", false);
			$("#returnBtn").attr("disabled", true);
			$("#confirmBtn").attr("disabled", true);
			$("#badBtn").attr("disabled", true);
			$("#add_cost").attr("disabled", false);
			$('#select_item_table .delete').attr("disabled", false);
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
			$('#projectFee').show();
		}
	});
	
	
	var projectFeeType = $('#bill_type').val();
	if(projectFeeType == 'wbill'||projectFeeType==""){
		$('#projectFee').hide();
		$('#invoiceDiv').hide();
	}else{
		$('#projectFee').show();
		$('#invoiceDiv').show();
	}
	
	$('#deposit_bank_list').on('mousedown','a',function(){
	   $('#account_no').val( $(this).attr('account_no'));
 	   $('#account_name').val( $(this).attr('account_name'));
 	   
    })
    

	$('#chargeAccept_table').on('click , input[type="checkbox"]',function(){
		var idsArray=[];
      	$('#chargeAccept_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
      			
//      			var order_type = $(this).parent().parent().find(".order_type").text();
      			idsArray.push(itemId);
      	});
      		$('#ids').val(idsArray);
      		selectContr.refleshSelectTable(idsArray);
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