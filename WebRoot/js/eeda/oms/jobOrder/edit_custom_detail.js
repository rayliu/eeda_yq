define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	var showServiceTab=function(service){
        if(service=='china'){
            $('#chinaTab').show();
            $('#chinaTab').parent().addClass('active');
            $('#chinaDetail').addClass('active in');
            
            $('#hkTab').parent().removeClass('active in');
            $('#hkTab').parent().removeClass('active in');
            $('#hkDetail').removeClass('active in');
            $('#abroadDetail').removeClass('active in');
        }else if(service=='HK/MAC'){
        	$('#hkTab').show();
        	$('#hkTab').parent().addClass('active');
            $('#hkDetail').addClass('active in');
            
            $('#chinaTab').parent().removeClass('active in');
            $('#abroadTab').parent().removeClass('active in');
            $('#chinaDetail').removeClass('active in');
            $('#abroadDetail').removeClass('active in');
        }else if(service=='abroad'){
        	$('#abroadTab').show();
        	$('#abroadTab').parent().addClass('active');
            $('#abroadDetail').addClass('active in');
            
            $('#chinaTab').parent().removeClass('active in');
            $('#hkTab').parent().removeClass('active in');
            $('#chinaDetail').removeClass('active in');
            $('#hkDetail').removeClass('active in');
        }
    };

    var hideServiceTab=function(service){
    	switch (service){
        case 'china':
          $('#chinaTab').hide();
          break;
        case 'HK/MAC':
          $('#hkTab').hide();
          break;
        case 'abroad':
          $('#abroadTab').hide();
          break;
      }
    };

    $('#custom_type input[type="checkbox"]').click(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
    
    //报关类型checkbox回显
    var checkArray =$('#hidden_custom_type').val().split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#custom_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);

                showServiceTab(checkValue);
	        }
	    })
    }
	
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
    	
    	var customForm = $('#customForm input,#customForm select');
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
    
    //报关类型,国外
    itemOrder.buildAbroadCustomDetail=function(){
    	var arrays = [];
    	var item = {};
    	
    	item['id'] = $('#abroad_custom_id').val();
    	item['custom_type'] = "abroad";
    	
    	var customForm = $('#abroadForm input,#abroadForm select');
    	for(var i = 0; i < customForm.length; i++){
    		var name = customForm[i].id;
    		var value =customForm[i].value;
    		if(name){
    			if( name.indexOf('abroad_')>-1 ){
    				name = name.replace('abroad_','');
    			}
    			item[name] = value;
    		}
    	}
    	
    	arrays.push(item);
    	return arrays;
    }
    itemOrder.buildHkCustomDetail=function(){
    	var arrays = [];
    	var item = {};
    	item['id'] = $('#hk_custom_id').val();
    	item['custom_type'] = "HK/MAC";
    	
    	var customForm = $('#hkForm input,#hkForm select');
    	for(var i = 0; i < customForm.length; i++){
    		var name = customForm[i].id;
    		var value =customForm[i].value;
    		if(name){
    			if( name.indexOf('hk_')>-1 ){
    				name = name.replace('hk_','');
    			}
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
    		
    	}
    })
    
});
});