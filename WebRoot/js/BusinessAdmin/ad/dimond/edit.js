define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {
	  
	  var time = new Date();
	  $("#twoYear").text(time.toLocaleString());
	  $("#diamondBuyDays span").text("730");
	  $("#years2").click(function(){
		  if($("#years2").prop("checked")){
			  $("#years1").prop("checked",false);
			  $("#oneYear").text("");
			  var time = new Date();
			  $("#twoYear").text(time.toLocaleString());
			  $("#diamondBuyDays span").text("730");
			  $("#total_price").text("1888元");
		  }
	  });
	  $("#years1").click(function(){
		  if($("#years1").prop("checked")){
			  $("#years2").prop("checked",false);
			  $("#twoYear").text("");
			  var time = new Date();
			  $("#oneYear").text(time.toLocaleString());
			  $("#diamondBuyDays span").text("365");
			  $("#total_price").text("999元");
		  }
	  });
	  
	  $('#paymentBtn').click(function(event) {
		  var years = "";
		  if($("#years2").prop("checked")){
			  years = "2";
		  }
		  if($("#years1").prop("checked")){
			  years = "1";
		  }
		  var total_price = $("#total_price").attr("value");
		  var status = "新建";
		  
		  $.post("/BusinessAdmin/ad/dimond_save",{years:years,total_price:total_price,status:status},function(data){
			  if(data){
				  alert("成功");
			  }else{
				  alert("失败");
			  }
		  });
	  });
	  
	 
  });
});