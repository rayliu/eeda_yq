define(['jquery'], function ($) {
    

        var dataTable = eeda.dt({
          id: 'field_checkbox_table',
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
              { "data": "SEQ", "width": "50px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="seq" value="'+data+'" class="form-control" style="width:50px"/>';
                  }
              },
              { "data": "NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="name" value="'+data+'" class="form-control" style="width:200px"/>';
                  }
              },
              { "data": "IS_DEFAULT", "width": "30px",
                "render": function ( data, type, full, meta ) {
                    if(data == 'Y'){
                      return '<input type="radio" name="is_default" class="checkBox" style="width:30px" checked>';
                    }
                    return '<input type="radio" name="is_default" class="checkBox" style="width:30px">';
                  }
              }
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#field_checkbox_table tbody').on('click', 'tr', function () {
            current_tr = this;
            $('#field_checkbox_table  tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = dataTable.row( this ).index();
            var data = dataTable.row( this ).data();
           
        } );

        var buildDto = function(){
            var dto = {
              ID: $('#check_box_id').val(),
              IS_SINGLE_CHECK: $('#is_single_check').prop('checked')==true?'Y':'N',
              LINE_DISPLAY_NUMBERS: $('#line_display_numbers').val()
            };
            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/4; i++) {
              var item={
                ID: $(inputs[i*4]).val(),
                SEQ: $(inputs[i*4 + 1]).val(),
                NAME: $(inputs[i*4 + 2]).val(),
                IS_DEFAULT: $(inputs[i*4 + 3]).prop('checked')==true?'Y':'N'
              };

              itemList.push(item);
            }
            var list = itemList.concat(deleteList);
            dto.ITEM_LIST = list;
            return dto;
        };

        var deleteList=[];
        $('#field_checkbox_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });
        
        var clear = function(){
          dataTable.clear().draw();
        };

        $('#addCheckboxBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#field_checkbox_table tr:eq('+current_tr_index+')');
        });

        return {
            clear: clear,
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});