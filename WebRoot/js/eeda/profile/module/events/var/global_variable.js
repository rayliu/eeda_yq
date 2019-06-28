define(['jquery'], function ($) {

    var actionTreeObj, actionTreeObjNode;
    var setActionTreeObjNode =function(node, tree){
        actionTreeObjNode=node;
        actionTreeObj=tree;
        redisplay(node);
    }

    $('#config_global_var_ul input[name=global_var_radio]').change(function(){
        var value = $(this).val();
        changeActionTreeNode();
    });

    $('#set_global_var_type').change(function(){
        var value = $(this).val();
        changeActionTreeNode();
    });

    function changeActionTreeNode(){
        var checkVarObj =$('#config_global_var_ul input[name=global_var_radio]:checked');

        var event_action_setting={
            checked_var: checkVarObj.val(),
            checked_var_option: $('#set_global_var_type').val()
        };
        console.log(event_action_setting);
        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

        var dataTable = eeda.dt({
          id: 'global_variable_table',
          paging: false,
          lengthChange: false,
          columns: [
              {  "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    var id='';
                    if(data){
                      id=data;
                    }
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'
                          +'<i class="fa fa-trash-o"></i> 删除</button>'
                          +'<input name="id" type="hidden" value="'+id+'">';
                  }
              },
              { "data": "NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<div class="form-group input-group">'
                           +'     <input type="text" class="form-control" name="name" value="'+data+'"">'
                           +'</div>';
                  }
              },
              { "data": "VALUE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                         data='';
                    return '<div class="form-group input-group" style="width: 100%;">'
                           +'     <input type="text" class="form-control" name="value" value="'+data+'">'
                           +'     <span class="input-group-btn">'
                           +'         <button class="btn btn-default formular_pop" target="value" type="button"><i class="fa fa-edit"></i>'
                           +'         </button>'
                           +'     </span>'
                           +' </div>';
                  }
              }
          ]
        });

        var deleteList=[];
        $('#global_variable_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();
          if(id==null){
        	  return;
          }
          deleteList.push({ID: id.toString(), action:'DELETE'});
          return false;
        });
        
        var buildDto = function(){
            var data = dataTable.rows().data();
            var inputs = dataTable.$('input, select');
            var itemList = [];
            for (var i = 0; i < inputs.length/3; i++) {
              var item={
                ID: $(inputs[i*3]).val(),
                NAME: $(inputs[i*3 + 1]).val(),
                VALUE: $(inputs[i*3 + 2]).val()
              };

              itemList.push(item);
            }
            var list = itemList.concat(deleteList);
            var dto ={
              FIELD_LIST: list
            }
            return dto;
        };

        $('#formular_global_variable_add_btn').click(function(){
          var item={};
          dataTable.row.add(item).draw(true);
        });

        $('#formular_global_variable_cancel_btn').click(function(){
          $('#formular_global_variable_modal').modal('hide');
        });

        $('#formular_global_variable_ok_btn').click(function(){
            var dto = buildDto();
            console.log(dto);
            $.post('/module/saveGlobalVar', {params: JSON.stringify(dto.FIELD_LIST)}, function(){
                refreshUl();
            });
            $('#formular_global_variable_modal').modal('hide');
        });

        //添加全局变量
        $('#add_global_var_btn').click(function(){
          deleteList=[];
          $.post('/module/getGlobalVar', function(data){
              if(data){
                dataTable.clear();
                data.forEach(element => {
                  var item={
                      ID: element.ID,
                      NAME: element.NAME,
                      VALUE: element.VALUE
                  }
                  dataTable.row.add(item).draw(true);
                });
              }
          });
          $('#formular_global_variable_modal').modal('show');
          $('#formular_global_variable_modal .modal-backdrop').css({"z-index":0});
        });

        var refreshUl=function(){
           //刷新全局变量列表
           $('#config_global_var_ul').empty();
           $.post('/module/getGlobalVar', function(data){
               if(data){
                 data.forEach(element => {
                   var li_html='<li style="display:flex;"><div class="radio">'+
                                 '<label>'+
                                   '<input type="radio" style="margin-top: 0px;" name="global_var_radio" value="'+element.NAME+'" checked>'+
                                   element.NAME+
                                 '</label>'+
                               '</div></li>';
                   $('#config_global_var_ul').append(li_html);
                 });
               }
           });
        }

        var redisplay = function(node){
            //刷新全局变量列表
            $('#config_global_var_ul').empty();
            $.post('/module/getGlobalVar', function(data){
                if(data){
                  data.forEach(element => {
                    var li_html='<li style="display:flex;"><div class="radio">'+
                                  '<label>'+
                                    '<input type="radio" style="margin-top: 0px;" name="global_var_radio" value="'+element.NAME+'" checked>'+
                                    element.NAME+
                                  '</label>'+
                                '</div></li>';
                    $('#config_global_var_ul').append(li_html);
                  });
                  //回显当前节点的值
                  if(node.event_action_setting){
                      var checked_var = node.event_action_setting.checked_var;
                      var checked_var_option = node.event_action_setting.checked_var_option;
                      $('#config_global_var_ul input[value='+checked_var+']').prop('checked', true);
                      $('#set_global_var_type').val(checked_var_option);
                  }
                  
                }
            });
            

        } 

        return {
            buildDto: buildDto,
            setActionTreeObjNode: setActionTreeObjNode
        };
    
});