define(['jquery', 'sco','dataTablesBootstrap', 'validate_cn'], function ($) {
  $(document).ready(function() {

	 var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: false,
          pageLength: false,
          ajax: "/BusinessAdmin/ad/mobilelist",
          columns: [
            { "data": "ORDER_NO","width": "120px"},
            { "data": "AMOUNT","class":"title", "width": "60px"},
            { "data": "PUT_IN_TIME","width": "90px"},
            { "data": "PRICE","width": "60px" },
            { "data": "TOTAL_PRICE", "width": "60px" },
            { "data": "PHONE", "width": "100px"},
            { "data": null, "width": "60px",
            	render: function(data,type,full,meta){
            		if(full.STATUS == '新建'){
            			return "<a class='stdbtn btn_blue editBtn' id='"+full.ID+"' amount='"+full.AMOUNT+"' put_in_time='"+full.PUT_IN_TIME+"' total_price='"+full.TOTAL_PRICE+"' phone='"+full.PHONE+"'  remark='"+full.REMARK+"' href='#put_in_time'>编辑</a>";
            		}else{
            			return full.STATUS;
            		}
            	  
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
		  var price = $('#price').text();
		  var amount = $('#amount').val();
		  var total_price = parseFloat(price)*parseFloat(amount);
		  $("#total_price").text(total_price);
	  }); 
	  
	  //初始化价格
	  function init(){
		  var price = $('#price').text();
		  var amount = $('#amount').val();
		  var total_price = parseFloat(price)*parseFloat(amount);
		  $("#total_price").text(total_price);
	  };
	  init();
	  
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
    	    order.put_in_time = $("#put_in_time").val();
    	    order.total_price = $("#total_price").text();
    	    order.phone = $("#telphone").val();
    	    order.remark = $("#remark").val();
			$.post('/BusinessAdmin/ad/mobile_save',{jsonStr:JSON.stringify(order)},function(data) {
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
    	  	var remark =($(this).attr("remark")=='null'?"该订单暂时没备注！":$(this).attr("remark"));
    	  	$('#order_id').val(id);
			$('#amount').val(amount);
			$('#put_in_time').val(put_in_time);
			$('#total_price').text(total_price);
			$('#telphone').val(phone);
			$('#remark').val(remark);
		});
      
      //异步刷新
      var refleshTable = function(){
    	  dataTable.ajax.url("/BusinessAdmin/ad/mobilelist").load();
      }
  });
});