$(document).ready(function() {
	if(order_no){
		document.title = order_no+' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');


	
	$('#charge_unit').keyup(function(){
			$('#pay_unit').val($(this).val());
	});

	$('#charge_person').keyup(function(){
		
		$('#pay_person').val($(this).val());
		
	});

	$('#pay_amount').keyup(function(){
			$('#charge_amount').val($(this).val());
		
	});

	$('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide(); 
    });

    //表单验证
	var validate = $('#inOutMiscOrderForm').validate({
        rules: {
        	charge_amount: {number: true},
        	pay_amount: {number: true},
        }
    });

	var saveOrder = function(e){
		$('#saveBtn').attr('disabled', true);
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();

		//提交前，校验数据
        if(!$("#inOutMiscOrderForm").valid()){
	       	return;
        }

        var order={
        	order_id: $('#orderId').val(),
        	order_type: $('#order_type').val(),
        	issue_date: $('#issue_date').val(),
        	pay_unit: $('#pay_unit').val(),
        	charge_unit: $('#charge_unit').val(),
        	biz_type: $('#biz_type').val(),
        	pay_person: $('#pay_person').val(),
        	charge_person: $('#charge_person').val(),
        	issue_office_id: $('#issue_office_id').val(),
        	ref_no: $('#ref_no').val(),
        	charge_amount: $('#charge_amount').val(),
        	pay_amount: $('#pay_amount').val(),
        	remark: $('#remark').val()
        };
        console.log(order);
		//异步向后台提交数据
		$.post('/inOutMiscOrder/save', {params:JSON.stringify(order)}, function(data){
			var order = data;
			if(order.ID>0){
				$("#orderNo").val(order.ORDER_NO);
				$("#create_date").html(order.CREATE_DATE);
				$("#orderId").val(order.ID);
				
				contactUrl("edit?id",order.ID);

				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$('#saveBtn').attr('disabled', false);
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				$('#saveBtn').attr('disabled', false);
			}
		},'json').fail(function() {
		    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    $('#saveBtn').attr('disabled', false);
		  });
	};
   
	
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveBtn").click(function(e){
 		saveOrder(e);
	});
	
    //初始化按钮
    if($("#pay_status").text()!='未付'||$("#charge_status").text()!='未收'){
    	$('#saveBtn').attr('disabled', true);
    }

    
} );