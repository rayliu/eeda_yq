define(['jquery'], function ($) {
    

        var dataTable = eeda.dt({
          id: 'field_ref_display_table',
          paging: false,
          lengthChange: false,
          info: false,
          searching: false,
          
          columns: [
              { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    var id='';
                    if(data){
                      id=data;
                    }
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>'
                          +'<input name="id" type="hidden" value="'+id+'">';
                  }
              },
              { "data": "NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="name" value="'+data+'" class="form-control" style="width:200px"/>';
                  }
              },
              { "data": "operator",
                "render": function ( data, type, full, meta ) {
                    return '=';
                  }
              },
              { "data": "VALUE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="value" value="'+data+'" class="form-control" style="width:200px"/>';
                  }
              }
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#field_ref_display_table tbody').on('click', 'tr', function () {
            current_tr = this;
            $('#field_ref_display_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = dataTable.row( this ).index();
            var data = dataTable.row( this ).data();
           
        } );

        var buildDto = function(){
            var dto = {
              ID: $('#ref_id').val(),
              REF_FORM: $('#ref_form').val(),
              REF_FIELD: $('#ref_field').val(),
              DISPLAY_TYPE: $('input[name=ref_field_display_type]:checked').val(),
              REF_CONDITION: $('#ref_condition').val()
            };
            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*3]).val(),
                NAME: $(inputs[i*3 + 1]).val(),
                VALUE: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }

            dto.ITEM_LIST = itemList;
            return dto;
        };

        var clear = function(){
          dataTable.clear().draw();
        };

        $('#add_ref_btn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#field_ref_display_table tr:eq('+current_tr_index+')');
        });

        return {
            clear: clear,
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});