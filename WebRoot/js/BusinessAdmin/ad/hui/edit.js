define(['jquery', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
	  
	  function init(){
		  var status = $("#status").val();
		  if(status == 'B'){
			 $("#saveBtn").attr("disabled",true).css({'background':'#e4e4e4'});
			  $("#ban_tip").text("优惠操作已经被关闭，请联系管理员恢复 ")
		  }else if($("input[name='DiscountOpen']:checked").val()==null){
			  $("#saveBtn").attr("disabled",true);
		  }else{
			  $("#saveBtn").attr("disabled",false);
		  }
		  
	  };
	  
	  init();
	  $("input[name='DiscountOpen']").change(function(){
		  init();
	  })
      $('#saveBtn').click(function() {
    	  var slef = this;
    	  slef.disabled = true;
          var discount = $('#discount').val();
          var is_active = $("input[name='DiscountOpen']:checked").val();
          $.post('/BusinessAdmin/ad/hui_save', {discount: discount, is_active: is_active}, function(data, textStatus, xhr) {
              if(data){
                  $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
              }else{
                  $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
              }
              slef.disabled = false;
          });
      });
  });
});