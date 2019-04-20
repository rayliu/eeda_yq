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
            eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_radio_tree"), setting, nodes);
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
            onClick: onNodeClick,
            onCheck: zTreeOnCheck
        }
    };

    function onNodeClick(event, treeId, treeNode){
        if (treeNode.level==0 ) return;
    };

    function zTreeOnCheck(event, treeId, treeNode) {
        console.log(treeNode.tId + ", " + treeNode.name + "," + treeNode.checked);
        console.log(treeNode);
        eventModuleTreeObj.selectNode(treeNode);

        var item_list=treeNode.item_list;
        var select=$('#config_element_set_radio_set_value');
        select.empty();
        for (let index = 0; index < item_list.length; index++) {
            const element = item_list[index];
            var select_html='<option value="'+element.NAME+'">'+element.NAME+'</option>';
            select.append(select_html);
        }
        changeActionTreeNode();
    };

    $('#config_element_set_radio_set_value').change(function(){
        changeActionTreeNode();
    });

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
            
            let field_types = ['复选框'];
            if(field_types.includes(field.FIELD_TYPE)){
                var field_type = "checkbox多选";
                if(field.CHECK_BOX.IS_SINGLE_CHECK=='Y'){
                    field_type = "radio单选";
                }else{
                    continue;
                }  

                var node = {
                    type:'field',
                    id: field.ID,
                    name: field.FIELD_DISPLAY_NAME+'('+field_type+')',
                    item_list: field.CHECK_BOX.ITEM_LIST
                };

                fields_children.push(node);
            }
                
            defaultNodes[0].children=fields_children;
        }

        eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_radio_tree"), setting, defaultNodes);
        eventModuleTreeObj.expandAll(true);
    };

    function changeActionTreeNode(){

        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_radio_tree");
        if(treeObj.getSelectedNodes().length>0){
            var select_node = treeObj.getSelectedNodes()[0];
            var select_value= $('#config_element_set_radio_set_value').val();
            select_node.select_value = select_value
            treeObj.updateNode(select_node);
        }

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