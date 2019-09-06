define(['jquery'], function ($) {
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

          var field_li = '';
          var block;
          var leng_li = 0;
          var ch = $('#custom_search_source_box').children('.table_block');
          if(ch) {
            block = ch.first();
            leng_li = block.find("li").length;
          }

          $.post('/module/getFormFieldsWithDetail', {form_name:node.name}, function(data){
              if(data){
                var options = '';
                var fields_arr = data;
                for (let index = 0; index < fields_arr.length; index++) {
                  const el = fields_arr[index];
                  if(el.FIELD_TYPE!="从表引用"){
                    options+='<option>'+el.FIELD_DISPLAY_NAME+'</option>';
                  }else{
                    var detail_fields_arr = el.FIELD_LIST;
                    for (let i = 0; i < detail_fields_arr.length; i++) {
                      const d_el = detail_fields_arr[i];
                      options+='<option>'+d_el.TARGET_FIELD_NAME+'</option>';
                    }
                  }
                }
                var select_li = '<li class="form-inline">'
                        +'    <a href="javascript:;" class="delete_source_field"><span class="glyphicon glyphicon-remove"></span></a>'
                        +'    <select class="form-control operator" name="field">'
                        +  options
                        +'    </select> '
                        +'</li>';
                for(var i=0;i<leng_li;i++){
                  field_li += select_li;
                }

                form_box = "<div class='table_block' field_arr='"+ JSON.stringify(fields_arr) +"' eeda_id='' form_name='"+node.name+"'>"
                    +'    <div>'+node.name
                    +'      <a href="javascript:;" class="add_field" style=" "><span class="glyphicon glyphicon-plus"></span></a>'
                    +'      <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                    +'    </div>'
                    +'    <ul style="padding-top: 5px;">'+field_li+'</ul>'
                    +'</div>';
                box.append(form_box);
              }
          });
          
         
       };

        var delete_block_list=[];
        //删除数据源表
        $('#custom_search_source_box').on('click', 'a.delete_source', function(event) {
           var block = $(this).closest('.table_block');
           var id=block.attr('eeda_id');
           if(id){
              delete_block_list.push({ID: id.toString(), IS_DELETE:'Y'});
           }
           lock = $(this).closest('.table_block').remove();
           
        });

        //添加关联字段
        $('#custom_search_source_box').on('click', 'a.add_field', function(event) {
          $('#custom_search_source_box .table_block').each(function(i, el){
            var block = $(el).closest('.table_block');
            var field_arr = JSON.parse(block.attr('field_arr'));
            var ul = block.children("ul").first();

            var options = '';
            for (let index = 0; index < field_arr.length; index++) {
              const el = field_arr[index];
              if(el.FIELD_TYPE!="从表引用"){
                options+='<option>'+el.FIELD_DISPLAY_NAME+'</option>';
              }else{
                var detail_fields_arr = el.FIELD_LIST;
                for (let i = 0; i < detail_fields_arr.length; i++) {
                  const d_el = detail_fields_arr[i];
                  options+='<option>'+d_el.TARGET_FIELD_NAME+'</option>';
                }
              }
            }
            var select_li = '<li class="form-inline">'
                    +'    <a href="javascript:;" class="delete_source_field"><span class="glyphicon glyphicon-remove"></span></a>'
                    +'    <select class="form-control operator" name="field">'
                    +  options
                    +'    </select> '
                    +'</li>';
            ul.append(select_li);
          });
          
       });

       var deleteList=[];
       //删除数据源表内的关联字段
       $('#custom_search_source_box').on('click', 'a.delete_source_field', function(event) {
            var btn = $(this);
            var li =btn.closest('li');
            var li_index = li.index();//同层所有li都要删除
           

            $('#custom_search_source_box .table_block').each(function(i, el){
              var block = $(el).closest('.table_block');
              var li = block.find("ul li:eq("+li_index+")");
              li.remove();
            });
        });

        
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
               var li_arr = block.find('li');
               var fields = [];
               $.each(li_arr, function(i, el){
                  var obj={
                      form_name: block.attr('form_name')
                      ,field_name: $(el).find('select').val()
                      ,seq: i
                  }
                  fields.push(obj);
               });

               var obj={
                  ID: block.attr('eeda_id'),
                  SEQ: index,
                  FORM_NAME: block.attr('form_name')
                  ,field_list: fields
               };
               block_arr.push(obj);
             
           });

           var new_block_arr = block_arr.concat(delete_block_list);

            // var data = dataTable.rows().data();
            // var inputs = dataTable.$('input, select');
            // var itemList = [];
            // for (var i = 0; i < inputs.length/6; i++) {
            //   var item={
            //     ID: $(inputs[i*6]).val(),
            //     FORM_LEFT: $(inputs[i*6 + 1]).val(),
            //     FORM_LEFT_FIELD: $(inputs[i*6 + 2]).val(),
            //     OPERATOR: $(inputs[i*6 + 3]).val(),
            //     FORM_RIGHT: $(inputs[i*6 + 4]).val(),
            //     FORM_RIGHT_FIELD: $(inputs[i*6 + 5]).val()
               
            //   };

            //   itemList.push(item);
            // }

            var list = [];//itemList.concat(deleteList);

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
          $.each(custom_search_source, function(i, el){
            var source = el;
            var li_length = source.FIELD_LIST.length;
            $.post('/module/getFormFieldsWithDetail', {form_name: source.FORM_NAME}, function(data){
                if(!data) return;
                var options = '';
                var fields_arr = data;
                var block =box.find('.table_block[eeda_id='+source.ID+']');
                block.attr('field_arr', JSON.stringify(fields_arr));

                var ul = block.find('ul');

                for (let index = 0; index < fields_arr.length; index++) {
                  const el = fields_arr[index];
                  if(el.FIELD_TYPE!="从表引用"){
                    options+='<option>'+el.FIELD_DISPLAY_NAME+'</option>';
                  }else{
                    var detail_fields_arr = el.FIELD_LIST;
                    for (let i = 0; i < detail_fields_arr.length; i++) {
                      const d_el = detail_fields_arr[i];
                      options+='<option>'+d_el.TARGET_FIELD_NAME+'</option>';
                    }
                  }
                }
                var select_li = '';
                for (let index = 0; index < li_length; index++) {
                    var li='<li class="form-inline">'
                          +'    <a href="javascript:;" class="delete_source_field"><span class="glyphicon glyphicon-remove"></span></a>'
                          +'    <select class="form-control operator" name="field">'
                          +  options
                          +'    </select> '
                          +'</li>';
                    select_li+=li;
                }
                var form_box = "<div class='table_block' field_arr='"+JSON.stringify(fields_arr)+"' eeda_id='"+source.ID+"' form_name='"+source.FORM_NAME+"'>"
                          +'    <div>'+source.FORM_NAME
                          +'      <a href="javascript:;" class="add_field" style=" "><span class="glyphicon glyphicon-plus"></span></a>'
                          +'      <a href="javascript:;" class="delete_source"><span class="glyphicon glyphicon-remove"></span></a>'
                          +'    </div>'
                          +'    <ul style="padding-top: 5px;">'+select_li+'</ul>'
                          +'</div>';
                box.append(form_box);

                //set option selected
                for (let index = 0; index < li_length; index++) {
                    var item = source.FIELD_LIST[index];
                    box.find('.table_block[eeda_id='+source.ID+'] li:eq('+index+') select').val(item.FIELD_NAME);
                }
            });
            
          });//end of for
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