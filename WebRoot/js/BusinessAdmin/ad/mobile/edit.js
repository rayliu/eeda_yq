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
				put_in_time:{
					required: true,
				},
				phone : {
				    required: true,
					isMobile:true
				}
			},
		    messages: {
		    	put_in_time: {
		    		required: "投放时间不能为空!!"
		    	},
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
      $('#saveBtn').click(function(event) {
    	  var self = this;
    	  if(!$('#eeda_form').valid()){
    		  return false;
    	  }
    	  
    	  self.disabled = true;
    	    var order = {};
    	    order.id=$("#order_id").val();
    	    order.amount = $("#amount").val();
    	    order.price = $("#price").text();
    	    order.total_price = $("#total_price").text();
    	    order.phone = $("#phone").val();
    	    order.remark = $("#remark").val();
			$.post('/BusinessAdmin/ad/mobile_save',{param:JSON.stringify(order)},function(data) {
				if(data){
					$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					refleshTable();
				}else{
					$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				}
				self.disabled = false;
			});
      });
      
   
      
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