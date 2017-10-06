define(['jquery', './interface_source', './interface_cols', './interface_filter'], 
  function ($, sourceCont, colCont, filterCont) {
    

        var dataTable = eeda.dt({
          id: 'interface_table',
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
              { "data": "NAME"}, 
              { "data": "TYPE"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        $('#interface_table tbody').on('click', 'tr', function (event) {
            current_tr = this;
            $('#interface_table tbody tr').css('background-color','#fff');
            $(current_tr).css('background-color','#00BCD4');
            current_tr_index = dataTable.row( this ).index();
            var data = dataTable.row( this ).data();
            
            $('#interface_id').val(data.ID);
            $('#interface_name').val(data.NAME);
            $('#interface_type').val(data.TYPE).change();
            $('#dialog_height').val(data.HEIGHT);
            $('#dialog_width').val(data.WIDTH);
            $('#interface_filter_condition').val(data.FILTER_CONDITION);

            if(data.IS_DISTINCT == 'Y'){
              $('#is_distinct').prop('checked', true);
            }else{
              $('#is_distinct').prop('checked', false);
            }

            if(data.TYPE=='列表选择'){
              
              var block_list = data.SOURCE.BLOCK_LIST;
              var box = $('#source_box').empty();
              if(block_list){
                for (var i = 0; i < block_list.length; i++) {
                    var item = block_list[i];
                    if(box.html().trim() == ''){
                      var form_box = '<div class="table_block" eeda_id="'+item.ID+'">'+item.FORM_NAME
                             +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                             +'</div>'
                      box.append(form_box);
                    }else{
                      var options = '        <option selected>交集</option>'
                                    +'        <option>左关联</option>';

                      if(item.JOIN_TYPE == '左关联'){
                          options = '        <option>交集</option>'
                                  +'        <option selected>左关联</option>';
                      }
                      var form_box = '<div class="table_block_right">'
                                    +'<div class="connect_line"></div>'
                                    +'<div class="connect_type" style="">  '
                                    +'    <select class="form-control operator" name="operator">'
                                    +options
                                    +'    </select> '
                                    +'</div>'
                                    +'<div class="connect_line"></div>'
                                    +'<div class="table_block" eeda_id="'+item.ID+'">'+item.FORM_NAME
                                    +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                                    +'</div>'
                                    +'</div>';
                      box.append(form_box);
                    }
                }
              }

              var join_list = data.SOURCE.JOIN_LIST;
              sourceCont.dataTable.clear().draw();
              if(join_list){
                for (var i = 0; i < join_list.length; i++) {
                    var item = join_list[i];
                    sourceCont.dataTable.row.add(item).draw(false);
                }
              }

              var col_list = data.COLS;
              colCont.dataTable.clear().draw();
              if(join_list){
                for (var i = 0; i < col_list.length; i++) {
                    var item = col_list[i];
                    colCont.dataTable.row.add(item).draw(false);
                }
              }

              var filter_list = data.FILTER;
              filterCont.dataTable.clear().draw();
              if(filter_list){
                for (var i = 0; i < filter_list.length; i++) {
                    var item = filter_list[i];
                    filterCont.dataTable.row.add(item).draw(false);
                }
              }
            }else if(data.FIELD_TYPE == '复选框'){
                //re_display_checkbox_values(data);
            }else if(data.FIELD_TYPE == '从表引用'){
                //re_display_detail_ref_values(data);
            }else if(data.FIELD_TYPE == '字段引用'){
                //re_display_ref_values(data);
            }
        } );

        var deleteList=[];
        $('#interface_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          if(id){
            deleteList.push({ID: id, is_delete:'Y'});
          }

          return false;
        });

        

        var buildDetail = function(){
            var data = dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var list = itemList.concat(deleteList);
            return list;
        };

        
        $('#addInterfaceBtn').click(function(){
            dataTable.row.add({}).draw(false);
            current_tr_index = dataTable.rows().data().length;
            current_tr = $('#interface_table tr:eq('+current_tr_index+')');
        });
        
        $('#field_type').change(function(event) {
          var checkText=$(this).find("option:selected").text();  //获取Select选择的Text
          $('.config').hide();
          if(checkText == '列表选择'){
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
        $('#interface_confirmFieldBtn').click(function(){
            var item = dataTable.rows(current_tr_index).data()[0];
            item.ID = $('#interface_id').val();
            item.NAME=$('#interface_name').val();
            item.TYPE=$('#interface_type').val();
            item.IS_DISTINCT=$('#is_distinct').prop('checked')==true?'Y':'N';
            item.HEIGHT=$('#dialog_height').val();
            item.WIDTH=$('#dialog_width').val();
            item.FILTER_CONDITION=$('#interface_filter_condition').val();

            var checkText=$('#interface_type').find("option:selected").text();
            if(checkText == '列表选择'){
              item.SOURCE = {};
              item.SOURCE=sourceCont.buildDetail();
              item.COLS=colCont.buildDetail();
              item.FILTER=filterCont.buildDetail();
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
            console.log(item);
            dataTable.row(current_tr).data( item ).draw();
        });

        var clear = function() {
          $('#interface_id').val('');
          $('#interface_name').val('');
          $('#interface_type').val('').change();
          $('#dialog_height').val('');
          $('#dialog_width').val('');
          $('#interface_filter_condition').val('');

          $('#is_distinct').prop('checked', true);
          $('#source_box').empty();

          sourceCont.clear(); 
          colCont.clear(); 
          filterCont.clear(); 
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            dataTable: dataTable
        };
    
});