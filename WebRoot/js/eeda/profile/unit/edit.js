define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {

      //校验是否已存在
        var order_id = $('#id').val();
        if(!order_id){
        	$('#orderForm').validate({
	            rules: {
	                code: {
	                	remote:{
		                    url: "/unit/checkCodeExist",
		                    type: "post",
		                    data:  {
		                        code: function() { 
		                              return $("#code").val();
		                        }
	                    	}
	                	}
	                },
	                name: {
	                	required: true,
	                	remote:{
	                		url: "/unit/checkNameExist",
	                		type: "post",
	                		data:  {
	                			code: function() { 
	                				return $("#name").val();
	                			}
	                		}
	                	}
	                },
	                name_eng: {
	                	remote:{
	                		url: "/unit/checkNameEngExist",
	                		type: "post",
	                		data:  {
	                			code: function() { 
	                				return $("#name_eng").val();
	                			}
	                		}
	                	}
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
        }
        
        
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $(this).attr('disabled', true);

           
            var order = {
                id: $('#id').val(),
                code: $('#code').val(),
                name: $('#name').val(),
                name_eng: $('#name_eng').val(),
                type: $('#type').val()
            };
            //异步向后台提交数据
            $.post('/unit/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#id").val(order.ID);
                	$("#code").val(order.CODE);
                	$("#name").val(order.NAME);
                	$("#name_eng").val(order.NAME_ENG);
                	$("#type").val(order.TYPE);
                    
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