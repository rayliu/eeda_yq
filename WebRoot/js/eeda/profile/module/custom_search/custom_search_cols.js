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
              { "data": "SORT",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                  }
                  var opt1 = '<option value=""></option>';
                  var opt2 = '<option value="desc">降序</option>';
                  var opt3 = '<option value="asc">升序</option>';
                  if(data==''){
                    opt1 = '<option value="" selected></option>';
                  }
                  if(data=='desc'){
                    opt2 = '<option value="desc" selected>降序</option>';
                  }
                  if(data=='asc'){
                      opt3 = '<option value="asc" selected>升序/option>';
                  }
                  return '<select name="sort_flag" class="form-control" style="width:100%">'
                          +opt1
                          +opt2
                          +opt3
                          +'</select>';
                }
              }, 
              { "data": "HIDDEN_FLAG",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                  }
                  var opt1 = '<option value="N">否</option>';
                  var opt2 = '<option value="Y">是</option>';  
                  if(data=='N'){
                    opt1 = '<option value="N" selected>否</option>';
                  }
                  if(data=='Y'){
                      opt2 = '<option value="Y" selected>是</option>';
                  }
                  return '<select name="hidden_flag" class="form-control" style="width:100%">'
                          +opt1
                          +opt2
                          +'</select>';
                }
              },
              { "data": "WIDTH",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="width" value="'+data+'">';
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
            for (var i = 0; i < inputs.length/6; i++) {
              var item={
                ID: $(inputs[i*6]).val(),
                FIELD_NAME: $(inputs[i*6 + 1]).val(),
                EXPRESSION: $(inputs[i*6 + 2]).val(),
                SORT: $(inputs[i*6 + 3]).val(),
                HIDDEN_FLAG: $(inputs[i*6 + 4]).val(),
                WIDTH: $(inputs[i*6 + 5]).val()
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