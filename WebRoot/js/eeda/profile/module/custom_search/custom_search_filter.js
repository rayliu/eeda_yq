define(['jquery'], 
  function ($) {
        var dataTable = eeda.dt({
          id: 'custom_filter_table',
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
                          +'<input name="ID" type="hidden" value="'+data+'">';;
                  }
              },
              { "data": "PARAM_NAME",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="col_name" value="'+data+'">';
                }
              }, 
              { "data": "DATA_TYPE",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="value" value="'+data+'">';
                }
              },
              { "data": "MUST_FLAG",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="width" value="'+data+'">';
                }
              }, 
              { "data": "DEFAULT_VALUE",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="visible" value="'+data+'">';
                }
              }
          ]
        });

        $('#add_custom_search_filter_btn').click(function(event) {
            dataTable.row.add({}).draw();
        });

        var deleteList=[];
        $('#custom_filter_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          if(id!=""){
        	  deleteList.push({ID: id, is_delete:'Y'});
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
                PARAM_NAME: $(inputs[i*5 + 1]).val(),
                DATA_TYPE: $(inputs[i*5 + 2]).val(),
                MUST_FLAG: $(inputs[i*5 + 3]).val(),
                DEFAULT_VALUE: $(inputs[i*5 + 4]).val()
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteList);

            return list;
        };

        var clear = function() {
          dataTable.clear().draw();
        };

        return {
            clear: clear,
            buildDetail: buildDetail,
            dataTable: dataTable
        };
    

});