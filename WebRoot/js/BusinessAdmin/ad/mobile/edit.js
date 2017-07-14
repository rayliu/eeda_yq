define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {

	  $('#eeda_form').validate({
			rules: {
				phone : {
				    required: true,
					isMobile:true
				}
			},
		    messages: {
		    	phone: {
		    		required: "电话不能为空!!"
			    }
		    }
	  });

	  
	  jQuery.validator.addMethod("isMobile", function(value, element) { 
		  var length = value.length; 
		  var mobile = /^1(3|4|5|7|8)\d{9}$/; 
		  return this.optional(element) || (length == 11 && mobile.test(value)); 
	  }, "请正确填写您的手机号码"); 

	  
	  //结算价格计算并默认显示
	  $("#amount").change(function(){
		  var price = $('#price').attr('value');
		  var amount = $('#amount').val();
		  var total_price = parseFloat(price)*parseFloat(amount);
		  $("#total_price").text(total_price);
	  }); 
	  
	  
	  //提交按钮
      $('#save_btn').click(function(event) {
    	  if(!$('#eeda_form').valid()){
    		  return;  
    	  }
    	  
    	  var order = {};
    	  order.amount = $("#amount").val();//投放条数获取值
    	  order.put_in_time = $("#put_in_time").val();//投放时间获取值
    	  order.price = $('#price').attr('value');//单价获取值
    	  order.total_price = $('#total_price').attr('value');//结算价格获取值
    	  order.phone = $("#phone").val();//联系电话获取值
    	  
    	  
    	  $.post("/BusinessAdmin/ad/mobile_save",{jsonStr:JSON.stringify(order)},function(data){
			  if(data){
				  $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  }else{
				  $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			  }
		  });
    	  
      });
  });
});