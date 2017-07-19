define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {
	  
	  
	  var year = new Date().getFullYear();
	  var month = new Date().getMonth();
	  var day = new Date().getDate();
	  var today = year+"-"+(month+1)+"-"+day;
	  
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
			  $("#total_price").text('999');
		  }else{
			  $("#put_in_days").text('730');
			  $("#total_price").text('1888');
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
		  order.status = "新建";
		 
		  self.disabled = true;
		  $.post("/BusinessAdmin/ad/dimond_save",{jsonStr:JSON.stringify(order)},function(data){
			  if(data){
				  $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			  }else{
				  $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			  }
			  self.disabled = false;
		  });
	  });
	  
	 
  });
});