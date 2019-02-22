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
              { "data": "FROM_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="value" value="'+data+'" class="form-control" style="width:200px" disabled/>';;
                  }
              },
              { "data": "TO_NAME",
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
                FROM_NAME: $(inputs[i*3 + 1]).val(),
                TO_NAME: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }

            dto.ITEM_LIST = itemList;
            return dto;
        };

        var clear = function(){
          dataTable.clear().draw();
        };
        //-------选择引用表单的字段 start----
        var form_fields_select_pop_dataTable = eeda.dt({
          id: 'form_fields_select_pop_dataTable',
          paging: true,
          lengthChange: false,
          columns: [
              { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                      var id='';
                      if(data){
                      id=data;
                      }
                  return '<input type="checkBox" name="checkBox" style="margin-right:5px;">'
                  +'<input name="id" type="hidden" value="'+id+'">';
                  }
              },
              { "data": "FIELD_DISPLAY_NAME",
                  "render": function ( data, type, full, meta ) {
                      var str = "";
                      if(data){
                          str = data;
                      }
                      return data;
                  }
              },
              { "data": "FIELD_TYPE",
                  "render": function ( data, type, full, meta ) {
                      var str = "";
                      if(data){
                          str = data;
                      }
                      return data;
                  }
              }
          ]
        });

        $('#add_ref_btn').click(function(){
            var ref_form = $('#ref_form').val();
            if(!ref_form){
                alert('请先选择引用表单.');
                return;
            }
                
            $('#form_fields_select_modal_form_name').text(ref_form);
            var url="/module/getFormFields?form_name="+ref_form;
            form_fields_select_pop_dataTable.ajax.url(url).load();
            var targetId = $(this).attr('target');
            $('#form_fields_select_modal_target_id').val(targetId);
            $('#form_fields_select_modal').modal('show');
            $('.modal-backdrop').css({"z-index":"0"});
        });

        $('#form_fields_select_modal_ok_btn').click(function(event) {
          var targetId = $('#form_fields_select_modal_target_id').val();
          var form_name=$('#form_fields_select_modal_form_name').text();
          var tr_rows = $('#form_fields_select_pop_dataTable td input[type=checkBox]:checked').closest('tr');
          for(var i=0; i< tr_rows.length; i++){
            var tr = tr_rows[i];
            var field_name = $(tr).find('td:nth-child(2)').text();
            var item={};
            item.ID = "";
            item.FROM_NAME = form_name+'.'+field_name;
            item.TO_NAME = "";
            dataTable.row.add(item).draw(false);
          }
           
          //$('#'+targetId).val(form_name+'.'+field_name);
          $('#form_fields_select_modal').modal('hide');
        });
        //-------选择引用表单的字段 end----

        return {
            clear: clear,
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});