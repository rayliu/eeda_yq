define(['jquery', 'template', 'dataTablesBootstrap', 'sco'], function ($, template) {
    $(document).ready(function() {
        
        var role_list_html;//提前获取rolelist
        var role_list=[];
        var permission_list=[];

        $('#clearBtn').click(function(event) {
            var user_id = $('#user_id').val();
            window.open('/sys/clearMenuCache/'+user_id);
        });

        var generateRoleList=function(default_val){

            var role_list_template =
                    '<select class="role_id form-control" name="role_id">'+
                        '{{each list as item i}}'+
                        '    <option code="{{item.code}}" value="{{item.id}}" style="margin-top: 0px;margin-left: -10px;" {{if default_val==item.id}}selected{{/if}}>{{item.name}}</option>'+
                        '{{/each}}'+
                    '</select>';
            var renderTemplate = template.compile(role_list_template);

            if(role_list.length>0){
                var html = renderTemplate({
                    default_val: default_val,
                    list: role_list
                });
                return html;
            }

            var getRoleListPromise = $.post('/module/getRoleList');

            getRoleListPromise.done(function(data){
                console.log(data);
                role_list=[];
                if(data){
                    $.each(data, function(i, item){
                        var role={
                            id: item.ID,
                            code: item.CODE,
                            name: item.NAME
                        };
                        role_list.push(role);
                    });

                    var html = renderTemplate({
                        default_val: default_val,
                        list: role_list
                    });
                    role_list_html = html;
                }
            });
            return getRoleListPromise;
        };

        $('#addAuthBtn').on('click', function(event) {
            var btn_tr = $('#permission_table tbody>tr');
            var need_save = false;
            permission_list = [];
            $.each(btn_tr, function(i, item){
                if($(item).attr('id') == ''){
                    need_save = true;
                }else{
                    permission_list.push({
                        id: $(item).attr('id'),
                        code: $(item).find('[name=code]').val(),
                        name: $(item).find('[name=name]').val()
                    });
                }
            });

            if(need_save){
                $.scojs_message('有新增的权限未保存,请先保存', $.scojs_message.TYPE_ERROR);
                return;
            }

            var item={
                "ID": '',
                "ROLE_ID": '',
                "ROLE_PERMISSION": ''
            };

            if(!role_list_html){
                $.when( generateRoleList()).done(function(data) {
                    auth_table.row.add(item).draw(true);
                });
            }else{
                auth_table.row.add(item).draw(true);
            }
        });
        //-------------   子表的动态处理

        var auth_tableSetting = {
            "paging": false,
            "ordering": false,
            "info": false,
            "processing": true,
            "searching": false,
            "autoWidth": false,
            "language": {
                "url": "/js/lib/datatables/i18n/Chinese.json"
            },
            "createdRow": function ( row, data, index ) {
                $(row).attr('id', data.ID);
                $(row).attr('level', data.LEVEL);
                console.log('createdRow....');

                var roleEl = $(row).find('.role_select');
                var role_id = roleEl.text();
                roleEl.empty();
                if(!role_list_html){
                    $.when( generateRoleList()).done(function(data) {
                        roleEl.append(role_list_html);
                        roleEl.find('select').val(role_id);
                    });
                }else{
                    roleEl.append(role_list_html);
                    roleEl.find('select').val(role_id);
                }

            },
            //"ajax": "/damageOrder/list",
            "columns": [
                { "width": "5%", "orderable":false,
                    "render": function ( data, type, full, meta ) {
                        if(full.LEVEL == 'default'){
                            return '';
                        }else{
                            return '<a class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i></a>&nbsp;&nbsp;';
                        }
                    }
                },
                { "data": "ID", visible: false},
                { "data": "ROLE_ID", width: '15%', className:"role_select",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        console.log('data out....')
                        return data;
                    }
                },
                { "data": "ROLE_PERMISSION", width: '80%',
                    "render": function ( data, type, full, meta ) {
                        var html = generateCheckGroup(data, permission_list);
                        return html;
                    }
                }
            ]
        };

        var generateCheckGroup=function(data, permission_list){
            if(permission_list.length==0){
                var btn_tr = $('#permission_table tbody>tr');
                $.each(btn_tr, function(i, item){
                    permission_list.push({
                        id: $(item).attr('id'),
                        code: $(item).find('[name=code]').val(),
                        name: $(item).find('[name=name]').val()
                    });
                });
            }

            if(data){
                $.each(data, function(i, item){
                    var dataPermissionId = item.PERMISSION_ID;
                    var is_checked = (item.IS_AUTHORIZE==1?'checked':'');
                    $.each(permission_list, function(j, pItem){
                        var pId = pItem.id;
                        if(dataPermissionId==pId){
                            pItem.row_id = item.ID;
                            pItem.checked = is_checked;
                        }
                    });
                });
            }

            var auth_list = permission_list;
            var check_box_template =
                '<div class="form-group">'+
                '{{each list as item i}}'+
                '    <label class="checkbox-inline">'+
                '        <input type="checkbox" class="btn_id" style="margin-top: 0px;" '+
                '           row_id="{{item.row_id}}" name="{{item.name}}" code="{{item.code}}" value="{{item.id}}" {{item.checked}}>{{item.name}}</label>'+
                '{{/each}}'+
                '</div>';
            var btn_arr=[];
            $.each(auth_list, function(i, el){
                var code = el.code;
                var name = el.name;
                var checked=el.checked;

                var item={
                    id: el.id, //permission id
                    row_id: el.row_id, //role_permission id
                    code: code,
                    name: name,
                    checked: checked
                };
                btn_arr.push(item);
            });

            var render = template.compile(check_box_template);
            var html = render({
                list: btn_arr
            });
            return html;
        };

        var auth_table = $('#auth_table').DataTable(auth_tableSetting);


        var $auth_table = $('#auth_table');

        //删除表中一行
        $auth_table.on('click', '.delete', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            eeda.deletedAuthTableIds.push(tr.attr('id'))

            auth_table.row(tr).remove().draw();
        });

    });
});