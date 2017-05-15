define(['jquery', 'metisMenu', 'sb_admin', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
        document.title = '车辆信息 | '+document.title;

        var car_no = $("#car_number").val();
        if(car_no){
            document.title = car_no +' | '+document.title;
        }

        jQuery.validator.addMethod("stringCheck", function(value, element) {
            return this.optional(element) || /^[u0391-uFFE5w]+$/.test(value);
            }, "只能包括中文字、英文字母、数字和下划线");

        $('#leadsForm').validate({
            rules: {
                username: {
                required: true,
                stringCheck:true,
                minlength: 5
                },
              password:{//form 中 name为必填
                required: true,
                minlength: 5
              },
              confirm_password: { 
                  required: true, 
                  minlength: 5, 
                  equalTo: "#password" 
                  } 
             },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
        $("[name=gongsiche],[name=jieche]").on('click',function(){
        	if(this.checked){
        		if(this.name=='gongsiche'){
        			$("[name=jieche]").prop('checked',false);
        		}else{
        			$("[name=gongsiche]").prop('checked',false);
        		}
        	}
        });
    });
});