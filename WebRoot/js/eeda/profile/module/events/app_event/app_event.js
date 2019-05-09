define(['jquery', 'zTree'], function ($) {
    var zTreeObj;
    var update_flag = "N";
    //---------------tree handle
    var app_event_setting = {
        data: {
            keep: {
                parent: true
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
            showRemoveBtn: showRemoveBtn,
            showRenameBtn: showRenameBtn,
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
            onRename: zTreeOnRename,//捕获节点编辑名称结束之后的事件回调
            onRemove: onNodeRemove,
            onClick: onNodeClick
        }
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
            childNodes[i].EVENT_ACTION = childNodes[i].EVENT_ACTION;
            childNodes[i].EVENT_JSON = childNodes[i].EVENT_JSON;
            childNodes[i].isParent=false;
        }
        return childNodes;
    };

    function zTreeOnRename(event, treeId, treeNode, isCancel){
        if(!isCancel){
            update_flag = "Y";
        }
    }
    function showRenameBtn(treeId, treeNode) {
        if(treeNode.level==1) return false;
        return !treeNode.isParent;
    }

    function showRemoveBtn(treeId, treeNode) {
        if(treeNode.level==1) return false;
        return !treeNode.isParent;
    }
    
    var deleteList=[];
    function onNodeRemove(event, treeId, treeNode) {
        update_flag = "Y";
        deleteList.push(treeNode.id);
    }

    function onNodeClick(event, treeId, treeNode){
        if (treeNode.level==0 || treeNode.level==1) return;

        currentNode = treeNode;
        $('#app_edit_event_name').val(currentNode.name);

        $('#app_edit_event_action').val(currentNode.EVENT_ACTION);
        $('#app_edit_event_action_json').val(currentNode.EVENT_JSON);
        $('#app_editEventConfirmBtn').removeClass("disabled");
    }

    function removeHoverDom(treeId, treeNode) {
        $("#addBtn_"+treeNode.tId).unbind().remove();
    };

    var newCount=1;
    function addHoverDom(treeId, treeNode) {
        var sObj = $("#" + treeNode.tId + "_span");
        if(treeNode.level==0) return;
        //如果是叶子则不能在其下级添加节点
        if (!treeNode.isParent|| $("#addBtn_"+treeNode.tId).length>0) return;

        var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
            + "' title='添加' onfocus='this.blur();'></span>";
        sObj.after(addStr);
        var btn = $("#addBtn_"+treeNode.tId);
        if (btn) btn.bind("click", function(){
            update_flag = "Y";
            //创建节点
            var nodeName = "按钮" + (newCount++);
            //id:data.ID, id: data.ID, 
            var newNodes = zTreeObj.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, EVENT_ACTION:'click'});
            currentNode=newNodes[0];
            return false;
        });
    };

    // var zNodes = [
    //     { name:"编辑页按钮", isParent:true, open: true, children: []}
    // ];

    // zTreeObj = $.fn.zTree.init($("#app_btn_tree"), app_event_setting, zNodes);

    var buildDto=function(){
        var treeObj=$.fn.zTree.getZTreeObj('app_event_tree');
        return treeObj.getNodes();
    }
    var isAppEventUpdated = function(){
        return update_flag;
    }

    var displayAppEventTree=function(btn_list){

        var list_btns = [], edit_btns = [];
        $.post('/module/searchFormBtns', {form_id: $('#form_id').val(), type:"'app_btn_list','app_btn_edit'"}, 
            function(data){
                console.log(data);
                if(data && data.TOOL_BAR_BTNS){
                    var tool_bar_btns = data.TOOL_BAR_BTNS;
                    for (var i=0; i<tool_bar_btns.length; i++) {
                        var btn = tool_bar_btns[i];
                        var node={id:btn.ID, btn_id: btn.ID, formId: btn.FORM_ID, name:btn.NAME, isParent:true, open: true};
                        if(btn.TYPE=='app_btn_list'){
                            list_btns.push(node);
                        }else{
                            edit_btns.push(node);
                        }
                        
                    }
                    var zNodes = [
                        { name:"列表页按钮", isParent:true, open: true, children: list_btns},
                        { name:"编辑页按钮", isParent:true, open: true, children: edit_btns}
                    ];

                    zTreeObj = $.fn.zTree.init($("#app_event_tree"), app_event_setting, zNodes);
                  //zTreeObj.expandAll(true);
                }
            },
        'json');
    }

    $('#app_editEventConfirmBtn').click(function(event) {
        update_flag = "Y";
        var edit_event_action=$('#app_edit_event_action').val();
        var edit_event_action_json= $('#app_edit_event_action_json').val();
        currentNode.name = $('#app_edit_event_name').val();
        currentNode.EVENT_ACTION = edit_event_action;
        currentNode.EVENT_JSON=edit_event_action_json;
        
        zTreeObj.updateNode(currentNode);
        $('#app_edit_event_name').val('');
        $('#app_edit_event_action_json').val('');

        $('#app_editEventConfirmBtn').addClass("disabled");
    });

    return{
        buildDto: buildDto,
        isAppEventUpdated:isAppEventUpdated,
        displayAppEventTree: displayAppEventTree
    }
});