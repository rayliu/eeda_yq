define(['jquery', 'sco'], function ($) {
        //按钮事件响应
        $('#page-wrapper').on('click', 'button', function(event) {
            event.preventDefault();
            var btn = $(this);
            var btn_id = btn.attr('id');
            var btn_name = btn.attr('name');
            
            if(btn_name == "add_detail_row_btn"){
                var target_table_id = btn.attr('target_table');
                var dataTable = $('#'+target_table_id).DataTable();
                dataTable.row.add({}).draw(false);
            }else if(btn_name == "table_delete_row_btn"){
                var target_table_id = btn.closest('table').attr('id');
                var target_table_tr = btn.closest('tr');

                var dataTable = $('#'+target_table_id).DataTable();

                //var row_index = table.row( this ).index();
                dataTable.row(target_table_tr).remove().draw();
            }else{
                console.log('['+btn_id+'] btn click:');

                var module_id = $('#module_id').val();
                var order_id = $('#order_id').val();
                var btn_id = btn_id.split('-')[1].split('_')[1];

                $.post('/form/'+module_id+'-click-'+btn_id, function(events){
                    console.log(events);
                    if(events){
                        for (var i = 0; i < events.length; i++) {
                            var event = events[i];
                            if(event.TYPE == "open"){
                                var url = '/form/'+event.OPEN.MODULE_ID+'-add';
                                if(event.OPEN.OPEN_TYPE = 'newTab'){
                                    window.open(url);
                                }else if(event.OPEN.OPEN_TYPE = 'self'){
                                    window.location.href=url;
                                }else{
                                    window.open(url);
                                }
                            }else if(event.TYPE == "save"){
                                var $form = $("#module_form");
                                var data = getFormData($form);
                                console.log('save action....');
                                console.log(data);
                                if(order_id==-1){
                                    doAdd(data);
                                }else{
                                    doUpdate(data);
                                }
                            }
                        }
                    }
                });
            }
        });

        function getFormData($form){
            var unindexed_array = $form.serializeArray();
            var indexed_array = {};

            $.map(unindexed_array, function(n, i){
                indexed_array[n['name']] = n['value'];
            });

            var tables = $('table[type=dynamic]');
            var field_id_list = [];
            var detail_tables = [];

            $.each(tables, function(index, item) {
                var id=$(item).attr('id');
                var field_id = id.split('_')[2];
                field_id_list.push(field_id);

                var ar = [];
                $("#"+id+" tbody").each(function() {// tr:nth-child(n+2)
                  rowData = $(this).find('input, select, textarea').serializeArray();
                  var rowAr = {};
                  $.each(rowData, function(e, v) {
                    rowAr[v['name']] = v['value'];
                  });
                  ar.push(rowAr);
                });
                var table_item={
                    table_id: id,
                    data_list: ar
                };
                detail_tables.push(table_item);
            });

            indexed_array.detail_tables = detail_tables;

            return indexed_array;
        }


        function doAdd(data){
            $.post('/form/'+data.module_id+'-doAdd', {data: JSON.stringify(data)}, function(dto){
                if(dto){
                    var url = '/form/'+data.module_id+'-edit-'+dto.ID;
                    window.location.href=url;
                }
            });
        }

        function doUpdate(data){
            $.post('/form/'+data.module_id+'-doUpdate', {data: JSON.stringify(data)}, function(dto){
                if(dto){
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                }
            });
        }

});
