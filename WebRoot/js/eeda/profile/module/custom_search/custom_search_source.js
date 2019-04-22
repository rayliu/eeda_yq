define(['jquery'], function ($) {
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
                return '<input name="form_left_field" style="width: 70px;" value="=">';
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

      //---------------tree handle
      var setting = {
        view: {
            //addHoverDom: addHoverDom,
            //removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        edit: {
            enable: false,
            editNameSelectAll: true,
            showRemoveBtn: false,
            showRenameBtn: false,
            renameTitle: "编辑",
            removeTitle: "删除",
            drag:{
                isCopy: false,
                isMove: true
            }
        },
        callback: {
            onClick: onNodeClick
        }
      };

      function onNodeClick(e, treeId, treeNode) {
          //if (treeNode.level==0 ) return; 
          addModule(treeNode);
          hideMenu();
      }

      var zNodes=[];
      var loadModuleTree = function(){
        $.post('/module/getMenuList', function(result){
            if(!result)
                return;
    
            var menuList = result;
            for(var i=0;i<menuList.length;i++){
                var preMenu = i>0?menuList[i-1]:null;
                var menu = menuList[i];
                var node = {
                    id:menu.ID,
                    name: menu.MODULE_NAME,
                    parent_id: menu.PARENT_ID,
                    isParent:true, 
                    children: []
                };

                if(menu.LVL2_LIST){
                  for (let index = 0; index < menu.LVL2_LIST.length; index++) {
                      const sub_menu = menu.LVL2_LIST[index];
                      var sub_node = {
                          id: sub_menu.ID,
                          name: sub_menu.MODULE_NAME,
                          parent_id: sub_menu.PARENT_ID
                      };
                      node.children.push(sub_node);
                  }
                }
                zNodes.push(node);
            }
            var eventModuleTreeObj = $.fn.zTree.init($("#custom_search_ul_tree"), setting, zNodes);
            eventModuleTreeObj.expandAll(true);
        });
    };
    loadModuleTree();

      function showMenu() {
          var targetObj = $("#custom_search_source_box");
          var targetOffset = targetObj.offset();
          var target_position=targetObj.position();
          //var modal_offset = $('#formular_edit_modal .modal-content').offset();
          console.log(target_position);
          $("#custom_search_menu_content").css(
              {left:targetOffset.left + "px", top:targetOffset.top-8 + "px"}).slideDown("fast");

          $("body").bind("mousedown", onBodyDown);
      }
      function hideMenu() {
          $("#custom_search_menu_content").fadeOut("fast");
          $("body").unbind("mousedown", onBodyDown);
      }
      function onBodyDown(event) {
          if (!(event.target.id == "add_custom_search_source_btn" ||
              event.target.id == "custom_search_menu_content" ||
              $(event.target).parents("#custom_search_menu_content").length>0)) {
              hideMenu();
          }
      }

      $('#add_custom_search_source_btn').click(function(event) {
          showMenu();
        });
          
      function addModule(node) {
          var tip=$('#custom_search_source_box_tip');
          if(tip) tip.remove();

          var box = $('#custom_search_source_box');

          if(box.html().trim() == ''){
            var form_box = '<div class="table_block" eeda_id="">'+node.name
                    +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                    +'</div>'
            box.append(form_box);
          }else{
           var form_box = '<div class="table_block_right">'
                         +'<div class="connect_line"></div>'
                         +'<div class="connect_type" style="">  '
                         +'    <select class="form-control operator" name="operator">'
                         +'        <option value="join">交集</option>'
                         +'        <option value="left_join">左关联</option>'
                         +'    </select> '
                         +'</div>'
                         +'<div class="connect_line"></div>'
                         +'<div class="table_block" eeda_id="">'+node.name
                         +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                         +'</div>'
                         +'</div>';
           box.append(form_box);
         }
       };

       var delete_block_list=[];
       $('#custom_search_source_box').on('click', 'a', function(event) {
           var block = $(this).closest('.table_block_right');
           var id=$(this).parent().attr('eeda_id');
           if(id != ''){
             delete_block_list.push({ID: id.toString(), IS_DELETE:'Y'});
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
           var block_arr=[];
           var blocks = $('#custom_search_source_box .table_block');
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
                 CONNECT_TYPE: block.parent().find('.operator').val()
               };
               block_arr.push(obj);
             }
           });

           var new_block_arr = block_arr.concat(delete_block_list);

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
              block_arr: new_block_arr,
              join_list: list
            };
        };

        var clear = function() {
          $('#custom_search_source_box').children().remove();
          // dataTable.clear().draw();
        }
        
       var display = function(custom_search_source){
       	var box = $('#custom_search_source_box');
       	for(var i = 0;i<custom_search_source.length;i++){
       		if(custom_search_source[i].CONNECT_TYPE==""||custom_search_source[i].CONNECT_TYPE==null){
   	            var form_box = '<div class="table_block" eeda_id="'+custom_search_source[i].ID+'">'+custom_search_source[i].FORM_NAME
   	                   +'<a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
   	                   +'</div>'
   	            box.append(form_box);
       		}else{
       			var selected_jiao = "";
       			var selected_zuo = "";
       			if(custom_search_source[i].connect_type=="join"){
       				selected_jiao = "selected";
       			}else if(custom_search_source[i].connect_type=="left_join"){
       				selected_zuo = "selected";
       			}
       			var form_box = '<div class="table_block_right">'
                       +'<div class="connect_line"></div>'
                       +'<div class="connect_type" style="">  '
                       +'    <select class="form-control operator" name="operator">'
                       +'        <option value="join" selected="'+selected_jiao+'">交集</option>'
                       +'        <option value="left_join" selected="'+selected_zuo+'">左关联</option>'
                       +'    </select> '
                       +'</div>'
                       +'<div class="connect_line"></div>'
                       +'<div class="table_block" eeda_id="'+custom_search_source[i].ID+'">'+custom_search_source[i].FORM_NAME
                       +'    <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                       +'</div>'
                       +'</div>';
       			box.append(form_box);
       		}
       	}
       }

        var tableDisplay = function(custom_search_source_condition){
             for (var i = 0; i < custom_search_source_condition.length; i++) {
                 var field = custom_search_source_condition[i];
                 dataTable.row.add(field).draw(false);
             }
        }
        
        return {
            buildDetail: buildDetail,
            // dataTable: dataTable,
            clear: clear,
            display:display,
            tableDisplay:tableDisplay
        };
    

});