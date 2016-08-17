$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_cost').addClass('active').find('ul').addClass('in');
	
	if(order_status=='新建'){
		$("#saveCostPreInvoiceOrderBtn").attr("disabled",false);
		$("#auditBtn").attr("disabled",true);
		$("#approvalBtn").attr("disabled",true);
	}else if(order_status=='已审核'){
		$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
		$("#auditBtn").attr("disabled",true);
		$("#approvalBtn").attr("disabled",false);
	}else if(order_status=='已审批'){
		$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
		$("#auditBtn").attr("disabled",true);
		$("#approvalBtn").attr("disabled",true);
		$("#printBtn").attr("disabled",false);
	}else if(order_status=='已复核'){
		$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
		$("#auditBtn").attr("disabled",true);
		$("#approvalBtn").attr("disabled",true);
		$("#printBtn").attr("disabled",false);
		$("#payConfirmBtn").attr("disabled",false);
	}else if(order_status=='已付款确认'){
		$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
		$("#auditBtn").attr("disabled",true);
		$("#approvalBtn").attr("disabled",true);
		$("#printBtn").attr("disabled",false);
		$("#payConfirmBtn").attr("disabled",true);
	}
	
	var invoiceNoArr=[];
	var saveCostPreInvoiceOrder = function(e){
		$("#account_id").val($("#account").val());
		$("#bank_name").val($("#bank_name1").val());
		$("#bank_no").val($("#bank_no1").val());
		$("#payee_unit").val($("#make_collections").val());
		$("#billing").val($("#billing_unit").val());
		$("#paymentMethod").val($("input[name='paymentMethod']:checked").val());
		$("#billtype").val($("input[name='payment']:checked").val());
		$("#num_name").val($("#num_name1").val());
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costPreInvoiceOrderForm").valid()){
	       	return;
        }

		//异步向后台提交数据
		$.post('/costPreInvoiceOrder/save',$("#costPreInvoiceOrderForm").serialize(), function(data){
			if(data.ID>0){	
				contactUrl("edit?id",data.ID);
				$("#sorder_no").html('<strong>'+data.ORDER_NO+'<strong>');
			  	$("#create_stamp").html(data.CREATE_STAMP);
			  	$("#remark").val(data.REMARK);
				$("#costPreInvoiceOrderId").val(data.ID);
				$("#auditBtn").attr("disabled",false);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",false);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			}else{
				alert('数据保存失败。');
			}
		},'json');
	};
	//供应商下拉列表
	/*$('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/costPreInvoiceOrder/sp_filter_list', {input:inputStr}, function(data){
			console.log(data);
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var company_name = data[i].COMPANY_NAME;
				var company_id=data[i].ID;
				if(company_name == null){
					company_name='';
				}
				spList.append("<li><a tabindex='-1' class='fromLocationItem' company_id="+company_id+">"+company_name+" </a></li>");
			}
		},'json');
		
		spList.css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });		 
			spList.show();
    });*/
	/*$('#sp_filter').on('blur', function(){
 		$('#spList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});*/

	// 选中供应商
	
	/*$('#spList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
		$('#sp_id').val($(this).attr('company_id'));
        $('#spList').hide();
    });*/
	//收款单位
	$('#make_collections').on('keyup click', function(){
		var inputStr = $('#make_collections').val();
		var collectionsList =$("#collectionsList");
		$.get('/costPreInvoiceOrder/sp_filter_list', {input:inputStr}, function(data){
			collectionsList.empty();
			for(var i = 0; i < data.length; i++){
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name='';
				}
				collectionsList.append("<li><a tabindex='-1' class='fromLoca'>"+company_name+" </a></li>");
			}
		},'json');
		
		collectionsList.css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });		 
			collectionsList.show();	 
    });
	$('#make_collections').on('blur', function(){
 		$('#collectionsList').hide();
 	});
	$('#collectionsList').on('blur', function(){
 		$('#collectionsList').hide();
 	});

	$('#collectionsList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	$('#collectionsList').on('mousedown', '.fromLoca', function(e){
		var message = $(this).text();
		$('#make_collections').val(message.substring(0, message.indexOf(" ")));
        $('#collectionsList').hide();
    });
	
	//开票单位
	$('#billing_unit').on('keyup click', function(){
		payment();
		var inputStr = $('#billing_unit').val();
		var billingList =$("#billingList");
		$.get('/costPreInvoiceOrder/sp_filter_list', {input:inputStr}, function(data){
			billingList.empty();
			for(var i = 0; i < data.length; i++){
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name='';
				}
				billingList.append("<li><a tabindex='-1' class='fromLo'>"+company_name+" </a></li>");
			}
		},'json');
		
		billingList.css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });		 
		billingList.show();	 
    });
	$('#billing_unit').on('blur', function(){
 		$('#billingList').hide();
 	});
	$('#billingList').on('blur', function(){
 		$('#billingList').hide();
 	});

	$('#billingList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	
	$('#billingList').on('mousedown', '.fromLo', function(e){
		var message = $(this).text();
		$('#billing_unit').val(message.substring(0, message.indexOf(" ")));
        $('#billingList').hide();
        payment();
    });
	
	// 审核
	$("#auditBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/auditCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.arapAuditOrder.ID>0){
				$("#audit_name").html(data.ul.C_NAME);
				$("#audit_stamp").html(data.arapAuditOrder.AUDIT_STAMP);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
				$("#auditBtn").attr("disabled",true);
				$("#approvalBtn").attr("disabled",false);
				$("#costPreInvoiceOrderStatus").text("已审核");
			}else{
				alert('数据审核失败。');
			}
		},'json');
	});
	
	// 审批
	$("#approvalBtn").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/approvalCostPreInvoiceOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.arapAuditOrder.ID>0){
				$("#approver_name").html(data.ul.C_NAME);
				$("#approval_stamp").html(data.arapAuditOrder.APPROVAL_STAMP);
				$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
				$("#auditBtn").attr("disabled",true);
				$("#approvalBtn").attr("disabled",true);
				$("#printBtn").attr("disabled",false);
				$("#costPreInvoiceOrderStatus").text("已审批");
			}else{
				alert('数据审批失败。');
			}
			
		},'json');
		
	});
//	if($("#costPreInvoiceOrderStatus").text()=="已确认"){
//		$("#auditBtn").attr("disabled",false);
//		$("#approvalBtn").attr("disabled",false);
//	}else if($("#costPreInvoiceOrderStatus").text()=="新建" || $("#costPreInvoiceOrderStatus").text()=="new"){
//		var str=$("#costPreInvoiceOrderId").val();
//		if(str !=null && str !=""){
//			$("#auditBtn").attr("disabled",false);
//		}
//	}else{
//		$("#auditBtn").attr("disabled",false);
//		$("#approvalBtn").attr("disabled",false);
//		$("#printBtn").attr("disabled",false);
//		if($("#costPreInvoiceOrderStatus").text()=="已付款确认"){
//			$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
//			$("#auditBtn").attr("disabled",true);
//			
//			$("#approvalBtn").attr("disabled",true);
//		}
//	}
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	$("input[name='payment']").on('click', function(e) {
		payment();
	});
	payment=function() {
		var payment=$("input[name='payment']:checked").val();
		if(payment=="mbill"){
			$("#make_collections").attr("readonly","readonly");
			$("#make_collections").val($("#sp_filter").val());
			$("#num_name1").val($("#sp_filter").val());
			$("#payeename").attr("readonly","readonly");
			var paymentMethod = $('input[name="paymentMethod"]:checked').val();
			   if(paymentMethod=="cash"){
				   $("#payeename").removeAttr("readonly");	   
			   }
			   else{
				   $("#payeename").val(""); 
			   }
		}
		else if(payment=="dbill"){
			$("#make_collections").attr("readonly","readonly");
			$("#make_collections").val($("#billing_unit").val());
			$("#num_name1").val($("#billing_unit").val());
			$("#payeename").attr("readonly","readonly");
			var paymentMethod = $('input[name="paymentMethod"]:checked').val();
			   if(paymentMethod=="cash"){
				   $("#payeename").removeAttr("readonly");	   
			   }
			   else{
				   $("#payeename").val(""); 
			   }
		}
		else{
			$("#make_collections").removeAttr("readonly");
			$("#payeename").removeAttr("readonly");
			$("#num_name1").val("");
		}
	};
	//设置一个变量值，用来保存当前的ID
	var parentId = "costPreInvoiceOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costPreInvoiceOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostPreInvoiceOrderBtn").click(function(e){
 		$("#saveCostPreInvoiceOrderBtn").attr("disabled",true);
 		
 		var money = $("#money").val();
 		var pay_amount = $("#costPreInvoiceOrder-table").children().children().find('input[name="pay_amount"]').val();
 		if(pay_amount == 0 || pay_amount == '' || money == null || money == ''){
 			$.scojs_message('申请金额不能为0', $.scojs_message.TYPE_FALSE);
 			$("#saveCostPreInvoiceOrderBtn").attr("disabled",false);
 			return;
 		}			
 		saveCostPreInvoiceOrder(e);
 		
	});
	
	$("#costPreInvoiceOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costPreInvoiceOrderForm").valid()){
	       	return;
        }
		//异步向后台提交数据
        var saveCallback=function(data){
			if(data.ID>0){
				$("#costPreInvoiceOrderId").val(data.ID);
				loadItem(data.ID);
			  	//$("#style").show();
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	if("costPreInvoiceOrderItem" == parentId){
			  		contactUrl("edit?id",data.ID);
			  	}
			}else{
				alert('数据保存失败。');
			}
		};
       
		parentId = e.target.getAttribute("id");	
		
		var loadItem = function(costPreInvoiceOrderId){
			invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
		    invoiceItemTable.fnDraw();
			var costCheckOrderIds = $("#costCheckOrderIds").val();	
			costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderList?costCheckOrderIds="+costCheckOrderIds;   
			costPreInvoiceOrderTable.fnDraw();
			
			costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
			costPreInvoiceOrderTable.fnDraw();
			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	};
		
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
		if(costPreInvoiceOrderId=="" || costPreInvoiceOrderId == null){
			$.post('/costPreInvoiceOrder/save',$("#costPreInvoiceOrderForm").serialize(), saveCallback, 'json');
		}else{
			loadItem(costPreInvoiceOrderId);
		}
		
		
	});
	
    if($("#costPreInvoiceOrderStatus").text() == 'new'){
    	$("#costPreInvoiceOrderStatus").text('新建');
	}    

	var invoiceItemTable =$('#invoiceItem-table').dataTable( {
	    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "sAjaxSource": "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+$("#costPreInvoiceOrderId").val(),
   			"aoColumns": [
            { "mDataProp": null,
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.INVOICE_NO == null){
  	            		str = "<input type='text' name='invoice_no'>";
  	            	}else{
  	            		str = "<input type='text' name='invoice_no' value='"+obj.aData.INVOICE_NO+"'>";
  	            	}
  	            	return str;
  	            }
  	        },
            {"mDataProp":"CNAME"},
            { "mDataProp": null,
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.AMOUNT == null){
  	            		str = "<input type='text' name='amount'>";
  	            	}else{
  	            		str = "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"'>";
  	            	}
  	            	return str;
  	            }
  	        },
            {"mDataProp":"COST_ORDER_NO"},
            {  
                "mDataProp": null, 
                "sWidth": "60px",  
            	"sClass": "remark",              
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger deleteInvoiceItem' code='"+obj.aData.ID+"'>"+
              		"<i class='fa fa-trash-o fa-fw'> </i> "+
              		"删除"+
              		"</a>";
                }
            }     
  	        
            /*{"mDataProp":null}*/
         ]
	});	
	
	//异步删除发票
	$("#invoiceItem-table").on('click', '.deleteInvoiceItem', function(e){
		var id = $(this).attr('code');
		e.preventDefault();
		$.post('/costPreInvoiceOrder/deleteInvoiceItem/'+id,function(data){
             //保存成功后，刷新列表
             invoiceItemTable.fnDraw();
        },'text');
	});

	$("#invoiceItem-table").on('blur', 'input', function(e){
		e.preventDefault();
		var invoiceItemId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/updateInvoiceItem', {costPreInvoiceOrderId:costPreInvoiceOrderId, invoiceItemId:invoiceItemId, name:name, value:value}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+$("#costPreInvoiceOrderId").val();   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
	});	
	
	var costPreInvoiceOrderTable=$('#costPreInvoiceOrder-table').dataTable({
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
			$(nRow).attr('cost_amount', aData.COST_AMOUNT);
			$(nRow).attr('yufu_amount', aData.YUFU_AMOUNT);
			return nRow;
		},
        "sAjaxSource": "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+$("#costPreInvoiceOrderId").val(),
        "aoColumns": [     
          { "mDataProp": "INVOICE_NO",
          	"fnRender": function(obj) {
          		  var str="";
          		  if(obj.aData.INVOICE_NO != null && obj.aData.INVOICE_NO != ''){
          			  /*invoiceNoArr = obj.aData.ALL_INVOICE_NO.split(',');
          			  if(obj.aData.ALL_INVOICE_NO.indexOf(obj.aData.INVOICE_NO) > -1){
          			  	  str="<option value='"+obj.aData.INVOICE_NO+"' selected = 'selected'>"+obj.aData.INVOICE_NO+"</option>";
          			  	  invoiceNoArr.splice(obj.aData.INVOICE_NO, 1);
          			  }
          			  for(var i=0;i<invoiceNoArr.length;i++){
          				  str += "<option value="+invoiceNoArr[i]+">"+invoiceNoArr[i]+"</option>";          			            				  
          			  }*/
                      
                    	  $("#itemInvoiceNoList").children().each(function(){
                    		  if(obj.aData.INVOICE_NO.indexOf($(this).text()) > -1 && $(this).text() != ''){
                    			  str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";                    			
                    		  }else{
                    			  if($(this).text != null){
                    				  str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
                    			  }
                    		  }
                    	  });
          		  }else{
          			  if(obj.aData.ALL_INVOICE_NO != null && obj.aData.ALL_INVOICE_NO != ""){
	          			  invoiceNoArr = obj.aData.ALL_INVOICE_NO.split(',');
	          			  for(var i=0;i<invoiceNoArr.length;i++){
	          				  str += "<option value="+invoiceNoArr[i]+">"+invoiceNoArr[i]+"</option>";          			            				  
	          			  }
          			  }
          		  }
                  return "<select name='invoice_no' multiple=''>"+str+"</select>";
          	   }
	        },  
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":"CNAME", "sWidth":"120px"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":"COST_AMOUNT"},
            {"mDataProp":"YUFU_AMOUNT"
            	/*"sClass": "yufu",
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.TOTAL_PAY == null){
  	            		str = obj.aData.TOTAL_PAY;
  	            	}else{
  	            		str = obj.aData.COST_AMOUNT - obj.aData.TOTAL_PAY;
  	            	}
  	            	return str;
  	            }*/
            },
            {"mDataProp":null,
  	            "fnRender": function(obj) {
  	            	var str;
  	            	if(obj.aData.PAY_AMOUNT == null){
  	            		str = "<input type='text' name='pay_amount' style='width:100px'>";
  	            		//str = "<input type='text' name='pay_amount' value='"+(obj.aData.COST_AMOUNT - obj.aData.TOTAL_PAY)+"'>";
  	            	}else{
  	            		str = "<input type='text' name='pay_amount' style='width:100px' value='"+obj.aData.PAY_AMOUNT+"'>";
  	            	}
  	            	$("#money").val(obj.aData.PAY_AMOUNT);
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
	
	$("#addInvoiceItemBtn").click(function(){		
		$.post('/costPreInvoiceOrder/addInvoiceItem', {costPreInvoiceOrderId:$("#costPreInvoiceOrderId").val()}, function(data){
			if(data.success){
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
		},'json');
	});
	
	
	$("#costPreInvoiceOrder-table").on('blur', 'input', function(e){
		e.preventDefault();
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		var costOrderId = $(this).parent().parent().attr("id");
		var yufu_amount = $(this).parent().parent().attr("yufu_amount");
		var name = $(this).attr("name");
		var value = $(this).val();

		$.post('/costPreInvoiceOrder/updateArapCostOrder', {costPreInvoiceOrderId:costPreInvoiceOrderId ,costOrderId:costOrderId, name:name, value:value}, function(data){
			if(data.costApplicationOrderRel.ID > 0){
				$("#tpayment").html(data.pay_amount_a);
				$("#paidAmount").html(parseFloat($("#paidAmounts").val())+parseFloat(data.pay_amount_a));
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
				costPreInvoiceOrderTable.fnDraw();

				if(data.tips=='success')
					$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
			}else{
				$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
			}
    	},'json');
		
	});	
	
	/*$("#costPreInvoiceOrderItem").click(function(){	
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/findAllInvoiceItemNo', {costPreInvoiceOrderId:costPreInvoiceOrderId}, function(data){
			if(data.length > 0){
				var itemInvoiceNoList = $("#itemInvoiceNoList");
				itemInvoiceNoList.empty();
				var option = "<option></option>";
				for(var i=0;i<data.length;i++){
					option += "<option value='"+data[i].INVOICE_NO+"'>"+data[i].INVOICE_NO+"</option>";
				}
				itemInvoiceNoList.append(option);	
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
				costPreInvoiceOrderTable.fnDraw();
			}
    	},'json');
			
	    invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
	    invoiceItemTable.fnDraw();		
	    
		costPreInvoiceOrderTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costCheckOrderListById?costPreInvoiceOrderId="+costPreInvoiceOrderId;    
		costPreInvoiceOrderTable.fnDraw();
	});	*/

	$("#costPreInvoiceOrder-table").on('blur', 'select', function(e){
		e.preventDefault();
		var costCheckOrderId = $(this).parent().parent().attr("id");
		var name = $(this).attr("name");
		var value = $(this).val();
		var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();
		$.post('/costPreInvoiceOrder/updatePreInvoice', {costPreInvoiceOrderId:costPreInvoiceOrderId, costCheckOrderId:costCheckOrderId, name:name, value:value}, function(data){
			if(data.success){
			    var costPreInvoiceOrderId = $("#costPreInvoiceOrderId").val();	
				invoiceItemTable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/costInvoiceItemList?costPreInvoiceOrderId="+costPreInvoiceOrderId;   
			    invoiceItemTable.fnDraw();
			}
    	},'json');
	});

    $.post('/costPreInvoiceOrder/searchAllAccount',function(data){
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
    $("input[name='payment']").each(function(){
		if($("#paymentRadio").val() == $(this).val()){
			$(this).attr('checked', true);
 		}
	 }); 
    
    $("#paymentMethods").on('click', 'input', function(){
    	if($(this).val() == 'cash'){
    		$("#accountTypeDiv").hide();
    	}else{
    		$("#accountTypeDiv").show();    		
    	}
    }); 
    $("#printBtn").on('click',function(){
    	var order_no = $("#sorder_no").text();
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
    
    
  //点击确认
	$("#auditBtn").click(function(){
		if(paymentMethod == '现金'){
			$("#paymentMethod1").attr("checked","checked");
			$("#cashLabel").show();
			$("#transfersLabel").hide();
			$("#accountTypeDiv").hide();
		}else if(paymentMethod == '转账'){
			$("#paymentMethod2").attr("checked","checked");
			$("#cashLabel").hide();
			$("#transfersLabel").show();
			$("#accountTypeDiv").show();
		}
	});
	
	//选择单据
	$("#costAccept-table").on('click', '.checkedOrUnchecked', function(){
		if($(this).prop('checked') == true){
			if(paymentMethod != ""){
				if(paymentMethod != $(this).parent().siblings('.payment_method')[0].innerHTML){
					alert("请选择相同的付款方式!");
					return false;
				}
			}else
				paymentMethod = $(this).parent().siblings('.payment_method')[0].innerHTML;
		}else{
			var checkedNumber = 0;
			$("#costAccept-table tr").each(function (){
				if($(this).find("td").find("input[type='checkbox']").prop('checked') == true)
					checkedNumber+=1;
			});
			if(checkedNumber == 0)
				paymentMethod = "";
		}
		console.log("付款方式:"+paymentMethod);
	});
	
	
	 $.post('/costMiscOrder/searchAllAccount',function(data){
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

   $("#paymentMethods").on('click', 'input', function(){
   	if($(this).val() == 'cash'){
   		$("#accountTypeDiv").hide();
   	}else{
   		$("#accountTypeDiv").show();    		
   	}
   });
   $("input[name=paymentMethod]").on('click', function(){
	   payment();
	   var paymentMethod = $('input[name="paymentMethod"]:checked').val();
	   if(paymentMethod=="transfers"){
		   $("#acc").show();
	   }
	   else{
		   $("#acc").hide();
	   }
   });
   $(document).ready(function(){
	   payment();
	   var paymentMethod = $('input[name="paymentMethod"]:checked').val();
	   if(paymentMethod=="transfers"){
		   $("#acc").show();
	   }
	   else{
		   
		   $("#acc").hide();
	   } 
   }); 
    
} );