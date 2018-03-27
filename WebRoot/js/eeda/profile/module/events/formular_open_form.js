define(['jquery'], function ($) {

        var dataTable = eeda.dt({
          id: 'list_open_form_fields_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "FIELD_DISPLAY_NAME", "width": "50px"}, 
              { "data": "OPERATOR", "width": "50px",
                "render": function ( data, type, full, meta ) {
                    return '=';
                  }
              }, 
              { "data": "FORMULAR", "width": "30px"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#list_open_form_fields_table tbody').on('click', 'tr', function () {
            current_tr = this;
            $('#list_open_form_fields_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = dataTable.row( this ).index();
            var data = dataTable.row( this ).data();
            
            if(data){
              $('#field_display_name').val(data.FIELD_DISPLAY_NAME);
              $('#field_type').val(data.FIELD_TYPE);
              $('#sort_type').val(data.SORT_TYPE);
              $('#default_value').val(data.DEFAULT_VALUE);
              $('#seq').val(data.SEQ);
            }
            
        } );

        var buildFieldsDetail = function(){
            var data = dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }
            return itemList;
        };

        
        $('#addFieldBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#list_open_form_fields_table tr:eq('+current_tr_index+')');
        });
        
        //回写到table
        $('#confirmFieldBtn').click(function(){
            var item = dataTable.rows(current_tr_index).data()[0];
            item.FIELD_DISPLAY_NAME=$('#field_display_name').val();
            item.FIELD_TYPE=$('#field_type').val();
            item.SORT_TYPE=$('#sort_type').val();
            item.DEFAULT_VALUE=$('#default_value').val();
            item.SEQ=$('#seq').val();
            dataTable.row(current_tr).data( item ).draw();
        });

        var dataTable = eeda.dt({
            id: 'edit_open_form_fields_table',
            paging: false,
            lengthChange: false,
            columns: [
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                            '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                { "data": "FIELD_DISPLAY_NAME", "width": "50px"}, 
                { "data": "OPERATOR", "width": "50px",
                  "render": function ( data, type, full, meta ) {
                      return '=';
                    }
                }, 
                { "data": "FORMULAR", "width": "30px"}
            ]
          });

        return {
            buildFieldsDetail: buildFieldsDetail,
            dataTable: dataTable
        };
    
});