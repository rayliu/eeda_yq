define(['jquery', 'zTree', './events/edit/type_set_css', './events/edit/type_set_value'],
   function ($, tree, setCssCont, setValueCont) {
    
    //---------------tree handle
    var setting = {
        view: {
            addHoverDom: addHoverDom,
            removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        edit: {
            enable: false,
            editNameSelectAll: true,
            showRemoveBtn: false,
            //showRenameBtn: showRenameBtn,
            renameTitle: "编辑",
            removeTitle: "删除",
            drag:{
                isCopy: false,
                isMove: true
            }
        },
        async: {
            enable: true,
            type: 'get',
            url:"/module/searchFormBtnEvents",
            autoParam:["id","name", "formId", "level=lv"],
            dataFilter: dataFilter//处理返回来的JSON 变为 nodes
        },
        callback: {
        //     beforeRename: beforeRename,
        //     onRename: onRename,
        //     beforeDrop: beforeDrop,//判断禁止模块拖拽到模块下
        //     onDrop: onDrop,
             onClick: onNodeClick
        }
    };

    var currentNode;
    function onNodeClick(event, treeId, treeNode){
      if (treeNode.level==0 ) return;
      if (!treeNode.type && treeNode.level==1  ) return;

      currentNode = treeNode;
      $('#edit_event_id').val(currentNode.ID || '');
      $('#edit_event_name').val(currentNode.name);
      $('#edit_event_type').val(currentNode.type).change();

      if(currentNode.SET_VALUE){
        $('#edit_event_value_id').val(currentNode.SET_VALUE.ID||'');
        $('#edit_event_set_value_condition').val(currentNode.SET_VALUE.CONDITION);

        var itemList = currentNode.SET_VALUE.SET_FIELD_LIST;
        setValueCont.dataTable.clear().draw();
        if(itemList){
          for (var i = 0; i < itemList.length; i++) {
              var item = itemList[i];
              setValueCont.dataTable.row.add(item).draw(false);
          }
        }
      }else if(currentNode.SET_CSS){
        $('#edit_event_css_id').val(currentNode.SET_CSS.ID||'');
        $('#edit_event_set_css_condition').val(currentNode.SET_CSS.CONDITION);
        $('#edit_set_css_target_field').val(currentNode.SET_CSS.TARGET_FIELD);

        var itemList = currentNode.SET_CSS.SET_FIELD_LIST;
        setCssCont.dataTable.clear().draw();
        if(itemList){
          for (var i = 0; i < itemList.length; i++) {
              var item = itemList[i];
              setCssCont.dataTable.row.add(item).draw(false);
          }
        }
      }else if(currentNode.OPEN_FORM){
        $('#event_open_condition').val(currentNode.OPEN_FORM.CONDITION);
        $('#event_open_module').val(currentNode.OPEN_FORM.MODULE_NAME);
        $('#event_open_type').val(currentNode.OPEN_FORM.OPEN_TYPE);
      }
      
    }
    
    var newCount=1;
    function addHoverDom(treeId, treeNode) {
            var sObj = $("#" + treeNode.tId + "_span");
            //如果是单据则不能在其下级添加节点
            if (treeNode.level==2 || treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            if (treeNode.level==1 && treeNode.MENU_TYPE=='value_change') return;
            if (treeNode.level==0 && treeNode.type=='default_event_root') return;

            var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
                + "' title='添加' onfocus='this.blur();'></span>";
            sObj.after(addStr);
            var btn = $("#addBtn_"+treeNode.tId);
            if (btn) btn.bind("click", function(){
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("editEventTree");
                var nodeName = "新事件" + (newCount++);
                if(treeNode.name == '值变化'){
                  var newNodes = zTree.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, menu_type:'value_change'});
                  currentNode=newNodes[0];
                  $('#edit_event_name').val(currentNode.name);
                  $('#edit_event_type').val(currentNode.type);
                  $('#edit_event_field').val(currentNode.field);
                }else if(treeNode.type == 'default_event_add_after_open' 
                  || treeNode.type == 'default_event_edit_after_open' 
                  || treeNode.type == 'default_event_save_before' 
                  || treeNode.type == 'default_event_save_after' ){
                    var newNodes = zTree.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, menu_type: treeNode.type});
                    currentNode=newNodes[0];
                    $('#edit_event_name').val(currentNode.name);
                    $('#edit_event_type').val(currentNode.type);
                }else{
                  var newNodes = zTree.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, menu_type:'btn'});
                  currentNode=newNodes[0];
                  $('#edit_event_name').val(currentNode.name);
                  $('#edit_event_type').val(currentNode.type);
                }

                zTree.selectNode(currentNode);
                return false;
            });
        };

    function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };

    function dataFilter(treeId, parentNode, childNodes) {
        if (!childNodes) return null;
        console.log(childNodes);
        for (var i=0, l=childNodes.length; i<l; i++) {
            var node = childNodes[i];
            childNodes[i].name = childNodes[i].NAME;
            childNodes[i].id = childNodes[i].ID;
            childNodes[i].parent_id = childNodes[i].PARENT_ID;
            childNodes[i].type = childNodes[i].TYPE;
            childNodes[i].isParent=false;

            if(node.MENU_TYPE=='value_change'){

            }else{
              
            }
        }
        return childNodes;
    };

    var displayBtnTree=function(){
      var btns = [];
      $.post('/module/searchFormBtns', {form_id: $('#form_id').val(), type:'edit'}, 
          function(data){
              if(data){
                for (var i=0; i<data.length; i++) {
                    var btn = data[i];
                    var node = {
                      id:btn.ID,
                      formId: btn.FORM_ID,
                      name: btn.NAME,
                      isParent:true
                    };
                    btns.push(node);
                }
                var zNodes = [
                   { name:"工具栏按钮", isParent:true, open: true, children: btns, formId:$('#form_id').val()},
                   { name:"值变化", isParent:true, formId:$('#form_id').val()},
                   { name:"表单默认事件", isParent:true, formId:$('#form_id').val(), type:'default_event_root',
                      children: [
                        { name:"新增-打开表单后", isParent:true, formId:$('#form_id').val(), type:'default_event_add_after_open'},
                        { name:"修改-打开表单后", isParent:true, formId:$('#form_id').val(), type:'default_event_edit_after_open'},
                        { name:"保存表单前", isParent:true, formId:$('#form_id').val(), type:'default_event_save_before'},
                        { name:"保存表单后", isParent:true, formId:$('#form_id').val(), type:'default_event_save_after'}
                      ]
                   }
                ];

                zTreeObj = $.fn.zTree.init($("#editEventTree"), setting, zNodes);
              }
          },
          'json');

      
    }
    
    var zTreeObj; 

    $('#editEventConfirmBtn').click(function(event) {
         var type=$('#edit_event_type').val();
         currentNode.name = $('#edit_event_name').val();
         currentNode.type = type;
         if(type=='open'){
            currentNode.openForm={
              condition : $('#edit_event_open_condition').val(),
              module_name : $('#edit_event_open_module').val(),
              open_type : $('#edit_event_open_type').val()
            }
          }else if(type=='set_css'){
            var set_css_dto = setCssCont.buildDto();

            var event_name = $('#edit_event_name').val();
            var operation_type=$('#edit_event_type').val();

            currentNode.EVENT_NAME= event_name;
            currentNode.SET_CSS = set_css_dto;
         }else if(type=='set_value'){
            var dto = setValueCont.buildDto();

            currentNode.EVENT_NAME= event_name;
            currentNode.SET_VALUE = dto;
         }
         zTreeObj.updateNode(currentNode);
    });

    var buildTreeNodes=function(){
      var node_list=[];
      var toolBarNodes = zTreeObj.getNodes()[0].children;
      for(var i = 0;i<toolBarNodes.length;i++){
        var eventNodes = toolBarNodes[i].children;

        if(!eventNodes) 
          continue;

        for(var j = 0;j<eventNodes.length;j++){
          var node=eventNodes[j];
          node_list.push(node);
        }
      }
      return node_list;
    }

    $('#edit_event_type').change(function(event) {
       hideAllDiv();
       var type = $(this).val();
       if(type == 'open'){
          $('#edit_open_form_div').show();
       }else if(type == 'set_css'){
          $('#edit_set_css_div').show();
       }else if(type == 'set_value'){
          $('#edit_set_value_div').show();
       }
    });

    var hideAllDiv = function(){
      $('#edit_open_form_div').hide();
      $('#edit_set_css_div').hide();
    }

    return {
        zTreeObj: zTreeObj,
        displayBtnTree: displayBtnTree,
        buildTreeNodes: buildTreeNodes
          // list_dataTable: list_dataTable,
          // edit_dataTable: edit_dataTable
    };
    


});