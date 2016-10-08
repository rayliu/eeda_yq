define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
  //构造主表json
	var getSpType = function(){
		var typeArray = [];
		$('#partyForm [type="checkbox"]:checked').each(function(){
			var value = $(this).val();
			typeArray.push(value);
		})
		return typeArray;
	}
	
    var buildOrder = function(){
    	var item = {};
    	var comType = $('#partyForm input[type="radio"]:checked').val();
    	if(comType == 'SP'){
    		item.sp_type = getSpType().join(";");
    	}
    	item.type=comType
    	var orderForm = $('#partyForm input');
    	for(var i = 0; i < orderForm.length; i++){
    		var name = orderForm[i].id;
        	var value =orderForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
        return item;
    }
    
    $('#savePartyBtn').on('click',function(e){
    	var self = this;
    	var comType = $('#partyForm input[type="radio"]:checked').val();
    	if(!comType){
    		$.scojs_message('请选择公司类型', $.scojs_message.TYPE_ERROR);
    		return false;
    	}
    	
    	if(!$('#partyForm').valid())
    		return false;
    	
    	$(self).attr('disabled', true);
    	
    	var order = buildOrder();
    	
    	 //异步向后台提交数据
        $.post('/jobOrder/saveParty', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID){
            	$('#partyForm')[0].reset();
            	$(self).attr('disabled', false);
            	$('#returnOrderBtn').click();
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $(self).attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
            $(self).attr('disabled', false);
          });
    })
    
    
     $('#partyForm input[type="radio"]').change(function(){
     	var type = $(this).val();
     	if(type == 'SP'){
     		$('#show_type').show()
     	}else{
     		$('#show_type').hide()
     	}
     })
    
} );
});