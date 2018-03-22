define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {
	  var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: false,
          pageLength: false,
          ajax: "/BusinessAdmin/ad/list",
          columns: [
            { "data": "ORDER_NO","width": "100px","className":"order_no"},
            { "data": "PUT_IN_DAYS","width": "60px"},
            { "data": "BEGIN_DATE","width": "60px" },
            { "data": "END_DATE", "width": "60px" },
            { "data": "TOTAL_PRICE","className":"total_price","width": "50px"},
            { "data": "TRADE_STATUS", "width": "60px",
            	render: function(data,type,full,meta){
            		 var result = '';
            		 if(data=='TRADE_SUCCESS'||data=='TRADE_FINISHED'){
            			 result = "<span>已付款</span>";
            		 }else{
        				 result = "<span>待付款</span>";
            		 }
	            	 return result;
            	}
            },
            { "data": "STATUS", "width": "60px",
            	render: function(data,type,full,meta){
            		var result = '';
	           		if(data == '新建'){
	           			if(full.TRADE_STATUS==null){
	           				result = "<input type='button' class='stdbtn btn_yellow pay' value='支付'>&nbsp"
	           					   + "<input type='button' class='stdbtn btn_red delete' value='删除'>";
	           			}else{
	           				result = "<span>待审核</span>";
	           			}
	           		}else{
	           			result = "<span>已开通</span>";
	           		}
            		return result;
            	  
            	}
            }
          ]
		});
      
	  var year = new Date().getFullYear();
	  var month = new Date().getMonth();
	  var day = new Date().getDate();
	  var today = year+"-"+(month+1)+"-"+day;
	  var price = $('#price').val();
	  var date = new Date(today);
	  date.setFullYear(date.getFullYear()+1); 
	  var newDate = date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
	  $("#one_year").text(newDate);
	  date.setFullYear(date.getFullYear()+1); 
	  newDate = date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
	  $("#tow_year").text(newDate);
	  
	  $("[name=years]").click(function(){
		  if(this.value == 1){
			  $("#put_in_days").text('365');
			  $("#total_price").text(price);
		  }else{
			  $("#put_in_days").text('730');
			  $("#total_price").text(price*2);
		  }
	  });
	  

	  $('#payBtn').click(function() {
		  var self= this;
		  
		  var order = {};
		  order.years = $("[name=years]:checked").val();
		  if(order.years==1){
			  order.end_date = $("#one_year").text();
		  }else{
			  order.end_date = $("#tow_year").text();
		  }
		  order.put_in_days =  $("#put_in_days").text();
		  order.total_price = $("#total_price").text();
		  order.remark = $("#remark").val();
		  order.status = "新建";
		  self.disabled = true;
		  $.post("/BusinessAdmin/ad/diamond_save",{jsonStr:JSON.stringify(order)},function(data){
			  if(data){
				  //新开支付页面
				  $('#WIDout_trade_no').val(data.ORDER_NO);
				  $('#WIDtotal_amount').val($("#total_price").text());
				  $('#diamond_alipayment_form').submit();
			  }else{
				  $.scojs_message('支付失败', $.scojs_message.TYPE_ERROR);
			  }
			  self.disabled = false;
		  });
	  });
	  
	  $("#eeda_table").on("click",".pay",function(){
		  var order_no = $(this).parent().parent().find(".order_no").text();
		  var total_price = $(this).parent().parent().find(".total_price").text();
		  //新开支付页面
		  $('#WIDout_trade_no').val(order_no);
		  $('#WIDtotal_amount').val(total_price);
		  $('#diamond_alipayment_form').submit();
	  });
	  
	  $("#eeda_table").on("click",".delete",function(){
		  var id = $(this).parent().parent().attr("id");
		  $.post("/BusinessAdmin/ad/diamondDelete",{id:id},function(data){
			  if(data.result){
				  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
				  refleshTable();
			  }
		  });
	  });
	 
	  //异步刷新
      var refleshTable = function(){
    	  dataTable.ajax.url("/BusinessAdmin/ad/list").load();
      }
      
      /*function daysBetween(sDate1,sDate2){
    	  //Date.parse() 解析一个日期时间字符串，并返回1970/1/1 午夜距离该日期时间的毫秒数
    	  var time1 = Date.parse(new Date(sDate1));
    	  var time2 = Date.parse(new Date(sDate2));
    	  var nDays = Math.abs(parseInt((time2 - time1)/1000/3600/24));
    	  return nDays;
      };*/
  });
});