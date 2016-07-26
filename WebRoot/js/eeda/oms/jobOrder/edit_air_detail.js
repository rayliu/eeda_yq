define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildAirDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#air_id').val();
    	var airForm = $('#airForm input,#airForm select,#airForm textarea');
    	for(var i = 0; i < airForm.length; i++){
    		var name = airForm[i].id;
        	var value =airForm[i].value;
        	if(name){
//        		if(name.indexOf("_input")){
//        			name.replace("_input","");
//        		}
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
} );
});