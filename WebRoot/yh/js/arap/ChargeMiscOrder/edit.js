$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_charge').addClass('active').find('ul').addClass('in');
	
	var saveChargeMiscOrder = function(e){
		$('#saveChargeMiscOrderBtn').attr('disabled', true);
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeMiscOrderForm").valid()){
	       	return;
        }
        
        //var tableItems = feeTable.fnGetData();
        var tableRows = $("#feeItemList-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
        	if(index==0)
        		continue;

        	var row = tableRows[index];
        	var id = $(row).attr('id');
        	if(!id){
        		id='';
        	}

        	var item={
        		ID: id,
        		CUSTOMER_ORDER_NO: $(row.children[0]).find('input').val(), 
			 	ITEM_DESC: $(row.children[1]).find('input').val(),
			 	NAME: $(row.children[2]).find('select').val(),
			 	AMOUNT: $(row.children[3]).find('input').val(),
			 	STATUS: '新建',
			 	ACTION: 'CREATE'
        	};
        	itemsArray.push(item);
        }


		var amount = 0;
        for(var i=0; i<itemsArray.length; i++){
        	amount+=Number(itemsArray[i].AMOUNT);
        	$('#totalAmountSpan').text(amount);
        }

        //add deleted items
        for(var index=0; index<deletedIds.length; index++){
        	var id = deletedIds[index];
        	var item={
        		ID: id,
			 	ACTION: 'DELETE'
        	};
        	itemsArray.push(item);
        }


        var order={
        	chargeMiscOrderId: $('#chargeMiscOrderId').val(),
        	customer_id: $('#customer_id').val(),
        	sp_id: $('#sp_id').val(),
        	biz_type: $('input[name="biz_type"]:checked').val(),
        	charge_from_type: $('input[name="charge_from_type"]:checked').val(),
        	others_name: $('#others_name').val(),
        	ref_no: $('#ref_no').val(),
        	remark: $('#remark').val(),
        	amount: amount,
        	items: itemsArray
        };
        console.log(order);
		//异步向后台提交数据
		$.post('/chargeMiscOrder/save', {params:JSON.stringify(order)}, function(data){
			var order = data.order;
			if(order.ID>0){
				$("#miscChargeOrderNo").html('<strong>'+order.ORDER_NO+'</strong>');
				$("#create_stamp").html(order.CREATE_STAMP);
				$("#chargeMiscOrderId").val(order.ID);
				if(order.REF_ORDER_NO)
					$("#refOrderNo").html('<strong>'+order.REF_ORDER_NO+'</strong>');
				contactUrl("edit?id",order.ID);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$('#saveChargeMiscOrderBtn').attr('disabled', false);
				deletedIds=[];

				feeTable.fnClearTable();

				for (var i = 0; i < data.itemList.length; i++) {
					var item = data.itemList[i];
					feeTable.fnAddData({
						ID: item.ID,
					 	CUSTOMER_ORDER_NO: item.CUSTOMER_ORDER_NO,
					 	ITEM_DESC: item.ITEM_DESC,
					 	NAME: item.NAME,
					 	AMOUNT: item.AMOUNT,
					 	CHANGE_AMOUNT: item.CHANGE_AMOUNT,
					 	STATUS: '新建'
					 });
				};
				window.location.reload();
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				$('#saveChargeMiscOrderBtn').attr('disabled', false);
			}
		},'json').fail(function() {
		    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    $('#saveChargeMiscOrderBtn').attr('disabled', false);
		  });
	};
   
	/*--------------------------------------------------------------------*/

	
	//设置一个变量值，用来保存当前的ID
	var parentId = "chargeMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#chargeMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveChargeMiscOrderBtn").click(function(e){
 		saveChargeMiscOrder(e);
	});
	
	$("#chargeMiscOrderItem").click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#chargeMiscOrderForm").valid()){
	       	return;
        }

        var items = feeTable.fnGetData();
        for(var index in items){
        	console.log(items[index]);
        }
		//异步向后台提交数据
		$.post('/chargeMiscOrder/save', {formData:$("#chargeMiscOrderForm").serialize(), itemTable:feeTable.fnGetData()}, function(data){
			if(data.ID>0){
				$("#chargeMiscOrderId").val(data.ID);
			  	//$("#style").show();
				
			  	$("#departureConfirmationBtn").attr("disabled", false);
			  	contactUrl("edit?id",data.ID);
			  	if("chargeMiscOrderbasic" == parentId){
			  		
			  		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  		
			  	}
			}else{
				alert('数据保存失败。');
			}
		},'json');
		parentId = e.target.getAttribute("id");
	});

	$('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var company_name = data[i].COMPANY_NAME;
				if(company_name == null){
					company_name='';
				}
				spList.append("<li><a tabindex='-1' spId='"+data[i].ID+"' class='fromLo'>"+company_name+" </a></li>");
			}
		},'json');
		
		spList.css({ 
        	left:$(this).position().left+"px", 
        	top:$(this).position().top+32+"px" 
        });		 
		spList.show();	 
    });
	$('#sp_filter').on('blur', function(){
 		$('#spList').hide();
 	});
	$('#spList').on('blur', function(){
 		$('#spList').hide();
 	});

	$('#spList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});
	
	$('#spList').on('mousedown', '.fromLo', function(e){
		var message = $(this).text();
		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
        $('#spList').hide();
        var spId = $(this).attr('spId');
        $('#sp_id').val(spId);
    });
    if($("#chargeMiscOrderStatus").text() == 'new'){
    	$("#chargeMiscOrderStatus").text('新建');
	}
    $("#printBtn").on('click',function(){
    	var order_no = $.trim($("#miscChargeOrderNo").text());
    	if(order_no != null && order_no != ""){
    		$.post('/report/printManualOrder', {order_no:order_no}, function(data){
        		window.open(data);
        	});
    	}else{
    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
    	}
    	
    });
    var feeTable = $('#feeItemList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
        "bSort": false,
    	 "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        //"sAjaxSource": "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val(),
        "aoColumns": [   
            {"mDataProp":"CUSTOMER_ORDER_NO",
            	"fnRender": function(obj) {
		        if(obj.aData.CUSTOMER_ORDER_NO!='' && obj.aData.CUSTOMER_ORDER_NO != null){
		            return "<input type='text' name='customer_order_no' value='"+obj.aData.CUSTOMER_ORDER_NO+"' class='form-control search-control'>";
		        }else{
		        	 return "<input type='text' name='customer_order_no' class='form-control search-control'>";
		        }
		     }},
          	{"mDataProp":"ITEM_DESC",
			    "fnRender": function(obj) {
			        if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
			            return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"' class='form-control search-control'>";
			        }else{
			        	 return "<input type='text'  name='item_desc' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":"NAME",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        			$("#receivableTotal").val(obj.aData.NAME);
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }
			 }},
			{"mDataProp":"AMOUNT",
			     "fnRender": function(obj) {
			    	 if($("#chargeMiscOrderStatus").text()!='新建'){
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
				             return obj.aData.AMOUNT;
				         }else{
				         	 return "";
				         }
			    	 }else{
			    		 if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null ){
				             return "<input type='text' name='amount' value='"+obj.aData.AMOUNT+"' class='form-control search-control'>";
				         }else{
				         	 return "<input type='text' name='amount' class='form-control search-control'>";
				         }
			    	 }
			 }},
			{"mDataProp":"STATUS","sClass": "status"},
            {"mDataProp": null,"sWidth": "80px",
                "fnRender": function(obj) {
                	if($("#chargeMiscOrderStatus").text()!='新建'){
                		return "";
                	}

                    return "<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                	
                }
            }    
        ]      
    });
    
   
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
	
    
  //保存费用明细客户与供应商的方法
    var savePartyInfo = function(partyId,partyType){
		var costMiscId = $("#costMiscOrderId").val();
		if(costMiscId != ""){
			$.post('/costMiscOrder/saveMiscPartyInfo',{miscId:costMiscId,partyId:partyId,partyType:partyType},function(data){
				if(!data.success){
					alert("保存出错");
				}
			});	
		}
	};
    
    //获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchPartCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				companyList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
            }
        },'json');
        companyList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
    });
    
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).attr("company_name"));
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customer_id').val(companyId);
        //savePartyInfo(companyId,"CUSTOMER");
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    
    
	//添加一行
	$("#addFee").click(function(){
		
		 feeTable.fnAddData({
		 	CUSTOMER_ORDER_NO:'',
		 	ITEM_DESC:'',
		 	NAME:'',
		 	AMOUNT: '0',
		 	STATUS: '新建'
		 });
		 feeTable.fnDraw(); 
	});	
	

	
	$("#chargeMiscOrderItem").click(function(){
		feeTable.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeMiscOrderItemList?chargeMiscOrderId="+$("#chargeMiscOrderId").val();
		feeTable.fnDraw();  
	});
	
	var deletedIds=[];
	//删除一行
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		e.preventDefault();
		var tr = $(this).parent().parent();
		deletedIds.push(tr.attr('id'))
		tr.remove();
	});	
	
	var typeRadio = $("#typeRadio").val();
	$("input[name='type']").each(function(){
		if(typeRadio == $(this).val()){
			$(this).prop('checked', true);
		}
	});
	
	var chargeCheckListTab = $('#chargeCheckList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeMiscOrder/chargeCheckList",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
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
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"CHARGE_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}                        
        ]      
    });	

    $("#chargeCheckList").click(function(){
    	chargeCheckListTab.fnSettings().sAjaxSource = "/chargeMiscOrder/chargeCheckList?chargeCheckOrderIds="+$("#chargeCheckOrderIds").val();
    	chargeCheckListTab.fnDraw();  
    });

    //初始化按钮
    if($("#chargeMiscOrderStatus").text()!='新建'){
    	$('#addFee').hide();    	
    	$('#saveChargeMiscOrderBtn').attr('disabled', true);
    }else{
    	$('#addFee').show();    	
    	$('#saveChargeMiscOrderBtn').attr('disabled', false);
    }

    
    
    var display = function(charge_from){
    	if(charge_from == 'sp'){
        	$("#customer_div").hide();
        	$("#sp_div").show();
        }else if(charge_from == 'customer'){
        	$("#sp_div").hide();
        	$("#customer_div").show();
        }else{
        	$("#customer_div").hide();
        	$("#sp_div").hide();
        }
    };
    
    
    $("input:radio[name='charge_from_type']").on("click",function(){
    	var charge_from = $("input:radio[name='charge_from_type']:checked").val();
    	display(charge_from);
    });
    
    
    //动作回显
    var charge_from = $("input:radio[name='charge_from_type']:checked").val();
    display(charge_from);
    
    
    
    

} );