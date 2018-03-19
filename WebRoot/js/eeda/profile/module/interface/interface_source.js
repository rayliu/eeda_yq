define(['jquery'], 
  function ($) {
        var dataTable = eeda.dt({
          id: 'interface_data_source_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "data": "ID",
                "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    if(!data){
                      data='';
                    }
                    return '<button type="button" class="btn table_btn btn-xs delete_field" >'
                          +'<i class="fa fa-trash-o"></i> 删除</button>'
                          +'<input name="ID" type="hidden" value="'+data+'">';
                  }
              },
              { "data": "FORM_LEFT",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="form_left" value="'+data+'">';
                }
              }, 
              { "data": "FORM_LEFT_FIELD",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="form_left_field" value="'+data+'">';
                }
              },
              { "data": null, 
                "render": function ( data, type, full, meta ) {
                    return '=';
                  }
              },
              { "data": "FORM_RIGHT",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="form_right" value="'+data+'">';
                }
              }, 
              { "data": "FORM_RIGHT_FIELD",
                "render": function ( data, type, full, meta ) {
                  if(!data){
                      data='';
                    }
                  return '<input name="form_right_field" value="'+data+'">';
                }
              }
          ]
        });

        $('#addInterfaceBtn_source').click(function(event) {
          var box = $('#source_box');

          if(box.html().trim() == ''){
            var form_box = '<div class="table_block" eeda_id="">'+$('#inteface_form_name').val()
                   +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                   +'</div>'
            box.append(form_box);
          }else{
            var form_box = '<div class="table_block_right">'
                          +'<div class="connect_line"></div>'
                          +'<div class="connect_type" style="">  '
                          +'    <select class="form-control operator" name="operator">'
                          +'        <option>交集</option>'
                          +'        <option>左关联</option>'
                          +'    </select> '
                          +'</div>'
                          +'<div class="connect_line"></div>'
                          +'<div class="table_block" eeda_id="">'+$('#inteface_form_name').val()
                          +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                          +'</div>'
                          +'</div>';
            box.append(form_box);
          }
        });

        var delete_block_list=[];
        $('#source_box').on('click', 'a', function(event) {
            var block = $(this).closest('.table_block_right');
            var id=block.attr('eeda_id');
            if(id != ''){
              delete_block_list.push({ID: id, is_delete:'Y'});
            }

            if(block.is('.table_block_right')){
              block.remove();
            }else{
              lock = $(this).closest('.table_block').remove();
            }

         });


        $('#add_source_condition_btn').click(function(event) {
            dataTable.row.add({}).draw();
        });

        var deleteList=[];
        $('#interface_data_source_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          if(id==null){
        	  return;
          }
          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });

        var buildDetail = function(){
            var block_arr=[];
            var blocks = $('#source_box .table_block');
            $.each(blocks, function(index, item) {
              var block = $(item);

              if(index==0){
                var obj={
                  ID: block.attr('eeda_id'),
                  SEQ: index,
                  FORM_NAME: block.text().trim()
                };
                block_arr.push(obj);
              }else{
                var obj={
                  ID: block.attr('eeda_id'),
                  SEQ: index,
                  FORM_NAME: block.text().trim(),
                  JOIN_TYPE: block.parent().find('.operator').val()
                };
                block_arr.push(obj);
              }
            });

            var new_block_arr = block_arr.concat(delete_block_list);

            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/5; i++) {
              var item={
                ID: $(inputs[i*5]).val(),
                FORM_LEFT: $(inputs[i*5 + 1]).val(),
                FORM_LEFT_FIELD: $(inputs[i*5 + 2]).val(),
                FORM_RIGHT: $(inputs[i*5 + 3]).val(),
                FORM_RIGHT_FIELD: $(inputs[i*5 + 4]).val()
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteList);

            return {
              block_arr: new_block_arr,
              join_list: list
            };
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