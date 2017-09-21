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
              { "data": "NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<input type="text" name="name" value="'+data+'" class="form-control" style="width:200px"/>';
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
              IS_DROPDOWN: $('#is_dropdown').prop('checked')==true?'Y':'N',
              REF_CONDITION: $('#ref_condition').val()
            };
            // var data = dataTable.rows().data();
            // var inputs = dataTable.$('input, select');
            // var itemList = [];
            // for (var i = 0; i < inputs.length/5; i++) {
            //   var item={
            //     ID: $(inputs[i*5]).val(),
            //     SEQ: $(inputs[i*5 + 1]).val(),
            //     NAME: $(inputs[i*5 + 2]).val(),
            //     CODE: $(inputs[i*5 + 3]).val(),
            //     IS_DEFAULT: $(inputs[i*5 + 4]).prop('checked')==true?'Y':'N'
            //   };

            //   itemList.push(item);
            // }

            // dto.ITEM_LIST = itemList;
            return dto;
        };

        
        $('#addCheckboxBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#field_checkbox_table tr:eq('+current_tr_index+')');
        });

        return {
            buildDto: buildDto,
            dataTable: dataTable
        };
    
});