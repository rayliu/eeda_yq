define(['jquery'], 
  function ($) {
        var dataTable = eeda.dt({
          id: 'custom_data_source_table',
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
              { "data": "OPERATOR", "width":"70px",
                "render": function ( data, type, full, meta ) {
                    return '<select class="form-control operator" name="operator" style="wdith:70px;">'
                    	  +'<option value="" selected=""></option>'
                          +'<option value="innder join" selected="">交集</option>'
                          +'<option value="left join" selected="">左关联</option></select>';
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

//        $('#add_custom_search_source_btn').click(function(event) {
//          var box = $('#custom_search_source_box');
//
//          if(box.html().trim() == ''){
//            var form_box = '<div class="table_block" eeda_id="">'+$('#custom_form_name').val()
//                   +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
//                   +'</div>'
//            box.append(form_box);
//          }else{
//            var form_box = '<div class="table_block_right">'
//                          +'<div class="connect_line"></div>'
//                          +'<div class="connect_type" style="">  '
//                          +'    <select class="form-control operator" name="operator">'
//                          +'        <option value="jiaoji">交集</option>'
//                          +'        <option value="zuoguanlian">左关联</option>'
//                          +'    </select> '
//                          +'</div>'
//                          +'<div class="connect_line"></div>'
//                          +'<div class="table_block" eeda_id="">'+$('#custom_form_name').val()
//                          +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
//                          +'</div>'
//                          +'</div>';
//            box.append(form_box);
//          }
//        });

//        var delete_block_list=[];
//        $('#custom_search_source_box').on('click', 'a', function(event) {
//            var block = $(this).closest('.table_block_right');
//            var id=$(this).parent().attr('eeda_id');
//            if(id != ''){
//              delete_block_list.push({ID: id.toString(), IS_DELETE:'Y'});
//            }
//
//            if(block.is('.table_block_right')){
//              block.remove();
//            }else{
//              lock = $(this).closest('.table_block').remove();
//            }
//
//         });


        $('#add_source_condition_btn').click(function(event) {
            dataTable.row.add({}).draw();
        });

        var deleteList=[];
        $('#custom_data_source_table tbody').on('click', 'button', function () {
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
//            var block_arr=[];
//            var blocks = $('#custom_search_source_box .table_block');
//            $.each(blocks, function(index, item) {
//              var block = $(item);
//
//              if(index==0){
//                var obj={
//                  ID: block.attr('eeda_id'),
//                  SEQ: index,
//                  FORM_NAME: block.text().trim()
//                };
//                block_arr.push(obj);
//              }else{
//                var obj={
//                  ID: block.attr('eeda_id'),
//                  SEQ: index,
//                  FORM_NAME: block.text().trim(),
//                  CONNECT_TYPE: block.parent().find('.operator').val()
//                };
//                block_arr.push(obj);
//              }
//            });
//
//            var new_block_arr = block_arr.concat(delete_block_list);

            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/6; i++) {
              var item={
                ID: $(inputs[i*6]).val(),
                FORM_LEFT: $(inputs[i*6 + 1]).val(),
                FORM_LEFT_FIELD: $(inputs[i*6 + 2]).val(),
                OPERATOR: $(inputs[i*6 + 3]).val(),
                FORM_RIGHT: $(inputs[i*6 + 4]).val(),
                FORM_RIGHT_FIELD: $(inputs[i*6 + 5]).val()
               
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteList);

            return {
              //block_arr: new_block_arr,
              join_list: list
            };
        };

        var clear = function() {
          $('#custom_search_source_box').children().remove();
          dataTable.clear().draw();
        }
        
//        var display = function(custom_search_source){
//        	var box = $('#custom_search_source_box');
//        	for(var i = 0;i<custom_search_source.length;i++){
//        		if(custom_search_source[i].CONNECT_TYPE==""||custom_search_source[i].CONNECT_TYPE==null){
//    	            var form_box = '<div class="table_block" eeda_id="'+custom_search_source[i].ID+'">'+custom_search_source[i].FORM_NAME
//    	                   +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
//    	                   +'</div>'
//    	            box.append(form_box);
//        		}else{
//        			var selected_jiao = "";
//        			var selected_zuo = "";
//        			if(custom_search_source[i].connect_type=="jiaoji"){
//        				selected_jiao = "selected";
//        			}else if(custom_search_source[i].connect_type=="zuoguanlian"){
//        				selected_zuo = "selected";
//        			}
//        			var form_box = '<div class="table_block_right">'
//                        +'<div class="connect_line"></div>'
//                        +'<div class="connect_type" style="">  '
//                        +'    <select class="form-control operator" name="operator">'
//                        +'        <option value="jiaoji" selected="'+selected_jiao+'">交集</option>'
//                        +'        <option value="zuoguanlian" selected="'+selected_zuo+'">左关联</option>'
//                        +'    </select> '
//                        +'</div>'
//                        +'<div class="connect_line"></div>'
//                        +'<div class="table_block" eeda_id="'+custom_search_source[i].ID+'">'+custom_search_source[i].FORM_NAME
//                        +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
//                        +'</div>'
//                        +'</div>';
//        			box.append(form_box);
//        		}
//        	}
//        }

        var tableDisplay = function(custom_search_source_condition){
             for (var i = 0; i < custom_search_source_condition.length; i++) {
                 var field = custom_search_source_condition[i];
                 dataTable.row.add(field).draw(false);
             }
        }
        
        return {
            buildDetail: buildDetail,
            dataTable: dataTable,
            clear: clear,
            tableDisplay:tableDisplay
        };
    

});