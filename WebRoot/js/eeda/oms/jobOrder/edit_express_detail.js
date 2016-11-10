define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildExpressDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#express_id').val();
    	var expressForm = $('#expressForm input,#expressForm select');
    	for(var i = 0; i < expressForm.length; i++){
    		var name = expressForm[i].id;
        	var value =expressForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
} );
});