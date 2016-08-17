$(document).ready(function() {
	document.title = '收款确认单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var orderIds = $("#orderIds").val();
    var order_type = $("#order_type").val();
    var total = 0.00;
    var noreceive = 0.00;
    var datatable=$('#InvorceApplication-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeConfirm/applicationList?orderIds="+$("#orderIds").val()+'&order_type='+$("#order_type").val(),
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({noreceive_amount: aData.NORECEIVE_AMOUNT}); 
			$(nRow).attr({total_amount: aData.RECEIVE_AMOUNT}); 
			return nRow;
		},
        "aoColumns": [   
             {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(order_type=='手工收入单')
            			return "<a href='/chargeMiscOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		else
            			return "<a href='/chargeInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"TOTAL_AMOUNT",
    			"fnRender": function(obj) {
    				total = total + parseFloat(obj.aData.TOTAL_AMOUNT) ;
    				$("#total").html(total);
    				$("#total_amount").val(total);
    				if($("#status").val()=='' ){
    				}else{
    					if($("#status").val()=='部分已收款'){
    						$("#saveBtn").attr("disabled", true);
    						$("#saveReceiveConfirmBtn").attr("disabled", false);
    					}else if($("#status").val()=='已收款'){
    						$("#saveBtn").attr("disabled", true);
    						$("#saveReceiveConfirmBtn").attr("disabled", true);
    					}
    				}
    				return obj.aData.TOTAL_AMOUNT;
    			}
        	},
        	{"mDataProp":null,
  	            "fnRender": function(obj) {
  	            	noreceive = noreceive + parseFloat(obj.aData.NORECEIVE_AMOUNT) ;
  	            	$("#total_receives").html(noreceive);
  	            	$("#receive_amount").val(noreceive);
  	            	$("#total_noreceive").val(noreceive);
  	            	$("#noreceive_amount").val(0);
  	            	return	"<input type='text' name='receive_amounts' value='"+obj.aData.NORECEIVE_AMOUNT+"'>";
  	            }
            },
            {"mDataProp":"CREATE_STAMP"},
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
		var $totalAmount = $(this).parent().parent().attr('total_amount');
		if(parseFloat($totalAmount)-parseFloat(currentValue)<0){
			$(this).val(0);
			$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
			return false;
		}
		
		$("input[name='receive_amounts']").each(function(){
			if($(this).val()!=null&&$(this).val()!=''){
				value = value + parseFloat($(this).val());
			}else{
				$("#InvorceApplication-table").on('blur', 'input', function(e){
					$(this).val(0);
				});
			}
			$("#total_receives").html(value);
    		$("#receive_amount").val(value);
    		$("#noreceive_amount").val(parseFloat($("#total_noreceive").val())-parseFloat(value));
	    });		
	});	
    

  
    
  //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#receive_bank').on('input click', function(){
    	var me = this;
		var inputStr = $('#receive_bank').val();
		var bankList =$("#bankList");
		bankList.empty();
		$.get('/chargeConfirm/searchAllAccount', {input:inputStr}, function(data){
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
	$('#receive_bank').on('blur', function(){
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
		$('#receive_bank').val(message.substring(0, message.indexOf(" ")));
	    $('#receive_bank').val($(this).attr('bank_name'));
	    $('#receive_account_no').val($(this).attr('account_no'));
        $('#bankList').hide();
        refreshData();
    });
    
	$("#receive_type").on('mouseup click',function(){
		if($("#receive_type").val() == 'cash'){
			
		};
	});
	
	
	
		
	//付款保存
	$("#saveBtn").on('click',function(){
		
		$.get('/chargeConfirm/save',$("#confirmForm").serialize(), function(data){
			if(data.ID >=0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#confirmId").val(data.ID);
				contactUrl("edit?id",data.ID);
				$("#saveBtn").attr("disabled", true);
				$("#saveReceiveConfirmBtn").attr("disabled", false);
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
	});
	
	
	//付款确认
	$("#saveReceiveConfirmBtn").on('click',function(){
		var array=[];
		$("#InvorceApplication-table input[name='receive_amounts']").each(function(){
			var obj={};
			obj.id = $(this).parent().parent().attr('id');
			obj.value = $(this).val();
			array.push(obj);
		});
		var str_JSON = JSON.stringify(array);
		console.log(str_JSON);
		$("#detailJson").val(str_JSON);
		
		if($("#noreceive_amount").val()=='0'){
			$("#saveReceiveConfirmBtn").attr("disabled", true);
		}else{
			$("#saveReceiveConfirmBtn").attr("disabled", false);
		}

		$.get('/chargeConfirm/saveConfirmLog',$("#confirmForm").serialize(), function(data){
			if(data.arapChargeReceiveConfirmOrder.ID>0){
				logTable.fnSettings().sAjaxSource="/chargeConfirm/logList?confirmId="+$("#confirmId").val();
				logTable.fnDraw();
				$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
				if(data.arapChargeReceiveConfirmOrder.STATUS=='已付款'){
				    $("#saveReceiveConfirmBtn").attr("disabled", true);
			    }
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}

			total = 0.00;
		    noreceive = 0.00;
		    $("#total_receives").html(0);
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
        "sAjaxSource": "/chargeConfirm/logList?confirmId="+$("#confirmId").val(),
        "aoColumns": [  
            {"mDataProp":"ID"},
            {"mDataProp":"RECEIVE_IN_BANK_NAME"},
            {"mDataProp":"RECEIVE_IN_ACCOUNT_NO"},
            {"mDataProp":"RECEIVE_TYPE",
            	"fnRender": function(obj) {
    				if(obj.aData.RECEIVE_TYPE=='cash'){
    					return '现金';
    				}else if(obj.aData.RECEIVE_TYPE=='transfers'){
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
        $('#receive_time').trigger('keyup');
    });
	
	
});