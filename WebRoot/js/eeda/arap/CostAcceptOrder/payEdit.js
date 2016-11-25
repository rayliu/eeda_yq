define(['jquery', 'metisMenu', 'sb_admin','./edit_doc_table',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '付款申请单 | '+document.title;
	$('#pay_date').val(eeda.getDate());
	$('#menu_finance').addClass('active').find('ul').addClass('in');
		//datatable, 动态处理
	    var ids = $("#ids").val();
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
		    id: 'CostOrder-table',
		    paging: true,
		    serverSide: true, //不打开会出现排序不对
		    ajax: "/costAcceptOrder/costOrderList?ids="+ids+"&application_id="+$("#application_id").val(),
            createdRow: function ( row, data, index ) {
                $(row).attr('id', data.ID);
                $(row).attr('payee_unit', data.SP_ID);//收款单位
            },
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
            		return '<a href="/costCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
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
    				if($('#application_id').val()==''){
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
    				if($('#application_id').val()==''){
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
    				if($('#application_id').val()==''){
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
    				if($('#application_id').val()==''){
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
    	debugger
    	$("#CostOrder-table input[name='app_usd']").each(function(){
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
    
    
    //保存
	$("#saveBtn").on('click',function(){
		$("#saveBtn").attr("disabled", true);
		$("#printBtn").attr("disabled", true);
	
		orderjson();
	
		if($("#payment_method").val()=='transfers'){
			if($("#deposit_bank").val()=='' && $("#bank_no").val()==''&& $("#account_name").val()==''){
				$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
				return false;
			}
		}
		
		
		$('#docJson').val(JSON.stringify(itemOrder.buildDocItem()));
		$.post('/costAcceptOrder/save',$("#checkForm").serialize(), function(data){
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#application_id").val(data.ID);
				$("#status").val(data.STATUS);
				$("#application_no").val(data.ORDER_NO);
				$("#application_date").val(data.CREATE_STAMP);
				$("#saveBtn").attr("disabled", false);
				$("#printBtn").attr("disabled", false);
				$("#checkBtn").attr('disabled',false);
				$("#deleteBtn").attr("disabled", false);
				eeda.contactUrl("edit?id",data.ID);
				total = 0.00;
				nopay = 0.00;
				pay = 0.00;
				
				//var url = "/costPreInvoiceOrder/costOrderList?application_id="+$("#application_id").val();
				//$('#CostOrder-table').dataTable().fnDraw();
				itemOrder.refleshDocTable(data.ID);
			}else{
				$.scojs_message('确认失败', $.scojs_message.TYPE_FALSE);
			}
		},'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
        });
		
	});
	
	
	  $("#checkBtn").on('click',function(){
		  	$("#checkBtn").attr("disabled", true);
		  	$("#saveBtn").attr("disabled", true);
		  	
		  	orderjson();
		  	
			$.get("/costAcceptOrder/checkStatus", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
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
	  
	  
	  //撤回
	  $("#returnBtn").on('click',function(){
		  	$("#returnBtn").attr("disabled", true);
		  	if(confirm("确定撤回未复核状态？")){
		  		orderjson();
				$.get("/costAcceptOrder/returnOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
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
		  	}else{
		  		$("#returnBtn").attr("disabled", false);
		  	}
		});
	  
	  
	  //付款确认
	  $("#confirmBtn").on('click',function(){
		  	$("#confirmBtn").attr("disabled", true);
		  	orderjson();
			$.get("/costAcceptOrder/confirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val(),pay_time:$('#pay_date').val(),pay_type:$('#pay_type').val(),pay_bank:$('#pay_bank').val()}, function(data){
				if(data.success){
					$("#returnBtn").attr("disabled", true);
					$("#deleteBtn").attr("disabled", true);
					$("#status").val('已付款');
					$("#returnConfirmBtn").attr("disabled", false);
					$.scojs_message('付款成功', $.scojs_message.TYPE_OK);
				}else{
					$("#confirmBtn").attr("disabled", false);
					$.scojs_message('付款失败', $.scojs_message.TYPE_FALSE);
				}
			},'json');
		});
	  
	  //付款确认撤回未确认状态
	  $("#returnConfirmBtn").on('click',function(){
		  	$("#returnConfirmBtn").attr("disabled", true);
		  	if(confirm("确定撤回未付款确认状态？")){
		  		orderjson();
				$.get("/costAcceptOrder/returnConfirmOrder", {application_id:$('#application_id').val(),detailJson:$('#detailJson').val()}, function(data){
					if(data.success){
						$.scojs_message('撤回成功', $.scojs_message.TYPE_OK);
					  	$("#confirmBtn").attr("disabled", false);
					  	$("#returnBtn").attr("disabled", false);
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
	  
	  
	  
	  //撤销单据
	  $("#deleteBtn").on('click',function(){
		  	$("#deleteBtn").attr("disabled", true);
		  	if(confirm("确定撤撤销此单据？返回到上一步重新做单？")){
		  		orderjson();
				$.get("/costAcceptOrder/deleteOrder", {application_id:$('#application_id').val()}, function(data){
					if(data.success){
						$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
						setTimeout(function(){
							location.href="/costAcceptOrder";
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
	
	
	  
	 //异步显示总金额
    $("#CostOrder-table").on('change', 'input', function(){
    	debugger
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
	if($('#application_id').val()==''){
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
	$('#pay_type').change(function(){
		var type = $(this).val();
		if(type == 'cash'){
			$('#pay_type_massage').hide();
		}else{
			$('#pay_type_massage').show();
		}
	})
	
	

});
});