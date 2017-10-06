define(['jquery'], function ($) {
    

        var list_dataTable = eeda.dt({
          id: 'list_btns_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px"},
              { "data": "NAME"}, 
              // { "data": "TYPE"}, // 方便后台区分 list,  edit
              { "data": "BTN_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(data == 'sys'){
                      return '系统';
                    }else{
                      return '用户自定义'
                    }
                  }
              }, //方便用户区分 sys,  customize
              { "data": "PERMISSION"},
              { "data": "HIDE_CONDITION"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#list_btns_table tbody').on('click', 'tr', function () {
            current_tr = this;
            $('#list_btns_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = list_dataTable.row( this ).index();
            var data = list_dataTable.row( this ).data();
            
            $('#list_btn_name').val(data.NAME);
            $('#list_btn_type').val(data.BTN_TYPE);
            $('#list_btn_seq').val(data.SEQ);
        } );

        //同时把两个table 的row 都放进去
        var buildTableDetail = function(){
            var data = list_dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var edit_data = edit_dataTable.rows().data();
            
            for (var i = 0; i < edit_data.length; i++) {
              itemList.push(edit_data[i]);
            }

            return itemList;
        };

        var clear = function(){
            $('#list_btn_name').val('');
            $('#list_btn_type').val('');
            $('#list_btn_seq').val('');

            $('#edit_btn_name').val('');
            $('#edit_btn_type').val('');
            $('#edit_btn_seq').val('');

            list_dataTable.clear().draw();
            edit_dataTable.clear().draw();
        }

        $('#addListBtn').click(function(){
            list_dataTable.row.add({type:'list', btn_type:'customize'}).draw(false);
            current_tr_index = list_dataTable.rows().data().length;
            current_tr = $('#list_btns_table tr:eq('+current_tr_index+')');
        });
        
        //回写到table
        $('#comfirmListBtn').click(function(){
            var item = list_dataTable.rows(current_tr_index).data()[0];
            item.NAME=$('#list_btn_name').val();
            item.BTN_TYPE=$('#list_btn_type').val();
            item.SEQ=$('#list_btn_seq').val();
            list_dataTable.row(current_tr).data( item ).draw();
        });

        //-------------------edit
        var edit_dataTable = eeda.dt({
          id: 'edit_btns_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px"},
              { "data": "NAME"}, 
              // { "data": "TYPE"}, // 方便后台区分 list,  edit
              { "data": "BTN_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(data == 'sys'){
                      return '系统';
                    }else{
                      return '用户自定义'
                    }
                  }
              }, //方便用户区分 sys,  customize
              { "data": "PERMISSION"},
              { "data": "HIDE_CONDITION"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#edit_btns_table tbody').on('click', 'tr', function () {
            current_tr = this;
            $('#edit_btns_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = edit_dataTable.row( this ).index();
            var data = edit_dataTable.row( this ).data();
            
            $('#edit_btn_name').val(data.NAME);
            $('#edit_btn_type').val(data.BTN_TYPE);
            $('#edit_btn_seq').val(data.SEQ);
        } );

        
        $('#addEditBtn').click(function(){
            edit_dataTable.row.add({type:'edit', btn_type:'customize'}).draw(false);
            current_tr_index = edit_dataTable.rows().data().length;
            current_tr = $('#edit_btns_table tr:eq('+current_tr_index+')');
        });
        
        //回写到table
        $('#comfirmEditBtn').click(function(){
            var item = edit_dataTable.rows(current_tr_index).data()[0];
            item.NAME=$('#edit_btn_name').val();
            item.BTN_TYPE=$('#edit_btn_type').val();
            item.SEQ=$('#edit_btn_seq').val();
            edit_dataTable.row(current_tr).data( item ).draw();
        });

        return {
            clear: clear,
            buildTableDetail: buildTableDetail,
            list_dataTable: list_dataTable,
            edit_dataTable: edit_dataTable
        };
    
});