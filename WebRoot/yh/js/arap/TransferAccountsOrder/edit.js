$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	$.post('/transferAccountsOrder/getFinAccount', function(data){
		var in_filter = $("#in_filter");
		var out_filter = $("#out_filter");
		var in_hidden=$("#in_hidden").val();
		var out_hidden=$("#out_hidden").val();
		out_filter.empty();
		in_filter.empty();
		out_filter.append("<option></option>");
		in_filter.append("<option></option>");
		for(var i = 0; i < data.length; i++){
			var bank_name = data[i].BANK_NAME;
			var account_no = data[i].ACCOUNT_NO;
			var bank_person = data[i].BANK_PERSON;
			var acc_id = data[i].ID;
			if(bank_name == null){
				bank_name='';
			}
			if(account_no == null){
				account_no='';
			}
			if(bank_person == null){
				bank_person='';
			}
			if(out_hidden==data[i].ID){
				out_filter.append("<option value="+acc_id+" selected='selected'>"+bank_name+"&nbsp&nbsp&nbsp&nbsp"+account_no+"&nbsp&nbsp&nbsp&nbsp"+bank_person+"</option>");
			}
			if(in_hidden==data[i].ID){
				in_filter.append("<option value="+acc_id+" selected='selected'>"+bank_name+"&nbsp&nbsp&nbsp&nbsp"+account_no+"&nbsp&nbsp&nbsp&nbsp"+bank_person+"</option>");
			}
			out_filter.append("<option value="+acc_id+">"+bank_name+"&nbsp&nbsp&nbsp&nbsp"+account_no+"&nbsp&nbsp&nbsp&nbsp"+bank_person+"</option>");
			in_filter.append("<option value="+acc_id+">"+bank_name+"&nbsp&nbsp&nbsp&nbsp"+account_no+"&nbsp&nbsp&nbsp&nbsp"+bank_person+"</option>");
		}
	},'json');
	
	
	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
    });
	
	
	

	 $("#transfer_filter option").each(function(){ //遍历全部option
	        var txt = $(this).val(); //获取option的内容
	        var type = $("#transfer_type").val();
	        if(txt==type){
	        	$(this).attr('selected', true);
	        }
	 });
	 
	 $("#saveBtn").click(function(e){
		$("#saveBtn").attr("disabled",true);
		saveTransferOrder();
	});
	 

	//保存异步向后台提交数据
    var saveTransferOrder = function(e){
		$.post('/transferAccountsOrder/save',$("#expenseAccountForm").serialize(), function(data){
			if(data.ID>0){	
				contactUrl("edit?id",data.ID);
				$("#sorder_no").html('<strong>'+data.ORDER_NO+'<strong>');
			  	$("#create_stamp").html(data.CREATE_STAMP);
			  	$.post('/transferAccountsOrder/findUser', {"userId":data.CREATE_ID}, function(data){
 					$("#create_name").html('<strong>'+data.C_NAME+'<strong>');
 				});
			  	$("#costPreInvoiceOrderStatus").html(data.STATUS);
			  	$("#remark").val(data.REMARK);
			  	$("#transferOrderId").val(data.ID);
				$("#saveBtn").attr("disabled",false);
				$("#confirmBtn").attr("disabled",false);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
	 
	
	$("#confirmBtn").click(function(e){
		$("#confirmBtn").attr("disabled",true);
		saveTransferOrder();
		confiremBtnTransferOrder();
	});
	
	 
	//确认异步向后台提交数据
    var confiremBtnTransferOrder = function(e){
		//异步向后台提交数据
		$.post('/transferAccountsOrder/confirem',$("#expenseAccountForm").serialize(), function(data){
			if(data.ID>0){	
				contactUrl("edit?id",data.ID);
			  	$("#confirm_stamp").html(data.CREATE_STAMP);
			  	$.post('/transferAccountsOrder/findUser', {"userId":data.CREATE_ID}, function(data){
 					$("#confirm_name").html('<strong>'+data.C_NAME+'<strong>');
 				});
			  	$("#costPreInvoiceOrderStatus").html(data.STATUS);
				$.scojs_message('转账成功', $.scojs_message.TYPE_OK);
				$("#saveBtn").attr("disabled",true);
				$("#returnConfirmBtn").attr("disabled",false);
			}else{
				$.scojs_message('转账失败', $.scojs_message.TYPE_FALSE);
				$("#saveBtn").attr("disabled",false);
			}
		},'json');
	};
	
	
	
    	
    	//收款确认撤回未确认状态
    $("#returnConfirmBtn").on('click',function(){
	  	$("#returnConfirmBtn").attr("disabled", true);
	  	if(confirm("确定撤回未转账确认状态？")){
			$.get("/transferAccountsOrder/returnConfirmOrder", {id:$('#transferOrderId').val()}, function(data){
				if(data.success){
					$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
				  	$("#saveBtn").attr("disabled", false);
				}else{
					$("#returnConfirmBtn").attr("disabled", false);
					$.scojs_message('撤回失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
	  	}else{
	  		$("#returnConfirmBtn").attr("disabled", false);
	  	}
	 });
  
	 
	  
	  
	 //状态控制按钮
	 order_status=$("#costPreInvoiceOrderStatus").html();
	 if(order_status==''){
		$("#saveBtn").attr("disabled",false);
	 }else if(order_status=='新建'){
		 $("#saveBtn").attr("disabled",false);
		 $("#confirmBtn").attr("disabled",false);
	 }else if(order_status=='已确认'){
		$("#saveBtn").attr("disabled",true);
		$("#confirmBtn").attr("disabled",true);
		$("#returnConfirmBtn").attr("disabled",false);
	 }
    	
});