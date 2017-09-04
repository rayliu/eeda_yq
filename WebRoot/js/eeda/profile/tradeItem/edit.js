define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {

        $('#menu_profile').addClass('active').find('ul').addClass('in');

        
        //校验是否已存在此费用
        $('#orderForm').validate({
	            rules: {
	            	commodity_name: {
	                	required: true,
	                	remote:{
		                    url: "/tradeItem/checkCommodityNameExist",
		                    type: "post",
		                    data:  {
		                    	commodity_name: function() { 
		                              return $("#commodity_name").val();
		                        }
	                    	}
	                	}
	                }
	            },
	            messages:{
	            	commodity_name:{
	                    remote:"此商品名称已存在"
	                }
	            },
	            highlight: function(element) {
	                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
	            },
	            success: function(element) {
	                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
	            }
        });
 
        
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return false;
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