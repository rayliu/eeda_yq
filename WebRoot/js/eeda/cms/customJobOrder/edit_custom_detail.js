define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	itemOrder.buildCustomDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#custom_id').val();
    	item['type'] = $('#customDetailExportForm input[name="type"]:checked').val();
    	var form = $('#customDetailExportForm input,#customDetailExportForm select,#customDetailExportForm textarea');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	if(name){
        		if(name.indexOf("custom_")==0){
        			var rName = name.replace("custom_","");
        			item[rName] = value;
        		}else{
        			item[name] = value;
        		}
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
        
});
});