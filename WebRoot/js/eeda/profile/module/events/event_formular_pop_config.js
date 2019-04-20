define(['jquery','./table/config_table_add_row','./form/config_form_set_value',
 './element/config_element_set_enable','./element/config_element_set_show_hide',
 './element/config_element_set_text','./element/config_element_set_checkbox'
 ,'./element/config_element_set_droplist','./element/config_element_set_radio'
 ,'./element/config_element_set_focus'], 
    function ($, config_table_add_row_cont, config_form_set_value_cont, 
        config_element_set_enable_cont, config_element_set_show_hide_cont,
        config_element_set_text_cont, config_element_set_checkbox_cont
        ,config_element_set_droplist_cont, config_element_set_radio_cont
        , config_element_set_focus_cont) {

    var actionTreeObj, actionTreeObjNode;
    var setActionTreeObjNode =function(node, tree){
        actionTreeObjNode=node;
        actionTreeObj=tree;
        
        switch (node.action_type) {
            case 'open_link':
                redisplay(node);
                break;
            case 'element_set_enable':
                config_element_set_enable_cont.loadFields();
                config_element_set_enable_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_show_hide':
                config_element_set_show_hide_cont.loadFields();
                config_element_set_show_hide_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_text':
                config_element_set_text_cont.loadFields();
                config_element_set_text_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_radio':
                config_element_set_radio_cont.loadFields();
                config_element_set_radio_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_checkbox':
                config_element_set_checkbox_cont.loadFields();
                config_element_set_checkbox_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_droplist':
                config_element_set_droplist_cont.loadFields();
                config_element_set_droplist_cont.setActionTreeObjNode(node, tree);
                break;
            case 'element_set_focus':
                config_element_set_focus_cont.loadFields();
                config_element_set_focus_cont.setActionTreeObjNode(node, tree);
                break;
            case 'form_set_value':
                config_form_set_value_cont.setActionTreeObjNode(node, tree);
                break;
            case 'table_add_row':
                config_table_add_row_cont.setActionTreeObjNode(node, tree);
                break;
            default:
                break;
        }
    }

    var redisplay= function(node){
        if(!node.event_action_setting) return;
        var radio_open_link_value=node.event_action_setting.radio_open_link
        $("input[type=radio][name=radio_open_link][value="+radio_open_link_value+"]").prop('checked',true);
        $('input[name=radio_open_link]:checked').val();
        $('#open_form_type').val(node.event_action_setting.open_form_type);
        $('#open_link_type').val(node.event_action_setting.open_link_type);
        var module_id = node.event_action_setting.module_id;
        var select_node= eventModuleTreeObj.getNodeByParam("id", module_id, null);
        if(select_node){
            eventModuleTreeObj.selectNode(select_node);
        }
    }
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

    function onNodeClick(event, treeId, treeNode){
        if (treeNode.level==0 ) return;
        console.log('组织动作 clicked: '+treeNode.name+', action_type: '+treeNode.action_type);
        //动作配置框显示对应的项目
        changeActionTreeNode();
    }

    function changeActionTreeNode(){
        var radio_open_link = $('input[name=radio_open_link]:checked').val();
        var open_form_type = $('#open_form_type').val();
        var open_link_type = $('#open_link_type').val();
        var open_link_type_name = "当前窗口";
        var target_name = "链接";
        var page_name = "";
        if(open_form_type=='list'){
            page_name = "列表页";
        }else{
            page_name = "编辑页";
        }
        if(open_link_type=='new'){
            open_link_type_name = "新窗口/新标签";
            switch (radio_open_link) {
                case 'reload':
                    $("input[type=radio][name=radio_open_link][value=open_form]").prop('checked',true);
                    break;
                case 'go_back':
                    $("input[type=radio][name=radio_open_link][value=open_form]").prop('checked',true);
                    break;
            }
        }
        var module_id, url;
        switch (radio_open_link) {
            case 'open_form':
                var eventModuleTreeObj = $.fn.zTree.getZTreeObj("eventModuleTree");
                var nodes = eventModuleTreeObj.getSelectedNodes("actionTree_1");
                if(nodes.length>0){
                    var node = nodes[0];
                    target_name = node.name;
                    module_id = node.id;
                }
                break;
            case 'open_url':
                target_name = $('#event_link_url').val();
                url=target_name;
                page_name = "";
                break;
            case 'reload':
                page_name = "";
                target_name = "重新加载";
                if(open_link_type=='new'){
                    target_name = "链接";
                }
                break;
            case 'go_back':
                page_name = "";
                target_name = "返回上一页";
                if(open_link_type=='new'){
                    target_name = "链接";
                }
                break;
            default:
                break;
        }
        var event_action_setting={
            radio_open_link:radio_open_link,
            open_link_type:open_link_type,
            open_form_type:open_form_type,//打开list还是edit
            module_id:module_id,
            url:url
        };
        var node_name = "在 "+open_link_type_name+" 打开 "+target_name+" "+page_name;
        actionTreeObjNode.name=node_name;
        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

    var menuNodes=[];
    var eventModuleTreeObj;
    var loadMenu = function(){
        $.post('/role/getMenuList', function(result){
            if(!result)
                return;
    
            var menuList = result.data;
            for(var i=0;i<menuList.length;i++){
                var preMenu = i>0?menuList[i-1]:null;
                var menu = menuList[i];
                var parent_node;
                if(parent_node && menu.PARENT_ID==parent_node.id){
                    var node = {
                        id:menu.ID,
                        name: menu.MODULE_NAME,
                        parent_id: menu.PARENT_ID
                    };
                    parent_node.children.push(node);
                }else{
                    var node = {
                        id:menu.ID,
                        name: menu.MODULE_NAME,
                        parent_id: menu.PARENT_ID,
                        isParent:true, 
                        children: []
                    };
                    menuNodes.push(node);
                    parent_node = node;
                }
            }
            eventModuleTreeObj = $.fn.zTree.init($("#eventModuleTree"), setting, menuNodes);
            eventModuleTreeObj.expandAll(true);
        });
    };
    loadMenu();

    $('#event_search_nodes').keyup(function(){
        var inputField = $(this);
        var val = inputField.val();
        $.fn.zTree.init($("#eventModuleTree"), setting, menuNodes);
        actionTreeObj.expandAll(true);
        var nodes= actionTreeObj.getNodesByParamFuzzy("name", val, null);
        if(nodes.length==0)
            return ;
        //将找到的nodelist节点更新至Ztree内
        if(val.length==0)
            nodes= menuNodes;
        $.fn.zTree.init($("#eventModuleTree"), setting, nodes);
        actionTreeObj.expandAll(true);
    });

    $('#event_link_url').keyup(function(){
        var inputField = $(this);
        var val = inputField.val();
        changeActionTreeNode();
    });

    $('#open_link_type').change(function(e){
        var val =  $(this).find(":selected").val();
        switch (val) {
            case 'current':
                $('.reload, .go_back').show();
                break;
            case 'new':
                $('.reload, .go_back').hide();
                break;
            default:
                break;
        }
        changeActionTreeNode();
    });

    $('#open_form_type').change(function(e){
        changeActionTreeNode();
    });

    $('input[name=radio_open_link]').change(function(){
        changeActionTreeNode();
    });

    function loadTargetTableName(){
        config_table_add_row_cont.loadTargetTableName();
    }
    return {
        loadMenu: loadMenu,
        setActionTreeObjNode: setActionTreeObjNode,
        loadTargetTableName:loadTargetTableName
    };
});