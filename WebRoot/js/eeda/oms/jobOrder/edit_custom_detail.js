define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildCustomDetail=function(){
		var arrays = [];
    	var item = {};
    	
    	//报关状态checkbox遍历取值
        var statusVal = [];
        $('#customForm input[type="checkbox"]:checked').each(function(){
        	statusVal.push($(this).val()); 
        });
        item.status = statusVal.toString();
        
    	item['id'] = $('#custom_id').val();
    	
    	var shipmentForm = $('#customForm input[type!="hidden"],#customForm select');
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
    
    //报关状态checkbox回显
    var checkArray = $('#hidden_status').val().split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#customForm input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);
	        }
	    })
    }
    
});
});