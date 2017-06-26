define(['jquery', 'sco', 'jquery_ui', 'validate_cn'], function ($) {
  $(document).ready(function() {

    // $('#datepicker').datepicker({
    //     format: 'yyyy-mm-dd'
    // });

    $('#eeda_form').validate({
        rules: {
          mobile: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
            required: true
          }
        },
        messages:{
            mobile:"请输入联系电话",
        }
    }); 


      $('#save_btn').click(function(event) {
        if(!$("#eeda_form").valid()){
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
      });
  });
});