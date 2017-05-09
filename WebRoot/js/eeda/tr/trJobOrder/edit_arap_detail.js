define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
    //------------------费用明细
    $('#collapseChargeInfo,#collapseCostInfo').on('show.bs.collapse', function () {
    	var thisType = $(this).attr('id');
    	var type = 'Charge';
    	if('collapseChargeInfo'!=thisType){
    		type='Cost';
    	}
		var	div = $('#'+type+'Div').empty();
        $('#collapse'+type+'Icon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#customer_id').val();
        if(order_type.trim() == '' || customer_id == ''){
        	$.scojs_message('请先选择类型和客户', $.scojs_message.TYPE_ERROR);
        	return
        }else{
        	$.post('/trJobOrder/getArapTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
        		if(data){
        			for(var i = 0;i<data.length;i++){
        				var json_obj = JSON.parse(data[i].JSON_VALUE);
        				var li = '';
        				var li_val = '';
        				for(var j = 0;j<json_obj.length;j++){
        					li +='<li '
        						+' sp_name="'+json_obj[j].sp_name+'" '
        						+'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
        						+'charge_id="'+json_obj[j].CHARGE_ID+'" '
        						+'currency_id="'+json_obj[j].CURRENCY_ID+'" '
        						+'sp_id="'+json_obj[j].SP_ID+'" '
        						+'unit_id="'+json_obj[j].UNIT_ID+'" '
        						+'amount="'+json_obj[j].amount+'" '
        						+'charge_name="'+json_obj[j].charge_name+'" '
        						+'charge_name_eng="'+json_obj[j].charge_eng_name+'" '
        						+'currency_name="'+json_obj[j].currency_name+'" '
        						+'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
        						+'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
        						+'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
        						+'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
        						+'exchange_rate="'+json_obj[j].exchange_rate+'" '
        						+'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                                +'exchange_currency_rate_rmb="'+json_obj[j].exchange_currency_rate_rmb+'" '
                                +'exchange_total_amount_rmb="'+json_obj[j].exchange_total_amount_rmb+'" '
                                +'rmb_difference="'+json_obj[j].rmb_difference+'" '
        						+'order_type="'+json_obj[j].order_type+'" '
        						+'price="'+json_obj[j].price+'" '
        						+'remark="'+json_obj[j].remark+'" '
        						+'total_amount="'+json_obj[j].total_amount+'" '
        						+'type="'+json_obj[j].type+'" '
        						+'unit_name="'+json_obj[j].unit_name+'" '
        						+'></li>';
        					li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].charge_eng_name+'<br/>';
        				}
        				
        				div.append('<ul class="used'+type+'Info" id="'+data[i].ID+'">'
        						+li
        						+'<div class="radio">'
        						+'	<a class="delete'+type+'Template" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
        						+'	<div class="select'+type+'Template" style="margin-left: 60px;padding-top: 0px;">'
        						+'      <input type="radio" value="1" name="used'+type+'Info">'
        						+		li_val
        						+'	</div>'
        						+'</div><hr/>'
        						+'</ul>');
        				
        			}
        		}
        	});
        }
    });
 
    $('#collapseChargeInfo,#collapseCostInfo').on('hide.bs.collapse', function () {
    	var thisType = $(this).attr('id');
    	var type = 'Charge';
    	if('collapseChargeInfo'!=thisType){
    		type='Cost';
    	}
    	$('#collapse'+type+'Icon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
  
    $('#ChargeDiv,#CostDiv').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
	  	$(this).attr('disabled', true);
	  	var ul = $(this).parent().parent();
	  	var id = ul.attr('id');
	  	$.post('/trJobOrder/deleteArapTemplate', {id:id}, function(data){
	  		if(data){
	  			$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
		  		$(this).attr('disabled', false);
		  		ul.css("display","none");
	  		}
	  	},'json').fail(function() {
	  		$(this).attr('disabled', false);
	          $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
	    });
    })
    
    
    //选中回显
    $('#ChargeDiv,#CostDiv').on('click', '.selectChargeTemplate,.selectCostTemplate', function(){
    	$(this).parent().find('[type=radio]').prop('checked',true)
    	
    	var thisType = $(this).attr('class');
    	var type = 'Charge';
    	var table = 'charge_table';
    	if('selectChargeTemplate'!=thisType){
    		type='Cost';
    		table='cost_table';
    	}
    	
        var li = $(this).parent().parent().find('li');
        var dataTable = $('#'+table).DataTable();
        
        for(var i=0; i<li.length; i++){
        	var row = $(li[i]);
        	var item={};
        	item.ID='';
        	item.TYPE=row.attr('type');
        	item.SP_ID=row.attr('sp_id');
        	item.CHARGE_ID= row.attr('charge_id');
        	item.CHARGE_ENG_ID= row.attr('charge_eng_id');
        	item.PRICE= row.attr('PRICE');
        	item.AMOUNT= row.attr('amount');
        	item.UNIT_ID= row.attr('unit_id');
        	item.TOTAL_AMOUNT= row.attr('total_amount');
        	item.CURRENCY_ID= row.attr('currency_id');
        	item.EXCHANGE_RATE= row.attr('exchange_rate');
        	item.CURRENCY_TOTAL_AMOUNT= row.attr('currency_total_amount');
        	item.EXCHANGE_CURRENCY_ID= row.attr('exchange_currency_id');
        	item.EXCHANGE_CURRENCY_RATE= row.attr('exchange_currency_rate');
        	item.EXCHANGE_TOTAL_AMOUNT= row.attr('exchange_total_amount');
            item.EXCHANGE_CURRENCY_RATE_RMB= row.attr('exchange_currency_rate_rmb');
            item.EXCHANGE_TOTAL_AMOUNT_RMB= row.attr('exchange_total_amount_rmb');
            item.RMB_DIFFERENCE= row.attr('rmb_difference');
        	item.REMARK= row.attr('remark');
        	item.SP_NAME=row.attr('sp_name');
        	item.CHARGE_NAME=row.attr('charge_name');
        	item.CHARGE_NAME_ENG=row.attr('charge_name_eng');
        	item.UNIT_NAME=row.attr('unit_name');
        	item.CURRENCY_NAME=row.attr('currency_name');
        	item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
        	item.AUDIT_FLAG='';
        	dataTable.row.add(item).draw();
        }
    });
    
});
});