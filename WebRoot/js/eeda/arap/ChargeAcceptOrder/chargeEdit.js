define(['jquery', 'metisMenu', 'sb_admin','./edit_doc_table', './chargeEdit_select_item', 'dataTablesBootstrap', 
        'validate_cn', 'sco'], function ($, metisMenu, sb, doc, selectContr) {
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
    //本次应收
    var apply_pay_usd_total = 0;
    var apply_pay_cny_total = 0;
    var apply_pay_hkd_total = 0;
    var apply_pay_jpy_total = 0;
   
	var dataTable = eeda.dt({
	    id: 'eeda-table',
	    paging: true,
	    serverSide: true, //不打开会出现排序不对
	    ajax: "/chargeAcceptOrder/chargeOrderList?ids="+ids+"&application_id="+$("#order_id").val(),
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
	        		return '<a href="/chargeCheckOrder/edit?id='+full.ID+'">'+data+'</a>';
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
			{"data":"WAIT_CNY","width": "100px","class":"to_pay_cny",
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
			{"data":"APPLY_PAY_USD","width": "100px",
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
    	$("#eeda-table tr[id]").each(function(index, item){
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
	  
	  
	  
	  $("#eeda-table").on('click', 'button', function(e){
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
	        var charge_flag='N';
	        if($(this).prop('checked')==true){
	            charge_flag='Y';
	        }
	        //注意 - call draw() 更新table.data()中的数据
	        cell.data(charge_flag).draw();
	        selectContr.calcTotal();

	        var selected_ids=[];
	        table.data().each(function(item, index) {
	            if(item.CHARGE_FLAG == 'N')
	                return;
	            selected_ids.push(item.ID);
	        });
	        $('#selected_ids').val(selected_ids);

	        
	        //获取对账单号
	        var chargeOrderNo = row.find('td:eq(1)').text();
	        console.log(chargeOrderNo);
	        //更新对应对账单的汇总金额
	        calcOrderTotal(chargeOrderNo);
	    });  
	  
	  
	  
	  var calcOrderTotal=function(chargeOrderNo) {
	        //根据对账单号获取 ’业务单据‘表格的行
	        var row_index = $("#eeda-table td:contains("+chargeOrderNo+")").parent().index();
	        var row = dataTable.row(row_index);
	        var row_data = row.data();
	        console.log(row_data);
	        row_data.APPLY_CHARGE_CNY=parseFloat($('#modal_cny').val());
	        row_data.APPLY_CHARGE_HKD=parseFloat($('#modal_hkd').val());
	        row_data.APPLY_CHARGE_JPY=parseFloat($('#modal_jpy').val());
	        row_data.APPLY_CHARGE_USD=parseFloat($('#modal_usd').val());
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
	
//	var ids = [];
//	var applied_arap_id = [];
//	var itemTable = eeda.dt({
//        id: 'charge-table',
//        columns:[
//	        {"data": "ID",
//	        	"render": function ( data, type, full, meta ) {
//	        		var str = '<input type="checkbox" style="width:30px">';
//	        		for(var i=0;i<ids.length;i++){
//	                    if(ids[i]==data){
//	                   	 str = '<input type="checkbox" style="width:30px" checked>';
//	                    }
//	                }
//	        		return str;
//			    }
//	        },
//	        { "data": "ORDER_NO"},
//	        { "data": "TYPE"},
//	        { "data": "CREATE_STAMP"},
//	        { "data": "SP_NAME"},
//	        { "data": "CURRENCY_NAME","class":"currency_name"},
//	        { "data": "TOTAL_AMOUNT","class":"total_amount",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "EXCHANGE_RATE"},
//	        { "data": "AFTER_TOTAL",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "NEW_RATE"},
//	        { "data": "AFTER_RATE_TOTAL",
//	        	"render": function ( data, type, full, meta ) {
//	        		if(full.ORDER_TYPE=='cost'){
//	            		return '<span style="color:red;">'+'-'+data+'</span>';
//	            	}
//	                return data;
//	              }
//	        },
//	        { "data": "EXCHANGE_CURRENCY_NAME","class":"EXCHANGE_CURRENCY_NAME"},
//	        { "data": "EXCHANGE_CURRENCY_RATE"},
//	        { "data": "EXCHANGE_TOTAL_AMOUNT","class":"EXCHANGE_TOTAL_AMOUNT"},
//	        { "data": "ORDER_TYPE", "visible": false,
//	            "render": function ( data, type, full, meta ) {
//	                if(!data)
//	                    data='';
//	                return data;
//	            }
//	        },
//	      ]
//	});
	
	
//	$('#eeda-table').on('click','td',function(){
//		
//		$('#chargeAlert').click();
//		var order_id = $(this).parent().attr('id');
//		$('#chargeAlert').attr('name',order_id);
//		var url = "/chargeCheckOrder/tableList?order_id="+order_id;
//    	itemTable.ajax.url(url).load();
//	})
	
	
	
	

});
});