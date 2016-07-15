define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	
    	document.title = order_no + ' | ' + document.title;

    	//------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $(this).attr('disabled', true);
            
          //取上面的form的所有input的值
        	var buidOrder = function(){
    	    	var order = {};
    	    	var orderForm = $('#orderForm input');
    	    	for(var i = 0; i < orderForm.length; i++){
    	    		var name = orderForm[i].id;
    	        	var value =orderForm[i].value;
    	        	if(name){
    	        		if(name == 'order_id'){
    	        			name = 'id';
    	        		}
    	        		
    	        		if(name!='creator'){
    	        			order[name] = value;
    	        		}
    	        	}
    	    	}
    	    	
    	        return order;
        	}
            
            var charge_array = itemOrder.buildChargeDetail();
            var cargo_array = itemOrder.buildCargoDetail();
            
            var order  = buidOrder();
            order.remark = $('#note').val();
            order.charge_list = charge_array;
            order.cargo_list = cargo_array;

            //异步向后台提交数据
            $.post('/truckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	
                    eeda.contactUrl("edit?id",order.ID);
                    $('#creator').val(order.CREATOR_NAME);
                    $('#order_no').val(order.ORDER_NO);
                    $('#customer_id').val(order.CUSTOMER_ID);
                    $('#sp_id').val(order.SP_ID);
                    $('#load_sp_id').val(order.LOAD_SP_ID);
                    $('#unload_sp_id').val(order.UNLOAD_SP_ID);
                    $('#create_stamp').val(order.CREATE_STAMP);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    
                    //异步刷新明细表
                    itemOrder.refleshCargoTable(order.ID);
                    itemOrder.refleshChargeTable(order.ID);
                    
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  

     });
});