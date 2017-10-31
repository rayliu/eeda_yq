define(['jquery', 'metisMenu', 'sb_admin', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	$("#leadsForm").validate({
    	    rules: {
    	    	car_no:{
    	    		maxlength:50
    	    	},
    	    	driver:{
    	    		maxlength:50
    	    	},
    	    	phone:{
    	    		maxlength:50
    	    	},
    	    	short_phone:{
    	    		maxlength:50
    	    	},
    	    	rated_load:{
    	    		number:true,
    	    		maxlength:10
    	    	},
    	    	initial_mileage:{
    	    		number:true,
    	    		maxlength:10
    	    	},
    	    	toca_num:{
    	    		number:true,
    	    		maxlength:20
    	    	},
    	    	hundred_fuel_standard:{
    	    		number:true,
    	    		maxlength:10
    	    	},
    	    	rated_cube:{
    	    		number:true,
    	    		maxlength:10
    	    	},
    	    	head_weight:{
    	    		maxlength:50
    	    	},
    	    	toca_weight:{
    	    		maxlength:50
    	    	}
    	    },
    	    messages: {
    	    	rated_load:{
    	    		 maxlength: $.validator.format("最多可以输入 {0} 个数字")
    	    	},
    	    	initial_mileage:{
    	    		maxlength: $.validator.format("最多可以输入 {0} 个数字")
    	    	},
    	    	toca_num:{
    	    		maxlength: $.validator.format("最多可以输入 {0} 个数字")
    	    	},
    	    	hundred_fuel_standard:{
    	    		maxlength: $.validator.format("最多可以输入 {0} 个数字")
    	    	},
    	    	rated_cube:{
    	    		maxlength: $.validator.format("最多可以输入 {0} 个数字")
    	    	}
    	    }
    	});
        //保存
        //自动提交改为手动提交
        $("#save").click(function(){
      	 if(!$("#leadsForm").valid()){
      		 $.scojs_message('请填上面的必填字段', $.scojs_message.TYPE_ERROR);
      		  return false;
      	 }
      	 $("#save").attr("disabled",true);
//      	 var order={};
//      	 order.cars_json =itemOrder.buildCarsDetail();
//      	 $("#acount_json").val(JSON.stringify(order));
      	 $.post("/carInfo/save", $("#leadsForm").serialize(),function(data){
      		if(data=='abbrError'){
      			$.scojs_message('该车牌已存在', $.scojs_message.TYPE_ERROR);
      			$("#save").attr("disabled",false);
      		}else if(data.ID != null && data.ID != ""){
       			eeda.contactUrl("edit?id",data.ID);
       			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
       			$("#carId").val(data.ID);
       			$("#save").attr("disabled",false);
       		}else{
       			$.scojs_message('数据有误', $.scojs_message.TYPE_ERROR);
       			$("#save").attr("disabled",false);
       		}
      		//异步刷新明细表
      		if(data!='abbrError'){
//      			itemOrder.refleshTable(data.ID);
      		}
      		
           });
      	  
        });
        
        var car_no = $("#car_no").val();
        if(car_no){
            document.title = car_no +' | '+document.title;
        }

        jQuery.validator.addMethod("stringCheck", function(value, element) {
            return this.optional(element) || /^[u0391-uFFE5w]+$/.test(value);
            }, "只能包括中文字、英文字母、数字和下划线");

        $('#leadsForm').validate({
            rules: {
            	car_no:{//form 中 abbr为必填
                    required: true
                  }
             },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
        //公司车，街车二选一；
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