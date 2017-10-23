define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
        $('#menu_profile').addClass('active').find('ul').addClass('in');
        
        var id = $("#id").val();
        
        $('#orderForm').validate({
            rules: {
            	commodity_name:{
            		maxlength:100
            	},
            	commodity_code:{
            		maxlength:255
            	},
            	unit_name:{
            		maxlength:50
            	},
            	unit_name_eng:{
            		maxlength:100
            	},
            	VAT_rate:{
            		number:true,
            		maxlength:10
            	},
            	rebate_rate:{
            		number:true,
            		maxlength:10
            	},
            	remark:{
            		maxlength:255
            	}
            },
            messages:{
            	VAT_rate:{
            		maxlength: $.validator.format( "请输入一个 长度最多是 {0} 的数字" )
            	},
            	rebate_rate:{
            		maxlength: $.validator.format( "请输入一个 长度最多是 {0} 的数字" )
            	}
            	
            }
        });
        $("#commodity_name").blur(function(){
        	var commodity_name = $("#commodity_name").val();
        	$.post("/tradeItem/checkCommodityNameExist",{commodity_name:commodity_name,order_id:id},function(data){
        		if(!data){
        			$("#commodity_name").parent().append("<span style='color:red;display:block;' class='error_span'>此商品名称已存在</span>")
        			$("#commodity_name").closest('.form-group').removeClass('has-success').addClass('has-error');
        		}else{
        			$("#commodity_name").addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
        		}
        	});
        });
        $('#commodity_name').on('focus',function(){
        	$(this).parent().find("span").remove();
        });
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return false;
            }
            var errorlength = $("[class=error_span]").length;
            if(errorlength>0){
            	$.scojs_message('单据存在填写错误字段未处理', $.scojs_message.TYPE_ERROR);
            	return;
            }
            $(this).attr('disabled', true);

           
            var order = {
                id: $('#id').val(),
                commodity_name: $('#commodity_name').val(),
                commodity_code: $('#commodity_code').val(),  
                unit_name: $('#unit_name').val(),                
                unit_name_eng: $('#unit_name_eng').val(),
                VAT_rate: $('#VAT_rate').val(),                
                rebate_rate: $('#rebate_rate').val(),   
                remark: $('#remark').val()
            };
            //异步向后台提交数据
            $.post('/tradeItem/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#id").val(order.ID);
                	$("#commodity_name").val(order.COMMODITY_NAME);
                	$("#unit_name").val(order.UNIT_NAME);
                	$("#unit_name_eng").val(order.UNIT_NAME_ENG);
                	$("#VAT_rate").val(order.VAT_RATE);
                	$("#rebate_rate").val(order.REBATE_RATE);
                	$("#remark").val(order.REMARK);
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            });
        });
        
     });
});