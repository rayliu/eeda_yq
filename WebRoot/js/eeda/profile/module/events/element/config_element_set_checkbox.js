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
            eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_checkbox_tree"), setting, nodes);
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
        var checkbox_val = treeNode.checkbox_val;
        $("#config_element_set_checkbox_input").val(checkbox_val);

        var item_list=treeNode.item_list;
        //var select=$('#config_element_set_droplist_set_value');
        zNodes=[];
        for (let index = 0; index < item_list.length; index++) {
            const item = item_list[index];
            var node={id:item.ID, pId:0, name:item.NAME};
            zNodes.push(node);
        }
        $.fn.zTree.init($("#ul_tree"), check_box_setting, zNodes);
    }

    function zTreeOnCheck(event, treeId, treeNode) {
        console.log(treeNode.tId + ", " + treeNode.name + "," + treeNode.checked);
        console.log(treeNode);
        eventModuleTreeObj.selectNode(treeNode);

        var item_list=treeNode.item_list;
        //var select=$('#config_element_set_droplist_set_value');
        zNodes=[];
        for (let index = 0; index < item_list.length; index++) {
            const item = item_list[index];
            var node={id:item.ID, pId:0, name:item.NAME};
            zNodes.push(node);
        }
        $.fn.zTree.init($("#ul_tree"), check_box_setting, zNodes);

        changeActionTreeNode();
    };

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
                if(field.CHECK_BOX.IS_SINGLE_CHECK=='Y')
                    continue;

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

        eventModuleTreeObj = $.fn.zTree.init($("#config_element_set_checkbox_tree"), setting, defaultNodes);
        eventModuleTreeObj.expandAll(true);
    };
    
    
    $('#config_element_set_checkbox_tree').on('click', 'label, .button.chk', function(){
        var tagName = $(this).prop("tagName");
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_checkbox_tree");
        
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
        var treeObj = $.fn.zTree.getZTreeObj("config_element_set_checkbox_tree");
        var nodes=treeObj.getNodes();

        var event_action_setting={
            tree_json:JSON.stringify(nodes)
        };

        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

    var check_box_setting = {
        view: {
            dblClickExpand: false,
            showLine: false,
            showIcon: false
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            beforeClick: beforeClick,
            onClick: onClick
        }
    };

    var zNodes =[
        // {id:1, pId:0, name:"北京"},
        // {id:2, pId:0, name:"天津"},
        // {id:3, pId:0, name:"上海"},
        // {id:6, pId:0, name:"重庆"}
     ];

    function beforeClick(treeId, treeNode) {
        var check = (treeNode && !treeNode.isParent);
        if (!check) alert("只能选择城市...");
        return check;
    }
    
    function onClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("ul_tree"),
        nodes = zTree.getSelectedNodes(),
        v = "";
        nodes.sort(function compare(a,b){return a.id-b.id;});
        for (var i=0, l=nodes.length; i<l; i++) {
            v += nodes[i].name + ",";
        }
        if (v.length > 0 ) v = v.substring(0, v.length-1);
        var cityObj = $("#config_element_set_checkbox_input");
        cityObj.val(v);

        var selected_node = eventModuleTreeObj.getSelectedNodes()[0];
        selected_node.checkbox_val=v;
        changeActionTreeNode();
    }

    function showMenu() {
        var cityObj = $("#config_element_set_checkbox_input");
        var cityOffset = cityObj.offset();
        var cityposition=cityObj.position();
        var modal_offset = $('#formular_edit_modal .modal-content').offset();
        console.log(cityposition);
        $("#config_element_set_checkbox_menu_content").css(
            {left:cityOffset.left-modal_offset.left + "px", top:cityOffset.top-8 + "px"}).slideDown("fast");

        $("body").bind("mousedown", onBodyDown);
    }
    function hideMenu() {
        $("#config_element_set_checkbox_menu_content").fadeOut("fast");
        $("body").unbind("mousedown", onBodyDown);
    }
    function onBodyDown(event) {
        if (!(event.target.id == "config_element_set_checkbox_edit_btn" ||
            event.target.id == "config_element_set_checkbox_menu_content" ||
            $(event.target).parents("#config_element_set_checkbox_menu_content").length>0)) {
            hideMenu();
        }
    }

    $('#config_element_set_checkbox_edit_btn').click(function(){
        showMenu();
        return false;
    });

    var ul_tree=$.fn.zTree.init($("#ul_tree"), check_box_setting, zNodes);

    return {
        loadFields:loadFields,
        setActionTreeObjNode:setActionTreeObjNode
    };
});