define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {

    // $('#datepicker').datepicker({
    //     format: 'yyyy-mm-dd'
    // });

    /*$('#eeda_form').validate({
        rules: {
          mobile: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
            required: true
          }
        },
        messages:{
            mobile:"请输入联系电话",
        }
    }); */
	  
	  
	  //结算价格计算并默认显示
	  var delivery_number = $("#delivery_number").val();
	  var price = $('#price').attr('value');
	  var settlement_price = delivery_number*price;
	  $('#settlement_price').attr('value',settlement_price);
	  $("#settlement_price").text(settlement_price);
	  
	  $("#delivery_number").change(function(){
		  var delivery_number = $("#delivery_number").val();
		  if(delivery_number == "100"){
			  $('#settlement_price').attr('value',100*price);
			  $("#settlement_price").text(100*price);
		  }
		  if(delivery_number == "200"){
			  $('#settlement_price').attr('value',200*price);
			  $("#settlement_price").text(200*price);
		  }
		  if(delivery_number == "500"){
			  $('#settlement_price').attr('value',500*price);
			  $("#settlement_price").text(500*price);
		  }
		  if(delivery_number == "1000"){
			  $('#settlement_price').attr('value',1000*price);
			  $("#settlement_price").text(1000*price);
		  }
		  if(delivery_number == "5000"){
			  $('#settlement_price').attr('value',5000*price);
			  $("#settlement_price").text(5000*price);
		  }
	  }); 
	  
	  
	  //提交按钮
      $('#save_btn').click(function(event) {
      /*  if(!$("#eeda_form").valid()){
            return;
        }
          var discount=$('#discount').val();
          var is_active=$("input[name='DiscountOpen']:checked").val();
          $.post('/BusinessAdmin/ad/mobile_save', {discount: discount, is_active: is_active}, function(data, textStatus, xhr) {
              if(data=='OK'){
                $.scojs_message('提交成功， 请等待工作人员与你联系', $.scojs_message.TYPE_OK);
              }else{
                $.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
              }
          });
      });*/
    	  
    	  var delivery_number = $("#delivery_number").val();//投放条数获取值
    	  var delivery_time = $("#delivery_time").val();//投放时间获取值
    	  var price = $('#price').attr('value');//单价获取值
    	  var settlement_price = $('#settlement_price').attr('value');//结算价格获取值
    	  var contact_phone = $("#contact_phone").val();//联系电话获取值
    	  
    	  var re = /^1\d{10}$/;
    	  
    	  if(!contact_phone){
    		  $.scojs_message('联系电话不能为空', $.scojs_message.TYPE_ERROR);
    		  return;
    	  }
    	  if(!re.test(contact_phone)){
    		  $.scojs_message('联系电话格式不正确', $.scojs_message.TYPE_ERROR);
    		  return;
    	  }
    	  
    	  $.post("/BusinessAdmin/ad/mobile_save",{delivery_number:delivery_number,delivery_time:delivery_time,price:price,settlement_price:settlement_price,contact_phone:contact_phone},function(data){
    			  if(data){
    				  $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
    			  }else{
    				  $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
    			  }
    		  });
    	  
      });
  });
});