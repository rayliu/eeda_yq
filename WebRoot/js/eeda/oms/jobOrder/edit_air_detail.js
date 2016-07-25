define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildAirDetail=function(){
		var arrays = [];
    	var item = {};
    	var airForm = $('#airForm input,select');
    	for(var i = 0; i < airForm.length; i++){
    		var name = airForm[i].id;
        	var value =airForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
} );
});