$(document).ready(function() {
	if(order_no){
		document.title=order_no+" | "+document.title;
	}	
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	if($("#chargePreInvoiceOrderId").val() == ""){
		$('#auditBtn').attr('disabled', true);
		$('#approvalBtn').attr('disabled', true);
	}else{
		if($("#chargePreInvoiceOrderStatus").text() == "新建"){
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', true);
		}else if($("#chargePreInvoiceOrderStatus").text() == "已审核"){
			$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', false);
		}else if($("#chargePreInvoiceOrderStatus").text() == "已审批"){
			$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
			$('#auditBtn').attr('disabled', true);
			$('#approvalBtn').attr('disabled', true);
		}
	}
	
	var savechargePreInvoiceOrder = function(e){
		$("#savechargePreInvoiceOrderBtn").attr('disabled', true);
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargePreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
		$.post('/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
			
			if(data.ID>0){
				$("#chargePreInvoiceOrderId").val(data.ID);
				$("#arapAudit_order_no").text(data.ORDER_NO);
				$("#createData").text(data.CREATE_STAMP);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#savechargePreInvoiceOrderBtn").attr('disabled', false);
				contactUrl("edit?id",data.ID);
				$('#auditBtn').attr('disabled', false);
			}else{
				 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			}
		},'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $("#savechargePreInvoiceOrderBtn").attr('disabled', false);
        });
	};
    
	// 审核
	$("#auditBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		$.post('/chargePreInvoiceOrder/auditChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
			
			if(data.arapAuditOrder.ID != null){
				$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
				$('#auditBtn').attr('disabled', true);
				$("#auditData").text(data.arapAuditOrder.AUDIT_STAMP);
				$("#chargePreInvoiceOrderStatus").html(data.arapAuditOrder.STATUS);
				if(data.user.C_NAME != null && data.user.C_NAME != ''){
					$("#auditName").text(data.user.C_NAME);
				}else{
					$("#auditName").text(data.user.USER_NAME);
				}
				
				$('#approvalBtn').attr('disabled', false);
			}
			
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		$.post('/chargePreInvoiceOrder/approvalChargePreInvoiceOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId}, function(data){
			if(data.arapAuditOrder.ID != null){
				$('#savechargePreInvoiceOrderBtn').attr('disabled', true);
				$('#auditBtn').attr('disabled', true);
				$('#approvalBtn').attr('disabled', true);
				$("#chargePreInvoiceOrderStatus").html(data.arapAuditOrder.STATUS);
				$("#approvalData").text(data.arapAuditOrder.APPROVAL_STAMP);
				if(data.user.C_NAME != null && data.user.C_NAME != ''){
					$("#approvalName").text(data.user.C_NAME);
				}else{
					$("#approvalName").text(data.user.USER_NAME);
				}
				
			}else{
				$.scojs_message('审批失败', $.scojs_message.TYPE_ERROR);
			}
		},'json');
	});
	
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "chargePreInvoiceOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargePreInvoiceOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#savechargePreInvoiceOrderBtn").click(function(e){
 		var money = $("#money").val();
 		var receive_amount = $("#chargeCheckList-table").children().children().find('input[name="receive_amount"]').val();
 		if(receive_amount == 0 || receive_amount == '' || money == null || money == ''){
 			$.scojs_message('申请金额不能为0', $.scojs_message.TYPE_FALSE);
 			$("#savechargePreInvoiceOrderBtn").attr("disabled",false);
 			return;
 		}				
 		savechargePreInvoiceOrder(e);
	});
	
	$("#chargePreInvoiceOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargePreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
        parentId = e.target.getAttribute("id");
		$.post('/chargePreInvoiceOrder/save', $("#chargePreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){
				$("#chargePreInvoiceOrderId").val(data.ID);
				$("#arapAudit_order_no").text(data.ORDER_NO);
				$("#createData").text(data.CREATE_STAMP);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	contactUrl("edit?id",data.ID);
			  	loadItem(data.ID);
			  	if("chargePreInvoiceOrderbasic" == parentId){
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		
		
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		var loadItem = function(chargePreInvoiceOrderId){
			chargeCheckListTab.fnSettings().sAjaxSource = "/chargePreInvoiceOrder/chargeOrderListByIds?chargeCheckOrderIds="+$("#chargeCheckOrderIds").val()+'&chargePreInvoiceOrderId='+chargePreInvoiceOrderId;
			chargeCheckListTab.fnDraw(); 
		};
		
		
	});
	
    if($("#chargePreInvoiceOrderStatus").text() == 'new'){
    	$("#chargePreInvoiceOrderStatus").text('新建');
	}

    var chargeCheckListTab = $('#chargeCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			$(nRow).attr('charge_amount', aData.CHARGE_AMOUNT);
			$(nRow).attr('noreceive_amount', aData.NORECEIVE_AMOUNT);
			return nRow;
		},
        "sAjaxSource": "/chargePreInvoiceOrder/chargeOrderListByIds?chargePreInvoiceOrderId="+$("#chargePreInvoiceOrderId").val(),
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(ChargeCheck.isUpdate || ChargeCheck.isAffirm){
            			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        			
        	}},
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CNAME"},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":"CHARGE_AMOUNT"},
            {"mDataProp":"NORECEIVE_AMOUNT"},
            {"mDataProp":null,
            	"fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.RECEIVE_AMOUNT == 0){
  	            		str = "<input type='text' name='receive_amount'>";
  	            	}else{
  	            		str = "<input type='text' name='receive_amount' value='"+obj.aData.RECEIVE_AMOUNT+"'>";
  	            	}
  	            	$("#money").val(obj.aData.RECEIVE_AMOUNT);
  	            	return str;
  	            }	
            },
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                       
        ]      
    });
    
    
    
    $("#chargeCheckList-table").on('blur', 'input', function(e){
		e.preventDefault();
		var chargePreInvoiceOrderId = $("#chargePreInvoiceOrderId").val();
		var chargeOrderId = $(this).parent().parent().attr("id");
		var ids = $("#chargeCheckOrderIds").val();
		var noreceive_amount = $(this).parent().parent().attr("noreceive_amount");
		var name = $(this).attr("name");
		var value = $(this).val();

		if(parseFloat(noreceive_amount) < parseFloat(value)){
			$.scojs_message('注意：此次收款金额已超过应收金额！！', $.scojs_message.FALSE);
			return;
		}else{
			$.post('/chargePreInvoiceOrder/updateArapChargeOrder', {chargePreInvoiceOrderId:chargePreInvoiceOrderId,chargeCheckOrderIds:ids ,chargeOrderId:chargeOrderId, name:name, value:value}, function(data){
				if(data.chargeApplicationOrderRel.ID > 0){
					$("#total_receive").html(data.total_receive);
					$("#total_noreceive").html(data.total_noreceive);
					$("#receive_amount").html(data.receive_amount);
					if(data.tips=='success')
						$.scojs_message('更新金额成功', $.scojs_message.TYPE_OK);
				}else{
					$.scojs_message('更新金额失败', $.scojs_message.TYPE_ERROR);
				}
	    	},'json');
		}	
	});	
    
    
    
    $.post('/chargePreInvoiceOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideAccountId){
					 accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
				 }else{
					 accountTypeSelect.append("<option value='"+data[i].ID+"'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");					 
				 }
			}
		}
	},'json');

    $("input[name='paymentMethod']").each(function(){
		if($("#paymentMethodRadio").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).val() == 'transfers'){	    		
	    		$("#accountTypeDiv").show();    		
	    	}
		}
	 }); 
    
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
    }); 
    
} );