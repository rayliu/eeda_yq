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
        		if(name.indexOf("air_")==0){
        			var rName = name.replace("air_","");
        			item[rName] = value;
        		}else{
        			item[name] = value;
        		}
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    
    //常用海运信息模版
    $('#usedAirInfo').on('click', 'li', function(){
        var li = $(this);
        $('#shipper_input').val(li.attr('shipper_abbr'));
        $('#shipper_info').val(li.attr('shipper_info'));
        $('#shipper').val(li.attr('shipper_id'));
        $('#consignee_input').val(li.attr('consignee_abbr'));
        $('#consignee_info').val(li.attr('consignee_info'));
        $('#consignee').val(li.attr('consignee_id'));
        $('#notify_party_input').val(li.attr('notify_abbr'));
        $('#notify_party_info').val(li.attr('notify_info'));
        $('#notify_party').val(li.attr('notify_id'));
        $('#booking_agent_input').val(li.attr('booking_agent_name'));
        $('#booking_agent').val(li.attr('booking_agent'));
    });
    $('#collapseAirInfo').on('show.bs.collapse', function () {
      $('#collapseAirIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
    });
    $('#collapseAirInfo').on('hide.bs.collapse', function () {
      $('#collapseAirIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
    
} );
});