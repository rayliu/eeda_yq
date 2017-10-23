define(['jquery', 'metisMenu', 'sb_admin', 'dataTables', 'validate_cn', 'jq_blockui', 'sco'], function ($, metisMenu) {

	$(document).ready(function() {


		$('#editForm').validate({
			rules : {
				rolename : {
					required : true,
					maxlength:50
					/* remote:{
	                	url: "/role/checkRoleNameExit", //后台处理程序    
                        type: "post",  //数据发送方式  
                        data:  {                     //要传递的数据   
                        	rolename: function() {   
                                return $("#rolename").val();   
                              }   

                        } 
					}*/
				},
				remark: {
					maxlength:50
				}
			},
			/* messages:{
            	 rolename:{
            		 remote:"岗位已存在"
            	 }
             }, */
			highlight : function(element) {
				$(element).closest('.form-group')
						.removeClass('has-success')
						.addClass('has-error');
			},
			success : function(element) {
				element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
			}
		});


        $('#saveBtn').click(function(event) {
            $.blockUI({ 
                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
            });
            var role_permisstions = [];
            var inputArr = $('input[type=checkbox]');
            $.each(inputArr, function(index, elment) {
                var el = $(elment);
                var module_id=el.attr('module_id');
                var permission_id=el.attr('permission_id');
                var permission_code=el.attr('code');
                var role_permission_id=el.attr('role_permission_id');
                 
                role_permisstions.push({
                    module_id: module_id,
                    permission_id: permission_id,
                    permission_code: permission_code,
                    role_permission_id: role_permission_id,
                    checked: el.prop('checked')
                });
                 
            });

            console.log(role_permisstions);
            var submitObj={
                role_id: $('#role_id').val(),
                role_name: $('#rolename').val(),
                role_desc: $('#remark').val(),
                role_permisstions: role_permisstions
            };

            $.post('/role/save', {submitObj:JSON.stringify(submitObj)}, function(data){
                if(data=='ok'){
                    $.unblockUI();
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                }
            }).fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            });
        });
	});
});