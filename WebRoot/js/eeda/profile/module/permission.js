define(['jquery', 'dataTablesBootstrap', 'sco'], function ($) {
   
    	
        
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
                { "data": "URL", width: '60%',
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '/<input type="text" name="url" value="'+data+'" class="form-control"/>';
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

        var refresh_table = function(permission_list){
        	deletedPermisstionTableIds.length = 0;
        	dataTable.clear().draw();
             for (var i = 0; i < permission_list.length; i++) {
                 var permission = permission_list[i];
                 var permissionItem ={
                     ID: permission.ID,
                     CODE: permission.CODE,
                     NAME: permission.NAME,
                     URL: permission.URL
                 };
                 dataTable.row.add(permissionItem).draw(false);
             }
        }

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

        return {
        	refresh_table:refresh_table,
            deletedPermisstionTableIds: deletedPermisstionTableIds
        }; 
    
 });