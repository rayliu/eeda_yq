define(['jquery'], function ($) {
    

        var connect_dataTable = eeda.dt({
          id: 'field_detail_ref_table',
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
              { "data": "FIELD_FROM",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="field_from" value="'+data+'" class="form-control" />';
                  }
              },
              { "data": null, "width": "50px",
                "render": function ( data, type, full, meta ) {
                    return '==';
                  }
              }, 
              { "data": "FIELD_TO",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="field_to" value="'+data+'" class="form-control" style="width:200px"/>';
                  }
              }
          ]
        });


        var display_dataTable = eeda.dt({
          id: 'field_detail_ref_display_table',
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
              { "data": "TARGET_FIELD_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="field_name" value="'+data+'" class="form-control" />';
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

        var buildDto = function(){
            var dto = {
              ID: $('#detail_ref_id').val(),
              TARGET_FORM_NAME: $('#detail_ref_form').val()
            };

            var data = connect_dataTable.rows().data();
            var inputs = connect_dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*3]).val(),
                FIELD_FROM: $(inputs[i*3 + 1]).val(),
                FIELD_TO: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }

            dto.JOIN_CONDITION = itemList;

             data = display_dataTable.rows().data();
             inputs = display_dataTable.$('input, select');
             itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*3]).val(),
                TARGET_FIELD_NAME: $(inputs[i*3 + 1]).val(),
                VALUE: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }

            dto.DISPLAY_FIELD = itemList;
            return dto;
        };

        
        $('#add_detail_ref_btn').click(function(){
            connect_dataTable.row.add({}).draw(false);
        });
        
        $('#add_detail_ref_display_btn').click(function(){
            display_dataTable.row.add({}).draw(false);
        });

        var clear = function(){
          connect_dataTable.clear().draw();
          display_dataTable.clear().draw();
        };

        return {
            clear: clear,
            buildDto: buildDto,
            connect_dataTable: connect_dataTable,
            display_dataTable: display_dataTable
        };
    
});