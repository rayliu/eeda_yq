define(['jquery', 'metisMenu', 'sb_admin','dataTablesBootstrap','validate_cn', 'sco', 'pageguide','./selectSupplier','./quotationItem'], function ($, metisMenu, sb, createStep1Contr, selectContr) {
$(document).ready(function() {

	tl.pg.init({
        pg_caption: '本页教程'
    });
    
    $('#receive_time').val(eeda.getDate());
    $('.hide_add_charge').hide();

    //构造主表json
    var buildOrder = function(){
    	var item = {};
    	item.id = $('#order_id').val();
    	item.cny_rate = $('#cny_rate').val();
    	item.usd_rate = $('#usd_rate').val();
    	item.jpy_rate = $('#jpy_rate').val();
    	item.hkd_rate = $('#hkd_rate').val();
    	item.jpy_rate = $('#jpy_rate').val();
    	item.target_currency = $('#target_currency').val();
    	item.service_type = $('#billing_method input[type="radio"]:checked').val();
    	item.total = $('#total').val();
    	item.status='新建';
    	var orderForm = $('#orderForm input,#orderForm select,#orderForm textarea');
    	for(var i = 0; i < orderForm.length; i++){
    		var name = orderForm[i].id;
        	var value =orderForm[i].value;
        	if(name){
        		if(name.indexOf("check_time_begin") != -1){
        			name = "begin_time";
        		}else if(name.indexOf("check_time_end") != -1){
        			name = "end_time"
        		}
        		if(name.indexOf("modal_") != -1){
            	  	value=value.replace(/,/g,'');
            	}
        		item[name] = value;
        	}
    	}
        return item;
    }

    
	//datatable, 动态处理
    var ids = $("#ids").val();
    var selected_item_ids = $("#selected_ids").val();

    
    
    //成本对比保存
	$("#saveBtn").on('click',function(){	
		$("#saveBtn").attr("disabled", true);		
		var order = buildOrder();
		//供应商明细
		order.SupplierItem_list = itemOrder.buildSelectSupplierItem();
		order.quotationItem_list = itemOrder.buildquotationItem();
		$.post('/costComparison/save',{params:JSON.stringify(order)}, function(data){
			$("#saveBtn").attr("disabled", false);
			if(data.ID>0){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#order_no").val(data.ORDER_NO);
				$("#order_id").val(data.ID);
				itemOrder.refleshSupplierTable(data.ID);
//				itemOrder.refleshQuotationTable(data.ID);
				eeda.contactUrl("edit?id",data.ID);				
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_FALSE);
			}
		 },'json').fail(function() {
	            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	        });
	});
	
	$('#target_currency_list').on('mousedown','.fromLocationItem',function(){
		var to_currency_name=$(this).text();
		$.post('/currencyRate/gainRateList',{params:to_currency_name},function(data){
			if(data){
				$('#cny_rate').val('');
				$('#usd_rate').val('');
				$('#jpy_rate').val('');
				$('#hkd_rate').val('');
				for(var i=0;i<data.length;i++){
					var currency_code = data[i].CURRENCY_CODE;
					if(currency_code=='CNY'){
						$('#cny_rate').val(data[i].RATE);					
					}else if(currency_code=='USD'){
						$('#usd_rate').val(data[i].RATE);
					}else if(currency_code=='JPY'){
						$('#jpy_rate').val(data[i].RATE);
					}else if(currency_code=='HKD'){
						$('#hkd_rate').val(data[i].RATE);
					}
				}
				sum_total();
			}
		},'json');
	});
	
	$("#cny_rate,#usd_rate,#jpy_rate,#hkd_rate").on('keyup',function(){
		sum_total();
	});
	
	var sum_total=function(){
		var cny_total = parseFloat($('#cny').val()*$('#cny_rate').val());
		var usd_total = parseFloat($('#usd').val()*$('#usd_rate').val());
		var jpy_total = parseFloat($('#jpy').val()*$('#jpy_rate').val());
		var hkd_total = parseFloat($('#hkd').val()*$('#hkd_rate').val());
		var total =parseFloat(cny_total+usd_total+jpy_total+hkd_total).toFixed(2);
		$('#total').val(total);
	}
	
	
	
	
	if($('#order_id').val()){
		setTimeout(function(){
			itemOrder.searchShowItem();
		},100)
	}	
});
});