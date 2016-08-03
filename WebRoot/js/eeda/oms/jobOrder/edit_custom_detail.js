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
	
//	itemOrder.buildCustomDetail=function(){
//			var arrays = [];
//	    	var item = {};//报关类型,国外
//	    	var item1 = {};//报关类型，国内
//	    	var item2 = {};//hk/mac
//	    	
//	    	//报关状态checkbox遍历取值
//	        var statusVal = [];
//	        $('#customForm input[type="checkbox"]:checked').each(function(){
//	        	statusVal.push($(this).val()); 
//	        });
//	        item.status = statusVal.toString();
//	        
//	        item['id'] = $('#custom_id').val();
//	        item1['id'] = $('#china_custom_id').val();
//	        item2['id'] = $('#hk_custom_id').val();
//	    	item['custom_type'] = "abroad";
//	    	item1['custom_type'] = "china";
//	    	item2['custom_type'] = "HK/MAC";
//	    	
//	    	var customForm = $('#customForm input,#customForm select');
//	    	for(var i = 0; i < customForm.length; i++){
//	    		var name = customForm[i].id;
//	        	var value =customForm[i].value;
//	        	if(name){
//	        		item[name] = value;
//	        	}
//	    	}
//	    	var chinaForm = $('#chinaForm input');
//	    	for(var i = 0; i < chinaForm.length; i++){
//	    		var name = chinaForm[i].id;
//	    		var value =chinaForm[i].value;
//	    		if(name){
//	    			if(name.indexOf('china_') > -1 ){
//	    				name = name.replace('china_','');
//	    			}
//	    			item1[name] = value;
//	    		}
//	    	}
//	    	var hkForm = $('#hkForm input');
//	    	for(var i = 0; i < hkForm.length; i++){
//	    		var name = hkForm[i].id;
//	    		var value =hkForm[i].value;
//	    		if(name){
//	    			if( name.indexOf('hk_')>-1 ){
//	    				name = name.replace('hk_','');
//	    			}
//			    	item2[name] = value;
//	    		}
//	    	}
//		    	arrays.push(item);
//		    	arrays.push(item1);
//		    	arrays.push(item2);
//		    	return arrays;
//    	}
    itemOrder.buildCustomDetail=function(){
		var arrays = [];
    	var item = {};//报关类型,国外
    	
    	//报关状态checkbox遍历取值
        var statusVal = [];
        $('#customForm input[type="checkbox"]:checked').each(function(){
        	statusVal.push($(this).val()); 
        });
        item.status = statusVal.toString();
        
        item['id'] = $('#custom_id').val();
    	item['custom_type'] = "abroad";
    	
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
    itemOrder.buildChinaCustomDetail=function(){
    	var arrays = [];
    	var item = {};//报关类型,国外
    	
    	item['id'] = $('#china_custom_id').val();
    	item['custom_type'] = "china";
    	
    	var customForm = $('#chinaForm input,#chinaForm select');
    	for(var i = 0; i < customForm.length; i++){
    		var name = customForm[i].id;
    		var value =customForm[i].value;
    		if(name){
    			if( name.indexOf('china_')>-1 ){
    				name = name.replace('china_','');
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
    
});
});