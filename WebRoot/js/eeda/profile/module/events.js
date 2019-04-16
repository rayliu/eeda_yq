define(['jquery', 'zTree', './events/formular_open_form', './edit_events']
  , function ($,zTree,openFormCont,editEventsCont) {
    
    //---------------tree handle
    var setting = {
        data: {
          keep: {
            parent: true//节点父节点属性锁，是否始终保持 isParent = true
          }
        },
        view: {
            addHoverDom: addHoverDom,
            removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        edit: {
            enable: true,
            editNameSelectAll: true,
            showRemoveBtn: setRemoveBtn,
            showRenameBtn: false,
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
            autoParam:["id", "formId", "level=lv"],
            dataFilter: dataFilter//处理返回来的JSON 变为 nodes
        },
        callback: {
        //     beforeRename: beforeRename,
        //    onRename: onRename,
        //     beforeDrop: beforeDrop,//判断禁止模块拖拽到模块下
        //     onDrop: onDrop,
            onClick: onNodeClick,
            beforeRemove:beforeRemove
        }
    };

    function setRenameBtn(treeId, treeNode) {
      return !treeNode.isParent;
    }

    function setRemoveBtn(treeId, treeNode) {
      return !treeNode.isParent;
    }

    var delete_node_ids = [];
    //点击节点删除的处理
    function beforeRemove(treeId, treeNode) {
      update_flag = "Y";
      delete_node_ids.push(treeNode.id);
    };

    var currentNode;
    function onNodeClick(event, treeId, treeNode){
      if (treeNode.level==0 || treeNode.level==1 ) return;

      currentNode = treeNode;
      $('#list_event_name').val(currentNode.name);
      $('#list_event_type').val(currentNode.type);

      $('#list_event_action').val(currentNode.EVENT_ACTION);
      $('#list_event_action_json').val(currentNode.EVENT_JSON);
      var event_action_nodes = JSON.parse( currentNode.EVENT_JSON );
      //redisplay tree
      var action_setting = {
          view: {
              selectedMulti: false
          },
          edit: {
              enable: false
          },
          callback: {
              //onClick: onNodeClick,
              //onDblClick: onNodeDblClick
          }
      };
      $.fn.zTree.init($("#list_event_tree"), action_setting, event_action_nodes);

      if(currentNode.OPEN_FORM){
        $('#list_event_open_condition').val(currentNode.OPEN_FORM.CONDITION);
        $('#list_event_open_module').val(currentNode.OPEN_FORM.MODULE_NAME);
        $('#list_event_open_type').val(currentNode.OPEN_FORM.OPEN_TYPE);
      }
      hide_div(currentNode.TYPE);
    }
    
    var hide_div = function(type){
    	$("#list_list_add_row_div").hide();
    	$("#list_open_form_div").hide();
    	$("#list_set_css_div").hide();
    	$("#list_set_value_div").hide();
    	$("#list_save_set_value_div").hide();
    	$("#list_check_form_div").hide();
    	$("#list_assign_form_div").hide();
    	if(type=="save"){
    		$("#list_save_set_value_div").show();
    	}else if(type=="open"){
    		$("#list_open_form_div").show();
    	}else if(type=="list_add_row"){
    		$("#list_list_add_row_div").show();
    	}else if(type=="set_value"){
    		$("#list_set_value_div").show();
    	}else if(type=="set_css"){
    		$("#list_set_css_div").show();
    	}else if(type=="check"){
    		$("#list_check_form_div").show();
    	}
    }
    
    $('#list_event_type').change(function(event) {
        var type = $(this).val();
        hide_div(type);
     });
    
    var newCount=1;
    function addHoverDom(treeId, treeNode) {
            var sObj = $("#" + treeNode.tId + "_span");
            //如果是1级则不能在其下级添加节点
            if (treeNode.level==0 || treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            //如果是叶子则不能在其下级添加节点
            if (!treeNode.isParent) return;

            var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
                + "' title='添加' onfocus='this.blur();'></span>";
            sObj.after(addStr);
            var btn = $("#addBtn_"+treeNode.tId);
            if (btn) btn.bind("click", function(){
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("listEventTree");
                var nodeName = "新事件" + (newCount++);
                //id:data.ID, id: data.ID, 
                var newNodes = zTree.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, type:'open'});
                currentNode=newNodes[0];
                $('#list_event_name').val(currentNode.name);
                $('#list_event_type').val(currentNode.type);

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
            childNodes[i].name = childNodes[i].NAME;
            childNodes[i].id = childNodes[i].ID;
            childNodes[i].parent_id = childNodes[i].PARENT_ID;
            childNodes[i].type = childNodes[i].TYPE;
            childNodes[i].isParent=false;
        }
        return childNodes;
    };

    var displayBtnTree=function(){
      update_flag = "N";
      delete_node_ids = [];
      var btns = [];
      $.post('/module/searchFormBtns', {form_id: $('#form_id').val(), type:'list'}, 
          function(data){
              if(data && data.TOOL_BAR_BTNS){
                var tool_bar_btns = data.TOOL_BAR_BTNS;
                for (var i=0; i<tool_bar_btns.length; i++) {
                    var btn = tool_bar_btns[i];
                    var node = {
                      id:btn.ID,
                      formId: btn.FORM_ID,
                      name: btn.NAME,
                      isParent:true
                    };
                    btns.push(node);
                }
                var zNodes = [
                   { name:"工具栏按钮", isParent:true, open: true, children: btns}
                ];

                 zTreeObj = $.fn.zTree.init($("#listEventTree"), setting, zNodes);
              }
          },
          'json');
    }
    
    var zTreeObj; 
    
    var update_flag = "N";
    $('#listEventConfirmBtn').click(function(event) {
    	   update_flag = "Y";
         //var type=$('#list_event_type').val();
         var list_event_action=$('#list_event_action').val();
         var list_event_action_json= $('#list_event_action_json').val();
         currentNode.name = $('#list_event_name').val();
         currentNode.event_action = list_event_action;
         currentNode.event_action_json=list_event_action_json;
         
         zTreeObj.updateNode(currentNode);
         $('#list_event_name').val('');
         $('#event_action_json').val('');
    });

    var buildTreeNodes=function(){
      var node_list=[];
      
      zTreeObj = $.fn.zTree.getZTreeObj("listEventTree");
      if(zTreeObj){
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
      }
      
      //edit tree nodes
      var zTree = $.fn.zTree.getZTreeObj("editEventTree");
      if(zTree){
        var toolBarBtnNodes = zTree.getNodes()[0].children;
        for(var i = 0;i<toolBarBtnNodes.length;i++){
          var eventNodes = toolBarBtnNodes[i].children;

          if(!eventNodes) 
            continue;

          for(var j = 0;j<eventNodes.length;j++){
            var node=eventNodes[j];
            node_list.push(node);
          }
        }
      }
      //页面按钮
      var page_btn_nodes = zTree.getNodes()[1].children;
      $.each(page_btn_nodes, function(index, item) {
        var nodes = item.children;
        if(nodes){
            $.each(nodes, function(index, node) {
              node_list.push(node);
          });
        }
      });
      //值改变
      if(zTreeObj){
        var value_change_nodes = zTree.getNodes()[2].children;
        if(value_change_nodes){
          for(var i = 0;i<value_change_nodes.length;i++){
            var node = value_change_nodes[i];

            if(!node) 
              continue;

            node_list.push(node);
          }
        }

        //默认事件
        var default_event_nodes = zTree.getNodes()[3].children;
        $.each(default_event_nodes, function(index, item) {
           var nodes = item.children;
           if(nodes){
              $.each(nodes, function(index, node) {
                node_list.push(node);
             });
           }
        });
      }
      delete_node_ids=delete_node_ids.concat(editEventsCont.deleteList);
      return {
        node_list: node_list,
        node_delete_ids: delete_node_ids
      };
    };

    var clear = function(){
      $('#list_event_name').val('');
      $('#list_event_type').val('');

      $('#event_open_condition').val('');
      $('#event_open_module').val('');
      $('#event_open_type').val('');
      
    }

    var listEvent_update_flag = function(){
    	return update_flag;
    }
    return {
        clear: clear,
        zTreeObj: zTreeObj,
        displayBtnTree: displayBtnTree,
        buildTreeNodes: buildTreeNodes,
        listEvent_update_flag:listEvent_update_flag
          // list_dataTable: list_dataTable,
          // edit_dataTable: edit_dataTable
    };
    

});