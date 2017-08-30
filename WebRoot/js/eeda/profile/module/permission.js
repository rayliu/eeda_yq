define(['jquery', 'dataTablesBootstrap', 'sco'], function ($) {
    $(document).ready(function(template) {
    	document.title = '模块定义 | '+document.title;
        
        //$('[data-toggle=tooltip]').tooltip();
        //-------------   子表的动态处理
        var subIndex=0;

        var deletedPermisstionTableIds=[];

        //删除一行
        $("#permission_table").on('click', '.delete', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            deletedPermisstionTableIds.push(tr.attr('id'))
            dataTable.row(tr).remove().draw();
        });

        //添加行
        $('#addPermissionBtn').click(function(event) {
            var item={
                "ID": '',
                "PERMISSION_CODE": '',
                "PERMISSION_NAME": '',
                "URL": ''
            };
            dataTable.row.add(item).draw(false);
        });

        //定义子表“显示类型”：字段，列表
        $('#fields_body').on('change', 'select.s_type', function(event) {
            var select = $(this);
            var add_btn_type_field = select.parent().parent().parent().find('div[name=add_btn_type_field]')
            if('字段' == select.val()){
                add_btn_type_field.hide();
            }else{
                add_btn_type_field.show();
            }
        });
        //定义“新增”按钮类型
        $('#fields_body').on('change', 'select.s_add_btn_type', function(event) {
            var select = $(this);
            var editIcon = select.parent().find('a[name=addBtnSetting]');
            if('添加空行' == select.val()){
                editIcon.hide();
            }else{
                editIcon.show();
            }
        });
        //-------------   子表的动态处理

        var tableSetting = {
            paging: false,
            "info": false,
            "processing": true,
            "searching": false,
            "autoWidth": false,
            "language": {
                "url": "/js/lib/datatables/i18n/Chinese.json"
            },
            "createdRow": function ( row, data, index ) {
                $(row).attr('id', data.ID);
               
            },
            //"ajax": "/damageOrder/list",
            "columns": [
                { "orderable":false, width: '7%',
                    "render": function ( data, type, full, meta ) {
                      return '<a class="remove delete" href="javascript:void(0)" title="删除"><i class="glyphicon glyphicon-remove"></i> </a>';
                    }
                },
                { "data": "ID", visible: false},
                { "data": "CODE",  width: '20%',
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                      return '<input type="text" name="code" value="'+data+'" class="form-control"/>';
                    }
                },
                { "data": "NAME", width: '15%',
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="name" value="'+data+'" class="form-control"/>';
                    }
                },
                { "data": "URL", width: '20%',
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '/<input type="text" name="url" value="'+data+'" class="form-control"/>';
                    }
                },
                { "data": "TEMPLATE_PATH", width: '60%',  visible: false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="template_path" value="'+data+'" class="form-control" style="width: 100%;"/>';
                    }
                }
            ]
        };

        var dataTable = $('#permission_table').DataTable(tableSetting);

        //等页面组件装载完成后，再绑定事件
        var bindFieldTableEvent= function(){
            var $fields_table = $("#fields_body table");

            $('.addBtnSetting').click(function(){
                addBtnSettingClick(this);
            });

            //编辑表中一行字段的属性
            $fields_table.on('click', '.edit', function(e){
                e.preventDefault();

                $("#modalForm")[0].reset();
                $("#modal_field_type_ext_div").hide();
                $("#customize_list").hide();

                var tr = $(this).parent().parent()[0];

                $('#modal_row_id').val($(tr).attr('id'));

                $("#modal_field_name").val($(tr.children[1]).find('input').val());
                $("#modal_field_type").val($(tr.children[2]).find('select').val());
                if('下拉列表' == $(tr.children[2]).find('select').val()){
                    $("#modal_field_type_ext_type").val($(tr).find('>input.ext_type').val());
                    $("#modal_field_type_ext_text").val($(tr).find('>textarea.ext_text').val());
                    $("#modal_field_type_ext_div").show();
                }
                if('自定义列表值' == $(tr).find('>input.ext_type').val()){
                    $("#customize_list").show();
                }

                $("#editField").modal('show');
            });

            //删除表中一行
            $fields_table.on('click', '.delete', function(e){
                e.preventDefault();
                var tr = $(this).parent().parent();
                var dataTable = $fields_table.DataTable();
                dataTable.row(tr).remove().draw();
            });

            $fields_table.find('tbody').sortable({
              revert: true
            });

        }

        $("#modal_field_type").on('change', function(){
            if('下拉列表' == $(this).val()){
                $("#modal_field_type_ext_div").show();
            }else{
                $("#modal_field_type_ext_div").hide();
            }
            if('弹出列表, 从其它数据表选取' == $(this).val()){
                $("#modal_field_type_pop_div").show();
            }else{
                $("#modal_field_type_pop_div").hide();
            }
        });

        $("#modal_field_type_ext_type").on('change', function(){
            if('自定义列表值' == $(this).val()){
                $("#customize_list").show();
            }else{
                $("#customize_list").hide();
            }
        });
        
        //对话框关闭，填值到列表中
        $('#modalFormOkBtn').click(function(){
            var row_id = $('#modal_row_id').val();
            var tr = $('tr#'+row_id)[0];
            $(tr.children[2]).find('select').val($("#modal_field_type").val());
            $(tr).find('>input.ext_type').val($("#modal_field_type_ext_type").val());
            $(tr).find('>textarea.ext_text').text($("#modal_field_type_ext_text").val());
            $("#editField").modal('hide');
        });

        eeda.deletedAuthTableIds=[];

        var buildAuthTableDetail=function(){
            var item_table_rows = $("#auth_table tr");
            var items_array=[];
            for(var index=0; index<item_table_rows.length; index++){
                if(index==0)
                    continue;

                var row = item_table_rows[index];
                var empty = $(row).find('.dataTables_empty').text();
                if(empty)
                  continue;
                
                var id = $(row).attr('id');
                if(!id){
                    id='';
                }
                
                var item={}
                item.id = id;
                item.role_id = $(row).find('[name=role_id]').val();
                item.role_code = $(row).find('[name=role_id] :selected').attr('code');
                var box_array=[];
                var checkBoxs = $(row).find('[type=checkbox]');
                for(var i = 0; i < checkBoxs.length; i++){
                    var boxEl= checkBoxs[i];
                    var boxItem={
                        id: $(boxEl).attr('row_id'),
                        permission_id: $(boxEl).val(),
                        permission_code: $(boxEl).attr('code'),
                        permission_name: $(boxEl).attr('name'),
                        is_authorize: $(boxEl).prop('checked')
                    }
                    box_array.push(boxItem);
                }
                item.permission_list = box_array;
                item.action = id.length > 0?'UPDATE':'CREATE';
                items_array.push(item);
            }

            //add deleted items
            for(var index=0; index<eeda.deletedAuthTableIds.length; index++){
                var id = eeda.deletedAuthTableIds[index];
                var item={
                    id: id,
                    action: 'DELETE'
                };
                items_array.push(item);
            }
            eeda.deletedAuthTableIds = [];
            return items_array;
        };

        var saveAction=function(btn, is_start){
            is_start = is_start || false; 

            var dto = {
                module_id: $('#module_id').text(),
                is_public: $("#is_public").prop('checked'),
                permission_list: eeda.buildTableDetail('permission_table', deletedPermisstionTableIds),
                auth_list: buildAuthTableDetail()
            };

            console.log('saveBtn.click....');
            console.log(dto);

            //异步向后台提交数据
            $.post('/module/saveStructure', {params:JSON.stringify(dto)}, function(data){
                var order = data;
                console.log(order);
                if(order.ID>0){
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    btn.attr('disabled', false);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    btn.attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                btn.attr('disabled', false);
            });
        };


        $('#saveBtn').on('click', function(e){
            $(this).attr('disabled', true);

            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            // if(!$("#orderForm").valid()){
            //     return;
            // }

            saveAction($(this));
        });

        //单据预览
        $('#previewBtn').click(function(){
            window.open('/module/preview/'+$("#module_id").text());
        });

        $('#startBtn').click(function(e){
            $(this).attr('disabled', true);

            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();

            saveAction($(this), true);
        });
    });
 });