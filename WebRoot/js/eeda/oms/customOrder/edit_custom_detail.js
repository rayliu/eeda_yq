define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
    
    //报关类型,国内
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
    	item['custom_type'] = "china";
    	
    	var customForm = $('#customForm input[type="hidden"]');
    	for(var i = 0; i < customForm.length; i++){
    		var name = customForm[i].id;
        	var value =customForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	
	    	arrays.push(item);
	    	return arrays;
	}
    
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
    
    //取得当前时间
    var getTime=function(){
	    var d = new Date();
	    var year = d.getFullYear();
	    var mon = d.getMonth() + 1;
	    var day = d.getDate();
	    var h = d.getHours(); 
	    var m = d.getMinutes(); 
	    var se = d.getSeconds(); 
	    timeStr=year+"-"+(mon<10 ? "0" + mon : mon)+"-"+(day<10 ? "0"+ day : day)+" "+(h<10 ? "0"+ h : h)+":"+(m<10 ? "0" + m : m)+":"+(se<10 ? "0" +se : se);
	    return timeStr;
    }
    
    //国内报关状态更新人更新时间处理
    var declareSuccess_time = $('#declareSuccess_time').val();
	var declareSuccess_updater = $('#declareSuccess_updater').val();
	var onDataBaseRoad_time = $('#onDataBaseRoad_time').val();
	var onDataBaseRoad_updater = $('#onDataBaseRoad_updater').val();
	var gateInSuccess_time = $('#gateInSuccess_time').val();
	var gateInSuccess_updater = $('#gateInSuccess_updater').val();
	var customOrderCheck_time = $('#customOrderCheck_time').val();
	var customOrderCheck_updater = $('#customOrderCheck_updater').val();
	var release_time = $('#release_time').val();
	var release_updater = $('#release_updater').val();
	var conclusion_time = $('#conclusion_time').val();
	var conclusion_updater = $('#conclusion_updater').val();

	$('.checkbox input[type="checkbox"]').click(function(){
    	if($(this).prop('checked')){
    		var updater = $('#updater').val();
    		var time = getTime();
    		if($(this).val()=='declareSuccess'){
    			$('#declareSuccess_time_span').html(time);
    			$('#declareSuccess_updater_span').html(updater);
	    		$('#declareSuccess_time').val(time);
	    		$('#declareSuccess_updater').val(updater);
    		}
    		if($(this).val()=='onDataBaseRoad'){
    			$('#onDataBaseRoad_time_span').html(time);
    			$('#onDataBaseRoad_updater_span').html(updater);
    			$('#onDataBaseRoad_time').val(time);
    			$('#onDataBaseRoad_updater').val(updater);
    		}
    		if($(this).val()=='gateInSuccess'){
    			$('#gateInSuccess_time_span').html(time);
    			$('#gateInSuccess_updater_span').html(updater);
    			$('#gateInSuccess_time').val(time);
    			$('#gateInSuccess_updater').val(updater);
    		}
    		if($(this).val()=='customOrderCheck'){
    			$('#customOrderCheck_time_span').html(time);
    			$('#customOrderCheck_updater_span').html(updater);
    			$('#customOrderCheck_time').val(time);
    			$('#customOrderCheck_updater').val(updater);
    		}
    		if($(this).val()=='release'){
    			$('#release_time_span').html(time);
    			$('#release_updater_span').html(updater);
    			$('#release_time').val(time);
    			$('#release_updater').val(updater);
    		}
    		if($(this).val()=='conclusion'){
    			$('#conclusion_time_span').html(time);
    			$('#conclusion_updater_span').html(updater);
    			$('#conclusion_time').val(time);
    			$('#conclusion_updater').val(updater);
    		}
    	}else{
    		if($(this).val()=='declareSuccess'){
    			$('#declareSuccess_time_span').html(declareSuccess_time);
    			$('#declareSuccess_updater_span').html(declareSuccess_updater);
        		$('#declareSuccess_time').val(declareSuccess_time);
        		$('#declareSuccess_updater').val(declareSuccess_updater);
    		}
    		if($(this).val()=='onDataBaseRoad'){
    			$('#onDataBaseRoad_time_span').html(onDataBaseRoad_time);
    			$('#onDataBaseRoad_updater_span').html(onDataBaseRoad_updater);
    			$('#onDataBaseRoad_time').val(onDataBaseRoad_time);
    			$('#onDataBaseRoad_updater').val(onDataBaseRoad_updater);
    		}
    		if($(this).val()=='gateInSuccess'){
    			$('#gateInSuccess_time_span').html(gateInSuccess_time);
    			$('#gateInSuccess_updater_span').html(gateInSuccess_updater);
    			$('#gateInSuccess_time').val(gateInSuccess_time);
    			$('#gateInSuccess_updater').val(gateInSuccess_updater);
    		}
    		if($(this).val()=='customOrderCheck'){
    			$('#customOrderCheck_time_span').html(customOrderCheck_time);
    			$('#customOrderCheck_updater_span').html(customOrderCheck_updater);
    			$('#customOrderCheck_time').val(customOrderCheck_time);
    			$('#customOrderCheck_updater').val(customOrderCheck_updater);
    		}
    		if($(this).val()=='release'){
    			$('#release_time_span').html(release_time);
    			$('#release_updater_span').html(release_updater);
    			$('#release_time').val(release_time);
    			$('#release_updater').val(release_updater);
    		}
    		if($(this).val()=='conclusion'){
    			$('#conclusion_time_span').html(conclusion_time);
    			$('#conclusion_updater_span').html(conclusion_updater);
    			$('#conclusion_time').val(conclusion_time);
    			$('#conclusion_updater').val(conclusion_updater);
    		}
    	}
    })
    
});
});