define(['jquery'], function ($) {
    

        var dataTable = eeda.dt({
          id: 'field_auto_no_table',
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
              { "data": "TYPE", "width": "70px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<select name="type" class="form-control" style="width:100%">'
                          +'<option>固定文字</option>'
                          +'<option>日期变量</option>'
                          +'<option>流水号位数</option>'
                          +'</select>' ;
                  }
              },
              { "data": "VALUE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="name" value="'+data+'" class="form-control" style="width:100%"/>';
                  }
              }
          ]
        });

        var deleteList=[];
        $('#field_auto_no_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });

        var buildDto = function(){
            var dto = {
              ID: $('#auto_no_id').val(),
              IS_GEN_BEFORE_SAVE: $('#is_gen_before_save').prop('checked')==true?'Y':'N'
            };
            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*3]).val(),
                TYPE: $(inputs[i*3 + 1]).val(),
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

        $('#addAutoNoBtn').click(function(){
            dataTable.row.add({}).draw(false);
        });

        return {
            clear: clear,
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});