define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {

        $('#menu_profile').addClass('active').find('ul').addClass('in');

        //校验是否已存在此费用
        $('#orderForm').validate({
	            rules: {
	                code: {
	                	maxlength:20,
	                	remote:{
		                    url: "/finItem/checkCodeExist",
		                    type: "post",
		                    data:  {
		                    	id: function() {
	                				return $("#id").val();
	                			},
		                        code: function() { 
		                              return $("#code").val();
		                        }
	                    	}
	                	}
	                },
	                name: {
	                	required: true,
	                	maxlength:100,
	                	remote:{
	                		url: "/finItem/checkNameExist",
	                		type: "post",
	                		data:  {
	                			id: function() {
	                				return $("#id").val();
	                			},
	                			name: function() { 
	                				return $("#name").val();
	                			}
	                		}
	                	}
	                },
	                name_eng: {
	                	maxlength:100,
	                	remote:{
	                		url: "/finItem/checkNameEngExist",
	                		type: "post",
	                		data:  {
	                			id: function() {
	                				return $("#id").val();
	                			},
	                			name_eng: function() { 
	                				return $("#name_eng").val();
	                			}
	                		}
	                	}
	                },
	                remark:{
	                	maxlength:255
	                }
	            },
	            messages:{
	                code:{
	                    remote:"此费用代码已存在"
	                },
	                name:{
	                	remote:"此费用名称已存在"
	                },
	                name_eng: {
	                	remote:"此费用名称(英)已存在"
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
            //提交前，校验必填
        	var formRequired = 0;
            $('#orderForm').each(function(){
            	if(!$(this).valid()){
            		formRequired++;
                }
            })
            
            if(formRequired>0){
            	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
                return;
            }
            
            $(this).attr('disabled', true);

           
            var order = {
                id: $('#id').val(),
                code: $('#code').val(),                
                name: $('#name').val(),                
                name_eng: $('#name_eng').val(),
                binding_currency: $('#binding_currency').val(),
                remark: $('#remark').val()
            };
            //异步向后台提交数据
            $.post('/finItem/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#id").val(order.ID);
                	$("#code").val(order.CODE);
                	$("#name").val(order.NAME);
                	$("#name_eng").val(order.NAME_ENG);
                	$('#binding_currency').val(order.BINDING_CURRENCY),
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