define(['jquery', 'metisMenu', 'sb_admin',
    './edit_doc_table', './payEdit_select_item', 
 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, sb, doc, selectContr) {
$(document).ready(function() {
	document.title = '付款申请单 | '+document.title;
	$('#pay_date').val(eeda.getDate());

	$('#menu_finance').addClass('active').find('ul').addClass('in');
		//datatable, 动态处理
	    var ids = $("#ids").val();
        var selected_item_ids = $("#selected_item_ids").val();
        var total_usd = 0;
        var total_cny = 0;
        var total_hkd = 0;
        var total_jpy = 0;
        //待申请
        var wait_pay_usd = 0;
        var wait_pay_jpy = 0;
        var wait_pay_hkd = 0;
        var wait_pay_cny = 0;
        //本次应付
        var apply_pay_usd_total = 0;
        var apply_pay_cny_total = 0;
        var apply_pay_hkd_total = 0;
        var apply_pay_jpy_total = 0;

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
        		$("#nopay_usd").html(wait_pay_usd.toFixed(2));
        		$("#pay_usd").html(apply_pay_usd_total.toFixed(2));
        		$("#app_usd").val(apply_pay_usd_total.toFixed(2));

        		$("#total_hkd").html(total_hkd.toFixed(2));
        		$("#nopay_hkd").html(wait_pay_hkd.toFixed(2));
        		$("#pay_hkd").html(apply_pay_hkd_total.toFixed(2));
        		$("#app_hkd").val(apply_pay_hkd_total.toFixed(2));

        		$("#total_cny").html(total_cny.toFixed(2));
        		$("#nopay_cny").html(wait_pay_cny.toFixed(2));
        		$("#pay_cny").html(apply_pay_cny_total.toFixed(2));
        		$("#app_cny").val(apply_pay_cny_total.toFixed(2));

        		$("#total_jpy").html(total_jpy.toFixed(2));
        		$("#nopay_jpy").html(wait_pay_jpy.toFixed(2));
        		$("#pay_jpy").html(apply_pay_jpy_total.toFixed(2));
        		$("#app_jpy").val(apply_pay_jpy_total.toFixed(2));

                selectContr.refleshSelectTable(ids, selected_item_ids);
            },
		    columns:[
             {"data":"ORDER_TYPE","width": "100px","sClass":"order_type"},
             {"data":"ORDER_NO","width": "120px",
            	"render": function(data, type, full, meta) {
            		return '<a href="/costCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
        		}
             },
        	{"data":"CNAME","width": "250px"},
            {"data":"CNY","width": "100px",
                "render": function(data, type, full, meta) {
                    if(!isNaN(data)&&data!=null){
                        data = parseFloat(data).toFixed(2);
                        total_cny = total_cny + parseFloat(data);
                    }
                    return data;
                }
            },
            {"data":"WAIT_CNY","width": "100px","class":"to_pay_cny",//待申请
                "render": function(data, type, full, meta) {
                    wait_pay_cny += data;//parseFloat(data).toFixed(2);
                    return data;
                }
            },
            {"data":"APPLY_PAY_CNY","width": "100px",
                "render": function(data, type, full, meta) {
                    apply_pay_cny_total += data;
                    return '<span name="wait_cny">'+data+'</span>';
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
    		{"data":"WAIT_HKD","width": "100px","class":"to_pay_hkd",
    			"render": function(data, type, full, meta) {
    				wait_pay_hkd += data;
    				return data;
    			}
    		},
    		{"data":"APPLY_PAY_HKD","width": "100px",
                "render": function(data, type, full, meta) {
                    apply_pay_hkd_total += data;
                    return '<span name="wait_hkd">'+data+'</span>';
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
    		{"data":"WAIT_JPY","width": "100px","class":"to_pay_jpy",
    			"render": function(data, type, full, meta) {
    				wait_pay_jpy += data;
    				return data;
    			}
    		},
    		{"data":"APPLY_PAY_JPY","width": "100px",
                "render": function(data, type, full, meta) {
                    apply_pay_jpy_total += data;
                    return '<span name="wait_jpy">'+data+'</span>';
                }
            },
            {"data":"USD","width": "100px",
                "render": function(data, type, full, meta) {
                    if(data!=null&&!isNaN(data)){
                        data = parseFloat(data).toFixed(2);
                        total_usd = total_usd + parseFloat(data);
                    }
                    return data;
                }
            },
            {"data":"WAIT_USD","width": "100px","class":"to_pay_usd",
                "render": function(data, type, full, meta) {
                    wait_pay_usd += data;
                    return data;
                }
            },
            {"data":"APPLY_PAY_USD","width": "100px",//WAIT_USD
                "render": function(data, type, full, meta) {
                    apply_pay_usd_total += data;
                    return '<span name="wait_usd">'+data+'</span>';
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
    	
        //获取所选的对账单id
    	$("#CostOrder-table tr[id]").each(function(index, item){
    		var obj={};
    		obj.id = $(item).attr('id');
    		obj.order_type = $(item).find('.order_type').text();
    		obj.payee_unit = $(item).attr('payee_unit');

    		obj.app_usd = $(item).find('[name=wait_usd]').text();
    		obj.app_cny = $(item).find('[name=wait_cny]').text();
    		obj.app_hkd = $(item).find('[name=wait_hkd]').text();
    		obj.app_jpy = $(item).find('[name=wait_jpy]').text();

    		sum_usd +=parseFloat(obj.app_usd);
    		sum_cny +=parseFloat(obj.app_cny);
    		sum_hkd +=parseFloat(obj.app_hkd);
    		sum_jpy +=parseFloat(obj.app_jpy);
    		array.push(obj);
    	});
    	
    	$("#total_app_cny").val(sum_cny);
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
			if($(".deposit_bank").val()=='' && $(".bank_no").val()==''&& $(".account_name").val()==''){
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
				if(data){
					$("#returnBtn").attr("disabled", true);
					$("#deleteBtn").attr("disabled", true);
					$("#confirm_name").val(data.CONFIRM_NAME);
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
	
	$("#CostOrder-table").on('click', 'button', function(e){
        e.preventDefault();
        var row = dataTable.row($(this).parent().parent());
        var row_index = row.index();//  td
        $('#select_row_index').val(row_index);

        var order_id = $(e.target).attr("order_id");
        var selected_ids = row.data().SELECTED_IDS;

        selectContr.refleshSelectTable(order_id, selected_ids);
        $('#select_item_modal').modal('show');

    });

	$('#select_item_table').on('click',"input[type=checkbox]",function () {
        var table = $('#select_item_table').DataTable();
        var row = $(this).parent().parent();
        var cell = table.cell($(this).parent());//  td
        var pay_flag='N';
        if($(this).prop('checked')==true){
            pay_flag='Y';
        }
        //注意 - call draw() 更新table.data()中的数据
        cell.data(pay_flag).draw();
        selectContr.calcTotal();

        var selected_ids=[];
        table.data().each(function(item, index) {
            if(item.PAY_FLAG == 'N')
                return;
            selected_ids.push(item.ID);
        });
        $('#selected_ids').val(selected_ids);

        
        //获取对账单号
        var costOrderNo = row.find('td:eq(1)').text();
        console.log(costOrderNo);
        //更新对应对账单的汇总金额
        calcOrderTotal(costOrderNo);
    });

    var calcOrderTotal=function(costOrderNo) {
        //根据对账单号获取 ’业务单据‘表格的行
        var row_index = $("#CostOrder-table td:contains("+costOrderNo+")").parent().index();
        var row = dataTable.row(row_index);
        var row_data = row.data();
        console.log(row_data);
        row_data.APPLY_PAY_CNY=parseFloat($('#modal_cny').val());
        row_data.APPLY_PAY_HKD=parseFloat($('#modal_hkd').val());
        row_data.APPLY_PAY_JPY=parseFloat($('#modal_jpy').val());
        row_data.APPLY_PAY_USD=parseFloat($('#modal_usd').val());
        row_data.SELECTED_IDS=$('#selected_ids').val();
        row.data(row_data);

        $("#pay_cny").html(parseFloat($('#modal_cny').val()).toFixed(2));
        $("#pay_hkd").html(parseFloat($('#modal_hkd').val()).toFixed(2));
        $("#pay_usd").html(parseFloat($('#modal_usd').val()).toFixed(2));
        $("#pay_jpy").html(parseFloat($('#modal_jpy').val()).toFixed(2));
        //dataTable.draw();
        console.log(row.data());
    };

	 //异步显示总金额
    $("#CostOrder-table").on('change', 'input', function(){x
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
		if(type == 'cash'||type == ''){
			$('#transfers_massage_cny').hide();
			$('#transfers_massage_usd').hide();
			$('#transfers_massage_hkd').hide();
			$('#pay_type_massage').hide();
		}else{
			$('#transfers_massage_cny').show();
			$('#transfers_massage_usd').show();
			$('#transfers_massage_hkd').show();
			$('#pay_type_massage').show();
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