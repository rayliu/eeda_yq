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
                          +'<input name="id" type="hidden" value="'+data+'">';
                  }
              },
              { "data": "FIELD_NAME",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="field_name" value="'+data+'">';
                }
              }, 
              { "data": "EXPRESSION",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="expression" value="'+data+'">';
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
              { "data": "HIDDEN_FLAG",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="hidden_flag" value="'+data+'">';
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

          if(id!=""){
        	  deleteList.push({ID: id.toString(), IS_DELETE:'Y'});
          }
          return false;
        });

        var buildDetail = function(){

            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/5; i++) {
              var item={
                ID: $(inputs[i*5]).val(),
                FIELD_NAME: $(inputs[i*5 + 1]).val(),
                EXPRESSION: $(inputs[i*5 + 2]).val(),
                WIDTH: $(inputs[i*5 + 3]).val(),
                HIDDEN_FLAG: $(inputs[i*5 + 4]).val()
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