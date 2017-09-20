define(['jquery'], function ($) {
    

        var dataTable = eeda.dt({
          id: 'edit_set_value_fields_table',
          paging: false,
          lengthChange: false,
          columns: [
              {  "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    var id='';
                    if(data){
                      id=data;
                    }
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'
                          +'<i class="fa fa-trash-o"></i> 删除</button>'
                          +'<input name="id" type="hidden" value="'+id+'">';
                  }
              },
              { "data": "NAME", "width": "200px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<div class="form-group input-group">'
                           +'     <input type="text" class="form-control" name="name" value="'+data+'"">'
                           +'     <span class="input-group-btn">'
                           +'         <button class="btn btn-default formular_pop"  target="name" type="button"><i class="fa fa-edit"></i>'
                           +'         </button>'
                           +'     </span>'
                           +' </div>';
                  }
              },
              { "data": null, "width": "30px",
                "render": function ( data, type, full, meta ) {
                    return '=';
                  }
              },
              { "data": "VALUE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<div class="form-group input-group" style="width: 100%;">'
                           +'     <input type="text" class="form-control" name="value" value="'+data+'">'
                           +'     <span class="input-group-btn">'
                           +'         <button class="btn btn-default formular_pop" target="value" type="button"><i class="fa fa-edit"></i>'
                           +'         </button>'
                           +'     </span>'
                           +' </div>';
                  }
              }
          ]
        });

        var buildDto = function(){

            var dto = {
              ID: $('#edit_event_value_id').val(),
              CONDITION : $('#edit_event_set_value_condition').val()
            };

            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*2]).val(),
                NAME: $(inputs[i*3 + 1]).val(),
                VALUE: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }

            dto.SET_FIELD_LIST = itemList;
            return dto;
        };

        $('#edit_set_value_table_addBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#edit_set_value_fields_table tr:eq('+current_tr_index+')');
        });

        return {
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});