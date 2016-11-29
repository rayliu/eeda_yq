define(['jquery', 'metisMenu', 'sb_admin','./edit_doc_table',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '收款申请单 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    $('#receive_time').val(eeda.getDate());
    
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
            		item[name] = value;
            }
            item.id = id;
            item.action = $('#order_id').val() != ''?'UPDATE':'CREATE';
            items_array.push(item);
        }
        return items_array;
    }
    
	//datatable, 动态处理
    var idsArray = $("#idsArray").val();
    var total_usd = 0;
    var total_cny = 0;
    var total_hkd = 0;
    var total_jpy = 0;
    var nopay_usd = 0;
    var nopay_jpy = 0;
    var nopay_hkd = 0;
    var nopay_cny = 0;
    var pay_usd = 0;
    var pay_cny = 0;
    var pay_hkd = 0;
    var pay_jpy = 0;
   
	var dataTable = eeda.dt({
	    id: 'eeda-table',
	    paging: true,
	    serverSide: true, //不打开会出现排序不对
	    ajax: "/chargeAcceptOrder/chargeOrderList?idsArray="+idsArray+"&application_id="+$("#order_id").val(),
	    initComplete: function( settings ) {
        	$("#total_usd").html(total_usd.toFixed(2));
    		$("#nopay_usd").html(nopay_usd.toFixed(2));
    		$("#pay_usd").html(pay_usd.toFixed(2));
    		$("#app_usd").val(pay_usd.toFixed(2));

    		$("#total_hkd").html(total_hkd.toFixed(2));
    		$("#nopay_hkd").html(nopay_hkd.toFixed(2));
    		$("#pay_hkd").html(pay_hkd.toFixed(2));
    		$("#app_hkd").val(pay_hkd.toFixed(2));

    		$("#total_cny").html(total_cny.toFixed(2));
    		$("#nopay_cny").html(nopay_cny.toFixed(2));
    		$("#pay_cny").html(pay_cny.toFixed(2));
    		$("#app_cny").val(pay_cny.toFixed(2));

    		$("#total_jpy").html(total_jpy.toFixed(2));
    		$("#nopay_jpy").html(nopay_jpy.toFixed(2));
    		$("#pay_jpy").html(pay_jpy.toFixed(2));
    		$("#app_jpy").val(pay_jpy.toFixed(2));
        },
	    columns:[
	         {"data":"ORDER_TYPE","width": "100px","sClass":"order_type"},
	         {"data":"ORDER_NO","width": "120px",
	        	"render": function(data, type, full, meta) {
	        		return '<a href="/chargeCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
	    		}
	         },
	    	{"data":"CNAME","width": "250px"},
			{"data":"USD","width": "100px",
				"render": function(data, type, full, meta) {
					if(data!=null&&!isNaN(data)){
						data = parseFloat(data).toFixed(2);
						total_usd = total_usd + parseFloat(data);
					}
					return data;
				}
			},
			{"data":"PAID_USD","width": "100px","class":"to_pay_usd",
				"render": function(data, type, full, meta) {
					if(data!=null&&!isNaN(data)&&full.USD!=null&&!isNaN(full.USD)){
						nopay_usd = nopay_usd + parseFloat(full.USD-data);
						data = parseFloat(full.USD-data).toFixed(2);
					}
					return data;
				}
			},
			{"data":"PAID_USD","width": "100px",
				"render": function(data, type, full, meta) {
					if($('#order_id').val()==''){
						if(data!=null&&full.USD!=null&&!isNaN(data)&&!isNaN(full.USD)){
							pay_usd = pay_usd + parseFloat(full.USD-data);
							data = parseFloat(full.USD-data).toFixed(2);
						}
					}else{
						if(data!=null&&!isNaN(data)){
							pay_usd = pay_usd + parseFloat(data);
							data = parseFloat(data).toFixed(2);
						}
					}
					return "<input type ='text' name='app_usd' style='width:80px' value='"+data+"'>";
				}
			},
			{"data":"HKD","width": "100px",
				"render": function(data, type, full, meta) {
					if(data!=null&&!isNaN(data)){
	    				data = parseFloat(data).toFixed(2);
	    				total_hkd = total_hkd + parseFloat(data);
					}
					return data;
				}
			},
			{"data":"PAID_HKD","width": "100px","class":"to_pay_hkd",
				"render": function(data, type, full, meta) {
					if(data!=null&&full.HKD!=null&&!isNaN(data)&&!isNaN(full.HKD)){
						nopay_hkd = nopay_hkd + parseFloat(full.HKD-data);
	    				data = parseFloat(full.HKD-data).toFixed(2);
					}
					return data;
				}
			},
			{"data":"PAID_HKD","width": "100px",
				"render": function(data, type, full, meta) {
					if($('#order_id').val()==''){
						if(!isNaN(data)&&full.HKD!=null&&!isNaN(full.HKD)&&data!=null){
							pay_hkd = pay_hkd + parseFloat(full.HKD-data);
							data = parseFloat(full.HKD-data).toFixed(2);
						}
					}else{
						if(data!=null&&!isNaN(data)){
							pay_hkd = pay_hkd + parseFloat(data);
							data = parseFloat(data).toFixed(2);
						}
					}
					return "<input type ='text' name='app_hkd' style='width:80px' value='"+data+"'>";
				}
			},
			{"data":"CNY","width": "100px",
				"render": function(data, type, full, meta) {
					if(!isNaN(data)&&data!=null){
	    				data = parseFloat(data).toFixed(2);
	    				total_cny = total_cny + parseFloat(data);
					}
					return data;
				}
			},
			{"data":"PAID_CNY","width": "100px","class":"to_pay_cny",
				"render": function(data, type, full, meta) {
					if(data!=null&&!isNaN(data)&&full.CNY!=null&&!isNaN(full.CNY)){
						nopay_cny = nopay_cny + parseFloat(full.CNY-data);
	    				data = parseFloat(full.CNY-data).toFixed(2);
					}
					return data;
				}
			},
			{"data":"PAID_CNY","width": "100px",
				"render": function(data, type, full, meta) {
					if($('#order_id').val()==''){
						if(data!=null&&!isNaN(data)&&full.CNY!=null&&!isNaN(full.CNY)){
							pay_cny = pay_cny + parseFloat(full.CNY-data);
							data = parseFloat(full.CNY-data).toFixed(2);
						}
					}else{
						if(data!=null&&!isNaN(data)){
							pay_cny = pay_cny + parseFloat(data);
							data = parseFloat(data).toFixed(2);
						}
					}
					return "<input type ='text' name='app_cny' style='width:80px' value='"+data+"'>";
				}
			},
			{"data":"JPY","width": "100px",
				"render": function(data, type, full, meta) {
					if(!isNaN(data)&&data!=null){
	    				data = parseFloat(data).toFixed(2);
	    				total_jpy = total_jpy + parseFloat(data);
					}
					return data;
				}
			},
			{"data":"PAID_JPY","width": "100px","class":"to_pay_jpy",
				"render": function(data, type, full, meta) {
					if(data!=null&&!isNaN(data)&&full.JPY!=null&&!isNaN(full.JPY)){
						nopay_jpy = nopay_jpy + parseFloat(full.JPY-data);
	    				data = parseFloat(full.JPY-data).toFixed(2);
					}
					return data;
				}
			},
			{"data":"PAID_JPY","width": "100px",
				"render": function(data, type, full, meta) {
					if($('#order_id').val()==''){
						if(data!=null&&!isNaN(data)&&full.JPY!=null&&!isNaN(full.JPY)){
							pay_jpy = pay_jpy + parseFloat(full.JPY-data);
							data = parseFloat(full.JPY-data).toFixed(2);
						}
					}else{
						if(data!=null&&!isNaN(data)){
							pay_jpy = pay_jpy + parseFloat(data);
							data = parseFloat(data).toFixed(2);
						}
					}
					return "<input type ='text' name='app_jpy' style='width:80px' value='"+data+"'>";
				}
			},
			{"data":"CREATOR_NAME","width": "120px"},
			{"data":"CREATE_STAMP","width": "150px"},
			{"data":"REMARK","width": "150px"}
	    ]      
    });	
    

	var orderjson = function(){
    	var array=[];
    	var sum_usd=0.0;
    	var sum_cny=0.0;
    	var sum_hkd=0.0;
    	var sum_jpy=0.0;
    	$("#eeda-table input[name='app_usd']").each(function(){
    		var obj={};
    		obj.id = $(this).parent().parent().attr('id');
    		obj.order_type = $(this).parent().parent().find('.order_type').text();
    		obj.payee_unit = $(this).parent().parent().attr('payee_unit');
    		
    		obj.app_usd = $(this).val();
    		obj.app_cny = $(this).parent().parent().find('[name=app_cny]').val();
    		obj.app_hkd = $(this).parent().parent().find('[name=app_hkd]').val();
    		obj.app_jpy = $(this).parent().parent().find('[name=app_jpy]').val();
    		
    		sum_usd +=parseFloat(obj.app_usd);
    		sum_cny +=parseFloat(obj.app_cny);
    		sum_hkd +=parseFloat(obj.app_hkd);
    		sum_jpy +=parseFloat(obj.app_jpy);
    		array.push(obj);
    	});
    	
    	$("#total_app_jpy").val(sum_jpy);
    	$("#total_app_usd").val(sum_usd);
    	$("#total_app_hkd").val(sum_hkd);
    	$("#total_app_jpy").val(sum_jpy);
    	var str_JSON = JSON.stringify(array);
    	$("#detailJson").val(str_JSON);
    };
    
	
    //申请保存
	$("#saveBtn").on('click',function(){
		$(this).attr("disabled", true);
		$("#printBtn").attr("disabled", true);
		
		orderjson();
	
		if($("#payment_method").val()=='transfers'){
			if($("#deposit_bank").val()=='' && $("#bank_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		
		var order = buildOrder();
		order.item_list = buildItem();
		order.doc_list = itemOrder.buildDocItem();
		
		$.get('/chargeAcceptOrder/save',{params:JSON.stringify(order)}, function(data){
			$("#saveBtn").attr("disabled", false);
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
				itemOrder.refleshDocTable(data.ID);
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
		  	
		  	if($("#receive_type").val()=='transfers'){
				if($("#receive_bank").val()==''){
					$.scojs_message('收入银行不能为空', $.scojs_message.TYPE_FALSE);
					return false;
				}
			}
			
			var order = buildOrder();
			order.item_list = buildItem();
		  	
			$.get("/chargeAcceptOrder/confirmOrder", {params:JSON.stringify(order)}, function(data){
				if(data.success){
					$("#status").val('已收款');
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
	    $("#eeda-table").on('change', 'input', function(){
	    	var tr =$(this).parent().parent();
			var value = 0.00;
			var currentValue = $(this).val();
			if(currentValue==''||isNaN(currentValue)){
				$(this).val('');
				$(this).val(0);
				return;
			}
			var name = $(this).attr('name');
			if(name=='app_usd'){
				var totalAmount = tr.find('.to_pay_usd').text();
			}else if(name == 'app_cny'){
				var totalAmount = tr.find('.to_pay_cny').text();
			}else if(name == 'app_hkd'){
				var totalAmount = tr.find('.to_pay_hkd').text();
			}else{
				var totalAmount = tr.find('.to_pay_jpy').text();
			}
			if(parseFloat(totalAmount)-parseFloat(currentValue)<0){
				$(this).val('');
				$(this).val(0);
				$.scojs_message('支付金额不能大于待付金额', $.scojs_message.TYPE_FALSE);
				return false;
			}
			$("input[name='"+name+"']").each(function(){
				if($(this).val()!=null&&$(this).val()!=''){
					value = value + parseFloat($(this).val());
				}
		    });		
			
			var name1 = name.replace("app","pay");
			$('#'+name+'').val(value);
			$('#'+name1+'').html(value);
		});	
    
 
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
		if(type == 'cash'){
			$('#transfers_massage').hide();
		}else{
			$('#transfers_massage').show();
		}
	})
	
	//发票类型（1）
	$('#invoice_type').change(function(){
		var type = $(this).val();
		if(type == 'wbill'){
			$('#invoiceDiv').hide();
		}else{
			$('#invoiceDiv').show();
		}
	})
	
	//付款方式回显（2）
	$('#receive_type').change(function(){
		var type = $(this).val();
		if(type == 'cash'){
			$('#receive_type_massage').hide();
		}else{
			$('#receive_type_massage').show();
		}
	})
	
	var ids = [];
	var applied_arap_id = [];
	var itemTable = eeda.dt({
        id: 'charge-table',
        columns:[
	        {"data": "ID",
	        	"render": function ( data, type, full, meta ) {
	        		var str = '<input type="checkbox" style="width:30px">';
	        		for(var i=0;i<ids.length;i++){
	                    if(ids[i]==data){
	                   	 str = '<input type="checkbox" style="width:30px" checked>';
	                    }
	                }
	        		return str;
			    }
	        },
	        { "data": "ORDER_NO"},
	        { "data": "TYPE"},
	        { "data": "CREATE_STAMP"},
	        { "data": "SP_NAME"},
	        { "data": "CURRENCY_NAME","class":"currency_name"},
	        { "data": "TOTAL_AMOUNT","class":"total_amount",
	        	"render": function ( data, type, full, meta ) {
	        		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
	                return data;
	              }
	        },
	        { "data": "EXCHANGE_RATE"},
	        { "data": "AFTER_TOTAL",
	        	"render": function ( data, type, full, meta ) {
	        		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
	                return data;
	              }
	        },
	        { "data": "NEW_RATE"},
	        { "data": "AFTER_RATE_TOTAL",
	        	"render": function ( data, type, full, meta ) {
	        		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
	                return data;
	              }
	        },
	        { "data": "EXCHANGE_CURRENCY_NAME","class":"EXCHANGE_CURRENCY_NAME"},
	        { "data": "EXCHANGE_CURRENCY_RATE"},
	        { "data": "EXCHANGE_TOTAL_AMOUNT","class":"EXCHANGE_TOTAL_AMOUNT"},
	        { "data": "ORDER_TYPE", "visible": false,
	            "render": function ( data, type, full, meta ) {
	                if(!data)
	                    data='';
	                return data;
	            }
	        },
	      ]
	});
	
	
	$('#eeda-table').on('click','td',function(){
		
		$('#chargeAlert').click();
		var order_id = $(this).parent().attr('id');
		$('#chargeAlert').attr('name',order_id);
		var url = "/chargeCheckOrder/tableList?order_id="+order_id;
    	itemTable.ajax.url(url).load();
	})
	
	
	$('#alertChargeDetail').on('click','.btn-primary',function(){
		var usd = 0 ;
		var cny = 0 ;
		var hkd = 0 ;
		var jpy = 0 ;
		$('#charge-table input[type=checkbox]:checked').each(function(){
			var tr = $(this).parent().parent();
			var EXCHANGE_CURRENCY_NAME = tr.find('.EXCHANGE_CURRENCY_NAME').text();
			var EXCHANGE_TOTAL_AMOUNT = tr.find('.EXCHANGE_TOTAL_AMOUNT').text();
			if(EXCHANGE_TOTAL_AMOUNT!=''){
				if(EXCHANGE_CURRENCY_NAME=='CNY'){
					cny += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='USD'){
					usd += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='HKD'){
					hkd += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='JPY'){
					jpy += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}
			}else{
				EXCHANGE_CURRENCY_NAME = tr.find('.currency_name').text();
				EXCHANGE_TOTAL_AMOUNT = tr.find('.total_amount').text();
				if(EXCHANGE_CURRENCY_NAME=='CNY'){
					cny += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='USD'){
					usd += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='HKD'){
					hkd += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}else if(EXCHANGE_CURRENCY_NAME=='JPY'){
					jpy += parseFloat(EXCHANGE_TOTAL_AMOUNT);
				}
			}
			debugger
			var tr_id = $('#chargeAlert').attr('name');
			$('#'+tr_id+'').find('[name=app_usd]').val(usd);
			
		})
	})
	

});
});