define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildInsuranceDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#insurance_id').val();
    	var shipmentForm = $('#insuranceForm input,#insuranceForm select');
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
} );
});