define(['jquery', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {

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