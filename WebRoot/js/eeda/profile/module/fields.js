define(['jquery', './fields/field_pro_check_box', './fields/field_pro_detail_ref', './fields/field_pro_ref'], 
  function ($, checkboxCont, detailTableCont, refCont) {
    

        var dataTable = eeda.dt({
          id: 'fields_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn btn-xs delete_field" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px"},
              { "data": "FIELD_DISPLAY_NAME"}, 
              { "data": "FIELD_TYPE"}, 
              { "data": "SORT_TYPE", "width": "30px"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#fields_table tbody').on('click', 'tr', function (event) {
            current_tr = this;
            $('#fields_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = dataTable.row( this ).index();
            var data = dataTable.row( this ).data();
            
            $('#field_display_name').val(data.FIELD_DISPLAY_NAME);
            $('#field_type').val(data.FIELD_TYPE).change();
            $('#sort_type').val(data.SORT_TYPE);
            $('#default_value').val(data.DEFAULT_VALUE);
            $('#seq').val(data.SEQ);
            if(data.LISTED == 'Y'){
              $('#is_not_list_col').prop('checked', false);
            }else{
              $('#is_not_list_col').prop('checked', true);
            }

            if(!data.FIELD_TYPE){
              //清空所有属性
              $('#check_box_id').val('');
              checkboxCont.dataTable.clear().draw();
            }else if(data.FIELD_TYPE == '复选框'){
                re_display_checkbox_values(data);
            }else if(data.FIELD_TYPE == '从表引用'){
                re_display_detail_ref_values(data);
            }else if(data.FIELD_TYPE == '字段引用'){
                re_display_ref_values(data);
            }
        } );

        var deleteList=[];
        $('#fields_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });

        var re_display_ref_values = function(data) {
          var ref = data.REF;
          $('#ref_id').val(ref.ID);
          $('#ref_form').val(ref.REF_FORM);
          $('#ref_field').val(ref.REF_FIELD);
          $('#ref_condition').val(ref.REF_CONDITION);

          if(ref.IS_DROPDOWN == 'Y'){
            $('#is_dropdown').prop('checked', true);
          }else{
            $('#is_dropdown').prop('checked', false);
          }
        }

        var re_display_detail_ref_values = function(data) {
          var detail_ref = data.DETAIL_REF;
          $('#detail_ref_id').val(detail_ref.ID);
          $('#detail_ref_form').val(detail_ref.TARGET_FORM_NAME);

          var condition_list = detail_ref.JOIN_CONDITION;
          detailTableCont.connect_dataTable.clear().draw();
          if(condition_list){
            for (var i = 0; i < condition_list.length; i++) {
                var item = condition_list[i];
                detailTableCont.connect_dataTable.row.add(item).draw(false);
            }
          }

          var display_list = detail_ref.DISPLAY_FIELD;
          detailTableCont.display_dataTable.clear().draw();
          if(display_list){
            for (var i = 0; i < display_list.length; i++) {
                var item = display_list[i];
                detailTableCont.display_dataTable.row.add(item).draw(false);
            }
          }
        }

        var re_display_checkbox_values = function(data) {
          var check_box = data.CHECK_BOX;
          $('#check_box_id').val(data.CHECK_BOX.ID);

          if(data.CHECK_BOX.IS_SINGLE_CHECK == 'Y'){
            $('#is_single_check').prop('checked', true);
          }else{
            $('#is_single_check').prop('checked', false);
          }
          if(data.CHECK_BOX.IS_SINGLE_CHECK == 'Y'){
            $('#is_single_check').prop('checked', true);
          }else{
            $('#is_single_check').prop('checked', false);
          }
          $('#line_display_numbers').val(data.CHECK_BOX.LINE_DISPLAY_NUM);

          var itemList = data.CHECK_BOX.ITEM_LIST;
          checkboxCont.dataTable.clear().draw();
          if(itemList){
            for (var i = 0; i < itemList.length; i++) {
                var item = itemList[i];
                checkboxCont.dataTable.row.add(item).draw(false);
            }
          }
        }

        var buildFieldsDetail = function(){
            var data = dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var list = itemList.concat(deleteList);
            return list;
        };

        
        $('#addFieldBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#fields_table tr:eq('+current_tr_index+')');
        });
        
        $('#field_type').change(function(event) {
          var checkText=$(this).find("option:selected").text();  //获取Select选择的Text
          $('.config').hide();
          if(checkText == '文本'){
            $('#text_config').show();
          }else if (checkText == '复选框') {
            checkboxCont.dataTable.clear().draw();
            $('.check_box_config').show();
          }else if (checkText == '从表引用') {
            checkboxCont.dataTable.clear().draw();
            $('.detail_table_re_config').show();
          }else if (checkText == '字段引用') {
            //checkboxCont.dataTable.clear().draw();
            $('.ref_config').show();
          }
        });



        //回写到table
        $('#field_tab_confirmFieldBtn').click(function(){
            var item = dataTable.rows(current_tr_index).data()[0];
            item.FIELD_DISPLAY_NAME=$('#field_display_name').val();
            item.FIELD_TYPE=$('#field_type').val();
            item.SORT_TYPE=$('#sort_type').val();
            item.DEFAULT_VALUE=$('#default_value').val();
            item.SEQ=$('#seq').val();
            item.READ_ONLY=$('#read_only').prop('checked')==true?'Y':'N';
            item.LISTED=$('#is_not_list_col').prop('checked')==true?'N':'Y';
            

            var checkText=$('#field_type').find("option:selected").text();
            if(checkText == '文本'){
              
            }else if (checkText == '复选框') {
              var check_dto = checkboxCont.buildDto();
              item.CHECK_BOX = check_dto;
            }else if (checkText == '从表引用') {
              var dto = detailTableCont.buildDto();
              item.DETAIL_REF = dto;
            }else if (checkText == '字段引用') {
              var dto = refCont.buildDto();
              item.REF = dto;
            }

            dataTable.row(current_tr).data( item ).draw();
        });


        return {
            buildFieldsDetail: buildFieldsDetail,
            dataTable: dataTable
        };
    
});