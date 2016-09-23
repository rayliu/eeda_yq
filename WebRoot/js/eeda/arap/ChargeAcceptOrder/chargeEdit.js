define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '收款申请单 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    //构造主表json
    var buildOrder = function(){
    	var item = {};
    	item.id = $('#order_id').val();
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


    var buildItem = function(){
    	var item_table_rows = $("#eeda-table tr");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index<2)
                continue;
            
            var row = item_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
            	continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            var item={};
            for(var i = 0; i < row.childNodes.length; i++){
            	var name = $(row.childNodes[i]).find('input').attr('name');
            	var value = $(row.childNodes[i]).find('input').val();
            	if(name){
            		item[name] = value;
            	}
            }
            item.id = id;
            item.action = $('#order_id').val() != ''?'UPDATE':'CREATE';
            items_array.push(item);
        }
        return items_array;
    }
    
	//datatable, 动态处理
    var idsArray = $("#idsArray").val();
    var total = 0.00;
    var nopay = 0.00;
    var pay = 0.00;
   
	var dataTable = eeda.dt({
	    id: 'eeda-table',
	    paging: true,
	    serverSide: true, //不打开会出现排序不对
	    ajax: "/chargeAcceptOrder/chargeOrderList?idsArray="+idsArray+"&application_id="+$("#order_id").val(),
	    columns:[
            {"data":"ORDER_TYPE","width": "100px","class":'order_type'},
            {"data":"ORDER_NO","width": "120px",
            	"render": function(data, type, full, meta) {
            		return '<a href="/chargeCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
        		}
            },
        	{"data":"SP_NAME","width": "120px"},
    		{"data":"TOTAL_AMOUNT","width": "100px",
    			"render": function(data, type, full, meta) {
					total = total + parseFloat(data) ;
					$("#total").html(parseFloat(total).toFixed(2));
					return data;
    			}
    		},
    		{"data":"NORECEIVE_AMOUNT","width": "100px","sClass":'yufu_amount',
    			"render": function(data, type, full, meta) {
					nopay = nopay + parseFloat(data) ;
					$("#nopay").html(parseFloat(nopay).toFixed(2));
					return data;
    			}
    		},
    		{"data":null,"width": "100px",
    			"render": function(data, type, full, meta) {
					if($('#order_id').val()==''){
						pay = pay + parseFloat(full.NORECEIVE_AMOUNT) ;
						$("#pay").html(parseFloat(pay).toFixed(2));
						$("#pay_amount").val(parseFloat(pay).toFixed(2));
						$("#total_amount").val(parseFloat(pay).toFixed(2));
						return "<input type ='text' name='amount' style='width:80px' value='"+full.NORECEIVE_AMOUNT+"'>";
					}
					else{
						if(full.NORECEIVE_AMOUNT==0){
	    					full.NORECEIVE_AMOUNT = full.NORECEIVE_AMOUNT;
	    				}
						pay = pay + parseFloat(full.RECEIVE_AMOUNT) ;
						$("#pay").html(parseFloat(pay).toFixed(2));
						$("#pay_amount").val(parseFloat(pay).toFixed(2));
						$("#total_amount").val(parseFloat(pay).toFixed(2));
						return "<input type ='text' name='amount' style='width:80px'  value='"+full.RECEIVE_AMOUNT+"'>";
					}
    			}
    		},
    		{"data":"CREATOR_NAME","width": "120px"},
    		{"data":"CREATE_STAMP","width": "150px"}
        ]      
    });	
    

    //申请保存
	$("#saveBtn").on('click',function(){
		$("#saveBtn").attr("disabled", true);
		$("#printBtn").attr("disabled", true);

	
		if($("#payment_method").val()=='transfers'){
			if($("#deposit_bank").val()=='' && $("#bank_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		
		var order = buildOrder();
		order.item_list = buildItem();
		
		$.get('/chargeAcceptOrder/save',{params:JSON.stringify(order)}, function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#order_id").val(data.ID);
				$("#order_no").val(data.ORDER_NO);
				$("#create_stamp").val(data.CREATE_STAMP);
				$("#creator_name").val(data.CREATOR_NAME);
				$("#saveBtn").attr("disabled", false);
				$("#printBtn").attr("disabled", false);
				$("#checkBtn").attr('disabled',false);
				$("#deleteBtn").attr("disabled", false);
				eeda.contactUrl("edit?id",data.ID);
				total = 0.00;
				nopay = 0.00;
				pay = 0.00;
				
				//dataTable.ajax.url("/chargeAcceptOrder/chargeOrderList?application_id="+$("#order_id").val()).load();
				
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
		  	
			$.get("/chargePreInvoiceOrder/checkStatus", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
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
	  
	  
	  //确认
	  $("#confirmBtn").on('click',function(){
		  	$("#confirmBtn").attr("disabled", true);
		  	orderjson();
			$.get("/chargePreInvoiceOrder/confirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val(),receive_time:$('#receive_time').val(),receive_type:$('#receive_type').val(),receive_bank:$('#receive_bank').val()}, function(data){
				if(data.success){
					$("#returnBtn").attr("disabled", true);
					$("#returnConfirmBtn").attr("disabled", false);
					$("#deleteBtn").attr("disabled", true);
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
	
	  
	 //异步显示总金额
    $("#eeda-table").on('input', 'input', function(e){
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
		$("#pay").html(parseFloat(value).toFixed(2));
		$("#pay_amount").val(parseFloat(value).toFixed(2));
		$("#total_amount").val(parseFloat(value).toFixed(2));
	});	
    
 

    
   
    //按钮控制
	if($('#status').val()==''){
		$("#saveBtn").attr('disabled',false);
		$("#deleteBtn").attr("disabled", true);
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
	}else if($('#status').val()=='已收款'){
		$("#returnConfirmBtn").attr('disabled',false);
		$("#printBtn").attr('disabled',false);
		$("#deleteBtn").attr("disabled", true);
	}
	

});
});