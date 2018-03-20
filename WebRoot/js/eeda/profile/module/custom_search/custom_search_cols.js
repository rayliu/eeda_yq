define(['jquery'], 
  function ($) {
        var dataTable = eeda.dt({
          id: 'custom_cols_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    if(!data){
                      data='';
                    }
                    return '<button type="button" class="btn table_btn btn-xs delete_field" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>'
                          +'<input name="ID" type="hidden" value="'+data+'">';
                  }
              },
              { "data": "COL_NAME",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="col_name" value="'+data+'">';
                }
              }, 
              { "data": "VALUE",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="value" value="'+data+'">';
                }
              },
              { "data": "WIDTH",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="width" value="'+data+'">';
                }
              }, 
              { "data": "IS_VISIBLE",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="visible" value="'+data+'">';
                }
              }
          ]
        });

        $('#add_source_col_btn').click(function(event) {
            dataTable.row.add({}).draw();
        });

        var deleteList=[];
        $('#custom_cols_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });

        var buildDetail = function(){

            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/5; i++) {
              var item={
                ID: $(inputs[i*5]).val(),
                COL_NAME: $(inputs[i*5 + 1]).val(),
                VALUE: $(inputs[i*5 + 2]).val(),
                WIDTH: $(inputs[i*5 + 3]).val(),
                IS_VISIBLE: $(inputs[i*5 + 4]).val()
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteList);

            return list;
        };

        var clear = function() {
          dataTable.clear().draw();
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            dataTable: dataTable
        };

});