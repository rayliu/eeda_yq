define(['jquery', 'metisMenu', 'layer', 'sb_admin', 'dataTables', 'validate_cn', 'jq_blockui'], 
    function ($, metisMenu) {

        $(document).ready(function() {
            document.title = '岗位编辑 | '+document.title;
            $("#breadcrumb_li").text('岗位编辑');
            
            $('#editForm').validate({
                rules : {
                    rolename : {
                        required : true
                        /* remote:{
                            url: "/role/checkRoleNameExit", //后台处理程序    
                            type: "post",  //数据发送方式  
                            data:  {                     //要传递的数据   
                                rolename: function() {   
                                    return $("#rolename").val();   
                                }   

                            } 
                        }*/
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


            $('#saveRoleBtn').click(function(event) {
                var layer_index = layer.load(1, {
                    shade: [0.3,'#000'] //0.3透明度的黑色背景
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
                    role_desc: $('#roleremark').val(),
                    role_permisstions: role_permisstions
                };

                $.post('/role/save', {submitObj:JSON.stringify(submitObj)}, function(data){
                    if(data){
                        layer.close(layer_index); 
                        $('#role_id').val(data.ID)
                        layer.alert('保存成功', {icon: 1});
                        //$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    }
                }).fail(function() {
                    layer.alert('保存失败', {icon: 1});
                    $('#saveRoleBtn').attr('disabled', false);
                    $.unblockUI();
                });
            });

        });
});