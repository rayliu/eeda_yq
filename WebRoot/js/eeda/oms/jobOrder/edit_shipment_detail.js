define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	itemOrder.buildShipmentDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#shipment_id').val();
    	item['release_type'] = $('#shipmentForm input[type="radio"]:checked').val();
    	item['prepaid'] = $('#prepaid').val($('#prepaid').prop('checked')==true?'Y':'N');
    	item['wait_overseaCustom'] = $('#wait_overseaCustom').val($('#wait_overseaCustom').prop('checked')==true?'Y':'N');
    	var shipmentForm = $('#shipmentForm input,#shipmentForm select,#shipmentForm textarea');
    	for(var i = 0; i < shipmentForm.length; i++){
    		var name = shipmentForm[i].id;
        	var value =shipmentForm[i].value;
        	if(name){
        		
        		if(name.indexOf("ocean_")==0){
        			var rName = name.replace("ocean_","");
        			item[rName] = value;
        		}else{
        			item[name] = value;
        		}
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    //放货方式radio回显
    var radioVal = $('#hidden_release_type').val();
    $('#shipmentForm input[type="radio"]').each(function(){
    	var checkValue = $(this).val();
    	if(radioVal==checkValue){
    		$(this).attr("checked",true);
    	}
    });
    //预付回显
    var checkBoxVal = $('#hidden_prepaid').val();
    if(checkBoxVal=='Y'){
        $('#prepaid').attr("checked",true);    	
    }
    else{
    	$('#prepaid').attr("checked",false);
    }
    //待海外报关回显
    var checkBoxVal = $('#hidden_wait_overseaCustom').val();
    if(checkBoxVal=='Y'){
        $('#wait_overseaCustom').attr("checked",true);    	
    }
    else{
    	$('#wait_overseaCustom').attr("checked",false);
    }
    
});
});