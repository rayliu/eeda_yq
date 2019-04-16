define(['jquery', './event_formular_pop_config' ], function ($, configCont) {

        //---------------tree handle
        var setting = {
          view: {
              //addHoverDom: addHoverDom,
              removeHoverDom: removeHoverDom,
              selectedMulti: false
          },
          edit: {
              enable: true,
              editNameSelectAll: true,
              showRemoveBtn: showRemoveBtn,
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

        function showRemoveBtn(treeId, treeNode) {
            return !treeNode.isParent;
        };

        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };

        function onNodeClick(event, treeId, treeNode){
            if (treeNode.level==0 ) return;
            console.log('组织动作 clicked: '+treeNode.name+', action_type: '+treeNode.action_type);
            //动作配置框显示对应的项目
            $('.event_config').hide();
            switch (treeNode.action_type) {
                case 'open_link':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_open_link').show();
                    break;
                case 'close_window':
                    $('#event_config_close_window').show();
                    break;
                case 'element_set_enable':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_enable').show();
                    break;
                case 'element_set_show_hide':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_show_hide').show();
                    break;
                case 'element_set_text':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_text').show();
                    break;
                case 'element_set_checkbox':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_checkbox').show();
                    break;
                case 'element_set_droplist':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_droplist').show();
                    break;
                case 'element_set_focus':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_element_set_focus').show();
                    break;
                case 'form_set_value':
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_form_set_value').show();
                    break;
                case 'table_add_row':
                    configCont.loadTargetTableName();
                    configCont.setActionTreeObjNode(treeNode, actionTreeObj);
                    $('#event_config_table').show();
                    break;  
                case 'table_delete_row':
                    $('#event_config_table_delete_row').show();
                    break;    
                default:
                    break;
            }
        }

        var actionNodes = [
            { name:"条件(空)", 
                action_type: 'condition',
                isParent:true, 
                children: []
            }
        ]
        var actionTreeObj = $.fn.zTree.init($("#actionTree"), setting, actionNodes);

        function redisplayActionTree(nodes){
            $.fn.zTree.init($("#actionTree"), setting, nodes);
        }
        
        return {
            actionTreeObj : actionTreeObj,
            redisplayActionTree:redisplayActionTree
        };
    
});