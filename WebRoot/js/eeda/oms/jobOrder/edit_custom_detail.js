define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {

	$('#createCustomPlanOrderBtn').click(function(event) {
        var id = $('#order_id').val();
        var custom_broker = $('#custom_broker').val();
        
        if(custom_broker.trim()==''){
        	$.scojs_message('请先选中报关行', $.scojs_message.TYPE_ERROR);
        }else{
        	if(id!='' && custom_type.split(',')[0]=='china'){
            	window.open("/customPlanOrder/create?jobOrderId="+id+"&to_office_id="+custom_broker, '_blank');
            }else{
            	 $.scojs_message('请先保存单据', $.scojs_message.TYPE_ERROR);
            }
        }
    });

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
        case 'entrust_custom':
        	$('#entrust_tab').hide();
        	break;
        case 'self_custom':
        	$('#self_tab').hide();
        	break;
      }
    };

    var showCustomTab=function(service){
        if(service=='entrust_custom'){
            $('#entrust_tab').show();
            $('#entrust_tab').parent().addClass('active in');
            $('#entrustDetail').addClass('active in');
            
            $('#self_tab').parent().removeClass('active in');
            $('#selfDetail').removeClass('active in');
        }else if(service=='self_custom'){
        	$('#self_tab').show();
        	$('#self_tab').parent().addClass('active in');
            $('#selfDetail').addClass('active in');
            
            $('#entrust_tab').parent().removeClass('active in');
            $('#entrustDetail').removeClass('active in');
        }
    }
    
    $('#custom_type input[type="checkbox"]').click(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
    
    $('#entrust_or_self_custom input[type="checkbox"]').click(function(){
    	var checkValue=$(this).val();
    	if($(this).prop('checked')){
    		showCustomTab(checkValue);
    	}else{
    		hideServiceTab(checkValue);
    	}
    });
    
    //报关类型checkbox回显
    var checkArray = custom_type.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#custom_type input[type="checkbox"]').each(function(){
	    	var checkValue = this.value;
	        if(checkArray[i]==checkValue){
	        	this.checked = true;
                showServiceTab(checkValue);
	        }
	    })
    }
    
    console.log(entrust_or_self_custom_str)
    var entrust_or_self_custom_arr = entrust_or_self_custom_str.split(",");
    for(var i=0;i<entrust_or_self_custom_arr.length;i++){
    	$('#entrust_or_self_custom input[type="checkbox"]').each(function(){
    		var checkValue = this.value;
    		if(entrust_or_self_custom_arr[i]==checkValue){
    			this.checked = true;
    			showCustomTab(checkValue);
    		}
    	})
    }
	
    //报关类型,国内,自理报关
    itemOrder.buildCustomSelfDetail=function(){
		var arrays = [];
    	var item = {};
    	
    	//报关状态checkbox遍历取值
        var statusVal = [];
        $('#custom_china_self_form input[type="checkbox"]:checked').each(function(){
        	statusVal.push($(this).val()); 
        });
        item.status = statusVal.toString();
        
        item['id'] = $('#customSelf_id').val();
    	item['custom_type'] = "china_self";
    	
    	var customForm = $('#custom_china_self_form input,#custom_china_self_form select');
    	for(var i = 0; i < customForm.length; i++){
    		var name = customForm[i].id;
        	var value =customForm[i].value;
        	if(name){
        		if( name.indexOf('self_')>-1 ){
    				name = name.replace('self_','');
    			}
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
    	return arrays;
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
    var checkArray = custom_status_hidden.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#customForm input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);
	        }
	    })
    }
    
    //报关状态checkbox回显
    var checkArray = custom_self_status_hidden.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#custom_china_self_form input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);
	        }
	    })
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

	$('#customForm input[type="checkbox"]').click(function(){
    	if($(this).prop('checked')){
    		var updater = loginUserName;
    		var time = eeda.getDate();
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
    
    //国内 , 自理报关， 状态，更新人，更新时间处理
    var self_declareSuccess_time = $('#self_declareSuccess_time').val();
	var self_declareSuccess_updater = $('#self_declareSuccess_updater').val();
	var self_onDataBaseRoad_time = $('#self_onDataBaseRoad_time').val();
	var self_onDataBaseRoad_updater = $('#self_onDataBaseRoad_updater').val();
	var self_gateInSuccess_time = $('#self_gateInSuccess_time').val();
	var self_gateInSuccess_updater = $('#self_gateInSuccess_updater').val();
	var self_customOrderCheck_time = $('#self_customOrderCheck_time').val();
	var self_customOrderCheck_updater = $('#self_customOrderCheck_updater').val();
	var self_release_time = $('#self_release_time').val();
	var self_release_updater = $('#self_release_updater').val();
	var self_conclusion_time = $('#self_conclusion_time').val();
	var self_conclusion_updater = $('#self_conclusion_updater').val();
	
	$('#custom_china_self_form input[type="checkbox"]').click(function(){
		if($(this).prop('checked')){
			var updater = loginUserName;
			var time = eeda.getDate();
			if($(this).val()=='declareSuccess'){
				$('#self_declareSuccess_time_span').html(time);
				$('#self_declareSuccess_updater_span').html(updater);
				$('#self_declareSuccess_time').val(time);
				$('#self_declareSuccess_updater').val(updater);
			}
			if($(this).val()=='onDataBaseRoad'){
				$('#self_onDataBaseRoad_time_span').html(time);
				$('#self_onDataBaseRoad_updater_span').html(updater);
				$('#self_onDataBaseRoad_time').val(time);
				$('#self_onDataBaseRoad_updater').val(updater);
			}
			if($(this).val()=='gateInSuccess'){
				$('#self_gateInSuccess_time_span').html(time);
				$('#self_gateInSuccess_updater_span').html(updater);
				$('#self_gateInSuccess_time').val(time);
				$('#self_gateInSuccess_updater').val(updater);
			}
			if($(this).val()=='customOrderCheck'){
				$('#self_customOrderCheck_time_span').html(time);
				$('#self_customOrderCheck_updater_span').html(updater);
				$('#self_customOrderCheck_time').val(time);
				$('#self_customOrderCheck_updater').val(updater);
			}
			if($(this).val()=='release'){
				$('#self_release_time_span').html(time);
				$('#self_release_updater_span').html(updater);
				$('#self_release_time').val(time);
				$('#self_release_updater').val(updater);
			}
			if($(this).val()=='conclusion'){
				$('#self_conclusion_time_span').html(time);
				$('#self_conclusion_updater_span').html(updater);
				$('#self_conclusion_time').val(time);
				$('#self_conclusion_updater').val(updater);
			}
		}else{
			if($(this).val()=='declareSuccess'){
				$('#self_declareSuccess_time_span').html(self_declareSuccess_time);
				$('#self_declareSuccess_updater_span').html(self_declareSuccess_updater);
				$('#self_declareSuccess_time').val(self_declareSuccess_time);
				$('#self_declareSuccess_updater').val(self_declareSuccess_updater);
			}
			if($(this).val()=='onDataBaseRoad'){
				$('#self_onDataBaseRoad_time_span').html(self_onDataBaseRoad_time);
				$('#self_onDataBaseRoad_updater_span').html(self_onDataBaseRoad_updater);
				$('#self_onDataBaseRoad_time').val(self_onDataBaseRoad_time);
				$('#self_onDataBaseRoad_updater').val(self_onDataBaseRoad_updater);
			}
			if($(this).val()=='gateInSuccess'){
				$('#self_gateInSuccess_time_span').html(self_gateInSuccess_time);
				$('#self_gateInSuccess_updater_span').html(self_gateInSuccess_updater);
				$('#self_gateInSuccess_time').val(self_gateInSuccess_time);
				$('#self_gateInSuccess_updater').val(self_gateInSuccess_updater);
			}
			if($(this).val()=='customOrderCheck'){
				$('#self_customOrderCheck_time_span').html(self_customOrderCheck_time);
				$('#self_customOrderCheck_updater_span').html(self_customOrderCheck_updater);
				$('#self_customOrderCheck_time').val(self_customOrderCheck_time);
				$('#self_customOrderCheck_updater').val(self_customOrderCheck_updater);
			}
			if($(this).val()=='release'){
				$('#self_release_time_span').html(self_release_time);
				$('#self_release_updater_span').html(self_release_updater);
				$('#self_release_time').val(self_release_time);
				$('#self_release_updater').val(self_release_updater);
			}
			if($(this).val()=='conclusion'){
				$('#self_conclusion_time_span').html(self_conclusion_time);
				$('#self_conclusion_updater_span').html(self_conclusion_updater);
				$('#self_conclusion_time').val(self_conclusion_time);
				$('#self_conclusion_updater').val(self_conclusion_updater);
			}
		}
	})
    
    
    var customTable = eeda.dt({
        id: 'custom_item_table',
        autoWidth: false,
        columns:[
                 { "data": "ID",'visible':false},
                 { "data": "CUSTOM_PLAN_NO",
                	"render": function ( data, type, full, meta ) {
                		return "<a href='/customPlanOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
               	  	}
                 },
                 { "data": "CUSTOM_BANK"},
                 { "data": "APPLYBILL_STATUS"},
                 { "data": "CUSTOM_ORDER_NO"},
                 { "data": "STATUS"},
                 { "data": "CREATOR"},
                 { "data": "CREATE_STAMP"},
                 { "data": "FILL_NAME"},
                 { "data": "FILL_STAMP"}
                 ]
        })
        
     itemOrder.refleshCustomAPPTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=custom_app";
    	customTable.ajax.url(url).load();
    };
        
    $('#reflesh').on('click',function(){
    	itemOrder.refleshCustomAPPTable($('#order_id').val());
    });
        
    
});
});