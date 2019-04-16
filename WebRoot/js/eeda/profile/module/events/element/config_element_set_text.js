define(['jquery'], function ($) {
    var actionTreeObj, actionTreeObjNode;
    var setActionTreeObjNode =function(node, tree){
        actionTreeObjNode=node;
        actionTreeObj=tree;
        redisplay(node);
    }

    var redisplay= function(node){
        var event_action_setting = node.event_action_setting;
        if(event_action_setting){
            var nodes=JSON.parse(event_action_setting.tree_json);
            eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_text_tree"), setting, nodes);
            eventModuleTreeObj.expandAll(true);
        }
    }

    //---------------tree handle
    var setting = {
        check: {
            enable: true
        },
        view: {
            //addHoverDom: addHoverDom,
            //removeHoverDom: removeHoverDom,
            // addDiyDom: addDiyDom,
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

    function onNodeClick(event, treeId, treeNode){
        if (treeNode.level==0 ) return;
        
    }

    var fields_children=[];
    var defaultNodes = [
        { 
            name:"编辑页字段", 
            isParent:true, 
            nocheck:true,
            children: fields_children
        }
    ];

    var eventModuleTreeObj;
    var loadFields = function(){
        fields_children=[];
        var module_obj = window.module_obj;
        
        for (var i = 0; i < module_obj.FORM_FIELDS.length; i++) {
            var field = module_obj.FORM_FIELDS[i];
            var node = {
                type:'field',
                id: field.ID,
                name: field.FIELD_DISPLAY_NAME+'('+field.FIELD_TYPE+')'
            };
            let field_types = ['文本', '多行文本', '日期', '日期时间', '网址', '网页HTML'];
            if(field_types.includes(field.FIELD_TYPE))
                fields_children.push(node);
            defaultNodes[0].children=fields_children;
        }

        eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_text_tree"), setting, defaultNodes);
        eventModuleTreeObj.expandAll(true);
    };
    
    
    $('#config_element_set_text_tree').on('click', 'label, .button.chk', function(){
        var tagName = $(this).prop("tagName");
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_text_tree");
        
        if("LABEL"==tagName){
            var tId = $(this).closest('li').attr('id');
            var selected_node=treeObj.getNodeByTId(tId);
            var input_name=$(this).find('input').attr('name');
            var radio_val=$('input[name='+input_name+']:checked').val();//like config_enable_field_88
            selected_node.radio_val=radio_val;
            console.log(input_name+' radio_val:'+radio_val);
            treeObj.updateNode(selected_node);
        }
        changeActionTreeNode();
    });

    function changeActionTreeNode(){
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_text_tree");
        var nodes=treeObj.getNodes();

        var event_action_setting={
            tree_json:JSON.stringify(nodes)
        };

        // var node_name = "在 "+target_table_name+" 添加 "+row_length+" 行";
        // actionTreeObjNode.name=node_name;
        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

    return {
        loadFields:loadFields,
        setActionTreeObjNode:setActionTreeObjNode
    };
});