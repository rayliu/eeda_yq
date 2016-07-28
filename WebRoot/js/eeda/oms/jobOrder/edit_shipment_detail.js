define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	itemOrder.buildShipmentDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#shipment_id').val();
    	item['release_type'] = $('#shipmentForm input[type="radio"]:checked').val();
    	var shipmentForm = $('#shipmentForm input,#shipmentForm select,#shipmentForm textarea');
    	for(var i = 0; i < shipmentForm.length; i++){
    		var name = shipmentForm[i].id;
        	var value =shipmentForm[i].value;
        	if(name){
        		if(name=='ocean_trans_clause')
        			name = 'trans_clause';
        		if(name=='ocean_shipper')
        			name = 'shipper';
        		if(name=='ocean_consignee')
        			name = 'consignee';
        		if(name=='ocean_notify_party')
        			name = 'notify_party';
        		if(name=='ocean_shipper_info')
        			name = 'shipper_info';
        		if(name=='ocean_consignee_info')
        			name = 'consignee_info';
        		if(name=='ocean_notify_party_info')
        			name = 'notify_party_info';
        		item[name] = value;
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
    
});
});