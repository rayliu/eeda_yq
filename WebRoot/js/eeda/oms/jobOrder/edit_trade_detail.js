define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildTradeDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#trade_id').val();
        	var shipmentForm = $('#tradeForm input,#tradeForm select,#tradeForm textarea');
        	for(var i = 0; i < shipmentForm.length; i++){
        		var name = shipmentForm[i].id;
            	var value =shipmentForm[i].value;
            	if(name){
            			item[name] = value;
            	}
        	}
        	arrays.push(item);
            return arrays;
        };
        
       $('#cost_currency_list').on('mousedown','a',function(){
    	   $('#cost_currency_rate').val( $(this).attr('rate'));
       })
        
    });
});