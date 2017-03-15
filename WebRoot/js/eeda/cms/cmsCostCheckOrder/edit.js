define(['jquery', 'metisMenu', 'sb_admin','./edit_item_table',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

$(document).ready(function() {

	document.title = '报关创建应付对账单 | ' + document.title;

    $('#menu_charge').addClass('active').find('ul').addClass('in');
    $("#breadcrumb_li").text('报关应付对账单');
    
    //构造主表json
    var buildOrder = function(){
    	var item = {};
    	var orderForm = $('#orderForm input,select,textarea');
    	for(var i = 0; i < orderForm.length; i++){
    		var name = orderForm[i].id;
        	var value =orderForm[i].value;
        	if(name){
        		if(name.indexOf("begin_time") != -1){
        			name = "begin_time";
        		}else if(name.indexOf("end_time") != -1){
        			name = "end_time"
        		}
        		item[name] = value;
        	}
    	}
        return item;
    }
    
    
    var buildCurJson = function(){
    	var items_array=[];
    	$('#currencyDiv ul li').each(function(){
    		var item={};
    		var new_rate = $(this).find('[name=new_rate]').val();
    		var rate = $(this).find('[name=rate]').val();
    		var currency_id = $(this).find('[name=new_rate]').attr('currency_id');
    		var rate_id = $(this).find('[name=new_rate]').attr('rate_id');
    		if(new_rate==''){
    			new_rate = rate;
    		}
    		
    		item.new_rate = new_rate;
    		item.rate = rate;
    		item.rate_id = rate_id;
    		item.currency_id = currency_id;
    		item.order_type = 'charge';
    		items_array.push(item);
    	})
    	return items_array;
    }
    
    //------------save
    $('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#orderForm").valid()){
            return;
        }
        
        $(this).attr('disabled', true);

        var order = buildOrder();
        order.have_invoice = $('input[type="radio"]:checked').val();
        order.id = $('#order_id').val();
        order.item_list = itemOrder.buildItemDetail();
        order.currency_list = buildCurJson();
        
        //异步向后台提交数据
        $.post('/cmsCostCheckOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
            	$("#creator_name").val(order.CREATOR_NAME);
                $("#create_time").val(order.CREATE_STAMP);
                $("#order_no").val(order.ORDER_NO);
                $("#order_id").val(order.ID);
                $("#status").val(order.STATUS);
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
                $('#confrimBtn').attr('disabled', false);
                $('#printTotaledBtn').attr('disabled', false);
                $('#printBtn').attr('disabled', false);                 
                //异步刷新明细表
                itemOrder.refleshTable(order.ID);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
        });
    });  


    //按钮控制
    var order_id = $("#order_id").val();
    var status = $("#status").val()
    if(order_id==""){
    	$('#saveBtn').attr('disabled', false);
    }else{
    	if(status=='新建'){
    		$('#saveBtn').attr('disabled', false);
    		$('#confrimBtn').attr('disabled', false);
    		$('#printTotaledBtn').attr('disabled', false);
    		$('#printBtn').attr('disabled', false);    		
    	}
    }
    
    //确认单据
    $('#confrimBtn').click(function(){
    	$(this).attr('disabled', true);
    	var id = $("#order_id").val();
    	 $.post('/cmsCostCheckOrder/confirm', {id:id}, function(data){
    		 if(data){
    			 $('#saveBtn').attr('disabled', true);
    			 $("#status").val('已确认');
    			 $("#confirm_name").val(data.CONFIRM_BY_NAME);
    			 $("#confirm_time").val(data.CONFIRM_STAMP); 
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
 			 
    		 }
         },'json').fail(function() {
        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
        	 $(this).attr('disabled', false);
         });
    })
    
    
    //打印应收对账明细
    $('#printBtn').click(function(){
    	var order_id = $('#order_id').val();
    	$.post('/jobOrderReport/printReceiveDetailPDF',{order_id:order_id},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    //打印应收对账明细
    $('#printTotaledBtn').click(function(){
    	var order_id = $('#order_id').val();
    	$.post('/jobOrderReport/printTotaledReceiveDetailPDF',{order_id:order_id},function(data){
    		if(data){
    			window.open(data);
    		}else{
    			$.scojs_message('生成应收对账单PDF失败',$.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    

});
});
