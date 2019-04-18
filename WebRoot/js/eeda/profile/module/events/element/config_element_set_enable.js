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
            eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_enable_tree"), setting, nodes);
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
            addDiyDom: addDiyDom,
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
            onCheck:zTreeOnCheck
            //onClick: onNodeClick
        }
    };

    function addDiyDom(treeId, treeNode) {
        if(treeNode.isParent) return;
        var aObj = $("#" + treeNode.tId + "_a");//获取当前节点对象
        if ($("#diyBtn_"+treeNode.id).length>0) return;
        var disable_checked="", enable_checked="";
        if(treeNode.radio_val&&treeNode.radio_val=='enable'){
            enable_checked="checked";
        }else{
            disable_checked="checked";
        }
        var selectStr = '<label style="margin-left:5px;"><input type="radio" class="sel" name="config_enable_field_'+treeNode.id+'" value="disable" '+disable_checked+'>禁用</label>'+
                ' <label><input type="radio" class="sel" name="config_enable_field_'+treeNode.id+'" value="enable" '+enable_checked+'>启用</label>';
        aObj.append(selectStr);
    };

    var query_btn_children=[], edit_btn_children=[], fields_children=[];
    var defaultNodes = [
        { name:"列表页按钮", 
         isParent:true, 
         nocheck:true,
         children: query_btn_children
        },
        { name:"编辑页按钮", 
         isParent:true, 
         nocheck:true,
         children: edit_btn_children
        },
        { name:"编辑页字段", 
         isParent:true, 
         nocheck:true,
         children: fields_children
        }
    ];

    var eventModuleTreeObj;
    var loadFields = function(){
        query_btn_children=[],edit_btn_children=[], fields_children=[];
        var module_obj = window.module_obj;
        for (var i = 0; i < module_obj.BTN_LIST_QUERY.length; i++) {
            var field = module_obj.BTN_LIST_QUERY[i];
            var node = {
                type:'btn',
                id: field.ID,
                name: field.NAME
            };
            query_btn_children.push(node);
            defaultNodes[0].children=query_btn_children;
        }
        for (var i = 0; i < module_obj.BTN_LIST_EDIT.length; i++) {
            var field = module_obj.BTN_LIST_EDIT[i];
            var node = {
                type:'btn',
                id: field.ID,
                name: field.NAME
            };
            edit_btn_children.push(node);
            defaultNodes[1].children=edit_btn_children;
        }
        for (var i = 0; i < module_obj.FORM_FIELDS.length; i++) {
            var field = module_obj.FORM_FIELDS[i];
            var node = {
                type:'field',
                id: field.ID,
                name: field.FIELD_DISPLAY_NAME
            };
            fields_children.push(node);
            defaultNodes[2].children=fields_children;
        }

        eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_enable_tree"), setting, defaultNodes);
        eventModuleTreeObj.expandAll(true);
    };
    
    function zTreeOnCheck(event, treeId, treeNode) {
        console.log(treeNode.tId + ", " + treeNode.name + "," + treeNode.checked);
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_enable_tree");
        var selected_node=treeNode;
        var input_name=$('#'+treeNode.tId).find('input').attr('name');
        var radio_val=$('input[name='+input_name+']:checked').val();//like config_enable_field_88
        selected_node.radio_val=radio_val;
        console.log(input_name+' radio_val:'+radio_val);
        treeObj.updateNode(selected_node);

        changeActionTreeNode();
    };

    function changeActionTreeNode(){
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_enable_tree");
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