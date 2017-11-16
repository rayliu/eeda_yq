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
	
	setTimeout(function(){
		itemOrder.searchShowItem();
	},100)
	
});
});