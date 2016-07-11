define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildShipmentDetail=function(){
		var arrays = [];
    	var item = {};
    	var shipmentForm = $('#shipmentForm input');
    	for(var i = 0; i < shipmentForm.length; i++){
    		var name = shipmentForm[i].id;
        	var value =shipmentForm[i].value;
        	if(name){
        		if(name.indexOf('begin_time')>=0){
        			name = 'schedule_from';
        		}else if(name.indexOf('end_time')>=0){
        			name = 'schedule_to';
        		}else if(name == 'item_id'){
        			name = 'id';
        		}
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
} );
});