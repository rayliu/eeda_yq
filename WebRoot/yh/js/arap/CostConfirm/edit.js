$(document).ready(function() {
	document.title = '付款确认单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var orderIds = $("#orderIds").val();
    var order_type = $("#order_type").val();
    var total = 0.00;
    var nopay = 0.00;
    var datatable=$('#InvorceApplication-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/applicationList?orderIds="+orderIds+"&order_type="+$("#order_type").val(),
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({nopay_amount: aData.NOPAY_AMOUNT}); 
			$(nRow).attr({total_amount: aData.PAY_AMOUNT}); 
			return nRow;
		},
        "aoColumns": [   
             {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(order_type == '报销单')
            			return "<a href='/costReimbursement/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else if(order_type == '成本单')
            			return "<a href='/costMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else if(order_type == '行车单')
            			return "<a href='/carsummary/edit?carSummaryId="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else
            			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"PAY_AMOUNT",
    			"fnRender": function(obj) {
    				total = total + parseFloat(obj.aData.PAY_AMOUNT) ;
    				$("#total").html(total);
    				$("#total_amount").val(total);
    				if($("#status").val()=='' ){
//    					$("#nopay_amount").val(total);
//    					$("#pay_amount").val(total);
    				}else{
    					if($("#status").val()=='部分已付款'){
    						$("#saveBtn").attr("disabled", true);
    						$("#savePayConfirmBtn").attr("disabled", false);
    					}else if($("#status").val()=='已付款'){
    						$("#saveBtn").attr("disabled", true);
    						$("#savePayConfirmBtn").attr("disabled", true);
    					}
    				}
    				return obj.aData.PAY_AMOUNT;
    			}
        	},
        	/*{"mDataProp":"NOPAY_AMOUNT", bVisible: false,
        		"fnRender": function(obj) {
        			$("#nopay_one").val(obj.aData.NOPAY_AMOUNT);
        			nopay = nopay + parseFloat(obj.aData.NOPAY_AMOUNT) ;
    				//$("#nopay_total").html(nopay);
    				$("#nopay_amount").val(nopay);
    				$("#pay_amount").val(nopay);
    				//$("#nopay_total").html(0);
    				$("#total_pays").html(nopay);
    				return "<span name='nopay_amounts'>"+0+"</span>";
        		}
        	},*/
        	{"mDataProp":null,
  	            "fnRender": function(obj) {
  	            	nopay = nopay + parseFloat(obj.aData.NOPAY_AMOUNT) ;
  	            	$("#total_pays").html(nopay);
  	            	$("#pay_amount").val(nopay);
  	            	$("#total_nopay").val(nopay);
  	            	$("#nopay_amount").val(0);
  	            	return	"<input type='text' name='pay_amounts' value='"+obj.aData.NOPAY_AMOUNT+"'>";
  	            }
            },
            {"mDataProp":"COST_STAMP"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"},
            {"mDataProp":null,"sWidth": "150px"}                       
        ]      
    });	
    
    //异步录入支付金额
    $("#InvorceApplication-table").on('input', 'input', function(e){
		e.preventDefault();
		var value = 0.00;
		var currentValue = $(this).val();
//		if(currentValue==''){
//			return ;
//		}
		//var row = $(this).parent().parent();
		//var $leftAmountObj = $(row.find("span[name='nopay_amounts']")[0]);
		var $totalAmount = $(this).parent().parent().attr('total_amount');
		//var $totalAmount = $("#nopay_one").val();
		if(parseFloat($totalAmount)-parseFloat(currentValue)<0){
			$(this).val(0);
			$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
			return false;
		}/*else{
			$leftAmountObj.text(parseFloat($totalAmount)-parseFloat(currentValue));
			$("#nopay_amount").text(parseFloat($totalAmount)-parseFloat(currentValue));
		}*/
		
		
		$("input[name='pay_amounts']").each(function(){
			if($(this).val()!=null&&$(this).val()!=''){
				/*if(parseFloat($(this).val())>parseFloat($(this).parent().parent().attr('nopay_amount'))){
					$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
					$(this).val(0);
					return false;
				}*/
				
				value = value + parseFloat($(this).val());
//	    		$("#total_pays").html(value);
//	    		$("#pay_amount").val(value);
//	    		$("#nopay_amount").val(parseFloat($("#total_amount").val())-parseFloat(value));
			}else{
				$("#InvorceApplication-table").on('blur', 'input', function(e){
					$(this).val(0);
				});
//				$("#total_pays").html(value);
//	    		$("#pay_amount").val(value);
			}
			$("#total_pays").html(value);
    		$("#pay_amount").val(value);
    		$("#nopay_amount").val(parseFloat($("#total_nopay").val())-parseFloat(value));
	    });		
	});	
    

   
  //银行
    $('#pay_bank').on('input click', function(){
    	var me = this;
		var inputStr = $('#pay_bank').val();
		var bankList =$("#bankList");
		bankList.empty();
		$.get('/costConfirm/searchAllAccount', {input:inputStr}, function(data){
			if(data.length > 0){
				 var accountTypeSelect = $("#accountTypeSelect");
				 accountTypeSelect.empty();
				 var hideAccountId = $("#hideAccountId").val();
				 accountTypeSelect.append("<option ></option>");
				 for(var i=0; i<data.length; i++){
					bankList.append("<li><a tabindex='-1' class='fromLocationItem' id='"+data[i].ID+"' bank_name='"+data[i].BANK_NAME+"' bank_person='"+data[i].BANK_PERSON+"' account_no='"+data[i].ACCOUNT_NO+"', >"+data[i].BANK_PERSON+" "+data[i].BANK_NAME+" "+data[i].ACCOUNT_NO+"</a></li>");
				 }
			}
			bankList.css({ 
	        	left:$(me).position().left+"px", 
	        	top:$(me).position().top+32+"px" 
	        }); 
			
			bankList.show();
			
		},'json');
    });
    
    // 没选中供应商，焦点离开，隐藏列表
	$('#pay_bank').on('blur', function(){
 		$('#bankList').hide();
 	});

	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
	$('#bankList').on('blur', function(){
 		$('#bankList').hide();
 	});

	$('#bankList').on('mousedown', function(){
		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	});

	// 选中银行列表
	$('#bankList').on('mousedown', '.fromLocationItem', function(e){
		console.log($('#bankList').is(":focus"));
		var message = $(this).text();
		$('#pay_bank').val(message.substring(0, message.indexOf(" ")));
	    $('#pay_bank').val($(this).attr('bank_name'));
	    $('#account_id').val($(this).attr('id'));
	    $('#pay_account_no').val($(this).attr('account_no'));
        $('#bankList').hide();
        refreshData();
    });
    
	$("#pay_type").on('mouseup click',function(){
		if($("#pay_type").val() == 'cash'){
			$("#pay_bank").val('');
			$("#pay_account_no").val('');
			$("#pay_bank").attr("disabled",true);
			$('#pay_account_no').attr("disabled",true);
			
		}else if($("#pay_type").val() == 'transfers'){
			$("#pay_bank").attr("disabled",false);
			$("#pay_account_no").attr("disabled",false);	
		};
	});
	
	
	
		
	//付款保存
	$("#saveBtn").on('click',function(){
		
		$.get('/costConfirm/save',$("#confirmForm").serialize(), function(data){
			if(data.ID >=0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#confirmId").val(data.ID);
				contactUrl("edit?id",data.ID);
				$("#saveBtn").attr("disabled", true);
				$("#savePayConfirmBtn").attr("disabled", false);
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
	});
	
	
	//付款确认
	$("#savePayConfirmBtn").on('click',function(){
		var array=[];
		$("#InvorceApplication-table input[name='pay_amounts']").each(function(){
			var obj={};
			obj.id = $(this).parent().parent().attr('id');
			obj.value = $(this).val();
			array.push(obj);
		});
		var str_JSON = JSON.stringify(array);
		console.log(str_JSON);
		$("#detailJson").val(str_JSON);
		
		if($("#nopay_amount").val()=='0'){
			$("#savePayConfirmBtn").attr("disabled", true);
		}else{
			$("#savePayConfirmBtn").attr("disabled", false);
		}
		
		if($("#pay_type").val()=='transfers'){
			if($("#pay_bank").val()==''){
				$.scojs_message('转账的账户不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}

		$.get('/costConfirm/saveConfirmLog',$("#confirmForm").serialize(), function(data){
			if(data.arapCostPayConfirmOrder.ID>0){
				logTable.fnSettings().sAjaxSource="/costConfirm/logList?confirmId="+$("#confirmId").val();
				logTable.fnDraw();
				$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
				if(data.arapCostPayConfirmOrder.STATUS=='已付款'){
				    $("#savePayConfirmBtn").attr("disabled", true);
			    }
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
			//$("#nopay_one").val($("#nopay_amount").val());
			total = 0.00;
		    nopay = 0.00;
		    $("#total_pays").html(0);
			datatable.fnDraw();
		},'json');
		
	});
	
	
	var logTable = $('#log-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/logList?confirmId="+$("#confirmId").val(),
        "aoColumns": [  
            {"mDataProp":"ID"},
            {"mDataProp":"PAY_OUT_BANK_NAME"},
            {"mDataProp":"PAY_OUT_ACCOUNT_NO"},
            {"mDataProp":"PAY_TYPE",
            	"fnRender": function(obj) {
    				if(obj.aData.PAY_TYPE=='cash'){
    					return '现金';
    				}else if(obj.aData.PAY_TYPE=='transfers'){
    					return '转账';
    				}else{
    					return '';
    				}
            	}
            },
            {"mDataProp":"AMOUNT"},
            {"mDataProp":"CREATE_DATE"},
            {"mDataProp":"C_NAME"}
        ]      
	});
	
	
	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#pay_time').trigger('keyup');
    });
	
	
});