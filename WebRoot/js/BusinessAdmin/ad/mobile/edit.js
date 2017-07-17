define(['jquery', 'sco','dataTablesBootstrap', 'validate_cn'], function ($) {
  $(document).ready(function() {

	 var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: false,
          pageLength: false,

          ajax: "/BusinessAdmin/ad/list",
          columns: [
            { "data": "ORDER_NO","width": "120px"},
            { "data": "AMOUNT","class":"title", "width": "60px"},
            { "data": "PUT_IN_TIME","width": "90px"},
            { "data": "PRICE","width": "60px" },
            { "data": "TOTAL_PRICE", "width": "60px" },
            { "data": "PHONE", "width": "100px"},
            { "data": "STATUS", "width": "70px"},
            { "data": null, "width": "60px",
              render: function(data,type,full,meta){
                return "<a class='stdbtn btn_blue editBtn' id='"+full.ID+"' amount='"+full.AMOUNT+"' put_in_time='"+full.PUT_IN_TIME+"' total_price='"+full.TOTAL_PRICE+"' phone='"+full.PHONE+"' href='#amount'>编辑</a>";
              }
            }
          ]
		});

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
		  $("#total_price").attr("value",total_price);
		  $("#total_price").text(total_price);
	  }); 
	  
	  //提交按钮
      $('#save_btn').click(function(event) {
    	  var id = $("#item_id").val();
    	  if(id){
    		  update();
    	  }else{
    		  save();
    	  }
    	  refleshTable();
      });
      
      //保存方法
      var save = function(){
    	  if(!$('#eeda_form').valid()){
    		  return;  
    	  }
    	  var time = new Date();//获取当前时间
    	  //获取当前时间的年月日时分秒毫秒来生成订单号
    	  var order_no = (time.getFullYear().toString())+((time.getMonth()+1).toString())+(time.getDate().toString())
			            +(time.getHours().toString())+(time.getMinutes().toString())+(time.getSeconds().toString())
			            +(time.getMilliseconds().toString());
    	  var order = {};
    	  order.order_no = order_no;//订单号
    	  order.amount = $("#amount").val();//投放条数获取值
    	  order.put_in_time = $("#put_in_time").val();//投放时间获取值
    	  order.price = $('#price').attr('value');//单价获取值
    	  order.total_price = $('#total_price').attr('value');//结算价格获取值
    	  order.phone = $("#phone").val();//联系电话获取值
    	  order.status = "新建"; 
    	  $.post("/BusinessAdmin/ad/mobile_save",{jsonStr:JSON.stringify(order)},function(data){
			  if(data){
				  $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  }else{
				  $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			  }
		  });
      }
      
      //修改方法
      var update = function(){
    	  var order = {};
    	  order.id = $("#item_id").val();
    	  order.amount = $("#amount").val();//投放条数获取值
    	  order.put_in_time = $("#put_in_time").val();//投放时间获取值
    	  order.price = $('#price').attr('value');//单价获取值
    	  order.total_price = $('#total_price').attr('value');//结算价格获取值
    	  order.phone = $("#phone").val();//联系电话获取值
    	  $.post("/BusinessAdmin/ad/update",{jsonStr:JSON.stringify(order)},function(data){
			  if(data){
				  $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
			  }else{
				  $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
			  }
		  });
      }
      
      //编辑按钮
      $('#eeda_table').on('click','.editBtn',function(){
    	  	var id = $(this).attr('id');
    	  	var amount = $(this).attr('amount');
    	  	var put_in_time = $(this).attr('put_in_time');
    	  	var total_price = $(this).attr('total_price');
    	  	var phone = $(this).attr('phone');
    	  	$('#item_id').val(id);
			$('#amount').val(amount);
			$('#put_in_time').val(put_in_time);
			$('#total_price').attr("value",total_price);
			$('#total_price').text(total_price);
			$('#phone').val(phone);
		});
      
      //异步刷新
      var refleshTable = function(){
    	  dataTable.ajax.url("/BusinessAdmin/ad/list").load();
     }
  });
});