$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	$("input[type='radio']").on('click',function(){
		if($(this).val()=='Y'){
			$("#isInvoice").show();
		}else{
			$("#payee").val('');
			$("#billing_unit").val('');
			$("#isInvoice").hide();
		}
	});
	
	if($("#chargeCheckOrderId").val() == ""){
		$('#saveChargeCheckOrderBtn').attr('disabled', false);
		$('#auditBtn').attr('disabled', true);
	}else{
		if($("#chargeCheckOrderStatus").text() == "新建"){
			$('#auditBtn').attr('disabled', false);
			$('#saveChargeCheckOrderBtn').attr('disabled', false);
		}
	}
		
	
	$("#printBtn").on('click',function(){
    	var order_no = $("#arap_order_no").text();
    	if(order_no != null && order_no != ""){
    		$.post('/report/printCustomerOrder', {order_no:order_no}, function(data){
        		window.open(data);
        	});
    	}else{
    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
    	}
    	
    });
	var saveChargeCheckOrder = function(){
		//阻止a 的默认响应行为，不需要跳转
		//e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeCheckOrderForm").valid()){
	       	return;
        }
        //数据响应之前回调按钮
        
        if($('#beginTime_filter').val()==""){
        	$.scojs_message('对账开始日期不能为空', $.scojs_message.TYPE_ERROR);
        	return;
        }
        if($('#endTime_filter').val()==""){
        	$.scojs_message('对账结束日期不能为空', $.scojs_message.TYPE_ERROR);
        	return;
        }
        $('#saveChargeCheckOrderBtn').attr('disabled', true);
        $("#addOrderBtn").attr("disabled", false);
        var tableRows = $("#chargeConfirem-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
            if(index==0)
                continue;

            var row = tableRows[index];
            var get_item_change_amount  = function(td){
                var element = td.find('input');
                if(element.length>0){
                    return element.val();
                }else{
                    return td.text();
                }
            };
            var item={
                ORDER_ID: $(row).attr('id'), 
                ORDER_TYPE: $(row).attr('order_type'),
                AMOUNT: $(row.children[2]).text(),
                CHANGE_AMOUNT: get_item_change_amount($(row.children[3]))
            };
            itemsArray.push(item);
        }

        var total_amount = 0.00;
        var change_amount = 0;
        for(var i=0; i<itemsArray.length; i++){
            total_amount += Number(itemsArray[i].AMOUNT);
            change_amount += Number(itemsArray[i].CHANGE_AMOUNT);
            $('#chargeAmount').html(total_amount);
        }

        var order={
            chargeCheckOrderId: $('#chargeCheckOrderId').val(),
            customer_id: $('#customer_id').val(),
            remark: $('#remark').val(),
            sp_id: $('#spId').val(),
            billing_unit: $('#billing_unit').val(),
            payee: $('#payee').val(),
            beginTime_filter:$('#beginTime_filter').val(),
            endTime_filter:$('#endTime_filter').val(),
            total_amount: total_amount,
            change_amount: change_amount,
            have_invoice: $('input:radio:checked').val(),
            items: itemsArray
        };

        console.log(order);
		//异步向后台提交数据
		$.post('/chargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
			if(data.ID>0){
				$("#chargeCheckOrderId").val(data.ID);
				$("#chargeAmount")[0].innerHTML = data.CHARGE_AMOUNT;
				$('#auditBtn').attr('disabled', false);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				contactUrl("edit?id",data.ID);
				$("#arap_order_no").text(data.ORDER_NO);
				$('#saveChargeCheckOrderBtn').attr('disabled', false);
			}else{
				alert('数据保存失败。');
			}
		},'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        });
	};
    $("#chargeConfirem-table").on('click', '.finItemdel', function(){
        var chargeCheckOrderId =$("#chargeCheckOrderId").val();
        var order_id = $(this).attr('code');
        var order_type = $(this).attr('order_type');
        var change_amount = $(this).attr('change_amount');
        if($("#chargeCheckOrderStatus").html()!="新建"){
            $.scojs_message('只能撤销新建单据', $.scojs_message.TYPE_ERROR);
            return false;
        }
        if($("#chargeCheckOrderId").val()==""){
            $.scojs_message('只能撤销已保存的单据', $.scojs_message.TYPE_ERROR);
            return false;
        }
        $.post('/chargeCheckOrder/deleteItem',
            {chargeCheckOrderId:chargeCheckOrderId,change_amount:change_amount,order_id:order_id,order_type:order_type},
             function(data){
              if(data=='ok'){
                var total_amount=$("#total_amount").html();
                var amount=$("#amount").html();
                var chargeAmount=$("#chargeAmount").html();
                chargeConfiremTable.fnSettings().oFeatures.bServerSide=true;
                chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/returnOrderList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
                chargeConfiremTable.fnDraw();
               $("#total_amount").html((parseFloat(total_amount )-parseFloat(change_amount)).toFixed(2));
               $("#amount").html((parseFloat(amount )-parseFloat(change_amount)).toFixed(2));
               $("#chargeAmount").html((parseFloat(chargeAmount )-parseFloat(change_amount)).toFixed(2));
              }else{
                  $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
              }
        });
    });
    if($("#chargeCheckOrderStatus").html()=="新建"&&$("#chargeCheckOrderId").val()!=""){
        $("#addOrderBtn").attr("disabled", false);
    }
	// 审核
	$("#auditBtn").click(function(e){
		$('#auditBtn').attr('disabled', true);
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//异步向后台提交数据
        $("#addOrderBtn").attr("disabled", true);
		$('#saveChargeCheckOrderBtn').attr('disabled', true);
		var chargeCheckOrderId = $("#chargeCheckOrderId").val();
		$.post('/chargeCheckOrder/auditChargeCheckOrder', {chargeCheckOrderId:chargeCheckOrderId}, function(data){
			$("#chargeCheckOrderStatus").html(data.STATUS);
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
	var parentId = "chargeCheckOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargeCheckOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveChargeCheckOrderBtn").click(function(){
 		//数据响应之前回调按钮
 		saveChargeCheckOrder();
    
 		//$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	});
	
	// $("#chargeCheckOrderItem").click(function(e){
	// 	//阻止a 的默认响应行为，不需要跳转
	// 	e.preventDefault();
	// 	//提交前，校验数据
 //        if(!$("#chargeCheckOrderForm").valid()){
	//        	return;
 //        }
	// 	//异步向后台提交数据
	// 	$.post('/chargeCheckOrder/save', $("#chargeCheckOrderForm").serialize(), function(data){
	// 		if(data.ID>0){
	// 			$("#chargeCheckOrderId").val(data.ID);
	// 		  	//$("#style").show();
	// 		  	$("#departureConfirmationBtn").attr("disabled", false);
	// 		  	if("chargeCheckOrderbasic" == parentId){
	// 		  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	// 		  	}
	// 		}else{
	// 			alert('数据保存失败。');
	// 		}
	// 	},'json');
	// 	parentId = e.target.getAttribute("id");
	// });
	
    if($("#chargeCheckOrderStatus").text() == 'new'){
    	$("#chargeCheckOrderStatus").text('新建');
	}
   
    var chargeConfiremTable = $('#chargeConfirem-table').dataTable({
        //"bPaginate": false, //翻页功能
        //"bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bPaginate" : false, //显示分页器
        //"bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        //"sAjaxSource": "/chargeCheckOrder/returnOrderList",
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
            $(nRow).attr('id', aData.ID);
            $(nRow).attr('order_type', aData.TPORDER);
            return nRow;
        },
        "aoColumns": [  
            {"mDataProp":"ID", "bVisible":false},
            {"mDataProp":"TPORDER", "bVisible":false},
            {"mDataProp":"ORDER_NO", "sWidth":"100px",
            	"fnRender": function(obj) {
            		if(Return.isUpdate || Return.isComplete){
            			return "<a href='/returnOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        		}},
		    {"mDataProp":null, "sWidth":"100px",
                "fnRender": function(obj) {
                    return "未收款";
                }
            },
            {"mDataProp":"CHARGE_TOTAL_AMOUNT", "sWidth":"100px"}, 
            {"mDataProp":"CHANGE_AMOUNT", "sWidth":"120px",
                "fnRender": function(obj) {
                    // return "<input style='width: 100%;' type='text' name='change_amount' value='0'/>";
                    if( obj.aData.TPORDER == "收入单"||$("#chargeCheckOrderStatus").text()=="已确认"||$("#chargeCheckOrderStatus").text()=="收款申请中"
                        ||$("#chargeCheckOrderStatus").text()=="收款确认中"||$("#chargeCheckOrderStatus").text()=="已收款确认"){
                        if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
                            return obj.aData.CHANGE_AMOUNT;  
                        }
                        else {
                            return obj.aData.CHARGE_TOTAL_AMOUNT;
                        }
                    }
                    else{
                        if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null ){
                            return "<input style='width: 80px;' type='text' name='change_amount' id='change' value='"+obj.aData.CHANGE_AMOUNT+"'/>";
                        }
                        else {
                            if(obj.aData.CHARGE_TOTAL_AMOUNT!=null){
                                return "<input style='width: 80px;' type='text' name='change_amount' id='change' value='"+obj.aData.CHARGE_TOTAL_AMOUNT+"'/>";
                            }
                            else{
                                return "<input style='width: 80px;' type='text' name='change_amount' id='change' value='0'/>";
                            }
                        }
                    }
                }
            },

            {"mDataProp":"PLANNING_TIME", "sWidth":"120px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"40px"},
            {"mDataProp":"REF_NO", "sWidth":"40px"},
            {"mDataProp":"PROVINCE", "sWidth":"120px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"120px"}, 
            {"mDataProp":"ROUTE_TO", "sWidth":"100px"},  
            {"mDataProp":"CNAME", "sWidth":"200px"},
            {"mDataProp":"RECEIPT_ADDRESS", "sWidth":"80px"},
            {"mDataProp":"SP", "sWidth":"200px"},
            {"mDataProp":null, "sWidth":"150px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"120px"},
            {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"120px"},	
            {"mDataProp":"TRANSACTION_STATUS", "sWidth": "120px", 
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.TRANSACTION_STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.TRANSACTION_STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.TRANSACTION_STATUS;
                }
            },           
            {"mDataProp":"RECEIPT_DATE", "sWidth":"150px"},        	
            {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},                        
                                
            /*{"mDataProp":null, "sWidth":"150px"},                         
            {"mDataProp":null, "sWidth":"100px"},*/                        
            {"mDataProp":"CONTRACT_AMOUNT", "sWidth":"150px", "bVisible":false},                        
            //{"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                       
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":null, "sWidth":"150px", "bVisible":false},                        
            {"mDataProp":"STEP_AMOUNT", "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"100px", "bVisible":false},                        
            {"mDataProp":null, "sWidth":"100px", "bVisible":false},                      
            {"mDataProp":null, "sWidth":"200px"},
            {"mDataProp": null, 
                "sWidth": "20px",                
                "fnRender": function(obj) {
                    return "<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"' order_type='"+obj.aData.TPORDER+"' change_amount='"+$(obj.aData.CHANGE_AMOUNT).attr('value')+"' <i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            } 
        ]      
    });
    var chargeMiscListTable = $('#chargeMiscList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
          "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/checkChargeMiscList",
        "aoColumns": [   
            {"mDataProp":"MISC_ORDER_NO", "sWidth":"80px",
                "fnRender": function(obj) {
                    return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.MISC_ORDER_NO+"</a>";
                }},
            {"mDataProp":"CNAME", "sWidth":"200px"},
            {"mDataProp":"NAME", "sWidth":"200px"},
            {"mDataProp":"AMOUNT", "sWidth":"100px"},
            {"mDataProp":"REMARK", "sWidth":"200px"}                        
        ]      
    });
    
    $("#chargeMiscList").click(function(){
    	chargeMiscListTable.fnSettings().sAjaxSource = "/chargeCheckOrder/checkChargeMiscList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
    	chargeMiscListTable.fnDraw();  
    });

    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });	 
    
    $('#datetimepicker2').datetimepicker({  
    	format: 'yyyy-MM-dd',  
    	language: 'zh-CN', 
    	autoclose: true,
    	pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
    	$(".bootstrap-datetimepicker-widget").hide();
    	$('#endTime_filter').trigger('keyup');
    });	
    
    var externalTab = $('#external-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/externalMiscOrderList",
        "aoColumns": [      
	        { "mDataProp": null,
	            "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
	        }, 
            {"mDataProp":"ORDER_NO","sWidth": "80px",
            	"fnRender": function(obj) {
        			return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"TYPE","sWidth": "100px",
            	"fnRender": function(obj) {
                    if(obj.aData.TYPE=='ordinary_receivables'){
                        return '普通收款';
                    }else if(obj.aData.TYPE=='offset_payment'){
                        return '抵销货款';
                    }
                    return obj.aData.TYPE;
                }
            },
            {"mDataProp":"STATUS","sWidth": "100px",
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
            {"mDataProp":"CREATE_STAMP","sWidth": "150px"},
            {"mDataProp":"CHARGE_ORDER_NO","sWidth": "150px"},
            {"mDataProp":"REMARK","sWidth": "150px"}                       
        ]      
    });	

    $("#addExternalMiscOrderBtn").click(function(){
    	externalTab.fnSettings().sAjaxSource = "/chargeCheckOrder/externalMiscOrderList";
    	externalTab.fnDraw();  
    });    

    var ids = [];
    // 未选中列表
	$("#external-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			ids.push($(this).val());
			$("#micsOrderIds").val(ids);
		}			
	});
	
	// 已选中列表
	$("#external-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#micsOrderIds").val(ids);
			}
		}			
	});
	
	$("#addExternalFormBtn").click(function(){
		var micsOrderIds = $("#micsOrderIds").val();
		var chargeCheckOrderId = $("#chargeCheckOrderId").val();
		$.post('/chargeCheckOrder/updateChargeMiscOrder', {micsOrderIds: micsOrderIds, chargeCheckOrderId: chargeCheckOrderId}, function(data){
			if(data.ID > 0){
				$("#debitAmountSpan")[0].innerHTML = data.DEBIT_AMOUNT;
				$("#debitAmount")[0].innerHTML = data.DEBIT_AMOUNT;
				$("#chargeAmount")[0].innerHTML = data.CHARGE_AMOUNT;
				$("#hiddenDebitAmount").val(data.DEBIT_AMOUNT);
				
				$('#addExternalMiscOrder').modal('hide');
		    	chargeMiscListTable.fnSettings().sAjaxSource = "/chargeCheckOrder/checkChargeMiscList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
		    	chargeMiscListTable.fnDraw();  
			}
		},'json');
	});
	
	/*//开票单位
	$('#billing_unit').on('keyup click', function(){

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
        //payment();
    });*/
	
	
	
	 //获取供应商的list，选中信息在下方展示其他信息
    $('#billing_unit').on('input click', function(){
    		var me= this;
    		var inputStr = $('#billing_unit').val();
    		if(inputStr == ""){
    			var pageSpName = $("#pageSpName");
    			pageSpName.empty();
    			var pageSpAddress = $("#pageSpAddress");
    			pageSpAddress.empty();
    			$('#sp_id').val($(this).attr(''));
    		}
    		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
    			if(inputStr!=$('#billing_unit').val()){//查询条件与当前输入值不相等，返回
					return;
				}
    			var spList =$("#spList");
    			spList.empty();
    			for(var i = 0; i < data.length; i++)
    			{
    				var abbr = data[i].ABBR;
 				if(abbr == null){
 					abbr = '';
 				}
 				var company_name = data[i].COMPANY_NAME;
 				if(company_name == null){
 					company_name = '';
 				}
 				var contact_person = data[i].CONTACT_PERSON;
 				if(contact_person == null){
 					contact_person = '';
 				}
 				var phone = data[i].PHONE;
 				if(phone == null){
 					phone = '';
 				}
 				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+company_name+"</a></li>");
 			}
 			$("#spList").css({ 
        	left:$(me).position().left+"px", 
        	top:$(me).position().top+28+"px" 
       }); 
       $('#spList').show();

    		},'json');

    		
    	});

    	// 没选中供应商，焦点离开，隐藏列表
    	$('#billing_unit').on('blur', function(){
     		$('#spList').hide();
     	});

    	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    	$('#spList').on('blur', function(){
     		$('#spList').hide();
     	});

    	$('#spList').on('mousedown', function(){
    		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    	});

    	// 选中供应商
    	$('#spList').on('mousedown', '.fromLocationItem', function(e){
    		//console.log($('#spList').is(":focus"))
    		var message = $(this).text();
    		$('#billing_unit').val(message);
    		var pageSpName = $("#pageSpName");
    		pageSpName.empty();
    		var pageSpAddress = $("#pageSpAddress");
    		pageSpAddress.empty();
    		pageSpAddress.append($(this).attr('address'));
    		var contact_person = $(this).attr('contact_person');
    		if(contact_person == 'null'){
    			contact_person = '';
    		}
    		pageSpName.append(contact_person+'&nbsp;');
    		var phone = $(this).attr('phone');
    		if(phone == 'null'){
    			phone = '';
    		}
    		pageSpName.append(phone); 
    		pageSpAddress.empty();
    		var address = $(this).attr('address');
    		if(address == 'null'){
    			address = '';
    		}
    		pageSpAddress.append(address);
            $('#spList').hide();
            //refreshList();
        });
    	
    	
    	// 回显订单类型
   	 $("input[name='invoiceType']").each(function(){
   		if($("#invoiceRadio").val() == $(this).val()){
   			$(this).attr('checked', true);
   		}
   		
   	 });
     var addChargeCheckTable = $('#uncheckedChargeCheck-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, // 不要排序
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10 ,25 ,50 ,100 ,9999999], [10 ,25 ,50 ,100, "All"] ],
        "bServerSide": true,
        "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/createList",
        "aoColumns": [ 
              { "mDataProp": null, "sWidth":"20px","bSortable": false,
                "fnRender": function(obj) {
                  return '<input type="checkbox" name="order_check_box" tporder="'+obj.aData.TPORDER+'" customer="'+obj.aData.CUSTOMER_ID+'" class="checkedOrUnchecked" id="'+obj.aData.ID+'" change_amount="'+obj.aData.CHARGE_TOTAL_AMOUNT+'">';
                }
              },  
              {"mDataProp":"ID", "bVisible": false},
              {"mDataProp":"ORDER_NO","sClass": "order_no",
                  "fnRender": function(obj) {
                      if(Return.isUpdate || Return.isComplete){
                          return "<a href='/returnOrder/edit?id="+obj.aData.ID+"'>"+obj.aData.ORDER_NO+"</a>";
                      }else{
                          return obj.aData.ORDER_NO;
                      }
                    
              }},
                {"mDataProp":null, "sWidth":"120px",
                    "fnRender": function(obj) {
                        return "未收款";
              }},
              {"mDataProp":"CHARGE_TOTAL_AMOUNT", "sWidth":"150px"},
              {"mDataProp":"CNAME","sClass": "cname", "sWidth":"200px"},
              {"mDataProp":"SP","sClass": "sp", "sWidth":"200px"},
              {"mDataProp":"PLANNING_TIME", "sWidth":"150px"},
              {"mDataProp":"ADDRESS", "sWidth":"200px"},
              {"mDataProp":null, "sWidth":"150px"},
              {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
              {"mDataProp":"DELIVERY_ORDER_NO", "sWidth":"200px"},
              {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"200px"},          
              {"mDataProp":null, "sWidth": "120px", 
                  "fnRender": function(obj) {
                      if(obj.aData.TRANSACTION_STATUS=='new'){
                          return '新建';
                      }else if(obj.aData.TRANSACTION_STATUS=='checking'){
                          return '已发送对帐';
                      }else if(obj.aData.TRANSACTION_STATUS=='confirmed'){
                          return '已审核';
                      }else if(obj.aData.TRANSACTION_STATUS=='completed'){
                          return '已结算';
                      }else if(obj.aData.TRANSACTION_STATUS=='cancel'){
                          return '取消';
                      }
                      return obj.aData.TRANSACTION_STATUS;
                  }
              },           
              {"mDataProp":"RECEIPT_DATE", "sWidth":"150px"},           
              {"mDataProp":"ROUTE_FROM", "sWidth":"100px"},                        
              {"mDataProp":"ROUTE_TO", "sWidth":"100px"},                     
              {"mDataProp":"CONTRACT_AMOUNT", "sWidth":"150px"},                        
              //{"mDataProp":"PICKUP_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":null, "sWidth":"100px"},                        
              {"mDataProp":"SEND_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":"INSURANCE_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":"SUPER_MILEAGE_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":"STEP_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":"INSTALLATION_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":null, "sWidth":"150px"},                        
              {"mDataProp":"WAREHOUSE_AMOUNT", "sWidth":"100px"},                        
              {"mDataProp":null, "sWidth":"100px"},                        
              {"mDataProp":null, "sWidth":"100px"},                        
              {"mDataProp":null, "sWidth":"150px"},                        
              {"mDataProp":null, "sWidth":"150px"},                        
              {"mDataProp":null, "sWidth":"150px"},                        
              {"mDataProp":null, "sWidth":"150px"},                        
              {"mDataProp":null, "sWidth":"200px"}                      
          ]          
    });
      $("#addOrderBtn").on('click',function(){
            $("#initialTotalAmount").val($("#total_amount").html())
            $("initialAmount").val($("#amount").html())
            $("initialChargeAmount").val($("#chargeAmount").html())
            refreshCreateList();
       });
        var addreturnIds = [];
        var addmiscOrderIds =[];
        var addamount =[];
	 $("#uncheckedChargeCheck-table").on('click', '.checkedOrUnchecked', function(){
        if($("#customer_id").val()!=$(this).attr('customer')){
            $.scojs_message('请选择相同的客户', $.scojs_message.TYPE_ERROR);
            return false;
        }
        var change_amount = $(this).attr('change_amount');
        var total_amount=$("#total_amount").html();
        var amount=$("#amount").html();
        var chargeAmount=$("#chargeAmount").html();
        if($(this).prop("checked") == true){
            addamount.push($(this).attr('change_amount'));
            $("#addAmount").val(addamount);
            if($(this).attr('tporder') == "收入单"){
                addmiscOrderIds.push($(this).attr('id'));
                $("#addMiscOrder").val(addmiscOrderIds);
            }else{
                addreturnIds.push($(this).attr('id'));
                $("#addReturnOrder").val(addreturnIds);
            }
            $("#total_amount").html((parseFloat(total_amount )+parseFloat(change_amount)).toFixed(2));
            $("#amount").html((parseFloat(amount )+parseFloat(change_amount)).toFixed(2));
            $("#chargeAmount").html((parseFloat(chargeAmount )+parseFloat(change_amount)).toFixed(2));
        }else {
            addamount.splice($.inArray($(this).attr('change_amount'),addamount), 1);
            $("#addAmount").val(addamount);
            if($(this).attr('tporder') == "收入单"){
                if(addmiscOrderIds.length != 0){
                    addmiscOrderIds.splice($.inArray($(this).attr('id'), addmiscOrderIds), 1);
                    $("#addMiscOrder").val(addmiscOrderIds);
                }
            }else{
                if(addreturnIds.length != 0){
                    addreturnIds.splice($.inArray($(this).attr('id'), addreturnIds), 1);
                    $("#addReturnOrder").val(addreturnIds);
                }
            }
            $("#total_amount").html((parseFloat(total_amount )-parseFloat(change_amount)).toFixed(2));
            $("#amount").html((parseFloat(amount )-parseFloat(change_amount)).toFixed(2));
            $("#chargeAmount").html((parseFloat(chargeAmount )-parseFloat(change_amount)).toFixed(2));
        }
     });
        $("#closeExternalFormBtn").on('click',function(){
                $("#total_amount").html($("#initialTotalAmount").val());
                $("#amount").html($("initialAmount").val());
                $("#chargeAmount").html($("initialChargeAmount").val());
                $("#addReturnOrder").val($("#closeId").val());
                $("#addMiscOrder").val($("#closeId").val());
                $("#addAmount").val($("#closeId").val());
                addreturnIds = [];
                addmiscOrderIds =[];
                addamount =[];
          });
        $("#addOrderFormBtn").on('click',function(){
        var chargeCheckOrderId =$("#chargeCheckOrderId").val();
        var addReturnOrder=$("#addReturnOrder").val();
        var addMiscOrder=$("#addMiscOrder").val();
        var addAmount=$("#addAmount").val();
        $.post('/chargeCheckOrder/addItem', {addReturnOrder:addReturnOrder,addMiscOrder:addMiscOrder,addAmount:addAmount,chargeCheckOrderId: chargeCheckOrderId}, function(data){
            if(data=='ok'){
                $('#addOrder').modal('hide');
                addChargeCheckTable.fnDraw();
                chargeConfiremTable.fnSettings().oFeatures.bServerSide=true;
                chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/returnOrderList?chargeCheckOrderId="+$("#chargeCheckOrderId").val();
                chargeConfiremTable.fnDraw();
                $.scojs_message('添加成功', $.scojs_message.TYPE_OK);
                $("#addReturnOrder").val($("#closeId").val());
                $("#addMiscOrder").val($("#closeId").val());
                $("#addAmount").val($("#closeId").val());
                addreturnIds = [];
                addmiscOrderIds =[];
                addamount =[];
            }
            else{
                $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
            }
        });
     });
       $('#ref_no').on( 'keyup', function () {
        refreshCreateList();
        } );
     var refreshCreateList = function() {
            $("#customer_no").val($("#customer_name").val())
            var ref_no = $("#ref_no").val();
            var customer_no = $("#customer_no").val();
            var ispage = "chargeCheckOrder";

            addChargeCheckTable.fnSettings().sAjaxSource = "/chargeCheckOrder/createList?ref_no="+ref_no
                                                            +"&ispage="+ispage
                                                            +"&customer_no="+customer_no;
            addChargeCheckTable.fnDraw();
        };
        
        
        $('#searchBtn').on('click',function(){
        	var serial_no =  $('#serial_no_filter').val();
        	var ref_no =  $('#ref_no_filter').val();
        	var customer_no =  $('#customerNo_filter').val();
        	var miscOrderIds = $("#miscOrderIds").val();
        	var chargeCheckOrderId = $("#chargeCheckOrderId").val();
        	var returnOrderIds = $("#returnOrderIds").val();

        	
        		if(chargeCheckOrderId!='' && chargeCheckOrderId!=null){
        			chargeConfiremTable.fnSettings().oFeatures.bServerSide=true;
                    chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/returnOrderList?chargeCheckOrderId="+chargeCheckOrderId
                    +"&serial_no="+serial_no+"&customer_no="+customer_no+"&ref_no="+ref_no;
                    chargeConfiremTable.fnDraw();
        		}else{
        			chargeConfiremTable.fnSettings().oFeatures.bServerSide=true;
                    chargeConfiremTable.fnSettings().sAjaxSource = "/chargeCheckOrder/searchItemList?returnOrderIds="+returnOrderIds
                    +"&miscOrderIds="+miscOrderIds+"&serial_no="+serial_no+"&customer_no="+customer_no+"&ref_no="+ref_no;
                    chargeConfiremTable.fnDraw();
        		}
        });
        
        //撤销订单
        $("#deleteBtn").on('click',function(){
        	var status = $("#chargeCheckOrderStatus").html();
        	var checkId = $("#chargeCheckOrderId").val();
        	if(!confirm("是否确认撤销此订单？"))
        		return;
        	if(checkId==""){
        		$.scojs_message('对不起，当前单据尚未保存，不能撤销', $.scojs_message.TYPE_ERROR);
        		return;
        	}else if(status!='新建'&& status!='已确认'){
    	    	$.scojs_message('对不起，当前单据已有下级单据，不能撤销', $.scojs_message.TYPE_ERROR);
        	}else{
        		$("#deleteBtn").attr('disabled',true);
        		$.post('/chargeCheckOrder/deleteOrder', {orderId:checkId}, function(data){ 
            		if(!data.success){
            			$("#deleteBtn").attr('disabled',false);
            			$.scojs_message('撤销失败', $.scojs_message.TYPE_ERROR);
            		}else{
            			$.scojs_message('撤销成功!,3秒后自动返回。。。', $.scojs_message.TYPE_OK);
            			setTimeout(function(){
    						location.href="/chargeCheckOrder";
    					}, 3000);
            		}
            	});
        	}
        });
        
        
} );